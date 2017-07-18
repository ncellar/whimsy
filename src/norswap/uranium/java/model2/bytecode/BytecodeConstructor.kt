package norswap.uranium.java.model2.bytecode
import norswap.uranium.java.model2.Constructor
import org.objectweb.asm.tree.MethodNode

class BytecodeConstructor (val node: MethodNode): Constructor()
{
    override val name
        = node.name
}