/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
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

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.skill.YouziStatuePacket;
import net.farmingtales.ftboss.util.YouziStatueRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class YouziStatueVisual {
    private static final int PINK = 16756695;
    private static final int WHITE = 0xFFF4FF;
    private static final Map<UUID, Aura> AURAS = new HashMap<UUID, Aura>();
    private static final List<Trail> TRAILS = new ArrayList<Trail>();
    private static final MultiBufferSource.BufferSource BUFFERS = MultiBufferSource.m_109898_((BufferBuilder)new BufferBuilder(8192));
    private static ItemStack miniature = ItemStack.f_41583_;

    private YouziStatueVisual() {
    }

    public static void receive(YouziStatuePacket packet) {
        Minecraft client = Minecraft.m_91087_();
        if (client.f_91073_ == null || packet.kind() < 0 || packet.kind() > 4 || packet.duration() < 0 || packet.duration() > 120 || !YouziStatueVisual.finite(packet.origin()) || !YouziStatueVisual.finite(packet.target())) {
            return;
        }
        long now = client.f_91073_.m_46467_();
        if (packet.kind() == 2 || packet.kind() == 4) {
            if (packet.origin().m_82557_(packet.target()) > 400.0 || packet.duration() == 0) {
                return;
            }
            if (TRAILS.size() >= 64) {
                TRAILS.remove(0);
            }
            TRAILS.add(new Trail(packet.kind(), packet.origin(), packet.target(), now, packet.duration()));
            return;
        }
        Entity entity = client.f_91073_.m_6815_(packet.actorId());
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        if (packet.kind() == 3) {
            AURAS.remove(player.m_20148_());
        } else if (AURAS.size() < 32 || AURAS.containsKey(player.m_20148_())) {
            AURAS.put(player.m_20148_(), new Aura(player.m_19879_(), packet.kind(), now, packet.duration()));
        }
    }

    private static boolean finite(Vec3 vector) {
        return Double.isFinite(vector.f_82479_) && Double.isFinite(vector.f_82480_) && Double.isFinite(vector.f_82481_);
    }

    private static boolean holding(Player player) {
        return player.m_21205_().m_150930_((Item)FTBossItems.STATUE_YOUZI.get()) || player.m_21206_().m_150930_((Item)FTBossItems.STATUE_YOUZI.get());
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        Minecraft client = Minecraft.m_91087_();
        if (event.phase != TickEvent.Phase.END || client.m_91104_()) {
            return;
        }
        if (client.f_91073_ == null) {
            AURAS.clear();
            TRAILS.clear();
            return;
        }
        long now = client.f_91073_.m_46467_();
        AURAS.entrySet().removeIf(entry -> {
            Player player;
            Entity patt4547$temp;
            Aura aura = (Aura)entry.getValue();
            return now < aura.start || now - aura.start >= (long)aura.duration || !((patt4547$temp = client.f_91073_.m_6815_(aura.entityId)) instanceof Player) || !(player = (Player)patt4547$temp).m_20148_().equals(entry.getKey()) || !player.m_6084_() || !YouziStatueVisual.holding(player);
        });
        TRAILS.removeIf(trail -> now < trail.born || now - trail.born >= (long)trail.duration);
        if (client.m_91288_() == null || now % 5L != 0L) {
            return;
        }
        int budget = 16;
        for (Player player : client.f_91073_.m_6907_()) {
            if (!player.m_6084_() || player.m_5833_() || !YouziStatueVisual.holding(player) || player.m_20280_(client.m_91288_()) > 1024.0) continue;
            boolean main = player.m_21205_().m_150930_((Item)FTBossItems.STATUE_YOUZI.get());
            double side = player.m_5737_() == HumanoidArm.RIGHT == main ? 0.3 : -0.3;
            double yaw = Math.toRadians(player.f_20883_);
            Vec3 wrist = player.m_20182_().m_82520_(-Math.cos(yaw) * side - Math.sin(yaw) * 0.2, player.m_6047_() ? 0.75 : 1.0, -Math.sin(yaw) * side + Math.cos(yaw) * 0.2);
            client.f_91073_.m_7106_((ParticleOptions)ParticleTypes.f_123810_, wrist.f_82479_, wrist.f_82480_, wrist.f_82481_, 0.0, 0.012, 0.0);
            if (--budget > 0) continue;
            break;
        }
    }

    @SubscribeEvent
    public static void onUnload(LevelEvent.Unload event) {
        if (!event.getLevel().m_5776_()) {
            return;
        }
        AURAS.clear();
        TRAILS.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || AURAS.isEmpty() && TRAILS.isEmpty()) {
            return;
        }
        Minecraft client = Minecraft.m_91087_();
        if (client.f_91073_ == null) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        float partial = event.getPartialTick();
        double now = (float)client.f_91073_.m_46467_() + partial;
        PoseStack pose = event.getPoseStack();
        pose.m_85836_();
        pose.m_85837_(-camera.f_82479_, -camera.f_82480_, -camera.f_82481_);
        try {
            float fade;
            for (Aura aura : AURAS.values()) {
                Vec3 echo;
                double angle;
                int index;
                float age;
                Player player;
                Vec3 origin;
                Entity entity = client.f_91073_.m_6815_(aura.entityId);
                if (!(entity instanceof Player) || (origin = (player = (Player)entity).m_20318_(partial)).m_82557_(camera) > 2304.0 || !event.getFrustum().m_113029_(player.m_20191_().m_82400_(1.5)) || (fade = Mth.m_14036_((float)((age = (float)(now - (double)aura.start)) / 5.0f), (float)0.0f, (float)1.0f) * Mth.m_14036_((float)(((float)aura.duration - age) / 10.0f), (float)0.0f, (float)1.0f)) <= 0.0f) continue;
                boolean ward = aura.kind == 1;
                VertexConsumer vertices = BUFFERS.m_6299_(GlowRenderType.TYPE);
                Matrix4f matrix = pose.m_85850_().m_252922_();
                YouziStatueVisual.ring(vertices, matrix, origin.m_82520_(0.0, 0.07, 0.0), ward ? 1.1 : 0.75, 0.035, 16756695, fade * 0.65f);
                if (ward) {
                    YouziStatueVisual.ring(vertices, matrix, origin.m_82520_(0.0, 0.85 + Math.sin((double)age * 0.12) * 0.08, 0.0), 0.92, 0.025, 0xFFF4FF, fade * 0.7f);
                }
                int remaining = ward ? 3 : Math.max(0, 3 - YouziStatueRules.dueEchoes((long)age));
                for (index = 0; index < remaining; ++index) {
                    angle = (double)age * 0.055 + (double)index * Math.PI * 2.0 / 3.0;
                    echo = origin.m_82520_(Math.cos(angle) * 0.82, 1.05 + Math.sin(angle * 2.0) * 0.1, Math.sin(angle) * 0.82);
                    YouziStatueVisual.ring(vertices, matrix, echo.m_82520_(0.0, -0.19, 0.0), 0.23, 0.045, 16756695, fade);
                    YouziStatueVisual.star(vertices, matrix, echo.m_82520_(0.0, 0.27, 0.0), camera, 0.12, 0xFFF4FF, fade);
                }
                BUFFERS.m_109912_(GlowRenderType.TYPE);
                for (index = 0; index < remaining; ++index) {
                    angle = (double)age * 0.055 + (double)index * Math.PI * 2.0 / 3.0;
                    echo = origin.m_82520_(Math.cos(angle) * 0.82, 1.05 + Math.sin(angle * 2.0) * 0.1, Math.sin(angle) * 0.82);
                    YouziStatueVisual.renderMiniature(client, pose, echo, age, index, fade);
                }
            }
            VertexConsumer vertices = BUFFERS.m_6299_(GlowRenderType.TYPE);
            Matrix4f matrix = pose.m_85850_().m_252922_();
            for (Trail trail : TRAILS) {
                float progress = Mth.m_14036_((float)((float)(now - (double)trail.born) / (float)trail.duration), (float)0.0f, (float)1.0f);
                fade = 1.0f - progress;
                if (fade <= 0.0f || !event.getFrustum().m_113029_(new AABB(trail.start, trail.end).m_82400_(2.0))) continue;
                if (trail.kind == 4) {
                    YouziStatueVisual.ring(vertices, matrix, trail.start.m_82520_(0.0, 0.9, 0.0), 0.8 + (double)progress * 0.65, 0.08 * (double)fade, 0xFFF4FF, fade);
                    continue;
                }
                YouziStatueVisual.beam(vertices, matrix, trail.start, trail.end, camera, 0.12 * (double)fade, 16756695, fade * 0.5f);
                YouziStatueVisual.beam(vertices, matrix, trail.start, trail.end, camera, 0.027 * (double)fade, 0xFFF4FF, fade);
                YouziStatueVisual.star(vertices, matrix, trail.end, camera, 0.18 + (double)progress * 0.3, 16756695, fade);
                YouziStatueVisual.star(vertices, matrix, trail.end, camera, 0.08 + (double)progress * 0.12, 0xFFF4FF, fade);
            }
        }
        finally {
            BUFFERS.m_109911_();
            pose.m_85849_();
        }
    }

    private static void renderMiniature(Minecraft client, PoseStack pose, Vec3 position, float age, int index, float fade) {
        if (miniature.m_41619_()) {
            miniature = new ItemStack((ItemLike)FTBossItems.STATUE_YOUZI.get());
        }
        pose.m_85836_();
        pose.m_85837_(position.f_82479_, position.f_82480_, position.f_82481_);
        pose.m_252781_(Axis.f_252436_.m_252977_(age * 2.0f + (float)index * 120.0f));
        float scale = 0.36f * fade;
        pose.m_85841_(scale, scale, scale);
        client.m_91291_().m_269128_(miniature, ItemDisplayContext.NONE, 0xF000F0, OverlayTexture.f_118083_, pose, (MultiBufferSource)BUFFERS, (Level)client.f_91073_, index);
        pose.m_85849_();
    }

    private static void beam(VertexConsumer vertices, Matrix4f matrix, Vec3 start, Vec3 end, Vec3 camera, double width, int color, float opacity) {
        Vec3 direction = end.m_82546_(start).m_82541_();
        Vec3 side = direction.m_82537_(camera.m_82546_(start)).m_82541_();
        if (side.m_82556_() < 0.01) {
            side = direction.m_82537_(new Vec3(0.0, 1.0, 0.0)).m_82541_();
        }
        if (side.m_82556_() < 0.01) {
            side = new Vec3(1.0, 0.0, 0.0);
        }
        side = side.m_82490_(width);
        YouziStatueVisual.quad(vertices, matrix, start.m_82546_(side), end.m_82546_(side), end.m_82549_(side), start.m_82549_(side), color, opacity);
    }

    private static void star(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 camera, double size, int color, float opacity) {
        Vec3 forward = camera.m_82546_(center).m_82541_();
        Vec3 right = forward.m_82537_(new Vec3(0.0, 1.0, 0.0)).m_82541_();
        if (right.m_82556_() < 0.01) {
            right = new Vec3(1.0, 0.0, 0.0);
        }
        Vec3 upward = right.m_82537_(forward).m_82541_();
        YouziStatueVisual.quad(vertices, matrix, center.m_82549_(upward.m_82490_(size)), center.m_82549_(right.m_82490_(size * 0.28)), center.m_82546_(upward.m_82490_(size)), center.m_82546_(right.m_82490_(size * 0.28)), color, opacity);
        YouziStatueVisual.quad(vertices, matrix, center.m_82549_(right.m_82490_(size)), center.m_82549_(upward.m_82490_(size * 0.28)), center.m_82546_(right.m_82490_(size)), center.m_82546_(upward.m_82490_(size * 0.28)), color, opacity);
    }

    private static void ring(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius, double width, int color, float opacity) {
        for (int index = 0; index < 48; ++index) {
            double start = (double)index * Math.PI / 24.0;
            double end = (double)(index + 1) * Math.PI / 24.0;
            YouziStatueVisual.quad(vertices, matrix, center.m_82520_(Math.cos(start) * radius, 0.0, Math.sin(start) * radius), center.m_82520_(Math.cos(end) * radius, 0.0, Math.sin(end) * radius), center.m_82520_(Math.cos(end) * (radius - width), 0.0, Math.sin(end) * (radius - width)), center.m_82520_(Math.cos(start) * (radius - width), 0.0, Math.sin(start) * (radius - width)), color, opacity);
        }
    }

    private static void quad(VertexConsumer vertices, Matrix4f matrix, Vec3 first, Vec3 second, Vec3 third, Vec3 fourth, int color, float opacity) {
        for (Vec3 point : new Vec3[]{first, second, third, fourth}) {
            vertices.m_252986_(matrix, (float)point.f_82479_, (float)point.f_82480_, (float)point.f_82481_).m_6122_(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, (int)(Mth.m_14036_((float)opacity, (float)0.0f, (float)1.0f) * 255.0f)).m_5752_();
        }
    }

    private record Trail(int kind, Vec3 start, Vec3 end, long born, int duration) {
    }

    private record Aura(int entityId, int kind, long start, int duration) {
    }

    private static final class GlowRenderType
    extends RenderType {
        private static final RenderType TYPE = GlowRenderType.m_173215_((String)"ftboss_youzi_glow", (VertexFormat)DefaultVertexFormat.f_85815_, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)4096, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.m_110628_().m_173292_(f_173104_).m_110685_(f_110136_).m_110663_(f_110113_).m_110687_(f_110115_).m_110661_(f_110110_).m_110691_(false));

        private GlowRenderType() {
            super("ftboss_youzi_glow", DefaultVertexFormat.f_85815_, VertexFormat.Mode.QUADS, 4096, false, true, () -> {}, () -> {});
        }
    }
}

