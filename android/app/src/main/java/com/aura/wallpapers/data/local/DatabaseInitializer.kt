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
            val hasLoadedUltraHD = prefs.getBoolean("catalog_ultra_hd_10x_v4", false)
            val catCount = categoryDao.getCategoryCount()
            val wallCount = wallpaperDao.getWallpaperCount()
            val liveCount = wallpaperDao.getLiveWallpaperCount()

            val shouldReload = !hasLoadedUltraHD || catCount == 0 || wallCount == 0 || liveCount == 0

            if (shouldReload) {
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
                            wallpaperDao.insertWallpapers(wallpapers)
                        }
                    }
                }

                runCatching {
                    context.assets.open("initial_live_wallpapers.json").use { stream ->
                        val type = object : TypeToken<List<LiveWallpaperEntity>>() {}.type
                        val liveWallpapers: List<LiveWallpaperEntity> = gson.fromJson(InputStreamReader(stream), type)
                        if (!liveWallpapers.isNullOrEmpty()) {
                            wallpaperDao.insertLiveWallpapers(liveWallpapers)
                        }
                    }
                }

                prefs.edit().putBoolean("catalog_ultra_hd_10x_v4", true).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
