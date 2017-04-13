package norswap.lang.java8.resolution
import norswap.lang.java8.ast.TypeDeclKind
import norswap.lang.java8.typing.ClassLike
import norswap.lang.java8.typing.FieldInfo
import norswap.lang.java8.typing.MethodInfo
import norswap.lang.java8.typing.RefType
import norswap.lang.java8.typing.TypeParameter
import norswap.utils.multimap.MutableMultiMap
import norswap.utils.multimap.multi_assoc
import org.apache.bcel.Const
import org.apache.bcel.classfile.ConstantUtf8
import org.apache.bcel.classfile.Field
import org.apache.bcel.classfile.InnerClass
import org.apache.bcel.classfile.InnerClasses
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.classfile.Method
import org.apache.bcel.classfile.Signature

// -------------------------------------------------------------------------------------------------

class BytecodeMethodInfo (val method: Method): MethodInfo()
{
    override val name: String = method.name
}

// -------------------------------------------------------------------------------------------------

@Suppress("CanBeParameter")
class BytecodeFieldInfo (val field: Field): FieldInfo()
{
    override val name: String = field.name
}

// -------------------------------------------------------------------------------------------------

class BytecodeTypeParameter: TypeParameter
{
    override val name: String
        get() = TODO()

    override val upper_bound: RefType
        get() = TODO()
}

// -------------------------------------------------------------------------------------------------

open class BytecodeClassLike (val bclass: JavaClass): ClassLike, ScopeBase()
{
    override val name
        = bclass.className.substringAfterLast(".")

    override val canonical_name
        = bclass.className!!

    override val kind = when
    {
        bclass.isClass      -> TypeDeclKind.CLASS
        bclass.isInterface  -> TypeDeclKind.INTERFACE
        bclass.isEnum       -> TypeDeclKind.ENUM
        bclass.isAnnotation -> TypeDeclKind.ANNOTATION
        else -> throw Error("implementation error: unknown class kind")
    }

    override val super_type
        get() = Resolver.klass(bclass.superclassName)

    override val fields      by lazy { compute_fields() }
    override val methods     by lazy { compute_methods() }
    override val class_likes by lazy { compute_class_likes() }
    override val type_params by lazy { compute_type_params() }

    private fun compute_fields(): MutableMap<String, FieldInfo>
        = bclass.fields.associateTo(HashMap()) { it.name to BytecodeFieldInfo(it) }

    private fun compute_methods(): MutableMultiMap<String, MethodInfo>
        = bclass.methods.multi_assoc { it.name to BytecodeMethodInfo(it) as MethodInfo }

    private fun compute_class_likes(): MutableMap<String, ClassLike>
    {
        val attr = bclass.attributes
            .filterIsInstance<InnerClasses>()
            .firstOrNull()
            ?: return HashMap<String, ClassLike>()

        return attr.innerClasses.associateTo(HashMap()) {
            // TODO anonymous class name handling
            val name = nested_class_name(it)
            name to Resolver.klass(this.canonical_name + "$" + name)
        }
    }

    private fun compute_type_params(): MutableMap<String, TypeParameter>
    {
        val sig = (bclass.attributes.first { it is Signature } as Signature).signature
        if (sig[0] != '<') return HashMap()
        // TODO parse type signature
        // spec: https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.4
        // e.g. https://github.com/JetBrains/jdk8u_jdk/blob/master/src/share/classes/sun/reflect/generics/parser/SignatureParser.java
        // or: https://jboss-javassist.github.io/javassist/html/javassist/CtClass.html#getGenericSignature--
        return HashMap()
    }

    private fun nested_class_name (nested: InnerClass): String
    {
        val index = nested.innerNameIndex
        if (index == 0) return "" // anonymous nested class
        val const = bclass.constantPool.getConstant(index, Const.CONSTANT_Utf8)
        return (const as ConstantUtf8).bytes
    }

    override fun toString() = canonical_name
}

// -------------------------------------------------------------------------------------------------
