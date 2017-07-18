package norswap.uranium.java.model.scopes
import norswap.lang.java8.ast.Package
import norswap.uranium.java.model.Klass

// null for default package
class PackageScope (val pkg: Package?)
{
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