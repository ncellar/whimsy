package norswap.lang.java8.resolution
import norswap.lang.java8.ast.ClassType
import norswap.lang.java8.ast.File
import norswap.lang.java8.ast.Import
import norswap.lang.java8.ast.TypeDecl
import norswap.lang.java8.ast.TypeDeclKind.*
import norswap.lang.java8.scopes.FileScope
import norswap.lang.java8.scopes.PackageScope
import norswap.lang.java8.scopes.Scope
import norswap.lang.java8.typing.TObject
import norswap.uranium.NodeVisitor
import norswap.utils.except
import norswap.uranium.Attribute
import norswap.uranium.Node
import norswap.uranium.Reactor
import norswap.uranium.Rule
import norswap.uranium.Reaction
import norswap.utils.inn
import kotlin.collections.listOf as list

// -------------------------------------------------------------------------------------------------

fun Reactor.install_java8_resolution_rules()
{
    val scope = ScopeBuilder()
    add_visitor(FileRule(scope))
    add_visitor(ImportRule(scope))
    add_visitor(ClassTypeRule(scope))
    add_visitor(TypeDeclRule(scope))
}

// -------------------------------------------------------------------------------------------------

abstract class ScopeRule <N: Node> (val scope: ScopeBuilder): Rule<N>()
{
    lateinit var sc: Scope

    override fun visit (node: N, begin: Boolean)
    {
        sc = scope.current
        super.visit(node, begin)
    }
}

// -------------------------------------------------------------------------------------------------

abstract class ResolutionRule <N: Node> (scope: ScopeBuilder): ScopeRule<N>(scope)
{
    override fun provided (node: N)
        = list(Attribute(node, "resolved"))
}

// -------------------------------------------------------------------------------------------------

abstract class ScopeContributor<N: Node> (val scope: ScopeBuilder): NodeVisitor<N>()

// -------------------------------------------------------------------------------------------------

abstract class ScopeMaker<N: Node> (scope: ScopeBuilder): ScopeContributor<N>(scope)
{
    abstract fun scope (node: N): Scope

    override fun visit (node: N, begin: Boolean)
    {
        if (begin)
            scope.push(scope(node))
        else
            scope.pop()
    }
}

// -------------------------------------------------------------------------------------------------

class FileRule (scope: ScopeBuilder): ScopeContributor<File>(scope)
{
    override fun visit (node: File, begin: Boolean)
    {
        if (begin) {
            val pkg = node.pkg
                .inn { PackageScope(it.name.joinToString(".")) }
                ?: PackageScope.Default
            scope.push(pkg)
            scope.push(FileScope(pkg))
        }
        else {
            scope.pop()
            scope.pop()
        }
    }
}

// -------------------------------------------------------------------------------------------------

class ImportRule (scope: ScopeBuilder): ScopeRule<Import>(scope)
{
    private fun attr_for_scope_nodes (node: Import, name: String): List<Attribute>
    {
        if (node.static) return list(
            Attribute(sc.field_node, name),
            Attribute(sc.method_node, name),
            Attribute(sc.class_like_node, name))
        else return list(
            Attribute(sc.class_like_node, name))
    }

    override fun provided (node: Import): List<Attribute>
    {
        if (!node.wildcard)
            return attr_for_scope_nodes(node, node.name.last())
        else
            return attr_for_scope_nodes(node, "+self")
    }

    override fun Reaction<Import>.compute()
    {
        if (node.static && node.wildcard)
        {
            val klass = Resolver.full_chain(node.name)
            if (klass != null) // TODO check
                klass.members().forEach { sc.put_member(it) }
        }
        else if (node.static)
        {
            val full_name = node.name.except(1).joinToString(".")
            val members = Resolver.resolve_members(full_name, node.name.last())
            members.forEach { sc.put_member(it) }
        }
        else if (node.wildcard)
        {
            val klass = Resolver.full_chain(node.name)
            if (klass != null) // TODO check
                klass.members().forEach { sc.put_member(it) }
        }
        else
        {
            val full_name = node.name.joinToString(".")
            val klass = Resolver.klass(full_name)!! // TODO hackfix
            sc.put_class_like(klass)
        }
    }
}

// -------------------------------------------------------------------------------------------------

class SuperclassRule (scope: ScopeBuilder): ScopeRule<TypeDecl>(scope)
{
    override fun provided (node: TypeDecl)
        = list(Attribute(node, "super_type"))

    override fun Reaction<TypeDecl>.compute()
    {
        when (node.kind) {
            ENUM        -> node["super_type"] = TObject
            INTERFACE   -> node["super_type"] = Attribute.None
            ANNOTATION  -> node["super_type"] = Attribute.None
            else        -> Unit
        }

        if (node.kind != CLASS) return

        if (node.extends.isEmpty())
            return run { node["super_type"] = TObject }

        val super_type = node.extends[0]
        if (super_type !is ClassType)
            return run { report(ExtendingNonClass(node)) }

        val super_name = super_type.parts.map { it.name }
        val superclass = Resolver.klass_chain(scope.current, super_name)
        if (superclass != null) // TODO check
            node["super_type"] = superclass
    }
}

// -------------------------------------------------------------------------------------------------

class TypeDeclRule (scope: ScopeBuilder): ScopeContributor<TypeDecl>(scope)
{
    override fun visit (node: TypeDecl, begin: Boolean)
    {
        if (!begin) scope.pop()

        val klass = SourceClassLike(scope.full_name(node.name), node)
        scope.current.put_class_like(klass)
        scope.push(klass)
    }
}

// -------------------------------------------------------------------------------------------------

class ClassTypeRule (scope: ScopeBuilder): ResolutionRule<ClassType>(scope)
{
    override val domain = list(ClassType::class.java)

    // TODO The problem with instanceof
    // - using package scope: will lookup java on scope, and continue
    //   - problem: we would like to see if there is not a result beyond that yet!
    //   - solution: catch the exception, try beyond, still throw
    // - using empty scope: will lookup java on scope, and fail
    //   - then lookup globally and finds it

    override fun Reaction<ClassType>.compute()
    {
        val name = node.parts.map { it.name }
        val klass = Resolver.klass_chain(sc, name)

        if (klass != null) // TODO check
            node["resolved"] = klass
    }
}

// -------------------------------------------------------------------------------------------------