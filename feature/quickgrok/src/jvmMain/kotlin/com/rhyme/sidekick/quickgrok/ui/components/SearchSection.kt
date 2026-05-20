package com.rhyme.sidekick.quickgrok.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rhyme.sidekick.quickgrok.domain.model.SearchFilterType
import com.rhyme.sidekick.quickgrok.presentation.QuickGrokUiEvent
import com.rhyme.sidekick.quickgrok.presentation.QuickGrokUiState

/**
 * 搜索区域组件
 */
@Composable
fun SearchSection(
    uiState: QuickGrokUiState,
    onEvent: (QuickGrokUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery = if (uiState is QuickGrokUiState.Success) {
        uiState.searchQuery
    } else {
        ""
    }
    
    val selectedFilter = if (uiState is QuickGrokUiState.Success) {
        uiState.selectedFilter
    } else {
        SearchFilterType.FULL_SEARCH
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 搜索输入框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { onEvent(QuickGrokUiEvent.SearchQueryChanged(it)) },
                placeholder = { 
                    Text(
                        "Search code, symbols, or definitions...",
                        color = Color(0xFF999999),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF999999),
                        modifier = Modifier.size(20.dp)
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge
            )
            
            // 过滤器选择栏
            FilterRibbon(
                selectedFilter = selectedFilter,
                onFilterSelect = { onEvent(QuickGrokUiEvent.SelectFilter(it)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 搜索按钮
            Button(
                onClick = { onEvent(QuickGrokUiEvent.ExecuteSearch) },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp, top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2a52be)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Search")
            }
        }
    }
}

/**
 * 过滤器选择栏
 */
@Composable
fun FilterRibbon(
    selectedFilter: SearchFilterType,
    onFilterSelect: (SearchFilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterPill(
            label = "Full Search",
            isSelected = selectedFilter == SearchFilterType.FULL_SEARCH,
            onClick = { onFilterSelect(SearchFilterType.FULL_SEARCH) }
        )
        FilterPill(
            label = "Definition",
            isSelected = selectedFilter == SearchFilterType.DEFINITION,
            onClick = { onFilterSelect(SearchFilterType.DEFINITION) }
        )
        FilterPill(
            label = "Symbol",
            isSelected = selectedFilter == SearchFilterType.SYMBOL,
            onClick = { onFilterSelect(SearchFilterType.SYMBOL) }
        )
        FilterPill(
            label = "File Path",
            isSelected = selectedFilter == SearchFilterType.FILE_PATH,
            onClick = { onFilterSelect(SearchFilterType.FILE_PATH) }
        )
        FilterPill(
            label = "History",
            isSelected = selectedFilter == SearchFilterType.HISTORY,
            onClick = { onFilterSelect(SearchFilterType.HISTORY) }
        )
    }
}

/**
 * 过滤器按钮
 */
@Composable
fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        Color(0xFF2a52be)
    } else {
        Color(0xFFf5f7fa)
    }
    
    val textColor = if (isSelected) {
        Color.White
    } else {
        Color(0xFF666666)
    }
    
    val borderColor = if (isSelected) {
        Color(0xFF2a52be)
    } else {
        Color(0xFFe0e0e0)
    }
    
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}
