package norswap.lang.java8
import norswap.lang.java8.scopes.Scope
import norswap.uranium.CNode
import norswap.uranium.Reactor
import norswap.uranium.ast_utils.nseq
import norswap.utils.first_instance

class JavaVirtualNode: CNode()
{
    val chains = Scope.Node()
    val classes = Scope.Node()

    override fun children() = nseq(chains, classes)
}

val Reactor.java_virtual_node: JavaVirtualNode
    get() = roots.first_instance()