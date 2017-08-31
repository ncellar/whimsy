package norswap.uranium

/**
 * A problem with the tree detected during the derivation of an attribute, which can be
 * reported to the [Reactor].
 */
class UraniumError (val msg: String, var attrs: List<Attribute>? = null)