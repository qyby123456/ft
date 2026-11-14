package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class OwnerStatueWearBoundary {
    private static final String ADMISSION = "net/farmingtales/ftboss/item/OwnerStatueWearAdmission";
    private static final String FRAME = "net/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame";
    private static final String LOAD = "net/farmingtales/ftboss/item/OwnerStatueWearAdmission$Load";
    private static final String STACK = "net/minecraft/world/item/ItemStack";
    private static final String INVENTORY = "net/minecraft/world/entity/player/Inventory";
    private static final Map<Integer, OwnerStatueWearBoundary.Original> ORIGINALS = new ConcurrentHashMap<>();
    private static final Map<Integer, OwnerStatueWearBoundary.Original> WRITERS = new ConcurrentHashMap<>();
    private static final List<OwnerStatueWearBoundary.Spec> SPECS = List.of(
        new OwnerStatueWearBoundary.Spec(1, "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V", 2, -1),
        new OwnerStatueWearBoundary.Spec(2, "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;)Z", 2, -1),
        new OwnerStatueWearBoundary.Spec(
            3,
            "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V",
            4,
            -1
        ),
        new OwnerStatueWearBoundary.Spec(
            4,
            "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
            2,
            -1
        ),
        new OwnerStatueWearBoundary.Spec(5, "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V", 2, -1),
        new OwnerStatueWearBoundary.Spec(
            6, "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;", 2, -1
        ),
        new OwnerStatueWearBoundary.Spec(7, "(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", 2, 1),
        new OwnerStatueWearBoundary.Spec(8, "(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", 3, 1),
        new OwnerStatueWearBoundary.Spec(9, "(I)V", -1, 1),
        new OwnerStatueWearBoundary.Spec(10, "(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", 1, -1),
        new OwnerStatueWearBoundary.Spec(11, "(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", 1, -1),
        new OwnerStatueWearBoundary.Spec(
            12,
            "(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            1,
            -1
        ),
        new OwnerStatueWearBoundary.Spec(13, "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V", 2, -1),
        new OwnerStatueWearBoundary.Spec(14, "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;I)V", 2, -1)
    );

    private OwnerStatueWearBoundary() {
    }

    static boolean original(int var0, String var1, String var2) {
        OwnerStatueWearBoundary.Original var3 = ORIGINALS.get(var0);
        return var3 != null && var3.name.equals(var1) && var3.descriptor.equals(var2);
    }

    static boolean writer(int var0, StackFrame var1) {
        OwnerStatueWearBoundary.Original var2 = WRITERS.get(var0);
        return var1.getDeclaringClass() == ItemStack.class
            && var2 != null
            && var2.name.equals(var1.getMethodName())
            && var2.descriptor.equals(var1.getDescriptor());
    }

    private static void registerWriter(ClassNode var0, int var1, String var2, String var3, String var4) {
        List var5 = var0.methods
            .stream()
            .filter(var3x -> var3x.name.contains(var3) && var3x.desc.equals(var4) && merged(var3x, "net.farmingtales.ftboss.mixin." + var2))
            .toList();
        if (var5.size() != 1) {
            throw new IllegalStateException("Missing unique actual original writer callback " + var3);
        } else {
            MethodNode var6 = (MethodNode)var5.get(0);
            OwnerStatueWearBoundary.Original var7 = new OwnerStatueWearBoundary.Original(var6.name, var6.desc);
            OwnerStatueWearBoundary.Original var8 = WRITERS.putIfAbsent(var1, var7);
            if (var8 != null && !var8.equals(var7)) {
                throw new IllegalStateException("Changed original writer callback identity");
            }
        }
    }

    private static void registerWriters(ClassNode var0) {
        registerWriter(
            var0,
            1,
            "OwnerStatueWearRawDamageMixin",
            "ftboss$statueRawDamage",
            "(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;ILcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V"
        );
        registerWriter(var0, 2, "WeaponMaterialDurabilityMixin", "ftboss$materialWear", "(I)I");
        registerWriter(var0, 3, "WeaponMaterialDurabilityMixin", "ftboss$protectedDamage", "(I)I");
        registerWriter(var0, 4, "WeaponMaterialDurabilityMixin", "ftboss$wearLoss", "(ILorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V");
        registerWriter(
            var0,
            5,
            "WeaponMaterialDurabilityMixin",
            "ftboss$keepExhausted",
            "(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        );
        registerWriter(
            var0, 6, "OwnerFullRepairItemStackMixin", "ftboss$fullRepairVoidMutation", "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        );
        registerWriter(
            var0,
            7,
            "OwnerFullRepairItemStackMixin",
            "ftboss$fullRepairReturnedMutation",
            "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        );
    }

    static int install(ClassNode var0) {
        boolean var1 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == NativeMapPutLateProofExtension.class && var0xx.getMethodName().equals("postApply"))
                        .orElse(false)
            );
        if (var1 && var0 != null && var0.name.equals("net/minecraft/world/level/storage/PlayerDataStorage")) return installPlayerSave(var0);
        if (var1 && var0 != null && (var0.name.equals("net/minecraft/world/item/ItemStack") || var0.name.equals("net/minecraft/world/entity/player/Inventory"))
            )
         {
            List<OwnerStatueWearBoundary.Spec> var2 = var0.name.equals("net/minecraft/world/item/ItemStack")
                ? SPECS
                : List.of(new OwnerStatueWearBoundary.Spec(0, "(Lnet/minecraft/nbt/ListTag;)V", -1, -1));
            ArrayList var3 = new ArrayList();

            for (OwnerStatueWearBoundary.Spec var5 : var2) {
                String var6 = var0.name.equals("net/minecraft/world/item/ItemStack")
                    ? "net.farmingtales.ftboss.mixin.OwnerStatueWearItemStackMixin"
                    : "net.farmingtales.ftboss.mixin.OwnerStatueWearInventoryMixin";
                String var7 = var5.kind == 0 ? "ftboss$statueWear$loadAnchor" : "ftboss$statueWear$anchor" + String.format(Locale.ROOT, "%02d", var5.kind);
                List var8 = var0.methods.stream().filter(var2x -> var2x.name.contains(var7) && merged(var2x, var6)).toList();
                if (var8.size() != 1) {
                    throw new IllegalStateException("Missing unique original wear anchor " + var7);
                }

                MethodNode var9 = (MethodNode)var8.get(0);
                List var10 = var0.methods
                    .stream()
                    .filter(var3x -> var3x.desc.equals(var5.descriptor) && (var3x.access & 1288) == 0 && calls(var3x, var0.name, var9.name, var9.desc))
                    .toList();
                if (var10.size() != 1) {
                    throw new IllegalStateException("Missing unique actual original wear method " + var7);
                }

                var3.add((MethodNode)var10.get(0));
            }

            if (var0.name.equals("net/minecraft/world/item/ItemStack")) {
                registerWriters(var0);
                guard(
                    var0,
                    "ftboss$statueWearEnterGuard",
                    "entered",
                    "(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;II)Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame;"
                );
                guard(var0, "ftboss$statueWearExitGuard", "exited", "(Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame;Ljava/lang/Throwable;)V");
            } else {
                guard(
                    var0,
                    "ftboss$statueWearLoadGuard",
                    "loadEntered",
                    "(Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/nbt/ListTag;)Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Load;"
                );
                guard(
                    var0,
                    "ftboss$statueWearBindGuard",
                    "loadBound",
                    "(Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Load;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/item/ItemStack;)V"
                );
                guard(
                    var0,
                    "ftboss$statueWearLoadExitGuard",
                    "loadExited",
                    "(Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Load;Ljava/lang/Throwable;)V"
                );
            }

            for (int var11 = 0; var11 < var2.size(); var11++) {
                wrap(var0, (MethodNode)var3.get(var11), (OwnerStatueWearBoundary.Spec)var2.get(var11));
            }

            for (int var12 = 0; var12 < var2.size(); var12++) {
                MethodNode var13 = (MethodNode)var3.get(var12);
                OwnerStatueWearBoundary.Original var14 = new OwnerStatueWearBoundary.Original(var13.name, var13.desc);
                OwnerStatueWearBoundary.Original var15 = ORIGINALS.putIfAbsent(((OwnerStatueWearBoundary.Spec)var2.get(var12)).kind, var14);
                if (var15 != null && !var15.equals(var14)) {
                    throw new IllegalStateException("Changed original wear method identity");
                }
            }

            return var2.size();
        } else {
            throw new IllegalStateException("Untrusted original wear installation");
        }
    }

    private static int installPlayerSave(ClassNode host) {
        String descriptor = "(Lnet/minecraft/world/entity/player/Player;)V";
        List<MethodNode> anchors = host.methods.stream().filter(method -> method.name.contains("ftboss$statueWear$loadAnchor")
            && merged(method, "net.farmingtales.ftboss.mixin.OwnerStatueWearInventoryMixin")).toList();
        if (anchors.size() != 1) throw new IllegalStateException("Missing original player-save anchor");
        MethodNode anchor = anchors.get(0);
        List<MethodNode> targets = host.methods.stream().filter(method -> descriptor.equals(method.desc)
            && (method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0
            && calls(method, host.name, anchor.name, anchor.desc)).toList();
        if (targets.size() != 1 || host.methods.stream().anyMatch(method -> method.name.startsWith("ftboss$nativeRepairSave")))
            throw new IllegalStateException("Conflicting original player-save boundary");
        MethodNode body = targets.get(0);
        List<MethodInsnNode> encodes = new ArrayList<>();
        List<MethodInsnNode> replacements = new ArrayList<>();
        for (AbstractInsnNode instruction : body.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode call) {
                if (call.owner.equals("net/minecraft/world/entity/player/Player") && call.name.equals("saveWithoutId")
                    && call.desc.equals("(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;")) encodes.add(call);
                if (call.owner.equals("net/minecraft/Util") && call.name.equals("safeReplaceFile")
                    && call.desc.equals("(Ljava/nio/file/Path;Ljava/nio/file/Path;Ljava/nio/file/Path;)V")) replacements.add(call);
            }
        }
        if (encodes.size() != 1 || replacements.size() != 1) throw new IllegalStateException("Changed original player-save serialization or replacement");
        MethodInsnNode replacement = replacements.get(0);
        int[] paths = new int[3];
        AbstractInsnNode previous = replacement.getPrevious();
        for (int index = 2; index >= 0; index--) {
            while (previous != null && previous.getOpcode() < 0) previous = previous.getPrevious();
            if (!(previous instanceof VarInsnNode load) || load.getOpcode() != Opcodes.ALOAD)
                throw new IllegalStateException("Changed original player-save replacement operands");
            paths[index] = load.var;
            previous = previous.getPrevious();
        }
        String originalName = body.name;
        int originalAccess = body.access;
        body.name = "ftboss$nativeRepairSaveBody";
        body.access = (body.access & ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC;
        Original publicIdentity = new Original(originalName, descriptor);
        Original bodyIdentity = new Original(body.name, descriptor);
        Original oldPublic = ORIGINALS.putIfAbsent(100, publicIdentity);
        Original oldBody = ORIGINALS.putIfAbsent(101, bodyIdentity);
        if (oldPublic != null && !oldPublic.equals(publicIdentity) || oldBody != null && !oldBody.equals(bodyIdentity))
            throw new IllegalStateException("Changed original player-save identity");
        String storage = "Lnet/minecraft/world/level/storage/PlayerDataStorage;";
        guard(host, "ftboss$nativeRepairSaveEnterGuard", "saveEntered", "(" + storage + "Lnet/minecraft/world/entity/player/Player;)L" + LOAD + ";");
        guard(host, "ftboss$nativeRepairSaveEncodeGuard", "saveEncoded", "(Lnet/minecraft/nbt/CompoundTag;)V");
        guard(host, "ftboss$nativeRepairSaveReplaceGuard", "saveReplaced", "(Ljava/nio/file/Path;Ljava/nio/file/Path;Ljava/nio/file/Path;)V");
        guard(host, "ftboss$nativeRepairSaveExitGuard", "saveExited", "(L" + LOAD + ";Ljava/lang/Throwable;)V");
        InsnList encoded = new InsnList();
        encoded.add(new InsnNode(Opcodes.DUP));
        encoded.add(new MethodInsnNode(Opcodes.INVOKESTATIC, host.name, "ftboss$nativeRepairSaveEncodeGuard", "(Lnet/minecraft/nbt/CompoundTag;)V", false));
        body.instructions.insert(encodes.get(0), encoded);
        InsnList replaced = new InsnList();
        for (int path : paths) replaced.add(new VarInsnNode(Opcodes.ALOAD, path));
        replaced.add(new MethodInsnNode(Opcodes.INVOKESTATIC, host.name, "ftboss$nativeRepairSaveReplaceGuard", "(Ljava/nio/file/Path;Ljava/nio/file/Path;Ljava/nio/file/Path;)V", false));
        body.instructions.insert(replacement, replaced);
        body.maxStack = Math.max(body.maxStack + 1, 4);
        MethodNode wrapper = new MethodNode(originalAccess, originalName, descriptor, body.signature,
            body.exceptions == null ? null : body.exceptions.toArray(String[]::new));
        LabelNode start = new LabelNode();
        LabelNode end = new LabelNode();
        LabelNode handler = new LabelNode();
        wrapper.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        wrapper.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        wrapper.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, host.name, "ftboss$nativeRepairSaveEnterGuard", "(" + storage + "Lnet/minecraft/world/entity/player/Player;)L" + LOAD + ";", false));
        wrapper.instructions.add(new VarInsnNode(Opcodes.ASTORE, 2));
        wrapper.instructions.add(start);
        wrapper.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        wrapper.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        wrapper.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, host.name, body.name, descriptor, false));
        wrapper.instructions.add(end);
        wrapper.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        wrapper.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
        wrapper.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, host.name, "ftboss$nativeRepairSaveExitGuard", "(L" + LOAD + ";Ljava/lang/Throwable;)V", false));
        wrapper.instructions.add(new InsnNode(Opcodes.RETURN));
        wrapper.instructions.add(handler);
        wrapper.instructions.add(new FrameNode(Opcodes.F_FULL, 3, new Object[]{host.name, "net/minecraft/world/entity/player/Player", LOAD}, 1, new Object[]{"java/lang/Throwable"}));
        wrapper.instructions.add(new VarInsnNode(Opcodes.ASTORE, 3));
        wrapper.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        wrapper.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
        wrapper.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, host.name, "ftboss$nativeRepairSaveExitGuard", "(L" + LOAD + ";Ljava/lang/Throwable;)V", false));
        wrapper.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
        wrapper.instructions.add(new InsnNode(Opcodes.ATHROW));
        wrapper.tryCatchBlocks.add(new TryCatchBlockNode(start, end, handler, "java/lang/Throwable"));
        wrapper.maxLocals = 4;
        wrapper.maxStack = 3;
        host.methods.add(wrapper);
        return 1;
    }

    private static boolean merged(MethodNode var0, String var1) {
        ArrayList<AnnotationNode> var2 = new ArrayList<>();
        if (var0.visibleAnnotations != null) {
            var2.addAll(var0.visibleAnnotations);
        }

        if (var0.invisibleAnnotations != null) {
            var2.addAll(var0.invisibleAnnotations);
        }

        for (AnnotationNode var4 : var2) {
            if (var4.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;") && var4.values != null) {
                for (byte var5 = 0; var5 + 1 < var4.values.size(); var5 += 2) {
                    if (var4.values.get(var5).equals("mixin") && var4.values.get(var5 + 1).equals(var1)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean calls(MethodNode var0, String var1, String var2, String var3) {
        for (AbstractInsnNode var5 : var0.instructions) {
            if (var5 instanceof MethodInsnNode var6 && var6.owner.equals(var1) && var6.name.equals(var2) && var6.desc.equals(var3)) {
                return true;
            }
        }

        return false;
    }

    private static void guard(ClassNode var0, String var1, String var2, String var3) {
        if (var0.methods.stream().anyMatch(var1x -> var1x.name.equals(var1))) {
            throw new IllegalStateException("Conflicting wear guard " + var1);
        } else {
            MethodNode var4 = new MethodNode(4106, var1, var3, null, null);
            LabelNode var5 = new LabelNode();
            LabelNode var6 = new LabelNode();
            LabelNode var7 = new LabelNode();
            var4.instructions.add(var5);
            ArrayList var8 = new ArrayList();
            int var9 = 0;

            for (Type var13 : Type.getArgumentTypes(var3)) {
                var4.instructions.add(new VarInsnNode(var13.getOpcode(21), var9));
                var8.add(frameType(var13));
                var9 += var13.getSize();
            }

            var4.instructions.add(new MethodInsnNode(184, "net/farmingtales/ftboss/item/OwnerStatueWearAdmission", var2, var3, false));
            var4.instructions.add(var6);
            Type var14 = Type.getReturnType(var3);
            var4.instructions.add(new InsnNode(var14.getOpcode(172)));
            var4.instructions.add(var7);
            var4.instructions.add(new FrameNode(0, var8.size(), var8.toArray(), 1, new Object[]{"java/lang/Throwable"}));
            var4.instructions.add(new InsnNode(87));
            if (var14.getSort() != 0) {
                var4.instructions.add(new InsnNode(1));
            }

            var4.instructions.add(new InsnNode(var14.getOpcode(172)));
            var4.tryCatchBlocks.add(new TryCatchBlockNode(var5, var6, var7, "java/lang/Throwable"));
            var4.maxLocals = var9;
            var4.maxStack = Math.max(var9, 1);
            var0.methods.add(var4);
        }
    }

    private static Object frameType(Type var0) {
        return switch (var0.getSort()) {
            case 1, 2, 3, 4, 5 -> Opcodes.INTEGER;
            case 6 -> Opcodes.FLOAT;
            case 7 -> Opcodes.LONG;
            case 8 -> Opcodes.DOUBLE;
            default -> var0.getInternalName();
        };
    }

    private static int frames(ClassNode var0, MethodNode var1, int var2, String var3) {
        int frameType = Opcodes.F_FULL;
        for (AbstractInsnNode instruction : var1.instructions) {
            if (instruction instanceof FrameNode frame && frame.type == Opcodes.F_NEW) {
                frameType = Opcodes.F_NEW;
                break;
            }
        }
        ArrayList var4 = new ArrayList();
        var4.add(var0.name);

        for (Type var8 : Type.getArgumentTypes(var1.desc)) {
            var4.add(frameType(var8));
        }

        for (AbstractInsnNode var13 : var1.instructions) {
            if (var13 instanceof FrameNode) {
                FrameNode var14 = (FrameNode)var13;
                switch (var14.type) {
                    case -1:
                    case 0:
                        var4 = new ArrayList(var14.local);
                        break;
                    case 1:
                        var4.addAll(var14.local);
                        break;
                    case 2:
                        for (int var15 = 0; var15 < var14.local.size(); var15++) {
                            var4.remove(var4.size() - 1);
                        }
                    case 3:
                    case 4:
                        break;
                    default:
                        throw new IllegalStateException("Unsupported original wear frame");
                }

                ArrayList var16 = new ArrayList(var4);
                int var9 = 0;

                for (Object var11 : var4) {
                    var9 += !Opcodes.LONG.equals(var11) && !Opcodes.DOUBLE.equals(var11) ? 1 : 2;
                }

                if (var9 > var2) {
                    throw new IllegalStateException("Original wear frame exceeds declared locals");
                }

                while (var9++ < var2) {
                    var16.add(Opcodes.TOP);
                }

                var16.add(var3);
                if (var14.stack == null && (var14.type == 1 || var14.type == 2 || var14.type == 3)) {
                    var14.stack = new ArrayList();
                }

                var14.type = frameType;
                var14.local = var16;
            }
        }
        return frameType;
    }

    private static InsnList terminal(ClassNode var0, int var1, boolean var2, int var3) {
        InsnList var4 = new InsnList();
        var4.add(new VarInsnNode(25, var1));
        var4.add((AbstractInsnNode)(var3 < 0 ? new InsnNode(1) : new VarInsnNode(25, var3)));
        var4.add(
            new MethodInsnNode(
                184,
                var0.name,
                var2 ? "ftboss$statueWearLoadExitGuard" : "ftboss$statueWearExitGuard",
                "(L"
                    + (var2 ? "net/farmingtales/ftboss/item/OwnerStatueWearAdmission$Load" : "net/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame")
                    + ";Ljava/lang/Throwable;)V",
                false
            )
        );
        return var4;
    }

    private static void wrap(ClassNode var0, MethodNode var1, OwnerStatueWearBoundary.Spec var2) {
        boolean var3 = var2.kind == 0;
        String var4 = var3 ? "net/farmingtales/ftboss/item/OwnerStatueWearAdmission$Load" : "net/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame";
        int var5 = var1.maxLocals;
        int var6 = var5 + 1;
        int var7 = var5 + 2;
        int var8 = var5 + 3;
        ArrayList<AbstractInsnNode> var9 = new ArrayList<>();
        ArrayList<MethodInsnNode> var10 = new ArrayList<>();

        for (AbstractInsnNode var12 : var1.instructions) {
            if (var12.getOpcode() >= 172 && var12.getOpcode() <= 177) {
                var9.add(var12);
            }

            if (var12 instanceof MethodInsnNode var13) {
                if (var13.owner.equals("net/farmingtales/ftboss/item/OwnerStatueWearAdmission")) {
                    throw new IllegalStateException("Duplicate original wear boundary");
                }

                if (var3
                    && var13.getOpcode() == 184
                    && var13.owner.equals("net/minecraft/world/item/ItemStack")
                    && var13.name.equals("parse")
                    && var13.desc.equals("(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/Tag;)Ljava/util/Optional;")) {
                    var10.add(var13);
                }
            }
        }

        if (!var9.isEmpty() && (!var3 || var10.size() == 1)) {
            int frameType = frames(var0, var1, var5, var4);
            LabelNode var19 = new LabelNode();
            LabelNode var20 = new LabelNode();
            LabelNode var21 = new LabelNode();
            InsnList var14 = new InsnList();
            var14.add(new VarInsnNode(25, 0));
            if (var3) {
                var14.add(new VarInsnNode(25, 1));
            } else {
                var14.add((AbstractInsnNode)(var2.actor < 0 ? new InsnNode(1) : new VarInsnNode(25, var2.actor)));
                var14.add(new IntInsnNode(16, var2.kind));
                var14.add((AbstractInsnNode)(var2.requested < 0 ? new InsnNode(3) : new VarInsnNode(21, var2.requested)));
            }

            var14.add(
                new MethodInsnNode(
                    184,
                    var0.name,
                    var3 ? "ftboss$statueWearLoadGuard" : "ftboss$statueWearEnterGuard",
                    var3
                        ? "(Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/nbt/ListTag;)Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Load;"
                        : "(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;II)Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame;",
                    false
                )
            );
            var14.add(new VarInsnNode(58, var5));
            var14.add(var19);
            var1.instructions.insert(var14);
            if (var3) {
                for (MethodInsnNode var16 : var10) {
                    InsnList var17 = new InsnList();
                    var17.add(new InsnNode(89));
                    var17.add(new VarInsnNode(58, var7));
                    var1.instructions.insertBefore(var16, var17);
                    InsnList var18 = new InsnList();
                    var18.add(new InsnNode(89));
                    var18.add(new VarInsnNode(58, var8));
                    var18.add(new VarInsnNode(25, var5));
                    var18.add(new VarInsnNode(25, var7));
                    var18.add(new VarInsnNode(25, var8));
                    var18.add(
                        new MethodInsnNode(
                            184,
                            var0.name,
                            "ftboss$statueWearBindGuard",
                            "(Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Load;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/item/ItemStack;)V",
                            false
                        )
                    );
                    AbstractInsnNode parsed = var16.getNext();
                    while (parsed != null && parsed.getOpcode() < 0) parsed = parsed.getNext();
                    if (!(parsed instanceof org.objectweb.asm.tree.FieldInsnNode empty) || empty.getOpcode() != Opcodes.GETSTATIC
                        || !empty.owner.equals(STACK) || !empty.name.equals("EMPTY") || !empty.desc.equals("L" + STACK + ";")) {
                        throw new IllegalStateException("Native inventory parse fallback differs");
                    }
                    parsed = parsed.getNext();
                    while (parsed != null && parsed.getOpcode() < 0) parsed = parsed.getNext();
                    if (!(parsed instanceof MethodInsnNode fallback) || !fallback.owner.equals("java/util/Optional")
                        || !fallback.name.equals("orElse") || !fallback.desc.equals("(Ljava/lang/Object;)Ljava/lang/Object;")) {
                        throw new IllegalStateException("Native inventory parse optional unwrap differs");
                    }
                    parsed = parsed.getNext();
                    while (parsed != null && parsed.getOpcode() < 0) parsed = parsed.getNext();
                    if (!(parsed instanceof org.objectweb.asm.tree.TypeInsnNode cast) || cast.getOpcode() != Opcodes.CHECKCAST || !cast.desc.equals(STACK)) {
                        throw new IllegalStateException("Native inventory parsed stack cast differs");
                    }
                    var1.instructions.insert(parsed, var18);
                }
            }

            for (AbstractInsnNode var24 : var9) {
                var1.instructions.insertBefore(var24, terminal(var0, var5, var3, -1));
            }

            InsnList var23 = new InsnList();
            var23.add(var20);
            var23.add(var21);
            ArrayList var25 = new ArrayList();

            for (int var26 = 0; var26 < var5; var26++) {
                var25.add(Opcodes.TOP);
            }

            var25.add(var4);
            var23.add(new FrameNode(frameType, var25.size(), var25.toArray(), 1, new Object[]{"java/lang/Throwable"}));
            var23.add(new VarInsnNode(58, var6));
            var23.add(terminal(var0, var5, var3, var6));
            var23.add(new VarInsnNode(25, var6));
            var23.add(new InsnNode(191));
            var1.instructions.add(var23);
            var1.tryCatchBlocks.add(new TryCatchBlockNode(var19, var20, var21, "java/lang/Throwable"));
            var1.maxLocals = var5 + (var3 ? 4 : 2);
            var1.maxStack = Math.max(var1.maxStack + (var3 ? 3 : 0), 4);
        } else {
            throw new IllegalStateException("Unsupported original wear/load body");
        }
    }

    private static record Original(String name, String descriptor) {
    }

    private static record Spec(int kind, String descriptor, int actor, int requested) {
    }
    static void nativeItemStackReplay165(ClassNode host, MethodNode parent, int kind) {
        if (host == null || parent == null || !STACK.equals(host.name) || kind < 1 || kind > SPECS.size() || !SPECS.get(kind - 1).descriptor.equals(parent.desc)) throw new IllegalArgumentException("Exact pure native wear clone projection required");
        wrap(host, parent, SPECS.get(kind - 1));
    }

    static ClassNode nativeItemStackGuards165(ClassNode host) {
        if (host == null || !STACK.equals(host.name)) throw new IllegalArgumentException("Exact native guard clone projection required");
        ClassNode projection = new ClassNode(); projection.version = host.version; projection.access = host.access; projection.name = host.name; projection.superName = host.superName;
        guard(projection, "ftboss$statueWearEnterGuard", "entered", "(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;II)Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame;");
        guard(projection, "ftboss$statueWearExitGuard", "exited", "(Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame;Ljava/lang/Throwable;)V");
        return projection;
    }

}
