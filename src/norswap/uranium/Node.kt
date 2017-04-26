package norswap.uranium
import norswap.utils.Visitable
import java.util.ArrayList
import java.util.HashMap

// =================================================================================================

/**
 * An AST node, which is a container for attributes.
 */
interface Node: Visitable<Node>
{
    // ---------------------------------------------------------------------------------------------

    val attrs     : HashMap<String, Any>
    val consumers : HashMap<String, ArrayList<Reaction<*>>>
    val suppliers : HashMap<String, ArrayList<Reaction<*>>>

    // ---------------------------------------------------------------------------------------------

    /**
     * Retrieve the value of the given attribute, or throws a [Fail] carrying an
     * [AttributeNotDefined] if it doesn't exist.
     */
    operator fun get (name: String): Any
         = attrs[name] ?: throw Fail(AttributeNotDefined(Attribute(this, name)))

    // ---------------------------------------------------------------------------------------------

    /**
     * Retrieve the value of the given attribute, or null if it doesn't exist.
     */
    fun raw (name: String): Any?
        = attrs[name]

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets the value of the given attribute. If the attribute is already defined, throws a
     * [Fail] carrying an [AttributeRedefined].
     *
     * This may cause consumers waiting for the attribute to be enqueued by the reactor
     * in order to be triggered.
     */
    operator fun set (name: String, value: Any)
    {
        val old = attrs.put(name, value)
        if (old != null)
            throw Fail(AttributeRedefined(Attribute(this, name)))

        (consumers[name] ?: emptyList<Reaction<*>>())
            .forEach { it.satisfy(this, name) }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the children of this node.
     *
     * The default implementation returns an empty sequence, override to supply the correct
     * behaviour.
     */
    override fun children() = emptySequence<Node>()

    // ---------------------------------------------------------------------------------------------
}

// =================================================================================================

/**
 * Default data implementation for [Node] fields.
 */
open class CNode: Node
{
    override val attrs     = HashMap<String, Any>()
    override val consumers = HashMap<String, ArrayList<Reaction<*>>>()
    override val suppliers = HashMap<String, ArrayList<Reaction<*>>>()
}

// =================================================================================================