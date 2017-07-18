package norswap.uranium.java.model2.source
import norswap.lang.java8.ast.Keyword
import norswap.lang.java8.ast.MethodDecl
import norswap.uranium.java.model2.Method

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
}