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

    /**
     * Returns the innermost class surrounding the passed block, or throws an error if the block
     * is not nested within a class (never happens in well formed java code).
     */
    val innermost_outer_class: SourceClass
        get() {
            var scope: Scope = this
            while (scope !is SourceClass)
                scope = scope.outer ?: throw Error("block is not nested within a class")
            return scope
        }

    // ---------------------------------------------------------------------------------------------
}