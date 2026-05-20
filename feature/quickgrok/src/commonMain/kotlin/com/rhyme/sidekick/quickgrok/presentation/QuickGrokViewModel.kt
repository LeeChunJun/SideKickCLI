package com.rhyme.sidekick.quickgrok.presentation

import com.rhyme.sidekick.cmdx.viewmodel.common.viewModelScope
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModel
import com.rhyme.sidekick.quickgrok.domain.model.SearchFilterType
import com.rhyme.sidekick.quickgrok.domain.model.SearchRequest
import com.rhyme.sidekick.quickgrok.domain.usecase.GetProjectsUseCase
import com.rhyme.sidekick.quickgrok.domain.usecase.SearchCodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * QuickGrok ViewModel - MVI模式
 */
abstract class BaseQuickGrokViewModel(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val searchCodeUseCase: SearchCodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuickGrokUiState>(QuickGrokUiState.Initial)
    val uiState: StateFlow<QuickGrokUiState> = _uiState.asStateFlow()
    
    /**
     * 处理UI事件
     */
    fun onEvent(event: QuickGrokUiEvent) {
        when (event) {
            is QuickGrokUiEvent.SearchQueryChanged -> {
                updateSearchQuery(event.query)
            }
            is QuickGrokUiEvent.ExecuteSearch -> {
                executeSearch()
            }
            is QuickGrokUiEvent.SelectFilter -> {
                selectFilter(event.filter)
            }
            is QuickGrokUiEvent.ToggleProject -> {
                toggleProject(event.projectName)
            }
            is QuickGrokUiEvent.LoadProjects -> {
                loadProjects()
            }
        }
    }
    
    /**
     * 更新搜索查询
     */
    private fun updateSearchQuery(query: String) {
        val currentState = _uiState.value
        if (currentState is QuickGrokUiState.Success) {
            _uiState.value = currentState.copy(searchQuery = query)
        } else {
            _uiState.value = QuickGrokUiState.Success(searchQuery = query)
        }
    }
    
    /**
     * 执行搜索
     */
    private fun executeSearch() {
        viewModelScope.launch {
            // 先获取当前状态，再设置Loading
            val currentState = _uiState.value
            val query = if (currentState is QuickGrokUiState.Success) {
                currentState.searchQuery
            } else {
                ""
            }
            val filter = if (currentState is QuickGrokUiState.Success) {
                currentState.selectedFilter
            } else {
                SearchFilterType.FULL_SEARCH
            }
            val selectedProjects = if (currentState is QuickGrokUiState.Success) {
                currentState.selectedProjects
            } else {
                emptyList()
            }
            
            _uiState.value = QuickGrokUiState.Loading
            
            try {
                val request = SearchRequest(
                    query = query,
                    filterType = filter,
                    projects = selectedProjects,
                    maxResults = 100
                )
                
                val result = searchCodeUseCase(request)
                result.onSuccess { searchResults ->
                    val projects = if (currentState is QuickGrokUiState.Success) {
                        currentState.projects
                    } else {
                        emptyList()
                    }
                    
                    _uiState.value = QuickGrokUiState.Success(
                        projects = projects,
                        searchResults = searchResults,
                        searchQuery = query,
                        selectedFilter = filter,
                        selectedProjects = selectedProjects
                    )
                }.onFailure { error ->
                    _uiState.value = QuickGrokUiState.Error("搜索失败: ${error.message}")
                }
            } catch (e: Exception) {
                _uiState.value = QuickGrokUiState.Error("搜索异常: ${e.message}")
            }
        }
    }
    
    /**
     * 选择过滤器
     */
    private fun selectFilter(filter: SearchFilterType) {
        val currentState = _uiState.value
        if (currentState is QuickGrokUiState.Success) {
            _uiState.value = currentState.copy(selectedFilter = filter)
        }
    }
    
    /**
     * 切换项目选择
     */
    private fun toggleProject(projectName: String) {
        val currentState = _uiState.value
        if (currentState is QuickGrokUiState.Success) {
            val currentSelected = currentState.selectedProjects.toMutableList()
            if (currentSelected.contains(projectName)) {
                currentSelected.remove(projectName)
            } else {
                currentSelected.add(projectName)
            }
            _uiState.value = currentState.copy(selectedProjects = currentSelected)
        }
    }
    
    /**
     * 加载项目列表
     */
    private fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = QuickGrokUiState.Loading
            
            try {
                val result = getProjectsUseCase()
                result.onSuccess { projects ->
                    val currentState = _uiState.value
                    val searchResults = if (currentState is QuickGrokUiState.Success) {
                        currentState.searchResults
                    } else {
                        emptyList()
                    }
                    val searchQuery = if (currentState is QuickGrokUiState.Success) {
                        currentState.searchQuery
                    } else {
                        ""
                    }
                    val selectedFilter = if (currentState is QuickGrokUiState.Success) {
                        currentState.selectedFilter
                    } else {
                        SearchFilterType.FULL_SEARCH
                    }
                    val selectedProjects = if (currentState is QuickGrokUiState.Success) {
                        currentState.selectedProjects
                    } else {
                        emptyList()
                    }
                    
                    _uiState.value = QuickGrokUiState.Success(
                        projects = projects,
                        searchResults = searchResults,
                        searchQuery = searchQuery,
                        selectedFilter = selectedFilter,
                        selectedProjects = selectedProjects
                    )
                }.onFailure { error ->
                    _uiState.value = QuickGrokUiState.Error("加载项目列表失败: ${error.message}")
                }
            } catch (e: Exception) {
                _uiState.value = QuickGrokUiState.Error("加载项目列表异常: ${e.message}")
            }
        }
    }
}
