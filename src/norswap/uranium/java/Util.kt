package norswap.uranium.java
import norswap.uranium.Propagator
import norswap.uranium.java.resolution.Resolver

val Propagator.resolver: Resolver
    get() = this.attachment as Resolver