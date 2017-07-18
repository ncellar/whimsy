package norswap.uranium.java.model2.source
import norswap.lang.java8.ast.TryResource
import norswap.uranium.java.model2.Data

class TryParameter (val node: TryResource): Data
{
    override val name
        = node.id.iden
}