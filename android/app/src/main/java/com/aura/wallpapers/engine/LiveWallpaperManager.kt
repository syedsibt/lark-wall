package com.aura.wallpapers.engine

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

object LiveWallpaperManager {

    fun openLiveWallpaperPreview(context: Context): Result<Unit> {
        return runCatching {
            val component = ComponentName(context, AuraLiveWallpaperService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                try {
                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                        putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return@runCatching
                } catch (e: Exception) {
                    // Fall back to system chooser if OEM ROM disables direct ACTION_CHANGE_LIVE_WALLPAPER
                }
            }

            // Fallback: Launch generic live wallpaper chooser
            val chooserIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooserIntent)
        }
    }
}
