package norswap.lang.java8.typing
import norswap.uranium.Attribute
import norswap.uranium.ErrorConstructor
import norswap.uranium.Node
import norswap.uranium.Reaction
import norswap.uranium.ReactorError

// =================================================================================================

inline fun TypeError (name: String, crossinline init: ReactorError.() -> Unit) = object: ErrorConstructor {
    val self = this
    override fun invoke(reac: Reaction<*>, node: Node) = ReactorError {
        _tag = self
        _reaction = reac
        affected = listOf(Attribute(node, "type"))
        init()
    }
    override fun toString() = name
}

// =================================================================================================

val NotTypeError = TypeError ("NotTypeError") {
    _msg = "Applying '!' on a non-boolean type."
}

// -------------------------------------------------------------------------------------------------

val ComplementTypeError = TypeError ("ComplementTypeError") {
    _msg = "Applying '~' on a non-integral type."
}

// -------------------------------------------------------------------------------------------------

val UnaryArithTypeError = TypeError ("UnaryArithTypeError") {
    _msg = "Applying an unary arithmetic operation on a non-numeric type."
}

// -------------------------------------------------------------------------------------------------

val BinaryArithTypeError = TypeError ("BinaryArithTypeError") {
    _msg = "Using a non-numeric value in an arithmetic expression."
}

// -------------------------------------------------------------------------------------------------

val ShiftTypeError = TypeError ("ShiftTypeError") {
    _msg = "Using a non-integral value in a shift expression."
}

// -------------------------------------------------------------------------------------------------

val OrderingTypeError = TypeError ("OrderingTypeError") {
    _msg = "Using a non-numeric value in a relational expression."
}

// -------------------------------------------------------------------------------------------------

val InstanceofValueError = TypeError ("InstanceofValueError") {
    _msg = "Operand of instanceof operator does not have a reference type."
}

// -------------------------------------------------------------------------------------------------

val InstanceofTypeError = TypeError ("InstanceofTypeError") {
    _msg = "Type operand of instanceof operator is not a reference type."
}

// -------------------------------------------------------------------------------------------------

val InstanceofReifiableError = TypeError ("InstanceofReifiableError") {
    _msg = "Type operand of instanceof operator is not a reifiable type."
}

// -------------------------------------------------------------------------------------------------

val InstanceofCompatError = TypeError ("InstanceofCompatError") {
    _msg = "Instanceof expression with incompatible operand and type."
}

// -------------------------------------------------------------------------------------------------

val EqualNumBoolError = TypeError ("EqualNumBoolError") {
    _msg = "Attempting to compare a numeric type with a boolean type."
}

// -------------------------------------------------------------------------------------------------

val EqualPrimRefError = TypeError ("EqualPrimeRefError") {
    _msg = "Attempting to compare a primitive type with a reference type."
}

// -------------------------------------------------------------------------------------------------

val EqualCompatError = TypeError ("EqualCompatError") {
    _msg = "Trying to compare two incompatible reference types."
}

// -------------------------------------------------------------------------------------------------

val BitwiseMixedError = TypeError ("BitwiseMixedError") {
    _msg = "Binary bitwise operator has a boolean and a non-boolean operand."
}

// -------------------------------------------------------------------------------------------------

val BitwiseRefError = TypeError ("BitwiseRefError") {
    _msg = "Using a non-integral or boolean value in a binary bitwise expression."
}

// -------------------------------------------------------------------------------------------------

val LogicalTypeError = TypeError ("LogicalTypeError") {
    _msg = "Using a non-boolean expression in a logical expression."
}

// ------------------------------------------------------------------------------------------------