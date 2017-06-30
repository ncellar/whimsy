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

    var apply: () -> Unit = {}

    // ---------------------------------------------------------------------------------------------

    var consumed = emptyList<Attribute>()

    // ---------------------------------------------------------------------------------------------

    var supplied = emptyList<Attribute>()

    // ---------------------------------------------------------------------------------------------

    lateinit var propagator: Propagator
        internal set

    // ---------------------------------------------------------------------------------------------

    private lateinit var satisfied: BooleanArray

    // ---------------------------------------------------------------------------------------------

    private var nsatisfied = 0

    // ---------------------------------------------------------------------------------------------

    internal fun satisfied (propagator: Propagator, attr: Attribute)
    {
        val i = consumed.indexOf(attr)
        if (!satisfied[i])
            ++ nsatisfied
        if (nsatisfied == consumed.size)
            propagator.enqueue(this)
        satisfied[i] = true
    }

    // ---------------------------------------------------------------------------------------------
}