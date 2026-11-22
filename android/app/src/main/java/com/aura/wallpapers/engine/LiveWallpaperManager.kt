package com.aura.wallpapers.engine

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log

object LiveWallpaperManager {

    private const val TAG = "LiveWallpaperManager"

    fun openLiveWallpaperPreview(context: Context): Result<Unit> {
        return runCatching {
            val component = ComponentName(context, AuraLiveWallpaperService::class.java)

            // Attempt 1: Direct ACTION_CHANGE_LIVE_WALLPAPER
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                try {
                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                        putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return@runCatching
                } catch (e: Throwable) {
                    Log.w(TAG, "ACTION_CHANGE_LIVE_WALLPAPER not supported, trying fallback 1: ${e.message}")
                }
            }

            // Attempt 2: Generic Live Wallpaper Chooser
            try {
                val chooserIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(chooserIntent)
                return@runCatching
            } catch (e: Throwable) {
                Log.w(TAG, "ACTION_LIVE_WALLPAPER_CHOOSER failed, trying fallback 2: ${e.message}")
            }

            // Attempt 3: General Wallpaper Settings
            try {
                val setWallpaperIntent = Intent(Intent.ACTION_SET_WALLPAPER).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(setWallpaperIntent)
                return@runCatching
            } catch (e: Throwable) {
                Log.w(TAG, "ACTION_SET_WALLPAPER failed, trying fallback 3: ${e.message}")
            }

            // Attempt 4: Display Settings
            val displaySettingsIntent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(displaySettingsIntent)
        }
    }
}
