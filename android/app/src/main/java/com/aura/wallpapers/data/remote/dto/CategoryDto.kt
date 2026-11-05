package com.aura.wallpapers.data.remote.dto

import com.aura.wallpapers.domain.model.Category
import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("description") val description: String?,
    @SerializedName("icon_name") val iconName: String?,
    @SerializedName("cover_image_url") val coverImageUrl: String?,
    @SerializedName("is_live") val isLive: Boolean?,
    @SerializedName("display_order") val displayOrder: Int?,
    @SerializedName("wallpaper_count") val wallpaperCount: Int?
) {
    fun toDomain(): Category = Category(
        id = id,
        name = name,
        slug = slug,
        description = description,
        iconName = iconName ?: "category",
        coverImageUrl = coverImageUrl,
        isLive = isLive ?: false,
        displayOrder = displayOrder ?: 0,
        wallpaperCount = wallpaperCount ?: 0
    )
}
