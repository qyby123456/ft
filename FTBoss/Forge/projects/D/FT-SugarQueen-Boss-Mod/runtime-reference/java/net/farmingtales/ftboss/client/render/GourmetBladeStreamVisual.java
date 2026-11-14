/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonParser
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.event.RegisterClientReloadListenersEvent
 *  net.minecraftforge.client.event.RenderHandEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.render.GourmetBladeStreamRenderType;
import net.farmingtales.ftboss.util.GourmetBladeStreamMotion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
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

public final class GourmetBladeStreamVisual {
    private static final Logger LOGGER = LoggerFactory.getLogger(GourmetBladeStreamVisual.class);
    private static final ResourceLocation ITEM = new ResourceLocation("ftboss", "gourmet_blank_blade");
    private static final ResourceLocation MODEL = new ResourceLocation("ftboss", "fx/gourmet_blade_stream.json");
    private static final RenderType GLOW = GourmetBladeStreamRenderType.TYPE;
    private static final int MAX_SWINGS = 32;
    private static final double RANGE_SQUARED = 1024.0;
    private static final Map<UUID, Swing> SWINGS = new LinkedHashMap<UUID, Swing>();
    private static final Map<UUID, Integer> PREVIOUS_SWINGS = new HashMap<UUID, Integer>();
    private static volatile Vertex[] mesh = new Vertex[0];
    private static Level currentLevel;

    private GourmetBladeStreamVisual() {
    }

    private static boolean isBlade(ItemStack itemStack) {
        return !itemStack.m_41619_() && ITEM.equals((Object)BuiltInRegistries.f_257033_.m_7981_((Object)itemStack.m_41720_()));
    }

    private static boolean eligible(Player player) {
        return player.m_6084_() && !player.m_5833_() && !player.m_20145_() && GourmetBladeStreamVisual.isBlade(player.m_21205_());
    }

    private static void useLevel(Level level) {
        if (currentLevel == level) {
            return;
        }
        SWINGS.clear();
        PREVIOUS_SWINGS.clear();
        currentLevel = level;
    }

    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        GourmetBladeStreamVisual.useLevel((Level)minecraft.f_91073_);
        if (minecraft.f_91073_ == null || minecraft.m_91104_() || minecraft.m_91288_() == null) {
            return;
        }
        long l = minecraft.f_91073_.m_46467_();
        SWINGS.values().removeIf(swing -> l - swing.start >= 8L || !GourmetBladeStreamVisual.eligible(swing.player) || swing.player.m_213877_() || swing.player.m_9236_() != currentLevel || swing.player.m_20280_(minecraft.m_91288_()) > 1024.0);
        PREVIOUS_SWINGS.keySet().removeIf(uUID -> minecraft.f_91073_.m_46003_(uUID) == null);
        for (Player player : minecraft.f_91073_.m_6907_()) {
            UUID uUID2 = player.m_20148_();
            if (!GourmetBladeStreamVisual.eligible(player) || player.m_20280_(minecraft.m_91288_()) > 1024.0) {
                PREVIOUS_SWINGS.remove(uUID2);
                continue;
            }
            boolean bl = player.f_20911_ && player.f_20912_ == InteractionHand.MAIN_HAND;
            int n = PREVIOUS_SWINGS.getOrDefault(uUID2, -1);
            if (bl && (n < 0 || player.f_20913_ < n) && (SWINGS.containsKey(uUID2) || SWINGS.size() < 32)) {
                int n2 = player.m_5737_() == HumanoidArm.RIGHT ? 1 : -1;
                SWINGS.put(uUID2, new Swing(player, l, n2));
            }
            PREVIOUS_SWINGS.put(uUID2, bl ? player.f_20913_ : -1);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void renderWorld(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        GourmetBladeStreamVisual.useLevel((Level)minecraft.f_91073_);
        if (minecraft.f_91073_ == null || mesh.length == 0 || SWINGS.isEmpty()) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        VertexConsumer vertexConsumer = bufferSource.m_6299_(GLOW);
        poseStack.m_85836_();
        try {
            poseStack.m_85837_(-vec3.f_82479_, -vec3.f_82480_, -vec3.f_82481_);
            for (Swing swing : SWINGS.values()) {
                Player player = swing.player;
                if (!GourmetBladeStreamVisual.eligible(player) || player.m_20238_(vec3) > 1024.0 || player == minecraft.m_91288_() && minecraft.f_91066_.m_92176_().m_90612_()) continue;
                Vec3 vec32 = player.m_20318_(renderLevelStageEvent.getPartialTick());
                float f = Mth.m_14189_((float)renderLevelStageEvent.getPartialTick(), (float)player.f_20884_, (float)player.f_20883_);
                poseStack.m_85836_();
                try {
                    poseStack.m_85837_(vec32.f_82479_, vec32.f_82480_ + (double)player.m_20206_() * 0.62, vec32.f_82481_);
                    poseStack.m_252781_(Axis.f_252436_.m_252977_(-f));
                    poseStack.m_85837_(-0.14 * (double)swing.side, 0.0, 0.8);
                    poseStack.m_252781_(Axis.f_252403_.m_252977_((float)(-28 * swing.side)));
                    poseStack.m_252781_(Axis.f_252436_.m_252977_(180.0f));
                    poseStack.m_85841_((float)swing.side, 0.85f, 1.0f);
                    GourmetBladeStreamVisual.draw(poseStack, vertexConsumer, (float)(minecraft.f_91073_.m_46467_() - swing.start) + renderLevelStageEvent.getPartialTick(), (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_());
                }
                finally {
                    poseStack.m_85849_();
                }
            }
        }
        finally {
            poseStack.m_85849_();
            bufferSource.m_109912_(GLOW);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void renderHand(RenderHandEvent renderHandEvent) {
        Minecraft minecraft = Minecraft.m_91087_();
        GourmetBladeStreamVisual.useLevel((Level)minecraft.f_91073_);
        if (!(minecraft.f_91073_ != null && minecraft.f_91074_ != null && mesh.length != 0 && renderHandEvent.getHand() == InteractionHand.MAIN_HAND && GourmetBladeStreamVisual.isBlade(renderHandEvent.getItemStack()) && GourmetBladeStreamVisual.eligible((Player)minecraft.f_91074_) && minecraft.m_91288_() == minecraft.f_91074_ && minecraft.f_91066_.m_92176_().m_90612_())) {
            return;
        }
        Swing swing = SWINGS.get(minecraft.f_91074_.m_20148_());
        if (swing == null) {
            return;
        }
        PoseStack poseStack = renderHandEvent.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        VertexConsumer vertexConsumer = bufferSource.m_6299_(GLOW);
        poseStack.m_85836_();
        try {
            poseStack.m_85837_(0.36 * (double)swing.side, -0.32, -1.15);
            poseStack.m_252781_(Axis.f_252403_.m_252977_(32.0f * (float)swing.side));
            poseStack.m_252781_(Axis.f_252436_.m_252977_((float)(-12 * swing.side)));
            poseStack.m_85841_(0.62f * (float)swing.side, 0.55f, 0.55f);
            GourmetBladeStreamVisual.draw(poseStack, vertexConsumer, (float)(minecraft.f_91073_.m_46467_() - swing.start) + renderHandEvent.getPartialTick(), (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_());
        }
        finally {
            poseStack.m_85849_();
            bufferSource.m_109912_(GLOW);
        }
    }

    private static void draw(PoseStack poseStack, VertexConsumer vertexConsumer, float f, ParticleStatus particleStatus) {
        Vertex[] vertexArray = mesh;
        float f2 = particleStatus == ParticleStatus.MINIMAL ? 0.45f : (particleStatus == ParticleStatus.DECREASED ? 0.65f : 0.82f);
        for (int i = 0; i < vertexArray.length; i += 4) {
            for (int j = 0; j < 4; ++j) {
                Vertex vertex = vertexArray[i + j];
                float f3 = f2 * GourmetBladeStreamMotion.opacity(f, vertex.along);
                vertexConsumer.m_252986_(poseStack.m_85850_().m_252922_(), vertex.x, vertex.y, vertex.z).m_85950_(1.0f, 1.0f, 1.0f, f3).m_7421_(vertex.along, vertex.across).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(poseStack.m_85850_().m_252943_(), 0.0f, 0.0f, 1.0f).m_5752_();
            }
        }
    }

    private static void reload(ResourceManager resourceManager) {
        mesh = new Vertex[0];
        SWINGS.clear();
        PREVIOUS_SWINGS.clear();
        try (InputStreamReader inputStreamReader = new InputStreamReader(resourceManager.m_215593_(MODEL).m_215507_(), StandardCharsets.UTF_8);){
            JsonArray jsonArray = JsonParser.parseReader((Reader)inputStreamReader).getAsJsonObject().getAsJsonArray("vertices");
            if (jsonArray.size() == 0 || jsonArray.size() % 4 != 0 || jsonArray.size() > 1024) {
                throw new IllegalArgumentException("Invalid stream quad count");
            }
            Vertex[] vertexArray = new Vertex[jsonArray.size()];
            for (int i = 0; i < vertexArray.length; ++i) {
                JsonArray jsonArray2 = jsonArray.get(i).getAsJsonArray();
                if (jsonArray2.size() != 5) {
                    throw new IllegalArgumentException("Invalid stream vertex");
                }
                float[] fArray = new float[5];
                for (int j = 0; j < fArray.length; ++j) {
                    fArray[j] = jsonArray2.get(j).getAsFloat();
                    if (Float.isFinite(fArray[j]) && !(Math.abs(fArray[j]) > 4.0f)) continue;
                    throw new IllegalArgumentException("Invalid stream coordinate");
                }
                if (fArray[3] < 0.0f || fArray[3] > 1.0f || fArray[4] < 0.0f || fArray[4] > 1.0f) {
                    throw new IllegalArgumentException("Invalid stream UV");
                }
                vertexArray[i] = new Vertex(fArray[0], fArray[1], fArray[2], fArray[3], fArray[4]);
            }
            mesh = vertexArray;
        }
        catch (Exception exception) {
            LOGGER.error("[FTBoss][BladeStream] Invalid VFX mesh; attack stream disabled", (Throwable)exception);
        }
    }

    private record Swing(Player player, long start, int side) {
    }

    private record Vertex(float x, float y, float z, float along, float across) {
    }

    public static final class Registration {
        public static void registerReload(RegisterClientReloadListenersEvent registerClientReloadListenersEvent) {
            registerClientReloadListenersEvent.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)GourmetBladeStreamVisual::reload));
        }
    }
}

