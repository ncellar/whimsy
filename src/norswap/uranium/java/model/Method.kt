package norswap.uranium.java.model
import norswap.lang.java8.ast.MethodDecl
import norswap.uranium.java.model.scopes.BlockScope
import norswap.uranium.java.model.scopes.MethodScope
import org.objectweb.asm.tree.MethodNode

// -------------------------------------------------------------------------------------------------

abstract class Method: Member()

// -------------------------------------------------------------------------------------------------

class BytecodeMethod (val node: MethodNode): Method()
{
    override val name = node.name
}

// -------------------------------------------------------------------------------------------------

class SourceMethod (val node: MethodDecl, outer: BlockScope?, val klass: Klass): Method()
{
    override val name = node.name
    val scope = MethodScope(node, outer, klass)
}

// -------------------------------------------------------------------------------------------------

class ReflectionMethod (val field: java.lang.reflect.Method): Method()
{
    override val name = field.name
}

// -------------------------------------------------------------------------------------------------