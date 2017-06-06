package norswap.lang.java8.resolution
import norswap.lang.java8.JavaVirtualNode
import norswap.lang.java8.Java8Config
import norswap.lang.java8.classes
import norswap.lang.java8.java_virtual_node
import norswap.lang.java8.scopes.BytecodeClassScope
import norswap.lang.java8.scopes.Scope
import norswap.lang.java8.scopes.ClassScope
import norswap.lang.java8.typing.MemberInfo
import norswap.uranium.Attribute
import norswap.uranium.Reaction
import norswap.uranium.Context
import norswap.utils.attempt
import norswap.utils.rangeTo
import org.apache.bcel.classfile.ClassParser
import java.net.URL
import java.net.URLClassLoader

/**
 * ## Lexicon
 *
 * Simple class name: the unqualified class name (single identifier).
 *
 * Full class name: the fully qualified class name (with outer classes and package), separated with
 * dots.
 *
 * Canonical class name: the fully qualified class name (with outer classes and package). Each
 * class is separated from its inner class by a dollar, while package components are separated by
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
     * Never escapes the resolver.
     */
    private object Miss: ClassScope()
    {
        override val super_type           = null
        override val timestamp            = 0L
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
    private val class_cache = HashMap<String, ClassScope>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps full class chains to their class information.
     * This is important because the canonical name of a class chain is ambiguous.
     */
    private val chain_cache = HashMap<List<String>, ClassScope>()

    // ---------------------------------------------------------------------------------------------

    private val syscl = ClassLoader.getSystemClassLoader() as URLClassLoader

    // ---------------------------------------------------------------------------------------------

    private val urls: Array<URL> = syscl.urLs

    // ---------------------------------------------------------------------------------------------

    private val loader = URLClassLoader(urls)

    // ---------------------------------------------------------------------------------------------

    /**
     * When a new class is loaded, no matter the source, this method should be called
     * to register it with the resolver and perform the appropriate conflict checks.
     */
    fun register_class (klass: ClassScope): Boolean
    {
        var success = true
        val cano_name = klass.canonical_name
        val old = class_cache.putIfAbsent(cano_name, klass)

        if (old == null) Context.classes[cano_name] = klass
        else success = handle_conflict(old, klass)

        if (success) register_class_chain(klass)
        return success
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Called by [register_class] when the same class is defined by two different sources.
     *
     * Returns true iff the new class was registered as a result of the resolution.
     *
     * In case of unhandled conflict, always keep the old class.
     */
    private fun handle_conflict (old: ClassScope, new: ClassScope): Boolean
    {
        val cano_name = new.canonical_name
        val reactor   = Context.reactor
        val conf      = reactor.attachment as Java8Config

        when (old) {
            is SourceClassLike ->
                when (new) {
                    is SourceClassLike ->
                        reactor.register_error(ConflictingSourceClasses(cano_name))

                    is BytecodeClassScope ->
                        if (conf.prefer_newer && new.timestamp > old.timestamp) {
                            class_cache.put(cano_name, new)
                            reactor.java_virtual_node.classes[cano_name] = new
                            return true
                        }

                    is ReflectionClassLike ->
                        reactor.register_error(ReflectiveClassConflict(cano_name))
                }

            is BytecodeClassScope ->
                when (new) {
                    is SourceClassLike ->
                        if (conf.prefer_source || new.timestamp > old.timestamp) {
                            class_cache.put(cano_name, new)
                            reactor.java_virtual_node.classes[cano_name] = new
                            return true
                        }

                    is BytecodeClassScope ->
                        reactor.register_error(ConflictingBytecodeClasses(cano_name))

                    is ReflectionClassLike ->
                        reactor.register_error(ReflectiveClassConflict(cano_name))
                }

            is ReflectionClassLike ->
                reactor.register_error(ReflectiveClassConflict(cano_name))
        }

        return false
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Called by [register_class] to handle class chain ambiguities:
     * we ensure that each full class chain is associated to the most specific class
     * it can designated (i.e. either the only one that matches the chain, or the one with
     * the shortest outer class path). We also report an error in case of ambiguity.
     *
     * Returns true iff the new class was registered (there was no unsolvable conflict).
     */
    private fun register_class_chain (klass: ClassScope)
    {
        val chain = klass.chain
        val old = chain_cache[chain] ?: return

        val reactor    = Context.reactor
        val chain_name = chain.joinToString(".")

        val i1 = klass.canonical_name.indexOf('$')
        val i2 = old  .canonical_name.indexOf('$')

        if (i1 > 0 && i1 < i2) {
            // The new class is more prioritary than the old class chain resolution.
            val chain_node = reactor.java_virtual_node.chains
            chain_node[chain_name] = klass
            chain_cache[chain] = klass
        }

        // While these kinds of ambiguities do not seem to be explicitly forbidden by the JLS
        // or by javac, they are extremely bad practice, so we register an error.
        if (!old.ambiguous_chain) {
            reactor.register_error(AmbiguousClassDefinitions(chain_name))
            klass.ambiguous_chain = true
            old  .ambiguous_chain = true
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the path to the class designated by the given canonical name using the given class
     * loader.
     */
    fun find_class_path (loader: URLClassLoader, cano_name: String): URL?
        = loader.findResource(cano_name.replace('.', '/') + ".class")

    // ---------------------------------------------------------------------------------------------

    /**
     * Load a class from the given URL, yielding a [ClassScope]. Returns null if
     * the class fails to load AND registers an error if the class fails to load.
     */
    private fun load_from_url (class_url: URL): ClassScope?
    {
        try {
            val cparser = ClassParser(class_url.openStream(), class_url.toString())
            val bclass = cparser.parse()
            return BytecodeClassScope(bclass)
        } catch (e: Exception) {
            // The class was found, but we cannot load it.
            Context.reactor.register_error(CannotLoadClassFile(class_url))
            return null
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Load a class with the given canonical name from the class path. Returns null if either there
     * is no class with the given name, or the class is found but fails to load (in which case an
     * error is also registered).
     */
    private fun load_from_classfile (cano_name: String): ClassScope?
    {
        return find_class_path(loader, cano_name)
            ?. let { load_from_url(it) }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Some core classes have no associated .class files, search for those through reflection.
     * e.g. `java.lang.Object`
     *
     * However it seems that sometimes these classes have associated class files in `rt.jar`.
     */
    private fun load_reflectively (cano_name: String): ClassScope?
    {
        return (cano_name.startsWith("java.") || cano_name.startsWith("javax.")) .. {
            attempt { syscl.loadClass(cano_name) } ?. let(::ReflectionClassLike)
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Try to load a class from .class files or reflectively, using its canonical name, without
     * reading or writing the class cache.
     */
    private fun load_class (cano_name: String): ClassScope?
    {
        return load_from_classfile(cano_name) ?: load_reflectively(cano_name)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Registers a reaction that will attempt loading the class with the given canonical name,
     * and register inside [scope] with the given [name].
     */
    private fun register_load_class_reaction(cano_name: String, name: String, scope: Scope.Node)
    {
        Reaction (scope) {
            _optional = true
            _provided = listOf(Attribute(scope, name))
            _trigger  = {
                val klass = load_class(cano_name)
                if (klass != null)
                    register_class(klass) .. { scope[name] = klass }
                else
                    class_cache[cano_name] = Miss
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Attempts to load a class given its canonical name.
     *
     * Input: the canonical name of a class, as well as a scope node and the name under which
     * the class should be registered within that scope.
     *
     * If we attempted loading the class earlier, the result (successful or not (null)) is returned.
     *
     * Otherwise we create a reaction tasked with loading the class, caching the result and
     * setting the scope appropriately; then we return null.
     */
    fun klass (cano_name: String, name: String, scope: Scope.Node): ClassScope?
    {
        val cached = class_cache[cano_name]
        if (cached == Miss) return null

        if (cached != null) {
            scope[name] = cached
            return cached
        }

        register_load_class_reaction(cano_name, name, scope)
        return null
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as  the 3-arg [klass], but automatically derives the simple name from the canonical name.
     */
    fun klass (cano_name: String, scope: Scope.Node): ClassScope?
    {
        return klass(cano_name, cano_to_simple_name(cano_name), scope)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as the 3-arg [klass], using [JavaVirtualNode.classes] as node and automatically
     * deriving the simple name from the canonical name.
     */
    fun klass (cano_name: String): ClassScope?
    {
        val reactor    = Context.reactor
        val klass_node = reactor.java_virtual_node.classes

        return klass(cano_name, cano_name, klass_node)
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
    fun eagerly (cano_name: String): ClassScope
    {
        val cached = class_cache[cano_name]
        if (cached != null && cached != Miss) return cached

        val klass = load_class(cano_name) ?: throw Error("could not load class: $cano_name")
        register_class(klass)
        return klass
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
     * TODO
     */
    fun full_chain (chain: List<String>): ClassScope?
    {
        // TODO to be inspect in the context of klass_chain

        val cached = chain_cache[chain]
        if (cached == Miss) return null
        if (cached != null) return cached

        val cano_names = cano_names(chain)
        val reactor    = Context.reactor
        val class_node = reactor.java_virtual_node.classes
        val chain_name = cano_names[0] // no static classes
        val chain_node = reactor.java_virtual_node.chains
        val chain_attr = Attribute(chain_node, chain_name)

        cano_names.forEach {
            class_cache[it] ?: register_load_class_reaction(it, it, class_node)
        }

        // Redo the current reaction when the chain becomes available,
        // or when its value changes (for a more specific one).
        Reaction (chain_node) {
            _optional = true
            _consumed = listOf(chain_attr)
            _pushed = Context.reaction
        }

        return null
    }

    // ---------------------------------------------------------------------------------------------


    // Java Chain Resolution
    //
    // Java always attempts to locate the outer class first.
    // This means that if there exists a package whose name is the same as the outer class,
    // the classes of the package are disregarded.
    //
    // Example:
    //
    // a/
    // |- Main.java  (references a.B)
    // |- B.java
    // |- a.java
    //
    // Here, the reference to `a.B` will fail, because Main resolve `a` as a reference to the
    // `a` class, which has no `B` nested class, even though `a.B` is the fully qualified name
    // of a class!

    /**
     * TODO
     */
    fun klass_chain (scope: Scope, chain: List<String>): ClassScope?
    {
        var cur_scope: Scope? = scope
        var last = 0

        // RETRY
        // - almost everything can change
        // - given scope: must be handled at another level
        //      - the one that calls klass_chain
        //      - will klass_chain ever be called with a class scope? probably only static scope
        // - new classes
        //      - must modify package scope, which must modify given scope
        //      - these lookup dependencies must be registered! TODO
        // - new inner classes
        //      - can't happen without actually changing the class
        //      - but if we wish to generalize, we need to record these dependencies

        // ERROR
        // - control whether the retry logic is option or not depending on whether
        //   there already is a resolution

        // TODO
        // - klass has no error handling or retry (and is currently unused)

        // TODO
        // - alternative idea: cache chains and install dependencies over partial chains

        for (item in chain) {
            cur_scope = cur_scope!!.class_like(item)
            if (cur_scope == null) break
            ++last
        }

        if (last == chain.size)
            return cur_scope as ClassScope
        if (last == 0)
            return full_chain(chain)
        else
            return null
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * TODO
     */
    fun resolve_members (cano_name: String, member: String): Collection<MemberInfo>
    {
        val klass = klass(cano_name)
        val members = klass?.member(member) ?: emptyList() // TODO hackfix
        if (members.isEmpty())
            throw Error() //Fail(MemberNotFoundScopeError())
        return members
    }

    // ---------------------------------------------------------------------------------------------
}