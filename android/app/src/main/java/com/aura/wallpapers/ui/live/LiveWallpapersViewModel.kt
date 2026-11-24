package com.aura.wallpapers.ui.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.wallpapers.data.local.DatabaseInitializer
import com.aura.wallpapers.domain.model.Category
import com.aura.wallpapers.domain.model.LiveWallpaper
import com.aura.wallpapers.domain.usecase.GetCategoriesUseCase
import com.aura.wallpapers.domain.usecase.GetLiveWallpapersUseCase
import com.aura.wallpapers.domain.repository.SettingsRepository
import com.aura.wallpapers.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    val isEcoModeEnabled: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LiveWallpapersViewModel @Inject constructor(
    private val databaseInitializer: DatabaseInitializer,
    private val getLiveWallpapersUseCase: GetLiveWallpapersUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private var wallpapersJob: Job? = null

    private val _uiState = MutableStateFlow(LiveUiState())
    val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            databaseInitializer.populateIfNeeded()
            loadLiveCategories()
            loadLiveWallpapers()
        }
        viewModelScope.launch {
            settingsRepository.isBatterySaverEnabled
                .catch { }
                .collect { enabled -> _uiState.update { it.copy(isEcoModeEnabled = enabled) } }
        }
    }

    /** Eco Mode = pause the applied live wallpaper while the system Battery Saver is on. */
    fun setEcoMode(enabled: Boolean) {
        _uiState.update { it.copy(isEcoModeEnabled = enabled) }
        viewModelScope.launch { settingsRepository.setBatterySaverEnabled(enabled) }
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
        // Cancel the previous collector; otherwise every category switch stacked another
        // Room flow collector and older ones kept overwriting the list.
        wallpapersJob?.cancel()
        wallpapersJob = viewModelScope.launch {
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
