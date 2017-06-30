package norswap.uranium.java.model

abstract class Member
{
    abstract val name: String
    override fun toString() = name
}