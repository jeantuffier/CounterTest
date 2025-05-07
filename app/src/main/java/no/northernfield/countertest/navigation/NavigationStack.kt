package no.northernfield.countertest.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import no.northernfield.countertest.EventBus

data class Screen(
    val key: String,
    val content: @Composable () -> Unit,
)

fun MutableSet<Screen>.screen(
    key: String,
    content: @Composable () -> Unit
) {
    add(
        Screen(
            key = key,
            content = content
        )
    )
}

data class Graph(
    val currentScreen: Screen,
    val screens: Set<Screen>,
)

class Navigator : EventBus<String>() {
    val stack: MutableList<String> = mutableListOf()

    fun currentKey(): String = stack.last()

    fun navigateTo(key: String) {
        stack.add(key)
        produceEvent(key)
    }

    fun pop() = produceEvent(stack.removeAt(stack.size - 1))
}

@Composable
fun Graph(
    root: String,
    navigator: Navigator,
    block: @Composable MutableSet<Screen>.() -> Unit
) {
    val screens = buildSet { block() }
    val graph = navigator.events.map { currentKey ->
        Graph(
            currentScreen = screens.first { it.key == currentKey },
            screens = screens,
        )
    }.collectAsStateWithLifecycle(
        initialValue = Graph(
            currentScreen = screens.first { it.key == root },
            screens = screens,
        )
    )
    CompositionLocalProvider(LocalNavigator provides navigator) {
        graph.value.currentScreen.content()
    }
}

val LocalNavigator = compositionLocalOf<Navigator> { error("Navigator not set.") }
