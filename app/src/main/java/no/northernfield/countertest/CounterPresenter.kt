package no.northernfield.countertest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import no.northernfield.countertest.CounterEvent.Decrement
import no.northernfield.countertest.CounterEvent.Increment
import no.northernfield.countertest.CounterEvent.Reset

sealed interface CounterEvent {
    object Increment : CounterEvent
    object Decrement : CounterEvent
    object Reset : CounterEvent
}

data class CounterState(val count: Int = 0)

@Composable
fun counterPresenter(key: String, events: Flow<CounterEvent>): State<CounterState> =
    produceRetainedState(key, CounterState()) {
        launch(Dispatchers.IO) {
            events.collect { event ->
                value = when (event) {
                    Increment -> value.copy(count = value.count + 1)
                    Decrement -> value.copy(count = value.count - 1)
                    Reset -> CounterState()
                }
            }
        }
    }
