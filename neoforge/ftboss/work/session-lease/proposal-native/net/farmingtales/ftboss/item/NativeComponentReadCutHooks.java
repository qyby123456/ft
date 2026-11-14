package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class NativeComponentReadCutHooks {
    private static volatile ClassNode frostVerifiedNode;
    private static volatile ClassLoader frostVerifiedLoader;
    private static final List<Target> TARGETS = List.of(
        new Target("NativeReadCutItemStackMixin", "net/minecraft/world/item/ItemStack", "weapon", List.of(
            new Rule("setDamageValue", "(I)V"),
            new Rule("set", "(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"),
            new Rule("update", "(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;"),
            new Rule("update", "(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;Ljava/util/function/UnaryOperator;)Ljava/lang/Object;"),
            new Rule("remove", "(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"),
            new Rule("applyComponentsAndValidate", "(Lnet/minecraft/core/component/DataComponentPatch;)V"),
            new Rule("applyComponents", "(Lnet/minecraft/core/component/DataComponentPatch;)V"),
            new Rule("applyComponents", "(Lnet/minecraft/core/component/DataComponentMap;)V"),
            new Rule("enchant", "(Lnet/minecraft/core/Holder;I)V"),
            new Rule("setCount", "(I)V"), new Rule("grow", "(I)V"), new Rule("shrink", "(I)V")
        )),
        new Target("NativeReadCutEntityMixin", "net/minecraft/world/entity/Entity", "player", List.of(
            new Rule("remove", "(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"),
            new Rule("setPos", "(Lnet/minecraft/world/phys/Vec3;)V"), new Rule("setPos", "(DDD)V"),
            new Rule("changeDimension", "(Lnet/minecraft/world/level/portal/DimensionTransition;)Lnet/minecraft/world/entity/Entity;"),
            new Rule("setLevel", "(Lnet/minecraft/world/level/Level;)V")
        )),
        new Target("NativeReadCutLivingEntityMixin", "net/minecraft/world/entity/LivingEntity", "player", List.of(
            new Rule("setHealth", "(F)V"),
            new Rule("setItemInHand", "(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V")
        )),
        new Target("NativeReadCutPlayerMixin", "net/minecraft/world/entity/player/Player", "player", List.of(
            new Rule("die", "(Lnet/minecraft/world/damagesource/DamageSource;)V"),
            new Rule("remove", "(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"),
            new Rule("setItemSlot", "(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V")
        )),
        new Target("NativeReadCutServerPlayerMixin", "net/minecraft/server/level/ServerPlayer", "player", List.of(
            new Rule("die", "(Lnet/minecraft/world/damagesource/DamageSource;)V"),
            new Rule("changeDimension", "(Lnet/minecraft/world/level/portal/DimensionTransition;)Lnet/minecraft/world/entity/Entity;"),
            new Rule("restoreFrom", "(Lnet/minecraft/server/level/ServerPlayer;Z)V"),
            new Rule("teleportTo", "(DDD)V"), new Rule("teleportRelative", "(DDD)V"),
            new Rule("teleportTo", "(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z"),
            new Rule("setGameMode", "(Lnet/minecraft/world/level/GameType;)Z"),
            new Rule("teleportTo", "(Lnet/minecraft/server/level/ServerLevel;DDDFF)V")
        )),
        new Target("NativeReadCutProjectileMixin", "net/minecraft/world/entity/projectile/Projectile", "projectile", List.of(
            new Rule("setOwner", "(Lnet/minecraft/world/entity/Entity;)V")
        )),
        new Target("NativeReadCutMinecraftServerMixin", "net/minecraft/server/MinecraftServer", "server", List.of(
            new Rule("tickServer", "(Ljava/util/function/BooleanSupplier;)V"),
            new Rule("stopServer", "()V"),
            new Rule("reloadResources", "(Ljava/util/Collection;)Ljava/util/concurrent/CompletableFuture;")
        ))
    );

    private NativeComponentReadCutHooks() {
    }

    public static int apply(ClassNode target, String mixin) {
        String name = mixin.substring(mixin.lastIndexOf('.') + 1);
        Target specification = TARGETS.stream().filter(entry -> entry.mixin.equals(name)).findFirst().orElse(null);
        if (specification == null) {
            return 0;
        }
        if (!specification.owner.equals(target.name)) {
            throw new IllegalStateException("Read-cut target mismatch " + target.name);
        }
        List<MethodNode> selected = new ArrayList<>();
        for (Rule rule : specification.rules) {
            List<MethodNode> matches = target.methods.stream()
                .filter(method -> method.name.equals(rule.name) && method.desc.equals(rule.descriptor)).toList();
            if (matches.size() != 1) {
                throw new IllegalStateException("Missing or duplicate native read-cut mutator " + target.name + "." + rule);
            }
            MethodNode method = matches.getFirst();
            if ((method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) != 0) {
                throw new IllegalStateException("Non-instance native read-cut mutator " + method.name);
            }
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof VarInsnNode local && local.var == 0 && local.getOpcode() == Opcodes.ASTORE) {
                    throw new IllegalStateException("Native read-cut receiver reassigned " + method.name);
                }
            }
            selected.add(method);
        }
        int changed = 0;
        for (MethodNode method : selected) {
            String kind = method.name.equals("remove") && method.desc.equals("(Lnet/minecraft/world/entity/Entity$RemovalReason;)V")
                ? "removed" : specification.kind;
            if (NativeReadCutHooks.install(target, method, kind)) {
                changed++;
            }
        }
        return changed;
    }

    static boolean frostIngressReady() {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != FoodWeaponSourceLedger.class) return false;
        return frostVerifiedNode != null && frostVerifiedLoader == FoodWeaponSourceLedger.class.getClassLoader();
    }

    static void verifyFrostIngressEarly(ClassNode target, String mixin) {
        if (!"net/farmingtales/ftboss/item/FrostQueenStaffItem".equals(target.name) || !"net/minecraft/world/item/Item".equals(target.superName)
            || (target.access & Opcodes.ACC_FINAL) == 0 || !mixin.equals("net.farmingtales.ftboss.mixin.NativeReadCutLivingEntityMixin")) throw new IllegalStateException("Frost ingress early target mismatch");
        frostMethod(target, "use", "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;");
        frostMethod(target, "strike", "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;FII)V");
        frostHandler(target, "nativeFrostHurt", "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;FLcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Z", true);
        frostHandler(target, "nativeFrostWear", "(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V", false);
    }

    static synchronized void verifyFrostIngress(ClassNode target, String mixin) {
        StackWalker.StackFrame caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != NativeMapPutLateProofExtension.class || !caller.getMethodName().equals("postApply") || frostVerifiedNode != null) throw new IllegalStateException("Frost final installation callback rejected");
        String owner = "net/farmingtales/ftboss/item/FrostQueenStaffItem";
        if (!owner.equals(target.name) || !"net/minecraft/world/item/Item".equals(target.superName)
            || (target.access & Opcodes.ACC_FINAL) == 0 || !mixin.equals("net.farmingtales.ftboss.mixin.NativeReadCutLivingEntityMixin")) {
            throw new IllegalStateException("Frost ingress installation target mismatch");
        }
        String useDescriptor = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;";
        MethodNode use = frostMethod(target, "use", useDescriptor);
        MethodNode strike = frostMethod(target, "strike", "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;FII)V");
        frostMethod(target, "ray", "(Lnet/minecraft/server/level/ServerPlayer;)V");
        frostMethod(target, "nova", "(Lnet/minecraft/server/level/ServerPlayer;)V");
        MethodNode hurt = frostHandler(target, "nativeFrostHurt", "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;FLcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Z", true);
        MethodNode wear = frostHandler(target, "nativeFrostWear", "(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V", false);
        if (frostCalls(strike, owner, hurt.name, hurt.desc) != 1 || frostCalls(use, owner, wear.name, wear.desc) != 1
            || frostCalls(use, owner, "ray", "(Lnet/minecraft/server/level/ServerPlayer;)V") != 1
            || frostCalls(use, owner, "nova", "(Lnet/minecraft/server/level/ServerPlayer;)V") != 1
            || frostCalls(hurt, "net/farmingtales/ftboss/item/FoodWeaponSourceLedger", "nativeFrostActualHurt", hurt.desc) != 1
            || frostCalls(wear, "net/farmingtales/ftboss/item/FoodWeaponSourceLedger", "nativeFrostOriginalWear", wear.desc) != 1) {
            throw new IllegalStateException("Frost ingress lacks exact original use/strike handlers");
        }
        frostBridge(target, "net/minecraft/world/entity/LivingEntity", "hurt", "(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            "([Ljava/lang/Object;)Ljava/lang/Boolean;", new String[]{"net/minecraft/world/entity/LivingEntity", "net/minecraft/world/damagesource/DamageSource", "float"}, "java/lang/Boolean");
        frostBridge(target, "net/minecraft/world/item/ItemStack", "hurtAndBreak", "(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            "([Ljava/lang/Object;)Ljava/lang/Void;", new String[]{"net/minecraft/world/item/ItemStack", "int", "net/minecraft/world/entity/LivingEntity", "net/minecraft/world/entity/EquipmentSlot"}, "java/lang/Void");
        frostVerifiedLoader = NativeComponentReadCutHooks.class.getClassLoader();
        frostVerifiedNode = target;
    }

    private static MethodNode frostMethod(ClassNode target, String name, String descriptor) {
        List<MethodNode> methods = target.methods.stream().filter(method -> method.name.equals(name) && method.desc.equals(descriptor)).toList();
        if (methods.size() != 1 || (methods.getFirst().access & (Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) != 0) throw new IllegalStateException("Frost original method missing or ambiguous: " + name);
        return methods.getFirst();
    }

    private static MethodNode frostHandler(ClassNode target, String name, String descriptor, boolean isStatic) {
        List<MethodNode> methods = target.methods.stream().filter(method -> method.name.endsWith("$ftboss$" + name) && method.desc.equals(descriptor)).toList();
        if (methods.size() != 1) throw new IllegalStateException("Frost handler missing or ambiguous: " + name);
        MethodNode method = methods.getFirst();
        if ((method.access & Opcodes.ACC_PRIVATE) == 0 || ((method.access & Opcodes.ACC_STATIC) != 0) != isStatic
            || method.visibleAnnotations == null || method.visibleAnnotations.stream().noneMatch(annotation -> {
                if (!annotation.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;") || annotation.values == null) return false;
                for (int index = 0; index + 1 < annotation.values.size(); index += 2) {
                    if (annotation.values.get(index).equals("mixin") && annotation.values.get(index + 1).equals("net.farmingtales.ftboss.mixin.NativeReadCutLivingEntityMixin")) return true;
                }
                return false;
            })) throw new IllegalStateException("Frost handler provenance rejected: " + name);
        return method;
    }

    private static int frostCalls(MethodNode method, String owner, String name, String descriptor) {
        int count = 0;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof org.objectweb.asm.tree.MethodInsnNode call && call.owner.equals(owner) && call.name.equals(name) && call.desc.equals(descriptor)) count++;
        }
        return count;
    }

    private static void frostBridge(ClassNode target, String owner, String name, String descriptor, String bridgeDescriptor, String[] arguments, String boxedReturn) {
        List<MethodNode> methods = target.methods.stream().filter(method -> frostCalls(method, owner, name, descriptor) != 0).toList();
        if (methods.size() != 1) throw new IllegalStateException("Frost original callback count differs: " + name);
        MethodNode bridge = methods.getFirst();
        if (!bridge.name.startsWith("mixinextras$bridge$" + name + "$") || !bridge.desc.equals(bridgeDescriptor)
            || (bridge.access & Opcodes.ACC_STATIC) == 0) throw new IllegalStateException("Frost original callback bridge differs: " + name);
        List<AbstractInsnNode> instructions = new ArrayList<>();
        for (AbstractInsnNode instruction : bridge.instructions) if (instruction.getOpcode() >= 0) instructions.add(instruction);
        List<AbstractInsnNode> expected = new ArrayList<>();
        expected.add(new VarInsnNode(Opcodes.ALOAD, 0));
        expected.add(new org.objectweb.asm.tree.IntInsnNode(Opcodes.BIPUSH, arguments.length));
        expected.add(new org.objectweb.asm.tree.LdcInsnNode("[" + java.util.Arrays.stream(arguments).map(argument -> argument.replace('/', '.')).collect(java.util.stream.Collectors.joining(", ")) + "]"));
        expected.add(new org.objectweb.asm.tree.MethodInsnNode(Opcodes.INVOKESTATIC, "com/llamalad7/mixinextras/injector/wrapoperation/WrapOperationRuntime", "checkArgumentCount", "([Ljava/lang/Object;ILjava/lang/String;)V", false));
        expected.add(new VarInsnNode(Opcodes.ALOAD, 0));
        for (int index = 0; index < arguments.length; index++) {
            String type = arguments[index].equals("float") ? "java/lang/Float" : arguments[index].equals("int") ? "java/lang/Integer" : arguments[index];
            expected.add(new org.objectweb.asm.tree.InsnNode(Opcodes.DUP));
            expected.add(new org.objectweb.asm.tree.IntInsnNode(Opcodes.BIPUSH, index));
            expected.add(new org.objectweb.asm.tree.InsnNode(Opcodes.AALOAD));
            expected.add(new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST, type));
            if (arguments[index].equals("float")) expected.add(new org.objectweb.asm.tree.MethodInsnNode(Opcodes.INVOKEVIRTUAL, type, "floatValue", "()F", false));
            if (arguments[index].equals("int")) expected.add(new org.objectweb.asm.tree.MethodInsnNode(Opcodes.INVOKEVIRTUAL, type, "intValue", "()I", false));
            expected.add(new org.objectweb.asm.tree.InsnNode(Opcodes.SWAP));
        }
        expected.add(new org.objectweb.asm.tree.InsnNode(Opcodes.POP));
        expected.add(new org.objectweb.asm.tree.MethodInsnNode(Opcodes.INVOKEVIRTUAL, owner, name, descriptor, false));
        if (boxedReturn.equals("java/lang/Boolean")) expected.add(new org.objectweb.asm.tree.MethodInsnNode(Opcodes.INVOKESTATIC, boxedReturn, "valueOf", "(Z)Ljava/lang/Boolean;", false));
        else {
            expected.add(new org.objectweb.asm.tree.InsnNode(Opcodes.ACONST_NULL));
            expected.add(new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST, boxedReturn));
        }
        expected.add(new org.objectweb.asm.tree.InsnNode(Opcodes.ARETURN));
        if (instructions.size() != expected.size()) throw new IllegalStateException("Frost original callback instruction count changed: " + name);
        for (int index = 0; index < instructions.size(); index++) {
            AbstractInsnNode actual = instructions.get(index);
            AbstractInsnNode wanted = expected.get(index);
            boolean same = actual.getClass() == wanted.getClass() && actual.getOpcode() == wanted.getOpcode();
            if (wanted instanceof VarInsnNode local) same &= ((VarInsnNode)actual).var == local.var;
            if (wanted instanceof org.objectweb.asm.tree.IntInsnNode literal) same &= ((org.objectweb.asm.tree.IntInsnNode)actual).operand == literal.operand;
            if (wanted instanceof org.objectweb.asm.tree.LdcInsnNode literal) same &= literal.cst.equals(((org.objectweb.asm.tree.LdcInsnNode)actual).cst);
            if (wanted instanceof org.objectweb.asm.tree.TypeInsnNode type) same &= type.desc.equals(((org.objectweb.asm.tree.TypeInsnNode)actual).desc);
            if (wanted instanceof org.objectweb.asm.tree.MethodInsnNode call) {
                org.objectweb.asm.tree.MethodInsnNode observed = (org.objectweb.asm.tree.MethodInsnNode)actual;
                same &= call.owner.equals(observed.owner) && call.name.equals(observed.name) && call.desc.equals(observed.desc) && call.itf == observed.itf;
            }
            if (!same) throw new IllegalStateException("Frost original callback instruction changed: " + name + ":" + index);
        }
    }

    private record Target(String mixin, String owner, String kind, List<Rule> rules) {
    }

    private record Rule(String name, String descriptor) {
    }
}

