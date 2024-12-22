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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.northernfield.countertest.ui.theme.CounterTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Log.d("MainActivity", "Setting content")
            CounterTestTheme {

                CounterScreen(CounterEventBus())
            }
        }
    }
}

@Composable
fun CounterScreen(bus: CounterEventBus) {
    val state by counterPresenter(
        key = "counter",
        events = bus.events,
    )
    Log.d("MainActivity", "Creating CounterScreen")
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Center,
        ) {
            Text("Counter: ${state.count}")
            Row(modifier = Modifier.padding(top = 16.dp)) {
                Button(onClick = bus::decrement) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        "Decrement"
                    )
                }
                Button(
                    onClick = bus::reset,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) { Icon(Icons.Default.Refresh, "Reset") }
                Button(onClick = bus::increment) {
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
fun PreviewCounterScreen() {
    CounterScreen(CounterEventBus())
}