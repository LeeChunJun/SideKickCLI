package com.rhyme.sidesick.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle
import com.rhyme.sidesick.ui.common.CLIColor

/**
 * 长文本列表组件（精确计算版）
 *
 * 核心策略：
 * 1. 预先计算每行原始文本经过 wrapTextInternal 后的实际显示行数
 * 2. 基于实际总显示行数来计算滚动位置，确保不超出终端边界
 * 3. 精确定位到目标百分比对应的原始行索引
 *
 * @param text 要显示的完整文本内容（原始字符串）
 * @param maxWidth 每行最大字符数（传递给WrappingText进行换行处理）
 * @param scrollPercentage 滚动百分比（0.0-1.0），表示当前视口在全文中的位置
 * @param visibleLines 终端可用高度（由外层传入）
 * @param lineColor 文本颜色
 * @param lineTextStyle 文本样式
 * @param modifier 修饰符
 * @param emptyText 当没有内容时显示的提示文本
 */
@Composable
fun LongTextList(
    text: String,
    maxWidth: Int,
    scrollPercentage: Float = 0f,
    visibleLines: Int = 12,
    lineColor: Color = Color.White,
    lineTextStyle: TextStyle = TextStyle.Empty,
    modifier: Modifier = Modifier,
    emptyText: String = "No content available."
) {
    if (text.isEmpty()) {
        Text(
            value = emptyText,
            color = lineColor,
            textStyle = lineTextStyle,
            modifier = modifier
        )
        return
    }

    // 【步骤1】将原始文本按换行符分割为行列表
    val originalLines = text.lines()

    // 【步骤2】计算每行原始文本经过自动换行后的实际显示行数
    // 例如：一行50字符的文本在maxWidth=20时会折成3行显示
    val wrappedLineCounts = originalLines.map { if (it.isEmpty()) 1 else wrapTextInternal(it, maxWidth).lines().size }

    // 【步骤3】计算所有原始行折行后的总显示行数
    val totalWrappedLines = wrappedLineCounts.sum()

    // 【步骤4】规范化滚动百分比到 [0.0, 1.0] 范围
    val normalizedPercentage = scrollPercentage.coerceIn(0f, 1f)
    
    // 【步骤5】计算目标起始位置（以折行后的行为单位）
    // 公式：目标位置 = 百分比 × (总行数 - 可见行数)
    // coerceAtLeast(0) 确保当内容不足一屏时不会出现负数
    val targetStartWrappedLine = (normalizedPercentage * (totalWrappedLines - visibleLines).coerceAtLeast(0)).toInt()

    // 【步骤6】根据目标折行位置，反推对应的原始行索引
    // 累计已遍历的折行数
    var accumulatedLines = 0
    // 起始原始行索引
    var startIndex = 0
    for (i in wrappedLineCounts.indices) {
        // 当累计折行数超过目标位置时，找到起始行
        if (accumulatedLines + wrappedLineCounts[i] > targetStartWrappedLine) {
            startIndex = i
            break
        }
        accumulatedLines += wrappedLineCounts[i]
    }

    // 【步骤7】先尝试用全部可见行数来计算能显示多少内容
    // 结束原始行索引（不包含）
    var endIndex = startIndex
    // 已渲染的折行数
    var renderedLines = 0

    // 【步骤8】从startIndex开始，逐行累加折行数，直到达到可见行数上限
    for (i in startIndex until originalLines.size) {
        if (renderedLines + wrappedLineCounts[i] <= visibleLines) {
            renderedLines += wrappedLineCounts[i]
            endIndex = i + 1
        } else {
            break  // 超出可视区域，停止
        }
    }

    // 【步骤9】判断是否需要显示"... more lines"提示
    val hasMoreLines = endIndex < originalLines.size
    
    // 【步骤10】如果有更多内容且当前显示已满，则需要腾出1行给提示
    if (hasMoreLines && renderedLines >= visibleLines) {
        // 回退最后一行，为提示腾出空间
        endIndex--
    }

    // 【步骤11】渲染可见范围内的原始行（WrappingText会自动处理折行）
    Column(modifier = modifier) {
        for (i in startIndex until endIndex) {
            WrappingText(value = originalLines[i], maxWidth = maxWidth, color = lineColor, textStyle = lineTextStyle)
        }

        // 【步骤12】如果还有未显示的行，显示提示
        if (hasMoreLines) {
            Text(
                value = "  ... (${originalLines.size - endIndex} more lines)",
                color = CLIColor.BrightBlack,
                textStyle = TextStyle.Italic
            )
        }
    }
}