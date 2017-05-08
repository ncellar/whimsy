package norswap.lang.java8.resolution
import norswap.lang.java8.ast.Type
import norswap.lang.java8.typing.TType

// -------------------------------------------------------------------------------------------------

/**
 * Accessor for the "resolved" property.
 */
inline var Type.resolved: TType
    get()      = this["resolved"] as TType
    set(value) { this["resolved"] = value }

// -------------------------------------------------------------------------------------------------

/**
 * Returns the simple name corresponding to the given canonical name, i.e. its last component.
 */
fun cano_to_simple_name (cano_name: String): String
{
    val i = cano_name.lastIndexOfAny(charArrayOf('.', '$'))
    if (i == -1) return cano_name
    return cano_name.substring(i + 1)
}

// -------------------------------------------------------------------------------------------------