package norswap.uranium.java.model
import norswap.lang.java8.ast.ConstructorDecl
import norswap.uranium.java.model.scopes.BlockScope
import norswap.uranium.java.model.scopes.MethodScope
import org.objectweb.asm.tree.MethodNode

// -------------------------------------------------------------------------------------------------

abstract class Constructor: Member()

// -------------------------------------------------------------------------------------------------

class BytecodeConstructor (val node: MethodNode): Constructor()
{
    override val name = node.name
}

// -------------------------------------------------------------------------------------------------

class SourceConstructor (val node: ConstructorDecl, outer: BlockScope?, val klass: Klass): Constructor()
{
    override val name = node.name
    val scope = MethodScope(node, outer, klass)
}

// -------------------------------------------------------------------------------------------------

class ReflectionConstructor (val field: java.lang.reflect.Constructor<*>): Constructor()
{
    override val name = field.name
}

// -------------------------------------------------------------------------------------------------