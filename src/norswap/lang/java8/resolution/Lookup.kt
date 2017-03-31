package norswap.lang.java8.resolution
import norswap.uranium.Attribute

// -------------------------------------------------------------------------------------------------

/**
 * Represents the result of a lookup for an item of type [T].
 */
sealed class Lookup <out T>
{
    /**
     * Converts this object to a lookup result for a list of items of type [T].
     */
    abstract operator fun unaryPlus(): LookupList<T>

    /**
     * If the item was found, return it, else throws an exception.
     */
    open val value: T
        get() = throw  Error("lookup failed")
}

// -------------------------------------------------------------------------------------------------

/**
 * A successful lookup for an item of type T.
 */
data class Found<T> (override val value: T): Lookup<T>()
{
    override fun unaryPlus() = Found(listOf(value))
}

// -------------------------------------------------------------------------------------------------

/**
 * An ongoing item lookup, indicating that the attributes in [required]
 * are needed in order to complete the lookup.
 */
data class ContinueOld (val required: List<Attribute>): Lookup<Nothing>()
{
    constructor (vararg required: Attribute): this(required.asList())

    override fun unaryPlus(): ContinueOld = this
}

// -------------------------------------------------------------------------------------------------

/**
 * A failed item lookup.
 */
object Missing: Lookup<Nothing>()
{
    override fun unaryPlus() = this
}

// -------------------------------------------------------------------------------------------------

typealias LookupList<T> = Lookup<List<T>>

// -------------------------------------------------------------------------------------------------

/**
 * Returns [thing] wrapped in a [Found] object if it is non-null, [Missing] otherwise.
 */
fun <T> lookup_wrap (thing: T?): Lookup<T>
    = thing ?. let { Found(it) } ?: Missing

// -------------------------------------------------------------------------------------------------

/**
 * Concatenates two [LookupList]. If any of the two lists is a [ContinueOld], the result
 * is a continue with all required attributes from both lists. If both lists are [Missing], the
 * result is [Missing]. Otherwise returns a [Found] with all found items.
 */
operator fun <T> LookupList<T>.plus (other: LookupList<T>)
    : LookupList<T>
    = when (this) {
        is Missing -> {
            when (other) {
                is Missing  -> Missing
                is Found    -> other
                is ContinueOld -> other
        }   }
        is Found -> {
            when (other) {
                is Missing  -> this
                is Found    -> Found(this.value + other.value)
                is ContinueOld -> other
        }   }
        is ContinueOld -> {
            when (other) {
                is Missing  -> this
                is Found    -> this
                is ContinueOld -> ContinueOld(this.required + other.required)
    }   }   }

// -------------------------------------------------------------------------------------------------