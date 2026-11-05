package com.aura.wallpapers.ui.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.wallpapers.data.local.DatabaseInitializer
import com.aura.wallpapers.domain.model.Category
import com.aura.wallpapers.domain.model.LiveWallpaper
import com.aura.wallpapers.domain.usecase.GetCategoriesUseCase
import com.aura.wallpapers.domain.usecase.GetLiveWallpapersUseCase
import com.aura.wallpapers.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveUiState(
    val isLoading: Boolean = true,
    val liveWallpapers: List<LiveWallpaper> = emptyList(),
    val liveCategories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val error: String? = null
)

@HiltViewModel
class LiveWallpapersViewModel @Inject constructor(
    private val databaseInitializer: DatabaseInitializer,
    private val getLiveWallpapersUseCase: GetLiveWallpapersUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveUiState())
    val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            databaseInitializer.populateIfNeeded()
            loadLiveCategories()
            loadLiveWallpapers()
        }
    }

    private fun loadLiveCategories() {
        viewModelScope.launch {
            getCategoriesUseCase(liveOnly = true)
                .catch { }
                .collect { list ->
                    _uiState.update { it.copy(liveCategories = list) }
                }
        }
    }

    fun loadLiveWallpapers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getLiveWallpapersUseCase(categoryId = _uiState.value.selectedCategoryId)
                .catch { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) }
                }
                .collect { list ->
                    _uiState.update { it.copy(isLoading = false, liveWallpapers = list) }
                }
        }
    }

    fun setCategory(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadLiveWallpapers()
    }

    fun toggleFavorite(live: LiveWallpaper) {
        viewModelScope.launch {
            toggleFavoriteUseCase(
                assetId = live.id,
                currentlyFavorite = live.isFavorite,
                isLive = true,
                title = live.title,
                thumbnailUrl = live.thumbnailUrl
            )
        }
    }
}
