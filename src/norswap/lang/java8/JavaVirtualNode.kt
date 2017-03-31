package norswap.lang.java8
import norswap.uranium.CNode
import norswap.uranium.Reactor
import norswap.utils.first_instance

class JavaVirtualNode: CNode()

val Reactor.java_virtual_node: JavaVirtualNode
    get() = roots.first_instance()