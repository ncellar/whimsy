package norswap.uranium.java.model2.source
import norswap.lang.java8.ast.FormalParameter
import norswap.uranium.java.model2.Data
import norswap.uranium.java.model2.Parameter

// -------------------------------------------------------------------------------------------------

abstract class SourceParameter: Parameter(), Data

// -------------------------------------------------------------------------------------------------

class TypedParameter (val node: FormalParameter): SourceParameter()
{
    override val name = node.name
}

// -------------------------------------------------------------------------------------------------

class UntypedParameter (override val name: String): SourceParameter()

// -------------------------------------------------------------------------------------------------