package norswap.uranium

// =================================================================================================

/**
 * Lightweight exception(no stack trace) meant to be thrown inside reactions and caught by the
 * reactor, indicating that the reaction will be continued in reaction [continuation].
 */
class Continue (val continuation: Reaction<*>)
    : RuntimeException("", null, false, false) // no stack trace

// =================================================================================================

/**
 * Lightweight exception (no stack trace) meant to be thrown inside reactions and caught by
 * the reactor, which extracts [error].
 */
class Fail (val error: ReactorError)
    : RuntimeException(error.msg, null, false, false) // no stack trace

// =================================================================================================