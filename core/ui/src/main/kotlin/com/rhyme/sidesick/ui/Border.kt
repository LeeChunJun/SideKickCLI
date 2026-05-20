package com.rhyme.sidesick.ui

import androidx.compose.runtime.Stable
import com.jakewharton.mosaic.layout.ContentDrawScope
import com.jakewharton.mosaic.layout.DrawModifier
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color

/**
 * 为组件添加边框
 *
 * 注意：此修饰符按字符数计算宽度。如果内容包含 CJK 字符（中文、日文、韩文等），
 * 由于 CJK 字符在终端中占用 2 个单元格宽度，边框位置可能会不正确。
 *
 * 对于需要支持 CJK 字符的场景，建议使用以下方案之一：
 * 1. 使用 BorderedInputBox 组件（针对输入框场景）
 * 2. 手动构建边框字符串，参考 BorderedInputBox 的实现
 * 3. 确保内容只包含单宽字符（ASCII）
 *
 * @param topStart 左上角字符
 * @param topEnd 右上角字符
 * @param bottomStart 左下角字符
 * @param bottomEnd 右下角字符
 * @param verticalStart 左边框字符
 * @param verticalEnd 右边框字符
 * @param horizontalTop 上边框字符
 * @param horizontalBottom 下边框字符
 * @param color 边框颜色
 */
@Stable
fun Modifier.border(
	topStart: Char = '┌',
	topEnd: Char = '┐',
	bottomStart: Char = '└',
	bottomEnd: Char = '┘',
	verticalStart: Char = '│',
	verticalEnd: Char = '│',
	horizontalTop: Char = '─',
	horizontalBottom: Char = '─',
	color: Color = Color.Unspecified,
): Modifier = this.then(
	BorderModifier(
		topStart.toString(),
		topEnd.toString(),
		bottomStart.toString(),
		bottomEnd.toString(),
		verticalStart.toString(),
		verticalEnd.toString(),
		horizontalTop.toString(),
		horizontalBottom.toString(),
		color,
	),
).padding(all = 1)

private class BorderModifier(
	private val topStart: String,
	private val topEnd: String,
	private val bottomStart: String,
	private val bottomEnd: String,
	private val verticalStart: String,
	private val verticalEnd: String,
	private val horizontalTop: String,
	private val horizontalBottom: String,
	private val color: Color,
) : DrawModifier {

	override fun ContentDrawScope.draw() {
		drawText(string = topStart, row = 0, column = 0, foreground = color)
		drawText(string = topEnd, row = 0, column = width - 1, foreground = color)
		drawText(string = bottomEnd, row = height - 1, column = width - 1, foreground = color)
		drawText(string = bottomStart, row = height - 1, column = 0, foreground = color)
		drawText(string = horizontalTop.repeat(width - 2), row = 0, column = 1, foreground = color)
		drawText(
			string = horizontalBottom.repeat(width - 2),
			row = height - 1,
			column = 1,
			foreground = color,
		)
		for (row in 1..height - 2) {
			drawText(string = verticalStart, row = row, column = 0, foreground = color)
			drawText(string = verticalEnd, row = row, column = width - 1, foreground = color)
		}
		drawContent()
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is BorderModifier) return false

		if (verticalStart != other.verticalStart) return false
		if (topStart != other.topStart) return false
		if (horizontalTop != other.horizontalTop) return false
		if (topEnd != other.topEnd) return false
		if (verticalEnd != other.verticalEnd) return false
		if (bottomEnd != other.bottomEnd) return false
		if (horizontalBottom != other.horizontalBottom) return false
		if (bottomStart != other.bottomStart) return false
		if (color != other.color) return false

		return true
	}

	override fun hashCode(): Int {
		var result = verticalStart.hashCode()
		result = 31 * result + topStart.hashCode()
		result = 31 * result + horizontalTop.hashCode()
		result = 31 * result + topEnd.hashCode()
		result = 31 * result + verticalEnd.hashCode()
		result = 31 * result + bottomEnd.hashCode()
		result = 31 * result + horizontalBottom.hashCode()
		result = 31 * result + bottomStart.hashCode()
		result = 31 * result + color.hashCode()
		return result
	}

	override fun toString() = "Border"
}
