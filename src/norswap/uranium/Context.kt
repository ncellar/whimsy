package norswap.uranium
import norswap.utils.thread_local.*

/**
 * Implicit context associated to a reactor.
 * This avoids passing the reactor to all function calls.
 */
object Context
{
    var reactor: Reactor by thread_local.late_init<Reactor>()
}