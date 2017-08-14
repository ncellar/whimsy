package norswap.uranium
import norswap.utils.Advice1

// -------------------------------------------------------------------------------------------------

/** Begone, ugly angle brackets. */
typealias AnyClass = Class<*>

// -------------------------------------------------------------------------------------------------

/** Type for AST node visitors, equivalent to `(Any, Boolean) -> Unit` */
typealias NodeVisitor = Advice1<Any, Unit>

// -------------------------------------------------------------------------------------------------