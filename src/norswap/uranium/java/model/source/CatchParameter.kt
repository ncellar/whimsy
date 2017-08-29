package norswap.uranium.java.model.source

import norswap.lang.java8.ast.CatchClause
import norswap.uranium.java.model.Data

class CatchParameter (val node: CatchClause): Data
{
    override val name
        = node.id.iden
}