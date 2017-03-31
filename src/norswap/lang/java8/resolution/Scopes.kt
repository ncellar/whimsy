package norswap.lang.java8.resolution
import norswap.lang.java8.typing.ClassLike
import norswap.lang.java8.typing.RefType
import norswap.lang.java8.typing.TypeParameter
import norswap.utils.multimap.*

// -------------------------------------------------------------------------------------------------

interface Scope
{
    // ---------------------------------------------------------------------------------------------

    fun field (name: String): Lookup<FieldInfo>
        = Missing

    // also has <init>, <clinit>
    fun method (name: String): Lookup<List<MethodInfo>>
        = Missing

    fun class_like (name: String): Lookup<ClassLike>
        = Missing

    fun type_param (name: String): Lookup<TypeParameter>
        = Missing

    // ---------------------------------------------------------------------------------------------

    fun member (name: String): Lookup<Collection<MemberInfo>>
        = +field(name) + method(name) + +class_like(name)

    fun type (name: String): Lookup<RefType>
        = type_param(name).let { if (it !is Missing) it else class_like(name) }

    // ---------------------------------------------------------------------------------------------

    fun fields(): LookupList<FieldInfo>
        = Found(emptyList())

    fun methods(): LookupList<MethodInfo>
        = Found(emptyList())

    fun class_likes(): LookupList<ClassLike>
        = Found(emptyList())

    fun type_params(): LookupList<TypeParameter>
        = Found(emptyList())

    // ---------------------------------------------------------------------------------------------

    fun members(): LookupList<MemberInfo>
        = fields() + methods() + class_likes()

    fun types(): LookupList<RefType>
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

// -------------------------------------------------------------------------------------------------

abstract class ScopeBase: Scope
{
    // ---------------------------------------------------------------------------------------------

    open val fields      : MutableMap<String, FieldInfo>       = HashMap()
    open val methods     : MutableMultiMap<String, MethodInfo> = HashMultiMap()
    open val class_likes : MutableMap<String, ClassLike>       = HashMap()
    open val type_params : MutableMap<String, TypeParameter>   = HashMap()

    // ---------------------------------------------------------------------------------------------

    override fun field (name: String)
        = lookup_wrap(fields[name])

    override fun method (name: String)
        = lookup_wrap(methods[name] as List<MethodInfo>?)

    override fun class_like (name: String)
        = lookup_wrap(class_likes[name])

    override fun type_param (name: String)
        = lookup_wrap(type_params[name])

    // ---------------------------------------------------------------------------------------------

    override fun type (name: String)
        = lookup_wrap(type_params[name] ?: class_likes[name])

    // ---------------------------------------------------------------------------------------------

    override fun fields()
        = Found(fields.values.toList())

    override fun methods()
        = Found(methods.values.flatten())

    override fun class_likes()
        = Found(class_likes.values.toList())

    override fun type_params()
        = Found(type_params.values.toList())

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

    // ---------------------------------------------------------------------------------------------
}

// -------------------------------------------------------------------------------------------------

object EmptyScope: Scope

// -------------------------------------------------------------------------------------------------

class PackageScope (val name: String): Scope
{
    override fun class_like (name: String): Lookup<ClassLike>
        = Resolver.klass(full_name(name))

    override fun full_name (klass: String): String
        = if (name == "") klass else "$name.$klass"
}

// -------------------------------------------------------------------------------------------------

class FileScope: Scope

// -------------------------------------------------------------------------------------------------

