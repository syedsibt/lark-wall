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
import java.net.HttpURLConnection
import java.net.URL

object WallpaperUtils {

    suspend fun applyStaticWallpaper(
        context: Context,
        imageUrl: String,
        target: ApplyTarget
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val wallpaperManager = WallpaperManager.getInstance(context)

            // Connect with high-bandwidth mobile headers
            val conn = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 25000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; Lark Wall 4K)")
            }

            val stream: InputStream = conn.inputStream

            // Decode in uncompressed 32-bit ARGB_8888 True Color
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inDither = true
                inScaled = false
            }

            val originalBitmap = BitmapFactory.decodeStream(stream, null, options)
                ?: throw IllegalStateException("Failed to decode Ultra HD image stream")

            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            // Scale and crop bitmap to pixel-perfect device screen aspect ratio
            val scale = maxOf(
                screenWidth.toFloat() / originalBitmap.width.toFloat(),
                screenHeight.toFloat() / originalBitmap.height.toFloat()
            )
            val scaledWidth = maxOf(1, (originalBitmap.width * scale).toInt())
            val scaledHeight = maxOf(1, (originalBitmap.height * scale).toInt())

            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true)

            val xOffset = maxOf(0, (scaledWidth - screenWidth) / 2)
            val yOffset = maxOf(0, (scaledHeight - screenHeight) / 2)
            val croppedBitmap = Bitmap.createBitmap(
                scaledBitmap,
                xOffset,
                yOffset,
                minOf(screenWidth, scaledWidth),
                minOf(screenHeight, scaledHeight)
            )

            val flags = when (target) {
                ApplyTarget.HOME_SCREEN -> WallpaperManager.FLAG_SYSTEM
                ApplyTarget.LOCK_SCREEN -> WallpaperManager.FLAG_LOCK
                ApplyTarget.BOTH_SCREENS -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wallpaperManager.setBitmap(croppedBitmap, null, true, flags)
            } else {
                wallpaperManager.setBitmap(croppedBitmap)
            }

            if (originalBitmap != scaledBitmap) originalBitmap.recycle()
            if (scaledBitmap != croppedBitmap) scaledBitmap.recycle()
            croppedBitmap.recycle()
        }
    }
}
