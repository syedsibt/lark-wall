package com.aura.wallpapers.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.wallpapers.domain.model.ThemeMode
import com.aura.wallpapers.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isWifiOnly: Boolean = false,
    val isBatterySaverEnabled: Boolean = true,
    val activeLiveWallpaperTitle: String? = null,
    val cacheSizeBytes: Long = 0L,
    val downloadsSizeBytes: Long = 0L,
    val userEmail: String? = null,
    val userName: String? = null,
    val isSigningIn: Boolean = false,
    val message: String? = null
) {
    val formattedCacheSize: String
        get() = formatBytes(cacheSizeBytes)

    val formattedDownloadsSize: String
        get() = formatBytes(downloadsSizeBytes)

    private fun formatBytes(bytes: Long): String = when {
        bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        refreshStorageSizes()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.themeMode,
                settingsRepository.isWifiOnly,
                settingsRepository.isBatterySaverEnabled,
                settingsRepository.activeLiveWallpaperTitle
            ) { theme, wifiOnly, batterySaver, liveTitle ->
                _uiState.update {
                    it.copy(
                        themeMode = theme,
                        isWifiOnly = wifiOnly,
                        isBatterySaverEnabled = batterySaver,
                        activeLiveWallpaperTitle = liveTitle
                    )
                }
            }.collect()
        }
        viewModelScope.launch {
            combine(
                settingsRepository.userEmail,
                settingsRepository.userName
            ) { email, name ->
                _uiState.update {
                    it.copy(
                        userEmail = email,
                        userName = name
                    )
                }
            }.collect()
        }
    }

    fun signInWithGoogle(email: String = "syedsibt@gmail.com", name: String = "Syed Sibt") {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningIn = true) }
            kotlinx.coroutines.delay(500)
            settingsRepository.setGoogleUser(email, name)
            _uiState.update {
                it.copy(
                    isSigningIn = false,
                    message = "Signed in with Google as $email"
                )
            }
        }
    }

    fun signOutGoogle() {
        viewModelScope.launch {
            settingsRepository.clearGoogleUser()
            _uiState.update { it.copy(message = "Signed out from Google account") }
        }
    }

    fun refreshStorageSizes() {
        viewModelScope.launch {
            val cache = settingsRepository.calculateCacheSizeBytes()
            val downloads = settingsRepository.calculateDownloadsSizeBytes()
            _uiState.update { it.copy(cacheSizeBytes = cache, downloadsSizeBytes = downloads) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWifiOnly(enabled)
        }
    }

    fun setBatterySaver(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBatterySaverEnabled(enabled)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            settingsRepository.clearCache()
            refreshStorageSizes()
            _uiState.update { it.copy(message = "Cache cleared successfully") }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
