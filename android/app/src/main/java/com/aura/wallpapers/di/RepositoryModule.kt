package com.aura.wallpapers.di

import com.aura.wallpapers.data.repository.CategoryRepositoryImpl
import com.aura.wallpapers.data.repository.DownloadRepositoryImpl
import com.aura.wallpapers.data.repository.FavoritesRepositoryImpl
import com.aura.wallpapers.data.repository.LiveWallpaperRepositoryImpl
import com.aura.wallpapers.data.repository.SettingsRepositoryImpl
import com.aura.wallpapers.data.repository.WallpaperRepositoryImpl
import com.aura.wallpapers.domain.repository.CategoryRepository
import com.aura.wallpapers.domain.repository.DownloadRepository
import com.aura.wallpapers.domain.repository.FavoritesRepository
import com.aura.wallpapers.domain.repository.LiveWallpaperRepository
import com.aura.wallpapers.domain.repository.SettingsRepository
import com.aura.wallpapers.domain.repository.WallpaperRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWallpaperRepository(impl: WallpaperRepositoryImpl): WallpaperRepository

    @Binds
    @Singleton
    abstract fun bindLiveWallpaperRepository(impl: LiveWallpaperRepositoryImpl): LiveWallpaperRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
