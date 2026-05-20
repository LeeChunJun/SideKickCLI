package com.rhyme.sidekick.dictionary.presentation

import com.rhyme.sidekick.cmdx.viewmodel.common.viewModelScope
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModel
import com.rhyme.sidekick.dictionary.domain.usecase.GetWordDetailUseCase
import com.rhyme.sidekick.dictionary.domain.usecase.SearchWordsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 词典ViewModel - MVI模式
 * 
 * 注意：由于数据源实现是平台特定的（jvmMain），
 * ViewModel的实际实例化在jvmMain中完成。
 */
abstract class BaseDictionaryViewModel(
    protected val searchWordsUseCase: SearchWordsUseCase,
    protected val getWordDetailUseCase: GetWordDetailUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<DictionaryUiState>(DictionaryUiState.Initial)
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()
    
    /**
     * 处理UI事件
     */
    fun onEvent(event: DictionaryUiEvent) {
        when (event) {
            is DictionaryUiEvent.SearchQueryChanged -> {
                updateSearchQuery(event.query)
            }
            is DictionaryUiEvent.SearchWords -> {
                searchWords()
            }
            is DictionaryUiEvent.SelectWord -> {
                selectWord(event.word)
            }
            is DictionaryUiEvent.ClearSelection -> {
                clearSelection()
            }
        }
    }
    
    private fun updateSearchQuery(query: String) {
        val currentState = _uiState.value
        if (currentState is DictionaryUiState.Success) {
            _uiState.value = currentState.copy(searchQuery = query)
        } else {
            _uiState.value = DictionaryUiState.Success(searchQuery = query)
        }
    }
    
    private fun searchWords() {
        viewModelScope.launch {
            // 先获取当前查询字符串，再设置Loading状态
            val currentState = _uiState.value
            val query = if (currentState is DictionaryUiState.Success) {
                currentState.searchQuery
            } else {
                ""
            }
            
            _uiState.value = DictionaryUiState.Loading
            
            try {
                val results = searchWordsUseCase(query)
                _uiState.value = DictionaryUiState.Success(
                    searchResults = results,
                    searchQuery = query
                )
            } catch (e: Exception) {
                _uiState.value = DictionaryUiState.Error("搜索失败: ${e.message}")
            }
        }
    }
    
    private fun selectWord(word: String) {
        viewModelScope.launch {
            _uiState.value = DictionaryUiState.Loading
            
            try {
                val result = getWordDetailUseCase(word)
                result.onSuccess { wordDetail ->
                    val currentState = _uiState.value
                    val searchResults = if (currentState is DictionaryUiState.Success) {
                        currentState.searchResults
                    } else {
                        emptyList()
                    }
                    val searchQuery = if (currentState is DictionaryUiState.Success) {
                        currentState.searchQuery
                    } else {
                        ""
                    }
                    
                    _uiState.value = DictionaryUiState.Success(
                        searchResults = searchResults,
                        selectedWord = wordDetail,
                        searchQuery = searchQuery
                    )
                }.onFailure { error ->
                    _uiState.value = DictionaryUiState.Error("查询单词详情失败: ${error.message}")
                }
            } catch (e: Exception) {
                _uiState.value = DictionaryUiState.Error("查询单词详情失败: ${e.message}")
            }
        }
    }
    
    private fun clearSelection() {
        val currentState = _uiState.value
        if (currentState is DictionaryUiState.Success) {
            _uiState.value = currentState.copy(selectedWord = null)
        }
    }
}
