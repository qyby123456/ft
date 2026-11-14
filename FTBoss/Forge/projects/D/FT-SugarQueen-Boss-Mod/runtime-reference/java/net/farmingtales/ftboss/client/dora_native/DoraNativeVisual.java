/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
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
package net.farmingtales.ftboss.client.dora_native;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.farmingtales.ftboss.client.CombatVisualEntities;
import net.farmingtales.ftboss.client.dora_native.DoraGeometry;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.WeaponFxPacket;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
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
public final class DoraNativeVisual {
    private static final int MAX_ACTIVE = 64;
    private static final int MAX_VISIBLE_BOSSES = 12;
    private static final double VIEW_DISTANCE_SQUARED = 4096.0;
    private static final ArrayDeque<Shot> SHOTS = new ArrayDeque();
    private static final Map<Integer, Long> RELOADS = new HashMap<Integer, Long>();
    private static ClientLevel world;

    private DoraNativeVisual() {
    }

    public static void receive(WeaponFxPacket weaponFxPacket) {
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (!(clientLevel != null && weaponFxPacket != null && weaponFxPacket.valid() && weaponFxPacket.kind().doraGun() && clientLevel.m_46472_().m_135782_().equals((Object)weaponFxPacket.dimension()))) {
            return;
        }
        DoraNativeVisual.bind(clientLevel);
        if (weaponFxPacket.kind() == WeaponFxRules.Kind.DORA_CHARGE) {
            SHOTS.removeIf(shot -> shot.packet.kind() == weaponFxPacket.kind() && shot.packet.start().m_82557_(weaponFxPacket.start()) < 1.0);
        }
        while (SHOTS.size() >= 64) {
            SHOTS.removeFirst();
        }
        SHOTS.addLast(new Shot(weaponFxPacket, clientLevel.m_46467_()));
    }

    public static CompoundTag cueForLegacy(EasterBossEntity easterBossEntity) {
        CompoundTag compoundTag = easterBossEntity.getSkillCue();
        if (easterBossEntity.getVariant() == 2 && !easterBossEntity.isMini() && !easterBossEntity.m_21525_() && compoundTag.m_128471_("doraSniper")) {
            return new CompoundTag();
        }
        return compoundTag;
    }

    private static boolean validCue(CompoundTag compoundTag) {
        if (compoundTag.m_128451_("duration") <= 0 || compoundTag.m_128451_("duration") > 200) {
            return false;
        }
        Vec3 vec3 = DoraNativeVisual.point(compoundTag, "");
        Vec3 vec32 = DoraNativeVisual.point(compoundTag, "end");
        double d = vec3.m_82557_(vec32);
        return DoraNativeVisual.finite(vec3) && DoraNativeVisual.finite(vec32) && Double.isFinite(d) && d <= 9216.0;
    }

    private static boolean finite(Vec3 vec3) {
        return WeaponFxRules.validPosition(vec3.f_82479_) && WeaponFxRules.validPosition(vec3.f_82480_) && WeaponFxRules.validPosition(vec3.f_82481_);
    }

    private static Vec3 point(CompoundTag compoundTag, String string) {
        return string.isEmpty() ? new Vec3(compoundTag.m_128459_("x"), compoundTag.m_128459_("y"), compoundTag.m_128459_("z")) : new Vec3(compoundTag.m_128459_(string + "X"), compoundTag.m_128459_(string + "Y"), compoundTag.m_128459_(string + "Z"));
    }

    private static void bind(ClientLevel clientLevel) {
        if (world != clientLevel) {
            DoraNativeVisual.clear();
            world = clientLevel;
        }
    }

    private static void clear() {
        SHOTS.clear();
        RELOADS.clear();
        world = null;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null) {
            DoraNativeVisual.clear();
            return;
        }
        DoraNativeVisual.bind(clientLevel);
        if (minecraft.m_91104_()) {
            return;
        }
        long l = clientLevel.m_46467_();
        SHOTS.removeIf(shot -> l < shot.started || l - shot.started >= (long)shot.packet.kind().ticks);
        RELOADS.keySet().removeIf(n -> {
            EasterBossEntity easterBossEntity;
            Entity entity = clientLevel.m_6815_(n.intValue());
            return !(entity instanceof EasterBossEntity) || (easterBossEntity = (EasterBossEntity)entity).getVariant() != 2 || !easterBossEntity.m_6084_() || !easterBossEntity.isDoraReloading();
        });
        for (EasterBossEntity easterBossEntity : CombatVisualEntities.get(clientLevel).hiddenBosses()) {
            if (easterBossEntity.getVariant() != 2 || !easterBossEntity.m_6084_() || easterBossEntity.isMini() || easterBossEntity.m_21525_() || !easterBossEntity.isDoraReloading()) continue;
            RELOADS.putIfAbsent(easterBossEntity.m_19879_(), l);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == world) {
            DoraNativeVisual.clear();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        DoraNativeVisual.clear();
    }

    private static Basis basis(Vec3 vec3) {
        Vec3 vec32 = vec3.m_82556_() < 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : vec3.m_82541_();
        Vec3 vec33 = Math.abs(vec32.f_82480_) > 0.92 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 vec34 = vec33.m_82537_(vec32).m_82541_();
        return new Basis(vec32, vec34, vec32.m_82537_(vec34).m_82541_());
    }

    private static int emit(VertexConsumer vertexConsumer, Matrix4f matrix4f, DoraGeometry.Mesh mesh, Vec3 vec3, Basis basis, Vec3 vec32, int n) {
        int n2 = 0;
        double[] dArray = new double[4];
        double[] dArray2 = new double[4];
        double[] dArray3 = new double[4];
        for (DoraGeometry.Face face : mesh.faces) {
            int n3;
            if (n2 >= n) break;
            boolean bl = false;
            double[] dArray4 = face.points();
            for (n3 = 0; n3 < 4; ++n3) {
                double d = dArray4[n3 * 3];
                double d2 = dArray4[n3 * 3 + 1];
                double d3 = dArray4[n3 * 3 + 2];
                dArray[n3] = vec3.f_82479_ + basis.side.f_82479_ * d + basis.up.f_82479_ * d2 + basis.forward.f_82479_ * d3;
                dArray2[n3] = vec3.f_82480_ + basis.side.f_82480_ * d + basis.up.f_82480_ * d2 + basis.forward.f_82480_ * d3;
                dArray3[n3] = vec3.f_82481_ + basis.side.f_82481_ * d + basis.up.f_82481_ * d2 + basis.forward.f_82481_ * d3;
                double d4 = dArray[n3] - vec32.f_82479_;
                double d5 = dArray2[n3] - vec32.f_82480_;
                double d6 = dArray3[n3] - vec32.f_82481_;
                if (!(d4 * d4 + d5 * d5 + d6 * d6 < 0.16)) continue;
                bl = true;
            }
            if (bl) continue;
            n3 = face.color();
            for (int i = 0; i < 4; ++i) {
                vertexConsumer.m_252986_(matrix4f, (float)dArray[i], (float)dArray2[i], (float)dArray3[i]).m_6122_(n3 >> 16 & 0xFF, n3 >> 8 & 0xFF, n3 & 0xFF, Math.round(face.alpha() * 255.0f)).m_5752_();
            }
            ++n2;
        }
        return n2;
    }

    private static boolean visible(RenderLevelStageEvent renderLevelStageEvent, Vec3 vec3, Vec3 vec32, Vec3 vec33, double d) {
        AABB aABB = new AABB(vec32, vec33).m_82400_(d);
        return aABB.m_82399_().m_82557_(vec3) <= 4096.0 && (renderLevelStageEvent.getFrustum() == null || renderLevelStageEvent.getFrustum().m_113029_(aABB));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        EasterBossEntity easterBossEntity222;
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null) {
            return;
        }
        DoraNativeVisual.bind(clientLevel);
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n = particleStatus == ParticleStatus.MINIMAL ? 480 : (particleStatus == ParticleStatus.DECREASED ? 1000 : 1800);
        int n2 = particleStatus == ParticleStatus.MINIMAL ? 0 : 1;
        float f = minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick();
        long l = clientLevel.m_46467_();
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        ArrayList<EasterBossEntity> arrayList = new ArrayList<EasterBossEntity>();
        for (EasterBossEntity easterBossEntity222 : CombatVisualEntities.get(clientLevel).hiddenBosses()) {
            if (easterBossEntity222.getVariant() != 2 || !easterBossEntity222.m_6084_() || easterBossEntity222.m_213877_() || easterBossEntity222.isMini() || easterBossEntity222.m_21525_() || !(easterBossEntity222.m_20318_(f).m_82557_(vec3) <= 4096.0) || !easterBossEntity222.getSkillCue().m_128471_("doraSniper") && easterBossEntity222.getDoraSpearTicks() <= 0 && !easterBossEntity222.isDoraReloading()) continue;
            arrayList.add(easterBossEntity222);
        }
        arrayList.sort(Comparator.comparingDouble(easterBossEntity -> easterBossEntity.m_20318_(f).m_82557_(vec3)));
        if (arrayList.isEmpty() && SHOTS.isEmpty()) {
            return;
        }
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        easterBossEntity222 = minecraft.m_91269_().m_110104_();
        poseStack.m_85836_();
        try {
            Object object;
            DoraGeometry.Mesh mesh;
            Object object2;
            poseStack.m_85837_(-vec3.f_82479_, -vec3.f_82480_, -vec3.f_82481_);
            Matrix4f matrix4f = poseStack.m_85850_().m_252922_();
            VertexConsumer vertexConsumer = easterBossEntity222.m_6299_(LightType.TYPE);
            int n3 = 0;
            for (EasterBossEntity iterator2 : arrayList) {
                Vec3 n5;
                Vec3 vec32;
                Vec3 d;
                if (n < 24 || n3++ >= 12) break;
                int shot3 = iterator2.m_20318_(f).m_82557_(vec3) > 576.0 ? 0 : n2;
                object2 = iterator2.getSkillCue();
                if (object2.m_128471_("doraSniper") && DoraNativeVisual.validCue((CompoundTag)object2) && DoraNativeVisual.visible(renderLevelStageEvent, vec3, d = DoraNativeVisual.point((CompoundTag)object2, ""), vec32 = DoraNativeVisual.point((CompoundTag)object2, "end"), 0.9)) {
                    double l2 = (float)(l - object2.m_128454_("start")) + f;
                    mesh = DoraGeometry.sniper(l2, object2.m_128451_("duration"), d.m_82554_(vec32), object2.m_128471_("firing"), object2.m_128471_("doraLocked"), shot3);
                    n -= DoraNativeVisual.emit(vertexConsumer, matrix4f, mesh, d, DoraNativeVisual.basis(vec32.m_82546_(d)), vec3, n);
                }
                d = iterator2.m_20318_(f);
                vec32 = iterator2.m_20252_(f);
                if (iterator2.getDoraSpearTicks() > 0 && DoraNativeVisual.visible(renderLevelStageEvent, vec3, n5 = d.m_82520_(0.0, (double)iterator2.m_20192_(), 0.0), object = clientLevel.m_45547_(new ClipContext(n5, n5.m_82549_(vec32.m_82490_(6.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)iterator2)).m_82450_(), 0.4)) {
                    n -= DoraNativeVisual.emit(vertexConsumer, matrix4f, DoraGeometry.spear((float)(4 - iterator2.getDoraSpearTicks()) + f, n5.m_82554_(object), shot3), n5, DoraNativeVisual.basis(object.m_82546_(n5)), vec3, n);
                }
                if (!iterator2.isDoraReloading() || !DoraNativeVisual.visible(renderLevelStageEvent, vec3, d.m_82520_(0.0, 0.7, 0.0), d.m_82546_(vec32.m_82490_(2.4)), 1.3)) continue;
                long l2 = RELOADS.getOrDefault(iterator2.m_19879_(), l);
                mesh = vec32.m_82542_(1.0, 0.0, 1.0);
                n -= DoraNativeVisual.emit(vertexConsumer, matrix4f, DoraGeometry.reload((float)(l - l2) + f, shot3), d, DoraNativeVisual.basis((Vec3)mesh), vec3, n);
            }
            ArrayList arrayList2 = new ArrayList();
            for (Shot shot2 : SHOTS) {
                if (!DoraNativeVisual.visible(renderLevelStageEvent, vec3, shot2.packet.start(), shot2.packet.end(), (double)shot2.packet.radius() + 0.7)) continue;
                arrayList2.add(shot2);
            }
            arrayList2.sort(Comparator.comparingDouble(shot -> shot.packet.start().m_82557_(vec3)));
            Iterator iterator = arrayList2.iterator();
            while (iterator.hasNext()) {
                Shot shot3 = (Shot)iterator.next();
                if (n < 12) {
                    break;
                }
                object2 = shot3.packet;
                double d = (float)(l - shot3.started) + f;
                int n4 = ((WeaponFxPacket)object2).start().m_82557_(vec3) > 576.0 ? 0 : n2;
                object = DoraGeometry.shot(((WeaponFxPacket)object2).kind().name(), d, ((WeaponFxPacket)object2).kind().ticks, ((WeaponFxPacket)object2).start().m_82554_(((WeaponFxPacket)object2).end()), ((WeaponFxPacket)object2).radius(), n4);
                mesh = ((WeaponFxPacket)object2).kind() == WeaponFxRules.Kind.DORA_IMPACT ? vec3.m_82546_(((WeaponFxPacket)object2).start()) : ((WeaponFxPacket)object2).end().m_82546_(((WeaponFxPacket)object2).start());
                n -= DoraNativeVisual.emit(vertexConsumer, matrix4f, (DoraGeometry.Mesh)object, ((WeaponFxPacket)object2).start(), DoraNativeVisual.basis((Vec3)mesh), vec3, n);
            }
        }
        finally {
            easterBossEntity222.m_109912_(LightType.TYPE);
            poseStack.m_85849_();
        }
    }

    private record Shot(WeaponFxPacket packet, long started) {
    }

    private record Basis(Vec3 forward, Vec3 side, Vec3 up) {
    }

    private static final class LightType
    extends RenderType {
        private static final RenderType TYPE = LightType.m_173215_((String)"ftboss_dora_native_light", (VertexFormat)DefaultVertexFormat.f_85815_, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)16384, (boolean)false, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.m_110628_().m_173292_(f_173104_).m_110685_(f_110135_).m_110663_(f_110113_).m_110687_(f_110115_).m_110661_(f_110110_).m_110691_(false));

        private LightType() {
            super("ftboss_dora_native_light", DefaultVertexFormat.f_85815_, VertexFormat.Mode.QUADS, 16384, false, false, () -> {}, () -> {});
        }
    }
}

