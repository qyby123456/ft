import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class NativePassiveProofReadOnly {
    public static void main(String[] arguments) throws Exception {
        ClassNode host = new ClassNode();
        new ClassReader(Files.readAllBytes(Path.of(arguments[0]))).accept(host, 0);
        ClassNode source = new ClassNode();
        new ClassReader(Files.readAllBytes(Path.of(arguments[1]))).accept(source, 0);
        Class<?> proof = Class.forName("net.farmingtales.ftboss.item.OwnerFieldInstallationProof");
        var check = proof.getDeclaredMethod("siteAndOrder", ClassNode.class, MethodNode.class, MethodNode.class);
        check.setAccessible(true);
        for (String name : List.of("ftboss$fullRepairVoidMutation", "ftboss$fullRepairReturnedMutation", "ftboss$fullRepairAlias")) {
            MethodNode original = source.methods.stream().filter(method -> method.name.equals(name)).findFirst().orElseThrow();
            MethodNode actual = host.methods.stream().filter(method -> method.name.endsWith("$" + name) && method.desc.equals(original.desc)).findFirst().orElseThrow();
            System.out.println(name + "=" + check.invoke(null, host, original, actual));
        }
    }
}
