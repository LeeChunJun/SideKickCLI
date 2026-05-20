package com.rhyme.sidesick.ui.common

import com.jakewharton.mosaic.ui.Color

/**
 * CLI 颜色
 *
 * Create a Mosaic Color from an ANSI 256 color index (0-255)
 */
object CLIColor {
    /**
     * 从ANSI 256色索引创建Mosaic Color
     * @param index ANSI 256色索引 (0-255)
     * @return 对应的Mosaic Color对象
     */
    fun fromAnsi256(index: Int): Color {
        require(index in 0..255) { "ANSI 256 color index must be between 0 and 255, got $index" }

        // ANSI 256色表映射到RGB值
        val (r, g, b) = when {
            index < 16 -> {
                // 标准16色
                when (index) {
                    0 -> Triple(0, 0, 0)         // Black
                    1 -> Triple(128, 0, 0)       // Red
                    2 -> Triple(0, 128, 0)       // Green
                    3 -> Triple(128, 128, 0)     // Yellow
                    4 -> Triple(0, 0, 128)       // Blue
                    5 -> Triple(128, 0, 128)     // Magenta
                    6 -> Triple(0, 128, 128)     // Cyan
                    7 -> Triple(192, 192, 192)   // White
                    8 -> Triple(128, 128, 128)   // Bright Black (Gray)
                    9 -> Triple(255, 0, 0)       // Bright Red
                    10 -> Triple(0, 255, 0)      // Bright Green
                    11 -> Triple(255, 255, 0)    // Bright Yellow
                    12 -> Triple(0, 0, 255)      // Bright Blue
                    13 -> Triple(255, 0, 255)    // Bright Magenta
                    14 -> Triple(0, 255, 255)    // Bright Cyan
                    15 -> Triple(255, 255, 255)  // Bright White
                    else -> throw IllegalStateException("Invalid standard color index: $index")
                }
            }

            index < 232 -> {
                // 6x6x6 颜色立方体 (16-231)
                val colorIndex = index - 16
                val red6Index = (colorIndex / 36) % 6
                val green6Index = (colorIndex / 6) % 6
                val blue6Index = colorIndex % 6

                // [0, 95, 135, 175, 215, 255]
                val channelValues = listOf(0, 55 + 1 * 40, 55 + 2 * 40, 55 + 3 * 40, 55 + 4 * 40, 55 + 5 * 40)

                Triple(
                    channelValues[red6Index],
                    channelValues[green6Index],
                    channelValues[blue6Index],
                )
            }

            else -> {
                // 灰度色阶 (232-255)
                val grayValue = 8 + (index - 232) * 10
                Triple(grayValue, grayValue, grayValue)
            }
        }

        return Color(r, g, b)
    }

    /**
     * 预定义的常用ANSI颜色
     */
    val Black = fromAnsi256(0)
    val Red = fromAnsi256(1)
    val Green = fromAnsi256(2)
    val Yellow = fromAnsi256(3)
    val Blue = fromAnsi256(4)
    val Magenta = fromAnsi256(5)
    val Cyan = fromAnsi256(6)
    val White = fromAnsi256(7)
    val BrightBlack = fromAnsi256(8)
    val BrightRed = fromAnsi256(9)
    val BrightGreen = fromAnsi256(10)
    val BrightYellow = fromAnsi256(11)
    val BrightBlue = fromAnsi256(12)
    val BrightMagenta = fromAnsi256(13)
    val BrightCyan = fromAnsi256(14)
    val BrightWhite = fromAnsi256(15)
}