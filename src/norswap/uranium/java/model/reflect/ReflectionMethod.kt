package norswap.uranium.java.model.reflect
import norswap.uranium.java.model.Method
import norswap.uranium.java.model.Parameter
import norswap.uranium.java.model.TypeParameter
import java.lang.reflect.Modifier

class ReflectionMethod (val method: java.lang.reflect.Method): Method()
{
    // ---------------------------------------------------------------------------------------------

    override val name
        = method.name

    // ---------------------------------------------------------------------------------------------

    override val static
        = Modifier.isStatic(method.modifiers)

    // ---------------------------------------------------------------------------------------------

    override val parameters: Map<String, Parameter>
        = method.parameters.associate { it.name to ReflectionParameter(it) }

    // ---------------------------------------------------------------------------------------------

    override val type_params: Map<String, TypeParameter>
        = method.typeParameters.associate { it.name to ReflectionTypeParameter(it) }

    // ---------------------------------------------------------------------------------------------
}