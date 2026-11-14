/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorMercy;
import net.farmingtales.ftboss.boss.ai.BossOutgoingDamageNerfs;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.effect.ShadowBlindEffect;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ShadowVoidSplit {
    public static final int SHADOW_VARIANT = 7;
    private static final long FX_CD = 20L;
    private static final long FX_LOG_CD = 100L;
    private static final long SPLIT_LOG_CD = 40L;
    private static final Map<UUID, Long> LAST_FX = new ConcurrentHashMap<UUID, Long>();
    private static final Map<UUID, Long> LAST_FX_LOG = new ConcurrentHashMap<UUID, Long>();
    private static final Map<UUID, Long> LAST_SPLIT_LOG = new ConcurrentHashMap<UUID, Long>();
    private static final long MAINTAIN_CD = 5L;
    private static final int MAINTAIN_TICKS = 60;
    private static final double MAINTAIN_RANGE = 24.0;
    private static final long MAINTAIN_HIT_WINDOW = 300L;
    private static final Map<UUID, Long> LAST_MAINTAIN_LOG = new ConcurrentHashMap<UUID, Long>();

    private ShadowVoidSplit() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent e) {
        try {
            Entity attacker = e.getSource().m_7639_();
            if (!(attacker instanceof EasterBossEntity)) {
                return;
            }
            EasterBossEntity boss = (EasterBossEntity)attacker;
            if (boss.getVariant() != 7 || boss.isMini()) {
                return;
            }
            LivingEntity livingEntity = e.getEntity();
            if (!(livingEntity instanceof Player)) {
                return;
            }
            Player p = (Player)livingEntity;
            long now = boss.m_9236_().m_46467_();
            boss.onShadowHitPlayer(now);
            ShadowVoidSplit.applyDarknessAndBlindness(p, now);
            if (!boss.shadowVoidMode()) {
                return;
            }
            float amount = e.getAmount();
            if (amount <= 0.0f) {
                return;
            }
            float extra = amount * 0.5f;
            p.f_19802_ = 0;
            ManorMercy.withOwner((Entity)boss, () -> p.m_6469_(p.m_269291_().m_269341_(), extra));
            ShadowVoidSplit.logSplit(p, extra, now);
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u6298\u8033\u6839\u300c\u8fde\u51fb\u8f6c\u865a\u7a7a\u300d\u7ed3\u7b97\u5931\u8d25: {}", (Object)t.toString());
        }
    }

    private static void applyDarknessAndBlindness(Player p, long now) {
        Long last = LAST_FX.get(p.m_20148_());
        if (last != null && now - last < 20L) {
            return;
        }
        LAST_FX.put(p.m_20148_(), now);
        boolean keptBlind = p.m_21023_(MobEffects.f_19610_);
        boolean keptDark = p.m_21023_(MobEffects.f_216964_);
        ShadowVoidSplit.applyShadowBlind(p, 300);
        Long ll = LAST_FX_LOG.get(p.m_20148_());
        if (ll != null && now - ll < 100L) {
            return;
        }
        LAST_FX_LOG.put(p.m_20148_(), now);
        if (LAST_FX_LOG.size() > 64) {
            LAST_FX_LOG.entrySet().removeIf(en -> now - (Long)en.getValue() >= 100L);
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u6298\u8033\u6839\u6697\u5f71\u6548\u679c -> {} (\u4e0a\u4e00\u8f6e\u6b8b\u7559 \u5931\u660e={} \u9ed1\u6697={} | \u73b0\u5728 \u5931\u660e={} \u9ed1\u6697={})", new Object[]{p.m_7755_().getString(), keptBlind, keptDark, p.m_21023_(MobEffects.f_19610_), p.m_21023_(MobEffects.f_216964_)});
    }

    private static void logSplit(Player p, float half, long now) {
        Long last = LAST_SPLIT_LOG.get(p.m_20148_());
        if (last != null && now - last < 40L) {
            return;
        }
        LAST_SPLIT_LOG.put(p.m_20148_(), now);
        if (LAST_SPLIT_LOG.size() > 64) {
            LAST_SPLIT_LOG.entrySet().removeIf(en -> now - (Long)en.getValue() >= 40L);
        }
        float applied = BossOutgoingDamageNerfs.zheergenDamage(half);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u6298\u8033\u6839\u8fde\u51fb\u8f6c\u865a\u7a7a -> {} (\u62c6\u51fa {} \u70b9 -> \u5b9e\u9645\u7ed3\u7b97 {} \u70b9\u865a\u7a7a/\u7a7f\u7532)", new Object[]{p.m_7755_().getString(), String.format("%.1f", Float.valueOf(half)), String.format("%.1f", Float.valueOf(applied))});
    }

    public static void maintainTick(EasterBossEntity boss, ServerLevel sl) {
        try {
            if (boss == null || boss.getVariant() != 7 || boss.isMini() || !boss.m_6084_()) {
                return;
            }
            long now = sl.m_46467_();
            if (now % 5L != 0L) {
                return;
            }
            for (ServerPlayer p : sl.m_6907_()) {
                boolean recentlyHit;
                if (!p.m_6084_() || p.m_5833_()) continue;
                boolean near = p.m_20280_((Entity)boss) <= 576.0;
                Long hit = LAST_FX.get(p.m_20148_());
                boolean bl = recentlyHit = hit != null && now - hit <= 300L;
                if (!near && !recentlyHit) continue;
                boolean hadBlind = p.m_21023_(MobEffects.f_19610_);
                boolean hadDark = p.m_21023_(MobEffects.f_216964_);
                ShadowVoidSplit.applyShadowBlind((Player)p, 60);
                Long ll = LAST_MAINTAIN_LOG.get(p.m_20148_());
                if (ll != null && now - ll < 100L) continue;
                LAST_MAINTAIN_LOG.put(p.m_20148_(), now);
                if (LAST_MAINTAIN_LOG.size() > 64) {
                    LAST_MAINTAIN_LOG.entrySet().removeIf(en -> now - (Long)en.getValue() >= 100L);
                }
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u6298\u8033\u6839\u5931\u660e\u7ef4\u6301 -> {} (\u8865\u6302\u524d \u5931\u660e={} \u9ed1\u6697={} | \u8865\u6302\u540e \u5931\u660e={} \u9ed1\u6697={} \u6697\u5f71\u7b3c\u7f69={})", new Object[]{p.m_7755_().getString(), hadBlind, hadDark, p.m_21023_(MobEffects.f_19610_), p.m_21023_(MobEffects.f_216964_), p.m_21023_((MobEffect)FTEffects.SHADOW_BLIND.get())});
            }
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u6298\u8033\u6839\u5931\u660e\u7ef4\u6301\u5931\u8d25: {}", (Object)t.toString());
        }
    }

    private static void applyShadowBlind(Player p, int ticks) {
        ShadowBlindEffect.applyTo((LivingEntity)p, ticks);
    }
}

