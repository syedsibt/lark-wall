package com.aura.wallpapers.util

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.key.Keyer
import coil.request.Options
import com.aura.wallpapers.engine.interactive.InteractiveSnapshot

/**
 * Coil fetcher for `engine://thumb/<presetId>` URIs.
 *
 * Interactive wallpapers are drawn by code, so instead of shipping a pre-rendered
 * image per wallpaper (which drifted out of sync with the real animation and bloated
 * the APK) the thumbnail is rendered from the same engine that draws the wallpaper.
 * Results are kept in Coil's memory cache through [InteractiveThumbnailKeyer].
 */
class InteractiveThumbnailFetcher(
    private val presetId: String,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val bitmap = InteractiveSnapshot.render(presetId, THUMB_WIDTH, THUMB_HEIGHT)
        return DrawableResult(
            drawable = BitmapDrawable(options.context.resources, bitmap),
            isSampled = false,
            dataSource = DataSource.MEMORY
        )
    }

    class Factory : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (!isThumbUri(data)) return null
            val presetId = data.lastPathSegment?.takeIf { it.isNotBlank() } ?: return null
            return InteractiveThumbnailFetcher(presetId, options)
        }
    }

    companion object {
        // 9:16 at roughly the on-screen size of a 2-column grid card
        const val THUMB_WIDTH = 432
        const val THUMB_HEIGHT = 768

        fun isThumbUri(data: Uri): Boolean = data.scheme == "engine" && data.host == "thumb"
    }
}

/** Gives engine thumbnails a stable memory-cache key (Coil skips caching data without a key). */
class InteractiveThumbnailKeyer : Keyer<Uri> {
    override fun key(data: Uri, options: Options): String? =
        if (InteractiveThumbnailFetcher.isThumbUri(data)) data.toString() else null
}
