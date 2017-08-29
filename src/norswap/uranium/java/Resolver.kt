package norswap.uranium.java
import norswap.uranium.java.model.bytecode.BytecodeClass
import norswap.uranium.java.model.Klass
import norswap.uranium.java.model.reflect.ReflectionClass
import norswap.utils.attempt
import norswap.utils.rangeTo
import java.net.URL
import java.net.URLClassLoader
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.nio.file.Paths
import norswap.uranium.java.Resolver.Result.*

class Resolver (val ctx: Context)
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The result of a class lookup.
     */
    sealed class Result
    {
        /**
         * The class couldn't be located.
         */
        object Miss: Result()

        /**
         * The class could be located at the given URL, but couldn't be loaded.
         */
        class  Fail (val url: URL): Result()

        /**
         * The klass was successfully loaded.
         */
        class  Success (val klass: Klass): Result()
    }

    // ---------------------------------------------------------------------------------------------

    /** binary class name -> Result */
    private val class_cache = HashMap<String, Result>()

    // ---------------------------------------------------------------------------------------------

    private val syscl = ClassLoader.getSystemClassLoader() as URLClassLoader

    // ---------------------------------------------------------------------------------------------

    // TODO temporary
    private val urls: Array<URL> = syscl.urLs + Paths.get("target/classes").toAbsolutePath().toUri().toURL()

    // ---------------------------------------------------------------------------------------------

    private val loader = URLClassLoader(urls)

    // ---------------------------------------------------------------------------------------------

    fun add_source_class (klass: Klass)
    {
        class_cache[klass.binary_name] = Success(klass)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the path to the class designated by the given binary name using the given class
     * loader, or null if no such path exists.
     */
    private fun find_class_path (loader: URLClassLoader, name: String): URL?
        = loader.findResource(name.replace('.', '/') + ".class")

    // ---------------------------------------------------------------------------------------------

    /**
     * Try loading a class from the given URL, yielding a [Success] wrapping a [BytecodeClass]
     * if successful.
     */
    private fun load_from_url (class_url: URL): Result
    {
        try {
            val node = ClassNode()
            val reader = ClassReader(class_url.openStream())
            reader.accept(node, 0)
            return Result.Success(BytecodeClass(node))
        }
        catch (e: Exception) {
            return Result.Fail(class_url)
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Try load a class with the given binary name from the class path, yielding a [Success]
     * wrapping a [BytecodeClass] if successful.
     */
    private fun load_from_classfile (name: String): Result
    {
        val path = find_class_path(loader, name) ?: return Result.Miss
        return load_from_url(path)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Try loading a class with the given binary name reflectively, yielding a [Success] wrapping
     * a [ReflectionClass] if successful.
     *
     * We need this because some core classes (e.g. `java.lang.Object`) may have no associated
     * .class files. However it seems that sometimes these classes have associated class files in
     * `rt.jar`. Not sure when that is / isn't the case.
     */
    private fun load_reflectively (name: String): Result
    {
        return (name.startsWith("java.") || name.startsWith("javax.")) .. {
            attempt { syscl.loadClass(name) } ?. let { Success(ReflectionClass(it)) }
        } ?: Miss
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a class' information given its binary name. If the information hasn't been loaded
     * yet, it will be loaded from a .class file, or reflectively.
     *
     * Returns Miss if there is no class with the given name, or null if the class is found but
     * fails to load. No errors are reported in any case.
     */
    fun load_class_errorless (name: String): Result
    {
        class_cache[name]?.let { return it }
        var result = load_from_classfile(name)

        result = if (result is Miss)
            load_reflectively(name)
        else
            result

        class_cache[name] = result
        return result
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a class' information given its binary name. If the information hasn't been loaded
     * yet, it will be loaded from a .class file, or reflectively.
     *
     * Returns null if either there is no class with the given name, or the class is found but fails
     * to load. In both cases, an error is reported.
     */
    fun load_class (name: String): Klass?
    {
        val result = load_class_errorless(name)

        when (result) {
            is Miss     -> ctx.report("Could not locate class $name")
            is Fail     -> ctx.report("Could not load class $name from ${result.url}")
            is Success  -> return result.klass
        }

        return null
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a class' information given its binary name. If the information hasn't been loaded
     * yet, it will be loaded from a .class file, or reflectively.
     *
     * Throws an error if the class can't be loaded for a reason or another.
     */
    fun load_class_strict (name: String): Klass
    {
        val result = load_class_errorless(name)

        when (result) {
            is Miss     -> throw Error("Could not locate class $name")
            is Fail     -> throw Error("Could not load class $name from ${result.url}")
            is Success  -> return result.klass
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Similar to `load_class(supertype_name)` but with error messages that mention
     * the base class.
     */
    private fun load_supertype (supertype_name: String, class_name: String): Klass?
    {
        val result = load_class_errorless(supertype_name)

        when (result) {
            is Miss     -> ctx.report("Couldn't locate supertype $supertype_name of $class_name")
            is Fail     -> ctx.report("Couldn't load supertype $supertype_name of $class_name from ${result.url}")
            is Success  -> return result.klass
        }

        return null
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Similar to `load_supertype(superclass_name, class_name)` but reports an error if the
     * loaded class is an interface. Returns the class of java.lang.Object if the class can't be
     * loaded (if an interface is loaded, it is returned!).
     */
    fun load_superclass (superclass_name: String, class_name: String): Klass
    {
        val klass = load_supertype(superclass_name, class_name)
            ?: return ctx.ObjectClass

        if (klass.is_interface)
            ctx.report("Superclass $superclass_name of $class_name is an interface.")

        return klass
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Similar to `load_supertype(superinterface_name, class_name)` but reports an error if the
     * loaded class isn't an interface. Returns null if the interface can't be
     * loaded (if a non-interface class is loaded, it is returned!).
     */
    fun load_superinterface (superinterface_name: String, class_name: String): Klass?
    {
        val klass = load_supertype(superinterface_name, class_name)
            ?: return null

        if (!klass.is_interface)
            ctx.report("Superclass $superinterface_name of $class_name isn't an interface.")

        return klass
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Similar to `load_class(outer_class_name)` but with error messages that mention
     * the inner class.
     */
    fun load_outer_class (outer_class_name: String, class_name: String): Klass?
    {
        val result = load_class_errorless(outer_class_name)

        when (result) {
            is Miss     -> ctx.report("Couldn't locate outer class $outer_class_name of $class_name")
            is Fail     -> ctx.report("Couldn't load outer class $outer_class_name of $class_name from ${result.url}")
            is Success  -> return result.klass
        }

        return null
    }

    // ---------------------------------------------------------------------------------------------
}