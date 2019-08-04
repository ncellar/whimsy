package norswap.uranium.java.types

object NullType: RefType
{
    override val reflection_type: java.lang.reflect.Type
        get() = object: java.lang.reflect.Type {
            override fun getTypeName() = "null"
        }
}