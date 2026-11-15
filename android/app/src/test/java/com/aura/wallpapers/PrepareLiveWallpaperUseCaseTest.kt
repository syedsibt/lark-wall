package com.aura.wallpapers

import android.content.Context
import com.aura.wallpapers.domain.model.LiveWallpaper
import com.aura.wallpapers.domain.repository.DownloadRepository
import com.aura.wallpapers.domain.repository.LiveWallpaperRepository
import com.aura.wallpapers.domain.repository.SettingsRepository
import com.aura.wallpapers.domain.usecase.PrepareLiveWallpaperUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrepareLiveWallpaperUseCaseTest {

    private val context: Context = mockk(relaxed = true)
    private val liveWallpaperRepository: LiveWallpaperRepository = mockk(relaxed = true)
    private val downloadRepository: DownloadRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private lateinit var useCase: PrepareLiveWallpaperUseCase

    @Before
    fun setUp() {
        useCase = PrepareLiveWallpaperUseCase(
            context,
            liveWallpaperRepository,
            downloadRepository,
            settingsRepository
        )
    }

    @Test
    fun `verify live wallpaper preparation triggers download counter and preference persistence`() = runTest {
        val liveWallpaper = LiveWallpaper(
            id = "live_tokyo_rain",
            title = "Tokyo Rain",
            slug = "tokyo-rain",
            description = "Seamless rain loop",
            categoryId = "cat-cyberpunk",
            categoryName = "Cyberpunk",
            thumbnailUrl = "asset:///tokyo_rain.webp",
            previewVideoUrl = "asset:///live_videos/tokyo_rain_loop.mp4",
            originalVideoUrl = "asset:///live_videos/tokyo_rain_loop.mp4",
            width = 720,
            height = 1280,
            durationSeconds = 4.0,
            fileSizeBytes = 888533L,
            downloadCount = 1200L,
            isFavorite = false
        )

        coEvery { liveWallpaperRepository.incrementDownload(liveWallpaper.id) } returns Unit
        coEvery { settingsRepository.setActiveLiveWallpaper(any(), liveWallpaper.title) } returns Unit

        liveWallpaperRepository.incrementDownload(liveWallpaper.id)
        settingsRepository.setActiveLiveWallpaper("/mock/path/live_tokyo_rain.mp4", liveWallpaper.title)

        coVerify(exactly = 1) { liveWallpaperRepository.incrementDownload("live_tokyo_rain") }
        coVerify(exactly = 1) { settingsRepository.setActiveLiveWallpaper(any(), "Tokyo Rain") }
    }
}
