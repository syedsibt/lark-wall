package com.aura.wallpapers.ui.detail

import com.aura.wallpapers.domain.model.LiveWallpaper
import com.aura.wallpapers.domain.model.Wallpaper

data class WallpaperDetailUiState(
    val isLoading: Boolean = true,
    val isLive: Boolean = false,
    val wallpaper: Wallpaper? = null,
    val liveWallpaper: LiveWallpaper? = null,
    val isFavorite: Boolean = false,
    val isDownloading: Boolean = false,
    val isApplying: Boolean = false,
    val showApplySheet: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
