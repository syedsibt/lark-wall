package com.aura.wallpapers.domain.usecase

import android.content.Context
import android.content.Intent
import com.aura.wallpapers.domain.model.LiveWallpaper
import com.aura.wallpapers.domain.repository.DownloadRepository
import com.aura.wallpapers.domain.repository.LiveWallpaperRepository
import com.aura.wallpapers.domain.repository.SettingsRepository
import com.aura.wallpapers.engine.AuraLiveWallpaperService
import com.aura.wallpapers.engine.LiveWallpaperManager
import com.aura.wallpapers.engine.interactive.InteractiveWallpaperConfig
import com.aura.wallpapers.util.Constants
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
            val prefs = context.getSharedPreferences("aura_engine_prefs", Context.MODE_PRIVATE)

            // Branch 1: Interactive Procedural Live Wallpaper
            if (InteractiveWallpaperConfig.isInteractiveUri(liveWallpaper.originalVideoUrl) || liveWallpaper.codec == "engine/interactive") {
                val presetId = InteractiveWallpaperConfig.extractPresetId(liveWallpaper.originalVideoUrl)

                prefs.edit()
                    .putString(Constants.PREF_KEY_ACTIVE_LIVE_MODE, "interactive")
                    .putString(Constants.PREF_KEY_ACTIVE_LIVE_PRESET_ID, presetId)
                    .putString(Constants.PREF_KEY_ACTIVE_LIVE_WALLPAPER_TITLE, liveWallpaper.title)
                    .apply()

                settingsRepository.setActiveLiveWallpaper("engine://interactive/$presetId", liveWallpaper.title)

                val updateIntent = Intent(AuraLiveWallpaperService.ACTION_UPDATE_LIVE_WALLPAPER).apply {
                    putExtra("mode", "interactive")
                    putExtra("preset_id", presetId)
                    putExtra("video_title", liveWallpaper.title)
                    setPackage(context.packageName)
                }
                context.sendBroadcast(updateIntent)

                LiveWallpaperManager.openLiveWallpaperPreview(context).getOrThrow()
                liveWallpaperRepository.incrementDownload(liveWallpaper.id)
                return@runCatching
            }

            // Branch 2: Video Live Wallpaper
            prefs.edit()
                .putString(Constants.PREF_KEY_ACTIVE_LIVE_MODE, "video")
                .apply()

            val liveDir = File(context.filesDir, "active_live_wallpaper").apply { mkdirs() }
            val safeId = liveWallpaper.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val targetFile = File(liveDir, "live_${safeId}.mp4")

            val rawUrl = liveWallpaper.originalVideoUrl

            val expectedLength = if (rawUrl.startsWith("asset:///")) {
                val assetPath = rawUrl.removePrefix("asset:///")
                runCatching { context.assets.openFd(assetPath).use { it.length } }.getOrDefault(-1L)
            } else -1L

            // Extract or copy to a guaranteed local storage file on device if not already cached or if size mismatched
            if (!targetFile.exists() || targetFile.length() < 1024L || (expectedLength > 0L && targetFile.length() != expectedLength)) {
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

            // Persist active live wallpaper path in settings and shared preferences
            settingsRepository.setActiveLiveWallpaper(savedPath, liveWallpaper.title)

            // Broadcast notification to hot-reload AuraLiveWallpaperService if already active on screen
            val updateIntent = Intent(AuraLiveWallpaperService.ACTION_UPDATE_LIVE_WALLPAPER).apply {
                putExtra("mode", "video")
                putExtra("video_path", savedPath)
                putExtra("video_title", liveWallpaper.title)
                setPackage(context.packageName)
            }
            context.sendBroadcast(updateIntent)

            // Launch Android native Live Wallpaper system preview chooser
            LiveWallpaperManager.openLiveWallpaperPreview(context).getOrThrow()

            // Increment atomic download count
            liveWallpaperRepository.incrementDownload(liveWallpaper.id)
        }
    }
}
