package com.aura.wallpapers.domain.model

data class Wallpaper(
    val id: String,
    val title: String,
    val slug: String,
    val description: String,
    val categoryId: String,
    val categoryName: String = "",
    val tags: List<String> = emptyList(),
    val thumbnailUrl: String,
    val previewUrl: String,
    val originalUrl: String,
    val width: Int,
    val height: Int,
    val aspectRatio: String = "9:16",
    val fileSizeBytes: Long,
    val format: String = "image/webp",
    val downloadCount: Long = 0,
    val favoriteCount: Long = 0,
    val isFeatured: Boolean = false,
    val isPremium: Boolean = false,
    val licenseType: String = "Royalty-Free",
    val authorName: String = "Aura Curated",
    val isFavorite: Boolean = false
) {
    val resolutionLabel: String
        get() = "${width}x${height}"

    val formattedSize: String
        get() = when {
            fileSizeBytes >= 1024 * 1024 -> String.format("%.2f MB", fileSizeBytes / (1024.0 * 1024.0))
            fileSizeBytes >= 1024 -> String.format("%.1f KB", fileSizeBytes / 1024.0)
            else -> "$fileSizeBytes B"
        }
}
