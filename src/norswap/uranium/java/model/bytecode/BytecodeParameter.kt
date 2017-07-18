package norswap.uranium.java.model.bytecode
import norswap.uranium.java.model.Parameter
import org.objectweb.asm.tree.ParameterNode

class BytecodeParameter (val node: ParameterNode): Parameter()
{
    override val name: String
        = node.name
}