package norswap.lang.java8.resolution
import norswap.lang.java8.typing.ClassLike
import norswap.lang.java8.typing.FieldInfo
import norswap.lang.java8.typing.MemberInfo
import norswap.lang.java8.typing.MethodInfo
import norswap.lang.java8.typing.RefType
import norswap.lang.java8.typing.TypeParameter
import norswap.utils.maybe_list
import norswap.utils.multimap.*

// =================================================================================================

interface Scope
{
    // ---------------------------------------------------------------------------------------------

    fun field (name: String): FieldInfo?
        = null

    // also has <init>, <clinit>
    fun method (name: String): Collection<MethodInfo>
        = emptyList()

    fun class_like (name: String): ClassLike?
        = null

    fun type_param (name: String): TypeParameter?
        = null

    // ---------------------------------------------------------------------------------------------

    fun member (name: String): Collection<MemberInfo>
        = maybe_list(field(name)) + method(name) + maybe_list(class_like(name))

    fun type (name: String): RefType?
        = type_param(name) ?: class_like(name)

    // ---------------------------------------------------------------------------------------------

    fun fields(): Collection<FieldInfo>
        = emptyList()

    fun methods(): Collection<MethodInfo>
        = emptyList()

    fun class_likes(): Collection<ClassLike>
        = emptyList()

    fun type_params(): Collection<TypeParameter>
        = emptyList()

    // ---------------------------------------------------------------------------------------------

    fun members(): Collection<MemberInfo>
        = fields() + methods() + class_likes()

    fun types(): Collection<RefType>
        = type_params() + class_likes()

    // ---------------------------------------------------------------------------------------------

    fun put_field (name: String, value: FieldInfo): Unit
        = throw NotImplementedError()

    fun put_method (name: String, value: MethodInfo): Unit
        = throw NotImplementedError()

    fun put_class_like (name: String, value: ClassLike): Unit
        = throw NotImplementedError()

    fun put_param (name: String, value: TypeParameter): Unit
        = throw NotImplementedError()

    // ---------------------------------------------------------------------------------------------

    fun put_member (name: String, value: MemberInfo)
    {
        when (value) {
            is FieldInfo    -> put_field      (name, value)
            is MethodInfo   -> put_method     (name, value)
            is ClassLike    -> put_class_like (name, value)
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun full_name (klass: String): String
        = klass
}

// =================================================================================================

abstract class ScopeBase: Scope
{
    // ---------------------------------------------------------------------------------------------

    open val fields      : MutableMap<String, FieldInfo>       = HashMap()
    open val methods     : MutableMultiMap<String, MethodInfo> = HashMultiMap()
    open val class_likes : MutableMap<String, ClassLike>       = HashMap()
    open val type_params : MutableMap<String, TypeParameter>   = HashMap()

    // ---------------------------------------------------------------------------------------------

    override fun field (name: String)
        = fields[name]

    override fun method (name: String): Collection<MethodInfo>
        = methods[name] ?: emptyList<MethodInfo>()

    override fun class_like (name: String)
        = class_likes[name]

    override fun type_param (name: String)
        = type_params[name]

    // ---------------------------------------------------------------------------------------------

    override fun type (name: String)
        = type_params[name] ?: class_likes[name]

    // ---------------------------------------------------------------------------------------------

    override fun fields(): Collection<FieldInfo>
        = fields.values

    override fun methods(): Collection<MethodInfo>
        = methods.values.flatten()

    override fun class_likes(): Collection<ClassLike>
        = class_likes.values

    override fun type_params(): Collection<TypeParameter>
        = type_params.values

    // ---------------------------------------------------------------------------------------------

    override fun put_field (name: String, value: FieldInfo) {
        fields[name] = value
    }

    override fun put_method (name: String, value: MethodInfo) {
        methods.append(name, value)
    }

    override fun put_class_like (name: String, value: ClassLike) {
        class_likes[name] = value
    }

    override fun put_param(name: String, value: TypeParameter) {
        type_params[name] = value
    }
}

// =================================================================================================

object EmptyScope: Scope

// =================================================================================================

open class PackageScope (val name: String): Scope
{
    object Empty: PackageScope("") {
        override fun full_name (klass: String) = klass
    }

    override fun class_like (name: String)
        = Resolver.klass(full_name(name))

    override fun full_name (klass: String)
        = if (name == "") klass else "$name.$klass"
}

// =================================================================================================
