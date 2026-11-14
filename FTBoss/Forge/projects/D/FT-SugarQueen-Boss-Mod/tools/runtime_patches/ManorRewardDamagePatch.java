import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class ManorRewardDamagePatch {
    private static final String HOOK = "net/farmingtales/ftboss/integ/spells/ManorRewardBalance.class";

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static void verify(byte[] bytes) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        require((node.version & 0xFFFF) == Opcodes.V17, "Java 17 bytecode required");
        int getters = 0;
        for (var method : node.methods) {
            new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
            for (var instruction : method.instructions) {
                if (!(instruction instanceof MethodInsnNode call)) continue;
                require(!call.name.equals("getEntity"), "Unmapped development getter remains");
                if (call.name.equals("m_7639_")) {
                    require(call.owner.equals("net/minecraft/world/damagesource/DamageSource"), "Getter must use vanilla owner");
                    require(call.desc.equals("()Lnet/minecraft/world/entity/Entity;"), "Wrong damage owner descriptor");
                    getters++;
                }
            }
        }
        require(getters == 1, "Exactly one production damage owner getter required");
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments[0].equals("remap")) {
            ClassWriter writer = new ClassWriter(0);
            new ClassReader(Files.readAllBytes(Path.of(arguments[1])))
                    .accept(new ClassRemapper(writer, new ZicaiVoidPatch.Names(Path.of(arguments[2]))), 0);
            byte[] bytes = writer.toByteArray();
            verify(bytes);
            Path output = Path.of(arguments[3]).resolve(HOOK);
            Files.createDirectories(output.getParent());
            Files.write(output, bytes);
            System.out.println("PASS damage getter remapped to vanilla DamageSource.m_7639_");
            return;
        }
        require(arguments[0].equals("verify"), "Expected remap or verify");
        try (ZipFile before = new ZipFile(arguments[1]); ZipFile after = new ZipFile(arguments[2])) {
            var originalNames = before.stream().map(entry -> entry.getName()).toList();
            var candidateNames = after.stream().map(entry -> entry.getName()).toList();
            require(new HashSet<>(originalNames).size() == originalNames.size(), "Duplicate baseline entries");
            require(new HashSet<>(candidateNames).size() == candidateNames.size(), "Duplicate candidate entries");
            require(new HashSet<>(originalNames).equals(new HashSet<>(candidateNames)), "Archive entries added or removed");
            int changed = 0;
            for (String name : originalNames) {
                byte[] original = before.getInputStream(before.getEntry(name)).readAllBytes();
                byte[] candidate = after.getInputStream(after.getEntry(name)).readAllBytes();
                if (!Arrays.equals(original, candidate)) {
                    require(name.equals(HOOK), "Unexpected runtime change: " + name);
                    changed++;
                    verify(candidate);
                }
            }
            require(changed == 1, "Only the damage event class must change");
            System.out.println("PASS exact archive delta: one class repaired, " + (originalNames.size() - 1) + " entries preserved");
        }
    }
}
