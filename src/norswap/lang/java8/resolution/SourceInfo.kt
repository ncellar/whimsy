package norswap.lang.java8.resolution
import norswap.lang.java8.ast.TypeDecl
import norswap.lang.java8.ast.TypeDeclKind
import norswap.lang.java8.typing.ClassLike
import norswap.lang.java8.typing.FieldInfo
import norswap.lang.java8.typing.MethodInfo
import norswap.lang.java8.typing.RefType
import norswap.lang.java8.typing.TypeParameter
import norswap.utils.cast
import norswap.utils.multimap.MutableMultiMap

// -------------------------------------------------------------------------------------------------

class SourceTypeParameter: TypeParameter
{
    override val name: String
        get() = TODO()

    override val upper_bound: RefType
        get() = TODO()
}

// -------------------------------------------------------------------------------------------------

class SourceClassLike (override val full_name: String, val decl: TypeDecl): ClassLike, ScopeBase()
{
    override val name = decl.name

    override val kind: TypeDeclKind
        get() = decl.kind

    override val super_type: RefType?
        get() = decl["super_type"].cast()

    override val fields: MutableMap<String, FieldInfo>
        get() = decl["fields"].cast()

    override val methods: MutableMultiMap<String, MethodInfo>
        get() = decl["methods"].cast()

    override val class_likes: MutableMap<String, ClassLike>
        get() = decl["class_likes"].cast()

    override val type_params: MutableMap<String, TypeParameter>
        get() = decl["type_params"].cast()

    override fun toString() = full_name
}

// -------------------------------------------------------------------------------------------------