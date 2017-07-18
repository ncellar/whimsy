package norswap.uranium.java.model2.source
import norswap.lang.java8.ast.AnnotationElemDecl
import norswap.uranium.java.model2.Method
import norswap.uranium.java.model2.Parameter
import norswap.uranium.java.model2.TypeParameter

class SourceAnnotationElement (node: AnnotationElemDecl): Method()
{
    // ---------------------------------------------------------------------------------------------

    override val name
        = node.name

    // ---------------------------------------------------------------------------------------------

    override val static
        = false

    // ---------------------------------------------------------------------------------------------

    override val parameters
        = emptyMap<String, Parameter>()

    // ---------------------------------------------------------------------------------------------

    override val type_params
        = emptyMap<String, TypeParameter>()

    // ---------------------------------------------------------------------------------------------
}