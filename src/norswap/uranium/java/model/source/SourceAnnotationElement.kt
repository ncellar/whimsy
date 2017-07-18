package norswap.uranium.java.model.source
import norswap.lang.java8.ast.AnnotationElemDecl
import norswap.uranium.java.model.Method
import norswap.uranium.java.model.Parameter
import norswap.uranium.java.model.TypeParameter

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