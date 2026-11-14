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
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Matrix4f
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.render.ManorCeremonyRenderType;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.ManorAssaultPacket;
import net.farmingtales.ftboss.util.ManorAssaultGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
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

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class ManorAssaultVisual {
    private static final Map<UUID, Show> ACTIVE = new HashMap<UUID, Show>();
    private static ClientLevel world;

    private ManorAssaultVisual() {
    }

    public static void accept(ManorAssaultPacket packet) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null || !level.m_46472_().m_135782_().equals((Object)packet.dimension()) || !ManorAssaultGeometry.supports(packet.variant())) {
            return;
        }
        if (world != level) {
            ACTIVE.clear();
            world = level;
        }
        if (ACTIVE.size() < 16 || ACTIVE.containsKey(packet.owner())) {
            ACTIVE.put(packet.owner(), new Show(packet, level.m_46467_()));
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null || level != world) {
            ManorAssaultVisual.clear();
            return;
        }
        ACTIVE.values().removeIf(show -> {
            long age = level.m_46467_() - show.started;
            return age < 0L || age > (show.packet.release() ? 14L : 32L);
        });
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == world) {
            ManorAssaultVisual.clear();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        ManorAssaultVisual.clear();
    }

    private static void clear() {
        ACTIVE.clear();
        world = null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        boolean bl = SpellBloomRenderer.begin(renderLevelStageEvent, ACTIVE.isEmpty() ^ true);
        try {
            ManorAssaultVisual.ftboss$renderWithoutSoftBloom(renderLevelStageEvent);
            return;
        }
        finally {
            SpellBloomRenderer.end(bl);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void ftboss$renderWithoutSoftBloom(RenderLevelStageEvent event) {
        Minecraft client = Minecraft.m_91087_();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || client.f_91073_ == null || ACTIVE.isEmpty()) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        MultiBufferSource.BufferSource buffers = client.m_91269_().m_110104_();
        VertexConsumer batch = buffers.m_6299_(ManorCeremonyRenderType.TYPE);
        for (Show show : ACTIVE.values()) {
            EasterBossEntity boss;
            Entity owner;
            Vec3 center = show.packet.center();
            if (camera.m_82557_(center) > 9216.0 || event.getFrustum() != null && !event.getFrustum().m_113029_(new AABB(center.m_82520_(-3.4, 0.0, -3.4), center.m_82520_(3.4, 2.2, 3.4))) || !((owner = client.f_91073_.m_6815_(show.packet.ownerId())) instanceof EasterBossEntity) || !(boss = (EasterBossEntity)owner).m_20148_().equals(show.packet.owner()) || !boss.m_6084_() || boss.getVariant() != show.packet.variant() || boss.isManorIntroHidden()) continue;
            double now = (double)client.f_91073_.m_46467_() + (client.m_91104_() ? 0.0 : (double)event.getPartialTick());
            double age = now - (double)show.started;
            float alpha = show.packet.release() ? 0.98f : 0.78f;
            boolean reduced = camera.m_82557_(center) > 2304.0 || client.f_91066_.m_231929_().m_231551_() != ParticleStatus.ALL;
            PoseStack poses = event.getPoseStack();
            poses.m_85836_();
            try {
                poses.m_85837_(center.f_82479_ - camera.f_82479_, center.f_82480_ - camera.f_82480_ + 0.025, center.f_82481_ - camera.f_82481_);
                Matrix4f matrix = poses.m_85850_().m_252922_();
                ManorAssaultGeometry.draw(show.packet.variant(), age, show.packet.release(), reduced, alpha, (x, y, z, color, opacity) -> batch.m_252986_(matrix, (float)x, (float)y, (float)z).m_85950_((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f, opacity).m_5752_());
            }
            finally {
                poses.m_85849_();
            }
        }
        buffers.m_109912_(ManorCeremonyRenderType.TYPE);
    }

    private record Show(ManorAssaultPacket packet, long started) {
    }
}

