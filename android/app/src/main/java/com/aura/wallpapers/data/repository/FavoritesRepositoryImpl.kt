package com.aura.wallpapers.data.repository

import com.aura.wallpapers.data.local.dao.FavoriteDao
import com.aura.wallpapers.data.local.entity.FavoriteEntity
import com.aura.wallpapers.domain.repository.FavoritesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val ioDispatcher: CoroutineDispatcher
) : FavoritesRepository {

    override suspend fun addFavorite(
        assetId: String,
        isLive: Boolean,
        title: String,
        thumbnailUrl: String
    ) = withContext(ioDispatcher) {
        favoriteDao.insertFavorite(
            FavoriteEntity(
                assetId = assetId,
                isLive = isLive,
                title = title,
                thumbnailUrl = thumbnailUrl
            )
        )
    }

    override suspend fun removeFavorite(assetId: String) = withContext(ioDispatcher) {
        favoriteDao.deleteFavorite(assetId)
    }

    override fun isFavorite(assetId: String): Flow<Boolean> {
        return favoriteDao.isFavorite(assetId).flowOn(ioDispatcher)
    }

    override fun getAllFavorites(): Flow<List<FavoriteEntity>> {
        return favoriteDao.getAllFavorites().flowOn(ioDispatcher)
    }
}
