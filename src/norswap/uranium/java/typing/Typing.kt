package norswap.uranium.java.typing
import norswap.lang.Node
import norswap.lang.java8.ast.BinaryOp
import norswap.lang.java8.ast.BinaryShiftRight
import norswap.lang.java8.ast.Complement
import norswap.lang.java8.ast.Diff
import norswap.lang.java8.ast.Division
import norswap.lang.java8.ast.Greater
import norswap.lang.java8.ast.GreaterEqual
import norswap.lang.java8.ast.Instanceof
import norswap.lang.java8.ast.Literal
import norswap.lang.java8.ast.Lower
import norswap.lang.java8.ast.LowerEqual
import norswap.lang.java8.ast.Negate
import norswap.lang.java8.ast.Product
import norswap.lang.java8.ast.Remainder
import norswap.lang.java8.ast.ShiftLeft
import norswap.lang.java8.ast.ShiftRight
import norswap.lang.java8.ast.Sum
import norswap.lang.java8.ast.UnaryMinus
import norswap.lang.java8.ast.UnaryOp
import norswap.lang.java8.ast.UnaryPlus
import norswap.uranium.Attribute
import norswap.uranium.Reactor
import norswap.uranium.Reaction
import norswap.uranium.java.Context
import norswap.uranium.java.types.BooleanType
import norswap.uranium.java.types.CharType
import norswap.uranium.java.types.DoubleType
import norswap.uranium.java.types.FloatType
import norswap.uranium.java.types.IntType
import norswap.uranium.java.types.IntegralType
import norswap.uranium.java.types.LongType
import norswap.uranium.java.types.NumericType
import norswap.uranium.java.types.RefType
import norswap.uranium.java.types.Type

// -------------------------------------------------------------------------------------------------

fun Context.register_java8_typing_rules()
{
    reactor.register_java8_typing_rules(this)
}

// -------------------------------------------------------------------------------------------------

private fun Reactor.register_java8_typing_rules (ctx: Context)
{
    add_visitor <Literal>           (ctx::type_literal)
    add_visitor <Negate>            (ctx::type_negation)
    add_visitor <Negate>            (ctx::check_negation)
    add_visitor <Complement>        (ctx::type_complement)
    add_visitor <UnaryPlus>         (ctx::type_unary_arith)
    add_visitor <UnaryMinus>        (ctx::type_unary_arith)
    add_visitor <Product>           (ctx::type_binary_arith)
    add_visitor <Division>          (ctx::type_binary_arith)
    add_visitor <Remainder>         (ctx::type_binary_arith)
    add_visitor <Sum>               (ctx::type_binary_arith)
    add_visitor <Diff>              (ctx::type_binary_arith)
    add_visitor <ShiftLeft>         (ctx::type_shift)
    add_visitor <ShiftRight>        (ctx::type_shift)
    add_visitor <BinaryShiftRight>  (ctx::type_shift)
    add_visitor <Greater>           (ctx::type_ordering)
    add_visitor <GreaterEqual>      (ctx::type_ordering)
    add_visitor <Lower>             (ctx::type_ordering)
    add_visitor <LowerEqual>        (ctx::type_ordering)
    add_visitor <Greater>           (ctx::check_ordering)
    add_visitor <GreaterEqual>      (ctx::check_ordering)
    add_visitor <Lower>             (ctx::check_ordering)
    add_visitor <LowerEqual>        (ctx::check_ordering)
    add_visitor <Instanceof>        (ctx::type_instanceof)
    add_visitor <Instanceof>        (ctx::check_instanceof)
}

// -------------------------------------------------------------------------------------------------

private fun Context.typing
    (node: Node, _name: String, vararg _consumed: Attribute, _apply: (Reaction) -> Unit)
{
    val reaction = Reaction {
        name = _name
        consumed = _consumed.toList()
        supplied = listOf(Attribute(node, "type"))
        apply = _apply
    }

    reactor.enqueue(reaction)
}

// -------------------------------------------------------------------------------------------------

private fun Context.checking (_name: String, vararg _consumed: Attribute, _apply: (Reaction) -> Unit)
{
    val reaction = Reaction {
        name = _name
        consumed = _consumed.toList()
        apply = _apply
    }

    reactor.enqueue(reaction)
}

// -------------------------------------------------------------------------------------------------

fun Context.type_literal (node: Literal, start: Boolean)
{
    if (!start) return
    typing (node, "Literal")
    {
        node.type = when (node.value) {
            is String   -> StringType
            is Int      -> IntType
            is Long     -> LongType
            is Float    -> FloatType
            is Double   -> DoubleType
            is Char     -> CharType
            is Boolean  -> BooleanType
            else        -> throw Error("unknown literal type")
        }
    }
}

// -------------------------------------------------------------------------------------------------

fun Context.type_negation (node: Negate, start: Boolean)
{
    if (!start) return
    typing (node, "Type Negation")
    {
        node.type = BooleanType
    }
}

// -------------------------------------------------------------------------------------------------

fun Context.check_negation (node: Negate, start: Boolean)
{
    if (!start) return
    checking ("Check Negation", node.operand.."type")
    {
        if (node.operand.type.unboxed !== BooleanType)
            report("Applying ! on a non-boolean type.")
    }
}

// -------------------------------------------------------------------------------------------------

fun Context.type_complement (node: Complement, start: Boolean)
{
    if (!start) return
    typing (node, "Complement", node.operand.."type")
    {
        val op_type = node.operand.type.unboxed

        if (op_type === IntType)
            node.type = unary_promotion(op_type)
        else
            report("Applying '~' on a non-integral type.")
    }
}

// -------------------------------------------------------------------------------------------------

fun Context.type_unary_arith (node: UnaryOp, start: Boolean)
{
    if (!start) return
    typing (node, "Unary Arithmetic", node.operand.."type")
    {
        val op_type = node.operand.type.unboxed

        if (op_type is NumericType)
            node.type = unary_promotion(op_type)
        else
            report("Applying an unary arithmetic operation on a non-numeric type.")
    }
}

// -------------------------------------------------------------------------------------------------

fun Context.type_binary_arith (node: BinaryOp, start: Boolean)
{
    if (!start) return
    typing (node, "Binary Arithmetic", node.left.."type", node.right.."type")
    {
        val lt = node.left .type.unboxed
        val rt = node.right.type.unboxed

        if (node is Sum && (lt === StringType || rt === StringType))
            node.type = StringType

        else if (lt is NumericType && rt is NumericType)
            node.type = binary_promotion(lt, rt)

        else
            report("Using a non-numeric value in an arithmetic expression.")
    }
}

// -------------------------------------------------------------------------------------------------

fun Context.type_shift (node: BinaryOp, start: Boolean)
{
    if (!start) return
    typing (node, "Shift", node.left.."type", node.right.."type")
    {
        val lt = node.left .type.unboxed
        val rt = node.right.type.unboxed

        if (lt is IntegralType && rt is IntegralType)
            node.type = unary_promotion(lt)
        else
            report("Using a non-integral value in a shift expression.")
    }
}

// -------------------------------------------------------------------------------------------------

fun Context.type_ordering (node: BinaryOp, start: Boolean)
{
    if (!start) return
    typing (node, "Type Ordering")
    {
        node.type = BooleanType
    }
}

// -------------------------------------------------------------------------------------------------

fun Context.check_ordering (node: BinaryOp, start: Boolean)
{
    if (!start) return
    checking ("Check Ordering", node.left.."type", node.right.."type")
    {
        val lt = node.left .type.unboxed
        val rt = node.right.type.unboxed

        if (lt !is NumericType || rt !is NumericType)
            report("Using a non-numeric value in a relational expression.")
    }
}

// -------------------------------------------------------------------------------------------------

fun Context.type_instanceof (node: Instanceof, start: Boolean)
{
    if (!start) return
    typing (node, "Type instanceof")
    {
        node.type = BooleanType
    }
}

// -------------------------------------------------------------------------------------------------

fun Context.check_instanceof (node: Instanceof, start: Boolean)
{
    if (!start) return
    checking ("Check instanceof", node.operand.."type", node.dest.."link")
    {
        val source_type = node.operand.type
        val target_type: Type = node.dest["link"]

        if (source_type !is RefType)
            report("Operand of instanceof operator does not have a reference type.")
        else if (target_type !is RefType)
            report("Type operand of instanceof operator is not a reference type.")
        else if (!target_type.reifiable)
            report("Type operand of instanceof operator is not a reifiable type.")
        else if (!cast_compatible(source_type, target_type))
            report("Instanceof expression with incompatible operand and type.")
    }
}

// -------------------------------------------------------------------------------------------------