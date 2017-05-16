package norswap.lang.java8.resolution
import norswap.uranium.Attribute
import norswap.uranium.Node
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

fun ExtendingNonClass (node: Node) = ReactorError {
    affected = listOf(Attribute(node, "super_type"))
    _msg = "Class extends a non-class type"
}

// =================================================================================================