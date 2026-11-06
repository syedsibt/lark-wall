package com.aura.wallpapers.data.local

import android.content.Context
import com.aura.wallpapers.data.local.dao.CategoryDao
import com.aura.wallpapers.data.local.dao.WallpaperDao
import com.aura.wallpapers.data.local.entity.CategoryEntity
import com.aura.wallpapers.data.local.entity.LiveWallpaperEntity
import com.aura.wallpapers.data.local.entity.WallpaperEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val categoryDao: CategoryDao,
    private val wallpaperDao: WallpaperDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val gson: Gson
) {
    suspend fun populateIfNeeded() = withContext(ioDispatcher) {
        try {
            val prefs = context.getSharedPreferences("aura_catalog_prefs", Context.MODE_PRIVATE)
            val hasLoadedUltraHD = prefs.getBoolean("catalog_ultra_hd_10x_v6", false)
            if (hasLoadedUltraHD) {
                // Already populated, skip disk I/O entirely for instant startup
                return@withContext
            }

            runCatching {
                context.assets.open("initial_categories.json").use { stream ->
                    val type = object : TypeToken<List<CategoryEntity>>() {}.type
                    val categories: List<CategoryEntity> = gson.fromJson(InputStreamReader(stream), type)
                    if (!categories.isNullOrEmpty()) {
                        categoryDao.insertCategories(categories)
                    }
                }
            }

            runCatching {
                context.assets.open("initial_wallpapers.json").use { stream ->
                    val type = object : TypeToken<List<WallpaperEntity>>() {}.type
                    val wallpapers: List<WallpaperEntity> = gson.fromJson(InputStreamReader(stream), type)
                    if (!wallpapers.isNullOrEmpty()) {
                        wallpapers.chunked(200).forEach { chunk ->
                            wallpaperDao.insertWallpapers(chunk)
                        }
                    }
                }
            }

            runCatching {
                context.assets.open("initial_live_wallpapers.json").use { stream ->
                    val type = object : TypeToken<List<LiveWallpaperEntity>>() {}.type
                    val liveWallpapers: List<LiveWallpaperEntity> = gson.fromJson(InputStreamReader(stream), type)
                    if (!liveWallpapers.isNullOrEmpty()) {
                        liveWallpapers.chunked(100).forEach { chunk ->
                            wallpaperDao.insertLiveWallpapers(chunk)
                        }
                    }
                }
            }

            prefs.edit().putBoolean("catalog_ultra_hd_10x_v6", true).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
