/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$RenderTickEvent
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.render.ManorCeremonyRenderType;
import net.farmingtales.ftboss.client.render.ManorIntroMesh;
import net.farmingtales.ftboss.skill.ManorIntroPacket;
import net.farmingtales.ftboss.util.PresentationClock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class ManorIntroVisual {
    private static final RenderType TYPE = ManorCeremonyRenderType.TYPE;
    private static final int MAX_ACTIVE = 4;
    private static final double VIEW_DISTANCE_SQUARED = 16384.0;
    private static final Map<UUID, Show> ACTIVE = new LinkedHashMap<UUID, Show>();

    private ManorIntroVisual() {
    }

    public static void receive(ManorIntroPacket packet) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (!packet.active()) {
            ACTIVE.remove(packet.id());
            return;
        }
        if (minecraft.f_91073_ == null || !minecraft.f_91073_.m_46472_().m_135782_().equals((Object)packet.dimension()) || packet.elapsedMillis() < 0L || packet.elapsedMillis() >= 46000L) {
            return;
        }
        Show show = ACTIVE.get(packet.id());
        if (show == null) {
            if (ACTIVE.size() >= 4) {
                ACTIVE.remove(ACTIVE.keySet().iterator().next());
            }
            ACTIVE.put(packet.id(), new Show(packet));
            FTBossMod.LOGGER.info("[FTBoss][ManorIntro] client mesh start id={} elapsed={}ms", (Object)packet.id(), (Object)packet.elapsedMillis());
        } else {
            show.move(packet.position());
        }
    }

    private static void clear() {
        ACTIVE.clear();
        PresentationClock.setPaused(false);
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            PresentationClock.setPaused(Minecraft.m_91087_().m_91104_());
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null) {
            ManorIntroVisual.clear();
            return;
        }
        long nowMillis = PresentationClock.millis();
        ACTIVE.values().removeIf(show -> !minecraft.f_91073_.m_46472_().m_135782_().equals((Object)show.dimension) || nowMillis - show.startMillis >= 46000L);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().m_5776_()) {
            ManorIntroVisual.clear();
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ManorIntroVisual.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        boolean rendered = false;
        VertexConsumer vertices = null;
        long nowMillis = PresentationClock.millis();
        for (Show show : ACTIVE.values()) {
            long elapsedMillis;
            if (!minecraft.f_91073_.m_46472_().m_135782_().equals((Object)show.dimension) || camera.m_82557_(show.position) > 16384.0 || event.getFrustum() != null && !event.getFrustum().m_113029_(show.bounds) || (elapsedMillis = nowMillis - show.startMillis) < 0L || elapsedMillis >= 46000L) continue;
            boolean reduced = minecraft.f_91066_.m_231929_().m_231551_() == ParticleStatus.MINIMAL || camera.m_82557_(show.position) > 2304.0;
            pose.m_85836_();
            try {
                pose.m_85837_(show.position.f_82479_ - camera.f_82479_, show.position.f_82480_ - camera.f_82480_, show.position.f_82481_ - camera.f_82481_);
                PoseStack.Pose matrix = pose.m_85850_();
                if (vertices == null) {
                    vertices = buffers.m_6299_(TYPE);
                }
                VertexConsumer batch = vertices;
                ManorIntroMesh.emit(elapsedMillis, reduced, (posX, posY, posZ, color, alpha) -> batch.m_252986_(matrix.m_252922_(), (float)posX, (float)posY, (float)posZ).m_85950_((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f, alpha).m_5752_());
                rendered = true;
            }
            finally {
                pose.m_85849_();
            }
        }
        if (rendered) {
            buffers.m_109912_(TYPE);
        }
    }

    private static final class Show {
        final ResourceLocation dimension;
        final long startMillis;
        Vec3 position;
        AABB bounds;

        Show(ManorIntroPacket packet) {
            this.dimension = packet.dimension();
            this.startMillis = PresentationClock.millis() - packet.elapsedMillis();
            this.move(packet.position());
        }

        void move(Vec3 nextPosition) {
            this.position = nextPosition;
            this.bounds = new AABB(this.position.f_82479_ - 12.0, this.position.f_82480_ - 1.0, this.position.f_82481_ - 12.0, this.position.f_82479_ + 12.0, this.position.f_82480_ + 27.0, this.position.f_82481_ + 12.0);
        }
    }
}

