import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class PatchManorArmorAndSupport {
    static final String ROOT = "net/farmingtales/ftboss/";
    static final String ENTITY = ROOT + "entity/EasterBossEntity";
    static final String RULES = ROOT + "util/ManorArmorBalanceRules";
    static final String HOOKS = ROOT + "maque/support/MaqueSupportHooks";
    static final String LAMBDA = "lambda$sparrowTick$0";
    static final String ARMOR = "m_21212_";

    static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    static MethodNode method(ClassNode node, String name, String descriptor) {
        return node.methods.stream().filter(method -> method.name.equals(name) && (descriptor == null || method.desc.equals(descriptor))).reduce((first, second) -> { throw new IllegalStateException("Ambiguous method " + name); }).orElseThrow();
    }

    static byte[] bytes(ClassNode node) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    static AbstractInsnNode previous(AbstractInsnNode node) {
        do { node = node.getPrevious(); } while (node != null && node.getOpcode() < 0);
        return node;
    }

    private static void logSupport(MethodNode method) {
        LdcInsnNode text = null;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof LdcInsnNode literal && literal.cst instanceof String value && value.contains("麻雀支援: 治疗")) {
                check(text == null, "Multiple support log messages");
                text = literal;
            }
        }
        check(text != null, "Support log not found");
        AbstractInsnNode start = previous(text);
        check(start instanceof FieldInsnNode field && field.name.equals("LOGGER"), "Unexpected log start");
        AbstractInsnNode end = text;
        while (!(end instanceof MethodInsnNode call && call.owner.equals("org/slf4j/Logger") && call.name.equals("info"))) end = end.getNext();
        InsnList log = new InsnList();
        FieldInsnNode logger = (FieldInsnNode)start;
        log.add(new FieldInsnNode(logger.getOpcode(), logger.owner, logger.name, logger.desc));
        log.add(new LdcInsnNode("[FTBoss][Easter] 麻雀支援: {} 获得 {} 个随机增益（不再给予百分比治疗）: {}"));
        log.add(new InsnNode(Opcodes.ICONST_3));
        log.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
        log.add(new InsnNode(Opcodes.DUP));
        log.add(new InsnNode(Opcodes.ICONST_0));
        log.add(new FieldInsnNode(Opcodes.GETSTATIC, ENTITY, "NAMES", "[Ljava/lang/String;"));
        log.add(new VarInsnNode(Opcodes.ALOAD, 2));
        log.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, ENTITY, "getVariant", "()I", false));
        log.add(new InsnNode(Opcodes.AALOAD));
        log.add(new InsnNode(Opcodes.AASTORE));
        log.add(new InsnNode(Opcodes.DUP));
        log.add(new InsnNode(Opcodes.ICONST_1));
        log.add(new InsnNode(Opcodes.ICONST_3));
        log.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));
        log.add(new InsnNode(Opcodes.AASTORE));
        log.add(new InsnNode(Opcodes.DUP));
        log.add(new InsnNode(Opcodes.ICONST_2));
        log.add(new VarInsnNode(Opcodes.ALOAD, 6));
        log.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));
        log.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "trim", "()Ljava/lang/String;", false));
        log.add(new InsnNode(Opcodes.AASTORE));
        log.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "org/slf4j/Logger", "info", "(Ljava/lang/String;[Ljava/lang/Object;)V", true));
        method.instructions.insertBefore(start, log);
        AbstractInsnNode following = end.getNext();
        for (AbstractInsnNode instruction = start; instruction != following;) {
            AbstractInsnNode next = instruction.getNext();
            method.instructions.remove(instruction);
            instruction = next;
        }
    }

    static void verifyMethods(ClassNode original, ClassNode updated) {
        ClassNode normalized = DomainVisualMethodMerge.read(bytes(original));
        normalized.fields.stream().filter(field -> field.name.equals("SPARROW_HEAL_PERCENT")).findFirst().orElseThrow().value = 0.0;
        check(Arrays.equals(DomainVisualMethodMerge.shell(normalized), DomainVisualMethodMerge.shell(updated)), "Unexpected field/shell change");
        check(updated.methods.size() == original.methods.size() + 1, "Expected only one armor getter added");
        Set<String> changed = Set.of("sparrowTick", LAMBDA);
        for (MethodNode source : original.methods) {
            MethodNode target = method(updated, source.name, source.desc);
            check(source.access == target.access, "Method ABI changed");
            boolean equal = Arrays.equals(DomainVisualMethodMerge.fingerprint(source), DomainVisualMethodMerge.fingerprint(target));
            check(equal != changed.contains(source.name), "Unexpected method change: " + source.name + source.desc);
        }
    }

    public static void main(String[] arguments) throws Exception {
        Path baseline = Path.of(arguments[0]);
        Path output = Path.of(arguments[1]);
        Path known = Path.of(arguments[2]);
        Path compiled = Path.of(arguments[3]);
        Map<String, byte[]> replacements = new LinkedHashMap<>();
        try (ZipFile current = new ZipFile(baseline.toFile()); ZipFile reference = new ZipFile(known.toFile())) {
            check(current.getEntry(RULES + ".class") == null, "Armor adjustment already installed");
            byte[] before = current.getInputStream(current.getEntry(ENTITY + ".class")).readAllBytes();
            ClassNode original = DomainVisualMethodMerge.read(before);
            ClassNode target = DomainVisualMethodMerge.read(before);
            ClassNode expected = DomainVisualMethodMerge.read(reference.getInputStream(reference.getEntry(ENTITY + ".class")).readAllBytes());
            check(target.methods.stream().noneMatch(method -> method.name.equals(ARMOR)), "Current armor getter changed; refuse blind override");
            for (String name : Set.of("sparrowTick", LAMBDA)) {
                MethodNode method = method(target, name, null);
                check(Arrays.equals(DomainVisualMethodMerge.fingerprint(method), DomainVisualMethodMerge.fingerprint(method(expected, name, method.desc))), "Concurrent scoped change: " + name);
            }
            MethodNode armor = new MethodNode(Opcodes.ACC_PUBLIC, ARMOR, "()I", null, null);
            armor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            armor.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, ENTITY, "getVariant", "()I", false));
            armor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            armor.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, ENTITY, "isMini", "()Z", false));
            armor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            armor.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, ENTITY, "isGiant", "()Z", false));
            armor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            armor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, target.superName, ARMOR, "()I", false));
            armor.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RULES, "armor", "(IZZI)I", false));
            armor.instructions.add(new InsnNode(Opcodes.IRETURN));
            target.methods.add(armor);
            target.fields.stream().filter(field -> field.name.equals("SPARROW_HEAL_PERCENT")).findFirst().orElseThrow().value = 0.0;
            MethodNode support = method(target, "sparrowTick", "()V");
            int calculations = 0;
            for (AbstractInsnNode instruction : support.instructions.toArray()) {
                if (instruction instanceof LdcInsnNode literal && Double.valueOf(0.005).equals(literal.cst)) {
                    AbstractInsnNode start = previous(previous(previous(literal)));
                    check(start instanceof VarInsnNode variable && variable.getOpcode() == Opcodes.ALOAD && variable.var == 2, "Unexpected healing target");
                    check(previous(literal).getOpcode() == Opcodes.F2D, "Unexpected percent conversion");
                    AbstractInsnNode end = literal.getNext().getNext();
                    check(literal.getNext().getOpcode() == Opcodes.DMUL && end.getOpcode() == Opcodes.D2F, "Unexpected heal calculation");
                    support.instructions.insertBefore(start, new InsnNode(Opcodes.FCONST_0));
                    AbstractInsnNode following = end.getNext();
                    for (AbstractInsnNode remove = start; remove != following;) {
                        AbstractInsnNode next = remove.getNext();
                        support.instructions.remove(remove);
                        remove = next;
                    }
                    calculations++;
                }
            }
            check(calculations == 1, "Expected one percent-heal calculation");
            logSupport(support);
            MethodNode lambda = method(target, LAMBDA, null);
            int removed = 0;
            for (AbstractInsnNode instruction : lambda.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals(HOOKS) && call.name.equals("heal")) {
                    InsnList discard = new InsnList();
                    for (int index = 0; index < 4; index++) discard.add(new InsnNode(Opcodes.POP));
                    lambda.instructions.insertBefore(call, discard);
                    lambda.instructions.remove(call);
                    removed++;
                }
            }
            check(removed == 1, "Expected exactly one direct percent-heal call");
            byte[] after = bytes(target);
            verifyMethods(original, DomainVisualMethodMerge.read(after));
            replacements.put(ENTITY + ".class", after);
            replacements.put(RULES + ".class", Files.readAllBytes(compiled.resolve(RULES + ".class")));
            try (ZipOutputStream destination = new ZipOutputStream(Files.newOutputStream(output))) {
                Set<String> seen = new HashSet<>();
                var entries = current.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    check(seen.add(entry.getName()), "Duplicate ZIP entry");
                    ZipEntry copy = new ZipEntry(entry.getName());
                    copy.setTime(entry.getTime());
                    destination.putNextEntry(copy);
                    destination.write(replacements.containsKey(entry.getName()) ? replacements.get(entry.getName()) : current.getInputStream(entry).readAllBytes());
                    destination.closeEntry();
                }
                destination.putNextEntry(new ZipEntry(RULES + ".class"));
                destination.write(replacements.get(RULES + ".class"));
                destination.closeEntry();
            }
        }
        int preserved = 0;
        try (ZipFile before = new ZipFile(baseline.toFile()); ZipFile after = new ZipFile(output.toFile())) {
            check(after.size() == before.size() + 1, "Only one new rule class expected");
            var entries = before.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                check(after.getEntry(entry.getName()) != null, "Missing entry");
                if (!entry.getName().equals(ENTITY + ".class")) {
                    check(Arrays.equals(before.getInputStream(entry).readAllBytes(), after.getInputStream(after.getEntry(entry.getName())).readAllBytes()), "Unrelated entry changed: " + entry.getName());
                    preserved++;
                }
            }
        }
        Files.writeString(output.resolveSibling(output.getFileName() + ".audit.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("unrelatedEntriesPreserved", preserved, "addedClass", RULES, "armorScale", 0.5, "percentHealRemoved", true, "buffsCleanseAndSharedHealHookPreserved", true)));
        System.out.println("PASS: total armor halved, direct sparrow percent heal removed; " + preserved + " unrelated entries preserved");
    }
}
