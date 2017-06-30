package norswap.uranium.java.model

// -------------------------------------------------------------------------------------------------

/**
 * Returns the simple name corresponding to the given binary name, i.e. its last component.
 */
fun binary_to_simple_name (name: String): String
{
    val i = name.lastIndexOfAny(charArrayOf('.', '$'))
    if (i == -1) return name
    return name.substring(i + 1)
}

// -------------------------------------------------------------------------------------------------

/**
 * Returns the binary name corresponding to the given internal name.
 */
fun internal_to_binary_name (name: String): String
    = name.replace('/', '.')

// -------------------------------------------------------------------------------------------------