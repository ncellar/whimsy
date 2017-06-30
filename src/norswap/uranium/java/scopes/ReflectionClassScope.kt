package norswap.uranium.java.scopes
import norswap.uranium.java.model.*

class ReflectionClassScope (val klass: Class<*>): ClassScope()
{
    override fun field(name: String): Field? {
        TODO()
    }

    override fun method(name: String): List<Method> {
        TODO()
    }

    override fun klass(name: String): Klass? {
        TODO()
    }

    override fun member(name: String): List<Member> {
        TODO()
    }
//
//    override val name = klass.simpleName!!
//
//    override val timestamp = 0L
//
//    override val canonical_name = klass.canonicalName!!
//
//    override val kind = when
//    {
//        klass.isInterface  -> TypeDeclKind.INTERFACE
//        klass.isEnum       -> TypeDeclKind.ENUM
//        klass.isAnnotation -> TypeDeclKind.ANNOTATION
//        else -> TypeDeclKind.CLASS
//    }
//
//    override val super_type
//        = klass.superclass ?. let { ReflectionClassLike(it) }
//
//    init {
//        klass.fields         .forEach { put_field      (ReflectionFieldInfo     (it)) }
//        klass.methods        .forEach { put_method     (ReflectionMethodInfo    (it)) }
//        klass.classes        .forEach { put_class_like (ReflectionClassLike     (it)) }
//        klass.typeParameters .forEach { put_param      (ReflectionTypeParameter (it)) }
//    }
//
//    override fun toString() = canonical_name
}