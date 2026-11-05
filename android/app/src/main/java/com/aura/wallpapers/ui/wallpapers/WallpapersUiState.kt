package com.aura.wallpapers.ui.wallpapers

import com.aura.wallpapers.domain.model.Category
import com.aura.wallpapers.domain.model.Wallpaper
import com.aura.wallpapers.domain.usecase.SortOption

data class WallpapersUiState(
    val isLoading: Boolean = true,
    val wallpapers: List<Wallpaper> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val selectedSort: SortOption = SortOption.POPULAR,
    val currentPage: Int = 0,
    val error: String? = null
)
