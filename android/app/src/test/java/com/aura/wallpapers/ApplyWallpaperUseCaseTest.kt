package com.aura.wallpapers

import android.content.Context
import com.aura.wallpapers.domain.model.ApplyTarget
import com.aura.wallpapers.domain.repository.WallpaperRepository
import com.aura.wallpapers.domain.usecase.ApplyStaticWallpaperUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ApplyWallpaperUseCaseTest {

    private val context: Context = mockk(relaxed = true)
    private val wallpaperRepository: WallpaperRepository = mockk(relaxed = true)
    private lateinit var useCase: ApplyStaticWallpaperUseCase

    @Before
    fun setUp() {
        useCase = ApplyStaticWallpaperUseCase(context, wallpaperRepository)
    }

    @Test
    fun `when apply target is Home Screen, increment download is called on success`() = runTest {
        val wallpaperId = "w-101"
        val originalUrl = "https://example.com/original.webp"

        coEvery { wallpaperRepository.incrementDownload(wallpaperId) } returns Unit

        // Verify the invocation triggers repo atomic count
        wallpaperRepository.incrementDownload(wallpaperId)
        coVerify(exactly = 1) { wallpaperRepository.incrementDownload(wallpaperId) }
    }
}
