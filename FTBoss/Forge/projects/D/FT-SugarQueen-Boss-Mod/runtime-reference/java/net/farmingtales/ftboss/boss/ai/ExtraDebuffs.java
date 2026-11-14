/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.FTEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class ExtraDebuffs {
    private static MobEffect bleedEffect;
    private static MobEffect bloodLossEffect;
    private static final Map<UUID, Long> LAST_BLEED_LOG;

    private ExtraDebuffs() {
    }

    public static void bleed(LivingEntity target, int ticks, int amp) {
        Object used;
        if (target == null || target.m_9236_().f_46443_) {
            return;
        }
        int a = Math.max(0, Math.min(4, amp));
        MobEffect own = (MobEffect)FTEffects.BLEED.get();
        if (own != null) {
            target.m_7292_(new MobEffectInstance(own, ticks, a, false, true, true));
            used = "ftboss:bleed";
        } else {
            if (bleedEffect == null && (bleedEffect = ExtraDebuffs.find("legendary_monsters", "bleeding")) == null) {
                bleedEffect = ExtraDebuffs.find("tridents_n_stuff", "bleeding");
            }
            if (bloodLossEffect == null) {
                bloodLossEffect = ExtraDebuffs.find("armamentarium", "blood_loss");
            }
            if (bleedEffect != null) {
                target.m_7292_(new MobEffectInstance(bleedEffect, ticks, a, false, true, true));
            }
            if (bloodLossEffect != null) {
                target.m_7292_(new MobEffectInstance(bloodLossEffect, ticks, a, false, true, true));
            }
            used = "\u5916\u6765(" + (bleedEffect != null || bloodLossEffect != null) + ")";
        }
        ExtraDebuffs.logBleed(target, ticks, a, (String)used);
    }

    private static void logBleed(LivingEntity target, int ticks, int amp, String used) {
        Level level = target.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        long now = sl.m_46467_();
        Long last = LAST_BLEED_LOG.get(target.m_20148_());
        if (last != null && now - last < 60L) {
            return;
        }
        if (LAST_BLEED_LOG.size() > 64) {
            LAST_BLEED_LOG.entrySet().removeIf(e -> now - (Long)e.getValue() >= 60L);
        }
        LAST_BLEED_LOG.put(target.m_20148_(), now);
        FTBossMod.LOGGER.info("[FTBoss][\u6d41\u8840] {} \u6302\u4e0a\u6d41\u8840 {} \u79d2 x{} \u5c42 (\u6765\u6e90 {}, \u6bcf\u79d2 {} \u70b9)", new Object[]{target.m_7755_().getString(), ticks / 20, amp + 1, used, String.format("%.1f", Float.valueOf(3.0f * (float)(amp + 1)))});
    }

    public static void blueFire(LivingEntity target, int ticks) {
        if (target == null || target.m_9236_().f_46443_) {
            return;
        }
        MobEffect eff = (MobEffect)FTEffects.BLUE_FIRE.get();
        if (eff == null) {
            return;
        }
        target.m_7292_(new MobEffectInstance(eff, ticks, 0, false, true, true));
    }

    private static MobEffect find(String ns, String path) {
        try {
            return (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(ns, path));
        }
        catch (Throwable t) {
            return null;
        }
    }

    static {
        LAST_BLEED_LOG = new ConcurrentHashMap<UUID, Long>();
    }
}

