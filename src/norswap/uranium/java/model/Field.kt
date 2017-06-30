package norswap.uranium.java.model
import norswap.lang.java8.ast.VarDecl
import org.objectweb.asm.tree.FieldNode

// -------------------------------------------------------------------------------------------------

abstract class Field: Member()

// -------------------------------------------------------------------------------------------------

class BytecodeField (node: FieldNode): Field()
{
    override val name = node.name!!
}

// -------------------------------------------------------------------------------------------------

class SourceField (node: VarDecl): Field()
{
    override val name = node.declarators[0].id.iden
}

// -------------------------------------------------------------------------------------------------

class ReflectionField (field: java.lang.reflect.Field): Field()
{
    override val name = field.name!!
}

// -------------------------------------------------------------------------------------------------