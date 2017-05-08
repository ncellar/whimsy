package norswap.lang.java8.resolution
import norswap.lang.java8.ast.TypeDeclKind
import norswap.lang.java8.typing.ClassLike
import norswap.lang.java8.typing.FieldInfo
import norswap.lang.java8.typing.MethodInfo
import norswap.lang.java8.typing.RefType
import norswap.lang.java8.typing.TypeParameter
import org.apache.bcel.Const
import org.apache.bcel.classfile.ConstantUtf8
import org.apache.bcel.classfile.Field
import org.apache.bcel.classfile.InnerClass
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.classfile.Method

// =================================================================================================

class BytecodeMethodInfo (val method: Method): MethodInfo()
{
    override val name: String = method.name
}

// =================================================================================================

@Suppress("CanBeParameter")
class BytecodeFieldInfo (val field: Field): FieldInfo()
{
    override val name: String = field.name
}

// =================================================================================================

class BytecodeTypeParameter: TypeParameter
{
    override val name: String
        get() = TODO()

    override val upper_bound: RefType
        get() = TODO()
}

// =================================================================================================

open class BytecodeClassLike (val bclass: JavaClass): ClassLike, ScopeBase()
{
    // ---------------------------------------------------------------------------------------------

    override val name
        = bclass.className.substringAfterLast(".")

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

    override val super_type
        get() = Resolver.klass(bclass.superclassName)

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

// =================================================================================================
