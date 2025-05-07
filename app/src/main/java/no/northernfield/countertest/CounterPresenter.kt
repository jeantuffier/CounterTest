package no.northernfield.countertest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.northernfield.countertest.CounterEvent.Decrement
import no.northernfield.countertest.CounterEvent.Increment
import no.northernfield.countertest.CounterEvent.Reset

sealed interface CounterEvent {
    object Increment : CounterEvent
    object Decrement : CounterEvent
    object Reset : CounterEvent
}

data class CounterState(val count: Int)

@Composable
fun counterPresenter(key: ScreenKey, events: Flow<CounterEvent>): State<CounterState> =
    produceRetainedState(key = key.name, initialValue = CounterState(count = 0)) {
        events.onEach { event ->
            value = when (event) {
                Increment -> value.copy(count = value.count + 1)
                Decrement -> value.copy(count = value.count - 1)
                Reset -> value.copy(count = 0)
            }
        }.launchIn(this)
    }
