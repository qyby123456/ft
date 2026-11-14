/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.util.Mth
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import net.farmingtales.ftboss.client.CombatVisualEntities;
import net.farmingtales.ftboss.client.dora_native.DoraNativeVisual;
import net.farmingtales.ftboss.client.xuanlannative.NativeScene;
import net.farmingtales.ftboss.client.xueli_combat.XueliCombatRenderer;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.qingyilight.client.LightClient;
import net.farmingtales.ftboss.util.EasterCombatMath;
import net.farmingtales.ftboss.util.GongdiCombatRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
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

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class EasterSkillVisual {
    private static final double VIEW_DISTANCE_SQUARED = 9216.0;
    private static final int MAX_VISIBLE_CUES = 24;
    private static final int PARTICLE_BUDGET = 32;
    private static final int[] COLORS = new int[]{16763490, 12820735, 16767124, 15751013, 6674175, 7597009, 8579776, 11172335, 10804308, 12252576, 11659775, 15497651};
    private static final DustParticleOptions[] DUST = new DustParticleOptions[COLORS.length];

    private EasterSkillVisual() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft client = Minecraft.m_91087_();
        if (event.phase != TickEvent.Phase.END || client.f_91073_ == null || client.m_91104_() || client.m_91288_() == null || client.f_91066_.m_231929_().m_231551_() == ParticleStatus.MINIMAL) {
            return;
        }
        int budget = client.f_91066_.m_231929_().m_231551_() == ParticleStatus.DECREASED ? 16 : 32;
        for (EasterBossEntity boss : CombatVisualEntities.get(client.f_91073_).hiddenBosses()) {
            if (boss.m_213877_() || !boss.m_6084_() || boss.m_21525_() || boss.isMini() || boss.getVariant() == 2 || boss.m_20280_(client.m_91288_()) > 9216.0 || boss.getCombatAnimationWeight(0.0f) <= 0.0f || (boss.f_19797_ + boss.m_19879_()) % 2 != 0 || !boss.isCastingLike() && boss.getCombatAnimationAge(0.0f) > 5.0f) continue;
            double angle = (double)boss.f_19797_ * 0.24 + (double)boss.m_19879_();
            double radius = boss.isCastingLike() ? 0.55 : 0.8;
            client.f_91073_.m_7106_((ParticleOptions)DUST[boss.getVariant()], boss.m_20185_() + Math.cos(angle) * radius, boss.m_20186_() + 1.2 + Math.sin(angle * 0.7) * 0.25, boss.m_20189_() + Math.sin(angle) * radius, 0.0, 0.018, 0.0);
            if (--budget <= 0) break;
            if (boss.getVariant() != 10 || !boss.isCastingLike()) continue;
            client.f_91073_.m_7106_((ParticleOptions)ParticleTypes.f_175821_, boss.m_20185_(), boss.m_20186_() + 1.8, boss.m_20189_(), 0.0, -0.025, 0.0);
            if (--budget > 0) continue;
            break;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - void declaration
     */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft client = Minecraft.m_91087_();
        if (client.f_91073_ == null) {
            return;
        }
        List<EasterBossEntity> candidates = CombatVisualEntities.get(client.f_91073_).hiddenBosses();
        if (candidates.isEmpty()) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = client.m_91269_().m_110104_();
        int drawn = 0;
        pose.m_85836_();
        try {
            pose.m_85837_(-camera.f_82479_, -camera.f_82480_, -camera.f_82481_);
            Matrix4f matrix = pose.m_85850_().m_252922_();
            for (EasterBossEntity easterBossEntity : candidates) {
                Vec3 origin;
                CompoundTag cue;
                void boss;
                if (NativeScene.ownsCue(easterBossEntity) || boss.m_213877_() || !boss.m_6084_() || boss.m_21525_() || boss.isMini() || (cue = XueliCombatRenderer.filterLegacyCue(LightClient.filterLegacyCue(DoraNativeVisual.cueForLegacy((EasterBossEntity)boss), (EasterBossEntity)boss), (EasterBossEntity)boss)).m_128456_() || cue.m_128441_("qizai")) continue;
                float age = (float)(client.f_91073_.m_46467_() - cue.m_128454_("start")) + event.getPartialTick();
                int duration = cue.m_128451_("duration");
                boolean released = cue.m_128471_("released");
                if (age < 0.0f || duration <= 0 || released && age >= (float)duration || age > (float)(duration + 40)) continue;
                Vec3 vec3 = origin = cue.m_128471_("follow") ? boss.m_20318_(event.getPartialTick()) : new Vec3(cue.m_128459_("x"), cue.m_128459_("y"), cue.m_128459_("z"));
                if (origin.m_82557_(camera) > 9216.0) continue;
                double radius = Mth.m_14008_((double)cue.m_128459_("radius"), (double)0.1, (double)16.0);
                Vec3 end = cue.m_128471_("beam") ? new Vec3(cue.m_128459_("endX"), cue.m_128459_("endY"), cue.m_128459_("endZ")) : origin;
                AABB bounds = new AABB(origin, end).m_82400_(radius + 0.5);
                if (event.getFrustum() != null && !event.getFrustum().m_113029_(bounds)) continue;
                float progress = EasterCombatMath.clamp(age / (float)duration);
                float opacity = released ? 1.0f - EasterCombatMath.smooth(progress) : 0.55f + 0.25f * progress;
                int color = COLORS[boss.getVariant()];
                VertexConsumer vertices = buffers.m_6299_(SkillRenderType.TYPE);
                if (cue.m_128471_("beam")) {
                    if (cue.m_128471_("doraSniper")) {
                        EasterSkillVisual.renderDoraSniperBeam(vertices, matrix, origin, end, radius, cue.m_128471_("firing"), cue.m_128471_("doraLocked"), opacity, age);
                    } else {
                        EasterSkillVisual.renderBeam(vertices, matrix, origin, end, radius, cue.m_128471_("firing"), color, 16755318, opacity, age);
                    }
                } else if (boss.getVariant() == 3 && cue.m_128441_("gongdiSkill")) {
                    EasterSkillVisual.renderGongdiCue(vertices, matrix, origin.m_82520_(0.0, 0.07, 0.0), cue, radius, progress, released, opacity);
                } else {
                    EasterSkillVisual.renderCircle(vertices, matrix, origin.m_82520_(0.0, 0.07, 0.0), radius, progress, released, color, opacity, origin.m_82557_(camera) > 1024.0 ? 32 : 64);
                }
                if (++drawn < 24) continue;
                break;
            }
        }
        finally {
            if (drawn > 0) {
                buffers.m_109912_(SkillRenderType.TYPE);
            }
            pose.m_85849_();
        }
    }

    private static void renderGongdiCue(VertexConsumer vertices, Matrix4f matrix, Vec3 center, CompoundTag cue, double radius, float progress, boolean released, float opacity) {
        Vec3 ray;
        String skill = cue.m_128461_("gongdiSkill");
        int color = 15751013;
        if (skill.equals("decimate")) {
            double innerRadius = cue.m_128459_("innerRadius");
            EasterSkillVisual.ring(vertices, matrix, center, innerRadius, 0.055, 1.0f, 8638400, opacity * 0.8f, 64);
            EasterSkillVisual.ring(vertices, matrix, center, radius, 0.1, 1.0f, color, opacity, 64);
            EasterSkillVisual.ring(vertices, matrix, center.m_82520_(0.0, 0.01, 0.0), radius - 0.14, 0.1, progress, color, opacity, 64);
            double band = released ? 0.65 : 0.18;
            EasterSkillVisual.ring(vertices, matrix, center.m_82520_(0.0, released ? 0.9 : 0.02, 0.0), innerRadius + (radius - innerRadius) * (released ? (double)progress : 0.92), band, 1.0f, color, opacity * (released ? 0.85f : 0.2f), 64);
            return;
        }
        if (skill.equals("guillotine")) {
            EasterSkillVisual.renderCircle(vertices, matrix, center, radius, progress, released, color, opacity, 48);
            EasterSkillVisual.ring(vertices, matrix, center.m_82520_(0.0, 0.02, 0.0), radius - 0.2, 0.08, 1.0f, 16766907, opacity * 0.6f, 48);
            return;
        }
        double halfAngle = skill.equals("apprehend") ? GongdiCombatRules.PULL_HALF_ANGLE : GongdiCombatRules.STRIKE_HALF_ANGLE;
        double heading = Math.atan2(cue.m_128459_("directionZ"), cue.m_128459_("directionX"));
        double inner = released ? radius * (1.0 - (double)progress) : 0.25;
        for (int segment = 0; segment < 32; ++segment) {
            double angle = heading - halfAngle + halfAngle * 2.0 * (double)segment / 32.0;
            double nextAngle = heading - halfAngle + halfAngle * 2.0 * (double)(segment + 1) / 32.0;
            ray = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            Vec3 nextRay = new Vec3(Math.cos(nextAngle), 0.0, Math.sin(nextAngle));
            EasterSkillVisual.quad(vertices, matrix, center.m_82549_(ray.m_82490_(inner)), center.m_82549_(ray.m_82490_(radius)), center.m_82549_(nextRay.m_82490_(radius)), center.m_82549_(nextRay.m_82490_(inner)), color, opacity * 0.1f);
            EasterSkillVisual.quad(vertices, matrix, center.m_82549_(ray.m_82490_(radius - 0.08)), center.m_82549_(ray.m_82490_(radius)), center.m_82549_(nextRay.m_82490_(radius)), center.m_82549_(nextRay.m_82490_(radius - 0.08)), color, opacity);
        }
        for (double boundary : new double[]{heading - halfAngle, heading + halfAngle}) {
            ray = new Vec3(Math.cos(boundary), 0.0, Math.sin(boundary));
            Vec3 width = new Vec3(-ray.f_82481_, 0.0, ray.f_82479_).m_82490_(0.035);
            EasterSkillVisual.ribbon(vertices, matrix, center, center.m_82549_(ray.m_82490_(radius)), width, color, opacity);
        }
    }

    private static void renderCircle(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius, float progress, boolean released, int color, float opacity, int segments) {
        EasterSkillVisual.ring(vertices, matrix, center, radius, 0.075, 1.0f, color, opacity * (released ? 0.35f : 0.7f), segments);
        if (released) {
            double waveRadius = radius * (0.2 + 0.8 * (double)EasterCombatMath.smooth(progress));
            EasterSkillVisual.ring(vertices, matrix, center.m_82520_(0.0, 0.04, 0.0), waveRadius, 0.24 * (1.0 - (double)progress) + 0.04, 1.0f, color, opacity, segments);
        } else {
            EasterSkillVisual.ring(vertices, matrix, center.m_82520_(0.0, 0.01, 0.0), radius * 0.96, 0.14, progress, color, opacity, segments);
            EasterSkillVisual.ring(vertices, matrix, center.m_82520_(0.0, 0.02, 0.0), radius * (1.0 - 0.7 * (double)progress), 0.04, 1.0f, color, opacity * 0.28f, segments);
        }
        for (int index = 0; index < 8; ++index) {
            double angle = (double)index * Math.PI / 4.0;
            Vec3 outward = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            Vec3 tangent = new Vec3(-Math.sin(angle), 0.0, Math.cos(angle)).m_82490_(0.055);
            Vec3 inner = center.m_82549_(outward.m_82490_(radius - 0.35));
            Vec3 outer = center.m_82549_(outward.m_82490_(radius));
            EasterSkillVisual.quad(vertices, matrix, inner.m_82546_(tangent), outer.m_82546_(tangent), outer.m_82549_(tangent), inner.m_82549_(tangent), color, opacity * 0.65f);
        }
    }

    private static void ring(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius, double width, float completion, int color, float opacity, int segments) {
        double outer = Math.max(0.02, radius);
        double inner = Math.max(0.0, outer - width);
        for (int index = 0; index < segments && (float)index < (float)segments * completion; ++index) {
            double start = (double)index * Math.PI * 2.0 / (double)segments;
            double end = Math.min((double)index + 1.0, (double)((float)segments * completion)) * Math.PI * 2.0 / (double)segments;
            EasterSkillVisual.quad(vertices, matrix, center.m_82520_(Math.cos(start) * inner, 0.0, Math.sin(start) * inner), center.m_82520_(Math.cos(start) * outer, 0.0, Math.sin(start) * outer), center.m_82520_(Math.cos(end) * outer, 0.0, Math.sin(end) * outer), center.m_82520_(Math.cos(end) * inner, 0.0, Math.sin(end) * inner), color, opacity);
        }
    }

    private static void renderBeam(VertexConsumer vertices, Matrix4f matrix, Vec3 origin, Vec3 end, double radius, boolean firing, int color, int warningColor, float opacity, float age) {
        Vec3 direction = end.m_82546_(origin);
        if (direction.m_82556_() < 0.001) {
            return;
        }
        Vec3 forward = direction.m_82541_();
        Vec3 side = forward.m_82537_(Math.abs(forward.f_82480_) > 0.95 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0)).m_82541_();
        Vec3 up = forward.m_82537_(side).m_82541_();
        double core = firing ? 0.14 + Math.sin((double)age * 1.2) * 0.025 : 0.035;
        int beamColor = firing ? color : warningColor;
        EasterSkillVisual.ribbon(vertices, matrix, origin, end, side.m_82490_(core), beamColor, opacity);
        EasterSkillVisual.ribbon(vertices, matrix, origin, end, up.m_82490_(core), beamColor, opacity);
        if (firing) {
            EasterSkillVisual.ribbon(vertices, matrix, origin, end, side.m_82490_(core * 0.3), 15466486, opacity);
            EasterSkillVisual.ribbon(vertices, matrix, origin, end, up.m_82490_(core * 0.3), 15466486, opacity);
        }
        int segments = 24;
        for (int index = 0; index < segments; ++index) {
            double angle = (double)index * Math.PI * 2.0 / (double)segments;
            double nextAngle = ((double)index + 1.0) * Math.PI * 2.0 / (double)segments;
            Vec3 offset = side.m_82490_(Math.cos(angle) * radius).m_82549_(up.m_82490_(Math.sin(angle) * radius));
            Vec3 next = side.m_82490_(Math.cos(nextAngle) * radius).m_82549_(up.m_82490_(Math.sin(nextAngle) * radius));
            EasterSkillVisual.quad(vertices, matrix, origin.m_82549_(offset), end.m_82549_(offset), end.m_82549_(next), origin.m_82549_(next), beamColor, opacity * (firing ? 0.035f : 0.012f));
            if (index % 6 != 0) continue;
            EasterSkillVisual.ribbon(vertices, matrix, origin.m_82549_(offset), end.m_82549_(offset), side.m_82490_(0.025), beamColor, opacity * 0.4f);
        }
    }

    private static void renderDoraSniperBeam(VertexConsumer vertices, Matrix4f matrix, Vec3 origin, Vec3 end, double radius, boolean firing, boolean locked, float opacity, float age) {
        int warning;
        Vec3 ray = end.m_82546_(origin);
        if (ray.m_82556_() < 0.001) {
            return;
        }
        Vec3 forward = ray.m_82541_();
        Vec3 reference = Math.abs(forward.f_82480_) > 0.95 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 side = forward.m_82537_(reference).m_82541_();
        Vec3 up = side.m_82537_(forward).m_82541_();
        int n = warning = locked ? 16732776 : 16763751;
        if (!firing) {
            double width = locked ? 0.046 : 0.03;
            int dashes = 18;
            for (int index = 0; index < dashes; index += 2) {
                double first = (double)index / (double)dashes;
                double second = Math.min(1.0, ((double)index + 1.25) / (double)dashes);
                Vec3 from = origin.m_165921_(end, first);
                Vec3 to = origin.m_165921_(end, second);
                EasterSkillVisual.ribbon(vertices, matrix, from, to, side.m_82490_(width), warning, opacity * 0.82f);
                EasterSkillVisual.ribbon(vertices, matrix, from, to, up.m_82490_(width * 0.55), 16773314, opacity * 0.58f);
            }
            double reticle = Math.max(0.18, radius * (locked ? 1.2 : 0.92));
            EasterSkillVisual.beamRing(vertices, matrix, end, side, up, reticle, 0.028, warning, opacity, 24, (double)age * 0.18);
            EasterSkillVisual.beamRing(vertices, matrix, end, side, up, reticle * 0.62, 0.018, 16773314, opacity * 0.72f, 16, (double)(-age) * 0.26);
            EasterSkillVisual.beamCross(vertices, matrix, end, side, up, reticle * 1.35, 0.018, warning, opacity * 0.85f);
            EasterSkillVisual.beamRing(vertices, matrix, origin.m_82549_(forward.m_82490_(0.12)), side, up, 0.18, 0.024, warning, opacity * 0.78f, 16, (double)(-age) * 0.35);
            return;
        }
        double pulse = 0.15 + Math.sin((double)age * 1.35) * 0.025;
        EasterSkillVisual.ribbon(vertices, matrix, origin, end, side.m_82490_(pulse), 3788741, opacity * 0.38f);
        EasterSkillVisual.ribbon(vertices, matrix, origin, end, up.m_82490_(pulse), 3788741, opacity * 0.38f);
        EasterSkillVisual.ribbon(vertices, matrix, origin, end, side.m_82490_(pulse * 0.42), 0x9FFFF0, opacity);
        EasterSkillVisual.ribbon(vertices, matrix, origin, end, up.m_82490_(pulse * 0.42), 0x9FFFF0, opacity);
        EasterSkillVisual.ribbon(vertices, matrix, origin, end, side.m_82490_(pulse * 0.12), 0xFFFFFF, opacity);
        int gates = 8;
        for (int index = 0; index < gates; ++index) {
            double along = ((double)index + 0.35 + (double)age * 0.22) / (double)gates;
            along -= Math.floor(along);
            EasterSkillVisual.beamRing(vertices, matrix, origin.m_165921_(end, along), side, up, pulse * 1.75, pulse * 0.16, index % 2 == 0 ? 14286839 : 6815712, opacity * 0.62f, 12, (double)age * 0.28 + (double)index);
        }
        EasterSkillVisual.beamRing(vertices, matrix, end, side, up, Math.max(0.3, radius * 1.45), 0.055, 14286839, opacity, 28, (double)age * 0.42);
        EasterSkillVisual.beamCross(vertices, matrix, end, side, up, Math.max(0.42, radius * 1.8), 0.035, 0xFFFFFF, opacity);
    }

    private static void beamRing(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 horizontal, Vec3 vertical, double radius, double width, int color, float opacity, int segments, double spin) {
        double inner = Math.max(0.0, radius - width);
        for (int index = 0; index < segments; ++index) {
            double first = spin + (double)index * Math.PI * 2.0 / (double)segments;
            double second = spin + ((double)index + 1.0) * Math.PI * 2.0 / (double)segments;
            Vec3 firstAxis = horizontal.m_82490_(Math.cos(first)).m_82549_(vertical.m_82490_(Math.sin(first)));
            Vec3 secondAxis = horizontal.m_82490_(Math.cos(second)).m_82549_(vertical.m_82490_(Math.sin(second)));
            EasterSkillVisual.quad(vertices, matrix, center.m_82549_(firstAxis.m_82490_(inner)), center.m_82549_(firstAxis.m_82490_(radius)), center.m_82549_(secondAxis.m_82490_(radius)), center.m_82549_(secondAxis.m_82490_(inner)), color, opacity);
        }
    }

    private static void beamCross(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 horizontal, Vec3 vertical, double radius, double width, int color, float opacity) {
        EasterSkillVisual.ribbon(vertices, matrix, center.m_82546_(horizontal.m_82490_(radius)), center.m_82549_(horizontal.m_82490_(radius)), vertical.m_82490_(width), color, opacity);
        EasterSkillVisual.ribbon(vertices, matrix, center.m_82546_(vertical.m_82490_(radius)), center.m_82549_(vertical.m_82490_(radius)), horizontal.m_82490_(width), color, opacity);
    }

    private static void ribbon(VertexConsumer vertices, Matrix4f matrix, Vec3 start, Vec3 end, Vec3 halfWidth, int color, float opacity) {
        EasterSkillVisual.quad(vertices, matrix, start.m_82546_(halfWidth), end.m_82546_(halfWidth), end.m_82549_(halfWidth), start.m_82549_(halfWidth), color, opacity);
    }

    private static void quad(VertexConsumer vertices, Matrix4f matrix, Vec3 first, Vec3 second, Vec3 third, Vec3 fourth, int color, float opacity) {
        EasterSkillVisual.vertex(vertices, matrix, first, color, opacity);
        EasterSkillVisual.vertex(vertices, matrix, second, color, opacity);
        EasterSkillVisual.vertex(vertices, matrix, third, color, opacity);
        EasterSkillVisual.vertex(vertices, matrix, fourth, color, opacity);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, Vec3 position, int color, float opacity) {
        vertices.m_252986_(matrix, (float)position.f_82479_, (float)position.f_82480_, (float)position.f_82481_).m_6122_(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, (int)(255.0f * EasterCombatMath.clamp(opacity))).m_5752_();
    }

    static {
        for (int index = 0; index < COLORS.length; ++index) {
            int color = COLORS[index];
            EasterSkillVisual.DUST[index] = new DustParticleOptions(new Vector3f((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f), 0.85f);
        }
    }

    private static final class SkillRenderType
    extends RenderType {
        private static final RenderType TYPE = SkillRenderType.m_173215_((String)"ftboss_easter_skill", (VertexFormat)DefaultVertexFormat.f_85815_, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)4096, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.m_110628_().m_173292_(f_173104_).m_110685_(f_110139_).m_110663_(f_110113_).m_110687_(f_110115_).m_110661_(f_110110_).m_110691_(false));

        private SkillRenderType() {
            super("ftboss_easter_skill", DefaultVertexFormat.f_85815_, VertexFormat.Mode.QUADS, 4096, false, true, () -> {}, () -> {});
        }
    }
}

