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
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Matrix4f
 *  org.joml.Vector3d
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.CombatVisualEntities;
import net.farmingtales.ftboss.entity.ChargedOrbEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.OrbType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3d;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class DefectOrbVisual {
    private static final Map<UUID, Map<String, Anchor>> ANCHORS = new HashMap<UUID, Map<String, Anchor>>();
    private static final String[] HANDS = new String[]{"hand_l", "hand_r"};
    private static Level currentLevel;

    private DefectOrbVisual() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft client = Minecraft.m_91087_();
        DefectOrbVisual.useLevel((Level)client.f_91073_);
        if (client.f_91073_ == null || client.m_91104_()) {
            return;
        }
        long tick = client.f_91073_.m_46467_();
        if (tick % 20L == 0L) {
            ANCHORS.values().removeIf(points -> points.values().stream().allMatch(point -> tick - point.tick > 40L));
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == currentLevel) {
            DefectOrbVisual.useLevel(null);
        }
    }

    private static void useLevel(Level level) {
        if (currentLevel == level) {
            return;
        }
        ANCHORS.clear();
        currentLevel = level;
    }

    public static void track(DefectCoreEntity boss, String bone, Vector3d position) {
        DefectOrbVisual.useLevel(boss.m_9236_());
        ANCHORS.computeIfAbsent(boss.m_20148_(), ignored -> new HashMap()).put(bone, new Anchor(new Vec3(position.x, position.y, position.z), boss.m_9236_().m_46467_()));
    }

    private static Vec3 anchor(DefectCoreEntity boss, String bone, float partialTick) {
        Anchor point;
        Map<String, Anchor> tracked = ANCHORS.get(boss.m_20148_());
        Anchor anchor = point = tracked == null ? null : tracked.get(bone);
        if (point != null && boss.m_9236_().m_46467_() - point.tick <= 1L) {
            return point.position;
        }
        return boss.orbAnchor(!bone.equals("core")).m_82549_(boss.m_20318_(partialTick).m_82546_(boss.m_20182_()));
    }

    private static int color(OrbType type) {
        return switch (type) {
            default -> throw new IncompatibleClassChangeError();
            case OrbType.GLASS -> 7532269;
            case OrbType.LIGHTNING -> 16769908;
            case OrbType.FROST -> 11987199;
            case OrbType.ION -> 0xEE91EE;
            case OrbType.DARK -> 11236852;
        };
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft client = Minecraft.m_91087_();
        DefectOrbVisual.useLevel((Level)client.f_91073_);
        if (client.f_91073_ == null) {
            return;
        }
        List<Entity> candidates = CombatVisualEntities.get(client.f_91073_).orbLinks();
        if (candidates.isEmpty()) {
            return;
        }
        long tick = client.f_91073_.m_46467_();
        Vec3 camera = event.getCamera().m_90583_();
        float partialTick = event.getPartialTick();
        double clock = (float)tick + partialTick;
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = client.m_91269_().m_110104_();
        pose.m_85836_();
        try {
            pose.m_85837_(-camera.f_82479_, -camera.f_82480_, -camera.f_82481_);
            Matrix4f matrix = pose.m_85850_().m_252922_();
            VertexConsumer vertices = buffers.m_6299_(LinkRenderType.TYPE);
            int linkBudget = 48;
            int coreBudget = 12;
            for (Entity candidate : candidates) {
                String stage;
                if (linkBudget <= 0 && coreBudget <= 0) break;
                if (candidate.m_213877_() || candidate.m_20238_(camera) > 4096.0) continue;
                if (candidate instanceof ChargedOrbEntity) {
                    ChargedOrbEntity orb = (ChargedOrbEntity)candidate;
                    if (linkBudget > 0) {
                        boolean flowing;
                        DefectCoreEntity boss = orb.ownerBoss();
                        if (boss == null || !boss.m_6084_()) continue;
                        stage = boss.orbLinkStage();
                        String visual = orb.visualState();
                        boolean focus = stage.equals("focus") && boss.orbCue().m_128451_("orb") == orb.m_19879_();
                        boolean forming = visual.equals("summon");
                        boolean bl = flowing = stage.startsWith("beam") || stage.equals("passive") && orb.getOrbType() != OrbType.DARK || visual.equals("drain") || visual.equals("return") || stage.equals("phase") || stage.equals("echo");
                        if (!focus && !forming && !flowing) continue;
                        int tint = DefectOrbVisual.color(orb.getOrbType());
                        Vec3 orbCenter = orb.m_20318_(partialTick).m_82520_(0.0, 0.25, 0.0);
                        String[] source = DefectOrbVisual.anchor(boss, focus || forming ? "hand_r" : "core", partialTick);
                        String[] start = forming ? source : orbCenter;
                        Vec3 end = forming ? orbCenter : source;
                        double thickness = stage.startsWith("beam") ? 0.028 : 0.014;
                        DefectOrbVisual.strand(vertices, matrix, (Vec3)start, end, camera, thickness, tint, 0.35f, clock, orb.m_19879_());
                        for (int bead = 0; bead < 3; ++bead) {
                            double progress = (clock * 0.055 + (double)bead / 3.0 + (double)orb.m_19879_() * 0.17) % 1.0;
                            Vec3 point = start.m_165921_(end, progress);
                            DefectOrbVisual.segment(vertices, matrix, point, start.m_165921_(end, Math.min(1.0, progress + 0.065)), camera, thickness * 2.6, 0xF6FFFF, 0.9f);
                        }
                        if (focus) {
                            DefectOrbVisual.ring(vertices, matrix, orbCenter, camera, 0.46, tint, 0.65f, clock * 0.04, false);
                        }
                        --linkBudget;
                        continue;
                    }
                }
                if (!(candidate instanceof DefectCoreEntity)) continue;
                DefectCoreEntity boss = (DefectCoreEntity)candidate;
                if (coreBudget <= 0 || !boss.m_6084_()) continue;
                CompoundTag cue = boss.orbCue();
                stage = boss.orbLinkStage();
                if (stage.isEmpty()) continue;
                double age = clock - (double)cue.m_128454_("start");
                double progress = Math.min(1.0, Math.max(0.0, age / (double)Math.max(1, cue.m_128451_("duration"))));
                OrbType type = OrbType.byId(cue.m_128461_("type"));
                int tint = DefectOrbVisual.color(type);
                Vec3 core = DefectOrbVisual.anchor(boss, "core", partialTick);
                if (stage.startsWith("beam")) {
                    DefectOrbVisual.ring(vertices, matrix, core, camera, 0.38 + 0.22 * progress, tint, 0.8f, clock * 0.07, false);
                    for (String hand : HANDS) {
                        DefectOrbVisual.strand(vertices, matrix, DefectOrbVisual.anchor(boss, hand, partialTick), core, camera, 0.035, tint, 0.7f, clock, 0);
                    }
                } else if (stage.equals("release") || stage.equals("interrupt")) {
                    Vec3 center = type == OrbType.FROST || type == OrbType.ION ? core : DefectOrbVisual.anchor(boss, "hand_r", partialTick);
                    double radius = stage.equals("interrupt") ? 0.55 * (1.0 - progress) : 0.25 + progress * 1.25;
                    DefectOrbVisual.ring(vertices, matrix, center, camera, radius, tint, (float)(0.8 * (1.0 - progress)), clock * 0.08, type == OrbType.FROST);
                    if (type == OrbType.ION || type == OrbType.DARK) {
                        DefectOrbVisual.ring(vertices, matrix, center, camera, radius * 0.72, tint, (float)(0.7 * (1.0 - progress)), -clock * 0.08, true);
                    }
                } else {
                    DefectOrbVisual.ring(vertices, matrix, core, camera, 0.32 + Math.sin(clock * 0.25) * 0.035, tint, 0.5f, clock * 0.03, false);
                }
                --coreBudget;
            }
            buffers.m_109912_(LinkRenderType.TYPE);
        }
        finally {
            pose.m_85849_();
        }
    }

    private static void strand(VertexConsumer vertices, Matrix4f matrix, Vec3 start, Vec3 end, Vec3 camera, double width, int color, float opacity, double clock, int seed) {
        Vec3 previous = start;
        for (int step = 1; step <= 10; ++step) {
            double progress = (double)step / 10.0;
            double wave = Math.sin(Math.PI * progress) * 0.055;
            Vec3 point = start.m_165921_(end, progress).m_82520_(Math.sin(clock * 0.16 + (double)seed + (double)step) * wave, Math.cos(clock * 0.13 + (double)seed + (double)step) * wave, 0.0);
            DefectOrbVisual.segment(vertices, matrix, previous, point, camera, width, color, opacity);
            previous = point;
        }
    }

    private static void ring(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 camera, double radius, int color, float opacity, double angle, boolean horizontal) {
        Vec3 normal = camera.m_82546_(center).m_82541_();
        Vec3 across = horizontal ? new Vec3(1.0, 0.0, 0.0) : normal.m_82537_(new Vec3(0.0, 1.0, 0.0)).m_82541_();
        Vec3 up = horizontal ? new Vec3(0.0, 0.0, 1.0) : across.m_82537_(normal).m_82541_();
        for (int step = 0; step < 32; ++step) {
            if (step % 8 == 7) continue;
            double first = angle + (double)step * Math.PI / 16.0;
            double second = angle + (double)(step + 1) * Math.PI / 16.0;
            Vec3 start = center.m_82549_(across.m_82490_(Math.cos(first) * radius)).m_82549_(up.m_82490_(Math.sin(first) * radius));
            Vec3 end = center.m_82549_(across.m_82490_(Math.cos(second) * radius)).m_82549_(up.m_82490_(Math.sin(second) * radius));
            DefectOrbVisual.segment(vertices, matrix, start, end, camera, 0.02, color, opacity);
        }
    }

    private static void segment(VertexConsumer vertices, Matrix4f matrix, Vec3 start, Vec3 end, Vec3 camera, double width, int color, float opacity) {
        Vec3 side = end.m_82546_(start).m_82537_(camera.m_82546_(start)).m_82541_().m_82490_(width);
        DefectOrbVisual.vertex(vertices, matrix, start.m_82549_(side), color, opacity);
        DefectOrbVisual.vertex(vertices, matrix, end.m_82549_(side), color, opacity);
        DefectOrbVisual.vertex(vertices, matrix, end.m_82546_(side), color, opacity);
        DefectOrbVisual.vertex(vertices, matrix, start.m_82546_(side), color, opacity);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, Vec3 point, int color, float opacity) {
        vertices.m_252986_(matrix, (float)point.f_82479_, (float)point.f_82480_, (float)point.f_82481_).m_6122_(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, (int)(255.0f * Math.max(0.0f, Math.min(1.0f, opacity)))).m_5752_();
    }

    private record Anchor(Vec3 position, long tick) {
    }

    private static final class LinkRenderType
    extends RenderType {
        private static final RenderType TYPE = LinkRenderType.m_173215_((String)"ftboss_defect_link", (VertexFormat)DefaultVertexFormat.f_85815_, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)8192, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.m_110628_().m_173292_(f_173104_).m_110685_(f_110139_).m_110663_(f_110113_).m_110687_(f_110115_).m_110661_(f_110110_).m_110691_(false));

        private LinkRenderType() {
            super("ftboss_defect_link", DefaultVertexFormat.f_85815_, VertexFormat.Mode.QUADS, 8192, false, true, () -> {}, () -> {});
        }
    }
}

