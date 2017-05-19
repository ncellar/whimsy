package norswap.lang.java8

/**
 * Configuration of the Java 8 compiler.
 */
class Java8Config constructor()
{
    constructor (init: Java8Config.() -> Unit): this() {
        init()
    }

    /**
     * When both a source and binary file exist for a class, perfer the source file.
     */
    var prefer_source = true
        set (x) {
            field = x
            if (prefer_newer == x) prefer_newer = !x
        }

    /**
     * When both a source and binary file exist for a class, perfer the newer file.
     */
    var prefer_newer = false
        set (x) {
            field = x
            if (prefer_source == x) prefer_source = !x
        }
}