package norswap.uranium.java.model2.reflect
import norswap.uranium.java.model2.Field
import norswap.uranium.java.model2.Klass
import norswap.utils.multimap.MultiMap
import norswap.utils.multimap.flat_filter_values
import norswap.utils.multimap.multi_assoc
import norswap.utils.rangeTo
import java.lang.reflect.Modifier

class ReflectionClass (val klass: Class<*>): Klass()
{
    // ---------------------------------------------------------------------------------------------

    override val name
        = klass.simpleName

    // ---------------------------------------------------------------------------------------------

    override val binary_name
        = klass.name

    // ---------------------------------------------------------------------------------------------

    override val static
        = Modifier.isStatic(klass.modifiers)

    // ---------------------------------------------------------------------------------------------

    override val enum
        = klass.isEnum

    // ---------------------------------------------------------------------------------------------

    override val fields: Map<String, ReflectionField>
        = klass.declaredFields.associate { it.name to ReflectionField(it) }

    // ---------------------------------------------------------------------------------------------

    override val static_fields: Map<String, Field>
        = fields.filterValues { it.static }

    // ---------------------------------------------------------------------------------------------

    override val methods: MultiMap<String, ReflectionMethod>
        = klass.declaredMethods.multi_assoc { it.name to ReflectionMethod(it) }

    // ---------------------------------------------------------------------------------------------

    override val static_methods: MultiMap<String, ReflectionMethod>
        = methods.flat_filter_values { it.static }

    // ---------------------------------------------------------------------------------------------

    override val constructors: List<ReflectionConstructor>
        = klass.constructors.map { ReflectionConstructor(it) }

    // ---------------------------------------------------------------------------------------------

    val klasses: Map<String, ReflectionClass>
        = klass.declaredClasses.associate { it.simpleName to ReflectionClass(it) }

    // ---------------------------------------------------------------------------------------------

    val static_klasses: Map<String, ReflectionClass>
        = klasses.filterValues { it.static }

    // ---------------------------------------------------------------------------------------------

    override val classes: Map<String, String>
        = klasses.mapValues { it.value.binary_name }

    // ---------------------------------------------------------------------------------------------

    override val static_classes: Map<String, String>
        = static_klasses.mapValues { it.value.binary_name }

    // ---------------------------------------------------------------------------------------------

    override val type_parameters: Map<String, ReflectionTypeParameter>
        = klass.typeParameters.associate { it.name to ReflectionTypeParameter(it) }

    // ---------------------------------------------------------------------------------------------

    override val enum_constants: Map<String, Field>?
        get() = enum .. { fields.filterValues { it.field.isEnumConstant } }

    // ---------------------------------------------------------------------------------------------
}