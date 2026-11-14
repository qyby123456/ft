/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;
import net.farmingtales.ftboss.client.render.OptimizedSpellModelLibrary;
import net.farmingtales.ftboss.client.render.OptimizedSpellPlacement;
import net.farmingtales.ftboss.client.render.OptimizedSpellSafety;
import net.farmingtales.ftboss.client.render.OptimizedSpellTelegraphs;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.client.render.QingyiSpellRenderer;
import net.farmingtales.ftboss.client.render.ZheergenArcaneRenderer;
import net.farmingtales.ftboss.client.render.dandan.DandanSpellRenderer;
import net.farmingtales.ftboss.client.render.reapflat.FlatReapRenderer;
import net.farmingtales.ftboss.client.zicaiarcane.ArcaneRenderer;
import net.farmingtales.ftboss.dandaniron.client.IronRenderer;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public final class OptimizedSpellRenderer {
    private static final RenderType GLYPHS = RenderType.m_234338_((ResourceLocation)new ResourceLocation("ftboss", "textures/fx/spells/spell_atlas.png"));
    private static final RenderType MODELS = RenderType.m_234338_((ResourceLocation)new ResourceLocation("ftboss", "fx/spell_materials.png"));

    private OptimizedSpellRenderer() {
    }

    public static void render(List<?> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel) {
        Vec3 vec3;
        AABB aABB;
        float f2;
        Vec3 vec32;
        Vec3 vec33;
        Object object2;
        Object object3;
        int n3;
        Entity entity;
        int n4;
        list = QingyiSpellRenderer.renderAndFilter(list, renderLevelStageEvent, clientLevel);
        list = IronRenderer.exclude(list, renderLevelStageEvent, clientLevel);
        list = FlatReapRenderer.intercept(list, renderLevelStageEvent, clientLevel);
        list = ArcaneRenderer.consume(list, renderLevelStageEvent, clientLevel);
        list = ZheergenArcaneRenderer.dispatch(list, renderLevelStageEvent, clientLevel);
        list = DandanSpellRenderer.renderAndExclude(list, renderLevelStageEvent, clientLevel);
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || list.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel2 = minecraft.f_91073_;
        if (clientLevel2 == null || clientLevel2 != clientLevel) {
            return;
        }
        Vec3 vec34 = renderLevelStageEvent.getCamera().m_90583_();
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        Entity entity2 = renderLevelStageEvent.getCamera().m_90592_();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n5 = particleStatus == ParticleStatus.MINIMAL ? 4 : (n4 = particleStatus == ParticleStatus.DECREASED ? 8 : 12);
        int n6 = particleStatus == ParticleStatus.MINIMAL ? 12 : (particleStatus == ParticleStatus.DECREASED ? 24 : 48);
        float f3 = minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick();
        List<OptimizedSpellVisual> list2 = list.stream().map(object -> (OptimizedSpellVisual)object).filter(optimizedSpellVisual -> optimizedSpellVisual.packet().valid() && clientLevel2.m_46472_().m_135782_().equals((Object)optimizedSpellVisual.packet().dimension())).sorted(Comparator.comparingDouble(optimizedSpellVisual -> optimizedSpellVisual.packet().start().m_82557_(vec34))).limit(n6).toList();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        Matrix4f matrix4f = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        Frame frame = new Frame(matrix4f, vec34, bl, particleStatus == ParticleStatus.MINIMAL ? 10000 : (particleStatus == ParticleStatus.DECREASED ? 24000 : 48000));
        for (AbstractClientPlayer object42 : clientLevel2.m_6907_()) {
            if (!(object42.m_20318_(f3).m_82557_(vec34) < 4096.0)) continue;
            frame.exclude(object42.m_20191_().m_82400_(0.12));
        }
        for (OptimizedSpellVisual optimizedSpellVisual2 : list2) {
            entity = OptimizedSpellRenderer.owner(clientLevel2, optimizedSpellVisual2.packet());
            if (entity == null) continue;
            frame.exclude(entity.m_20191_().m_82400_(0.12));
        }
        HashSet hashSet = new HashSet();
        ArrayList<Runnable> arrayList = new ArrayList<Runnable>();
        entity = bufferSource.m_6299_(MODELS);
        for (n3 = Math.min(n4, list2.size()) - 1; n3 >= 0; --n3) {
            OptimizedSpellModelLibrary.Model model;
            OptimizedSpellModelLibrary.Model model2;
            OptimizedSpellVisual optimizedSpellVisual3 = list2.get(n3);
            object3 = optimizedSpellVisual3.packet();
            object2 = OptimizedSpellRenderer.owner(clientLevel2, (SpellVisualPacket)object3);
            double d = (float)(clientLevel2.m_46467_() - optimizedSpellVisual3.born()) + f3;
            double d2 = (float)(clientLevel2.m_46467_() - optimizedSpellVisual3.refreshed()) + f3;
            float f4 = SpellVisualRules.modelOpacity(d2, ((SpellVisualPacket)object3).spell().lifetime(((SpellVisualPacket)object3).phase())) * 0.88f;
            if (f4 <= 0.0f) continue;
            boolean bl2 = ((SpellVisualPacket)object3).phase() == SpellVisualRules.Phase.BEAM;
            OptimizedSpellModelLibrary.Model model3 = OptimizedSpellModelLibrary.find(((SpellVisualPacket)object3).spell().id);
            boolean bl3 = ((SpellVisualPacket)object3).spell().followsCaster(((SpellVisualPacket)object3).phase()) || model3 != null && OptimizedSpellPlacement.casterSpace(model3.scene());
            vec33 = !bl2 && bl3 && object2 != null ? object2.m_20318_(f3) : ((SpellVisualPacket)object3).start();
            vec32 = bl2 ? OptimizedSpellRenderer.clip(clientLevel2, (Entity)object2, ((SpellVisualPacket)object3).start(), ((SpellVisualPacket)object3).end()) : vec33;
            f2 = (float)SpellVisuals.radiusScale((Entity)object2, ((SpellVisualPacket)object3).spell().id);
            if (!Float.isFinite(f2) || f2 <= 0.0f) {
                f2 = 1.0f;
            }
            if ((aABB = new AABB(vec33, vec32).m_82400_(Math.max(16.0, ((SpellVisualPacket)object3).spell().radius * (double)f2))).m_272282_(vec34) > 4096.0 || !renderLevelStageEvent.getFrustum().m_113029_(aABB)) continue;
            vec3 = vec32.m_82546_(((SpellVisualPacket)object3).start());
            if (bl2) {
                double d3 = vec3.m_82553_();
                if (d3 < 0.015 || d3 > 128.0) continue;
                Transform transform = new Transform(((SpellVisualPacket)object3).start(), vec3, true, f2, 0.0);
                int n7 = frame.written;
                model2 = OptimizedSpellModelLibrary.find(((SpellVisualPacket)object3).spell().id);
                if (model2 != null && model2.beamBody().faces() > 0) {
                    frame.beam((VertexConsumer)entity, transform, model2.beamBody(), f4, d3, d);
                    if (particleStatus == ParticleStatus.ALL && n3 < 4) {
                        arrayList.add(() -> OptimizedSpellRenderer.lambda$render$3(frame, model2, (VertexConsumer)entity, transform, f4, d3, d));
                    }
                }
                if (frame.written <= n7) continue;
                hashSet.add(n3);
                continue;
            }
            if (false && entity2 != null && ((SpellVisualPacket)object3).owner().equals(entity2.m_20148_()) && ((SpellVisualPacket)object3).spell().attached || (model = OptimizedSpellModelLibrary.find(((SpellVisualPacket)object3).spell().id)) == null) continue;
            Vec3 vec35 = ((SpellVisualPacket)object3).end().m_82546_(((SpellVisualPacket)object3).start());
            if (object2 != null && (((SpellVisualPacket)object3).spell().form.name().equals("WINGS") || vec35.f_82479_ * vec35.f_82479_ + vec35.f_82481_ * vec35.f_82481_ < 1.0E-5)) {
                vec35 = object2.m_20252_(f3);
            }
            double d4 = model.scene().effect().equals("VORTEX") ? d * 0.024 : (model.scene().effect().equals("ORBIT") ? d * 0.014 : 0.0);
            model2 = OptimizedSpellRenderer.ground(clientLevel2, (Entity)object2, vec33, false);
            Transform transform = new Transform((Vec3)model2, vec35, false, OptimizedSpellPlacement.scale(model, f2, d), d4);
            transform.animate(((SpellVisualPacket)object3).spell(), ((SpellVisualPacket)object3).phase(), d);
            transform.place(model, object2 != null && bl3 ? (double)object2.m_20205_() : 0.6, object2 != null && bl3 ? (double)object2.m_20206_() : 1.8);
            float f5 = f4 * (bl ? transform.hullOpacity(model, frame.camera) : 1.0f);
            frame.group((VertexConsumer)entity, transform, model.body(), f5);
            if (particleStatus != ParticleStatus.ALL || n3 >= 4 || !(vec33.m_82557_(vec34) < 144.0)) continue;
            arrayList.add(() -> OptimizedSpellRenderer.lambda$render$4(frame, model, (VertexConsumer)entity, transform, f5));
        }
        for (n3 = arrayList.size() - 1; n3 >= 0; --n3) {
            ((Runnable)arrayList.get(n3)).run();
        }
        bufferSource.m_109912_(MODELS);
        VertexConsumer vertexConsumer = bufferSource.m_6299_(GLYPHS);
        for (int i = list2.size() - 1; i >= 0; --i) {
            object3 = list2.get(i);
            object2 = object3.packet();
            Entity entity3 = OptimizedSpellRenderer.owner(clientLevel2, (SpellVisualPacket)object2);
            double d = (float)(clientLevel2.m_46467_() - object3.born()) + f3;
            float f6 = SpellVisualRules.opacity((float)(clientLevel2.m_46467_() - object3.refreshed()) + f3, ((SpellVisualPacket)object2).spell().lifetime(((SpellVisualPacket)object2).phase()));
            if (f6 <= 0.0f) continue;
            boolean bl4 = ((SpellVisualPacket)object2).phase() == SpellVisualRules.Phase.BEAM;
            OptimizedSpellModelLibrary.Model model = OptimizedSpellModelLibrary.find(((SpellVisualPacket)object2).spell().id);
            boolean bl5 = ((SpellVisualPacket)object2).spell().followsCaster(((SpellVisualPacket)object2).phase()) || model != null && OptimizedSpellPlacement.casterSpace(model.scene());
            Vec3 vec36 = !bl4 && bl5 && entity3 != null ? entity3.m_20318_(f3) : ((SpellVisualPacket)object2).start();
            vec33 = bl4 ? OptimizedSpellRenderer.clip(clientLevel2, entity3, ((SpellVisualPacket)object2).start(), ((SpellVisualPacket)object2).end()) : ((SpellVisualPacket)object2).end();
            vec32 = vec33.m_82546_(((SpellVisualPacket)object2).start());
            if (!bl4 && entity3 != null && vec32.f_82479_ * vec32.f_82479_ + vec32.f_82481_ * vec32.f_82481_ < 1.0E-5) {
                vec32 = entity3.m_20252_(f3);
            }
            if (!Float.isFinite(f2 = (float)SpellVisuals.radiusScale(entity3, ((SpellVisualPacket)object2).spell().id)) || f2 <= 0.0f) {
                f2 = 1.0f;
            }
            if ((aABB = new AABB(vec36, bl4 ? vec33 : vec36).m_82400_(Math.max(16.0, ((SpellVisualPacket)object2).spell().radius * (double)f2))).m_272282_(vec34) > 4096.0 || !renderLevelStageEvent.getFrustum().m_113029_(aABB)) continue;
            vec3 = bl4 ? ((SpellVisualPacket)object2).start() : OptimizedSpellRenderer.ground(clientLevel2, entity3, vec36, true);
            Transform transform = new Transform(vec3, vec32, bl4, f2, 0.0);
            boolean bl6 = hashSet.contains(i);
            OptimizedSpellTelegraphs.draw(((SpellVisualPacket)object2).spell(), ((SpellVisualPacket)object2).phase(), d, f6, vec32.m_82553_(), bl6, (point, point2, point3, point4, n, f, n2) -> frame.textured(vertexConsumer, transform, frame.corners(point.horizontalX(), point.verticalY(), point.horizontalZ(), point2.horizontalX(), point2.verticalY(), point2.horizontalZ(), point3.horizontalX(), point3.verticalY(), point3.horizontalZ(), point4.horizontalX(), point4.verticalY(), point4.horizontalZ()), n, f, n2));
        }
        bufferSource.m_109912_(GLYPHS);
    }

    private static Entity owner(ClientLevel clientLevel, SpellVisualPacket spellVisualPacket) {
        Entity entity = clientLevel.m_6815_(spellVisualPacket.ownerId());
        return entity != null && entity.m_20148_().equals(spellVisualPacket.owner()) ? entity : null;
    }

    private static Vec3 clip(ClientLevel clientLevel, Entity entity, Vec3 vec3, Vec3 vec32) {
        BlockHitResult blockHitResult = clientLevel.m_45547_(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        return blockHitResult.m_6662_() == HitResult.Type.MISS ? vec32 : blockHitResult.m_82450_();
    }

    private static Vec3 ground(ClientLevel clientLevel, Entity entity, Vec3 vec3, boolean bl) {
        BlockHitResult blockHitResult = clientLevel.m_45547_(new ClipContext(vec3.m_82520_(0.0, 0.3, 0.0), vec3.m_82520_(0.0, -1.5, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        if (blockHitResult.m_6662_() == HitResult.Type.MISS) {
            return vec3;
        }
        return new Vec3(vec3.f_82479_, bl ? blockHitResult.m_82450_().f_82480_ : Math.max(vec3.f_82480_, blockHitResult.m_82450_().f_82480_), vec3.f_82481_);
    }

    private static /* synthetic */ void lambda$render$4(Frame frame, OptimizedSpellModelLibrary.Model model, VertexConsumer vertexConsumer, Transform transform, float f) {
        if (frame.written + model.detail().faces() < frame.solidLimit) {
            frame.group(vertexConsumer, transform, model.detail(), f);
        }
    }

    private static /* synthetic */ void lambda$render$3(Frame frame, OptimizedSpellModelLibrary.Model model, VertexConsumer vertexConsumer, Transform transform, float f, double d, double d2) {
        if (frame.written + model.beamDetail().faces() < frame.solidLimit) {
            frame.beam(vertexConsumer, transform, model.beamDetail(), f, d, d2);
        }
    }

    private static final class Frame {
        private final Matrix4f matrix;
        private final Matrix3f normal = new Matrix3f();
        private final double[] camera;
        private final boolean firstPerson;
        private final int limit;
        private final int solidLimit;
        private int written;
        private final double[][] local = new double[4][3];
        private final double[][] transformed = new double[4][3];
        private final double[] authoredUv = new double[8];
        private final List<double[]> actors = new ArrayList<double[]>();
        private boolean bodyPass;

        private void exclude(AABB aABB) {
            double[] dArray = new double[]{aABB.f_82288_, aABB.f_82289_, aABB.f_82290_, aABB.f_82291_, aABB.f_82292_, aABB.f_82293_};
            for (double[] dArray2 : this.actors) {
                if (!Arrays.equals(dArray2, dArray)) continue;
                return;
            }
            this.actors.add(dArray);
        }

        private Frame(Matrix4f matrix4f, Vec3 vec3, boolean bl, int n) {
            this.matrix = matrix4f;
            this.camera = new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
            this.firstPerson = bl;
            this.limit = n;
            this.solidLimit = n - (n <= 3500 ? 700 : (n <= 6500 ? 1200 : 2200));
        }

        private void group(VertexConsumer vertexConsumer, Transform transform, OptimizedSpellModelLibrary.Group group, float f) {
            this.bodyPass = true;
            for (int i = 0; i < group.faces() && this.written < this.solidLimit; ++i) {
                for (int j = 0; j < 4; ++j) {
                    System.arraycopy(group.vertices(), i * 12 + j * 3, this.local[j], 0, 3);
                }
                if (group.bones()[i] != 0) {
                    transform.animatePart(this.local, group.bones()[i], group.pivots(), i * 3);
                }
                System.arraycopy(group.uvs(), i * 8, this.authoredUv, 0, 8);
                this.emit(vertexConsumer, transform.world(this.local, this.transformed), group.colors()[i * 4], f * group.alphas()[i], group.materials()[i] + 256, this.authoredUv);
            }
            this.bodyPass = false;
        }

        private void beam(VertexConsumer vertexConsumer, Transform transform, OptimizedSpellModelLibrary.Group group, float f, double d, double d2) {
            double d3 = d2 * 0.07;
            double d4 = Math.cos(d3);
            double d5 = Math.sin(d3);
            for (int i = 0; i < group.faces() && this.written < this.solidLimit; ++i) {
                for (int j = 0; j < 4; ++j) {
                    int n = i * 12 + j * 3;
                    double d6 = group.vertices()[n];
                    double d7 = group.vertices()[n + 1];
                    this.local[j][0] = d6 * d4 - d7 * d5;
                    this.local[j][1] = d6 * d5 + d7 * d4;
                    this.local[j][2] = Math.max(0.0, Math.min(d, group.vertices()[n + 2] * d));
                }
                System.arraycopy(group.uvs(), i * 8, this.authoredUv, 0, 8);
                this.emit(vertexConsumer, transform.world(this.local, this.transformed), group.colors()[i * 4], f * group.alphas()[i], group.materials()[i] + 256, this.authoredUv);
            }
        }

        private void solid(VertexConsumer vertexConsumer, Transform transform, double[][] dArray, int n, float f, Set<Long> set, double d) {
            if (this.written >= this.solidLimit || OptimizedSpellSafety.area(dArray) < 1.0E-6 || !set.add(OptimizedSpellSafety.signature(dArray))) {
                return;
            }
            for (double[] dArray2 : dArray) {
                dArray2[2] = Math.max(0.0, Math.min(d, dArray2[2]));
            }
            this.emit(vertexConsumer, transform.world(dArray, this.transformed), n, f, -1);
        }

        private void textured(VertexConsumer vertexConsumer, Transform transform, double[][] dArray, int n, float f, int n2) {
            if (this.written < this.limit) {
                this.emit(vertexConsumer, transform.world(dArray, this.transformed), n, f, n2);
            }
        }

        private double[][] corners(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12) {
            this.local[0][0] = d;
            this.local[0][1] = d2;
            this.local[0][2] = d3;
            this.local[1][0] = d4;
            this.local[1][1] = d5;
            this.local[1][2] = d6;
            this.local[2][0] = d7;
            this.local[2][1] = d8;
            this.local[2][2] = d9;
            this.local[3][0] = d10;
            this.local[3][1] = d11;
            this.local[3][2] = d12;
            return this.local;
        }

        private void emit(VertexConsumer vertexConsumer, double[][] dArray, int n, float f, int n2) {
            this.emit(vertexConsumer, dArray, n, f, n2, null);
        }

        private void emit(VertexConsumer vertexConsumer, double[][] dArray, int n, float f, int n2, double[] dArray2) {
            double d;
            double d2;
            int n3;
            double d3 = OptimizedSpellSafety.distanceSquared(dArray, this.camera);
            if (d3 < 0.0 || this.written >= this.limit) {
                return;
            }
            if (d3 < 1.21 && (n3 = Math.min(96, (int)Math.ceil(Math.max(d2 = Frame.edge(dArray[0], dArray[1]) + Frame.edge(dArray[3], dArray[2]), d = Frame.edge(dArray[0], dArray[3]) + Frame.edge(dArray[1], dArray[2])) / 1.3))) > 1) {
                for (int i = 0; i < n3 && this.written < this.limit; ++i) {
                    double[][] dArrayArray;
                    double d4 = (double)i / (double)n3;
                    double d5 = (double)(i + 1) / (double)n3;
                    if (d2 >= d) {
                        double[][] dArrayArray2 = new double[4][];
                        dArrayArray2[0] = Frame.lerp(dArray[0], dArray[1], d4);
                        dArrayArray2[1] = Frame.lerp(dArray[0], dArray[1], d5);
                        dArrayArray2[2] = Frame.lerp(dArray[3], dArray[2], d5);
                        dArrayArray = dArrayArray2;
                        dArrayArray2[3] = Frame.lerp(dArray[3], dArray[2], d4);
                    } else {
                        double[][] dArrayArray3 = new double[4][];
                        dArrayArray3[0] = Frame.lerp(dArray[0], dArray[3], d4);
                        dArrayArray3[1] = Frame.lerp(dArray[1], dArray[2], d4);
                        dArrayArray3[2] = Frame.lerp(dArray[1], dArray[2], d5);
                        dArrayArray = dArrayArray3;
                        dArrayArray3[3] = Frame.lerp(dArray[0], dArray[3], d5);
                    }
                    double[][] dArrayArray4 = dArrayArray;
                    this.face(vertexConsumer, dArrayArray4, n, f, n2, Frame.subUv(dArray2, d4, d5, d2 >= d));
                }
                return;
            }
            this.face(vertexConsumer, dArray, n, f, n2, dArray2);
        }

        private void face(VertexConsumer vertexConsumer, double[][] dArray, int n, float f, int n2, double[] dArray2) {
            int n3;
            float f2 = f * OptimizedSpellSafety.cameraOpacity(dArray, this.camera, this.firstPerson);
            if (this.bodyPass) {
                for (double[] dArray3 : this.actors) {
                    if (!((double)(f2 *= OptimizedSpellPlacement.actorOpacity(dArray, dArray3)) < 0.012)) continue;
                    return;
                }
            }
            if ((double)f2 < 0.012 || this.written >= this.limit || OptimizedSpellSafety.area(dArray) < 1.0E-6) {
                return;
            }
            float f3 = (float)(n2 % 16 * 128 + 1) / 2048.0f;
            float f4 = (float)(n2 / 16 * 128 + 1) / 2048.0f;
            float f5 = f3 + 0.061523438f;
            float f6 = f4 + 0.061523438f;
            if (n2 == 255) {
                f6 = 0.96875f;
                f4 = 0.96875f;
                f5 = 0.96875f;
                f3 = 0.96875f;
            }
            if (n2 >= 256) {
                n3 = n2 - 256;
                f3 = (float)(n3 % 64 * 16 + 2) / 1024.0f;
                f4 = (float)(n3 / 64 * 16 + 2) / 1024.0f;
                f5 = f3 + 0.0107421875f;
                f6 = f4 + 0.0107421875f;
            }
            for (n3 = 0; n3 < 4; ++n3) {
                float f7;
                float f8;
                VertexConsumer vertexConsumer2 = vertexConsumer.m_252986_(this.matrix, (float)(dArray[n3][0] - this.camera[0]), (float)(dArray[n3][1] - this.camera[1]), (float)(dArray[n3][2] - this.camera[2])).m_6122_(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, Math.min(255, Math.round(f2 * 255.0f)));
                float f9 = dArray2 == null ? (n3 < 2 ? f3 : f5) : (f8 = (float)((double)f3 + (double)(f5 - f3) * dArray2[n3 * 2]));
                float f10 = dArray2 == null ? (n3 == 0 || n3 == 3 ? f6 : f4) : (f7 = (float)((double)f4 + (double)(f6 - f4) * dArray2[n3 * 2 + 1]));
                if (n2 >= 0) {
                    vertexConsumer2.m_7421_(f8, f7).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(this.normal, 0.0f, 1.0f, 0.0f);
                }
                vertexConsumer2.m_5752_();
            }
            ++this.written;
        }

        private static double edge(double[] dArray, double[] dArray2) {
            return Math.sqrt(Math.pow(dArray[0] - dArray2[0], 2.0) + Math.pow(dArray[1] - dArray2[1], 2.0) + Math.pow(dArray[2] - dArray2[2], 2.0));
        }

        private static double[] lerp(double[] dArray, double[] dArray2, double d) {
            return new double[]{dArray[0] + (dArray2[0] - dArray[0]) * d, dArray[1] + (dArray2[1] - dArray[1]) * d, dArray[2] + (dArray2[2] - dArray[2]) * d};
        }

        private static double[] subUv(double[] dArray, double d, double d2, boolean bl) {
            double[] dArray2;
            int[] nArray;
            int[] nArray2;
            if (dArray == null) {
                return null;
            }
            if (bl) {
                int[] nArray3 = new int[4];
                nArray3[0] = 0;
                nArray3[1] = 0;
                nArray3[2] = 3;
                nArray2 = nArray3;
                nArray3[3] = 3;
            } else {
                int[] nArray4 = new int[4];
                nArray4[0] = 0;
                nArray4[1] = 1;
                nArray4[2] = 1;
                nArray2 = nArray4;
                nArray4[3] = 0;
            }
            int[] nArray5 = nArray2;
            if (bl) {
                int[] nArray6 = new int[4];
                nArray6[0] = 1;
                nArray6[1] = 1;
                nArray6[2] = 2;
                nArray = nArray6;
                nArray6[3] = 2;
            } else {
                int[] nArray7 = new int[4];
                nArray7[0] = 3;
                nArray7[1] = 2;
                nArray7[2] = 2;
                nArray = nArray7;
                nArray7[3] = 3;
            }
            int[] nArray8 = nArray;
            if (bl) {
                double[] dArray3 = new double[4];
                dArray3[0] = d;
                dArray3[1] = d2;
                dArray3[2] = d2;
                dArray2 = dArray3;
                dArray3[3] = d;
            } else {
                double[] dArray4 = new double[4];
                dArray4[0] = d;
                dArray4[1] = d;
                dArray4[2] = d2;
                dArray2 = dArray4;
                dArray4[3] = d2;
            }
            double[] dArray5 = dArray2;
            double[] dArray6 = new double[8];
            for (int i = 0; i < 4; ++i) {
                for (int j = 0; j < 2; ++j) {
                    int n = nArray5[i] * 2 + j;
                    dArray6[i * 2 + j] = dArray[n] + (dArray[nArray8[i] * 2 + j] - dArray[n]) * dArray5[i];
                }
            }
            return dArray6;
        }
    }

    private static final class Transform {
        private static final Set<String> WEAPON_FORMS = Set.of("CLEAVE", "REAP", "OATH", "VERDICT", "SERAPH");
        private static final double[] WEAPON_TIMES = new double[]{0.0, 0.75, 1.375, 1.9583};
        private static final double[] WEAPON_PITCH = new double[]{0.0, -115.9351, 134.06, 0.0};
        private static final double[] WEAPON_ROLL = new double[]{0.0, -65.81454, -65.81, 0.0};
        private Vec3 origin;
        private final double[] facing;
        private final Quaternionf rotation;
        private final double scale;
        private final double depthScale;
        private double wingAngle;
        private double motionAge;
        private final Vector3f scratch = new Vector3f();

        private void animate(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase, double d) {
            double d2;
            double d3;
            this.motionAge = Math.max(0.0, d);
            double d4 = phase == SpellVisualRules.Phase.RELEASE ? -0.18 * Math.exp(-Math.max(0.0, d) * 0.42) : 0.0;
            double d5 = switch (spell.form.name()) {
                case "CLEAVE", "REAP", "OATH", "VERDICT", "SERAPH" -> 0.045 * Math.sin(d * 0.14) + d4;
                case "DOLL", "PROCESSION" -> 0.05 * Math.sin(d * 0.18 + (double)spell.id.hashCode() * 0.01);
                case "FEATHERS", "WINGS", "REFUGE" -> 0.03 * Math.sin(d * 0.2);
                default -> 0.0;
            };
            this.rotation.rotateZ((float)d5);
            if (WEAPON_FORMS.contains(spell.form.name()) && (phase == SpellVisualRules.Phase.CHARGE || phase == SpellVisualRules.Phase.RELEASE)) {
                double d6 = Math.min(1.0, Math.max(0.0, d) / (double)spell.lifetime(phase));
                d3 = phase == SpellVisualRules.Phase.CHARGE ? d6 * 0.75 : 0.75 + d6 * 1.2083;
                double d7 = Transform.keyframe(d3, WEAPON_TIMES, WEAPON_PITCH);
                double d8 = Transform.keyframe(d3, WEAPON_TIMES, WEAPON_ROLL);
                this.rotation.rotateX((float)Math.toRadians(d7 * 0.055)).rotateZ((float)Math.toRadians(d8 * 0.12));
            }
            d3 = (d2 = Math.max(0.0, d) % 5.0 / 5.0) < 0.5 ? d2 * 2.0 : 2.0 - d2 * 2.0;
            this.wingAngle = Math.toRadians((d3 - 0.5) * 70.0 * 0.35);
        }

        private static double keyframe(double d, double[] dArray, double[] dArray2) {
            for (int i = 1; i < dArray.length; ++i) {
                if (!(d <= dArray[i])) continue;
                double d2 = Math.max(0.0, Math.min(1.0, (d - dArray[i - 1]) / (dArray[i] - dArray[i - 1])));
                return dArray2[i - 1] + (dArray2[i] - dArray2[i - 1]) * d2;
            }
            return dArray2[dArray2.length - 1];
        }

        private void place(OptimizedSpellModelLibrary.Model model, double d, double d2) {
            double[] dArray = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
            double[] dArray2 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
            for (int i = 0; i < 8; ++i) {
                Vector3f vector3f = this.rotation.transform(this.scratch.set((float)((i % 2 == 0 ? model.minimum()[0] : model.maximum()[0]) * this.scale), (float)(((i & 2) == 0 ? model.minimum()[1] : model.maximum()[1]) * this.scale), (float)(((i & 4) == 0 ? model.minimum()[2] : model.maximum()[2]) * this.scale)));
                double[] dArray3 = new double[]{(double)vector3f.x * this.facing[2] - (double)vector3f.z * this.facing[0], vector3f.y, (double)vector3f.x * this.facing[0] + (double)vector3f.z * this.facing[2]};
                for (int j = 0; j < 3; ++j) {
                    dArray[j] = Math.min(dArray[j], dArray3[j]);
                    dArray2[j] = Math.max(dArray2[j], dArray3[j]);
                }
            }
            double[] dArray4 = OptimizedSpellPlacement.offset(model.scene(), dArray, dArray2, d, d2);
            this.origin = this.origin.m_82520_(this.facing[2] * dArray4[0] + this.facing[0] * dArray4[2], dArray4[1], -this.facing[0] * dArray4[0] + this.facing[2] * dArray4[2]);
        }

        private void animatePart(double[][] dArray, int n, double[] dArray2, int n2) {
            if (n >= 8) {
                int n3 = n == 8 ? 2 : 1;
                double d = n == 8 ? 0.65 * Math.sin(this.motionAge * 0.16) : 0.45 * Math.cos(this.motionAge * 0.13);
                for (double[] dArray3 : dArray) {
                    int n4 = n3;
                    dArray3[n4] = dArray3[n4] + d;
                }
                return;
            }
            boolean bl = n <= 2;
            double d = switch (n) {
                case 1 -> -this.wingAngle;
                case 2 -> this.wingAngle;
                case 3 -> this.motionAge * 0.045;
                case 4 -> this.motionAge * 0.065;
                case 5 -> this.motionAge * 0.018;
                case 7 -> -this.motionAge * 0.038;
                default -> 0.0;
            };
            int n5 = bl ? 2 : 1;
            double d2 = Math.cos(d);
            double d3 = Math.sin(d);
            double d4 = dArray2[n2];
            double d5 = dArray2[n2 + n5];
            for (double[] dArray4 : dArray) {
                double d6 = dArray4[0] - d4;
                double d7 = dArray4[n5] - d5;
                dArray4[0] = d4 + d6 * d2 + (double)(bl ? 1 : -1) * d7 * d3;
                dArray4[n5] = d5 + (double)(bl ? -1 : 1) * d6 * d3 + d7 * d2;
            }
        }

        private Transform(Vec3 vec3, Vec3 vec32, boolean bl, double d, double d2) {
            this.origin = vec3;
            this.scale = d;
            this.depthScale = bl ? 1.0 : d;
            double[] dArray = OptimizedSpellSafety.direction(vec32.f_82479_, bl ? vec32.f_82480_ : 0.0, vec32.f_82481_);
            this.facing = OptimizedSpellSafety.direction(vec32.f_82479_, 0.0, vec32.f_82481_);
            this.rotation = new Quaternionf().rotationTo(0.0f, 0.0f, 1.0f, (float)dArray[0], (float)dArray[1], (float)dArray[2]);
            if (!bl && d2 != 0.0) {
                this.rotation.rotateY((float)d2);
            }
        }

        private double[][] world(double[][] dArray, double[][] dArray2) {
            for (int i = 0; i < 4; ++i) {
                Vector3f vector3f = this.rotation.transform(this.scratch.set((float)(dArray[i][0] * this.scale), (float)(dArray[i][1] * this.scale), (float)(dArray[i][2] * this.depthScale)));
                dArray2[i][0] = this.origin.f_82479_ + (double)vector3f.x;
                dArray2[i][1] = this.origin.f_82480_ + (double)vector3f.y;
                dArray2[i][2] = this.origin.f_82481_ + (double)vector3f.z;
            }
            return dArray2;
        }

        private float hullOpacity(OptimizedSpellModelLibrary.Model model, double[] dArray) {
            if (FirstPersonSpellVisibility.firstPerson()) {
                return 1.0f;
            }
            Vector3f vector3f = new Quaternionf((Quaternionfc)this.rotation).conjugate().transform(new Vector3f((float)(dArray[0] - this.origin.f_82479_), (float)(dArray[1] - this.origin.f_82480_), (float)(dArray[2] - this.origin.f_82481_)));
            double[] dArray2 = new double[]{(double)vector3f.x / this.scale, (double)vector3f.y / this.scale, (double)vector3f.z / this.depthScale};
            double d = 0.0;
            for (int i = 0; i < 3; ++i) {
                double d2 = Math.max(0.0, Math.max(model.minimum()[i] - dArray2[i], dArray2[i] - model.maximum()[i])) * this.scale;
                d += d2 * d2;
            }
            double d3 = Math.min(1.0, Math.sqrt(d) / 0.5);
            return (float)(0.16 + 0.84 * d3 * d3 * (3.0 - 2.0 * d3));
        }
    }
}

