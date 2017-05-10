package norswap.lang.java8.typing
import norswap.lang.java8.resolution.Resolver
import norswap.utils.maybe_list

// -------------------------------------------------------------------------------------------------

interface TType
{
    val name: String
}

// -------------------------------------------------------------------------------------------------

abstract class PrimitiveType (override val name: String): TType

abstract class NumericType  (name: String) : PrimitiveType(name)
abstract class IntegerType  (name: String) : NumericType(name)
abstract class FloatingType (name: String) : NumericType(name)

object TVoid   : PrimitiveType("void")
object TBool   : PrimitiveType("boolean")
object TByte   : IntegerType("byte")
object TChar   : IntegerType("char")
object TShort  : IntegerType("short")
object TInt    : IntegerType("int")
object TLong   : IntegerType("long")
object TFloat  : FloatingType("float")
object TDouble : FloatingType("double")

// -------------------------------------------------------------------------------------------------

interface RefType: TType
{
    val super_interfaces: List<RefType>
        get() = emptyList<RefType>()

    val super_types: List<RefType>
        get() = super_interfaces

    val ancestors: List<RefType>
        get() {
            val list = ArrayList(super_interfaces)
            var next = 0
            while (next != list.size) {
                val end = list.size
                for (i in next..(end-1)) list.addAll(list[i].super_types)
                next = end
            }
            return list.distinct()
        }

    val erasure: RefType
        get() = this
}

// -------------------------------------------------------------------------------------------------

interface InstantiableType: RefType
{
    val super_type: RefType?

    override val super_types: List<RefType>
        get() = super_interfaces + maybe_list(super_type)
}

// -------------------------------------------------------------------------------------------------

interface IntersectionType: RefType
{
    val members: List<RefType> // not intersection themsevles
}

// -------------------------------------------------------------------------------------------------

interface NestedType: RefType
{
    val types: List<RefType> // not nested themselves
}

// -------------------------------------------------------------------------------------------------

interface ParameterizedType: RefType
{
    val raw: RefType
    val type_args: List<RefType>

    override val erasure: RefType
        get() = raw
}

// -------------------------------------------------------------------------------------------------

interface WildcardType: RefType
{
    val upper_bounds: List<RefType>
    val lower_bounds: List<RefType>
}

// -------------------------------------------------------------------------------------------------

interface TypeParameter: RefType
{
    val upper_bound: RefType
}

// -------------------------------------------------------------------------------------------------

interface ArrayType: InstantiableType
{
    val component: TType

    override val super_interfaces
        get() = listOf(TSerializable, TCloneable)
}

// -------------------------------------------------------------------------------------------------

object TNull: RefType
{
    override val name = "null"
}

// -------------------------------------------------------------------------------------------------

val TObject         = Resolver.eagerly("java.lang.Object")
val TEnum           = Resolver.eagerly("java.lang.Enum")
val TAnnotation     = Resolver.eagerly("java.lang.annotation.Annotation")
val TString         = Resolver.eagerly("java.lang.String")
val TSerializable   = Resolver.eagerly("java.io.Serializable")
val TCloneable      = Resolver.eagerly("java.lang.Cloneable")

// -------------------------------------------------------------------------------------------------

val BVoid   = Resolver.eagerly("java.lang.Void")
val BBool   = Resolver.eagerly("java.lang.Boolean")
val BByte   = Resolver.eagerly("java.lang.Byte")
val BChar   = Resolver.eagerly("java.lang.Character")
val BShort  = Resolver.eagerly("java.lang.Short")
val BInt    = Resolver.eagerly("java.lang.Integer")
val BLong   = Resolver.eagerly("java.lang.Long")
val BFloat  = Resolver.eagerly("java.lang.Float")
val BDouble = Resolver.eagerly("java.lang.Double")

// -------------------------------------------------------------------------------------------------

val TType.is_boxed: Boolean
    get() = when (this) {
        BInt    -> true
        BChar   -> true
        BDouble -> true
        BLong   -> true
        BFloat  -> true
        BBool   -> true
        BVoid   -> true
        BByte   -> true
        BShort  -> true
        else    -> false
    }

// -------------------------------------------------------------------------------------------------

interface MemberInfo
{
    val name: String
}

// -------------------------------------------------------------------------------------------------

abstract class MethodInfo: MemberInfo
{
    override fun toString() = name
}

// -------------------------------------------------------------------------------------------------

abstract class FieldInfo: MemberInfo
{
    override fun toString() = name
}

// -------------------------------------------------------------------------------------------------