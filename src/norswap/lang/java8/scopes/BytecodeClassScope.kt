package norswap.lang.java8.scopes
import norswap.lang.java8.ast.TypeDeclKind
import norswap.lang.java8.resolution.BytecodeFieldInfo
import norswap.lang.java8.resolution.BytecodeMethodInfo
import norswap.lang.java8.resolution.Resolver
import norswap.lang.java8.typing.FieldInfo
import norswap.lang.java8.typing.MethodInfo
import norswap.lang.java8.typing.RefType
import norswap.lang.java8.typing.TypeParameter
import norswap.utils.cast
import org.apache.bcel.Const
import org.apache.bcel.classfile.ConstantUtf8
import org.apache.bcel.classfile.InnerClass
import org.apache.bcel.classfile.JavaClass

open class BytecodeClassScope(val bclass: JavaClass): ClassScope()
{
    // ---------------------------------------------------------------------------------------------
    // MemberInfo
    // ---------------------------------------------------------------------------------------------

    override val name
        = bclass.className.substringAfterLast(".")

    // ---------------------------------------------------------------------------------------------
    // ClassScope
    // ---------------------------------------------------------------------------------------------

    override val canonical_name
        = bclass.className!!

    // ---------------------------------------------------------------------------------------------

    override val kind = when
    {
        bclass.isClass      -> TypeDeclKind.CLASS
        bclass.isInterface  -> TypeDeclKind.INTERFACE
        bclass.isEnum       -> TypeDeclKind.ENUM
        bclass.isAnnotation -> TypeDeclKind.ANNOTATION
        else -> throw Error("unknown class kind")
    }

    // ---------------------------------------------------------------------------------------------
    // InstantiableType
    // ---------------------------------------------------------------------------------------------

    // TODO
    override val super_type
        get() = Resolver.klass(bclass.superclassName)

    // ---------------------------------------------------------------------------------------------

    override val super_interfaces: List<RefType>
        get() = super.super_interfaces

    // ---------------------------------------------------------------------------------------------
    // Scope
    // ---------------------------------------------------------------------------------------------

    override fun field (name: String): FieldInfo?
    {
        val out: FieldInfo? = field_node.raw(name).cast()
        if (out != null) return out
        return outer?.field(name)
    }

    override fun method (name: String): Collection<MethodInfo>
    {
        val out: Collection<MethodInfo>? = method_node.raw(name).cast()
        if (out != null) return out
        modifier("Method Scope Update", method_node, name)
        return outer?.method(name) ?: emptyList<MethodInfo>()
    }

    override fun class_like (name: String): ClassScope?
    {
        val out: ClassScope? = class_like_node.raw(name).cast()
        if (out != null) return out
        modifier("Class Scope Update", class_like_node, name)
        return outer?.class_like(name)
    }

    override fun type_param (name: String): TypeParameter?
    {
        val out: TypeParameter? = type_param_node.raw(name).cast()
        if (out != null) return out
        modifier("Field Scope Update", type_param_node, name)
        return outer?.type_param(name)
    }

    // ---------------------------------------------------------------------------------------------
    // REST
    // ---------------------------------------------------------------------------------------------

    init {
        bclass.fields  .forEach { put_field  (BytecodeFieldInfo(it)) }
        bclass.methods .forEach { put_method (BytecodeMethodInfo(it)) }

        // TODO Do not register inner classes for now, these will be loaded lazily.

//        val inner_info = bclass.attributes
//            .filterIsInstance<InnerClasses>()
//            .firstOrNull()
//
//        inner_info?.innerClasses?.forEach {
//            val inner_name = nested_class_name(it)
//            val inner_canonical_name = canonical_name + '$' + inner_name
//            val klass = Resolver.klass(inner_canonical_name)
//            put_class_like(klass)
//        }

        // TODO parse type signature
        // val sig = bclass.attributes.find_instance<Signature>()?.signature
        // spec: https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.4
        // e.g. https://github.com/JetBrains/jdk8u_jdk/blob/master/src/share/classes/sun/reflect/generics/parser/SignatureParser.java
        // or: https://jboss-javassist.github.io/javassist/html/javassist/CtClass.html#getGenericSignature--
    }

    // ---------------------------------------------------------------------------------------------

    private fun nested_class_name (nested: InnerClass): String
    {
        // TODO anonymous class name handling
        val index = nested.innerNameIndex
        if (index == 0) return "" // anonymous nested class
        val const = bclass.constantPool.getConstant(index, Const.CONSTANT_Utf8)
        return (const as ConstantUtf8).bytes
    }

    // ---------------------------------------------------------------------------------------------

    override fun toString() = canonical_name
}