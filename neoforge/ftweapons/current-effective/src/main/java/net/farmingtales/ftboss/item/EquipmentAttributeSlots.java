package net.farmingtales.ftboss.item;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

public final class EquipmentAttributeSlots {
    private EquipmentAttributeSlots() {
    }

    public static boolean heldItem(ItemStack var0) {
        return var0 != null && !var0.isEmpty() && !(var0.getItem() instanceof ArmorItem);
    }

    public static boolean growthSlot(EquipmentSlot var0, ItemStack var1) {
        if (var1 != null && !var1.isEmpty()) {
            return !(var1.getItem() instanceof ArmorItem var2)
                ? var0 == EquipmentSlot.MAINHAND
                : EquipmentWeaponBuild.applies(var1) && var0 == var2.getEquipmentSlot();
        } else {
            return false;
        }
    }

    public static boolean blessingSlot(EquipmentSlot var0, ItemStack var1) {
        return var0 != null && var1 != null && !var1.isEmpty()
            ? growthSlot(var0, var1) || heldItem(var1) && var0 == EquipmentSlot.OFFHAND
            : false;
    }

    public static UUID modifierId(UUID var0, ItemStack var1) {
        return var1.getItem() instanceof ArmorItem var2
            ? UUID.nameUUIDFromBytes(("ftboss.armor_growth." + var2.getEquipmentSlot().getName() + "." + var0).getBytes(StandardCharsets.UTF_8))
            : var0;
    }

    public static void attributes(ItemAttributeModifierEvent var0) {
        ItemStack var1 = var0.getItemStack();
        if (var1.getItem() instanceof ArmorItem) {
            if (growthSlot(NativeEquipmentAttributes.primarySlot(var1), var1)) {
                FoodWeaponEvolution.growthAttributes(var0);
            }
        } else {
            BowGrowthCombat.attributes(var0);
            FoodBlessingCombat.blessingAttributes(var0, var1, EquipmentSlot.OFFHAND);
        }
    }
}
