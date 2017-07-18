package norswap.uranium.java.model.source
import norswap.lang.java8.ast.EnhancedFor
import norswap.uranium.java.model.Data

class ForParameter (val node: EnhancedFor): Data
{
    override val name: String
        = node.id.iden
}