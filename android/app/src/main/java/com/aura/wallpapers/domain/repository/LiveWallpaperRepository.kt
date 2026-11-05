package com.aura.wallpapers.domain.repository

import com.aura.wallpapers.domain.model.LiveWallpaper
import kotlinx.coroutines.flow.Flow

interface LiveWallpaperRepository {
    fun getPopularLiveWallpapers(page: Int): Flow<List<LiveWallpaper>>
    fun getLiveWallpapersByCategory(categoryId: String, page: Int): Flow<List<LiveWallpaper>>
    suspend fun getLiveWallpaperById(id: String): LiveWallpaper?
    fun searchLiveWallpapers(query: String): Flow<List<LiveWallpaper>>
    suspend fun incrementDownload(id: String)
}
