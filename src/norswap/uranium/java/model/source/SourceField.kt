package norswap.uranium.java.model.source
import norswap.lang.java8.ast.Keyword
import norswap.lang.java8.ast.VarDecl
import norswap.lang.java8.ast.VarDeclarator
import norswap.uranium.java.model.Field

class SourceField (val node: VarDecl, val decl: VarDeclarator): Field()
{
    override val name
        = decl.id.iden

    override val static
        = node.mods.contains(Keyword.static)
}