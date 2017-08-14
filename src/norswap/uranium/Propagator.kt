package norswap.uranium
import norswap.utils.cast
import norswap.utils.multimap.HashMultiMap
import norswap.utils.multimap.append
import norswap.utils.visit_around
import java.util.ArrayDeque

class Propagator (val roots: List<Any>)
{
    // ---------------------------------------------------------------------------------------------

    /**
     * - Maps node types to visitor that want to visit them.
     * - Use [add_visitor] to add items from Kotlin.
     */
    val visitors = HashMultiMap<AnyClass, NodeVisitor>()

    // ---------------------------------------------------------------------------------------------

    /**
     * A function that enables walking a tree.
     * The function parameter is applied on all the children of the node passed as first parameter.
     */
    lateinit var walker: (Any, (Any) -> Unit) -> Unit

    // ---------------------------------------------------------------------------------------------

    private val store = HashMap<Attribute, Any>()

    // ---------------------------------------------------------------------------------------------

    private val consumers = HashMultiMap<Attribute, Reaction>()

    // ---------------------------------------------------------------------------------------------

    private val suppliers = HashMultiMap<Attribute, Reaction>()

    // ---------------------------------------------------------------------------------------------

    private val queue = ArrayDeque<Reaction>()

    // ---------------------------------------------------------------------------------------------

    private val errors = ArrayList<UraniumError>()

    // ---------------------------------------------------------------------------------------------

    private var initialized = false

    // ---------------------------------------------------------------------------------------------

    /**
     * Registers the given error for the current attribute derivation.
     */
    fun report (error: UraniumError)
    {
        errors.add(error)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a [visitor] to the propagator. Should be a subtype of [NodeVisitor].
     */
    inline fun <reified T> add_visitor (visitor: Any)
    {
        visitors.append(T::class.java, visitor.cast<NodeVisitor>())
    }

    // ---------------------------------------------------------------------------------------------

    internal fun enqueue (reaction: Reaction)
    {
        queue.add(reaction)
    }

    // ---------------------------------------------------------------------------------------------

    internal fun register (reaction: Reaction)
    {
        reaction.consumed.forEach { consumers.append(it, reaction) }
        reaction.supplied.forEach { suppliers.append(it, reaction) }
        if (reaction.consumed.isEmpty())
            enqueue(reaction)
    }

    // ---------------------------------------------------------------------------------------------

    private fun initialize()
    {
        if (initialized) return
        initialized = true

        for (root in roots)
            root.visit_around(walker, this::visit)
    }

    // ---------------------------------------------------------------------------------------------

    private fun visit (node: Any, begin: Boolean)
    {
        var klass: AnyClass? = node::class.java
        while (klass != null) {
            visitors[klass]?.forEach { it(node, begin) }
            klass = klass.superclass
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets the value of the given attribute.
     */
    operator fun set (attr: Attribute, value: Any?)
    {
        if (value == null)
            store.remove(attr)
        else
            store[attr] = value

        val consumers1 = consumers[attr] ?: return
        consumers1.forEach { it.satisfied(this, attr) }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets the value of the attribute defined by [node] and [name].
     */
    operator fun set (node: Any, name: String, value: Any?)
    {
        set(Attribute(node, name), value)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Get the value of the given attribute.
     */
    operator fun get (attr: Attribute): Any?
        = store[attr]

    // ---------------------------------------------------------------------------------------------

    /**
     * Get the value of the attribute defined by [node] and [name].
     */
    operator fun get (node: Any, name: String): Any?
        = store[Attribute(node,name)]

    // ---------------------------------------------------------------------------------------------

    /**
     * Starts (or resumes) the derivations of attributes over the [roots] of the propagator.
     */
    fun propagate()
    {
        initialize()
        while (queue.isNotEmpty())
        {
            val reaction = queue.remove()
            reaction.apply()
        }
    }

    // ---------------------------------------------------------------------------------------------
}