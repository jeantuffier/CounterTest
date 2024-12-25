package no.northernfield.countertest

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow

private const val TAG = "UiEventSink"

class UiEventSink<T> {
    private val sharedFlow: MutableSharedFlow<T> = MutableSharedFlow(extraBufferCapacity = 1)

    fun sink(event: T) {
        val result = sharedFlow.tryEmit(event)
        if (!result) Log.e(TAG, "Failed to emit $event")
    }

    suspend fun collect(block: suspend (T) -> Unit) {
        sharedFlow.collect(block)
    }
}
