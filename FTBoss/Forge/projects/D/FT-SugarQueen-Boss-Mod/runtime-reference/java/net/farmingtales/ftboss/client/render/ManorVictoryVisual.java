/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.render.ManorCeremonyRenderType;
import net.farmingtales.ftboss.skill.ManorVictoryPacket;
import net.farmingtales.ftboss.util.ManorVictoryGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
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
public final class ManorVictoryVisual {
    private static final Map<UUID, Show> ACTIVE = new LinkedHashMap<UUID, Show>();

    private ManorVictoryVisual() {
    }

    public static void receive(ManorVictoryPacket packet) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (!packet.valid() || level == null || !level.m_46472_().m_135782_().equals((Object)packet.dimension())) {
            return;
        }
        if (!ACTIVE.containsKey(packet.id()) && ACTIVE.size() >= 4) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
        ACTIVE.putIfAbsent(packet.id(), new Show(packet, level.m_46467_() - (long)packet.age()));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            ACTIVE.clear();
            return;
        }
        ACTIVE.values().removeIf(show -> !level.m_46472_().m_135782_().equals((Object)show.packet.dimension()) || level.m_46467_() < show.started || level.m_46467_() - show.started >= 120L);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().m_5776_()) {
            ACTIVE.clear();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        ACTIVE.clear();
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
        if (minecraft.f_91073_ == null) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        boolean rendered = false;
        VertexConsumer vertices = null;
        for (Show show : ACTIVE.values()) {
            Vec3 center;
            if (!minecraft.f_91073_.m_46472_().m_135782_().equals((Object)show.packet.dimension()) || camera.m_82557_(center = show.packet.center()) > 16384.0 || event.getFrustum() != null && !event.getFrustum().m_113029_(new AABB(center, center.m_82520_(0.0, 5.0, 0.0)).m_82400_(7.0))) continue;
            double age = (float)(minecraft.f_91073_.m_46467_() - show.started) + (minecraft.m_91104_() ? 0.0f : event.getPartialTick());
            PoseStack poses = event.getPoseStack();
            poses.m_85836_();
            try {
                poses.m_85837_(center.f_82479_ - camera.f_82479_, center.f_82480_ - camera.f_82480_, center.f_82481_ - camera.f_82481_);
                PoseStack.Pose matrix = poses.m_85850_();
                if (vertices == null) {
                    vertices = buffers.m_6299_(ManorCeremonyRenderType.TYPE);
                }
                VertexConsumer batch = vertices;
                boolean reduced = minecraft.f_91066_.m_231929_().m_231551_() != ParticleStatus.ALL || camera.m_82557_(center) > 2304.0;
                ManorVictoryGeometry.draw(age, reduced, (horizontal, height, depth, color, alpha) -> batch.m_252986_(matrix.m_252922_(), (float)horizontal, (float)height, (float)depth).m_85950_((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f, alpha).m_5752_());
                rendered = true;
            }
            finally {
                poses.m_85849_();
            }
        }
        if (rendered) {
            buffers.m_109912_(ManorCeremonyRenderType.TYPE);
        }
    }

    private record Show(ManorVictoryPacket packet, long started) {
    }
}

