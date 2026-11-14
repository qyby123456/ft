package net.farmingtales.ftboss.item;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem;
import net.farmingtales.ftboss.util.MaterialCrossbowRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

public final class NativeEquipmentAttributes {
    private static final Set<ResourceLocation> TRIDENT_DAMAGE_IDS = Set.of(
        ResourceLocation.fromNamespaceAndPath("ftboss", "material/894c7a17-bf58-492f-ae96-d21abeb6a811"),
        ResourceLocation.fromNamespaceAndPath("ftboss", "food_evolution_damage"),
        ResourceLocation.fromNamespaceAndPath("ftboss", "material_rarity/" + UUID.nameUUIDFromBytes(
            "ftboss.material_rarity.mainhand.attack_damage".getBytes(StandardCharsets.UTF_8))),
        ResourceLocation.fromNamespaceAndPath("ftboss", "food_blessing/" + UUID.nameUUIDFromBytes(
            "ftboss.food_blessing.cleaver_might".getBytes(StandardCharsets.UTF_8)))
    );

    private NativeEquipmentAttributes() {
    }

    public static EquipmentSlot primarySlot(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem armor ? armor.getEquipmentSlot() : EquipmentSlot.MAINHAND;
    }

    public static double sum(ItemAttributeModifierEvent event, Holder<Attribute> attribute, EquipmentSlot slot) {
        double amount = event.getModifiers().stream()
            .filter(entry -> entry.attribute().equals(attribute) && entry.slot().test(slot))
            .mapToDouble(entry -> entry.modifier().amount()).sum();
        return Double.isFinite(amount) ? Math.max(0.0, amount) : 0.0;
    }

    public static void copyUtilityModifiers(ItemAttributeModifierEvent source, ItemAttributeModifierEvent destination) {
        for (var entry : source.getModifiers()) {
            if (!entry.attribute().equals(Attributes.ATTACK_DAMAGE) && !entry.attribute().equals(Attributes.ATTACK_SPEED)) {
                destination.addModifier(entry.attribute(), entry.modifier(), entry.slot());
            }
        }
    }

    public static MaterialCrossbowRules.Profile crossbowProfile(MaterialCrossbowItem crossbow) {
        return MaterialCrossbowRules.profile(BuiltInRegistries.ITEM.getKey(crossbow).getPath());
    }

    public static boolean tridentDamageId(ResourceLocation identifier) {
        return TRIDENT_DAMAGE_IDS.contains(identifier);
    }
}
