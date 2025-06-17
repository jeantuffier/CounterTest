package no.northernfield.countertest

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize
import no.northernfield.countertest.CounterEvent.Decrement
import no.northernfield.countertest.CounterEvent.Increment
import no.northernfield.countertest.CounterEvent.Reset
import no.northernfield.countertest.navigation.rememberNavigationRegistry

sealed interface CounterEvent {
    object Increment : CounterEvent
    object Decrement : CounterEvent
    object Reset : CounterEvent
}

@Parcelize
data class CounterState(val count: Int): Parcelable

@Composable
fun counterPresenter(key: ScreenKey, events: Flow<CounterEvent>): State<CounterState> =
    produceRetainedState<ScreenKey, CounterState>(
        key = key,
        initialValue = CounterState(count = 0),
        registry = rememberNavigationRegistry<ScreenKey, CounterState>()
    ) {
        events.onEach { event ->
            value = when (event) {
                Increment -> value.copy(count = value.count + 1)
                Decrement -> value.copy(count = value.count - 1)
                Reset -> value.copy(count = 0)
            }
        }.launchIn(this)
    }

@Composable
fun counterPresenter2(events: Flow<CounterEvent>): State<CounterState> =
    produceSaveableState<CounterState>(CounterState(count = 0)) {
        events.onEach { event ->
            value = when (event) {
                Increment -> value.copy(count = value.count + 1)
                Decrement -> value.copy(count = value.count - 1)
                Reset -> value.copy(count = 0)
            }
        }.launchIn(this)
    }
