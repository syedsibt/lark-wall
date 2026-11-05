package com.aura.wallpapers.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aura.wallpapers.domain.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val slug: String,
    val description: String?,
    val iconName: String,
    val coverImageUrl: String?,
    val isLive: Boolean,
    val displayOrder: Int,
    val wallpaperCount: Int
) {
    fun toDomain(): Category = Category(
        id = id,
        name = name,
        slug = slug,
        description = description,
        iconName = iconName,
        coverImageUrl = coverImageUrl,
        isLive = isLive,
        displayOrder = displayOrder,
        wallpaperCount = wallpaperCount
    )
}

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    slug = slug,
    description = description,
    iconName = iconName,
    coverImageUrl = coverImageUrl,
    isLive = isLive,
    displayOrder = displayOrder,
    wallpaperCount = wallpaperCount
)
