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
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.world.entity.Entity
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
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.render.ManorRecallBoundaryRenderType;
import net.farmingtales.ftboss.skill.ManorRecallBoundaryPacket;
import net.farmingtales.ftboss.util.ManorRecallBoundaryGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
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
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class ManorRecallBoundaryVisual {
    private static final Map<UUID, Boundary> ACTIVE = new LinkedHashMap<UUID, Boundary>();
    private static final DustParticleOptions RED_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.12f, 0.28f), 0.85f);
    private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.72f, 0.24f), 0.72f);
    private static final int TIMEOUT_TICKS = 45;

    private ManorRecallBoundaryVisual() {
    }

    public static void receive(ManorRecallBoundaryPacket packet) {
        if (!packet.valid()) {
            return;
        }
        if (!packet.active()) {
            ACTIVE.remove(packet.fightId());
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null || !level.m_46472_().m_135782_().equals((Object)packet.dimension())) {
            return;
        }
        if (!ACTIVE.containsKey(packet.fightId()) && ACTIVE.size() >= 4) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
        ACTIVE.put(packet.fightId(), new Boundary(packet, level.m_46467_()));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft client = Minecraft.m_91087_();
        if (client.f_91073_ == null || client.f_91074_ == null) {
            ACTIVE.clear();
            return;
        }
        long now = client.f_91073_.m_46467_();
        ACTIVE.values().removeIf(boundary -> !boundary.packet.dimension().equals((Object)client.f_91073_.m_46472_().m_135782_()) || now < boundary.refreshed || now - boundary.refreshed > 45L);
        if (client.m_91104_() || now % 4L != 0L) {
            return;
        }
        ParticleStatus status = (ParticleStatus)client.f_91066_.m_231929_().m_231551_();
        int particles = status == ParticleStatus.MINIMAL ? 2 : (status == ParticleStatus.DECREASED ? 4 : 6);
        for (Boundary boundary2 : ACTIVE.values()) {
            Entity boss = ManorRecallBoundaryVisual.boss(client, boundary2.packet);
            if (boss == null) continue;
            Vec3 center = boss.m_20182_();
            Vec3 toward = client.f_91074_.m_20182_().m_82546_(center).m_82542_(1.0, 0.0, 1.0);
            double base = toward.m_82556_() < 1.0E-6 ? (double)now * 0.08 : Math.atan2(toward.f_82481_, toward.f_82479_);
            for (int index = 0; index < particles; ++index) {
                double offset = ((double)index - (double)(particles - 1) * 0.5) * 0.105;
                double angle = base + offset + Math.sin((double)now * 0.07 + (double)index) * 0.018;
                double radius = boundary2.packet.radius();
                double height = center.f_82480_ + 0.16 + (double)(index % 3) * 0.34;
                client.f_91073_.m_7106_((ParticleOptions)(index % 3 == 0 ? GOLD_DUST : RED_DUST), center.f_82479_ + Math.cos(angle) * radius, height, center.f_82481_ + Math.sin(angle) * radius, 0.0, 0.008 + (double)(index % 2) * 0.006, 0.0);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        Minecraft client = Minecraft.m_91087_();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || client.f_91073_ == null || client.f_91074_ == null || ACTIVE.isEmpty()) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        MultiBufferSource.BufferSource buffers = client.m_91269_().m_110104_();
        VertexConsumer vertices = null;
        float partial = client.m_91104_() ? 0.0f : event.getPartialTick();
        boolean reduced = client.f_91066_.m_231929_().m_231551_() != ParticleStatus.ALL;
        for (Boundary boundary : ACTIVE.values()) {
            Entity boss = ManorRecallBoundaryVisual.boss(client, boundary.packet);
            if (boss == null) continue;
            Vec3 center = boss.m_20318_(partial);
            double radius = boundary.packet.radius();
            AABB bounds = new AABB(center.f_82479_ - radius - 1.0, center.f_82480_ - 1.0, center.f_82481_ - radius - 1.0, center.f_82479_ + radius + 1.0, center.f_82480_ + 3.0, center.f_82481_ + radius + 1.0);
            if (camera.m_82557_(center) > 12544.0 || event.getFrustum() != null && !event.getFrustum().m_113029_(bounds)) continue;
            float urgency = Math.max(boundary.packet.disengageWarning(), ManorRecallBoundaryGeometry.urgency(client.f_91074_.m_20270_(boss), radius));
            if (vertices == null) {
                vertices = buffers.m_6299_(ManorRecallBoundaryRenderType.TYPE);
            }
            VertexConsumer batch = vertices;
            PoseStack poses = event.getPoseStack();
            poses.m_85836_();
            try {
                poses.m_85837_(center.f_82479_ - camera.f_82479_, center.f_82480_ - camera.f_82480_, center.f_82481_ - camera.f_82481_);
                Matrix4f matrix = poses.m_85850_().m_252922_();
                double age = (float)client.f_91073_.m_46467_() + partial;
                ManorRecallBoundaryGeometry.draw(age, radius, reduced, urgency, (first, second, third, fourth, color, alpha) -> {
                    ManorRecallBoundaryVisual.vertex(batch, matrix, first, color, alpha);
                    ManorRecallBoundaryVisual.vertex(batch, matrix, second, color, alpha);
                    ManorRecallBoundaryVisual.vertex(batch, matrix, third, color, alpha);
                    ManorRecallBoundaryVisual.vertex(batch, matrix, fourth, color, alpha);
                });
            }
            finally {
                poses.m_85849_();
            }
        }
        if (vertices != null) {
            buffers.m_109912_(ManorRecallBoundaryRenderType.TYPE);
        }
    }

    private static Entity boss(Minecraft client, ManorRecallBoundaryPacket packet) {
        if (client.f_91073_ == null || !packet.active()) {
            return null;
        }
        Entity entity = client.f_91073_.m_6815_(packet.bossEntityId());
        return entity != null && entity.m_6084_() && packet.bossId().equals(entity.m_20148_()) ? entity : null;
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, ManorRecallBoundaryGeometry.Point point, int color, float alpha) {
        vertices.m_252986_(matrix, (float)point.x(), (float)point.y(), (float)point.z()).m_6122_(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, Math.max(0, Math.min(255, Math.round(alpha * 255.0f)))).m_5752_();
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

    private record Boundary(ManorRecallBoundaryPacket packet, long refreshed) {
    }
}

