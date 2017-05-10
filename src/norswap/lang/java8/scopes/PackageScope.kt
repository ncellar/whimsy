package norswap.lang.java8.scopes
import norswap.lang.java8.resolution.Resolver
import norswap.lang.java8.typing.MethodInfo
import norswap.uranium.Context

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

    override val class_like_node = Scope.Node()

    // ---------------------------------------------------------------------------------------------

    override fun class_like (name: String): ClassScope?
    {
        var klass = class_like_node.raw(name)
        if (klass != null) return klass as ClassScope

        klass = Resolver.klass(full_name(name), class_like_node)
        if (klass != null) return klass

        val consumers = class_like_node.consumers[name]
        if (consumers == null) {
            modifier("Class Scope Update (Package)", class_like_node, name)
            return null
        }

        val cur_reac = Context.reaction
        if (consumers.none { it.pushed == cur_reac }) {
            modifier("Class Scope Update (Package)", class_like_node, name)
            return null
        }

        return null
    }

    // ---------------------------------------------------------------------------------------------

    override fun field (name: String) = null
    override fun method (name: String) = emptyList<MethodInfo>()
    override fun type_param (name: String) = null

    // ---------------------------------------------------------------------------------------------
}