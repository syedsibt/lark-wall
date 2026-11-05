package com.aura.wallpapers.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aura.wallpapers.domain.model.ThemeMode
import com.aura.wallpapers.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.PREFERENCES_NAME)

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyThemeMode = stringPreferencesKey(Constants.PREF_KEY_THEME_MODE)
    private val keyWifiOnly = booleanPreferencesKey(Constants.PREF_KEY_WIFI_ONLY)
    private val keyBatterySaver = booleanPreferencesKey(Constants.PREF_KEY_BATTERY_SAVER_ENABLED)
    private val keyActiveLivePath = stringPreferencesKey(Constants.PREF_KEY_ACTIVE_LIVE_WALLPAPER_PATH)
    private val keyActiveLiveTitle = stringPreferencesKey(Constants.PREF_KEY_ACTIVE_LIVE_WALLPAPER_TITLE)

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[keyThemeMode] ?: ThemeMode.DARK.name
        runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.DARK)
    }

    val isWifiOnly: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[keyWifiOnly] ?: false
    }

    val isBatterySaverEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[keyBatterySaver] ?: true
    }

    val activeLiveWallpaperPath: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[keyActiveLivePath]
    }

    val activeLiveWallpaperTitle: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[keyActiveLiveTitle]
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[keyThemeMode] = themeMode.name
        }
    }

    suspend fun setWifiOnly(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[keyWifiOnly] = enabled
        }
    }

    suspend fun setBatterySaverEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[keyBatterySaver] = enabled
        }
    }

    suspend fun setActiveLiveWallpaper(path: String, title: String) {
        context.dataStore.edit { prefs ->
            prefs[keyActiveLivePath] = path
            prefs[keyActiveLiveTitle] = title
        }
        // Also write to standard SharedPreferences for sync access in WallpaperService
        val legacyPrefs = context.getSharedPreferences("aura_engine_prefs", Context.MODE_PRIVATE)
        legacyPrefs.edit()
            .putString(Constants.PREF_KEY_ACTIVE_LIVE_WALLPAPER_PATH, path)
            .putString(Constants.PREF_KEY_ACTIVE_LIVE_WALLPAPER_TITLE, title)
            .apply()
    }
}
