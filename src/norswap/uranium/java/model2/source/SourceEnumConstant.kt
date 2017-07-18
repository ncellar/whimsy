package norswap.uranium.java.model2.source
import norswap.lang.java8.ast.EnumConstant
import norswap.uranium.java.model2.Field

class SourceEnumConstant (val node: EnumConstant): Field()
{
    override val name
        = node.name

    override val static = true
}