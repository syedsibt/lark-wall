package com.aura.wallpapers.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val assetId: String,
    val isLive: Boolean,
    val title: String,
    val thumbnailUrl: String,
    val addedAt: Long = System.currentTimeMillis()
)
