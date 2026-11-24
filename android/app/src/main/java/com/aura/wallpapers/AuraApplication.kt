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
import com.aura.wallpapers.util.InteractiveThumbnailFetcher
import com.aura.wallpapers.util.InteractiveThumbnailKeyer
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
            .components {
                add(coil.map.Mapper<String, String> { data, _ ->
                    if (data.startsWith("asset:///")) {
                        "file:///android_asset/" + data.removePrefix("asset:///")
                    } else {
                        data
                    }
                })
                add(coil.map.Mapper<android.net.Uri, android.net.Uri> { data, _ ->
                    if (data.scheme == "asset") {
                        android.net.Uri.parse("file:///android_asset/" + (data.path ?: "").trimStart('/'))
                    } else {
                        data
                    }
                })
                // Thumbnails of interactive wallpapers are rendered by the wallpaper engine itself
                add(InteractiveThumbnailKeyer())
                add(InteractiveThumbnailFetcher.Factory())
            }
            .allowRgb565(false) // Never degrade to 16-bit compressed colors
            .allowHardware(true) // High-performance GPU hardware bitmap rendering (zero heap GC pauses)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 25% of RAM for ultra-fluid wallpaper memory caching
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, "image_cache"))
                    .maxSizeBytes(500L * 1024L * 1024L) // 500 MB high-speed disk cache
                    .build()
            }
            .crossfade(150)
            .respectCacheHeaders(true)
            .build()
    }
}
