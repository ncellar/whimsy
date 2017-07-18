package norswap.uranium.java.model2.bytecode.sig
import norswap.uranium.java.model2.bytecode.BytecodeTypeParameter
import org.objectweb.asm.Opcodes.ASM5
import org.objectweb.asm.signature.SignatureVisitor

/*

The methods of this class must be called in one of the two following order:

ClassSignature =
    ( visitFormalTypeParameter visitClassBound? visitInterfaceBound* )*
    ( visitSuperclass visitInterface* )

MethodSignature =
    ( visitFormalTypeParameter visitClassBound? visitInterfaceBound* )*
    ( visitParameterType* visitReturnType visitExceptionType* )

*/
class ClassOrMethodSignatureParser : SignatureVisitor(ASM5)
{
    // ---------------------------------------------------------------------------------------------

    val parameters = HashMap<String, BytecodeTypeParameter>()
    lateinit var last: BytecodeTypeParameter

    // ---------------------------------------------------------------------------------------------

    override fun visitFormalTypeParameter (name: String)
    {
        last = BytecodeTypeParameter(name)
        parameters[name] = last
    }

    // ---------------------------------------------------------------------------------------------

    override fun visitClassBound(): SignatureVisitor
    {
        val visitor = TypeSignatureParser()
        visitor.ops.push { last.class_bound = visitor.type!! }
        return visitor
    }

    // ---------------------------------------------------------------------------------------------

    override fun visitInterfaceBound(): SignatureVisitor
    {
        val visitor = TypeSignatureParser()
        visitor.ops.push { last.interface_bounds += visitor.type!! }
        return visitor
    }

    // ---------------------------------------------------------------------------------------------

    /*

    Methods that are called but not overriden use the default implementation, which returns `this`.
    This is fine, because only methods not overriden by this class are called on the returned
    object.

     */

    // ---------------------------------------------------------------------------------------------
}