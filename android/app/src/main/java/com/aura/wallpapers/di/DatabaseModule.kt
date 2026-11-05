package com.aura.wallpapers.di

import android.content.Context
import androidx.room.Room
import com.aura.wallpapers.data.local.AuraDatabase
import com.aura.wallpapers.data.local.dao.CategoryDao
import com.aura.wallpapers.data.local.dao.DownloadDao
import com.aura.wallpapers.data.local.dao.FavoriteDao
import com.aura.wallpapers.data.local.dao.SearchDao
import com.aura.wallpapers.data.local.dao.WallpaperDao
import com.aura.wallpapers.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAuraDatabase(@ApplicationContext context: Context): AuraDatabase {
        return Room.databaseBuilder(
            context,
            AuraDatabase::class.java,
            Constants.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideWallpaperDao(db: AuraDatabase): WallpaperDao = db.wallpaperDao()

    @Provides
    fun provideCategoryDao(db: AuraDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideFavoriteDao(db: AuraDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideDownloadDao(db: AuraDatabase): DownloadDao = db.downloadDao()

    @Provides
    fun provideSearchDao(db: AuraDatabase): SearchDao = db.searchDao()
}
