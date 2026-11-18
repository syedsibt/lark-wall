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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedReader
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
    private val initMutex = Mutex()

    suspend fun populateIfNeeded() = withContext(ioDispatcher) {
        initMutex.withLock {
            try {
                val prefs = context.getSharedPreferences("aura_catalog_prefs", Context.MODE_PRIVATE)
                val hasLoadedInteractiveV9 = prefs.getBoolean("catalog_interactive_live_v9", false)
                if (hasLoadedInteractiveV9) {
                    // Already populated, skip disk I/O entirely for instant startup
                    return@withContext
                }

                runCatching {
                    context.assets.open("initial_categories.json").use { stream ->
                        BufferedReader(InputStreamReader(stream), 32768).use { reader ->
                            val type = object : TypeToken<List<CategoryEntity>>() {}.type
                            val categories: List<CategoryEntity> = gson.fromJson(reader, type)
                            if (!categories.isNullOrEmpty()) {
                                categoryDao.insertCategories(categories)
                            }
                        }
                    }
                }

                runCatching {
                    context.assets.open("initial_wallpapers.json").use { stream ->
                        BufferedReader(InputStreamReader(stream), 32768).use { reader ->
                            val type = object : TypeToken<List<WallpaperEntity>>() {}.type
                            val wallpapers: List<WallpaperEntity> = gson.fromJson(reader, type)
                            if (!wallpapers.isNullOrEmpty()) {
                                wallpaperDao.insertWallpapers(wallpapers)
                            }
                        }
                    }
                }

                runCatching {
                    context.assets.open("initial_live_wallpapers.json").use { stream ->
                        BufferedReader(InputStreamReader(stream), 32768).use { reader ->
                            val type = object : TypeToken<List<LiveWallpaperEntity>>() {}.type
                            val liveWallpapers: List<LiveWallpaperEntity> = gson.fromJson(reader, type)
                            if (!liveWallpapers.isNullOrEmpty()) {
                                wallpaperDao.insertLiveWallpapers(liveWallpapers)
                            }
                        }
                    }
                }

                prefs.edit().putBoolean("catalog_interactive_live_v9", true).commit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
