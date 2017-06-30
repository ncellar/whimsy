package norswap.uranium.java.scopes
import norswap.uranium.java.model.*
import norswap.uranium.java.resolution.Resolver
import norswap.utils.assoc_not_null
import norswap.utils.cast
import norswap.utils.inn
import norswap.utils.maybe_list
import norswap.utils.multimap.*
import org.objectweb.asm.tree.*

class BytecodeClassScope (val node: ClassNode): ClassScope()
{
    // ---------------------------------------------------------------------------------------------

    val binary_name = internal_to_binary_name(node.name)

    // ---------------------------------------------------------------------------------------------

    inline private fun <reified T> List<*>.of() = this.cast<List<T>>()

    // ---------------------------------------------------------------------------------------------

    private val fmap: Map<String, BytecodeField> by lazy {
        node.fields.of<FieldNode>().associate { it.name to BytecodeField(it) }
    }

    // ---------------------------------------------------------------------------------------------

    private val mmap: MultiMap<String, BytecodeMethod> by lazy {
        node.methods.of<MethodNode>().multi_assoc { it.name to BytecodeMethod(it) }
    }

    // ---------------------------------------------------------------------------------------------

    private val cmap: Map<String, Klass> by lazy {
        node.innerClasses.of<InnerClassNode>().assoc_not_null {
            val klass = Resolver.load_class(binary_name + "$" + it.name)
            klass.inn { it.name to it }
        }
    }

    // ---------------------------------------------------------------------------------------------

    val superclass = Resolver.load_class(node.superName.replace('/', '.'))

    // ---------------------------------------------------------------------------------------------

    override fun field (name: String): Field?
        = fmap[name] ?: superclass?.scope?.field(name)

    // ---------------------------------------------------------------------------------------------

    override fun method (name: String): List<Method>
        = mmap[name]
            ?: superclass?.scope?.method(name)
            ?: emptyList<Method>()

    // ---------------------------------------------------------------------------------------------

    override fun klass (name: String): Klass?
        = cmap[name] ?: superclass?.scope?.klass(name)

    // ---------------------------------------------------------------------------------------------

    override fun member(name: String): List<Member>
        = method(name) + maybe_list(field(name)) + maybe_list(klass(name))

    // ---------------------------------------------------------------------------------------------
}