/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.ManorFlyBan;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ManorChaseTracker {
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        try {
            EasterBossEntity boss;
            if (!(event.getEntity() instanceof Player)) {
                return;
            }
            if (event.getAmount() <= 0.0f) {
                return;
            }
            Entity source = event.getSource().m_7639_();
            if (source == null) {
                source = event.getSource().m_7640_();
            }
            if ((boss = ManorChaseTracker.resolveBoss(source)) != null) {
                boss.onPlayerHitLanded();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static EasterBossEntity resolveBoss(Entity source) {
        for (int depth = 0; source != null && depth < 8; ++depth) {
            Projectile projectile;
            if (source instanceof EasterBossEntity) {
                EasterBossEntity boss = (EasterBossEntity)source;
                return boss;
            }
            if (source instanceof Projectile && (projectile = (Projectile)source).m_19749_() != null) {
                source = projectile.m_19749_();
                continue;
            }
            Entity owner = ManorFightMembers.owner(source);
            if (owner == null || owner == source) {
                return null;
            }
            source = owner;
        }
        return null;
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ManorFlyBan.clear(null);
        FTBossMod.LOGGER.info("[FTBoss][Manor] \u670d\u52a1\u5668\u505c\u673a: \u6e05\u7a7a\u573a\u5730\u7981\u98de\u7a97\u53e3");
    }
}

