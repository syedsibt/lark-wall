package com.aura.wallpapers.engine.interactive

import android.graphics.Bitmap
import android.graphics.Canvas

/**
 * Renders a still frame of an interactive wallpaper into a Bitmap.
 *
 * Used for grid thumbnails (so a card always shows exactly what the wallpaper looks like)
 * and for "Download" of interactive wallpapers. Safe to call from a background thread:
 * every call uses its own renderer instance.
 */
object InteractiveSnapshot {

    /** Logical design width the renderers are tuned for. */
    private const val DESIGN_WIDTH = 1080

    /** Point in the animation used for stills, far enough in for every design to be "in motion". */
    const val DEFAULT_TIME_SECONDS = 2.6f

    fun render(
        presetId: String,
        outWidth: Int,
        outHeight: Int,
        timeSeconds: Float = DEFAULT_TIME_SECONDS
    ): Bitmap {
        val w = outWidth.coerceAtLeast(1)
        val h = outHeight.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val scale = w / DESIGN_WIDTH.toFloat()
        val logicalHeight = (h / scale).toInt().coerceAtLeast(1)

        val config = InteractiveWallpaperRegistry.getConfig(presetId)
        val renderer = InteractiveWallpaperRenderer(config)
        renderer.setDimensions(DESIGN_WIDTH, logicalHeight)
        renderer.setSimulationTime(timeSeconds * config.speed)

        canvas.save()
        canvas.scale(scale, scale)
        renderer.render(canvas, 1f / 60f)
        canvas.restore()
        return bitmap
    }

    /** URI understood by [com.aura.wallpapers.util.InteractiveThumbnailFetcher]. */
    fun thumbnailUri(presetId: String): String = "$THUMB_PREFIX$presetId"

    const val THUMB_PREFIX = "engine://thumb/"
}
