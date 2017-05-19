package norswap.lang.java8.resolution
import norswap.uranium.Attribute
import norswap.uranium.Node
import norswap.uranium.ReactorError
import norswap.lang.java8.resolution.ResolutionErrors.*
import java.net.URL

// =================================================================================================

enum class ResolutionErrors
{
    AmbiguousClassDefinitions,
    ConflictingSourceClasses,
    ExtendingNonClass,
    ConflictingBytecodeClasses,
    ReflectiveClassConflict,
    CannotLoadClassFile
}

// =================================================================================================

fun AmbiguousClassDefinitions (chain_name: String) = ReactorError {
    _tag = AmbiguousClassDefinitions
    _msg = "Ambiguous class definitions for class chain: $chain_name."
}

// =================================================================================================

fun ExtendingNonClass (node: Node) = ReactorError {
    _tag = ExtendingNonClass
    affected = listOf(Attribute(node, "super_type"))
    _msg = "Class extends a non-class type"
}

// =================================================================================================

fun ConflictingSourceClasses (cano_name: String) = ReactorError {
    _tag = ConflictingSourceClasses
    _msg = "Conflicting source definitions for $cano_name"
}

// =================================================================================================

fun ConflictingBytecodeClasses (cano_name: String) = ReactorError {
    _tag = ConflictingBytecodeClasses
    _msg = "Conflicting bytecode definitions for $cano_name"
}

// =================================================================================================

fun ReflectiveClassConflict (cano_name: String) = ReactorError {
    _tag = ReflectiveClassConflict
    _msg = "Class conflicting with reflective definition for $cano_name"
}

// =================================================================================================

fun CannotLoadClassFile (url: URL) = ReactorError {
    _tag = CannotLoadClassFile
    _msg = "Cannot load class file: $url"
}

// =================================================================================================