package norswap.uranium.java.types

// -------------------------------------------------------------------------------------------------

interface PrimitiveType: Type
{
    val boxed: BoxedType
}

// -------------------------------------------------------------------------------------------------

interface NumericType: PrimitiveType

// -------------------------------------------------------------------------------------------------

interface IntegralType : NumericType

// -------------------------------------------------------------------------------------------------

interface FloatingType: NumericType

// -------------------------------------------------------------------------------------------------

object ByteType: IntegralType
{
    override val boxed = ByteBoxed
    override val reflection_type = java.lang.Byte.TYPE
}

// -------------------------------------------------------------------------------------------------

object CharType: IntegralType
{
    override val boxed = ByteBoxed
    override val reflection_type = java.lang.Character.TYPE
}

// -------------------------------------------------------------------------------------------------

object ShortType: IntegralType
{
    override val boxed = ByteBoxed
    override val reflection_type = java.lang.Short.TYPE
}

// -------------------------------------------------------------------------------------------------

object IntType: IntegralType
{
    override val boxed = ByteBoxed
    override val reflection_type = java.lang.Integer.TYPE
}

// -------------------------------------------------------------------------------------------------

object LongType: IntegralType
{
    override val boxed = ByteBoxed
    override val reflection_type = java.lang.Long.TYPE
}

// -------------------------------------------------------------------------------------------------

object FloatType: IntegralType
{
    override val boxed = ByteBoxed
    override val reflection_type = java.lang.Float.TYPE
}

// -------------------------------------------------------------------------------------------------

object DoubleType: IntegralType
{
    override val boxed = ByteBoxed
    override val reflection_type = java.lang.Double.TYPE
}

// -------------------------------------------------------------------------------------------------

object BooleanType: PrimitiveType
{
    override val boxed = BooleanBoxed
    override val reflection_type = java.lang.Boolean.TYPE
}

// -------------------------------------------------------------------------------------------------

object VoidType: PrimitiveType
{
    override val boxed = ByteBoxed
    override val reflection_type = java.lang.Void.TYPE
}

// -------------------------------------------------------------------------------------------------