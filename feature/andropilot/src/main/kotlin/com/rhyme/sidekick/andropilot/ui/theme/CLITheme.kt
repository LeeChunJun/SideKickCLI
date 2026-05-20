package com.rhyme.sidekick.andropilot.ui.theme

import androidx.compose.runtime.*
import com.rhyme.sidesick.ui.common.CLIColor
import com.rhyme.sidesick.ui.common.CLISpace

/**
 * 颜色主题
 */
val LocalCLIColorScheme: ProvidableCompositionLocal<CLIColorTheme> = staticCompositionLocalOf {
    CLIColorTheme.dark()
}

/**
 * 颜色
 */
val LocalCLIColor: ProvidableCompositionLocal<CLIColor> = staticCompositionLocalOf { CLIColor }

/**
 * 尺寸大小
 */
val LocalCLISpace: ProvidableCompositionLocal<CLISpace> = staticCompositionLocalOf { CLISpace }

/**
 * CLI 主题
 */
@Composable
fun CLITheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = remember(darkTheme) {
        if (darkTheme) CLIColorTheme.dark() else CLIColorTheme.light()
    }
    CompositionLocalProvider(
        LocalCLIColorScheme provides colorScheme,
        LocalCLIColor provides CLIColor,
        LocalCLISpace provides CLISpace,
        content = content
    )
}

/**
 * 主题单例对象
 */
object CLITheme {

    /**
     * 在组合层级中调用颜色模式
     */
    val colorScheme: CLIColorTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalCLIColorScheme.current

    /**
     * 在组合层级中调用间距大小
     */
    val space: CLISpace
        @Composable
        @ReadOnlyComposable
        get() = LocalCLISpace.current

    /**
     * 在组合层级中调用颜色
     */
    val color: CLIColor
        @Composable
        @ReadOnlyComposable
        get() = LocalCLIColor.current
}