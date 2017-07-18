package norswap.uranium.java.model2.bytecode.sig
import norswap.uranium.java.model2.bytecode.BytecodeTypeParameter
import org.objectweb.asm.signature.SignatureReader

// -------------------------------------------------------------------------------------------------

/**
 * Parse the given signature and returns a map of type parameters.
 */
fun parse_type_parameters (signature: String): Map<String, BytecodeTypeParameter>
{
    val reader  = SignatureReader(signature)
    val visitor = ClassOrMethodSignatureParser()
    reader.accept(visitor)
    return visitor.parameters
}

// -------------------------------------------------------------------------------------------------

/*

Class SignatureVisitor

A visitor to visit a generic signature. The methods of this interface must be called in one of the
three following orders (the last one is the only valid order for a SignatureVisitor that is returned
by a method of this interface):

ClassSignature =
    ( visitFormalTypeParameter visitClassBound? visitInterfaceBound* )*
    ( visitSuperclass visitInterface* )

MethodSignature =
    ( visitFormalTypeParameter visitClassBound? visitInterfaceBound* )*
    ( visitParameterType* visitReturnType visitExceptionType* )

TypeSignature =
    visitBaseType | visitTypeVariable | visitArrayType |
    ( visitClassType visitTypeArgument* ( visitInnerClassType visitTypeArgument* )* visitEnd ) )

*/