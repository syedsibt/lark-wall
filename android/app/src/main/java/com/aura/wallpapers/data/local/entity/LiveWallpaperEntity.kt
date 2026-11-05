package com.aura.wallpapers.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aura.wallpapers.domain.model.LiveWallpaper

@Entity(tableName = "live_wallpapers")
data class LiveWallpaperEntity(
    @PrimaryKey val id: String,
    val title: String,
    val slug: String,
    val description: String,
    val categoryId: String,
    val categoryName: String,
    val tagsCsv: String,
    val thumbnailUrl: String,
    val previewVideoUrl: String,
    val originalVideoUrl: String,
    val width: Int,
    val height: Int,
    val durationSeconds: Double,
    val frameRate: Double,
    val hasAudio: Boolean,
    val codec: String,
    val fileSizeBytes: Long,
    val isSeamlessLoop: Boolean,
    val downloadCount: Long,
    val favoriteCount: Long,
    val isFeatured: Boolean,
    val isPremium: Boolean,
    val licenseType: String,
    val authorName: String,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(isFavorite: Boolean = false): LiveWallpaper = LiveWallpaper(
        id = id,
        title = title,
        slug = slug,
        description = description,
        categoryId = categoryId,
        categoryName = categoryName,
        tags = if (tagsCsv.isBlank()) emptyList() else tagsCsv.split(",").map { it.trim() },
        thumbnailUrl = thumbnailUrl,
        previewVideoUrl = previewVideoUrl,
        originalVideoUrl = originalVideoUrl,
        width = width,
        height = height,
        durationSeconds = durationSeconds,
        frameRate = frameRate,
        hasAudio = hasAudio,
        codec = codec,
        fileSizeBytes = fileSizeBytes,
        isSeamlessLoop = isSeamlessLoop,
        downloadCount = downloadCount,
        favoriteCount = favoriteCount,
        isFeatured = isFeatured,
        isPremium = isPremium,
        licenseType = licenseType,
        authorName = authorName,
        isFavorite = isFavorite
    )
}

fun LiveWallpaper.toEntity(): LiveWallpaperEntity = LiveWallpaperEntity(
    id = id,
    title = title,
    slug = slug,
    description = description,
    categoryId = categoryId,
    categoryName = categoryName,
    tagsCsv = tags.joinToString(","),
    thumbnailUrl = thumbnailUrl,
    previewVideoUrl = previewVideoUrl,
    originalVideoUrl = originalVideoUrl,
    width = width,
    height = height,
    durationSeconds = durationSeconds,
    frameRate = frameRate,
    hasAudio = hasAudio,
    codec = codec,
    fileSizeBytes = fileSizeBytes,
    isSeamlessLoop = isSeamlessLoop,
    downloadCount = downloadCount,
    favoriteCount = favoriteCount,
    isFeatured = isFeatured,
    isPremium = isPremium,
    licenseType = licenseType,
    authorName = authorName
)
