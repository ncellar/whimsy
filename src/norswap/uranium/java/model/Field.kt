package norswap.uranium.java.model
import norswap.lang.java8.ast.EnumConstant
import norswap.lang.java8.ast.FormalParameter
import norswap.lang.java8.ast.VarDecl
import norswap.lang.java8.ast.VarDeclarator
import org.objectweb.asm.tree.FieldNode

// -------------------------------------------------------------------------------------------------

abstract class Field: Member()

// -------------------------------------------------------------------------------------------------

class BytecodeField (val node: FieldNode): Field()
{
    override val name = node.name
}

// -------------------------------------------------------------------------------------------------

class SourceField (val node: VarDecl, val decl: VarDeclarator): Field()
{
    override val name = decl.id.iden
}

// -------------------------------------------------------------------------------------------------

class SourceParameter (val node: FormalParameter): Field()
{
    override val name = node.name
}

// -------------------------------------------------------------------------------------------------

class SourceEnumConstant (val node: EnumConstant): Field()
{
    override val name = node.name
}

// -------------------------------------------------------------------------------------------------

class UntypedSourceParameter (override val name: String): Field()

// -------------------------------------------------------------------------------------------------

class ReflectionField (val field: java.lang.reflect.Field): Field()
{
    override val name = field.name
}

// -------------------------------------------------------------------------------------------------