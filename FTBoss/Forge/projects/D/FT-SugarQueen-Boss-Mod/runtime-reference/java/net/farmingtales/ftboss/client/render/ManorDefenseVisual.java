/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
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
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.model.ManorDefenseModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.network.ManorDefenseNetwork;
import net.farmingtales.ftboss.util.ManorDefenseRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class ManorDefenseVisual {
    private static final Map<UUID, Visible> ACTIVE = new LinkedHashMap<UUID, Visible>();
    private static ClientLevel world;
    private static long lastTick;

    private ManorDefenseVisual() {
    }

    public static void accept(ManorDefenseNetwork.Snapshot packet) {
        Visible old;
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null || !packet.valid() || !level.m_46472_().m_135782_().equals((Object)packet.dimension())) {
            return;
        }
        if (world != level) {
            ManorDefenseVisual.reset(level);
        }
        if ((old = ACTIVE.get(packet.uuid())) != null && old.packet.sequence() >= packet.sequence()) {
            return;
        }
        if (old == null && ACTIVE.size() >= 64) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
        ACTIVE.put(packet.uuid(), new Visible(packet, level.m_46467_()));
    }

    private static void reset(ClientLevel level) {
        ACTIVE.clear();
        world = level;
        lastTick = level == null ? 0L : level.m_46467_();
    }

    private static boolean visible(EasterBossEntity boss, ManorDefenseNetwork.Snapshot packet) {
        return boss.m_20148_().equals(packet.uuid()) && boss.getVariant() == packet.variant() && boss.m_6084_() && !boss.m_213877_() && !boss.isMini() && !boss.m_20145_() && !boss.isManorIntroHidden() && !boss.isXuanlanSkyHidden();
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        long now;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (world != level || level == null || level.m_46467_() < lastTick) {
            ManorDefenseVisual.reset(level);
            return;
        }
        lastTick = now = level.m_46467_();
        ACTIVE.values().removeIf(state -> {
            EasterBossEntity boss;
            Entity entity = level.m_6815_(state.packet.entityId());
            return now - state.received > 60L || entity == null && now - state.received > 5L || entity != null && (!(entity instanceof EasterBossEntity) || !ManorDefenseVisual.visible(boss = (EasterBossEntity)entity, state.packet));
        });
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == world) {
            ManorDefenseVisual.reset(null);
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        ManorDefenseVisual.reset(null);
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || world == null || minecraft.f_91073_ != world) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        LinkedHashMap<Material, List> groups = new LinkedHashMap<Material, List>();
        for (Visible state : ACTIVE.values()) {
            double distanceSquared;
            EasterBossEntity boss;
            Entity entity;
            if (state.packet.phase() == 0 && state.packet.adaptiveKind() == 0 || !((entity = world.m_6815_(state.packet.entityId())) instanceof EasterBossEntity) || !ManorDefenseVisual.visible(boss = (EasterBossEntity)entity, state.packet) || (distanceSquared = camera.m_82557_(boss.m_20182_())) < 6.25 || distanceSquared > 4096.0 || event.getFrustum() != null && !event.getFrustum().m_113029_(boss.m_20191_().m_82400_(1.6))) continue;
            groups.computeIfAbsent(new Material(state.packet.variant(), state.packet.adaptiveKind()), ignored -> new ArrayList()).add(new Draw(boss, state, distanceSquared));
        }
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        for (Map.Entry group : groups.entrySet()) {
            ManorDefenseModel model = ManorDefenseModel.get(((Material)group.getKey()).variant());
            if (model == null) continue;
            ((List)group.getValue()).sort((first, second) -> Double.compare(second.distanceSquared, first.distanceSquared));
            for (int pass = 0; pass < 2; ++pass) {
                RenderType type = RenderType.m_110473_((ResourceLocation)(pass == 0 ? model.texture : model.emission(((Material)group.getKey()).adaptiveKind())));
                VertexConsumer vertices = buffers.m_6299_(type);
                for (Draw draw : (List)group.getValue()) {
                    ManorDefenseVisual.renderOne(draw, event, camera, vertices, model, pass == 1);
                }
                buffers.m_109912_(type);
            }
        }
    }

    private static void renderOne(Draw draw, RenderLevelStageEvent event, Vec3 camera, VertexConsumer vertices, ManorDefenseModel model, boolean glow) {
        ManorDefenseNetwork.Snapshot packet = draw.visible.packet;
        float age = (float)(Math.max(0L, packet.sent() - packet.start()) + world.m_46467_() - draw.visible.received) + event.getPartialTick();
        int phase = ManorDefenseRules.visualPhase(packet.phase(), packet.adaptiveKind(), age, packet.end() - packet.start());
        if (phase == 0) {
            return;
        }
        float duration = phase == 1 ? 12.0f : (float)Math.max(1L, packet.end() - packet.start());
        float progress = Mth.m_14036_((float)(age / duration), (float)0.0f, (float)1.0f);
        float alpha = ManorDefenseRules.opacity(phase, progress);
        alpha *= ManorDefenseRules.smooth((float)(Math.sqrt(draw.distanceSquared) - 2.5) / 1.5f);
        if (glow) {
            alpha *= packet.adaptiveKind() != 0 ? 0.85f : (phase == 1 ? 0.28f : 0.35f);
        }
        PoseStack pose = event.getPoseStack();
        EasterBossEntity boss = draw.boss;
        float partial = event.getPartialTick();
        pose.m_85836_();
        pose.m_85837_(Mth.m_14139_((double)partial, (double)boss.f_19854_, (double)boss.m_20185_()) - camera.f_82479_, Mth.m_14139_((double)partial, (double)boss.f_19855_, (double)boss.m_20186_()) - camera.f_82480_, Mth.m_14139_((double)partial, (double)boss.f_19856_, (double)boss.m_20189_()) - camera.f_82481_);
        pose.m_252781_(Axis.f_252436_.m_252977_(180.0f - Mth.m_14189_((float)partial, (float)boss.f_20884_, (float)boss.f_20883_)));
        boolean reduced = draw.distanceSquared > 576.0 || Minecraft.m_91087_().f_91066_.m_231929_().m_231551_() == ParticleStatus.MINIMAL;
        int light = Minecraft.m_91087_().m_91290_().m_114394_((Entity)boss, partial);
        model.render(pose, vertices, ManorDefenseRules.openness(phase, progress), alpha, reduced, glow, light);
        pose.m_85849_();
    }

    private record Visible(ManorDefenseNetwork.Snapshot packet, long received) {
    }

    private record Material(int variant, int adaptiveKind) {
    }

    private record Draw(EasterBossEntity boss, Visible visible, double distanceSquared) {
    }
}

