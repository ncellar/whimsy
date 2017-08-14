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

    /**
     * Maps simple class name to their associated type, for classes defined in the file.
     *
     * This is never actually useful for lookups (the relevant scope is the package).
     */
    val classes = HashMap<String, SourceClass>()

    // ---------------------------------------------------------------------------------------------

    override fun toString(): String
    {
        val b = StringBuilder()
        b += "{\n"

        if (pkg.pkg != null)
            b += "  package " + pkg.name + "\n\n"

        single_imports          .forEach { b += "  import ${it.value}\n" }
        single_static_imports   .forEach { b += "  import static ${it.value}\n" }
        wildcard_imports        .forEach { b += "  import $it.*\n" }
        wildcard_static_imports .forEach { b += "  import static $it.*\n" }

        if ((single_imports          .size != 0 ||
             single_static_imports   .size != 0 ||
             wildcard_imports        .size != 0 ||
             wildcard_static_imports .size != 0) &&
             classes.size != 0)
                b += "\n"

        classes.entries.forEach { (name, _) -> b +=  "  class $name\n" }

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