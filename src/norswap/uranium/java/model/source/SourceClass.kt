package norswap.uranium.java.model.source
import norswap.lang.Node
import norswap.lang.java8.ast.ClassType
import norswap.lang.java8.ast.CtorCall
import norswap.lang.java8.ast.Keyword
import norswap.lang.java8.ast.Type
import norswap.lang.java8.ast.TypeDecl
import norswap.lang.java8.ast.TypeDeclKind.ANNOTATION
import norswap.lang.java8.ast.TypeDeclKind.ENUM
import norswap.lang.java8.ast.TypeDeclKind.INTERFACE
import norswap.uranium.java.Context
import norswap.uranium.java.model.Data
import norswap.uranium.java.model.Field
import norswap.uranium.java.model.Klass
import norswap.uranium.java.model.Method
import norswap.uranium.java.types.ParameterType
import norswap.uranium.java.types.RefType
import norswap.uranium.java.typing.resolve_qualified_type
import norswap.uranium.java.types.ClassType as ClassT
import norswap.utils.cast
import norswap.utils.maybe_list
import norswap.utils.multimap.HashMultiMap
import norswap.utils.multimap.MultiMap
import norswap.utils.multimap.flat_filter_values
import norswap.utils.proclaim
import norswap.utils.rangeTo

// node is TypeDecl or CtorCall
class SourceClass (val node: Node, override val outer: Scope, val index: Int): Klass(), Scope
{
    // ---------------------------------------------------------------------------------------------

    val decl get() = node as TypeDecl
    val call get() = node as CtorCall

    // ---------------------------------------------------------------------------------------------

    override val is_anonymous
        = node is CtorCall

    // ---------------------------------------------------------------------------------------------

    override val is_local
        = !is_anonymous && index != 0

    // ---------------------------------------------------------------------------------------------

    override val name = when {
        is_anonymous -> "$index"
        is_local     -> "$index" + decl.name
        else         -> decl.name
    }

    // ---------------------------------------------------------------------------------------------

    // NOTE(norswap): The anon/local checks below are in case the user provided an illegal modifier.

    // ---------------------------------------------------------------------------------------------

    override val is_nested
        get() = !is_anonymous && outer is SourceClass

    // ---------------------------------------------------------------------------------------------

    override val static
        get() = if (is_anonymous || is_local) false else decl.mods.contains(Keyword.static)

    // ---------------------------------------------------------------------------------------------

    override val is_enum
        get() = if (is_anonymous || is_local) false else decl.kind == ENUM

    // ---------------------------------------------------------------------------------------------

    override val is_interface
        get() = if (is_anonymous || is_local) false else decl.kind == INTERFACE || decl.kind == ANNOTATION

    // ---------------------------------------------------------------------------------------------

    override val is_annotation
        get() = if (is_anonymous || is_local) false else decl.kind == ANNOTATION

    // ---------------------------------------------------------------------------------------------

    override val binary_name: String
    init {
        binary_name = when (outer) {
            is File  -> outer.pkg.prefix + name
            is Klass -> outer.binary_name + "$" + name
            is Block -> outer.innermost_outer_class.binary_name + "$" + index + if (this.is_anonymous) "" else name
            else     -> throw Error("unknown scope type")
        }
    }

    // ---------------------------------------------------------------------------------------------

    // Constructed by visitors.
    override val fields =  HashMap<String, Field>()

    // ---------------------------------------------------------------------------------------------

    override val static_fields: Map<String, Field> by lazy {
        fields.filterValues { it.static }
    }

    // ---------------------------------------------------------------------------------------------

    // Constructed by visitors.
    override val methods = HashMultiMap<String, Method>()

    // ---------------------------------------------------------------------------------------------

    override val static_methods: MultiMap<String, Method> by lazy {
        methods.flat_filter_values { it.static }
    }

    // ---------------------------------------------------------------------------------------------

    // Constructed by visitors.
    override val constructors = ArrayList<SourceConstructor>()

    // ---------------------------------------------------------------------------------------------

    // Constructed by visitors.
    val klasses = HashMap<String, SourceClass>()

    // ---------------------------------------------------------------------------------------------

    val static_klasses: Map<String, SourceClass> by lazy {
        klasses.filterValues { it.static }
    }

    // ---------------------------------------------------------------------------------------------

    override val classes: Map<String, String> by lazy {
        klasses.mapValues { it.value.binary_name }
    }

    // ---------------------------------------------------------------------------------------------

    override val static_classes: Map<String, String> by lazy {
        static_klasses.mapValues { it.value.binary_name }
    }

    // ---------------------------------------------------------------------------------------------

    // Constructed by visitors.
    override val type_parameters = HashMap<String, SourceTypeParameter>()

    // ---------------------------------------------------------------------------------------------

    override val enum_constants: Map<String, SourceEnumConstant>?
        = (is_enum.. { fields.filterValues { it is SourceEnumConstant } }).cast()

    // ---------------------------------------------------------------------------------------------

    override fun superclass (ctx: Context): Klass
    {
        if (node is CtorCall) // anonymous
            return lookup_superclass(node.type, ctx)

        proclaim (node as TypeDecl)

        if (is_interface) // senseless query!
            return ctx.ObjectClass

        if (node.extends.size == 0) // no extends clause
            if (is_enum)
                return ctx.EnumClass
            else
                return ctx.ObjectClass

        return lookup_superclass(node.extends[0], ctx)
    }

    // ---------------------------------------------------------------------------------------------

    private fun lookup_superclass (type: Type, ctx: Context): Klass
    {
        if (type !is ClassType) {
            ctx.report("Invalid superclass reference $type for $binary_name")
            return ctx.ObjectClass
        }

        val superclass_name = type.parts.map { it.name }
        val superclass = resolve_qualified_type(superclass_name, ctx)

        if (superclass == null || superclass.is_interface)
            return ctx.ObjectClass
        else
            return superclass
    }

    // ---------------------------------------------------------------------------------------------

    override fun superinterfaces (ctx: Context): List<Klass>
    {
        if (node is CtorCall) // anonymous
            return maybe_list(lookup_superinterface(node.type, ctx))
        else
            return superinterfaces_of_named(node as TypeDecl, ctx)
    }

    // ---------------------------------------------------------------------------------------------

    fun lookup_superinterface (type: Type, ctx: Context): Klass?
    {
        if (type !is ClassType) {
            ctx.report("Invalid superinterface reference $type for $binary_name")
            return null
        }

        val superinterface_name = type.parts.map { it.name }
        val superinterface = resolve_qualified_type(superinterface_name, ctx)

        if (superinterface?.is_interface == true)
            return null
        else
            return superinterface
    }

    // ---------------------------------------------------------------------------------------------

    fun superinterfaces_of_named (node: TypeDecl, ctx: Context): List<Klass>
    {
        val list = ArrayList<Klass>()

        if (is_annotation)
            list.add(ctx.AnnotationClass)

        // We are forgiving and allow "implements" on interfaces.
        // We also allow "extends" on annotations.
        // The error(s) will be reported separately by another check.
        val types
            = if (is_interface)
            node.extends + node.implements
        else
            node.implements

        types.forEach {
            lookup_superinterface(it, ctx)?.let { list.add(it) }
        }

        return list
    }

    // ---------------------------------------------------------------------------------------------

    override fun outer_class (ctx: Context): Klass?
        = is_nested .. { outer as SourceClass }

    // ---------------------------------------------------------------------------------------------

    override fun get_data (name: String): Data?
        = fields[name]

    // ---------------------------------------------------------------------------------------------

    override fun get_method (name: String): List<Method>
        = methods[name] ?: emptyList()

    // ---------------------------------------------------------------------------------------------

    // TODO what about ClassT' parameters?
    // Type parameters shadow inner classes.
    override fun get_type (name: String, ctx: Context): RefType?
        = type_parameters[name]?.let(::ParameterType) ?: klasses[name]?.let { ClassT(it) }

    // ---------------------------------------------------------------------------------------------

    override fun klass (name: String, ctx: Context): Klass?
        = klasses[name]

    // ---------------------------------------------------------------------------------------------
}