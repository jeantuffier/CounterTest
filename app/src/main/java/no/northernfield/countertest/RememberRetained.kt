package no.northernfield.countertest

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProduceStateScope
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.CoroutineContext

interface RememberRetainedRegistry<KEY, VALUE> {
    val values: MutableMap<KEY, VALUE>

    private fun value(key: KEY): VALUE? =
        if (values.keys.any { it == key }) values[key] else null

    fun clear(key: KEY) {
        values.remove(key)
        Log.d("REGISTRY", "Registry after clear: $values")
    }

    @Suppress("UNCHECKED_CAST")
    fun cache(
        key: KEY,
        block: @DisallowComposableCalls () -> VALUE,
    ): VALUE = value(key) ?: block().also { values[key] = it }
        .also { Log.d("REGISTRY", "Registry after cache: $values") }

    companion object {
        private val defaultRegistry: RememberRetainedRegistry<String, Any> by lazy {
            GenericRegistry()
        }

        fun <T : Any> cache(
            key: String,
            block: @DisallowComposableCalls () -> T,
        ): T = defaultRegistry.cache(key, block) as T
    }
}

class GenericRegistry : RememberRetainedRegistry<String, Any> {
    override val values: MutableMap<String, Any> = mutableMapOf()
}

@Composable
fun <T : Any> rememberRetained(
    key: String,
    calculation: @DisallowComposableCalls () -> T
): T = RememberRetainedRegistry.cache(key, calculation)

@Composable
fun <KEY, VALUE> rememberRetained(
    key: KEY,
    registry: RememberRetainedRegistry<KEY, VALUE>,
    calculation: @DisallowComposableCalls () -> VALUE
): VALUE = registry.cache(key, calculation)

@Composable
fun <KEY, VALUE> produceRetainedState(
    key: KEY,
    initialValue: VALUE,
    registry: RememberRetainedRegistry<KEY, MutableState<VALUE>>,
    producer: suspend ProduceStateScope<VALUE>.() -> Unit
): State<VALUE> {
    val result = rememberRetained(key, registry) { mutableStateOf(initialValue) }
    LaunchedEffect(Unit) {
        ProduceRetainedStateScopeImpl(result, coroutineContext).producer()
    }
    return result
}

@Composable
fun <T> produceSaveableState(
    initialValue: T,
    producer: suspend ProduceStateScope<T>.() -> Unit
): State<T> {
    val result = rememberSaveable { mutableStateOf(initialValue) }
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
