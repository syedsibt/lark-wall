package com.aura.wallpapers.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aura.wallpapers.data.local.dao.CategoryDao
import com.aura.wallpapers.data.local.dao.DownloadDao
import com.aura.wallpapers.data.local.dao.FavoriteDao
import com.aura.wallpapers.data.local.dao.SearchDao
import com.aura.wallpapers.data.local.dao.WallpaperDao
import com.aura.wallpapers.data.local.entity.CategoryEntity
import com.aura.wallpapers.data.local.entity.DownloadEntity
import com.aura.wallpapers.data.local.entity.FavoriteEntity
import com.aura.wallpapers.data.local.entity.LiveWallpaperEntity
import com.aura.wallpapers.data.local.entity.SearchHistoryEntity
import com.aura.wallpapers.data.local.entity.WallpaperEntity

@Database(
    entities = [
        WallpaperEntity::class,
        LiveWallpaperEntity::class,
        CategoryEntity::class,
        FavoriteEntity::class,
        DownloadEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun wallpaperDao(): WallpaperDao
    abstract fun categoryDao(): CategoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun downloadDao(): DownloadDao
    abstract fun searchDao(): SearchDao
}
