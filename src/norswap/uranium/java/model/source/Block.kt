package norswap.uranium.java.model.source
import norswap.uranium.java.Context
import norswap.uranium.java.model.Data
import norswap.uranium.java.model.Klass
import norswap.uranium.java.types.ClassType
import norswap.uranium.java.types.RefType

class Block (val node: norswap.lang.java8.ast.Block, override val outer: Scope): Scope
{
    // ---------------------------------------------------------------------------------------------

    val variables = HashMap<String, Variable>()

    // ---------------------------------------------------------------------------------------------

    val classes = HashMap<String, Klass>()

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

    override fun get_type (name: String, ctx: Context): RefType?
        = classes[name]?.let { ClassType(it) }

    override fun get_data (name: String): Data?
        = variables[name]

    override fun get_label (name: String): Int?
        = labels[name]

    // ---------------------------------------------------------------------------------------------
}