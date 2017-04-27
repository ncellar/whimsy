package norswap.uranium
import norswap.uranium.ReactorErrorTag.*

// =================================================================================================

/**
 * A function that creates an error from the reaction and node where the error occurred.
 */
typealias ErrorConstructor = (Reaction<*>, Node) -> ReactorError

// =================================================================================================

enum class ReactorErrorTag
{
    ReactionNotTriggered,
    NoSupplier,
    AttributeNotDefined,
    AttributeNotProvided
}

// =================================================================================================

open class ReactorError
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates the type of error.
     */
    val tag get() = _tag
    lateinit var _tag: Any

    // ---------------------------------------------------------------------------------------------

    /**
     * The rule instance that caused the error (if any).
     */
    val reaction:  Reaction<*>? get() = _reaction
    var _reaction: Reaction<*>? = null

    // ---------------------------------------------------------------------------------------------

    /**
     * A description of the error.
     */
    val msg:  String get() = _msg
    var _msg: String = ""

    // ---------------------------------------------------------------------------------------------

    /**
     * The attributes whose value cannot be derived because of the error.
     *
     * Defaults to all the attributes provided by [reaction].
     */
    var affected: List<Attribute> = emptyList()

    // ---------------------------------------------------------------------------------------------

    /**
     * A list of errors that caused this error (possibly empty).
     */
    val causes get() = _causes
    var _causes: List<ReactorError> = emptyList()

    // ---------------------------------------------------------------------------------------------

    /**
     * The attribute that caused the error, if any.
     */
    val attribute_cause get() = _attribute_cause
    var _attribute_cause: Attribute? = null

    // ---------------------------------------------------------------------------------------------

    override fun toString() = msg

    // ---------------------------------------------------------------------------------------------
}

// =================================================================================================

/**
 * Initializing constructor for [ReactorError].
 */
inline fun ReactorError (init: ReactorError.() -> Unit): ReactorError
    = ReactorError().apply(init)

// =================================================================================================

/**
 * An error created at the end of a reactor's run, that indicates that a reaction
 * was not triggered during the run.
 */
fun ReactionNotTriggered (reaction: Reaction<*>) = ReactorError {
    _tag = ReactionNotTriggered
    _reaction = reaction
    _msg = "Reaction not triggered: $reaction"
}

// =================================================================================================

/**
 * An error create at the end of a reactor's run, that indicates that there were no rules
 * that could supply [attribute].
 */
fun NoSupplier (attribute: Attribute) = ReactorError {
    _tag = NoSupplier
    affected = listOf(attribute)
    _attribute_cause = attribute
    _msg = "No supplier for attribute: $attribute"
}

// =================================================================================================

/**
 * Indicates that a reaction tried to access [attribute] that wasn't defined yet.
 */
fun AttributeNotDefined (attribute: Attribute) = ReactorError {
    _tag = AttributeNotDefined
    _attribute_cause = attribute
    _msg = "Attribute not defined: $attribute"
}

// =================================================================================================

/**
 * Indicate that a rule instance that should have supplied an attribute didn't do so.
 */
fun AttributeNotProvided (attribute: Attribute) = ReactorError {
    _tag = NoSupplier
    affected = listOf(attribute)
    _attribute_cause = attribute
    _msg = "Attribute not provided: $attribute"
}

// =================================================================================================