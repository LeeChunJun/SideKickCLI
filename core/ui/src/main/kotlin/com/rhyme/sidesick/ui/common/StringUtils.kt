package com.rhyme.sidesick.ui.common

/**
 * 计算字符串的终端显示宽度（单元格数）
 * 
 * 在终端中，不同字符占用的宽度不同：
 * - ASCII 字符（英文、数字、符号）：1 个单元格
 * - CJK 字符（中文、日文、韩文）：2 个单元格
 * - 全角字符：2 个单元格
 * - 组合字符：0 个单元格
 * 
 * @param text 要计算宽度的字符串
 * @return 字符串在终端中占用的单元格数量
 */
fun String.cellWidth(): Int {
    return this.sumOf { char ->
        when {
            // 控制字符不可见
            char < ' ' -> 0
            
            // 组合字符（附加在基础字符上）
            Character.getType(char) == Character.NON_SPACING_MARK.toInt() -> 0
            Character.getType(char) == Character.COMBINING_SPACING_MARK.toInt() -> 0
            Character.getType(char) == Character.ENCLOSING_MARK.toInt() -> 0
            
            // CJK 统一表意文字
            char in '\u4e00'..'\u9fff' -> 2  // CJK Unified Ideographs
            char in '\u3400'..'\u4dbf' -> 2  // CJK Unified Ideographs Extension A
            
            // CJK 符号和标点
            char in '\u3000'..'\u303f' -> 2  // CJK Symbols and Punctuation
            
            // CJK 兼容字符
            char in '\uf900'..'\ufaff' -> 2  // CJK Compatibility Ideographs
            char in '\ufe30'..'\ufe6f' -> 2  // CJK Compatibility Forms
            
            // 全角字符（Fullwidth Forms）
            char in '\uff00'..'\uffef' -> 2  // Halfwidth and Fullwidth Forms
            
            // 平假名和片假名
            char in '\u3040'..'\u309f' -> 2  // Hiragana
            char in '\u30a0'..'\u30ff' -> 2  // Katakana
            
            // 谚文（韩文）
            char in '\uac00'..'\ud7af' -> 2  // Hangul Syllables
            char in '\u1100'..'\u11ff' -> 2  // Hangul Jamo
            
            // 其他双宽字符区域
            char in '\u2e80'..'\u2eff' -> 2  // CJK Radicals Supplement
            char in '\u2f00'..'\u2fdf' -> 2  // Kangxi Radicals
            
            // 半角片假名（占用1格）
            char in '\uff65'..'\uff9f' -> 1  // Halfwidth Katakana
            
            // 默认：其他字符假设为单宽
            else -> 1
        }
    }
}

/**
 * 将字符串填充到指定单元格宽度
 * 
 * @param targetWidth 目标单元格宽度
 * @param padChar 填充字符（默认为空格）
 * @return 填充后的字符串
 */
fun String.padToCellWidth(targetWidth: Int, padChar: Char = ' '): String {
    val currentWidth = this.cellWidth()
    val paddingNeeded = targetWidth - currentWidth
    
    if (paddingNeeded <= 0) {
        return this
    }
    
    return this + padChar.toString().repeat(paddingNeeded)
}

/**
 * 截断字符串到指定单元格宽度
 * 
 * @param maxWidth 最大单元格宽度
 * @return 截断后的字符串（如果需要截断会添加 "..."）
 */
fun String.truncateToCellWidth(maxWidth: Int): String {
    if (this.cellWidth() <= maxWidth) {
        return this
    }
    
    var currentWidth = 0
    val result = StringBuilder()
    
    for (char in this) {
        val charWidth = char.toString().cellWidth()
        if (currentWidth + charWidth > maxWidth) {
            break
        }
        result.append(char)
        currentWidth += charWidth
    }
    
    // 添加省略号
    val ellipsisWidth = 3  // "..." 占 3 个单元格
    if (result.length + ellipsisWidth <= maxWidth) {
        result.append("...")
    }
    
    return result.toString()
}
