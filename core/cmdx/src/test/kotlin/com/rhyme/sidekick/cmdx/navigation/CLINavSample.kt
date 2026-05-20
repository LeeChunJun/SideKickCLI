package com.rhyme.sidekick.cmdx.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jakewharton.mosaic.runMosaicBlocking
import com.jakewharton.mosaic.ui.Text
import com.rhyme.sidekick.cmdx.navigation.simple.CLINavigator
import com.rhyme.sidekick.cmdx.navigation.simple.LocalCLINavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

@Composable
fun CLIRoot(content: @Composable () -> Unit) {
    val cliNavigator = CLINavigator("home")

    CompositionLocalProvider(
        LocalCLINavigator provides cliNavigator,
        content = content
    )
}

@Composable
fun CLIRouter() {
    val cliNavigator = LocalCLINavigator.current
    val backStack by cliNavigator.backStack.collectAsState()
    val currentRoute = backStack.last().route

    when(currentRoute) {
        "home" -> HomeScreen()
        "todo" -> ToDoScreen()
        else -> ErrorScreen()
    }
}

@Composable
fun HomeScreen() {
    Text("hello from Home")
}

@Composable
fun ToDoScreen() {
    Text("hello from ToDo")
}

@Composable
fun ErrorScreen() {
    Text("complain from Error")
}

/**
 * CLI navigation 用例
 */
fun main() {
    runMosaicBlocking {
        CLIRoot {
            CLIRouter()
        }
    }
}
