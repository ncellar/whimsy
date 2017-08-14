package norswap.uranium

/**
 * Handle to a node's attribute.
 *
 * [node] is compared through identity.
 */
data class Attribute (val node: Any, val name: String)
{
    override fun equals (other: Any?)
        =  other is Attribute
        && node === other.node
        && name == other.name

    override fun hashCode()
        = System.identityHashCode(node) * 31 + name.hashCode()
}