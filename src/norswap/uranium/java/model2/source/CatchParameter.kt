package norswap.uranium.java.model2.source
import norswap.lang.java8.ast.CatchClause
import norswap.uranium.java.model2.Data

class CatchParameter (val node: CatchClause): Data
{
    override val name
        = node.id.iden
}