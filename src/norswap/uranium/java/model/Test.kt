package norswap.uranium.java.model
import norswap.autumn.CaughtException
import norswap.autumn.ParseInput
import norswap.autumn.UncaughtException
import norswap.lang.Node
import norswap.lang.java8.Java8Grammar
import norswap.lang.java8.ast.File
import norswap.uranium.Propagator
import norswap.uranium.java.java_walker
import norswap.uranium.java.scopes.ScopesBuilder
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
    val ASTs = ArrayList<Node>()

    val limit = 10
    var i = 0

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
        if (node.imports.size != 0)
            ASTs.add(grammar.stack.peek().cast())
        grammar.reset()
    }

    val propagator = Propagator(ASTs)
    val builder = ScopesBuilder()

    propagator.walker = ::java_walker.cast()

    builder.register_with(propagator)
    propagator.propagate()

    ASTs.map { propagator[it, "scope"] }
        .forEach { println(it) }

    println("All done")
}