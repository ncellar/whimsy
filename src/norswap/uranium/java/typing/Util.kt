package norswap.uranium.java.typing
import norswap.uranium.java.types.ArrayType
import norswap.uranium.java.types.BoxedType
import norswap.uranium.java.types.ClassType
import norswap.uranium.java.types.PrimitiveType
import norswap.uranium.java.types.Type
import norswap.uranium.java.types.WildcardType
import norswap.utils.proclaim

// -------------------------------------------------------------------------------------------------

val Type.unboxed: Type
    get() = if (this is BoxedType) this.unboxed else this

// -------------------------------------------------------------------------------------------------

val Type.boxed: Type
    get() = if (this is PrimitiveType) this.boxed else this

// -------------------------------------------------------------------------------------------------

val Type.reifiable: Boolean
    get() {
        if (this is PrimitiveType)
            return true

        if (this is ArrayType)
            return component.reifiable

        if (this is WildcardType)
            return unbounded

        proclaim (this as ClassType)

        if (!type_params.all(Type::reifiable))
            return false

        if (outer_type != null)
            return outer_type.reifiable

        return true
    }

// -------------------------------------------------------------------------------------------------

fun cast_compatible (src: Type, dst: Type): Boolean
{
    // TODO
    return false
}

// -------------------------------------------------------------------------------------------------

