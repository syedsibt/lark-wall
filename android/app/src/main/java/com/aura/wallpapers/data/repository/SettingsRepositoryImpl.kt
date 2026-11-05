package com.aura.wallpapers.data.repository

import android.content.Context
import android.os.Environment
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.aura.wallpapers.data.local.preferences.UserPreferencesManager
import com.aura.wallpapers.domain.model.ThemeMode
import com.aura.wallpapers.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: UserPreferencesManager,
    private val ioDispatcher: CoroutineDispatcher
) : SettingsRepository {

    override val themeMode: Flow<ThemeMode> = preferencesManager.themeMode
    override val isWifiOnly: Flow<Boolean> = preferencesManager.isWifiOnly
    override val isBatterySaverEnabled: Flow<Boolean> = preferencesManager.isBatterySaverEnabled
    override val activeLiveWallpaperTitle: Flow<String?> = preferencesManager.activeLiveWallpaperTitle

    override suspend fun setThemeMode(mode: ThemeMode) {
        preferencesManager.setThemeMode(mode)
    }

    override suspend fun setWifiOnly(enabled: Boolean) {
        preferencesManager.setWifiOnly(enabled)
    }

    override suspend fun setBatterySaverEnabled(enabled: Boolean) {
        preferencesManager.setBatterySaverEnabled(enabled)
    }

    override suspend fun setActiveLiveWallpaper(path: String, title: String) {
        preferencesManager.setActiveLiveWallpaper(path, title)
    }

    override suspend fun calculateCacheSizeBytes(): Long = withContext(ioDispatcher) {
        val cacheDir = context.cacheDir
        calculateDirSize(cacheDir)
    }

    @OptIn(ExperimentalCoilApi::class)
    override suspend fun clearCache() = withContext(ioDispatcher) {
        context.imageLoader.diskCache?.clear()
        context.imageLoader.memoryCache?.clear()
        val cacheDir = context.cacheDir
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
        Unit
    }

    override suspend fun calculateDownloadsSizeBytes(): Long = withContext(ioDispatcher) {
        var size = 0L
        val pics = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val movies = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        if (pics != null) size += calculateDirSize(pics)
        if (movies != null) size += calculateDirSize(movies)
        size
    }

    private fun calculateDirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var total = 0L
        dir.listFiles()?.forEach { file ->
            total += if (file.isDirectory) calculateDirSize(file) else file.length()
        }
        return total
    }
}
