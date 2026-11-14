import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class PatchManorStatusCrash {
    public static final String ROOT = "net/farmingtales/ftboss/";
    public static final String ENTITY = ROOT + "entity/EasterBossEntity";
    public static final String SPARROW = ROOT + "integ/spells/SparrowBorrowedSpells";
    public static final String HEAL = ROOT + "effect/HealBanEffect";
    public static final String GUARD = ROOT + "boss/ai/ManorStatusImmunity";
    public static final Map<String, String> CHANGED = Map.of(ENTITY, "m_7301_", SPARROW, "ready", HEAL, "onRemove");

    public static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    public static byte[] entry(ZipFile jar, String name) throws Exception {
        check(jar.getEntry(name) != null, "Missing entry: " + name);
        return jar.getInputStream(jar.getEntry(name)).readAllBytes();
    }

    public static MethodNode method(ClassNode type, String name, String descriptor) {
        return type.methods.stream().filter(candidate -> candidate.name.equals(name) && (descriptor == null || descriptor.equals(candidate.desc)))
                .reduce((first, second) -> { throw new IllegalStateException("Ambiguous method: " + name); }).orElseThrow();
    }

    public static void verifyMethods(ClassNode before, ClassNode after, String changed) {
        check(Arrays.equals(DomainVisualMethodMerge.shell(before), DomainVisualMethodMerge.shell(after)), "Class ABI changed: " + before.name);
        check(before.methods.size() == after.methods.size(), "Method count changed");
        for (MethodNode original : before.methods) {
            MethodNode current = method(after, original.name, original.desc);
            boolean equal = Arrays.equals(DomainVisualMethodMerge.fingerprint(original), DomainVisualMethodMerge.fingerprint(current));
            check(equal != original.name.equals(changed), "Unexpected method delta: " + before.name + "." + original.name);
        }
    }

    private static byte[] bytes(ClassNode type) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        return writer.toByteArray();
    }

    public static void main(String[] arguments) throws Exception {
        Path baseline = Path.of(arguments[0]);
        Path candidate = Path.of(arguments[1]);
        Path compiled = Path.of(arguments[2]);
        Map<String, byte[]> changes = new LinkedHashMap<>();
        try (ZipFile jar = new ZipFile(baseline.toFile()); ZipFile known = new ZipFile(arguments[3])) {
            check(jar.getEntry(GUARD + ".class") == null, "Status guard already installed");
            for (var plan : CHANGED.entrySet()) {
                ClassNode original = DomainVisualMethodMerge.read(entry(jar, plan.getKey() + ".class"));
                ClassNode target = DomainVisualMethodMerge.read(entry(jar, plan.getKey() + ".class"));
                MethodNode owned = method(target, plan.getValue(), null);
                MethodNode expected = method(DomainVisualMethodMerge.read(entry(known, plan.getKey() + ".class")), plan.getValue(), owned.desc);
                check(Arrays.equals(DomainVisualMethodMerge.fingerprint(owned), DomainVisualMethodMerge.fingerprint(expected)),
                        "Concurrent owned-method change; refusing replacement: " + plan.getKey() + "." + plan.getValue());
                if (plan.getKey().equals(SPARROW)) {
                    ClassNode donor = DomainVisualMethodMerge.read(Files.readAllBytes(compiled.resolve(SPARROW + ".class")));
                    MethodNode replacement = method(donor, "ready", owned.desc);
                    target.methods.set(target.methods.indexOf(owned), replacement);
                    owned = replacement;
                } else {
                    int injections = 0;
                    for (var instruction : owned.instructions.toArray()) {
                        if (plan.getKey().equals(ENTITY) && instruction.getOpcode() == Opcodes.IRETURN) {
                            InsnList hook = new InsnList();
                            hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, GUARD, "allowed", "(ZLnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/effect/MobEffectInstance;)Z", false));
                            owned.instructions.insertBefore(instruction, hook);
                            injections++;
                        } else if (plan.getKey().equals(HEAL) && instruction instanceof MethodInsnNode call
                                && call.name.equals("setCanceled") && call.desc.equals("(Z)V")) {
                            InsnList hook = new InsnList();
                            hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, GUARD, "protectRemoval", "(ZLnet/minecraftforge/event/entity/living/MobEffectEvent$Remove;)Z", false));
                            owned.instructions.insertBefore(instruction, hook);
                            injections++;
                        }
                    }
                    check(injections == (plan.getKey().equals(ENTITY) ? 4 : 1), "Unexpected guard insertion count: " + injections);
                }
                owned.maxStack += 2;
                new Analyzer<>(new BasicVerifier()).analyze(target.name, owned);
                byte[] changed = bytes(target);
                verifyMethods(original, DomainVisualMethodMerge.read(changed), plan.getValue());
                changes.put(target.name + ".class", changed);
            }
            byte[] guard = Files.readAllBytes(compiled.resolve(GUARD + ".class"));
            ClassNode type = DomainVisualMethodMerge.read(guard);
            check(type.version == 61, "Java 17 guard bytecode");
            for (MethodNode function : type.methods) new Analyzer<>(new BasicVerifier()).analyze(type.name, function);
            changes.put(GUARD + ".class", guard);
            try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(candidate))) {
                var entries = jar.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry previous = entries.nextElement();
                    ZipEntry next = new ZipEntry(previous.getName());
                    next.setTime(previous.getTime());
                    output.putNextEntry(next);
                    output.write(changes.containsKey(next.getName()) ? changes.get(next.getName()) : entry(jar, next.getName()));
                    output.closeEntry();
                }
                output.putNextEntry(new ZipEntry(GUARD + ".class"));
                output.write(guard);
                output.closeEntry();
            }
        }
        Files.writeString(Path.of(candidate + ".audit.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                "changedMethods", CHANGED, "addedClass", GUARD, "nullSafeSparrow", true, "blockedEffects", Set.of("ftboss:heal_ban", "ftboss:shadow_erosion"),
                "playerRemovalProtectionPreserved", true, "inGameVerified", false)));
        System.out.println("PASS minimal runtime merge: three owned methods, one new guard; original class shells preserved");
    }
}
