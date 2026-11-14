/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
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
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client.saya;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.client.saya.SayaModels;
import net.farmingtales.ftboss.client.saya.SayaMotion;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.saya.SayaCastLifecycle;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class SayaSpellRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/fx/saya/materials.png");
    private static final RenderType SOLID = RenderType.m_110473_((ResourceLocation)TEXTURE);
    private static final RenderType LIGHT = RenderType.m_110488_((ResourceLocation)TEXTURE);
    private static final List<Visual> ACTIVE = new ArrayList<Visual>();
    private static Object world;

    private SayaSpellRenderer() {
    }

    public static boolean supports(String string) {
        return SayaModels.SPELLS.contains(string);
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (!(spellVisualPacket.valid() && SayaSpellRenderer.supports(spellVisualPacket.spell().id) && minecraft.f_91073_ != null && minecraft.f_91073_.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension()))) {
            return false;
        }
        if (SayaModels.get(spellVisualPacket.spell().id) == null) {
            return false;
        }
        if (world != minecraft.f_91073_) {
            ACTIVE.clear();
            world = minecraft.f_91073_;
        }
        long l = minecraft.f_91073_.m_46467_();
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE) {
            for (Visual visual2 : ACTIVE) {
                if (!visual2.packet.owner().equals(spellVisualPacket.owner()) || visual2.packet.spell() != spellVisualPacket.spell() || visual2.packet.phase() != spellVisualPacket.phase()) continue;
                visual2.packet = spellVisualPacket;
                return true;
            }
        } else {
            ACTIVE.removeIf(visual -> visual.packet.owner().equals(spellVisualPacket.owner()) && visual.packet.spell() == spellVisualPacket.spell() && (visual.packet.phase() == SpellVisualRules.Phase.CHARGE || visual.packet.phase() == spellVisualPacket.phase() && (spellVisualPacket.phase() != SpellVisualRules.Phase.BEAM || visual.packet.start().m_82557_(spellVisualPacket.start()) < 0.01 && visual.packet.end().m_82557_(spellVisualPacket.end()) < 0.01)));
        }
        if (ACTIVE.size() >= 24) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Visual(spellVisualPacket, l));
        return true;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null || world != minecraft.f_91073_) {
            SayaSpellRenderer.clear();
            world = minecraft.f_91073_;
            return;
        }
        long l = minecraft.f_91073_.m_46467_();
        ACTIVE.removeIf(visual -> {
            TeaTreeBossEntity teaTreeBossEntity;
            long l2 = l - visual.started;
            Entity entity = minecraft.f_91073_.m_6815_(visual.packet.ownerId());
            boolean bl = visual.packet.phase() == SpellVisualRules.Phase.CHARGE && entity instanceof TeaTreeBossEntity;
            return l2 < 0L || !bl && l2 >= (long)visual.packet.spell().lifetime(visual.packet.phase()) || entity == null || !entity.m_20148_().equals(visual.packet.owner()) || entity.m_213877_() || visual.packet.phase() == SpellVisualRules.Phase.CHARGE && entity instanceof TeaTreeBossEntity && SayaCastLifecycle.clearCharge(visual.packet.spell().id, (teaTreeBossEntity = (TeaTreeBossEntity)entity).signatureCastAction().name(), teaTreeBossEntity.signatureCastAction().elapsed(0.0f), l2);
        });
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == world) {
            SayaSpellRenderer.clear();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        SayaSpellRenderer.clear();
    }

    public static void clear() {
        ACTIVE.clear();
        SayaModels.clear();
        world = null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || ACTIVE.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null || minecraft.f_91073_ != world) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        Vec3 vec32 = new Vec3(renderLevelStageEvent.getCamera().m_253058_());
        boolean bl = minecraft.f_91066_.m_92176_().m_90612_();
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        int n = 0;
        ArrayList<Visual> arrayList = new ArrayList<Visual>(ACTIVE);
        arrayList.sort(Comparator.comparingDouble(visual -> visual.packet.start().m_82557_(vec3)));
        for (Visual visual2 : arrayList) {
            boolean bl2;
            SayaModels.Part part;
            int n2;
            double d;
            TeaTreeBossEntity teaTreeBossEntity;
            TeaTreeBossEntity teaTreeBossEntity2;
            int n3;
            SpellVisualPacket spellVisualPacket = visual2.packet;
            Entity entity = minecraft.f_91073_.m_6815_(spellVisualPacket.ownerId());
            if (entity == null || !entity.m_20148_().equals(spellVisualPacket.owner())) continue;
            double d2 = (float)(minecraft.f_91073_.m_46467_() - visual2.started) + renderLevelStageEvent.getPartialTick();
            int n4 = spellVisualPacket.spell().lifetime(spellVisualPacket.phase());
            if (spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE && entity instanceof TeaTreeBossEntity && (n3 = SayaCastLifecycle.chargeDuration(spellVisualPacket.spell().id, (teaTreeBossEntity2 = (TeaTreeBossEntity)entity).signatureCastAction().name(), -1)) > 0) {
                n4 = n3;
                d2 = teaTreeBossEntity2.signatureCastAction().elapsed(renderLevelStageEvent.getPartialTick());
            }
            if (d2 < 0.0 || d2 >= (double)n4) continue;
            double d3 = d2 / (double)n4;
            if (spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE && entity instanceof TeaTreeBossEntity) {
                teaTreeBossEntity = (TeaTreeBossEntity)entity;
                d3 = SayaCastLifecycle.chargeProgress(spellVisualPacket.spell().id, teaTreeBossEntity.signatureCastAction().name(), teaTreeBossEntity.signatureCastAction().elapsed(renderLevelStageEvent.getPartialTick()), d3);
            }
            if ((d = (teaTreeBossEntity = spellVisualPacket.spell().followsCaster(spellVisualPacket.phase()) ? new Vec3(entity.f_19854_ + (entity.m_20185_() - entity.f_19854_) * (double)renderLevelStageEvent.getPartialTick(), entity.f_19855_ + (entity.m_20186_() - entity.f_19855_) * (double)renderLevelStageEvent.getPartialTick(), entity.f_19856_ + (entity.m_20189_() - entity.f_19856_) * (double)renderLevelStageEvent.getPartialTick()) : spellVisualPacket.start()).m_82557_(vec3)) > 4096.0) continue;
            float f = SpellVisualRules.opacity(d2, n4);
            if (spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM) {
                if (n + 96 > 12000) continue;
                n += SayaSpellRenderer.beam(poseStack, bufferSource.m_6299_(LIGHT), spellVisualPacket.start(), spellVisualPacket.end(), vec3, vec32, d3, f, bl);
                continue;
            }
            SayaModels.Model model = SayaModels.get(spellVisualPacket.spell().id);
            if (model == null) continue;
            Vec3 vec33 = spellVisualPacket.end().m_82546_(spellVisualPacket.start());
            double d4 = Math.atan2(vec33.f_82479_, vec33.f_82481_);
            double d5 = Math.cos(d4);
            double d6 = Math.sin(d4);
            double d7 = SpellVisuals.radiusScale(entity, spellVisualPacket.spell().id);
            if (!Double.isFinite(d7) || d7 <= 0.0) continue;
            int n5 = d > 324.0 || ACTIVE.size() > 8 ? 2 : 1;
            int n6 = 0;
            for (n2 = 0; n2 < model.parts.length; ++n2) {
                part = model.parts[n2];
                boolean bl3 = bl2 = part.glow || part.name.contains("stamen") || part.name.contains("pollen");
                if (n5 > 1 && bl2 && n2 % n5 != 0) continue;
                n6 += part.triangleCount;
            }
            if (n + n6 > 12000) continue;
            for (n2 = 0; n2 < model.parts.length; ++n2) {
                part = model.parts[n2];
                boolean bl4 = bl2 = part.glow || part.name.contains("stamen") || part.name.contains("pollen");
                if (n5 > 1 && bl2 && n2 % n5 != 0) continue;
                double[] dArray = SayaMotion.pose(part.motion, part.delay, d3, spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE);
                double[][] dArray2 = part.posedVertices;
                for (int i = 0; i < dArray2.length; ++i) {
                    double[] dArray3 = dArray2[i];
                    SayaMotion.vertex(part.vertices[i], part.pivot, dArray, dArray3);
                    double d8 = dArray3[0] * d7;
                    double d9 = dArray3[2] * d7;
                    dArray3[0] = ((Vec3)teaTreeBossEntity).f_82479_ + d8 * d5 + d9 * d6;
                    dArray3[1] = ((Vec3)teaTreeBossEntity).f_82480_ + dArray3[1] * d7;
                    dArray3[2] = ((Vec3)teaTreeBossEntity).f_82481_ - d8 * d6 + d9 * d5;
                }
                VertexConsumer vertexConsumer = bufferSource.m_6299_(part.glow ? LIGHT : SOLID);
                for (int i = 0; i < part.faces.length; ++i) {
                    int[] nArray = part.faces[i];
                    for (int j = 1; j < nArray.length - 1 && n < 12000; ++n, ++j) {
                        SayaSpellRenderer.triangle(poseStack, vertexConsumer, dArray2[nArray[0]], dArray2[nArray[j]], dArray2[nArray[j + 1]], part.tile, part.faceUvs[i][0], part.faceUvs[i][j], part.faceUvs[i][j + 1], vec3, vec32, f * (part.glow ? 0.72f : 1.0f), bl);
                    }
                    if (n >= 12000) break;
                }
                if (n >= 12000) break;
            }
            if (n < 12000) continue;
            break;
        }
        bufferSource.m_109912_(SOLID);
        bufferSource.m_109912_(LIGHT);
    }

    private static int beam(PoseStack poseStack, VertexConsumer vertexConsumer, Vec3 vec3, Vec3 vec32, Vec3 vec33, Vec3 vec34, double d, float f, boolean bl) {
        Vec3 vec35 = vec32.m_82546_(vec3);
        if (vec35.m_82556_() < 0.0025 || vec35.m_82556_() > 4096.0) {
            return 0;
        }
        int n = Math.min(96, Math.max(12, (int)(vec35.m_82553_() * 9.0)));
        for (int i = 0; i < n; ++i) {
            double d2 = ((double)i / (double)n + d * 0.5) % 1.0;
            Vec3 vec36 = vec3.m_82549_(vec35.m_82490_(d2)).m_82520_(Math.sin((double)i * 0.7) * 0.05, Math.cos((double)i * 0.6) * 0.04, 0.0);
            double d3 = 0.018;
            double[] dArray = new double[]{vec36.f_82479_, vec36.f_82480_ + d3, vec36.f_82481_};
            double[] dArray2 = new double[]{vec36.f_82479_ - d3, vec36.f_82480_ - d3, vec36.f_82481_};
            double[] dArray3 = new double[]{vec36.f_82479_ + d3, vec36.f_82480_ - d3, vec36.f_82481_};
            SayaSpellRenderer.triangle(poseStack, vertexConsumer, dArray, dArray2, dArray3, 10, null, null, null, vec33, vec34, f * (float)Math.sin(d2 * Math.PI), bl);
        }
        return n;
    }

    private static void triangle(PoseStack poseStack, VertexConsumer vertexConsumer, double[] dArray, double[] dArray2, double[] dArray3, int n, double[] dArray4, double[] dArray5, double[] dArray6, Vec3 vec3, Vec3 vec32, float f, boolean bl) {
        if ((double)(f *= SayaMotion.viewAlpha((dArray[0] + dArray2[0] + dArray3[0]) / 3.0 - vec3.f_82479_, (dArray[1] + dArray2[1] + dArray3[1]) / 3.0 - vec3.f_82480_, (dArray[2] + dArray2[2] + dArray3[2]) / 3.0 - vec3.f_82481_, vec32.f_82479_, vec32.f_82480_, vec32.f_82481_, bl)) <= 0.001) {
            return;
        }
        double d = dArray2[1] - dArray[1];
        double d2 = dArray3[2] - dArray[2];
        double d3 = dArray2[2] - dArray[2];
        double d4 = dArray3[1] - dArray[1];
        double d5 = d * d2 - d3 * d4;
        double d6 = dArray3[0] - dArray[0];
        double d7 = dArray2[0] - dArray[0];
        double d8 = d3 * d6 - d7 * d2;
        double d9 = d7 * d4 - d * d6;
        double d10 = Math.sqrt(d5 * d5 + d8 * d8 + d9 * d9);
        if (d10 < 1.0E-12) {
            return;
        }
        float f2 = (float)(n % 4 * 32 + 2) / 128.0f;
        float f3 = (float)(n / 4 * 32 + 2) / 128.0f;
        for (int i = 0; i < 4; ++i) {
            float f4;
            double[] dArray7;
            double[] dArray8;
            double[] dArray9 = i == 0 ? dArray : (dArray8 = i == 1 ? dArray2 : dArray3);
            double[] dArray10 = i == 0 ? dArray4 : (dArray7 = i == 1 ? dArray5 : dArray6);
            float f5 = dArray7 == null ? f2 + (i > 0 ? 0.21875f : 0.0f) : (f4 = (float)dArray7[0]);
            float f6 = dArray7 == null ? f3 + (i < 2 ? 0.21875f : 0.0f) : (float)dArray7[1];
            vertexConsumer.m_252986_(poseStack.m_85850_().m_252922_(), (float)(dArray8[0] - vec3.f_82479_), (float)(dArray8[1] - vec3.f_82480_), (float)(dArray8[2] - vec3.f_82481_)).m_85950_(1.0f, 1.0f, 1.0f, f).m_7421_(f4, f6).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(poseStack.m_85850_().m_252943_(), (float)(d5 / d10), (float)(d8 / d10), (float)(d9 / d10)).m_5752_();
        }
    }

    private static final class Visual {
        private SpellVisualPacket packet;
        private long started;

        private Visual(SpellVisualPacket spellVisualPacket, long l) {
            this.packet = spellVisualPacket;
            this.started = l;
        }
    }
}

