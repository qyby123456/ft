package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.damage.DamageSources;
import net.farmingtales.ftboss.boss.ai.ExtraDebuffs;
import net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage;
import net.farmingtales.ftboss.effect.BlueFireEffect;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.effect.ShadowErosionEffect;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.GongdiCombatRules;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class ManorBossSpellProfile {
    private ManorBossSpellProfile() {}

    public static float power(Kind kind) {
        return switch (kind) {
            case EFFIGY_VOLLEY -> 16;
            case CANDLE_REAP -> 26;
            case CANDLE_PRISON -> 14;
            case ARCANE_FUSILLADE -> 15;
            case BREACH_SHELL -> 32;
            case RENDING_CLEAVE -> 28;
            case SEARING_PAN -> EasterBossEntity.PAN_DAMAGE;
            case FOUR_TIDES -> 26;
            case ELIXIR_RAIN -> 13;
            case ORBITAL_GEARS -> 17;
            case PRESSURE_LANCE -> 335.2F;
            case SHADOW_BRAND -> (float) (EasterBossEntity.SHADOW_DAMAGE * EasterBossEntity.SHADOW_DAMAGE_CUT);
            case VOID_ECHO -> 24;
            case LUMINOUS_REBUKE -> 26;
            case FEATHER_FAN -> 25;
            case FROST_NEEDLES -> 24;
            case WHITEOUT_EDICT -> 14;
            case EFFIGY_PROCESSION -> 17;
            case CANDLE_ECLIPSE -> 36;
            case PRISM_BATTERY -> 20;
            case BLOOD_VERDICT -> 38;
            case CITRUS_NOVA -> 32;
            case MAGNETIC_STORM -> 16;
            case NIGHTFALL_BLADES -> 22;
            case FADED_VOID_SPIRAL, VOID_FOUNTAIN_CASCADE -> kind.damage + 8;
            case SERAPH_DESCENT -> 34;
            case WINTER_CLOCK -> 16;
            case STAR_CROSS -> net.farmingtales.ftboss.util.QizaiRules.CROSS_DAMAGE;
            case ROSE_STARFALL -> net.farmingtales.ftboss.util.QizaiRules.STARFALL_DAMAGE;
            case STAR_INFERNO -> net.farmingtales.ftboss.util.QizaiSweepRules.STAR_DAMAGE;
            case EFFIGY_WARD, CRIMSON_OATH, HEARTH_FEAST, HALO_SANCTUARY, WINGSTEP, TIDAL_LOTUS, FEATHER_REFUGE -> 0;
        };
    }

    public static float damage(EasterBossEntity boss, LivingEntity target, Kind kind, float base) {
        return switch (kind) {
            case RENDING_CLEAVE -> GongdiCombatRules.cleaveDamage(boss.gongdiMeleeDamage(target),
                    boss.position().subtract(target.position()).horizontalDistanceSqr()
                            >= Math.pow(GongdiCombatRules.INNER_RADIUS * BossSpellTuning.radiusScale(boss), 2));
            case SEARING_PAN, SHADOW_BRAND -> boss.manorSkillDamage(target, base, 2.0);
            case PRESSURE_LANCE -> boss.manorSkillDamage(target, base, 1.5);
            default -> BossSpellTuning.scaledPower(ManorIronSpells.spell(kind), boss, base);
        };
    }

    public static boolean hurt(EasterBossEntity boss, LivingEntity target, ManorSignatureSpell spell, float power) {
        float amount = damage(boss, target, spell.kind, power);
        float before = target.getHealth();
        boolean hit;
        if (spell.kind == Kind.SHADOW_BRAND) {
            float voidPart = (float) (amount * EasterBossEntity.SHADOW_VOID_HALF * 1.5);
            target.invulnerableTime = 0;
            hit = target.hurt(boss.damageSources().mobAttack(boss), amount - voidPart);
            target.invulnerableTime = 0;
            hit |= ManorAdaptiveDamage.hurtSkill(target, boss, boss.damageSources().fellOutOfWorld(), voidPart);
        } else {
            target.invulnerableTime = 0;
            hit = DamageSources.applyDamage(target, amount, spell.getDamageSource(boss));
        }
        if (!hit) return false;
        switch (spell.kind) {
            case SEARING_PAN -> {
                Vec3 outward = target.position().subtract(boss.position()).multiply(1, 0, 1).normalize();
                net.farmingtales.ftboss.util.BossKnockback.push(target, outward.x * 0.9, 1.65, outward.z * 0.9);
                target.hurtMarked = true;
                target.setSecondsOnFire(18);
                ExtraDebuffs.blueFire(target, BlueFireEffect.TICKS);
                ManorSpellEffects.effect(boss, target, MobEffects.HUNGER, 480, 1);
            }
            case PRESSURE_LANCE -> {
                Vec3 direction = target.position().subtract(boss.position()).multiply(1, 0, 1).normalize();
                net.farmingtales.ftboss.util.BossKnockback.push(target, direction.x * EasterBossEntity.VENT_KNOCKBACK, 0.22,
                        direction.z * EasterBossEntity.VENT_KNOCKBACK);
                target.hurtMarked = true;
                ManorSpellEffects.effect(boss, target, MobEffects.MOVEMENT_SLOWDOWN, 360, 1);
                ManorSpellEffects.effect(boss, target, MobEffects.WEAKNESS, 360, 1);
            }
            case SHADOW_BRAND -> {
                ManorSpellEffects.effect(boss, target, MobEffects.DARKNESS, EasterBossEntity.SHADOW_DARKNESS_TICKS, 0);
                ManorSpellEffects.effect(boss, target, MobEffects.BLINDNESS, EasterBossEntity.SHADOW_DARKNESS_TICKS, 1);
                ManorSpellEffects.effect(boss, target, MobEffects.WITHER, 360, 2);
                MobEffectInstance current = target.getEffect(FTEffects.SHADOW_EROSION.get());
                int amplifier = current == null ? 0 : Math.min(ShadowErosionEffect.MAX_STACKS - 1, current.getAmplifier() + 1);
                ManorSpellEffects.effect(boss, target, FTEffects.SHADOW_EROSION.get(), ShadowErosionEffect.TICKS, amplifier);
            }
            case RENDING_CLEAVE -> {
                if (target instanceof Player player && boss.level() instanceof ServerLevel level
                        && boss.position().subtract(target.position()).horizontalDistanceSqr()
                        >= Math.pow(GongdiCombatRules.INNER_RADIUS * BossSpellTuning.radiusScale(boss), 2)) {
                    boss.onSignatureCleaveHit(level, player, Math.max(0, before - target.getHealth()));
                }
            }
            default -> { }
        }
        return true;
    }
}
