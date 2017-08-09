package norswap.uranium.java;
import java.lang.invoke.MethodHandle;

// Because Kotlin doesn't support @PolymorphicSignature
// https://youtrack.jetbrains.com/issue/KT-14416
public class WalkerJavaSupport
{
    public static Object invoke (MethodHandle handle) throws Throwable
    {
        return handle.invoke();
    }
}
