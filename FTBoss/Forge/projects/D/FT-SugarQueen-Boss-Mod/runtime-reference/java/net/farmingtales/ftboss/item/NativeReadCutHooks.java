/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.Opcodes
 *  org.objectweb.asm.Type
 *  org.objectweb.asm.tree.AbstractInsnNode
 *  org.objectweb.asm.tree.ClassNode
 *  org.objectweb.asm.tree.FrameNode
 *  org.objectweb.asm.tree.InsnList
 *  org.objectweb.asm.tree.InsnNode
 *  org.objectweb.asm.tree.JumpInsnNode
 *  org.objectweb.asm.tree.LabelNode
 *  org.objectweb.asm.tree.LdcInsnNode
 *  org.objectweb.asm.tree.MethodInsnNode
 *  org.objectweb.asm.tree.MethodNode
 *  org.objectweb.asm.tree.TryCatchBlockNode
 *  org.objectweb.asm.tree.VarInsnNode
 *  org.objectweb.asm.tree.analysis.Analyzer
 *  org.objectweb.asm.tree.analysis.AnalyzerException
 *  org.objectweb.asm.tree.analysis.BasicInterpreter
 *  org.objectweb.asm.tree.analysis.BasicValue
 *  org.objectweb.asm.tree.analysis.Frame
 *  org.objectweb.asm.tree.analysis.Interpreter
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

public final class NativeReadCutHooks {
    private static final String MARKER = "ftboss:p10:read-cut:v1";
    private static final String FRAME = "net/farmingtales/ftboss/item/NativeObservationFrame";
    private static final String OBSERVER = "net/farmingtales/ftboss/item/NativeReadCutObserver";

    private NativeReadCutHooks() {
    }

    public static int apply(ClassNode classNode, String string) {
        Object object;
        MethodNode methodNode2;
        List<MethodNode> list;
        String string2;
        String string3 = string.substring(string.lastIndexOf(46) + 1);
        switch (string3) {
            case "NativeReadCutItemStackMixin": {
                String string4 = "net/minecraft/world/item/ItemStack";
                break;
            }
            case "NativeReadCutCapabilityProviderMixin": {
                String string4 = "net/minecraftforge/common/capabilities/CapabilityProvider";
                break;
            }
            case "NativeReadCutEntityMixin": {
                String string4 = "net/minecraft/world/entity/Entity";
                break;
            }
            case "NativeReadCutLivingEntityMixin": {
                String string4 = "net/minecraft/world/entity/LivingEntity";
                break;
            }
            case "NativeReadCutPlayerMixin": {
                String string4 = "net/minecraft/world/entity/player/Player";
                break;
            }
            case "NativeReadCutServerPlayerMixin": {
                String string4 = "net/minecraft/server/level/ServerPlayer";
                break;
            }
            case "NativeReadCutProjectileMixin": {
                String string4 = "net/minecraft/world/entity/projectile/Projectile";
                break;
            }
            case "NativeReadCutMinecraftServerMixin": {
                String string4 = "net/minecraft/server/MinecraftServer";
                break;
            }
            default: {
                String string4 = string2 = null;
            }
        }
        if (string2 == null) {
            return 0;
        }
        if (!string2.equals(classNode.name)) {
            throw new IllegalStateException("Read-cut target mismatch " + classNode.name);
        }
        Object object3 = new ArrayList();
        for (Rule object22 : NativeReadCutHooks.rules(string2)) {
            list = classNode.methods.stream().filter(methodNode -> methodNode.desc.equals(rule.descriptor) && (methodNode.name.equals(rule.official) || methodNode.name.equals(rule.srg))).toList();
            if (list.size() != 1) {
                throw new IllegalStateException("Missing or duplicate read-cut mutator " + string2 + "." + object22);
            }
            methodNode2 = (MethodNode)list.get(0);
            if ((methodNode2.access & 0x508) != 0) {
                throw new IllegalStateException("Non-instance read-cut mutator " + methodNode2.name);
            }
            object = methodNode2.instructions.iterator();
            while (object.hasNext()) {
                AbstractInsnNode abstractInsnNode = (AbstractInsnNode)object.next();
                if (!(abstractInsnNode instanceof VarInsnNode)) continue;
                VarInsnNode varInsnNode = (VarInsnNode)abstractInsnNode;
                if (varInsnNode.var != 0 || varInsnNode.getOpcode() != 58) continue;
                throw new IllegalStateException("Read-cut receiver reassigned " + methodNode2.name);
            }
            object3.add(methodNode2);
        }
        int n = 0;
        String string5 = string2.equals("net/minecraft/world/item/ItemStack") || string2.equals("net/minecraftforge/common/capabilities/CapabilityProvider") ? "weapon" : "player";
        list = object3.iterator();
        while (list.hasNext()) {
            methodNode2 = (MethodNode)list.next();
            if (!NativeReadCutHooks.install(classNode, methodNode2, (String)(object = string2.equals("net/minecraft/server/MinecraftServer") ? "server" : (string2.equals("net/minecraft/world/entity/projectile/Projectile") ? "projectile" : (methodNode2.desc.equals("(Lnet/minecraft/world/entity/Entity$RemovalReason;)V") && (methodNode2.name.equals("remove") || methodNode2.name.equals("m_142687_")) ? "removed" : string5))))) continue;
            ++n;
        }
        return n;
    }

    private static List<Rule> rules(String string) {
        return switch (string) {
            case "net/minecraft/world/entity/projectile/Projectile" -> List.of(new Rule("setOwner", "m_5602_", "(Lnet/minecraft/world/entity/Entity;)V"));
            case "net/minecraft/server/MinecraftServer" -> List.of(new Rule("tickServer", "m_5705_", "(Ljava/util/function/BooleanSupplier;)V"), new Rule("stopServer", "m_7041_", "()V"), new Rule("reloadResources", "m_129861_", "(Ljava/util/Collection;)Ljava/util/concurrent/CompletableFuture;"));
            case "net/minecraft/world/item/ItemStack" -> List.of(new Rule("setDamageValue", "m_41721_", "(I)V"), new Rule("getOrCreateTag", "m_41784_", "()Lnet/minecraft/nbt/CompoundTag;"), new Rule("getOrCreateTagElement", "m_41698_", "(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;"), new Rule("removeTagKey", "m_41749_", "(Ljava/lang/String;)V"), new Rule("setTag", "m_41751_", "(Lnet/minecraft/nbt/CompoundTag;)V"), new Rule("setHoverName", "m_41714_", "(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/world/item/ItemStack;"), new Rule("resetHoverName", "m_41787_", "()V"), new Rule("hideTooltipPart", "m_41654_", "(Lnet/minecraft/world/item/ItemStack$TooltipPart;)V"), new Rule("enchant", "m_41663_", "(Lnet/minecraft/world/item/enchantment/Enchantment;I)V"), new Rule("addTagElement", "m_41700_", "(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V"), new Rule("setRepairCost", "m_41742_", "(I)V"), new Rule("setCount", "m_41764_", "(I)V"), new Rule("grow", "m_41769_", "(I)V"), new Rule("shrink", "m_41774_", "(I)V"));
            case "net/minecraftforge/common/capabilities/CapabilityProvider" -> List.of(new Rule("gatherCapabilities", "gatherCapabilities", "()V"), new Rule("gatherCapabilities", "gatherCapabilities", "(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;)V"), new Rule("gatherCapabilities", "gatherCapabilities", "(Ljava/util/function/Supplier;)V"), new Rule("deserializeCaps", "deserializeCaps", "(Lnet/minecraft/nbt/CompoundTag;)V"), new Rule("invalidateCaps", "invalidateCaps", "()V"), new Rule("reviveCaps", "reviveCaps", "()V"));
            case "net/minecraft/world/entity/Entity" -> List.of(new Rule("remove", "m_142687_", "(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"), new Rule("setPos", "m_146884_", "(Lnet/minecraft/world/phys/Vec3;)V"), new Rule("setPos", "m_6034_", "(DDD)V"), new Rule("changeDimension", "m_5489_", "(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;"), new Rule("changeDimension", "changeDimension", "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;"), new Rule("setLevel", "m_284535_", "(Lnet/minecraft/world/level/Level;)V"));
            case "net/minecraft/world/entity/LivingEntity" -> List.of(new Rule("setHealth", "m_21153_", "(F)V"), new Rule("setItemInHand", "m_21008_", "(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"));
            case "net/minecraft/world/entity/player/Player" -> List.of(new Rule("die", "m_6667_", "(Lnet/minecraft/world/damagesource/DamageSource;)V"), new Rule("remove", "m_142687_", "(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"), new Rule("setItemSlot", "m_8061_", "(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"));
            case "net/minecraft/server/level/ServerPlayer" -> List.of(new Rule("die", "m_6667_", "(Lnet/minecraft/world/damagesource/DamageSource;)V"), new Rule("changeDimension", "changeDimension", "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;"), new Rule("restoreFrom", "m_9015_", "(Lnet/minecraft/server/level/ServerPlayer;Z)V"), new Rule("teleportTo", "m_6021_", "(DDD)V"), new Rule("teleportRelative", "m_246847_", "(DDD)V"), new Rule("teleportTo", "m_264318_", "(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z"), new Rule("setGameMode", "m_143403_", "(Lnet/minecraft/world/level/GameType;)Z"), new Rule("teleportTo", "m_8999_", "(Lnet/minecraft/server/level/ServerLevel;DDDFF)V"));
            default -> throw new IllegalStateException("No read-cut rules " + string);
        };
    }

    private static boolean install(ClassNode classNode, MethodNode methodNode, String string) {
        Object object2;
        Type type2;
        for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
            LdcInsnNode ldcInsnNode;
            if (abstractInsnNode.getOpcode() < 0) continue;
            if (abstractInsnNode instanceof LdcInsnNode) {
                ldcInsnNode = (LdcInsnNode)abstractInsnNode;
                if (MARKER.equals(ldcInsnNode.cst)) {
                    return false;
                }
            }
            if (!(abstractInsnNode instanceof LdcInsnNode)) break;
            ldcInsnNode = (LdcInsnNode)abstractInsnNode;
            if (!"ftboss:p8:read-cut:v1".equals(ldcInsnNode.cst) && !"ftboss:p9:read-cut:v1".equals(ldcInsnNode.cst)) break;
            throw new IllegalStateException("Stale transformed method is not an original P10 input " + methodNode.name);
        }
        ListIterator listIterator = Type.getReturnType((String)methodNode.desc);
        int n = methodNode.maxLocals;
        int n2 = n + listIterator.getSize();
        int n3 = n2 + 1;
        int n4 = n3 + 1;
        ArrayList<Object> arrayList = new ArrayList<Object>();
        arrayList.add(classNode.name);
        for (Type type2 : Type.getArgumentTypes((String)methodNode.desc)) {
            arrayList.add(NativeReadCutHooks.frameType(type2));
        }
        Map<AbstractInsnNode, List<Integer>> map = NativeReadCutHooks.returnRemainders(classNode, methodNode, (Type)listIterator);
        NativeReadCutHooks.carryObservationFrame(methodNode, arrayList, n2);
        LabelNode labelNode = new LabelNode();
        LabelNode labelNode2 = new LabelNode();
        type2 = new LabelNode();
        LabelNode labelNode3 = new LabelNode();
        LabelNode labelNode4 = new LabelNode();
        LabelNode labelNode5 = new LabelNode();
        LabelNode labelNode6 = new LabelNode();
        LabelNode labelNode7 = new LabelNode();
        LabelNode labelNode8 = new LabelNode();
        LabelNode labelNode9 = new LabelNode();
        LabelNode labelNode10 = new LabelNode();
        LabelNode labelNode11 = new LabelNode();
        LabelNode labelNode12 = new LabelNode();
        LabelNode labelNode13 = new LabelNode();
        ArrayList<AbstractInsnNode> arrayList2 = new ArrayList<AbstractInsnNode>();
        for (Object object2 : methodNode.instructions) {
            int n5 = object2.getOpcode();
            if (n5 < 172 || n5 > 177) continue;
            arrayList2.add((AbstractInsnNode)object2);
        }
        ListIterator listIterator2 = new InsnList();
        listIterator2.add((AbstractInsnNode)new LdcInsnNode((Object)MARKER));
        listIterator2.add((AbstractInsnNode)new InsnNode(87));
        listIterator2.add((AbstractInsnNode)new VarInsnNode(25, 0));
        listIterator2.add((AbstractInsnNode)new LdcInsnNode((Object)(classNode.name + "." + methodNode.name + methodNode.desc)));
        listIterator2.add((AbstractInsnNode)new MethodInsnNode(184, FRAME, "open", "(Ljava/lang/Object;Ljava/lang/String;)Lnet/farmingtales/ftboss/item/NativeObservationFrame;", false));
        listIterator2.add((AbstractInsnNode)new VarInsnNode(58, n2));
        listIterator2.add((AbstractInsnNode)labelNode);
        NativeReadCutHooks.observe((InsnList)listIterator2, string);
        listIterator2.add((AbstractInsnNode)labelNode2);
        listIterator2.add((AbstractInsnNode)new JumpInsnNode(167, labelNode3));
        listIterator2.add((AbstractInsnNode)type2);
        listIterator2.add((AbstractInsnNode)NativeReadCutHooks.observationFrame(arrayList, n2, -1, null, "java/lang/Throwable"));
        listIterator2.add((AbstractInsnNode)new VarInsnNode(58, n4));
        NativeReadCutHooks.record(methodNode, (InsnList)listIterator2, n2, n4, 0, arrayList, -1, null);
        listIterator2.add((AbstractInsnNode)labelNode3);
        listIterator2.add((AbstractInsnNode)NativeReadCutHooks.observationFrame(arrayList, n2, -1, null, null));
        methodNode.instructions.insert((InsnList)listIterator2);
        for (AbstractInsnNode abstractInsnNode : arrayList2) {
            InsnList insnList = new InsnList();
            if (listIterator.getSort() != 0) {
                insnList.add((AbstractInsnNode)new VarInsnNode(listIterator.getOpcode(54), n));
            }
            Iterator iterator = ((List)map.get(abstractInsnNode)).iterator();
            while (iterator.hasNext()) {
                int n6 = (Integer)iterator.next();
                insnList.add((AbstractInsnNode)new InsnNode(n6 == 2 ? 88 : 87));
            }
            insnList.add((AbstractInsnNode)new JumpInsnNode(167, labelNode5));
            methodNode.instructions.insertBefore(abstractInsnNode, insnList);
            methodNode.instructions.remove(abstractInsnNode);
        }
        object2 = List.of(classNode.name);
        methodNode.instructions.add((AbstractInsnNode)labelNode4);
        methodNode.instructions.add((AbstractInsnNode)labelNode6);
        methodNode.instructions.add((AbstractInsnNode)NativeReadCutHooks.observationFrame((List<Object>)object2, n2, -1, null, "java/lang/Throwable"));
        methodNode.instructions.add((AbstractInsnNode)new VarInsnNode(58, n3));
        methodNode.instructions.add((AbstractInsnNode)new VarInsnNode(25, n2));
        methodNode.instructions.add((AbstractInsnNode)new VarInsnNode(25, n3));
        methodNode.instructions.add((AbstractInsnNode)new MethodInsnNode(182, FRAME, "originalFailed", "(Ljava/lang/Throwable;)V", false));
        methodNode.instructions.add((AbstractInsnNode)labelNode7);
        NativeReadCutHooks.observe(methodNode.instructions, string);
        methodNode.instructions.add((AbstractInsnNode)labelNode8);
        methodNode.instructions.add((AbstractInsnNode)new JumpInsnNode(167, labelNode10));
        methodNode.instructions.add((AbstractInsnNode)labelNode9);
        methodNode.instructions.add((AbstractInsnNode)NativeReadCutHooks.observationFrame((List<Object>)object2, n2, n3, "java/lang/Throwable", "java/lang/Throwable"));
        methodNode.instructions.add((AbstractInsnNode)new VarInsnNode(58, n4));
        NativeReadCutHooks.record(methodNode, methodNode.instructions, n2, n4, 2, (List<Object>)object2, n3, "java/lang/Throwable");
        methodNode.instructions.add((AbstractInsnNode)labelNode10);
        methodNode.instructions.add((AbstractInsnNode)NativeReadCutHooks.observationFrame((List<Object>)object2, n2, n3, "java/lang/Throwable", null));
        methodNode.instructions.add((AbstractInsnNode)new VarInsnNode(25, n3));
        methodNode.instructions.add((AbstractInsnNode)new InsnNode(191));
        if (!arrayList2.isEmpty()) {
            int n7 = listIterator.getSort() == 0 ? -1 : n;
            methodNode.instructions.add((AbstractInsnNode)labelNode5);
            methodNode.instructions.add((AbstractInsnNode)NativeReadCutHooks.observationFrame((List<Object>)object2, n2, n7, NativeReadCutHooks.frameType((Type)listIterator), null));
            NativeReadCutHooks.observe(methodNode.instructions, string);
            methodNode.instructions.add((AbstractInsnNode)labelNode11);
            methodNode.instructions.add((AbstractInsnNode)new JumpInsnNode(167, labelNode13));
            methodNode.instructions.add((AbstractInsnNode)labelNode12);
            methodNode.instructions.add((AbstractInsnNode)NativeReadCutHooks.observationFrame((List<Object>)object2, n2, n7, NativeReadCutHooks.frameType((Type)listIterator), "java/lang/Throwable"));
            methodNode.instructions.add((AbstractInsnNode)new VarInsnNode(58, n4));
            NativeReadCutHooks.record(methodNode, methodNode.instructions, n2, n4, 1, (List<Object>)object2, n7, NativeReadCutHooks.frameType((Type)listIterator));
            methodNode.instructions.add((AbstractInsnNode)labelNode13);
            methodNode.instructions.add((AbstractInsnNode)NativeReadCutHooks.observationFrame((List<Object>)object2, n2, n7, NativeReadCutHooks.frameType((Type)listIterator), null));
            if (listIterator.getSort() != 0) {
                methodNode.instructions.add((AbstractInsnNode)new VarInsnNode(listIterator.getOpcode(21), n));
            }
            methodNode.instructions.add((AbstractInsnNode)new InsnNode(listIterator.getOpcode(172)));
            methodNode.tryCatchBlocks.add(new TryCatchBlockNode(labelNode5, labelNode11, labelNode12, "java/lang/Throwable"));
        }
        methodNode.tryCatchBlocks.add(new TryCatchBlockNode(labelNode, labelNode2, (LabelNode)type2, "java/lang/Throwable"));
        methodNode.tryCatchBlocks.add(new TryCatchBlockNode(labelNode3, labelNode4, labelNode6, "java/lang/Throwable"));
        methodNode.tryCatchBlocks.add(new TryCatchBlockNode(labelNode7, labelNode8, labelNode9, "java/lang/Throwable"));
        methodNode.maxLocals = n4 + 1;
        methodNode.maxStack = Math.max(methodNode.maxStack, 3);
        return true;
    }

    private static Map<AbstractInsnNode, List<Integer>> returnRemainders(ClassNode classNode, MethodNode methodNode, Type type) {
        Frame[] frameArray;
        try {
            frameArray = new Analyzer((Interpreter)new BasicInterpreter()).analyze(classNode.name, methodNode);
        }
        catch (AnalyzerException analyzerException) {
            throw new IllegalStateException("Invalid original read-cut control flow " + classNode.name + "." + methodNode.name + methodNode.desc, analyzerException);
        }
        IdentityHashMap<AbstractInsnNode, List<Integer>> identityHashMap = new IdentityHashMap<AbstractInsnNode, List<Integer>>();
        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode abstractInsnNode = methodNode.instructions.get(i);
            int n = abstractInsnNode.getOpcode();
            if (n < 172 || n > 177) continue;
            Frame frame = frameArray[i];
            if (frame == null) {
                identityHashMap.put(abstractInsnNode, List.of());
                continue;
            }
            int n2 = frame.getStackSize() - (type.getSort() == 0 ? 0 : 1);
            if (n2 < 0) {
                throw new IllegalStateException("Missing original return value " + methodNode.name + methodNode.desc);
            }
            ArrayList<Integer> arrayList = new ArrayList<Integer>();
            for (int j = n2 - 1; j >= 0; --j) {
                arrayList.add(((BasicValue)frame.getStack(j)).getSize());
            }
            identityHashMap.put(abstractInsnNode, arrayList);
        }
        return identityHashMap;
    }

    private static void record(MethodNode methodNode, InsnList insnList, int n, int n2, int n3, List<Object> list, int n4, Object object) {
        insnList.add((AbstractInsnNode)new VarInsnNode(25, n));
        insnList.add((AbstractInsnNode)new InsnNode(3 + n3));
        insnList.add((AbstractInsnNode)new VarInsnNode(25, n2));
        insnList.add((AbstractInsnNode)new MethodInsnNode(182, FRAME, "observationFailed", "(ILjava/lang/Throwable;)V", false));
        LabelNode labelNode = new LabelNode();
        LabelNode labelNode2 = new LabelNode();
        LabelNode labelNode3 = new LabelNode();
        LabelNode labelNode4 = new LabelNode();
        insnList.add((AbstractInsnNode)labelNode);
        insnList.add((AbstractInsnNode)new VarInsnNode(25, 0));
        insnList.add((AbstractInsnNode)new VarInsnNode(25, n));
        insnList.add((AbstractInsnNode)new MethodInsnNode(184, OBSERVER, "failed", "(Ljava/lang/Object;Lnet/farmingtales/ftboss/item/NativeObservationFrame;)V", false));
        insnList.add((AbstractInsnNode)labelNode2);
        insnList.add((AbstractInsnNode)new JumpInsnNode(167, labelNode4));
        insnList.add((AbstractInsnNode)labelNode3);
        insnList.add((AbstractInsnNode)NativeReadCutHooks.observationFrame(list, n, n4, object, "java/lang/Throwable"));
        insnList.add((AbstractInsnNode)new VarInsnNode(58, n2));
        insnList.add((AbstractInsnNode)new VarInsnNode(25, n));
        insnList.add((AbstractInsnNode)new InsnNode(6));
        insnList.add((AbstractInsnNode)new VarInsnNode(25, n2));
        insnList.add((AbstractInsnNode)new MethodInsnNode(182, FRAME, "observationFailed", "(ILjava/lang/Throwable;)V", false));
        insnList.add((AbstractInsnNode)labelNode4);
        methodNode.tryCatchBlocks.add(new TryCatchBlockNode(labelNode, labelNode2, labelNode3, "java/lang/Throwable"));
    }

    private static void carryObservationFrame(MethodNode methodNode, List<Object> list, int n) {
        ArrayList<Object> arrayList = new ArrayList<Object>(list);
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
                    arrayList = new ArrayList(arrayList.subList(0, arrayList.size() - frameNode.local.size()));
                    break;
                }
                case 3: 
                case 4: {
                    break;
                }
                default: {
                    throw new IllegalStateException("Unknown original StackMap frame");
                }
            }
            frameNode.type = 0;
            if (frameNode.stack == null) {
                frameNode.stack = new ArrayList();
            }
            frameNode.local = new ArrayList<Object>(arrayList);
            NativeReadCutHooks.appendLocal(frameNode.local, n, FRAME);
        }
    }

    private static FrameNode observationFrame(List<Object> list, int n, int n2, Object object, String string) {
        Object[] objectArray;
        ArrayList<Object> arrayList = new ArrayList<Object>(list);
        if (n2 >= 0 && n2 < n) {
            NativeReadCutHooks.appendLocal(arrayList, n2, object);
        }
        NativeReadCutHooks.appendLocal(arrayList, n, FRAME);
        if (n2 > n) {
            NativeReadCutHooks.appendLocal(arrayList, n2, object);
        }
        int n3 = arrayList.size();
        Object[] objectArray2 = arrayList.toArray();
        int n4 = string == null ? 0 : 1;
        if (string == null) {
            objectArray = new Object[]{};
        } else {
            Object[] objectArray3 = new Object[1];
            objectArray = objectArray3;
            objectArray3[0] = string;
        }
        return new FrameNode(0, n3, objectArray2, n4, objectArray);
    }

    private static void appendLocal(List<Object> list, int n, Object object) {
        int n2 = 0;
        for (Object object2 : list) {
            n2 += object2.equals(Opcodes.LONG) || object2.equals(Opcodes.DOUBLE) ? 2 : 1;
        }
        if (n2 > n) {
            throw new IllegalStateException("Overlapping original StackMap local");
        }
        while (n2++ < n) {
            list.add(Opcodes.TOP);
        }
        list.add(object);
    }

    private static void observe(InsnList insnList, String string) {
        insnList.add((AbstractInsnNode)new VarInsnNode(25, 0));
        insnList.add((AbstractInsnNode)new MethodInsnNode(184, OBSERVER, string, "(Ljava/lang/Object;)V", false));
    }

    private static Object frameType(Type type) {
        return switch (type.getSort()) {
            case 0 -> null;
            case 7 -> Opcodes.LONG;
            case 8 -> Opcodes.DOUBLE;
            case 6 -> Opcodes.FLOAT;
            case 9 -> type.getDescriptor();
            case 10 -> type.getInternalName();
            default -> Opcodes.INTEGER;
        };
    }

    private static FrameNode frame(String string, int n, Object object, String string2) {
        Object[] objectArray;
        ArrayList<Object> arrayList = new ArrayList<Object>();
        arrayList.add(string);
        if (n >= 0) {
            while (arrayList.size() < n) {
                arrayList.add(Opcodes.TOP);
            }
            arrayList.add(object);
        }
        int n2 = arrayList.size();
        Object[] objectArray2 = arrayList.toArray();
        int n3 = string2 == null ? 0 : 1;
        if (string2 == null) {
            objectArray = new Object[]{};
        } else {
            Object[] objectArray3 = new Object[1];
            objectArray = objectArray3;
            objectArray3[0] = string2;
        }
        return new FrameNode(0, n2, objectArray2, n3, objectArray);
    }

    private record Rule(String official, String srg, String descriptor) {
    }
}

