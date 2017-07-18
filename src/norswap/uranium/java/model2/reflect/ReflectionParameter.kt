package norswap.uranium.java.model2.reflect
import norswap.uranium.java.model2.Parameter

class ReflectionParameter (val parameter: java.lang.reflect.Parameter): Parameter()
{
    override val name
        = parameter.name
}