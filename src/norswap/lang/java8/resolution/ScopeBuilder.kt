package norswap.lang.java8.resolution
import java.util.ArrayDeque

class ScopeBuilder
{
    var current: Scope = PackageScope("")
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

    fun full_name (klass: String): String
    {
        var name: String?
        for (scope in scope_stack) {
            name = scope.full_name(klass)
            if (name != null) return name
        }
        throw Error("Could not find full name of $klass")
    }
}