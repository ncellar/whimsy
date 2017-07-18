package norswap.uranium.java.model2
import norswap.uranium.java.model2.source.Scope

// null for default package
class Package (val pkg: norswap.lang.java8.ast.Package?): Scope
{
    // ---------------------------------------------------------------------------------------------

    // Not part of the normal static lookup chain, but delegated to by [File].
    override val outer = null

    // ---------------------------------------------------------------------------------------------

    val name: String
        get() = pkg?.name?.joinToString(".") ?: ""

    // ---------------------------------------------------------------------------------------------

    /**
     * - Prefix for forming the canonical/binary name of classes in the pacakge.
     * - Package name then a dot, or nothing if this is the default package.
     */
    val prefix: String
        get() = pkg?.name?.joinToString(".", postfix = ".") ?: ""

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps simple class name to their associated type.
     */
    val classes = HashMap<String, Klass>()

    // ---------------------------------------------------------------------------------------------
}