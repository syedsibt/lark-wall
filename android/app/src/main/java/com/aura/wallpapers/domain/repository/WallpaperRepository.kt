package com.aura.wallpapers.domain.repository

import com.aura.wallpapers.domain.model.Wallpaper
import kotlinx.coroutines.flow.Flow

interface WallpaperRepository {
    fun getPopularWallpapers(page: Int): Flow<List<Wallpaper>>
    fun getNewestWallpapers(page: Int): Flow<List<Wallpaper>>
    fun getFeaturedWallpapers(): Flow<List<Wallpaper>>
    fun getWallpapersByCategory(categoryId: String, page: Int): Flow<List<Wallpaper>>
    suspend fun getWallpaperById(id: String): Wallpaper?
    fun searchWallpapers(query: String): Flow<List<Wallpaper>>
    suspend fun incrementDownload(id: String)
}
