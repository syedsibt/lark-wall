package com.aura.wallpapers.domain.repository

import com.aura.wallpapers.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    suspend fun addFavorite(assetId: String, isLive: Boolean, title: String, thumbnailUrl: String)
    suspend fun removeFavorite(assetId: String)
    fun isFavorite(assetId: String): Flow<Boolean>
    fun getAllFavorites(): Flow<List<FavoriteEntity>>
}
