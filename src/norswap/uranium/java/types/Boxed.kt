package norswap.uranium.java.types
import norswap.uranium.AnyClass
import norswap.uranium.java.model.reflect.ReflectionClass

// -------------------------------------------------------------------------------------------------

abstract class BoxedType (klass: AnyClass): ClassType(ReflectionClass(klass))
{
    abstract val unboxed: PrimitiveType
    override val reflection_type = klass
}

// -------------------------------------------------------------------------------------------------


object ByteBoxed: BoxedType (java.lang.Byte::class.java)
{
    override val unboxed = ByteType
}

// -------------------------------------------------------------------------------------------------

object CharBoxed: BoxedType (java.lang.Character::class.java)
{
    override val unboxed = CharType
}

// -------------------------------------------------------------------------------------------------

object ShortBoxed: BoxedType (java.lang.Short::class.java)
{
    override val unboxed = ShortType
}

// -------------------------------------------------------------------------------------------------

object IntBoxed: BoxedType (java.lang.Integer::class.java)
{
    override val unboxed = IntType
}

// -------------------------------------------------------------------------------------------------

object LongBoxed: BoxedType (java.lang.Long::class.java)
{
    override val unboxed = LongType
}

// -------------------------------------------------------------------------------------------------

object FloatBoxed: BoxedType (java.lang.Float::class.java)
{
    override val unboxed = FloatType
}

// -------------------------------------------------------------------------------------------------

object DoubleBoxed: BoxedType (java.lang.Double::class.java)
{
    override val unboxed = DoubleType
}

// -------------------------------------------------------------------------------------------------

object BooleanBoxed: BoxedType (java.lang.Boolean::class.java)
{
    override val unboxed = BooleanType
}

// -------------------------------------------------------------------------------------------------

object VoidBoxed: BoxedType (java.lang.Void::class.java)
{
    override val unboxed = VoidType
}

// -------------------------------------------------------------------------------------------------