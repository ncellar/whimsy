package norswap.uranium.java.model.source
import norswap.lang.java8.ast.TryResource
import norswap.uranium.java.model.Data

class TryParameter (val node: TryResource): Data
{
    override val name
        = node.id.iden
}