package norswap.lang.java8.resolution
import norswap.lang.java8.ast.TypeDecl
import norswap.lang.java8.ast.TypeDeclKind
import norswap.lang.java8.scopes.ClassScope
import norswap.lang.java8.typing.RefType
import norswap.lang.java8.typing.TypeParameter
import norswap.utils.cast

// -------------------------------------------------------------------------------------------------

class SourceTypeParameter: TypeParameter
{
    override val name: String
        get() = TODO()

    override val upper_bound: RefType
        get() = TODO()
}

// -------------------------------------------------------------------------------------------------

class SourceClassLike (override val canonical_name: String, val decl: TypeDecl): ClassScope()
{
    override val name = decl.name

    override val kind: TypeDeclKind
        get() = decl.kind

    override val super_type: RefType?
        get() = decl["super_type"].cast()

    override fun toString() = canonical_name
}

// -------------------------------------------------------------------------------------------------