package no.northernfield.countertest

import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

interface EventBus<T> {
    val events: Flow<T>
    fun send(event: T)
}

fun <T> eventBus() = object : EventBus<T> {
    private val _events = Channel<T>()
    override val events = _events.receiveAsFlow()
    override fun send(event: T) {
        _events.trySend(event)
            .onFailure { Log.d("EventBus", "Could not send event: $event") }
    }
}
