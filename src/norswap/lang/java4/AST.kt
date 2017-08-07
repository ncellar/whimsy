@file:Suppress("PackageDirectoryMismatch")
package norswap.lang.java4.ast
import norswap.lang.*
import kotlin.sequences.sequenceOf as seq

// Expressions -------------------------------------------------------------------------------------

interface Expr: Stmt

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

interface Type

data class PrimitiveType (
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

interface RefType
    : Type

data class ClassType (
    val parts: List<String>)
    : CNode(), RefType
{
    override fun toString() = parts.joinToString(separator=".")
}

data class ArrayType (
    val stem: Type,
    val dims: Int)
    : CNode(), RefType
{
    override fun toString() = "$stem[]"
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

data class ArrayInit (
    val items: List<Expr>)
    : CNode(), Expr

data class ArrayCtorCall (
    val type: Type,
    val dim_exprs: List<Expr>,
    val dims: Int,
    val init: Expr?)
    : CNode(), Expr

data class CtorCall (
    val type: Type,
    val args: List<Expr>,
    val body: List<Decl>?)
    : CNode(), Expr

data class ParenExpr (
    val expr: Expr)
    : CNode(), Expr

data class MethodCall (
    val op: Expr?,
    val name: String,
    val args: List<Expr>)
    : CNode(), Expr

// -------------------------------------------------------------------------------------------------
// -------------------------------------------------------------------------------------------------
// -------------------------------------------------------------------------------------------------

interface Stmt
interface Decl