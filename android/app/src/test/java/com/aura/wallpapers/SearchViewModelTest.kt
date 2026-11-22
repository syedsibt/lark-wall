package com.aura.wallpapers

import com.aura.wallpapers.data.local.dao.SearchDao
import com.aura.wallpapers.data.local.entity.SearchHistoryEntity
import com.aura.wallpapers.domain.model.LiveWallpaper
import com.aura.wallpapers.domain.model.Wallpaper
import com.aura.wallpapers.domain.repository.LiveWallpaperRepository
import com.aura.wallpapers.domain.repository.WallpaperRepository
import com.aura.wallpapers.ui.search.SearchFilter
import com.aura.wallpapers.ui.search.SearchViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val wallpaperRepository: WallpaperRepository = mockk(relaxed = true)
    private val liveWallpaperRepository: LiveWallpaperRepository = mockk(relaxed = true)
    private val searchDao: SearchDao = mockk(relaxed = true)

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { searchDao.getRecentSearches() } returns flowOf(
            listOf(SearchHistoryEntity(query = "cyberpunk", searchedAt = 1000L))
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads recent searches correctly`() = runTest(testDispatcher) {
        viewModel = SearchViewModel(wallpaperRepository, liveWallpaperRepository, searchDao)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("cyberpunk"), state.recentSearches)
        assertEquals(SearchFilter.ALL, state.activeFilter)
        assertFalse(state.isSearching)
    }

    @Test
    fun `when filter is changed, uiState reflects active filter`() = runTest(testDispatcher) {
        viewModel = SearchViewModel(wallpaperRepository, liveWallpaperRepository, searchDao)
        advanceUntilIdle()

        viewModel.setFilter(SearchFilter.LIVE)
        assertEquals(SearchFilter.LIVE, viewModel.uiState.value.activeFilter)

        viewModel.setFilter(SearchFilter.STATIC)
        assertEquals(SearchFilter.STATIC, viewModel.uiState.value.activeFilter)
    }

    @Test
    fun `executeSearch combines static and live results without deadlock`() = runTest(testDispatcher) {
        val staticWp = Wallpaper(
            id = "w1",
            title = "Cyber Skyline",
            slug = "cyber-skyline",
            description = "Neon city",
            categoryId = "c1",
            categoryName = "Cyberpunk",
            tags = listOf("neon", "cyberpunk"),
            thumbnailUrl = "https://example.com/thumb.webp",
            previewUrl = "https://example.com/prev.webp",
            originalUrl = "https://example.com/orig.webp",
            width = 1080,
            height = 1920,
            aspectRatio = "9:16",
            fileSizeBytes = 500000L,
            format = "WEBP",
            downloadCount = 100L,
            favoriteCount = 20L,
            isFeatured = true,
            isPremium = false,
            licenseType = "Free",
            authorName = "Artist"
        )
        val liveWp = LiveWallpaper(
            id = "l1",
            title = "Aurora Drift",
            slug = "aurora-drift",
            description = "Loop",
            categoryId = "c2",
            categoryName = "Motion Loops",
            tags = listOf("aurora", "loop"),
            thumbnailUrl = "asset:///thumb.webp",
            previewVideoUrl = "engine://interactive/ml_01",
            originalVideoUrl = "engine://interactive/ml_01",
            width = 1080,
            height = 1920,
            durationSeconds = 60.0,
            fileSizeBytes = 40000L,
            downloadCount = 200L,
            isFavorite = false
        )

        every { wallpaperRepository.searchWallpapers("cyber") } returns flowOf(listOf(staticWp))
        every { liveWallpaperRepository.searchLiveWallpapers("cyber") } returns flowOf(listOf(liveWp))

        viewModel = SearchViewModel(wallpaperRepository, liveWallpaperRepository, searchDao)
        advanceUntilIdle()

        viewModel.executeSearch("cyber")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSearching)
        assertEquals(1, state.staticResults.size)
        assertEquals("Cyber Skyline", state.staticResults[0].title)
        assertEquals(1, state.liveResults.size)
        assertEquals("Aurora Drift", state.liveResults[0].title)
        coVerify(exactly = 1) { searchDao.insertSearchQuery(any()) }
    }

    @Test
    fun `clearHistory invokes dao clearSearchHistory`() = runTest(testDispatcher) {
        viewModel = SearchViewModel(wallpaperRepository, liveWallpaperRepository, searchDao)
        advanceUntilIdle()

        viewModel.clearHistory()
        advanceUntilIdle()

        coVerify(exactly = 1) { searchDao.clearSearchHistory() }
    }
}
