package norswap.lang.java8.test
import norswap.lang.java8.Java8Grammar
import norswap.lang.java8.install_java8_rules
import norswap.lang.java8.typing.*
import norswap.lang.java8.typing.TypeError.*
import norswap.uranium.Reactor
import norswap.uranium.test.GrammarReactorFixture
import org.testng.annotations.Test

class TypingRules: GrammarReactorFixture()
{
    // ---------------------------------------------------------------------------------------------

    override val g = Java8Grammar()

    // ---------------------------------------------------------------------------------------------

    override fun Reactor.init()
        = install_java8_rules()

    // ---------------------------------------------------------------------------------------------

    fun type_error (input: String, tag: Any)
    {
        root_error(input, "type", tag)
    }

    // ---------------------------------------------------------------------------------------------

    fun type (input: String, value: TType)
    {
        attr(input, "type", value)
    }

    // ---------------------------------------------------------------------------------------------

    val str = "\"a\""

    // ---------------------------------------------------------------------------------------------

    @Test fun literal()
    {
        top_fun { g.literal() }
        type("1",       TInt)
        type("1L",      TLong)
        type("1f",      TFloat)
        type("1.0",     TDouble)
        type("'a'",     TChar)
        type("\"a\"",   TString)
        type("true",    TBool)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun not()
    {
        top_fun { g.not() }
        type("!true",   TBool)
        type("!!false", TBool)
        type_error("!1", NotTypeError)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun complement()
    {
        top_fun { g.complement() }
        type("~1",  TInt)
        type("~1L", TLong)
        // todo short etc
        type_error("~1.0",  ComplementTypeError)
        type_error("~$str", ComplementTypeError)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun unary_arith()
    {
        top_fun { g.prefix_expr() }
        type("+1",      TInt)
        type("-1",      TInt)
        type("+1L",     TLong)
        type("-1.0",    TDouble)
        // todo short etc
        type_error("+true", UnaryArithTypeError)
        type_error("+$str", UnaryArithTypeError)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun binary_arith()
    {
        top_fun { g.add_expr() }
        type("1+1",     TInt)
        type("1L-1",    TLong)
        type("1.0*1",   TDouble)
        type("1f/1",    TFloat)
        type("1L%1",    TLong)
        type("$str+1",  TString)
        type("1+$str",  TString)
        // todo short etc
        type_error("1*true", BinaryArithTypeError)
        type_error("1-$str", BinaryArithTypeError)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun shift()
    {
        top_fun { g.shift_expr() }
        type("1>>1",    TInt)
        type("-1>>>1L", TInt)
        type("1L<<1",   TLong)
        // todo short etc
        type_error("1>>1f",   ShiftTypeError)
        type_error("true<<1", ShiftTypeError)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun ordering()
    {
        top_fun { g.order_expr() }
        type("1 > 1",       TBool)
        type("1L < 1",      TBool)
        type("1 >= 1f",     TBool)
        type("1.0 <= 1L",   TBool)
        type_error("1 > $str",  OrderingTypeError)
        type_error("true <= 1", OrderingTypeError)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun instanceof()
    {
        top_fun { g.order_expr() }
        type("\"\" instanceof java.lang.String", TBool)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun equal()
    {
        top_fun { g.eq_expr() }
        type("1==1",        TBool)
        type("1f!=1",       TBool)
        type("1L==1d",      TBool)
        type("true!=false", TBool)
        // todo cast compatibility
        type_error("1 == true", EqualNumBoolError)
        type_error("true == 1", EqualNumBoolError)
        type_error("1==$str",   EqualPrimRefError)
        type_error("$str==1",   EqualPrimRefError)
        // todo cast compatibility
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun bitwise()
    {
        top_fun { g.binary_or_expr() }
        type("true & false", TBool)
        type("false | true", TBool)
        type("true ^ true", TBool)
        type("1 & 1", TInt)
        type("1 | 1L", TLong)
        type("1L ^ 1", TLong)
        // todo short etc
        type_error("true & 1", BitwiseMixedError)
        type_error("1 | true", BitwiseMixedError)
        type_error("$str | 1", BitwiseRefError)
        type_error("1 ^ $str", BitwiseRefError)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun logical()
    {
        top_fun { g.or_expr() }
        type("true && false", TBool)
        type("false || true", TBool)
        type_error("1 && true", LogicalTypeError)
        type_error("false || 1", LogicalTypeError)
        type_error("$str && false", LogicalTypeError)
        type_error("true || $str", LogicalTypeError)
    }

    // ---------------------------------------------------------------------------------------------
}