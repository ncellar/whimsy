package norswap.uranium.java.types
import norswap.uranium.java.model.Klass

open class ClassType (
    val source: Klass,
    val outer_type: Type? = null,
    val type_params: List<Type> = emptyList())
    : RefType
{
    override val reflection_type: java.lang.reflect.Type get() = TODO()
}