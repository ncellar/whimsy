package norswap.uranium
import norswap.utils.*
import norswap.utils.multimap.*
import java.util.ArrayDeque
import java.util.ArrayList

/**
 * The reactor orchestrates the derviation of attributes on one or multiple ASTs.
 *
 * ## Operations
 *
 * The reactor has a few basic operations:
 *
 * - Adding/removing visitors: [add_visitor], [remove_visitor]
 * - Adding/removing a root node: [roots], [visit_root]
 * - Visiting a node: [visit], [visit_root]
 * - Deriving attributes: [derive]
 * - Register an error: [register_error]
 * - Report errors: [errors]
 *
 * The reactor stores a set of root nodes. Each node is supposed to be the root of an AST, or the
 * root of a virtual node tree. A "virtual node" is a node that reifies concept that are not
 * encoded as syntax (e.g. scopes).
 *
 * The reactor also keeps a set of [NodeVisitor] instances that want to visit particular types of
 * nodes. When we ask the reactor to visit a node (which must occur under one of the roots), these
 * visitors will be applied to matching nodes.
 *
 * The purpose of the reactor is of course to derive attributes, and this is done with [derive].
 *
 * All the operations above have no strict ordering requirements: so you can interleave visits,
 * derivations, adding/removing new visitors and roots.
 *
 * ## Typical Scenario
 *
 * While operations are very flexible, the final goal is to derive attributes.
 * To do that, the usual way to proceed is to register visitors that create [Reaction] instances on
 * the tree. The most usual way to do this is by means of [Rule]. Then some roots have to be added
 * and visits have to be launched.
 *
 * When a [Reaction] without dependencies is registered on a [Node], it is automatically added
 * to the reactor's queue of ready reactions. Running [derive] will trigger all
 * such reactions. In turn, these reactions may satisfy the requirements of other reactions, which
 * are themselves added to the queue. This process continue until no more reactions are ready.
 *
 * Using [errors], the user can see the errors that occurred during the reactor's lifetime. There
 * are actually two type of errors reported: errors that occured when running reactions, and errors
 * that indicate that some reactions were not run. The first type of error is permanent, while the
 * second kind may disappear after adding nodes and/or reactions and running [derive] again.
 */
class Reactor
{
    // ---------------------------------------------------------------------------------------------

    /**
     * A queue of rules that are ready to be applied, during the execution of the reactor.
     */
    private val queue = ArrayDeque<Reaction<*>>()

    // ---------------------------------------------------------------------------------------------

    /**
     * A list of errors that occured during the lifetime of the reactor.
     */
    private val errors = ArrayList<ReactorError>()

    // ---------------------------------------------------------------------------------------------

    /**
     * List of root nodes over which this reactor operates.
     */
    val roots = ArrayList<Node>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps node types to visitor that want to visit them.
     */
    private val visitors = HashMultiMap<Class<out Node>, NodeVisitor<*>>()

    // ---------------------------------------------------------------------------------------------

    fun add_visitor (visitor: NodeVisitor<*>)
    {
        visitor.reactor = this
        visitor.domain
            ?. forEach { visitors.append(it, visitor) }
            ?: nth_superclass_targ(visitor, 1)
                ?. let_as <Class<out Node>, Unit> { visitors.append(it, visitor) }
                ?: throw Error("No domain specified.")
    }

    // ---------------------------------------------------------------------------------------------

    fun remove_visitor (visitor: NodeVisitor<*>)
    {
        visitor.domain
            ?. forEach { visitors.remove(it, visitor) }
            ?: nth_superclass_targ(visitor, 1)
                ?. let_as <Class<out Node>, Unit> { visitors.remove(it, visitor) }
                ?: throw Error("No domain specified.")
    }

    // ---------------------------------------------------------------------------------------------

    internal fun enqueue (reaction: Reaction<*>)
    {
        queue.add(reaction)
    }

    // ---------------------------------------------------------------------------------------------

    fun register_error (error: ReactorError, reaction: Reaction<*>? = null)
    {
        if (error.reaction == null)
            error._reaction = reaction
        errors.add(error)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds [node] as a new root, and visit it.
     */
    fun visit_root (node: Node)
    {
        roots.add(node)
        visit(node)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Visit [node], which must be a registered root, or a descendant of such a root.
     * Otherwise use [visit_root].
     */
    fun visit (node: Node)
    {
        Context.reactor = this
        node.visit_around { node, begin ->
            val vs = visitors.get_or_empty(node::class.java)
            vs.forEach { it(node, begin) }
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Start the reactor, applying ready reactions from this reactor's queue until it is empty.
     */
    fun derive()
    {
        while (queue.isNotEmpty())
        {
            val reaction = queue.remove()
            val errors_size = errors.size
            var error: ReactorError? = null

            try {
                reaction.triggered = true
                reaction.trigger()
            }
            catch (e: Fail) {
                error = e.error
                register_error(error, reaction)
            }
            catch (e: Continue) {
                reaction.continued_in = e.continuation
                e.continuation.continued_from = reaction
                continue
            }

            if (reaction.continued_from != null)
                enqueue(reaction.continued_from!!)

            // check that all attributes have been provided
            for (attr in reaction.provided)
            {
                // attribute provided
                if (attr.get() != null) continue

                // attribute not provided because of an error
                val new_errors = errors.subList(errors_size, errors.size)
                val covered = new_errors.any { it.affected.contains(attr) }
                if (covered) continue

                // attribute not provided: peg on main error
                if (error != null)
                    error.affected += attr
                // attribute unexplainably not provided: register a new error
                else
                    register_error(AttributeNotProvided(attr), reaction)
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a list of errors that occurred during this reactor's lifetime,
     * as well as [ReactionNotTriggered]s and [NoSupplier]s for all reactions
     * associated to any tree under the currently registered roots.
     */
    fun errors (): List<ReactorError>
        = roots
        .map(this::pending_reactions)
        .flatten()
        .map(::ReactionNotTriggered)
        .let(this::errors_with_pending)

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a list of errors that occurred during this reactor's lifetime,
     * as well as [ReactionNotTriggered]s and [NoSupplier]s for all reactions
     * associated to tree under [node].
     */
    fun errors (node: Node): List<ReactorError>
        = pending_reactions(node)
        .map(::ReactionNotTriggered)
        .let(this::errors_with_pending)

    // ---------------------------------------------------------------------------------------------

    private fun pending_reactions (node: Node): Set<Reaction<*>>
    {
        val set = HashSet<Reaction<*>>()

        node.visit_around { node, begin ->
            if (!begin) return@visit_around
            node.suppliers.values.flat_foreach {
                if (!it.triggered) set.add(it)
            }
        }

        return set
    }

    // ---------------------------------------------------------------------------------------------

    private fun errors_with_pending (pending: List<ReactorError>): List<ReactorError>
    {
        val errors = ArrayList<ReactorError>(this.errors)
        errors.addAll(pending)

        // find causes for missing attributes
        pending.forEach {
            val missing_attributes = it.reaction!!.consumed.filter { it.get() == null }

            it._causes = missing_attributes.map { missing ->
                var cause = errors.find { it.affected.contains(missing) }
                if (cause == null) {
                    cause = NoSupplier(missing)
                    errors.add(cause)
                    cause
                }
                else cause
            }
        }

        return errors
    }

    // ---------------------------------------------------------------------------------------------

}