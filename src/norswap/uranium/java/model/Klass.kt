package norswap.uranium.java.model
import norswap.lang.java8.ast.CtorCall
import norswap.lang.java8.ast.TypeDecl
import norswap.uranium.java.model.scopes.*
import org.objectweb.asm.tree.ClassNode

// -------------------------------------------------------------------------------------------------

abstract class Klass: Member()
{
    abstract val scope: ClassScope
    abstract val binary_name: String
}

// -------------------------------------------------------------------------------------------------

class BytecodeClass (val node: ClassNode): Klass()
{
    override val name = binary_to_simple_name(node.name)
    override val binary_name = node.name
    override val scope = BytecodeClassScope(node)
}

// -------------------------------------------------------------------------------------------------

class SourceClass (val node: TypeDecl, val outer: BlockScope?, val outer_klass: Klass?, val file: FileScope): Klass()
{
    override val name = node.name
    override val binary_name = file.pkg.prefix + name
    override val scope = SourceClassScope(node, outer, outer_klass, file)
}

// -------------------------------------------------------------------------------------------------

class AnonymousSourceClass (val node: CtorCall, val outer: BlockScope?, val outer_klass: Klass, val file: FileScope, val index: Int): Klass()
{
    override val name = "" + index
    override val binary_name = outer_klass.binary_name + "$" + index
    override val scope = SourceClassScope(node, outer, outer_klass, file)
}

// -------------------------------------------------------------------------------------------------

class SourceAnnotation (val node: TypeDecl, val outer: BlockScope?, val outer_klass: Klass?, val file: FileScope): Klass()
{
    override val name = node.name
    override val binary_name = file.pkg.prefix + name
    override val scope = SourceAnnotationScope(node, outer, outer_klass, file)
}

// -------------------------------------------------------------------------------------------------

class ReflectionClass (val klass: Class<*>): Klass()
{
    override val name = klass.simpleName
    override val binary_name = klass.name
    override val scope = ReflectionClassScope(klass)
}

// -------------------------------------------------------------------------------------------------