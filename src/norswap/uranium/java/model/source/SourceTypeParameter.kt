package norswap.uranium.java.model.source
import norswap.lang.java8.ast.TypeParam
import norswap.uranium.java.model.TypeParameter

class SourceTypeParameter (val node: TypeParam): TypeParameter()
{
    override val name = node.name
}