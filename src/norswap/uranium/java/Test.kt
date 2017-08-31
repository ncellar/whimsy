package norswap.uranium.java
import norswap.autumn.CaughtException
import norswap.autumn.ParseInput
import norswap.autumn.UncaughtException
import norswap.lang.java8.Java8Grammar
import norswap.lang.java8.ast.File
import norswap.uranium.Reactor
import norswap.uranium.java.typing.register_java8_typing_rules
import norswap.utils.cast
import norswap.utils.glob
import java.nio.file.Paths

fun main (args: Array<String>)
{
    val grammar = Java8Grammar()
    val os = System.getProperty("os.name")

    val corpus =
        if (os == "Mac OS X")
            "/Users/nilaurent/Documents/bench" // v14.9
        else
            "D:/spring" // v21.8

    val paths = glob("**/*.java", Paths.get(corpus))
    val ASTs = ArrayList<File>()

    val limit = 40
    var i = 0
    var time = System.currentTimeMillis()

    paths.forEach b@ {
        if (++i > limit) return@b

        val result = grammar.parse(ParseInput(it))

        if (!result) {
            println("----------------------------------------")
            println("failure at (${grammar.input.string(grammar.fail_pos)}): " + grammar.failure?.invoke())
            val failure = grammar.failure
            if (failure is UncaughtException)
                failure.e.printStackTrace()
            if (failure is CaughtException)
                failure.e.printStackTrace()
            println("----------------------------------------")
            System.exit(1)
        }

        val node = grammar.stack.peek() as File
        if (node.pkg == null && node.imports.size == 0) -- i
        else ASTs.add(node)
        grammar.reset()
    }

    println("parse time: " + (System.currentTimeMillis() - time) / 1000.0)
    time = System.currentTimeMillis()

    val propagator = Reactor(ASTs)
    val context = Context(propagator)

    propagator.walker       = JavaWalker().cast()

    context.register_java8_scopes_builder()
    context.register_java8_typing_rules()

    println("init time: " + (System.currentTimeMillis() - time) / 1000.0)
    time = System.currentTimeMillis()

    propagator.start()

    println("propag time: " + (System.currentTimeMillis() - time) / 1000.0)

    ASTs.forEach {
        val file_name = it.input.name
        val scope = propagator[it, "scope"] as norswap.uranium.java.model.source.File
        // println("\n$file_name\n${propagator[it, "scope"]}")
        for (klass in scope.classes.values) {
            println(klass.binary_name)
            for (inner in klass.classes.values) {
                println("  " + inner)
            }
        }
    }
}