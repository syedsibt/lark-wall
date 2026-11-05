package com.aura.wallpapers.domain.usecase

import com.aura.wallpapers.domain.model.Wallpaper
import com.aura.wallpapers.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWallpapersUseCase @Inject constructor(
    private val wallpaperRepository: WallpaperRepository
) {
    operator fun invoke(sortBy: SortOption = SortOption.POPULAR, categoryId: String? = null, page: Int = 0): Flow<List<Wallpaper>> {
        return when {
            categoryId != null -> wallpaperRepository.getWallpapersByCategory(categoryId, page)
            sortBy == SortOption.POPULAR -> wallpaperRepository.getPopularWallpapers(page)
            sortBy == SortOption.NEWEST -> wallpaperRepository.getNewestWallpapers(page)
            sortBy == SortOption.FEATURED -> wallpaperRepository.getFeaturedWallpapers()
            else -> wallpaperRepository.getPopularWallpapers(page)
        }
    }
}

enum class SortOption {
    POPULAR,
    NEWEST,
    FEATURED,
    MOST_DOWNLOADED
}
