/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.integ.spells;

import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.integ.spells.ManorSpellEffects;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.IronSpellLightRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class ManorThirdSpellEffects {
    private ManorThirdSpellEffects() {
    }

    public static void support(ServerLevel level, LivingEntity caster, ManorSignatureSpell spell, int rank) {
        for (LivingEntity ally : ManorSpellEffects.allies(level, caster, caster.m_20182_())) {
            if (spell.kind == ManorSpellRules.Kind.TIDAL_LOTUS) {
                ally.m_20095_();
                ally.m_21195_(MobEffects.f_19614_);
                ally.m_5634_((float)(3 + rank));
                ManorSpellEffects.effect(caster, ally, MobEffects.f_19608_, 160 + rank * 40, 0);
                ManorSpellEffects.effect(caster, ally, MobEffects.f_19606_, 60 + rank * 20, 0);
                continue;
            }
            if (spell.kind != ManorSpellRules.Kind.FEATHER_REFUGE) continue;
            ManorSpellEffects.effect(caster, ally, MobEffects.f_19617_, 120 + rank * 40, (rank - 1) / 2);
            ManorSpellEffects.effect(caster, ally, MobEffects.f_19591_, 160 + rank * 40, 0);
            ManorSpellEffects.effect(caster, ally, MobEffects.f_19605_, 60 + rank * 20, 0);
        }
    }

    public static void pulse(ServerLevel level, LivingEntity caster, ManorSignatureSpell spell, int rank, Vec3 center, float damage, int age) {
        if (!ManorSpellRules.extraPulse(spell.kind, age)) {
            return;
        }
        SpellVisuals.show(level, caster, spell.kind.id, SpellVisualRules.Phase.PULSE, center);
        float stolen = 0.0f;
        for (LivingEntity target : ManorSpellEffects.targets(level, caster, center, ManorSpellRules.extraRadius(spell.kind))) {
            float health = target.m_21223_();
            if (!ManorSpellEffects.hurt(caster, target, spell, damage)) continue;
            if (IronSpellLightRules.beam(SpellVisualRules.find(spell.kind.id)) != IronSpellLightRules.Beam.NONE) {
                Vec3 start = spell.kind == ManorSpellRules.Kind.CANDLE_ECLIPSE ? target.m_146892_() : (spell.kind == ManorSpellRules.Kind.SERAPH_DESCENT ? target.m_20182_().m_82520_(0.0, 3.0, 0.0) : center.m_82520_(0.0, 0.9, 0.0));
                Vec3 end = spell.kind == ManorSpellRules.Kind.CANDLE_ECLIPSE ? caster.m_146892_() : target.m_20191_().m_82399_();
                SpellVisuals.beam(level, caster, spell.kind.id, start, end);
            }
            switch (spell.kind) {
                case EFFIGY_PROCESSION: {
                    ManorSpellEffects.effect(caster, target, MobEffects.f_19613_, 50, 0);
                    break;
                }
                case CANDLE_ECLIPSE: {
                    stolen += Math.max(0.0f, health - target.m_21223_()) * 0.15f;
                    break;
                }
                case PRISM_BATTERY: {
                    ManorSpellEffects.effect(caster, target, MobEffects.f_19619_, 80, 0);
                    break;
                }
                case BLOOD_VERDICT: {
                    ManorSpellEffects.effect(caster, target, (MobEffect)FTEffects.ARMOR_SHRED.get(), 80, 0);
                    break;
                }
                case CITRUS_NOVA: {
                    target.m_20254_(3 + rank);
                    BossKnockback.knockback((Entity)caster, target, 0.6, center.f_82479_ - target.m_20185_(), center.f_82481_ - target.m_20189_());
                    break;
                }
                case MAGNETIC_STORM: {
                    Vec3 offset = center.m_82546_(target.m_20182_()).m_82542_(1.0, 0.0, 1.0);
                    if (offset.m_82556_() > 0.36) {
                        Vec3 impulse = offset.m_82541_().m_82490_(0.18);
                        BossKnockback.push((Entity)caster, (Entity)target, impulse.f_82479_, 0.0, impulse.f_82481_);
                        target.f_19864_ = true;
                    }
                    ManorSpellEffects.effect(caster, target, MobEffects.f_19597_, 30, 0);
                    break;
                }
                case NIGHTFALL_BLADES: {
                    ManorSpellEffects.effect(caster, target, MobEffects.f_19613_, 60, 0);
                    break;
                }
                case SERAPH_DESCENT: {
                    ManorSpellEffects.effect(caster, target, MobEffects.f_19619_, 100, 0);
                    BossKnockback.knockback((Entity)caster, target, 0.5, center.f_82479_ - target.m_20185_(), center.f_82481_ - target.m_20189_());
                    break;
                }
                case WINTER_CLOCK: {
                    target.m_146917_(Math.max(target.m_146888_(), 100 + rank * 10));
                    int pulse = (age - ManorSpellRules.extraDelay(spell.kind)) / 20;
                    ManorSpellEffects.effect(caster, target, MobEffects.f_19597_, 50, Math.min(2, pulse));
                    break;
                }
            }
        }
        if (stolen > 0.0f) {
            caster.m_5634_(Math.min(6.0f, stolen));
        }
    }
}

