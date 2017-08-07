package norswap.uranium.java
import norswap.lang.Node
import norswap.utils.cast

fun java_walker (node: Node): List<Node>
{
    val children = ArrayList<Node>()
    val fields = node.javaClass.methods.filter { it.parameterCount == 0 }

    val node_col = GenericType(Collection::class.java, Node::class.java)
    val pair     = GenericType(Pair::class.java, Object::class.java, Node::class.java)
    val pair_col = GenericType(Collection::class.java, pair)

    // direct Node members
    fields
        .filter { it.returnType iz Node::class.java }
        .map { it.invoke(node) as Node? }
        .filterNotNull()
        .toCollection(children)

    // Collection<Node> members
    fields
        .filter { it.genericReturnType iz node_col }
        .map { it.invoke(node).cast<Collection<Node>?>() }
        .filterNotNull()
        .forEach { children.addAll(it) }

    // List<Pair<Object, Node>> members
    fields
        .filter { it.genericReturnType iz pair_col }
        .flatMap { it.invoke(node).cast<List<Pair<*, Node>>>() }
        .mapTo (children) { it.second }

    return children
}

