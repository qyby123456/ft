/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
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
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package net.farmingtales.ftboss.client.xuanlannative;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.CombatVisualEntities;
import net.farmingtales.ftboss.client.xuanlannative.NativeDraw;
import net.farmingtales.ftboss.client.xuanlannative.NativeFacing;
import net.farmingtales.ftboss.client.xuanlannative.NativeMotion;
import net.farmingtales.ftboss.client.xuanlannative.NativeWheel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.farmingtales.ftboss.entity.XuanlanEliteFlywheels;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class NativeScene {
    private static final ArrayList<Moment> MOMENTS = new ArrayList();
    private static final Map<Integer, Trail> TRAILS = new LinkedHashMap<Integer, Trail>();
    private static final ResourceLocation ATLAS = new ResourceLocation("ftboss", "xuanlan_native/material.png");
    private static final RenderType INK = RenderType.m_110473_((ResourceLocation)ATLAS);
    private static ClientLevel current;
    private static Vec3 renderOrigin;

    private NativeScene() {
    }

    private static void use(ClientLevel clientLevel) {
        if (current != clientLevel) {
            MOMENTS.clear();
            TRAILS.clear();
            current = clientLevel;
        }
    }

    public static boolean ownsProjectile(Entity entity) {
        return entity instanceof FlywheelProjectileEntity;
    }

    public static boolean ownsCue(EasterBossEntity easterBossEntity) {
        CompoundTag compoundTag = easterBossEntity.getSkillCue();
        return easterBossEntity.getVariant() == 6 && !easterBossEntity.isMini() && !compoundTag.m_128456_() && (compoundTag.m_128471_("beam") || compoundTag.m_128441_("xuanlanNative"));
    }

    public static boolean accept(XuanlanFxPacket xuanlanFxPacket) {
        if (xuanlanFxPacket.kind() < 20 || xuanlanFxPacket.kind() > 26) {
            return false;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        NativeScene.use(clientLevel);
        if (clientLevel == null || !clientLevel.m_46472_().m_135782_().equals((Object)xuanlanFxPacket.dimension())) {
            return true;
        }
        if (xuanlanFxPacket.kind() == 24) {
            MOMENTS.removeIf(moment -> moment.packet.entityId() == xuanlanFxPacket.entityId() && moment.packet.kind() >= 20 && moment.packet.kind() <= 23);
            return true;
        }
        if (xuanlanFxPacket.from().m_82557_(xuanlanFxPacket.to()) > 16384.0 || !NativeScene.finite(xuanlanFxPacket.from()) || !NativeScene.finite(xuanlanFxPacket.to())) {
            return true;
        }
        int n = xuanlanFxPacket.kind() == 21 ? 10 : (xuanlanFxPacket.kind() == 20 || xuanlanFxPacket.kind() == 23 ? 20 : (xuanlanFxPacket.kind() == 22 ? 14 : 12));
        Entity entity = clientLevel.m_6815_(xuanlanFxPacket.entityId());
        if (MOMENTS.size() >= 64) {
            MOMENTS.remove(0);
        }
        MOMENTS.add(new Moment(xuanlanFxPacket, entity == null ? null : entity.m_20148_(), clientLevel.m_46467_(), n));
        return true;
    }

    private static boolean finite(Vec3 vec3) {
        return Double.isFinite(vec3.f_82479_) && Double.isFinite(vec3.f_82480_) && Double.isFinite(vec3.f_82481_);
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        NativeScene.use(null);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == current) {
            NativeScene.use(null);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        NativeScene.use(clientLevel);
        if (clientLevel == null || minecraft.m_91104_()) {
            return;
        }
        long l = clientLevel.m_46467_();
        MOMENTS.removeIf(moment -> l < moment.born || l - moment.born >= (long)moment.life || NativeScene.invalidOwner(clientLevel, moment));
        TRAILS.values().removeIf(trail -> trail.entity.m_213877_() || clientLevel.m_6815_(trail.entity.m_19879_()) != trail.entity || trail.entity.m_9236_() != clientLevel);
        Entity entity = minecraft.m_91288_();
        if (entity == null) {
            return;
        }
        TRAILS.values().removeIf(trail -> trail.entity.m_20280_(entity) > 2304.0);
        for (Trail trail2 : TRAILS.values()) {
            while (!trail2.points.isEmpty() && l - trail2.points.getFirst().tick >= 9L) {
                trail2.points.removeFirst();
            }
        }
        for (Entity entity2 : CombatVisualEntities.get(clientLevel).flywheels()) {
            if (!NativeScene.ownsProjectile(entity2) || entity2.m_213877_() || entity2.m_20280_(entity) > 2304.0) continue;
            Trail trail2 = TRAILS.get(entity2.m_19879_());
            if (trail2 == null || trail2.entity != entity2) {
                if (TRAILS.size() >= 32) continue;
                trail2 = new Trail(entity2);
                TRAILS.put(entity2.m_19879_(), trail2);
            }
            Vec3 vec3 = entity2.m_20182_();
            if (!trail2.points.isEmpty() && (l <= trail2.points.getLast().tick || vec3.m_82557_(trail2.points.getLast().position) > 64.0)) {
                trail2.points.clear();
            }
            trail2.points.addLast(new Point(vec3, l));
            while (trail2.points.size() > 9) {
                trail2.points.removeFirst();
            }
        }
    }

    private static boolean invalidOwner(ClientLevel clientLevel, Moment moment) {
        Entity entity = clientLevel.m_6815_(moment.packet.entityId());
        return entity != null && moment.owner != null && (!entity.m_20148_().equals(moment.owner) || !entity.m_6084_());
    }

    private static Matrix4f frame(Vec3 vec3, Vec3 vec32, double d, double d2, double d3) {
        Vec3 vec33 = vec32.m_82556_() < 1.0E-10 ? new Vec3(0.0, 0.0, 1.0) : vec32.m_82541_();
        return NativeScene.location(vec3).rotate((Quaternionfc)NativeFacing.orientPositiveZ(new Quaternionf(), 0.0f, 0.0f, 1.0f, (float)vec33.f_82479_, (float)vec33.f_82480_, (float)vec33.f_82481_)).scale((float)d, (float)d2, (float)d3);
    }

    private static Matrix4f location(Vec3 vec3) {
        Vec3 vec32 = vec3.m_82546_(renderOrigin);
        return new Matrix4f().translation((float)vec32.f_82479_, (float)vec32.f_82480_, (float)vec32.f_82481_);
    }

    private static Matrix4f ground(Vec3 vec3, double d) {
        return NativeScene.location(vec3).scale((float)d, 1.0f, (float)d);
    }

    /*
     * WARNING - void declaration
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        int n;
        double d;
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        NativeScene.use(clientLevel);
        if (clientLevel == null) {
            return;
        }
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n2 = particleStatus == ParticleStatus.MINIMAL ? 4000 : (particleStatus == ParticleStatus.DECREASED ? 8000 : 14000);
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        VertexConsumer vertexConsumer = bufferSource.m_6299_(INK);
        float f = minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick();
        renderOrigin = vec3;
        int n3 = MOMENTS.stream().anyMatch(moment -> moment.packet.kind() == 21) ? Math.min(n2, 1200) : 0;
        for (EasterBossEntity object2 : CombatVisualEntities.get(clientLevel).hiddenBosses()) {
            Matrix4f matrix4f;
            Matrix4f matrix4f2;
            String string;
            boolean bl2;
            if (n2 <= n3) break;
            if (!NativeScene.ownsCue(object2) || !object2.m_6084_() || object2.m_20238_(vec3) > 9216.0) continue;
            CompoundTag compoundTag = object2.getSkillCue();
            double d2 = (float)clientLevel.m_46467_() + f - (float)compoundTag.m_128454_("start");
            int n4 = compoundTag.m_128451_("duration");
            if (d2 < 0.0 || n4 < 1 || d2 >= (double)n4) continue;
            Vec3 vec32 = new Vec3(compoundTag.m_128459_("x"), compoundTag.m_128459_("y"), compoundTag.m_128459_("z"));
            boolean bl3 = bl2 = bl && minecraft.m_91288_() == object2;
            if (compoundTag.m_128471_("beam")) {
                string = new Vec3(compoundTag.m_128459_("endX"), compoundTag.m_128459_("endY"), compoundTag.m_128459_("endZ"));
                Vec3 vec33 = string.m_82546_(vec32);
                d = Math.min(128.0, vec33.m_82553_());
                double d3 = Math.max(0.1, Math.min(16.0, compoundTag.m_128459_("radius")));
                if (d < 0.05) continue;
                double d4 = Math.min(0.8, d * 0.2);
                matrix4f2 = NativeScene.frame(vec32, vec33, d3, d3, d);
                matrix4f = NativeScene.frame(vec32.m_82549_(vec33.m_82541_().m_82490_(d4)), vec33, d3, d3, d - d4);
                n2 -= NativeDraw.model(compoundTag.m_128471_("firing") ? "beam_fire" : "beam_lock", matrix4f2, matrix4f, d2, n4, compoundTag.m_128471_("released") ? NativeMotion.fade(d2, n4) : 1.0f, bl2, bl, Vec3.f_82478_, renderLevelStageEvent.getPoseStack(), vertexConsumer, n2 - n3);
                continue;
            }
            string = compoundTag.m_128461_("xuanlanNative");
            double d5 = compoundTag.m_128459_("radius");
            int n5 = compoundTag.m_128451_("windup");
            int n6 = Math.max(1, compoundTag.m_128451_("interval"));
            int n7 = Math.max(1, Math.min(8, compoundTag.m_128451_("pulses")));
            if (string.equals("xuanlan_rail")) {
                boolean bl4;
                Vec3 vec34 = new Vec3(compoundTag.m_128459_("directionX"), 0.0, compoundTag.m_128459_("directionZ"));
                Vec3 vec35 = vec32.m_82520_(0.0, 1.0, 0.0);
                matrix4f2 = clientLevel.m_45547_(new ClipContext(vec35, vec35.m_82549_(vec34.m_82490_(d5)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)object2)).m_82450_();
                double d6 = Math.max(0.05, Math.min(d5, matrix4f2.m_82554_(vec35)));
                boolean bl5 = compoundTag.m_128471_("released");
                boolean bl6 = bl4 = !bl5 && d2 < (double)n5;
                double d7 = bl4 ? d2 : (bl5 ? d2 : (d2 - (double)n5) % (double)n6);
                Matrix4f matrix4f3 = NativeScene.frame(vec32, vec34, 2.2, 3.0, d5);
                Matrix4f matrix4f4 = NativeScene.frame(vec35, vec34, 2.2, 1.0, d6);
                n2 -= NativeDraw.model(bl4 ? "rail_lock" : "rail_pulse", matrix4f3, matrix4f4, d7, bl4 ? (double)n5 : (double)n6, bl4 ? 1.0f : NativeMotion.fade(d7, n6), bl2, bl, Vec3.f_82478_, renderLevelStageEvent.getPoseStack(), vertexConsumer, n2 - n3);
                continue;
            }
            if (!string.equals("xuanlan_ring")) continue;
            if (compoundTag.m_128471_("released")) {
                Matrix4f matrix4f5 = NativeScene.ground(vec32, d5);
                n2 -= NativeDraw.model("ring_pulse", matrix4f5, matrix4f5, d2, n4, NativeMotion.fade(d2, n4), false, bl, Vec3.f_82478_, renderLevelStageEvent.getPoseStack(), vertexConsumer, n2 - n3);
                continue;
            }
            if (d2 < (double)n5) {
                Matrix4f matrix4f6 = NativeScene.ground(vec32, d5);
                n2 -= NativeDraw.model("ring_lock", matrix4f6, matrix4f6, d2, n5, 1.0f, false, bl, Vec3.f_82478_, renderLevelStageEvent.getPoseStack(), vertexConsumer, n2 - n3);
                continue;
            }
            for (int i = 0; i < n7; ++i) {
                double d8 = d2 - (double)n5 - (double)(i * n6);
                if (d8 < 0.0 || d8 >= (double)n6) continue;
                matrix4f = NativeScene.ground(vec32, NativeMotion.waveRadius(d5, i, n7));
                n2 -= NativeDraw.model("ring_pulse", matrix4f, matrix4f, d8, n6, NativeMotion.fade(d8, n6), false, bl, Vec3.f_82478_, renderLevelStageEvent.getPoseStack(), vertexConsumer, n2 - n3);
            }
        }
        block9: for (n = 0; n < 2; ++n) {
            for (Moment moment2 : MOMENTS) {
                String string;
                if (n2 <= 0) continue block9;
                if (moment2.packet.kind() == 21 != (n == 0)) continue;
                XuanlanFxPacket xuanlanFxPacket = moment2.packet;
                double d9 = (float)clientLevel.m_46467_() + f - (float)moment2.born;
                if (d9 < 0.0 || d9 >= (double)moment2.life || NativeScene.invalidOwner(clientLevel, moment2) || Math.min(vec3.m_82557_(xuanlanFxPacket.from()), vec3.m_82557_(xuanlanFxPacket.to())) > 9216.0) continue;
                switch (xuanlanFxPacket.kind()) {
                    case 20: {
                        String string2 = "sky_rise";
                        break;
                    }
                    case 21: {
                        String string2 = "sky_warning";
                        break;
                    }
                    case 22: {
                        String string2 = "sky_impact";
                        break;
                    }
                    case 23: {
                        String string2 = "sky_return";
                        break;
                    }
                    case 25: {
                        String string2 = "contact_break";
                        break;
                    }
                    default: {
                        String string2 = string = "pressure_vent";
                    }
                }
                Matrix4f matrix4f = xuanlanFxPacket.kind() == 21 ? NativeScene.ground(xuanlanFxPacket.from(), 3.0) : (xuanlanFxPacket.kind() == 22 ? NativeScene.location(xuanlanFxPacket.from()).scale(3.0f, (float)Math.max(0.1, Math.min(128.0, xuanlanFxPacket.to().f_82480_ - xuanlanFxPacket.from().f_82480_)), 3.0f) : (xuanlanFxPacket.kind() == 26 ? NativeScene.frame(xuanlanFxPacket.from(), xuanlanFxPacket.to().m_82546_(xuanlanFxPacket.from()), 7.5, 7.5, Math.min(10.75, xuanlanFxPacket.from().m_82554_(xuanlanFxPacket.to()))) : NativeScene.location(xuanlanFxPacket.from())));
                n2 -= NativeDraw.model(string, matrix4f, matrix4f, d9, moment2.life, xuanlanFxPacket.kind() == 21 ? 1.0f : NativeMotion.fade(d9, moment2.life), false, bl, Vec3.f_82478_, renderLevelStageEvent.getPoseStack(), vertexConsumer, n2);
            }
        }
        n = particleStatus == ParticleStatus.MINIMAL ? 16 : 32;
        boolean bl7 = false;
        for (Trail trail : TRAILS.values()) {
            void var12_17;
            if (++var12_17 > n || trail.entity.m_20238_(vec3) > 2304.0 || trail.points.size() < 2) continue;
            Point point = null;
            for (Point point2 : trail.points) {
                Vec3 vec36;
                double d10;
                if (point != null && (d10 = (vec36 = point2.position.m_82546_(point.position)).m_82553_()) > 0.001 && d10 < 8.0) {
                    d = (float)clientLevel.m_46467_() + f - (float)point2.tick;
                    Matrix4f matrix4f = NativeScene.frame(point.position, vec36, 1.0, 1.0, d10);
                    n2 -= NativeDraw.model("flywheel_trail", matrix4f, matrix4f, d, 9.0, NativeMotion.fade(d, 9.0), false, bl, Vec3.f_82478_, renderLevelStageEvent.getPoseStack(), vertexConsumer, n2);
                }
                point = point2;
            }
        }
        bufferSource.m_109912_(INK);
    }

    public static void flywheel(FlywheelProjectileEntity flywheelProjectileEntity, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        if (flywheelProjectileEntity.m_20145_()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        Vec3 vec3 = minecraft.f_91063_.m_109153_().m_90583_();
        double d = vec3.m_82554_(flywheelProjectileEntity.m_20318_(f2));
        if (d > 64.0) {
            return;
        }
        double d2 = (float)flywheelProjectileEntity.f_19797_ + (minecraft.m_91104_() ? 0.0f : f2);
        Vec3 vec32 = NativeFacing.projectileDirection(flywheelProjectileEntity.m_20184_(), f, flywheelProjectileEntity.m_146909_());
        NativeWheel.render(flywheelProjectileEntity, vec32, d2, poseStack, multiBufferSource, n);
        Matrix4f matrix4f = NativeFacing.projectile(vec32, d2, XuanlanEliteFlywheels.echoScale((Entity)flywheelProjectileEntity, 0.45f));
        int n2 = minecraft.f_91066_.m_231929_().m_231551_() == ParticleStatus.MINIMAL ? 210 : 400;
        NativeDraw.model("flywheel_echo", matrix4f, matrix4f, d2, 60.0, NativeMotion.near(d) * 0.3f, false, false, Vec3.f_82478_, poseStack, multiBufferSource.m_6299_(INK), n2);
    }

    static {
        renderOrigin = Vec3.f_82478_;
    }

    private record Moment(XuanlanFxPacket packet, UUID owner, long born, int life) {
    }

    private static final class Trail {
        final Entity entity;
        final ArrayDeque<Point> points = new ArrayDeque();

        Trail(Entity entity) {
            this.entity = entity;
        }
    }

    private record Point(Vec3 position, long tick) {
    }
}

