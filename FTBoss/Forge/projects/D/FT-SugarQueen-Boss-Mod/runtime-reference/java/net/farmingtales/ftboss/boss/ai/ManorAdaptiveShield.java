/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ai.ManorContributionData;
import net.farmingtales.ftboss.boss.ai.ManorDamageKind;
import net.farmingtales.ftboss.boss.ai.ManorRaidCombat;
import net.farmingtales.ftboss.boss.ai.ManorRaidLedger;
import net.farmingtales.ftboss.boss.ai.ManorRaidRules;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class ManorAdaptiveShield {
    private static final double SUPPORT_RANGE = 64.0;
    private static final String ANNOUNCED = "ftboss:manor_adaptive_shield_announced";

    private ManorAdaptiveShield() {
    }

    public static int damageKind(DamageSource source) {
        return ManorDamageKind.classify(source);
    }

    public static int activeKind(LivingEntity entity) {
        UUID fight = ManorRaidCombat.mainFight(entity);
        if (fight == null || !entity.m_20148_().equals(ManorFight.currentBoss(fight)) || !entity.m_6084_() || entity.m_213877_() || entity.m_20145_()) {
            return 0;
        }
        EasterBossEntity boss = (EasterBossEntity)entity;
        if (boss.isManorIntroHidden() || boss.isXuanlanSkyHidden() || HiddenBossIntro.isActing(boss.m_20148_())) {
            return 0;
        }
        ServerLevel level = (ServerLevel)entity.m_9236_();
        int dominant = ManorContributionData.get(level.m_7654_()).observe(fight, entity.m_20148_()).adaptiveKind();
        if (dominant == 0) {
            return dominant;
        }
        boolean support = !level.m_6443_(EasterBossEntity.class, boss.m_20191_().m_82400_(64.0), sparrow -> sparrow.getVariant() == 9 && !sparrow.isMini() && sparrow.m_6084_() && !sparrow.m_213877_() && !sparrow.m_20145_() && !sparrow.isManorIntroHidden() && !sparrow.isXuanlanSkyHidden() && !HiddenBossIntro.isActing(sparrow.m_20148_()) && fight.equals(ManorFight.fightId((Entity)sparrow)) && sparrow.m_20280_((Entity)boss) <= 4096.0).isEmpty();
        return support ? dominant : 0;
    }

    public static float reduce(LivingEntity entity, DamageSource source, float damage) {
        if (source == null || !Float.isFinite(damage) || damage <= 0.0f || source.m_269533_(DamageTypeTags.f_268738_) || source.m_276093_(DamageTypes.f_286979_)) {
            return damage;
        }
        int dominant = ManorAdaptiveShield.activeKind(entity);
        if (dominant == 0 || dominant != ManorAdaptiveShield.damageKind(source)) {
            return damage;
        }
        UUID fight = ManorRaidCombat.mainFight(entity);
        EasterBossEntity boss = (EasterBossEntity)entity;
        ServerLevel level = (ServerLevel)entity.m_9236_();
        ManorRaidLedger ledger = ManorContributionData.get(level.m_7654_()).observe(fight, entity.m_20148_());
        if (!boss.getPersistentData().m_128471_(ANNOUNCED)) {
            boss.getPersistentData().m_128379_(ANNOUNCED, true);
            FTBossMod.LOGGER.info("[FTBoss][\u9ebb\u96c0\u9002\u5e94\u6027\u62a4\u76fe] {} \u83b7\u5f97{}\u989d\u59165%\u51cf\u4f24\uff1b\u4e0a\u4e00\u4f4d\u7269\u7406\u603b\u91cf={}\u3001\u6cd5\u672f\u603b\u91cf={}", new Object[]{boss.m_7755_().getString(), dominant == 1 ? "\u7269\u7406" : "\u6cd5\u672f", ledger.previousPhysical(), ledger.previousMagic()});
        }
        return ManorRaidRules.adaptiveDamage(damage, dominant, ManorAdaptiveShield.damageKind(source), true, false);
    }
}

