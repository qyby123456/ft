/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
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
 *  org.joml.Matrix4f
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.render.ManorCeremonyRenderType;
import net.farmingtales.ftboss.skill.ManorTransitionPacket;
import net.farmingtales.ftboss.util.ManorCeremonyGeometry;
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
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class ManorTransitionVisual {
    private static final Map<UUID, Show> ACTIVE = new LinkedHashMap<UUID, Show>();

    private ManorTransitionVisual() {
    }

    public static void receive(ManorTransitionPacket packet) {
        Show previous;
        if (!packet.valid()) {
            return;
        }
        if (!packet.active()) {
            ACTIVE.remove(packet.id());
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null || !level.m_46472_().m_135782_().equals((Object)packet.dimension())) {
            return;
        }
        if (!ACTIVE.containsKey(packet.id()) && ACTIVE.size() >= 4) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
        long started = (previous = ACTIVE.get(packet.id())) == null || packet.age() < previous.packet.age() ? level.m_46467_() - (long)packet.age() : previous.started;
        ACTIVE.put(packet.id(), new Show(packet, started));
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
        ACTIVE.values().removeIf(show -> !show.packet.dimension().equals((Object)level.m_46472_().m_135782_()) || level.m_46467_() < show.started || level.m_46467_() - show.started >= 90L);
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
        Minecraft client = Minecraft.m_91087_();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || client.f_91073_ == null || ACTIVE.isEmpty()) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        MultiBufferSource.BufferSource buffers = client.m_91269_().m_110104_();
        VertexConsumer vertices = null;
        for (Show show : ACTIVE.values()) {
            double age;
            Vec3 center = show.packet.center();
            if (!show.packet.dimension().equals((Object)client.f_91073_.m_46472_().m_135782_()) || camera.m_82557_(center) > 16384.0 || event.getFrustum() != null && !event.getFrustum().m_113029_(new AABB(center, center.m_82520_(0.0, 8.0, 0.0)).m_82400_(6.0)) || (age = (double)((float)(client.f_91073_.m_46467_() - show.started) + (client.m_91104_() ? 0.0f : event.getPartialTick()))) < 0.0 || age >= 90.0) continue;
            if (vertices == null) {
                vertices = buffers.m_6299_(ManorCeremonyRenderType.TYPE);
            }
            VertexConsumer batch = vertices;
            PoseStack pose = event.getPoseStack();
            pose.m_85836_();
            try {
                pose.m_85837_(center.f_82479_ - camera.f_82479_, center.f_82480_ - camera.f_82480_, center.f_82481_ - camera.f_82481_);
                Matrix4f matrix = pose.m_85850_().m_252922_();
                ManorCeremonyGeometry.transition(age, show.packet.variant(), client.f_91066_.m_231929_().m_231551_() != ParticleStatus.ALL || camera.m_82557_(center) > 2304.0, (posX, posY, posZ, color, alpha) -> batch.m_252986_(matrix, (float)posX, (float)posY, (float)posZ).m_85950_((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f, alpha).m_5752_());
            }
            finally {
                pose.m_85849_();
            }
        }
        if (vertices != null) {
            buffers.m_109912_(ManorCeremonyRenderType.TYPE);
        }
    }

    private record Show(ManorTransitionPacket packet, long started) {
    }
}

