package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.farmingtales.ftboss.client.model.ManorDefenseModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.network.ManorDefenseNetwork.Snapshot;
import net.farmingtales.ftboss.util.ManorDefenseRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss",
    value = {Dist.CLIENT}
)
public final class ManorDefenseVisual {
    private static final Map<UUID, ManorDefenseVisual.Visible> ACTIVE = new LinkedHashMap<>();
    private static ClientLevel world;
    private static long lastTick;

    private ManorDefenseVisual() {
    }

    public static void accept(Snapshot packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && packet.valid() && level.dimension().location().equals(packet.dimension())) {
            if (world != level) {
                reset(level);
            }

            ManorDefenseVisual.Visible old = ACTIVE.get(packet.uuid());
            if (old == null || old.packet.sequence() < packet.sequence()) {
                if (old == null && ACTIVE.size() >= 64) {
                    ACTIVE.remove(ACTIVE.keySet().iterator().next());
                }

                ACTIVE.put(packet.uuid(), new ManorDefenseVisual.Visible(packet, level.getGameTime()));
            }
        }
    }

    private static void reset(ClientLevel level) {
        ACTIVE.clear();
        world = level;
        lastTick = level == null ? 0L : level.getGameTime();
    }

    private static boolean visible(EasterBossEntity boss, Snapshot packet) {
        return boss.getUUID().equals(packet.uuid())
            && boss.getVariant() == packet.variant()
            && boss.isAlive()
            && !boss.isRemoved()
            && !boss.isMini()
            && !boss.isInvisible()
            && !boss.isManorIntroHidden()
            && !boss.isXuanlanSkyHidden();
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        {
            ClientLevel level = Minecraft.getInstance().level;
            if (world == level && level != null && level.getGameTime() >= lastTick) {
                long now = level.getGameTime();
                lastTick = now;
                ACTIVE.values()
                    .removeIf(
                        state -> {
                            Entity entity = level.getEntity(state.packet.entityId());
                            return now - state.received > 60L
                                || entity == null && now - state.received > 5L
                                || entity != null && (!(entity instanceof EasterBossEntity boss) || !visible(boss, state.packet));
                        }
                    );
            } else {
                reset(level);
            }
        }
    }

    @SubscribeEvent
    public static void unload(Unload event) {
        if (event.getLevel() == world) {
            reset(null);
        }
    }

    @SubscribeEvent
    public static void logout(LoggingOut event) {
        reset(null);
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getStage() == Stage.AFTER_PARTICLES && world != null && minecraft.level == world) {
            Vec3 camera = event.getCamera().getPosition();
            Map<ManorDefenseVisual.Material, List<ManorDefenseVisual.Draw>> groups = new LinkedHashMap<>();

            for (ManorDefenseVisual.Visible state : ACTIVE.values()) {
                if (state.packet.phase() != 0 || state.packet.adaptiveKind() != 0) {
                    Entity model = world.getEntity(state.packet.entityId());
                    if (model instanceof EasterBossEntity) {
                        EasterBossEntity boss = (EasterBossEntity)model;
                        if (visible(boss, state.packet)) {
                            double distanceSquared = camera.distanceToSqr(boss.position());
                            if (!(distanceSquared < 6.25)
                                && !(distanceSquared > 4096.0)
                                && (event.getFrustum() == null || event.getFrustum().isVisible(boss.getBoundingBox().inflate(1.6)))) {
                                groups.computeIfAbsent(
                                        new ManorDefenseVisual.Material(state.packet.variant(), state.packet.adaptiveKind()), ignored -> new ArrayList<>()
                                    )
                                    .add(new ManorDefenseVisual.Draw(boss, state, distanceSquared));
                            }
                        }
                    }
                }
            }

            BufferSource buffers = minecraft.renderBuffers().bufferSource();

            for (Entry<ManorDefenseVisual.Material, List<ManorDefenseVisual.Draw>> group : groups.entrySet()) {
                ManorDefenseModel model = ManorDefenseModel.get(group.getKey().variant());
                if (model != null) {
                    group.getValue().sort((first, second) -> Double.compare(second.distanceSquared, first.distanceSquared));

                    for (int pass = 0; pass < 2; pass++) {
                        RenderType type = RenderType.entityTranslucent(pass == 0 ? model.texture : model.emission(group.getKey().adaptiveKind()));
                        VertexConsumer vertices = buffers.getBuffer(type);

                        for (ManorDefenseVisual.Draw draw : group.getValue()) {
                            renderOne(draw, event, camera, vertices, model, pass == 1);
                        }

                        buffers.endBatch(type);
                    }
                }
            }
        }
    }

    private static void renderOne(
        ManorDefenseVisual.Draw draw, RenderLevelStageEvent event, Vec3 camera, VertexConsumer vertices, ManorDefenseModel model, boolean glow
    ) {
        Snapshot packet = draw.visible.packet;
        float age = (float)(Math.max(0L, packet.sent() - packet.start()) + world.getGameTime() - draw.visible.received) + event.getPartialTick().getGameTimeDeltaPartialTick(false);
        int phase = ManorDefenseRules.visualPhase(packet.phase(), packet.adaptiveKind(), age, packet.end() - packet.start());
        if (phase != 0) {
            float duration = phase == 1 ? 12.0F : (float)Math.max(1L, packet.end() - packet.start());
            float progress = Mth.clamp(age / duration, 0.0F, 1.0F);
            float alpha = ManorDefenseRules.opacity(phase, progress);
            alpha *= ManorDefenseRules.smooth((float)(Math.sqrt(draw.distanceSquared) - 2.5) / 1.5F);
            if (glow) {
                alpha *= packet.adaptiveKind() != 0 ? 0.85F : (phase == 1 ? 0.28F : 0.35F);
            }

            PoseStack pose = event.getPoseStack();
            EasterBossEntity boss = draw.boss;
            float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
            pose.pushPose();
            pose.translate(
                Mth.lerp((double)partial, boss.xo, boss.getX()) - camera.x,
                Mth.lerp((double)partial, boss.yo, boss.getY()) - camera.y,
                Mth.lerp((double)partial, boss.zo, boss.getZ()) - camera.z
            );
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - Mth.rotLerp(partial, boss.yBodyRotO, boss.yBodyRot)));
            boolean reduced = draw.distanceSquared > 576.0 || Minecraft.getInstance().options.particles().get() == ParticleStatus.MINIMAL;
            int light = Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(boss, partial);
            model.render(pose, vertices, ManorDefenseRules.openness(phase, progress), alpha, reduced, glow, light);
            pose.popPose();
        }
    }

    private static record Draw(EasterBossEntity boss, ManorDefenseVisual.Visible visible, double distanceSquared) {
    }

    private static record Material(int variant, int adaptiveKind) {
    }

    private static record Visible(Snapshot packet, long received) {
    }
}
