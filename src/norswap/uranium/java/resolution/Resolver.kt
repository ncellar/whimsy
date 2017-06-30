package norswap.uranium.java.resolution
import norswap.uranium.java.model.BytecodeClass
import norswap.uranium.java.model.Klass
import norswap.uranium.java.model.ReflectionClass
import norswap.utils.attempt
import norswap.utils.rangeTo
import java.net.URL
import java.net.URLClassLoader
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode


object Resolver
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

    /**
     * Returns the path to the class designated by the given *binary* name using the given class
     * loader.
     */
    private fun find_class_path (loader: URLClassLoader, name: String): URL?
        = loader.findResource(name.replace('.', '/') + ".class")

    // ---------------------------------------------------------------------------------------------

    /**
     * Load a class from the given URL, yielding a [BytecodeClass]. Returns null if
     * the class fails to load AND registers an error if the class fails to load.
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
            // The class was found, but we cannot load it.
            // TODO register error
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
     * Try to load a class from .class files or reflectively, using its *binary* name.
     */
    fun load_class (name: String): Klass?
    {
        // TODO update class cache
        return load_from_classfile(name) ?: load_reflectively(name)
    }

    // ---------------------------------------------------------------------------------------------
}