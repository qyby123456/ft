package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.util.GourmetBladeStreamMotion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 万味胚刃旧流光实现；2026-10-20 停止注册所有事件与资源重载，保留武器模型和战斗逻辑。 */
public final class GourmetBladeStreamVisual {
    private static final Logger LOGGER = LoggerFactory.getLogger(GourmetBladeStreamVisual.class);
    private static final ResourceLocation ITEM = new ResourceLocation("ftboss", "gourmet_blank_blade");
    private static final ResourceLocation MODEL = new ResourceLocation("ftboss", "fx/gourmet_blade_stream.json");
    private static final RenderType GLOW = GourmetBladeStreamRenderType.TYPE;
    private static final int MAX_SWINGS = 32;
    private static final double RANGE_SQUARED = 32 * 32;
    private static final Map<UUID, Swing> SWINGS = new LinkedHashMap<>();
    private static final Map<UUID, Integer> PREVIOUS_SWINGS = new HashMap<>();
    private static volatile Vertex[] mesh = new Vertex[0];
    private static Level currentLevel;

    private record Vertex(float x, float y, float z, float along, float across) {}
    private record Swing(Player player, long start, int side) {}

    private GourmetBladeStreamVisual() {}

    private static boolean isBlade(ItemStack stack) {
        return !stack.isEmpty() && ITEM.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static boolean eligible(Player player) {
        return player.isAlive() && !player.isSpectator() && !player.isInvisible() && isBlade(player.getMainHandItem());
    }

    private static void useLevel(Level level) {
        if (currentLevel == level) return;
        SWINGS.clear();
        PREVIOUS_SWINGS.clear();
        currentLevel = level;
    }

    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        useLevel(client.level);
        if (client.level == null || client.isPaused() || client.getCameraEntity() == null) return;
        long now = client.level.getGameTime();
        SWINGS.values().removeIf(swing -> now - swing.start >= GourmetBladeStreamMotion.LIFE_TICKS
                || !eligible(swing.player) || swing.player.isRemoved() || swing.player.level() != currentLevel
                || swing.player.distanceToSqr(client.getCameraEntity()) > RANGE_SQUARED);
        PREVIOUS_SWINGS.keySet().removeIf(uuid -> client.level.getPlayerByUUID(uuid) == null);
        for (Player player : client.level.players()) {
            UUID uuid = player.getUUID();
            if (!eligible(player) || player.distanceToSqr(client.getCameraEntity()) > RANGE_SQUARED) {
                PREVIOUS_SWINGS.remove(uuid);
                continue;
            }
            boolean swinging = player.swinging && player.swingingArm == InteractionHand.MAIN_HAND;
            int previous = PREVIOUS_SWINGS.getOrDefault(uuid, -1);
            if (swinging && (previous < 0 || player.swingTime < previous) && (SWINGS.containsKey(uuid) || SWINGS.size() < MAX_SWINGS)) {
                int side = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
                SWINGS.put(uuid, new Swing(player, now, side));
            }
            PREVIOUS_SWINGS.put(uuid, swinging ? player.swingTime : -1);
        }
    }

    public static void renderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft client = Minecraft.getInstance();
        useLevel(client.level);
        if (client.level == null || mesh.length == 0 || SWINGS.isEmpty()) return;
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        var buffers = client.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(GLOW);
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            for (Swing swing : SWINGS.values()) {
                Player player = swing.player;
                if (!eligible(player) || player.distanceToSqr(camera) > RANGE_SQUARED) continue;
                if (player == client.getCameraEntity() && client.options.getCameraType().isFirstPerson()) continue;
                Vec3 position = player.getPosition(event.getPartialTick());
                float yaw = net.minecraft.util.Mth.rotLerp(event.getPartialTick(), player.yBodyRotO, player.yBodyRot);
                pose.pushPose();
                try {
                    pose.translate(position.x, position.y + player.getBbHeight() * 0.62, position.z);
                    pose.mulPose(Axis.YP.rotationDegrees(-yaw));
                    pose.translate(-0.14 * swing.side, 0, 0.8);
                    pose.mulPose(Axis.ZP.rotationDegrees(-28 * swing.side));
                    pose.mulPose(Axis.YP.rotationDegrees(GourmetBladeStreamMotion.WORLD_LOCAL_YAW_DEGREES));
                    pose.scale(swing.side, 0.85F, 1);
                    draw(pose, vertices, client.level.getGameTime() - swing.start + event.getPartialTick(), client.options.particles().get());
                } finally {
                    pose.popPose();
                }
            }
        } finally {
            pose.popPose();
            buffers.endBatch(GLOW);
        }
    }

    public static void renderHand(RenderHandEvent event) {
        Minecraft client = Minecraft.getInstance();
        useLevel(client.level);
        if (client.level == null || client.player == null || mesh.length == 0 || event.getHand() != InteractionHand.MAIN_HAND
                || !isBlade(event.getItemStack()) || !eligible(client.player) || client.getCameraEntity() != client.player
                || !client.options.getCameraType().isFirstPerson()) return;
        Swing swing = SWINGS.get(client.player.getUUID());
        if (swing == null) return;
        PoseStack pose = event.getPoseStack();
        var buffers = client.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(GLOW);
        pose.pushPose();
        try {
            pose.translate(0.36 * swing.side, -0.32, -1.15);
            pose.mulPose(Axis.ZP.rotationDegrees(GourmetBladeStreamMotion.FIRST_PERSON_ROLL_DEGREES * swing.side));
            pose.mulPose(Axis.YP.rotationDegrees(-12 * swing.side));
            pose.scale(0.62F * swing.side, 0.55F, 0.55F);
            draw(pose, vertices, client.level.getGameTime() - swing.start + event.getPartialTick(), client.options.particles().get());
        } finally {
            pose.popPose();
            buffers.endBatch(GLOW);
        }
    }

    private static void draw(PoseStack pose, VertexConsumer vertices, float age, ParticleStatus setting) {
        Vertex[] snapshot = mesh;
        float strength = setting == ParticleStatus.MINIMAL ? 0.45F : setting == ParticleStatus.DECREASED ? 0.65F : 0.82F;
        for (int offset = 0; offset < snapshot.length; offset += 4) {
            for (int corner = 0; corner < 4; corner++) {
                Vertex vertex = snapshot[offset + corner];
                float alpha = strength * GourmetBladeStreamMotion.opacity(age, vertex.along);
                vertices.vertex(pose.last().pose(), vertex.x, vertex.y, vertex.z).color(1, 1, 1, alpha)
                        .uv(vertex.along, vertex.across).overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(LightTexture.FULL_BRIGHT).normal(pose.last().normal(), 0, 0, 1).endVertex();
            }
        }
    }

    private static void reload(ResourceManager resources) {
        mesh = new Vertex[0];
        SWINGS.clear();
        PREVIOUS_SWINGS.clear();
        try (var reader = new InputStreamReader(resources.getResourceOrThrow(MODEL).open(), StandardCharsets.UTF_8)) {
            JsonArray values = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("vertices");
            if (values.size() == 0 || values.size() % 4 != 0 || values.size() > 1024) throw new IllegalArgumentException("Invalid stream quad count");
            Vertex[] loaded = new Vertex[values.size()];
            for (int index = 0; index < loaded.length; index++) {
                JsonArray fields = values.get(index).getAsJsonArray();
                if (fields.size() != 5) throw new IllegalArgumentException("Invalid stream vertex");
                float[] numbers = new float[5];
                for (int field = 0; field < numbers.length; field++) {
                    numbers[field] = fields.get(field).getAsFloat();
                    if (!Float.isFinite(numbers[field]) || Math.abs(numbers[field]) > 4) throw new IllegalArgumentException("Invalid stream coordinate");
                }
                if (numbers[3] < 0 || numbers[3] > 1 || numbers[4] < 0 || numbers[4] > 1) throw new IllegalArgumentException("Invalid stream UV");
                loaded[index] = new Vertex(numbers[0], numbers[1], numbers[2], numbers[3], numbers[4]);
            }
            mesh = loaded;
        } catch (Exception exception) {
            LOGGER.error("[FTBoss][BladeStream] Invalid VFX mesh; attack stream disabled", exception);
        }
    }

    public static final class Registration {
        public static void registerReload(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) GourmetBladeStreamVisual::reload);
        }
    }
}
