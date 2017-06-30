package norswap.uranium.java.model
import norswap.lang.java8.ast.TypeDecl
import norswap.uranium.java.scopes.*
import org.objectweb.asm.tree.ClassNode

// -------------------------------------------------------------------------------------------------

abstract class Klass: Member()
{
    abstract val scope: ClassScope
}

// -------------------------------------------------------------------------------------------------

class BytecodeClass (node: ClassNode): Klass()
{
    override val name = binary_to_simple_name(node.name)
    override val scope = BytecodeClassScope(node)
}

// -------------------------------------------------------------------------------------------------

class SourceClass (node: TypeDecl): Klass()
{
    override val name = node.name
    override val scope = SourceClassScope(node)
}

// -------------------------------------------------------------------------------------------------

class ReflectionClass (klass: Class<*>): Klass()
{
    override val name = klass.simpleName!!
    override val scope = ReflectionClassScope(klass)
}

// -------------------------------------------------------------------------------------------------