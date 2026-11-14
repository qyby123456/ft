/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
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
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.xueli_arcane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.xueli_arcane.ArcaneAsset;
import net.farmingtales.ftboss.client.xueli_arcane.ArcanePose;
import net.farmingtales.ftboss.client.xueli_arcane.ArcaneSafety;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class XueliArcaneRenderer {
    private static final Map<String, ArcaneAsset.Effect> EFFECTS = ArcaneAsset.load();
    private static final ResourceLocation ATLAS = new ResourceLocation("ftboss", "xueli_arcane/frost_arcana.png");
    private static final RenderType ICE = RenderType.m_110470_((ResourceLocation)ATLAS);
    private static final RenderType LIGHT = RenderType.m_234338_((ResourceLocation)ATLAS);
    private static final List<Activation> ACTIVE = new ArrayList<Activation>();
    private static ClientLevel attachedWorld;

    private XueliArcaneRenderer() {
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket) {
        long l;
        boolean bl;
        if (spellVisualPacket == null || spellVisualPacket.spell() == null || !ArcaneAsset.IDS.contains(spellVisualPacket.spell().id)) {
            return false;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !spellVisualPacket.valid() || !spellVisualPacket.dimension().equals((Object)clientLevel.m_46472_().m_135782_())) {
            return true;
        }
        if (attachedWorld != clientLevel) {
            ACTIVE.clear();
            attachedWorld = clientLevel;
        }
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE) {
            return true;
        }
        boolean bl2 = bl = spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM;
        if (spellVisualPacket.spell().id.equals("frost_needles") && !bl) {
            return true;
        }
        long l2 = clientLevel.m_46467_();
        ACTIVE.removeIf(activation -> l2 - activation.origin > (long)activation.durationTicks());
        for (Activation activation2 : ACTIVE) {
            if (!activation2.packet.owner().equals(spellVisualPacket.owner()) || activation2.packet.spell() != spellVisualPacket.spell() || activation2.ray != bl || activation2.packet.start().m_82557_(spellVisualPacket.start()) > 0.06 || bl && activation2.packet.end().m_82557_(spellVisualPacket.end()) > 0.06 || spellVisualPacket.phase() != SpellVisualRules.Phase.PULSE && l2 - activation2.origin >= 4L) continue;
            activation2.packet = spellVisualPacket;
            return true;
        }
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.PULSE) {
            l = switch (spellVisualPacket.spell().id) {
                case "winter_clock" -> 12;
                case "royal_shatter" -> 20;
                default -> 0;
            };
        } else {
            l = 0L;
        }
        long l3 = l;
        ACTIVE.add(new Activation(spellVisualPacket, l2 - l3));
        while (ACTIVE.size() > 36) {
            ACTIVE.remove(0);
        }
        return true;
    }

    @SubscribeEvent
    public static void disconnect(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        ACTIVE.clear();
        attachedWorld = null;
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == attachedWorld) {
            ACTIVE.clear();
            attachedWorld = null;
        }
    }

    private static double[] coordinates(Vec3 vec3) {
        return new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
    }

    private static double[] box(Entity entity) {
        AABB aABB = entity.m_20191_().m_82400_(0.09);
        return new double[]{aABB.f_82288_, aABB.f_82289_, aABB.f_82290_, aABB.f_82291_, aABB.f_82292_, aABB.f_82293_};
    }

    @SubscribeEvent
    public static void paint(RenderLevelStageEvent renderLevelStageEvent) {
        ArcaneSafety.Corner corner2;
        Object object;
        Matrix3f matrix3f;
        Entity entity;
        ArcaneAsset.Effect effect;
        Activation activation222;
        int n;
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null || clientLevel != attachedWorld) {
            ACTIVE.clear();
            attachedWorld = clientLevel;
            return;
        }
        long l = clientLevel.m_46467_();
        ACTIVE.removeIf(activation -> l - activation.origin >= (long)activation.durationTicks());
        if (ACTIVE.isEmpty()) {
            return;
        }
        double d = minecraft.m_91104_() ? 0.0 : (double)renderLevelStageEvent.getPartialTick();
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        double[] dArray2 = XueliArcaneRenderer.coordinates(vec3);
        double d2 = (double)renderLevelStageEvent.getCamera().m_90590_() * Math.PI / 180.0;
        double d3 = (double)renderLevelStageEvent.getCamera().m_90589_() * Math.PI / 180.0;
        double[] dArray3 = new double[]{-Math.sin(d2) * Math.cos(d3), -Math.sin(d3), Math.cos(d2) * Math.cos(d3)};
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n2 = particleStatus == ParticleStatus.MINIMAL ? 3 : (n = particleStatus == ParticleStatus.DECREASED ? 6 : 9);
        int n3 = particleStatus == ParticleStatus.MINIMAL ? 1000 : (particleStatus == ParticleStatus.DECREASED ? 2400 : 5200);
        List<double[]> list = clientLevel.m_6907_().stream().filter(abstractClientPlayer -> abstractClientPlayer.m_20238_(vec3) < 900.0).map(XueliArcaneRenderer::box).toList();
        ArrayList<DrawFace> arrayList = new ArrayList<DrawFace>();
        int n4 = 0;
        List<Activation> list2 = ACTIVE.stream().filter(activation -> activation.packet.start().m_82557_(vec3) < 4096.0).sorted(Comparator.comparingDouble(activation -> activation.packet.start().m_82557_(vec3))).limit(n).toList();
        for (Activation activation222 : list2) {
            List<ArcaneSafety.Corner> list3;
            double d4;
            effect = EFFECTS.get(activation222.packet.spell().id);
            entity = clientLevel.m_6815_(activation222.packet.ownerId());
            if (entity != null && !entity.m_20148_().equals(activation222.packet.owner())) {
                entity = null;
            }
            matrix3f = effect.anchor().equals("SELF") && entity != null ? entity.m_20318_((float)d) : activation222.packet.start();
            Vec3 vec32 = activation222.packet.end().m_82546_(activation222.packet.start());
            if (!activation222.ray) {
                Vec3 vec33 = vec32 = entity == null ? new Vec3(0.0, 0.0, 1.0) : entity.m_20154_().m_82542_(1.0, 0.0, 1.0);
            }
            if (vec32.m_82556_() < 1.0E-8) {
                vec32 = new Vec3(0.0, 0.0, 1.0);
            }
            vec32 = vec32.m_82541_();
            Quaternionf quaternionf = new Quaternionf().rotationTo(0.0f, 0.0f, 1.0f, (float)vec32.f_82479_, (float)vec32.f_82480_, (float)vec32.f_82481_);
            double d5 = ((double)(l - activation222.origin) + d) / 20.0;
            double d6 = Math.min(3.65, d5);
            if (effect.anchor().equals("SELF")) {
                d6 = d5 % 3.65;
            }
            double d7 = activation222.ray ? 0.86 : effect.scale();
            double d8 = effect.radiusScale(entity == null ? 0.6 : (double)entity.m_20205_());
            object = activation222.ray ? new double[3] : effect.offset();
            corner2 = null;
            double d9 = 0.0;
            if (activation222.ray) {
                corner2 = clientLevel.m_45547_(new ClipContext(activation222.packet.start(), activation222.packet.end(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).m_82450_();
                double d10 = Math.sqrt(corner2.m_82557_(activation222.packet.start()));
                if (d10 < 0.55) continue;
                d9 = d10;
                matrix3f = activation222.packet.start().m_82549_(vec32.m_82490_(Math.min(d10, 0.7 + Math.max(0.0, d10 - 0.7) * ArcanePose.nativeAdvance(d6) / 0.94)));
            }
            Vector3f vector3f = quaternionf.transform(new Vector3f((float)object[0], (float)object[1], (float)object[2]));
            matrix3f = matrix3f.m_82520_((double)vector3f.x, (double)vector3f.y, (double)vector3f.z);
            double d11 = d6;
            ArcanePose.State[] stateArray = (ArcanePose.State[])effect.bones().stream().map(bone -> ArcanePose.at(bone.track(), d11)).toArray(ArcanePose.State[]::new);
            int[] nArray = new int[stateArray.length];
            boolean[] blArray = new boolean[stateArray.length];
            for (ArcaneAsset.Face face : effect.faces()) {
                if (activation222.ray && !ArcaneAsset.centerRay(effect.bones().get(face.bone()).track()) || !(stateArray[face.bone()].alpha() * face.alpha() > 0.003)) continue;
                int n5 = face.bone();
                nArray[n5] = nArray[n5] + (face.positions().length + (activation222.ray ? 1 : -1));
            }
            for (int i = 0; i < nArray.length; ++i) {
                if (nArray[i] <= 0 || n4 + nArray[i] > n3) continue;
                n4 += nArray[i];
                blArray[i] = true;
            }
            double d12 = Math.min(1.0, Math.max(0.0, ((double)((long)activation222.durationTicks() - l + activation222.origin) - d) / 5.0));
            double[] dArray4 = entity == null ? null : XueliArcaneRenderer.box(entity);
            boolean[] blArray2 = new boolean[stateArray.length];
            ArrayList<PendingFace> arrayList2 = new ArrayList<PendingFace>();
            for (ArcaneAsset.Face face : effect.faces()) {
                if (!blArray[face.bone()] || activation222.ray && !ArcaneAsset.centerRay(effect.bones().get(face.bone()).track()) || (d4 = ((ArcanePose.State)((Object)(list3 = stateArray[face.bone()]))).alpha() * face.alpha() * d12) < 0.003) continue;
                ArcaneAsset.Bone bone2 = effect.bones().get(face.bone());
                ArrayList<ArcaneSafety.Corner> arrayList3 = new ArrayList<ArcaneSafety.Corner>();
                for (int i = 0; i < face.positions().length; ++i) {
                    double[] dArray5 = ArcanePose.point(face.positions()[i], bone2.pivot(), list3);
                    if (activation222.ray) {
                        dArray5[2] = dArray5[2] - (((ArcanePose.State)((Object)list3)).move()[2] + ArcaneSafety.rayTrailLag(ArcanePose.nativeAdvance(d6), ((ArcanePose.State)((Object)list3)).move()[2], d9) / d7);
                    }
                    Vector3f vector3f2 = quaternionf.transform(new Vector3f((float)(dArray5[0] * d7 * d8), (float)(dArray5[1] * d7), (float)(dArray5[2] * d7 * d8)));
                    arrayList3.add(new ArcaneSafety.Corner(new double[]{matrix3f.f_82479_ + (double)vector3f2.x, matrix3f.f_82480_ + (double)vector3f2.y, matrix3f.f_82481_ + (double)vector3f2.z}, face.uv()[i]));
                }
                ArrayList<ArcaneSafety.Corner> arrayList4 = arrayList3;
                if (!activation222.ray && (dArray4 != null && ArcaneSafety.bodyOverlap(arrayList4, dArray4) || list.stream().anyMatch(dArray -> ArcaneSafety.bodyOverlap(arrayList4, dArray)))) {
                    if (face.light()) continue;
                    blArray2[face.bone()] = true;
                    continue;
                }
                arrayList2.add(new PendingFace(arrayList3, d4, face.light(), face.bone()));
            }
            for (PendingFace pendingFace : arrayList2) {
                if (blArray2[pendingFace.bone()]) continue;
                list3 = pendingFace.corners();
                list3 = ArcaneSafety.cut(list3, dArray2, dArray3, 0.55);
                if (corner2 != null) {
                    list3 = ArcaneSafety.cut(list3, XueliArcaneRenderer.coordinates((Vec3)corner2), new double[]{-vec32.f_82479_, -vec32.f_82480_, -vec32.f_82481_}, 0.0);
                    list3 = ArcaneSafety.cut(list3, XueliArcaneRenderer.coordinates(activation222.packet.start()), XueliArcaneRenderer.coordinates(vec32), 0.55);
                }
                if (list3.size() < 3) continue;
                d4 = list3.stream().mapToDouble(corner -> ArcaneSafety.distance(corner.position(), dArray2)).average().orElse(0.0);
                arrayList.add(new DrawFace(list3, pendingFace.alpha(), pendingFace.light(), activation222.ray, d4));
            }
        }
        arrayList.sort(Comparator.comparingDouble(DrawFace::depth).reversed());
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        activation222 = bufferSource.m_6299_(ICE);
        effect = bufferSource.m_6299_(LIGHT);
        entity = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        matrix3f = renderLevelStageEvent.getPoseStack().m_85850_().m_252943_();
        int n6 = 0;
        for (DrawFace drawFace : arrayList) {
            if (n6 >= n3) break;
            List<ArcaneSafety.Corner> list4 = drawFace.corners();
            double[] dArray6 = new double[3];
            for (ArcaneSafety.Corner corner3 : list4) {
                for (int i = 0; i < 3; ++i) {
                    int n7 = i;
                    dArray6[n7] = dArray6[n7] + corner3.position()[i] / (double)list4.size();
                }
            }
            double d5 = ArcaneSafety.visibility(dArray6, dArray2, dArray3, bl, drawFace.ray());
            Object object2 = drawFace.light() ? effect : activation222;
            for (int i = 1; i < list4.size() - 1 && n6 < n3; ++i) {
                ArcaneSafety.Corner corner3 = list4.get(0);
                object = list4.get(i);
                corner2 = list4.get(i + 1);
                Vector3f vector3f = new Vector3f((float)(((ArcaneSafety.Corner)object).position()[0] - corner3.position()[0]), (float)(((ArcaneSafety.Corner)object).position()[1] - corner3.position()[1]), (float)(((ArcaneSafety.Corner)object).position()[2] - corner3.position()[2])).cross((Vector3fc)new Vector3f((float)(corner2.position()[0] - corner3.position()[0]), (float)(corner2.position()[1] - corner3.position()[1]), (float)(corner2.position()[2] - corner3.position()[2])));
                if ((double)vector3f.lengthSquared() < 1.0E-12) continue;
                vector3f.normalize();
                for (ArcaneSafety.Corner corner5 : List.of(corner3, object, corner2, corner2)) {
                    int n8 = (int)Math.round(drawFace.alpha() * 255.0 * Math.min(d5, ArcaneSafety.visibility(corner5.position(), dArray2, dArray3, bl, drawFace.ray())));
                    object2.m_252986_((Matrix4f)entity, (float)(corner5.position()[0] - dArray2[0]), (float)(corner5.position()[1] - dArray2[1]), (float)(corner5.position()[2] - dArray2[2])).m_6122_(255, 255, 255, Math.max(0, Math.min(255, n8))).m_7421_((float)corner5.uv()[0], (float)corner5.uv()[1]).m_86008_(655360).m_85969_(0xF000F0).m_252939_(matrix3f, vector3f.x, vector3f.y, vector3f.z).m_5752_();
                }
                ++n6;
            }
        }
        bufferSource.m_109912_(ICE);
        bufferSource.m_109912_(LIGHT);
    }

    private static final class Activation {
        private SpellVisualPacket packet;
        private final long origin;
        private final boolean ray;

        private Activation(SpellVisualPacket spellVisualPacket, long l) {
            this.packet = spellVisualPacket;
            this.origin = l;
            this.ray = spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM;
        }

        private int durationTicks() {
            return switch (this.packet.spell().id) {
                case "frost_needles" -> 15;
                case "glacial_prism" -> 160;
                case "sugar_hail" -> 60;
                case "royal_shatter" -> 52;
                case "winter_clock" -> 66;
                default -> 75;
            };
        }
    }

    private record PendingFace(List<ArcaneSafety.Corner> corners, double alpha, boolean light, int bone) {
    }

    private record DrawFace(List<ArcaneSafety.Corner> corners, double alpha, boolean light, boolean ray, double depth) {
    }
}

