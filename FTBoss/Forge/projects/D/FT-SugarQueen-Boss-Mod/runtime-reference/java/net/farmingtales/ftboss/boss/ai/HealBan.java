/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingHealEvent
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Applicable
 *  net.minecraftforge.eventbus.api.Event$Result
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.FTEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class HealBan {
    public static final int BAN_TICKS = 600;
    private static final Map<UUID, Long> UNTIL = new ConcurrentHashMap<UUID, Long>();
    private static final Map<UUID, Long> LAST_LOG = new ConcurrentHashMap<UUID, Long>();
    private static final int LOG_CAP = 256;
    private static final Map<UUID, Float> SNAPSHOT = new ConcurrentHashMap<UUID, Float>();
    private static final Set<String> RECOVERY_EFFECTS = Set.of("minecraft:regeneration", "minecraft:absorption", "minecraft:saturation", "minecraft:instant_health");

    private HealBan() {
    }

    private static MobEffect healBanEffect() {
        try {
            return (MobEffect)FTEffects.HEAL_BAN.get();
        }
        catch (Throwable t) {
            return null;
        }
    }

    public static void ban(ServerPlayer p) {
        HealBan.ban(p, "\u9ebb\u96c0");
    }

    public static void ban(ServerPlayer p, String byWhom) {
        if (p == null) {
            return;
        }
        MobEffect eff = HealBan.healBanEffect();
        if (eff != null) {
            p.m_7292_(new MobEffectInstance(eff, 600, 0, false, true, true));
        } else {
            UNTIL.put(p.m_20148_(), p.m_9236_().m_46467_() + 600L);
        }
        p.m_5661_((Component)Component.m_237113_((String)("\u00a74\u00a7l" + byWhom + "\u5c01\u4f4f\u4e86\u4f60\u7684\u6062\u590d\u529b\uff01\u00a7c30 \u79d2\u5185\u65e0\u6cd5\u56de\u590d\u8840\u91cf")), true);
        FTBossMod.LOGGER.info("[FTBoss][\u7981\u7597] {} \u88ab\u300c{}\u300d\u7981\u7597 {} \u79d2(\u6807\u8bb0\u6548\u679c={})", new Object[]{p.m_7755_().getString(), byWhom, 30, eff != null ? "ftboss:heal_ban" : "\u4ec5\u8ba1\u65f6\u8868"});
    }

    public static boolean isBanned(LivingEntity le) {
        if (le == null) {
            return false;
        }
        MobEffect eff = HealBan.healBanEffect();
        if (eff != null && le.m_21023_(eff)) {
            return true;
        }
        Long t = UNTIL.get(le.m_20148_());
        if (t == null) {
            return false;
        }
        if (le.m_9236_().m_46467_() > t) {
            UNTIL.remove(le.m_20148_());
            return false;
        }
        return true;
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent e) {
        try {
            if (e.getAmount() > 0.0f && HealBan.isBanned(e.getEntity())) {
                LivingEntity le = e.getEntity();
                long now = le.m_9236_().m_46467_();
                float blocked = e.getAmount();
                e.setAmount(0.0f);
                Long last = LAST_LOG.get(le.m_20148_());
                if (last == null || now - last >= 40L) {
                    if (LAST_LOG.size() > 256) {
                        LAST_LOG.entrySet().removeIf(en -> now - (Long)en.getValue() >= 40L);
                    }
                    LAST_LOG.put(le.m_20148_(), now);
                    FTBossMod.LOGGER.info("[FTBoss][\u7981\u7597] {} \u7684\u56de\u8840\u88ab\u62e6\u4e0b({} \u70b9)", (Object)le.m_7755_().getString(), (Object)String.format("%.2f", Float.valueOf(blocked)));
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @SubscribeEvent
    public static void onPlayerTickEnd(TickEvent.PlayerTickEvent e) {
        try {
            if (e == null || e.player == null) {
                return;
            }
            if (e.phase != TickEvent.Phase.END) {
                return;
            }
            Player le = e.player;
            UUID id = le.m_20148_();
            if (!HealBan.isBanned((LivingEntity)le)) {
                SNAPSHOT.remove(id);
                return;
            }
            float now = le.m_21223_();
            Float cap = SNAPSHOT.get(id);
            if (cap == null) {
                SNAPSHOT.put(id, Float.valueOf(now));
                return;
            }
            if (now > cap.floatValue() && cap.floatValue() > 0.0f) {
                le.m_21153_(cap.floatValue());
                HealBan.logBlocked((LivingEntity)le, "\u56de\u590d", cap.floatValue());
            }
            SNAPSHOT.put(id, Float.valueOf(le.m_21223_()));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static boolean isRecoveryEffect(MobEffect effect) {
        if (effect == null) {
            return false;
        }
        try {
            ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey((Object)effect);
            return key != null && RECOVERY_EFFECTS.contains(key.toString());
        }
        catch (Throwable t) {
            return false;
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onEffectApplicable(MobEffectEvent.Applicable e) {
        try {
            if (e == null || e.getEntity() == null) {
                return;
            }
            if (!HealBan.isRecoveryEffect(e.getEffectInstance() == null ? null : e.getEffectInstance().m_19544_())) {
                return;
            }
            if (!HealBan.isBanned(e.getEntity())) {
                return;
            }
            e.setResult(Event.Result.DENY);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void logBlocked(LivingEntity le, String what, float kept) {
        long now = le.m_9236_().m_46467_();
        Long last = LAST_LOG.get(le.m_20148_());
        if (last != null && now - last < 40L) {
            return;
        }
        if (LAST_LOG.size() > 256) {
            LAST_LOG.entrySet().removeIf(en -> now - (Long)en.getValue() >= 40L);
        }
        LAST_LOG.put(le.m_20148_(), now);
        FTBossMod.LOGGER.info("[FTBoss][\u7981\u7597] {} \u7684{}\u88ab\u62e6\u4e0b(\u8840\u91cf\u4fdd\u6301\u5728 {})", new Object[]{le.m_7755_().getString(), what, String.format("%.1f", Float.valueOf(kept))});
        if (le instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)le;
            sp.m_5661_((Component)Component.m_237113_((String)"\u00a74\u7981\u7597\u4e2d\uff1a\u6062\u590d\u6548\u679c\u65e0\u6548"), true);
        }
    }
}

