package norswap.uranium.java.model2.source
import norswap.lang.java8.ast.Keyword
import norswap.lang.java8.ast.VarDecl
import norswap.lang.java8.ast.VarDeclarator
import norswap.uranium.java.model2.Field

class SourceField (val node: VarDecl, val decl: VarDeclarator): Field()
{
    override val name
        = decl.id.iden

    override val static
        = node.mods.contains(Keyword.static)
}