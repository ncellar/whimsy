package norswap.uranium.java.model2.reflect
import norswap.uranium.java.model2.Constructor

class ReflectionConstructor (val field: java.lang.reflect.Constructor<*>): Constructor()
{
    override val name
        = field.name
}
