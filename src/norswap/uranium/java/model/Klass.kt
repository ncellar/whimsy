package norswap.uranium.java.model
import norswap.uranium.java.Context
import norswap.utils.multimap.MultiMap
import norswap.uranium.java.Resolver.Result.*

abstract class Klass: Member
{
    // ---------------------------------------------------------------------------------------------

    abstract val binary_name: String

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the class is nested within another class.
     *
     * This includes both static nested classes and non-static nested classes (aka inner classes).
     * Distinguish between both using [Member.static]
     */
    abstract val is_nested: Boolean

    // ---------------------------------------------------------------------------------------------

    abstract val is_enum: Boolean

    // ---------------------------------------------------------------------------------------------

    abstract val is_interface: Boolean

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the class is an annotation type. All annotation types are also interfaces.
     */
    abstract val is_annotation: Boolean

    // ---------------------------------------------------------------------------------------------

    abstract val is_local: Boolean

    // ---------------------------------------------------------------------------------------------

    abstract val is_anonymous: Boolean

    // ---------------------------------------------------------------------------------------------

    abstract val fields: Map<String, Field>

    // ---------------------------------------------------------------------------------------------

    abstract val static_fields: Map<String, Field>

    // ---------------------------------------------------------------------------------------------

    abstract val methods: MultiMap<String, Method>

    // ---------------------------------------------------------------------------------------------

    abstract val static_methods: MultiMap<String, Method>

    // ---------------------------------------------------------------------------------------------

    abstract val constructors: List<Constructor>

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps the simple name of the inner classes to their binary name.
     */
    abstract val classes: Map<String, String>

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps the simple name of the inner classes to their binary name.
     */
    abstract val static_classes: Map<String, String>

    // ---------------------------------------------------------------------------------------------

    abstract val type_parameters: Map<String, TypeParameter>

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a map of enum constants, if this object represents an enumeration class.
     */
    abstract val enum_constants: Map<String, Field>?

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the superclass of this class, potentially using the context's resolver to retrieve it.
     *
     * This must only be called after static scopes have been built.
     *
     * If the superclass can't be resolved, an error should be reported, and the Object class
     * returned instead.
     */
    abstract fun superclass (ctx: Context): Klass

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the superinterfaces of this class, potentially using the context's resolver
     * to retrieve them.
     *
     * This must only be called after static scopes have been built.
     *
     * If one or more superinterfaces can't be resolved, (an) error(s) are reported.
     * The unresoled interfaces are simply omitted from the returned list.
     */
    abstract fun superinterfaces (ctx: Context): List<Klass>

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the outer class of this class, if any, potentially using the context's resolver
     * to retrieve it.
     *
     * This must only be called after static scopes have been built.
     *
     * If an outer class exists but can't be resolved, an error is reported and null
     * returned instead.
     */
    abstract fun outer_class (ctx: Context): Klass?

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the inner class of this class with the given name, potentially using the context's
     * resolver to retrieve it.
     *
     * This must only be called after static scopes have been built.
     *
     * If an inner class class with the given name does not exist (i.e. is not registered within
     * this class), returns null. If the class exists but can't be located or loaded, an error is
     * reported and null is reported.
     */
    open fun klass (name: String, ctx: Context): Klass?
    {
        val class_name = classes[name] ?: return null
        val result = ctx.resolver.load_class_errorless(class_name)
        when (result) {
            is Miss    -> ctx.report("Couldn't locate inner class $name of $binary_name")
            is Fail    -> ctx.report("Couldn't load inner class $name of $binary_name from ${result.url}")
            is Success -> return result.klass
        }
        return null
    }

    // ---------------------------------------------------------------------------------------------
}