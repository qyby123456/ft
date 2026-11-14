package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.CombatVisualEntities;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.EasterCombatMath;
import net.farmingtales.ftboss.util.GongdiCombatRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class EasterSkillVisual {
    private static final double VIEW_DISTANCE_SQUARED = 96.0 * 96.0;
    private static final int MAX_VISIBLE_CUES = 24;
    private static final int PARTICLE_BUDGET = 32;
    private static final int[] COLORS = {
            0xFFCA62, 0xC3A0FF, 0xFFD894, 0xF05765, 0x65D6FF, 0x73EBD1,
            0x82EAC0, 0xAA79EF, 0xA4DC54, 0xBAF5A0, 0xB1E9FF, 0xEC79B3
    };
    private static final DustParticleOptions[] DUST = new DustParticleOptions[COLORS.length];

    static {
        for (int index = 0; index < COLORS.length; index++) {
            int color = COLORS[index];
            DUST[index] = new DustParticleOptions(new Vector3f((color >> 16 & 255) / 255.0f,
                    (color >> 8 & 255) / 255.0f, (color & 255) / 255.0f), 0.85f);
        }
    }

    private EasterSkillVisual() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || client.level == null || client.isPaused()
                || client.getCameraEntity() == null || client.options.particles().get() == ParticleStatus.MINIMAL) return;
        int budget = client.options.particles().get() == ParticleStatus.DECREASED ? PARTICLE_BUDGET / 2 : PARTICLE_BUDGET;
        for (EasterBossEntity boss : CombatVisualEntities.get(client.level).hiddenBosses()) {
            if (boss.isRemoved() || !boss.isAlive() || boss.isNoAi()
                    || boss.isMini() || boss.getVariant() == EasterBossEntity.DORA_VARIANT
                    || boss.distanceToSqr(client.getCameraEntity()) > VIEW_DISTANCE_SQUARED) continue;
            if (boss.getCombatAnimationWeight(0.0f) <= 0.0f || (boss.tickCount + boss.getId()) % 2 != 0) continue;
            if (!boss.isCastingLike() && boss.getCombatAnimationAge(0.0f) > 5.0f) continue;
            double angle = boss.tickCount * 0.24 + boss.getId();
            double radius = boss.isCastingLike() ? 0.55 : 0.8;
            client.level.addParticle(DUST[boss.getVariant()], boss.getX() + Math.cos(angle) * radius,
                    boss.getY() + 1.2 + Math.sin(angle * 0.7) * 0.25,
                    boss.getZ() + Math.sin(angle) * radius, 0.0, 0.018, 0.0);
            if (--budget <= 0) break;
            if (boss.getVariant() == EasterBossEntity.XUELI_VARIANT && boss.isCastingLike()) {
                client.level.addParticle(ParticleTypes.SNOWFLAKE, boss.getX(), boss.getY() + 1.8,
                        boss.getZ(), 0.0, -0.025, 0.0);
                if (--budget <= 0) break;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        var candidates = CombatVisualEntities.get(client.level).hiddenBosses();
        if (candidates.isEmpty()) return;
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = client.renderBuffers().bufferSource();
        int drawn = 0;
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            Matrix4f matrix = pose.last().pose();
            for (EasterBossEntity boss : candidates) {
                if (boss.isRemoved() || !boss.isAlive() || boss.isNoAi() || boss.isMini()) continue;
                CompoundTag cue = boss.getSkillCue();
                if (cue.isEmpty() || cue.contains("qizai")) continue;
                float age = client.level.getGameTime() - cue.getLong("start") + event.getPartialTick();
                int duration = cue.getInt("duration");
                boolean released = cue.getBoolean("released");
                if (age < 0.0f || duration <= 0 || (released && age >= duration) || age > duration + 40) continue;
                Vec3 origin = cue.getBoolean("follow") ? boss.getPosition(event.getPartialTick())
                        : new Vec3(cue.getDouble("x"), cue.getDouble("y"), cue.getDouble("z"));
                if (origin.distanceToSqr(camera) > VIEW_DISTANCE_SQUARED) continue;
                double radius = Mth.clamp(cue.getDouble("radius"), 0.1, 16.0);
                Vec3 end = cue.getBoolean("beam")
                        ? new Vec3(cue.getDouble("endX"), cue.getDouble("endY"), cue.getDouble("endZ")) : origin;
                AABB bounds = new AABB(origin, end).inflate(radius + 0.5);
                if (event.getFrustum() != null && !event.getFrustum().isVisible(bounds)) continue;
                float progress = EasterCombatMath.clamp(age / duration);
                float opacity = released ? 1.0f - EasterCombatMath.smooth(progress) : 0.55f + 0.25f * progress;
                int color = COLORS[boss.getVariant()];
                VertexConsumer vertices = buffers.getBuffer(SkillRenderType.TYPE);
                if (cue.getBoolean("beam")) {
                    if (cue.getBoolean("doraSniper")) {
                        renderDoraSniperBeam(vertices, matrix, origin, end, radius, cue.getBoolean("firing"),
                                cue.getBoolean("doraLocked"), opacity, age);
                    } else {
                        renderBeam(vertices, matrix, origin, end, radius, cue.getBoolean("firing"), color,
                                0xFFAA76, opacity, age);
                    }
                } else if (boss.getVariant() == 3 && cue.contains("gongdiSkill")) {
                    renderGongdiCue(vertices, matrix, origin.add(0.0, 0.07, 0.0), cue, radius, progress, released, opacity);
                } else {
                    renderCircle(vertices, matrix, origin.add(0.0, 0.07, 0.0), radius,
                            progress, released, color, opacity, origin.distanceToSqr(camera) > 32.0 * 32.0 ? 32 : 64);
                }
                if (++drawn >= MAX_VISIBLE_CUES) break;
            }
        } finally {
            if (drawn > 0) buffers.endBatch(SkillRenderType.TYPE);
            pose.popPose();
        }
    }

    private static void renderGongdiCue(VertexConsumer vertices, Matrix4f matrix, Vec3 center, CompoundTag cue,
                                      double radius, float progress, boolean released, float opacity) {
        String skill = cue.getString("gongdiSkill");
        int color = 0xF05765;
        if (skill.equals("decimate")) {
            double innerRadius = cue.getDouble("innerRadius");
            ring(vertices, matrix, center, innerRadius, 0.055, 1.0f, 0x83CFC0, opacity * 0.8f, 64);
            ring(vertices, matrix, center, radius, 0.10, 1.0f, color, opacity, 64);
            ring(vertices, matrix, center.add(0.0, 0.01, 0.0), radius - 0.14, 0.10, progress, color, opacity, 64);
            double band = released ? 0.65 : 0.18;
            ring(vertices, matrix, center.add(0.0, released ? 0.9 : 0.02, 0.0),
                    innerRadius + (radius - innerRadius) * (released ? progress : 0.92), band,
                    1.0f, color, opacity * (released ? 0.85f : 0.2f), 64);
            return;
        }
        if (skill.equals("guillotine")) {
            renderCircle(vertices, matrix, center, radius, progress, released, color, opacity, 48);
            ring(vertices, matrix, center.add(0.0, 0.02, 0.0), radius - 0.20, 0.08,
                    1.0f, 0xFFD7BB, opacity * 0.6f, 48);
            return;
        }
        double halfAngle = skill.equals("apprehend") ? GongdiCombatRules.PULL_HALF_ANGLE : GongdiCombatRules.STRIKE_HALF_ANGLE;
        double heading = Math.atan2(cue.getDouble("directionZ"), cue.getDouble("directionX"));
        double inner = released ? radius * (1.0 - progress) : 0.25;
        for (int segment = 0; segment < 32; segment++) {
            double angle = heading - halfAngle + halfAngle * 2.0 * segment / 32;
            double nextAngle = heading - halfAngle + halfAngle * 2.0 * (segment + 1) / 32;
            Vec3 ray = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            Vec3 nextRay = new Vec3(Math.cos(nextAngle), 0.0, Math.sin(nextAngle));
            quad(vertices, matrix, center.add(ray.scale(inner)), center.add(ray.scale(radius)),
                    center.add(nextRay.scale(radius)), center.add(nextRay.scale(inner)), color, opacity * 0.10f);
            quad(vertices, matrix, center.add(ray.scale(radius - 0.08)), center.add(ray.scale(radius)),
                    center.add(nextRay.scale(radius)), center.add(nextRay.scale(radius - 0.08)), color, opacity);
        }
        for (double boundary : new double[]{heading - halfAngle, heading + halfAngle}) {
            Vec3 ray = new Vec3(Math.cos(boundary), 0.0, Math.sin(boundary));
            Vec3 width = new Vec3(-ray.z, 0.0, ray.x).scale(0.035);
            ribbon(vertices, matrix, center, center.add(ray.scale(radius)), width, color, opacity);
        }
    }

    private static void renderCircle(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius,
                                     float progress, boolean released, int color, float opacity, int segments) {
        ring(vertices, matrix, center, radius, 0.075, 1.0f, color, opacity * (released ? 0.35f : 0.7f), segments);
        if (released) {
            double waveRadius = radius * (0.2 + 0.8 * EasterCombatMath.smooth(progress));
            ring(vertices, matrix, center.add(0.0, 0.04, 0.0), waveRadius, 0.24 * (1.0 - progress) + 0.04,
                    1.0f, color, opacity, segments);
        } else {
            ring(vertices, matrix, center.add(0.0, 0.01, 0.0), radius * 0.96, 0.14, progress, color, opacity, segments);
            ring(vertices, matrix, center.add(0.0, 0.02, 0.0), radius * (1.0 - 0.7 * progress),
                    0.04, 1.0f, color, opacity * 0.28f, segments);
        }
        for (int index = 0; index < 8; index++) {
            double angle = index * Math.PI / 4.0;
            Vec3 outward = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            Vec3 tangent = new Vec3(-Math.sin(angle), 0.0, Math.cos(angle)).scale(0.055);
            Vec3 inner = center.add(outward.scale(radius - 0.35));
            Vec3 outer = center.add(outward.scale(radius));
            quad(vertices, matrix, inner.subtract(tangent), outer.subtract(tangent),
                    outer.add(tangent), inner.add(tangent), color, opacity * 0.65f);
        }
    }

    private static void ring(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius, double width,
                             float completion, int color, float opacity, int segments) {
        double outer = Math.max(0.02, radius);
        double inner = Math.max(0.0, outer - width);
        for (int index = 0; index < segments && index < segments * completion; index++) {
            double start = index * Math.PI * 2.0 / segments;
            double end = Math.min(index + 1.0, segments * completion) * Math.PI * 2.0 / segments;
            quad(vertices, matrix, center.add(Math.cos(start) * inner, 0.0, Math.sin(start) * inner),
                    center.add(Math.cos(start) * outer, 0.0, Math.sin(start) * outer),
                    center.add(Math.cos(end) * outer, 0.0, Math.sin(end) * outer),
                    center.add(Math.cos(end) * inner, 0.0, Math.sin(end) * inner), color, opacity);
        }
    }

    private static void renderBeam(VertexConsumer vertices, Matrix4f matrix, Vec3 origin, Vec3 end,
                                   double radius, boolean firing, int color, int warningColor, float opacity, float age) {
        Vec3 direction = end.subtract(origin);
        if (direction.lengthSqr() < 0.001) return;
        Vec3 forward = direction.normalize();
        Vec3 side = forward.cross(Math.abs(forward.y) > 0.95 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0)).normalize();
        Vec3 up = forward.cross(side).normalize();
        double core = firing ? 0.14 + Math.sin(age * 1.2) * 0.025 : 0.035;
        int beamColor = firing ? color : warningColor;
        ribbon(vertices, matrix, origin, end, side.scale(core), beamColor, opacity);
        ribbon(vertices, matrix, origin, end, up.scale(core), beamColor, opacity);
        if (firing) {
            ribbon(vertices, matrix, origin, end, side.scale(core * 0.3), 0xEBFFF6, opacity);
            ribbon(vertices, matrix, origin, end, up.scale(core * 0.3), 0xEBFFF6, opacity);
        }
        int segments = 24;
        for (int index = 0; index < segments; index++) {
            double angle = index * Math.PI * 2.0 / segments;
            double nextAngle = (index + 1.0) * Math.PI * 2.0 / segments;
            Vec3 offset = side.scale(Math.cos(angle) * radius).add(up.scale(Math.sin(angle) * radius));
            Vec3 next = side.scale(Math.cos(nextAngle) * radius).add(up.scale(Math.sin(nextAngle) * radius));
            quad(vertices, matrix, origin.add(offset), end.add(offset), end.add(next), origin.add(next),
                    beamColor, opacity * (firing ? 0.035f : 0.012f));
            if (index % 6 == 0) {
                ribbon(vertices, matrix, origin.add(offset), end.add(offset), side.scale(0.025), beamColor, opacity * 0.4f);
            }
        }
    }

    private static void renderDoraSniperBeam(VertexConsumer vertices, Matrix4f matrix, Vec3 origin, Vec3 end,
                                               double radius, boolean firing, boolean locked, float opacity, float age) {
        Vec3 ray = end.subtract(origin);
        if (ray.lengthSqr() < 0.001) return;
        Vec3 forward = ray.normalize();
        Vec3 reference = Math.abs(forward.y) > 0.95 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 side = forward.cross(reference).normalize();
        Vec3 up = side.cross(forward).normalize();
        int warning = locked ? 0xFF5268 : 0xFFCB67;
        if (!firing) {
            double width = locked ? 0.046 : 0.030;
            int dashes = 18;
            for (int index = 0; index < dashes; index += 2) {
                double first = index / (double) dashes;
                double second = Math.min(1.0, (index + 1.25) / dashes);
                Vec3 from = origin.lerp(end, first);
                Vec3 to = origin.lerp(end, second);
                ribbon(vertices, matrix, from, to, side.scale(width), warning, opacity * 0.82f);
                ribbon(vertices, matrix, from, to, up.scale(width * 0.55), 0xFFF0C2, opacity * 0.58f);
            }
            double reticle = Math.max(0.18, radius * (locked ? 1.2 : 0.92));
            beamRing(vertices, matrix, end, side, up, reticle, 0.028, warning, opacity, 24, age * 0.18);
            beamRing(vertices, matrix, end, side, up, reticle * 0.62, 0.018, 0xFFF0C2,
                    opacity * 0.72f, 16, -age * 0.26);
            beamCross(vertices, matrix, end, side, up, reticle * 1.35, 0.018, warning, opacity * 0.85f);
            beamRing(vertices, matrix, origin.add(forward.scale(0.12)), side, up, 0.18, 0.024,
                    warning, opacity * 0.78f, 16, -age * 0.35);
            return;
        }

        double pulse = 0.15 + Math.sin(age * 1.35) * 0.025;
        ribbon(vertices, matrix, origin, end, side.scale(pulse), 0x39CFC5, opacity * 0.38f);
        ribbon(vertices, matrix, origin, end, up.scale(pulse), 0x39CFC5, opacity * 0.38f);
        ribbon(vertices, matrix, origin, end, side.scale(pulse * 0.42), 0x9FFFF0, opacity);
        ribbon(vertices, matrix, origin, end, up.scale(pulse * 0.42), 0x9FFFF0, opacity);
        ribbon(vertices, matrix, origin, end, side.scale(pulse * 0.12), 0xFFFFFF, opacity);
        int gates = 8;
        for (int index = 0; index < gates; index++) {
            double along = (index + 0.35 + age * 0.22) / gates;
            along -= Math.floor(along);
            beamRing(vertices, matrix, origin.lerp(end, along), side, up, pulse * 1.75, pulse * 0.16,
                    index % 2 == 0 ? 0xD9FFF7 : 0x67FFE0, opacity * 0.62f, 12, age * 0.28 + index);
        }
        beamRing(vertices, matrix, end, side, up, Math.max(0.30, radius * 1.45), 0.055,
                0xD9FFF7, opacity, 28, age * 0.42);
        beamCross(vertices, matrix, end, side, up, Math.max(0.42, radius * 1.8), 0.035,
                0xFFFFFF, opacity);
    }

    private static void beamRing(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 horizontal, Vec3 vertical,
                                 double radius, double width, int color, float opacity, int segments, double spin) {
        double inner = Math.max(0.0, radius - width);
        for (int index = 0; index < segments; index++) {
            double first = spin + index * Math.PI * 2.0 / segments;
            double second = spin + (index + 1.0) * Math.PI * 2.0 / segments;
            Vec3 firstAxis = horizontal.scale(Math.cos(first)).add(vertical.scale(Math.sin(first)));
            Vec3 secondAxis = horizontal.scale(Math.cos(second)).add(vertical.scale(Math.sin(second)));
            quad(vertices, matrix, center.add(firstAxis.scale(inner)), center.add(firstAxis.scale(radius)),
                    center.add(secondAxis.scale(radius)), center.add(secondAxis.scale(inner)), color, opacity);
        }
    }

    private static void beamCross(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 horizontal, Vec3 vertical,
                                  double radius, double width, int color, float opacity) {
        ribbon(vertices, matrix, center.subtract(horizontal.scale(radius)), center.add(horizontal.scale(radius)),
                vertical.scale(width), color, opacity);
        ribbon(vertices, matrix, center.subtract(vertical.scale(radius)), center.add(vertical.scale(radius)),
                horizontal.scale(width), color, opacity);
    }

    private static void ribbon(VertexConsumer vertices, Matrix4f matrix, Vec3 start, Vec3 end, Vec3 halfWidth,
                               int color, float opacity) {
        quad(vertices, matrix, start.subtract(halfWidth), end.subtract(halfWidth), end.add(halfWidth),
                start.add(halfWidth), color, opacity);
    }

    private static void quad(VertexConsumer vertices, Matrix4f matrix, Vec3 first, Vec3 second, Vec3 third, Vec3 fourth,
                             int color, float opacity) {
        vertex(vertices, matrix, first, color, opacity);
        vertex(vertices, matrix, second, color, opacity);
        vertex(vertices, matrix, third, color, opacity);
        vertex(vertices, matrix, fourth, color, opacity);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, Vec3 position, int color, float opacity) {
        vertices.vertex(matrix, (float) position.x, (float) position.y, (float) position.z)
                .color(color >> 16 & 255, color >> 8 & 255, color & 255, (int) (255 * EasterCombatMath.clamp(opacity))).endVertex();
    }

    private static final class SkillRenderType extends RenderType {
        private static final RenderType TYPE = create("ftboss_easter_skill", DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS, 4096, false, true, CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false));

        private SkillRenderType() {
            super("ftboss_easter_skill", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                    4096, false, true, () -> {}, () -> {});
        }
    }
}
