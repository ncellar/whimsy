package norswap.uranium.java.scopes
import norswap.lang.java8.ast.TypeDecl
import norswap.uranium.java.model.*
import norswap.utils.multimap.*

class SourceClassScope (val node: TypeDecl): ClassScope()
{
    // ---------------------------------------------------------------------------------------------

    // Constructed by visitors.
    internal val fmap =  HashMap<String, SourceField>()

    // ---------------------------------------------------------------------------------------------

    // Constructed by visitors.
    internal val mmap = HashMultiMap<String, SourceMethod>()

    // ---------------------------------------------------------------------------------------------

    // Constructed by visitors.
    internal val cmap =  HashMap<String, Klass>()

    // ---------------------------------------------------------------------------------------------

    // TODO
    val superclass = node.extends[0]

    // ---------------------------------------------------------------------------------------------

    override fun field(name: String): Field? {
        TODO()
    }

    // ---------------------------------------------------------------------------------------------

    override fun method(name: String): List<Method> {
        TODO()
    }

    // ---------------------------------------------------------------------------------------------

    override fun klass(name: String): Klass? {
        TODO()
    }

    // ---------------------------------------------------------------------------------------------

    override fun member(name: String): List<Member> {
        TODO()
    }

    // ---------------------------------------------------------------------------------------------
}