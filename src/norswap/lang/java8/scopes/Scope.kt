package norswap.lang.java8.scopes
import norswap.lang.java8.typing.FieldInfo
import norswap.lang.java8.typing.MemberInfo
import norswap.lang.java8.typing.MethodInfo
import norswap.lang.java8.typing.RefType
import norswap.lang.java8.typing.TypeParameter
import norswap.uranium.Attribute
import norswap.uranium.CNode
import norswap.uranium.Context
import norswap.uranium.Reaction
import norswap.utils.cast
import norswap.utils.maybe_list

interface Scope
{
    // ---------------------------------------------------------------------------------------------

    open class Node: CNode()

    // ---------------------------------------------------------------------------------------------

    /**
     * A node that should always remain empty: it's [set] method throws an error.
     */
    object EmptyNode : Scope.Node()
    {
        override fun set(name: String, value: Any)
        {
            throw NotImplementedError("Don't set attributes of the empty scope node!")
        }
    }

    // ---------------------------------------------------------------------------------------------

    val outer: Scope?
        get() = null

    // ---------------------------------------------------------------------------------------------

    val field_node: Node
        get() = EmptyNode

    val method_node: Node
        get() = EmptyNode

    val class_like_node: Node
        get() = EmptyNode

    val type_param_node: Node
        get() = EmptyNode

    // ---------------------------------------------------------------------------------------------

    fun modifier (label: String, node: Scope.Node, name: String)
    {
        Reaction(node) {
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

    fun class_like (name: String): ClassScope?
    {
        val out: ClassScope? = class_like_node.raw(name).cast()
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

    fun class_likes(): Collection<ClassScope>
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

    fun put_class_like (value: ClassScope)
        { class_like_node[value.name] = value }

    fun put_param (value: TypeParameter)
        { type_param_node[value.name] = value }

    // ---------------------------------------------------------------------------------------------

    fun put_member (value: MemberInfo)
    {
        when (value) {
            is FieldInfo -> put_field      (value)
            is MethodInfo -> put_method     (value)
            is ClassScope -> put_class_like (value)
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun full_name (klass: String): String = outer!!.full_name(klass)
}