package com.aura.wallpapers.domain.usecase

import android.content.Context
import com.aura.wallpapers.domain.model.LiveWallpaper
import com.aura.wallpapers.domain.repository.DownloadRepository
import com.aura.wallpapers.domain.repository.LiveWallpaperRepository
import com.aura.wallpapers.domain.repository.SettingsRepository
import com.aura.wallpapers.engine.LiveWallpaperManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class PrepareLiveWallpaperUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val liveWallpaperRepository: LiveWallpaperRepository,
    private val downloadRepository: DownloadRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(liveWallpaper: LiveWallpaper): Result<Unit> {
        return runCatching {
            // Check if already downloaded locally
            val localDownload = downloadRepository.getDownloadByAssetId(liveWallpaper.id)
            val path = if (localDownload?.localFilePath != null && File(localDownload.localFilePath).exists()) {
                localDownload.localFilePath
            } else {
                liveWallpaper.originalVideoUrl
            }

            // Save active live wallpaper path and title
            settingsRepository.setActiveLiveWallpaper(path, liveWallpaper.title)

            // Trigger system live wallpaper chooser
            LiveWallpaperManager.openLiveWallpaperPreview(context).getOrThrow()

            // Increment atomic download count
            liveWallpaperRepository.incrementDownload(liveWallpaper.id)
        }
    }
}
