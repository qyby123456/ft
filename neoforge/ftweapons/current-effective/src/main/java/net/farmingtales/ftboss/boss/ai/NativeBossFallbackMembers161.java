package net.farmingtales.ftboss.boss.ai;

import java.util.List;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public final class NativeBossFallbackMembers161 implements Opcodes {
    private NativeBossFallbackMembers161() {
    }

    public static void install(ClassNode target) {
        require(target.name.equals("net/farmingtales/ftboss/entity/EasterBossEntity"), "Exact native Boss owner required");
        for (FieldNode expected : fields()) {
            List<FieldNode> found = target.fields.stream().filter(field -> field.name.equals(expected.name)).toList();
            require(found.size() <= 1, "Duplicate original phantom field");
            if (found.isEmpty()) target.fields.add(expected);
            else require(found.getFirst().access == expected.access && found.getFirst().desc.equals(expected.desc) && found.getFirst().value == null, "Original phantom field drift");
        }
        if (!target.interfaces.contains("net/farmingtales/ftboss/zheergenphantom/PhantomAccess")) target.interfaces.add("net/farmingtales/ftboss/zheergenphantom/PhantomAccess");
        initialize(target, true);
        installTick(target);
        for (MethodNode expected : methods()) {
            List<MethodNode> found = target.methods.stream().filter(method -> method.name.equals(expected.name) && method.desc.equals(expected.desc)).toList();
            require(found.size() <= 1, "Duplicate original Boss fallback member");
            if (found.isEmpty()) target.methods.add(expected);
            else require(same(found.getFirst(), expected), "Original Boss fallback member drift: " + expected.name);
        }
        verifyInstalled(target);
    }

    public static void verifyInstalled(ClassNode target) {
        require(target.interfaces.stream().filter("net/farmingtales/ftboss/zheergenphantom/PhantomAccess"::equals).count() == 1, "Unique original phantom access required");
        initialize(target, false);
        verifyTick(target);
        require(target.name.equals("net/farmingtales/ftboss/entity/EasterBossEntity"), "Exact native Boss owner required");
        for (FieldNode expected : fields()) { List<FieldNode> found = target.fields.stream().filter(field -> field.name.equals(expected.name)).toList(); require(found.size() == 1 && found.getFirst().access == expected.access && found.getFirst().desc.equals(expected.desc) && found.getFirst().value == null, "Complete original phantom fields required"); }
        for (MethodNode expected : methods()) { List<MethodNode> found = target.methods.stream().filter(method -> method.name.equals(expected.name) && method.desc.equals(expected.desc)).toList(); require(found.size() == 1 && same(found.getFirst(), expected), "Complete original Boss fallback body required: " + expected.name); }
    }

    private static void initialize(ClassNode target, boolean install) {
        List<MethodNode> constructors = target.methods.stream().filter(method -> method.name.equals("<init>") && method.desc.equals("(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V")).toList();
        require(constructors.size() == 1, "Exact existing native Boss constructor required");
        MethodNode constructor = constructors.getFirst();
        long writes = java.util.Arrays.stream(constructor.instructions.toArray()).filter(instruction -> instruction instanceof org.objectweb.asm.tree.FieldInsnNode field && field.getOpcode() == PUTFIELD && field.owner.equals(target.name) && field.name.equals("phantomEchoTicks")).count();
        if (writes == 0) {
            require(install, "Original phantom initializer missing");
            List<org.objectweb.asm.tree.AbstractInsnNode> returns = java.util.Arrays.stream(constructor.instructions.toArray()).filter(instruction -> instruction.getOpcode() == RETURN).toList();
            require(returns.size() == 1, "Exact native constructor return required");
            org.objectweb.asm.tree.InsnList init = new org.objectweb.asm.tree.InsnList();
            init.add(new org.objectweb.asm.tree.VarInsnNode(ALOAD, 0)); init.add(new org.objectweb.asm.tree.InsnNode(ICONST_M1)); init.add(new org.objectweb.asm.tree.FieldInsnNode(PUTFIELD, target.name, "phantomEchoTicks", "I")); constructor.instructions.insertBefore(returns.getFirst(), init); constructor.maxStack = Math.max(constructor.maxStack, 2);
        } else require(writes == 1, "Duplicate original phantom initializer");
        for (org.objectweb.asm.tree.AbstractInsnNode instruction : constructor.instructions) if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode field && field.getOpcode() == PUTFIELD && field.owner.equals(target.name) && field.name.equals("phantomEchoTicks")) { org.objectweb.asm.tree.AbstractInsnNode value = field.getPrevious(); while (value != null && value.getOpcode() < 0) value = value.getPrevious(); require(field.desc.equals("I") && value != null && value.getOpcode() == ICONST_M1, "Original phantom delay must initialize to minus one"); org.objectweb.asm.tree.AbstractInsnNode receiver = value.getPrevious(); while (receiver != null && receiver.getOpcode() < 0) receiver = receiver.getPrevious(); require(receiver instanceof org.objectweb.asm.tree.VarInsnNode variable && variable.getOpcode() == ALOAD && variable.var == 0, "Original constructor initializer receiver required"); }
    }

    private static void installTick(ClassNode target) {
        MethodNode tick = tick(target);
        List<org.objectweb.asm.tree.MethodInsnNode> hooks = tickHooks(tick);
        if (hooks.isEmpty()) {
            require(NativeBossLivingComposerSupportV2.methodFingerprint(tick).equals("1e5610155f07b4d2f98b1491b5047af1c3af22ed76d9eb045c19c96d64305680"), "Complete frozen native tick preimage required");
            org.objectweb.asm.tree.InsnList hook = new org.objectweb.asm.tree.InsnList();
            hook.add(new org.objectweb.asm.tree.VarInsnNode(ALOAD, 0));
            hook.add(new org.objectweb.asm.tree.MethodInsnNode(INVOKESTATIC, "net/farmingtales/ftboss/zheergenphantom/PhantomHooks", "tick", "(Lnet/farmingtales/ftboss/entity/EasterBossEntity;)V", false));
            tick.instructions.insertBefore(tickAnchor(tick), hook);
        }
        verifyTick(target);
    }

    private static MethodNode tick(ClassNode target) {
        List<MethodNode> ticks = target.methods.stream().filter(method -> method.name.equals("tick") && method.desc.equals("()V")).toList();
        require(ticks.size() == 1, "Unique complete native Boss tick required");
        return ticks.getFirst();
    }

    private static List<org.objectweb.asm.tree.MethodInsnNode> tickHooks(MethodNode tick) {
        return java.util.Arrays.stream(tick.instructions.toArray()).filter(instruction -> instruction instanceof org.objectweb.asm.tree.MethodInsnNode call && call.owner.equals("net/farmingtales/ftboss/zheergenphantom/PhantomHooks")).map(instruction -> (org.objectweb.asm.tree.MethodInsnNode) instruction).toList();
    }

    private static org.objectweb.asm.tree.AbstractInsnNode tickAnchor(MethodNode tick) {
        List<org.objectweb.asm.tree.AbstractInsnNode> matches = java.util.Arrays.stream(tick.instructions.toArray()).filter(instruction -> instruction instanceof org.objectweb.asm.tree.IntInsnNode value && value.getOpcode() == SIPUSH && value.operand == 200 && value.getPrevious() instanceof org.objectweb.asm.tree.FieldInsnNode field && field.getOpcode() == GETFIELD && field.owner.equals("net/farmingtales/ftboss/entity/EasterBossEntity") && field.name.equals("tickCount") && field.desc.equals("I") && field.getPrevious() instanceof org.objectweb.asm.tree.VarInsnNode receiver && receiver.getOpcode() == ALOAD && receiver.var == 0 && value.getNext().getOpcode() == IREM).toList();
        require(matches.size() == 1, "Exact post-initial-equipment native periodic tick anchor required");
        return matches.getFirst().getPrevious().getPrevious();
    }

    private static void verifyTick(ClassNode target) {
        MethodNode tick = tick(target);
        List<org.objectweb.asm.tree.MethodInsnNode> hooks = tickHooks(tick);
        require(hooks.size() == 1, "Unique original phantom tick required");
        org.objectweb.asm.tree.MethodInsnNode call = hooks.getFirst();
        require(call.getOpcode() == INVOKESTATIC && call.name.equals("tick") && call.desc.equals("(Lnet/farmingtales/ftboss/entity/EasterBossEntity;)V") && !call.itf && call.getPrevious() instanceof org.objectweb.asm.tree.VarInsnNode receiver && receiver.getOpcode() == ALOAD && receiver.var == 0 && call.getNext() == tickAnchor(tick), "Exact original phantom tick placement and receiver required");
        MethodNode preserved = NativeBossLivingComposerSupportV2.copy(tick);
        org.objectweb.asm.tree.MethodInsnNode copied = tickHooks(preserved).getFirst();
        preserved.instructions.remove(copied.getPrevious());
        preserved.instructions.remove(copied);
        require(NativeBossLivingComposerSupportV2.methodFingerprint(preserved).equals("1e5610155f07b4d2f98b1491b5047af1c3af22ed76d9eb045c19c96d64305680"), "Every existing native tick instruction must remain unchanged");
    }

    private static boolean same(MethodNode actual, MethodNode expected) {
        return actual.access == expected.access && actual.desc.equals(expected.desc) && java.util.Objects.equals(actual.signature, expected.signature) && actual.exceptions.equals(expected.exceptions) && actual.maxLocals == expected.maxLocals && NativeBossLivingComposerSupportV2.methodFingerprint(actual).equals(NativeBossLivingComposerSupportV2.methodFingerprint(expected));
    }

    private static List<FieldNode> fields() {
        return List.of(new FieldNode(ACC_PRIVATE, "phantomEchoTicks", "I", null, null), new FieldNode(ACC_PRIVATE, "phantomEchoSkill", "Ljava/lang/String;", null, null), new FieldNode(ACC_PRIVATE, "phantomEchoTarget", "Ljava/util/UUID;", null, null), new FieldNode(ACC_PRIVATE, "phantomEchoX", "D", null, null), new FieldNode(ACC_PRIVATE, "phantomEchoY", "D", null, null), new FieldNode(ACC_PRIVATE, "phantomEchoZ", "D", null, null));
    }

    private static List<MethodNode> methods() {
        return List.of(method9(), method33(), method37(), method45(), method2(), method34(), method48(), method49(), method50(), method51(), method41(), method43(), method52());
    }

    private static MethodNode method41() {
        MethodNode methodVisitor = new MethodNode(2, "knockbackPlayer", "(Lnet/minecraft/world/entity/LivingEntity;)V", null, new String[]{});
        Label[] labels = new Label[7];
        for (int index = 0; index < labels.length; index++) {
            labels[index] = new Label();
        }
        method41Part0(methodVisitor, labels);
        return methodVisitor;
    }

    private static MethodNode method43() {
        MethodNode methodVisitor = new MethodNode(2, "applyShadowStrikeEffect", "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;DZ)V", null, new String[]{});
        Label[] labels = new Label[4];
        for (int index = 0; index < labels.length; index++) {
            labels[index] = new Label();
        }
        method43Part0(methodVisitor, labels);
        return methodVisitor;
    }

    private static MethodNode method52() {
        MethodNode methodVisitor = new MethodNode(2, "applyShadowVoid", "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/server/level/ServerLevel;)V", null, new String[]{});
        Label[] labels = new Label[3];
        for (int index = 0; index < labels.length; index++) {
            labels[index] = new Label();
        }
        method52Part0(methodVisitor, labels);
        return methodVisitor;
    }

    private static void method41Part0(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getX", "()D", false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "getX", "()D", false);
        methodVisitor.visitInsn(DSUB);
        methodVisitor.visitVarInsn(DSTORE, 2);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getZ", "()D", false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "getZ", "()D", false);
        methodVisitor.visitInsn(DSUB);
        methodVisitor.visitVarInsn(DSTORE, 4);
        methodVisitor.visitLdcInsn(Double.valueOf("0.1"));
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitVarInsn(DLOAD, 4);
        methodVisitor.visitVarInsn(DLOAD, 4);
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitInsn(DADD);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sqrt", "(D)D", false);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "max", "(DD)D", false);
        methodVisitor.visitVarInsn(DSTORE, 6);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "isMini", "()Z", false);
        methodVisitor.visitJumpInsn(IFNE, labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "getVariant", "()I", false);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/util/ManorFiveCombatRules", "hasExpandedSkills", "(I)Z", false);
        methodVisitor.visitJumpInsn(IFEQ, labels[0]);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitJumpInsn(GOTO, labels[1]);
        methodVisitor.visitLabel(labels[0]);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitLabel(labels[1]);
        methodVisitor.visitVarInsn(ISTORE, 8);
        methodVisitor.visitVarInsn(ILOAD, 8);
        methodVisitor.visitJumpInsn(IFEQ, labels[2]);
        methodVisitor.visitLdcInsn(Double.valueOf("1.5"));
        methodVisitor.visitJumpInsn(GOTO, labels[3]);
        methodVisitor.visitLabel(labels[2]);
        methodVisitor.visitInsn(DCONST_1);
        methodVisitor.visitLabel(labels[3]);
        methodVisitor.visitVarInsn(DSTORE, 9);
        methodVisitor.visitVarInsn(ILOAD, 8);
        methodVisitor.visitJumpInsn(IFEQ, labels[4]);
        methodVisitor.visitLdcInsn(Double.valueOf("0.55"));
        methodVisitor.visitJumpInsn(GOTO, labels[5]);
        methodVisitor.visitLabel(labels[4]);
        methodVisitor.visitLdcInsn(Double.valueOf("0.42"));
        methodVisitor.visitLabel(labels[5]);
        methodVisitor.visitVarInsn(DSTORE, 11);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitVarInsn(DLOAD, 6);
        methodVisitor.visitInsn(DDIV);
        methodVisitor.visitVarInsn(DLOAD, 9);
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitVarInsn(DLOAD, 11);
        methodVisitor.visitVarInsn(DLOAD, 4);
        methodVisitor.visitVarInsn(DLOAD, 6);
        methodVisitor.visitInsn(DDIV);
        methodVisitor.visitVarInsn(DLOAD, 9);
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/util/BossKnockback", "push", "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;DDD)V", false);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/world/entity/LivingEntity", "hurtMarked", "Z");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "level", "()Lnet/minecraft/world/level/Level;", false);
        methodVisitor.visitVarInsn(ASTORE, 14);
        methodVisitor.visitVarInsn(ALOAD, 14);
        methodVisitor.visitTypeInsn(INSTANCEOF, "net/minecraft/server/level/ServerLevel");
        methodVisitor.visitJumpInsn(IFEQ, labels[6]);
        methodVisitor.visitVarInsn(ALOAD, 14);
        methodVisitor.visitTypeInsn(CHECKCAST, "net/minecraft/server/level/ServerLevel");
        methodVisitor.visitVarInsn(ASTORE, 13);
        methodVisitor.visitVarInsn(ALOAD, 13);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getX", "()D", false);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getY", "()D", false);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getZ", "()D", false);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/sounds/SoundEvents", "PLAYER_ATTACK_KNOCKBACK", "Lnet/minecraft/sounds/SoundEvent;");
        methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/sounds/SoundSource", "HOSTILE", "Lnet/minecraft/sounds/SoundSource;");
        methodVisitor.visitInsn(FCONST_1);
        methodVisitor.visitLdcInsn(Float.valueOf("0.9"));
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/level/ServerLevel", "playSound", "(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", false);
        methodVisitor.visitLabel(labels[6]);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(12, 15);
        methodVisitor.visitEnd();
    }

    private static void method43Part0(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitIntInsn(SIPUSH, 300);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/effect/ShadowBlindEffect", "applyTo", "(Lnet/minecraft/world/entity/LivingEntity;I)V", false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitLdcInsn(Float.valueOf("78.4"));
        methodVisitor.visitLdcInsn(Double.valueOf("2.0"));
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "manorSkillDamage", "(Lnet/minecraft/world/entity/LivingEntity;FD)F", false);
        methodVisitor.visitInsn(F2D);
        methodVisitor.visitVarInsn(DLOAD, 3);
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitInsn(D2F);
        methodVisitor.visitVarInsn(FSTORE, 6);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitJumpInsn(IFEQ, labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/world/entity/LivingEntity", "invulnerableTime", "I");
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "damageSources", "()Lnet/minecraft/world/damagesource/DamageSources;", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/damagesource/DamageSources", "fellOutOfWorld", "()Lnet/minecraft/world/damagesource/DamageSource;", false);
        methodVisitor.visitVarInsn(FLOAD, 6);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/boss/ai/ManorAdaptiveDamage", "hurtSkill", "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z", false);
        methodVisitor.visitInsn(POP);
        methodVisitor.visitJumpInsn(GOTO, labels[1]);
        methodVisitor.visitLabel(labels[0]);
        methodVisitor.visitVarInsn(FLOAD, 6);
        methodVisitor.visitInsn(F2D);
        methodVisitor.visitLdcInsn(Double.valueOf("0.5"));
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitLdcInsn(Double.valueOf("1.5"));
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitInsn(D2F);
        methodVisitor.visitVarInsn(FSTORE, 7);
        methodVisitor.visitVarInsn(FLOAD, 6);
        methodVisitor.visitVarInsn(FLOAD, 7);
        methodVisitor.visitInsn(FSUB);
        methodVisitor.visitVarInsn(FSTORE, 8);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/world/entity/LivingEntity", "invulnerableTime", "I");
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "damageSources", "()Lnet/minecraft/world/damagesource/DamageSources;", false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/damagesource/DamageSources", "mobAttack", "(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;", false);
        methodVisitor.visitVarInsn(FLOAD, 8);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "hurt", "(Lnet/minecraft/world/damagesource/DamageSource;F)Z", false);
        methodVisitor.visitInsn(POP);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/world/entity/LivingEntity", "invulnerableTime", "I");
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "damageSources", "()Lnet/minecraft/world/damagesource/DamageSources;", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/damagesource/DamageSources", "fellOutOfWorld", "()Lnet/minecraft/world/damagesource/DamageSource;", false);
        methodVisitor.visitVarInsn(FLOAD, 7);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/boss/ai/ManorAdaptiveDamage", "hurtSkill", "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z", false);
        methodVisitor.visitInsn(POP);
        methodVisitor.visitLabel(labels[1]);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitVarInsn(ASTORE, 8);
        methodVisitor.visitVarInsn(ALOAD, 8);
        methodVisitor.visitTypeInsn(INSTANCEOF, "net/minecraft/world/entity/LivingEntity");
        methodVisitor.visitJumpInsn(IFEQ, labels[2]);
        methodVisitor.visitVarInsn(ALOAD, 8);
        methodVisitor.visitTypeInsn(CHECKCAST, "net/minecraft/world/entity/LivingEntity");
        methodVisitor.visitVarInsn(ASTORE, 7);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 7);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "knockbackPlayer", "(Lnet/minecraft/world/entity/LivingEntity;)V", false);
        methodVisitor.visitLabel(labels[2]);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitTypeInsn(NEW, "net/minecraft/world/effect/MobEffectInstance");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/world/effect/MobEffects", "WITHER", "Lnet/minecraft/core/Holder;");
        methodVisitor.visitIntInsn(SIPUSH, 360);
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "net/minecraft/world/effect/MobEffectInstance", "<init>", "(Lnet/minecraft/core/Holder;II)V", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "addEffect", "(Lnet/minecraft/world/effect/MobEffectInstance;)Z", false);
        methodVisitor.visitInsn(POP);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitVarInsn(ASTORE, 8);
        methodVisitor.visitVarInsn(ALOAD, 8);
        methodVisitor.visitTypeInsn(INSTANCEOF, "net/minecraft/world/entity/LivingEntity");
        methodVisitor.visitJumpInsn(IFEQ, labels[3]);
        methodVisitor.visitVarInsn(ALOAD, 8);
        methodVisitor.visitTypeInsn(CHECKCAST, "net/minecraft/world/entity/LivingEntity");
        methodVisitor.visitVarInsn(ASTORE, 7);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 7);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "applyShadowVoid", "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/server/level/ServerLevel;)V", false);
        methodVisitor.visitLabel(labels[3]);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/core/particles/ParticleTypes", "SCULK_SOUL", "Lnet/minecraft/core/particles/SimpleParticleType;");
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getX", "()D", false);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getY", "()D", false);
        methodVisitor.visitInsn(DCONST_1);
        methodVisitor.visitInsn(DADD);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getZ", "()D", false);
        methodVisitor.visitIntInsn(BIPUSH, 20);
        methodVisitor.visitLdcInsn(Double.valueOf("0.3"));
        methodVisitor.visitLdcInsn(Double.valueOf("0.5"));
        methodVisitor.visitLdcInsn(Double.valueOf("0.3"));
        methodVisitor.visitLdcInsn(Double.valueOf("0.05"));
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/level/ServerLevel", "sendParticles", "(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", false);
        methodVisitor.visitInsn(POP);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(17, 9);
        methodVisitor.visitEnd();
    }

    private static void method52Part0(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitJumpInsn(IFNULL, labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "isSpectator", "()Z", false);
        methodVisitor.visitJumpInsn(IFNE, labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/boss/ai/ManorLivingTargets", "creative", "(Lnet/minecraft/world/entity/LivingEntity;)Z", false);
        methodVisitor.visitJumpInsn(IFNE, labels[0]);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/effect/FTEffects", "SHADOW_EROSION", "Lnet/neoforged/neoforge/registries/DeferredHolder;");
        methodVisitor.visitVarInsn(ASTORE, 3);
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitJumpInsn(IFNULL, labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getEffect", "(Lnet/minecraft/core/Holder;)Lnet/minecraft/world/effect/MobEffectInstance;", false);
        methodVisitor.visitVarInsn(ASTORE, 4);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitJumpInsn(IFNONNULL, labels[1]);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitJumpInsn(GOTO, labels[2]);
        methodVisitor.visitLabel(labels[1]);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/effect/MobEffectInstance", "getAmplifier", "()I", false);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitInsn(IADD);
        methodVisitor.visitLabel(labels[2]);
        methodVisitor.visitVarInsn(ISTORE, 5);
        methodVisitor.visitInsn(ICONST_5);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitInsn(IADD);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "min", "(II)I", false);
        methodVisitor.visitVarInsn(ISTORE, 6);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitTypeInsn(NEW, "net/minecraft/world/effect/MobEffectInstance");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitIntInsn(SIPUSH, 200);
        methodVisitor.visitVarInsn(ILOAD, 6);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitInsn(ISUB);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "net/minecraft/world/effect/MobEffectInstance", "<init>", "(Lnet/minecraft/core/Holder;IIZZZ)V", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "addEffect", "(Lnet/minecraft/world/effect/MobEffectInstance;)Z", false);
        methodVisitor.visitInsn(POP);
        methodVisitor.visitVarInsn(ILOAD, 6);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitJumpInsn(IF_ICMPLE, labels[0]);
        methodVisitor.visitLdcInsn(Float.valueOf("4.0"));
        methodVisitor.visitVarInsn(ILOAD, 6);
        methodVisitor.visitInsn(I2F);
        methodVisitor.visitInsn(FMUL);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/zheergenphantom/ErosionRules", "unscaledDamage", "(F)F", false);
        methodVisitor.visitInsn(F2I);
        methodVisitor.visitVarInsn(ISTORE, 7);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ILOAD, 6);
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitInvokeDynamicInsn("makeConcatWithConstants", "(II)Ljava/lang/String;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/StringConcatFactory", "makeConcatWithConstants", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;", false), new Object[]{"\u00a75\u6298\u8033\u6839 \u00a77\u7684\u6697\u5f71\u7f20\u4f4f\u4e86\u4f60: \u00a7d\u865a\u7a7a\u4fb5\u8680 \u00a77\u0001 \u5c42 \u00a77(\u6bcf\u79d2 \u0001 \u70b9\u865a\u7a7a\u4f24\u5bb3, \u7a7f\u7532)"});
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/minecraft/network/chat/Component", "literal", "(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", true);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/boss/ai/ManorLivingTargets", "tell", "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/network/chat/Component;Z)V", false);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/FTBossMod", "LOGGER", "Lorg/slf4j/Logger;");
        methodVisitor.visitLdcInsn("[FTBoss][Easter] \u6298\u8033\u6839\u300c\u865a\u7a7a\u4fb5\u8680\u300d-> {} \u53e0\u5230 {} \u5c42(\u6bcf\u79d2 {} \u70b9\u865a\u7a7a\u4f24\u5bb3)");
        methodVisitor.visitInsn(ICONST_3);
        methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getName", "()Lnet/minecraft/network/chat/Component;", false);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "net/minecraft/network/chat/Component", "getString", "()Ljava/lang/String;", true);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitVarInsn(ILOAD, 6);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "info", "(Ljava/lang/String;[Ljava/lang/Object;)V", true);
        methodVisitor.visitLabel(labels[0]);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(9, 8);
        methodVisitor.visitEnd();
    }

    private static void require(boolean condition, String reason) {
        if (!condition) throw new IllegalStateException(reason);
    }

    private static MethodNode method2() {
        MethodNode methodVisitor = new MethodNode(2, "combatPlayer", "(Lnet/minecraft/world/entity/LivingEntity;)Z", null, new String[]{});
        Label[] labels = new Label[5];
        for (int index = 0; index < labels.length; index++) {
            labels[index] = new Label();
        }
        method2Part0(methodVisitor, labels);
        return methodVisitor;
    }

    private static void method2Part0(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/boss/ai/ManorCombatContinuity", "present", "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;)Z", false);
        methodVisitor.visitJumpInsn(IFNE, labels[0]);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitInsn(IRETURN);
        methodVisitor.visitLabel(labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/boss/ai/ManorFinaleCombat", "manages", "(Lnet/minecraft/world/entity/LivingEntity;)Z", false);
        methodVisitor.visitJumpInsn(IFEQ, labels[1]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/boss/ai/ManorFinaleCombat", "validTarget", "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z", false);
        methodVisitor.visitInsn(IRETURN);
        methodVisitor.visitLabel(labels[1]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/boss/ai/ManorLivingTargets", "valid", "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;)Z", false);
        methodVisitor.visitJumpInsn(IFNE, labels[2]);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitJumpInsn(GOTO, labels[3]);
        methodVisitor.visitLabel(labels[2]);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "isAlive", "()Z", false);
        methodVisitor.visitJumpInsn(IFEQ, labels[4]);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/boss/ai/ManorLivingTargets", "creative", "(Lnet/minecraft/world/entity/LivingEntity;)Z", false);
        methodVisitor.visitJumpInsn(IFNE, labels[4]);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "isSpectator", "()Z", false);
        methodVisitor.visitJumpInsn(IFNE, labels[4]);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitJumpInsn(GOTO, labels[3]);
        methodVisitor.visitLabel(labels[4]);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitLabel(labels[3]);
        methodVisitor.visitInsn(IRETURN);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
    }

    private static MethodNode method34() {
        MethodNode methodVisitor = new MethodNode(2, "firePhantomEcho", "(Lnet/minecraft/server/level/ServerLevel;)V", null, new String[]{});
        Label[] labels = new Label[17];
        for (int index = 0; index < labels.length; index++) {
            labels[index] = new Label();
        }
        method34Part0(methodVisitor, labels);
        method34Part1(methodVisitor, labels);
        method34Part2(methodVisitor, labels);
        return methodVisitor;
    }

    private static MethodNode method48() {
        MethodNode methodVisitor = new MethodNode(1, "ftboss$phantomSnapshot", "()Lnet/farmingtales/ftboss/zheergenphantom/PhantomSnapshot;", null, new String[]{});
        Label[] labels = new Label[0];
        for (int index = 0; index < labels.length; index++) {
            labels[index] = new Label();
        }
        method48Part0(methodVisitor, labels);
        return methodVisitor;
    }

    private static MethodNode method49() {
        MethodNode methodVisitor = new MethodNode(1, "ftboss$phantomRestore", "(Lnet/farmingtales/ftboss/zheergenphantom/PhantomSnapshot;)V", null, new String[]{});
        Label[] labels = new Label[0];
        for (int index = 0; index < labels.length; index++) {
            labels[index] = new Label();
        }
        method49Part0(methodVisitor, labels);
        return methodVisitor;
    }

    private static MethodNode method50() {
        MethodNode methodVisitor = new MethodNode(1, "ftboss$phantomClear", "()V", null, new String[]{});
        Label[] labels = new Label[0];
        for (int index = 0; index < labels.length; index++) {
            labels[index] = new Label();
        }
        method50Part0(methodVisitor, labels);
        return methodVisitor;
    }

    private static MethodNode method51() {
        MethodNode methodVisitor = new MethodNode(1, "ftboss$phantomReplay", "(Lnet/minecraft/server/level/ServerLevel;)V", null, new String[]{});
        Label[] labels = new Label[0];
        for (int index = 0; index < labels.length; index++) {
            labels[index] = new Label();
        }
        method51Part0(methodVisitor, labels);
        return methodVisitor;
    }

    private static void method34Part0(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitCode();
        methodVisitor.visitTryCatchBlock(labels[0], labels[1], labels[2], "java/lang/Throwable");
        methodVisitor.visitTryCatchBlock(labels[0], labels[1], labels[3], null);
        methodVisitor.visitTryCatchBlock(labels[2], labels[4], labels[3], null);
        methodVisitor.visitTryCatchBlock(labels[3], labels[5], labels[3], null);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoX", "D");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoY", "D");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoZ", "D");
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/zheergenfx/LightHooks", "echo", "(Lnet/farmingtales/ftboss/entity/EasterBossEntity;DDD)V", false);
        methodVisitor.visitLabel(labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoX", "D");
        methodVisitor.visitVarInsn(DSTORE, 2);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoY", "D");
        methodVisitor.visitVarInsn(DSTORE, 4);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoZ", "D");
        methodVisitor.visitVarInsn(DSTORE, 6);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoSkill", "Ljava/lang/String;");
        methodVisitor.visitVarInsn(ASTORE, 8);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitVarInsn(ASTORE, 9);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTarget", "Ljava/util/UUID;");
        methodVisitor.visitJumpInsn(IFNULL, labels[6]);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTarget", "Ljava/util/UUID;");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/level/ServerLevel", "getEntity", "(Ljava/util/UUID;)Lnet/minecraft/world/entity/Entity;", false);
        methodVisitor.visitVarInsn(ASTORE, 11);
        methodVisitor.visitVarInsn(ALOAD, 11);
        methodVisitor.visitTypeInsn(INSTANCEOF, "net/minecraft/world/entity/LivingEntity");
        methodVisitor.visitJumpInsn(IFEQ, labels[6]);
        methodVisitor.visitVarInsn(ALOAD, 11);
        methodVisitor.visitTypeInsn(CHECKCAST, "net/minecraft/world/entity/LivingEntity");
        methodVisitor.visitVarInsn(ASTORE, 10);
        methodVisitor.visitVarInsn(ALOAD, 10);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "isAlive", "()Z", false);
        methodVisitor.visitJumpInsn(IFEQ, labels[6]);
        methodVisitor.visitVarInsn(ALOAD, 10);
        methodVisitor.visitVarInsn(ASTORE, 9);
        methodVisitor.visitLabel(labels[6]);
        methodVisitor.visitVarInsn(ALOAD, 9);
        methodVisitor.visitJumpInsn(IFNONNULL, labels[7]);
        methodVisitor.visitLdcInsn(Double.valueOf("576.0"));
        methodVisitor.visitVarInsn(DSTORE, 10);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/boss/ai/ManorLivingTargets", "nearby", "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;)Ljava/util/List;", false);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true);
        methodVisitor.visitVarInsn(ASTORE, 12);
        methodVisitor.visitLabel(labels[8]);
        methodVisitor.visitVarInsn(ALOAD, 12);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
        methodVisitor.visitJumpInsn(IFEQ, labels[7]);
        methodVisitor.visitVarInsn(ALOAD, 12);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
        methodVisitor.visitTypeInsn(CHECKCAST, "net/minecraft/world/entity/LivingEntity");
        methodVisitor.visitVarInsn(ASTORE, 13);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 13);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "combatPlayer", "(Lnet/minecraft/world/entity/LivingEntity;)Z", false);
        methodVisitor.visitJumpInsn(IFEQ, labels[9]);
        methodVisitor.visitVarInsn(ALOAD, 13);
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitVarInsn(DLOAD, 4);
        methodVisitor.visitVarInsn(DLOAD, 6);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "distanceToSqr", "(DDD)D", false);
        methodVisitor.visitVarInsn(DSTORE, 14);
        methodVisitor.visitVarInsn(DLOAD, 14);
        methodVisitor.visitVarInsn(DLOAD, 10);
        methodVisitor.visitInsn(DCMPG);
        methodVisitor.visitJumpInsn(IFGE, labels[9]);
        methodVisitor.visitVarInsn(DLOAD, 14);
        methodVisitor.visitVarInsn(DSTORE, 10);
        methodVisitor.visitVarInsn(ALOAD, 13);
        methodVisitor.visitVarInsn(ASTORE, 9);
        methodVisitor.visitLabel(labels[9]);
        methodVisitor.visitJumpInsn(GOTO, labels[8]);
        methodVisitor.visitLabel(labels[7]);
        methodVisitor.visitVarInsn(ALOAD, 9);
        methodVisitor.visitJumpInsn(IFNULL, labels[10]);
        methodVisitor.visitVarInsn(ALOAD, 9);
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitVarInsn(DLOAD, 4);
        methodVisitor.visitVarInsn(DLOAD, 6);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "distanceToSqr", "(DDD)D", false);
        methodVisitor.visitLdcInsn(Double.valueOf("256.0"));
        methodVisitor.visitInsn(DCMPG);
        methodVisitor.visitJumpInsn(IFGT, labels[10]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 9);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "hasLineOfSight", "(Lnet/minecraft/world/entity/Entity;)Z", false);
        methodVisitor.visitJumpInsn(IFEQ, labels[10]);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitJumpInsn(GOTO, labels[11]);
        methodVisitor.visitLabel(labels[10]);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitLabel(labels[11]);
        methodVisitor.visitVarInsn(ISTORE, 10);
        methodVisitor.visitLdcInsn("shadow_strike");
        methodVisitor.visitVarInsn(ALOAD, 8);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
        methodVisitor.visitJumpInsn(IFEQ, labels[12]);
        methodVisitor.visitVarInsn(ILOAD, 10);
        methodVisitor.visitJumpInsn(IFEQ, labels[13]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ALOAD, 9);
        methodVisitor.visitLdcInsn(Double.valueOf("0.25"));
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "applyShadowStrikeEffect", "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;DZ)V", false);
        methodVisitor.visitLabel(labels[13]);
        methodVisitor.visitVarInsn(ALOAD, 1);
    }

    private static void method34Part1(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitVarInsn(DLOAD, 4);
        methodVisitor.visitVarInsn(DLOAD, 6);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/sounds/SoundEvents", "SCULK_SHRIEKER_SHRIEK", "Lnet/minecraft/sounds/SoundEvent;");
        methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/sounds/SoundSource", "HOSTILE", "Lnet/minecraft/sounds/SoundSource;");
        methodVisitor.visitLdcInsn(Float.valueOf("0.8"));
        methodVisitor.visitLdcInsn(Float.valueOf("1.5"));
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/level/ServerLevel", "playSound", "(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", false);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/core/particles/ParticleTypes", "SCULK_SOUL", "Lnet/minecraft/core/particles/SimpleParticleType;");
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitVarInsn(DLOAD, 4);
        methodVisitor.visitInsn(DCONST_1);
        methodVisitor.visitInsn(DADD);
        methodVisitor.visitVarInsn(DLOAD, 6);
        methodVisitor.visitIntInsn(BIPUSH, 24);
        methodVisitor.visitLdcInsn(Double.valueOf("0.4"));
        methodVisitor.visitLdcInsn(Double.valueOf("0.6"));
        methodVisitor.visitLdcInsn(Double.valueOf("0.4"));
        methodVisitor.visitLdcInsn(Double.valueOf("0.05"));
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/level/ServerLevel", "sendParticles", "(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", false);
        methodVisitor.visitInsn(POP);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/FTBossMod", "LOGGER", "Lorg/slf4j/Logger;");
        methodVisitor.visitLdcInsn("[FTBoss][Easter] \u6298\u8033\u6839\u6b8b\u5f71\u91cd\u653e\u300c{}\u300d: \u547d\u4e2d={} ({}% \u771f\u4f24)");
        methodVisitor.visitInsn(ICONST_3);
        methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ALOAD, 8);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitVarInsn(ILOAD, 10);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitIntInsn(BIPUSH, 25);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "info", "(Ljava/lang/String;[Ljava/lang/Object;)V", true);
        methodVisitor.visitJumpInsn(GOTO, labels[1]);
        methodVisitor.visitLabel(labels[12]);
        methodVisitor.visitVarInsn(ALOAD, 8);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/util/ManorFiveCombatRules$Skill", "byId", "(Ljava/lang/String;)Lnet/farmingtales/ftboss/util/ManorFiveCombatRules$Skill;", false);
        methodVisitor.visitVarInsn(ASTORE, 11);
        methodVisitor.visitVarInsn(ALOAD, 11);
        methodVisitor.visitJumpInsn(IFNONNULL, labels[14]);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitJumpInsn(GOTO, labels[15]);
        methodVisitor.visitLabel(labels[14]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "manorFiveSkills", "Lnet/farmingtales/ftboss/boss/ai/ManorFiveSkills;");
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ALOAD, 11);
        methodVisitor.visitTypeInsn(NEW, "net/minecraft/world/phys/Vec3");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitVarInsn(DLOAD, 4);
        methodVisitor.visitVarInsn(DLOAD, 6);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "net/minecraft/world/phys/Vec3", "<init>", "(DDD)V", false);
        methodVisitor.visitVarInsn(ALOAD, 9);
        methodVisitor.visitLdcInsn(Double.valueOf("0.25"));
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/boss/ai/ManorFiveSkills", "echoCast", "(Lnet/minecraft/server/level/ServerLevel;Lnet/farmingtales/ftboss/util/ManorFiveCombatRules$Skill;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/LivingEntity;DZ)I", false);
        methodVisitor.visitLabel(labels[15]);
        methodVisitor.visitVarInsn(ISTORE, 12);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/FTBossMod", "LOGGER", "Lorg/slf4j/Logger;");
        methodVisitor.visitLdcInsn("[FTBoss][Easter] \u6298\u8033\u6839\u6b8b\u5f71\u91cd\u653e\u300c{}\u300d: \u6210\u529f\u547d\u4e2d {} \u4eba ({}% \u771f\u4f24)");
        methodVisitor.visitInsn(ICONST_3);
        methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ALOAD, 8);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitVarInsn(ILOAD, 12);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitIntInsn(BIPUSH, 25);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "info", "(Ljava/lang/String;[Ljava/lang/Object;)V", true);
        methodVisitor.visitLabel(labels[1]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(ICONST_M1);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTicks", "I");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoSkill", "Ljava/lang/String;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTarget", "Ljava/util/UUID;");
        methodVisitor.visitJumpInsn(GOTO, labels[16]);
        methodVisitor.visitLabel(labels[2]);
        methodVisitor.visitVarInsn(ASTORE, 2);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/FTBossMod", "LOGGER", "Lorg/slf4j/Logger;");
        methodVisitor.visitLdcInsn("[FTBoss][Easter] \u6298\u8033\u6839\u6b8b\u5f71\u590d\u73b0\u5931\u8d25(\u5df2\u5ffd\u7565, \u4e0d\u5d29\u670d): {}");
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Throwable", "toString", "()Ljava/lang/String;", false);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "warn", "(Ljava/lang/String;Ljava/lang/Object;)V", true);
        methodVisitor.visitLabel(labels[4]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(ICONST_M1);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTicks", "I");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoSkill", "Ljava/lang/String;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTarget", "Ljava/util/UUID;");
        methodVisitor.visitJumpInsn(GOTO, labels[16]);
        methodVisitor.visitLabel(labels[3]);
        methodVisitor.visitVarInsn(ASTORE, 16);
        methodVisitor.visitLabel(labels[5]);
        methodVisitor.visitVarInsn(ALOAD, 0);
    }

    private static void method34Part2(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitInsn(ICONST_M1);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTicks", "I");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoSkill", "Ljava/lang/String;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTarget", "Ljava/util/UUID;");
        methodVisitor.visitVarInsn(ALOAD, 16);
        methodVisitor.visitInsn(ATHROW);
        methodVisitor.visitLabel(labels[16]);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(17, 17);
        methodVisitor.visitEnd();
    }

    private static void method48Part0(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitCode();
        methodVisitor.visitTypeInsn(NEW, "net/farmingtales/ftboss/zheergenphantom/PhantomSnapshot");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTicks", "I");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoSkill", "Ljava/lang/String;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTarget", "Ljava/util/UUID;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoX", "D");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoY", "D");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoZ", "D");
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "net/farmingtales/ftboss/zheergenphantom/PhantomSnapshot", "<init>", "(ILjava/lang/String;Ljava/util/UUID;DDD)V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(11, 1);
        methodVisitor.visitEnd();
    }

    private static void method49Part0(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/zheergenphantom/PhantomSnapshot", "delay", "()I", false);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTicks", "I");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/zheergenphantom/PhantomSnapshot", "skill", "()Ljava/lang/String;", false);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoSkill", "Ljava/lang/String;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/zheergenphantom/PhantomSnapshot", "target", "()Ljava/util/UUID;", false);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTarget", "Ljava/util/UUID;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/zheergenphantom/PhantomSnapshot", "horizontal", "()D", false);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoX", "D");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/zheergenphantom/PhantomSnapshot", "vertical", "()D", false);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoY", "D");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/zheergenphantom/PhantomSnapshot", "depth", "()D", false);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoZ", "D");
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(3, 2);
        methodVisitor.visitEnd();
    }

    private static void method50Part0(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(ICONST_M1);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTicks", "I");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoSkill", "Ljava/lang/String;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTarget", "Ljava/util/UUID;");
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(2, 1);
        methodVisitor.visitEnd();
    }

    private static void method51Part0(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "firePhantomEcho", "(Lnet/minecraft/server/level/ServerLevel;)V", false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
    }

    private static MethodNode method9() {
        MethodNode methodVisitor = new MethodNode(2, "doraHit", "(Lnet/minecraft/world/entity/LivingEntity;)V", null, new String[]{});
        Label[] labels = new Label[2];
        for (int index = 0; index < labels.length; index++) {
            labels[index] = new Label();
        }
        method9Part0(methodVisitor, labels);
        method9Part1(methodVisitor, labels);
        return methodVisitor;
    }

    private static MethodNode method33() {
        MethodNode methodVisitor = new MethodNode(2, "canShadowStrike", "(Lnet/minecraft/world/entity/LivingEntity;)Z", null, new String[]{});
        Label[] labels = new Label[4];
        for (int index = 0; index < labels.length; index++) {
            labels[index] = new Label();
        }
        method33Part0(methodVisitor, labels);
        return methodVisitor;
    }

    private static MethodNode method37() {
        MethodNode methodVisitor = new MethodNode(2, "doraKnockback", "(Lnet/minecraft/world/entity/LivingEntity;DD)V", null, new String[]{});
        Label[] labels = new Label[1];
        for (int index = 0; index < labels.length; index++) {
            labels[index] = new Label();
        }
        method37Part0(methodVisitor, labels);
        return methodVisitor;
    }

    private static MethodNode method45() {
        MethodNode methodVisitor = new MethodNode(2, "spawnShadowPhantom", "(Lnet/minecraft/server/level/ServerLevel;DDDLnet/minecraft/world/entity/LivingEntity;Ljava/lang/String;)V", null, new String[]{});
        Label[] labels = new Label[14];
        for (int index = 0; index < labels.length; index++) {
            labels[index] = new Label();
        }
        method45Part0(methodVisitor, labels);
        method45Part1(methodVisitor, labels);
        return methodVisitor;
    }

    private static void method9Part0(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "getVariant", "()I", false);
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitJumpInsn(IF_ICMPNE, labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "combatPlayer", "(Lnet/minecraft/world/entity/LivingEntity;)Z", false);
        methodVisitor.visitJumpInsn(IFEQ, labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "isNoAi", "()Z", false);
        methodVisitor.visitJumpInsn(IFNE, labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "isMini", "()Z", false);
        methodVisitor.visitJumpInsn(IFNE, labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "level", "()Lnet/minecraft/world/level/Level;", false);
        methodVisitor.visitVarInsn(ASTORE, 3);
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitTypeInsn(INSTANCEOF, "net/minecraft/server/level/ServerLevel");
        methodVisitor.visitJumpInsn(IFEQ, labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitTypeInsn(CHECKCAST, "net/minecraft/server/level/ServerLevel");
        methodVisitor.visitVarInsn(ASTORE, 2);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "doraTempo", "Lnet/farmingtales/ftboss/util/DoraCombatTempo;");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/util/DoraCombatTempo", "stacks", "()I", false);
        methodVisitor.visitVarInsn(ISTORE, 3);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "doraTempo", "Lnet/farmingtales/ftboss/util/DoraCombatTempo;");
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getUUID", "()Ljava/util/UUID;", false);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/level/ServerLevel", "getGameTime", "()J", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/util/DoraCombatTempo", "hit", "(Ljava/util/UUID;J)I", false);
        methodVisitor.visitVarInsn(ISTORE, 4);
        methodVisitor.visitVarInsn(ILOAD, 4);
        methodVisitor.visitJumpInsn(IFGT, labels[1]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/effect/FTEffects", "LETHAL_TEMPO", "Lnet/neoforged/neoforge/registries/DeferredHolder;");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "removeEffect", "(Lnet/minecraft/core/Holder;)Z", false);
        methodVisitor.visitInsn(POP);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "bossBar", "Lnet/minecraft/server/level/ServerBossEvent;");
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/entity/EasterBossEntity", "NAMES", "[Ljava/lang/String;");
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitInsn(AALOAD);
        methodVisitor.visitInvokeDynamicInsn("makeConcatWithConstants", "(Ljava/lang/String;)Ljava/lang/String;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/StringConcatFactory", "makeConcatWithConstants", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;", false), new Object[]{"\u00a7c\u0001"});
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/minecraft/network/chat/Component", "literal", "(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", true);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/level/ServerBossEvent", "setName", "(Lnet/minecraft/network/chat/Component;)V", false);
        methodVisitor.visitJumpInsn(GOTO, labels[0]);
        methodVisitor.visitLabel(labels[1]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitTypeInsn(NEW, "net/minecraft/world/effect/MobEffectInstance");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/effect/FTEffects", "LETHAL_TEMPO", "Lnet/neoforged/neoforge/registries/DeferredHolder;");
        methodVisitor.visitIntInsn(BIPUSH, 120);
        methodVisitor.visitVarInsn(ILOAD, 4);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitInsn(ISUB);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "net/minecraft/world/effect/MobEffectInstance", "<init>", "(Lnet/minecraft/core/Holder;IIZZZ)V", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "addEffect", "(Lnet/minecraft/world/effect/MobEffectInstance;)Z", false);
        methodVisitor.visitInsn(POP);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "bossBar", "Lnet/minecraft/server/level/ServerBossEvent;");
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/entity/EasterBossEntity", "NAMES", "[Ljava/lang/String;");
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitInsn(AALOAD);
        methodVisitor.visitVarInsn(ILOAD, 4);
        methodVisitor.visitInvokeDynamicInsn("makeConcatWithConstants", "(Ljava/lang/String;I)Ljava/lang/String;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/StringConcatFactory", "makeConcatWithConstants", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;", false), new Object[]{"\u00a7c\u0001 \u00a76[\u81f4\u547d\u8282\u594f \u0001/6]"});
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/minecraft/network/chat/Component", "literal", "(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", true);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/level/ServerBossEvent", "setName", "(Lnet/minecraft/network/chat/Component;)V", false);
        methodVisitor.visitVarInsn(ILOAD, 3);
        methodVisitor.visitVarInsn(ILOAD, 4);
        methodVisitor.visitJumpInsn(IF_ICMPEQ, labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "position", "()Lnet/minecraft/world/phys/Vec3;", false);
        methodVisitor.visitInsn(DCONST_0);
        methodVisitor.visitInsn(DCONST_1);
        methodVisitor.visitInsn(DCONST_0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/phys/Vec3", "add", "(DDD)Lnet/minecraft/world/phys/Vec3;", false);
        methodVisitor.visitVarInsn(ASTORE, 5);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/util/WeaponFxRules$Kind", "DORA_IMPACT", "Lnet/farmingtales/ftboss/util/WeaponFxRules$Kind;");
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/util/WeaponFxRules$Theme", "GOLD", "Lnet/farmingtales/ftboss/util/WeaponFxRules$Theme;");
        methodVisitor.visitVarInsn(ALOAD, 5);
        methodVisitor.visitVarInsn(ALOAD, 5);
        methodVisitor.visitLdcInsn(Double.valueOf("0.52"));
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "getYRot", "()F", false);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/skill/WeaponFx", "send", "(Lnet/minecraft/server/level/ServerLevel;Lnet/farmingtales/ftboss/util/WeaponFxRules$Kind;Lnet/farmingtales/ftboss/util/WeaponFxRules$Theme;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;DF)V", false);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/FTBossMod", "LOGGER", "Lorg/slf4j/Logger;");
        methodVisitor.visitLdcInsn("[FTBoss][Dora] \u81f4\u547d\u8282\u594f {}/6: \u653b\u901fx{}, \u51b7\u5374x{}, \u8fde\u5c04{}\u53d1");
        methodVisitor.visitInsn(ICONST_4);
        methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ILOAD, 4);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "doraTempo", "Lnet/farmingtales/ftboss/util/DoraCombatTempo;");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/util/DoraCombatTempo", "attackSpeedMultiplier", "()D", false);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "doraTempo", "Lnet/farmingtales/ftboss/util/DoraCombatTempo;");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/util/DoraCombatTempo", "cooldownMultiplier", "()D", false);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_3);
        methodVisitor.visitVarInsn(ALOAD, 0);
    }

    private static void method9Part1(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "doraTempo", "Lnet/farmingtales/ftboss/util/DoraCombatTempo;");
        methodVisitor.visitIntInsn(BIPUSH, 32);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/util/DoraCombatTempo", "burstShots", "(I)I", false);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "info", "(Ljava/lang/String;[Ljava/lang/Object;)V", true);
        methodVisitor.visitLabel(labels[0]);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(9, 6);
        methodVisitor.visitEnd();
    }

    private static void method33Part0(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "canTeleportWith", "(Lnet/minecraft/world/entity/LivingEntity;)Z", false);
        methodVisitor.visitJumpInsn(IFEQ, labels[0]);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitInsn(IRETURN);
        methodVisitor.visitLabel(labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitTypeInsn(INSTANCEOF, "net/minecraft/server/level/ServerPlayer");
        methodVisitor.visitJumpInsn(IFEQ, labels[1]);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitTypeInsn(CHECKCAST, "net/minecraft/server/level/ServerPlayer");
        methodVisitor.visitVarInsn(ASTORE, 2);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "combatPlayer", "(Lnet/minecraft/world/entity/LivingEntity;)Z", false);
        methodVisitor.visitJumpInsn(IFEQ, labels[2]);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/level/ServerPlayer", "level", "()Lnet/minecraft/world/level/Level;", false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "level", "()Lnet/minecraft/world/level/Level;", false);
        methodVisitor.visitJumpInsn(IF_ACMPNE, labels[2]);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/boss/ManorFight", "isPlayerFighting", "(Lnet/minecraft/world/entity/player/Player;)Z", false);
        methodVisitor.visitJumpInsn(GOTO, labels[3]);
        methodVisitor.visitLabel(labels[2]);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitLabel(labels[3]);
        methodVisitor.visitInsn(IRETURN);
        methodVisitor.visitLabel(labels[1]);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitInsn(IRETURN);
        methodVisitor.visitMaxs(2, 3);
        methodVisitor.visitEnd();
    }

    private static void method37Part0(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitCode();
        methodVisitor.visitTypeInsn(NEW, "net/minecraft/world/phys/Vec3");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getX", "()D", false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "getX", "()D", false);
        methodVisitor.visitInsn(DSUB);
        methodVisitor.visitInsn(DCONST_0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getZ", "()D", false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "getZ", "()D", false);
        methodVisitor.visitInsn(DSUB);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "net/minecraft/world/phys/Vec3", "<init>", "(DDD)V", false);
        methodVisitor.visitVarInsn(ASTORE, 6);
        methodVisitor.visitVarInsn(ALOAD, 6);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/phys/Vec3", "lengthSqr", "()D", false);
        methodVisitor.visitLdcInsn(Double.valueOf("1.0E-6"));
        methodVisitor.visitInsn(DCMPG);
        methodVisitor.visitJumpInsn(IFGE, labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "getLookAngle", "()Lnet/minecraft/world/phys/Vec3;", false);
        methodVisitor.visitVarInsn(ASTORE, 7);
        methodVisitor.visitTypeInsn(NEW, "net/minecraft/world/phys/Vec3");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 7);
        methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/world/phys/Vec3", "x", "D");
        methodVisitor.visitInsn(DCONST_0);
        methodVisitor.visitVarInsn(ALOAD, 7);
        methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/world/phys/Vec3", "z", "D");
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "net/minecraft/world/phys/Vec3", "<init>", "(DDD)V", false);
        methodVisitor.visitVarInsn(ASTORE, 6);
        methodVisitor.visitVarInsn(ALOAD, 6);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/phys/Vec3", "lengthSqr", "()D", false);
        methodVisitor.visitLdcInsn(Double.valueOf("1.0E-6"));
        methodVisitor.visitInsn(DCMPG);
        methodVisitor.visitJumpInsn(IFGE, labels[0]);
        methodVisitor.visitTypeInsn(NEW, "net/minecraft/world/phys/Vec3");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(DCONST_0);
        methodVisitor.visitInsn(DCONST_0);
        methodVisitor.visitInsn(DCONST_1);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "net/minecraft/world/phys/Vec3", "<init>", "(DDD)V", false);
        methodVisitor.visitVarInsn(ASTORE, 6);
        methodVisitor.visitLabel(labels[0]);
        methodVisitor.visitVarInsn(ALOAD, 6);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/phys/Vec3", "normalize", "()Lnet/minecraft/world/phys/Vec3;", false);
        methodVisitor.visitVarInsn(ASTORE, 6);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ALOAD, 6);
        methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/world/phys/Vec3", "x", "D");
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitVarInsn(DLOAD, 4);
        methodVisitor.visitVarInsn(ALOAD, 6);
        methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/world/phys/Vec3", "z", "D");
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/util/BossKnockback", "push", "(Lnet/minecraft/world/entity/Entity;DDD)V", false);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/world/entity/LivingEntity", "hurtMarked", "Z");
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(10, 8);
        methodVisitor.visitEnd();
    }

    private static void method45Part0(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitCode();
        methodVisitor.visitTryCatchBlock(labels[0], labels[1], labels[2], "java/lang/Throwable");
        methodVisitor.visitTryCatchBlock(labels[3], labels[4], labels[2], "java/lang/Throwable");
        methodVisitor.visitTryCatchBlock(labels[5], labels[6], labels[2], "java/lang/Throwable");
        methodVisitor.visitLabel(labels[0]);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/entity/FTEntities", "SHADOW_PHANTOM", "Lnet/neoforged/neoforge/registries/DeferredHolder;");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/neoforged/neoforge/registries/DeferredHolder", "get", "()Ljava/lang/Object;", false);
        methodVisitor.visitTypeInsn(CHECKCAST, "net/minecraft/world/entity/EntityType");
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/EntityType", "create", "(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;", false);
        methodVisitor.visitTypeInsn(CHECKCAST, "net/farmingtales/ftboss/entity/ShadowPhantomEntity");
        methodVisitor.visitVarInsn(ASTORE, 10);
        methodVisitor.visitVarInsn(ALOAD, 10);
        methodVisitor.visitJumpInsn(IFNONNULL, labels[3]);
        methodVisitor.visitLabel(labels[1]);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitLabel(labels[3]);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "random", "Lnet/minecraft/util/RandomSource;");
        methodVisitor.visitIntInsn(BIPUSH, 101);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "net/minecraft/util/RandomSource", "nextInt", "(I)I", true);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/zheergenphantom/PhantomTimeline", "readableDelay", "(I)I", false);
        methodVisitor.visitInsn(IADD);
        methodVisitor.visitVarInsn(ISTORE, 11);
        methodVisitor.visitInsn(DCONST_1);
        methodVisitor.visitVarInsn(DSTORE, 12);
        methodVisitor.visitInsn(DCONST_0);
        methodVisitor.visitVarInsn(DSTORE, 14);
        methodVisitor.visitVarInsn(ALOAD, 8);
        methodVisitor.visitJumpInsn(IFNULL, labels[7]);
        methodVisitor.visitVarInsn(ALOAD, 8);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getX", "()D", false);
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitInsn(DSUB);
        methodVisitor.visitVarInsn(DSTORE, 16);
        methodVisitor.visitVarInsn(ALOAD, 8);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getZ", "()D", false);
        methodVisitor.visitVarInsn(DLOAD, 6);
        methodVisitor.visitInsn(DSUB);
        methodVisitor.visitVarInsn(DSTORE, 18);
        methodVisitor.visitVarInsn(DLOAD, 16);
        methodVisitor.visitVarInsn(DLOAD, 16);
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitVarInsn(DLOAD, 18);
        methodVisitor.visitVarInsn(DLOAD, 18);
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitInsn(DADD);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sqrt", "(D)D", false);
        methodVisitor.visitVarInsn(DSTORE, 20);
        methodVisitor.visitVarInsn(DLOAD, 20);
        methodVisitor.visitLdcInsn(Double.valueOf("1.0E-4"));
        methodVisitor.visitInsn(DCMPL);
        methodVisitor.visitJumpInsn(IFLE, labels[8]);
        methodVisitor.visitVarInsn(DLOAD, 18);
        methodVisitor.visitInsn(DNEG);
        methodVisitor.visitVarInsn(DLOAD, 20);
        methodVisitor.visitInsn(DDIV);
        methodVisitor.visitVarInsn(DSTORE, 12);
        methodVisitor.visitVarInsn(DLOAD, 16);
        methodVisitor.visitVarInsn(DLOAD, 20);
        methodVisitor.visitInsn(DDIV);
        methodVisitor.visitVarInsn(DSTORE, 14);
        methodVisitor.visitJumpInsn(GOTO, labels[9]);
        methodVisitor.visitLabel(labels[8]);
        methodVisitor.visitInsn(DCONST_0);
        methodVisitor.visitVarInsn(DSTORE, 12);
        methodVisitor.visitInsn(DCONST_1);
        methodVisitor.visitVarInsn(DSTORE, 14);
        methodVisitor.visitLabel(labels[9]);
        methodVisitor.visitJumpInsn(GOTO, labels[10]);
        methodVisitor.visitLabel(labels[7]);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "random", "Lnet/minecraft/util/RandomSource;");
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "net/minecraft/util/RandomSource", "nextDouble", "()D", true);
        methodVisitor.visitLdcInsn(Double.valueOf("3.141592653589793"));
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitLdcInsn(Double.valueOf("2.0"));
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitVarInsn(DSTORE, 16);
        methodVisitor.visitVarInsn(DLOAD, 16);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
        methodVisitor.visitVarInsn(DSTORE, 12);
        methodVisitor.visitVarInsn(DLOAD, 16);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
        methodVisitor.visitVarInsn(DSTORE, 14);
        methodVisitor.visitLabel(labels[10]);
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitVarInsn(DLOAD, 12);
        methodVisitor.visitLdcInsn(Double.valueOf("0.9"));
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitInsn(DADD);
        methodVisitor.visitVarInsn(DSTORE, 16);
        methodVisitor.visitVarInsn(DLOAD, 6);
        methodVisitor.visitVarInsn(DLOAD, 14);
        methodVisitor.visitLdcInsn(Double.valueOf("0.9"));
        methodVisitor.visitInsn(DMUL);
        methodVisitor.visitInsn(DADD);
        methodVisitor.visitVarInsn(DSTORE, 18);
        methodVisitor.visitVarInsn(ALOAD, 10);
        methodVisitor.visitVarInsn(DLOAD, 16);
        methodVisitor.visitVarInsn(DLOAD, 4);
        methodVisitor.visitVarInsn(DLOAD, 18);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "getYRot", "()F", false);
        methodVisitor.visitInsn(FCONST_0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/ShadowPhantomEntity", "moveTo", "(DDDFF)V", false);
        methodVisitor.visitVarInsn(ALOAD, 10);
        methodVisitor.visitVarInsn(ILOAD, 11);
        methodVisitor.visitIntInsn(BIPUSH, 20);
        methodVisitor.visitInsn(IADD);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/ShadowPhantomEntity", "setLife", "(I)V", false);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ALOAD, 10);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/level/ServerLevel", "addFreshEntity", "(Lnet/minecraft/world/entity/Entity;)Z", false);
        methodVisitor.visitJumpInsn(IFNE, labels[5]);
        methodVisitor.visitLabel(labels[4]);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitLabel(labels[5]);
        methodVisitor.visitVarInsn(ALOAD, 0);
    }

    private static void method45Part1(MethodVisitor methodVisitor, Label[] labels) {
        methodVisitor.visitVarInsn(ILOAD, 11);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTicks", "I");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 9);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoSkill", "Ljava/lang/String;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 8);
        methodVisitor.visitJumpInsn(IFNONNULL, labels[11]);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitJumpInsn(GOTO, labels[12]);
        methodVisitor.visitLabel(labels[11]);
        methodVisitor.visitVarInsn(ALOAD, 8);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "getUUID", "()Ljava/util/UUID;", false);
        methodVisitor.visitLabel(labels[12]);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTarget", "Ljava/util/UUID;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(DLOAD, 16);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoX", "D");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(DLOAD, 4);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoY", "D");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(DLOAD, 18);
        methodVisitor.visitFieldInsn(PUTFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoZ", "D");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoX", "D");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoY", "D");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoZ", "D");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/farmingtales/ftboss/entity/EasterBossEntity", "phantomEchoTicks", "I");
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/zheergenfx/LightHooks", "phantom", "(Lnet/farmingtales/ftboss/entity/EasterBossEntity;DDDI)V", false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "net/farmingtales/ftboss/zheergenphantom/PhantomHooks", "capture", "(Lnet/farmingtales/ftboss/entity/EasterBossEntity;)V", false);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitVarInsn(DLOAD, 16);
        methodVisitor.visitVarInsn(DLOAD, 4);
        methodVisitor.visitVarInsn(DLOAD, 18);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/sounds/SoundEvents", "ENDERMAN_TELEPORT", "Lnet/minecraft/sounds/SoundEvent;");
        methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/sounds/SoundSource", "HOSTILE", "Lnet/minecraft/sounds/SoundSource;");
        methodVisitor.visitLdcInsn(Float.valueOf("0.8"));
        methodVisitor.visitLdcInsn(Float.valueOf("0.5"));
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/level/ServerLevel", "playSound", "(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", false);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/core/particles/ParticleTypes", "REVERSE_PORTAL", "Lnet/minecraft/core/particles/SimpleParticleType;");
        methodVisitor.visitVarInsn(DLOAD, 16);
        methodVisitor.visitVarInsn(DLOAD, 4);
        methodVisitor.visitInsn(DCONST_1);
        methodVisitor.visitInsn(DADD);
        methodVisitor.visitVarInsn(DLOAD, 18);
        methodVisitor.visitIntInsn(BIPUSH, 30);
        methodVisitor.visitLdcInsn(Double.valueOf("0.5"));
        methodVisitor.visitLdcInsn(Double.valueOf("0.9"));
        methodVisitor.visitLdcInsn(Double.valueOf("0.5"));
        methodVisitor.visitLdcInsn(Double.valueOf("0.08"));
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/level/ServerLevel", "sendParticles", "(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", false);
        methodVisitor.visitInsn(POP);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/FTBossMod", "LOGGER", "Lorg/slf4j/Logger;");
        methodVisitor.visitLdcInsn("[FTBoss][Easter] \u6298\u8033\u6839\u300c\u6697\u5f71\u7a81\u88ad\u300d\u7559\u4e0b\u7d2b\u8272\u6b8b\u5f71 @{},{},{} (\u8bb0\u5f55\u6280\u80fd={}, {} tick \u540e\u590d\u73b0\u4e00\u6b21, \u4f24\u5bb3={}% \u771f\u4f24)");
        methodVisitor.visitIntInsn(BIPUSH, 6);
        methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(DLOAD, 16);
        methodVisitor.visitInsn(D2I);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitVarInsn(DLOAD, 4);
        methodVisitor.visitInsn(D2I);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitVarInsn(DLOAD, 18);
        methodVisitor.visitInsn(D2I);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_3);
        methodVisitor.visitVarInsn(ALOAD, 9);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_4);
        methodVisitor.visitVarInsn(ILOAD, 11);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitInsn(ICONST_5);
        methodVisitor.visitIntInsn(BIPUSH, 25);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitInsn(AASTORE);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "info", "(Ljava/lang/String;[Ljava/lang/Object;)V", true);
        methodVisitor.visitLabel(labels[6]);
        methodVisitor.visitJumpInsn(GOTO, labels[13]);
        methodVisitor.visitLabel(labels[2]);
        methodVisitor.visitVarInsn(ASTORE, 10);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/farmingtales/ftboss/FTBossMod", "LOGGER", "Lorg/slf4j/Logger;");
        methodVisitor.visitLdcInsn("[FTBoss][Easter] \u6298\u8033\u6839\u6b8b\u5f71\u751f\u6210\u5931\u8d25(\u5df2\u5ffd\u7565, \u4e0d\u5d29\u670d): {}");
        methodVisitor.visitVarInsn(ALOAD, 10);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Throwable", "toString", "()Ljava/lang/String;", false);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "warn", "(Ljava/lang/String;Ljava/lang/Object;)V", true);
        methodVisitor.visitLabel(labels[13]);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(17, 22);
        methodVisitor.visitEnd();
    }
}

