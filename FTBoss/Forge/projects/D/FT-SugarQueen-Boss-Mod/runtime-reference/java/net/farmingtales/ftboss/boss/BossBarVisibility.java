/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerBossEvent
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 */
package net.farmingtales.ftboss.boss;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class BossBarVisibility {
    private static final Map<ServerBossEvent, WeakReference<Entity>> OWNERS = Collections.synchronizedMap(new WeakHashMap());

    private BossBarVisibility() {
    }

    private static boolean shouldHide(boolean bl, boolean bl2, boolean bl3) {
        return bl || bl2 || bl3;
    }

    public static boolean summoned(Entity entity) {
        EasterBossEntity easterBossEntity;
        return entity != null && BossBarVisibility.shouldHide(entity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)entity).isMini(), LemonSummonRewards.suppressed(entity), ManorFightMembers.ownerId(entity) != null);
    }

    public static void track(Entity entity, ServerBossEvent serverBossEvent) {
        if (entity != null && serverBossEvent != null) {
            OWNERS.put(serverBossEvent, new WeakReference<Entity>(entity));
        }
    }

    private static boolean hidden(ServerBossEvent serverBossEvent) {
        WeakReference<Entity> weakReference = OWNERS.get(serverBossEvent);
        return weakReference != null && BossBarVisibility.summoned((Entity)weakReference.get());
    }

    public static void visible(ServerBossEvent serverBossEvent, boolean bl) {
        serverBossEvent.m_8321_(bl && !BossBarVisibility.hidden(serverBossEvent));
    }

    public static void addPlayer(ServerBossEvent serverBossEvent, ServerPlayer serverPlayer) {
        if (BossBarVisibility.hidden(serverBossEvent)) {
            BossBars.hide(serverBossEvent);
        } else {
            serverBossEvent.m_6543_(serverPlayer);
        }
    }
}

