package com.rhyme.sidekick.quickgrok.presentation

import com.rhyme.sidekick.quickgrok.domain.model.ProjectInfo
import com.rhyme.sidekick.quickgrok.domain.model.SearchFilterType
import com.rhyme.sidekick.quickgrok.domain.model.SearchResultItem

/**
 * UI状态
 */
sealed interface QuickGrokUiState {
    object Initial : QuickGrokUiState
    object Loading : QuickGrokUiState
    data class Success(
        val projects: List<ProjectInfo> = emptyList(),
        val searchResults: List<SearchResultItem> = emptyList(),
        val searchQuery: String = "",
        val selectedFilter: SearchFilterType = SearchFilterType.FULL_SEARCH,
        val selectedProjects: List<String> = emptyList()
    ) : QuickGrokUiState
    data class Error(val message: String) : QuickGrokUiState
}

/**
 * UI事件（用户操作）
 */
sealed interface QuickGrokUiEvent {
    data class SearchQueryChanged(val query: String) : QuickGrokUiEvent
    object ExecuteSearch : QuickGrokUiEvent
    data class SelectFilter(val filter: SearchFilterType) : QuickGrokUiEvent
    data class ToggleProject(val projectName: String) : QuickGrokUiEvent
    object LoadProjects : QuickGrokUiEvent
}
