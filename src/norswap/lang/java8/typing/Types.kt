package norswap.lang.java8.typing
import norswap.lang.java8.ast.TypeDeclKind
import norswap.lang.java8.resolution.Continue
import norswap.lang.java8.resolution.EmptyScope
import norswap.lang.java8.resolution.Found
import norswap.lang.java8.resolution.Lookup
import norswap.lang.java8.resolution.LookupList
import norswap.lang.java8.resolution.MemberInfo
import norswap.lang.java8.resolution.Resolver
import norswap.lang.java8.resolution.Scope
import norswap.lang.java8.resolution.plus

// -------------------------------------------------------------------------------------------------

interface TType
{
    val name: String

    val scope: Scope
        get() = EmptyScope
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
    val super_interfaces: LookupList<RefType>
        get() = Found(emptyList<RefType>())

    val super_types: LookupList<RefType>
        get() = super_interfaces

    val ancestors: LookupList<RefType>
        get() {
            val supa = super_interfaces
            if (supa is Continue) return supa

            val list = ArrayList(supa.value)
            var next = 0
            while (next != list.size) {
                val end = list.size
                for (i in next..(end-1)) {
                    val supah = list[i].super_types
                    if (supah is Continue) return supah
                    list.addAll(supah.value)
                }
                next = end
            }
            return Found(list.distinct())
        }

    val erasure: RefType
        get() = this
}

// -------------------------------------------------------------------------------------------------

interface InstantiableType: RefType
{
    val super_type: Lookup<RefType>
        get() = Found(TObject)

    override val super_types: LookupList<RefType>
        get() = super_interfaces + +super_type
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

    override val super_interfaces: LookupList<RefType>
        get() = Found(listOf(TSerializable, TCloneable))
}

// -------------------------------------------------------------------------------------------------

object TNull: RefType
{
    override val name = "null"
}

// -------------------------------------------------------------------------------------------------

interface ClassLike: InstantiableType, Scope, MemberInfo
{
    val full_name: String
    val kind: TypeDeclKind
}

// -------------------------------------------------------------------------------------------------

val TObject         : ClassLike = Resolver.load("java.lang.Object")
val TString         : ClassLike = Resolver.load("java.lang.String")
val TSerializable   : ClassLike = Resolver.load("java.io.Serializable")
val TCloneable      : ClassLike = Resolver.load("java.lang.Cloneable")

// -------------------------------------------------------------------------------------------------

abstract class BoxedType (full_name: String, val loaded: ClassLike =  Resolver.load(full_name))
    : ClassLike by loaded

// -------------------------------------------------------------------------------------------------

object BVoid   : BoxedType("java.lang.Void")
object BBool   : BoxedType("java.lang.Boolean")
object BByte   : BoxedType("java.lang.Bytes")
object BChar   : BoxedType("java.lang.Character")
object BShort  : BoxedType("java.lang.Short")
object BInt    : BoxedType("java.lang.Integer")
object BLong   : BoxedType("java.lang.Long")
object BFloat  : BoxedType("java.lang.Float")
object BDouble : BoxedType("java.lang.Double")

// -------------------------------------------------------------------------------------------------
