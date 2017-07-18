package norswap.uranium.java.model.scopes
import norswap.uranium.java.model.ArrayType
import norswap.uranium.java.model.ClassType
import norswap.uranium.java.model.ExtendsWildcard
import norswap.uranium.java.model.InnerClassType
import norswap.uranium.java.model.ParameterizedType
import norswap.uranium.java.model.PrimitiveType
import norswap.uranium.java.model.SuperWildcard
import norswap.uranium.java.model.Type
import norswap.uranium.java.model.TypeParameter
import norswap.uranium.java.model.TypeVariable
import norswap.uranium.java.model.Wildcard
import org.objectweb.asm.Opcodes.ASM5
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor
import java.util.ArrayDeque

fun parse_class_signature (signature: String): List<TypeParameter>
{
    val reader  = SignatureReader(signature)
    val visitor = ClassSignatureParser()
    reader.accept(visitor)
    return visitor.parameters
}

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

open class TypeSignatureParser: SignatureVisitor(ASM5)
{
    var type: Type? = null
    val ops = ArrayDeque<() -> Unit>()

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

    override fun visitTypeVariable (name: String)
    {
        type = TypeVariable(name)
        ops.forEach { it() }
    }

    override fun visitArrayType(): SignatureVisitor
    {
        ops.push { type = ArrayType(type!!) }
        return this
    }

    override fun visitClassType (name: String)
    {
        val type = ClassType(name)
        ops.push { type.type_arguments.reverse() }
        this.type = type
    }

    override fun visitTypeArgument()
    {
        val type = type as ParameterizedType
        ops.push { type.type_arguments += Wildcard }
    }

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

    override fun visitEnd()
    {
        ops.forEach { it() }
    }

    override fun visitInnerClassType (name: String)
    {
        val type = InnerClassType(type!!, name)
        ops.push { type.type_arguments.reverse() }
        this.type = type
    }
}

class ClassSignatureParser: SignatureVisitor(ASM5)
{
    val parameters = ArrayList<TypeParameter>()

    override fun visitFormalTypeParameter (name: String)
    {
        parameters += TypeParameter(name)
    }

    override fun visitClassBound(): SignatureVisitor
    {
        val visitor = TypeSignatureParser()
        visitor.ops.push { parameters.last().class_bound = visitor.type!! }
        return visitor
    }

    override fun visitInterfaceBound(): SignatureVisitor
    {
        val visitor = TypeSignatureParser()
        visitor.ops.push { parameters.last().interface_bounds += visitor.type!! }
        return visitor
    }

    override fun visitSuperclass(): SignatureVisitor
    {

        return this
    }
}