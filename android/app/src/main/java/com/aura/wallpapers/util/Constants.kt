package com.aura.wallpapers.util

object Constants {
    const val DATABASE_NAME = "aura_wallpapers.db"
    const val PREFERENCES_NAME = "aura_user_preferences"

    // Default Supabase PostgREST base endpoint (configurable at runtime or via BuildConfig)
    const val DEFAULT_SUPABASE_URL = "https://aura-wallpapers.supabase.co/rest/v1/"
    const val DEFAULT_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy_anon_key_for_aura_client"

    // Download worker keys
    const val KEY_DOWNLOAD_ID = "key_download_id"
    const val KEY_DOWNLOAD_URL = "key_download_url"
    const val KEY_DOWNLOAD_TITLE = "key_download_title"
    const val KEY_DOWNLOAD_FILE_NAME = "key_download_file_name"
    const val KEY_IS_LIVE = "key_is_live"

    // Notification channel
    const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "aura_downloads_channel"
    const val DOWNLOAD_NOTIFICATION_CHANNEL_NAME = "Wallpaper Downloads"

    // Engine preferences
    const val PREF_KEY_ACTIVE_LIVE_WALLPAPER_PATH = "pref_active_live_wallpaper_path"
    const val PREF_KEY_ACTIVE_LIVE_WALLPAPER_TITLE = "pref_active_live_wallpaper_title"
    const val PREF_KEY_BATTERY_SAVER_ENABLED = "pref_battery_saver_enabled"
    const val PREF_KEY_WIFI_ONLY = "pref_wifi_only"
    const val PREF_KEY_THEME_MODE = "pref_theme_mode"

    const val PAGE_SIZE = 30
}
