package norswap.uranium.java.types
import java.lang.reflect.GenericArrayType

class ArrayType (val component: Type): RefType
{
    override val reflection_type = object: GenericArrayType
    {
        override fun getGenericComponentType() = component.reflection_type
    }
}