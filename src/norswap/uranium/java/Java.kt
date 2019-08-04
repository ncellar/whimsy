package norswap.uranium.java
import norswap.uranium.java.typing.register_java8_typing_rules

fun Context.register_java8_rules()
{
    register_java8_scopes_builder()
    register_java8_typing_rules()
    register_java8_supertypes_rules()
}