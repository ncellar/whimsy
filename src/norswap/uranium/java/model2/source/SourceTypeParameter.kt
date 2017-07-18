package norswap.uranium.java.model2.source
import norswap.lang.java8.ast.TypeParam
import norswap.uranium.java.model2.TypeParameter

class SourceTypeParameter (val node: TypeParam): TypeParameter()
{
    override val name = node.name
}