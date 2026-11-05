package com.aura.wallpapers.ui.wallpapers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.wallpapers.domain.model.Wallpaper
import com.aura.wallpapers.domain.usecase.GetCategoriesUseCase
import com.aura.wallpapers.domain.usecase.GetWallpapersUseCase
import com.aura.wallpapers.domain.usecase.SortOption
import com.aura.wallpapers.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpapersViewModel @Inject constructor(
    private val getWallpapersUseCase: GetWallpapersUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WallpapersUiState())
    val uiState: StateFlow<WallpapersUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadWallpapers()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase(liveOnly = false)
                .catch { }
                .collect { list ->
                    _uiState.update { it.copy(categories = list) }
                }
        }
    }

    fun loadWallpapers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getWallpapersUseCase(
                sortBy = _uiState.value.selectedSort,
                categoryId = _uiState.value.selectedCategoryId,
                page = _uiState.value.currentPage
            )
            .catch { err ->
                _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) }
            }
            .collect { list ->
                _uiState.update { it.copy(isLoading = false, wallpapers = list) }
            }
        }
    }

    fun setCategory(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, currentPage = 0) }
        loadWallpapers()
    }

    fun setSort(sort: SortOption) {
        _uiState.update { it.copy(selectedSort = sort, currentPage = 0) }
        loadWallpapers()
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch {
            toggleFavoriteUseCase(
                assetId = wallpaper.id,
                currentlyFavorite = wallpaper.isFavorite,
                isLive = false,
                title = wallpaper.title,
                thumbnailUrl = wallpaper.thumbnailUrl
            )
        }
    }
}
