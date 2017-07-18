package norswap.uranium.java.model2
import norswap.uranium.java.model2.source.Variable
import norswap.uranium.java.model2.source.SourceParameter

/**
 * Parent for [Field], [Variable] and [SourceParameter].
 */
interface Data
{
    val name: String
}