package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

/** 麻雀适应性护盾：上一位Boss物理/法术总承伤较高的一类，支援存活时额外减伤10%，不叠层。 */
public final class ManorAdaptiveShield {
    private static final double SUPPORT_RANGE = 64.0D;
    private static final String ANNOUNCED = "ftboss:manor_adaptive_shield_announced";

    private ManorAdaptiveShield() {}

    public static int damageKind(DamageSource source) {
        return ManorDamageKind.classify(source);
    }

    public static int activeKind(LivingEntity entity) {
        UUID fight = ManorRaidCombat.mainFight(entity);
        if (fight == null || !entity.getUUID().equals(ManorFight.currentBoss(fight)) || !entity.isAlive()
                || entity.isRemoved() || entity.isInvisible()) return ManorRaidRules.OTHER_DAMAGE;
        EasterBossEntity boss = (EasterBossEntity) entity;
        if (boss.isManorIntroHidden() || boss.isXuanlanSkyHidden() || HiddenBossIntro.isActing(boss.getUUID())) {
            return ManorRaidRules.OTHER_DAMAGE;
        }
        ServerLevel level = (ServerLevel) entity.level();
        int dominant = ManorContributionData.get(level.getServer()).observe(fight, entity.getUUID()).adaptiveKind();
        if (dominant == ManorRaidRules.OTHER_DAMAGE) return dominant;
        boolean support = !level.getEntitiesOfClass(EasterBossEntity.class, boss.getBoundingBox().inflate(SUPPORT_RANGE),
                sparrow -> sparrow.getVariant() == EasterBossEntity.SPARROW_VARIANT && !sparrow.isMini()
                        && sparrow.isAlive() && !sparrow.isRemoved() && !sparrow.isInvisible()
                        && !sparrow.isManorIntroHidden() && !sparrow.isXuanlanSkyHidden()
                        && !HiddenBossIntro.isActing(sparrow.getUUID()) && fight.equals(ManorFight.fightId(sparrow))
                        && sparrow.distanceToSqr(boss) <= SUPPORT_RANGE * SUPPORT_RANGE).isEmpty();
        return support ? dominant : ManorRaidRules.OTHER_DAMAGE;
    }

    public static float reduce(LivingEntity entity, DamageSource source, float damage) {
        if (source == null || !Float.isFinite(damage) || damage <= 0.0F
                || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.is(DamageTypes.GENERIC_KILL)) return damage;
        int dominant = activeKind(entity);
        if (dominant == ManorRaidRules.OTHER_DAMAGE || dominant != damageKind(source)) return damage;
        UUID fight = ManorRaidCombat.mainFight(entity);
        EasterBossEntity boss = (EasterBossEntity) entity;
        ServerLevel level = (ServerLevel) entity.level();
        ManorRaidLedger ledger = ManorContributionData.get(level.getServer()).observe(fight, entity.getUUID());
        if (!boss.getPersistentData().getBoolean(ANNOUNCED)) {
            boss.getPersistentData().putBoolean(ANNOUNCED, true);
            FTBossMod.LOGGER.info("[FTBoss][麻雀适应性护盾] {} 获得{}额外10%减伤；上一位物理总量={}、法术总量={}",
                    boss.getName().getString(), dominant == ManorRaidRules.PHYSICAL_DAMAGE ? "物理" : "法术",
                    ledger.previousPhysical(), ledger.previousMagic());
        }
        return ManorRaidRules.adaptiveDamage(damage, dominant, damageKind(source), true, false);
    }
}
