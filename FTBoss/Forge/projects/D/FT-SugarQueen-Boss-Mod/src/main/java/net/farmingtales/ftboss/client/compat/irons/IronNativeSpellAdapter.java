package net.farmingtales.ftboss.client.compat.irons;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.IronNativeSpellRules;
import net.farmingtales.ftboss.util.IronNativeSpellRules.Style;
import net.farmingtales.ftboss.util.IronNativeSpellState;
import net.farmingtales.ftboss.util.IronNativeSpellState.Entry;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

public final class IronNativeSpellAdapter {
    private static final IronNativeSpellState<Burst> STATE = new IronNativeSpellState<>();
    private static final MultiBufferSource.BufferSource BUFFERS = MultiBufferSource.immediate(new BufferBuilder(131072));
    private static ClientLevel world;
    private static long particleTick = Long.MIN_VALUE;

    private IronNativeSpellAdapter() {}

    public static boolean accept(SpellVisualPacket packet) {
        Style style = IronNativeSpellRules.style(packet.spell(), packet.phase());
        ClientLevel level = Minecraft.getInstance().level;
        if (style == null || level == null || !packet.valid() || !level.dimension().location().equals(packet.dimension())) return false;
        advance(level);
        for (Entry<Burst> entry : STATE.entries()) {
            if (entry.owner().equals(packet.owner()) && entry.spell() == packet.spell()) entry.payload().clear();
        }
        if (STATE.entries().size() >= IronNativeSpellRules.MAX_ACTIVE) STATE.entries().get(0).payload().clear();
        STATE.add(packet.owner(), packet.spell(), style, new Burst(packet), level.getGameTime());
        return true;
    }

    private static void advance(ClientLevel level) {
        if (world != level || level == null || level.getGameTime() < particleTick) clear();
        world = level;
        if (level == null) return;
        long now = level.getGameTime();
        for (Entry<Burst> entry : STATE.entries()) {
            if (now - entry.born() >= IronNativeSpellRules.lifetime(entry.style()) || !alive(entry.payload())) entry.payload().clear();
        }
        STATE.advance(level, now, IronNativeSpellAdapter::alive);
    }

    private static boolean alive(Burst burst) {
        Entity owner = world.getEntity(burst.packet.ownerId());
        if (owner == null) return !IronNativeSpellRules.follows(IronNativeSpellRules.style(burst.packet.spell(), burst.packet.phase()));
        return owner.getUUID().equals(burst.packet.owner()) && !owner.isRemoved() && !owner.isSpectator()
                && (!(owner instanceof LivingEntity living) || living.isAlive());
    }

    private static Vec3 origin(Entry<Burst> entry, float partial) {
        Entity owner = world.getEntity(entry.payload().packet.ownerId());
        return IronNativeSpellRules.follows(entry.style()) && owner != null && owner.getUUID().equals(entry.owner())
                ? owner.getPosition(partial) : entry.payload().packet.start();
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        advance(minecraft.level);
        if (world == null || minecraft.isPaused() || particleTick == world.getGameTime()) return;
        particleTick = world.getGameTime();
        int quality = minecraft.options.particles().get() == ParticleStatus.MINIMAL ? 2
                : minecraft.options.particles().get() == ParticleStatus.DECREASED ? 1 : 0;
        int budget = IronNativeSpellRules.particleBudget(quality);
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        List<Entry<Burst>> entries = STATE.entries().stream()
                .sorted(Comparator.comparingDouble(entry -> origin(entry, 1).distanceToSqr(camera))).toList();
        for (Entry<Burst> entry : entries) {
            Burst burst = entry.payload();
            int wave = IronNativeSpellRules.wave(particleTick - entry.born(), entry.style());
            if (wave < 0 || wave <= burst.emitted) continue;
            burst.emitted = wave;
            Vec3 center = origin(entry, 1);
            if (center.distanceToSqr(camera) > IronNativeSpellRules.VIEW_RANGE * IronNativeSpellRules.VIEW_RANGE) continue;
            int count = Math.min(budget, IronNativeSpellRules.particlesPerWave(quality));
            if (count == 0) continue;
            emit(entry, center, count, wave);
            budget -= count;
        }
    }

    private static void emit(Entry<Burst> entry, Vec3 center, int count, int wave) {
        Style style = entry.style();
        int color = entry.spell().color;
        float red = (color >> 16 & 255) / 255F;
        float green = (color >> 8 & 255) / 255F;
        float blue = (color & 255) / 255F;
        float radiusScale = (float) net.farmingtales.ftboss.skill.SpellVisuals.radiusScale(
                world.getEntity(entry.payload().packet.ownerId()), entry.spell().id);
        double radius = entry.spell().radius * 0.8 * radiusScale;
        for (int index = 0; index < count; index++) {
            double angle = index * 2.399963 + wave * 0.5;
            double spread = radius * Math.sqrt((index + 0.5) / count);
            Vec3 offset = new Vec3(Math.cos(angle) * spread, 0.3 + index % 3 * 0.15, Math.sin(angle) * spread);
            Vec3 velocity = new Vec3(0, 0.035, 0);
            ParticleOptions options;
            switch (style) {
                case CASK_BLAST, SHELL_BLAST -> {
                    if (index == 0) {
                        options = new BlastwaveParticleOptions(new Vector3f(red, green, blue),
                                (style == Style.CASK_BLAST ? 3.4F : 2.8F) * radiusScale);
                        offset = new Vec3(0, 0.5, 0);
                        velocity = Vec3.ZERO;
                    } else {
                        options = index % 3 == 0 ? ParticleRegistry.FIERY_SMOKE_PARTICLE.get() : ParticleRegistry.EMBER_PARTICLE.get();
                        offset = offset.scale(0.35);
                        velocity = new Vec3(Math.cos(angle) * 0.17, 0.06 + index % 4 * 0.025, Math.sin(angle) * 0.17);
                    }
                }
                case BLOOD_REAP -> {
                    options = ParticleRegistry.BLOOD_PARTICLE.get();
                    Vec3 direction = entry.payload().packet.end().subtract(entry.payload().packet.start());
                    double heading = Math.atan2(direction.x, direction.z);
                    double sweep = heading + (index / (double) count - 0.5) * Math.PI * 0.8;
                    offset = new Vec3(Math.sin(sweep) * radius, 0.65 + wave * 0.18, Math.cos(sweep) * radius);
                    velocity = new Vec3(-Math.sin(sweep) * 0.07, 0.035, -Math.cos(sweep) * 0.07);
                }
                case SPORES -> {
                    options = index % 3 == 0 ? ParticleRegistry.ACID_PARTICLE.get() : ParticleRegistry.ACID_BUBBLE_PARTICLE.get();
                    offset = offset.add(0, index % 4 * 0.22, 0);
                }
                case MEDICINAL_RAIN -> {
                    options = ParticleRegistry.ACID_BUBBLE_PARTICLE.get();
                    offset = offset.add(0, 2.5, 0);
                    velocity = new Vec3(0, -0.14, 0);
                }
                case SNOW, ICE_CROWN -> {
                    options = index % 4 == 0 ? ParticleRegistry.SNOW_DUST.get() : ParticleRegistry.SNOWFLAKE_PARTICLE.get();
                    offset = offset.add(0, style == Style.SNOW ? 2.4 : 0.8, 0);
                    velocity = new Vec3(0.025, -0.045, -0.015);
                }
                case HEARTH -> options = index % 4 == 0 ? ParticleRegistry.EMBER_PARTICLE.get() : ParticleRegistry.CLEANSE_PARTICLE.get();
                default -> options = ParticleRegistry.CLEANSE_PARTICLE.get();
            }
            Vec3 point = center.add(offset);
            Particle particle = Minecraft.getInstance().particleEngine.createParticle(options, point.x, point.y, point.z,
                    velocity.x, velocity.y, velocity.z);
            if (particle != null) {
                if (!(options instanceof BlastwaveParticleOptions)) {
                    particle.setColor(red, green, blue);
                    if (style == Style.BLOOD_REAP) particle.setColor(0.85F, 0.28F, 0.32F);
                    particle.setLifetime(14 + index % 6);
                }
                entry.payload().particles.add(particle);
            }
        }
    }

    public static void render(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (world == null || minecraft.level != world) return;
        Vec3 camera = event.getCamera().getPosition();
        float partial = minecraft.isPaused() ? 0 : event.getPartialTick();
        int models = 0;
        int limit = minecraft.options.particles().get() == ParticleStatus.MINIMAL ? 16 : IronNativeSpellRules.MAX_MODELS_PER_FRAME;
        for (Entry<Burst> entry : STATE.entries().stream()
                .sorted(Comparator.comparingDouble(candidate -> origin(candidate, partial).distanceToSqr(camera))).toList()) {
            if (!IronNativeSpellRules.model(entry.style())) continue;
            int count = IronNativeSpellModels.instances(entry.style());
            if (models + count > limit) continue;
            Vec3 position = origin(entry, partial);
            if (position.distanceToSqr(camera) > IronNativeSpellRules.VIEW_RANGE * IronNativeSpellRules.VIEW_RANGE
                    || !event.getFrustum().isVisible(new AABB(position, position.add(0, 4, 0)).inflate(4))) continue;
            double age = world.getGameTime() - entry.born() + partial;
            float alpha = SpellVisualRules.opacity(age, IronNativeSpellRules.lifetime(entry.style())) * 0.8F;
            if (alpha <= 0) continue;
            PoseStack poses = event.getPoseStack();
            RenderType type = RenderType.entityTranslucentEmissive(IronNativeSpellTextures.texture(entry.style()));
            poses.pushPose();
            try {
                poses.translate(position.x - camera.x, position.y - camera.y, position.z - camera.z);
                Entity owner = world.getEntity(entry.payload().packet.ownerId());
                float radiusScale = (float) net.farmingtales.ftboss.skill.SpellVisuals.radiusScale(owner, entry.spell().id);
                poses.scale(radiusScale, radiusScale, radiusScale);
                if (entry.style() == Style.WINGS && owner instanceof LivingEntity living) {
                    poses.mulPose(Axis.YP.rotationDegrees(-Mth.rotLerp(partial, living.yBodyRotO, living.yBodyRot)));
                }
                IronNativeSpellModels.render(entry.style(), age, entry.spell().color, alpha, poses, BUFFERS.getBuffer(type));
                models += count;
            } finally {
                poses.popPose();
                BUFFERS.endBatch(type);
            }
        }
    }

    public static void clear() {
        STATE.entries().forEach(entry -> entry.payload().clear());
        STATE.clear();
        world = null;
        particleTick = Long.MIN_VALUE;
    }

    private static final class Burst {
        private final SpellVisualPacket packet;
        private final List<Particle> particles = new ArrayList<>();
        private int emitted = -1;

        private Burst(SpellVisualPacket packet) { this.packet = packet; }
        private void clear() { particles.forEach(Particle::remove); particles.clear(); }
    }
}
