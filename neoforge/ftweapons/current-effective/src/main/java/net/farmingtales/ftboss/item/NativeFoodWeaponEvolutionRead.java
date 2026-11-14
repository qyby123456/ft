package net.farmingtales.ftboss.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class NativeFoodWeaponEvolutionRead {
    private NativeFoodWeaponEvolutionRead() {
    }

    public static String repairProfileId(ItemStack stack) {
        ResourceLocation identifier = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!stack.isEmpty() && identifier != null && "create".equals(identifier.getNamespace())) {
            return "";
        }
        String nativeProfile = nativeProfile(identifier);
        if (!nativeProfile.isEmpty()) {
            return nativeProfile;
        }
        CompoundTag data = FoodWeaponData.read(stack, "FTBossFoodEvolution");
        if (data == null) {
            return "";
        }
        String storedProfile = data.getString("Profile");
        return FoodWeaponEvolution.Profile.byId(storedProfile) == null ? "" : storedProfile;
    }

    static String nativeProfile(ResourceLocation identifier) {
        if (identifier == null) {
            return "";
        }
        return switch (identifier.toString()) {
            case "ftboss:candy_cane" -> "sugar";
            case "ftboss:turkey_legs" -> "butcher";
            case "ftboss:blooming_thorns" -> "saya";
            case "ftboss:wine_bottle_mace", "ftboss:barrel_crossbow" -> "wine";
            case "ftboss:cheese_wheel_mace" -> "rat";
            case "ftboss:defect_claw" -> "defect";
            case "ftboss:compost_crown" -> "compost";
            case "ftboss:apple_stem" -> "apple";
            case "ftboss:tide_trident" -> "tide";
            case "ftboss:dog_beating_stick" -> "benger";
            default -> "";
        };
    }
}
