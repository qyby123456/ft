package net.farmingtales.ftboss.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class NativeDamageAttackAttributes {
    private static final ResourceLocation MATERIAL_DAMAGE = ResourceLocation.fromNamespaceAndPath(
        "ftboss", "material/894c7a17-bf58-492f-ae96-d21abeb6a811");
    private static final ResourceLocation EVOLUTION_DAMAGE = ResourceLocation.fromNamespaceAndPath(
        "ftboss", "food_evolution_damage");

    private NativeDamageAttackAttributes() {
    }

    public static double withoutWeaponGrowth(AttributeInstance attribute) {
        if (attribute == null) {
            return 0.0;
        }
        AttributeInstance nativeAttack = new AttributeInstance(Attributes.ATTACK_DAMAGE, changed -> {
        });
        nativeAttack.setBaseValue(attribute.getBaseValue());
        for (var modifier : attribute.getModifiers()) {
            if (!modifier.is(MATERIAL_DAMAGE) && !modifier.is(EVOLUTION_DAMAGE)) {
                nativeAttack.addTransientModifier(modifier);
            }
        }
        return Math.max(0.0, nativeAttack.getValue());
    }
}
