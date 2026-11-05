package com.aura.wallpapers.domain.repository

import com.aura.wallpapers.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val isWifiOnly: Flow<Boolean>
    val isBatterySaverEnabled: Flow<Boolean>
    val activeLiveWallpaperTitle: Flow<String?>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setWifiOnly(enabled: Boolean)
    suspend fun setBatterySaverEnabled(enabled: Boolean)
    suspend fun setActiveLiveWallpaper(path: String, title: String)
    suspend fun calculateCacheSizeBytes(): Long
    suspend fun clearCache()
    suspend fun calculateDownloadsSizeBytes(): Long
}
