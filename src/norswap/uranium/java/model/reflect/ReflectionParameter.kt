package norswap.uranium.java.model.reflect
import norswap.uranium.java.model.Parameter

class ReflectionParameter (val parameter: java.lang.reflect.Parameter): Parameter()
{
    override val name
        = parameter.name
}