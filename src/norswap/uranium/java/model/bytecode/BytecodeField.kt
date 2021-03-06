package norswap.uranium.java.model.bytecode
import norswap.uranium.java.model.Field
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.tree.FieldNode

class BytecodeField (val node: FieldNode): Field()
{
    override val name
        = node.name

    override val static
        = node.access and ACC_STATIC != 0
}