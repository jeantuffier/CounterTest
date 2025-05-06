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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.northernfield.countertest.CounterEvent.Decrement
import no.northernfield.countertest.CounterEvent.Increment
import no.northernfield.countertest.CounterEvent.Reset
import no.northernfield.countertest.ui.theme.CounterTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CounterTestTheme {
                CounterScreen(CounterEventBus())
            }
        }
    }
}

@Composable
fun CounterScreen(bus: CounterEventBus) {
    val state by counterPresenter("counter", bus.events)
    CounterScreenContent(
        count = state.count,
        onDecrement = { bus.produceEvent(Decrement) },
        onReset = { bus.produceEvent(Reset) },
        onIncrement = { bus.produceEvent(Increment) },
    )
}

@Composable
fun CounterScreenContent(
    count: Int,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    onIncrement: () -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Center,
        ) {
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
                    modifier = Modifier.padding(horizontal = 8.dp).testTag("reset")
                ) { Icon(Icons.Default.Refresh, "Reset") }
                Button(onClick = onIncrement, modifier = Modifier.testTag("increment")) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        "Increment"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewCounterScreenContent() {
    CounterScreenContent(
        count = 42,
        onDecrement = {},
        onReset = {},
        onIncrement = {},
    )
}
