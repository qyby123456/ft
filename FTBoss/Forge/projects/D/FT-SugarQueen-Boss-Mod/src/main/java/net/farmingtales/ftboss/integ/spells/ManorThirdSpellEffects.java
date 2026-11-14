package net.farmingtales.ftboss.integ.spells;

import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.util.IronSpellLightRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class ManorThirdSpellEffects {
    private ManorThirdSpellEffects() {}

    public static void support(ServerLevel level, LivingEntity caster, ManorSignatureSpell spell, int rank) {
        for (LivingEntity ally : ManorSpellEffects.allies(level, caster, caster.position())) {
            if (spell.kind == Kind.TIDAL_LOTUS) {
                ally.clearFire();
                ally.removeEffect(MobEffects.POISON);
                ally.heal(3 + rank);
                ManorSpellEffects.effect(caster, ally, MobEffects.WATER_BREATHING, 160 + rank * 40, 0);
                ManorSpellEffects.effect(caster, ally, MobEffects.DAMAGE_RESISTANCE, 60 + rank * 20, 0);
            } else if (spell.kind == Kind.FEATHER_REFUGE) {
                ManorSpellEffects.effect(caster, ally, MobEffects.ABSORPTION, 120 + rank * 40, (rank - 1) / 2);
                ManorSpellEffects.effect(caster, ally, MobEffects.SLOW_FALLING, 160 + rank * 40, 0);
                ManorSpellEffects.effect(caster, ally, MobEffects.REGENERATION, 60 + rank * 20, 0);
            }
        }
    }

    public static void pulse(ServerLevel level, LivingEntity caster, ManorSignatureSpell spell, int rank,
                             Vec3 center, float damage, int age) {
        if (!ManorSpellRules.extraPulse(spell.kind, age)) return;
        SpellVisuals.show(level, caster, spell.kind.id, Phase.PULSE, center);
        float stolen = 0;
        for (LivingEntity target : ManorSpellEffects.targets(level, caster, center, ManorSpellRules.extraRadius(spell.kind))) {
            float health = target.getHealth();
            if (!ManorSpellEffects.hurt(caster, target, spell, damage)) continue;
            if (IronSpellLightRules.beam(SpellVisualRules.find(spell.kind.id)) != IronSpellLightRules.Beam.NONE) {
                Vec3 start = spell.kind == Kind.CANDLE_ECLIPSE ? target.getEyePosition()
                        : spell.kind == Kind.SERAPH_DESCENT ? target.position().add(0, 3, 0) : center.add(0, 0.9, 0);
                Vec3 end = spell.kind == Kind.CANDLE_ECLIPSE ? caster.getEyePosition() : target.getBoundingBox().getCenter();
                SpellVisuals.beam(level, caster, spell.kind.id, start, end);
            }
            switch (spell.kind) {
                case EFFIGY_PROCESSION -> ManorSpellEffects.effect(caster, target, MobEffects.WEAKNESS, 50, 0);
                case CANDLE_ECLIPSE -> stolen += Math.max(0, health - target.getHealth()) * 0.15F;
                case PRISM_BATTERY -> ManorSpellEffects.effect(caster, target, MobEffects.GLOWING, 80, 0);
                case BLOOD_VERDICT -> ManorSpellEffects.effect(caster, target, FTEffects.ARMOR_SHRED.get(), 80, 0);
                case CITRUS_NOVA -> {
                    target.setSecondsOnFire(3 + rank);
                    net.farmingtales.ftboss.util.BossKnockback.knockback(caster, target, 0.6, center.x - target.getX(), center.z - target.getZ());
                }
                case MAGNETIC_STORM -> {
                    Vec3 offset = center.subtract(target.position()).multiply(1, 0, 1);
                    if (offset.lengthSqr() > 0.36) {
                        Vec3 impulse = offset.normalize().scale(0.18);
                        net.farmingtales.ftboss.util.BossKnockback.push(caster, target, impulse.x, 0, impulse.z);
                        target.hurtMarked = true;
                    }
                    ManorSpellEffects.effect(caster, target, MobEffects.MOVEMENT_SLOWDOWN, 30, 0);
                }
                case NIGHTFALL_BLADES -> ManorSpellEffects.effect(caster, target, MobEffects.WEAKNESS, 60, 0);
                case SERAPH_DESCENT -> {
                    ManorSpellEffects.effect(caster, target, MobEffects.GLOWING, 100, 0);
                    net.farmingtales.ftboss.util.BossKnockback.knockback(caster, target, 0.5, center.x - target.getX(), center.z - target.getZ());
                }
                case WINTER_CLOCK -> {
                    target.setTicksFrozen(Math.max(target.getTicksFrozen(), 100 + rank * 10));
                    int pulse = (age - ManorSpellRules.extraDelay(spell.kind)) / 20;
                    ManorSpellEffects.effect(caster, target, MobEffects.MOVEMENT_SLOWDOWN, 50, Math.min(2, pulse));
                }
                default -> { }
            }
        }
        if (stolen > 0) caster.heal(Math.min(6, stolen));
    }
}
