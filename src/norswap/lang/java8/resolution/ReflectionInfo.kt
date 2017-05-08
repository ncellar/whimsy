package norswap.lang.java8.resolution
import norswap.lang.java8.ast.TypeDeclKind
import norswap.lang.java8.typing.ClassLike
import norswap.lang.java8.typing.FieldInfo
import norswap.lang.java8.typing.MethodInfo
import norswap.lang.java8.typing.RefType
import norswap.lang.java8.typing.TypeParameter
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

    override val canonical_name = klass.canonicalName!!

    override val kind = when
    {
        klass.isInterface  -> TypeDeclKind.INTERFACE
        klass.isEnum       -> TypeDeclKind.ENUM
        klass.isAnnotation -> TypeDeclKind.ANNOTATION
        else -> TypeDeclKind.CLASS
    }

    override val super_type
        = klass.superclass ?. let { ReflectionClassLike(it) }

    init {
        klass.fields         .forEach { put_field      (ReflectionFieldInfo     (it)) }
        klass.methods        .forEach { put_method     (ReflectionMethodInfo    (it)) }
        klass.classes        .forEach { put_class_like (ReflectionClassLike     (it)) }
        klass.typeParameters .forEach { put_param      (ReflectionTypeParameter (it)) }
    }

    override fun toString() = canonical_name
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