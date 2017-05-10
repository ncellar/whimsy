package norswap.lang.java8.resolution
import norswap.lang.java8.typing.FieldInfo
import norswap.lang.java8.typing.MethodInfo
import norswap.lang.java8.typing.RefType
import norswap.lang.java8.typing.TypeParameter
import org.apache.bcel.classfile.Field
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

// =================================================================================================
