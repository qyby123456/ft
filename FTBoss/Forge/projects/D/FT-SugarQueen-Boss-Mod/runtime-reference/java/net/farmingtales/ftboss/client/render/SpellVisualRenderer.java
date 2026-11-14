/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.ModList
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Quaternionf
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.client.apple.AppleKnightEffects;
import net.farmingtales.ftboss.client.compostzero.CompostRenderer;
import net.farmingtales.ftboss.client.defectzero.DefectZeroRenderer;
import net.farmingtales.ftboss.client.dorazero.DoraSpellVisuals;
import net.farmingtales.ftboss.client.maque.MaqueSpellRenderer;
import net.farmingtales.ftboss.client.qizaiarcane.ArcanePacketVisuals;
import net.farmingtales.ftboss.client.render.GongdiSpellCues;
import net.farmingtales.ftboss.client.render.GongdiSpellRenderer;
import net.farmingtales.ftboss.client.render.IronSpellLightParticles;
import net.farmingtales.ftboss.client.render.ManorBlockbenchSpellModels;
import net.farmingtales.ftboss.client.render.ManorSpellMergeRules;
import net.farmingtales.ftboss.client.render.ManorSpellModelRenderType;
import net.farmingtales.ftboss.client.render.OptimizedSpellLights;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.client.render.QingyiSpellRenderer;
import net.farmingtales.ftboss.client.render.SpellModelFacing;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.client.saya.SayaSpellRenderer;
import net.farmingtales.ftboss.client.sugarqueen.QueenRenderer;
import net.farmingtales.ftboss.client.tidefish.TideFishSpellVisual;
import net.farmingtales.ftboss.client.wineking.WineSpellFx;
import net.farmingtales.ftboss.client.xuanlanarcane.ArcaneScene;
import net.farmingtales.ftboss.client.xueli_arcane.XueliArcaneRenderer;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.ManorStandaloneSpellModels;
import net.farmingtales.ftboss.util.QizaiRoseStarfallModel;
import net.farmingtales.ftboss.util.RemainingSpellModels;
import net.farmingtales.ftboss.util.SpellBeamModels;
import net.farmingtales.ftboss.util.SpellVisualGeometry;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.youzi.spells.client.YouziSpellVisual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class SpellVisualRenderer {
    private static final ResourceLocation ATLAS = new ResourceLocation("ftboss", "textures/fx/spells/spell_atlas.png");
    private static final RenderType TYPE = RenderType.m_234338_((ResourceLocation)ATLAS);
    private static final List<Visual> ACTIVE = new ArrayList<Visual>();
    private static final boolean NATIVE_LIGHTS = ModList.get().isLoaded("irons_spellbooks") && !Boolean.getBoolean("ftboss.disableNativeSpellLights");
    private static ClientLevel world;

    private SpellVisualRenderer() {
    }

    public static void accept(SpellVisualPacket spellVisualPacket) {
        if (TideFishSpellVisual.accept(spellVisualPacket)) {
            return;
        }
        if (ArcaneScene.accept(spellVisualPacket)) {
            return;
        }
        if (ArcaneScene.accept(spellVisualPacket)) {
            return;
        }
        GongdiSpellCues.accept(spellVisualPacket);
        if (YouziSpellVisual.accept(spellVisualPacket)) {
            return;
        }
        if (QingyiSpellRenderer.accept(spellVisualPacket)) {
            return;
        }
        if (QueenRenderer.accept(spellVisualPacket)) {
            return;
        }
        if (ArcanePacketVisuals.receive(spellVisualPacket)) {
            return;
        }
        if (AppleKnightEffects.accept(spellVisualPacket)) {
            return;
        }
        if (CompostRenderer.accept(spellVisualPacket)) {
            return;
        }
        if (SayaSpellRenderer.accept(spellVisualPacket)) {
            return;
        }
        if (DefectZeroRenderer.accept(spellVisualPacket)) {
            return;
        }
        if (WineSpellFx.accept(spellVisualPacket)) {
            return;
        }
        if (DoraSpellVisuals.accept(spellVisualPacket)) {
            return;
        }
        if (XueliArcaneRenderer.accept(spellVisualPacket)) {
            return;
        }
        if (ArcaneScene.accept(spellVisualPacket)) {
            return;
        }
        if (MaqueSpellRenderer.accept(spellVisualPacket)) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !spellVisualPacket.valid() || !clientLevel.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) {
            return;
        }
        if (world != clientLevel) {
            ACTIVE.clear();
            world = clientLevel;
        }
        if (!ManorBlockbenchSpellModels.supports(spellVisualPacket.spell()) && !RemainingSpellModels.supports(spellVisualPacket.spell())) {
            if (false) {
                ACTIVE.removeIf(visual -> visual.same(spellVisualPacket) && visual.packet.phase() != SpellVisualRules.Phase.BEAM);
                return;
            }
        }
        long l = clientLevel.m_46467_();
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE) {
            for (Visual visual2 : ACTIVE) {
                if (!visual2.same(spellVisualPacket) || visual2.packet.phase() != SpellVisualRules.Phase.CHARGE) continue;
                visual2.packet = spellVisualPacket;
                visual2.refreshed = l;
                return;
            }
        } else {
            ACTIVE.removeIf(visual -> visual.same(spellVisualPacket) && (visual.packet.phase() == SpellVisualRules.Phase.CHARGE || spellVisualPacket.phase() != SpellVisualRules.Phase.BEAM && visual.packet.phase() != SpellVisualRules.Phase.BEAM));
        }
        if (ACTIVE.size() >= 96) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Visual(spellVisualPacket, l, !SpellBeamModels.replaces(spellVisualPacket.spell(), spellVisualPacket.phase()) && NATIVE_LIGHTS && OptimizedSpellLights.emit(spellVisualPacket)));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || clientLevel != world) {
            ACTIVE.clear();
            if (NATIVE_LIGHTS) {
                IronSpellLightParticles.clear();
            }
            world = clientLevel;
            return;
        }
        long l = clientLevel.m_46467_();
        ACTIVE.removeIf(visual -> {
            LivingEntity livingEntity;
            long l2 = l - visual.refreshed;
            Entity entity = SpellVisualRenderer.owner(clientLevel, visual.packet);
            return l2 < 0L || l2 >= (long)visual.packet.spell().lifetime(visual.packet.phase()) || entity != null && (entity.m_213877_() || entity instanceof LivingEntity && !(livingEntity = (LivingEntity)entity).m_6084_());
        });
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == world) {
            ACTIVE.clear();
            if (NATIVE_LIGHTS) {
                IronSpellLightParticles.clear();
            }
            world = null;
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        ACTIVE.clear();
        if (NATIVE_LIGHTS) {
            IronSpellLightParticles.clear();
        }
        world = null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        QingyiSpellRenderer.render(renderLevelStageEvent);
        boolean bl = SpellBloomRenderer.begin(renderLevelStageEvent, ACTIVE.isEmpty() ^ true);
        try {
            SpellVisualRenderer.ftboss$renderWithoutSoftBloom(renderLevelStageEvent);
            return;
        }
        finally {
            SpellBloomRenderer.end(bl);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderModels(List<Visual> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel, Vec3 vec3, float f, ParticleStatus particleStatus, MultiBufferSource.BufferSource bufferSource) {
        if (list.stream().noneMatch(visual -> ManorBlockbenchSpellModels.renders(visual.packet.spell(), visual.packet.phase()) || ManorStandaloneSpellModels.renders(visual.packet.spell(), visual.packet.phase()) || RemainingSpellModels.renders(visual.packet.spell(), visual.packet.phase()) || SpellBeamModels.renders(visual.packet.spell(), visual.packet.phase()) || SpellBeamModels.rendersAccent(visual.packet.spell(), visual.packet.phase()) || SpellVisualRenderer.roseStarfallBeam(visual.packet))) {
            return;
        }
        VertexConsumer vertexConsumer = bufferSource.m_6299_(ManorSpellModelRenderType.TYPE);
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        Entity entity = renderLevelStageEvent.getCamera().m_90592_();
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        boolean bl2 = bl;
        int n2 = particleStatus == ParticleStatus.MINIMAL ? 4 : (particleStatus == ParticleStatus.DECREASED ? 8 : 12);
        int n3 = 0;
        try {
            for (Visual visual2 : list) {
                double d;
                boolean bl3;
                SpellVisualPacket spellVisualPacket = visual2.packet;
                boolean bl4 = SpellVisualRenderer.roseStarfallBeam(spellVisualPacket);
                boolean bl5 = SpellBeamModels.rendersAccent(spellVisualPacket.spell(), spellVisualPacket.phase());
                boolean bl6 = RemainingSpellModels.renders(spellVisualPacket.spell(), spellVisualPacket.phase());
                boolean bl7 = ManorBlockbenchSpellModels.renders(spellVisualPacket.spell(), spellVisualPacket.phase());
                boolean bl8 = entity != null && entity.m_20148_().equals(spellVisualPacket.owner());
                boolean bl9 = bl3 = bl7 && ManorSpellMergeRules.hideBulkyInFirstPerson(spellVisualPacket.spell(), spellVisualPacket.phase(), bl, bl8);
                if (!bl7 && !bl4 && !bl5 && !bl6 && !ManorStandaloneSpellModels.renders(spellVisualPacket.spell(), spellVisualPacket.phase()) || n3 >= n2 || SpellVisualRules.modelHiddenForCamera(spellVisualPacket.spell(), bl, bl8) || bl3) continue;
                Entity entity2 = SpellVisualRenderer.owner(clientLevel, spellVisualPacket);
                float f2 = (float)SpellVisuals.radiusScale(entity2, spellVisualPacket.spell().id);
                Vec3 vec32 = spellVisualPacket.spell().followsCaster(spellVisualPacket.phase()) && entity2 != null ? entity2.m_20318_(f) : spellVisualPacket.start();
                AABB aABB = new AABB(vec32, bl4 ? spellVisualPacket.end() : vec32).m_82400_((double)(7.0f * f2));
                if (aABB.m_272282_(vec3) > 4096.0 || !renderLevelStageEvent.getFrustum().m_113029_(aABB)) continue;
                double d2 = (float)(clientLevel.m_46467_() - visual2.born) + f;
                double d3 = (float)(clientLevel.m_46467_() - visual2.refreshed) + f;
                float f3 = SpellVisualRules.modelOpacity(d3, spellVisualPacket.spell().lifetime(spellVisualPacket.phase()));
                if (f3 <= 0.0f) continue;
                Vec3 vec33 = spellVisualPacket.end().m_82546_(spellVisualPacket.start());
                if (spellVisualPacket.spell() == SpellVisualRules.Spell.WINGSTEP && entity2 != null) {
                    vec33 = entity2.m_20252_(f);
                }
                vec33 = SpellModelFacing.direction(spellVisualPacket, entity2, f);
                if (!bl4) {
                    vec33 = vec33.m_82542_(1.0, 0.0, 1.0);
                }
                double d4 = vec33.m_82553_();
                vec33 = d < 0.001 ? new Vec3(0.0, 0.0, 1.0) : vec33.m_82490_(1.0 / d4);
                poseStack.m_85836_();
                try {
                    poseStack.m_85837_(vec32.f_82479_ - vec3.f_82479_, vec32.f_82480_ - vec3.f_82480_, vec32.f_82481_ - vec3.f_82481_);
                    poseStack.m_252781_(new Quaternionf().rotationTo(0.0f, 0.0f, 1.0f, (float)vec33.f_82479_, (float)vec33.f_82480_, (float)vec33.f_82481_));
                    poseStack.m_85841_(f2, f2, f2);
                    PoseStack.Pose pose = poseStack.m_85850_();
                    int n4 = bl7 && ManorBlockbenchSpellModels.render(spellVisualPacket.spell(), spellVisualPacket.phase(), d2, f3, d4, f2, poseStack, bufferSource) ? 1 : 0;
                    poseStack.m_85836_();
                    poseStack.m_85837_(0.0, (double)ManorSpellMergeRules.legacyVerticalOffset(spellVisualPacket.spell(), spellVisualPacket.phase()), 0.0);
                    PoseStack.Pose pose2 = poseStack.m_85850_();
                    poseStack.m_85849_();
                    if (bl4) {
                        int[] nArray = new int[]{0};
                        QizaiRoseStarfallModel.solid(d2, d4, particleStatus != ParticleStatus.ALL, 1.0f, (point, point2, point3, point4, n) -> {
                            SpellVisualRenderer.roseModelVertex(vertexConsumer, pose, point, n, f3);
                            SpellVisualRenderer.roseModelVertex(vertexConsumer, pose, point2, n, f3);
                            SpellVisualRenderer.roseModelVertex(vertexConsumer, pose, point3, n, f3);
                            SpellVisualRenderer.roseModelVertex(vertexConsumer, pose, point4, n, f3);
                            nArray[0] = nArray[0] + 1;
                        });
                        n4 += nArray[0];
                    } else {
                        n4 += bl5 ? SpellBeamModels.drawAccent(spellVisualPacket.spell(), spellVisualPacket.phase(), d2, (point, point2, point3, point4, n) -> {
                            SpellVisualRenderer.modelVertex(vertexConsumer, pose2, point, n, f3);
                            SpellVisualRenderer.modelVertex(vertexConsumer, pose2, point2, n, f3);
                            SpellVisualRenderer.modelVertex(vertexConsumer, pose2, point3, n, f3);
                            SpellVisualRenderer.modelVertex(vertexConsumer, pose2, point4, n, f3);
                        }) : (bl6 ? RemainingSpellModels.draw(spellVisualPacket.spell(), spellVisualPacket.phase(), d3, particleStatus != ParticleStatus.ALL, (point, point2, point3, point4, n) -> {
                            SpellVisualRenderer.modelVertex(vertexConsumer, pose2, point, n, f3);
                            SpellVisualRenderer.modelVertex(vertexConsumer, pose2, point2, n, f3);
                            SpellVisualRenderer.modelVertex(vertexConsumer, pose2, point3, n, f3);
                            SpellVisualRenderer.modelVertex(vertexConsumer, pose2, point4, n, f3);
                        }) : ManorStandaloneSpellModels.draw(spellVisualPacket.spell(), spellVisualPacket.phase(), d2, (point, point2, point3, point4, n) -> {
                            SpellVisualRenderer.modelVertex(vertexConsumer, pose2, point, n, f3);
                            SpellVisualRenderer.modelVertex(vertexConsumer, pose2, point2, n, f3);
                            SpellVisualRenderer.modelVertex(vertexConsumer, pose2, point3, n, f3);
                            SpellVisualRenderer.modelVertex(vertexConsumer, pose2, point4, n, f3);
                        }));
                    }
                    if (n4 <= 0) continue;
                    ++n3;
                }
                finally {
                    poseStack.m_85849_();
                }
            }
            SpellVisualRenderer.renderBeamModels(list, renderLevelStageEvent, clientLevel, vec3, f, particleStatus, vertexConsumer);
        }
        finally {
            bufferSource.m_109912_(ManorSpellModelRenderType.TYPE);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderBeamModels(List<Visual> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel, Vec3 vec3, float f, ParticleStatus particleStatus, VertexConsumer vertexConsumer) {
        int n2 = particleStatus == ParticleStatus.MINIMAL ? 6 : (particleStatus == ParticleStatus.DECREASED ? 12 : 24);
        int n3 = 0;
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        Entity entity = renderLevelStageEvent.getCamera().m_90592_();
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        for (Visual visual : list) {
            boolean bl2;
            SpellVisualPacket spellVisualPacket = visual.packet;
            boolean bl3 = bl2 = entity != null && entity.m_20148_().equals(spellVisualPacket.owner());
            if (ManorSpellMergeRules.hideBulkyInFirstPerson(spellVisualPacket.spell(), spellVisualPacket.phase(), bl, bl2) || SpellVisualRenderer.roseStarfallBeam(spellVisualPacket) || !SpellBeamModels.renders(spellVisualPacket.spell(), spellVisualPacket.phase()) || n3 >= n2) continue;
            double d = (float)(clientLevel.m_46467_() - visual.born) + f;
            float f2 = SpellVisualRules.modelOpacity(d, spellVisualPacket.spell().lifetime(spellVisualPacket.phase()));
            Vec3 vec32 = spellVisualPacket.end().m_82546_(spellVisualPacket.start());
            double d2 = vec32.m_82553_();
            if (f2 <= 0.0f || d2 < 0.015 || d2 > 64.0) continue;
            Entity entity2 = SpellVisualRenderer.owner(clientLevel, spellVisualPacket);
            float f3 = (float)SpellVisuals.radiusScale(entity2, spellVisualPacket.spell().id);
            AABB aABB = new AABB(spellVisualPacket.start(), spellVisualPacket.end()).m_82400_(1.2 * (double)f3);
            if (aABB.m_272282_(vec3) > 4096.0 || !renderLevelStageEvent.getFrustum().m_113029_(aABB)) continue;
            vec32 = vec32.m_82490_(1.0 / d2);
            poseStack.m_85836_();
            try {
                poseStack.m_85837_(spellVisualPacket.start().f_82479_ - vec3.f_82479_, spellVisualPacket.start().f_82480_ - vec3.f_82480_, spellVisualPacket.start().f_82481_ - vec3.f_82481_);
                poseStack.m_252781_(new Quaternionf().rotationTo(0.0f, 0.0f, 1.0f, (float)vec32.f_82479_, (float)vec32.f_82480_, (float)vec32.f_82481_));
                poseStack.m_85841_(f3, f3, 1.0f);
                PoseStack.Pose pose = poseStack.m_85850_();
                int n4 = SpellBeamModels.draw(spellVisualPacket.spell(), spellVisualPacket.phase(), d, d2, particleStatus != ParticleStatus.ALL, (point, point2, point3, point4, n) -> {
                    SpellVisualRenderer.modelVertex(vertexConsumer, pose, point, n, f2);
                    SpellVisualRenderer.modelVertex(vertexConsumer, pose, point2, n, f2);
                    SpellVisualRenderer.modelVertex(vertexConsumer, pose, point3, n, f2);
                    SpellVisualRenderer.modelVertex(vertexConsumer, pose, point4, n, f2);
                });
                if (n4 <= 0) continue;
                ++n3;
            }
            finally {
                poseStack.m_85849_();
            }
        }
    }

    private static boolean doraFocusLaserBeam(SpellVisualPacket spellVisualPacket) {
        return spellVisualPacket.spell() == SpellVisualRules.Spell.ARCANE_FUSILLADE && spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM;
    }

    private static boolean roseStarfallBeam(SpellVisualPacket spellVisualPacket) {
        return spellVisualPacket.spell() == SpellVisualRules.Spell.ROSE_STARFALL && spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM;
    }

    private static void roseModelVertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, QizaiRoseStarfallModel.Point point, int n, float f) {
        vertexConsumer.m_252986_(pose.m_252922_(), (float)point.x(), (float)point.y(), (float)point.z()).m_6122_(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, SpellVisualRenderer.alphaByte(f)).m_5752_();
    }

    private static void modelVertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, ManorStandaloneSpellModels.Point point, int n, float f) {
        vertexConsumer.m_252986_(pose.m_252922_(), (float)point.x(), (float)point.y(), (float)point.z()).m_6122_(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, SpellVisualRenderer.alphaByte(f)).m_5752_();
    }

    private static int alphaByte(float f) {
        return Math.max(0, Math.min(255, Math.round(f * 255.0f)));
    }

    private static Entity owner(ClientLevel clientLevel, SpellVisualPacket spellVisualPacket) {
        Entity entity = clientLevel.m_6815_(spellVisualPacket.ownerId());
        return entity != null && entity.m_20148_().equals(spellVisualPacket.owner()) ? entity : null;
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, SpellVisualGeometry.Point point, int n, float f, float f2, float f3) {
        vertexConsumer.m_252986_(pose.m_252922_(), (float)point.horizontalX(), (float)point.verticalY(), (float)point.horizontalZ()).m_6122_(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, Math.round(f * 255.0f)).m_7421_(f2, f3).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(pose.m_252943_(), 0.0f, 1.0f, 0.0f).m_5752_();
    }

    private static /* synthetic */ void lambda$render$2(VertexConsumer vertexConsumer, PoseStack.Pose pose, SpellVisualGeometry.Point point, SpellVisualGeometry.Point point2, SpellVisualGeometry.Point point3, SpellVisualGeometry.Point point4, int n, float f, int n2) {
        int n3 = 16;
        int n4 = 2048;
        float f2 = ((float)(n2 % n3 * 128) + 1.0f) / (float)n4;
        float f3 = ((float)(n2 / n3 * 128) + 1.0f) / (float)n4;
        float f4 = f2 + 126.0f / (float)n4;
        float f5 = f3 + 126.0f / (float)n4;
        if (n2 == 255) {
            f2 = f4 = ((float)((n3 - 1) * 128) + 64.0f) / (float)n4;
            f5 = f4;
            f3 = f4;
        }
        SpellVisualRenderer.vertex(vertexConsumer, pose, point, n, f, f2, f5);
        SpellVisualRenderer.vertex(vertexConsumer, pose, point2, n, f, f2, f3);
        SpellVisualRenderer.vertex(vertexConsumer, pose, point3, n, f, f4, f3);
        SpellVisualRenderer.vertex(vertexConsumer, pose, point4, n, f, f4, f5);
    }

    private static /* synthetic */ double lambda$render$1(Vec3 vec3, Visual visual) {
        return visual.packet.start().m_82557_(vec3);
    }

    private static /* synthetic */ boolean lambda$render$0(boolean bl, Entity entity, Visual visual) {
        return !SpellVisualRules.hiddenForCamera(visual.packet.spell(), bl, entity != null && entity.m_20148_().equals(visual.packet.owner()));
    }

    private static void ftboss$renderWithoutSoftBloom(RenderLevelStageEvent renderLevelStageEvent) {
        GongdiSpellRenderer.render(ACTIVE, renderLevelStageEvent, world);
    }

    private static final class Visual
    implements OptimizedSpellVisual {
        private SpellVisualPacket packet;
        private final long born;
        private final boolean nativeLight;
        private long refreshed;

        private Visual(SpellVisualPacket spellVisualPacket, long l, boolean bl) {
            this.packet = spellVisualPacket;
            this.born = l;
            this.refreshed = l;
            this.nativeLight = bl;
        }

        private boolean same(SpellVisualPacket spellVisualPacket) {
            return this.packet.owner().equals(spellVisualPacket.owner()) && this.packet.spell() == spellVisualPacket.spell() && (this.packet.spell() != SpellVisualRules.Spell.ROSE_STARFALL || this.packet.start().m_82557_(spellVisualPacket.start()) < 0.01);
        }

        @Override
        public SpellVisualPacket packet() {
            return this.packet;
        }

        @Override
        public long born() {
            return this.born;
        }

        @Override
        public boolean nativeLight() {
            return this.nativeLight;
        }

        @Override
        public long refreshed() {
            return this.refreshed;
        }
    }
}

