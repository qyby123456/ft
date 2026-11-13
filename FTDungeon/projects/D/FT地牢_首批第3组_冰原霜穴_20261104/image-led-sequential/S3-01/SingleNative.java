import java.lang.reflect.Method;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public final class SingleNative {
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Method inspect = VerifyDeliveries.class.getDeclaredMethod("inspect", Path.class);
        inspect.setAccessible(true);
        System.out.println(inspect.invoke(null, Path.of(arguments[0])));
    }
}
