package com.aura.wallpapers.data.remote.dto

import com.aura.wallpapers.domain.model.LiveWallpaper
import com.google.gson.annotations.SerializedName

data class LiveWallpaperDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("description") val description: String?,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("categories") val category: CategorySnippetDto?,
    @SerializedName("tags") val tags: List<String>?,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("preview_video_url") val previewVideoUrl: String,
    @SerializedName("original_video_url") val originalVideoUrl: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("duration_seconds") val durationSeconds: Double,
    @SerializedName("frame_rate") val frameRate: Double?,
    @SerializedName("has_audio") val hasAudio: Boolean?,
    @SerializedName("codec") val codec: String?,
    @SerializedName("file_size_bytes") val fileSizeBytes: Long,
    @SerializedName("is_seamless_loop") val isSeamlessLoop: Boolean?,
    @SerializedName("download_count") val downloadCount: Long?,
    @SerializedName("favorite_count") val favoriteCount: Long?,
    @SerializedName("is_featured") val isFeatured: Boolean?,
    @SerializedName("is_premium") val isPremium: Boolean?,
    @SerializedName("license_type") val licenseType: String?,
    @SerializedName("author_name") val authorName: String?
) {
    fun toDomain(isFavorite: Boolean = false): LiveWallpaper = LiveWallpaper(
        id = id,
        title = title,
        slug = slug,
        description = description ?: "",
        categoryId = categoryId,
        categoryName = category?.name ?: "",
        tags = tags ?: emptyList(),
        thumbnailUrl = thumbnailUrl,
        previewVideoUrl = previewVideoUrl,
        originalVideoUrl = originalVideoUrl,
        width = width,
        height = height,
        durationSeconds = durationSeconds,
        frameRate = frameRate ?: 30.0,
        hasAudio = hasAudio ?: false,
        codec = codec ?: "video/mp4",
        fileSizeBytes = fileSizeBytes,
        isSeamlessLoop = isSeamlessLoop ?: true,
        downloadCount = downloadCount ?: 0,
        favoriteCount = favoriteCount ?: 0,
        isFeatured = isFeatured ?: false,
        isPremium = isPremium ?: false,
        licenseType = licenseType ?: "Royalty-Free",
        authorName = authorName ?: "Aura Motion Studio",
        isFavorite = isFavorite
    )
}
