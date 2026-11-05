package com.aura.wallpapers.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aura.wallpapers.domain.model.Wallpaper

@Entity(tableName = "wallpapers")
data class WallpaperEntity(
    @PrimaryKey val id: String,
    val title: String,
    val slug: String,
    val description: String,
    val categoryId: String,
    val categoryName: String,
    val tagsCsv: String,
    val thumbnailUrl: String,
    val previewUrl: String,
    val originalUrl: String,
    val width: Int,
    val height: Int,
    val aspectRatio: String,
    val fileSizeBytes: Long,
    val format: String,
    val downloadCount: Long,
    val favoriteCount: Long,
    val isFeatured: Boolean,
    val isPremium: Boolean,
    val licenseType: String,
    val authorName: String,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(isFavorite: Boolean = false): Wallpaper = Wallpaper(
        id = id,
        title = title,
        slug = slug,
        description = description,
        categoryId = categoryId,
        categoryName = categoryName,
        tags = if (tagsCsv.isBlank()) emptyList() else tagsCsv.split(",").map { it.trim() },
        thumbnailUrl = thumbnailUrl,
        previewUrl = previewUrl,
        originalUrl = originalUrl,
        width = width,
        height = height,
        aspectRatio = aspectRatio,
        fileSizeBytes = fileSizeBytes,
        format = format,
        downloadCount = downloadCount,
        favoriteCount = favoriteCount,
        isFeatured = isFeatured,
        isPremium = isPremium,
        licenseType = licenseType,
        authorName = authorName,
        isFavorite = isFavorite
    )
}

fun Wallpaper.toEntity(): WallpaperEntity = WallpaperEntity(
    id = id,
    title = title,
    slug = slug,
    description = description,
    categoryId = categoryId,
    categoryName = categoryName,
    tagsCsv = tags.joinToString(","),
    thumbnailUrl = thumbnailUrl,
    previewUrl = previewUrl,
    originalUrl = originalUrl,
    width = width,
    height = height,
    aspectRatio = aspectRatio,
    fileSizeBytes = fileSizeBytes,
    format = format,
    downloadCount = downloadCount,
    favoriteCount = favoriteCount,
    isFeatured = isFeatured,
    isPremium = isPremium,
    licenseType = licenseType,
    authorName = authorName
)
