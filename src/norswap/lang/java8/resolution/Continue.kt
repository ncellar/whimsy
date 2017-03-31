package norswap.lang.java8.resolution
import norswap.uranium.Attribute

class Continue (val required: List<Attribute>)
    : RuntimeException("", null, false, false) // no stack trace
{
    constructor (vararg required: Attribute): this(required.toList())
}