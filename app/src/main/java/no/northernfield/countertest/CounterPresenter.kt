package no.northernfield.countertest

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import no.northernfield.countertest.CounterEvent.Decrement
import no.northernfield.countertest.CounterEvent.Increment
import no.northernfield.countertest.CounterEvent.Reset

sealed interface CounterEvent {
    object Increment : CounterEvent
    object Decrement : CounterEvent
    object Reset : CounterEvent
}

class CounterEventBus {
    private val _events = Channel<CounterEvent>()
    val events = _events.receiveAsFlow()

    fun increment() {
        _events.trySend(Increment)
            .onFailure { Log.d("CounterEvent", "Could not send Increment") }
    }

    fun decrement() {
        _events.trySend(Decrement)
            .onFailure { Log.d("CounterEvent", "Could not send Decrement") }
    }

    fun reset() {
        _events.trySend(Reset)
            .onFailure { Log.d("CounterEvent", "Could not send Reset") }
    }
}

data class CounterState(val count: Int = 0)

@Composable
fun CounterPresenter(events: Flow<CounterEvent>): State<CounterState> =
    produceState(CounterState()) {
        launch(Dispatchers.IO) {
            Log.d("CounterPresenter", "Collecting from events")
            events.collect { event ->
                Log.d("CounterPresenter", "Received event: $event")
                value = when (event) {
                    Increment -> value.copy(count = value.count + 1)
                    Decrement -> value.copy(count = value.count - 1)
                    Reset -> CounterState()
                }
            }
        }
    }