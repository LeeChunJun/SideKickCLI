package com.rhyme.sidekick.andropilot.util

import androidx.compose.runtime.*
import com.jakewharton.mosaic.layout.onKeyEvent
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.runMosaicMain
import com.jakewharton.mosaic.ui.Text
import kotlinx.coroutines.awaitCancellation

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.andropilot.config
 *
 * @author 李春俊-Rhyme
 * @since 14:23 2026/5/12 星期二
 * @version 1.0
 */
fun main() = runMosaicMain {
    var message by remember { mutableStateOf("Press any key") }

    Text(message, modifier = Modifier.onKeyEvent { event ->
        // 打印详细的按键信息用于调试
        println("=== KEY EVENT DEBUG ===")
        println("Key: ${event.key}")
        println("Ctrl: ${event.ctrl}")
        println("Alt: ${event.alt}")
        println("Shift: ${event.shift}")
        println("======================")

        message = "Key: ${event.key}, Ctrl: ${event.ctrl}, Alt: ${event.alt}, Shift: ${event.shift}"
        true // 拦截事件
    })

    LaunchedEffect(Unit) {
        awaitCancellation()
    }
}