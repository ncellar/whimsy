package norswap.uranium.java.model.source
import norswap.uranium.java.Context
import norswap.uranium.java.model.Data
import norswap.uranium.java.model.Klass
import norswap.uranium.java.model.Method
import norswap.uranium.java.types.RefType

interface Scope
{
    // ---------------------------------------------------------------------------------------------

    val outer: Scope?

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a type declared in the scope: either a local type definition, or a type parameter.
     *
     * Note that we do not use this to lookup dot-qualified inner class references, but rather
     * [Klass.klass].
     *
     * The supplied context's resolver may potentially be used to resolve the target type.
     */
    fun get_type (name: String, ctx: Context): RefType?
        = null

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns some data declared in the scope: a parameter, field or variable.
     */
    fun get_data (name: String): Data?
        = null

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a method declared in the scope.
     */
    fun get_method (name: String): List<Method>
        = emptyList()

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a label declared in the scope.
     */
    fun get_label (name: String): Int?
        = null

    // ---------------------------------------------------------------------------------------------
}