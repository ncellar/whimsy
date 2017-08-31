package norswap.uranium.java
import norswap.lang.Node
import norswap.uranium.Attribute
import norswap.uranium.Reactor
import norswap.uranium.UraniumError
import norswap.uranium.java.types.ClassType
import norswap.uranium.java.types.Type
import norswap.utils.cast

/**
 * Context for a typing / name resolution job.
 */
class Context (val reactor: Reactor)
{
    // ---------------------------------------------------------------------------------------------

    val resolver = Resolver(this)

    // ---------------------------------------------------------------------------------------------

    fun report (error: UraniumError)
    {
        reactor.report(error)
    }

    // ---------------------------------------------------------------------------------------------

    fun report (msg: String)
    {
        reactor.report(UraniumError(msg))
    }

    // ---------------------------------------------------------------------------------------------

    operator fun <T> Node.get (name: String): T
        = reactor[this, name].cast()

    // ---------------------------------------------------------------------------------------------

    operator fun Node.set (name: String, value: Any)
        { reactor[this, name] = value }

    // ---------------------------------------------------------------------------------------------

    operator fun Node.rangeTo (name: String): Attribute
        = Attribute(this, name)

    // ---------------------------------------------------------------------------------------------

    var Node.type: Type
        get() = reactor[this, "type"].cast()
        set(value) { reactor[this, "type"] = value }

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