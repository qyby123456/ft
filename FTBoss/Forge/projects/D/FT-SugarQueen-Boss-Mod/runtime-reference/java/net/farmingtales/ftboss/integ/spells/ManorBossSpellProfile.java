/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.damage.DamageSources
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.damage.DamageSources;
import net.farmingtales.ftboss.boss.ai.ExtraDebuffs;
import net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.ManorIronSpells;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.integ.spells.ManorSpellEffects;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.GongdiCombatRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ManorBossSpellProfile {
    private ManorBossSpellProfile() {
    }

    public static float power(ManorSpellRules.Kind kind) {
        return switch (kind) {
            default -> throw new IncompatibleClassChangeError();
            case ManorSpellRules.Kind.EFFIGY_VOLLEY -> 16.0f;
            case ManorSpellRules.Kind.CANDLE_REAP -> 26.0f;
            case ManorSpellRules.Kind.CANDLE_PRISON -> 14.0f;
            case ManorSpellRules.Kind.ARCANE_FUSILLADE -> 15.0f;
            case ManorSpellRules.Kind.BREACH_SHELL -> 32.0f;
            case ManorSpellRules.Kind.RENDING_CLEAVE -> 28.0f;
            case ManorSpellRules.Kind.SEARING_PAN -> 100.8f;
            case ManorSpellRules.Kind.FOUR_TIDES -> 26.0f;
            case ManorSpellRules.Kind.ELIXIR_RAIN -> 13.0f;
            case ManorSpellRules.Kind.ORBITAL_GEARS -> 17.0f;
            case ManorSpellRules.Kind.PRESSURE_LANCE -> 335.2f;
            case ManorSpellRules.Kind.SHADOW_BRAND -> 78.4f;
            case ManorSpellRules.Kind.VOID_ECHO -> 24.0f;
            case ManorSpellRules.Kind.LUMINOUS_REBUKE -> 26.0f;
            case ManorSpellRules.Kind.FEATHER_FAN -> 25.0f;
            case ManorSpellRules.Kind.FROST_NEEDLES -> 24.0f;
            case ManorSpellRules.Kind.WHITEOUT_EDICT -> 14.0f;
            case ManorSpellRules.Kind.EFFIGY_PROCESSION -> 17.0f;
            case ManorSpellRules.Kind.CANDLE_ECLIPSE -> 36.0f;
            case ManorSpellRules.Kind.PRISM_BATTERY -> 20.0f;
            case ManorSpellRules.Kind.BLOOD_VERDICT -> 38.0f;
            case ManorSpellRules.Kind.CITRUS_NOVA -> 32.0f;
            case ManorSpellRules.Kind.MAGNETIC_STORM -> 16.0f;
            case ManorSpellRules.Kind.NIGHTFALL_BLADES -> 22.0f;
            case ManorSpellRules.Kind.SERAPH_DESCENT -> 34.0f;
            case ManorSpellRules.Kind.WINTER_CLOCK -> 16.0f;
            case ManorSpellRules.Kind.STAR_CROSS -> 104.0f;
            case ManorSpellRules.Kind.ROSE_STARFALL -> 64.0f;
            case ManorSpellRules.Kind.EFFIGY_WARD, ManorSpellRules.Kind.CRIMSON_OATH, ManorSpellRules.Kind.HEARTH_FEAST, ManorSpellRules.Kind.HALO_SANCTUARY, ManorSpellRules.Kind.WINGSTEP, ManorSpellRules.Kind.TIDAL_LOTUS, ManorSpellRules.Kind.FEATHER_REFUGE -> 0.0f;
        };
    }

    public static float damage(EasterBossEntity boss, LivingEntity target, ManorSpellRules.Kind kind, float base) {
        return switch (kind) {
            case ManorSpellRules.Kind.RENDING_CLEAVE -> GongdiCombatRules.cleaveDamage(boss.gongdiMeleeDamage(target), boss.m_20182_().m_82546_(target.m_20182_()).m_165925_() >= Math.pow(4.5 * BossSpellTuning.radiusScale((LivingEntity)boss), 2.0));
            case ManorSpellRules.Kind.SEARING_PAN, ManorSpellRules.Kind.SHADOW_BRAND -> boss.manorSkillDamage(target, base, 2.0);
            case ManorSpellRules.Kind.PRESSURE_LANCE -> boss.manorSkillDamage(target, base, 1.5);
            default -> BossSpellTuning.scaledPower(ManorIronSpells.spell(kind), (LivingEntity)boss, base);
        };
    }

    public static boolean hurt(EasterBossEntity easterBossEntity, LivingEntity livingEntity, ManorSignatureSpell manorSignatureSpell, float f) {
        boolean bl;
        float f2 = ManorBossSpellProfile.damage(easterBossEntity, livingEntity, manorSignatureSpell.kind, f);
        float f3 = livingEntity.m_21223_();
        if (manorSignatureSpell.kind == ManorSpellRules.Kind.SHADOW_BRAND) {
            float f4 = (float)((double)f2 * 0.5 * 1.5);
            livingEntity.f_19802_ = 0;
            bl = livingEntity.m_6469_(easterBossEntity.m_269291_().m_269333_((LivingEntity)easterBossEntity), f2 - f4);
            livingEntity.f_19802_ = 0;
            bl |= ManorAdaptiveDamage.hurtSkill(livingEntity, (Entity)easterBossEntity, easterBossEntity.m_269291_().m_269341_(), f4);
        } else {
            livingEntity.f_19802_ = 0;
            bl = DamageSources.applyDamage((Entity)livingEntity, (float)f2, (DamageSource)manorSignatureSpell.getDamageSource((Entity)easterBossEntity));
        }
        if (!bl) {
            return false;
        }
        switch (manorSignatureSpell.kind) {
            case SEARING_PAN: {
                Vec3 vec3 = livingEntity.m_20182_().m_82546_(easterBossEntity.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
                BossKnockback.push((Entity)livingEntity, vec3.f_82479_ * 0.9, 1.65, vec3.f_82481_ * 0.9);
                livingEntity.f_19864_ = true;
                livingEntity.m_20254_(18);
                ExtraDebuffs.blueFire(livingEntity, 360);
                ManorSpellEffects.effect((LivingEntity)easterBossEntity, livingEntity, MobEffects.f_19612_, 480, 1);
                break;
            }
            case PRESSURE_LANCE: {
                Vec3 vec3 = livingEntity.m_20182_().m_82546_(easterBossEntity.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
                BossKnockback.push((Entity)livingEntity, vec3.f_82479_ * 0.98, 0.22, vec3.f_82481_ * 0.98);
                livingEntity.f_19864_ = true;
                ManorSpellEffects.effect((LivingEntity)easterBossEntity, livingEntity, MobEffects.f_19597_, 360, 1);
                ManorSpellEffects.effect((LivingEntity)easterBossEntity, livingEntity, MobEffects.f_19613_, 360, 1);
                break;
            }
            case SHADOW_BRAND: {
                ManorSpellEffects.effect((LivingEntity)easterBossEntity, livingEntity, MobEffects.f_216964_, 300, 0);
                ManorSpellEffects.effect((LivingEntity)easterBossEntity, livingEntity, MobEffects.f_19610_, 300, 1);
                ManorSpellEffects.effect((LivingEntity)easterBossEntity, livingEntity, MobEffects.f_19615_, 360, 2);
                MobEffectInstance mobEffectInstance = livingEntity.m_21124_((MobEffect)FTEffects.SHADOW_EROSION.get());
                int n = mobEffectInstance == null ? 0 : Math.min(4, mobEffectInstance.m_19564_() + 1);
                ManorSpellEffects.effect((LivingEntity)easterBossEntity, livingEntity, (MobEffect)FTEffects.SHADOW_EROSION.get(), 200, n);
                break;
            }
            case RENDING_CLEAVE: {
                if (!(livingEntity instanceof LivingEntity)) break;
                LivingEntity livingEntity2 = livingEntity;
                Level level = easterBossEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break;
                ServerLevel serverLevel = (ServerLevel)level;
                if (!(easterBossEntity.m_20182_().m_82546_(livingEntity.m_20182_()).m_165925_() >= Math.pow(4.5 * BossSpellTuning.radiusScale((LivingEntity)easterBossEntity), 2.0))) break;
                easterBossEntity.onSignatureCleaveHit(serverLevel, livingEntity2, Math.max(0.0f, f3 - livingEntity.m_21223_()));
                break;
            }
        }
        return true;
    }
}

