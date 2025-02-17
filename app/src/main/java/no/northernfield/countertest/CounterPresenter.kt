package no.northernfield.countertest

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    fun produceEvent(event: CounterEvent) {
        _events.trySend(event)
            .onFailure { Log.d("CounterEvent", "Could not send $event") }
    }
}

data class CounterState(val count: Int)

@Composable
fun counterPresenter(events: Flow<CounterEvent>): State<CounterState> =
    produceState(CounterState(0)) {
        events.onEach {
            value = when (it) {
                Increment -> value.copy(count = value.count + 1)
                Decrement -> value.copy(count = value.count - 1)
                Reset -> CounterState(0)
            }
        }.launchIn(this)
    }