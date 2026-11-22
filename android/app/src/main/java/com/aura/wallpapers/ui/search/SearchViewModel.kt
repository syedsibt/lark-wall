package com.aura.wallpapers.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.wallpapers.data.local.dao.SearchDao
import com.aura.wallpapers.data.local.entity.SearchHistoryEntity
import com.aura.wallpapers.domain.model.LiveWallpaper
import com.aura.wallpapers.domain.model.Wallpaper
import com.aura.wallpapers.domain.repository.LiveWallpaperRepository
import com.aura.wallpapers.domain.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchFilter {
    ALL,
    STATIC,
    LIVE
}

data class SearchUiState(
    val query: String = "",
    val activeFilter: SearchFilter = SearchFilter.ALL,
    val isSearching: Boolean = false,
    val staticResults: List<Wallpaper> = emptyList(),
    val liveResults: List<LiveWallpaper> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val trendingTags: List<String> = listOf("amoled", "space", "minimal", "mountains", "neon", "cyberpunk", "nature", "4k", "dark")
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val liveWallpaperRepository: LiveWallpaperRepository,
    private val searchDao: SearchDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadRecentSearches()
        observeQuery()
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            searchDao.getRecentSearches().collect { list ->
                _uiState.update { it.copy(recentSearches = list.map { it.query }) }
            }
        }
    }

    private fun observeQuery() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collectLatest { query ->
                    executeSearch(query)
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchQueryFlow.value = newQuery
    }

    fun setFilter(filter: SearchFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    private var searchJob: Job? = null

    fun executeSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(isSearching = false, staticResults = emptyList(), liveResults = emptyList()) }
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            searchDao.insertSearchQuery(SearchHistoryEntity(trimmed))

            combine(
                wallpaperRepository.searchWallpapers(trimmed),
                liveWallpaperRepository.searchLiveWallpapers(trimmed)
            ) { staticList, liveList ->
                Pair(staticList, liveList)
            }.collect { (staticList, liveList) ->
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        staticResults = staticList,
                        liveResults = liveList
                    )
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchDao.clearSearchHistory()
        }
    }
}
