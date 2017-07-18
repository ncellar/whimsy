package norswap.uranium.java.model2.source
import norswap.lang.java8.ast.EnhancedFor
import norswap.uranium.java.model2.Data

class ForParameter (val node: EnhancedFor): Data
{
    override val name: String
        = node.id.iden
}