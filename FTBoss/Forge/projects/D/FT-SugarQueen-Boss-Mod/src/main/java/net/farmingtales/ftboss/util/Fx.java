package net.farmingtales.ftboss.util;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * 统一的战斗特效/音效工具。
 * 之前各 Boss 实体各自复制了一份 sendParticles/playSound 封装, 这里收口成一处。
 * 所有方法都自带"服务端 + 非空"保护, 客户端调用直接静默返回。
 */
public final class Fx {

    private Fx() {}

    private static ServerLevel server(Level level) {
        return level instanceof ServerLevel s ? s : null;
    }

    /** 彩色尘埃粒子(酒液/金屑/木屑等) */
    public static void dust(Level level, double x, double y, double z, int count,
                            float r, float g, float b, double spread, double speed) {
        ServerLevel s = server(level);
        if (s == null) return;
        s.sendParticles(new DustParticleOptions(new Vector3f(r, g, b), 1.0f), x, y, z, count,
                spread, spread, spread, speed);
    }

    /** 原版简单粒子(POOF/CRIT/CLOUD/WITCH/SPORE_BLOSSOM_AIR 等) */
    public static void particles(Level level, double x, double y, double z, int count,
                                 ParticleOptions type, double spread, double speed) {
        ServerLevel s = server(level);
        if (s == null) return;
        s.sendParticles(type, x, y, z, count, spread, spread, spread, speed);
    }

    /** 水平圆环粒子(砸地冲击波) */
    public static void ring(Level level, double cx, double cy, double cz, double radius,
                            int points, ParticleOptions type, double speed) {
        ServerLevel s = server(level);
        if (s == null) return;
        for (int i = 0; i < points; i++) {
            double a = i * (Math.PI * 2 / points);
            s.sendParticles(type, cx + Math.cos(a) * radius, cy, cz + Math.sin(a) * radius,
                    1, 0, 0.1, 0, speed);
        }
    }

    /** 敌对声源音效(位置用实体坐标, 稍抬高) */
    public static void sound(Level level, double x, double y, double z, SoundEvent event, float volume, float pitch) {
        level.playSound(null, x, y, z, event, SoundSource.HOSTILE, volume, pitch);
    }
}
