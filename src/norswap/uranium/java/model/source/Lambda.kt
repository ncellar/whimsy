package norswap.uranium.java.model.source
import norswap.uranium.java.model.Data

class Lambda (val node: norswap.lang.java8.ast.Lambda, override val outer: Scope): Scope
{
    // ---------------------------------------------------------------------------------------------

    val parameters = HashMap<String, SourceParameter>()

    // ---------------------------------------------------------------------------------------------

    val body =
        if (node.body is norswap.lang.java8.ast.Block) { Block(node.body, this) } else null

    // ---------------------------------------------------------------------------------------------

    override fun get_data (name: String): Data?
        = parameters[name]

    // ---------------------------------------------------------------------------------------------
}