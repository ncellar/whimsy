package norswap.uranium.java.model.bytecode
import norswap.uranium.java.model.Constructor
import org.objectweb.asm.tree.MethodNode

class BytecodeConstructor (val node: MethodNode): Constructor()
{
    override val name
        = node.name
}