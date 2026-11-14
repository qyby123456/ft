import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class BossMaterialBoundaryProof {
    private static ClassNode read(Path path) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(path)).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return node;
    }

    private static List<String> instructions(MethodNode method) {
        Textifier printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        return printer.getText().stream().map(Object::toString).toList();
    }

    private static Map<String, String> originalIds(Path jar) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new java.net.URL[]{jar.toUri().toURL()}, ClassLoader.getPlatformClassLoader())) {
            Class<?> kind = Class.forName("net.farmingtales.ftboss.util.BossRelicRules$Kind", true, loader);
            Class<?> rules = Class.forName("net.farmingtales.ftboss.util.WeaponBossMaterialRules", true, loader);
            var itemId = rules.getMethod("itemId", kind);
            var count = rules.getMethod("count", kind);
            Map<String, String> ids = new LinkedHashMap<>();
            for (Object value : kind.getEnumConstants()) {
                if (!Integer.valueOf(1).equals(count.invoke(null, value))) throw new AssertionError("Original count changed");
                ids.put(((Enum<?>) value).name(), (String) itemId.invoke(null, value));
            }
            if (!Integer.valueOf(0).equals(count.invoke(null, new Object[]{null}))) throw new AssertionError("Null original reward is not empty");
            return ids;
        }
    }

    public static void main(String[] arguments) throws Exception {
        Path originalApplication = Path.of(arguments[0]);
        Path compiled = Path.of(arguments[1]);
        ClassNode previous = read(originalApplication.resolve("net/farmingtales/ftboss/skill/BossRelicDrops.class"));
        ClassNode current = read(compiled.resolve("net/farmingtales/ftboss/skill/BossRelicDrops.class"));
        int normalized = 0;
        if (previous.methods.size() != current.methods.size()) throw new AssertionError("Reward method set changed");
        for (MethodNode method : current.methods) {
            new Analyzer<>(new BasicVerifier()).analyze(current.name, method);
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals("net/farmingtales/ftboss/item/BossMaterialDrops") && call.name.equals("reward")) {
                    call.owner = "net/farmingtales/ftboss/item/WeaponBossMaterialItems";
                    normalized++;
                }
            }
            MethodNode before = previous.methods.stream().filter(entry -> entry.name.equals(method.name) && entry.desc.equals(method.desc)).findFirst().orElseThrow();
            if (!instructions(before).equals(instructions(method))) throw new AssertionError("Original reward instructions differ beyond material callback owner: " + method.name);
        }
        if (normalized != 1) throw new AssertionError("Expected one direct Boss material callback");
        int methods = 0;
        try (var paths = Files.walk(compiled)) {
            for (Path path : paths.filter(value -> value.toString().endsWith(".class")).toList()) {
                ClassNode node = read(path);
                for (MethodNode method : node.methods) {
                    if ((method.access & (org.objectweb.asm.Opcodes.ACC_ABSTRACT | org.objectweb.asm.Opcodes.ACC_NATIVE)) != 0) continue;
                    new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
                    methods++;
                }
            }
        }
        Map<String, String> ids = originalIds(Path.of(arguments[2]));
        Map<String, String> expected = Map.of("FROST", "frost_material", "BUTCHER", "butcher_material", "TEA", "tea_material", "WINE", "wine_material", "RAT", "cheese_material", "DEFECT", "capacitor_material", "COMPOST", "spore_material", "APPLE", "apple_material", "BENGER", "shepherd_material", "TIDE", "tide_material");
        if (!ids.equals(expected)) throw new AssertionError("Frozen Forge material mapping differs: " + ids);
        System.out.println("failures=0;originalRewardMethodsPreserved=" + current.methods.size() + ";onlyCallbackOwnerChanged=true;compiledMethodsBasicVerified=" + methods + ";originalForgeMaterialIds=" + ids + ";originalCountEach=1;originalNullCount=0");
    }
}
