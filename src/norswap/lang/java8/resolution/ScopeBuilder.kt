package norswap.lang.java8.resolution
import norswap.lang.java8.typing.ClassLike
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

    fun type_chain (chain: List<String>): ClassLike?
    {
        TODO()
//        var klass = current.class_like(chain[0])
//        var i = 1
//
//        while (klass is Missing && i < chain.size) {
//            klass = Resolver.resolve_fully_qualified_class(chain.subList(0, i))
//            ++i
//        }
//
//        if (klass is ContinueOld) return klass
//        if (i == chain.size)   return Missing
//
//        // TODO: DELEGATION COMES INTO IT
//
//        for (j in i..chain.lastIndex) {
//            proclaim(klass as ClassLike)
//            klass = klass.class_likes[chain[j]]
//            if (klass == null) return null
//        }
//
//        return klass
    }
}