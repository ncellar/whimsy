package norswap.uranium.java.model2.bytecode.sig

// -------------------------------------------------------------------------------------------------

open class SignatureType

// -------------------------------------------------------------------------------------------------

class PrimitiveType (val name: String): SignatureType()

// -------------------------------------------------------------------------------------------------

open class ParameterizedType: SignatureType()
{
    val type_arguments = ArrayList<SignatureType>()
}

// -------------------------------------------------------------------------------------------------

// binary name
class ClassType (val name: String): ParameterizedType()

// -------------------------------------------------------------------------------------------------

// simple name
class InnerClassType (val outer: SignatureType, val name: String): ParameterizedType()

// -------------------------------------------------------------------------------------------------

class TypeVariable (val name: String): SignatureType()

// -------------------------------------------------------------------------------------------------

class ArrayType (val base_type: SignatureType): SignatureType()

// -------------------------------------------------------------------------------------------------

object Wildcard: SignatureType()

// -------------------------------------------------------------------------------------------------

class SuperWildcard (val bound: SignatureType): SignatureType()

// -------------------------------------------------------------------------------------------------

class ExtendsWildcard (val bound: SignatureType): SignatureType()

// -------------------------------------------------------------------------------------------------