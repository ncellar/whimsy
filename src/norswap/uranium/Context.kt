package norswap.uranium
import norswap.utils.thread_local.*

/**
 * Implicit context associated to a reactor.
 */
object Context
{
    var reactor: Reactor by thread_local.late_init()
    var reaction: Reaction<*> by thread_local.late_init()
}