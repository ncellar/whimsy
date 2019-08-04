package norswap.lang.java8
import norswap.autumn.TokenGrammar
import norswap.autumn.naive.*
import norswap.autumn.naive.CharRange
import norswap.autumn.naive.Not
import norswap.autumn.model.graph_compiler.set_refs
import norswap.lang.java_base.*
import norswap.lang.java8.ast.*
import norswap.lang.java8.ast.TypeDeclKind.*

class GraphGrammar: TokenGrammar()
{
    val Parser.g: Parser get() = apply { grammar = this@GraphGrammar }

    val refs = ArrayList<ReferenceParser>()

    fun ref (name: String) = ReferenceParser(name).also { refs.add(it) }

    /// LEXICAL ====================================================================================

    // Whitespace -------------------------------------------------------------

    val line_comment = Seq(Str("//").g, Until0(CharAny().g, Str("\n").g).g).g

    val multi_comment = Seq(Str("/*").g, Until0(CharAny().g, Str("*/").g).g).g

    val whitespace = Repeat0(Choice(SpaceChar().g, line_comment, multi_comment).g).g

    // Keywords and Operators -------------------------------------------------

    val boolean = Token("boolean".token).g

    val byte = Token("byte".token).g

    val char = Token("char".token).g

    val double = Token("double".token).g

    val float = Token("float".token).g

    val int = Token("int".token).g

    val long = Token("long".token).g

    val short = Token("short".token).g

    val void = Token("void".token).g

    val abstract = Token("abstract".token).g

    val default = Token("default".token).g

    val final = Token("final".token).g

    val native = Token("native".token).g

    val private = Token("private".token).g

    val protected = Token("protected".token).g

    val public = Token("public".token).g

    val static = Token("static".token).g

    val strictfp = Token("strictfp".token).g

    val synchronized = Token("synchronized".token).g

    val transient = Token("transient".token).g

    val volatile = Token("volatile".token).g

    val `false` = Token(token ({ false }, Str("false").g)).g

    val `true` = Token(token ({ true }, Str("true").g)).g

    val `null` = Token(token ({ Null }, Str("null").g)).g

    val assert = Token("assert".keyword).g

    val `break` = Token("break".keyword).g

    val case = Token("case".keyword).g

    val catch = Token("catch".keyword).g

    val `class` = Token("class".keyword).g

    val const = Token("const".keyword).g

    val `continue` = Token("continue".keyword).g

    val `do` = Token("do".keyword).g

    val `else` = Token("else".keyword).g

    val enum = Token("enum".keyword).g

    val extends = Token("extends".keyword).g

    val finally = Token("finally".keyword).g

    val `for` = Token("for".keyword).g

    val goto = Token("goto".keyword).g

    val `if` = Token("if".keyword).g

    val implements = Token("implements".keyword).g

    val import = Token("import".keyword).g

    val `interface` = Token("interface".keyword).g

    val instanceof = Token("instanceof".keyword).g

    val new = Token("new".keyword).g

    val `package` = Token("package".keyword).g

    val `return` = Token("return".keyword).g

    val `super` = Token("super".keyword).g

    val switch = Token("switch".keyword).g

    val `this` = Token("this".keyword).g

    val throws = Token("throws".keyword).g

    val `throw` = Token("throw".keyword).g

    val `try` = Token("try".keyword).g

    val `while` = Token("while".keyword).g

    val `!` = Token("!".keyword).g

    val `%` = Token("%".keyword).g

    val `%=` = Token("%=".keyword).g

    val `&` = Token("&".keyword).g

    val `&&` = Token("&&".keyword).g

    val `&=` = Token("&=".keyword).g

    val `(` = Token("(".keyword).g

    val `)` = Token(")".keyword).g

    val `*` = Token("*".keyword).g

    val `*=` = Token("*=".keyword).g

    val `+` = Token("+".keyword).g

    val `++` = Token("++".keyword).g

    val `+=` = Token("+=".keyword).g

    val `,` = Token(",".keyword).g

    val `-` = Token("-".keyword).g

    val `--` = Token("--".keyword).g

    val `-=` = Token("-=".keyword).g

    val `=` = Token("=".keyword).g

    val `==` = Token("==".keyword).g

    val `?` = Token("?".keyword).g

    val `^` = Token("^".keyword).g

    val `^=` = Token("^=".keyword).g

    val `{` = Token("{".keyword).g

    val `|` = Token("|".keyword).g

    val `|=` = Token("|=".keyword).g

    val `!=` = Token("!=".keyword).g

    val `||` = Token("||".keyword).g

    val `}` = Token("}".keyword).g

    val `~` = Token("~".keyword).g

    val `@` = Token("@".keyword).g

    val div = Token("/".keyword).g

    val dive = Token("/=".keyword).g

    val gt = Token(">".keyword).g

    val lt = Token("<".keyword).g

    val ge = Token(">=".keyword).g

    val le = Token("<=".keyword).g

    val sl = Token("<<".keyword).g

    val sle = Token("<<=".keyword).g

    val sr = WordString(">>").g

    val sre = Token(">>=".keyword).g

    val bsr = WordString(">>>").g

    val bsre = Token(">>>=".keyword).g

    val lsbra = Token("[".keyword).g

    val rsbra = Token("]".keyword).g

    val arrow = Token("->".keyword).g

    val colon = Token(":".keyword).g

    val semi = Token(";".keyword).g

    val dot = Token(".".keyword).g

    val ellipsis = Token("...".keyword).g

    val dcolon = Token("::".keyword).g

    // Identifiers ------------------------------------------------------------

    val iden = Token(token({ it }, JavaIden().g)).g

    val `_` = Str("_").g

    val dlit = Str(".").g

    val hex_prefix = Choice(Str("0x").g, Str("0x").g).g

    val underscores = Repeat0(`_`).g

    val digits1 = Around1(Digit().g, underscores).g

    val digits0 = Around0(Digit().g, underscores).g

    val hex_digits = Around1(HexDigit().g, underscores).g

    val hex_num = Seq(hex_prefix, hex_digits).g

    // Numerals - Floating Point ----------------------------------------------

    val hex_significand = Choice(Seq(hex_prefix, Opt(hex_digits).g, dlit, hex_digits).g, Seq(hex_num, Opt(dlit).g).g).g

    val exp_sign_opt = Opt(CharSet("+-").g).g

    val exponent = Seq(CharSet("eE").g, exp_sign_opt, digits1).g

    val binary_exponent = Seq(CharSet("pP").g, exp_sign_opt, digits1).g

    val float_suffix = CharSet("fFdD").g

    val float_suffix_opt = Opt(float_suffix).g

    val hex_float_lit = Seq(hex_significand, binary_exponent, float_suffix_opt).g

    val decimal_float_lit = Choice(Seq(digits1, dlit, digits0, Opt(exponent).g, float_suffix_opt).g, Seq(dlit, digits1, Opt(exponent).g, float_suffix_opt).g, Seq(digits1, exponent, float_suffix_opt).g, Seq(digits1, Opt(exponent).g, float_suffix).g).g

    val float_literal = Token(token ({ parse_float(it) }, Choice(hex_float_lit, decimal_float_lit).g)).g

    // Numerals - Integral ----------------------------------------------------

    val bit = CharSet("01").g

    val binary_prefix = Choice(Str("0b").g, Str("0B").g).g

    val binary_num = Seq(binary_prefix, Around1(Repeat1(bit).g, underscores).g).g

    val octal_num = Seq(Str("0").g, Repeat1(Seq(underscores, OctalDigit().g).g).g).g

    val decimal_num = Choice(Str("0").g, digits1).g

    val integer_num = Choice(hex_num, binary_num, octal_num, decimal_num).g

    val integer_literal = Token(token ({ parse_int(it) }, Seq(integer_num, Opt(CharSet("lL").g).g).g)).g

    // Characters and Strings -------------------------------------------------

    val octal_escape = Choice(Seq(CharRange('0', '3').g, OctalDigit().g, OctalDigit().g).g, Seq(OctalDigit().g, Opt(OctalDigit().g).g).g).g

    val unicode_escape = Seq(Repeat1(Str("u").g).g, Repeat(4, HexDigit().g ).g).g

    val escape = Seq(Str("\\").g, Choice(CharSet("btnfr\"'\\").g, octal_escape, unicode_escape).g).g

    val naked_char = Choice(escape, Seq(Not(CharSet("'\\\n\r").g).g, CharAny().g).g).g

    val char_literal = Token(token ({ parse_char(it) }, Seq(Str("'").g, naked_char, Str("'").g).g)).g

    val naked_string_char = Choice(escape, Seq(Not(CharSet("\"\\\n\r").g).g, CharAny().g).g).g

    val string_literal = Token(token ({ parse_string(it) }, Seq(Str("\"").g, Repeat0(naked_string_char).g, Str("\"").g).g)).g

    // Literal ----------------------------------------------------------------

    val literal_syntax = TokenChoice(this, integer_literal, string_literal, `null`, float_literal, `true`, `false`, char_literal).g

    val literal = Build(
        syntax = literal_syntax,
        effect = {Literal(it(0))}).g

    /// ANNOTATIONS ================================================================================

    val annotation_element: Parser = Choice(ref("ternary"), ref("annotation_element_list"), ref("annotation")).g

    val annotation_inner_list = CommaListTerm0(ref("annotation_element")).g

    val annotation_element_list = Build(
        syntax = Curlies(annotation_inner_list).g,
        effect = {AnnotationElementList(it.list())}).g

    val annotation_element_pair = Build(
        syntax = Seq(iden, `=`, annotation_element).g,
        effect = {Pair<String, AnnotationElement>(it(0), it(1))}).g

    val normal_annotation_suffix = Build(1,
        syntax = Parens(CommaList1(annotation_element_pair).g).g,
        effect = {val elements = it.list<Pair<String, AnnotationElement>>(1).unzip()
                   NormalAnnotation(it(0), elements.first, elements.second)}).g

    val single_element_annotation_suffix = Build(1,
        syntax = Parens(annotation_element).g,
        effect = {SingleElementAnnotation(it(0), it(1))}).g

    val marker_annotation_suffix = Build(1,
        syntax = Opt(ParensEmpty().g).g,
        effect = {MarkerAnnotation(it(0))}).g

    val annotation_suffix = Choice(normal_annotation_suffix, single_element_annotation_suffix, marker_annotation_suffix).g

    val qualified_iden = Build(
        syntax = Around1(iden, dot).g,
        effect = {it.list<String>()}).g

    val annotation = Seq(`@`, qualified_iden, annotation_suffix).g

    val annotations = Build(
        syntax = Repeat0(annotation).g,
        effect = {it.list<Annotation>()}).g

    /// TYPES ======================================================================================

    val basic_type = TokenChoice(this, byte, short, int, long, char, float, double, boolean, void).g

    val primitive_type = Build(
        syntax = Seq(annotations, basic_type).g,
        effect = {PrimitiveType(it(0), it(1))}).g

    val extends_bound = Build(
        syntax = Seq(extends, ref("type")).g,
        effect = {ExtendsBound(it(0))}).g

    val super_bound = Build(
        syntax = Seq(`super`, ref("type")).g,
        effect = {SuperBound(it(0))}).g

    val type_bound = Maybe(Choice(extends_bound, super_bound).g).g

    val wildcard = Build(
        syntax = Seq(annotations, `?`, type_bound).g,
        effect = {Wildcard(it(0), it(1))}).g

    val type_args = Build(
        syntax = Opt(Angles(CommaList0(Choice(ref("type"), wildcard).g).g).g).g,
        effect = {it.list<Type>()}).g

    val class_type_part = Build(
        syntax = Seq(annotations, iden, type_args).g,
        effect = {ClassTypePart(it(0), it(1), it(2))}).g

    val class_type = Build(
        syntax = Around1(class_type_part, dot).g,
        effect = {ClassType(it.list<ClassTypePart>())}).g

    val stem_type = Choice(primitive_type, class_type).g

    val dim = Build(
        syntax = Seq(annotations, SquaresEmpty().g).g,
        effect = {Dimension(it(0))}).g

    val dims = Build(
        syntax = Repeat0(dim).g,
        effect = {it.list<Dimension>()}).g

    val dims1 = Build(
        syntax = Repeat1(dim).g,
        effect = {it.list<Dimension>()}).g

    val type_dim_suffix = Build(1,
        syntax = dims1,
        effect = {ArrayType(it(0), it(1))}).g

    val type: Parser = Seq(stem_type, Opt(type_dim_suffix).g).g

    val type_union_syntax = Around1(ref("type"), `&`).g

    val type_union = Build(
        syntax = type_union_syntax,
        effect = {it.list<Type>()}).g

    val type_bounds = Build(
        syntax = Opt(Seq(extends, type_union_syntax).g).g,
        effect = {it.list<Type>()}).g

    val type_param = Build(
        syntax = Seq(annotations, iden, type_bounds).g,
        effect = {TypeParam(it(0), it(1), it(2))}).g

    val type_params = Build(
        syntax = Opt(Angles(CommaList0(type_param).g).g).g,
        effect = {it.list<TypeParam>()}).g

    /// MODIFIERS ==================================================================================

    val keyword_modifier = Build(
        syntax = Choice(public, protected, private, abstract, static, final, synchronized, native, strictfp, default, transient, volatile).g,
        effect = {Keyword.valueOf(it(0))}).g

    val modifier = Choice(annotation, keyword_modifier).g

    val modifiers = Build(
        syntax = Repeat0(modifier).g,
        effect = {it.list<Modifier>()}).g

    /// PARAMETERS =================================================================================

    val args = Build(
        syntax = Parens(CommaList0(ref("expr")).g).g,
        effect = {it.list<Expr>()}).g

    val this_parameter_qualifier = Build(
        syntax = Repeat0(Seq(iden, dot).g).g,
        effect = {it.list<String>()}).g

    val this_param_suffix = Build(2,
        syntax = Seq(this_parameter_qualifier, `this`).g,
        effect = {ThisParameter(it(0), it(1), it(2))}).g

    val iden_param_suffix = Build(2,
        syntax = Seq(iden, dims).g,
        effect = {IdenParameter(it(0), it(1), it(2), it(3))}).g

    val variadic_param_suffix = Build(2,
        syntax = Seq(annotations, ellipsis, iden).g,
        effect = {VariadicParameter(it(0), it(1), it(2), it(3))}).g

    val formal_param_suffix = Choice(iden_param_suffix, this_param_suffix, variadic_param_suffix).g

    val formal_param = Seq(modifiers, type, formal_param_suffix).g

    val formal_params = Build(
        syntax = Parens(CommaList0(formal_param).g).g,
        effect = {FormalParameters(it.list())}).g

    val untyped_params = Build(
        syntax = Parens(CommaList1(iden).g).g,
        effect = {UntypedParameters(it.list())}).g

    val single_param = Build(
        syntax = iden,
        effect = {UntypedParameters(it.list<String>())}).g

    val lambda_params = Choice(formal_params, untyped_params, single_param).g

    /// NON-TYPE DECLARATIONS ======================================================================

    val var_init: Parser = Choice(ref("expr"), ref("array_init")).g

    val array_init = Build(
        syntax = Curlies(CommaListTerm0(var_init).g).g,
        effect = {ArrayInit(it.list())}).g

    val var_declarator_id = Build(
        syntax = Seq(iden, dims).g,
        effect = {VarDeclaratorID(it(0), it(1))}).g

    val var_declarator = Build(
        syntax = Seq(var_declarator_id, Maybe(Seq(`=`, var_init).g).g).g,
        effect = {VarDeclarator(it(0), it(1))}).g

    val var_decl_no_semi = Build(1,
        syntax = Seq(type, CommaList1(var_declarator).g).g,
        effect = {VarDecl(it(0), it(1), it.list(2))}).g

    val var_decl_suffix = Seq(var_decl_no_semi, semi).g

    val var_decl = Seq(modifiers, var_decl_suffix).g

    val throws_clause = Build(
        syntax = Opt(Seq(throws, CommaList1(type).g).g).g,
        effect = {it.list<Type>()}).g

    val block_or_semi = Choice(ref("block"), AsVal(null, semi ).g).g

    val method_decl_suffix = Build(1,
        syntax = Seq(type_params, type, iden, formal_params, dims, throws_clause, block_or_semi).g,
        effect = {MethodDecl(it(0), it(1), it(2), it(3), it(4), it(5), it(6), it(7))}).g

    val constructor_decl_suffix = Build(1,
        syntax = Seq(type_params, iden, formal_params, throws_clause, ref("block")).g,
        effect = {ConstructorDecl(it(0), it(1), it(2), it(3), it(4), it(5))}).g

    val init_block = Build(
        syntax = Seq(AsBool(static).g, ref("block")).g,
        effect = {InitBlock(it(0), it(1))}).g

    /// TYPE DECLARATIONS ==========================================================================

    // Common -----------------------------------------------------------------

    val extends_clause = Build(
        syntax = Opt(Seq(extends, CommaList0(type).g).g).g,
        effect = {it.list<Type>()}).g

    val implements_clause = Build(
        syntax = Opt(Seq(implements, CommaList0(type).g).g).g,
        effect = {it.list<Type>()}).g

    val type_sig = Seq(iden, type_params, extends_clause, implements_clause).g

    val class_modified_decl = Seq(modifiers, Choice(var_decl_suffix, method_decl_suffix, constructor_decl_suffix, ref("type_decl_suffix")).g).g

    val class_body_decl: Parser = Choice(class_modified_decl, init_block, semi).g

    val class_body_decls = Build(
        syntax = Repeat0(class_body_decl).g,
        effect = {it.list<Decl>()}).g

    val type_body = Curlies(class_body_decls).g

    // Enum -------------------------------------------------------------------

    val enum_constant = Build(
        syntax = Seq(annotations, iden, Maybe(args).g, Maybe(type_body).g).g,
        effect = {EnumConstant(it(0), it(1), it(2), it(3))}).g

    val enum_class_decls = Build(
        syntax = Opt(Seq(semi, Repeat0(class_body_decl).g).g).g,
        effect = {it.list<Decl>()}).g

    val enum_constants = Build(
        syntax = Opt(CommaList1(enum_constant).g).g,
        effect = {it.list<EnumConstant>()}).g

    val enum_body = Affect(
        syntax = Curlies(Seq(enum_constants, enum_class_decls).g).g,
        effect = { stack.push(it(1)) ; stack.push(it(0)) /* swap */ }).g

    val enum_decl = Build(1,
        syntax = Seq(enum, type_sig, enum_body).g,
        effect = {val td = TypeDecl(input, ENUM, it(0), it(1), it(2), it(3), it(4), it(5))
                   EnumDecl(td, it(6))}).g

    // Annotations ------------------------------------------------------------

    val annot_default_clause = Build(
        syntax = Seq(default, annotation_element).g,
        effect = {it(1)}).g

    val annot_elem_decl = Build(
        syntax = Seq(modifiers, type, iden, ParensEmpty().g, dims, Maybe(annot_default_clause).g, semi).g,
        effect = {AnnotationElemDecl(it(0), it(1), it(2), it(3), it(4))}).g

    val annot_body_decls = Build(
        syntax = Repeat0(Choice(annot_elem_decl, class_body_decl).g).g,
        effect = {it.list<Decl>()}).g

    val annotation_decl = Build(1,
        syntax = Seq(`@`, `interface`, type_sig, Curlies(annot_body_decls).g).g,
        effect = {TypeDecl(input, ANNOTATION, it(0), it(1), it(2), it(3), it(4), it(5))}).g

    //// ------------------------------------------------------------------------

    val class_decl = Build(1,
        syntax = Seq(`class`, type_sig, type_body).g,
        effect = {TypeDecl(input, CLASS, it(0), it(1), it(2), it(3), it(4), it(5))}).g

    val interface_declaration = Build(1,
        syntax = Seq(`interface`, type_sig, type_body).g,
        effect = {TypeDecl(input, INTERFACE, it(0), it(1), it(2), it(3), it(4), it(5))}).g

    val type_decl_suffix = Choice(class_decl, interface_declaration, enum_decl, annotation_decl).g

    val type_decl = Seq(modifiers, type_decl_suffix).g

    val type_decls = Build(
        syntax = Repeat0(Choice(type_decl, semi).g).g,
        effect = {it.list<Decl>()}).g

    /// EXPRESSIONS ================================================================================

    // Array Constructor ------------------------------------------------------

    val dim_expr = Build(
        syntax = Seq(annotations, Squares(ref("expr")).g).g,
        effect = {DimExpr(it(0), it(1))}).g

    val dim_exprs = Build(
        syntax = Repeat1(dim_expr).g,
        effect = {it.list<DimExpr>()}).g

    val dim_expr_array_creator = Build(
        syntax = Seq(stem_type, dim_exprs, dims).g,
        effect = {ArrayCtorCall(it(0), it(1), it(2), null)}).g

    val init_array_creator = Build(
        syntax = Seq(stem_type, dims1, array_init).g,
        effect = {ArrayCtorCall(it(0), emptyList(), it(1), it(2))}).g

    val array_ctor_call = Seq(new, Choice(dim_expr_array_creator, init_array_creator).g).g

    // Lambda Expression ------------------------------------------------------

    val lambda = Build(
        syntax = Seq(lambda_params, arrow, Choice(ref("block"), ref("expr")).g).g,
        effect = {Lambda(it(0), it(1))}).g

    // Expression - Primary ---------------------------------------------------

    val par_expr = Build(
        syntax = Parens(ref("expr")).g,
        effect = {ParenExpr(it(0))}).g

    val ctor_call = Build(
        syntax = Seq(new, type_args, stem_type, args, Maybe(type_body).g).g,
        effect = {CtorCall(it(0), it(1), it(2), it(3))}).g

    val new_ref_suffix = Build(2,
        syntax = new,
        effect = {NewReference(it(0), it(1))}).g

    val method_ref_suffix = Build(2,
        syntax = iden,
        effect = {MaybeBoundMethodReference(it(0), it(1), it(2))}).g

    val ref_suffix = Seq(dcolon, type_args, Choice(new_ref_suffix, method_ref_suffix).g).g

    val class_expr_suffix = Build(1,
        syntax = Seq(dot, `class`).g,
        effect = {ClassExpr(it(0))}).g

    val type_suffix_expr = Seq(type, Choice(ref_suffix, class_expr_suffix).g).g

    val iden_or_method_expr = Build(
        syntax = Seq(iden, Maybe(args).g).g,
        effect = {it[1] ?. let { MethodCall(null, listOf(), it(0), it(1)) } ?: Identifier(it(0))}).g

    val this_expr = Build(
        syntax = Seq(`this`, Maybe(args).g).g,
        effect = {it[0] ?. let { ThisCall(it(0)) } ?: This}).g

    val super_expr = Build(
        syntax = Seq(`super`, Maybe(args).g).g,
        effect = {it[0] ?. let { SuperCall(it(0)) } ?: Super}).g

    val class_expr = Build(
        syntax = Seq(type, dot, `class`).g,
        effect = {ClassExpr(it(0))}).g

    val primary_expr = Choice(par_expr, array_ctor_call, ctor_call, type_suffix_expr, iden_or_method_expr, this_expr, super_expr, literal).g

    // Expression - Postfix ---------------------------------------------------

    val dot_this = Build(1,
        syntax = `this`,
        effect = {DotThis(it(0))}).g

    val dot_super = Build(1,
        syntax = `super`,
        effect = {DotSuper(it(0))}).g

    val dot_iden = Build(1,
        syntax = iden,
        effect = {DotIden(it(0), it(1))}).g

    val dot_new = Build(1,
        syntax = ctor_call,
        effect = {DotNew(it(0), it(1))}).g

    val dot_method = Build(1,
        syntax = Seq(type_args, iden, args).g,
        effect = {MethodCall(it(0), it(1), it(2), it(3))}).g

    val dot_postfix = Choice(dot_method, dot_iden, dot_this, dot_super, dot_new).g

    val ref_postfix = Build(1,
        syntax = Seq(dcolon, type_args, iden).g,
        effect = {BoundMethodReference(it(0), it(1), it(2))}).g

    val array_postfix = Build(1,
        syntax = Squares(ref("expr")).g,
        effect = {ArrayAccess(it(0), it(1))}).g

    val inc_suffix = Build(1,
        syntax = `++`,
        effect = {PostIncrement(it(0))}).g

    val dec_suffix = Build(1,
        syntax = `--`,
        effect = {PostDecrement(it(0))}).g

    val postfix = Choice(Seq(dot, dot_postfix).g, array_postfix, inc_suffix, dec_suffix, ref_postfix).g

    val postfix_expr = Seq(primary_expr, Repeat0(postfix).g).g

    val inc_prefix = Build(
        syntax = Seq(`++`, ref("prefix_expr")).g,
        effect = {PreIncrement(it(0))}).g

    val dec_prefix = Build(
        syntax = Seq(`--`, ref("prefix_expr")).g,
        effect = {PreDecrement(it(0))}).g

    val unary_plus = Build(
        syntax = Seq(`+`, ref("prefix_expr")).g,
        effect = {UnaryPlus(it(0))}).g

    val unary_minus = Build(
        syntax = Seq(`-`, ref("prefix_expr")).g,
        effect = {UnaryMinus(it(0))}).g

    val complement = Build(
        syntax = Seq(`~`, ref("prefix_expr")).g,
        effect = {Complement(it(0))}).g

    val not = Build(
        syntax = Seq(`!`, ref("prefix_expr")).g,
        effect = {Negate(it(0))}).g

    val cast = Build(
        syntax = Seq(Parens(type_union).g, Choice(lambda, ref("prefix_expr")).g).g,
        effect = {Cast(it(0), it(1))}).g

    val prefix_expr: Parser = Choice(inc_prefix, dec_prefix, unary_plus, unary_minus, complement, not, cast, postfix_expr).g

    // Expression - Binary ----------------------------------------------------

    val mult_expr = AssocLeft(this) { 
        operands = prefix_expr
        op(`*`, { Product(it(0), it(1)) })
        op(div, { Division(it(0), it(1)) })
        op(`%`, { Remainder(it(0), it(1)) })
    }

    val add_expr = AssocLeft(this) { 
        operands = mult_expr
        op(`+`, { Sum(it(0), it(1)) })
        op(`-`, { Diff(it(0), it(1)) })
    }

    val shift_expr = AssocLeft(this) { 
        operands = add_expr
        op(sl, { ShiftLeft(it(0), it(1)) })
        op(sr, { ShiftRight(it(0), it(1)) })
        op(bsr, { BinaryShiftRight(it(0), it(1)) })
    }

    val order_expr = AssocLeft(this) { 
        operands = shift_expr
        op(lt, { Lower(it(0), it(1)) })
        op(le, { LowerEqual(it(0), it(1)) })
        op(gt, { Greater(it(0), it(1)) })
        op(ge, { GreaterEqual(it(0), it(1)) })
        postfix(Seq(instanceof, type).g, { Instanceof(it(0), it(1)) })
    }

    val eq_expr = AssocLeft(this) { 
        operands = order_expr
        op(`==`, { Equal(it(0), it(1)) })
        op(`!=`, { NotEqual(it(0), it(1)) })
    }

    val binary_and_expr = AssocLeft(this) { 
        operands = eq_expr
        op(`&`, { BinaryAnd(it(0), it(1)) })
    }

    val xor_expr = AssocLeft(this) { 
        operands = binary_and_expr
        op(`^`, { Xor(it(0), it(1)) })
    }

    val binary_or_expr = AssocLeft(this) { 
        operands = xor_expr
        op(`|`, { BinaryOr(it(0), it(1)) })
    }

    val and_expr = AssocLeft(this) { 
        operands = binary_or_expr
        op(`&&`, { And(it(0), it(1)) })
    }

    val or_expr = AssocLeft(this) { 
        operands = and_expr
        op(`||`, { Or(it(0), it(1)) })
    }

    val ternary_suffix = Build(1,
        syntax = Seq(`?`, ref("expr"), colon, ref("expr")).g,
        effect = {Ternary(it(0), it(1), it(2))}).g

    val ternary = Seq(or_expr, Opt(ternary_suffix).g).g

    val assignment_suffix = Choice(
             Build(1,Seq(`=`, ref("expr")).g, {Assign(it(0), it(1), "=")}).g, 
             Build(1,Seq(`+=`, ref("expr")).g, {Assign(it(0), it(1), "+=")}).g, 
             Build(1,Seq(`-=`, ref("expr")).g, {Assign(it(0), it(1), "-=")}).g, 
             Build(1,Seq(`*=`, ref("expr")).g, {Assign(it(0), it(1), "*=")}).g, 
             Build(1,Seq(dive, ref("expr")).g, {Assign(it(0), it(1), "/=")}).g, 
             Build(1,Seq(`%=`, ref("expr")).g, {Assign(it(0), it(1), "%=")}).g, 
             Build(1,Seq(sle, ref("expr")).g, {Assign(it(0), it(1), "<<=")}).g, 
             Build(1,Seq(sre, ref("expr")).g, {Assign(it(0), it(1), ">>=")}).g, 
             Build(1,Seq(bsre, ref("expr")).g, {Assign(it(0), it(1), ">>>=")}).g, 
             Build(1,Seq(`&=`, ref("expr")).g, {Assign(it(0), it(1), "&=")}).g, 
             Build(1,Seq(`^=`, ref("expr")).g, {Assign(it(0), it(1), "^=")}).g, 
             Build(1,Seq(`|=`, ref("expr")).g, {Assign(it(0), it(1), "|=")}).g).g

    val assignment = Seq(ternary, Opt(assignment_suffix).g).g

    val expr: Parser = Choice(lambda, assignment).g

    /// STATEMENTS =================================================================================

    val if_stmt = Build(
        syntax = Seq(`if`, par_expr, ref("stmt"), Maybe(Seq(`else`, ref("stmt")).g).g).g,
        effect = {If(it(0), it(1), it(2))}).g

    val expr_stmt_list = Build(
        syntax = CommaList0(expr).g,
        effect = {it.list<Stmt>()}).g

    val for_init_decl = Build(
        syntax = Seq(modifiers, var_decl_no_semi).g,
        effect = {it.list<Stmt>()}).g

    val for_init = Choice(for_init_decl, expr_stmt_list).g

    val basic_for_paren_part = Seq(for_init, semi, Maybe(expr).g, semi, Opt(expr_stmt_list).g).g

    val basic_for_stmt = Build(
        syntax = Seq(`for`, Parens(basic_for_paren_part).g, ref("stmt")).g,
        effect = {BasicFor(it(0), it(1), it(2), it(3))}).g

    val for_val_decl = Seq(modifiers, type, var_declarator_id, colon, expr).g

    val enhanced_for_stmt = Build(
        syntax = Seq(`for`, Parens(for_val_decl).g, ref("stmt")).g,
        effect = {EnhancedFor(it(0), it(1), it(2), it(3), it(4))}).g

    val while_stmt = Build(
        syntax = Seq(`while`, par_expr, ref("stmt")).g,
        effect = {WhileStmt(it(0), it(1))}).g

    val do_while_stmt = Build(
        syntax = Seq(`do`, ref("stmt"), `while`, par_expr, semi).g,
        effect = {DoWhileStmt(it(0), it(1))}).g

    val catch_parameter_types = Build(
        syntax = Around0(type, `|`).g,
        effect = {it.list<Type>()}).g

    val catch_parameter = Seq(modifiers, catch_parameter_types, var_declarator_id).g

    val catch_clause = Build(
        syntax = Seq(catch, Parens(catch_parameter).g, ref("block")).g,
        effect = {CatchClause(it(0), it(1), it(2), it(3))}).g

    val catch_clauses = Build(
        syntax = Repeat0(catch_clause).g,
        effect = {it.list<CatchClause>()}).g

    val finally_clause = Seq(finally, ref("block")).g

    val resource = Build(
        syntax = Seq(modifiers, type, var_declarator_id, `=`, expr).g,
        effect = {TryResource(it(0), it(1), it(2), it(3))}).g

    val resources = Build(
        syntax = Opt(Parens(Around1(resource, semi).g).g).g,
        effect = {it.list<TryResource>()}).g

    val try_stmt = Build(
        syntax = Seq(`try`, resources, ref("block"), catch_clauses, Maybe(finally_clause).g).g,
        effect = {TryStmt(it(0), it(1), it(2), it(3))}).g

    val default_label = Build(
        syntax = Seq(default, colon).g,
        effect = {DefaultLabel}).g

    val case_label = Build(
        syntax = Seq(case, expr, colon).g,
        effect = {CaseLabel(it(0))}).g

    val switch_label = Choice(case_label, default_label).g

    val switch_clause = Build(
        syntax = Seq(switch_label, ref("stmts")).g,
        effect = {SwitchClause(it(0), it(1))}).g

    val switch_stmt = Build(
        syntax = Seq(switch, par_expr, Curlies(Repeat0(switch_clause).g).g).g,
        effect = {SwitchStmt(it(0), it.list(1))}).g

    val synchronized_stmt = Build(
        syntax = Seq(synchronized, par_expr, ref("block")).g,
        effect = {SynchronizedStmt(it(1), it(2))}).g

    val return_stmt = Build(
        syntax = Seq(`return`, Maybe(expr).g, semi).g,
        effect = {ReturnStmt(it(0))}).g

    val throw_stmt = Build(
        syntax = Seq(`throw`, expr, semi).g,
        effect = {ThrowStmt(it(0))}).g

    val break_stmt = Build(
        syntax = Seq(`break`, Maybe(iden).g, semi).g,
        effect = {BreakStmt(it(0))}).g

    val continue_stmt = Build(
        syntax = Seq(`continue`, Maybe(iden).g, semi).g,
        effect = {ContinueStmt(it(0))}).g

    val assert_stmt = Build(
        syntax = Seq(assert, expr, Maybe(Seq(colon, expr).g).g, semi).g,
        effect = {AssertStmt(it(0), it(1))}).g

    val semi_stmt = Build(
        syntax = semi,
        effect = {SemiStmt}).g

    val expr_stmt = Seq(expr, semi).g

    val labelled_stmt = Build(
        syntax = Seq(iden, colon, ref("stmt")).g,
        effect = {LabelledStmt(it(0), it(1))}).g

    val stmt: Parser = Choice(ref("block"), if_stmt, basic_for_stmt, enhanced_for_stmt, while_stmt, do_while_stmt, try_stmt, switch_stmt, synchronized_stmt, return_stmt, throw_stmt, break_stmt, continue_stmt, assert_stmt, semi_stmt, expr_stmt, labelled_stmt, var_decl, type_decl).g

    val block = Build(
        syntax = Curlies(Repeat0(stmt).g).g,
        effect = {Block(it.list())}).g

    val stmts = Build(
        syntax = Repeat0(stmt).g,
        effect = {it.list<Stmt>()}).g

    /// TOP-LEVEL ==================================================================================

    val package_decl = Build(
        syntax = Seq(annotations, `package`, qualified_iden, semi).g,
        effect = {Package(it(0), it(1))}).g

    val import_decl = Build(
        syntax = Seq(import, AsBool(static).g, qualified_iden, AsBool(Seq(dot, `*`).g).g, semi).g,
        effect = {Import(it(0), it(1), it(2))}).g

    val import_decls = Build(
        syntax = Repeat0(import_decl).g,
        effect = {it.list<Import>()}).g

    val root = Build(
        syntax = Seq(ref("whitespace"), Maybe(package_decl).g, import_decls, type_decls).g,
        effect = {File(input, it(0), it(1), it(2))}).g

    override fun whitespace() = whitespace.invoke()

    override fun root() = root.invoke()

    init { set_refs(this, refs) }


}

fun main (args: Array<String>) { GraphGrammar() }
