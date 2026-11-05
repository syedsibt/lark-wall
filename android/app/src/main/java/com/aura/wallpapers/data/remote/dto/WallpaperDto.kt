package com.aura.wallpapers.data.remote.dto

import com.aura.wallpapers.domain.model.Wallpaper
import com.google.gson.annotations.SerializedName

data class WallpaperDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("description") val description: String?,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("categories") val category: CategorySnippetDto?,
    @SerializedName("tags") val tags: List<String>?,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("preview_url") val previewUrl: String,
    @SerializedName("original_url") val originalUrl: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("aspect_ratio") val aspectRatio: String?,
    @SerializedName("file_size_bytes") val fileSizeBytes: Long,
    @SerializedName("format") val format: String?,
    @SerializedName("download_count") val downloadCount: Long?,
    @SerializedName("favorite_count") val favoriteCount: Long?,
    @SerializedName("is_featured") val isFeatured: Boolean?,
    @SerializedName("is_premium") val isPremium: Boolean?,
    @SerializedName("license_type") val licenseType: String?,
    @SerializedName("author_name") val authorName: String?
) {
    fun toDomain(isFavorite: Boolean = false): Wallpaper = Wallpaper(
        id = id,
        title = title,
        slug = slug,
        description = description ?: "",
        categoryId = categoryId,
        categoryName = category?.name ?: "",
        tags = tags ?: emptyList(),
        thumbnailUrl = thumbnailUrl,
        previewUrl = previewUrl,
        originalUrl = originalUrl,
        width = width,
        height = height,
        aspectRatio = aspectRatio ?: "9:16",
        fileSizeBytes = fileSizeBytes,
        format = format ?: "image/webp",
        downloadCount = downloadCount ?: 0,
        favoriteCount = favoriteCount ?: 0,
        isFeatured = isFeatured ?: false,
        isPremium = isPremium ?: false,
        licenseType = licenseType ?: "Royalty-Free",
        authorName = authorName ?: "Aura Curated",
        isFavorite = isFavorite
    )
}

data class CategorySnippetDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String
)
