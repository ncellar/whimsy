package norswap.lang.java8.typing
import norswap.uranium.Attribute
import norswap.uranium.Node
import norswap.uranium.ReactorError
import norswap.lang.java8.typing.TypeError.*
import norswap.uranium.Reaction

// =================================================================================================

enum class TypeError
{
    NotTypeError,
    ComplementTypeError,
    UnaryArithTypeError,
    BinaryArithTypeError,
    ShiftTypeError,
    OrderingTypeError,
    InstanceofValueError,
    InstanceofTypeError,
    InstanceofReifiableError,
    InstanceofCompatError,
    EqualNumBoolError,
    EqualPrimRefError,
    EqualCompatError,
    BitwiseMixedError,
    BitwiseRefError,
    LogicalTypeError,
}

// =================================================================================================

inline fun TypeError (reaction: Reaction<*>, node: Node, crossinline init: ReactorError.() -> Unit)
    = ReactorError {
        affected = listOf(Attribute(node, "type"))
        _reaction = reaction
        init()
    }

// =================================================================================================

fun NotTypeError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = NotTypeError
    _msg = "Applying '!' on a non-boolean type."
}

// -------------------------------------------------------------------------------------------------

fun ComplementTypeError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = ComplementTypeError
    _msg = "Applying '~' on a non-integral type."
}

// -------------------------------------------------------------------------------------------------

fun UnaryArithTypeError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = UnaryArithTypeError
    _msg = "Applying an unary arithmetic operation on a non-numeric type."
}

// -------------------------------------------------------------------------------------------------

fun BinaryArithTypeError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = BinaryArithTypeError
    _msg = "Using a non-numeric value in an arithmetic expression."
}

// -------------------------------------------------------------------------------------------------

fun ShiftTypeError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = ShiftTypeError
    _msg = "Using a non-integral value in a shift expression."
}

// -------------------------------------------------------------------------------------------------

fun OrderingTypeError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = OrderingTypeError
    _msg = "Using a non-numeric value in a relational expression."
}

// -------------------------------------------------------------------------------------------------

fun InstanceofValueError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = InstanceofValueError
    _msg = "Operand of instanceof operator does not have a reference type."
}

// -------------------------------------------------------------------------------------------------

fun InstanceofTypeError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = InstanceofTypeError
    _msg = "Type operand of instanceof operator is not a reference type."
}

// -------------------------------------------------------------------------------------------------

fun InstanceofReifiableError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = InstanceofReifiableError
    _msg = "Type operand of instanceof operator is not a reifiable type."
}

// -------------------------------------------------------------------------------------------------

fun InstanceofCompatError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = InstanceofCompatError
    _msg = "Instanceof expression with incompatible operand and type."
}

// -------------------------------------------------------------------------------------------------

fun EqualNumBoolError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = EqualNumBoolError
    _msg = "Attempting to compare a numeric type with a boolean type."
}

// -------------------------------------------------------------------------------------------------

fun EqualPrimRefError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = EqualPrimRefError
    _msg = "Attempting to compare a primitive type with a reference type."
}

// -------------------------------------------------------------------------------------------------

fun EqualCompatError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = EqualCompatError
    _msg = "Trying to compare two incompatible reference types."
}

// -------------------------------------------------------------------------------------------------

fun BitwiseMixedError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = BitwiseMixedError
    _msg = "Binary bitwise operator has a boolean and a non-boolean operand."
}

// -------------------------------------------------------------------------------------------------

fun BitwiseRefError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = BitwiseRefError
    _msg = "Using a non-integral or boolean value in a binary bitwise expression."
}

// -------------------------------------------------------------------------------------------------

fun LogicalTypeError (reaction: Reaction<*>, node: Node) = TypeError (reaction, node) {
    _tag = LogicalTypeError
    _msg = "Using a non-boolean expression in a logical expression."
}

// ------------------------------------------------------------------------------------------------