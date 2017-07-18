package norswap.uranium.java.model2.bytecode
import norswap.uranium.java.model2.Constructor
import norswap.uranium.java.model2.binary_to_simple_name
import norswap.uranium.java.model2.Klass
import norswap.uranium.java.model2.internal_to_binary_name
import norswap.uranium.java.model2.bytecode.sig.parse_type_parameters
import norswap.utils.cast
import norswap.utils.multimap.MultiMap
import norswap.utils.multimap.flat_filter_values
import norswap.utils.multimap.multi_assoc_not_null
import norswap.utils.rangeTo
import org.objectweb.asm.Opcodes.ACC_ENUM
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InnerClassNode
import org.objectweb.asm.tree.MethodNode

class BytecodeClass (val node: ClassNode): Klass()
{
    // ---------------------------------------------------------------------------------------------

    override val binary_name
        = internal_to_binary_name(node.name)

    // ---------------------------------------------------------------------------------------------

    override val name
        = binary_to_simple_name(binary_name)

    // ---------------------------------------------------------------------------------------------

    override val static
        = node.access and ACC_STATIC != 0

    // ---------------------------------------------------------------------------------------------

    override val enum
        = node.access and ACC_ENUM != 0

    // ---------------------------------------------------------------------------------------------

    /**
     * The binary name of the superclass.
     */
    val superclass: String
        = internal_to_binary_name(node.superName)

    // ---------------------------------------------------------------------------------------------

    /**
     * The binary names of the  implemented interfaces.
     */
    val superinterfaces: List<String>
        = node.interfaces.of<String>().map { internal_to_binary_name(it) }

    // ---------------------------------------------------------------------------------------------

    // Latest versions of ASM on Maven do not include generic informations.
    inline private fun <reified T> List<*>.of()
        = this.cast<List<T>>()

    // ---------------------------------------------------------------------------------------------

    override val fields: Map<String, BytecodeField>
        = node.fields.of<FieldNode>().associate { it.name to BytecodeField(it) }

    // ---------------------------------------------------------------------------------------------

    override val static_fields: Map<String, BytecodeField>
        = fields.filterValues { it.static }

    // ---------------------------------------------------------------------------------------------

    override val methods: MultiMap<String, BytecodeMethod>
        = node.methods.of<MethodNode>().multi_assoc_not_null {
            (it.name != "<init>") .. { it.name to BytecodeMethod(it) }
        }

    // ---------------------------------------------------------------------------------------------

    override val static_methods: MultiMap<String, BytecodeMethod>
        = methods.flat_filter_values { it.static }

    // ---------------------------------------------------------------------------------------------

    override val constructors: List<Constructor>
        = node.methods.of<MethodNode>().mapNotNull {
           (it.name == "<init>") .. { BytecodeConstructor(it) }
        }

    // ---------------------------------------------------------------------------------------------

    // TODO does this work for anonymous and local classes as well?

    override val classes: Map<String, String>
        = node.innerClasses.of<InnerClassNode>()
            .associate { it.name to binary_name + "$" + it.name}

    // ---------------------------------------------------------------------------------------------

    override val static_classes: Map<String, String>
        = node.innerClasses.of<InnerClassNode>()
            .filter { it.access and ACC_STATIC != 0 }
            .associate { it.name to binary_name + "$" + it.name }

    // ---------------------------------------------------------------------------------------------

    override val type_parameters: Map<String, BytecodeTypeParameter>
        = parse_type_parameters(node.signature)

    // ---------------------------------------------------------------------------------------------

    override val enum_constants: Map<String, BytecodeField>?
        = enum .. { static_fields.filterValues { it.node.access and ACC_ENUM != 0 } }

    // ---------------------------------------------------------------------------------------------
}