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

    var continued_from: Reaction? = null

    // ---------------------------------------------------------------------------------------------

    private var _continuations: ArrayList<Reaction>? = null

    // ---------------------------------------------------------------------------------------------

    fun continuation (continuation: Reaction)
    {
        if (_continuations == null)
            _continuations = ArrayList()

        _continuations!!.add(continuation)
    }

    // ---------------------------------------------------------------------------------------------

    val continuations: List<Reaction>
        get() = _continuations ?: emptyList()

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