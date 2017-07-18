package norswap.uranium.java.model.bytecode
import norswap.uranium.java.model.TypeParameter
import norswap.uranium.java.model.bytecode.sig.SignatureType

class BytecodeTypeParameter (override val name: String): TypeParameter()
{
    var class_bound: SignatureType? = null
    val interface_bounds = ArrayList<SignatureType>()
}