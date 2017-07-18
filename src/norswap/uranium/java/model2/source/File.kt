package norswap.uranium.java.model2.source
import norswap.uranium.java.model2.Package

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
}

// NOTE(norswap): Lookup Priority
//
// 1. in-file
// 2. explicit imports
// 3. package classes
// 4. wildcard imports
//
// Only a single explicit import (static or not) is allowd for a given name.