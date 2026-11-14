import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class SparrowZhuniBalancePatch {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static final String HELPER = PREFIX + "integ/spells/ZhuniSpellShield";

    private static ClassNode read(byte[] bytes) {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, 0);
        return type;
    }

    private static void save(ClassNode type, Path payload) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        byte[] bytes = writer.toByteArray();
        ClassNode checked = read(bytes);
        for (MethodNode method : checked.methods) {
            if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0)
                new Analyzer<>(new BasicVerifier()).analyze(checked.name, method);
        }
        Path output = payload.resolve(type.name + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, bytes);
    }

    private static void expect(int actual, int expected, String label) {
        if (actual != expected) throw new IllegalStateException(label + ": expected " + expected + ", got " + actual);
    }

    private static AbstractInsnNode previous(AbstractInsnNode instruction) {
        do { instruction = instruction.getPrevious(); } while (instruction != null && instruction.getOpcode() < 0);
        return instruction;
    }

    private static InsnList helper(ClassNode bird) {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new FieldInsnNode(Opcodes.GETFIELD, bird.name, "spellShield", "L" + HELPER + ";"));
        return code;
    }

    public static void main(String[] arguments) throws Exception {
        Path compiled = Path.of(arguments[1]);
        Path payload = Path.of(arguments[3]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        try (ZipFile baseline = new ZipFile(arguments[0])) {
            for (String name : new String[]{"util/SparrowAppearanceRules", "integ/spells/ZhuniSpellShield"}) {
                ClassNode type = new ClassNode();
                new ClassReader(Files.readAllBytes(compiled.resolve(PREFIX + name + ".class")))
                        .accept(new ClassRemapper(type, names), 0);
                save(type, payload);
            }
            for (String name : new String[]{"boss/ai/ManorRaidRules", "boss/ai/ManorAdaptiveShield", "boss/ManorFightData",
                    "turret/XuanlanTurretEntity", "turret/ZhuniEntity"}) {
                ClassNode type = read(baseline.getInputStream(baseline.getEntry(PREFIX + name + ".class")).readAllBytes());
                int changes = 0;
                if (name.endsWith("ManorRaidRules")) {
                    for (FieldNode field : type.fields) if (field.name.equals("ADAPTIVE_REDUCTION")) field.value = 0.10F;
                    for (MethodNode method : type.methods) for (AbstractInsnNode instruction : method.instructions)
                        if (method.name.equals("adaptiveDamage") && instruction instanceof LdcInsnNode literal
                                && Float.valueOf(0.95F).equals(literal.cst)) { literal.cst = 0.90F; changes++; }
                    expect(changes, 1, name);
                } else if (name.endsWith("ManorAdaptiveShield")) {
                    for (MethodNode method : type.methods) for (AbstractInsnNode instruction : method.instructions)
                        if (instruction instanceof LdcInsnNode literal && literal.cst instanceof String text
                                && text.contains("[FTBoss][麻雀适应性护盾]") && text.contains("5%")) {
                            literal.cst = text.replace("5%", "10%"); changes++;
                        }
                    expect(changes, 1, name);
                } else if (name.endsWith("ManorFightData")) {
                    for (MethodNode method : type.methods) for (AbstractInsnNode instruction : method.instructions)
                        if (instruction instanceof IntInsnNode integer && integer.operand == 0xdff) {
                            integer.operand = 0xdbf; changes++;
                        }
                    expect(changes, 2, name);
                } else if (name.endsWith("XuanlanTurretEntity")) {
                    for (MethodNode method : type.methods) for (AbstractInsnNode instruction : method.instructions)
                        if ((method.name.equals("<init>") || method.name.equals("attributes")) && instruction instanceof LdcInsnNode literal) {
                            if (Float.valueOf(150).equals(literal.cst)) { literal.cst = 750.0F; changes++; }
                            else if (Double.valueOf(150).equals(literal.cst)) { literal.cst = 750.0D; changes++; }
                        }
                    expect(changes, 2, name);
                    type.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                            "MAX_HEALTH", "F", null, 750.0F));
                } else {
                    if (type.fields.stream().anyMatch(field -> field.name.equals("spellShield")))
                        throw new IllegalStateException("Already patched");
                    type.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "spellShield", "L" + HELPER + ";", null, null));
                    for (MethodNode method : type.methods) for (AbstractInsnNode instruction : method.instructions.toArray()) {
                        if (method.name.equals("<init>") && instruction instanceof FieldInsnNode field
                                && field.getOpcode() == Opcodes.PUTFIELD && field.name.equals("turretDrops")) {
                            InsnList code = new InsnList();
                            code.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            code.add(new TypeInsnNode(Opcodes.NEW, HELPER));
                            code.add(new InsnNode(Opcodes.DUP));
                            code.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, HELPER, "<init>", "()V", false));
                            code.add(new FieldInsnNode(Opcodes.PUTFIELD, type.name, "spellShield", "L" + HELPER + ";"));
                            method.instructions.insert(instruction, code); changes++;
                        }
                        if (!(instruction instanceof MethodInsnNode call) || !call.owner.equals(PREFIX + "turret/ZhuniTurretDrops")) continue;
                        if (call.name.equals("revealedAt")) {
                            InsnList code = helper(type);
                            code.add(new VarInsnNode(Opcodes.LLOAD, 1));
                            code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, HELPER, "reset", "(J)V", false));
                            method.instructions.insert(call, code); changes++;
                        } else if (call.name.equals("tick")) {
                            if (!(previous(call) instanceof VarInsnNode level) || !(previous(level) instanceof VarInsnNode boss))
                                throw new IllegalStateException("Unexpected turret tick locals");
                            InsnList code = helper(type);
                            code.add(new VarInsnNode(Opcodes.ALOAD, boss.var));
                            code.add(new VarInsnNode(Opcodes.ALOAD, level.var));
                            code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, HELPER, "tick", call.desc, false));
                            method.instructions.insert(call, code); changes++;
                        }
                    }
                    expect(changes, 3, name);
                    ClassNode source = new ClassNode();
                    new ClassReader(Files.readAllBytes(compiled.resolve(type.name + ".class"))).accept(new ClassRemapper(source, names), 0);
                    String remove = names.mapMethodName("net/minecraft/world/entity/Entity", "remove", "(Lnet/minecraft/world/entity/Entity$RemovalReason;)V");
                    MethodNode removal = source.methods.stream().filter(method -> method.name.equals(remove)).findFirst().orElseThrow();
                    if (type.methods.stream().anyMatch(method -> method.name.equals(remove))) throw new IllegalStateException("Existing remove override");
                    type.methods.add(removal);
                }
                save(type, payload);
            }
        }
        System.out.println("PASS: seven-class surgical patch, verified JVM stacks; all unrelated runtime code retained");
    }
}
