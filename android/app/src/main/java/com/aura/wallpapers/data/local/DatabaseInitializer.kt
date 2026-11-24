package com.aura.wallpapers.data.local

import android.content.Context
import android.util.Log
import com.aura.wallpapers.data.local.dao.CategoryDao
import com.aura.wallpapers.data.local.dao.DownloadDao
import com.aura.wallpapers.data.local.dao.FavoriteDao
import com.aura.wallpapers.data.local.dao.WallpaperDao
import com.aura.wallpapers.data.local.entity.CategoryEntity
import com.aura.wallpapers.data.local.entity.LiveWallpaperEntity
import com.aura.wallpapers.data.local.entity.WallpaperEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val categoryDao: CategoryDao,
    private val wallpaperDao: WallpaperDao,
    private val favoriteDao: FavoriteDao,
    private val downloadDao: DownloadDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val gson: Gson
) {
    private val initMutex = Mutex()

    suspend fun populateIfNeeded() = withContext(ioDispatcher) {
        initMutex.withLock {
            try {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val loadedVersion = prefs.getInt(KEY_CATALOG_VERSION, 0)
                val wpCount = wallpaperDao.getWallpaperCount()
                val liveCount = wallpaperDao.getLiveWallpaperCount()
                if (loadedVersion >= CATALOG_VERSION && wpCount > 0 && liveCount > 0) {
                    // Already populated, skip disk I/O entirely for instant startup
                    return@withContext
                }

                // Snapshot the previous catalog so favourites of removed duplicates can be carried over
                val oldStatic = runCatching { wallpaperDao.getAllWallpapersList() }.getOrDefault(emptyList())
                val oldLive = runCatching { wallpaperDao.getAllLiveWallpapersList() }.getOrDefault(emptyList())

                val categories: List<CategoryEntity> =
                    readAsset("initial_categories.json", object : TypeToken<List<CategoryEntity>>() {}.type)
                val wallpapers: List<WallpaperEntity> =
                    readAsset("initial_wallpapers.json", object : TypeToken<List<WallpaperEntity>>() {}.type)
                val liveWallpapers: List<LiveWallpaperEntity> =
                    readAsset("initial_live_wallpapers.json", object : TypeToken<List<LiveWallpaperEntity>>() {}.type)

                if (categories.isNotEmpty()) categoryDao.insertCategories(categories)

                // Replace (not merge) both catalogs. Merging with REPLACE left every
                // previously shipped duplicate in the database forever.
                if (wallpapers.isNotEmpty()) {
                    wallpaperDao.clearWallpapers()
                    wallpaperDao.insertWallpapers(wallpapers)
                }
                if (liveWallpapers.isNotEmpty()) {
                    wallpaperDao.clearLiveWallpapers()
                    wallpaperDao.insertLiveWallpapers(liveWallpapers)
                }

                if (wallpapers.isNotEmpty() && liveWallpapers.isNotEmpty()) {
                    migrateUserData(oldStatic, oldLive, wallpapers, liveWallpapers)
                    prefs.edit().putInt(KEY_CATALOG_VERSION, CATALOG_VERSION).commit()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Catalog population failed", e)
            }
        }
    }

    private fun <T> readAsset(name: String, type: Type): List<T> = runCatching {
        context.assets.open(name).use { stream ->
            BufferedReader(InputStreamReader(stream), 32768).use { reader ->
                gson.fromJson<List<T>>(reader, type) ?: emptyList()
            }
        }
    }.onFailure { Log.e(TAG, "Failed to read $name", it) }.getOrDefault(emptyList())

    /**
     * Favourites point at catalog ids. When a duplicate was removed, move the favourite to the
     * wallpaper that kept the same design/image, and refresh thumbnails that changed.
     */
    private suspend fun migrateUserData(
        oldStatic: List<WallpaperEntity>,
        oldLive: List<LiveWallpaperEntity>,
        newStatic: List<WallpaperEntity>,
        newLive: List<LiveWallpaperEntity>
    ) {
        val newStaticById = newStatic.associateBy { it.id }
        val newLiveById = newLive.associateBy { it.id }
        val newStaticByKey = HashMap<String, WallpaperEntity>()
        newStatic.forEach { newStaticByKey.putIfAbsent(staticKey(it.originalUrl), it) }
        val newLiveByKey = HashMap<String, LiveWallpaperEntity>()
        newLive.forEach { newLiveByKey.putIfAbsent(liveKey(it.previewVideoUrl), it) }
        val oldStaticKeyById = oldStatic.associate { it.id to staticKey(it.originalUrl) }
        val oldLiveKeyById = oldLive.associate { it.id to liveKey(it.previewVideoUrl) }

        val favorites = runCatching { favoriteDao.getAllFavoritesList() }.getOrDefault(emptyList())
        val kept = HashSet<String>()
        favorites.forEach { fav ->
            if (fav.isLive) {
                val target = newLiveById[fav.assetId] ?: oldLiveKeyById[fav.assetId]?.let { newLiveByKey[it] }
                if (target == null) {
                    favoriteDao.deleteFavorite(fav.assetId)
                } else if (target.id != fav.assetId || target.thumbnailUrl != fav.thumbnailUrl) {
                    favoriteDao.deleteFavorite(fav.assetId)
                    if (kept.add(target.id)) {
                        favoriteDao.insertFavorite(fav.copy(assetId = target.id, title = target.title, thumbnailUrl = target.thumbnailUrl))
                    }
                } else {
                    kept.add(fav.assetId)
                }
            } else {
                val target = newStaticById[fav.assetId] ?: oldStaticKeyById[fav.assetId]?.let { newStaticByKey[it] }
                if (target == null) {
                    favoriteDao.deleteFavorite(fav.assetId)
                } else if (target.id != fav.assetId) {
                    favoriteDao.deleteFavorite(fav.assetId)
                    if (kept.add(target.id)) {
                        favoriteDao.insertFavorite(fav.copy(assetId = target.id, title = target.title, thumbnailUrl = target.thumbnailUrl))
                    }
                } else {
                    kept.add(fav.assetId)
                }
            }
        }

        // Live thumbnails moved from bundled images to engine-rendered previews
        newLive.forEach { live ->
            runCatching { downloadDao.updateThumbnailForAsset(live.id, live.thumbnailUrl) }
        }
    }

    private fun staticKey(url: String): String = url.substringBefore('?')

    private fun liveKey(previewUrl: String): String {
        val id = previewUrl.removePrefix("engine://interactive/")
        return if (id.startsWith("ml_") || id.startsWith("sig_") || id.contains("/")) id
        else id.replace(Regex("_\\d+$"), "")
    }

    companion object {
        private const val TAG = "DatabaseInitializer"
        private const val PREFS_NAME = "aura_catalog_prefs"
        private const val KEY_CATALOG_VERSION = "catalog_version"

        /** Bump whenever the bundled JSON catalog changes so existing installs re-seed. */
        const val CATALOG_VERSION = 13
    }
}
