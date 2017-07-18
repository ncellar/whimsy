package norswap.uranium.java.model2.bytecode
import norswap.uranium.java.model2.TypeParameter
import norswap.uranium.java.model2.bytecode.sig.SignatureType

class BytecodeTypeParameter (override val name: String): TypeParameter()
{
    var class_bound: SignatureType? = null
    val interface_bounds = ArrayList<SignatureType>()
}