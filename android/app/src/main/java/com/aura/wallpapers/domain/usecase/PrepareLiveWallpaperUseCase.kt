package com.aura.wallpapers.domain.usecase

import android.content.Context
import android.content.Intent
import com.aura.wallpapers.domain.model.LiveWallpaper
import com.aura.wallpapers.domain.repository.DownloadRepository
import com.aura.wallpapers.domain.repository.LiveWallpaperRepository
import com.aura.wallpapers.domain.repository.SettingsRepository
import com.aura.wallpapers.engine.AuraLiveWallpaperService
import com.aura.wallpapers.engine.LiveWallpaperManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PrepareLiveWallpaperUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val liveWallpaperRepository: LiveWallpaperRepository,
    private val downloadRepository: DownloadRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(liveWallpaper: LiveWallpaper): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val liveDir = File(context.filesDir, "active_live_wallpaper").apply { mkdirs() }
            val safeId = liveWallpaper.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val targetFile = File(liveDir, "live_${safeId}.mp4")

            val rawUrl = liveWallpaper.originalVideoUrl

            // 1. Extract or copy to a guaranteed local storage file on device if not already cached
            if (!targetFile.exists() || targetFile.length() < 1024L) {
                val tempFile = File(liveDir, "live_${safeId}.tmp")
                if (rawUrl.startsWith("asset:///")) {
                    val assetPath = rawUrl.removePrefix("asset:///")
                    context.assets.open(assetPath).use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                } else if (File(rawUrl).exists()) {
                    File(rawUrl).copyTo(tempFile, overwrite = true)
                } else if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                    java.net.URL(rawUrl).openStream().use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                } else {
                    // Try as raw asset name
                    runCatching {
                        context.assets.open("live_videos/$rawUrl").use { input ->
                            FileOutputStream(tempFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }

                // Fallback safety check: If temp file is missing or empty, extract liquid chrome loop
                if (!tempFile.exists() || tempFile.length() < 1024L) {
                    context.assets.open("live_videos/liquid_chrome_loop.mp4").use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                // Atomic file swap: reader never encounters truncated in-place files
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                tempFile.renameTo(targetFile)
            }

            val savedPath = targetFile.absolutePath

            // 2. Persist active live wallpaper path in settings and shared preferences
            settingsRepository.setActiveLiveWallpaper(savedPath, liveWallpaper.title)

            // 3. Broadcast notification to hot-reload AuraLiveWallpaperService if already active on screen
            val updateIntent = Intent(AuraLiveWallpaperService.ACTION_UPDATE_LIVE_WALLPAPER).apply {
                putExtra("video_path", savedPath)
                putExtra("video_title", liveWallpaper.title)
                setPackage(context.packageName)
            }
            context.sendBroadcast(updateIntent)

            // 4. Launch Android native Live Wallpaper system preview chooser
            LiveWallpaperManager.openLiveWallpaperPreview(context).getOrThrow()

            // 5. Increment atomic download count
            liveWallpaperRepository.incrementDownload(liveWallpaper.id)
        }
    }
}
