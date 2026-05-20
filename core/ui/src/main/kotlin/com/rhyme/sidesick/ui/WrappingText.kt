package com.rhyme.sidesick.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.text.withStyle
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle
import com.jakewharton.mosaic.modifier.Modifier

/**
 * 支持自动换行的文本组件
 *
 * 该组件会根据指定的最大宽度对长文本进行自动换行处理，避免在终端中显示时出现溢出问题。
 * 使用buildAnnotatedString方式在内部进行append('\n')换行拼接。
 *
 * @param value 要显示的文本内容
 * @param maxWidth 每行最大字符数（会自动限制在合理范围内）
 * @param color 文本颜色
 * @param textStyle 文本样式
 * @param modifier 修饰符
 */
@Composable
fun WrappingText(
    value: String,
    maxWidth: Int,
    color: Color = Color.White,
    textStyle: TextStyle = TextStyle.Empty,
    modifier: Modifier = Modifier
) {
    // 确保宽度在合理范围内
    val safeMaxWidth = maxWidth.coerceIn(10, 500)  // 最小 10，最大 500

    if (value.isEmpty()) {
        Text(value = "", color = color, textStyle = textStyle, modifier = modifier)
        return
    }

    // 将文本按指定宽度进行换行处理
    val wrappedText = wrapTextInternal(value, safeMaxWidth)

    // 使用buildAnnotatedString构建带样式的文本
    val annotatedString = buildAnnotatedString {
        withStyle(SpanStyle(color = color, textStyle = textStyle)) {
            append(wrappedText)
        }
    }

    Text(
        value = annotatedString,
        modifier = modifier
    )
}

/**
 * 文本自动换行工具函数
 * 将长文本按指定宽度拆分成多行，优先在空格处断行
 * 对于超长单词（如URL），会强制在最大宽度处断行
 *
 * @param value 原始文本
 * @param maxWidth 每行最大字符数（会自动限制在合理范围内）
 * @return 换行后的文本（包含 \n 分隔符）
 */
fun wrapTextInternal(value: String, maxWidth: Int): String {
    // 【关键修复】：确保宽度在合理范围内
    val safeMaxWidth = maxWidth.coerceIn(10, 500)  // 最小 10，最大 500

    if (value.isEmpty()) {
        return ""  // 空文本直接返回
    }

    val lines = mutableListOf<String>()
    var currentLine = ""

    // 按空格分割单词
    val words = value.split(Regex("\\s+"))

    for (word in words) {
        if (word.isEmpty()) continue

        // 【关键修复】：处理超长单词（如URL）
        if (word.length > safeMaxWidth) {
            // 如果当前行有内容，先保存当前行
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = ""
            }

            // 将超长单词按最大宽度强制分割
            var remaining = word
            while (remaining.length > safeMaxWidth) {
                lines.add(remaining.substring(0, safeMaxWidth))
                remaining = remaining.substring(safeMaxWidth)
            }
            // 剩余部分作为新行的开始
            if (remaining.isNotEmpty()) {
                currentLine = remaining
            }
        } else if (currentLine.isEmpty()) {
            // 当前行为空，直接添加单词
            currentLine = word
        } else if ((currentLine.length + 1 + word.length) <= safeMaxWidth) {
            // 当前行加上新单词不超过最大宽度
            currentLine += " $word"
        } else {
            // 超出宽度，保存当前行并开始新行
            lines.add(currentLine)
            currentLine = word
        }
    }

    // 添加最后一行
    if (currentLine.isNotEmpty()) {
        lines.add(currentLine)
    }

    // 【防御性检查】：如果没有任何行，返回原文本
    return if (lines.isEmpty()) value else lines.joinToString("\n")
}