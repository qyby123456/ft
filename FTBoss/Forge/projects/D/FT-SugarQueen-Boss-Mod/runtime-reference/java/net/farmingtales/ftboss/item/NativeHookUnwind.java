/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.modlauncher.api.INameMappingService$Domain
 *  net.minecraftforge.fml.util.ObfuscationReflectionHelper
 *  org.objectweb.asm.Opcodes
 *  org.objectweb.asm.Type
 *  org.objectweb.asm.tree.AbstractInsnNode
 *  org.objectweb.asm.tree.ClassNode
 *  org.objectweb.asm.tree.FrameNode
 *  org.objectweb.asm.tree.IincInsnNode
 *  org.objectweb.asm.tree.InsnList
 *  org.objectweb.asm.tree.InsnNode
 *  org.objectweb.asm.tree.LabelNode
 *  org.objectweb.asm.tree.MethodInsnNode
 *  org.objectweb.asm.tree.MethodNode
 *  org.objectweb.asm.tree.TryCatchBlockNode
 *  org.objectweb.asm.tree.TypeInsnNode
 *  org.objectweb.asm.tree.VarInsnNode
 */
package net.farmingtales.ftboss.item;

import cpw.mods.modlauncher.api.INameMappingService;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class NativeHookUnwind {
    private NativeHookUnwind() {
    }

    public static int apply(ClassNode classNode, String string) {
        List<String> list;
        String string2;
        if (string.endsWith("NativeUseTickIngressMixin")) {
            string2 = "ftboss$v9$nativeUnwind$useTick";
            list = List.of("m_5929_");
        } else if (string.endsWith("NativeReleaseIngressMixin")) {
            string2 = "ftboss$v9$nativeUnwind$release";
            list = List.of("m_5551_");
        } else if (string.endsWith("NativeLeftClickIngressMixin")) {
            string2 = "ftboss$v9$nativeUnwind$leftClick";
            list = List.of("onLeftClick");
        } else if (string.endsWith("NativeAncientMeleeIngressMixin")) {
            string2 = "ftboss$v9$nativeUnwind$melee";
            list = List.of("m_7579_");
        } else if (string.endsWith("NativeWearTokenMixin")) {
            string2 = "ftboss$v9$wearUnwind";
            list = List.of("hurtAndBreak", "m_41622_");
        } else {
            if (string.endsWith("NativeIronSelectionMixin")) {
                return NativeHookUnwind.installIronUse(classNode);
            }
            if (string.endsWith("NativeRecastTokenCleanupMixin")) {
                string2 = "ftboss$v9$recastUnwind";
                list = List.of("removeRecast");
            } else {
                if (string.endsWith("FoodWeaponStaffCastMixin")) {
                    return NativeHookUnwind.install(classNode, "attemptInitiateCast", "ftboss$v9$ironAttemptUnwind") + NativeHookUnwind.install(classNode, "castSpell", "ftboss$v9$ironCastUnwind");
                }
                return 0;
            }
        }
        int n = 0;
        for (String string3 : list) {
            n += NativeHookUnwind.install(classNode, string3, string2);
        }
        return n;
    }

    private static int installIronUse(ClassNode classNode) {
        AbstractInsnNode abstractInsnNode;
        IincInsnNode iincInsnNode;
        VarInsnNode varInsnNode;
        if (!classNode.name.equals("io/redspace/ironsspellbooks/item/CastingItem")) {
            throw new IllegalStateException("Unexpected Iron use host " + classNode.name);
        }
        String string = "net/farmingtales/ftboss/integ/NativeIronExecution$UseFrame";
        String string2 = "(L" + string + ";)V";
        String string3 = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;";
        String string4 = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V";
        MethodNode methodNode2 = NativeHookUnwind.ironUseHook(classNode, "ftboss$v9$ironUseBegin", string4, false);
        MethodNode methodNode3 = NativeHookUnwind.ironUseHook(classNode, "ftboss$v9$ironUseReturn", string4, false);
        MethodNode methodNode4 = NativeHookUnwind.ironUseHook(classNode, "ftboss$v9$ironUseUnwind", string2, true);
        String string5 = ObfuscationReflectionHelper.remapName((INameMappingService.Domain)INameMappingService.Domain.METHOD, (String)"m_7203_");
        if (!string5.equals("use") && !string5.equals("m_7203_")) {
            throw new IllegalStateException("Unsupported Iron use namespace " + string5);
        }
        List<MethodNode> list = classNode.methods.stream().filter(methodNode -> methodNode.name.equals(string5) && methodNode.desc.equals(string3) && (methodNode.access & 0x508) == 0).toList();
        if (list.size() != 1) {
            throw new IllegalStateException("Missing unique actual Iron use method " + classNode.name);
        }
        MethodNode methodNode5 = list.get(0);
        String string6 = "ftboss$v9$unwindGuard$" + methodNode4.name;
        for (Object object : methodNode5.instructions) {
            if (!(object instanceof MethodInsnNode)) continue;
            MethodInsnNode methodInsnNode = (MethodInsnNode)object;
            if (!methodInsnNode.owner.equals(classNode.name) || !methodInsnNode.name.equals(string6) || !methodInsnNode.desc.equals(string2)) continue;
            return 0;
        }
        ArrayList arrayList = new ArrayList();
        if (!methodNode5.tryCatchBlocks.isEmpty()) {
            throw new IllegalStateException("Conflicting Iron use exception regions " + classNode.name);
        }
        for (MethodInsnNode methodInsnNode : methodNode5.instructions) {
            block14: {
                block13: {
                    if (methodInsnNode.getOpcode() == 176) {
                        arrayList.add(methodInsnNode);
                    }
                    if (!(methodInsnNode instanceof VarInsnNode)) break block13;
                    varInsnNode = (VarInsnNode)methodInsnNode;
                    if (varInsnNode.var < 4 && varInsnNode.getOpcode() >= 54 && varInsnNode.getOpcode() <= 58) break block14;
                }
                if (!(methodInsnNode instanceof IincInsnNode)) continue;
                iincInsnNode = (IincInsnNode)methodInsnNode;
                if (iincInsnNode.var >= 4) continue;
            }
            throw new IllegalStateException("Overwritten Iron use argument slot " + classNode.name);
        }
        if (arrayList.size() != 6) {
            throw new IllegalStateException("Expected six actual Iron use return sites " + classNode.name);
        }
        int n = methodNode5.maxLocals;
        int n2 = n + 1;
        NativeHookUnwind.ironUseFrames(methodNode5, n, string);
        varInsnNode = new LabelNode();
        iincInsnNode = new LabelNode();
        LabelNode labelNode = new LabelNode();
        InsnList insnList = new InsnList();
        insnList.add((AbstractInsnNode)new TypeInsnNode(187, string));
        insnList.add((AbstractInsnNode)new InsnNode(89));
        insnList.add((AbstractInsnNode)new MethodInsnNode(183, string, "<init>", "()V", false));
        insnList.add((AbstractInsnNode)new VarInsnNode(58, n));
        insnList.add((AbstractInsnNode)varInsnNode);
        insnList.add(NativeHookUnwind.ironUseCall(classNode, methodNode2, n));
        methodNode5.instructions.insert(insnList);
        MethodNode methodNode6 = arrayList.iterator();
        while (methodNode6.hasNext()) {
            abstractInsnNode = (AbstractInsnNode)methodNode6.next();
            methodNode5.instructions.insertBefore(abstractInsnNode, NativeHookUnwind.ironUseCall(classNode, methodNode3, n));
        }
        methodNode6 = NativeHookUnwind.ironUseGuard(classNode, methodNode4, string2, string);
        abstractInsnNode = new InsnList();
        abstractInsnNode.add((AbstractInsnNode)iincInsnNode);
        abstractInsnNode.add((AbstractInsnNode)labelNode);
        ArrayList<Object> arrayList2 = new ArrayList<Object>();
        for (int i = 0; i < n; ++i) {
            arrayList2.add(Opcodes.TOP);
        }
        arrayList2.add(string);
        abstractInsnNode.add((AbstractInsnNode)new FrameNode(0, arrayList2.size(), arrayList2.toArray(), 1, new Object[]{"java/lang/Throwable"}));
        abstractInsnNode.add((AbstractInsnNode)new VarInsnNode(58, n2));
        abstractInsnNode.add((AbstractInsnNode)new VarInsnNode(25, n));
        abstractInsnNode.add((AbstractInsnNode)new MethodInsnNode(184, classNode.name, methodNode6.name, string2, false));
        abstractInsnNode.add((AbstractInsnNode)new VarInsnNode(25, n2));
        abstractInsnNode.add((AbstractInsnNode)new InsnNode(191));
        methodNode5.instructions.add((InsnList)abstractInsnNode);
        methodNode5.tryCatchBlocks.add(new TryCatchBlockNode((LabelNode)varInsnNode, (LabelNode)iincInsnNode, labelNode, "java/lang/Throwable"));
        methodNode5.maxLocals = n2 + 1;
        methodNode5.maxStack = Math.max(6, methodNode5.maxStack + 5);
        return 1;
    }

    private static MethodNode ironUseHook(ClassNode classNode, String string, String string2, boolean bl) {
        List<MethodNode> list = classNode.methods.stream().filter(methodNode -> (methodNode.name.equals(string) || methodNode.name.startsWith(string + "_$md$") || methodNode.name.matches("md[^$]+\\$" + Pattern.quote(string) + "\\$[0-9a-f]+")) && methodNode.desc.equals(string2) && (methodNode.access & 8) != 0 == bl).toList();
        if (list.size() != 1) {
            throw new IllegalStateException("Missing unique actual merged Iron use hook " + classNode.name + "." + string);
        }
        return list.get(0);
    }

    private static InsnList ironUseCall(ClassNode classNode, MethodNode methodNode, int n) {
        InsnList insnList = new InsnList();
        for (int i = 0; i < 4; ++i) {
            insnList.add((AbstractInsnNode)new VarInsnNode(25, i));
        }
        insnList.add((AbstractInsnNode)new VarInsnNode(25, n));
        insnList.add((AbstractInsnNode)new MethodInsnNode(183, classNode.name, methodNode.name, methodNode.desc, false));
        return insnList;
    }

    /*
     * WARNING - void declaration
     */
    private static void ironUseFrames(MethodNode methodNode, int n, String string) {
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("io/redspace/ironsspellbooks/item/CastingItem");
        for (Type object : Type.getArgumentTypes((String)methodNode.desc)) {
            arrayList.add(object.getInternalName());
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
                    void var7_12;
                    boolean bl = false;
                    while (var7_12 < frameNode.local.size()) {
                        arrayList.remove(arrayList.size() - 1);
                        ++var7_12;
                    }
                    break;
                }
                case 3: 
                case 4: {
                    break;
                }
                default: {
                    throw new IllegalStateException("Unsupported Iron use frame " + frameNode.type);
                }
            }
            ArrayList<Object> arrayList2 = new ArrayList<Object>(arrayList);
            int n2 = 0;
            for (Object e : arrayList) {
                n2 += Opcodes.LONG.equals(e) || Opcodes.DOUBLE.equals(e) ? 2 : 1;
            }
            if (n2 > n) {
                throw new IllegalStateException("Iron use original frame exceeds maxLocals");
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

    private static MethodNode ironUseGuard(ClassNode classNode, MethodNode methodNode2, String string, String string2) {
        String string3 = "ftboss$v9$unwindGuard$" + methodNode2.name;
        if (classNode.methods.stream().anyMatch(methodNode -> methodNode.name.equals(string3))) {
            throw new IllegalStateException("Conflicting Iron use guard " + string3);
        }
        MethodNode methodNode3 = new MethodNode(4106, string3, string, null, null);
        LabelNode labelNode = new LabelNode();
        LabelNode labelNode2 = new LabelNode();
        LabelNode labelNode3 = new LabelNode();
        methodNode3.instructions.add((AbstractInsnNode)labelNode);
        methodNode3.instructions.add((AbstractInsnNode)new VarInsnNode(25, 0));
        methodNode3.instructions.add((AbstractInsnNode)new MethodInsnNode(184, classNode.name, methodNode2.name, string, false));
        methodNode3.instructions.add((AbstractInsnNode)labelNode2);
        methodNode3.instructions.add((AbstractInsnNode)new InsnNode(177));
        methodNode3.instructions.add((AbstractInsnNode)labelNode3);
        methodNode3.instructions.add((AbstractInsnNode)new FrameNode(0, 1, new Object[]{string2}, 1, new Object[]{"java/lang/Throwable"}));
        methodNode3.instructions.add((AbstractInsnNode)new InsnNode(87));
        methodNode3.instructions.add((AbstractInsnNode)new InsnNode(177));
        methodNode3.tryCatchBlocks.add(new TryCatchBlockNode(labelNode, labelNode2, labelNode3, "java/lang/Throwable"));
        methodNode3.maxLocals = 1;
        methodNode3.maxStack = 1;
        classNode.methods.add(methodNode3);
        return methodNode3;
    }

    private static int install(ClassNode classNode, String string, String string2) {
        MethodNode methodNode2 = classNode.methods.stream().filter(methodNode -> (methodNode.name.equals(string2) || methodNode.name.startsWith(string2 + "_$md$") || methodNode.name.matches("md[^$]+\\$" + Pattern.quote(string2) + "\\$[0-9a-f]+")) && methodNode.desc.equals("()V")).findFirst().orElse(null);
        if (methodNode2 == null) {
            throw new IllegalStateException("Missing actual merged Native unwind handler " + classNode.name + "." + string2);
        }
        MethodNode methodNode3 = NativeHookUnwind.guard(classNode, methodNode2);
        int n = 0;
        for (MethodNode methodNode4 : classNode.methods) {
            boolean bl;
            MethodInsnNode methodInsnNode;
            AbstractInsnNode abstractInsnNode2;
            if (!methodNode4.name.equals(string) || (methodNode4.access & 0x500) != 0) continue;
            boolean bl2 = false;
            for (AbstractInsnNode abstractInsnNode2 : methodNode4.instructions) {
                if (!(abstractInsnNode2 instanceof MethodInsnNode)) continue;
                methodInsnNode = (MethodInsnNode)abstractInsnNode2;
                if (!methodInsnNode.owner.equals(classNode.name) || !methodInsnNode.name.equals(methodNode3.name) || !methodInsnNode.desc.equals("()V")) continue;
                bl2 = true;
            }
            if (bl2) continue;
            int n2 = methodNode4.maxLocals;
            abstractInsnNode2 = new LabelNode();
            methodInsnNode = new LabelNode();
            LabelNode labelNode = new LabelNode();
            methodNode4.instructions.insert(abstractInsnNode2);
            InsnList insnList = new InsnList();
            insnList.add((AbstractInsnNode)methodInsnNode);
            insnList.add((AbstractInsnNode)labelNode);
            ArrayList<Object> arrayList = new ArrayList<Object>();
            if ((methodNode4.access & 8) == 0) {
                arrayList.add(classNode.name);
            }
            for (Type type : Type.getArgumentTypes((String)methodNode4.desc)) {
                arrayList.add(switch (type.getSort()) {
                    case 1, 2, 3, 4, 5 -> Opcodes.INTEGER;
                    case 6 -> Opcodes.FLOAT;
                    case 7 -> Opcodes.LONG;
                    case 8 -> Opcodes.DOUBLE;
                    default -> type.getInternalName();
                });
            }
            insnList.add((AbstractInsnNode)new FrameNode(0, arrayList.size(), arrayList.toArray(), 1, new Object[]{"java/lang/Throwable"}));
            insnList.add((AbstractInsnNode)new VarInsnNode(58, n2));
            boolean bl3 = bl = (methodNode3.access & 8) != 0;
            if (!bl) {
                insnList.add((AbstractInsnNode)new VarInsnNode(25, 0));
            }
            insnList.add((AbstractInsnNode)new MethodInsnNode(bl ? 184 : 183, classNode.name, methodNode3.name, "()V", false));
            insnList.add((AbstractInsnNode)new VarInsnNode(25, n2));
            insnList.add((AbstractInsnNode)new InsnNode(191));
            methodNode4.instructions.add(insnList);
            methodNode4.tryCatchBlocks.add(new TryCatchBlockNode((LabelNode)abstractInsnNode2, (LabelNode)methodInsnNode, labelNode, "java/lang/Throwable"));
            methodNode4.maxLocals = n2 + 1;
            methodNode4.maxStack = Math.max(2, methodNode4.maxStack);
            ++n;
        }
        return n;
    }

    private static MethodNode guard(ClassNode classNode, MethodNode methodNode2) {
        Object[] objectArray;
        String string = "ftboss$v9$unwindGuard$" + methodNode2.name;
        MethodNode methodNode3 = classNode.methods.stream().filter(methodNode -> methodNode.name.equals(string) && methodNode.desc.equals("()V")).findFirst().orElse(null);
        if (methodNode3 != null) {
            return methodNode3;
        }
        boolean bl = (methodNode2.access & 8) != 0;
        MethodNode methodNode4 = new MethodNode(0x1002 | (bl ? 8 : 0), string, "()V", null, null);
        LabelNode labelNode = new LabelNode();
        LabelNode labelNode2 = new LabelNode();
        LabelNode labelNode3 = new LabelNode();
        methodNode4.instructions.add((AbstractInsnNode)labelNode);
        if (!bl) {
            methodNode4.instructions.add((AbstractInsnNode)new VarInsnNode(25, 0));
        }
        methodNode4.instructions.add((AbstractInsnNode)new MethodInsnNode(bl ? 184 : 183, classNode.name, methodNode2.name, "()V", false));
        methodNode4.instructions.add((AbstractInsnNode)labelNode2);
        methodNode4.instructions.add((AbstractInsnNode)new InsnNode(177));
        methodNode4.instructions.add((AbstractInsnNode)labelNode3);
        InsnList insnList = methodNode4.instructions;
        int n = bl ? 0 : 1;
        if (bl) {
            objectArray = new Object[]{};
        } else {
            Object[] objectArray2 = new Object[1];
            objectArray = objectArray2;
            objectArray2[0] = classNode.name;
        }
        insnList.add((AbstractInsnNode)new FrameNode(0, n, objectArray, 1, new Object[]{"java/lang/Throwable"}));
        methodNode4.instructions.add((AbstractInsnNode)new InsnNode(87));
        methodNode4.instructions.add((AbstractInsnNode)new InsnNode(177));
        methodNode4.tryCatchBlocks.add(new TryCatchBlockNode(labelNode, labelNode2, labelNode3, "java/lang/Throwable"));
        methodNode4.maxLocals = bl ? 0 : 1;
        methodNode4.maxStack = 1;
        classNode.methods.add(methodNode4);
        return methodNode4;
    }
}

