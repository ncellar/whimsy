package norswap.uranium.java
import norswap.uranium.UraniumError
import norswap.uranium.java.model.bytecode.BytecodeClass
import norswap.uranium.java.model.Klass
import norswap.uranium.java.model.reflect.ReflectionClass
import norswap.utils.attempt
import norswap.utils.rangeTo
import java.net.URL
import java.net.URLClassLoader
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode

class Resolver (val context: Context)
{
    // ---------------------------------------------------------------------------------------------

    private val class_cache = HashMap<String, Klass>()

    // ---------------------------------------------------------------------------------------------

    private val syscl = ClassLoader.getSystemClassLoader() as URLClassLoader

    // ---------------------------------------------------------------------------------------------

    private val urls: Array<URL> = syscl.urLs

    // ---------------------------------------------------------------------------------------------

    private val loader = URLClassLoader(urls)

    // ---------------------------------------------------------------------------------------------

    fun add_source_class(klass: Klass)
    {
        class_cache[klass.binary_name] = klass
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
     * Load a class from the given URL, yielding a [BytecodeClass]. Returns null and registers
     * an error if the class fails to load.
     */
    private fun load_from_url (class_url: URL): Klass?
    {
        try {
            val node = ClassNode()
            val reader = ClassReader(class_url.toString())
            reader.accept(node, 0)
            return BytecodeClass(node)
        }
        catch (e: Exception) {
            context.report(UraniumError("Could not load: $class_url"))
            return null
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Load a class with the given binary name from the class path. Returns null if either there
     * is no class with the given name, or the class is found but fails to load (in which case an
     * error is also registered).
     */
    private fun load_from_classfile (name: String): Klass?
    {
        return find_class_path(loader, name)
            ?. let { load_from_url(it) }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Some core classes have no associated .class files, search for those through reflection.
     * e.g. `java.lang.Object`
     *
     * However it seems that sometimes these classes have associated class files in `rt.jar`.
     */
    private fun load_reflectively (cano_name: String): Klass?
    {
        return (cano_name.startsWith("java.") || cano_name.startsWith("javax.")) .. {
            attempt { syscl.loadClass(cano_name) } ?. let(::ReflectionClass)
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a class' information given its binary name. If the information hasn't been loaded
     * yet, it will be loaded from a .class file, or reflectively.
     *
     * Returns null if either there is no class with the given name, or the class is found but fails
     * to load (in which case an error is also registered).
     */
    fun load_class (name: String): Klass?
    {
        val cached = class_cache[name]
        if (cached != null) return cached

        val loaded = load_from_classfile(name) ?: load_reflectively(name)
        if (loaded != null)
            class_cache[name] = loaded

        return loaded
    }

    // ---------------------------------------------------------------------------------------------
}