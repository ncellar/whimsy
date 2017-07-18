package norswap.uranium.java.model2.bytecode.sig
import org.objectweb.asm.Opcodes.ASM5
import org.objectweb.asm.signature.SignatureVisitor
import java.util.ArrayDeque

/*

The methods of this class must be called in the following order:

TypeSignature =
    visitBaseType | visitTypeVariable | visitArrayType |
    ( visitClassType visitTypeArgument* ( visitInnerClassType visitTypeArgument* )* visitEnd ) )

*/
open class TypeSignatureParser: SignatureVisitor(ASM5)
{
    // ---------------------------------------------------------------------------------------------

    var type: SignatureType? = null
    val ops = ArrayDeque<() -> Unit>()

    // ---------------------------------------------------------------------------------------------

    override fun visitBaseType (descriptor: Char)
    {
        type = PrimitiveType(when (descriptor) {
            'W'  -> "boolean"
            'B'  -> "byte"
            'C'  -> "char"
            'S'  -> "short"
            'I'  -> "int"
            'J'  -> "long"
            'F'  -> "float"
            'D'  -> "double"
            'L'  -> "long"
            'V'  -> "void"
            else -> throw Error()
        })
        ops.forEach { it() }
    }

    // ---------------------------------------------------------------------------------------------

    override fun visitTypeVariable (name: String)
    {
        type = TypeVariable(name)
        ops.forEach { it() }
    }

    // ---------------------------------------------------------------------------------------------

    override fun visitArrayType(): SignatureVisitor
    {
        ops.push { type = ArrayType(type!!) }
        return this
    }

    // ---------------------------------------------------------------------------------------------

    override fun visitClassType (name: String)
    {
        val type = ClassType(name)
        ops.push { type.type_arguments.reverse() }
        this.type = type
    }

    // ---------------------------------------------------------------------------------------------

    override fun visitTypeArgument()
    {
        val type = type as ParameterizedType
        ops.push { type.type_arguments += Wildcard }
    }

    // ---------------------------------------------------------------------------------------------

    override fun visitTypeArgument (wildcard: Char): SignatureVisitor
    {
        val visitor = TypeSignatureParser()
        val type = type as ParameterizedType

        ops.push {
            val vtype = visitor.type!!
            type.type_arguments += when (wildcard) {
                '='  -> vtype
                '+'  -> ExtendsWildcard(vtype)
                '-'  -> SuperWildcard(vtype)
                else -> throw Error()
            }
        }

        return visitor
    }

    // ---------------------------------------------------------------------------------------------

    override fun visitEnd()
    {
        ops.forEach { it() }
    }

    // ---------------------------------------------------------------------------------------------

    override fun visitInnerClassType (name: String)
    {
        val type = InnerClassType(type!!, name)
        ops.push { type.type_arguments.reverse() }
        this.type = type
    }

    // ---------------------------------------------------------------------------------------------
}