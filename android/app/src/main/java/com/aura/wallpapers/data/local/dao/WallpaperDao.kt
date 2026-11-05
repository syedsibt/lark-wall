package com.aura.wallpapers.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aura.wallpapers.data.local.entity.LiveWallpaperEntity
import com.aura.wallpapers.data.local.entity.WallpaperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpapers(wallpapers: List<WallpaperEntity>)

    @Query("SELECT COUNT(*) FROM wallpapers")
    suspend fun getWallpaperCount(): Int

    @Query("SELECT * FROM wallpapers ORDER BY downloadCount DESC LIMIT :limit OFFSET :offset")
    fun getPopularWallpapers(limit: Int, offset: Int): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers ORDER BY cachedAt DESC LIMIT :limit OFFSET :offset")
    fun getNewestWallpapers(limit: Int, offset: Int): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isFeatured = 1 ORDER BY downloadCount DESC LIMIT :limit")
    fun getFeaturedWallpapers(limit: Int = 10): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE categoryId = :categoryId ORDER BY downloadCount DESC LIMIT :limit OFFSET :offset")
    fun getWallpapersByCategory(categoryId: String, limit: Int, offset: Int): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE id = :id LIMIT 1")
    suspend fun getWallpaperById(id: String): WallpaperEntity?

    @Query("SELECT * FROM wallpapers WHERE title LIKE '%' || :query || '%' OR tagsCsv LIKE '%' || :query || '%'")
    fun searchWallpapers(query: String): Flow<List<WallpaperEntity>>

    // Live Wallpapers
    @Query("SELECT COUNT(*) FROM live_wallpapers")
    suspend fun getLiveWallpaperCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveWallpapers(liveWallpapers: List<LiveWallpaperEntity>)

    @Query("SELECT * FROM live_wallpapers ORDER BY downloadCount DESC LIMIT :limit OFFSET :offset")
    fun getPopularLiveWallpapers(limit: Int, offset: Int): Flow<List<LiveWallpaperEntity>>

    @Query("SELECT * FROM live_wallpapers WHERE categoryId = :categoryId ORDER BY downloadCount DESC LIMIT :limit OFFSET :offset")
    fun getLiveWallpapersByCategory(categoryId: String, limit: Int, offset: Int): Flow<List<LiveWallpaperEntity>>

    @Query("SELECT * FROM live_wallpapers WHERE id = :id LIMIT 1")
    suspend fun getLiveWallpaperById(id: String): LiveWallpaperEntity?

    @Query("SELECT * FROM live_wallpapers WHERE title LIKE '%' || :query || '%' OR tagsCsv LIKE '%' || :query || '%'")
    fun searchLiveWallpapers(query: String): Flow<List<LiveWallpaperEntity>>
}
