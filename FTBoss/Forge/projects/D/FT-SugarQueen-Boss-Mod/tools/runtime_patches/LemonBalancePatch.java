import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.zip.ZipFile;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class LemonBalancePatch implements Opcodes {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static final String BALANCE = PREFIX + "boss/ai/LemonCombatBalance";
    private static final String SACRIFICE = PREFIX + "boss/ai/LemonSacrifice";
    private static final String EASTER = PREFIX + "entity/EasterBossEntity";

    private static ClassNode read(byte[] bytes) {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, 0);
        return type;
    }

    private static void save(ClassNode type, Path payload) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        byte[] bytes = writer.toByteArray();
        for (MethodNode method : type.methods) {
            if ((method.access & (ACC_ABSTRACT | ACC_NATIVE)) == 0)
                new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
        }
        Path output = payload.resolve(type.name + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, bytes);
    }

    private static void expect(int actual, int expected, String label) {
        if (actual != expected) throw new IllegalStateException(label + ": " + actual + " != " + expected);
    }

    public static void main(String[] arguments) throws Exception {
        Path compiled = Path.of(arguments[1]);
        Path payload = Path.of(arguments[3]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        try (ZipFile baseline = new ZipFile(arguments[0]); var files = Files.walk(compiled)) {
            for (Path file : files.filter(candidate -> candidate.toString().endsWith(".class")).toList()) {
                ClassNode updated = new ClassNode();
                new ClassReader(Files.readAllBytes(file)).accept(new ClassRemapper(updated, names), 0);
                if (updated.name.equals(SACRIFICE)) {
                    ClassNode original = read(baseline.getInputStream(baseline.getEntry(SACRIFICE + ".class")).readAllBytes());
                    Set<String> changed = Set.of("requiredBosses", "summonCount", "isSacrificeable", "countsAsSummonedBoss", "trySacrifice");
                    for (MethodNode method : updated.methods) {
                        if (!changed.contains(method.name) && !method.name.startsWith("lambda$trySacrifice$")) continue;
                        original.methods.removeIf(before -> before.name.equals(method.name) && before.desc.equals(method.desc));
                        original.methods.add(method);
                    }
                    for (FieldNode field : original.fields) if (field.name.equals("MIN_SUMMONED_BOSSES")) field.value = 3;
                    save(original, payload);
                } else save(updated, payload);
            }
            ClassNode entity = read(baseline.getInputStream(baseline.getEntry(EASTER + ".class")).readAllBytes());
            int healing = 0;
            int threshold = 0;
            int refill = 0;
            for (MethodNode method : entity.methods) {
                if (method.name.equals("m_5634_") && method.desc.equals("(F)V")) {
                    for (AbstractInsnNode instruction : method.instructions.toArray()) {
                        if (!(instruction instanceof MethodInsnNode call) || call.getOpcode() != INVOKESPECIAL
                                || !call.name.equals("m_5634_")) continue;
                        int before = method.maxLocals++;
                        InsnList prefix = new InsnList();
                        prefix.add(new InsnNode(POP2));
                        prefix.add(new VarInsnNode(ALOAD, 0));
                        prefix.add(new VarInsnNode(FLOAD, 1));
                        prefix.add(new MethodInsnNode(INVOKESTATIC, BALANCE, "limitHealing", "(L" + EASTER + ";F)F", false));
                        prefix.add(new VarInsnNode(FSTORE, 1));
                        prefix.add(new VarInsnNode(ALOAD, 0));
                        prefix.add(new MethodInsnNode(INVOKEVIRTUAL, EASTER, "m_21223_", "()F", false));
                        prefix.add(new VarInsnNode(FSTORE, before));
                        prefix.add(new VarInsnNode(ALOAD, 0));
                        prefix.add(new VarInsnNode(FLOAD, 1));
                        method.instructions.insertBefore(call, prefix);
                        InsnList after = new InsnList();
                        after.add(new VarInsnNode(ALOAD, 0));
                        after.add(new VarInsnNode(FLOAD, before));
                        after.add(new MethodInsnNode(INVOKESTATIC, BALANCE, "recordHealing", "(L" + EASTER + ";F)V", false));
                        method.instructions.insert(call, after);
                        healing++;
                    }
                }
                if (method.name.equals("tryHalfHealthLemonSacrifice")) {
                    for (AbstractInsnNode instruction : method.instructions.toArray()) {
                        if (instruction instanceof MethodInsnNode call && call.owner.equals(PREFIX + "lemonparty/LemonPartyRules")
                                && call.name.equals("absorbMinimum")) {
                            call.owner = BALANCE;
                            call.name = "requiredBosses";
                            threshold++;
                        }
                        if (instruction instanceof JumpInsnNode jump && jump.getOpcode() == IF_ICMPGE
                                && instruction.getPrevious().getOpcode() == ICONST_2) {
                            AbstractInsnNode oldCount = instruction.getPrevious();
                            InsnList count = new InsnList();
                            count.add(new VarInsnNode(ALOAD, 0));
                            count.add(new FieldInsnNode(GETFIELD, EASTER, "lemonSacrifice", "L" + SACRIFICE + ";"));
                            count.add(new MethodInsnNode(INVOKEVIRTUAL, SACRIFICE, "requiredBosses", "()I", false));
                            method.instructions.insertBefore(oldCount, count);
                            method.instructions.remove(oldCount);
                            threshold++;
                        }
                        if (instruction instanceof LdcInsnNode literal && literal.cst instanceof String text && text.contains("已立即召齐两位"))
                            literal.cst = text.replace("已立即召齐两位", "已按参战人数召齐");
                    }
                }
                if (method.name.equals("skillSummonFoodBosses")) {
                    for (AbstractInsnNode instruction : method.instructions.toArray()) {
                        if (instruction instanceof MethodInsnNode call && call.owner.equals("java/util/List") && call.name.equals("isEmpty")) {
                            InsnList refillPool = new InsnList();
                            refillPool.add(new InsnNode(DUP));
                            refillPool.add(new MethodInsnNode(INVOKESTATIC, BALANCE, "refillBossPool", "(Ljava/util/List;)V", false));
                            method.instructions.insertBefore(call, refillPool);
                            refill++;
                        }
                    }
                }
            }
            expect(healing, 1, "healing hook");
            expect(threshold, 1, "half health threshold");
            expect(refill, 1, "food boss pool refill");
            save(entity, payload);
        }
        System.out.println("PASS: targeted Lemon methods patched; unrelated entity methods preserved");
    }
}
