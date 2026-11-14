package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class WeaponAttachmentPolicy {
    private WeaponAttachmentPolicy() {}

    public static boolean excluded(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!WeaponSystemScope410.supports(stack)) return true;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && SwordCompatibilityRules.attachmentsExcluded(id.toString());
    }
}
