package com.rhyme.sidekick.quickgrok.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.rhyme.sidekick.quickgrok.domain.model.SearchResultItem
import com.rhyme.sidekick.quickgrok.presentation.QuickGrokUiState
import com.rhyme.sidekick.quickgrok.domain.model.CodeLine

/**
 * 搜索结果区域
 */
@Composable
fun ResultsSection(
    uiState: QuickGrokUiState,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is QuickGrokUiState.Initial -> {
            Box(
                modifier = modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Enter a search query and press Search to begin",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6b6b70)
                )
            }
        }
        
        is QuickGrokUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is QuickGrokUiState.Success -> {
            if (uiState.searchResults.isEmpty()) {
                Box(
                    modifier = modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No results found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF6b6b70)
                    )
                }
            } else {
                LazyColumn(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Found ${uiState.searchResults.size} result(s)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(uiState.searchResults) { result ->
                        SearchResultCard(result = result)
                    }
                }
            }
        }
        
        is QuickGrokUiState.Error -> {
            Box(
                modifier = modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 单个搜索结果卡片
 */
@Composable
fun SearchResultCard(
    result: SearchResultItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFf8f9fa)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 结果头部 - 文件路径和项目名
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.filePath,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF2a52be)
                )
                
                Text(
                    text = result.projectName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF666666),
                    modifier = Modifier
                        .background(
                            Color(0xFFe8e8e8),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            // 分隔线
            Divider(color = Color(0xFFe0e0e0), thickness = 1.dp)
            
            // 代码片段
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                result.lines.forEach { codeLine ->
                    CodeLineItem(codeLine = codeLine)
                }
            }
        }
    }
}

/**
 * 单行代码展示
 */
@Composable
fun CodeLineItem(
    codeLine: CodeLine,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 行号
        Text(
            text = codeLine.lineNumber,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF999999),
            modifier = Modifier.width(40.dp)
        )
        
        // 代码内容
        Text(
            text = codeLine.content,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF333333)
        )
    }
}
