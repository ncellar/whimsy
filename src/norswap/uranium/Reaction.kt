package norswap.uranium

import norswap.utils.multimap.append

/**
 * A reaction is a procedure that consumes some attributes ([consumed]) in order to derive other
 * attributes ([provided]).
 *
 * A reaction is typically associated with a particular [node] -- usually providing attributes of
 * this node. It may also be the node that caused the reactions' creation. The reaction keeps
 * a reference to this node that can be used in the implementation of [trigger], the method that
 * computes the provided attributes.
 */
class Reaction <N: Node> internal constructor (node: N)
{
    // ---------------------------------------------------------------------------------------------

    constructor (node: N, init: Reaction<N>.() -> Unit): this(node) {
        init()

        // Register the attributes consumed and provided by this reaction with [node].
        for ((node1, name) in provided) { node1.suppliers.append(name, this) }
        for ((node1, name) in consumed) {
            node1.consumers.append(name, this)
            if (node.raw(name) != null) ++ deps_count
        }

        if (deps_count == consumed.size)
            reactor.enqueue(this)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * The node associated with this reaction.
     */
    val node: N = node

    // ---------------------------------------------------------------------------------------------

    /**
     * Trigger the reaction in order to derive the supplied attributes.
     */
    val trigger get() = _trigger
    lateinit var _trigger: () -> Unit

    // ---------------------------------------------------------------------------------------------

    /**
     * The reaction will be triggered when these attributes are available.
     */
    val consumed get() = _consumed
    var _consumed: List<Attribute> = emptyList()

    // ---------------------------------------------------------------------------------------------

    /**
     * Once the reaction is triggered, these attributes will be made available.
     */
    val provided get() = _provided
    var _provided: List<Attribute> = emptyList()

    // ---------------------------------------------------------------------------------------------

    /**
     * Reactor to which this instance is associated.
     */
    val reactor get() = _reactor
    var _reactor: Reactor = Context.reactor

    // ---------------------------------------------------------------------------------------------

    /**
     * The reaction that continues this one (if any), or null.
     */
    var continued_in: Reaction<*>? = null

    // ---------------------------------------------------------------------------------------------

    /**
     * The reaction that this reaction is continued from (if any), or null.
     */
    var continued_from: Reaction<*>? = null

    // ---------------------------------------------------------------------------------------------

    private var deps_count = 0

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the reaction has been triggered or not.
     *
     * This is set to true by the reactor before running [trigger], so will be true even if
     * [trigger] throws an exception.
     */
    var triggered = false
        internal set

    // ---------------------------------------------------------------------------------------------

    @Suppress("UNUSED_PARAMETER")
    internal fun satisfy (node: Node, attr: String)
    {
        if (++ deps_count == consumed.size)
            reactor.enqueue(this)
    }

    // ---------------------------------------------------------------------------------------------

    override fun toString() = "$consumed -> $provided"

    // ---------------------------------------------------------------------------------------------
}