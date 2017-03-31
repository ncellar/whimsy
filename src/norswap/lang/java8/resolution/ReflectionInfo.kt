package norswap.lang.java8.resolution
import norswap.lang.java8.ast.TypeDeclKind
import norswap.lang.java8.typing.ClassLike
import norswap.lang.java8.typing.FieldInfo
import norswap.lang.java8.typing.MethodInfo
import norswap.lang.java8.typing.RefType
import norswap.lang.java8.typing.TypeParameter
import norswap.utils.multimap.MutableMultiMap
import norswap.utils.multimap.multi_assoc
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.TypeVariable

// -------------------------------------------------------------------------------------------------

class ReflectionMethodInfo (val method: Method): MethodInfo()
{
    override val name = method.name
}

// -------------------------------------------------------------------------------------------------

@Suppress("CanBeParameter")
class ReflectionFieldInfo (val field: Field): FieldInfo()
{
    override val name: String = field.name
}

// -------------------------------------------------------------------------------------------------

open class ReflectionClassLike (val klass: Class<*>): ClassLike, ScopeBase()
{
    override val name = klass.simpleName!!

    override val full_name = klass.canonicalName!!

    override val kind = when
    {
        klass.isInterface  -> TypeDeclKind.INTERFACE
        klass.isEnum       -> TypeDeclKind.ENUM
        klass.isAnnotation -> TypeDeclKind.ANNOTATION
        else -> TypeDeclKind.CLASS
    }

    override val super_type
        = klass.superclass ?. let { ReflectionClassLike(it) }

    override val fields      by lazy { compute_fields()  }
    override val methods     by lazy { compute_methods() }
    override val class_likes by lazy { compute_class_likes() }
    override val type_params by lazy { compute_type_params() }

    private fun compute_fields(): MutableMap<String, FieldInfo>
        = klass.fields.associateTo(HashMap()) { it.name to ReflectionFieldInfo(it) }

    private fun compute_methods(): MutableMultiMap<String, MethodInfo>
        = klass.methods.multi_assoc { it.name to ReflectionMethodInfo(it) as MethodInfo }

    private fun compute_class_likes(): MutableMap<String, ClassLike>
        = klass.classes.associateTo(HashMap()) { it.name to ReflectionClassLike(it) }

    private fun compute_type_params(): MutableMap<String, TypeParameter>
        = klass.typeParameters.associateTo(HashMap()) { it.name to ReflectionTypeParameter(it) }

    override fun toString() = full_name
}

// -------------------------------------------------------------------------------------------------

class ReflectionTypeParameter (val typevar: TypeVariable<*>) : TypeParameter
{
    override val name: String
        get() = typevar.name

    override val upper_bound: RefType
        get() = TODO()
}

// -------------------------------------------------------------------------------------------------