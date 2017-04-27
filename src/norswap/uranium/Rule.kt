package norswap.uranium

/**
 * A reactive rule, i.e. a [NodeVisitor] whose [invoke] method registers a [Reaction] with a
 * [Reactor].
 *
 * In particular, given a domain node, the rule defines [consumed] attributes and [provided]
 * attributes, as well as how to derived the later ([compute]).
 */
abstract class Rule <N: Node>: NodeVisitor<N>()
{
    // ---------------------------------------------------------------------------------------------

    override fun visit (node: N, begin: Boolean)
    {
        if (begin) return

        Reaction(node) {
            _consumed = consumed(node)
            _provided = provided(node)
            _trigger  = { compute() }
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Given a domain node, returns the attributes required for a [Reaction] created by
     * this rule to trigger.
     *
     * The default implementation returns an empty list.
     */
    open fun consumed (node: N) = emptyList<Attribute>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Given a domain node, returns the attributes provided by a [Reaction] created by this rule.
     */
    abstract fun provided (node: N): List<Attribute>

    // ---------------------------------------------------------------------------------------------

    /**
     * Derive the provided attributes: implementation for [RuleReaction.trigger].
     */
    abstract fun Reaction<N>.compute()

    // ---------------------------------------------------------------------------------------------

    /**
     * An utility function for reporting errors using an [ErrorConstructor].
     */
    inline fun Reaction<N>.report (mk: ErrorConstructor): Unit
        = reactor.register_error(mk(this, node), this)

    // ---------------------------------------------------------------------------------------------
}
