package norswap.lang.java8.scopes
import norswap.lang.java8.ast.TypeDeclKind
import norswap.lang.java8.typing.InstantiableType
import norswap.lang.java8.typing.MemberInfo

abstract class ClassScope: InstantiableType, Scope, MemberInfo
{
    // ---------------------------------------------------------------------------------------------

    abstract val canonical_name: String

    // ---------------------------------------------------------------------------------------------

    abstract val timestamp: Long

    // ---------------------------------------------------------------------------------------------

    open val chain: List<String>
        get() = canonical_name.replace('$', '.').split('.')

    // ---------------------------------------------------------------------------------------------

    var ambiguous_chain = false

    // ---------------------------------------------------------------------------------------------

    open val inner: Boolean
        get() = canonical_name.contains('$')

    // ---------------------------------------------------------------------------------------------

    abstract val kind: TypeDeclKind

    // ---------------------------------------------------------------------------------------------

    override val outer get() = _outer
    var _outer: Scope? = null

    // ---------------------------------------------------------------------------------------------

    override val field_node         = Scope.Node()
    override val method_node        = Scope.Node()
    override val class_like_node    = Scope.Node()
    override val type_param_node    = Scope.Node()

    // ---------------------------------------------------------------------------------------------
}