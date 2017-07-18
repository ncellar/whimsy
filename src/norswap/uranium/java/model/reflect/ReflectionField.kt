package norswap.uranium.java.model.reflect
import norswap.uranium.java.model.Field
import java.lang.reflect.Modifier

class ReflectionField (val field: java.lang.reflect.Field): Field()
{
    override val name
        = field.name

    override val static
        = Modifier.isStatic(field.modifiers)
}