package no.northernfield.countertest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProduceStateScope
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.CoroutineContext

object RememberRetainedRegistry {
    private val values = mutableMapOf<String, Any>()

    fun isRegistered(key: String) = values.keys.any { it == key }

    private fun <T : Any> registerValue(key: String, state: T) {
        values[key] = state
    }

    private fun value(key: String): Any? {
        if (!isRegistered(key)) return null
        return values[key]
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> cache(
        key: String,
        block: @DisallowComposableCalls () -> T,
    ): T {
        val value = block()
        val rememberedValue = value(key)
        val result = if (rememberedValue == null) {
            registerValue(key, value)
            value
        } else {
            rememberedValue as T
        }
        return result
    }
}

@Composable
fun <T : Any> rememberRetained(
    key: String,
    calculation: @DisallowComposableCalls () -> T
): T = RememberRetainedRegistry.cache(key, calculation)

@Composable
fun <T> produceRetainedState(
    key: String,
    initialValue: T,
    producer: suspend ProduceStateScope<T>.() -> Unit
): State<T> {
    val result = rememberRetained(key) { mutableStateOf(initialValue) }
    LaunchedEffect(Unit) {
        ProduceRetainedStateScopeImpl(result, coroutineContext).producer()
    }
    return result
}

class ProduceRetainedStateScopeImpl<T>(
    state: MutableState<T>,
    override val coroutineContext: CoroutineContext
) : ProduceStateScope<T>, MutableState<T> by state {

    override suspend fun awaitDispose(onDispose: () -> Unit): Nothing {
        try {
            suspendCancellableCoroutine<Nothing> { }
        } finally {
            onDispose()
        }
    }
}