package com.aura.wallpapers.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.wallpapers.domain.model.DownloadItem
import com.aura.wallpapers.domain.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadsUiState(
    val isLoading: Boolean = true,
    val downloads: List<DownloadItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        loadDownloads()
    }

    private fun loadDownloads() {
        viewModelScope.launch {
            downloadRepository.getAllDownloads()
                .catch { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) }
                }
                .collect { list ->
                    _uiState.update { it.copy(isLoading = false, downloads = list) }
                }
        }
    }

    fun deleteDownload(id: String) {
        viewModelScope.launch {
            downloadRepository.deleteDownload(id)
        }
    }
}
