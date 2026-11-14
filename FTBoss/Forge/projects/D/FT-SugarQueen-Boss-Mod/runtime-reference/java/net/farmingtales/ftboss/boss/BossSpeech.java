/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.server.ServerLifecycleHooks
 */
package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

public class BossSpeech {
    private static final List<Line> PENDING = new CopyOnWriteArrayList<Line>();

    public static void speak(Level level, double x, double y, double z, double radius, int intervalTicks, String ... lines) {
        if (level == null || level.f_46443_) {
            return;
        }
        long now = level.m_46467_();
        int n = 0;
        for (int i = 0; i < lines.length; ++i) {
            if (lines[i] == null || lines[i].isEmpty()) continue;
            PENDING.add(new Line((ResourceKey<Level>)level.m_46472_(), x, y, z, radius, now + (long)i * (long)intervalTicks, lines[i]));
            ++n;
        }
        FTBossMod.LOGGER.info("[FTBoss] \u53f0\u8bcd\u6392\u961f: {} \u53e5, \u95f4\u9694 {} tick", (Object)n, (Object)intervalTicks);
    }

    public static void clear() {
        PENDING.clear();
    }

    private static final class Line {
        final ResourceKey<Level> dim;
        final double x;
        final double y;
        final double z;
        final double radius;
        final long due;
        final String text;

        Line(ResourceKey<Level> dim, double x, double y, double z, double radius, long due, String text) {
            this.dim = dim;
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
            this.due = due;
            this.text = text;
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss")
    public static final class Ticker {
        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent e) {
            if (e.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
                return;
            }
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return;
            }
            ArrayList<Line> done = null;
            for (Line l : PENDING) {
                ServerLevel sl = server.m_129880_(l.dim);
                if (sl == null) {
                    if (done == null) {
                        done = new ArrayList<Line>();
                    }
                    done.add(l);
                    continue;
                }
                if (sl.m_46467_() < l.due) continue;
                for (ServerPlayer p : sl.m_6907_()) {
                    if (!(p.m_20275_(l.x, l.y, l.z) <= l.radius * l.radius)) continue;
                    p.m_213846_((Component)Component.m_237113_((String)l.text));
                }
                if (done == null) {
                    done = new ArrayList();
                }
                done.add(l);
            }
            if (done != null && !done.isEmpty()) {
                PENDING.removeAll(done);
            }
        }
    }
}

