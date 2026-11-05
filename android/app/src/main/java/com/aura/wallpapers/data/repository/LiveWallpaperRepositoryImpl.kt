package com.aura.wallpapers.data.repository

import com.aura.wallpapers.data.local.dao.FavoriteDao
import com.aura.wallpapers.data.local.dao.WallpaperDao
import com.aura.wallpapers.data.local.entity.toEntity
import com.aura.wallpapers.data.remote.AuraApiService
import com.aura.wallpapers.data.remote.IncrementDownloadRequest
import com.aura.wallpapers.domain.model.LiveWallpaper
import com.aura.wallpapers.domain.repository.LiveWallpaperRepository
import com.aura.wallpapers.util.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveWallpaperRepositoryImpl @Inject constructor(
    private val apiService: AuraApiService,
    private val wallpaperDao: WallpaperDao,
    private val favoriteDao: FavoriteDao,
    private val ioDispatcher: CoroutineDispatcher
) : LiveWallpaperRepository {

    override fun getPopularLiveWallpapers(page: Int): Flow<List<LiveWallpaper>> {
        val offset = page * Constants.PAGE_SIZE
        return wallpaperDao.getPopularLiveWallpapers(Constants.PAGE_SIZE, offset)
            .combine(favoriteDao.getAllFavorites()) { entities, favorites ->
                val favIds = favorites.map { it.assetId }.toSet()
                entities.map { it.toDomain(isFavorite = favIds.contains(it.id)) }
            }.flowOn(ioDispatcher)
    }

    override fun getLiveWallpapersByCategory(categoryId: String, page: Int): Flow<List<LiveWallpaper>> {
        val offset = page * Constants.PAGE_SIZE
        return wallpaperDao.getLiveWallpapersByCategory(categoryId, Constants.PAGE_SIZE, offset)
            .combine(favoriteDao.getAllFavorites()) { entities, favorites ->
                val favIds = favorites.map { it.assetId }.toSet()
                entities.map { it.toDomain(isFavorite = favIds.contains(it.id)) }
            }.flowOn(ioDispatcher)
    }

    override suspend fun getLiveWallpaperById(id: String): LiveWallpaper? = withContext(ioDispatcher) {
        val entity = wallpaperDao.getLiveWallpaperById(id) ?: return@withContext null
        entity.toDomain()
    }

    override fun searchLiveWallpapers(query: String): Flow<List<LiveWallpaper>> {
        return wallpaperDao.searchLiveWallpapers(query)
            .combine(favoriteDao.getAllFavorites()) { entities, favorites ->
                val favIds = favorites.map { it.assetId }.toSet()
                entities.map { it.toDomain(isFavorite = favIds.contains(it.id)) }
            }.flowOn(ioDispatcher)
    }

    override suspend fun incrementDownload(id: String): Unit = withContext(ioDispatcher) {
        runCatching {
            apiService.incrementDownloadCount(IncrementDownloadRequest(p_asset_id = id, p_asset_type = "live"))
        }
    }

    suspend fun refreshLiveWallpapers(page: Int = 0) = withContext(ioDispatcher) {
        runCatching {
            val dts = apiService.getLiveWallpapers(
                order = "download_count.desc",
                limit = Constants.PAGE_SIZE,
                offset = page * Constants.PAGE_SIZE
            )
            wallpaperDao.insertLiveWallpapers(dts.map { it.toDomain().toEntity() })
        }
    }
}
