package norswap.lang.java8.resolution
import norswap.lang.java8.typing.ClassLike
import norswap.lang.java8.typing.FieldInfo
import norswap.lang.java8.typing.MemberInfo
import norswap.lang.java8.typing.MethodInfo
import norswap.lang.java8.typing.RefType
import norswap.lang.java8.typing.TypeParameter
import norswap.uranium.Attribute
import norswap.uranium.CNode
import norswap.uranium.Context
import norswap.uranium.Node
import norswap.uranium.Reaction
import norswap.utils.cast
import norswap.utils.maybe_list

// =================================================================================================

open class ScopeNode: CNode()

// =================================================================================================

/**
 * A node that should always remain empty: it's [set] method throws an error.
 */
object EmptyScopeNode: ScopeNode()
{
    override fun set(name: String, value: Any)
    {
        throw NotImplementedError("Don't set attributes of the empty scope node!")
    }
}

// =================================================================================================

interface Scope
{
    // ---------------------------------------------------------------------------------------------

    val outer: Scope?
        get() = null

    // ---------------------------------------------------------------------------------------------

    val field_node: ScopeNode
        get() = EmptyScopeNode

    val method_node: ScopeNode
        get() = EmptyScopeNode

    val class_like_node: ScopeNode
        get() = EmptyScopeNode

    val type_param_node: ScopeNode
        get() = EmptyScopeNode

    // ---------------------------------------------------------------------------------------------

    fun modifier (label: String, node: ScopeNode, name: String)
    {
        Reaction (node) {
            this.label = label
            _pushed = Context.reaction
            _consumed = listOf(Attribute(node, name))
            _trigger = {}
            _optional = true
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun field (name: String): FieldInfo?
    {
        val out: FieldInfo? = field_node.raw(name).cast()
        if (out != null) return out
        modifier("Field Scope Update", field_node, name)
        return outer?.field(name)
    }

    // also has <init>, <clinit>
    fun method (name: String): Collection<MethodInfo>
    {
        val out: Collection<MethodInfo>? = method_node.raw(name).cast()
        if (out != null) return out
        modifier("Method Scope Update", method_node, name)
        return outer?.method(name) ?: emptyList<MethodInfo>()
    }

    fun class_like (name: String): ClassLike?
    {
        val out: ClassLike? = class_like_node.raw(name).cast()
        if (out != null) return out
        modifier("Class Scope Update", class_like_node, name)
        return outer?.class_like(name)
    }

    fun type_param (name: String): TypeParameter?
    {
        val out: TypeParameter? = type_param_node.raw(name).cast()
        if (out != null) return out
        modifier("Field Scope Update", type_param_node, name)
        return outer?.type_param(name)
    }

    // ---------------------------------------------------------------------------------------------

    fun member (name: String): Collection<MemberInfo>
        = maybe_list(field(name)) + method(name) + maybe_list(class_like(name))

    fun type (name: String): RefType?
        = type_param(name) ?: class_like(name)

    // ---------------------------------------------------------------------------------------------

    fun fields(): Collection<FieldInfo>
        = field_node.attrs.values.cast()

    fun methods(): Collection<MethodInfo>
        = (method_node.attrs.values.cast<Collection<Collection<MethodInfo>>>()).flatten()

    fun class_likes(): Collection<ClassLike>
        = class_like_node.attrs.values.cast()

    fun type_params(): Collection<TypeParameter>
        = type_param_node.attrs.values.cast()

    fun members(): Collection<MemberInfo>
        = fields() + methods() + class_likes()

    // ---------------------------------------------------------------------------------------------

    fun put_field (value: FieldInfo)
        { field_node[value.name] = value }

    fun put_method (value: MethodInfo) {
        val list = method_node.raw(value.name).cast<ArrayList<MethodInfo>?>() ?: ArrayList()
        list.add(value)
        method_node[value.name] = list
    }

    fun put_class_like (value: ClassLike)
        { class_like_node[value.name] = value }

    fun put_param (value: TypeParameter)
        { type_param_node[value.name] = value }

    // ---------------------------------------------------------------------------------------------

    fun put_member (value: MemberInfo)
    {
        when (value) {
            is FieldInfo    -> put_field      (value)
            is MethodInfo   -> put_method     (value)
            is ClassLike    -> put_class_like (value)
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun full_name (klass: String): String? = outer?.full_name(klass)
}

// =================================================================================================

abstract class ScopeBase: Scope
{
    // ---------------------------------------------------------------------------------------------

    override val outer get() = _outer
    var _outer: Scope? = null

    // ---------------------------------------------------------------------------------------------

    override val field_node         = ScopeNode()
    override val method_node        = ScopeNode()
    override val class_like_node    = ScopeNode()
    override val type_param_node    = ScopeNode()

    // ---------------------------------------------------------------------------------------------
}

// =================================================================================================

object EmptyScope: Scope

// =================================================================================================

open class PackageScope (val name: String): Scope
{
    // ---------------------------------------------------------------------------------------------

    object Default: PackageScope("") {
        override fun full_name (klass: String) = klass
    }

    // ---------------------------------------------------------------------------------------------

    override fun full_name (klass: String)
        = "$name.$klass"

    // ---------------------------------------------------------------------------------------------

    override val class_like_node = ScopeNode()

    // ---------------------------------------------------------------------------------------------

    override fun class_like (name: String): ClassLike?
    {
        var klass = class_like_node.raw(name)
        if (klass != null) return klass as ClassLike

        klass = Resolver.klass(full_name(name), class_like_node)

        if (klass == null) {
            // TODO this should only be registered once
            // - what to look for, consumers?
            // - what inside them?
            // - something that relates them to the attribute being derived
            // - so current reaction in pushed?
            // - must access current reaction...  -> can be done through context
            // - the issue is that rules may be re-run, if in case of repeated misses, don't want
            //   to re-register modifier
            // - problem: lookup cost: sifting through consumers

            //val consumers = class_like_node.consumers[name]?.contains(null)
            modifier("Package Scope Update", class_like_node, name)
        }


        return klass
    }

    // ---------------------------------------------------------------------------------------------

    override fun field (name: String) = null
    override fun method (name: String) = emptyList<MethodInfo>()
    override fun type_param (name: String) = null

    // ---------------------------------------------------------------------------------------------
}

// =================================================================================================

open class FileScope: Scope

// =================================================================================================
