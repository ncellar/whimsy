package norswap.lang.java8.typing
import norswap.uranium.Attribute
import norswap.uranium.Node
import norswap.uranium.ReactorError
import norswap.lang.java8.typing.TypeError.*

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

inline fun TypeError (node: Node, crossinline init: ReactorError.() -> Unit)
    = ReactorError {
        affected = listOf(Attribute(node, "type"))
        init()
    }

// =================================================================================================

fun NotTypeError (node: Node) = TypeError (node) {
    _tag = NotTypeError
    _msg = "Applying '!' on a non-boolean type."
}

// -------------------------------------------------------------------------------------------------

fun ComplementTypeError (node: Node) = TypeError (node) {
    _tag = ComplementTypeError
    _msg = "Applying '~' on a non-integral type."
}

// -------------------------------------------------------------------------------------------------

fun UnaryArithTypeError (node: Node) = TypeError (node) {
    _tag = UnaryArithTypeError
    _msg = "Applying an unary arithmetic operation on a non-numeric type."
}

// -------------------------------------------------------------------------------------------------

fun BinaryArithTypeError (node: Node) = TypeError (node) {
    _tag = BinaryArithTypeError
    _msg = "Using a non-numeric value in an arithmetic expression."
}

// -------------------------------------------------------------------------------------------------

fun ShiftTypeError (node: Node) = TypeError (node) {
    _tag = ShiftTypeError
    _msg = "Using a non-integral value in a shift expression."
}

// -------------------------------------------------------------------------------------------------

fun OrderingTypeError (node: Node) = TypeError (node) {
    _tag = OrderingTypeError
    _msg = "Using a non-numeric value in a relational expression."
}

// -------------------------------------------------------------------------------------------------

fun InstanceofValueError (node: Node) = TypeError (node) {
    _tag = InstanceofValueError
    _msg = "Operand of instanceof operator does not have a reference type."
}

// -------------------------------------------------------------------------------------------------

fun InstanceofTypeError (node: Node) = TypeError (node) {
    _tag = InstanceofTypeError
    _msg = "Type operand of instanceof operator is not a reference type."
}

// -------------------------------------------------------------------------------------------------

fun InstanceofReifiableError (node: Node) = TypeError (node) {
    _tag = InstanceofReifiableError
    _msg = "Type operand of instanceof operator is not a reifiable type."
}

// -------------------------------------------------------------------------------------------------

fun InstanceofCompatError (node: Node) = TypeError (node) {
    _tag = InstanceofCompatError
    _msg = "Instanceof expression with incompatible operand and type."
}

// -------------------------------------------------------------------------------------------------

fun EqualNumBoolError (node: Node) = TypeError (node) {
    _tag = EqualNumBoolError
    _msg = "Attempting to compare a numeric type with a boolean type."
}

// -------------------------------------------------------------------------------------------------

fun EqualPrimRefError (node: Node) = TypeError (node) {
    _tag = EqualPrimRefError
    _msg = "Attempting to compare a primitive type with a reference type."
}

// -------------------------------------------------------------------------------------------------

fun EqualCompatError (node: Node) = TypeError (node) {
    _tag = EqualCompatError
    _msg = "Trying to compare two incompatible reference types."
}

// -------------------------------------------------------------------------------------------------

fun BitwiseMixedError (node: Node) = TypeError (node) {
    _tag = BitwiseMixedError
    _msg = "Binary bitwise operator has a boolean and a non-boolean operand."
}

// -------------------------------------------------------------------------------------------------

fun BitwiseRefError (node: Node) = TypeError (node) {
    _tag = BitwiseRefError
    _msg = "Using a non-integral or boolean value in a binary bitwise expression."
}

// -------------------------------------------------------------------------------------------------

fun LogicalTypeError (node: Node) = TypeError (node) {
    _tag = LogicalTypeError
    _msg = "Using a non-boolean expression in a logical expression."
}

// ------------------------------------------------------------------------------------------------