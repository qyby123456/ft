import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class FlightBanBoundaryProof {
    private static final String ORIGINAL_EVENT = "net/minecraftforge/event/TickEvent$PlayerTickEvent";
    private static final String NATIVE_EVENT = "net/neoforged/neoforge/event/tick/PlayerTickEvent$";
    private static final String CLASS_NAME = "net/farmingtales/ftboss/boss/ai/FullFightFlightBan";

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static ClassNode read(byte[] payload, int flags) {
        ClassNode node = new ClassNode();
        new ClassReader(payload).accept(node, flags);
        return node;
    }

    private static String map(String name, String phase) {
        if (name.equals(ORIGINAL_EVENT)) return NATIVE_EVENT + phase;
        if (name.equals("net/minecraftforge/fml/common/Mod$EventBusSubscriber")) return "net/neoforged/fml/common/EventBusSubscriber";
        if (name.startsWith("net/minecraftforge/eventbus/api/")) return name.replace("net/minecraftforge/eventbus/api/", "net/neoforged/bus/api/");
        return name;
    }

    private static ClassNode bridge(byte[] original, String phase) {
        ClassNode node = new ClassNode();
        new ClassReader(original).accept(new ClassRemapper(node, new Remapper() {
            @Override public String map(String name) { return FlightBanBoundaryProof.map(name, phase); }
        }), 0);
        node.innerClasses.clear();
        int reads = 0;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETFIELD && field.owner.equals(NATIVE_EVENT + phase) && field.name.equals("player")) {
                    method.instructions.set(field, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, field.owner, "getEntity", "()" + field.desc, false));
                    reads++;
                }
            }
        }
        require(reads == 2, "Original player field must map only to its two native entity getter reads");
        return node;
    }

    private static List<String> instructions(MethodNode method) {
        Textifier text = new Textifier();
        method.instructions.accept(new TraceMethodVisitor(text));
        return text.getText().stream().map(Object::toString).toList();
    }

    private static MethodNode normalized(MethodNode actual) {
        MethodNode restored = new MethodNode(actual.access, actual.name, actual.desc, actual.signature, actual.exceptions.toArray(String[]::new));
        actual.accept(restored);
        for (AbstractInsnNode instruction : restored.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode call && call.owner.startsWith(NATIVE_EVENT) && call.name.equals("getEntity")) {
                require(call.desc.equals("()Lnet/minecraft/world/entity/player/Player;"), "Native entity getter descriptor drift");
                restored.instructions.set(call, new FieldInsnNode(Opcodes.GETFIELD, ORIGINAL_EVENT, "player", "Lnet/minecraft/world/entity/player/Player;"));
            }
        }
        return restored;
    }

    public static void main(String[] arguments) throws Exception {
        byte[] original;
        try (ZipFile source = new ZipFile(arguments[0])) { original = source.getInputStream(source.getEntry(CLASS_NAME + ".class")).readAllBytes(); }
        ClassNode before = read(original, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        ClassNode nativeClass = bridge(original, "Pre");
        ClassNode postClass = bridge(original, "Post");
        nativeClass.methods.add(postClass.methods.stream().filter(method -> method.name.equals("onPlayerTick")).findFirst().orElseThrow());
        ClassWriter writer = new ClassWriter(0);
        nativeClass.accept(writer);
        byte[] payload = writer.toByteArray();
        require(!new String(payload, StandardCharsets.ISO_8859_1).contains("net/minecraftforge/"), "Original Forge API reference remains");
        ClassNode after = read(payload, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        require(after.methods.size() == 3, "Only original constructor and two required native phase bridges are permitted");
        MethodNode originalTick = before.methods.stream().filter(method -> method.name.equals("onPlayerTick")).findFirst().orElseThrow();
        int checked = 0;
        for (MethodNode method : after.methods) {
            new Analyzer<>(new BasicVerifier()).analyze(after.name, method);
            checked++;
            if (method.name.equals("onPlayerTick")) {
                require(instructions(originalTick).equals(instructions(normalized(method))), "Original guard/call/jump/return instructions changed");
                AnnotationNode subscription = method.visibleAnnotations.stream().filter(annotation -> annotation.desc.equals("Lnet/neoforged/bus/api/SubscribeEvent;")).findFirst().orElseThrow();
                int priorityIndex = subscription.values.indexOf("priority");
                String[] priority = (String[]) subscription.values.get(priorityIndex + 1);
                require(priority[0].equals("Lnet/neoforged/bus/api/EventPriority;") && priority[1].equals("LOWEST"), "Original final priority not preserved");
            } else {
                MethodNode constructor = before.methods.stream().filter(item -> item.name.equals(method.name)).findFirst().orElseThrow();
                require(instructions(constructor).equals(instructions(method)), "Original constructor changed");
            }
        }
        require(after.visibleAnnotations.stream().anyMatch(annotation -> annotation.desc.equals("Lnet/neoforged/fml/common/EventBusSubscriber;") && annotation.values.contains("ftboss")), "Native ftboss subscriber metadata absent");
        Path output = Path.of(arguments[1]).resolve(CLASS_NAME + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, payload);
        System.out.println("failures=0;methodsBasicVerified=" + checked + ";originalConstructorPreserved=true;bothNativeTickPhasesPreserveAllOriginalInstructions=true;originalLowestPriorityPreserved=true;onlyForgeToNeoForgeEventApiAdaptation=true;gameplayValuesAndExistingBossClassesChanged=false");
    }
}
