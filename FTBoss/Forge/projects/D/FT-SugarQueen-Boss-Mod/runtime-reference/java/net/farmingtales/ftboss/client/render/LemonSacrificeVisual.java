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
 *  net.minecraft.client.renderer.texture.OverlayTexture
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.render.ManorIntroRenderType;
import net.farmingtales.ftboss.skill.LemonSacrificePacket;
import net.farmingtales.ftboss.util.LemonSacrificeGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
public final class LemonSacrificeVisual {
    private static final int MAX_ACTIVE = 8;
    private static final Map<UUID, Show> ACTIVE = new LinkedHashMap<UUID, Show>();
    private static ClientLevel world;

    private LemonSacrificeVisual() {
    }

    public static void receive(LemonSacrificePacket packet) {
        Show previous;
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (!packet.valid() || level == null || !level.m_46472_().m_135782_().equals((Object)packet.dimension())) {
            return;
        }
        long now = level.m_46467_();
        if (packet.started() > now + 20L || now - packet.started() >= 60L) {
            return;
        }
        if (world != level) {
            ACTIVE.clear();
            world = level;
        }
        if ((previous = ACTIVE.get(packet.owner())) != null && previous.packet.started() >= packet.started()) {
            return;
        }
        if (previous == null && ACTIVE.size() >= 8) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
        ACTIVE.put(packet.owner(), new Show(packet, Math.min(now, packet.started())));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null || world != level) {
            ACTIVE.clear();
            world = level;
            return;
        }
        ACTIVE.values().removeIf(show -> !LemonSacrificeGeometry.alive(level.m_46467_() - show.started));
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == world) {
            ACTIVE.clear();
            world = null;
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        ACTIVE.clear();
        world = null;
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
        if (minecraft.f_91073_ == null || minecraft.f_91073_ != world) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        boolean reduced = minecraft.f_91066_.m_231929_().m_231551_() != ParticleStatus.ALL;
        List<Show> visible = ACTIVE.values().stream().filter(show -> camera.m_82557_(show.packet.center()) <= 9216.0).filter(show -> event.getFrustum() == null || event.getFrustum().m_113029_(new AABB(show.packet.center(), show.packet.center().m_82520_(0.0, 10.0, 0.0)).m_82400_(26.0))).sorted(Comparator.comparingDouble(show -> camera.m_82557_(show.packet.center()))).limit(reduced ? 2L : 4L).toList();
        if (visible.isEmpty()) {
            return;
        }
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        VertexConsumer vertices = buffers.m_6299_(ManorIntroRenderType.TYPE);
        PoseStack poses = event.getPoseStack();
        try {
            for (Show show2 : visible) {
                LemonSacrificePacket packet = show2.packet;
                double age = (float)(minecraft.f_91073_.m_46467_() - show2.started) + (minecraft.m_91104_() ? 0.0f : event.getPartialTick());
                poses.m_85836_();
                try {
                    poses.m_85837_(packet.center().f_82479_ - camera.f_82479_, packet.center().f_82480_ - camera.f_82480_, packet.center().f_82481_ - camera.f_82481_);
                    PoseStack.Pose matrix = poses.m_85850_();
                    LemonSacrificeGeometry.draw(age, reduced || camera.m_82557_(packet.center()) > 2304.0, packet.souls(), (horizontal, height, depth, color, alpha) -> vertices.m_252986_(matrix.m_252922_(), (float)horizontal, (float)height, (float)depth).m_85950_((float)(color >> 16 & 0xFF) / 255.0f * alpha, (float)(color >> 8 & 0xFF) / 255.0f * alpha, (float)(color & 0xFF) / 255.0f * alpha, 1.0f).m_7421_(0.5f, 0.5f).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix.m_252943_(), 0.0f, 1.0f, 0.0f).m_5752_());
                }
                finally {
                    poses.m_85849_();
                }
            }
        }
        finally {
            buffers.m_109912_(ManorIntroRenderType.TYPE);
        }
    }

    private record Show(LemonSacrificePacket packet, long started) {
    }
}

