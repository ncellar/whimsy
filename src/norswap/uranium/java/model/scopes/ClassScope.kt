package norswap.uranium.java.model.scopes
import norswap.lang.Node
import norswap.lang.java8.ast.AnnotationElemDecl
import norswap.lang.java8.ast.TypeParam
import norswap.uranium.java.model.BytecodeConstructor
import norswap.uranium.java.model.BytecodeField
import norswap.uranium.java.model.BytecodeMethod
import norswap.uranium.java.model.Constructor
import norswap.uranium.java.model.Field
import norswap.uranium.java.model.Klass
import norswap.uranium.java.model.Method
import norswap.uranium.java.model.ReflectionConstructor
import norswap.uranium.java.model.ReflectionField
import norswap.uranium.java.model.ReflectionMethod
import norswap.uranium.java.model.SourceConstructor
import norswap.uranium.java.model.SourceMethod
import norswap.uranium.java.model.TypeParameter
import norswap.uranium.java.model.internal_to_binary_name
import norswap.utils.cast
import norswap.utils.rangeTo
import norswap.utils.multimap.HashMultiMap
import norswap.utils.multimap.MultiMap
import norswap.utils.multimap.empty_multimap
import norswap.utils.multimap.multi_assoc
import norswap.utils.multimap.multi_assoc_not_null
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InnerClassNode
import org.objectweb.asm.tree.MethodNode
import java.lang.reflect.TypeVariable

// =================================================================================================

abstract class ClassScope
{
    // ---------------------------------------------------------------------------------------------

    abstract val fields: Map<String, Field>

    // ---------------------------------------------------------------------------------------------

    abstract val methods: MultiMap<String, Method>

    // ---------------------------------------------------------------------------------------------

    abstract val constructors: List<Constructor>

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps the simple name of the inner classes to their binary name.
     */
    abstract val classes: Map<String, String>

    // ---------------------------------------------------------------------------------------------
}

// =================================================================================================

class BytecodeClassScope (val node: ClassNode): ClassScope()
{
    // ---------------------------------------------------------------------------------------------

    inline private fun <reified T> List<*>.of()
        = this.cast<List<T>>()

    // ---------------------------------------------------------------------------------------------

    val binary_name
        = internal_to_binary_name(node.name)

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

    val type_params: List<TypeParameter>
        = parse_class_signature(node.signature)

    // ---------------------------------------------------------------------------------------------

    override val fields: Map<String, Field>
        = node.fields.of<FieldNode>().associate { it.name to BytecodeField(it) }

    // ---------------------------------------------------------------------------------------------

    override val methods: MultiMap<String, Method>
        = node.methods.of<MethodNode>().multi_assoc_not_null {
            (it.name != "<init>") .. { it.name to BytecodeMethod(it) }
        }

    // ---------------------------------------------------------------------------------------------

    override val constructors: List<Constructor>
        = node.methods.of<MethodNode>().mapNotNull {
            (it.name == "<init>") .. { BytecodeConstructor(it) }
        }

    // ---------------------------------------------------------------------------------------------

    override val classes: Map<String, String>
        = node.innerClasses.of<InnerClassNode>()
            .associate { it.name to binary_name + "$" + it.name}

    // ---------------------------------------------------------------------------------------------
}

// =================================================================================================

class ReflectionClassScope (val klass: Class<*>): ClassScope()
{
    // ---------------------------------------------------------------------------------------------

    // TODO not needed?
    val superclass: Class<*>
        = klass.superclass

    // ---------------------------------------------------------------------------------------------

    // TODO not needed?
    val superinterfaces: List<Class<*>>
        = klass.interfaces.toList()

    // ---------------------------------------------------------------------------------------------

    // TODO not needed?
    val type_params: List<TypeVariable<*>>
        = klass.typeParameters.toList()

    // ---------------------------------------------------------------------------------------------

    override val fields: Map<String, ReflectionField>
        = klass.declaredFields.associate { it.name to ReflectionField(it) }

    // ---------------------------------------------------------------------------------------------

    override val methods: MultiMap<String, ReflectionMethod>
        = klass.declaredMethods.multi_assoc { it.name to ReflectionMethod(it) }

    // ---------------------------------------------------------------------------------------------

    override val constructors: List<Constructor>
        = klass.constructors.map { ReflectionConstructor(it) }

    // ---------------------------------------------------------------------------------------------

    override val classes: Map<String, String>
        = klass.declaredClasses.associate { it.simpleName to it.name }

    // ---------------------------------------------------------------------------------------------
}

// =================================================================================================

class SourceClassScope (
    val node: Node, val outer: BlockScope?, val outer_klass: Klass?, val file: FileScope)
    : ClassScope()
{
    // ---------------------------------------------------------------------------------------------

    // Constructed by visitors.
    val type_params = HashMap<String, TypeParam>()

    // ---------------------------------------------------------------------------------------------

    // Constructed by visitors.
    override val fields =  HashMap<String, Field>()

    // ---------------------------------------------------------------------------------------------

    // Constructed by visitors.
    override val methods = HashMultiMap<String, SourceMethod>()

    // ---------------------------------------------------------------------------------------------

    // Constructed by visitors.
    override val constructors = ArrayList<SourceConstructor>()

    // ---------------------------------------------------------------------------------------------

    // Constructed by visitors.
    override val classes =  HashMap<String, String>()

    // ---------------------------------------------------------------------------------------------
}

// =================================================================================================

// NOTE: enumerations can't appear inside a scope! do this for better diagnostics in case of error
class SourceAnnotationScope(
    val node: Node, val outer: BlockScope?, val outer_klass: Klass?, val file: FileScope)
    : ClassScope()
{
    // ---------------------------------------------------------------------------------------------

    override val fields         get() = emptyMap<String, Field>()
    override val methods        get() = empty_multimap<String, Method>()
    override val constructors   get() = emptyList<Constructor>()
    override val classes        get() = emptyMap<String, String>()

    // ---------------------------------------------------------------------------------------------

    // Constructed by visitors.
    val elements = HashMap<String, AnnotationElemDecl>()

    // ---------------------------------------------------------------------------------------------
}

// =================================================================================================