package norswap.uranium.java
import norswap.lang.Node
import norswap.lang.java8.ast.ClassType
import norswap.lang.java8.ast.TypeDecl
import norswap.uranium.Attribute
import norswap.uranium.Reaction
import norswap.uranium.Reactor
import norswap.uranium.java.model.Klass
import norswap.uranium.java.model.source.SourceClass
import norswap.uranium.java.typing.find_first_class
import kotlin.jvm.internal.Ref

// -------------------------------------------------------------------------------------------------

fun Context.register_java8_supertypes_rules()
{
    reactor.register_java8_supertypes_rules(this)
}

// -------------------------------------------------------------------------------------------------

fun Reactor.register_java8_supertypes_rules (ctx: Context)
{
    add_visitor <TypeDecl> (ctx::check_multiple_extends)
    add_visitor <TypeDecl> (ctx::resolve_extends)
}

// -------------------------------------------------------------------------------------------------

private fun Context.supertyping
    (node: Node, _name: String, _apply: (Reaction) -> Unit)
{
    val reaction = Reaction {
        name = _name
        consumed = emptyList()
        supplied = listOf(Attribute(node, "extends"))
        apply = _apply
    }

    reactor.register(reaction)
}

// -------------------------------------------------------------------------------------------------

fun Context.check_multiple_extends (node: TypeDecl, klass: SourceClass)
{
    if (node.extends.size < 2) return
    report("Multiple types in extends clause for ${klass.binary_name}, only considering first one.")
}

// -------------------------------------------------------------------------------------------------

fun Context.resolve_extends (node: TypeDecl, start: Boolean)
{
    if (!start) return
    val klass: SourceClass = node["scope"]
    if (klass.is_interface)
        resolve_extends_interface(node, klass)
    else
        resolve_extends_class(node, klass)
}

// -------------------------------------------------------------------------------------------------

fun Context.resolve_extends_class (node: TypeDecl, klass: SourceClass)
{
    supertyping (node, "extends class") b@ { reaction ->

        if (node.extends.size == 0) {
            node["extends"] = ObjectClass
            return@b
        }

        val type = node.extends[0]
        if (type !is ClassType) {
            report("Invalid superclass reference: $type")
            return@b
        }

        val full_name = type.parts.map { it.name }
        val size = Ref.IntRef()
        val first_class = klass.outer.find_first_class(full_name, this, size) ?: return@b

        reaction.continuation(
            extends_class_continuation(node, full_name, first_class, size.element))

        //reaction.continuation()
        TODO()
    }
}

// -------------------------------------------------------------------------------------------------

fun Context.extends_class_continuation
    (node: Node, full_name: List<String>, current_class: Klass, current_size: Int): Reaction
= Reaction {
    name = "extends class continuation"
    consumed = listOf(Attribute(current_class, "ready")) // TODO generate this
    supplied = listOf(Attribute(node, "extends"))
    apply = c@ {
        if (current_size == full_name.size) {
            node["extends"] = current_class
            return@c
        }

        val inner_simple_name = full_name[current_size]
        val inner_binary_name = current_class.classes[inner_simple_name]

        if (inner_binary_name == null) {
            report("$current_class.$inner_simple_name is not a class type.")
            return@c
        }

        val inner_class = resolver.load_class(inner_binary_name) ?: return@c
        continuation(
            extends_class_continuation(node, full_name, inner_class, current_size + 1))
    }
}

// -------------------------------------------------------------------------------------------------

fun Context.ready_klass (klass: Klass)
{
    val ctx = this

    if (klass is SourceClass)
        Reaction {
            name = "ready source class"
            consumed = listOf(Attribute(klass.node, "ready")) // TODO generate this
            supplied = listOf(Attribute(klass, "ready"))
            apply = {
                reactor[klass, "ready"] = true
            }
        }
    else
        Reaction {
            name = "ready non-source class"
            consumed = emptyList()
            supplied = listOf(Attribute(klass, "ready"))
            apply = {
                // TODO
                klass.superclass(ctx)
                klass.superinterfaces(ctx)
                reactor[klass, "ready"] = true
            }
        }
}

// -------------------------------------------------------------------------------------------------

fun Context.resolve_extends_interface (node: TypeDecl, klass: SourceClass)
{
    supertyping (node, "extends interface")
    {
        TODO()
    }
}

// -------------------------------------------------------------------------------------------------

fun Context.resolve_implements (node: TypeDecl, start: Boolean)
{
    if (!start) return
}

// -------------------------------------------------------------------------------------------------