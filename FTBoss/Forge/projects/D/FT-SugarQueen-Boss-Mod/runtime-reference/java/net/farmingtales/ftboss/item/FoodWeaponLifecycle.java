/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.server.ServerLifecycleHooks
 */
package net.farmingtales.ftboss.item;

import java.util.UUID;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.farmingtales.ftboss.item.OwnerStatueTaskWrites;
import net.farmingtales.ftboss.item.OwnerStatueWearWrites;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class FoodWeaponLifecycle {
    public static final String ROOT = "FTBossWeaponLifecycle";
    private static final ThreadLocal<ItemStack> INTERNAL_WRITE = new ThreadLocal();

    private FoodWeaponLifecycle() {
    }

    public static int wearStep(ItemStack itemStack) {
        return FoodWeaponLifecycleRules.wearStep(itemStack.m_41773_(), itemStack.m_41776_());
    }

    public static String repairProfile(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        String string = compoundTag == null ? "" : compoundTag.m_128461_("RepairProfile");
        WeaponEvolutionSkillPool.Aspect aspect = WeaponEvolutionSkillPool.Aspect.fromId(string);
        return aspect == null ? FoodWeaponEvolution.repairProfileId(itemStack) : aspect.id;
    }

    private static boolean loseSocket(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41737_("FTBossFoodEvolution");
        if (compoundTag == null) {
            return false;
        }
        ListTag listTag = compoundTag.m_128437_("Sockets", 10);
        if (listTag.isEmpty()) {
            return false;
        }
        listTag.remove(listTag.size() - 1);
        return true;
    }

    private static void setInternalDamage(ItemStack itemStack, int n) {
        ItemStack itemStack2 = INTERNAL_WRITE.get();
        INTERNAL_WRITE.set(itemStack);
        try {
            itemStack.m_41721_(n);
        }
        finally {
            if (itemStack2 == null) {
                INTERNAL_WRITE.remove();
            } else {
                INTERNAL_WRITE.set(itemStack2);
            }
        }
    }

    private static boolean loseMaterial(ItemStack itemStack) {
        if (SwordCavityBuild.active(itemStack)) {
            return SwordCavityBuild.losePart(itemStack);
        }
        CompoundTag compoundTag = itemStack.m_41737_("FTBossWeaponMaterials");
        if (compoundTag == null) {
            return false;
        }
        FoodWeaponBuildRules.Part[] partArray = FoodWeaponBuildRules.Part.values();
        for (int i = partArray.length - 1; i >= 0; --i) {
            String string = partArray[i].name();
            if (FoodWeaponBuildRules.Material.byId(compoundTag.m_128461_(string)) == FoodWeaponBuildRules.Material.ORIGINAL) continue;
            compoundTag.m_128359_(string, FoodWeaponBuildRules.Material.ORIGINAL.id());
            return true;
        }
        return false;
    }

    public static boolean isBroken(ItemStack itemStack) {
        if (!FoodWeaponLifecycle.managed(itemStack)) {
            return false;
        }
        return FoodWeaponLifecycle.durabilityDepleted(itemStack) || SwordCavityBuild.missingRequired(itemStack);
    }

    public static boolean durabilityDepleted(ItemStack itemStack) {
        if (!FoodWeaponLifecycle.managed(itemStack)) {
            return false;
        }
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        return compoundTag != null && compoundTag.m_128471_("Broken") || itemStack.m_41773_() >= itemStack.m_41776_();
    }

    public static boolean managed(ItemStack itemStack) {
        return NativeFoodWeaponWear.eligible(itemStack);
    }

    public static boolean requiresStatueRepair(ItemStack itemStack) {
        return NativeFoodWeaponWear.eligible(itemStack);
    }

    public static UUID weaponId(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        if (NativeFoodWeaponWear.eligible(itemStack)) {
            return compoundTag != null && compoundTag.m_128403_("Weapon") ? compoundTag.m_128342_("Weapon") : null;
        }
        return compoundTag != null && OwnerStatueTaskWrites.hasWeaponIdentity(itemStack, compoundTag) ? OwnerStatueTaskWrites.weaponIdentity(itemStack, compoundTag) : null;
    }

    public static void ensureTracked(ItemStack itemStack) {
        String string;
        if (!NativeFoodWeaponWear.nativeDamageable(itemStack) || !FoodWeaponEvolution.isEvolutionCandidate(itemStack)) {
            return;
        }
        MinecraftServer minecraftServer = ServerLifecycleHooks.getCurrentServer();
        if (minecraftServer == null || !minecraftServer.m_18695_()) {
            return;
        }
        CompoundTag compoundTag = itemStack.m_41698_(ROOT);
        if (!OwnerStatueTaskWrites.hasWeaponIdentity(itemStack, compoundTag)) {
            compoundTag.m_128362_("Weapon", UUID.randomUUID());
        }
        if (!compoundTag.m_128425_("WearStep", 3)) {
            compoundTag.m_128405_("WearStep", FoodWeaponLifecycle.wearStep(itemStack));
        }
        if (FoodWeaponEvolution.evolutionLevel(itemStack) > 0) {
            OwnerStatueWearWrites.putBoolean(itemStack, 2, compoundTag, "WasEvolved", true);
        }
        if (compoundTag.m_128461_("RepairProfile").isEmpty() && !(string = FoodWeaponEvolution.repairProfileId(itemStack)).isEmpty()) {
            compoundTag.m_128359_("RepairProfile", string);
        }
        if (FoodWeaponLifecycle.managed(itemStack) && itemStack.m_41773_() >= itemStack.m_41776_()) {
            OwnerStatueWearWrites.putBoolean(itemStack, 2, compoundTag, "Broken", true);
        }
    }

    public static void markEvolved(ItemStack itemStack) {
        if (!NativeFoodWeaponWear.nativeDamageable(itemStack)) {
            return;
        }
        NativeFoodWeaponWear.manageBound(itemStack);
        FoodWeaponLifecycle.ensureTracked(itemStack);
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        if (compoundTag != null) {
            compoundTag.m_128379_("WasEvolved", true);
        }
    }

    public static int beforeDamageChange(ItemStack itemStack, int n) {
        block7: {
            block6: {
                if (INTERNAL_WRITE.get() == itemStack) break block6;
                if (itemStack != null && n >= 0 && itemStack.m_41773_() == n && !NativeFoodWeaponWear.statueRepairWrite(itemStack)) {
                    return n;
                }
                if (FoodWeaponLifecycle.managed(itemStack)) break block7;
            }
            return n;
        }
        if (NativeFoodWeaponWear.statueRepairWrite(itemStack)) {
            return Math.max(0, Math.min(itemStack.m_41776_(), n));
        }
        if (n < itemStack.m_41773_()) {
            return itemStack.m_41773_();
        }
        return Math.max(itemStack.m_41773_(), Math.max(0, Math.min(itemStack.m_41776_(), n)));
    }

    public static void afterDamageChange(ItemStack itemStack) {
        if (INTERNAL_WRITE.get() != itemStack) {
            NativeFoodWeaponWear.damageChanged(itemStack);
        }
    }

    public static boolean finishHurt(ItemStack itemStack, boolean bl, ServerPlayer serverPlayer) {
        if (!FoodWeaponLifecycle.managed(itemStack)) {
            return bl;
        }
        NativeFoodWeaponWear.damageChanged(itemStack);
        if (serverPlayer != null && !serverPlayer.m_9236_().f_46443_ && serverPlayer.m_20194_() != null && serverPlayer.m_20194_().m_18695_() && FoodWeaponLifecycle.isBroken(itemStack)) {
            serverPlayer.m_5810_();
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.weapon.broken"), true);
        }
        return false;
    }

    static void restoreAfterQuest(ItemStack itemStack) {
        if (!NativeFoodWeaponWear.statueRepairWrite(itemStack)) {
            return;
        }
        itemStack.m_41721_(0);
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        if (compoundTag != null) {
            compoundTag.m_128473_("Broken");
        }
    }

    static void materialDamage(ItemStack itemStack, int n) {
        if (!NativeFoodWeaponWear.eligible(itemStack)) {
            return;
        }
        NativeFoodWeaponWear.componentsChanged(itemStack);
    }
}

