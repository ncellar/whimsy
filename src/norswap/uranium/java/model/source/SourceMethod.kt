package norswap.uranium.java.model.source
import norswap.lang.java8.ast.Keyword
import norswap.lang.java8.ast.MethodDecl
import norswap.uranium.java.Context
import norswap.uranium.java.model.Method
import norswap.uranium.java.types.ParameterType

class SourceMethod (val node: MethodDecl, override val outer: SourceClass): Method(), Scope
{
    // ---------------------------------------------------------------------------------------------

    override val name
        = node.name

    // ---------------------------------------------------------------------------------------------

    override val static
        = node.mods.contains(Keyword.static)

    // ---------------------------------------------------------------------------------------------

    override val parameters = HashMap<String, SourceParameter>()

    // ---------------------------------------------------------------------------------------------

    override val type_params = HashMap<String, SourceTypeParameter>()

    // ---------------------------------------------------------------------------------------------

    override fun get_data (name: String)
        = parameters[name]

    // ---------------------------------------------------------------------------------------------

    override fun get_type (name: String, ctx: Context)
        = type_params[name]?.let(::ParameterType)

    // ---------------------------------------------------------------------------------------------
}