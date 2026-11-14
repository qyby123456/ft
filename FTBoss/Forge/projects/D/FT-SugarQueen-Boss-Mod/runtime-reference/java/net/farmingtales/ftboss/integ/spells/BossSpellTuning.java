/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.config.SpellConfigManager
 *  io.redspace.ironsspellbooks.api.config.SpellConfigParameter
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.registry.AttributeRegistry
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.CastType
 *  io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.player.Player
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.config.SpellConfigParameter;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.farmingtales.ftboss.boss.NonManorFinaleDifficulty;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.ManorBossCombatRules;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

public final class BossSpellTuning {
    public static final double BOSS_CAST_MULT = 0.6666666666666666;
    public static final float PLAYER_POWER_MULT = 2.0f;
    public static final int PLAYER_BUFF_MULT = 2;

    private BossSpellTuning() {
    }

    public static int bossCast(int ticks) {
        return ticks <= 0 ? 0 : Math.max(1, (int)Math.ceil((double)ticks * 0.6666666666666666));
    }

    public static int bossCast(LivingEntity caster, int ticks) {
        return NonManorFinaleDifficulty.acceleratedTicks(caster, BossSpellTuning.bossCast(ticks));
    }

    public static boolean regularBoss(LivingEntity caster) {
        return caster != null && !BossSpellRules.forBoss(EntityType.m_20613_((EntityType)caster.m_6095_()).toString()).isEmpty();
    }

    public static float signaturePower(BossSpellRules.Kind kind) {
        float baseline = switch (kind) {
            default -> throw new IncompatibleClassChangeError();
            case BossSpellRules.Kind.FROST, BossSpellRules.Kind.DEFECT, BossSpellRules.Kind.COMPOST_REAP, BossSpellRules.Kind.WINE_FUSE, BossSpellRules.Kind.APPLE_HARVEST -> 34.0f;
            case BossSpellRules.Kind.TEA, BossSpellRules.Kind.TIDE_SURGE -> 30.0f;
            case BossSpellRules.Kind.WINE, BossSpellRules.Kind.TIDE, BossSpellRules.Kind.COMPOST_ERUPTION -> 32.0f;
            case BossSpellRules.Kind.COMPOST, BossSpellRules.Kind.FROST_HAIL, BossSpellRules.Kind.TEA_SUTURE, BossSpellRules.Kind.APPLE_ORBIT -> 24.0f;
            case BossSpellRules.Kind.APPLE, BossSpellRules.Kind.DEFECT_PULSE, BossSpellRules.Kind.COMPOST_ROT -> 36.0f;
            case BossSpellRules.Kind.FROST_LANCE, BossSpellRules.Kind.TEA_FLESH, BossSpellRules.Kind.APPLE_SEED -> 40.0f;
            case BossSpellRules.Kind.WINE_BOMB, BossSpellRules.Kind.FROST_SHATTER -> 42.0f;
            case BossSpellRules.Kind.TIDE_GEYSER, BossSpellRules.Kind.DEFECT_RAIL -> 44.0f;
            case BossSpellRules.Kind.TEA_THORNS -> 26.0f;
            case BossSpellRules.Kind.TEA_POLLEN -> 21.0f;
            case BossSpellRules.Kind.WINE_BREATH, BossSpellRules.Kind.DEFECT_STORM -> 23.0f;
            case BossSpellRules.Kind.TIDE_RIP -> 22.0f;
            case BossSpellRules.Kind.TEA_SANCTUARY, BossSpellRules.Kind.WINE_TOAST, BossSpellRules.Kind.DEFECT_REBOOT -> 10.0f;
            case BossSpellRules.Kind.APPLE_AEGIS, BossSpellRules.Kind.TEA_RENEWAL -> 8.0f;
            case BossSpellRules.Kind.FROST_PRISM, BossSpellRules.Kind.TIDE_BUBBLE -> 12.0f;
            case BossSpellRules.Kind.COMPOST_MULCH -> 9.0f;
        };
        return BossSpellRules.support(kind) ? baseline : baseline * 2.0f;
    }

    public static float scaledPower(AbstractSpell spell, LivingEntity caster, float base) {
        double config = (Double)SpellConfigManager.getSpellConfigValue((AbstractSpell)spell, (SpellConfigParameter)SpellConfigParameter.POWER_MULTIPLIER);
        double power = (double)base * caster.m_21133_((Attribute)AttributeRegistry.SPELL_POWER.get()) * spell.getSchoolType().getPowerFor(caster) * config;
        return Double.isFinite(power) ? (float)Math.max(0.0, Math.min(3.4028234663852886E38, power)) : 0.0f;
    }

    public static void fasterLegacyCast(AbstractSpellCastingMob caster, AbstractSpell spell) {
        if (!BossSpellTuning.regularBoss((LivingEntity)caster) || spell instanceof BossSignatureSpell || spell.getCastType() != CastType.LONG || !caster.isCasting()) {
            return;
        }
        MagicData magic = caster.getMagicData();
        if (!spell.getSpellId().equals(magic.getCastingSpellId())) {
            return;
        }
        magic.initiateCast(spell, magic.getCastingSpellLevel(), BossSpellTuning.bossCast((LivingEntity)caster, magic.getCastDuration()), magic.getCastSource(), magic.getCastingEquipmentSlot());
    }

    public static boolean manorBoss(LivingEntity caster) {
        EasterBossEntity boss;
        return caster instanceof EasterBossEntity && !(boss = (EasterBossEntity)caster).isMini() && !boss.isGiant();
    }

    public static boolean playerCast(LivingEntity caster) {
        return caster instanceof Player;
    }

    public static float playerPower(LivingEntity caster, float power) {
        return BossSpellTuning.playerCast(caster) ? power * 2.0f : power;
    }

    public static int buffTicks(LivingEntity caster, MobEffect effect, int ticks) {
        return BossSpellTuning.playerCast(caster) && effect != null && effect.m_19486_() ? ticks * 2 : ticks;
    }

    public static double radiusScale(LivingEntity caster) {
        return BossSpellTuning.manorBoss(caster) ? ManorBossCombatRules.radiusScale(((EasterBossEntity)caster).getVariant()) : 1.0;
    }

    public static double radius(LivingEntity caster, double baseRadius) {
        return BossSpellTuning.manorBoss(caster) ? ManorBossCombatRules.spellRadius(((EasterBossEntity)caster).getVariant(), baseRadius) : baseRadius;
    }
}

