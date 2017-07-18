package norswap.uranium.java.model2.source
import norswap.lang.Node
import norswap.uranium.java.model2.Data

/**
 * Scope for catch statements, try with resources, and for.
 */
class ControlFlow (val node: Node, override val outer: Scope): Scope
{
    val variables = HashMap<String, Data>()
}