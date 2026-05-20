package com.rhyme.sidekick.dictionary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.rhyme.sidekick.cmdx.viewmodel.common.CLIViewModelStoreOwner
import com.rhyme.sidekick.cmdx.viewmodel.common.LocalCLIViewModelStoreOwner
import com.rhyme.sidekick.cmdx.viewmodel.common.cliViewModel
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModelStore
import com.rhyme.sidekick.dictionary.presentation.DictionaryUiEvent
import com.rhyme.sidekick.dictionary.presentation.DictionaryUiState
import com.rhyme.sidekick.dictionary.presentation.DictionaryViewModel

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Dictionary - QuickSearch") {
        MaterialTheme {
            DictionaryRoot {
                DictionaryApp()
            }
        }
    }
}

@Composable
fun DictionaryRoot(content: @Composable () -> Unit) {
    val owner = remember { CLIViewModelStoreOwner() }
    
    CompositionLocalProvider(
        LocalCLIViewModelStoreOwner provides owner,
        content = content
    )
}

@Composable
fun DictionaryApp() {
    // 使用 cmdx 的 cliViewModel 获取 ViewModel 实例
    val viewModel: DictionaryViewModel = cliViewModel("dictionary") { ViewModelStore() }
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Text(
            text = "English Dictionary",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // 搜索区域
        SearchSection(
            uiState = uiState,
            onEvent = viewModel::onEvent
        )
        
        // 内容区域
        ContentSection(
            uiState = uiState,
            onEvent = viewModel::onEvent
        )
    }
}

@Composable
fun SearchSection(
    uiState: DictionaryUiState,
    onEvent: (DictionaryUiEvent) -> Unit
) {
    val searchQuery = if (uiState is DictionaryUiState.Success) {
        uiState.searchQuery
    } else {
        ""
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { onEvent(DictionaryUiEvent.SearchQueryChanged(it)) },
                label = { Text("Search word") },
                placeholder = { Text("Type to search...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Button(
                onClick = { onEvent(DictionaryUiEvent.SearchWords) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Search")
            }
        }
    }
}

@Composable
fun ContentSection(
    uiState: DictionaryUiState,
    onEvent: (DictionaryUiEvent) -> Unit
) {
    when (uiState) {
        is DictionaryUiState.Initial -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Enter a word and press Search to begin")
            }
        }
        
        is DictionaryUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is DictionaryUiState.Success -> {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 左侧：搜索结果列表
                SearchResultsList(
                    results = uiState.searchResults,
                    selectedWord = uiState.selectedWord?.text,
                    onWordSelect = { onEvent(DictionaryUiEvent.SelectWord(it)) },
                    modifier = Modifier.weight(1f)
                )
                
                // 右侧：单词详情
                WordDetailPanel(
                    word = uiState.selectedWord,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
        
        is DictionaryUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SearchResultsList(
    results: List<String>,
    selectedWord: String?,
    onWordSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Search Results (${results.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (results.isEmpty()) {
                Text(
                    text = "No results found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(results) { word ->
                        SearchResultItem(
                            word = word,
                            isSelected = word == selectedWord,
                            onClick = { onWordSelect(word) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    word: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Text(
            text = word,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun WordDetailPanel(
    word: com.rhyme.sidekick.dictionary.domain.model.Word?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Word Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (word == null) {
                Text(
                    text = "Select a word to view details",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                // 单词基本信息
                Text(
                    text = word.text,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                word.phonetic?.let { phonetic ->
                    Text(
                        text = "/$phonetic/",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
                
                word.origin?.let { origin ->
                    Divider()
                    Text(
                        text = "Origin:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = origin,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // 释义列表
                if (word.meanings.isNotEmpty()) {
                    Divider()
                    Text(
                        text = "Meanings:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    word.meanings.forEach { meaning ->
                        MeaningSection(meaning = meaning)
                    }
                }
            }
        }
    }
}

@Composable
fun MeaningSection(
    meaning: com.rhyme.sidekick.dictionary.domain.model.WordMeaning
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = meaning.partOfSpeech,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        meaning.definitions.forEachIndexed { index, definition ->
            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${index + 1}. ${definition.definition}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                definition.example?.let { example ->
                    Text(
                        text = "Example: \"$example\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                if (definition.synonyms.isNotEmpty()) {
                    Text(
                        text = "Synonyms: ${definition.synonyms.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
