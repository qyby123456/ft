package net.farmingtales.ftboss.item;

import java.util.Objects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

final class NativeLegacyEvolutionItemData {
    private NativeLegacyEvolutionItemData() {
    }

    static boolean hasPositiveMainHandDamage(ItemStack stack) {
        boolean[] found = new boolean[]{false};
        stack.forEachModifier(net.minecraft.world.entity.EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) && modifier.amount() > 0.0) found[0] = true;
        });
        return found[0];
    }

    static double defaultMainHandAmount(net.minecraft.world.item.component.ItemAttributeModifiers modifiers, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
        return modifiers.modifiers().stream()
            .filter(entry -> entry.attribute().equals(attribute) && entry.slot().test(net.minecraft.world.entity.EquipmentSlot.MAINHAND))
            .mapToDouble(entry -> entry.modifier().amount()).sum();
    }

    static CompoundTag root(ItemStack stack) {
        CustomData data = Objects.requireNonNull(stack).get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    static CompoundTag element(ItemStack stack, String key) {
        CompoundTag root = root(stack);
        return root != null && root.contains(key, 10) ? root.getCompound(key) : null;
    }

    static CompoundTag writable(ItemStack stack, String key) {
        CompoundTag element = element(stack, key);
        if (element == null) {
            element = new CompoundTag();
            write(stack, key, element);
        }
        return element;
    }

    static void write(ItemStack stack, String key, CompoundTag element) {
        Objects.requireNonNull(stack);
        Objects.requireNonNull(key);
        Objects.requireNonNull(element);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> root.put(key, element.copy()));
    }
}
