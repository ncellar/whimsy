package norswap.uranium
import norswap.utils.Advice1
import norswap.utils.cast

/**
 * An advice used to visit nodes within a [Reactor].
 */
abstract class NodeVisitor<N: Node>: Advice1<Node, Unit>
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Casts [node] to [N] and invokes [visit]. Hence must only be called
     * when [node] has a class in [domain].
     */
    override operator fun invoke (node: Node, begin: Boolean): Unit
        = visit(node.cast<N>(), begin)

    // ---------------------------------------------------------------------------------------------

    /**
     * The actual visit logic. Override this instead of [invoke].
     */
    abstract fun visit (node: N, begin: Boolean)

    // ---------------------------------------------------------------------------------------------

    /**
     * The classes of the nodes that visitor wants to visit.
     * If null, will use the first type parameter of the superclass as the domain
     * (so it must be a subclass of [N]).
     */
    open val domain: List<Class<out N>>? = null

    // ---------------------------------------------------------------------------------------------

    /**
     * The reactor that initiated the visit.
     * This will be set by the reactor when the visitor is registered.
     */
    lateinit var reactor: Reactor
        internal set

    // ---------------------------------------------------------------------------------------------
}