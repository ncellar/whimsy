package norswap.uranium.java.model2.bytecode
import norswap.uranium.java.model2.Parameter
import org.objectweb.asm.tree.ParameterNode

class BytecodeParameter (val node: ParameterNode): Parameter()
{
    override val name: String
        = node.name
}