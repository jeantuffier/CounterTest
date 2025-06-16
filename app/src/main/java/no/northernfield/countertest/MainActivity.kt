package no.northernfield.countertest

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import no.northernfield.countertest.CounterEvent.Decrement
import no.northernfield.countertest.CounterEvent.Increment
import no.northernfield.countertest.CounterEvent.Reset
import no.northernfield.countertest.ScreenKey.Counter1
import no.northernfield.countertest.ScreenKey.Counter2
import no.northernfield.countertest.ScreenKey.Counter3
import no.northernfield.countertest.ScreenKey.NestedScreen
import no.northernfield.countertest.navigation.rememberNavigationRegistry
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            App()
        }
    }
}

sealed interface ScreenKey : NavKey {
    @Serializable
    object Counter1 : ScreenKey

    @Serializable
    object Counter2 : ScreenKey

    @Serializable
    object Counter3 : ScreenKey

    @Serializable
    object NestedScreen : ScreenKey
}

sealed interface Screen {
    val key: ScreenKey

    enum class TopLevel(
        override val key: ScreenKey,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector,
    ) : Screen {
        Counter1(ScreenKey.Counter1, Icons.Filled.Home, Icons.Outlined.Home),
        Counter2(ScreenKey.Counter2, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
        Counter3(ScreenKey.Counter3, Icons.Filled.Star, Icons.Outlined.Star),
    }

    data object NestedScreen : Screen {
        override val key = ScreenKey.NestedScreen
    }
}

@Composable
fun App() {
    val backStack = rememberNavBackStack<ScreenKey>(Counter1)
    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.TopLevel.entries.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (backStack.last() == screen.key) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        label = { Text(screen.key::class.java.simpleName) },
                        selected = backStack.last() == screen.key,
                        onClick = { backStack.add(screen.key) }
                    )
                }
            }
        }
    ) { padding ->

        val nestedNavigation: () -> Unit = {
            Log.d("CounterScreen", "Navigating to Nested Screen")
            backStack.add(NestedScreen)
        }

        NavDisplay(
            modifier = Modifier.padding(padding),
            backStack = backStack,
            onBack = {
                backStack.removeLastOrNull()
            },

            entryProvider = entryProvider {
                entry<Counter1> { CounterScreen(Counter1, EventBus(), nestedNavigation) }
                entry<Counter2> { CounterScreen(Counter2, EventBus(), nestedNavigation) }
                entry<Counter3> { CounterScreen(Counter3, EventBus(), nestedNavigation) }
                entry<NestedScreen> { NestedScreen { backStack.removeLastOrNull() } }
            },
        )
    }
}

@Composable
fun CounterScreen(key: ScreenKey, bus: EventBus<CounterEvent>, onNavigate: () -> Unit) {
    val state by counterPresenter(key, bus.events)
    CounterScreenContent(
        key = key,
        count = state.count,
        onDecrement = { bus.produceEvent(Decrement) },
        onReset = { bus.produceEvent(Reset) },
        onIncrement = { bus.produceEvent(Increment) },
        onNavigate = onNavigate,
    )
}

@Composable
fun CounterScreenContent(
    key: ScreenKey,
    count: Int,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    onIncrement: () -> Unit,
    onNavigate: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Center,
    ) {
        Text("Key: ${key::class.java.simpleName}")
        Text(
            text = "Counter: $count",
            modifier = Modifier.testTag("counter")
        )
        Row(modifier = Modifier.padding(top = 16.dp)) {
            Button(onClick = onDecrement, modifier = Modifier.testTag("decrement")) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    "Decrement"
                )
            }
            Button(
                onClick = onReset,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .testTag("reset")
            ) { Icon(Icons.Default.Refresh, "Reset") }
            Button(onClick = onIncrement, modifier = Modifier.testTag("increment")) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    "Increment"
                )
            }
        }

        Button(
            onClick = onNavigate,
            modifier = Modifier.testTag("nestedNavigation")
        ) {
            Text("Navigate to Nested Screen")
        }
    }
}

@Composable
fun NestedScreen(onBack: () -> Unit) {
    Log.d("NestedScreen", "recomposition of NestedScreen")
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Center,
    ) {
        val state by rememberRetained(
            key = NestedScreen,
            registry = rememberNavigationRegistry<ScreenKey, Int>(),
        ) { mutableIntStateOf(Random.nextInt()) }

        Text("Nested Screen: $state")
        Button(
            onClick = onBack,
            modifier = Modifier.testTag("nestedNavigation")
        ) {
            Text("Navigate to Nested Screen")
        }
    }
}

@Preview
@Composable
fun PreviewCounterScreenContent() {
    CounterScreenContent(
        key = Counter1,
        count = 42,
        onDecrement = {},
        onReset = {},
        onIncrement = {},
        onNavigate = {},
    )
}
