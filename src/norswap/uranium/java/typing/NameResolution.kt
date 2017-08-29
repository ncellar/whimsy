package norswap.uranium.java.typing
import norswap.uranium.java.Context
import norswap.uranium.java.model.Klass
import norswap.uranium.java.model.source.Scope
import norswap.uranium.java.model.source.SourceClass
import norswap.uranium.java.types.ClassType
import norswap.uranium.java.types.ParameterType
import norswap.uranium.java.types.RefType
import kotlin.jvm.internal.Ref.IntRef
import norswap.uranium.java.Resolver.Result.*

// -------------------------------------------------------------------------------------------------

/**
 * Given a simple class name (no dots), tries to look it up inside the current scope, walking
 * through the outer scopes and the superclasses as needed. The context's resolver is potentially
 * used to resolve the superclasses as well as the looked up class itself.
 *
 * Since we may need to resolve superclasses' names, this procedure is potentially recursive.
 */
fun Scope.resolve_simple_type (name: String, ctx: Context): RefType?
{
    var scope: Scope? = this

    // walk through nested scopes
    while (scope != null)
    {
        scope.get_type(name, ctx)?.let { return it }

        // walk through superclasses
        if (scope is SourceClass)
        {
            var class_scope = scope.superclass(ctx)

            do {
                class_scope.klass(name, ctx)?.let { return ClassType(it) }
                class_scope = class_scope.superclass(ctx)
            }
            while (class_scope != ctx.ObjectClass)
        }

        scope = scope.outer
    }

    return null
}

// -------------------------------------------------------------------------------------------------

/**
 * Given a simple class name (no dots), tries to look it up inside the current scope, walking
 * through the outer scopes and the superclasses as needed. The context's resolver is potentially
 * used to resolve the superclasses as well as the looked up class itself.
 */
fun Scope.resolve_type_or_data (name: String, ctx: Context): Any
{
    TODO()
}

// -------------------------------------------------------------------------------------------------

/**
 * Given a qualified (dot-separated) class name, tries to look it up inside the current scope,
 * walking through the outer scopes and the superclasses as needed. The context's resolver is
 * potentially used to resolve the superclasses as well as the looked up class itself.
 */
fun Scope.resolve_qualified_type (name: String, ctx: Context): Klass?
    = resolve_qualified_type(name.split('.'), ctx)

// -------------------------------------------------------------------------------------------------

/**
 * Given a qualified (dot-separated) class name, tries to look it up inside the current scope,
 * walking through the outer scopes and the superclasses as needed. The context's resolver is
 * potentially used to resolve the superclasses as well as the looked up class itself.
 *
 * This method assumes that a class name *must* occur, and so will report error if something
 * else is encountered, or if at some point in the chain a class fails to be found or to be loaded.
 *
 * @pre name.size > 0
 */
fun Scope.resolve_qualified_type (name: List<String>, ctx: Context): Klass?
{
    // NOTE(norswap)
    // Since we assume a type is required, there is no need to worry about
    // fields / inner class name conflicts.

    val iref = IntRef()
    var klass = find_first_class(name, ctx, iref) ?: return null

    for (i in iref.element until name.size)
    {
        val klass_name = klass.classes[name[i]]

        if (klass_name == null) {
            ctx.report("$klass.${name[i]} is not a class type.")
            return null
        }

        klass = ctx.resolver.load_class(klass_name) ?: return null
    }

    return klass
}

// -------------------------------------------------------------------------------------------------

/**
 * Find the first class name in a qualified class name. This can either be a simple class name
 * or a qualified class name itself, with a package part.
 *
 * Reports an error if either a class isn't found, or if one is found but the class information
 * can't be loaded.
 */
private fun Scope.find_first_class (name: List<String>, ctx: Context, size: IntRef): Klass?
{
    // simple type?
    resolve_simple_type(name[0], ctx)?.let {

        if (it is ParameterType) {
            ctx.report("$it is a parameter type, but a class type was expected.")
            return null
        }

        if (it !is ClassType)
            throw Error("Implementation error: $it not a class or parameter type.")

        // simple class name
        size.element = 1
        return it.source
    }

    // package-qualified class-name?
    loop@ for (i in 2..name.size) // start at 2: we can't package-qualify with the default package
    {
        val klass_name = name.subList(0, i).joinToString(".")
        val result = ctx.resolver.load_class_errorless(klass_name)

        when (result)
        {
            is Miss -> continue@loop

            is Fail -> {
                // class exists but could be loaded, don't look further
                ctx.report("Couldn't load type $klass_name from ${result.url}")
                return null
            }

            is Success -> {
                size.element = i
                return result.klass
            }
        }
    }

    ctx.report("Could not find a prefix of ${name.joinToString(".")} that is a valid class name.")
    return null
}

// -------------------------------------------------------------------------------------------------