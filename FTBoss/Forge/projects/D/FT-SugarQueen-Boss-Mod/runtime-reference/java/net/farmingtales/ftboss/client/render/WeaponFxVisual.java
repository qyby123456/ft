/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.particles.DustColorTransitionOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.render;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import net.farmingtales.ftboss.skill.WeaponFxPacket;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class WeaponFxVisual {
    private static final List<Effect> ACTIVE = new ArrayList<Effect>();
    private static final EnumMap<WeaponFxRules.Theme, ParticleOptions> COLORS = new EnumMap(WeaponFxRules.Theme.class);
    private static ClientLevel activeLevel;
    private static int remainingBudget;
    private static int rotation;

    private WeaponFxVisual() {
    }

    private static Vector3f color(int rgb) {
        return new Vector3f((float)(rgb >> 16 & 0xFF) / 255.0f, (float)(rgb >> 8 & 0xFF) / 255.0f, (float)(rgb & 0xFF) / 255.0f);
    }

    public static void receive(WeaponFxPacket packet) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null || !packet.valid() || packet.kind().doraGun() || packet.kind().pufferBat() || !level.m_46472_().m_135782_().equals((Object)packet.dimension())) {
            return;
        }
        if (activeLevel != level) {
            ACTIVE.clear();
            activeLevel = level;
        }
        if (packet.kind() == WeaponFxRules.Kind.MIST || packet.kind() == WeaponFxRules.Kind.RING) {
            ACTIVE.removeIf(effect -> effect.packet.kind() == packet.kind() && effect.packet.theme() == packet.theme() && effect.packet.start().m_82557_(packet.start()) < 0.01);
        }
        if (packet.kind() == WeaponFxRules.Kind.GOURMET_HIT) {
            level.m_7785_(packet.start().f_82479_, packet.start().f_82480_, packet.start().f_82481_, SoundEvents.f_144243_, SoundSource.PLAYERS, 0.38f, 1.16f, false);
        }
        if (ACTIVE.size() >= 64) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Effect(packet));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null || minecraft.f_91073_ != activeLevel) {
            ACTIVE.clear();
            activeLevel = minecraft.f_91073_;
            return;
        }
        if (minecraft.m_91104_() || minecraft.f_91074_ == null || ACTIVE.isEmpty()) {
            return;
        }
        int setting = ((ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_()).ordinal();
        remainingBudget = WeaponFxRules.budget(setting);
        Vec3 camera = minecraft.f_91063_.m_109153_().m_90583_();
        int size = ACTIVE.size();
        int first = Math.floorMod(rotation++, size);
        for (int index = 0; index < size; ++index) {
            Effect effect2 = ACTIVE.get((first + index) % size);
            WeaponFxPacket packet = effect2.packet;
            Vec3 segment = packet.end().m_82546_(packet.start());
            double along = segment.m_82556_() < 1.0E-8 ? 0.0 : Math.max(0.0, Math.min(1.0, camera.m_82546_(packet.start()).m_82526_(segment) / segment.m_82556_()));
            double distance = Math.max(0.0, camera.m_82554_(packet.start().m_82549_(segment.m_82490_(along))) - (double)packet.radius());
            int requested = packet.kind() == WeaponFxRules.Kind.GOURMET_HIT ? 18 : (packet.kind() == WeaponFxRules.Kind.HIT || packet.kind() == WeaponFxRules.Kind.TRACER ? 12 : 28);
            int samples = WeaponFxRules.samples(requested, setting, distance * distance);
            if (samples > 0 && remainingBudget > 0) {
                WeaponFxVisual.emit(minecraft, effect2, samples);
            }
            ++effect2.age;
        }
        ACTIVE.removeIf(effect -> effect.age >= effect.packet.kind().ticks);
    }

    private static void emit(Minecraft minecraft, Effect effect, int samples) {
        WeaponFxPacket packet = effect.packet;
        Vec3 center = packet.start();
        int age = effect.age;
        WeaponFxRules.Theme theme = packet.theme();
        ParticleOptions dust = COLORS.get((Object)theme);
        if (packet.kind() == WeaponFxRules.Kind.BEAM || packet.kind() == WeaponFxRules.Kind.TRACER) {
            if (age != 0 && age != 3) {
                return;
            }
            Vec3 direction = packet.end().m_82546_(center).m_82541_();
            Vec3 side = direction.m_82537_(Math.abs(direction.f_82480_) > 0.95 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0)).m_82541_();
            Vec3 up = side.m_82537_(direction).m_82541_();
            for (int index = 1; index <= samples; ++index) {
                double progress = (double)index / (double)samples;
                double angle = progress * Math.PI * 6.0;
                Vec3 point = center.m_165921_(packet.end(), progress);
                if (age == 3) {
                    point = point.m_82549_(side.m_82490_(Math.cos(angle) * 0.075)).m_82549_(up.m_82490_(Math.sin(angle) * 0.075));
                }
                WeaponFxVisual.particle(minecraft, dust, point, Vec3.f_82478_, packet.kind() == WeaponFxRules.Kind.TRACER ? 3 : (age == 0 ? 7 : 4));
                if (age != 0 || index % 7 != 0) continue;
                WeaponFxVisual.particle(minecraft, WeaponFxVisual.accent(theme), point, Vec3.f_82478_, 5);
            }
            if (age == 0 && packet.kind() != WeaponFxRules.Kind.TRACER) {
                WeaponFxVisual.spark(minecraft, packet.end(), theme, 4);
            }
            return;
        }
        if (packet.kind() == WeaponFxRules.Kind.GOURMET_HIT) {
            WeaponFxVisual.emitGourmetHit(minecraft, packet, age, samples, dust);
            return;
        }
        if (packet.kind() == WeaponFxRules.Kind.HIT) {
            if (age != 0 && age != 2) {
                return;
            }
            Vec3 side = new Vec3(1.0, 0.0, 0.0).m_82524_((float)(-Math.toRadians(packet.yaw())));
            for (int index = 0; index < samples; ++index) {
                double progress = (double)index / (double)(samples - 1);
                double offset = (progress - 0.5) * (double)packet.radius() * 2.0;
                Vec3 point = center.m_82549_(side.m_82490_(offset)).m_82520_(0.0, offset * (age == 0 ? 0.65 : -0.65), 0.0);
                WeaponFxVisual.particle(minecraft, dust, point, new Vec3(0.0, 0.006, 0.0), 6);
            }
            if (age == 0) {
                WeaponFxVisual.spark(minecraft, center, theme, 3);
            }
            return;
        }
        if (packet.kind() == WeaponFxRules.Kind.MIST) {
            if (age % 2 != 0) {
                return;
            }
            if (age == 0) {
                for (int index = 0; index < samples; ++index) {
                    double angle = (double)index * Math.PI * 2.0 / (double)samples;
                    Vec3 point = center.m_82520_(Math.cos(angle) * (double)packet.radius(), 0.08, Math.sin(angle) * (double)packet.radius());
                    WeaponFxVisual.particle(minecraft, dust, point, Vec3.f_82478_, 10);
                }
            }
            int count = Math.max(3, samples / 4);
            for (int index = 0; index < count; ++index) {
                double angle = (double)index * Math.PI * 2.0 / (double)count + (double)age * 0.31;
                double radius = (double)packet.radius() * (0.25 + 0.65 * minecraft.f_91073_.f_46441_.m_188500_());
                Vec3 point = center.m_82520_(Math.cos(angle) * radius, 0.12 + (double)(age % 4) * 0.12, Math.sin(angle) * radius);
                WeaponFxVisual.particle(minecraft, dust, point, new Vec3(0.0, 0.012, 0.0), 10);
                if (index != 0) continue;
                WeaponFxVisual.particle(minecraft, (ParticleOptions)ParticleTypes.f_123772_, point, new Vec3(0.0, 0.02, 0.0), 8);
            }
            return;
        }
        if (age % 3 != 0) {
            return;
        }
        double progress = WeaponFxRules.ease(Math.min(1.0, (double)age / (double)(packet.kind().ticks - 3)));
        double radius = (double)packet.radius() * (packet.kind() == WeaponFxRules.Kind.BLAST ? 0.2 + 0.8 * progress : 1.0);
        for (int index = 0; index < samples; ++index) {
            double angle = packet.kind() == WeaponFxRules.Kind.SWEEP ? WeaponFxRules.sweepAngle(packet.yaw(), progress, (double)index / ((double)samples - 1.0)) : Math.PI * 2 * (double)index / (double)samples + (double)age * 0.055;
            Vec3 point = center.m_82520_(Math.cos(angle) * radius, 0.06 + Math.sin(angle * 3.0) * 0.035, Math.sin(angle) * radius);
            if (packet.kind() == WeaponFxRules.Kind.SWEEP) {
                point = minecraft.f_91073_.m_45547_(new ClipContext(center, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)minecraft.f_91074_)).m_82450_();
            }
            WeaponFxVisual.particle(minecraft, dust, point, new Vec3(0.0, 0.005, 0.0), 7);
            if (index % 8 != 0 || age != 0) continue;
            WeaponFxVisual.particle(minecraft, WeaponFxVisual.accent(theme), point, new Vec3(0.0, 0.025, 0.0), 8);
        }
        if (packet.kind() == WeaponFxRules.Kind.BLAST && age == 0) {
            WeaponFxVisual.spark(minecraft, center.m_82520_(0.0, 0.2, 0.0), theme, 6);
        }
    }

    private static void emitGourmetHit(Minecraft minecraft, WeaponFxPacket packet, int age, int samples, ParticleOptions dust) {
        Vec3 center = packet.start();
        Vec3 side = new Vec3(1.0, 0.0, 0.0).m_82524_((float)(-Math.toRadians(packet.yaw())));
        Vec3 depth = new Vec3(-side.f_82481_, 0.0, side.f_82479_);
        if (age == 4) {
            int count = Math.max(6, samples / 2);
            double radius = (double)packet.radius() * 0.38;
            for (int index = 0; index < count; ++index) {
                double angle = Math.PI * 2 * (double)index / (double)count;
                Vec3 point = center.m_82549_(side.m_82490_(Math.cos(angle) * radius)).m_82520_(0.0, Math.sin(angle) * radius * 0.72, 0.0);
                WeaponFxVisual.particle(minecraft, dust, point, depth.m_82490_(0.004), 5);
                if (index % 3 != 0) continue;
                WeaponFxVisual.particle(minecraft, (ParticleOptions)ParticleTypes.f_123810_, point, new Vec3(0.0, 0.012, 0.0), 5);
            }
            WeaponFxVisual.spark(minecraft, center, WeaponFxRules.Theme.GOURMET, 5);
            return;
        }
        if (age > 3) {
            return;
        }
        boolean rising = age < 2;
        double echo = (age & 1) == 0 ? 0.0 : 0.055;
        for (int index = 0; index < samples; ++index) {
            double progress = (double)index / (double)Math.max(1, samples - 1);
            double offset = (progress - 0.5) * (double)packet.radius() * 2.0;
            double arch = Math.sin(progress * Math.PI) * (double)packet.radius() * 0.18;
            double height = offset * (rising ? 0.72 : -0.72) + arch - echo;
            Vec3 point = center.m_82549_(side.m_82490_(offset)).m_82520_(0.0, height, 0.0).m_82549_(depth.m_82490_((progress - 0.5) * echo));
            WeaponFxVisual.particle(minecraft, dust, point, new Vec3(0.0, 0.007, 0.0), (age & 1) == 0 ? 7 : 5);
        }
        if ((age & 1) == 0) {
            WeaponFxVisual.spark(minecraft, center.m_82520_(0.0, (double)packet.radius() * 0.08, 0.0), WeaponFxRules.Theme.GOURMET, 4);
        }
    }

    private static ParticleOptions accent(WeaponFxRules.Theme theme) {
        return switch (theme) {
            case WeaponFxRules.Theme.FROST -> ParticleTypes.f_175821_;
            case WeaponFxRules.Theme.BLOOD -> ParticleTypes.f_123797_;
            case WeaponFxRules.Theme.NATURE, WeaponFxRules.Theme.BLESSING -> ParticleTypes.f_123748_;
            case WeaponFxRules.Theme.WINE, WeaponFxRules.Theme.TIDE, WeaponFxRules.Theme.COMPOST -> ParticleTypes.f_123772_;
            case WeaponFxRules.Theme.ARC, WeaponFxRules.Theme.GOLD -> ParticleTypes.f_175830_;
            case WeaponFxRules.Theme.GOURMET -> ParticleTypes.f_123810_;
            case WeaponFxRules.Theme.SHADOW -> ParticleTypes.f_123789_;
            default -> ParticleTypes.f_123810_;
        };
    }

    private static void spark(Minecraft minecraft, Vec3 center, WeaponFxRules.Theme theme, int count) {
        for (int index = 0; index < count; ++index) {
            double angle = Math.PI * 2 * (double)index / (double)count;
            Vec3 velocity = new Vec3(Math.cos(angle) * 0.07, 0.035 + (double)index * 0.006, Math.sin(angle) * 0.07);
            WeaponFxVisual.particle(minecraft, WeaponFxVisual.accent(theme), center, velocity, 9);
        }
    }

    private static void particle(Minecraft minecraft, ParticleOptions options, Vec3 point, Vec3 velocity, int lifetime) {
        if (remainingBudget <= 0 || !minecraft.f_91073_.m_46805_(BlockPos.m_274446_((Position)point)) || minecraft.f_91063_.m_109153_().m_90583_().m_82557_(point) < 0.16) {
            return;
        }
        --remainingBudget;
        Particle particle = minecraft.f_91061_.m_107370_(options, point.f_82479_, point.f_82480_, point.f_82481_, velocity.f_82479_, velocity.f_82480_, velocity.f_82481_);
        if (particle != null) {
            particle.m_107257_(Math.min(lifetime, 14));
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == activeLevel) {
            ACTIVE.clear();
            activeLevel = null;
        }
    }

    static {
        for (WeaponFxRules.Theme theme : WeaponFxRules.Theme.values()) {
            COLORS.put(theme, (ParticleOptions)new DustColorTransitionOptions(WeaponFxVisual.color(theme.highlight), WeaponFxVisual.color(theme.color), 0.9f));
        }
    }

    private static final class Effect {
        final WeaponFxPacket packet;
        int age;

        Effect(WeaponFxPacket packet) {
            this.packet = packet;
        }
    }
}

