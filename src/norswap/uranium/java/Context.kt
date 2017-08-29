package norswap.uranium.java
import norswap.lang.Node
import norswap.uranium.Attribute
import norswap.uranium.Propagator
import norswap.uranium.UraniumError
import norswap.uranium.java.types.ClassType
import norswap.uranium.java.types.Type
import norswap.utils.cast

/**
 * Context for a typing / name resolution job.
 */
class Context (val propagator: Propagator)
{
    // ---------------------------------------------------------------------------------------------

    val resolver = Resolver(this)

    // ---------------------------------------------------------------------------------------------

    fun report (error: UraniumError)
    {
        propagator.report(error)
    }

    // ---------------------------------------------------------------------------------------------

    fun report (msg: String)
    {
        propagator.report(UraniumError(msg))
    }

    // ---------------------------------------------------------------------------------------------

    operator fun <T> Node.get (name: String): T
        = propagator[this, name].cast()

    // ---------------------------------------------------------------------------------------------

    operator fun Node.set (name: String, value: Any)
        { propagator[this, name] = value }

    // ---------------------------------------------------------------------------------------------

    operator fun Node.rangeTo (name: String): Attribute
        = Attribute(this, name)

    // ---------------------------------------------------------------------------------------------

    var Node.type: Type
        get() = propagator[this, "type"].cast()
        set(value) { propagator[this, "type"] = value }

    // ---------------------------------------------------------------------------------------------

    val StringClass = resolver.load_class_strict("java.lang.String")

    // ---------------------------------------------------------------------------------------------

    val StringType = ClassType(StringClass)

    // ---------------------------------------------------------------------------------------------

    val ObjectClass = resolver.load_class_strict("java.lang.Object")

    // ---------------------------------------------------------------------------------------------

    val ObjectType = ClassType(ObjectClass)

    // ---------------------------------------------------------------------------------------------

    val EnumClass = resolver.load_class_strict("java.lang.Enum")

    // ---------------------------------------------------------------------------------------------

    val EnumType = ClassType(EnumClass)

    // ---------------------------------------------------------------------------------------------

    val AnnotationClass = resolver.load_class_strict("java.lang.annotation.Annotation")

    // ---------------------------------------------------------------------------------------------

    val AnnotationType = ClassType(AnnotationClass)

    // ---------------------------------------------------------------------------------------------
}