package norswap.uranium.java.model.source
import norswap.uranium.java.Context
import norswap.uranium.java.Resolver.Result.*
import norswap.uranium.java.model.Package
import norswap.uranium.java.types.ClassType
import norswap.uranium.java.types.RefType
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

    override fun get_type (name: String, ctx: Context): RefType?
    {
        classes[name]
            ?.let { return ClassType(it) }

        val klass_name = single_imports[name] ?: single_static_imports[name]

        if (klass_name != null) {
            val klass = ctx.resolver.load_class(klass_name)
            return klass ?. let { ClassType(it) }
        }

        try_load(pkg.prefix + name, ctx) { return it }

        listOf(wildcard_imports, wildcard_static_imports).forEach {
            it.forEach {
                try_load(it + "." + name, ctx) { return it }
            }
        }

        return null
    }

    // ---------------------------------------------------------------------------------------------

    private inline fun try_load (binary_name: String, ctx: Context, ret: (RefType?) -> Unit)
    {
        val result = ctx.resolver.load_class_errorless(binary_name)

        if (result is Success)
            ret(ClassType(result.klass))
        if (result is Fail) {
            ctx.report("Could not load class $binary_name from ${result.url}")
            ret(null)
        }
    }

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