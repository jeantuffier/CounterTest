package no.northernfield.countertest

import android.util.Log
import androidx.compose.runtime.Composable
import no.northernfield.countertest.NodeRegistery.TopLevelGraphBuilder

@JvmInline
value class Route(val value: String)
typealias ComposableFunction = @Composable () -> Unit

sealed interface NavigationNode {
    fun findNode(route: Route): NavigationNode?

    data class Graph(
        val root: Route,
        val children: Map<Route, NavigationNode>
    ) : NavigationNode {
        override fun findNode(route: Route): NavigationNode? =
            if (this.root == route) {
                when (val child = children[root]) {
                    is Screen -> child
                    is Graph -> child.findNode(route)
                    null -> null
                }
            } else {
                children.values.firstOrNull {
                    it.findNode(route) != null
                }
            }
    }

    data class Screen(
        val route: Route,
        val composable: ComposableFunction,
        val child: NavigationNode?,
    ) : NavigationNode {
        override fun findNode(route: Route): NavigationNode? =
            if (this.route == route) {
                this
            } else child?.findNode(route)
    }
}

object NodeRegistery {
    private lateinit var appGraph: NavigationNode.Graph
    private val currentRoute = mutableListOf<Route>()
    private val index = mutableSetOf<Route>()

    fun init(graph: NavigationNode.Graph): @Composable () -> Unit {
        if (!this::appGraph.isInitialized) {
            appGraph = graph
            currentRoute.add(graph.root)
        }
        return findScreen(graph.root).composable
    }

    fun findScreen(route: Route): NavigationNode.Screen {
        val node = appGraph.findNode(route) ?: throw Exception("Screen for route $route not found")
        return node as NavigationNode.Screen
    }

    class TopLevelGraphBuilder {
        private val children = mutableMapOf<Route, NavigationNode>()

        fun screen(route: Route, composable: ComposableFunction): TopLevelGraphBuilder {
            if (index.contains(route)) {
                Log.d("Navigation", "$route already registered")
            } else {
                Log.d("Navigation", "Adding $route to the graph")
                index.add(route)
                children[route] = NavigationNode.Screen(
                    route,
                    composable,
                    null,
                )
            }
            return this
        }

        fun build(root: Route): NavigationNode.Graph {
            return NavigationNode.Graph(root, children)
        }
    }
}

/*class Navigator(
    private val bus: EventBus<@Composable () -> Unit>,
) {
    fun goBack() {
        registry.pop()
        bus.send(registry.currentScreen())
    }

    fun goTo(route: String) {
        if (route == registry.currentRoute()) return

        registry.screen(route)
    }
}*/

@Composable
fun TopLevelGraph(
    root: Route,
    block: TopLevelGraphBuilder.() -> TopLevelGraphBuilder,
) {
    val builder = block(TopLevelGraphBuilder())
    val rootScreen = NodeRegistery.init(builder.build(root))
    rootScreen()
}
