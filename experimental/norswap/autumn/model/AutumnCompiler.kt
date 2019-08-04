@file:Suppress("PackageDirectoryMismatch")
package norswap.autumn.model.autumn_compiler
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
    val str = compile_model("JavaGrammar", Java8Model())
    val writer = PrintWriter("src/norswap/lang/java8/JavaGrammar.javam")
    writer.println(str)
    writer.flush()
}

// -------------------------------------------------------------------------------------------------
// Global Context (global variables for now)

/**
 * Whether the parser being compiled is a top-level parser.
 */
var top_level = true

val token_list = arrayListOf<String>()

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

    b += "package norswap.lang.java;\n"
    b += "import norswap.autumn.Parse;\n"
    b += "import norswap.autumn.Parser;\n"

    b += "\npublic final class $klass_name extends DSL\n{\n"

    b += "    public static void main (String[] args) {\n"
    b += "        new Parse(\"\");"
    b += "\n    }"

    builders(model).forEach {
        b += "\n\n"
        b += compiler_top_level(it)
    }

    b += "\n}"
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
        b += "    Parser ${it.name}"
        b += " = $str.get();"
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
    // TODO noop remove
    on <Case> {
        value(it)
    }
}

// -------------------------------------------------------------------------------------------------

inline fun <reified T: WrapperBuilder>
        Poly1<ParserBuilder, String>
        .parser (suffix: String)
{
    parser <T> { "${digest(it.child)}.$suffix" }
}

//inline fun <reified T: WrapperBuilder>
//        Poly1<ParserBuilder, String>
//        .parser (prefix: String)
//{
//    parser <T> { "${digest(it.child)}.$prefix()" }
//}

// -------------------------------------------------------------------------------------------------

val model_compiler = Poly1 <ParserBuilder, String>().apply {

    on <ReferenceBuilder> {
        "lazy(\"${it.str}\", () -> this.${it.str})"
    }

    on <ParserCodeBuilder> {
        it.code
    }

    parser <StrBuilder> {
        "str(\"${it.str.escape()}\")"
    }

    parser <WordBuilder> {
        "word(\"${it.str.escape()}\")"
    }

    parser <StrTokenBuilder> {
        // TODO needs to add RULE name to token list
        "word(\"${it.str.escape()}\")"
    }

    parser <TokenBuilder> {
        // TODO needs to add RULE name to token list
        "${digest(it.child)}.reduce((p,xs) -> ${it.value})"
    }

    parser <KeywordBuilder> {
        // TODO needs to add RULE name to token list
        "word(\"${it.str.escape()}\")"
    }

    parser <TokenChoiceBuilder> {
        // TODO maybe incomplete
        // Implicit assumption: the tokens have been assigned to variables.
        val children = it.list.map { it.name }.joinToString()
        "tokens.token_choice($children)"
    }

    parser <PlainTokenBuilder> {
        // TODO needs to add RULE name to token list
        digest(it.child)
    }

    parser <CharRangeBuilder> {
        "range('${it.start}', '${it.end}')"
    }

    parser <CharSetBuilder> {
        "set(\"${it.str.escape()}\")"
    }

    parser <SeqBuilder> {
        val children = it.list.map { digest(it) }.joinToString()
        "seq($children)"
    }

    parser <ChoiceBuilder> {
        val children = it.list
                .map { digest(it) }
                .joinToString()
                .replace("\n", "\n             ")
        "choice($children)"
    }

    parser <LongestBuilder> {
        val children = it.list.map { digest(it) }.joinToString()
        "longest($children)"
    }

    parser <AheadBuilder>   ("ahead()")
    parser <NotBuilder>     ("not()")
    parser <OptBuilder>     ("opt()")
    parser <MaybeBuilder>   ("maybe()")
    parser <AsBoolBuilder>  ("as_bool()")
    parser <Repeat0Builder> ("at_least(0)")
    parser <Repeat1Builder> ("at_least(1)")
    parser <AnglesBuilder>  ("bracketed(\"<>\")") // TODO inadequate - wrap
    parser <CurliesBuilder> ("bracketed(\"{}\")")
    parser <SquaresBuilder> ("bracketed(\"[]\")")
    parser <ParensBuilder>  ("bracketed(\"()\")")

    // TODO needs name translation for those
    parser <EmptyAnglesBuilder>  { "seq(LT, GT)" }
    parser <EmptyCurliesBuilder> { "seq(LBRACE, RBRACE)" }
    parser <EmptySquaresBuilder> { "seq(LBRACKET, RBRACKET)" }
    parser <EmptyParensBuilder>  { "seq(LPAREN, RPAREN)" }

    parser <CommaList0Builder>      ("sep(0, COMMA)")
    parser <CommaList1Builder>      ("sep(1, COMMA)")
    parser <CommaListTerm0Builder>  ("sep_trailing(0, COMMA)")
    parser <CommaListTerm1Builder>  ("sep_trailing(1, COMMA)")

    parser <AsValBuilder> {
        "${digest(it.child)}.as_val(${it.value})"
    }

    parser <RepeatNBuilder> {
        "${digest(it.child)}.repeat(${it.n})"
    }

    parser <Around0Builder> {
        "${digest(it.around)}.sep(0, ${digest(it.inside)})"
    }

    parser <Around1Builder> {
        "${digest(it.around)}.sep(1, ${digest(it.inside)})"
    }

    parser <Until0Builder> {
        val term = digest(it.terminator)
        "seq(seq(${digest(it.repeat)}, $term.not()).at_least(0), $term)"
    }

    parser <Until1Builder> {
        val term = digest(it.terminator)
        "seq(seq(${digest(it.repeat)}, $term.not()).at_least(1), $term)"
    }

    parser <BuildBuilder> {
        val backlog = if (it.backlog == 0) "" else "${it.backlog},"
        if (top_level)
            "${digest(it.child)}.reduce((p,xs) -> {${it.effect.replace("\n", "\n" + " ".repeat(19))}}"
        else
            // TODO pop the backlog
            "${digest(it.child)}.reduce((p,xs) -> {${it.effect}}"
    }

    parser <AffectBuilder> {
        val backlog = if (it.backlog == 0) "" else "${it.backlog},"
        // TODO backlog
        "${digest(it.child)}.collect((p,xs) -> ${it.effect})"
    }

    parser <BuildStrBuilder> {
        "${digest(it.child)}.reduce_str((p,str) -> {${it.effect}})"
    }

    parser <ParameterlessBuilder> {
        if (it.parser_name == "char_any") "any()"
        else it.parser_name
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

