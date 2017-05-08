package norswap.lang.java8
import norswap.lang.java8.resolution.ScopeNode
import norswap.uranium.CNode
import norswap.uranium.Reactor
import norswap.uranium.ast_utils.nseq
import norswap.utils.first_instance

class JavaVirtualNode: CNode()
{
    val chains = ScopeNode()
    val classes = ScopeNode()

    override fun children() = nseq(chains, classes)
}

val Reactor.java_virtual_node: JavaVirtualNode
    get() = roots.first_instance()