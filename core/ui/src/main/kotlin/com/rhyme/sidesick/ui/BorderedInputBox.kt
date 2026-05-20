package com.rhyme.sidesick.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.text.withStyle
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle
import com.rhyme.sidesick.ui.common.CLIColor
import com.rhyme.sidesick.ui.common.CLISpace
import com.rhyme.sidesick.ui.common.cellWidth

/**
 * 带边框的输入框组件（支持 CJK 字符宽度）
 *
 * 问题：Mosaic 框架按字符数计算宽度，而 CJK 字符在终端中占用 2 个单元格。
 * 解决：手动计算单元格宽度并构建边框字符串。
 *
 * 这是一个受控组件，所有的状态（value、focus）都由外部控制。
 *
 * @param modifier 修饰符
 * @param value 当前输入的值（由外部控制）
 * @param placeholder 当无输入内容时的提示文字
 * @param focus 是否处于聚焦状态（由外部控制）
 * @param cursorPosition 光标位置（0 到 value.length），仅在 focus=true 时有效
 * @param textColorPair 文本颜色对（分别对应未聚焦和聚焦状态）
 * @param borderColorPair 边框颜色对（分别对应未聚焦和聚焦状态）
 * @param minWidth 最小尺寸宽度
 * @param maxWidth 最大尺寸宽度
 */
@Composable
fun BorderedInputBox(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String = "Search...",
    focus: Boolean = false,
    cursorPosition: Int = 0,
    textColorPair: Pair<Color, Color> = Pair(CLIColor.White, CLIColor.BrightWhite),
    borderColorPair: Pair<Color, Color> = Pair(CLIColor.White, CLIColor.BrightYellow),
    minWidth: Int = 20,
    maxWidth: Int? = null,
) {
    // 根据是否聚焦决定边框颜色和文字样式
    val borderColor = if (focus) borderColorPair.second else borderColorPair.first
    val textColor = if (focus) textColorPair.second else textColorPair.first
    val textStyle = if (value.isNotEmpty()) TextStyle.Empty else TextStyle.Italic

    val displayText = value.ifEmpty { placeholder }
    
    // 确保光标位置在有效范围内
    val validCursorPosition = if (focus) cursorPosition.coerceIn(0, value.length) else 0
    
    // 计算内容的实际单元格宽度（CJK 字符占 2 格）
    // 如果聚焦且光标在末尾，需要额外加上光标的宽度（1个字符）
    val actualDisplayText = if (focus && value.isNotEmpty() && validCursorPosition == value.length) {
        "$value " // 末尾光标占用一个空格
    } else {
        displayText
    }
    val contentCellWidth = actualDisplayText.cellWidth()
    // 加上左右内边距（各 1 列）
    val paddingWidth = CLISpace.xs
    var innerWidth = contentCellWidth + paddingWidth * 2
    
    // 应用最小宽度限制
    innerWidth = maxOf(innerWidth, minWidth)
    
    // 应用最大宽度限制（如果指定了最大值）
    if (maxWidth != null) {
        innerWidth = minOf(innerWidth, maxWidth)
    }

    // 构建带颜色的边框字符串
    val annotatedString = buildAnnotatedString {
        // Top border with rounded corners
        withStyle(SpanStyle(borderColor)) {
            append('╭')
            append("─".repeat(innerWidth))
            append('╮')
        }
        append('\n')

        // Content line with left and right borders
        withStyle(SpanStyle(borderColor)) {
            append('│')
        }
        append(" ".repeat(paddingWidth))
        
        // Display text with cursor support
        if (focus && value.isNotEmpty()) {
            // 分割文本为光标前和光标后两部分
            val beforeCursor = value.substring(0, validCursorPosition)
            val afterCursor = value.substring(validCursorPosition)
            
            // 光标前的文本
            withStyle(SpanStyle(color = textColor, textStyle = textStyle)) {
                append(beforeCursor)
            }
            
            // 光标位置显示一个块状光标（使用反向背景色）
            if (validCursorPosition < value.length) {
                // 光标在字符上：高亮显示当前字符
                val currentChar = afterCursor.first().toString()
                withStyle(SpanStyle(color = CLIColor.Black, background = textColor)) {
                    append(currentChar)
                }
                // 剩余文本
                withStyle(SpanStyle(color = textColor, textStyle = textStyle)) {
                    append(afterCursor.drop(1))
                }
            } else {
                // 光标在末尾：显示一个块状光标
                withStyle(SpanStyle(color = CLIColor.Black, background = textColor)) {
                    append(' ')
                }
            }
        } else {
            // 非聚焦状态或空文本，正常显示
            withStyle(SpanStyle(color = textColor, textStyle = textStyle)) {
                append(displayText)
            }
        }
        
        // Right padding to align border
        val rightPadding = innerWidth - paddingWidth - contentCellWidth
        append(" ".repeat(rightPadding))
        withStyle(SpanStyle(borderColor)) {
            append('│')
        }
        append('\n')

        // Bottom border with rounded corners
        withStyle(SpanStyle(borderColor)) {
            append('╰')
            append("─".repeat(innerWidth))
            append('╯')
        }
    }

    Text(
        value = annotatedString,
        modifier = modifier,
    )
}
