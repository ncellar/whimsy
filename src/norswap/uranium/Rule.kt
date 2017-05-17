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
            label = this@Rule.label
            _consumed = consumed(node)
            _provided = provided(node)
            _trigger  = { compute() }
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * The label for reactions created by this rule.
     */
    open val label = this::class.java.simpleName

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
     * Derive the provided attributes: implementation for [Reaction.trigger].
     */
    abstract fun Reaction<N>.compute()

    // ---------------------------------------------------------------------------------------------

    /**
     * An utility function for reporting errors using an [ErrorConstructor].
     */
    fun Reaction<N>.report (error: ReactorError): Unit
        = reactor.register_error(error, this)

    // ---------------------------------------------------------------------------------------------
}
