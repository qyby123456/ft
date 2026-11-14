/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions
 *  io.redspace.ironsspellbooks.registries.ParticleRegistry
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.compat.irons;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.client.compat.irons.IronNativeSpellModels;
import net.farmingtales.ftboss.client.compat.irons.IronNativeSpellTextures;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.IronNativeSpellRules;
import net.farmingtales.ftboss.util.IronNativeSpellState;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

public final class IronNativeSpellAdapter {
    private static final IronNativeSpellState<Burst> STATE = new IronNativeSpellState();
    private static final MultiBufferSource.BufferSource BUFFERS = MultiBufferSource.m_109898_((BufferBuilder)new BufferBuilder(131072));
    private static ClientLevel world;
    private static long particleTick;

    private IronNativeSpellAdapter() {
    }

    public static boolean accept(SpellVisualPacket packet) {
        IronNativeSpellRules.Style style = IronNativeSpellRules.style(packet.spell(), packet.phase());
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (style == null || level == null || !packet.valid() || !level.m_46472_().m_135782_().equals((Object)packet.dimension())) {
            return false;
        }
        IronNativeSpellAdapter.advance(level);
        for (IronNativeSpellState.Entry<Burst> entry : STATE.entries()) {
            if (!entry.owner().equals(packet.owner()) || entry.spell() != packet.spell()) continue;
            entry.payload().clear();
        }
        if (STATE.entries().size() >= 24) {
            STATE.entries().get(0).payload().clear();
        }
        STATE.add(packet.owner(), packet.spell(), style, new Burst(packet), level.m_46467_());
        return true;
    }

    private static void advance(ClientLevel level) {
        if (world != level || level == null || level.m_46467_() < particleTick) {
            IronNativeSpellAdapter.clear();
        }
        world = level;
        if (level == null) {
            return;
        }
        long now = level.m_46467_();
        for (IronNativeSpellState.Entry<Burst> entry : STATE.entries()) {
            if (now - entry.born() < (long)IronNativeSpellRules.lifetime(entry.style()) && IronNativeSpellAdapter.alive(entry.payload())) continue;
            entry.payload().clear();
        }
        STATE.advance(level, now, IronNativeSpellAdapter::alive);
    }

    private static boolean alive(Burst burst) {
        LivingEntity living;
        Entity owner = world.m_6815_(burst.packet.ownerId());
        if (owner == null) {
            return !IronNativeSpellRules.follows(IronNativeSpellRules.style(burst.packet.spell(), burst.packet.phase()));
        }
        return owner.m_20148_().equals(burst.packet.owner()) && !owner.m_213877_() && !owner.m_5833_() && (!(owner instanceof LivingEntity) || (living = (LivingEntity)owner).m_6084_());
    }

    private static Vec3 origin(IronNativeSpellState.Entry<Burst> entry, float partial) {
        Entity owner = world.m_6815_(entry.payload().packet.ownerId());
        return IronNativeSpellRules.follows(entry.style()) && owner != null && owner.m_20148_().equals(entry.owner()) ? owner.m_20318_(partial) : entry.payload().packet.start();
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.m_91087_();
        IronNativeSpellAdapter.advance(minecraft.f_91073_);
        if (world == null || minecraft.m_91104_() || particleTick == world.m_46467_()) {
            return;
        }
        particleTick = world.m_46467_();
        int quality = minecraft.f_91066_.m_231929_().m_231551_() == ParticleStatus.MINIMAL ? 2 : (minecraft.f_91066_.m_231929_().m_231551_() == ParticleStatus.DECREASED ? 1 : 0);
        int budget = IronNativeSpellRules.particleBudget(quality);
        Vec3 camera = minecraft.f_91063_.m_109153_().m_90583_();
        List<IronNativeSpellState.Entry> entries = STATE.entries().stream().sorted(Comparator.comparingDouble(entry -> IronNativeSpellAdapter.origin(entry, 1.0f).m_82557_(camera))).toList();
        for (IronNativeSpellState.Entry entry2 : entries) {
            int count;
            Burst burst = (Burst)entry2.payload();
            int wave = IronNativeSpellRules.wave(particleTick - entry2.born(), entry2.style());
            if (wave < 0 || wave <= burst.emitted) continue;
            burst.emitted = wave;
            Vec3 center = IronNativeSpellAdapter.origin(entry2, 1.0f);
            if (center.m_82557_(camera) > 2304.0 || (count = Math.min(budget, IronNativeSpellRules.particlesPerWave(quality))) == 0) continue;
            IronNativeSpellAdapter.emit(entry2, center, count, wave);
            budget -= count;
        }
    }

    private static void emit(IronNativeSpellState.Entry<Burst> entry, Vec3 center, int count, int wave) {
        IronNativeSpellRules.Style style = entry.style();
        int color = entry.spell().color;
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        float radiusScale = (float)SpellVisuals.radiusScale(world.m_6815_(entry.payload().packet.ownerId()), entry.spell().id);
        double radius = entry.spell().radius * 0.8 * (double)radiusScale;
        for (int index = 0; index < count; ++index) {
            ParticleOptions options;
            double angle = (double)index * 2.399963 + (double)wave * 0.5;
            double spread = radius * Math.sqrt(((double)index + 0.5) / (double)count);
            Vec3 offset = new Vec3(Math.cos(angle) * spread, 0.3 + (double)(index % 3) * 0.15, Math.sin(angle) * spread);
            Vec3 velocity = new Vec3(0.0, 0.035, 0.0);
            switch (style) {
                case CASK_BLAST: 
                case SHELL_BLAST: {
                    if (index == 0) {
                        options = new BlastwaveParticleOptions(new Vector3f(red, green, blue), (style == IronNativeSpellRules.Style.CASK_BLAST ? 3.4f : 2.8f) * radiusScale);
                        offset = new Vec3(0.0, 0.5, 0.0);
                        velocity = Vec3.f_82478_;
                        break;
                    }
                    options = index % 3 == 0 ? (ParticleOptions)ParticleRegistry.FIERY_SMOKE_PARTICLE.get() : (ParticleOptions)ParticleRegistry.EMBER_PARTICLE.get();
                    offset = offset.m_82490_(0.35);
                    velocity = new Vec3(Math.cos(angle) * 0.17, 0.06 + (double)(index % 4) * 0.025, Math.sin(angle) * 0.17);
                    break;
                }
                case BLOOD_REAP: {
                    options = (ParticleOptions)ParticleRegistry.BLOOD_PARTICLE.get();
                    Vec3 direction = entry.payload().packet.end().m_82546_(entry.payload().packet.start());
                    double heading = Math.atan2(direction.f_82479_, direction.f_82481_);
                    double sweep = heading + ((double)index / (double)count - 0.5) * Math.PI * 0.8;
                    offset = new Vec3(Math.sin(sweep) * radius, 0.65 + (double)wave * 0.18, Math.cos(sweep) * radius);
                    velocity = new Vec3(-Math.sin(sweep) * 0.07, 0.035, -Math.cos(sweep) * 0.07);
                    break;
                }
                case SPORES: {
                    options = index % 3 == 0 ? (ParticleOptions)ParticleRegistry.ACID_PARTICLE.get() : (ParticleOptions)ParticleRegistry.ACID_BUBBLE_PARTICLE.get();
                    offset = offset.m_82520_(0.0, (double)(index % 4) * 0.22, 0.0);
                    break;
                }
                case MEDICINAL_RAIN: {
                    options = (ParticleOptions)ParticleRegistry.ACID_BUBBLE_PARTICLE.get();
                    offset = offset.m_82520_(0.0, 2.5, 0.0);
                    velocity = new Vec3(0.0, -0.14, 0.0);
                    break;
                }
                case SNOW: 
                case ICE_CROWN: {
                    options = index % 4 == 0 ? (ParticleOptions)ParticleRegistry.SNOW_DUST.get() : (ParticleOptions)ParticleRegistry.SNOWFLAKE_PARTICLE.get();
                    offset = offset.m_82520_(0.0, style == IronNativeSpellRules.Style.SNOW ? 2.4 : 0.8, 0.0);
                    velocity = new Vec3(0.025, -0.045, -0.015);
                    break;
                }
                case HEARTH: {
                    options = index % 4 == 0 ? (ParticleOptions)ParticleRegistry.EMBER_PARTICLE.get() : (ParticleOptions)ParticleRegistry.CLEANSE_PARTICLE.get();
                    break;
                }
                default: {
                    options = (ParticleOptions)ParticleRegistry.CLEANSE_PARTICLE.get();
                }
            }
            Vec3 point = center.m_82549_(offset);
            Particle particle = Minecraft.m_91087_().f_91061_.m_107370_(options, point.f_82479_, point.f_82480_, point.f_82481_, velocity.f_82479_, velocity.f_82480_, velocity.f_82481_);
            if (particle == null) continue;
            if (!(options instanceof BlastwaveParticleOptions)) {
                particle.m_107253_(red, green, blue);
                if (style == IronNativeSpellRules.Style.BLOOD_REAP) {
                    particle.m_107253_(0.85f, 0.28f, 0.32f);
                }
                particle.m_107257_(14 + index % 6);
            }
            entry.payload().particles.add(particle);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (world == null || minecraft.f_91073_ != world) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        float partial = minecraft.m_91104_() ? 0.0f : event.getPartialTick();
        int models = 0;
        int limit = minecraft.f_91066_.m_231929_().m_231551_() == ParticleStatus.MINIMAL ? 16 : 48;
        for (IronNativeSpellState.Entry entry : STATE.entries().stream().sorted(Comparator.comparingDouble(candidate -> IronNativeSpellAdapter.origin(candidate, partial).m_82557_(camera))).toList()) {
            double age;
            float alpha;
            Vec3 position;
            int count;
            if (!IronNativeSpellRules.model(entry.style()) || models + (count = IronNativeSpellModels.instances(entry.style())) > limit || (position = IronNativeSpellAdapter.origin(entry, partial)).m_82557_(camera) > 2304.0 || !event.getFrustum().m_113029_(new AABB(position, position.m_82520_(0.0, 4.0, 0.0)).m_82400_(4.0)) || (alpha = SpellVisualRules.opacity(age = (double)((float)(world.m_46467_() - entry.born()) + partial), IronNativeSpellRules.lifetime(entry.style())) * 0.8f) <= 0.0f) continue;
            PoseStack poses = event.getPoseStack();
            RenderType type = RenderType.m_234338_((ResourceLocation)IronNativeSpellTextures.texture(entry.style()));
            poses.m_85836_();
            try {
                poses.m_85837_(position.f_82479_ - camera.f_82479_, position.f_82480_ - camera.f_82480_, position.f_82481_ - camera.f_82481_);
                Entity owner = world.m_6815_(((Burst)entry.payload()).packet.ownerId());
                float radiusScale = (float)SpellVisuals.radiusScale(owner, entry.spell().id);
                poses.m_85841_(radiusScale, radiusScale, radiusScale);
                if (entry.style() == IronNativeSpellRules.Style.WINGS && owner instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity)owner;
                    poses.m_252781_(Axis.f_252436_.m_252977_(-Mth.m_14189_((float)partial, (float)living.f_20884_, (float)living.f_20883_)));
                }
                IronNativeSpellModels.render(entry.style(), age, entry.spell().color, alpha, poses, BUFFERS.m_6299_(type));
                models += count;
            }
            finally {
                poses.m_85849_();
                BUFFERS.m_109912_(type);
            }
        }
    }

    public static void clear() {
        STATE.entries().forEach(entry -> ((Burst)entry.payload()).clear());
        STATE.clear();
        world = null;
        particleTick = Long.MIN_VALUE;
    }

    static {
        particleTick = Long.MIN_VALUE;
    }

    private static final class Burst {
        private final SpellVisualPacket packet;
        private final List<Particle> particles = new ArrayList<Particle>();
        private int emitted = -1;

        private Burst(SpellVisualPacket packet) {
            this.packet = packet;
        }

        private void clear() {
            this.particles.forEach(Particle::m_107274_);
            this.particles.clear();
        }
    }
}

