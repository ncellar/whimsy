package norswap.uranium.java.model.reflect
import norswap.uranium.java.model.TypeParameter
import java.lang.reflect.TypeVariable

class ReflectionTypeParameter (val variable: TypeVariable<*>): TypeParameter()
{
    override val name
        = variable.name
}