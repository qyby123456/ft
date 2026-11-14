import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class QizaiUltimateTimingPatch {
    private static final String ROOT = "net/farmingtales/ftboss/";

    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(ROOT + name + ".class")).readAllBytes()).accept(node, 0);
        return node;
    }

    private static void save(ClassNode node, Path payload) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Path output = payload.resolve(node.name + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
    }

    private static AbstractInsnNode next(AbstractInsnNode node) {
        do { node = node.getNext(); } while (node != null && node.getOpcode() < 0);
        return node;
    }

    public static void main(String[] arguments) throws Exception {
        Path payload = Path.of(arguments[2]);
        try (JarFile baseline = new JarFile(arguments[0])) {
            ClassNode rules = read(baseline, "util/ManorSpellCastRules");
            ClassNode donor = new ClassNode();
            new ClassReader(Files.readAllBytes(Path.of(arguments[1]).resolve(ROOT + "util/ManorSpellCastRules.class"))).accept(donor, 0);
            MethodNode replacement = donor.methods.stream().filter(method -> method.name.equals("castTicks")).findFirst().orElseThrow();
            int slot = rules.methods.indexOf(rules.methods.stream().filter(method -> method.name.equals("castTicks")).findFirst().orElseThrow());
            rules.methods.set(slot, replacement);
            save(rules, payload);

            ClassNode kinds = read(baseline, "util/ManorSpellRules$Kind");
            MethodNode initializer = kinds.methods.stream().filter(method -> method.name.equals("<clinit>")).findFirst().orElseThrow();
            int patched = 0;
            for (var instruction : initializer.instructions.toArray()) {
                if (!(instruction instanceof LdcInsnNode constant) || !constant.cst.equals("qizai_star_inferno")) continue;
                AbstractInsnNode school = next(instruction);
                AbstractInsnNode damage = next(school);
                AbstractInsnNode mana = next(damage);
                AbstractInsnNode cooldown = next(mana);
                AbstractInsnNode cast = next(cooldown);
                if (!(mana instanceof IntInsnNode manaValue) || !List.of(180, 600).contains(manaValue.operand))
                    throw new IllegalStateException("Unexpected inferno mana instruction");
                if (!(cooldown instanceof IntInsnNode integer) || !List.of(45, 90, 120).contains(integer.operand))
                    throw new IllegalStateException("Unexpected inferno cooldown instruction");
                initializer.instructions.set(mana, new IntInsnNode(Opcodes.SIPUSH, 600));
                initializer.instructions.set(cooldown, new IntInsnNode(Opcodes.BIPUSH, 90));
                initializer.instructions.set(cast, new IntInsnNode(Opcodes.SIPUSH, 300));
                patched++;
            }
            if (patched != 1) throw new IllegalStateException("Expected one inferno initializer");
            save(kinds, payload);

            ClassNode signature = read(baseline, "integ/spells/ManorSignatureSpell");
            MethodNode signatureConstructor = signature.methods.stream().filter(method -> method.name.equals("<init>")).findFirst().orElseThrow();
            MethodInsnNode signatureSuper = null;
            for (var instruction : signatureConstructor.instructions) {
                if (instruction instanceof FieldInsnNode field && field.name.equals("baseManaCost"))
                    throw new IllegalStateException("Signature mana override already present");
                if (instruction instanceof MethodInsnNode call && call.owner.equals(ROOT + "integ/spells/FullPowerBossSpell")
                        && call.name.equals("<init>") && call.desc.equals("(IIII)V")) signatureSuper = call;
            }
            if (signatureSuper == null) throw new IllegalStateException("Missing signature base constructor");
            LabelNode ordinary = new LabelNode();
            InsnList manaOverride = new InsnList();
            manaOverride.add(new VarInsnNode(Opcodes.ALOAD, 1));
            manaOverride.add(new FieldInsnNode(Opcodes.GETSTATIC, ROOT + "util/ManorSpellRules$Kind", "STAR_INFERNO", "L" + ROOT + "util/ManorSpellRules$Kind;"));
            manaOverride.add(new JumpInsnNode(Opcodes.IF_ACMPNE, ordinary));
            manaOverride.add(new VarInsnNode(Opcodes.ALOAD, 0));
            manaOverride.add(new VarInsnNode(Opcodes.ALOAD, 1));
            manaOverride.add(new FieldInsnNode(Opcodes.GETFIELD, ROOT + "util/ManorSpellRules$Kind", "mana", "I"));
            manaOverride.add(new FieldInsnNode(Opcodes.PUTFIELD, signature.name, "baseManaCost", "I"));
            manaOverride.add(ordinary);
            manaOverride.add(new FrameNode(Opcodes.F_FULL, 2, new Object[]{signature.name, ROOT + "util/ManorSpellRules$Kind"}, 0, null));
            signatureConstructor.instructions.insert(signatureSuper, manaOverride);
            save(signature, payload);

            ClassNode barrage = read(baseline, "integ/spells/QizaiBarrageSpell");
            MethodNode constructor = barrage.methods.stream().filter(method -> method.name.equals("<init>")).findFirst().orElseThrow();
            patched = 0;
            for (var instruction : constructor.instructions.toArray()) {
                if (instruction instanceof LdcInsnNode constant && constant.cst instanceof Double cooldown
                        && (cooldown == 60.0 || cooldown == 90.0 || cooldown == 120.0)) {
                    if (!(next(instruction) instanceof MethodInsnNode call)
                            || !call.name.equals("setCooldownSeconds")) continue;
                    constant.cst = 90.0;
                    patched++;
                }
            }
            if (patched != 1) throw new IllegalStateException("Expected one barrage cooldown");
            var firstLoad = constructor.instructions.getFirst();
            while (firstLoad != null && firstLoad.getOpcode() < 0) firstLoad = firstLoad.getNext();
            var barrageMana = next(firstLoad);
            if (!(barrageMana instanceof IntInsnNode manaValue) || !List.of(200, 600).contains(manaValue.operand))
                throw new IllegalStateException("Unexpected barrage mana instruction");
            constructor.instructions.set(barrageMana, new IntInsnNode(Opcodes.SIPUSH, 600));
            save(barrage, payload);
            ClassNode barrageRules = read(baseline, "util/QizaiBarrageRules");
            barrageRules.fields.stream().filter(field -> field.name.equals("COOLDOWN_SECONDS")).findFirst().orElseThrow().value = 90;
            barrageRules.fields.stream().filter(field -> field.name.equals("MANA")).findFirst().orElseThrow().value = 600;
            save(barrageRules, payload);
        }
        System.out.println("PASS patched both spells to 90-second cooldowns, 300-tick casts and exactly 600 base mana");
    }
}
