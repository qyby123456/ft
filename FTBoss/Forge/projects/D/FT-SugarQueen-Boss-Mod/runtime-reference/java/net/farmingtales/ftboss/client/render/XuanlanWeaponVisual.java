/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
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
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.client.render.XuanlanSkyVisual;
import net.farmingtales.ftboss.client.render.XuanlanWeaponMotion;
import net.farmingtales.ftboss.client.render.XuanlanWeaponRenderer;
import net.farmingtales.ftboss.client.xuanlannative.NativeScene;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class XuanlanWeaponVisual {
    private static final List<Effect> EFFECTS = new ArrayList<Effect>();
    private static final int GREEN = 6815692;
    private static final int WHITE = 14413567;

    private XuanlanWeaponVisual() {
    }

    public static void receive(XuanlanFxPacket xuanlanFxPacket) {
        XuanlanFxPacket packet;
        if (NativeScene.accept(xuanlanFxPacket)) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (XuanlanMotionRules.action(packet.kind())) {
            XuanlanWeaponMotion.receive(packet);
            return;
        }
        if (level == null || !level.m_46472_().m_135782_().equals((Object)packet.dimension()) || packet.kind() < 0 || packet.kind() > 5 && !XuanlanSkyVisual.handles(packet.kind())) {
            return;
        }
        if (packet.kind() == 24) {
            EFFECTS.removeIf(effect -> effect.packet.entityId() == packet.entityId() && XuanlanSkyVisual.handles(effect.packet.kind()));
            return;
        }
        if (packet.kind() == 2 || packet.kind() == 3) {
            EFFECTS.removeIf(effect -> effect.packet.kind() == 2 && effect.packet.entityId() == packet.entityId());
        }
        if (EFFECTS.size() >= 128) {
            EFFECTS.remove(0);
        }
        int life = XuanlanSkyVisual.handles(packet.kind()) ? XuanlanSkyVisual.lifetime(packet.kind()) : (packet.kind() == 2 ? 120 : (packet.kind() <= 1 ? 8 : 14));
        EFFECTS.add(new Effect(packet, level.m_46467_(), life));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        XuanlanWeaponMotion.tick((Level)level);
        if (level == null) {
            EFFECTS.clear();
        } else {
            EFFECTS.removeIf(effect -> !level.m_46472_().m_135782_().equals((Object)effect.packet.dimension()) || level.m_46467_() - effect.born >= (long)effect.life || level.m_46467_() < effect.born);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().m_5776_()) {
            EFFECTS.clear();
            if (event.getLevel() == Minecraft.m_91087_().f_91073_) {
                XuanlanWeaponMotion.useLevel(null);
            }
            XuanlanWeaponRenderer.clearPrepared();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || minecraft.f_91073_ == null) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        Vec3 right = new Vec3(event.getCamera().m_252775_());
        Vec3 up = new Vec3(event.getCamera().m_253028_());
        float partial = event.getPartialTick();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        VertexConsumer vertices = buffers.m_6299_(RenderType.m_110502_());
        pose.m_85836_();
        try {
            pose.m_85837_(-camera.f_82479_, -camera.f_82480_, -camera.f_82481_);
            for (Effect effect : EFFECTS) {
                XuanlanFxPacket packet = effect.packet;
                float age = (float)(minecraft.f_91073_.m_46467_() - effect.born) + partial;
                float fade = Math.max(0.0f, 1.0f - age / (float)effect.life);
                if (XuanlanSkyVisual.handles(packet.kind())) {
                    XuanlanSkyVisual.render(pose, vertices, packet, camera, age, effect.life);
                    continue;
                }
                if (packet.kind() <= 1) {
                    boolean empowered = packet.kind() == 1;
                    XuanlanWeaponVisual.line(pose, vertices, packet.from(), packet.to(), camera, empowered ? 0.11 : 0.065, 6815692, fade * 0.35f);
                    XuanlanWeaponVisual.line(pose, vertices, packet.from(), packet.to(), camera, 0.018, 14413567, fade);
                    XuanlanWeaponVisual.ring(pose, vertices, packet.to(), right, up, 0.12 + (double)age * 0.025, 0.025, 6815692, fade, (double)age * 0.12, false);
                    continue;
                }
                if (packet.kind() == 2) {
                    Entity target = minecraft.f_91073_.m_6815_(packet.entityId());
                    if (target == null || !target.m_6084_()) continue;
                    Vec3 center = target.m_20318_(partial).m_82520_(0.0, (double)target.m_20206_() + 0.3, 0.0);
                    float opacity = Math.min(1.0f, ((float)effect.life - age) / 12.0f) * 0.8f;
                    XuanlanWeaponVisual.ring(pose, vertices, center, right, up, 0.25, 0.024, 6815692, opacity, (double)age * 0.035, true);
                    XuanlanWeaponVisual.line(pose, vertices, center.m_82549_(up.m_82490_(-0.09)), center.m_82549_(up.m_82490_(0.09)), camera, 0.018, 14413567, opacity);
                    continue;
                }
                boolean burst = packet.kind() == 3;
                double radius = (burst ? 0.2 : 0.12) + (double)age * (burst ? 0.095 : 0.035);
                XuanlanWeaponVisual.ring(pose, vertices, packet.from(), right, up, radius, 0.035 * (double)fade, burst ? 6815692 : 14413567, fade, (double)age * 0.16, true);
                if (!burst) continue;
                XuanlanWeaponVisual.ring(pose, vertices, packet.from(), right, up, radius * 0.72, 0.025, 14413567, fade, (double)(-age) * 0.12, true);
            }
        }
        finally {
            pose.m_85849_();
            buffers.m_109912_(RenderType.m_110502_());
        }
    }

    public static void ring(PoseStack pose, VertexConsumer vertices, Vec3 center, Vec3 right, Vec3 up, double radius, double thickness, int color, float alpha, double rotation, boolean broken) {
        for (int segment = 0; segment < 48; ++segment) {
            if (broken && segment % 24 >= 18) continue;
            double angle = rotation + (double)segment * Math.PI / 24.0;
            double next = rotation + (double)(segment + 1) * Math.PI / 24.0;
            Vec3 direction = right.m_82490_(Math.cos(angle)).m_82549_(up.m_82490_(Math.sin(angle)));
            Vec3 nextDirection = right.m_82490_(Math.cos(next)).m_82549_(up.m_82490_(Math.sin(next)));
            XuanlanWeaponVisual.quad(pose, vertices, center.m_82549_(direction.m_82490_(radius + thickness)), center.m_82549_(nextDirection.m_82490_(radius + thickness)), center.m_82549_(nextDirection.m_82490_(Math.max(0.0, radius - thickness))), center.m_82549_(direction.m_82490_(Math.max(0.0, radius - thickness))), color, alpha);
        }
    }

    public static void line(PoseStack pose, VertexConsumer vertices, Vec3 from, Vec3 to, Vec3 camera, double width, int color, float alpha) {
        Vec3 side = to.m_82546_(from).m_82537_(camera.m_82546_(from)).m_82541_();
        if (side.m_82556_() < 0.01) {
            side = new Vec3(1.0, 0.0, 0.0);
        }
        side = side.m_82490_(width);
        XuanlanWeaponVisual.quad(pose, vertices, from.m_82549_(side), to.m_82549_(side), to.m_82546_(side), from.m_82546_(side), color, alpha);
    }

    private static void quad(PoseStack pose, VertexConsumer vertices, Vec3 first, Vec3 second, Vec3 third, Vec3 fourth, int color, float alpha) {
        XuanlanWeaponVisual.vertex(pose, vertices, first, color, alpha);
        XuanlanWeaponVisual.vertex(pose, vertices, second, color, alpha);
        XuanlanWeaponVisual.vertex(pose, vertices, third, color, alpha);
        XuanlanWeaponVisual.vertex(pose, vertices, fourth, color, alpha);
    }

    private static void vertex(PoseStack pose, VertexConsumer vertices, Vec3 position, int color, float alpha) {
        vertices.m_252986_(pose.m_85850_().m_252922_(), (float)position.f_82479_, (float)position.f_82480_, (float)position.f_82481_).m_6122_(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, Math.max(0, Math.min(255, (int)(alpha * 255.0f)))).m_5752_();
    }

    private record Effect(XuanlanFxPacket packet, long born, int life) {
    }
}

