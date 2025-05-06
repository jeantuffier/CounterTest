package no.northernfield.countertest

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.receiveAsFlow

open class EventBus<T> {
    private val _events = Channel<T>()
    val events = _events.receiveAsFlow()

    fun produceEvent(event: T) {
        _events.trySend(event)
            .onFailure { println("Failed to emit $event") }
    }
}
