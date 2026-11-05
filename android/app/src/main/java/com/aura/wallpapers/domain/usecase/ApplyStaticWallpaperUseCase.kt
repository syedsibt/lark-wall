package com.aura.wallpapers.domain.usecase

import android.content.Context
import com.aura.wallpapers.domain.model.ApplyTarget
import com.aura.wallpapers.domain.repository.WallpaperRepository
import com.aura.wallpapers.util.WallpaperUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ApplyStaticWallpaperUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wallpaperRepository: WallpaperRepository
) {
    suspend operator fun invoke(
        wallpaperId: String,
        originalUrl: String,
        target: ApplyTarget
    ): Result<Unit> {
        val result = WallpaperUtils.applyStaticWallpaper(context, originalUrl, target)
        if (result.isSuccess) {
            wallpaperRepository.incrementDownload(wallpaperId)
        }
        return result
    }
}
