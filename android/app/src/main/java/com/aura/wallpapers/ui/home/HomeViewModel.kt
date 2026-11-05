package com.aura.wallpapers.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.wallpapers.domain.model.Wallpaper
import com.aura.wallpapers.domain.usecase.GetCategoriesUseCase
import com.aura.wallpapers.domain.usecase.GetLiveWallpapersUseCase
import com.aura.wallpapers.domain.usecase.GetWallpapersUseCase
import com.aura.wallpapers.domain.usecase.SortOption
import com.aura.wallpapers.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getWallpapersUseCase: GetWallpapersUseCase,
    private val getLiveWallpapersUseCase: GetLiveWallpapersUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Trigger background refresh of categories if needed
            runCatching { getCategoriesUseCase.refresh() }

            combine(
                getWallpapersUseCase(SortOption.FEATURED),
                getWallpapersUseCase(SortOption.POPULAR),
                getLiveWallpapersUseCase(),
                getWallpapersUseCase(SortOption.NEWEST),
                getCategoriesUseCase()
            ) { featured, trending, live, newest, categories ->
                HomeUiState(
                    isLoading = false,
                    featuredWallpapers = featured,
                    trendingWallpapers = trending.take(10),
                    popularLiveWallpapers = live.take(8),
                    recentlyAddedWallpapers = newest.take(10),
                    categories = categories,
                    dailyWallpaper = featured.firstOrNull() ?: trending.firstOrNull()
                )
            }.catch { error ->
                _uiState.update { it.copy(isLoading = false, error = error.localizedMessage ?: "Failed to load catalog") }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onCategorySelected(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
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

    fun toggleLiveFavorite(assetId: String, currentlyFavorite: Boolean, title: String, thumb: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(
                assetId = assetId,
                currentlyFavorite = currentlyFavorite,
                isLive = true,
                title = title,
                thumbnailUrl = thumb
            )
        }
    }
}
