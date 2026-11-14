/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
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
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.skill.LemonCatFxPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class LemonCatSkillVisual {
    private static final int LIFE = 22;
    private static final int MAX_ACTIVE = 48;
    private static final float[] GOLD = new float[]{1.0f, 0.72f, 0.15f};
    private static final float[] MINT = new float[]{0.35f, 1.0f, 0.68f};
    private static final List<Flash> ACTIVE = new ArrayList<Flash>();
    private static ClientLevel activeLevel;

    public static void receive(LemonCatFxPacket message) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null || !message.dimension().equals((Object)level.m_46472_().m_135782_()) || message.kind() < 0 || message.kind() > 3) {
            return;
        }
        Vec3 position = message.position();
        if (!(Double.isFinite(position.f_82479_) && Double.isFinite(position.f_82480_) && Double.isFinite(position.f_82481_) && Float.isFinite(message.yaw()))) {
            return;
        }
        if (activeLevel != level) {
            ACTIVE.clear();
            activeLevel = level;
        }
        if (ACTIVE.size() >= 48) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Flash(message, level.m_46467_()));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null || level != activeLevel) {
            ACTIVE.clear();
            activeLevel = level;
            return;
        }
        ACTIVE.removeIf(flash -> level.m_46467_() - flash.born >= 22L);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().m_5776_()) {
            ACTIVE.clear();
            activeLevel = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null || minecraft.f_91073_ != activeLevel) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        RenderType glow = GlowType.VALUE;
        VertexConsumer vertices = buffers.m_6299_(glow);
        pose.m_85836_();
        try {
            pose.m_85837_(-camera.f_82479_, -camera.f_82480_, -camera.f_82481_);
            for (Flash flash : ACTIVE) {
                Vec3 center = flash.message.position();
                if (camera.m_82557_(center) > 4096.0) continue;
                float age = (float)(minecraft.f_91073_.m_46467_() - flash.born) + event.getPartialTick();
                float progress = Mth.m_14036_((float)(age / 22.0f), (float)0.0f, (float)1.0f);
                float alpha = (1.0f - progress) * 0.85f;
                boolean heal = flash.message.kind() % 2 == 1;
                boolean paired = flash.message.kind() >= 2;
                pose.m_85836_();
                try {
                    pose.m_85837_(center.f_82479_, center.f_82480_ + 0.12, center.f_82481_);
                    float heading = (float)Math.toRadians(flash.message.yaw());
                    if (heal) {
                        LemonCatSkillVisual.ring(vertices, pose.m_85850_(), 0.0f, 0.0f, 0.0f, 0.6f + progress * 1.35f, 0.1f, 0.0f, (float)Math.PI * 2, MINT, alpha);
                        if (paired) {
                            LemonCatSkillVisual.ring(vertices, pose.m_85850_(), 0.0f, 0.1f, 0.0f, 0.85f + progress * 1.4f, 0.07f, 0.0f, (float)Math.PI * 2, GOLD, alpha);
                        }
                        LemonCatSkillVisual.ring(vertices, pose.m_85850_(), 0.0f, 0.01f, 0.05f, 0.28f, 0.28f, 0.0f, (float)Math.PI * 2, MINT, alpha * 0.5f);
                        for (int toe = 0; toe < 4; ++toe) {
                            float angle = -1.0f + (float)toe * 0.67f;
                            LemonCatSkillVisual.ring(vertices, pose.m_85850_(), Mth.m_14031_((float)angle) * 0.46f, 0.01f, -Mth.m_14089_((float)angle) * 0.46f, 0.105f, 0.105f, 0.0f, (float)Math.PI * 2, GOLD, alpha * 0.65f);
                        }
                        continue;
                    }
                    float radius = (paired ? 5.5f : 4.5f) * Math.min(1.0f, 0.15f + progress * 1.8f);
                    LemonCatSkillVisual.ring(vertices, pose.m_85850_(), 0.0f, 0.55f + progress * 0.55f, 0.0f, radius, 0.16f, heading - 0.96f, heading + 0.96f, GOLD, alpha);
                    if (!paired) continue;
                    LemonCatSkillVisual.ring(vertices, pose.m_85850_(), 0.0f, 0.4f + progress * 0.4f, 0.0f, radius * 0.88f, 0.09f, heading - 0.96f, heading + 0.96f, MINT, alpha);
                }
                finally {
                    pose.m_85849_();
                }
            }
        }
        finally {
            pose.m_85849_();
            buffers.m_109912_(glow);
        }
    }

    private static void ring(VertexConsumer vertices, PoseStack.Pose pose, float centerX, float height, float centerZ, float radius, float width, float from, float to, float[] color, float alpha) {
        int segments = 40;
        for (int segment = 0; segment < segments; ++segment) {
            float first = Mth.m_14179_((float)((float)segment / (float)segments), (float)from, (float)to);
            float second = Mth.m_14179_((float)(((float)segment + 1.0f) / (float)segments), (float)from, (float)to);
            LemonCatSkillVisual.vertex(vertices, pose, centerX - Mth.m_14031_((float)first) * radius, height, centerZ + Mth.m_14089_((float)first) * radius, color, alpha);
            LemonCatSkillVisual.vertex(vertices, pose, centerX - Mth.m_14031_((float)first) * (radius - width), height, centerZ + Mth.m_14089_((float)first) * (radius - width), color, alpha);
            LemonCatSkillVisual.vertex(vertices, pose, centerX - Mth.m_14031_((float)second) * (radius - width), height, centerZ + Mth.m_14089_((float)second) * (radius - width), color, alpha);
            LemonCatSkillVisual.vertex(vertices, pose, centerX - Mth.m_14031_((float)second) * radius, height, centerZ + Mth.m_14089_((float)second) * radius, color, alpha);
        }
    }

    private static void vertex(VertexConsumer vertices, PoseStack.Pose pose, float horizontal, float height, float depth, float[] color, float alpha) {
        vertices.m_252986_(pose.m_252922_(), horizontal, height, depth).m_85950_(color[0] * alpha, color[1] * alpha, color[2] * alpha, alpha).m_7421_(0.5f, 0.5f).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(pose.m_252943_(), 0.0f, 1.0f, 0.0f).m_5752_();
    }

    private record Flash(LemonCatFxPacket message, long born) {
    }

    private static final class GlowType {
        private static final RenderType VALUE = RenderType.m_110488_((ResourceLocation)new ResourceLocation("ftboss", "textures/fx/lemoncat_white.png"));

        private GlowType() {
        }
    }
}

