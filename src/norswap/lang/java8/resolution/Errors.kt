package norswap.lang.java8.resolution
import norswap.uranium.Attribute
import norswap.uranium.Node
import norswap.uranium.Reaction
import norswap.uranium.ReactorError
import norswap.lang.java8.resolution.ResolutionErrors.*

// =================================================================================================

enum class ResolutionErrors
{
    ConflictingClassDefinitions,
    AmbiguousClassDefinitions
}

// =================================================================================================

fun ConflictingClassDefinitions (cano_name: String) = ReactorError {
    _tag = ConflictingClassDefinitions
    _msg = "Conflicting class definitions for $cano_name."
}

// =================================================================================================

fun AmbiguousClassDefinitions (full_name: String) = ReactorError {
    _tag = AmbiguousClassDefinitions
    _msg = "Ambiguous class definitions for $full_name."
}

// =================================================================================================

// TODO where, name?, tag
fun ClassNotFoundScopeError () = ReactorError {
    _msg = "Could not find class definition"
}

// =================================================================================================

// TODO where, name?, tag
fun MemberNotFoundScopeError () = ReactorError {
    _msg = "Could not find member"
}

// =================================================================================================

// TODO ???
fun MemberNotFoundResolutionError (reac: Reaction<*>, node: Node) =  ReactorError {
    _msg = "Could not find member"
}

// =================================================================================================

// TODO reaction ???
fun ExtendingNonClass (reaction: Reaction<*>, node: Node) = ReactorError {
    this.affected = listOf(Attribute(node, "super_type"))
    _msg = "Class extends a non-class type"
}

// =================================================================================================




//// =================================================================================================
//
//abstract class ResolutionError (reac: Reaction<*>, node: Node) : ReactorError()
//{
//    override val reaction = reac
//    override val affected = listOf(Attribute(node, "resolved"))
//}
//
//// =================================================================================================
//
//abstract class ScopeError: ReactorError()
//{
//    override val reaction = null
//}
//
//// =================================================================================================
//
//class ConflictingClassDefinitions (cano_name: String): ReactorError()
//{
//    override val reaction = null
//    override val msg = "Conflicting class definitions for $cano_name."
//}
//
//// =================================================================================================
//
//class AmbiguousClassDefinitions (full_name: String): ReactorError()
//{
//    override val reaction = null
//    override val msg = "Ambiguous class definitions for $full_name."
//}
//
//// =================================================================================================
//
//class ClassNotFoundScopeError: ScopeError()
//{
//    override val msg = "Could not find class definition"
//}
//
//// =================================================================================================
//
//class MemberNotFoundScopeError: ScopeError()
//{
//    override val msg = "Could not find member"
//}
//
//// =================================================================================================
//
//class MemberNotFoundResolutionError (reac: Reaction<*>, node: Node) : ResolutionError(reac, node)
//{
//    override val msg = "Could not find member"
//}
//
//// =================================================================================================
//
//class ExtendingNonClass (override val reaction: Reaction<*>, node: Node) : ReactorError()
//{
//    override val affected = listOf(Attribute(node, "super_type"))
//    override val msg = "Class extends a non-class type"
//}
//
//// =================================================================================================