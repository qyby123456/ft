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
import net.farmingtales.ftboss.network.ManorDefenseNetwork.Snapshot;
import net.farmingtales.ftboss.util.ManorDefenseRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 客户端防御表现：2026-10-13，实体绑定、深度测试、近镜头淡出；无粒子、无伤害裁决。 */
@Mod.EventBusSubscriber(modid = "ftboss", value = Dist.CLIENT)
public final class ManorDefenseVisual {
    private static final Map<UUID, Visible> ACTIVE = new LinkedHashMap<>();
    private static ClientLevel world;
    private static long lastTick;
    private record Visible(Snapshot packet, long received) {}
    private record Draw(EasterBossEntity boss, Visible visible, double distanceSquared) {}
    private record Material(int variant, int adaptiveKind) {}

    private ManorDefenseVisual() {}

    public static void accept(Snapshot packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !packet.valid() || !level.dimension().location().equals(packet.dimension())) return;
        if (world != level) reset(level);
        Visible old = ACTIVE.get(packet.uuid());
        if (old != null && old.packet.sequence() >= packet.sequence()) return;
        if (old == null && ACTIVE.size() >= ManorDefenseRules.MAX_CLIENT_STATES) ACTIVE.remove(ACTIVE.keySet().iterator().next());
        ACTIVE.put(packet.uuid(), new Visible(packet, level.getGameTime()));
    }

    private static void reset(ClientLevel level) {
        ACTIVE.clear();
        world = level;
        lastTick = level == null ? 0 : level.getGameTime();
    }

    private static boolean visible(EasterBossEntity boss, Snapshot packet) {
        return boss.getUUID().equals(packet.uuid()) && boss.getVariant() == packet.variant() && boss.isAlive()
                && !boss.isRemoved() && !boss.isMini() && !boss.isInvisible()
                && !boss.isManorIntroHidden() && !boss.isXuanlanSkyHidden();
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (world != level || level == null || level.getGameTime() < lastTick) {
            reset(level);
            return;
        }
        long now = level.getGameTime();
        lastTick = now;
        ACTIVE.values().removeIf(state -> {
            var entity = level.getEntity(state.packet.entityId());
            return now - state.received > ManorDefenseRules.CLIENT_TIMEOUT_TICKS
                    || entity == null && now - state.received > 5
                    || entity != null && (!(entity instanceof EasterBossEntity boss) || !visible(boss, state.packet));
        });
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { if (event.getLevel() == world) reset(null); }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { reset(null); }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || world == null || minecraft.level != world) return;
        Vec3 camera = event.getCamera().getPosition();
        Map<Material, List<Draw>> groups = new LinkedHashMap<>();
        for (Visible state : ACTIVE.values()) {
            if (state.packet.phase() == ManorDefenseRules.IDLE && state.packet.adaptiveKind() == 0) continue;
            if (!(world.getEntity(state.packet.entityId()) instanceof EasterBossEntity boss) || !visible(boss, state.packet)) continue;
            double distanceSquared = camera.distanceToSqr(boss.position());
            if (distanceSquared < 2.5 * 2.5 || distanceSquared > 64 * 64
                    || event.getFrustum() != null && !event.getFrustum().isVisible(boss.getBoundingBox().inflate(1.6))) continue;
            groups.computeIfAbsent(new Material(state.packet.variant(), state.packet.adaptiveKind()), ignored -> new ArrayList<>())
                    .add(new Draw(boss, state, distanceSquared));
        }
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        for (var group : groups.entrySet()) {
            ManorDefenseModel model = ManorDefenseModel.get(group.getKey().variant());
            if (model == null) continue;
            group.getValue().sort((first, second) -> Double.compare(second.distanceSquared, first.distanceSquared));
            for (int pass = 0; pass < 2; pass++) {
                RenderType type = RenderType.entityTranslucent(pass == 0 ? model.texture : model.emission(group.getKey().adaptiveKind()));
                VertexConsumer vertices = buffers.getBuffer(type);
                for (Draw draw : group.getValue()) renderOne(draw, event, camera, vertices, model, pass == 1);
                buffers.endBatch(type);
            }
        }
    }

    private static void renderOne(Draw draw, RenderLevelStageEvent event, Vec3 camera, VertexConsumer vertices,
                                  ManorDefenseModel model, boolean glow) {
        Snapshot packet = draw.visible.packet;
        float age = Math.max(0, packet.sent() - packet.start()) + world.getGameTime() - draw.visible.received + event.getPartialTick();
        int phase = ManorDefenseRules.visualPhase(packet.phase(), packet.adaptiveKind(), age, packet.end() - packet.start());
        if (phase == ManorDefenseRules.IDLE) return;
        float duration = phase == ManorDefenseRules.WARNING ? ManorDefenseRules.WARNING_TICKS : Math.max(1, packet.end() - packet.start());
        float progress = Mth.clamp(age / duration, 0, 1);
        float alpha = ManorDefenseRules.opacity(phase, progress);
        alpha *= ManorDefenseRules.smooth((float) (Math.sqrt(draw.distanceSquared) - 2.5) / 1.5F);
        if (glow) alpha *= packet.adaptiveKind() != 0 ? 0.85F : phase == ManorDefenseRules.WARNING ? 0.28F : 0.35F;
        PoseStack pose = event.getPoseStack();
        EasterBossEntity boss = draw.boss;
        float partial = event.getPartialTick();
        pose.pushPose();
        pose.translate(Mth.lerp(partial, boss.xo, boss.getX()) - camera.x,
                Mth.lerp(partial, boss.yo, boss.getY()) - camera.y, Mth.lerp(partial, boss.zo, boss.getZ()) - camera.z);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - Mth.rotLerp(partial, boss.yBodyRotO, boss.yBodyRot)));
        boolean reduced = draw.distanceSquared > 24 * 24 || Minecraft.getInstance().options.particles().get() == ParticleStatus.MINIMAL;
        int light = Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(boss, partial);
        model.render(pose, vertices, ManorDefenseRules.openness(phase, progress), alpha, reduced, glow, light);
        pose.popPose();
    }
}
