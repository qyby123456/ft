package net.farmingtales.ftboss.item;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class NativeRepairStackImage {
    private static final String COMPONENTS = "components";
    private static final String CUSTOM_DATA = "minecraft:custom_data";
    private static final String DAMAGE = "minecraft:damage";

    private NativeRepairStackImage() {
    }

    private static void requireNativeLayout(CompoundTag image) {
        Objects.requireNonNull(image, "image");
        if (image.contains("tag") || image.contains("Count") || image.contains("ForgeCaps")) {
            throw new IllegalArgumentException("Legacy repair item image requires an explicit migration policy");
        }
        if (image.contains(COMPONENTS) && !image.contains(COMPONENTS, 10)) {
            throw new IllegalArgumentException("Invalid native repair component image");
        }
    }

    public static ItemStack decode(HolderLookup.Provider registries, CompoundTag image) {
        requireNativeLayout(image);
        return NativeExpectedFieldObservation.decodeForRead(Objects.requireNonNull(registries, "registries"), image.copy());
    }

    public static CompoundTag data(CompoundTag image) {
        requireNativeLayout(image);
        return image.getCompound(COMPONENTS).getCompound(CUSTOM_DATA);
    }

    public static int damage(CompoundTag image) {
        requireNativeLayout(image);
        return image.getCompound(COMPONENTS).getInt(DAMAGE);
    }

    public static void putDamage(CompoundTag image, int damage) {
        requireNativeLayout(image);
        CompoundTag components = image.getCompound(COMPONENTS);
        components.putInt(DAMAGE, damage);
        image.put(COMPONENTS, components);
    }

    public static int count(CompoundTag image) {
        requireNativeLayout(image);
        return image.getInt("count");
    }

    public static void putCount(CompoundTag image, int count) {
        requireNativeLayout(image);
        image.putInt("count", count);
    }

    public static void normalize(HolderLookup.Provider registries, CompoundTag image) {
        ItemStack stack = decode(registries, image);
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Cannot normalize an empty or invalid native repair item image");
        }
        CompoundTag encoded = (CompoundTag)stack.saveOptional(registries);
        for (String key : List.of("id", "count", COMPONENTS)) {
            if (key.equals(COMPONENTS) && encoded.contains(key, 10) && image.contains(key, 10)) {
                CompoundTag existing = image.getCompound(key);
                CompoundTag normalized = encoded.getCompound(key);
                for (String component : Set.copyOf(existing.getAllKeys())) {
                    if (!normalized.contains(component)) {
                        existing.remove(component);
                    }
                }
                for (String component : normalized.getAllKeys()) {
                    if (!normalized.get(component).equals(existing.get(component))) {
                        existing.put(component, normalized.get(component).copy());
                    }
                }
            } else if (encoded.contains(key)) {
                if (!encoded.get(key).equals(image.get(key))) {
                    image.put(key, encoded.get(key).copy());
                }
            } else {
                image.remove(key);
            }
        }
    }
}
