package norswap.uranium.java.model2.reflect
import norswap.uranium.java.model2.TypeParameter
import java.lang.reflect.TypeVariable

class ReflectionTypeParameter (val variable: TypeVariable<*>): TypeParameter()
{
    override val name
        = variable.name
}