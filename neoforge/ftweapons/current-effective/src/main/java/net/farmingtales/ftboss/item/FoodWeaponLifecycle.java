package net.farmingtales.ftboss.item;

import java.util.UUID;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class FoodWeaponLifecycle {
    public static final String ROOT = "FTBossWeaponLifecycle";
    private static final ThreadLocal<ItemStack> INTERNAL_WRITE = new ThreadLocal<>();

    private FoodWeaponLifecycle() {
    }

    public static int wearStep(ItemStack var0) {
        return FoodWeaponLifecycleRules.wearStep(var0.getDamageValue(), var0.getMaxDamage());
    }

    public static String repairProfile(ItemStack var0) {
        CompoundTag var1 = FoodWeaponData.read(var0, "FTBossWeaponLifecycle");
        String var2 = var1 == null ? "" : var1.getString("RepairProfile");
        return FoodWeaponEvolution.Profile.byId(var2) == null ? NativeFoodWeaponEvolutionRead.repairProfileId(var0) : var2;
    }

    private static boolean loseSocket(ItemStack var0) {
        CompoundTag var1 = FoodWeaponData.read(var0, "FTBossFoodEvolution");
        if (var1 == null) {
            return false;
        } else {
            CompoundTag nativeBefore = var1.copy();
            ListTag var2 = var1.getList("Sockets", 10);
            if (var2.isEmpty()) {
                return false;
            } else {
                var2.remove(var2.size() - 1);
                NativeLifecycleItemData.commit(var0, "FTBossFoodEvolution", nativeBefore, var1);
                return true;
            }
        }
    }

    private static void setInternalDamage(ItemStack var0, int var1) {
        ItemStack var2 = INTERNAL_WRITE.get();
        INTERNAL_WRITE.set(var0);

        try {
            var0.setDamageValue(var1);
        } finally {
            if (var2 == null) {
                INTERNAL_WRITE.remove();
            } else {
                INTERNAL_WRITE.set(var2);
            }
        }
    }

    private static boolean loseMaterial(ItemStack var0) {
        if (SwordCavityBuild.active(var0)) {
            return SwordCavityBuild.losePart(var0);
        } else {
            CompoundTag var1 = FoodWeaponData.read(var0, "FTBossWeaponMaterials");
            if (var1 == null) {
                return false;
            } else {
                CompoundTag nativeBefore = var1.copy();
                Part[] var2 = Part.values();

                for (int var3 = var2.length - 1; var3 >= 0; var3--) {
                    String var4 = var2[var3].name();
                    if (Material.byId(var1.getString(var4)) != Material.ORIGINAL) {
                        var1.putString(var4, Material.ORIGINAL.id());
                        NativeLifecycleItemData.commit(var0, "FTBossWeaponMaterials", nativeBefore, var1);
                        return true;
                    }
                }

                return false;
            }
        }
    }

    public static boolean isBroken(ItemStack var0) {
        return !managed(var0) ? false : durabilityDepleted(var0)
            || (FourWeaponFoundationAdapter.applies(var0) ? FourWeaponFoundationAdapter.blocksUse(var0) : SwordCavityBuild.missingRequired(var0));
    }

    public static boolean durabilityDepleted(ItemStack var0) {
        if (!managed(var0)) {
            return false;
        } else {
            CompoundTag var1 = FoodWeaponData.read(var0, "FTBossWeaponLifecycle");
            return var1 != null && var1.getBoolean("Broken") || var0.getDamageValue() >= var0.getMaxDamage();
        }
    }

    public static boolean managed(ItemStack var0) {
        return NativeFoodWeaponWear.eligible(var0);
    }

    public static boolean requiresStatueRepair(ItemStack var0) {
        return NativeFoodWeaponWear.eligible(var0);
    }

    public static UUID weaponId(ItemStack var0) {
        CompoundTag var1 = FoodWeaponData.read(var0, "FTBossWeaponLifecycle");
        if (NativeFoodWeaponWear.eligible(var0)) {
            return var1 != null && var1.hasUUID("Weapon") ? var1.getUUID("Weapon") : null;
        } else {
            return var1 != null && OwnerStatueTaskWrites.hasWeaponIdentity(var0, var1) ? OwnerStatueTaskWrites.weaponIdentity(var0, var1) : null;
        }
    }

    public static void ensureTracked(ItemStack var0) {
        if (NativeFoodWeaponWear.nativeDamageable(var0) && FoodWeaponEvolution.isEvolutionCandidate(var0)) {
            MinecraftServer var1 = ServerLifecycleHooks.getCurrentServer();
            if (var1 != null && var1.isSameThread()) {
                CompoundTag var2 = FoodWeaponData.read(var0, "FTBossWeaponLifecycle");
                CompoundTag nativeBefore = var2 == null ? null : var2.copy();
                if (var2 == null) {
                    var2 = new CompoundTag();
                }
                if (!OwnerStatueTaskWrites.hasWeaponIdentity(var0, var2)) {
                    var2.putUUID("Weapon", OwnerNativeSemanticAuthority.nativeLifecycleIdentity(var0));
                }

                if (!var2.contains("WearStep", 3)) {
                    var2.putInt("WearStep", wearStep(var0));
                }

                if (FoodWeaponEvolution.evolutionLevel(var0) > 0) {
                    OwnerStatueWearWrites.putBoolean(var0, 2, var2, "WasEvolved", true);
                }

                if (var2.getString("RepairProfile").isEmpty()) {
                    String var3 = NativeFoodWeaponEvolutionRead.repairProfileId(var0);
                    if (!var3.isEmpty()) {
                        var2.putString("RepairProfile", var3);
                    }
                }

                if (managed(var0) && var0.getDamageValue() >= var0.getMaxDamage()) {
                    OwnerStatueWearWrites.putBoolean(var0, 2, var2, "Broken", true);
                }
                if (!java.util.Objects.equals(nativeBefore, var2)) {
                    NativeLifecycleItemData.commit(var0, "FTBossWeaponLifecycle", nativeBefore, var2);
                }
            }
        }
    }

    public static void markEvolved(ItemStack var0) {
        if (NativeFoodWeaponWear.nativeDamageable(var0)) {
            NativeFoodWeaponWear.manageBound(var0);
            ensureTracked(var0);
            CompoundTag var1 = FoodWeaponData.read(var0, "FTBossWeaponLifecycle");
            if (var1 != null) {
                FoodWeaponData.edit(var0, "FTBossWeaponLifecycle", current -> current.putBoolean("WasEvolved", true));
            }
        }
    }

    public static int beforeDamageChange(ItemStack var0, int var1) {
        if (INTERNAL_WRITE.get() != var0) {
            if (var0 != null && var1 >= 0 && var0.getDamageValue() == var1 && !NativeFoodWeaponWear.statueRepairWrite(var0)) {
                return var1;
            }

            if (managed(var0)) {
                if (NativeFoodWeaponWear.statueRepairWrite(var0)) {
                    return Math.max(0, Math.min(var0.getMaxDamage(), var1));
                }

                if (var1 < var0.getDamageValue()) {
                    return var0.getDamageValue();
                }

                return Math.max(var0.getDamageValue(), Math.max(0, Math.min(var0.getMaxDamage(), var1)));
            }
        }

        return var1;
    }

    public static void afterDamageChange(ItemStack var0) {
        if (INTERNAL_WRITE.get() != var0) {
            NativeFoodWeaponWear.damageChanged(var0);
        }
    }

    public static boolean finishHurt(ItemStack var0, boolean var1, ServerPlayer var2) {
        if (!managed(var0)) {
            return var1;
        } else {
            NativeFoodWeaponWear.damageChanged(var0);
            if (var2 != null && !var2.level().isClientSide && var2.getServer() != null && var2.getServer().isSameThread() && isBroken(var0)) {
                var2.stopUsingItem();
                var2.displayClientMessage(Component.translatable("message.ftboss.weapon.broken"), true);
            }

            return false;
        }
    }

    static void restoreAfterQuest(ItemStack var0) {
        if (NativeFoodWeaponWear.statueRepairWrite(var0)) {
            NativeFoodWeaponWear.beforeNativeRepairWrite(var0, var0, "Damage", net.minecraft.nbt.IntTag.valueOf(0));
            Throwable failure = null;
            try {
                var0.setDamageValue(0);
            } catch (RuntimeException | Error original) {
                failure = original;
                throw original;
            } finally {
                NativeFoodWeaponWear.afterNativeRepairWrite(var0, failure);
            }
            net.minecraft.world.item.component.CustomData data = var0.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            CompoundTag root = data == null ? null : data.getUnsafe();
            if (root != null && root.contains("FTBossWeaponLifecycle", 10)) {
                OwnerStatueTaskWrites.remove(var0, root.getCompound("FTBossWeaponLifecycle"), "Broken");
            }
        }
    }

    static void materialDamage(ItemStack var0, int var1) {
        if (NativeFoodWeaponWear.eligible(var0)) {
            NativeFoodWeaponWear.componentsChanged(var0);
        }
    }
}
