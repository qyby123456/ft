/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerBossEvent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.boss;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class BossBars {
    private static final Map<UUID, ServerBossEvent> BARS = new ConcurrentHashMap<UUID, ServerBossEvent>();

    private BossBars() {
    }

    /*
     * WARNING - void declaration
     */
    public static void track(Entity entity, ServerBossEvent serverBossEvent) {
        void bar;
        Entity owner;
        BossBarVisibility.track(entity, serverBossEvent);
        if (owner == null || bar == null) {
            return;
        }
        if (owner.m_9236_().f_46443_) {
            return;
        }
        BARS.put(owner.m_20148_(), (ServerBossEvent)bar);
    }

    public static void hide(ServerBossEvent bar) {
        BossBarVisibility.visible(bar, false);
        bar.m_7706_();
    }

    public static void untrack(Entity owner, ServerBossEvent bar) {
        if (owner == null) {
            return;
        }
        if (owner.m_9236_().f_46443_) {
            return;
        }
        BARS.remove(owner.m_20148_(), bar);
        BossBars.hide(bar);
    }

    public static void forgetPlayer(ServerPlayer p) {
        if (p == null) {
            return;
        }
        for (ServerBossEvent bar : BARS.values()) {
            try {
                bar.m_6539_(p);
            }
            catch (Throwable throwable) {}
        }
    }

    public static void forEach(MinecraftServer server, Consumer<LivingEntity> fn) {
        if (server == null) {
            return;
        }
        block0: for (UUID id : BARS.keySet()) {
            for (ServerLevel sl : server.m_129785_()) {
                Entity e = sl.m_8791_(id);
                if (!(e instanceof LivingEntity)) continue;
                LivingEntity le = (LivingEntity)e;
                fn.accept(le);
                continue block0;
            }
        }
    }

    public static void sweep(MinecraftServer server) {
        if (server == null || BARS.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, ServerBossEvent>> it = BARS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, ServerBossEvent> en = it.next();
            boolean alive = false;
            for (ServerLevel sl : server.m_129785_()) {
                LivingEntity le;
                Entity e = sl.m_8791_(en.getKey());
                if (!(e instanceof LivingEntity) || (le = (LivingEntity)e).m_213877_() || !le.m_6084_()) continue;
                alive = true;
                break;
            }
            if (alive) continue;
            BossBars.hide(en.getValue());
            it.remove();
        }
    }
}

