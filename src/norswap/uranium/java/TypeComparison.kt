package norswap.uranium.java
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

// -------------------------------------------------------------------------------------------------
/*

This implements subtype checking between java.lang.reflect.Type subclasses (namely, Class,
ParameterizedType and TypeVariable). Does not work with arrays but should be otherwise correct.

ParameterizedType arguments with no parameters are implictly converted to Class. When comparing
Class and ParameterizedType, the class is treated like a raw type. This means that
a ParameterizedType with 1+ arguments can be a subtype of a Class, but not the other way around.

We also expose the GenericType class in order to construct ParameterizedType instances easily.

*/
// -------------------------------------------------------------------------------------------------

private val ParameterizedType.raw_class: Class<*>
    get() = rawType as Class<*>

// -------------------------------------------------------------------------------------------------

infix fun Type.iz (type: Type) = when (this)
{
    is Class<*>          -> this iz type
    is ParameterizedType -> this iz type
    is TypeVariable<*>   -> this iz type
    else                 -> false
}

infix fun Type.izn (type: Type)
    = !(this iz type)

infix fun Type.haz (type: Type)
    = type iz this

infix fun Type.hazn (type: Type)
    = !(type iz this)

// -------------------------------------------------------------------------------------------------

infix fun Type.iz (klass: Class<*>) = when (this)
{
    is Class<*>          -> this iz klass
    is ParameterizedType -> this iz klass
    is TypeVariable<*>   -> this iz klass
    else                 -> false
}

infix fun Type.izn (klass: Class<*>)
    = !(this iz klass)

infix fun Type.haz (klass: Class<*>)
    = klass iz this

infix fun Type.hazn (klass: Class<*>)
    = !(klass iz this)

// -------------------------------------------------------------------------------------------------

infix fun Type.iz (type: ParameterizedType) = when (this)
{
    is Class<*>          -> this iz type
    is ParameterizedType -> this iz type
    is TypeVariable<*>   -> this iz type
    else                 -> false
}

infix fun Type.izn (type: ParameterizedType)
    = !(this iz type)

infix fun Type.haz (type: ParameterizedType)
    = type iz this

infix fun Type.hazn (type: ParameterizedType)
    = !(type iz this)

// -------------------------------------------------------------------------------------------------

infix fun Type.iz (variable: TypeVariable<*>) = when (this)
{
    is TypeVariable<*> -> this iz variable
    else               -> false
}

infix fun Type.izn (variable: TypeVariable<*>)
    = !(this iz variable)

infix fun Type.haz (variable: TypeVariable<*>)
    = variable iz this

infix fun Type.hazn (variable: TypeVariable<*>)
    = !(variable iz this)

// -------------------------------------------------------------------------------------------------

infix fun Class<*>.iz (type: Type) = when (type)
{
    is Class<*>          -> this iz type
    is ParameterizedType -> this iz type
    else                 -> false
}

infix fun Class<*>.izn (type: Type)
    = !(this iz type)

infix fun Class<*>.haz (type: Type)
    = type iz this

infix fun Class<*>.hazn (type: Type)
    = !(type iz this)

// -------------------------------------------------------------------------------------------------

infix fun Class<*>.iz (klass: Class<*>)
    = klass.isAssignableFrom(this)

infix fun Class<*>.izn (klass: Class<*>)
    = !(this iz klass)

infix fun Class<*>.haz (klass: Class<*>)
    = klass iz this

infix fun Class<*>.hazn (klass: Class<*>)
    = !(klass iz this)

// -------------------------------------------------------------------------------------------------

infix fun Class<*>.iz (type: ParameterizedType)
    = type.actualTypeArguments.size == 0 && this iz type.raw_class

infix fun Class<*>.izn (type: ParameterizedType)
    = !(this iz type)

infix fun Class<*>.haz (type: ParameterizedType)
    = type iz this

infix fun Class<*>.hazn (type: ParameterizedType)
    = !(type iz this)

// -------------------------------------------------------------------------------------------------

infix fun TypeVariable<*>.iz (type: Type) = when (type)
{
    is Class<*>          -> this iz type
    is ParameterizedType -> this iz type
    is TypeVariable<*>   -> this iz type
    else                 -> false
}

infix fun TypeVariable<*>.izn (type: Type)
    = !(this iz type)

infix fun TypeVariable<*>.haz (type: Type)
    = type iz this

infix fun TypeVariable<*>.hazn (type: Type)
    = !(type iz this)

// -------------------------------------------------------------------------------------------------

infix fun TypeVariable<*>.iz (variable: TypeVariable<*>)
    = this == variable

infix fun TypeVariable<*>.izn (variable: TypeVariable<*>)
    = !(this iz variable)

infix fun TypeVariable<*>.haz (variable: TypeVariable<*>)
    = variable iz this

infix fun TypeVariable<*>.hazn (variable: TypeVariable<*>)
    = !(variable iz this)

// -------------------------------------------------------------------------------------------------

infix fun TypeVariable<*>.iz (klass: Class<*>): Boolean
    = this.bounds.any { it iz klass }

infix fun TypeVariable<*>.izn (klass: Class<*>)
    = !(this iz klass)

// -------------------------------------------------------------------------------------------------

infix fun TypeVariable<*>.iz (type: ParameterizedType): Boolean
    = this.bounds.any { it iz type }

infix fun TypeVariable<*>.izn (type: ParameterizedType)
    = !(this iz type)

// -------------------------------------------------------------------------------------------------

infix fun ParameterizedType.iz (type: Type) = when (type)
{
    is Class<*>          -> this iz type
    is ParameterizedType -> this iz type
    else                 -> false
}

infix fun ParameterizedType.izn (type: Type)
    = !(this iz type)

infix fun ParameterizedType.haz (type: Type)
    = type iz this

infix fun ParameterizedType.hazn (type: Type)
    = !(type iz this)

// -------------------------------------------------------------------------------------------------

infix fun ParameterizedType.iz (klass: Class<*>)
    = this.raw_class iz klass

infix fun ParameterizedType.izn (klass: Class<*>)
    = !(this iz klass)

infix fun ParameterizedType.haz (klass: Class<*>)
    = klass iz this

infix fun ParameterizedType.hazn (klass: Class<*>)
    = !(klass iz this)

// -------------------------------------------------------------------------------------------------

infix fun ParameterizedType.iz (type: ParameterizedType): Boolean
{
    val args1 = this.actualTypeArguments
    val args2 = type.actualTypeArguments
    val raw1  = this.rawType as Class<*>
    val raw2  = type.rawType as Class<*>

    if (args1.size == 0)
        return this.raw_class iz type

    if (args2.size == 0)
        return this iz type.raw_class

    if (args1.size != args2.size)
        return false

    return raw1 iz raw2 && (args1 zip args2).all { (a, b) -> a iz b }
}

infix fun ParameterizedType.izn (type: ParameterizedType)
    = !(this iz type)

infix fun ParameterizedType.haz (type: ParameterizedType)
    = type iz this

infix fun ParameterizedType.hazn (type: ParameterizedType)
    = !(type iz this)

// -------------------------------------------------------------------------------------------------

class GenericType (val raw: Class<*>, vararg val parameters: Type): Type, ParameterizedType
{
    override fun getRawType() = raw
    override fun getOwnerType() = null
    override fun getActualTypeArguments() = parameters

    override fun toString()
        = parameters.joinToString(", ", "${raw.name}<", ">")
}

// -------------------------------------------------------------------------------------------------