package norswap.uranium.java.types
import norswap.utils.mapToArray

class WildcardType (
    val upper_bounds: List<Type> = emptyList(),
    val lower_bounds: List<Type> = emptyList())
    : RefType
{
    val unbounded: Boolean
        = upper_bounds.size == 0 && lower_bounds.size == 0

    override val reflection_type = object: java.lang.reflect.WildcardType
    {
        override fun getUpperBounds()
            = upper_bounds.mapToArray { it.reflection_type }

        override fun getLowerBounds()
            = lower_bounds.mapToArray { it.reflection_type }
    }
}

val WILDCARD = WildcardType()