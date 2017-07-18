package norswap.uranium.java.model.bytecode
import norswap.uranium.java.model.Method
import norswap.uranium.java.model.TypeParameter
import norswap.uranium.java.model.bytecode.sig.parse_type_parameters
import norswap.utils.cast
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.ParameterNode

class BytecodeMethod (val node: MethodNode): Method()
{
    // ---------------------------------------------------------------------------------------------

    // Latest versions of ASM on Maven do not include generic informations.
    inline private fun <reified T> List<*>.of()
        = this.cast<List<T>>()

    // ---------------------------------------------------------------------------------------------

    override val name
        = node.name

    // ---------------------------------------------------------------------------------------------

    override val static
        = node.access and Opcodes.ACC_STATIC != 0

    // ---------------------------------------------------------------------------------------------

    override val parameters: Map<String, BytecodeParameter>
        = node.parameters.of<ParameterNode>().associate { it.name to BytecodeParameter(it) }

        // ---------------------------------------------------------------------------------------------

    override val type_params: Map<String, TypeParameter>
        = parse_type_parameters(node.signature)

    // ---------------------------------------------------------------------------------------------
}