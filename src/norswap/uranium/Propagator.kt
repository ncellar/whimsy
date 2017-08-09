package norswap.uranium
import norswap.utils.multimap.HashMultiMap
import norswap.utils.multimap.append
import norswap.utils.visit_around
import java.util.ArrayDeque

class Propagator (val roots: List<Any>)
{
    // ---------------------------------------------------------------------------------------------

    /**
     * User-controlled field used to attach additional information to the propagator.
     */
    var attachment: Any = Any()

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps node types to visitor that want to visit them.
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

    fun report (error: UraniumError)
    {
        errors.add(error)
    }

    // ---------------------------------------------------------------------------------------------

    inline fun <reified T> add_visitor (noinline visitor: NodeVisitor)
    {
        visitors.append(T::class.java, visitor)
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

    operator fun set (node: Any, name: String, value: Any?)
    {
        set(Attribute(node, name), value)
    }

    // ---------------------------------------------------------------------------------------------

    operator fun get (attr: Attribute): Any?
        = store[attr]

    // ---------------------------------------------------------------------------------------------

    operator fun get (node: Any, name: String): Any?
        = store[Attribute(node,name)]

    // ---------------------------------------------------------------------------------------------

    fun propagate()
    {
        initialize()
        while (queue.isNotEmpty())
        {
            val reaction = queue.remove()
            reaction.propagator = this
            reaction.apply()
        }
    }

    // ---------------------------------------------------------------------------------------------
}