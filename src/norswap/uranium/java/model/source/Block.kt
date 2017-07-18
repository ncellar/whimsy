package norswap.uranium.java.model.source
import norswap.uranium.java.model.Klass

class Block (val node: norswap.lang.java8.ast.Block, override val outer: Scope): Scope
{
    // ---------------------------------------------------------------------------------------------

    val variables =  HashMap<String, Variable>()

    // ---------------------------------------------------------------------------------------------

    val classes =  HashMap<String, Klass>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps labels to their index in `node.stmts`.
     */
    val labels = HashMap<String, Int>()

    // ---------------------------------------------------------------------------------------------
}