@file:Suppress("PackageDirectoryMismatch")
package norswap.lang.java8.ast
import norswap.autumn.ParseInput
import norswap.lang.*

// Annotations -------------------------------------------------------------------------------------

interface AnnotationElement: Node

interface Annotation: AnnotationElement

data class AnnotationElementList(
    val elems: List<AnnotationElement>)
: CNode(), AnnotationElement

data class NormalAnnotation (
    val name: List<String>,
    val elem_names: List<String>,
    val elems: List<AnnotationElement>)
: CNode(), Annotation

data class MarkerAnnotation (
    val name: List<String>)
: CNode(), Annotation

data class SingleElementAnnotation (
    val name: List<String>,
    val elem: AnnotationElement)
: CNode(), Annotation

// Expressions -------------------------------------------------------------------------------------

interface Expr: AnnotationElement, Stmt

object Null
    : CNode(), Expr
{
    override fun toString() = "null"
}

data class Literal (
    val value: Any)
    : CNode(), Expr
{
    override fun toString() = "<$value>"
}

// Types -------------------------------------------------------------------------------------------

abstract class TypeBound: CNode()
{
    abstract val type: Type
}

data class SuperBound   (override val type: Type): TypeBound()
{
    override fun toString() = "super $type"
}

data class ExtendsBound (override val type: Type): TypeBound()
{
    override fun toString() = "extends $type"
}

interface Type: Node

data class PrimitiveType (
    val ann: List<Annotation>,
    val name: String)
    : CNode(), Type
{
    override fun toString() = name
}

object Void
    : CNode(), Type
{
    override fun toString() = "void"
}

interface RefType: Type

data class Wildcard (
    val ann: List<Annotation>,
    val bound: TypeBound?)
    : CNode(), RefType
{
    override fun toString() = "?"
}

data class ClassType (
    val parts: List<ClassTypePart>)
    : CNode(), RefType
{
    override fun toString() = parts.joinToString(separator=".")
}

data class ClassTypePart (
    val ann: List<Annotation>,
    val name: String,
    val targs: List<Type>)
    : CNode()
{
    override fun toString() = name
}

data class ArrayType (
    val stem: Type,
    val dims: List<Dimension>)
    : CNode(), RefType
{
    override fun toString() = "$stem[]"
}

data class Dimension (
    val ann: List<Annotation>)
    : CNode()

data class TypeParam (
    val ann: List<Annotation>,
    val name: String,
    val bounds: List<Type>)
    : CNode()
{
    override fun toString() = name
}

// Expressions -------------------------------------------------------------------------------------

object Super
    : CNode(), Expr
{
    override fun toString() = "super"
}

object This
    : CNode(), Expr
{
    override fun toString() = "this"
}

data class SuperCall (
    val args: List<Expr>)
    : CNode(), Expr

data class ThisCall (
    val args: List<Expr>)
    : CNode(), Expr

data class Identifier (
    val name: String)
    : CNode(), Expr

data class ClassExpr (
    val type: Type)
    : CNode(), Expr

data class DimExpr (
    val ann: List<Annotation>,
    val expr: Expr)
    : CNode()

data class ArrayInit (
    val items: List<Expr>)
    : CNode(), Expr

data class ArrayCtorCall (
    val type: Type,
    val dim_exprs: List<DimExpr>,
    val dims: List<Dimension>,
    val init: Expr?)
    : CNode(), Expr

data class CtorCall (
    val targs: List<Type>,
    val type: Type,
    val args: List<Expr>,
    val body: List<Decl>?)
    : CNode(), Expr

data class ParenExpr (
    val expr: Expr)
    : CNode(), Expr

abstract class UnaryExpression: CNode(), Expr
{
    abstract val operand: Expr
}

data class MethodCall (
    val op: Expr?,
    val targs: List<Type>,
    val name: String,
    val args: List<Expr>)
    : CNode(), Expr

data class DotIden (
    override val operand: Expr,
    val name: String)
    : UnaryExpression()

data class DotThis (
    override val operand: Expr)
    : UnaryExpression()

data class DotSuper (
    override val operand: Expr)
    :  UnaryExpression()

data class DotNew (
    override val operand: Expr,
    val ctor: CtorCall)
    : UnaryExpression()

data class ArrayAccess (
    override val operand: Expr,
    val index: Expr)
    : UnaryExpression()

data class PostIncrement (override val operand: Expr): UnaryExpression()
data class PostDecrement (override val operand: Expr): UnaryExpression()
data class PreIncrement  (override val operand: Expr): UnaryExpression()
data class PreDecrement  (override val operand: Expr): UnaryExpression()

data class MaybeBoundMethodReference (
    val type: Type,
    val targs: List<Type>,
    val name: String)
    : CNode(), Expr

data class BoundMethodReference(
    val receiver: Expr,
    val targs: List<Type>,
    val name: String)
    : CNode(), Expr

data class NewReference (
    val type: Type,
    val targs: List<Type>)
    : CNode(), Expr

data class Cast (
    val types: List<Type>,
    override val operand: Expr)
    : UnaryExpression()

abstract class UnaryOp: UnaryExpression()

data class UnaryPlus    (override val operand: Expr): UnaryOp()
data class UnaryMinus   (override val operand: Expr): UnaryOp()
data class Complement   (override val operand: Expr): UnaryOp()
data class Negate       (override val operand: Expr): UnaryOp()

abstract class BinaryOp: CNode(), Expr
{
    abstract val left: Expr
    abstract val right: Expr
}

data class Product          (override val left: Expr, override val right: Expr): BinaryOp()
data class Division         (override val left: Expr, override val right: Expr): BinaryOp()
data class Remainder        (override val left: Expr, override val right: Expr): BinaryOp()
data class Sum              (override val left: Expr, override val right: Expr): BinaryOp()
data class Diff             (override val left: Expr, override val right: Expr): BinaryOp()
data class ShiftLeft        (override val left: Expr, override val right: Expr): BinaryOp()
data class ShiftRight       (override val left: Expr, override val right: Expr): BinaryOp()
data class BinaryShiftRight (override val left: Expr, override val right: Expr): BinaryOp()
data class Lower            (override val left: Expr, override val right: Expr): BinaryOp()
data class LowerEqual       (override val left: Expr, override val right: Expr): BinaryOp()
data class Greater          (override val left: Expr, override val right: Expr): BinaryOp()
data class GreaterEqual     (override val left: Expr, override val right: Expr): BinaryOp()
data class Equal            (override val left: Expr, override val right: Expr): BinaryOp()
data class NotEqual         (override val left: Expr, override val right: Expr): BinaryOp()
data class BinaryAnd        (override val left: Expr, override val right: Expr): BinaryOp()
data class Xor              (override val left: Expr, override val right: Expr): BinaryOp()
data class BinaryOr         (override val left: Expr, override val right: Expr): BinaryOp()
data class And              (override val left: Expr, override val right: Expr): BinaryOp()
data class Or               (override val left: Expr, override val right: Expr): BinaryOp()

data class Assign (
    override val left: Expr,
    override val right: Expr,
    val op: String)
    : BinaryOp()

data class Instanceof(
    val op: Expr,
    val type: Type)
    : CNode(), Expr
{
    override fun toString() = "instanceof"
}

data class Ternary (
    val cond: Expr,
    val ifPart: Expr,
    val elsePart: Expr)
    : CNode(), Expr

// NOTE: body can be an expression
data class Lambda (
    val params: Parameters,
    val body: Stmt)
    : CNode(), Expr

// -------------------------------------------------------------------------------------------------
// -------------------------------------------------------------------------------------------------
// -------------------------------------------------------------------------------------------------

// Declarations  -----------------------------------------------------------------------------------

interface Modifier

enum class Keyword: Modifier {
    abstract,
    default,
    final,
    native,
    private,
    protected,
    public,
    static,
    synchronized,
    strictfp,
    transient,
    volatile
}

interface FormalParameter: Node
{
    val name: String
}

data class IdenParameter (
    val mods: List<Modifier>,
    val type: Type,
    override val name: String,
    val dims: List<Dimension>)
    : CNode(), FormalParameter

data class ThisParameter (
    val mods: List<Modifier>,
    val type: Type,
    val qualifier: List<String>)
    : CNode(), FormalParameter
{
    override val name = "this"
}

data class VariadicParameter (
    val mods: List<Modifier>,
    val type: Type,
    val arrayMods: List<Annotation>,
    override val name: String)
    : CNode(), FormalParameter

interface Parameters: Node

data class FormalParameters (
    val params: List<FormalParameter>)
    : CNode(), Parameters

data class UntypedParameters (
    val params: List<String>)
    : CNode(), Parameters

////

data class Package (
        val ann: List<Annotation>,
        val name: List<String>): CNode()

data class Import (
        val static: Boolean,
        val name: List<String>,
        val wildcard: Boolean): CNode()

////

interface Decl: Stmt

data class EnumConstant (
    val ann: List<Annotation>,
    val name: String,
    val params: List<Expr>?,
    val body: List<Decl>?)
    : CNode()

enum class TypeDeclKind { ANNOTATION, CLASS, ENUM, INTERFACE }

data class TypeDecl (
    val input: ParseInput,
    val kind: TypeDeclKind,
    val mods: List<Modifier>,
    val name: String,
    val tparams: List<TypeParam>,
    val extends: List<Type>,
    val implements: List<Type>,
    val decls: List<Decl>)
    : CNode(), Decl

data class EnumDecl (
    val decl: TypeDecl,
    val constants: List<EnumConstant>)
    : CNode(), Decl

data class AnnotationElemDecl (
    val mods: List<Modifier>,
    val type: Type,
    val name: String,
    val dims: List<Dimension>,
    val value: AnnotationElement?)
    : CNode(), Decl

/*

AST concerns

- easy to build (?)

- syntactic fidelity
    - i.e. desugarings?

- DRY
    - class and interfaces almost identical in structure
        - common ancestor?      -> verbose
        - separate?             -> verbose, no common interface
        - enum discrimination?  -> unsafe accesses

- error handling
    - e.g. have an extends field for interfaces
        - can't possibly be right
        - but leads to better error reporting during validation
    - on the parsing side: vs additional parsing work

- extensibility
    - attributes (e.g. typing)
    - new computations (with caching in attributes?)

trade-offs

- validation vs unsafe accesses
    - two interfaces?   -> verbose

- uniformity (apply desugarings) vs syntactic fidelity
    - use (cached?) syntactic predicates

- validation vs nice interface
    - e.g. multiple vs single extends vs no extends, implements vs not
    - use an "implements" for interface extends and class implements?

--> language + code generation?

2.

- may need multiple levels of grammar
    - speed for correct files
    - details for incorrect ones

- I made arrayInit an expr, but not one that can be acquired via expr

 */

// Statements --------------------------------------------------------------------------------------

interface Stmt: Node

data class Block (
    val stmts: List<Stmt>)
    : CNode(), Stmt

data class If (
    val cond: Expr,
    val ifPart: Stmt,
    val elsePart: Stmt?)
    : CNode(), Stmt

data class BasicFor (
    val init: List<Stmt>,
    val cond: Expr?,
    val iter: List<Stmt>,
    val body: Stmt)
    : CNode(), Stmt

data class EnhancedFor (
    val mods: List<Modifier>,
    val type: Type,
    val id: VarDeclaratorID,
    val iter: Expr,
    val body: Stmt)
    : CNode(), Stmt

data class WhileStmt (
    val cond: Expr,
    val body: Stmt)
    : CNode(), Stmt

data class DoWhileStmt (
    val body: Stmt,
    val cond: Expr)
    : CNode(), Stmt

data class SynchronizedStmt (
    val expr: Expr,
    val body: Block)
    : CNode(), Stmt

data class ReturnStmt (val expr: Expr?) : CNode(), Stmt
data class ThrowStmt  (val expr: Expr)  : CNode(), Stmt

data class BreakStmt    (val label: String?): CNode(), Stmt
data class ContinueStmt (val label: String?): CNode(), Stmt

data class AssertStmt (
    val expr: Expr,
    val msg: Expr?)
    : CNode(), Stmt

data class LabelledStmt (
    val label: String,
    val stmt: Stmt)
    : CNode(), Stmt

data class CatchClause (
    val mods: List<Modifier>,
    val types: List<Type>,
    val id: VarDeclaratorID,
    val body: Block)
    : CNode(), Stmt

data class TryResource (
    val mods: List<Modifier>,
    val type: Type,
    val id: VarDeclaratorID,
    val value: Expr)
    : CNode(), Stmt

data class TryStmt (
    val resources: List<TryResource>,
    val body: Block,
    val catch: List<CatchClause>,
    val finally: Block?)
    : CNode(), Stmt

interface SwitchLabel: Node

data class CaseLabel (
    val expr: Expr)
    : CNode(), SwitchLabel

object DefaultLabel
    : CNode(), SwitchLabel

data class SwitchClause (
        val label: SwitchLabel,
        val stmts: List<Stmt>)

data class SwitchStmt (
    val expr: Expr,
    val clauses: List<SwitchClause>)
    : CNode(), Stmt

data class VarDeclaratorID (
    val iden: String,
    val dims: List<Dimension>)
    : CNode(), Stmt

data class VarDeclarator (
    val id: VarDeclaratorID,
    val init: Expr?)
    : CNode(), Stmt

data class VarDecl (
    val mods: List<Modifier>,
    val type: Type,
    val declarators: List<VarDeclarator>)
    : CNode(), Decl

object SemiStmt: CNode(), Stmt

abstract class CodeDecl: CNode(), Decl
{
    abstract val mods: List<Modifier>
    abstract val tparams: List<TypeParam>
    abstract val name: String
    abstract val params: FormalParameters
    abstract val throwing: List<Type>
    abstract val body: Block?
}

data class MethodDecl (
    override val mods: List<Modifier>,
    override val tparams: List<TypeParam>,
             val retType: Type,
    override val name: String,
    override val params: FormalParameters,
             val dims: List<Dimension>,
    override val throwing: List<Type>,
    override val body: Block?)
    : CodeDecl(), Decl

data class InitBlock (
    val static: Boolean,
    val block: Block)
    : CNode(), Decl

data class ConstructorDecl (
    override val mods: List<Modifier>,
    override val tparams: List<TypeParam>,
    override val name: String,
    override val params: FormalParameters,
    override val throwing: List<Type>,
    override val body: Block)
    : CodeDecl(), Decl

// Root --------------------------------------------------------------------------------------------

data class File (
    val input: ParseInput,
    val pkg: Package?,
    val imports: List<Import>,
    val typeDecls: List<Decl>)
    : CNode()