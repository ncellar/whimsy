package norswap.uranium.java.model.source
import norswap.lang.java8.ast.ConstructorDecl
import norswap.lang.java8.ast.TypeParam
import norswap.uranium.java.model.Constructor

class SourceConstructor (val node: ConstructorDecl, override val outer: Scope): Constructor(), Scope
{
    // ---------------------------------------------------------------------------------------------

    override val name = node.name

    // ---------------------------------------------------------------------------------------------

    val parameters = HashMap<String, SourceParameter>()

    // ---------------------------------------------------------------------------------------------

    val type_params = HashMap<String, TypeParam>()

    // ---------------------------------------------------------------------------------------------
}