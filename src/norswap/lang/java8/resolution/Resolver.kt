package norswap.lang.java8.resolution
import norswap.lang.java8.java_virtual_node
import norswap.lang.java8.typing.ClassLike
import norswap.lang.java8.typing.MemberInfo
import norswap.uranium.Attribute
import norswap.uranium.Continue
import norswap.uranium.Fail
import norswap.uranium.Reaction
import norswap.uranium.Context
import norswap.utils.attempt
import org.apache.bcel.classfile.ClassParser
import java.net.URL
import java.net.URLClassLoader

// =================================================================================================

// TODO
// - consistency of chain decisions
//      - what when new source introduces ambiguity?
//      - always keep oldest?
// - rationalize klass, class, class_like

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
     * Bogus class-like object used to represent "failures to load" in the caches.
     * [continuation] the reaction that can provide the requested class.
     * Never escapes the resolver.
     */
    private class Miss (val continuation: Reaction<*>): ClassLike
    {
        override val name           get() = throw NotImplementedError()
        override val canonical_name get() = throw NotImplementedError()
        override val kind           get() = throw NotImplementedError()
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps canonical class names to their class information.
     *
     * A null value indicates that the class could not be loaded from .class files or through
     * reflection, but could be defined later by a source file.
     */
    private val class_cache = HashMap<String, ClassLike>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps full class chains to their class information.
     * This is important because the canonical name of a class chain is ambiguous.
     */
    private val chain_cache = HashMap<List<String>, ClassLike>()

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
        val old = class_cache.putIfAbsent(class_like.canonical_name, class_like)
        if (old != null)
            throw Fail(ConflictingClassDefinitions(class_like.canonical_name))
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
     * - previous attempts to load the class were unsuccessful
     *
     * In the first case, the method schedules an attempt to load the class information with
     * the reactor, then throws [Continue].
     *
     * In the second case, we might still be able to get the information later, when a source
     * file is added to the reactor, so we throw [Continue].
     */
    fun klass (cano_name: String): ClassLike
    {
        val cached = class_cache[cano_name]
        if (cached is Miss) throw Continue(cached.continuation)  // must come from source file
        if (cached != null) return cached

        val reactor    = Context.reactor
        val klass_node = reactor.java_virtual_node.classes
        val required   = Attribute(klass_node, cano_name)

        // schedule attempt to load class
        throw Continue(Reaction(klass_node) {
            _provided = listOf(required)
            _trigger  = {
                val klass = load_class(cano_name)
                if (klass != null) required += klass
                class_cache[cano_name] = klass ?: Miss(this)
            }
        })
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Loads the named class eagerly (from the cache or by reading class files).
     * This function may block while class files are being read.
     * The cache may be updated as a result.
     * Throws an error if the class cannot be loaded in this way.
     *
     * This is used to load "well-known" Java classes (e.g. `java.lang.Object`).
     */
    fun eagerly (cano_name: String): ClassLike
    {
        val cached = class_cache[cano_name]
        if (cached != null) return cached

        val klass = load_class(cano_name) ?: throw Error("could not load class: $cano_name")
        class_cache[cano_name] = klass
        return klass
    }

    // ---------------------------------------------------------------------------------------------

    private fun extract_chain_class (cano_names: List<String>, class_likes: List<ClassLike>)
            : ClassLike?
        = when (class_likes.size) {
            0 -> null
            1 -> class_likes[0]
            else -> {
                val full_name = cano_names[0].replace('$', '.')
                throw Fail(AmbiguousClassDefinitions(full_name))
            }
        }

    // ---------------------------------------------------------------------------------------------

    /**
     * Given a full class chain, returns the class info, without reading or writing from
     * any cache.
     */
    private fun load_full_chain (cano_names: List<String>): ClassLike?
    {
        val class_likes = cano_names.mapNotNull(this::load_class)
        return extract_chain_class(cano_names, class_likes)
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
     * If the info is not available from the chain cache, two cases are possible:
     *
     * - there hasn't been an attempt to load the chain yet
     * - previous attempts to load the chain were unsuccessful
     *
     * In the first case, the method schedules an attempt to load the chain information with
     * the reactor, then throws [Continue].
     *
     * In the second case, we might still be able to get the information later, when a source
     * file is added to the reactor, so we throw [Continue].
     */
    fun full_chain (chain: List<String>): ClassLike
    {
        val cached = chain_cache[chain]
        if (cached is Miss) throw Continue(cached.continuation)
        if (cached != null) return cached

        val cano_names = cano_names(chain)

        // lookup the canonical names in the cache
        val class_likes = cano_names.mapNotNull(class_cache::get)
        val klass = extract_chain_class(cano_names, class_likes)
        if (klass != null) {
            chain_cache[chain] = klass
            return klass
        }

        val reactor    = Context.reactor
        val chain_node = reactor.java_virtual_node.chains
        val required   = Attribute(chain_node, chain.joinToString("."))

        // schedule attempt to load class
        throw Continue(Reaction(chain_node) {
            _provided = listOf(required)
            _trigger  = {
                val klass1 = load_full_chain(cano_names)
                chain_cache[chain] = klass1 ?: Miss(this)
                if (klass1 != null) {
                    required += klass1
                    class_cache[klass1.canonical_name] = klass1
                }
            }
        })
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Given a class chain, returns the class info.
     *
     * TODO
     */
    fun klass_chain (scope: Scope, chain: List<String>): ClassLike
    {
        var cur_scope: Scope? = scope

        for (item in chain) {
            val next = cur_scope?.class_like(item)
            cur_scope = next
            if (next == null) break
        }

        if (cur_scope is ClassLike) // non-null
            return cur_scope

        return full_chain(chain)
    }

    // ---------------------------------------------------------------------------------------------

    fun resolve_members (cano_name: String, member: String): Collection<MemberInfo>
    {
        val klass = klass(cano_name)
        val members = klass.member(member)
        if (members.isEmpty())
            throw Fail(MemberNotFoundScopeError())
        return members
    }
}

// =================================================================================================

class PathClassLoader (urls: Array<URL>): URLClassLoader(urls)
{
    fun find_class_path (full_name: String): URL?
        = findResource(full_name.replace('.', '/') + ".class")
}

// =================================================================================================