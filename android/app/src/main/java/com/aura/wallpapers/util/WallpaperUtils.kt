package com.aura.wallpapers.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.aura.wallpapers.domain.model.ApplyTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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

            fun openStream(): InputStream {
                return when {
                    imageUrl.startsWith("asset:///") -> {
                        val assetPath = imageUrl.removePrefix("asset:///")
                        context.assets.open(assetPath)
                    }
                    imageUrl.startsWith("file://") -> {
                        val path = imageUrl.removePrefix("file://")
                        File(path).inputStream()
                    }
                    File(imageUrl).exists() -> {
                        File(imageUrl).inputStream()
                    }
                    else -> {
                        val conn = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                            connectTimeout = 15000
                            readTimeout = 25000
                            setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; Lark Wall 4K)")
                        }
                        conn.inputStream
                    }
                }
            }

            // 1. First decode bounds to compute safe inSampleSize and avoid OutOfMemoryError
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            openStream().use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            var inSampleSize = 1
            if (boundsOptions.outHeight > screenHeight * 2 || boundsOptions.outWidth > screenWidth * 2) {
                val halfHeight = boundsOptions.outHeight / 2
                val halfWidth = boundsOptions.outWidth / 2
                while ((halfHeight / inSampleSize) >= screenHeight && (halfWidth / inSampleSize) >= screenWidth) {
                    inSampleSize *= 2
                }
            }

            // 2. Decode bitmap with computed sample size
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inDither = true
                inScaled = false
            }

            val originalBitmap = openStream().use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: throw IllegalStateException("Failed to decode Ultra HD image stream")

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

            if (originalBitmap != scaledBitmap && !originalBitmap.isRecycled) originalBitmap.recycle()
            if (scaledBitmap != croppedBitmap && !scaledBitmap.isRecycled) scaledBitmap.recycle()
            if (!croppedBitmap.isRecycled) croppedBitmap.recycle()
        }
    }
}
