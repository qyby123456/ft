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

    public static int apply(ClassNode var0, String var1) {
        boolean var2 = var1.equals("net.farmingtales.ftboss.mixin.FoodWeaponBowMixin");
        boolean var3 = var1.equals("net.farmingtales.ftboss.mixin.FoodWeaponCrossbowMixin");
        if (!var2 && !var3) {
            return 0;
        } else if (!var0.name.equals(var2 ? "net/minecraft/world/item/BowItem" : "net/minecraft/world/item/CrossbowItem")) {
            throw new IllegalStateException("P11 ranged host " + var0.name);
        } else {
            MethodNode var4 = original(
                var0,
                var2 ? "releaseUsing" : "shootProjectile",
                var2 ? "m_5551_" : "m_40894_",
                var2
                    ? "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"
                    : "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;FZFFF)V"
            );
            if (var2 && modernBow(var0, var4)) {
                return 0;
            }
            boolean var5 = var4.name.startsWith("m_");
            if ((var4.access & 8) != 0 != var3) {
                throw new IllegalStateException("P11 ranged access " + var4.name);
            } else {
                String var6 = var5 ? "Srg" : "";
                MethodNode var7 = helper(
                    var0,
                    (var2 ? "ftboss$bowLaunch" : "ftboss$crossbowLaunch") + var6,
                    var2
                        ? "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Z"
                        : "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;)Z",
                    var3
                );
                MethodInsnNode var8 = invocation(
                    var4, "net/minecraft/world/level/Level", var5 ? "m_7967_" : "addFreshEntity", "(Lnet/minecraft/world/entity/Entity;)Z"
                );
                MethodNode var9 = var2
                    ? helper(
                        var0,
                        "ftboss$prepareArrow" + var6,
                        "(Lnet/minecraft/world/item/BowItem;Lnet/minecraft/world/entity/projectile/AbstractArrow;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Lnet/minecraft/world/entity/projectile/AbstractArrow;",
                        false
                    )
                    : null;
                MethodInsnNode var10 = var2
                    ? invocation(
                        var4,
                        "net/minecraft/world/item/BowItem",
                        "customArrow",
                        "(Lnet/minecraft/world/entity/projectile/AbstractArrow;)Lnet/minecraft/world/entity/projectile/AbstractArrow;"
                    )
                    : null;
                if (var2) {
                    replace(var0, var4, var10, var9, true);
                }

                replace(var0, var4, var8, var7, var2);
                if (var2) {
                    var4.maxStack += 5;
                }

                return var2 ? 2 : 1;
            }
        }
    }

    private static boolean modernBow(ClassNode owner, MethodNode release) {
        String shootDescriptor = "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V";
        ArrayList<MethodInsnNode> shooting = new ArrayList<>();
        for (AbstractInsnNode instruction : release.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(BOW) && call.name.equals("shoot") && call.desc.equals(shootDescriptor)) shooting.add(call);
        }
        if (shooting.isEmpty()) return false;
        if (shooting.size() != 1 || shooting.get(0).getOpcode() != 182 || shooting.get(0).itf) throw new IllegalStateException("Changed native shared bow shooting boundary");
        String guardDescriptor = "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;ILorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V";
        ArrayList<MethodNode> guards = new ArrayList<>();
        for (MethodNode method : owner.methods) {
            if (method.name.contains("ftboss$bowLaunch") || method.name.contains("ftboss$prepareArrow")) throw new IllegalStateException("Legacy and native bow factories mixed");
            if (method.name.contains("ftboss$brokenBow") && method.desc.equals(guardDescriptor)) guards.add(method);
        }
        if (guards.size() != 1) throw new IllegalStateException("Native bow broken-item guard missing");
        MethodNode guard = guards.get(0);
        int guardCalls = 0, brokenReads = 0, cancellations = 0, draws = 0;
        for (AbstractInsnNode instruction : release.instructions) {
            if (instruction instanceof MethodInsnNode call) {
                if (call.owner.equals(BOW) && call.name.equals(guard.name) && call.desc.equals(guard.desc)) guardCalls++;
                if (call.owner.equals(BOW) && call.name.equals("draw") && call.desc.equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/List;")) draws++;
                if (call.owner.equals(LEVEL) && call.name.equals("addFreshEntity") || call.owner.equals(BOW) && call.name.equals("customArrow")) throw new IllegalStateException("Legacy direct bow factory remains");
            }
        }
        for (AbstractInsnNode instruction : guard.instructions) {
            if (instruction instanceof MethodInsnNode call) {
                if (call.owner.equals("net/farmingtales/ftboss/item/FoodWeaponLifecycle") && call.name.equals("isBroken") && call.desc.equals("(Lnet/minecraft/world/item/ItemStack;)Z")) brokenReads++;
                if (call.owner.equals("org/spongepowered/asm/mixin/injection/callback/CallbackInfo") && call.name.equals("cancel") && call.desc.equals("()V")) cancellations++;
            }
        }
        if (guardCalls != 1 || brokenReads != 1 || cancellations != 1 || draws != 1) throw new IllegalStateException("Native bow guard/draw composition changed");
        return true;
    }

    private static MethodNode original(ClassNode var0, String var1, String var2, String var3) {
        ArrayList var4 = new ArrayList();

        for (MethodNode var6 : var0.methods) {
            if ((var6.name.equals(var1) || var6.name.equals(var2)) && var6.desc.equals(var3)) {
                var4.add(var6);
            }
        }

        if (var4.size() != 1) {
            throw new IllegalStateException("P11 exact ranged method " + var1 + " count=" + var4.size());
        } else {
            return (MethodNode)var4.get(0);
        }
    }

    private static MethodNode helper(ClassNode var0, String var1, String var2, boolean var3) {
        ArrayList var4 = new ArrayList();

        for (MethodNode var6 : var0.methods) {
            if (var6.name.equals(var1) && var6.desc.equals(var2)) {
                var4.add(var6);
            }
        }

        if (var4.size() != 1) {
            throw new IllegalStateException("P11 merged ranged helper " + var1 + " count=" + var4.size());
        } else {
            MethodNode var7 = (MethodNode)var4.get(0);
            if ((var7.access & 8) != 0 == var3 && (var7.access & 2) != 0) {
                return var7;
            } else {
                throw new IllegalStateException("P11 merged ranged helper access " + var1);
            }
        }
    }

    private static MethodInsnNode invocation(MethodNode var0, String var1, String var2, String var3) {
        ArrayList var4 = new ArrayList();

        for (AbstractInsnNode var6 : var0.instructions) {
            if (var6 instanceof MethodInsnNode) {
                MethodInsnNode var7 = (MethodInsnNode)var6;
                if (var7.owner.equals(var1) && var7.name.equals(var2) && var7.desc.equals(var3)) {
                    var4.add(var7);
                }
            }
        }

        if (var4.size() == 1 && ((MethodInsnNode)var4.get(0)).getOpcode() == 182 && !((MethodInsnNode)var4.get(0)).itf) {
            return (MethodInsnNode)var4.get(0);
        } else {
            throw new IllegalStateException("P11 exact SDK invocation " + var0.name + " " + var1 + "." + var2 + " count=" + var4.size());
        }
    }

    private static void replace(ClassNode var0, MethodNode var1, MethodInsnNode var2, MethodNode var3, boolean var4) {
        if (var4) {
            InsnList var5 = new InsnList();
            var5.add(new VarInsnNode(25, 0));
            var5.add(new InsnNode(91));
            var5.add(new InsnNode(87));
            var5.add(new VarInsnNode(25, 1));
            var5.add(new VarInsnNode(25, 2));
            var5.add(new VarInsnNode(25, 3));
            var5.add(new VarInsnNode(21, 4));
            var1.instructions.insertBefore(var2, var5);
        }

        var1.instructions.set(var2, new MethodInsnNode(var4 ? 183 : 184, var0.name, var3.name, var3.desc, false));
    }
}
