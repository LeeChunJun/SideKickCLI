package com.rhyme.sidekick.quickgrok.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rhyme.sidekick.quickgrok.domain.model.ProjectInfo

/**
 * 项目列表组件
 */
@Composable
fun ProjectList(
    projects: List<ProjectInfo>,
    selectedProjects: List<String>,
    onToggleProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(projects) { project ->
            ProjectItem(
                project = project,
                isSelected = selectedProjects.contains(project.name),
                onToggle = { onToggleProject(project.name) }
            )
        }
    }
}

/**
 * 单个项目项
 */
@Composable
fun ProjectItem(
    project: ProjectInfo,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dotColor = when (project.name) {
        "kernel-mainline" -> Color(0xFF2a52be)
        "android-common" -> Color(0xFFff4444)
        "llvm-project" -> Color(0xFF4f6ae5)
        "rust-lang" -> Color(0xFF4f6ae5)
        "chromium" -> Color(0xFFffaa00)
        else -> Color(0xFF4f6ae5)
    }
    
    val backgroundColor = if (isSelected) {
        Color(0xFF2a52be).copy(alpha = 0.08f)
    } else {
        Color.Transparent
    }
    
    val textColor = if (isSelected) {
        Color(0xFF2a52be)
    } else {
        Color(0xFF333333)
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 项目指示点
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = dotColor,
                    shape = CircleShape
                )
        )
        
        // 项目名称
        Text(
            text = project.name,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}
