package norswap.uranium.java.typing
import norswap.uranium.java.types.ByteType
import norswap.uranium.java.types.CharType
import norswap.uranium.java.types.DoubleType
import norswap.uranium.java.types.FloatType
import norswap.uranium.java.types.IntType
import norswap.uranium.java.types.LongType
import norswap.uranium.java.types.NumericType
import norswap.uranium.java.types.ShortType

// -------------------------------------------------------------------------------------------------

/**
 * Promotes integer types to `int` if less wide, otherwise returns the type itself
 * (float, double, int, long).
 */
fun unary_promotion (type: NumericType): NumericType
{
    return when {
        type === ByteType  -> IntType
        type === CharType  -> IntType
        type === ShortType -> IntType
        else               -> type
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Returns the wider of the two numeric types, after [unary_promotion] for each.
 */
fun binary_promotion (lt: NumericType, rt: NumericType): NumericType
{
    return  when {
        lt === DoubleType || rt === DoubleType -> DoubleType
        lt === FloatType  || rt === FloatType  -> FloatType
        lt === LongType   || rt === LongType   -> LongType
        else                                   -> IntType
    }
}

// -------------------------------------------------------------------------------------------------