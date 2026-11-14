import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class PatchManorEntryBuffs {
    static final String ROOT = "net/farmingtales/ftboss/";
    static final String ENTITY = ROOT + "entity/EasterBossEntity";
    static final String INTRO = ROOT + "boss/HiddenBossIntro";
    static final String FIGHT = ROOT + "boss/ManorFight";
    static final String HOOK = "prepareManorEntryBuffs";
    static final String START = "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;ILjava/util/UUID;L" + ROOT + "boss/ManorDifficulty;[Ljava/lang/String;)V";

    static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    static MethodNode method(ClassNode node, String name, String descriptor) {
        return node.methods.stream().filter(method -> method.name.equals(name) && (descriptor == null || method.desc.equals(descriptor))).reduce((first, second) -> { throw new IllegalStateException("Ambiguous method " + name); }).orElseThrow();
    }

    static AbstractInsnNode previous(AbstractInsnNode node) {
        do { node = node.getPrevious(); } while (node != null && node.getOpcode() < 0);
        return node;
    }

    static byte[] bytes(ClassNode node) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    static void verifyUnrelatedMethods(ClassNode before, ClassNode after, Set<String> allowed, boolean addedHook) {
        check(Arrays.equals(DomainVisualMethodMerge.shell(before), DomainVisualMethodMerge.shell(after)), "Class shell changed: " + before.name);
        check(after.methods.size() == before.methods.size() + (addedHook ? 1 : 0), "Unexpected method count");
        for (MethodNode original : before.methods) {
            MethodNode updated = method(after, original.name, original.desc);
            check(original.access == updated.access, "Method access changed");
            boolean equal = Arrays.equals(DomainVisualMethodMerge.fingerprint(original), DomainVisualMethodMerge.fingerprint(updated));
            check(equal != allowed.contains(original.name + original.desc), "Unexpected method delta: " + before.name + "." + original.name + original.desc);
        }
    }

    private static void expectCurrentMethod(ClassNode current, ClassNode expected, MethodNode target) {
        check(Arrays.equals(DomainVisualMethodMerge.fingerprint(target), DomainVisualMethodMerge.fingerprint(method(expected, target.name, target.desc))), "Concurrent scoped method change: " + current.name + "." + target.name);
        check(target.instructions.iterator().hasNext(), "Empty target");
    }

    private static void beforeAdd(MethodNode method) {
        int count = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode call && call.name.equals("m_7967_") && call.owner.equals("net/minecraft/server/level/ServerLevel")) {
                InsnList hook = new InsnList();
                hook.add(new InsnNode(Opcodes.DUP));
                hook.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, ENTITY, HOOK, "()V", false));
                method.instructions.insertBefore(call, hook);
                count++;
            }
        }
        check(count == 1, "Expected exactly one world-entry point");
    }

    private static void beforeRelease(MethodNode method, String releaseName) {
        int count = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(ENTITY) && call.name.equals(releaseName)) {
                AbstractInsnNode flag = previous(call);
                AbstractInsnNode receiver = previous(flag);
                check(flag.getOpcode() == Opcodes.ICONST_0 && receiver instanceof VarInsnNode && receiver.getOpcode() == Opcodes.ALOAD, "Unexpected release stack");
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, ((VarInsnNode)receiver).var));
                hook.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, ENTITY, HOOK, "()V", false));
                method.instructions.insertBefore(receiver, hook);
                count++;
            }
        }
        check(count == 1, "Expected exactly one release point: " + releaseName);
    }

    public static void main(String[] arguments) throws Exception {
        Path baseline = Path.of(arguments[0]);
        Path candidate = Path.of(arguments[1]);
        Path known = Path.of(arguments[2]);
        Map<String, byte[]> replacements = new LinkedHashMap<>();
        try (ZipFile current = new ZipFile(baseline.toFile()); ZipFile reference = new ZipFile(known.toFile())) {
            for (String owner : List.of(ENTITY, INTRO, FIGHT)) {
                String entry = owner + ".class";
                ClassNode before = DomainVisualMethodMerge.read(current.getInputStream(current.getEntry(entry)).readAllBytes());
                ClassNode target = DomainVisualMethodMerge.read(current.getInputStream(current.getEntry(entry)).readAllBytes());
                ClassNode expected = DomainVisualMethodMerge.read(reference.getInputStream(reference.getEntry(entry)).readAllBytes());
                Set<String> changed = new HashSet<>();
                if (owner.equals(ENTITY)) {
                    check(target.methods.stream().noneMatch(method -> method.name.equals(HOOK)), "Entry hook already installed");
                    expectCurrentMethod(target, expected, method(target, "syncPlayerBuffs", "()V"));
                    MethodNode hook = new MethodNode(Opcodes.ACC_PUBLIC, HOOK, "()V", null, null);
                    hook.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, ENTITY, "syncPlayerBuffs", "()V", false));
                    hook.instructions.add(new InsnNode(Opcodes.RETURN));
                    target.methods.add(hook);
                } else if (owner.equals(INTRO)) {
                    MethodNode start = method(target, "start", START);
                    MethodNode pump = method(target, "pumpAll", "()V");
                    expectCurrentMethod(target, expected, start);
                    expectCurrentMethod(target, expected, pump);
                    beforeAdd(start);
                    beforeRelease(pump, "m_20331_");
                    changed.add(start.name + start.desc);
                    changed.add(pump.name + pump.desc);
                } else {
                    MethodNode spawn = method(target, "spawnVariant", null);
                    expectCurrentMethod(target, expected, spawn);
                    beforeRelease(spawn, "m_21557_");
                    changed.add(spawn.name + spawn.desc);
                }
                byte[] result = bytes(target);
                verifyUnrelatedMethods(before, DomainVisualMethodMerge.read(result), changed, owner.equals(ENTITY));
                replacements.put(entry, result);
            }
            try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(candidate))) {
                Set<String> seen = new HashSet<>();
                var entries = current.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    check(seen.add(entry.getName()), "Duplicate ZIP entry");
                    ZipEntry copy = new ZipEntry(entry.getName());
                    copy.setTime(entry.getTime());
                    output.putNextEntry(copy);
                    output.write(replacements.containsKey(entry.getName()) ? replacements.get(entry.getName()) : current.getInputStream(entry).readAllBytes());
                    output.closeEntry();
                }
            }
        }
        int preserved = 0;
        try (ZipFile before = new ZipFile(baseline.toFile()); ZipFile after = new ZipFile(candidate.toFile())) {
            check(before.size() == after.size(), "ZIP entry count changed");
            var entries = before.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                check(after.getEntry(entry.getName()) != null, "Missing ZIP entry");
                if (!replacements.containsKey(entry.getName())) {
                    check(Arrays.equals(before.getInputStream(entry).readAllBytes(), after.getInputStream(after.getEntry(entry.getName())).readAllBytes()), "Unrelated entry changed: " + entry.getName());
                    preserved++;
                }
            }
        }
        Files.writeString(candidate.resolveSibling(candidate.getFileName() + ".audit.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("unrelatedEntriesPreserved", preserved, "changedClasses", replacements.keySet(), "existingCopyLogicUnchanged", true, "introTimelineUnchanged", true)));
        System.out.println("PASS: three entry points patched, original copy logic preserved; " + preserved + " unrelated JAR entries preserved");
    }
}
