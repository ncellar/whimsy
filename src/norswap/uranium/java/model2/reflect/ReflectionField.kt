package norswap.uranium.java.model2.reflect
import norswap.uranium.java.model2.Field
import java.lang.reflect.Modifier

class ReflectionField (val field: java.lang.reflect.Field): Field()
{
    override val name
        = field.name

    override val static
        = Modifier.isStatic(field.modifiers)
}