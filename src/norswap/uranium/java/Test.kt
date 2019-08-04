package norswap.uranium.java
import norswap.autumn.CaughtException
import norswap.autumn.Grammar
import norswap.autumn.ParseInput
import norswap.autumn.UncaughtException
import norswap.lang.java8.Java8Grammar
import norswap.lang.java8.ast.File
import norswap.uranium.Reactor
import norswap.uranium.java.typing.register_java8_typing_rules
import norswap.utils.cast
import norswap.utils.glob
import norswap.utils.visit_around
import java.nio.file.Path
import java.nio.file.Paths

// -------------------------------------------------------------------------------------------------

fun corpus(): List<Path>
{
    val os = System.getProperty("os.name")

    val path =
        if (os == "Mac OS X")
            "/Users/nilaurent/Documents/bench" // v14.9
        else
            "D:/spring" // v21.8

    return glob("**/*.java", Paths.get(path))
}

// -------------------------------------------------------------------------------------------------

fun parse_file (grammar: Grammar, path: Path): File
{
    val result = grammar.parse(ParseInput(path))

    if (!result) {
        println("----------------------------------------")
        val pos = grammar.input.string(grammar.fail_pos)
        val msg = grammar.failure?.invoke() ?: "-"
        println("failure at ($pos): $msg")

        val failure = grammar.failure
        if (failure is UncaughtException)
            failure.e.printStackTrace()
        if (failure is CaughtException)
            failure.e.printStackTrace()
        println("----------------------------------------")
        System.exit(1)
    }

    val node = grammar.stack.peek() as File
    grammar.reset()
    return node
}

// -------------------------------------------------------------------------------------------------

fun main (args: Array<String>)
{
    val grammar = Java8Grammar()
    val corpus  = corpus()
    val ASTs    = ArrayList<File>()

    val limit = 18
    var i     = 0
    var time  = System.currentTimeMillis()

    for (path in corpus) {
        if (++i > limit) break
        println("$i - $path") // TODO
        val node = parse_file(grammar, path)
        ASTs.add(node)
    }

    val parse_time = (System.currentTimeMillis() - time) / 1000.0
    println("parse time: $parse_time")
    time = System.currentTimeMillis()

    val reactor = Reactor(ASTs)
    val context = Context(reactor)

    val walker: (Any, (Any) -> Unit) -> Unit = JavaWalker().cast()
    reactor.walker = { root, visitor -> root.visit_around(walker, visitor) }

    context.register_java8_scopes_builder()
    context.register_java8_typing_rules()

    val init_time = (System.currentTimeMillis() - time) / 1000.0
    println("init time: $init_time")
    time = System.currentTimeMillis()

    reactor.start()

    val propag_time = (System.currentTimeMillis() - time) / 1000.0
    println("propag time: $propag_time")

    ASTs.forEach {
        val file_name = it.input.name
        val scope = reactor[it, "scope"] as norswap.uranium.java.model.source.File?
    }
}