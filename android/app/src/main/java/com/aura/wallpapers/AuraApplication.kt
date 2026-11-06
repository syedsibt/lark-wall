package com.aura.wallpapers

import android.app.Application
import android.graphics.Bitmap
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class AuraApplication : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var okHttpClient: OkHttpClient

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .bitmapConfig(Bitmap.Config.ARGB_8888) // 32-bit True Color (10X Image Fidelity, Zero Banding)
            .allowRgb565(false) // Never degrade to 16-bit compressed colors
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.30) // 30% of RAM for ultra-fluid 4K wallpaper caching
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, "image_cache"))
                    .maxSizeBytes(500L * 1024L * 1024L) // 500 MB high-speed disk cache
                    .build()
            }
            .crossfade(true)
            .respectCacheHeaders(true)
            .allowHardware(true) // High-performance GPU hardware bitmap rendering
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}
