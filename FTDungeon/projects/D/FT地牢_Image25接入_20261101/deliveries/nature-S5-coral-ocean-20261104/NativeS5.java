import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.GsonBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public final class NativeS5 {
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Method inspect = VerifyDeliveries.class.getDeclaredMethod("inspect", Path.class);
        inspect.setAccessible(true);
        for (String argument : arguments) {
            Path folder = Path.of(argument);
            Object result = inspect.invoke(null, folder);
            Files.writeString(folder.resolve("native-qa.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result));
            System.out.println(folder.getFileName() + " native PASS");
        }
    }
}
