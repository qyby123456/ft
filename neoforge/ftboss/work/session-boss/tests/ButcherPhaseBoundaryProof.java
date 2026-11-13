import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class ButcherPhaseBoundaryProof {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static int verified;
    private static int preserved;

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static MethodNode find(ClassNode node, String name) {
        List<MethodNode> methods = node.methods.stream().filter(method -> method.name.equals(name)).toList();
        require(methods.size() == 1, "Expected unique method " + node.name + "." + name);
        return methods.getFirst();
    }

    private static String instructions(MethodNode method) {
        MethodNode copy = new MethodNode(method.access, method.name, method.desc, method.signature, method.exceptions.toArray(String[]::new));
        method.accept(copy);
        for (AbstractInsnNode instruction : copy.instructions.toArray()) {
            if (instruction instanceof LineNumberNode || instruction instanceof FrameNode) copy.instructions.remove(instruction);
        }
        Textifier text = new Textifier();
        copy.instructions.accept(new TraceMethodVisitor(text));
        return text.getText().toString();
    }

    private static void verify(ClassNode node) throws Exception {
        for (MethodNode method : node.methods) {
            if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0) {
                new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
                verified++;
            }
        }
    }

    private static Set<String> calls(MethodNode method) {
        Set<String> result = new HashSet<>();
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call) result.add(call.owner + "." + call.name);
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        Path compiled = Path.of(args[2]);
        Path output = Path.of(args[3]);
        Map<String, Set<String>> whitelist = new LinkedHashMap<>();
        whitelist.put("entity/MeatMountainEntity", Set.of("hurt", "tick", "tickBossBar", "getButcherPhase", "resetButcherCombat"));
        whitelist.put("boss/MeatBoss", Set.of("tick", "tryPopTotem", "onPhaseChanged", "lambda$tick$0"));
        whitelist.put("boss/ai/ButcherCombat", Set.of("reset"));
        try (ZipFile base = new ZipFile(args[0]); ZipFile original = new ZipFile(args[1])) {
            for (var entry : whitelist.entrySet()) {
                String name = PREFIX + entry.getKey();
                ClassNode before = read(base.getInputStream(base.getEntry(name + ".class")).readAllBytes());
                ClassNode linked = read(Files.readAllBytes(compiled.resolve(name + ".class")));
                Map<String, String> untouched = new LinkedHashMap<>();
                for (MethodNode method : before.methods) {
                    if (!entry.getValue().contains(method.name)) untouched.put(method.name + method.desc, instructions(method));
                }
                for (String methodName : entry.getValue()) {
                    MethodNode replacement = find(linked, methodName);
                    before.methods.removeIf(method -> method.name.equals(methodName));
                    before.methods.add(replacement);
                }
                ClassWriter writer = new ClassWriter(0);
                before.accept(writer);
                byte[] bytes = writer.toByteArray();
                ClassNode after = read(bytes);
                for (MethodNode method : after.methods) {
                    String key = method.name + method.desc;
                    if (untouched.containsKey(key)) {
                        require(untouched.get(key).equals(instructions(method)), "Unrelated method changed " + name + "." + key);
                        preserved++;
                    }
                }
                require(after.fields.size() == before.fields.size(), "Field structure drift");
                if (name.endsWith("MeatMountainEntity")) {
                    Set<String> hurt = calls(find(after, "hurt"));
                    for (String required : Set.of("dodge", "beginDamage", "endDamage")) require(hurt.contains(PREFIX + "boss/ai/ButcherPhases." + required), "Missing original damage lifecycle " + required);
                    require(!find(after, "hurt").tryCatchBlocks.isEmpty(), "Damage bypass cleanup lost finally handler");
                    require(calls(find(after, "tick")).contains(PREFIX + "boss/ai/ButcherPhases.tick"), "Natural tick hook missing");
                }
                verify(after);
                Path target = output.resolve(name + ".class");
                Files.createDirectories(target.getParent());
                Files.write(target, bytes);
            }
            String rulesName = PREFIX + "boss/ai/ButcherPhaseRules.class";
            byte[] rules = original.getInputStream(original.getEntry(rulesName)).readAllBytes();
            Path rulesTarget = output.resolve(rulesName);
            Files.createDirectories(rulesTarget.getParent());
            Files.write(rulesTarget, rules);
            verify(read(rules));
            String phaseName = PREFIX + "boss/ai/ButcherPhases.class";
            byte[] phase = Files.readAllBytes(compiled.resolve(phaseName));
            require(!new String(phase, StandardCharsets.ISO_8859_1).contains("net/minecraftforge/"), "Forge API leaked");
            verify(read(phase));
            Files.write(output.resolve(phaseName), phase);
        }
        System.out.println("failures=0;methodsBasicVerified=" + verified + ";unrelatedMethodsPreserved=" + preserved + ";originalPhaseRulesByteIdentical=true;attackUpdateAndHitUnchanged=true");
    }
}
