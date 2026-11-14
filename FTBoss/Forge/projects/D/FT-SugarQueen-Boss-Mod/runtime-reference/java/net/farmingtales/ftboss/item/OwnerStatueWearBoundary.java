/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  org.objectweb.asm.Opcodes
 *  org.objectweb.asm.Type
 *  org.objectweb.asm.tree.AbstractInsnNode
 *  org.objectweb.asm.tree.AnnotationNode
 *  org.objectweb.asm.tree.ClassNode
 *  org.objectweb.asm.tree.FrameNode
 *  org.objectweb.asm.tree.InsnList
 *  org.objectweb.asm.tree.InsnNode
 *  org.objectweb.asm.tree.IntInsnNode
 *  org.objectweb.asm.tree.LabelNode
 *  org.objectweb.asm.tree.MethodInsnNode
 *  org.objectweb.asm.tree.MethodNode
 *  org.objectweb.asm.tree.TryCatchBlockNode
 *  org.objectweb.asm.tree.VarInsnNode
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.item.NativeMapPutLateProofExtension;
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
    private static final Map<Integer, Original> ORIGINALS = new ConcurrentHashMap<Integer, Original>();
    private static final Map<Integer, Original> WRITERS = new ConcurrentHashMap<Integer, Original>();
    private static final List<Spec> SPECS = List.of(new Spec(1, "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V", 2, -1), new Spec(2, "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;)V", 2, -1), new Spec(3, "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V", 4, -1), new Spec(4, "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;", 2, -1), new Spec(5, "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V", 2, -1), new Spec(6, "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;", 2, -1), new Spec(7, "(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", 2, 1), new Spec(8, "(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", 3, 1), new Spec(9, "(I)V", -1, 1), new Spec(10, "(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", 1, -1), new Spec(11, "(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", 1, -1), new Spec(12, "(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", 1, -1), new Spec(13, "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V", 2, -1), new Spec(14, "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;I)V", 2, -1));

    private OwnerStatueWearBoundary() {
    }

    static boolean original(int n, String string, String string2) {
        Original original = ORIGINALS.get(n);
        return original != null && original.name.equals(string) && original.descriptor.equals(string2);
    }

    static boolean writer(int n, StackWalker.StackFrame stackFrame) {
        Original original = WRITERS.get(n);
        return stackFrame.getDeclaringClass() == ItemStack.class && original != null && original.name.equals(stackFrame.getMethodName()) && original.descriptor.equals(stackFrame.getDescriptor());
    }

    private static void registerWriter(ClassNode classNode, int n, String string, String string2, String string3) {
        List<MethodNode> list = classNode.methods.stream().filter(methodNode -> methodNode.name.contains(string2) && methodNode.desc.equals(string3) && OwnerStatueWearBoundary.merged(methodNode, "net.farmingtales.ftboss.mixin." + string)).toList();
        if (list.size() != 1) {
            throw new IllegalStateException("Missing unique actual original writer callback " + string2);
        }
        MethodNode methodNode2 = list.get(0);
        Original original = new Original(methodNode2.name, methodNode2.desc);
        Original original2 = WRITERS.putIfAbsent(n, original);
        if (original2 != null && !original2.equals(original)) {
            throw new IllegalStateException("Changed original writer callback identity");
        }
    }

    private static void registerWriters(ClassNode classNode) {
        OwnerStatueWearBoundary.registerWriter(classNode, 1, "OwnerStatueWearRawDamageMixin", "ftboss$statueRawDamage", "(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;ILcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V");
        OwnerStatueWearBoundary.registerWriter(classNode, 2, "WeaponMaterialDurabilityMixin", "ftboss$materialWear", "(I)I");
        OwnerStatueWearBoundary.registerWriter(classNode, 3, "WeaponMaterialDurabilityMixin", "ftboss$protectedDamage", "(I)I");
        OwnerStatueWearBoundary.registerWriter(classNode, 4, "WeaponMaterialDurabilityMixin", "ftboss$wearLoss", "(ILorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V");
        OwnerStatueWearBoundary.registerWriter(classNode, 5, "WeaponMaterialDurabilityMixin", "ftboss$keepExhausted", "(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V");
        OwnerStatueWearBoundary.registerWriter(classNode, 6, "OwnerFullRepairItemStackMixin", "ftboss$fullRepairVoidMutation", "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V");
        OwnerStatueWearBoundary.registerWriter(classNode, 7, "OwnerFullRepairItemStackMixin", "ftboss$fullRepairReturnedMutation", "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V");
    }

    static int install(ClassNode classNode) {
        int n;
        Object object;
        Object object2;
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == NativeMapPutLateProofExtension.class && stackFrame.getMethodName().equals("postApply")).orElse(false));
        if (!bl || classNode == null || !classNode.name.equals(STACK) && !classNode.name.equals(INVENTORY)) {
            throw new IllegalStateException("Untrusted original wear installation");
        }
        List<Spec> list = classNode.name.equals(STACK) ? SPECS : List.of(new Spec(0, "(Lnet/minecraft/nbt/ListTag;)V", -1, -1));
        ArrayList<MethodNode> arrayList = new ArrayList<MethodNode>();
        for (Spec spec : list) {
            object2 = classNode.name.equals(STACK) ? "net.farmingtales.ftboss.mixin.OwnerStatueWearItemStackMixin" : "net.farmingtales.ftboss.mixin.OwnerStatueWearInventoryMixin";
            object = spec.kind == 0 ? "ftboss$statueWear$loadAnchor" : "ftboss$statueWear$anchor" + String.format(Locale.ROOT, "%02d", spec.kind);
            List<MethodNode> list2 = classNode.methods.stream().filter(arg_0 -> OwnerStatueWearBoundary.lambda$install$3((String)object, (String)object2, arg_0)).toList();
            if (list2.size() != 1) {
                throw new IllegalStateException("Missing unique original wear anchor " + (String)object);
            }
            MethodNode methodNode = list2.get(0);
            List<MethodNode> list3 = classNode.methods.stream().filter(methodNode2 -> methodNode2.desc.equals(spec.descriptor) && (methodNode2.access & 0x508) == 0 && OwnerStatueWearBoundary.calls(methodNode2, classNode.name, methodNode.name, methodNode.desc)).toList();
            if (list3.size() != 1) {
                throw new IllegalStateException("Missing unique actual original wear method " + (String)object);
            }
            arrayList.add(list3.get(0));
        }
        if (classNode.name.equals(STACK)) {
            OwnerStatueWearBoundary.registerWriters(classNode);
            OwnerStatueWearBoundary.guard(classNode, "ftboss$statueWearEnterGuard", "entered", "(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;II)Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame;");
            OwnerStatueWearBoundary.guard(classNode, "ftboss$statueWearExitGuard", "exited", "(Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame;Ljava/lang/Throwable;)V");
        } else {
            OwnerStatueWearBoundary.guard(classNode, "ftboss$statueWearLoadGuard", "loadEntered", "(Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/nbt/ListTag;)Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Load;");
            OwnerStatueWearBoundary.guard(classNode, "ftboss$statueWearBindGuard", "loadBound", "(Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Load;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/item/ItemStack;)V");
            OwnerStatueWearBoundary.guard(classNode, "ftboss$statueWearLoadExitGuard", "loadExited", "(Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Load;Ljava/lang/Throwable;)V");
        }
        for (n = 0; n < list.size(); ++n) {
            OwnerStatueWearBoundary.wrap(classNode, (MethodNode)arrayList.get(n), list.get(n));
        }
        for (n = 0; n < list.size(); ++n) {
            Spec spec;
            spec = (MethodNode)arrayList.get(n);
            object2 = new Original(((MethodNode)spec).name, ((MethodNode)spec).desc);
            object = ORIGINALS.putIfAbsent(list.get((int)n).kind, (Original)object2);
            if (object == null || ((Original)object).equals(object2)) continue;
            throw new IllegalStateException("Changed original wear method identity");
        }
        return list.size();
    }

    private static boolean merged(MethodNode methodNode, String string) {
        ArrayList arrayList = new ArrayList();
        if (methodNode.visibleAnnotations != null) {
            arrayList.addAll(methodNode.visibleAnnotations);
        }
        if (methodNode.invisibleAnnotations != null) {
            arrayList.addAll(methodNode.invisibleAnnotations);
        }
        for (AnnotationNode annotationNode : arrayList) {
            if (!annotationNode.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;") || annotationNode.values == null) continue;
            int n = 0;
            while (n + 1 < annotationNode.values.size()) {
                if (annotationNode.values.get(n).equals("mixin") && annotationNode.values.get(n + 1).equals(string)) {
                    return true;
                }
                n += 2;
            }
        }
        return false;
    }

    private static boolean calls(MethodNode methodNode, String string, String string2, String string3) {
        for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
            if (!(abstractInsnNode instanceof MethodInsnNode)) continue;
            MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;
            if (!methodInsnNode.owner.equals(string) || !methodInsnNode.name.equals(string2) || !methodInsnNode.desc.equals(string3)) continue;
            return true;
        }
        return false;
    }

    private static void guard(ClassNode classNode, String string, String string2, String string3) {
        if (classNode.methods.stream().anyMatch(methodNode -> methodNode.name.equals(string))) {
            throw new IllegalStateException("Conflicting wear guard " + string);
        }
        MethodNode methodNode2 = new MethodNode(4106, string, string3, null, null);
        LabelNode labelNode = new LabelNode();
        LabelNode labelNode2 = new LabelNode();
        LabelNode labelNode3 = new LabelNode();
        methodNode2.instructions.add((AbstractInsnNode)labelNode);
        ArrayList<Object> arrayList = new ArrayList<Object>();
        int n = 0;
        for (Type type : Type.getArgumentTypes((String)string3)) {
            methodNode2.instructions.add((AbstractInsnNode)new VarInsnNode(type.getOpcode(21), n));
            arrayList.add(OwnerStatueWearBoundary.frameType(type));
            n += type.getSize();
        }
        methodNode2.instructions.add((AbstractInsnNode)new MethodInsnNode(184, ADMISSION, string2, string3, false));
        methodNode2.instructions.add((AbstractInsnNode)labelNode2);
        Type type = Type.getReturnType((String)string3);
        methodNode2.instructions.add((AbstractInsnNode)new InsnNode(type.getOpcode(172)));
        methodNode2.instructions.add((AbstractInsnNode)labelNode3);
        methodNode2.instructions.add((AbstractInsnNode)new FrameNode(0, arrayList.size(), arrayList.toArray(), 1, new Object[]{"java/lang/Throwable"}));
        methodNode2.instructions.add((AbstractInsnNode)new InsnNode(87));
        if (type.getSort() != 0) {
            methodNode2.instructions.add((AbstractInsnNode)new InsnNode(1));
        }
        methodNode2.instructions.add((AbstractInsnNode)new InsnNode(type.getOpcode(172)));
        methodNode2.tryCatchBlocks.add(new TryCatchBlockNode(labelNode, labelNode2, labelNode3, "java/lang/Throwable"));
        methodNode2.maxLocals = n;
        methodNode2.maxStack = Math.max(n, 1);
        classNode.methods.add(methodNode2);
    }

    private static Object frameType(Type type) {
        return switch (type.getSort()) {
            case 1, 2, 3, 4, 5 -> Opcodes.INTEGER;
            case 6 -> Opcodes.FLOAT;
            case 7 -> Opcodes.LONG;
            case 8 -> Opcodes.DOUBLE;
            default -> type.getInternalName();
        };
    }

    /*
     * WARNING - void declaration
     */
    private static void frames(ClassNode classNode, MethodNode methodNode, int n, String string) {
        ArrayList<Object> arrayList = new ArrayList<Object>();
        arrayList.add(classNode.name);
        for (Type object : Type.getArgumentTypes((String)methodNode.desc)) {
            arrayList.add(OwnerStatueWearBoundary.frameType(object));
        }
        for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
            if (!(abstractInsnNode instanceof FrameNode)) continue;
            FrameNode frameNode = (FrameNode)abstractInsnNode;
            switch (frameNode.type) {
                case -1: 
                case 0: {
                    arrayList = new ArrayList(frameNode.local);
                    break;
                }
                case 1: {
                    arrayList.addAll(frameNode.local);
                    break;
                }
                case 2: {
                    void var8_13;
                    boolean bl = false;
                    while (var8_13 < frameNode.local.size()) {
                        arrayList.remove(arrayList.size() - 1);
                        ++var8_13;
                    }
                    break;
                }
                case 3: 
                case 4: {
                    break;
                }
                default: {
                    throw new IllegalStateException("Unsupported original wear frame");
                }
            }
            ArrayList<Object> arrayList2 = new ArrayList<Object>(arrayList);
            int n2 = 0;
            for (Object e : arrayList) {
                n2 += Opcodes.LONG.equals(e) || Opcodes.DOUBLE.equals(e) ? 2 : 1;
            }
            if (n2 > n) {
                throw new IllegalStateException("Original wear frame exceeds declared locals");
            }
            while (n2++ < n) {
                arrayList2.add(Opcodes.TOP);
            }
            arrayList2.add(string);
            if (frameNode.stack == null && (frameNode.type == 1 || frameNode.type == 2 || frameNode.type == 3)) {
                frameNode.stack = new ArrayList();
            }
            frameNode.type = 0;
            frameNode.local = arrayList2;
        }
    }

    private static InsnList terminal(ClassNode classNode, int n, boolean bl, int n2) {
        InsnList insnList = new InsnList();
        insnList.add((AbstractInsnNode)new VarInsnNode(25, n));
        insnList.add((AbstractInsnNode)(n2 < 0 ? new InsnNode(1) : new VarInsnNode(25, n2)));
        insnList.add((AbstractInsnNode)new MethodInsnNode(184, classNode.name, bl ? "ftboss$statueWearLoadExitGuard" : "ftboss$statueWearExitGuard", "(L" + (bl ? LOAD : FRAME) + ";Ljava/lang/Throwable;)V", false));
        return insnList;
    }

    private static void wrap(ClassNode classNode, MethodNode methodNode, Spec spec) {
        MethodInsnNode methodInsnNode;
        AbstractInsnNode abstractInsnNode22;
        boolean bl = spec.kind == 0;
        String string = bl ? LOAD : FRAME;
        int n = methodNode.maxLocals;
        int n2 = n + 1;
        int n3 = n + 2;
        int n4 = n + 3;
        ArrayList<AbstractInsnNode> arrayList = new ArrayList<AbstractInsnNode>();
        ArrayList<MethodInsnNode> arrayList2 = new ArrayList<MethodInsnNode>();
        for (AbstractInsnNode abstractInsnNode22 : methodNode.instructions) {
            if (abstractInsnNode22.getOpcode() >= 172 && abstractInsnNode22.getOpcode() <= 177) {
                arrayList.add(abstractInsnNode22);
            }
            if (!(abstractInsnNode22 instanceof MethodInsnNode)) continue;
            methodInsnNode = (MethodInsnNode)abstractInsnNode22;
            if (methodInsnNode.owner.equals(ADMISSION)) {
                throw new IllegalStateException("Duplicate original wear boundary");
            }
            if (!bl || methodInsnNode.getOpcode() != 184 || !methodInsnNode.owner.equals(STACK) || !methodInsnNode.desc.equals("(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/item/ItemStack;")) continue;
            arrayList2.add(methodInsnNode);
        }
        if (arrayList.isEmpty() || bl && arrayList2.size() != 1) {
            throw new IllegalStateException("Unsupported original wear/load body");
        }
        OwnerStatueWearBoundary.frames(classNode, methodNode, n, string);
        ListIterator listIterator = new LabelNode();
        abstractInsnNode22 = new LabelNode();
        methodInsnNode = new LabelNode();
        InsnList insnList = new InsnList();
        insnList.add((AbstractInsnNode)new VarInsnNode(25, 0));
        if (bl) {
            insnList.add((AbstractInsnNode)new VarInsnNode(25, 1));
        } else {
            insnList.add((AbstractInsnNode)(spec.actor < 0 ? new InsnNode(1) : new VarInsnNode(25, spec.actor)));
            insnList.add((AbstractInsnNode)new IntInsnNode(16, spec.kind));
            insnList.add((AbstractInsnNode)(spec.requested < 0 ? new InsnNode(3) : new VarInsnNode(21, spec.requested)));
        }
        insnList.add((AbstractInsnNode)new MethodInsnNode(184, classNode.name, bl ? "ftboss$statueWearLoadGuard" : "ftboss$statueWearEnterGuard", bl ? "(Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/nbt/ListTag;)Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Load;" : "(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;II)Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame;", false));
        insnList.add((AbstractInsnNode)new VarInsnNode(58, n));
        insnList.add((AbstractInsnNode)listIterator);
        methodNode.instructions.insert(insnList);
        if (bl) {
            for (MethodInsnNode object2 : arrayList2) {
                InsnList insnList2 = new InsnList();
                insnList2.add((AbstractInsnNode)new InsnNode(89));
                insnList2.add((AbstractInsnNode)new VarInsnNode(58, n3));
                methodNode.instructions.insertBefore((AbstractInsnNode)object2, insnList2);
                InsnList insnList3 = new InsnList();
                insnList3.add((AbstractInsnNode)new InsnNode(89));
                insnList3.add((AbstractInsnNode)new VarInsnNode(58, n4));
                insnList3.add((AbstractInsnNode)new VarInsnNode(25, n));
                insnList3.add((AbstractInsnNode)new VarInsnNode(25, n3));
                insnList3.add((AbstractInsnNode)new VarInsnNode(25, n4));
                insnList3.add((AbstractInsnNode)new MethodInsnNode(184, classNode.name, "ftboss$statueWearBindGuard", "(Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Load;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/item/ItemStack;)V", false));
                methodNode.instructions.insert((AbstractInsnNode)object2, insnList3);
            }
        }
        for (AbstractInsnNode abstractInsnNode : arrayList) {
            methodNode.instructions.insertBefore(abstractInsnNode, OwnerStatueWearBoundary.terminal(classNode, n, bl, -1));
        }
        InsnList insnList4 = new InsnList();
        insnList4.add(abstractInsnNode22);
        insnList4.add((AbstractInsnNode)methodInsnNode);
        ArrayList<Object> arrayList3 = new ArrayList<Object>();
        for (int i = 0; i < n; ++i) {
            arrayList3.add(Opcodes.TOP);
        }
        arrayList3.add(string);
        insnList4.add((AbstractInsnNode)new FrameNode(0, arrayList3.size(), arrayList3.toArray(), 1, new Object[]{"java/lang/Throwable"}));
        insnList4.add((AbstractInsnNode)new VarInsnNode(58, n2));
        insnList4.add(OwnerStatueWearBoundary.terminal(classNode, n, bl, n2));
        insnList4.add((AbstractInsnNode)new VarInsnNode(25, n2));
        insnList4.add((AbstractInsnNode)new InsnNode(191));
        methodNode.instructions.add(insnList4);
        methodNode.tryCatchBlocks.add(new TryCatchBlockNode((LabelNode)listIterator, (LabelNode)abstractInsnNode22, (LabelNode)methodInsnNode, "java/lang/Throwable"));
        methodNode.maxLocals = n + (bl ? 4 : 2);
        methodNode.maxStack = Math.max(methodNode.maxStack + (bl ? 3 : 0), 4);
    }

    private static /* synthetic */ boolean lambda$install$3(String string, String string2, MethodNode methodNode) {
        return methodNode.name.contains(string) && OwnerStatueWearBoundary.merged(methodNode, string2);
    }

    private record Original(String name, String descriptor) {
    }

    private record Spec(int kind, String descriptor, int actor, int requested) {
    }
}

