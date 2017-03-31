package norswap.lang.java8.resolution
import norswap.lang.java8.java_virtual_node
import norswap.lang.java8.typing.ClassLike
import norswap.uranium.Attribute
import norswap.uranium.Reaction
import norswap.uranium.ReactorContext
import norswap.utils.attempt
import org.apache.bcel.classfile.ClassParser
import java.net.URL
import java.net.URLClassLoader

// =================================================================================================

object Resolver
{
    // ---------------------------------------------------------------------------------------------

    private val class_cache = HashMap<String, ClassLike?>()

    // ---------------------------------------------------------------------------------------------

    private val syscl = ClassLoader.getSystemClassLoader() as URLClassLoader

    // ---------------------------------------------------------------------------------------------

    val urls: Array<URL> = syscl.urLs

    // ---------------------------------------------------------------------------------------------

    val loader = PathClassLoader(urls)

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the named class' information from the cache, or schedules the acquisition of such
     * information with the reactor, using [seek_class]. If the info is not immediately available
     * from the cache or cannot be loaded via [seek_class], throws [Continue].
     */
    fun klass (full_name: String): ClassLike
    {
        val cached = class_cache[full_name]
        if (cached != null) return cached

        val reactor   = ReactorContext.reactor
        val java_node = reactor.java_virtual_node
        val resolved  = Attribute(java_node, full_name)

        if (class_cache.contains(full_name))
            throw Continue(resolved) // cannot load class, must come from source file

        // schedule attempt to load class
        reactor.enqueue(Reaction(java_node) {
            _provided = listOf(resolved)
            _trigger  = {
                // TODO make sure this is added to the cache
                val klass = seek_class(full_name)
                if (klass != null) resolved += klass
            }
        })

        throw Continue(resolved)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Loads the named class, from the cache if possible or using [seek_class] (in which case
     * it updates the cache with the result).
     *
     * Throws an exception if the class cannot be loaded in this way.
     */
    fun load (full_name: String): ClassLike
    {
        val cached = class_cache[full_name]
        if (cached != null) return cached

        val klass = seek_class(full_name) ?: throw Error("could not load class: $full_name")
        class_cache[full_name] = klass
        return klass
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Try to load the named class from .class files or reflectively, without reading or
     * writing the class cache.
     */
    private fun seek_class (full_name: String): ClassLike?
    {
        val class_url = loader.find_class_path(full_name)

        if (class_url != null) {
            val cparser = ClassParser(class_url.openStream(), class_url.toString())
            val bclass = cparser.parse()
            return BytecodeClassLike(bclass)
        }

        // Some core classes have no associated .class files, search for those through reflection.
        // e.g. Object (but sometimes it is in rt.jar)
        if (full_name.startsWith("java.") || full_name.startsWith("javax."))
            return attempt { syscl.loadClass(full_name) } ?. let(::ReflectionClassLike)

        return null
    }

    // ---------------------------------------------------------------------------------------------

    fun resolve_fully_qualified_class (chain: List<String>): ClassLike?
    {
        TODO()
//        top@ for (i in chain.indices) {
//            val prefix = chain.subList(0, chain.size - i).joinToString(".")
//            var klass = resolve_class(prefix) ?: continue
//            for (j in 1..i) {
//                val name = chain[chain.size - i - 1 + j]
//                klass = resolve_nested_class(klass, name) ?: continue@top
//            }
//            return klass
//        }
//        return null
    }

    // ---------------------------------------------------------------------------------------------

    fun resolve_nested_class (klass: ClassLike, name: String): ClassLike?
    {
        TODO()
//        val nested = klass.class_likes[name] ?: return null
//        return resolve_class(klass.full_name + "$" + nested.name)
    }
}

// =================================================================================================

class PathClassLoader (urls: Array<URL>): URLClassLoader(urls)
{
    fun find_class_path (full_name: String): URL?
        = findResource(full_name.replace('.', '/') + ".class")
}

// =================================================================================================