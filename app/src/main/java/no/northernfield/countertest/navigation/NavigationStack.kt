package no.northernfield.countertest.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import no.northernfield.countertest.RememberRetainedRegistry
import no.northernfield.countertest.rememberRetained

@Composable
fun <KEY, VALUE : Any> rememberNavigationRegistry(): RememberRetainedRegistry<KEY, MutableState<VALUE>> =
    rememberRetained("navigationRegistry") {
        object : RememberRetainedRegistry<KEY, MutableState<VALUE>> {
            override val values: MutableMap<KEY, MutableState<VALUE>> = mutableMapOf()

            override fun toString() = values.map { "key: ${it.key}, value: ${it.value}" }
                .joinToString(" | ")
        }
    }
