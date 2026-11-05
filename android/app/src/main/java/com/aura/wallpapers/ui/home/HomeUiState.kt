package com.aura.wallpapers.ui.home

import com.aura.wallpapers.domain.model.Category
import com.aura.wallpapers.domain.model.LiveWallpaper
import com.aura.wallpapers.domain.model.Wallpaper

data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredWallpapers: List<Wallpaper> = emptyList(),
    val trendingWallpapers: List<Wallpaper> = emptyList(),
    val popularLiveWallpapers: List<LiveWallpaper> = emptyList(),
    val recentlyAddedWallpapers: List<Wallpaper> = emptyList(),
    val categories: List<Category> = emptyList(),
    val dailyWallpaper: Wallpaper? = null,
    val selectedCategoryId: String? = null,
    val error: String? = null
)
