package norswap.uranium.java.model.reflect
import norswap.uranium.AnyClass
import norswap.uranium.java.Context
import norswap.uranium.java.model.Field
import norswap.uranium.java.model.Klass
import norswap.utils.multimap.MultiMap
import norswap.utils.multimap.flat_filter_values
import norswap.utils.multimap.multi_assoc
import norswap.utils.rangeTo
import java.lang.reflect.Modifier

class ReflectionClass (val klass: AnyClass): Klass()
{
    // ---------------------------------------------------------------------------------------------

    override val name
        = klass.simpleName

    // ---------------------------------------------------------------------------------------------

    override val binary_name
        = klass.name

    // ---------------------------------------------------------------------------------------------

    override val is_nested
        get() = klass.enclosingClass != null

    // ---------------------------------------------------------------------------------------------

    override val static
        get() = Modifier.isStatic(klass.modifiers)

    // ---------------------------------------------------------------------------------------------

    override val is_enum
        get() = klass.isEnum

    // ---------------------------------------------------------------------------------------------

    override val is_interface
        get() = klass.isInterface

    // ---------------------------------------------------------------------------------------------

    override val is_annotation
        get() = klass.isAnnotation

    // ---------------------------------------------------------------------------------------------

    override val is_local
        get() = klass.isLocalClass

    // ---------------------------------------------------------------------------------------------

    override val is_anonymous
        get() = klass.isAnonymousClass

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
        get() = is_enum.. { fields.filterValues { it.field.isEnumConstant } }

    // ---------------------------------------------------------------------------------------------

    // BELOW
    // - We never wrap Class<*> in a ReflectionClass directly, instead we go through the resolver
    //   to ensure the classes are unique.
    // - Failure to resolve should normally never occur for reflection-loaded classes, but you
    //   never know.

    // ---------------------------------------------------------------------------------------------

    override fun superclass (ctx: Context): Klass
        =  ctx.resolver.load_superclass(klass.superclass.name, binary_name)

    // ---------------------------------------------------------------------------------------------

    override fun superinterfaces (ctx: Context): List<Klass>
    {
        val list = ArrayList<Klass>()
        klass.interfaces.forEach {
            ctx.resolver.load_superinterface(it.name, binary_name)
                ?. let { list.add(it) }
        }
        return list
    }

    // ---------------------------------------------------------------------------------------------

    override fun outer_class (ctx: Context): Klass?
        = klass.enclosingClass?.let { ctx.resolver.load_outer_class(it.name, binary_name) }

    // ---------------------------------------------------------------------------------------------
}