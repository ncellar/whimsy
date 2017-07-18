package norswap.uranium.java.model
import norswap.uranium.java.model.source.Variable
import norswap.uranium.java.model.source.SourceParameter

/**
 * Parent for [Field], [Variable] and [SourceParameter].
 */
interface Data
{
    val name: String
}