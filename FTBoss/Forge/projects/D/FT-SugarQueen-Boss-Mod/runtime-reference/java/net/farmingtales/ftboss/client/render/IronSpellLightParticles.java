/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.particle.TraceParticleOptions
 *  io.redspace.ironsspellbooks.particle.ZapParticleOption
 *  io.redspace.ironsspellbooks.registries.ParticleRegistry
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.render;

import io.redspace.ironsspellbooks.particle.TraceParticleOptions;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.IronSpellLightRules;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class IronSpellLightParticles {
    private static IronSpellLightRules.Budget budget = new IronSpellLightRules.Budget();
    private static ClientLevel world;

    private IronSpellLightParticles() {
    }

    public static boolean emit(SpellVisualPacket packet) {
        boolean reduced;
        double progress;
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || !packet.valid() || !level.m_46472_().m_135782_().equals((Object)packet.dimension()) || minecraft.f_91066_.m_231929_().m_231551_() == ParticleStatus.MINIMAL || minecraft.m_91104_()) {
            return false;
        }
        if (world != level) {
            world = level;
            budget = new IronSpellLightRules.Budget();
        }
        Vec3 camera = minecraft.f_91063_.m_109153_().m_90583_();
        Vec3 segment = packet.end().m_82546_(packet.start());
        double d = progress = segment.m_82556_() < 0.001 ? 0.0 : Math.max(0.0, Math.min(1.0, camera.m_82546_(packet.start()).m_82526_(segment) / segment.m_82556_()));
        if (camera.m_82557_(packet.start().m_82549_(segment.m_82490_(progress))) > 4096.0) {
            return false;
        }
        Entity owner = level.m_6815_(packet.ownerId());
        if (owner == null || !owner.m_20148_().equals(packet.owner())) {
            owner = minecraft.f_91074_;
        }
        if (owner == null) {
            return false;
        }
        boolean bl = reduced = minecraft.f_91066_.m_231929_().m_231551_() == ParticleStatus.DECREASED;
        if (packet.phase() == SpellVisualRules.Phase.BEAM) {
            IronSpellLightRules.Beam mode = IronSpellLightRules.beam(packet.spell());
            if (mode == IronSpellLightRules.Beam.NONE) {
                return false;
            }
            Vec3 end = IronSpellLightParticles.clip(level, owner, packet.start(), packet.end());
            Vec3 start = packet.start();
            if (packet.spell() == SpellVisualRules.Spell.SANGUINE_SUTURE) {
                Vec3 originalStart = start;
                start = end;
                end = originalStart;
            }
            return IronSpellLightParticles.line(level, mode, packet.spell().color, start, end, reduced);
        }
        IronSpellLightRules.Accent accent = IronSpellLightRules.accent(packet.spell(), packet.phase());
        if (accent == IronSpellLightRules.Accent.NONE) {
            return false;
        }
        Vec3 center = packet.start().m_82520_(0.0, accent == IronSpellLightRules.Accent.REBOOT_ZAP ? 1.1 : 0.7, 0.0);
        int count = reduced ? 3 : 6;
        for (int index = 0; index < count; ++index) {
            double angle = (double)index * Math.PI * 2.0 / (double)count;
            double radius = accent == IronSpellLightRules.Accent.REBOOT_ZAP ? 1.3 : packet.spell().radius;
            Vec3 end = center.m_82520_(Math.cos(angle) * (radius *= SpellVisuals.radiusScale(owner, packet.spell().id)), accent == IronSpellLightRules.Accent.HOLY_RAYS ? 1.2 : 0.0, Math.sin(angle) * radius);
            end = IronSpellLightParticles.clip(level, owner, center, end);
            IronSpellLightParticles.line(level, accent == IronSpellLightRules.Accent.HOLY_RAYS ? IronSpellLightRules.Beam.TRACE : IronSpellLightRules.Beam.ZAP, packet.spell().color, center, end, reduced);
        }
        return false;
    }

    public static void clear() {
        world = null;
        budget = new IronSpellLightRules.Budget();
    }

    private static Vec3 clip(ClientLevel level, Entity owner, Vec3 start, Vec3 end) {
        return level.m_45547_(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner)).m_82450_();
    }

    private static boolean line(ClientLevel level, IronSpellLightRules.Beam mode, int color, Vec3 start, Vec3 end, boolean reduced) {
        Vec3 segment = end.m_82546_(start);
        double distance = segment.m_82553_();
        int samples = IronSpellLightRules.samples(distance, reduced);
        if (samples == 0) {
            return false;
        }
        if (!(mode != IronSpellLightRules.Beam.TRACE && mode != IronSpellLightRules.Beam.FROST_TRACE && mode != IronSpellLightRules.Beam.SIPHON || IronSpellLightRules.traceSafe(end.f_82479_) && IronSpellLightRules.traceSafe(end.f_82480_) && IronSpellLightRules.traceSafe(end.f_82481_))) {
            return false;
        }
        int cost = IronSpellLightRules.cost(mode, distance, reduced);
        if (!budget.reserve(level.m_46467_(), cost, reduced ? 128 : 256)) {
            return false;
        }
        Vector3f tint = new Vector3f((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f);
        if (mode == IronSpellLightRules.Beam.ZAP) {
            Particle particle = IronSpellLightParticles.create((ParticleOptions)new ZapParticleOption(end), start, Vec3.f_82478_);
            if (particle == null) {
                return false;
            }
            particle.m_107253_(tint.x, tint.y, tint.z);
            particle.m_107257_(12);
            particle.m_107259_(new AABB(start, end).m_82400_(1.0));
            return true;
        }
        TraceParticleOptions options = new TraceParticleOptions(end.m_252839_(), tint);
        int emitted = 0;
        for (int index = 0; index < samples; ++index) {
            Particle blood;
            Vec3 point = start.m_165921_(end, (double)index / (double)samples);
            double remaining = point.m_82554_(end);
            Vec3 velocity = segment.m_82541_().m_82490_(Math.min(0.18, remaining / 10.0));
            Particle particle = IronSpellLightParticles.create((ParticleOptions)options, point, velocity);
            if (particle == null) continue;
            particle.m_6569_(mode == IronSpellLightRules.Beam.FROST_TRACE ? 0.55f : 0.65f);
            particle.m_107257_(8);
            ++emitted;
            if (mode != IronSpellLightRules.Beam.SIPHON || (blood = IronSpellLightParticles.create((ParticleOptions)ParticleRegistry.SIPHON_PARTICLE.get(), point, velocity.m_82490_(0.5))) == null) continue;
            blood.m_107257_(8);
            blood.m_6569_(0.7f);
        }
        return emitted > 0;
    }

    private static Particle create(ParticleOptions options, Vec3 position, Vec3 velocity) {
        return Minecraft.m_91087_().f_91061_.m_107370_(options, position.f_82479_, position.f_82480_, position.f_82481_, velocity.f_82479_, velocity.f_82480_, velocity.f_82481_);
    }
}

