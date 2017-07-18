package norswap.uranium.java.model.scopes
import norswap.lang.Node
import norswap.lang.java8.ast.TypeParam
import norswap.uranium.java.model.Klass

// node may be MethodDecl, ConstructorDecl or Lambda
class MethodScope (node: Node, parent: BlockScope?, klass: Klass): BlockScope(node, parent, klass)
{
    // ---------------------------------------------------------------------------------------------

    val type_params = HashMap<String, TypeParam>()

    // ---------------------------------------------------------------------------------------------
}