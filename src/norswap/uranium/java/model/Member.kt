package norswap.uranium.java.model

/**
 * Parent for [Klass], [Method], [Field] and [Constructor].
 */
interface Member
{
    val name: String
    val static: Boolean
}