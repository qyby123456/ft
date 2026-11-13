import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Bytecode patches for the Sanguine Bond (血腥契约) bleed nerf.
 *
 * TridentModifier#applySanguine
 *   1. MobEffectInstance(BLEEDING, <duration>, amplifier) -> amplifier clamped to maxAmplifier (4 == 5 levels)
 *   2. bleeding duration 100 -> duration (400 ticks == 20 s)
 *   3. damage/heal multiplier 2^(amp+1) -> 2^min(amp+1, maxExponent) (maxExponent 4 == 16x)
 *
 * TnsEvents#onLivingHurt
 *   4. LivingHurtEvent multiplier 2^(amp+1) -> 2^min(amp+1, maxExponent)
 *
 * Usage: PatchMod <inDir> <outDir> <maxAmplifier> <durationTicks> <maxExponent>
 */
public final class PatchMod {

    private static final String MOB_EFFECT_INSTANCE = "net/minecraft/world/effect/MobEffectInstance";
    private static final String PACKAGE = "me/fzzyhmstrs/tridents_n_stuff/";

    public static void main(String[] args) throws Exception {
        Path inDir = Path.of(args[0]);
        Path outDir = Path.of(args[1]);
        int maxAmplifier = Integer.parseInt(args[2]);
        int duration = Integer.parseInt(args[3]);
        int maxExponent = Integer.parseInt(args[4]);

        patchTridentModifier(inDir, outDir, maxAmplifier, duration, maxExponent);
        patchTnsEvents(inDir, outDir, maxExponent);
    }

    private static void patchTridentModifier(Path inDir, Path outDir, int maxAmplifier, int duration, int maxExponent)
            throws Exception {
        ClassNode node = read(inDir, "TridentModifier.class");
        MethodNode method = require(node, "applySanguine");

        IntInsnNode durationNode = null;
        MethodInsnNode ctorCall = null;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() == Opcodes.BIPUSH && ((IntInsnNode) insn).operand == 100) {
                durationNode = (IntInsnNode) insn;
            } else if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
                MethodInsnNode call = (MethodInsnNode) insn;
                if (MOB_EFFECT_INSTANCE.equals(call.owner) && "<init>".equals(call.name)
                        && "(Lnet/minecraft/world/effect/MobEffect;II)V".equals(call.desc)) {
                    ctorCall = call;
                }
            }
        }
        if (durationNode == null) {
            throw new IllegalStateException("bleeding duration constant (bipush 100) not found");
        }
        if (ctorCall == null) {
            throw new IllegalStateException("MobEffectInstance(MobEffect,int,int) call not found");
        }

        // 1. longer window so the 10 s decay tail fits into a single instance
        method.instructions.set(durationNode, new IntInsnNode(Opcodes.SIPUSH, duration));

        // 2. clamp the amplifier: stack is [.., effect, duration, amplifier] before the ctor call
        method.instructions.insertBefore(ctorCall, new InsnNode(Opcodes.ICONST_0 + maxAmplifier));
        method.instructions.insertBefore(ctorCall, new MethodInsnNode(
                Opcodes.INVOKESTATIC, "java/lang/Math", "min", "(II)I", false));

        // 3. clamp the multiplier exponent
        int clamped = clampPowExponent(method, maxExponent);
        if (clamped != 1) {
            throw new IllegalStateException("expected 1 Math.pow in applySanguine, found " + clamped);
        }

        write(node, outDir, "TridentModifier.class");
        System.out.println("patched TridentModifier  maxAmplifier=" + maxAmplifier
                + " duration=" + duration + " maxExponent=" + maxExponent);
    }

    private static void patchTnsEvents(Path inDir, Path outDir, int maxExponent) throws Exception {
        ClassNode node = read(inDir, "TnsEvents.class");
        MethodNode method = require(node, "onLivingHurt");
        int clamped = clampPowExponent(method, maxExponent);
        if (clamped != 1) {
            throw new IllegalStateException("expected 1 Math.pow in onLivingHurt, found " + clamped);
        }
        write(node, outDir, "TnsEvents.class");
        System.out.println("patched TnsEvents        maxExponent=" + maxExponent);
    }

    /**
     * Rewrites every {@code Math.pow(2.0, <int>) } in the method into
     * {@code Math.pow(2.0, Math.min(<int>, maxExponent))}. The int is always
     * {@code i2d} of the value below it, so the min() goes in front of the i2d.
     */
    private static int clampPowExponent(MethodNode method, int maxExponent) {
        int count = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() != Opcodes.INVOKESTATIC) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) insn;
            if (!"java/lang/Math".equals(call.owner) || !"pow".equals(call.name) || !"(DD)D".equals(call.desc)) {
                continue;
            }
            AbstractInsnNode toDouble = insn.getPrevious();
            if (toDouble == null || toDouble.getOpcode() != Opcodes.I2D) {
                throw new IllegalStateException("unexpected Math.pow argument shape");
            }
            method.instructions.insertBefore(toDouble, new InsnNode(Opcodes.ICONST_0 + maxExponent));
            method.instructions.insertBefore(toDouble, new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "java/lang/Math", "min", "(II)I", false));
            count++;
        }
        return count;
    }

    private static ClassNode read(Path inDir, String simpleName) throws Exception {
        Path file = inDir.resolve(PACKAGE + simpleName);
        ClassReader reader = new ClassReader(Files.readAllBytes(file));
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        return node;
    }

    private static void write(ClassNode node, Path outDir, String simpleName) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Path file = outDir.resolve(PACKAGE + simpleName);
        Files.createDirectories(file.getParent());
        Files.write(file, writer.toByteArray());
    }

    private static MethodNode require(ClassNode node, String name) {
        for (MethodNode method : node.methods) {
            if (name.equals(method.name)) {
                return method;
            }
        }
        throw new IllegalStateException(name + " not found in " + node.name);
    }
}
