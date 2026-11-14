package net.farmingtales.ftboss.effect;

import net.farmingtales.ftboss.boss.ai.CombatStatusRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** 共享状态的固定属性与燃烧伤害；等级不放大破甲或控制强度。2026-10-26：新增。 */
public final class CombatStatusEffect extends MobEffect {
    private final String id;

    public CombatStatusEffect(String id, int color) {
        super(id.equals("valor") ? MobEffectCategory.BENEFICIAL : MobEffectCategory.HARMFUL, color);
        this.id = id;
        if (id.equals("armor_break") || id.equals("grievous_wound")) {
            double reduction = id.equals("armor_break") ? CombatStatusRules.ARMOR_BREAK : CombatStatusRules.GRIEVOUS_ARMOR;
            modifier(Attributes.ARMOR, "71c82b10-c4e2-4e97-8ca1-6b4130561001", reduction, AttributeModifier.Operation.MULTIPLY_TOTAL);
            modifier(Attributes.ARMOR_TOUGHNESS, "71c82b10-c4e2-4e97-8ca1-6b4130561002", reduction, AttributeModifier.Operation.MULTIPLY_TOTAL);
        } else if (id.equals("bone_fracture")) {
            modifier(Attributes.MOVEMENT_SPEED, "71c82b10-c4e2-4e97-8ca1-6b4130561003",
                    CombatStatusRules.BONE_SLOWNESS, AttributeModifier.Operation.MULTIPLY_TOTAL);
            modifier(Attributes.ATTACK_DAMAGE, "71c82b10-c4e2-4e97-8ca1-6b4130561004",
                    CombatStatusRules.BONE_WEAKNESS, AttributeModifier.Operation.ADDITION);
        } else if (id.equals("stun") || id.equals("rooted")) {
            modifier(Attributes.MOVEMENT_SPEED, "71c82b10-c4e2-4e97-8ca1-6b4130561005", -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
    }

    private void modifier(Attribute attribute, String uuid, double amount, AttributeModifier.Operation operation) {
        String unique = java.util.UUID.nameUUIDFromBytes((uuid + id).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
        addAttributeModifier(attribute, unique, amount, operation);
    }

    public String id() { return id; }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        return modifier.getAmount();
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return id.equals("ignite") && duration % CombatStatusRules.SECOND == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel level) || !entity.isAlive()) return;
        entity.hurt(entity.damageSources().onFire(), 1.0F);
        level.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5,
                entity.getZ(), 6, 0.2, 0.3, 0.2, 0.01);
    }
}
