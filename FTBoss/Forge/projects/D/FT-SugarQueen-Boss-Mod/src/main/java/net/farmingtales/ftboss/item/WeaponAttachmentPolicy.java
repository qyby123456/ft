package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class WeaponAttachmentPolicy {
    private WeaponAttachmentPolicy() {}

    public static boolean excluded(ItemStack weapon) {
        if (weapon.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(weapon.getItem());
        if (weapon.getItem() instanceof ArmorItem) return id != null && id.getNamespace().equals("create");
        return id != null && SwordCompatibilityRules.attachmentsExcluded(id.toString());
    }
}
