package no.northernfield.countertest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.northernfield.countertest.CounterEvent.Decrement
import no.northernfield.countertest.CounterEvent.Increment
import no.northernfield.countertest.CounterEvent.Reset

sealed interface CounterEvent {
    object Increment : CounterEvent
    object Decrement : CounterEvent
    object Reset : CounterEvent
}

data class CounterState(
    val count: Int,
    val sink: (CounterEvent) -> Unit,
)

@Composable
fun counterPresenter(key: String): State<CounterState> {
    val uiEventSink by rememberRetained("$key-UiEventSink") { mutableStateOf(UiEventSink<CounterEvent>()) }
    return produceRetainedState(
        key = key,
        initialValue = CounterState(
            count = 0,
            sink = uiEventSink::sink,
        )
    ) {
        launch(Dispatchers.IO) {
            uiEventSink.collect { event ->
                value = when (event) {
                    Increment -> value.copy(count = value.count + 1)
                    Decrement -> value.copy(count = value.count - 1)
                    Reset -> value.copy(count = 0)
                }
            }
        }
    }
}
