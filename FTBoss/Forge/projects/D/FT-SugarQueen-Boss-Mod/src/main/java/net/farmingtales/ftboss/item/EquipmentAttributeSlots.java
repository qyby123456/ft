package net.farmingtales.ftboss.item;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;

public final class EquipmentAttributeSlots {
    private EquipmentAttributeSlots() {}

    public static boolean heldItem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && !(stack.getItem() instanceof ArmorItem);
    }

    public static boolean growthSlot(EquipmentSlot slot, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() instanceof ArmorItem armor) {
            return EquipmentWeaponBuild.applies(stack) && slot == armor.getEquipmentSlot();
        }
        return slot == EquipmentSlot.MAINHAND;
    }

    public static boolean blessingSlot(ItemAttributeModifierEvent event, ItemStack stack) {
        if (event == null || stack == null || stack.isEmpty()) return false;
        return growthSlot(event.getSlotType(), stack)
                || heldItem(stack) && event.getSlotType() == EquipmentSlot.OFFHAND;
    }

    public static UUID modifierId(UUID original, ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem armor)) return original;
        return UUID.nameUUIDFromBytes(("ftboss.armor_growth." + armor.getEquipmentSlot().getName() + "." + original)
                .getBytes(StandardCharsets.UTF_8));
    }

    public static void attributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof ArmorItem) {
            if (growthSlot(event.getSlotType(), stack)) FoodWeaponEvolution.growthAttributes(event);
        } else if (event.getSlotType() == EquipmentSlot.MAINHAND) {
            BowGrowthCombat.attributes(event);
        } else if (event.getSlotType() == EquipmentSlot.OFFHAND) {
            FoodBlessingCombat.blessingAttributes(event, stack);
        }
    }
}
