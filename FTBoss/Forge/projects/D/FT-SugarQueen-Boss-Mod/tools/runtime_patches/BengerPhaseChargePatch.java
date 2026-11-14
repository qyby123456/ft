import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class BengerPhaseChargePatch {
    private static final String ENTITY = "net/farmingtales/ftboss/entity/BengerEntity";
    private static final String RENDERER = "net/farmingtales/ftboss/client/render/BengerRenderer";
    private static final Set<String> REPLACED = Set.of("skillTick", "intangTick", "isIntangible", "startCharge", "chargeTick");
    private static final Set<String> ADDED = Set.of("syncIntangibleState", "intangibleParticles", "finishCharge");

    private static ClassNode read(JarFile jar, String name, int flags) throws Exception {
        ClassNode model = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(name + ".class")).readAllBytes()).accept(model, flags);
        return model;
    }

    private static Map<String, MethodNode> methods(ClassNode model) {
        Map<String, MethodNode> result = new HashMap<>();
        for (MethodNode method : model.methods) result.put(method.name + method.desc, method);
        return result;
    }

    private static String text(MethodNode method) {
        if (method == null) return "absent";
        Textifier printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        StringWriter output = new StringWriter();
        printer.print(new PrintWriter(output));
        return output.toString();
    }

    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[3]);
        Files.createDirectories(output);
        var changes = new ArrayList<String>();
        int preserved = 0;
        try (JarFile baseline = new JarFile(arguments[0]); JarFile before = new JarFile(arguments[1]); JarFile after = new JarFile(arguments[2])) {
            for (String name : new String[] {ENTITY, RENDERER}) {
                ClassNode target = read(baseline, name, 0);
                var original = methods(read(baseline, name, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES));
                var prior = methods(read(before, name, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES));
                var next = methods(read(after, name, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES));
                var donors = methods(read(after, name, 0));
                var changedKeys = new java.util.HashSet<String>();
                for (String key : next.keySet()) {
                    MethodNode donor = next.get(key);
                    if (text(prior.get(key)).equals(text(donor))) continue;
                    if (name.equals(ENTITY) && donor.name.equals("m_8119_")) continue;
                    boolean allowed = name.equals(ENTITY) ? REPLACED.contains(donor.name) || ADDED.contains(donor.name)
                            : donor.name.equals("getRenderColor");
                    if (!allowed) throw new IllegalStateException("Unexpected source delta: " + name + ":" + key);
                    if (prior.containsKey(key) && !text(prior.get(key)).equals(text(original.get(key)))) {
                        throw new IllegalStateException("Runtime-only changes in selected method; rebase required: " + name + ":" + key);
                    }
                    if (!prior.containsKey(key) && original.containsKey(key)) throw new IllegalStateException("New method already exists: " + key);
                    target.methods.removeIf(method -> (method.name + method.desc).equals(key));
                    target.methods.add(donors.get(key));
                    changedKeys.add(key);
                    changes.add(name + ":" + key);
                }
                if (name.equals(ENTITY)) {
                    var donorFields = read(after, name, 0).fields;
                    target.fields.removeIf(field -> field.name.equals("CHARGE_MIN"));
                    target.fields.add(donorFields.stream().filter(field -> field.name.equals("CHARGE_MIN")).findFirst().orElseThrow());
                    if (changedKeys.size() != 8) throw new IllegalStateException("Expected five replacements and three helpers");
                    MethodNode tick = target.methods.stream().filter(method -> method.name.equals("m_8119_") && method.desc.equals("()V")).findFirst().orElseThrow();
                    int returns = 0;
                    int installedHooks = 0;
                    for (AbstractInsnNode instruction : tick.instructions.toArray()) {
                        if (instruction instanceof MethodInsnNode call && call.owner.endsWith("/BengerSkillSync")) installedHooks++;
                        if (instruction instanceof MethodInsnNode call && call.name.equals("syncIntangibleState")) throw new IllegalStateException("Already patched");
                        if (instruction.getOpcode() == Opcodes.RETURN) {
                            InsnList hook = new InsnList();
                            hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            hook.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, ENTITY, "syncIntangibleState", "()V", false));
                            tick.instructions.insertBefore(instruction, hook);
                            returns++;
                        }
                    }
                    if (returns != 1 || installedHooks != 2) throw new IllegalStateException("Unexpected tick/hook layout");
                    changedKeys.add("m_8119_()V");
                    changes.add(name + ":m_8119_()V (append synchronization; retain skill hooks)");
                }
                ClassWriter writer = new ClassWriter(0);
                target.accept(writer);
                byte[] bytes = writer.toByteArray();
                StringWriter diagnostics = new StringWriter();
                CheckClassAdapter.verify(new ClassReader(bytes), false, new PrintWriter(diagnostics));
                if (!diagnostics.toString().isBlank()) throw new IllegalStateException(diagnostics.toString());
                ClassNode verified = new ClassNode();
                new ClassReader(bytes).accept(verified, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                var verifiedMethods = methods(verified);
                for (String key : original.keySet()) {
                    if (changedKeys.contains(key)) continue;
                    if (!text(original.get(key)).equals(text(verifiedMethods.get(key)))) throw new IllegalStateException("Unrelated method changed: " + key);
                    preserved++;
                }
                Path destination = output.resolve(name + ".class");
                Files.createDirectories(destination.getParent());
                Files.write(destination, bytes);
            }
        }
        changes.add("preservedUnrelatedMethods=" + preserved);
        Files.write(Path.of(arguments[4]), changes);
        System.out.println("PASS scoped Benger patch: " + (changes.size() - 1) + " changed methods; " + preserved + " unrelated methods retained");
    }
}
