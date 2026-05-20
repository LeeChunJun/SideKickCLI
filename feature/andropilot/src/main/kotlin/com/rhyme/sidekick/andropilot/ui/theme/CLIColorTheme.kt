package com.rhyme.sidekick.andropilot.ui.theme

import com.jakewharton.mosaic.ui.Color
import com.rhyme.sidesick.ui.common.CLIColor

/**
 * CLI 颜色主题
 *
 * Color scheme for CLI using ANSI 256 color palette
 */
data class CLIColorTheme(
    val primary: Color,
    val onPrimary: Color,
    val surface: Color,
    val onSurface: Color,
    val background: Color,
    val onBackground: Color,
    val divider: Color,
    val error: Color,
    val success: Color,
    val warning: Color
) {
    companion object {
        /**
         * 暗色颜色主题
         */
        fun dark(): CLIColorTheme = CLIColorTheme(
            primary = CLIColor.fromAnsi256(141),
            onPrimary = CLIColor.BrightWhite,
            surface = CLIColor.fromAnsi256(235),
            onSurface = CLIColor.fromAnsi256(252),
            background = CLIColor.fromAnsi256(232),
            onBackground = CLIColor.fromAnsi256(252),
            divider = CLIColor.fromAnsi256(240),
            error = CLIColor.fromAnsi256(196),
            success = CLIColor.fromAnsi256(46),
            warning = CLIColor.fromAnsi256(214)
        )

        /**
         * 亮色颜色主题
         */
        fun light(): CLIColorTheme = CLIColorTheme(
            primary = CLIColor.fromAnsi256(92),
            onPrimary = CLIColor.BrightWhite,
            surface = CLIColor.fromAnsi256(254),
            onSurface = CLIColor.fromAnsi256(235),
            background = CLIColor.BrightWhite,
            onBackground = CLIColor.fromAnsi256(235),
            divider = CLIColor.fromAnsi256(245),
            error = CLIColor.fromAnsi256(124),
            success = CLIColor.fromAnsi256(22),
            warning = CLIColor.fromAnsi256(130)
        )
    }
}