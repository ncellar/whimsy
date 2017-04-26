package norswap.lang.java8.typing
import norswap.lang.java8.ast.Expr
import norswap.utils.proclaim

// -------------------------------------------------------------------------------------------------

/**
 * Accessor for the "type" attribute.
 */
inline var Expr.typea: TType
    get()      = this["type"] as TType
    set(value) { this["type"] = value }

// -------------------------------------------------------------------------------------------------

/**
 * Promotes integer types to `int` if less wide, otherwise returns the type itself
 * (float, double, int, long).
 */
fun unary_promotion (type: NumericType): NumericType
{
    return when {
        type === TByte  -> TInt
        type === TChar  -> TInt
        type === TShort -> TInt
        else            -> type
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Returns the wider of the two numeric types, after [unary_promotion] for each.
 */
fun binary_promotion (lt: NumericType, rt: NumericType): NumericType
{
    return  when {
        lt === TDouble || rt === TDouble -> TDouble
        lt === TFloat  || rt === TFloat  -> TFloat
        lt === TLong   || rt === TLong   -> TLong
        else                             -> TInt
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * If the type is a boxed numeric type, return the corresponding unboxed primitive type.
 */
val TType.unboxed: TType
    get() = when (this) {
        !is BoxedType -> this
        BByte         -> TByte
        BChar         -> TChar
        BInt          -> TInt
        BLong         -> TLong
        BFloat        -> TFloat
        BDouble       -> TDouble
        BBool         -> TBool
        BVoid         -> TVoid
        else          -> this // unreachable
    }

// -------------------------------------------------------------------------------------------------

/**
 * Returns the unboxed primitive type corresponding to this boxed type.
 */
val BoxedType.unboxed: PrimitiveType
    get() = (this as TType).unboxed as PrimitiveType

// -------------------------------------------------------------------------------------------------

/**
 * Returns the boxed type corresponding to this primitive type.
 */
val PrimitiveType.boxed: RefType
    get() = when (this) {
        TByte         -> BByte
        TChar         -> BChar
        TInt          -> BInt
        TLong         -> BLong
        TFloat        -> BFloat
        TDouble       -> BDouble
        TBool         -> BBool
        TVoid         -> BVoid
        else          -> throw Error() // unreachable if no new primitive types
    }

// -------------------------------------------------------------------------------------------------

/**
 * True if the type is reifiable (available for reflection at run-time).
 */
fun TType.reifiable(): Boolean
{
    return if (this !is PrimitiveType) true
    else   if (this is ArrayType) component.reifiable()
    else   if (this is NestedType) types.all { it.reifiable() }
    else   if (this !is ParameterizedType) true
    else   type_args.all { it is WildcardType }
}

// -------------------------------------------------------------------------------------------------

/**
 * True if the receiver is a subclass of `other`.
 */
infix fun RefType.sub (other: RefType): Boolean
{
    // TODO: not that simple: wildcards
    return ancestors.contains(other)
}

// -------------------------------------------------------------------------------------------------

/**
 * True if the receiver is a superclass of `other`.
 */
infix fun RefType.sup (other: RefType): Boolean
{
    return other.sub(this)
}

// -------------------------------------------------------------------------------------------------

fun primitive_rank (type: PrimitiveType): Int
    = when (type) {
        TByte   -> 0
        TChar   -> 1
        TShort  -> 1
        TInt    -> 2
        TLong   -> 3
        TFloat  -> 4
        TDouble -> 5
        else    -> -1
    }

// -------------------------------------------------------------------------------------------------

/**
 * True if a value of type [src] can be converted to type [dst] via a widening
 * or primitive conversion, or via identity conversion.
 */
fun prim_widening_conversion (src: PrimitiveType, dst: PrimitiveType): Boolean
{
    val src_rank = primitive_rank(src)
    val dst_rank = primitive_rank(dst)
    return src_rank >= 0 && dst_rank >= 0 && dst_rank >= src_rank
}

// -------------------------------------------------------------------------------------------------

/**
 * True if a value of type [src] can be converted to type [dst] via a narrowing
 * or primitive conversion, or via identity conversion.
 */
fun prim_narrowing_conversion (src: PrimitiveType, dst: PrimitiveType): Boolean
    = prim_widening_conversion(dst, src)

// -------------------------------------------------------------------------------------------------

// TODO
fun ref_widening_conversion (source: RefType, target: RefType): Boolean
    = source sub target

// TODO
fun ref_narrowing_conversion (source: RefType, target: RefType): Boolean
    = source sup target

// -------------------------------------------------------------------------------------------------

/**
 * Returns the first detected conflicting parameterization: a pair of distinct types, an ancestor
 * of t1 and an ancestor of t2, that have the same erasure.
 */
fun find_conflicting_parameterization (t1: RefType, t2: RefType): Pair<RefType, RefType>?
{
    val t1_ancestors = t1.ancestors.filterIsInstance<ParameterizedType>()
    val t2_ancestors = t2.ancestors.filterIsInstance<ParameterizedType>()

    for (o1 in t1_ancestors)
        for (o2 in t2_ancestors)
            if (o1.raw == o2.raw && o1.type_args != o2.type_args)
                return o1 to o2

    return null
}

// -------------------------------------------------------------------------------------------------

/**
 * True of [src] and [dst] are cast-compatible: a between from a value with type
 * [src] to type [dst] may potentially succeed.
 *
 * See http://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.5
 */
fun cast_compatible (src: TType, dst: TType): Boolean
{
    if (src == dst)
        return true

    if (src is IntersectionType)
        return src.members.all { cast_compatible(it, dst) }

    if (dst is IntersectionType)
        return dst.members.all { cast_compatible(src, it) }

    if (src is TypeParameter)
        return cast_compatible(src.upper_bound, dst)

    if (dst is TypeParameter)
        return cast_compatible(src, dst.upper_bound)

    if (src is PrimitiveType) {
        if (dst is PrimitiveType)
            return src is NumericType && dst is NumericType
        else
            return ref_widening_conversion(src.boxed, dst as RefType)
    }

    proclaim(src as RefType)

    if (dst is PrimitiveType) {
        if (src is BoxedType)
            return prim_widening_conversion(src.unboxed, dst)
        else
            return ref_narrowing_conversion(src, dst.boxed)
    }

    proclaim(dst as RefType)

    if (src is ArrayType) {
        if (dst is ArrayType) {
            val sc = src.component
            val tc = dst.component
            if (sc is RefType && tc is RefType)
                return cast_compatible(sc, tc)
            else
                return sc == tc // same primitive type
        }
        else
            // target must be Object, Serializable or Cloneable
            return src sub dst
    }

    if (dst is ArrayType)
        // source must be Object, Serializable or Cloneable
        return dst sub src

    val conflict = find_conflicting_parameterization(src, dst)

    return conflict != null
        || ref_narrowing_conversion (src.erasure, dst.erasure)
        || ref_widening_conversion  (src.erasure, dst.erasure)
}

// -------------------------------------------------------------------------------------------------