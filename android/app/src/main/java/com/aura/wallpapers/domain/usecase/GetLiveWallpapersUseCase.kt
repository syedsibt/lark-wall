package com.aura.wallpapers.domain.usecase

import com.aura.wallpapers.domain.model.LiveWallpaper
import com.aura.wallpapers.domain.repository.LiveWallpaperRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLiveWallpapersUseCase @Inject constructor(
    private val liveWallpaperRepository: LiveWallpaperRepository
) {
    operator fun invoke(categoryId: String? = null, page: Int = 0): Flow<List<LiveWallpaper>> {
        return if (categoryId != null) {
            liveWallpaperRepository.getLiveWallpapersByCategory(categoryId, page)
        } else {
            liveWallpaperRepository.getPopularLiveWallpapers(page)
        }
    }
}
