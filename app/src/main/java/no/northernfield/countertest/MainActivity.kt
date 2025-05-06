package no.northernfield.countertest

import android.os.Bundle
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
import androidx.compose.material3.rememberBottomAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.northernfield.countertest.CounterEvent.Decrement
import no.northernfield.countertest.CounterEvent.Increment
import no.northernfield.countertest.CounterEvent.Reset
import no.northernfield.countertest.ScreenKey.COUNTER_1
import no.northernfield.countertest.ScreenKey.COUNTER_2
import no.northernfield.countertest.ScreenKey.COUNTER_3
import no.northernfield.countertest.navigation.Graph
import no.northernfield.countertest.navigation.NavigationEvent
import no.northernfield.countertest.navigation.rememberNavigationRegistry
import no.northernfield.countertest.navigation.rememberNavigator
import no.northernfield.countertest.navigation.screen
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

enum class ScreenKey {
    COUNTER_1,
    COUNTER_2,
    COUNTER_3,
    NESTED_SCREEN,
}

sealed interface Screen {
    val key: ScreenKey

    enum class TopLevel (
        override val key: ScreenKey,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector,
    ): Screen {
        Counter1(COUNTER_1, Icons.Filled.Home, Icons.Outlined.Home),
        Counter2(COUNTER_2, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
        Counter3(COUNTER_3, Icons.Filled.Star, Icons.Outlined.Star),
    }

    data object NestedScreen : Screen {
        override val key = ScreenKey.NESTED_SCREEN
    }
}

@Composable
fun App() {
    val navigator = rememberNavigator<ScreenKey>()
    var selectedItem by rememberRetained("selectedItem") { mutableIntStateOf(0) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.TopLevel.entries.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selectedItem == index) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        label = { Text(screen.key.name) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            navigator.navigateTo(screen.key)
                        }
                    )
                }
            }
        }
    ) { padding ->
        Graph(modifier = Modifier.padding(padding), COUNTER_1) {
            screen(COUNTER_1) { CounterScreen(COUNTER_1, EventBus()) }
            screen(COUNTER_2) { CounterScreen(COUNTER_2, EventBus()) }
            screen(COUNTER_3) { CounterScreen(COUNTER_3, EventBus()) }
            screen(ScreenKey.NESTED_SCREEN) { NestedScreen() }
        }
    }
}

@Composable
fun CounterScreen(key: ScreenKey, bus: EventBus<CounterEvent>) {
    val state by counterPresenter(key, bus.events)
    CounterScreenContent(
        key = key,
        count = state.count,
        onDecrement = { bus.produceEvent(Decrement) },
        onReset = { bus.produceEvent(Reset) },
        onIncrement = { bus.produceEvent(Increment) },
    )
}

@Composable
fun CounterScreenContent(
    key: ScreenKey,
    count: Int,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    onIncrement: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Center,
    ) {
        Text("Key: $key")
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
        val navigator = rememberNavigator<ScreenKey>()
        Button(onClick = { navigator.navigateTo(ScreenKey.NESTED_SCREEN) }, modifier = Modifier.testTag("nestedNavigation")) {
            Text("Navigate to Nested Screen")
        }
    }
}

@Composable
fun NestedScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Center,
    ) {
        val state by rememberRetained(
            key = ScreenKey.NESTED_SCREEN,
            registry = rememberNavigationRegistry<ScreenKey, Int>(),
        ) { mutableIntStateOf(Random.nextInt()) }
        Text("Nested Screen: $state")
        val navigator = rememberNavigator<ScreenKey>()
        Button(onClick = { navigator.pop() }, modifier = Modifier.testTag("nestedNavigation")) {
            Text("Navigate to Nested Screen")
        }
    }
}

@Preview
@Composable
fun PreviewCounterScreenContent() {
    CounterScreenContent(
        key = COUNTER_1,
        count = 42,
        onDecrement = {},
        onReset = {},
        onIncrement = {},
    )
}
