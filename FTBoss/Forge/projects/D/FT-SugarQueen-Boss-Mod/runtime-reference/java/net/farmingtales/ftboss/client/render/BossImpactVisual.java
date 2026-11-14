/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayDeque;
import net.farmingtales.ftboss.client.render.BossImpactRenderType;
import net.farmingtales.ftboss.skill.BossImpactPacket;
import net.farmingtales.ftboss.util.BossImpactGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class BossImpactVisual {
    private static final ArrayDeque<Impact> ACTIVE = new ArrayDeque();
    private static ClientLevel activeLevel;
    private static long lastSound;

    private BossImpactVisual() {
    }

    private static void clear() {
        ACTIVE.clear();
        activeLevel = null;
        lastSound = Long.MIN_VALUE;
    }

    public static void receive(BossImpactPacket packet) {
        boolean heavy;
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (!packet.valid() || level == null || !level.m_46472_().m_135782_().equals((Object)packet.dimension())) {
            return;
        }
        if (activeLevel != level) {
            BossImpactVisual.clear();
            activeLevel = level;
        }
        if (minecraft.f_91074_ == null || minecraft.f_91074_.m_20238_(packet.position()) > 2304.0) {
            return;
        }
        if (ACTIVE.size() >= 32) {
            ACTIVE.removeFirst();
        }
        long now = level.m_46467_();
        ACTIVE.addLast(new Impact(packet, now));
        if (minecraft.f_91074_.m_20238_(packet.position()) > 256.0 || lastSound != Long.MIN_VALUE && now >= lastSound && now - lastSound < 6L) {
            return;
        }
        lastSound = now;
        boolean bl = heavy = packet.strength() >= 0.75f;
        SoundEvent sound = packet.shield() ? SoundEvents.f_144245_ : (heavy ? SoundEvents.f_12316_ : SoundEvents.f_12318_);
        Vec3 position = packet.position();
        level.m_7785_(position.f_82479_, position.f_82480_, position.f_82481_, sound, SoundSource.HOSTILE, packet.shield() ? 0.24f : (heavy ? 0.32f : 0.16f), 0.92f + level.f_46441_.m_188501_() * 0.14f, false);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null || minecraft.f_91073_ != activeLevel) {
            BossImpactVisual.clear();
            return;
        }
        if (!minecraft.m_91104_()) {
            ACTIVE.removeIf(impact -> !BossImpactGeometry.alive(minecraft.f_91073_.m_46467_() - impact.started));
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().m_5776_()) {
            BossImpactVisual.clear();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        BossImpactVisual.clear();
    }

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
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        VertexConsumer vertices = buffers.m_6299_(BossImpactRenderType.TYPE);
        PoseStack.Pose matrix = event.getPoseStack().m_85850_();
        for (Impact impact : ACTIVE) {
            BossImpactPacket packet = impact.packet;
            if (camera.m_82557_(packet.position()) > 2304.0 || event.getFrustum() != null && !event.getFrustum().m_113029_(new AABB(packet.position(), packet.position()).m_82400_(1.8))) continue;
            double age = (float)(minecraft.f_91073_.m_46467_() - impact.started) + (minecraft.m_91104_() ? 0.0f : event.getPartialTick());
            Vec3 normal = packet.normal();
            Vec3 reference = Math.abs(normal.f_82480_) > 0.95 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
            Vec3 right = reference.m_82537_(normal).m_82541_();
            Vec3 up = normal.m_82537_(right).m_82541_();
            Vec3 origin = packet.position().m_82546_(camera);
            int detail = ((ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_()).ordinal();
            if (camera.m_82557_(packet.position()) > 576.0) {
                detail = Math.max(1, detail);
            }
            BossImpactGeometry.draw(age, packet.strength(), packet.shield(), packet.color(), detail, (horizontal, vertical, color, alpha) -> {
                Vec3 point = origin.m_82549_(right.m_82490_(horizontal * (double)packet.size())).m_82549_(up.m_82490_(vertical * (double)packet.size()));
                vertices.m_252986_(matrix.m_252922_(), (float)point.f_82479_, (float)point.f_82480_, (float)point.f_82481_).m_85950_((float)(color >> 16 & 0xFF) / 255.0f * alpha, (float)(color >> 8 & 0xFF) / 255.0f * alpha, (float)(color & 0xFF) / 255.0f * alpha, 1.0f).m_7421_(0.5f, 0.5f).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix.m_252943_(), (float)normal.f_82479_, (float)normal.f_82480_, (float)normal.f_82481_).m_5752_();
            });
        }
        buffers.m_109912_(BossImpactRenderType.TYPE);
    }

    static {
        lastSound = Long.MIN_VALUE;
    }

    private record Impact(BossImpactPacket packet, long started) {
    }
}

