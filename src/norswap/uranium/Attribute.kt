package norswap.uranium

/**
 * Handel to a node's attribute.
 */
@Suppress("EqualsOrHashCode")
data class Attribute (val node: Any, val name: String)
{
    override fun hashCode(): Int {
        return System.identityHashCode(node) * 31 + name.hashCode()
    }
}