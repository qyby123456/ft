package net.farmingtales.ftboss.item;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class NativeLifecycleItemData {
    public static final String ROOT = "FTBossWeaponLifecycle";

    private NativeLifecycleItemData() {
    }

    public static boolean nativeDamageable(ItemStack stack) {
        return stack != null
            && !stack.isEmpty()
            && stack.getCount() == 1
            && !stack.has(DataComponents.UNBREAKABLE)
            && stack.getItem().getMaxDamage(stack) > 0
            && stack.isDamageableItem()
            && stack.getMaxDamage() > 0;
    }

    public static boolean eligible(ItemStack stack) {
        if (!nativeDamageable(stack)) {
            return false;
        }
        CompoundTag lifecycle = FoodWeaponData.read(stack, ROOT);
        CompoundTag wear = lifecycle == null ? null : lifecycle.getCompound("NativeWear");
        return wear != null
            && wear.getBoolean("Managed")
            && wear.getInt("Version") == 1
            && wear.hasUUID("Identity")
            && FoodWeaponEvolution.isEvolutionCandidate(stack);
    }

    public static boolean durabilityDepleted(ItemStack stack) {
        if (!eligible(stack)) {
            return false;
        }
        CompoundTag lifecycle = FoodWeaponData.read(stack, ROOT);
        return lifecycle != null && lifecycle.getBoolean("Broken") || stack.getDamageValue() >= stack.getMaxDamage();
    }

    public static void commit(ItemStack stack, String key, CompoundTag before, CompoundTag after) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(key, "key");
        CompoundTag original = before == null ? new CompoundTag() : before.copy();
        CompoundTag changed = Objects.requireNonNull(after, "after").copy();
        NativeExpectedFieldObservation.nativeComponentCommit(stack, key, original, changed, current -> merge(current, original, changed));
    }

    public static void updateWearMask(ItemStack stack, UUID identity, int mask) {
        Objects.requireNonNull(identity, "identity");
        FoodWeaponData.edit(stack, ROOT, lifecycle -> {
            CompoundTag wear = lifecycle.getCompound("NativeWear");
            if (!wear.getBoolean("Managed") || wear.getInt("Version") != 1 || !wear.hasUUID("Identity")
                || !identity.equals(wear.getUUID("Identity"))) {
                throw new IllegalStateException("Native wear identity changed before mask commit");
            }
            wear.putInt("Mask", mask);
        });
    }

    private static void merge(CompoundTag current, CompoundTag before, CompoundTag after) {
        var keys = new LinkedHashSet<String>(java.util.List.of(NativeExpectedFieldObservation.nativeComponentMergeKeys391(before)));
        keys.addAll(java.util.List.of(NativeExpectedFieldObservation.nativeComponentMergeKeys391(after)));
        for (String key : keys) {
            Tag original = before.get(key);
            Tag changed = after.get(key);
            if (Objects.equals(original, changed)) {
                continue;
            }
            if (changed == null) {
                NativeExpectedFieldObservation.nativeComponentMergeWrite(current, key, null, () -> current.remove(key));
            } else if ((original == null || original instanceof CompoundTag) && changed instanceof CompoundTag changedCompound
                && current.get(key) instanceof CompoundTag currentCompound) {
                merge(currentCompound, original instanceof CompoundTag originalCompound ? originalCompound : new CompoundTag(), changedCompound);
            } else {
                NativeExpectedFieldObservation.nativeComponentMergeWrite(current, key, changed, () -> current.put(key, changed.copy()));
            }
        }
    }
}
