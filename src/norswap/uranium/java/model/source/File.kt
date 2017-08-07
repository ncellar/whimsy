package norswap.uranium.java.model.source
import norswap.uranium.java.model.Package
import norswap.utils.plusAssign

class File (val file: norswap.lang.java8.ast.File, var pkg: Package): Scope
{
    // ---------------------------------------------------------------------------------------------

    override val outer = null

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps imported simple class names to their canonical names.
     */
    val single_imports = HashMap<String, String>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps imported names (may represent a static class, field or method) to their canonical names).
     */
    val single_static_imports = HashMap<String, String>()

    // ---------------------------------------------------------------------------------------------

    /**
     * List of wildcard-imported packages.
     */
    val wildcard_imports = ArrayList<String>()

    // ---------------------------------------------------------------------------------------------

    /**
     * List of static wildcard-imported packages.
     */
    val wildcard_static_imports = ArrayList<String>()

    // ---------------------------------------------------------------------------------------------

    override fun toString(): String
    {
        val b = StringBuilder()

        if (pkg.pkg != null)
            b += "package " + pkg.name

        single_imports          .forEach { b += "import " + it.value + "\n" }
        single_static_imports   .forEach { b += "import " + it.value + "\n" }
        wildcard_imports        .forEach { b += "import " + it + "\n" }
        wildcard_static_imports .forEach { b += "import " + it + "\n" }

        b += "{\n"
        b += "}"

        return b.toString()
    }

    // ---------------------------------------------------------------------------------------------
}

// NOTE(norswap): Lookup Priority
//
// 1. in-file
// 2. explicit imports
// 3. package classes
// 4. wildcard imports
//
// Only a single explicit import (static or not) is allowd for a given name.