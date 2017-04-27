package norswap.lang.java8.resolution
import norswap.lang.java8.ast.ClassType
import norswap.lang.java8.ast.File
import norswap.lang.java8.ast.Import
import norswap.lang.java8.ast.TypeDecl
import norswap.lang.java8.ast.TypeDeclKind.*
import norswap.lang.java8.typing.TObject
import norswap.uranium.NodeVisitor
import norswap.utils.except
import norswap.uranium.Attribute
import norswap.uranium.Node
import norswap.uranium.Reactor
import norswap.uranium.Rule
import norswap.uranium.Reaction
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

abstract class ResolutionRule <N: Node>: Rule<N>()
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
            if (node.pkg != null)
                scope.push(PackageScope(node.pkg.name.joinToString(".")))
                scope.push(FileScope())
        }
        else {
            if (node.pkg != null) scope.pop()
            scope.pop()
        }
    }
}

// -------------------------------------------------------------------------------------------------

class ImportRule (scope: ScopeBuilder): ScopeContributor<Import>(scope)
{
    override fun visit (node: Import, begin: Boolean)
    {
        // TODO this is visitor, not a rule!
        // TODO rewrite as incremental, visitor-integrated

        if (node.static && node.wildcard)
        {
            val klass = Resolver.full_chain(node.name)
            klass.members().forEach { scope.current.put_member(it.name, it) }
        }
        else if (node.static)
        {
            val full_name = node.name.except(1).joinToString(".")
            val members = Resolver.resolve_members(full_name, node.name.last())
            members.forEach { scope.current.put_member(it.name, it) }
        }
        else if (node.wildcard)
        {
            val klass = Resolver.full_chain(node.name)
            klass.members().forEach { scope.current.put_member(it.name, it) }
        }
        else
        {
            val full_name = node.name.joinToString(".")
            val klass = Resolver.klass(full_name)
            scope.current.put_class_like(node.name.last(), klass)
        }
    }
}

// -------------------------------------------------------------------------------------------------

class SuperclassRule (val scope: ScopeBuilder): Rule<TypeDecl>()
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
            return run { report(::ExtendingNonClass) }

        val super_name = super_type.parts.map { it.name }
        val superclass = Resolver.klass_chain(scope.current, super_name)
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
        scope.current.put_class_like(klass.name, klass)
        scope.push(klass)
    }
}

// -------------------------------------------------------------------------------------------------

class ClassTypeRule (val scope: ScopeBuilder): ResolutionRule<ClassType>()
{
    override val domain = list(ClassType::class.java)

    override fun Reaction<ClassType>.compute()
    {
        val name = node.parts.map { it.name }.joinToString(".")
        val klass = Resolver.klass(name)
        node["resolved"] = klass
    }
}

// -------------------------------------------------------------------------------------------------