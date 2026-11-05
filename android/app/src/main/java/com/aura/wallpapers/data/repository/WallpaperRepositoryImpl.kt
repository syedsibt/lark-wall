package com.aura.wallpapers.data.repository

import com.aura.wallpapers.data.local.dao.FavoriteDao
import com.aura.wallpapers.data.local.dao.WallpaperDao
import com.aura.wallpapers.data.local.entity.toEntity
import com.aura.wallpapers.data.remote.AuraApiService
import com.aura.wallpapers.data.remote.IncrementDownloadRequest
import com.aura.wallpapers.domain.model.Wallpaper
import com.aura.wallpapers.domain.repository.WallpaperRepository
import com.aura.wallpapers.util.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperRepositoryImpl @Inject constructor(
    private val apiService: AuraApiService,
    private val wallpaperDao: WallpaperDao,
    private val favoriteDao: FavoriteDao,
    private val ioDispatcher: CoroutineDispatcher
) : WallpaperRepository {

    override fun getPopularWallpapers(page: Int): Flow<List<Wallpaper>> {
        val offset = page * Constants.PAGE_SIZE
        return wallpaperDao.getPopularWallpapers(Constants.PAGE_SIZE, offset)
            .combine(favoriteDao.getAllFavorites()) { entities, favorites ->
                val favIds = favorites.map { it.assetId }.toSet()
                entities.map { it.toDomain(isFavorite = favIds.contains(it.id)) }
            }.flowOn(ioDispatcher)
    }

    override fun getNewestWallpapers(page: Int): Flow<List<Wallpaper>> {
        val offset = page * Constants.PAGE_SIZE
        return wallpaperDao.getNewestWallpapers(Constants.PAGE_SIZE, offset)
            .combine(favoriteDao.getAllFavorites()) { entities, favorites ->
                val favIds = favorites.map { it.assetId }.toSet()
                entities.map { it.toDomain(isFavorite = favIds.contains(it.id)) }
            }.flowOn(ioDispatcher)
    }

    override fun getFeaturedWallpapers(): Flow<List<Wallpaper>> {
        return wallpaperDao.getFeaturedWallpapers(10)
            .combine(favoriteDao.getAllFavorites()) { entities, favorites ->
                val favIds = favorites.map { it.assetId }.toSet()
                entities.map { it.toDomain(isFavorite = favIds.contains(it.id)) }
            }.flowOn(ioDispatcher)
    }

    override fun getWallpapersByCategory(categoryId: String, page: Int): Flow<List<Wallpaper>> {
        val offset = page * Constants.PAGE_SIZE
        return wallpaperDao.getWallpapersByCategory(categoryId, Constants.PAGE_SIZE, offset)
            .combine(favoriteDao.getAllFavorites()) { entities, favorites ->
                val favIds = favorites.map { it.assetId }.toSet()
                entities.map { it.toDomain(isFavorite = favIds.contains(it.id)) }
            }.flowOn(ioDispatcher)
    }

    override suspend fun getWallpaperById(id: String): Wallpaper? = withContext(ioDispatcher) {
        val entity = wallpaperDao.getWallpaperById(id) ?: return@withContext null
        entity.toDomain()
    }

    override fun searchWallpapers(query: String): Flow<List<Wallpaper>> {
        return wallpaperDao.searchWallpapers(query)
            .combine(favoriteDao.getAllFavorites()) { entities, favorites ->
                val favIds = favorites.map { it.assetId }.toSet()
                entities.map { it.toDomain(isFavorite = favIds.contains(it.id)) }
            }.flowOn(ioDispatcher)
    }

    override suspend fun incrementDownload(id: String): Unit = withContext(ioDispatcher) {
        runCatching {
            apiService.incrementDownloadCount(IncrementDownloadRequest(p_asset_id = id, p_asset_type = "static"))
        }
    }

    suspend fun refreshWallpapers(page: Int = 0) = withContext(ioDispatcher) {
        runCatching {
            val dts = apiService.getWallpapers(
                order = "download_count.desc",
                limit = Constants.PAGE_SIZE,
                offset = page * Constants.PAGE_SIZE
            )
            wallpaperDao.insertWallpapers(dts.map { it.toDomain().toEntity() })
        }
    }
}
