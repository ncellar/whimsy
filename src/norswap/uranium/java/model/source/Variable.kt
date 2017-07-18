package norswap.uranium.java.model.source
import norswap.lang.java8.ast.VarDecl
import norswap.lang.java8.ast.VarDeclarator
import norswap.uranium.java.model.Data

class Variable (val node: VarDecl, val decl: VarDeclarator): Data
{
    override val name = decl.id.iden
}