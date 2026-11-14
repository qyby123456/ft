package ftboss.nativeprobe;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;
import java.util.Set;
public final class LoadedBossClassCaptureV1 {
    public static void premain(String argument,Instrumentation instrumentation) throws Exception {
        Path directory=Path.of(argument);Files.createDirectories(directory);
        Set<String> selected=Set.of("net/farmingtales/ftboss/entity/EasterBossEntity","net/farmingtales/ftboss/boss/ai/ManorKiting","net/farmingtales/ftboss/boss/HiddenBossIntro","net/farmingtales/ftboss/boss/MeatBoss");
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override public byte[] transform(ClassLoader loader,String name,Class<?> redefined,ProtectionDomain domain,byte[] bytes) {
                if (selected.contains(name)) try {
                    Path output=directory.resolve(name.replace('/','_')+".class");
                    if (!Files.exists(output)) Files.write(output,bytes,StandardOpenOption.CREATE_NEW);
                } catch (Exception failure) { failure.printStackTrace(); }
                return null;
            }
        },false);
    }
}
