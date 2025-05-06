package no.northernfield.countertest.navigation

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import no.northernfield.countertest.EventBus

@JvmInline
value class ScreenKey(val value: String)

data class Screen(
    val key: ScreenKey,
    val root: Boolean = false,
    val content: @Composable () -> Unit,
)

@Composable
fun MutableList<Screen>.screen(
    key: ScreenKey,
    root: Boolean = false,
    content: @Composable () -> Unit
) {
    add(
        Screen(
            key = key,
            root = root,
            content = content
        )
    )
}

data class Graph(val screens: List<Screen>)

@Composable
fun graph(block: @Composable MutableList<Screen>.() -> Unit): Graph {
    return Graph(
        screens = buildList { block() }
    )
}