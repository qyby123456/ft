/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.level.Level
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.util;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public final class Fx {
    private Fx() {
    }

    private static ServerLevel server(Level level) {
        ServerLevel s;
        return level instanceof ServerLevel ? (s = (ServerLevel)level) : null;
    }

    public static void dust(Level level, double x, double y, double z, int count, float r, float g, float b, double spread, double speed) {
        ServerLevel s = Fx.server(level);
        if (s == null) {
            return;
        }
        s.m_8767_((ParticleOptions)new DustParticleOptions(new Vector3f(r, g, b), 1.0f), x, y, z, count, spread, spread, spread, speed);
    }

    public static void particles(Level level, double x, double y, double z, int count, ParticleOptions type, double spread, double speed) {
        ServerLevel s = Fx.server(level);
        if (s == null) {
            return;
        }
        s.m_8767_(type, x, y, z, count, spread, spread, spread, speed);
    }

    public static void ring(Level level, double cx, double cy, double cz, double radius, int points, ParticleOptions type, double speed) {
        ServerLevel s = Fx.server(level);
        if (s == null) {
            return;
        }
        for (int i = 0; i < points; ++i) {
            double a = (double)i * (Math.PI * 2 / (double)points);
            s.m_8767_(type, cx + Math.cos(a) * radius, cy, cz + Math.sin(a) * radius, 1, 0.0, 0.1, 0.0, speed);
        }
    }

    public static void sound(Level level, double x, double y, double z, SoundEvent event, float volume, float pitch) {
        level.m_6263_(null, x, y, z, event, SoundSource.HOSTILE, volume, pitch);
    }
}

