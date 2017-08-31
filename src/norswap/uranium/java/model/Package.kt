package norswap.uranium.java.model

// null for default package
class Package (val pkg: norswap.lang.java8.ast.Package?)
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
    val classes: Map<String, Klass>
        get() = TODO()

    // NOTE(norswap): The proper way to do this is a classpath + source class scan.

    // ---------------------------------------------------------------------------------------------
}