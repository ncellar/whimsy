package norswap.uranium.java.model2
import norswap.utils.multimap.MultiMap

abstract class Klass: Member
{
    // ---------------------------------------------------------------------------------------------

    abstract val binary_name: String

    // ---------------------------------------------------------------------------------------------

    abstract val enum: Boolean

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
}