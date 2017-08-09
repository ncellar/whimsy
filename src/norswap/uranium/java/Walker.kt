package norswap.uranium.java
import norswap.lang.Node
import norswap.uranium.java.WalkerJavaSupport.invoke
import norswap.utils.cast
import norswap.utils.str
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method

// ---------------------------------------------------------------------------------------------

@FunctionalInterface
interface NodeProvider {
    fun apply (node: Node): Node?
}

// ---------------------------------------------------------------------------------------------

@FunctionalInterface
interface CollProvider {
    fun apply (node: Node): Iterable<Node>?
}

// ---------------------------------------------------------------------------------------------

/**
 * Converts a node accessor ([method]) into a fast compiled [NodeProvider].
 */
private fun lambda_direct (lookup: MethodHandles.Lookup, method: Method): NodeProvider
{
    val handle = lookup.unreflect(method)

    val site = LambdaMetafactory.metafactory(
        lookup, "apply",
        MethodType.methodType(NodeProvider::class.java),
        MethodType.methodType(Node::class.java, Node::class.java),
        handle, handle.type())

    return invoke(site.target) as NodeProvider
}

// ---------------------------------------------------------------------------------------------

/**
 * Converts an accessor for a collection of nodes ([method]) into a fast compiled [CollProvider].
 */
private fun lambda_coll (lookup: MethodHandles.Lookup, method: Method): CollProvider
{
    val handle = lookup.unreflect(method)

    val site = LambdaMetafactory.metafactory(
        lookup, "apply",
        MethodType.methodType(CollProvider::class.java),
        MethodType.methodType(Iterable::class.java, Node::class.java),
        handle, handle.type())

    return invoke(site.target) as CollProvider
}

// -------------------------------------------------------------------------------------------------

/**
 * [Type] instance described a collection of nodes.
 */
private val NODE_COLLECTION = GenericType(Collection::class.java, Node::class.java)

// -------------------------------------------------------------------------------------------------

/**
 * Stores compiled field accessors for a given [Node] subclass.
 */
private class ClassData (lookup: MethodHandles.Lookup, klass: Class<*>)
{
    val directs = ArrayList<NodeProvider>()
    val colls = ArrayList<CollProvider>()

    init {
        for (field in klass.methods) // methods, because kotlin generates setters
        {
            // small perf boost
            field.isAccessible = true

            // we only want getters
            if (field.parameterCount != 0) continue

            // filter out auto Kotlin data class fields
            if (field.name.startsWith("component")) continue

            if (field.returnType iz Node::class.java) {
                directs.add(lambda_direct(lookup, field))
                continue
            }

            if (field.genericReturnType iz NODE_COLLECTION) {
                colls.add(lambda_coll(lookup, field))
                continue
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Walks over the given node, by applying the function parameter to all its children nodes.
 */
class JavaWalker: (Node, (Node) -> Unit) -> Unit
{
    private val lookup = MethodHandles.lookup()
    private val cdatas = HashMap<Class<*>, ClassData>()

    override fun invoke (node: Node, f: (Node) -> Unit)
    {
        val klass = node.javaClass
        val data = cdatas.getOrPut(klass) { ClassData(lookup, klass) }

        if (node is Array<*>) println(node.str)

        data.directs.forEach {
            it.apply(node)?.let { f(it.cast()) }
        }

        data.colls.forEach {
            it.apply(node)?.forEach(f)
        }
    }
}

// -------------------------------------------------------------------------------------------------