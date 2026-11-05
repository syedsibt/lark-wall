package com.aura.wallpapers.domain.model

data class LiveWallpaper(
    val id: String,
    val title: String,
    val slug: String,
    val description: String,
    val categoryId: String,
    val categoryName: String = "",
    val tags: List<String> = emptyList(),
    val thumbnailUrl: String,
    val previewVideoUrl: String,
    val originalVideoUrl: String,
    val width: Int,
    val height: Int,
    val durationSeconds: Double,
    val frameRate: Double = 30.0,
    val hasAudio: Boolean = false,
    val codec: String = "video/mp4",
    val fileSizeBytes: Long,
    val isSeamlessLoop: Boolean = true,
    val downloadCount: Long = 0,
    val favoriteCount: Long = 0,
    val isFeatured: Boolean = false,
    val isPremium: Boolean = false,
    val licenseType: String = "Royalty-Free",
    val authorName: String = "Aura Motion Studio",
    val isFavorite: Boolean = false
) {
    val durationLabel: String
        get() = "${durationSeconds.toInt()}s"

    val resolutionLabel: String
        get() = "${width}x${height} • ${frameRate.toInt()}fps"

    val formattedSize: String
        get() = when {
            fileSizeBytes >= 1024 * 1024 -> String.format("%.2f MB", fileSizeBytes / (1024.0 * 1024.0))
            fileSizeBytes >= 1024 -> String.format("%.1f KB", fileSizeBytes / 1024.0)
            else -> "$fileSizeBytes B"
        }
}
