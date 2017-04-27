package norswap.lang.java8.typing
import norswap.lang.java8.ast.TypeDeclKind
import norswap.lang.java8.resolution.EmptyScope
import norswap.lang.java8.resolution.Resolver
import norswap.lang.java8.resolution.Scope
import norswap.utils.maybe_list

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
        get() = TObject

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

interface ClassLike: InstantiableType, Scope, MemberInfo
{
    val canonical_name: String
    val kind: TypeDeclKind
}

// -------------------------------------------------------------------------------------------------

val TObject         = Resolver.eagerly("java.lang.Object")
val TEnum           = Resolver.eagerly("java.lang.Enum")
val TAnnotation     = Resolver.eagerly("java.lang.annotation.Annotation")
val TString         = Resolver.eagerly("java.lang.String")
val TSerializable   = Resolver.eagerly("java.io.Serializable")
val TCloneable      = Resolver.eagerly("java.lang.Cloneable")

// -------------------------------------------------------------------------------------------------

abstract class BoxedType (full_name: String, val loaded: ClassLike =  Resolver.eagerly(full_name))
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