package norswap.uranium.java.model.reflect
import norswap.uranium.java.model.Constructor

class ReflectionConstructor (val field: java.lang.reflect.Constructor<*>): Constructor()
{
    override val name
        = field.name
}
