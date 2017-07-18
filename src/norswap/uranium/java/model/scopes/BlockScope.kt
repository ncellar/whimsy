package norswap.uranium.java.model.scopes
import norswap.lang.Node
import norswap.uranium.java.model.Field
import norswap.uranium.java.model.Klass

/**
 * Scope information for blocks and method bodies.
 */
open class BlockScope (val node: Node, val parent: BlockScope?, val klass: Klass)
{
    // ---------------------------------------------------------------------------------------------

    val variables =  HashMap<String, Field>()

    // ---------------------------------------------------------------------------------------------

    val classes =  HashMap<String, Klass>()

    // ---------------------------------------------------------------------------------------------
}