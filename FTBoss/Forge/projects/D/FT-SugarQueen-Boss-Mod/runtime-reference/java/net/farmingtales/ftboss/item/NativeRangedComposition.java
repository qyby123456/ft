/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.tree.AbstractInsnNode
 *  org.objectweb.asm.tree.ClassNode
 *  org.objectweb.asm.tree.InsnList
 *  org.objectweb.asm.tree.InsnNode
 *  org.objectweb.asm.tree.MethodInsnNode
 *  org.objectweb.asm.tree.MethodNode
 *  org.objectweb.asm.tree.VarInsnNode
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class NativeRangedComposition {
    private static final String BOW = "net/minecraft/world/item/BowItem";
    private static final String CROSSBOW = "net/minecraft/world/item/CrossbowItem";
    private static final String LEVEL = "net/minecraft/world/level/Level";
    private static final String ENTITY = "Lnet/minecraft/world/entity/Entity;";
    private static final String ARROW = "Lnet/minecraft/world/entity/projectile/AbstractArrow;";
    private static final String RELEASE_ARGUMENTS = "Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I";
    private static final String RELEASE = "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V";
    private static final String SHOOT = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;FZFFF)V";
    private static final String ADD = "(Lnet/minecraft/world/entity/Entity;)Z";
    private static final String CROSSBOW_ADD = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;)Z";
    private static final String BOW_ADD = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Z";
    private static final String BOW_PREPARE = "(Lnet/minecraft/world/item/BowItem;Lnet/minecraft/world/entity/projectile/AbstractArrow;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Lnet/minecraft/world/entity/projectile/AbstractArrow;";

    private NativeRangedComposition() {
    }

    public static int apply(ClassNode classNode, String string) {
        MethodInsnNode methodInsnNode;
        boolean bl = string.equals("net.farmingtales.ftboss.mixin.FoodWeaponBowMixin");
        boolean bl2 = string.equals("net.farmingtales.ftboss.mixin.FoodWeaponCrossbowMixin");
        if (!bl && !bl2) {
            return 0;
        }
        if (!classNode.name.equals(bl ? BOW : CROSSBOW)) {
            throw new IllegalStateException("P11 ranged host " + classNode.name);
        }
        MethodNode methodNode = NativeRangedComposition.original(classNode, bl ? "releaseUsing" : "shootProjectile", bl ? "m_5551_" : "m_40894_", bl ? RELEASE : SHOOT);
        boolean bl3 = methodNode.name.startsWith("m_");
        if ((methodNode.access & 8) != 0 != bl2) {
            throw new IllegalStateException("P11 ranged access " + methodNode.name);
        }
        String string2 = bl3 ? "Srg" : "";
        MethodNode methodNode2 = NativeRangedComposition.helper(classNode, (bl ? "ftboss$bowLaunch" : "ftboss$crossbowLaunch") + string2, bl ? BOW_ADD : CROSSBOW_ADD, bl2);
        MethodInsnNode methodInsnNode2 = NativeRangedComposition.invocation(methodNode, LEVEL, bl3 ? "m_7967_" : "addFreshEntity", ADD);
        MethodNode methodNode3 = bl ? NativeRangedComposition.helper(classNode, "ftboss$prepareArrow" + string2, BOW_PREPARE, false) : null;
        MethodInsnNode methodInsnNode3 = methodInsnNode = bl ? NativeRangedComposition.invocation(methodNode, BOW, "customArrow", "(Lnet/minecraft/world/entity/projectile/AbstractArrow;)Lnet/minecraft/world/entity/projectile/AbstractArrow;") : null;
        if (bl) {
            NativeRangedComposition.replace(classNode, methodNode, methodInsnNode, methodNode3, true);
        }
        NativeRangedComposition.replace(classNode, methodNode, methodInsnNode2, methodNode2, bl);
        if (bl) {
            methodNode.maxStack += 5;
        }
        return bl ? 2 : 1;
    }

    private static MethodNode original(ClassNode classNode, String string, String string2, String string3) {
        ArrayList<MethodNode> arrayList = new ArrayList<MethodNode>();
        for (MethodNode methodNode : classNode.methods) {
            if (!methodNode.name.equals(string) && !methodNode.name.equals(string2) || !methodNode.desc.equals(string3)) continue;
            arrayList.add(methodNode);
        }
        if (arrayList.size() != 1) {
            throw new IllegalStateException("P11 exact ranged method " + string + " count=" + arrayList.size());
        }
        return (MethodNode)arrayList.get(0);
    }

    private static MethodNode helper(ClassNode classNode, String string, String string2, boolean bl) {
        ArrayList<MethodNode> arrayList = new ArrayList<MethodNode>();
        for (MethodNode methodNode : classNode.methods) {
            if (!methodNode.name.equals(string) || !methodNode.desc.equals(string2)) continue;
            arrayList.add(methodNode);
        }
        if (arrayList.size() != 1) {
            throw new IllegalStateException("P11 merged ranged helper " + string + " count=" + arrayList.size());
        }
        MethodNode methodNode = (MethodNode)arrayList.get(0);
        if ((methodNode.access & 8) != 0 != bl || (methodNode.access & 2) == 0) {
            throw new IllegalStateException("P11 merged ranged helper access " + string);
        }
        return methodNode;
    }

    private static MethodInsnNode invocation(MethodNode methodNode, String string, String string2, String string3) {
        ArrayList<MethodInsnNode> arrayList = new ArrayList<MethodInsnNode>();
        for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
            if (!(abstractInsnNode instanceof MethodInsnNode)) continue;
            MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;
            if (!methodInsnNode.owner.equals(string) || !methodInsnNode.name.equals(string2) || !methodInsnNode.desc.equals(string3)) continue;
            arrayList.add(methodInsnNode);
        }
        if (arrayList.size() != 1 || ((MethodInsnNode)arrayList.get(0)).getOpcode() != 182 || ((MethodInsnNode)arrayList.get((int)0)).itf) {
            throw new IllegalStateException("P11 exact SDK invocation " + methodNode.name + " " + string + "." + string2 + " count=" + arrayList.size());
        }
        return (MethodInsnNode)arrayList.get(0);
    }

    private static void replace(ClassNode classNode, MethodNode methodNode, MethodInsnNode methodInsnNode, MethodNode methodNode2, boolean bl) {
        if (bl) {
            InsnList insnList = new InsnList();
            insnList.add((AbstractInsnNode)new VarInsnNode(25, 0));
            insnList.add((AbstractInsnNode)new InsnNode(91));
            insnList.add((AbstractInsnNode)new InsnNode(87));
            insnList.add((AbstractInsnNode)new VarInsnNode(25, 1));
            insnList.add((AbstractInsnNode)new VarInsnNode(25, 2));
            insnList.add((AbstractInsnNode)new VarInsnNode(25, 3));
            insnList.add((AbstractInsnNode)new VarInsnNode(21, 4));
            methodNode.instructions.insertBefore((AbstractInsnNode)methodInsnNode, insnList);
        }
        methodNode.instructions.set((AbstractInsnNode)methodInsnNode, (AbstractInsnNode)new MethodInsnNode(bl ? 183 : 184, classNode.name, methodNode2.name, methodNode2.desc, false));
    }
}

