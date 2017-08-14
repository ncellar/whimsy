package norswap.uranium.java.model

// -------------------------------------------------------------------------------------------------
/*

Suppose a nested class `my.package.MyClass.MyNestedClass`.

It's *simple name* is:                  `MyNestedClass`
It's *binary name* (Class#getName) is:  `my.package.MyClass$MyNestedClass`
It's *canonical name* is:               `my.package.MyClass.MyNestedClass`
It's *internal name* is:                `my/package/MyClass$MyNestedClass`

 */
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