@file:Suppress("PackageDirectoryMismatch")
package norswap.autumn.model.graph_compiler
import norswap.autumn.Grammar
import norswap.autumn.model.*
import norswap.autumn.model.compiler.builders
import norswap.autumn.naive.Parser
import norswap.autumn.naive.ReferenceParser
import norswap.lang.java8.Java8Model
import norswap.lang.java_base.escape
import norswap.utils.cast
import norswap.utils.plusAssign
import norswap.utils.poly.Poly1
import norswap.utils.snake_to_camel
import java.io.PrintWriter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

// -------------------------------------------------------------------------------------------------

fun main (args: Array<String>)
{
    val str = compile_model("GraphGrammar", Java8Model())
    val writer = PrintWriter("src/norswap/lang/java8/GraphGrammar.kt")
    writer.println(str)
    writer.flush()
}

// -------------------------------------------------------------------------------------------------
// Global Context (global variables for now)

/**
 * Whether the parser being compiled is a top-level parser.
 */
var top_level = true

// -------------------------------------------------------------------------------------------------

fun set_refs (grammar: Grammar, refs: List<ReferenceParser>)
{
    val klass = grammar::class
    val props = klass.declaredMemberProperties

    for (ref in refs) {
        val prop = props.find { it.name == ref.name }
            .cast<KProperty1<Grammar, Parser>?>()
            ?: throw Error("cannot resolve reference: " + ref.name)

        ref.parser = prop.get(grammar)
    }
}

// -------------------------------------------------------------------------------------------------

fun compile_model (klass_name: String, model: Any): String
{
    order_next = 0
    val b = StringBuilder()

    b += "package norswap.lang.java8\n"
    b += "import norswap.autumn.TokenGrammar\n"
    b += "import norswap.autumn.naive.*\n"
    b += "import norswap.autumn.naive.CharRange\n"
    b += "import norswap.autumn.naive.Not\n"
    b += "import norswap.autumn.model.graph_compiler.set_refs\n"
    b += "import norswap.lang.java_base.*\n"
    b += "import norswap.lang.java8.ast.*\n"
    b += "import norswap.lang.java8.ast.TypeDeclKind.*\n\n"

    b += "class $klass_name: TokenGrammar()\n{"
    b +=   "\n    val Parser.g: Parser get() = apply { grammar = this@$klass_name }"
    b += "\n\n    val refs = ArrayList<ReferenceParser>()"
    b += "\n\n    fun ref (name: String) = ReferenceParser(name).also { refs.add(it) }"

    builders(model).forEach {
        b += "\n\n"
        b += compiler_top_level(it)
    }

    b += "\n\n    override fun whitespace() = whitespace.invoke()"
    b += "\n\n    override fun root() = root.invoke()"
    b += "\n\n    init { set_refs(this, refs) }\n\n"
    b += "\n}"

    b += "\n\nfun main (args: Array<String>) { $klass_name() }"
    return b.toString()
}

// -------------------------------------------------------------------------------------------------

val compiler_top_level = Poly1<Builder, String>().apply {

    default { "" }

    on <SectionBuilder> {
        when (it.level) {
            1 -> "    /// ${it.name!!.capitalize()} " + "=".repeat(91 - it.name!!.length)
            2 ->  "    // ${it.name!!.capitalize()} " + "-".repeat(71 - it.name!!.length)
            else -> ""
        }   }

    on <SeparatorBuilder> {
        when (it.level) {
            1 ->  "    /// " + "=".repeat(92)
            2 -> "    //// " + "-".repeat(72)
            else -> ""
        }   }

    on <CodeBuilder> {
        it.code.prependIndent("    ")
    }

    on <ParserBuilder> {
        top_level = true
        val str = model_compiler(it)
        val b = StringBuilder()
        b += "    val ${it.name}"
        if (it.attributes.contains(TypeHint))
            b += ": Parser"
        b += " = $str"
        b.toString()
    }
}

// -------------------------------------------------------------------------------------------------

fun Poly1<ParserBuilder, String>.digest (p: ParserBuilder): String
{
    top_level = false
    return p.name ?: invoke(p)
}

// -------------------------------------------------------------------------------------------------

inline fun <reified Case: ParserBuilder>
    Poly1<ParserBuilder, String>
    .parser (noinline value: (Case) -> String)
{
    on <Case> {
        value(it) + ".g"
    }
}

// -------------------------------------------------------------------------------------------------

inline fun <reified T: WrapperBuilder>
    Poly1<ParserBuilder, String>
    .parser (prefix: String)
{
    parser <T> { "$prefix(${digest(it.child)})" }
}

// -------------------------------------------------------------------------------------------------

val model_compiler = Poly1 <ParserBuilder, String>().apply {

    on <ReferenceBuilder> {
        "ref(\"${it.str}\")"
    }

    on <ParserCodeBuilder> {
        it.code
    }

    parser <StrBuilder> {
        "Str(\"${it.str.escape()}\")"
    }

    parser <WordBuilder> {
        "WordString(\"${it.str.escape()}\")"
    }

    parser <StrTokenBuilder> {
        "Token(\"${it.str.escape()}\".token)"
    }

    parser <TokenBuilder> {
        "Token(token ({ ${it.value} }, ${digest(it.child)}))"
    }

    parser <KeywordBuilder> {
        "Token(\"${it.str.escape()}\".keyword)"
    }

    parser <TokenChoiceBuilder> {
        // Implicit assumption: the tokens have been assigned to variables.
        val children = it.list.map { it.name }.joinToString()
        "TokenChoice(this, $children)"
    }

    parser <PlainTokenBuilder> {
        "Token(token({ it }, ${digest(it.child)}))"
    }

    parser <CharRangeBuilder> {
        "CharRange('${it.start}', '${it.end}')"
    }

    parser <CharSetBuilder> {
        "CharSet(\"${it.str.escape()}\")"
    }

    parser <SeqBuilder> {
        val children = it.list.map { digest(it) }.joinToString()
        "Seq($children)"
    }

    parser <ChoiceBuilder> {
        val children = it.list
            .map { digest(it) }
            .joinToString()
            .replace("\n", "\n             ")
        "Choice($children)"
    }

    parser <LongestBuilder> {
        val children = it.list.map { digest(it) }.joinToString()
        "Longest($children)"
    }

    parser <AheadBuilder>   ("Ahead")
    parser <NotBuilder>     ("Not")
    parser <OptBuilder>     ("Opt")
    parser <MaybeBuilder>   ("Maybe")
    parser <AsBoolBuilder>  ("AsBool")
    parser <Repeat0Builder> ("Repeat0")
    parser <Repeat1Builder> ("Repeat1")
    parser <AnglesBuilder>  ("Angles")
    parser <CurliesBuilder> ("Curlies")
    parser <SquaresBuilder> ("Squares")
    parser <ParensBuilder>  ("Parens")

    parser <EmptyAnglesBuilder>  { "AnglesEmpty()" }
    parser <EmptyCurliesBuilder> { "CurliesEmpty()" }
    parser <EmptySquaresBuilder> { "SquaresEmpty()" }
    parser <EmptyParensBuilder>  { "ParensEmpty()" }

    parser <CommaList0Builder>      ("CommaList0")
    parser <CommaList1Builder>      ("CommaList1")
    parser <CommaListTerm0Builder>  ("CommaListTerm0")
    parser <CommaListTerm1Builder>  ("CommaListTerm1")

    parser <AsValBuilder> {
        "AsVal(${it.value}, ${digest(it.child)} )"
    }

    parser <RepeatNBuilder> {
        "Repeat(${it.n}, ${digest(it.child)} )"
    }

    parser <Around0Builder> {
        "Around0(${digest(it.around)}, ${digest(it.inside)})"
    }

    parser <Around1Builder> {
        "Around1(${digest(it.around)}, ${digest(it.inside)})"
    }

    parser <Until0Builder> {
        "Until0(${digest(it.repeat)}, ${digest(it.terminator)})"
    }

    parser <Until1Builder> {
        "Until1(${digest(it.repeat)}, ${digest(it.terminator)})"
    }

    parser <BuildBuilder> {
        val backlog = if (it.backlog == 0) "" else "${it.backlog},"
        if (top_level)
            "Build($backlog\n" +
                "        syntax = ${digest(it.child)},\n" +
                "        effect = {${it.effect.replace("\n", "\n" + " ".repeat(19))}})"
        else
            "\nBuild($backlog${digest(it.child)}, {${it.effect}})"
    }

    parser <AffectBuilder> {
        val backlog = if (it.backlog == 0) "" else "${it.backlog},"
        "Affect($backlog\n" +
            "        syntax = ${digest(it.child)},\n" +
            "        effect = { ${it.effect} })"
    }

    parser <BuildStrBuilder> {
        "BuildStr(\n" +
            "        syntax = ${digest(it.child)},\n" +
            "        value = {${it.effect}})"
    }

    parser <ParameterlessBuilder> {
        it.parser_name.snake_to_camel() + "()"
    }

    on <AssocLeftBuilder> {
        val b = StringBuilder()

        if (it is AssocLeftBuilder)
            b += "AssocLeft(this) { \n"

        if (it.operands != null && (it.left != null || it.right != null))
            throw Error("Cannot set both operands and left/right.")

        if (it.strict != null)
            b += "        strict = ${it.strict!!}\n"
        if (it.operands != null)
            b += "        operands = ${digest(it.operands!!)}\n"
        if (it.left != null)
            b += "        left = ${digest(it.left!!)}\n"
        if (it.right != null)
            b += "        right = ${digest(it.right!!)}\n"

        it.operators.forEach {
            b += "        ${it.kind}(${digest(it.parser)}, { ${it.effect} })\n"
        }

        b += "    }"
        b.toString()
    }
}

// -------------------------------------------------------------------------------------------------

