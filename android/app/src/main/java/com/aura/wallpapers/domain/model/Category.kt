package com.aura.wallpapers.domain.model

data class Category(
    val id: String,
    val name: String,
    val slug: String,
    val description: String? = null,
    val iconName: String = "category",
    val coverImageUrl: String? = null,
    val isLive: Boolean = false,
    val displayOrder: Int = 0,
    val wallpaperCount: Int = 0
)
