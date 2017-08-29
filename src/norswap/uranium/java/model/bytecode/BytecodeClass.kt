package norswap.uranium.java.model.bytecode
import norswap.uranium.java.Context
import norswap.uranium.java.model.Constructor
import norswap.uranium.java.model.binary_to_simple_name
import norswap.uranium.java.model.Klass
import norswap.uranium.java.model.internal_to_binary_name
import norswap.uranium.java.model.bytecode.sig.parse_type_parameters
import norswap.utils.cast
import norswap.utils.doesnt_throw
import norswap.utils.multimap.MultiMap
import norswap.utils.multimap.flat_filter_values
import norswap.utils.multimap.multi_assoc_not_null
import norswap.utils.rangeTo
import org.objectweb.asm.Opcodes.ACC_ANNOTATION
import org.objectweb.asm.Opcodes.ACC_ENUM
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ACC_INTERFACE
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

    override val is_nested
        get() = node.outerClass != null

    // ---------------------------------------------------------------------------------------------

    override val static
        get() = node.access and ACC_STATIC != 0

    // ---------------------------------------------------------------------------------------------

    override val is_enum
        get() = node.access and ACC_ENUM != 0

    // ---------------------------------------------------------------------------------------------

    override val is_interface
        get() = node.access and ACC_INTERFACE != 0

    // ---------------------------------------------------------------------------------------------

    override val is_annotation
        get() = node.access and ACC_ANNOTATION != 0

    // ---------------------------------------------------------------------------------------------

    override val is_local
        get() = node.outerMethod != null && !is_anonymous

    // ---------------------------------------------------------------------------------------------

    override val is_anonymous
        get() = doesnt_throw { name.toInt() }

    // ---------------------------------------------------------------------------------------------

    /**
     * The binary name of the superclass.
     */
    val superclass: String
        = internal_to_binary_name(node.superName)

    // ---------------------------------------------------------------------------------------------

    /**
     * The binary name of the outer class, if any, or null.
     */
    val outer_class: String?
        = node.outerClass?.let(::internal_to_binary_name)

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

    // NOTE(norswap): this works with anonymous and local classes as well
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
        = node.signature?.let(::parse_type_parameters) ?: emptyMap()

    // ---------------------------------------------------------------------------------------------

    override val enum_constants: Map<String, BytecodeField>?
        = is_enum.. { static_fields.filterValues { it.node.access and ACC_ENUM != 0 } }

    // ---------------------------------------------------------------------------------------------

    override fun superclass (ctx: Context): Klass
        =  ctx.resolver.load_superclass(superclass, binary_name)

    // ---------------------------------------------------------------------------------------------

    override fun superinterfaces (ctx: Context): List<Klass>
    {
        val list = ArrayList<Klass>()
        superinterfaces.forEach {
            ctx.resolver.load_superinterface(it, binary_name)
                ?. let { list.add(it) }
        }
        return list
    }

    // ---------------------------------------------------------------------------------------------

    override fun outer_class (ctx: Context): Klass?
        = outer_class?.let { ctx.resolver.load_outer_class(it, binary_name) }

    // ---------------------------------------------------------------------------------------------
}