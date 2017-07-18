package norswap.uranium.java.model2.source
import norswap.lang.Node
import norswap.lang.java8.ast.CtorCall
import norswap.lang.java8.ast.Keyword
import norswap.lang.java8.ast.TypeDecl
import norswap.lang.java8.ast.TypeDeclKind.ENUM
import norswap.uranium.java.model2.Field
import norswap.uranium.java.model2.Klass
import norswap.uranium.java.model2.Method
import norswap.uranium.java.model2.class_for
import norswap.utils.cast
import norswap.utils.multimap.HashMultiMap
import norswap.utils.multimap.MultiMap
import norswap.utils.multimap.flat_filter_values
import norswap.utils.rangeTo

// node is TypeDecl or CtorCall
class SourceClass (val node: Node, override val outer: Scope, val index: Int): Klass(), Scope
{
    // ---------------------------------------------------------------------------------------------

    val anonymous  = node is CtorCall
    val decl get() = node as TypeDecl
    val call get() = node as CtorCall

    // ---------------------------------------------------------------------------------------------

    override val name =
        if (anonymous) decl.name else "" + index

    // ---------------------------------------------------------------------------------------------

    override val static =
        if (anonymous) false else decl.mods.contains(Keyword.static)

    // ---------------------------------------------------------------------------------------------

    override val enum =
        if (anonymous) false else decl.kind == ENUM

    // ---------------------------------------------------------------------------------------------

    override val binary_name: String
    init {
        binary_name = when (outer) {
            is File  -> outer.pkg.prefix + name
            is Klass -> outer.binary_name + "$" + name
            is Block -> class_for(outer).binary_name + "$" + index + if (anonymous) "" else name
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
        = (enum .. { fields.filterValues { it is SourceEnumConstant } }).cast()

    // ---------------------------------------------------------------------------------------------
}