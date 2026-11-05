package com.aura.wallpapers.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.wallpapers.data.local.entity.FavoriteEntity
import com.aura.wallpapers.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val favorites: List<FavoriteEntity> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            favoritesRepository.getAllFavorites()
                .catch { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) }
                }
                .collect { list ->
                    _uiState.update { it.copy(isLoading = false, favorites = list) }
                }
        }
    }

    fun removeFavorite(assetId: String) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(assetId)
        }
    }
}
