package norswap.lang.java8.resolution
import norswap.lang.java8.scopes.PackageScope
import norswap.lang.java8.scopes.Scope
import java.util.ArrayDeque

class ScopeBuilder
{
    var current: Scope = PackageScope("") // EmptyScope
        private set

    private val scope_stack = ArrayDeque<Scope>()

    fun push (scope: Scope)
    {
        scope_stack.push(current)
        current = scope
    }

    fun pop(): Scope
    {
        val out = current
        current = scope_stack.pop()
        return out
    }
}