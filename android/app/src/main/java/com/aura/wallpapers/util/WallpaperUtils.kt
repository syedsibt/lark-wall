package com.aura.wallpapers.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.aura.wallpapers.domain.model.ApplyTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL

object WallpaperUtils {

    suspend fun applyStaticWallpaper(
        context: Context,
        imageUrl: String,
        target: ApplyTarget
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val stream: InputStream = URL(imageUrl).openStream()
            val bitmap = BitmapFactory.decodeStream(stream)
                ?: throw IllegalStateException("Failed to decode image stream from $imageUrl")

            val flags = when (target) {
                ApplyTarget.HOME_SCREEN -> WallpaperManager.FLAG_SYSTEM
                ApplyTarget.LOCK_SCREEN -> WallpaperManager.FLAG_LOCK
                ApplyTarget.BOTH_SCREENS -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wallpaperManager.setBitmap(bitmap, null, true, flags)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }
            bitmap.recycle()
        }
    }
}
