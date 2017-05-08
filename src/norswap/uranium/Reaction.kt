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
        satisfied = BooleanArray(consumed.size)

        // Register the attributes consumed and provided by this reaction with [node].
        for ((node1, name) in provided) { node1.suppliers.append(name, this) }
        for ((node1, name) in consumed) {
            node1.consumers.append(name, this)
            if (node1.raw(name) != null) update_satisfaction(node1, name)
        }

        if (satisfied_count == satisfied.size)
            reactor.enqueue(this)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * The node associated with this reaction.
     */
    val node: N = node

    // ---------------------------------------------------------------------------------------------

    /**
     * A label for the reaction, to display in front of its required/provided types when
     * printing.
     */
    var label: String = "Reaction"

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
     * Indicates whether the reaction is optional. Optional reactions do not cause
     * an error if they are never triggered, and are not forced to provide their attributes.
     */
    val optional get() = _optional
    var _optional = false

    // ---------------------------------------------------------------------------------------------

    /**
     * The reaction that continues this one (if any), or null.
     */
    var continued_in: Reaction<*>? = null

    // ---------------------------------------------------------------------------------------------

    /**
     * A reactions that should be enqueued in the reactor (if ready) after this reaction
     * triggers.
     */
    val pushed: Reaction<*>? get() = _pushed
    var _pushed: Reaction<*>? = null

    // ---------------------------------------------------------------------------------------------

    private var satisfied_count = 0
    private lateinit var satisfied: BooleanArray

    // ---------------------------------------------------------------------------------------------

    /**
     * The number of times the reaction has been triggered.
     * This is updated after the reaction has completed triggering without [Continue].
     */
    var triggers = 0
        internal set

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the reaction has been triggered at least once.
     * This is updated after the reaction has completed triggering without [Continue].
     */
    val triggered: Boolean
        get() = triggers > 0

    // ---------------------------------------------------------------------------------------------

    private fun update_satisfaction (node: Node, name: String)
    {
        val i = consumed.indexOfFirst { it.node === node && it.name == name }
        if (satisfied[i]) return
        satisfied[i] = true
        ++satisfied_count
    }

    // ---------------------------------------------------------------------------------------------

    @Suppress("UNUSED_PARAMETER")
    internal fun satisfy (node: Node, name: String)
    {
        update_satisfaction(node, name)
        if (satisfied_count == satisfied.size)
            reactor.enqueue(this)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the reaction is ready to be triggered (all its required attributes are available).
     */
    val ready: Boolean
        get() = satisfied_count == satisfied.size

    // ---------------------------------------------------------------------------------------------

    override fun toString() = "$label: $consumed -> $provided"

    // ---------------------------------------------------------------------------------------------
}