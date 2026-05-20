package com.rhyme.sidekick.dictionary.presentation

import com.rhyme.sidekick.dictionary.domain.model.Word

/**
 * UI状态
 */
sealed interface DictionaryUiState {
    object Initial : DictionaryUiState
    object Loading : DictionaryUiState
    data class Success(
        val searchResults: List<String> = emptyList(),
        val selectedWord: Word? = null,
        val searchQuery: String = ""
    ) : DictionaryUiState
    data class Error(val message: String) : DictionaryUiState
}

/**
 * UI事件（用户操作）
 */
sealed interface DictionaryUiEvent {
    data class SearchQueryChanged(val query: String) : DictionaryUiEvent
    object SearchWords : DictionaryUiEvent
    data class SelectWord(val word: String) : DictionaryUiEvent
    object ClearSelection : DictionaryUiEvent
}
