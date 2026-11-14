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
import net.farmingtales.ftboss.client.render.ManorDomainRenderType;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.ManorDomainPacket;
import net.farmingtales.ftboss.util.ManorCeremonyModel;
import net.farmingtales.ftboss.util.ManorDomainExpansion;
import net.farmingtales.ftboss.util.ManorDomainGeometryV2;
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
public final class ManorDomainVisual {
    private static final Map<UUID, Show> ACTIVE = new HashMap<UUID, Show>();
    private static final Map<UUID, Show> RECENT = new HashMap<UUID, Show>();
    private static ClientLevel world;

    private ManorDomainVisual() {
    }

    public static void accept(ManorDomainPacket manorDomainPacket) {
        long l;
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !clientLevel.m_46472_().m_135782_().equals((Object)manorDomainPacket.dimension())) {
            return;
        }
        if (world != clientLevel) {
            ManorDomainVisual.clear();
            world = clientLevel;
        }
        if (ManorDomainGeometryV2.model(manorDomainPacket.variant()) == null) {
            ACTIVE.remove(manorDomainPacket.owner());
            return;
        }
        long l2 = clientLevel.m_46467_();
        Show show = ACTIVE.get(manorDomainPacket.owner());
        if (show == null) {
            show = RECENT.get(manorDomainPacket.owner());
        }
        long l3 = l = show == null || show.packet.variant() != manorDomainPacket.variant() || l2 < show.updated || l2 - show.updated > 1200L ? l2 : show.started;
        if (ACTIVE.size() < 16 || ACTIVE.containsKey(manorDomainPacket.owner())) {
            Show show2 = new Show(manorDomainPacket, l, l2);
            ACTIVE.put(manorDomainPacket.owner(), show2);
            if (RECENT.size() < 64 || show != null) {
                RECENT.put(manorDomainPacket.owner(), show2);
            }
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel != world || clientLevel == null) {
            ManorDomainVisual.clear();
            return;
        }
        long l = clientLevel.m_46467_();
        ACTIVE.values().removeIf(show -> l < show.updated || l - show.updated > 65L);
        RECENT.values().removeIf(show -> l < show.updated || l - show.updated > 1200L);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == world) {
            ManorDomainVisual.clear();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        ManorDomainVisual.clear();
    }

    private static void clear() {
        ACTIVE.clear();
        RECENT.clear();
        world = null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        boolean bl = SpellBloomRenderer.begin(renderLevelStageEvent, ACTIVE.isEmpty() ^ true);
        try {
            ManorDomainVisual.ftboss$renderWithoutSoftBloom(renderLevelStageEvent);
            return;
        }
        finally {
            SpellBloomRenderer.end(bl);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void ftboss$renderWithoutSoftBloom(RenderLevelStageEvent renderLevelStageEvent) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || minecraft.f_91073_ == null || ACTIVE.isEmpty()) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        double d4 = (double)minecraft.f_91073_.m_46467_() + (minecraft.m_91104_() ? 0.0 : (double)renderLevelStageEvent.getPartialTick());
        boolean bl = minecraft.f_91066_.m_231929_().m_231551_() != ParticleStatus.ALL;
        for (int i = 0; i < 2; ++i) {
            boolean bl2 = i == 1;
            String string = bl2 ? "glow" : "solid";
            VertexConsumer vertexConsumer = bufferSource.m_6299_(bl2 ? ManorDomainRenderType.GLOW : ManorDomainRenderType.SOLID);
            for (Show show : ACTIVE.values()) {
                ManorDomainGeometryV2.Model model;
                EasterBossEntity easterBossEntity;
                Entity entity;
                Vec3 vec32 = show.packet.center();
                double d5 = vec3.m_82557_(vec32);
                double d6 = vec3.f_82479_ - vec32.f_82479_;
                double d7 = vec3.f_82480_ - vec32.f_82480_;
                double d8 = vec3.f_82481_ - vec32.f_82481_;
                double d9 = 81.06666666666666;
                double d10 = Math.max(0.0, Math.hypot(d6, d8) - d9);
                if (d10 * d10 + d7 * d7 > 9216.0 || renderLevelStageEvent.getFrustum() != null && !renderLevelStageEvent.getFrustum().m_113029_(new AABB(vec32.m_82520_(-d9, 0.0, -d9), vec32.m_82520_(d9, 2.5, d9))) || !((entity = minecraft.f_91073_.m_6815_(show.packet.ownerId())) instanceof EasterBossEntity) || !(easterBossEntity = (EasterBossEntity)entity).m_20148_().equals(show.packet.owner()) || !easterBossEntity.m_6084_() || easterBossEntity.getVariant() != show.packet.variant() || easterBossEntity.isManorIntroHidden() || (model = ManorDomainGeometryV2.model(show.packet.variant())) == null) continue;
                double d11 = d4 - (double)show.started;
                float f2 = (float)(ManorDomainExpansion.smooth(d11 / 6.0) * (1.0 - ManorDomainExpansion.smooth((d4 - (double)show.updated - 40.0) / 25.0)));
                float f3 = f2 * (show.packet.supported() ? 0.96f : 0.42f);
                if (f3 <= 0.001f) continue;
                PoseStack poseStack = renderLevelStageEvent.getPoseStack();
                poseStack.m_85836_();
                try {
                    poseStack.m_85837_(vec32.f_82479_ - vec3.f_82479_, vec32.f_82480_ - vec3.f_82480_ + 0.03, vec32.f_82481_ - vec3.f_82481_);
                    poseStack.m_85841_(8.444445f, 1.0f, 8.444445f);
                    Matrix4f matrix4f = poseStack.m_85850_().m_252922_();
                    ManorCeremonyModel.Sink sink = (d, d2, d3, n, f) -> vertexConsumer.m_252986_(matrix4f, (float)d, (float)d2, (float)d3).m_85950_((float)(n >> 16 & 0xFF) / 255.0f, (float)(n >> 8 & 0xFF) / 255.0f, (float)(n & 0xFF) / 255.0f, f).m_5752_();
                    boolean bl3 = d5 > 2304.0 || bl;
                    ManorDomainGeometryV2.emit(model, string, d11, bl3, f3, sink);
                    if (!bl2) continue;
                    ManorDomainExpansion.emit(model.variant, model.glow, model.accent, d11, bl3, f3, sink);
                }
                finally {
                    poseStack.m_85849_();
                }
            }
            bufferSource.m_109912_(bl2 ? ManorDomainRenderType.GLOW : ManorDomainRenderType.SOLID);
        }
    }

    private record Show(ManorDomainPacket packet, long started, long updated) {
    }
}

