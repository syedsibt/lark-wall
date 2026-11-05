package com.aura.wallpapers

import com.aura.wallpapers.data.local.dao.FavoriteDao
import com.aura.wallpapers.data.local.dao.WallpaperDao
import com.aura.wallpapers.data.local.entity.FavoriteEntity
import com.aura.wallpapers.data.local.entity.WallpaperEntity
import com.aura.wallpapers.data.remote.AuraApiService
import com.aura.wallpapers.data.repository.WallpaperRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WallpaperRepositoryTest {

    private val apiService: AuraApiService = mockk()
    private val wallpaperDao: WallpaperDao = mockk()
    private val favoriteDao: FavoriteDao = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: WallpaperRepositoryImpl

    @Before
    fun setUp() {
        repository = WallpaperRepositoryImpl(
            apiService = apiService,
            wallpaperDao = wallpaperDao,
            favoriteDao = favoriteDao,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `getPopularWallpapers accurately maps entities and reflects favorite status`() = runTest(testDispatcher) {
        val entity = WallpaperEntity(
            id = "w1",
            title = "Obsidian Peaks",
            slug = "obsidian-peaks",
            description = "Dark mountain peak",
            categoryId = "c1",
            categoryName = "Mountains",
            tagsCsv = "dark,mountains,amoled",
            thumbnailUrl = "thumb.webp",
            previewUrl = "preview.webp",
            originalUrl = "original.webp",
            width = 2160,
            height = 3840,
            aspectRatio = "9:16",
            fileSizeBytes = 2048000,
            format = "image/webp",
            downloadCount = 1500,
            favoriteCount = 300,
            isFeatured = true,
            isPremium = false,
            licenseType = "Royalty-Free",
            authorName = "Aura Studio"
        )

        every { wallpaperDao.getPopularWallpapers(any(), any()) } returns flowOf(listOf(entity))
        every { favoriteDao.getAllFavorites() } returns flowOf(listOf(FavoriteEntity("w1", false, "Obsidian Peaks", "thumb.webp")))

        val result = repository.getPopularWallpapers(page = 0).first()

        assertEquals(1, result.size)
        assertEquals("Obsidian Peaks", result[0].title)
        assertTrue("Expected wallpaper to be marked as favorite", result[0].isFavorite)
    }
}
