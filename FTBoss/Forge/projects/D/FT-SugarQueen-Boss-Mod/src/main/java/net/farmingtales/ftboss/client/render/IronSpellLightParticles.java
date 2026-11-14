package net.farmingtales.ftboss.client.render;

import io.redspace.ironsspellbooks.particle.TraceParticleOptions;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.IronSpellLightRules;
import net.farmingtales.ftboss.util.IronSpellLightRules.Accent;
import net.farmingtales.ftboss.util.IronSpellLightRules.Beam;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;
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

    private IronSpellLightParticles() {}

    public static boolean emit(SpellVisualPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !packet.valid() || !level.dimension().location().equals(packet.dimension())
                || minecraft.options.particles().get() == ParticleStatus.MINIMAL || minecraft.isPaused()) return false;
        if (world != level) { world = level; budget = new IronSpellLightRules.Budget(); }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        Vec3 segment = packet.end().subtract(packet.start());
        double progress = segment.lengthSqr() < 0.001 ? 0 : Math.max(0, Math.min(1, camera.subtract(packet.start()).dot(segment) / segment.lengthSqr()));
        if (camera.distanceToSqr(packet.start().add(segment.scale(progress))) > SpellVisualRules.VIEW_RANGE * SpellVisualRules.VIEW_RANGE) return false;
        Entity owner = level.getEntity(packet.ownerId());
        if (owner == null || !owner.getUUID().equals(packet.owner())) owner = minecraft.player;
        if (owner == null) return false;
        boolean reduced = minecraft.options.particles().get() == ParticleStatus.DECREASED;
        if (packet.phase() == Phase.BEAM) {
            Beam mode = IronSpellLightRules.beam(packet.spell());
            if (mode == Beam.NONE) return false;
            Vec3 end = clip(level, owner, packet.start(), packet.end());
            Vec3 start = packet.start();
            if (packet.spell() == Spell.SANGUINE_SUTURE) {
                Vec3 originalStart = start;
                start = end;
                end = originalStart;
            }
            return line(level, mode, packet.spell().color, start, end, reduced);
        }
        Accent accent = IronSpellLightRules.accent(packet.spell(), packet.phase());
        if (accent == Accent.NONE) return false;
        Vec3 center = packet.start().add(0, accent == Accent.REBOOT_ZAP ? 1.1 : 0.7, 0);
        int count = reduced ? 3 : 6;
        for (int index = 0; index < count; index++) {
            double angle = index * Math.PI * 2 / count;
            double radius = accent == Accent.REBOOT_ZAP ? 1.3 : packet.spell().radius;
            radius *= net.farmingtales.ftboss.skill.SpellVisuals.radiusScale(owner, packet.spell().id);
            Vec3 end = center.add(Math.cos(angle) * radius, accent == Accent.HOLY_RAYS ? 1.2 : 0, Math.sin(angle) * radius);
            end = clip(level, owner, center, end);
            line(level, accent == Accent.HOLY_RAYS ? Beam.TRACE : Beam.ZAP, packet.spell().color, center, end, reduced);
        }
        return false;
    }

    public static void clear() { world = null; budget = new IronSpellLightRules.Budget(); }

    private static Vec3 clip(ClientLevel level, Entity owner, Vec3 start, Vec3 end) {
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner)).getLocation();
    }

    private static boolean line(ClientLevel level, Beam mode, int color, Vec3 start, Vec3 end, boolean reduced) {
        Vec3 segment = end.subtract(start);
        double distance = segment.length();
        int samples = IronSpellLightRules.samples(distance, reduced);
        if (samples == 0) return false;
        if ((mode == Beam.TRACE || mode == Beam.FROST_TRACE || mode == Beam.SIPHON)
                && (!IronSpellLightRules.traceSafe(end.x) || !IronSpellLightRules.traceSafe(end.y) || !IronSpellLightRules.traceSafe(end.z))) return false;
        int cost = IronSpellLightRules.cost(mode, distance, reduced);
        if (!budget.reserve(level.getGameTime(), cost, reduced ? 128 : 256)) return false;
        Vector3f tint = new Vector3f((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F);
        if (mode == Beam.ZAP) {
            /* v156: 这里原本还有一条 `mode == Beam.SOUL_RAY` 的分支，会 new
               irons_spellbooks 的 SoulfireRayParticleOptions。那颗粒子的 drawQuad 自己有 bug
               （2026-09-18 22:59 客户端崩在 SoulfireRayParticle.java:111 的 endVertex，
               报 "Not filled all elements of the vertex"），而触发它的是我们。
               SOUL_RAY 已从 beam() 映射里摘掉，这里也不再生成任何"铁魔法自带射线"粒子。 */
            Particle particle = create(new ZapParticleOption(end), start, Vec3.ZERO);
            if (particle == null) return false;
            particle.setColor(tint.x, tint.y, tint.z);
            particle.setLifetime(12);
            particle.setBoundingBox(new AABB(start, end).inflate(1));
            return true;
        }
        TraceParticleOptions options = new TraceParticleOptions(end.toVector3f(), tint);
        int emitted = 0;
        for (int index = 0; index < samples; index++) {
            Vec3 point = start.lerp(end, index / (double) samples);
            double remaining = point.distanceTo(end);
            Vec3 velocity = segment.normalize().scale(Math.min(0.18, remaining / 10));
            Particle particle = create(options, point, velocity);
            if (particle == null) continue;
            particle.scale(mode == Beam.FROST_TRACE ? 0.55F : 0.65F);
            particle.setLifetime(8);
            emitted++;
            if (mode == Beam.SIPHON) {
                Particle blood = create(ParticleRegistry.SIPHON_PARTICLE.get(), point, velocity.scale(0.5));
                if (blood != null) { blood.setLifetime(8); blood.scale(0.7F); }
            }
        }
        return emitted > 0;
    }

    private static Particle create(ParticleOptions options, Vec3 position, Vec3 velocity) {
        return Minecraft.getInstance().particleEngine.createParticle(options,
                position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
    }
}
