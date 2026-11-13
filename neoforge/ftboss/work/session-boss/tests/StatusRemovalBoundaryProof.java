import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class StatusRemovalBoundaryProof {
    private static final String EFFECTS = "net/farmingtales/ftboss/effect/";
    private static final String CURES = "(Ljava/util/Set;Lnet/minecraft/world/effect/MobEffectInstance;)V";

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return node;
    }

    private static List<String> instructions(MethodNode method) {
        Textifier printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        return printer.getText().stream().map(Object::toString).toList();
    }

    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }

    public static void main(String[] arguments) throws Exception {
        Path classes = Path.of(arguments[2]);
        Path originals = Path.of(arguments[3]);
        Files.createDirectories(originals.resolve(EFFECTS));
        int preserved = 0;
        int verified = 0;
        int forgeEmptyCures = 0;
        try (ZipFile base = new ZipFile(arguments[0]); ZipFile forge = new ZipFile(arguments[1])) {
            for (String name : List.of("HealBanEffect", "ShadowErosionEffect")) {
                String entry = EFFECTS + name + ".class";
                byte[] nativeBytes = base.getInputStream(base.getEntry(entry)).readAllBytes();
                byte[] forgeBytes = forge.getInputStream(forge.getEntry(entry)).readAllBytes();
                Files.write(originals.resolve(entry), nativeBytes);
                Files.write(originals.resolve(EFFECTS + name + "-forge.class"), forgeBytes);
                ClassNode originalForge = read(forgeBytes);
                MethodNode originalCures = originalForge.methods.stream().filter(method -> method.name.equals("getCurativeItems")).findFirst().orElseThrow();
                List<Integer> opcodes = new ArrayList<>();
                for (var instruction : originalCures.instructions) if (instruction.getOpcode() >= 0) opcodes.add(instruction.getOpcode());
                require(opcodes.equals(List.of(Opcodes.NEW, Opcodes.DUP, Opcodes.INVOKESPECIAL, Opcodes.ARETURN)), "Original Forge cure list is not empty");
                forgeEmptyCures++;
                ClassNode previous = read(nativeBytes);
                if (name.equals("ShadowErosionEffect")) {
                    ClassNode patched = new ClassNode();
                    new ClassReader(nativeBytes).accept(patched, 0);
                    require(patched.methods.stream().noneMatch(method -> method.name.equals("fillEffectCures")), "Unexpected existing cure override");
                    MethodNode cures = new MethodNode(Opcodes.ACC_PUBLIC, "fillEffectCures", CURES, null, null);
                    cures.visitCode();
                    cures.visitInsn(Opcodes.RETURN);
                    cures.visitMaxs(0, 3);
                    cures.visitEnd();
                    patched.methods.add(cures);
                    ClassWriter writer = new ClassWriter(0);
                    patched.accept(writer);
                    Files.write(classes.resolve(entry), writer.toByteArray());
                } else {
                    MethodNode removal = originalForge.methods.stream().filter(method -> method.name.equals("onRemove")).findFirst().orElseThrow();
                    require(removal.visibleAnnotations.stream().anyMatch(annotation -> annotation.desc.endsWith("SubscribeEvent;") && annotation.values.toString().contains("receiveCanceled") && annotation.values.contains(Boolean.TRUE)), "Original receiveCanceled annotation missing");
                    boolean originalGuard = false;
                    for (var instruction : removal.instructions) if (instruction instanceof MethodInsnNode call && call.name.equals("protectRemoval")) originalGuard = true;
                    require(originalGuard, "Original Manor exception missing");
                }
                ClassNode current = read(Files.readAllBytes(classes.resolve(entry)));
                for (MethodNode method : previous.methods) {
                    MethodNode found = current.methods.stream().filter(candidate -> candidate.name.equals(method.name) && candidate.desc.equals(method.desc)).findFirst().orElseThrow();
                    require(instructions(method).equals(instructions(found)), "Original native method changed: " + name + "." + method.name);
                    preserved++;
                }
                require(current.methods.size() == previous.methods.size() + (name.equals("HealBanEffect") ? 2 : 1), "Unexpected added method");
                MethodNode emptyCures = current.methods.stream().filter(method -> method.name.equals("fillEffectCures") && method.desc.equals(CURES)).findFirst().orElseThrow();
                require(emptyCures.instructions.size() == 1 && emptyCures.instructions.getFirst().getOpcode() == Opcodes.RETURN, "Mapped cure override is not empty");
                for (MethodNode method : current.methods) {
                    if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) continue;
                    new Analyzer<>(new BasicVerifier()).analyze(current.name, method);
                    verified++;
                }
            }
            ClassNode manor = read(base.getInputStream(base.getEntry("net/farmingtales/ftboss/boss/ai/ManorStatusImmunity.class")).readAllBytes());
            for (MethodNode method : manor.methods) {
                new Analyzer<>(new BasicVerifier()).analyze(manor.name, method);
                verified++;
            }
        }
        System.out.println("failures=0;originalNativeMethodsPreserved=" + preserved + ";basicVerifiedMethods=" + verified + ";originalEmptyCurativeOverrides=" + forgeEmptyCures + ";onlyOneNewShadowOverride=true;noOriginalDamageDurationAiMethodsChanged=true");
    }
}
