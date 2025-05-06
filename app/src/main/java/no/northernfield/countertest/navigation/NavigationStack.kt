package no.northernfield.countertest.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import no.northernfield.countertest.EventBus
import no.northernfield.countertest.RememberRetainedRegistry
import no.northernfield.countertest.ScreenKey
import no.northernfield.countertest.produceRetainedState
import no.northernfield.countertest.rememberRetained


data class Screen<T>(
    val key: T,
    val content: @Composable (Modifier) -> Unit,
)

fun <T> MutableSet<Screen<T>>.screen(
    key: T,
    content: @Composable (Modifier) -> Unit
) {
    add(
        Screen(
            key = key,
            content = content
        )
    )
}

sealed interface NavigationEvent {
    data class NavigateTo<T>(val key: T) : NavigationEvent
    data object Pop : NavigationEvent
}

class Navigator<T> : EventBus<NavigationEvent>() {
    fun navigateTo(key: T) = produceEvent(NavigationEvent.NavigateTo(key))
    fun pop() = produceEvent(NavigationEvent.Pop)
}

@Composable
fun <T> Graph(
    modifier: Modifier,
    root: T,
    block: MutableSet<Screen<T>>.() -> Unit
) {
    val screens = remember { buildSet { block() } }
    var stack by rememberRetained("navigationStack") { mutableStateOf(listOf<T>(root)) }
    val navigator = rememberNavigator<ScreenKey>()
    val registry = rememberNavigationRegistry<T, MutableState<*>>()

    val screen by produceRetainedState("currentScreen", screens.first { it.key == root }) {
        navigator.events.onEach {
            when (it) {
                is NavigationEvent.NavigateTo<*> -> {
                    stack = stack + it.key as T
                    Log.d("Graph", "Navigated to ${it.key}. Stack state: $stack")
                }

                NavigationEvent.Pop -> {
                    if (stack.isNotEmpty()) {
                        registry.clear(stack.last())
                        stack = stack.dropLast(1)
                    }
                    Log.d("Graph", "Poped. Stack state: $stack")
                }
            }
        }.map {
            value = screens.first { it.key == stack.last() }
        }.launchIn(this)
    }

    screen.content(modifier)
}

@Composable
fun <T> rememberNavigator(): Navigator<T> = rememberRetained("navigator") { Navigator() }

@Composable
fun <KEY, VALUE: Any> rememberNavigationRegistry(): RememberRetainedRegistry<KEY, MutableState<VALUE>> =
    rememberRetained("navigationRegistry") {
        object : RememberRetainedRegistry<KEY, MutableState<VALUE>> {
            override val values: MutableMap<KEY, MutableState<VALUE>> = mutableMapOf()

            override fun toString() = values.map { "key: ${it.key}, value: ${it.value}" }
                .joinToString(" | ")
        }
    }
