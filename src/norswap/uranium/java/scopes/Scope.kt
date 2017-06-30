package norswap.uranium.java.scopes
import norswap.uranium.java.model.*

interface Scope
{
    fun field (name: String): Field?
    fun method (name: String): List<Method>
    fun klass (name: String): Klass?
    fun member (name: String): List<Member>
}