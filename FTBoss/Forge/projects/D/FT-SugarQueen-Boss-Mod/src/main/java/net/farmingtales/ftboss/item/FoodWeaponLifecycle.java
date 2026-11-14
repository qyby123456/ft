package net.farmingtales.ftboss.item;

import java.util.UUID;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** 食武耐久生命周期；2026-10-13 保留耗尽、脱落和曾进化修复保护，功能不足仅禁用而不误判需要耐久修复。 */
public final class FoodWeaponLifecycle {
    public static final String ROOT = "FTBossWeaponLifecycle";
    private static final ThreadLocal<ItemStack> INTERNAL_WRITE = new ThreadLocal<>();

    private FoodWeaponLifecycle() {}

    public static boolean managed(ItemStack stack) {
        return !stack.isEmpty() && FoodWeaponEvolution.isEvolutionCandidate(stack) && stack.isDamageableItem() && stack.getMaxDamage() > 0;
    }

    public static boolean isBroken(ItemStack stack) {
        boolean equipment = EquipmentWeaponBuild.applies(stack);
        if (!managed(stack) && !equipment) return false;
        Boolean cached = equipment ? WeaponTooltipReadScope.cachedValue(stack, "ftboss.armor.broken") : null;
        if (cached != null) return cached;
        CompoundTag state = stack.getTagElement(ROOT);
        boolean broken = state != null && state.getBoolean("Broken") || durabilityDepleted(stack) || SwordCavityBuild.missingRequired(stack);
        return equipment ? WeaponTooltipReadScope.rememberValue(stack, "ftboss.armor.broken", broken) : broken;
    }

    public static boolean durabilityDepleted(ItemStack stack) {
        if (!managed(stack)) return false;
        CompoundTag state = stack.getTagElement(ROOT);
        return state != null && state.getBoolean("Broken") || stack.getDamageValue() >= stack.getMaxDamage();
    }

    public static boolean requiresStatueRepair(ItemStack stack) {
        if (stack.isEmpty() || !FoodWeaponEvolution.isEvolutionCandidate(stack)) return false;
        CompoundTag state = stack.getTagElement(ROOT);
        return state != null && state.getBoolean("WasEvolved") || FoodWeaponEvolution.evolutionLevel(stack) > 0 || durabilityDepleted(stack);
    }

    public static int wearStep(ItemStack stack) {
        return FoodWeaponLifecycleRules.wearStep(stack.getDamageValue(), stack.getMaxDamage());
    }

    public static UUID weaponId(ItemStack stack) {
        CompoundTag state = stack.getTagElement(ROOT);
        return state != null && state.hasUUID("Weapon") ? state.getUUID("Weapon") : null;
    }

    public static String repairProfile(ItemStack stack) {
        CompoundTag state = stack.getTagElement(ROOT);
        String stored = state == null ? "" : state.getString("RepairProfile");
        var aspect = net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Aspect.fromId(stored);
        return aspect == null ? FoodWeaponEvolution.repairProfileId(stack) : aspect.id;
    }

    public static void ensureTracked(ItemStack stack) {
        if (ManorWeaponCavity.applies(stack)) GourmetBladeCavity.ensureIdentity(stack);
        if (GourmetBladeCavity.identityOnly(stack)) {
            GourmetBladeCavity.ensureIdentity(stack);
            return;
        }
        if (stack.isEmpty() || !FoodWeaponEvolution.isEvolutionCandidate(stack)) return;
        CompoundTag state = stack.getOrCreateTagElement(ROOT);
        if (!state.hasUUID("Weapon")) state.putUUID("Weapon", UUID.randomUUID());
        if (!state.contains("WearStep", Tag.TAG_INT)) state.putInt("WearStep", wearStep(stack));
        if (FoodWeaponEvolution.evolutionLevel(stack) > 0) state.putBoolean("WasEvolved", true);
        if (state.getString("RepairProfile").isEmpty()) {
            String profile = FoodWeaponEvolution.repairProfileId(stack);
            if (!profile.isEmpty()) state.putString("RepairProfile", profile);
        }
        if (managed(stack) && stack.getDamageValue() >= stack.getMaxDamage()) state.putBoolean("Broken", true);
    }

    public static void markEvolved(ItemStack stack) {
        ensureTracked(stack);
        stack.getOrCreateTagElement(ROOT).putBoolean("WasEvolved", true);
    }

    public static int beforeDamageChange(ItemStack stack, int requested) {
        if (INTERNAL_WRITE.get() == stack || !managed(stack)) return requested;
        int current = stack.getDamageValue();
        if (requested == current) return requested;
        if (requested < current && requiresStatueRepair(stack)) return current;
        ensureTracked(stack);
        int value = Math.max(0, Math.min(stack.getMaxDamage(), requested));
        if (value < current) stack.getOrCreateTagElement(ROOT).putInt("WearStep",
                FoodWeaponLifecycleRules.wearStep(value, stack.getMaxDamage()));
        return value;
    }

    public static void afterDamageChange(ItemStack stack) {
        if (INTERNAL_WRITE.get() == stack || !managed(stack)) return;
        CompoundTag state = stack.getTagElement(ROOT);
        if (state == null) return;
        int damage = stack.getDamageValue();
        int maximum = stack.getMaxDamage();
        int losses = FoodWeaponLifecycleRules.pendingLosses(state.getInt("WearStep"), damage, maximum);
        if (losses > 0) {
            state.putInt("WearStep", wearStep(stack));
            int materials = 0;
            int sockets = 0;
            for (int loss = 0; loss < losses; loss++) {
                if (loseMaterial(stack)) materials++;
                if (loseSocket(stack)) sockets++;
            }
            int newMaximum = stack.getMaxDamage();
            setInternalDamage(stack, FoodWeaponLifecycleRules.preserveWear(damage, maximum, newMaximum));
            state.putInt("PendingMaterials", state.getInt("PendingMaterials") + materials);
            state.putInt("PendingSockets", state.getInt("PendingSockets") + sockets);
        }
        if (stack.getDamageValue() >= stack.getMaxDamage()) state.putBoolean("Broken", true);
    }

    private static boolean loseMaterial(ItemStack stack) {
        if (SwordCavityBuild.active(stack)) return SwordCavityBuild.losePart(stack);
        CompoundTag materials = stack.getTagElement(WeaponMaterialBuild.ROOT);
        if (materials == null) return false;
        Part[] parts = Part.values();
        for (int index = parts.length - 1; index >= 0; index--) {
            String key = parts[index].name();
            if (Material.byId(materials.getString(key)) != Material.ORIGINAL) {
                materials.putString(key, Material.ORIGINAL.id());
                return true;
            }
        }
        return false;
    }

    private static boolean loseSocket(ItemStack stack) {
        CompoundTag evolution = stack.getTagElement(FoodWeaponProgression.ROOT);
        if (evolution == null) return false;
        ListTag sockets = evolution.getList("Sockets", Tag.TAG_COMPOUND);
        if (sockets.isEmpty()) return false;
        sockets.remove(sockets.size() - 1);
        return true;
    }

    public static boolean finishHurt(ItemStack stack, boolean original, ServerPlayer player) {
        if (!managed(stack)) return original;
        ensureTracked(stack);
        CompoundTag state = stack.getOrCreateTagElement(ROOT);
        if (player != null && (state.getInt("PendingMaterials") > 0 || state.getInt("PendingSockets") > 0)) {
            player.displayClientMessage(Component.translatable("message.ftboss.weapon.wear_loss",
                    state.getInt("PendingMaterials"), state.getInt("PendingSockets")), true);
            state.remove("PendingMaterials");
            state.remove("PendingSockets");
        }
        if (player != null && isBroken(stack)) {
            player.stopUsingItem();
            player.displayClientMessage(Component.translatable("message.ftboss.weapon.broken"), true);
        }
        return false;
    }

    private static void setInternalDamage(ItemStack stack, int value) {
        ItemStack previous = INTERNAL_WRITE.get();
        INTERNAL_WRITE.set(stack);
        try { stack.setDamageValue(value); }
        finally {
            if (previous == null) INTERNAL_WRITE.remove();
            else INTERNAL_WRITE.set(previous);
        }
    }

    static void restoreAfterQuest(ItemStack stack) {
        ensureTracked(stack);
        setInternalDamage(stack, 0);
        CompoundTag state = stack.getOrCreateTagElement(ROOT);
        state.remove("Broken");
        state.remove("PendingMaterials");
        state.remove("PendingSockets");
        state.putInt("WearStep", 0);
        CompoundTag materials = stack.getTagElement(WeaponMaterialBuild.ROOT);
        if (materials != null) materials.putDouble("WearCarry", 0);
    }

    static void materialDamage(ItemStack stack, int value) {
        setInternalDamage(stack, value);
        CompoundTag state = stack.getOrCreateTagElement(ROOT);
        state.putInt("WearStep", Math.max(state.getInt("WearStep"), wearStep(stack)));
    }
}
