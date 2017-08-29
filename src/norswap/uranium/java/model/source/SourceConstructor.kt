package norswap.uranium.java.model.source
import norswap.lang.java8.ast.ConstructorDecl
import norswap.uranium.java.Context
import norswap.uranium.java.model.Constructor
import norswap.uranium.java.model.Data
import norswap.uranium.java.types.ParameterType
import norswap.uranium.java.types.RefType

class SourceConstructor (val node: ConstructorDecl, override val outer: Scope): Constructor(), Scope
{
    // ---------------------------------------------------------------------------------------------

    override val name = node.name

    // ---------------------------------------------------------------------------------------------

    val parameters = HashMap<String, SourceParameter>()

    // ---------------------------------------------------------------------------------------------

    val type_params = HashMap<String, SourceTypeParameter>()

    // ---------------------------------------------------------------------------------------------

    override fun get_data (name: String): Data?
        = parameters[name]

    // ---------------------------------------------------------------------------------------------

    override fun get_type (name: String, ctx: Context): RefType?
        = type_params[name]?.let(::ParameterType)

    // ---------------------------------------------------------------------------------------------
}