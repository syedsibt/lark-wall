package com.aura.wallpapers.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.wallpapers.domain.model.ApplyTarget
import com.aura.wallpapers.domain.repository.DownloadRepository
import com.aura.wallpapers.domain.repository.FavoritesRepository
import com.aura.wallpapers.domain.repository.LiveWallpaperRepository
import com.aura.wallpapers.domain.repository.WallpaperRepository
import com.aura.wallpapers.domain.usecase.ApplyStaticWallpaperUseCase
import com.aura.wallpapers.domain.usecase.PrepareLiveWallpaperUseCase
import com.aura.wallpapers.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wallpaperRepository: WallpaperRepository,
    private val liveWallpaperRepository: LiveWallpaperRepository,
    private val favoritesRepository: FavoritesRepository,
    private val downloadRepository: DownloadRepository,
    private val applyStaticWallpaperUseCase: ApplyStaticWallpaperUseCase,
    private val prepareLiveWallpaperUseCase: PrepareLiveWallpaperUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val assetId: String = checkNotNull(savedStateHandle["assetId"])
    private val isLive: Boolean = savedStateHandle.get<Boolean>("isLive") ?: false

    private val _uiState = MutableStateFlow(WallpaperDetailUiState(isLive = isLive))
    val uiState: StateFlow<WallpaperDetailUiState> = _uiState.asStateFlow()

    init {
        loadAsset()
        observeFavoriteStatus()
    }

    private fun loadAsset() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            if (isLive) {
                val live = liveWallpaperRepository.getLiveWallpaperById(assetId)
                if (live != null) {
                    _uiState.update { it.copy(isLoading = false, liveWallpaper = live) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Live wallpaper not found") }
                }
            } else {
                val static = wallpaperRepository.getWallpaperById(assetId)
                if (static != null) {
                    _uiState.update { it.copy(isLoading = false, wallpaper = static) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Wallpaper not found") }
                }
            }
        }
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            favoritesRepository.isFavorite(assetId).collect { fav ->
                _uiState.update { it.copy(isFavorite = fav) }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val title = _uiState.value.wallpaper?.title ?: _uiState.value.liveWallpaper?.title ?: "Wallpaper"
            val thumb = _uiState.value.wallpaper?.thumbnailUrl ?: _uiState.value.liveWallpaper?.thumbnailUrl ?: ""
            toggleFavoriteUseCase(
                assetId = assetId,
                currentlyFavorite = _uiState.value.isFavorite,
                isLive = isLive,
                title = title,
                thumbnailUrl = thumb
            )
        }
    }

    fun openApplySheet() {
        _uiState.update { it.copy(showApplySheet = true) }
    }

    fun dismissApplySheet() {
        _uiState.update { it.copy(showApplySheet = false) }
    }

    fun applyStaticWallpaper(target: ApplyTarget) {
        val wp = _uiState.value.wallpaper ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isApplying = true) }
            val result = applyStaticWallpaperUseCase(wp.id, wp.originalUrl, target)
            _uiState.update {
                it.copy(
                    isApplying = false,
                    showApplySheet = false,
                    message = if (result.isSuccess) "Wallpaper applied successfully" else "Failed to apply wallpaper"
                )
            }
        }
    }

    fun applyLiveWallpaper() {
        val live = _uiState.value.liveWallpaper ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isApplying = true) }
            val result = prepareLiveWallpaperUseCase(live)
            _uiState.update {
                it.copy(
                    isApplying = false,
                    message = if (result.isSuccess) "Opening Live Wallpaper settings..." else "Failed to launch Live Wallpaper engine"
                )
            }
        }
    }

    fun downloadAsset() {
        viewModelScope.launch {
            val title: String
            val url: String
            val thumb: String
            val size: Long

            if (isLive) {
                val live = _uiState.value.liveWallpaper ?: return@launch
                title = live.title
                url = live.originalVideoUrl
                thumb = live.thumbnailUrl
                size = live.fileSizeBytes
            } else {
                val wp = _uiState.value.wallpaper ?: return@launch
                title = wp.title
                url = wp.originalUrl
                thumb = wp.thumbnailUrl
                size = wp.fileSizeBytes
            }

            downloadRepository.enqueueDownload(
                assetId = assetId,
                title = title,
                isLive = isLive,
                downloadUrl = url,
                thumbnailUrl = thumb,
                fileSizeBytes = size
            )
            _uiState.update { it.copy(message = "Download queued in background") }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
