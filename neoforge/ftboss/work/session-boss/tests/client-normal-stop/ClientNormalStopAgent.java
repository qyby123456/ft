import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class ClientNormalStopAgent {
    public static void agentmain(String receiptPath,Instrumentation instrumentation) throws Exception {
        Class<?> clientType=Arrays.stream(instrumentation.getAllLoadedClasses()).filter(type->type.getName().equals("net.minecraft.client.Minecraft")).findFirst().orElseThrow();
        Object client=clientType.getMethod("getInstance").invoke(null);
        Files.writeString(Path.of(receiptPath),"{\"cleanupOnly\":true,\"scheduledOnClientThread\":true,\"authenticationOrAuthorityModified\":false,\"stopCalled\":false}");
        Runnable stop=()->{
            try {
                Files.writeString(Path.of(receiptPath),"{\"cleanupOnly\":true,\"scheduledOnClientThread\":true,\"authenticationOrAuthorityModified\":false,\"stopCalled\":true}");
                clientType.getMethod("stop").invoke(client);
            } catch(Exception failure) {throw new RuntimeException(failure);}
        };
        clientType.getMethod("execute",Runnable.class).invoke(client,stop);
    }
}
