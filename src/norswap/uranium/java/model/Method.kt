package norswap.uranium.java.model
import norswap.lang.java8.ast.MethodDecl
import org.objectweb.asm.tree.MethodNode

// -------------------------------------------------------------------------------------------------

abstract class Method: Member()

// -------------------------------------------------------------------------------------------------

class BytecodeMethod (node: MethodNode): Method()
{
    override val name = node.name!!
}

// -------------------------------------------------------------------------------------------------

class SourceMethod (node: MethodDecl): Method()
{
    override val name = node.name
}

// -------------------------------------------------------------------------------------------------

class ReflectionMethod (field: java.lang.reflect.Method): Method()
{
    override val name = field.name!!
}

// -------------------------------------------------------------------------------------------------