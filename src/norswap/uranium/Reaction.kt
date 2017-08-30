package norswap.uranium

class Reaction private constructor()
{
    // ---------------------------------------------------------------------------------------------

    constructor (init: Reaction.() -> Unit): this()
    {
        init()
        satisfied = kotlin.BooleanArray(consumed.size)
    }

    // ---------------------------------------------------------------------------------------------

    var name = "Anonymous"

    // ---------------------------------------------------------------------------------------------

    var apply: (Reaction) -> Unit = {}

    // ---------------------------------------------------------------------------------------------

    var consumed = emptyList<Attribute>()

    // ---------------------------------------------------------------------------------------------

    var supplied = emptyList<Attribute>()

    // ---------------------------------------------------------------------------------------------

    private lateinit var satisfied: BooleanArray

    // ---------------------------------------------------------------------------------------------

    private var nsatisfied = 0

    // ---------------------------------------------------------------------------------------------

    internal fun satisfied (propagator: Propagator, attr: Attribute)
    {
        val i = consumed.indexOf(attr)
        if (!satisfied[i]) {
            ++nsatisfied
            satisfied[i] = true
        }
        if (nsatisfied == consumed.size)
            propagator.enqueue(this)
    }

    // ---------------------------------------------------------------------------------------------
}