package norswap.lang.java8.resolution
import norswap.lang.java8.java_virtual_node
import norswap.lang.java8.typing.ClassLike
import norswap.lang.java8.typing.MemberInfo
import norswap.uranium.Attribute
import norswap.uranium.Reaction
import norswap.uranium.ReactorContext
import norswap.utils.attempt
import org.apache.bcel.classfile.ClassParser
import java.net.URL
import java.net.URLClassLoader

// =================================================================================================

// TODO
// - register_source_class: check for clashes
// - reactor errors for ambiguity
// - full_chain contains check
// - java virtual node subnodes
//      - for full class names
//      - for chains
// - implement klass_chain
// - implement resolve_members

// TODO: later
// - delete [ClassNotFoundScopeError] ?
// - handle ambiguity problems with eager loading
//      - e.g. class/package clashes

/**
 * ## Lexicon
 *
 * Simple class name: the unqualified class name (single identifier).
 *
 * Full class name: the fully qualified class name (with outer classes and package), separated with
 * dots.
 *
 * Canonical class name: the fully qualified class name (with outer classes and package). Each
 * class is separated from its inner classe by a dollar, while package components are separated by
 * dots.
 *
 * Full class chain: the list of components of a full class name.
 *
 * Class chain: a list of class name components that may or may not include the package part and/or
 * outer classes. Hence full class chains are a subset of all class chains.
 */
object Resolver
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Maps canonical class names to their class information.
     *
     * A null value indicates that the class could not be loaded from .class files or through
     * reflection, but could be defined later by a source file.
     */
    private val class_cache = HashMap<String, ClassLike?>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps full class chains to their class information.
     * This is important because the canonical name of a class chain is ambiguous.
     */
    private val chain_cache = HashMap<List<String>, ClassLike?>()

    // ---------------------------------------------------------------------------------------------

    private val syscl = ClassLoader.getSystemClassLoader() as URLClassLoader

    // ---------------------------------------------------------------------------------------------

    private val urls: Array<URL> = syscl.urLs

    // ---------------------------------------------------------------------------------------------

    private val loader = PathClassLoader(urls)

    // ---------------------------------------------------------------------------------------------

    /**
     * Register class information derived from a source file.
     */
    fun register_source_class (class_like: SourceClassLike)
    {
        // TODO: should really be cano name
        class_cache[class_like.canonical_name] = class_like
    }

    // ---------------------------------------------------------------------------------------------

    private fun load_from_url (class_url: URL): ClassLike
    {
        val cparser = ClassParser(class_url.openStream(), class_url.toString())
        val bclass = cparser.parse()
        return BytecodeClassLike(bclass)
    }

    // ---------------------------------------------------------------------------------------------

    private fun load_from_classfile (cano_name: String): ClassLike?
    {
        return loader.find_class_path(cano_name)
            ?. let { load_from_url(it) }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Some core classes have no associated .class files, search for those through reflection.
     * e.g. `java.lang.Object`
     *
     * However it seems that sometimes these classes have associated class files in `rt.jar`.
     */
    private fun load_reflectively (cano_name: String): ClassLike?
    {
        if (cano_name.startsWith("java.") || cano_name.startsWith("javax."))
            return attempt { syscl.loadClass(cano_name) } ?. let(::ReflectionClassLike)
        else
            return null
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Try to load a class from .class files or reflectively, using its canonical name, without
     * reading or writing the class cache.
     */
    private fun load_class (cano_name: String): ClassLike?
    {
        return load_from_classfile(cano_name) ?: load_reflectively(cano_name)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Given the canonical name of a class, returns its information from the cache, or schedules its
     * acquisition with the reactor. If the info is not available, two cases are possible:
     *
     * - there hasn't been an attempt to load the class yet
     * - previous attempts to load the file were unsuccessful
     *
     * In the first case, the method schedules an attempt to load the class information with
     * the reactor, then throws [Continue].
     *
     * In the second case, we might still be able to get the information later, when a source
     * file is added to the reactor. If [greedy] is false, we throw [Continue], else
     * we return null.
     *
     * This means that if [greedy] is true, only .class files and already visited source
     * files will be considered.
     */
    fun klass (cano_name: String, greedy: Boolean): ClassLike?
    {
        val cached = class_cache[cano_name]
        if (cached != null || greedy && class_cache.contains(cano_name))
            return cached

        val reactor   = ReactorContext.reactor
        val java_node = reactor.java_virtual_node
        val resolved  = Attribute(java_node, cano_name)

        if (!greedy && class_cache.contains(cano_name))
            throw Continue(resolved) // cannot load class, must come from source file

        // schedule attempt to load class
        reactor.enqueue(Reaction(java_node) {
            _provided = listOf(resolved)
            _trigger  = {
                val klass = load_class(cano_name)
                if (klass != null) resolved += klass
                class_cache[cano_name] = klass
            }
        })

        throw Continue(resolved)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Sugar for the other [klass] method, with `greedy` = false.
     */
    fun klass (cano_name: String): ClassLike
        = klass(cano_name, greedy = false)!!

    // ---------------------------------------------------------------------------------------------

    /**
     * Loads the named class eagerly (from the cache or by reading class files).
     * This function may block while class files are being read.
     * The cache may be updated as a result.
     * Throws an error if the class cannot be loaded in this way.
     *
     * This is used to load "well-known" Java classes (e.g. `java.lang.Object`).
     */
    fun eagerly (full_name: String): ClassLike
    {
        val cached = class_cache[full_name]
        if (cached != null) return cached

        val klass = load_class(full_name) ?: throw Error("could not load class: $full_name")
        class_cache[full_name] = klass
        return klass
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Given a full class chain, returns the class info, without reading or writing from
     * any cache.
     */
    private fun load_full_chain (cano_names: List<String>): ClassLike?
    {
        val class_likes = cano_names.mapNotNull(this::load_class)

        return when (class_likes.size) {
            0 -> null
            1 -> class_likes[0]
            else -> {
                // TODO reactor error
                null
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Derives all possible canonical names for the chain.
     */
    private fun cano_names (chain: List<String>): List<String>
    {
        val size = chain.size
        return chain.indices.map {
            val prefix = chain.subList(0, size - it).joinToString(".")
            if (it == 0) prefix
            else         prefix + chain.subList(size - it, size).joinToString("$", prefix = "$")
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Given a full class chain (a full top-level class name, followed by an optional sequence of
     * nested class names), returns the class info.
     *
     * - If there has been no attempt to load the class yet, throws [Continue].
     * - If previous attempts to load the class were unsuccessful, returns null.
     */
    fun full_chain (chain: List<String>): ClassLike?
    {
        // TODO contains check

        val cached = chain_cache[chain]
        if (cached != null) return cached

        val cano_names = cano_names(chain)

        // lookup the canonical names in the cache
        val class_likes = cano_names.mapNotNull(class_cache::get)
        when (class_likes.size) {
            0 -> {}
            1 -> return class_likes[0]
            else -> {
                // TODO reactor error
                return null
            }
        }

        val reactor   = ReactorContext.reactor
        val java_node = reactor.java_virtual_node
        val resolved  = Attribute(java_node, "chain($chain)") // TODO

        // schedule attempt to load class
        reactor.enqueue(Reaction(java_node) {
            _provided = listOf(resolved)
            _trigger  = {
                val klass = load_full_chain(cano_names)
                chain_cache[chain] = klass
                if (klass != null) {
                    resolved += klass
                    // TODO should really be cano name
                    class_cache[klass.canonical_name] = klass
                }
            }
        })

        throw Continue(resolved)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Given a class chain (a simple OR full class name, followed by an optional sequence of nested
     * class names), returns the class info.
     *
     * - If there has been no attempt to load the class yet, throws [Continue].
     * - If previous attempts to load the class were unsuccessful, returns null.
     */
    fun klass_chain (chain: List<String>): ClassLike?
    {
        // TODO
        // - how does discrimination between full and simple class names work
        TODO()
    }

    // ---------------------------------------------------------------------------------------------

    fun resolve_members (full_name: String, member: String): List<MemberInfo>
    {
        TODO()
//        val klass = klass(full_name) ?: return emptyList()
//        val members = klass.members(member)
//        if (members.isEmpty())
//            ReactorContext.reactor.register_error(MemberNotFoundScopeError())
//        return members
    }
}

// =================================================================================================

class PathClassLoader (urls: Array<URL>): URLClassLoader(urls)
{
    fun find_class_path (full_name: String): URL?
        = findResource(full_name.replace('.', '/') + ".class")
}

// =================================================================================================