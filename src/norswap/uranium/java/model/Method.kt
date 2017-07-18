package norswap.uranium.java.model

abstract class Method: Member
{
    abstract val parameters: Map<String, Parameter>
    abstract val type_params: Map<String, TypeParameter>
}