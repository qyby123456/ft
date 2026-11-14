/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaterniond
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.render.GongdiSpellGlowType;
import net.farmingtales.ftboss.client.render.GongdiSpellModel;
import net.farmingtales.ftboss.client.render.GongdiVerdictPlacement;
import net.farmingtales.ftboss.client.render.OptimizedSpellRenderer;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Vector3f;

public final class GongdiSpellRenderer {
    private GongdiSpellRenderer() {
    }

    private static Map<String, RenderType> surfaces() {
        LinkedHashMap<String, RenderType> linkedHashMap = new LinkedHashMap<String, RenderType>();
        for (String string : List.of("metal", "wave", "cut", "blood", "blood_fine", "trace", "glow")) {
            ResourceLocation resourceLocation = new ResourceLocation("ftboss", "textures/fx/gongdi/" + string + ".png");
            linkedHashMap.put(string, string.equals("glow") ? GongdiSpellGlowType.layer(resourceLocation) : RenderType.m_234338_((ResourceLocation)resourceLocation));
        }
        return Map.copyOf(linkedHashMap);
    }

    public static boolean owns(String string) {
        return string.equals("rending_cleave") || string.equals("crimson_oath") || string.equals("blood_verdict");
    }

    public static void render(List<?> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel) {
        SpellVisualPacket spellVisualPacket;
        OptimizedSpellVisual optimizedSpellVisual2;
        Object object22;
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            OptimizedSpellRenderer.render(list, renderLevelStageEvent, clientLevel);
            return;
        }
        ArrayList arrayList = new ArrayList();
        ArrayList<OptimizedSpellVisual> arrayList2 = new ArrayList<OptimizedSpellVisual>();
        for (Object object22 : list) {
            if (object22 instanceof OptimizedSpellVisual) {
                optimizedSpellVisual2 = (OptimizedSpellVisual)object22;
                if (GongdiSpellRenderer.owns(optimizedSpellVisual2.packet().spell().id)) {
                    arrayList2.add(optimizedSpellVisual2);
                    continue;
                }
            }
            arrayList.add(object22);
        }
        OptimizedSpellRenderer.render(arrayList, renderLevelStageEvent, clientLevel);
        if (arrayList2.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        object22 = minecraft.f_91073_;
        if (object22 == null || object22 != clientLevel) {
            return;
        }
        optimizedSpellVisual2 = renderLevelStageEvent.getCamera().m_90583_();
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        Entity entity = renderLevelStageEvent.getCamera().m_90592_();
        float f = minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n = particleStatus == ParticleStatus.MINIMAL ? 1 : (particleStatus == ParticleStatus.DECREASED ? 2 : 4);
        arrayList2.removeIf(optimizedSpellVisual -> !optimizedSpellVisual.packet().valid() || !object22.m_46472_().m_135782_().equals((Object)optimizedSpellVisual.packet().dimension()));
        arrayList2.sort(Comparator.comparingDouble(arg_0 -> GongdiSpellRenderer.lambda$render$1((Vec3)optimizedSpellVisual2, arg_0)));
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        Painter painter = new Painter(renderLevelStageEvent.getPoseStack().m_85850_().m_252922_(), (Vec3)optimizedSpellVisual2, bl, particleStatus == ParticleStatus.MINIMAL ? 14000 : (particleStatus == ParticleStatus.DECREASED ? 28000 : 56000));
        Vector3f vector3f = renderLevelStageEvent.getCamera().m_253058_();
        painter.look = new double[]{vector3f.x, vector3f.y, vector3f.z};
        if (entity != null && !bl) {
            painter.actor(GongdiSpellRenderer.bounds(entity.m_20191_()));
        }
        int n2 = 0;
        for (OptimizedSpellVisual object3 : arrayList2) {
            Vec3 vec3;
            GongdiSpellModel.Model model;
            boolean bl2;
            if (n2 >= n || painter.written >= painter.limit) break;
            spellVisualPacket = object3.packet();
            double d = Math.max(0.0f, (float)(object22.m_46467_() - object3.born()) + f);
            double d2 = Math.max(0.0f, (float)(object22.m_46467_() - object3.refreshed()) + f);
            double d3 = spellVisualPacket.spell().lifetime(spellVisualPacket.phase());
            boolean bl3 = bl2 = spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE;
            if (!bl2 && d >= d3 || d2 >= d3) continue;
            float f2 = (float)Math.min(1.0, (d3 - (bl2 ? d2 : d)) / Math.min(3.0, d3 * 0.25));
            Entity entity3 = object22.m_6815_(spellVisualPacket.ownerId());
            if (entity3 != null && !entity3.m_20148_().equals(spellVisualPacket.owner())) {
                entity3 = null;
            }
            if (entity3 != null) {
                painter.actor(GongdiSpellRenderer.bounds(entity3.m_20191_()));
            }
            if (spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM) {
                if (!spellVisualPacket.spell().id.equals("rending_cleave")) continue;
                model = object22.m_45547_(new ClipContext(spellVisualPacket.start(), spellVisualPacket.end(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity3));
                Vec3 vec32 = vec3 = model.m_6662_() == HitResult.Type.MISS ? spellVisualPacket.end() : model.m_82450_();
                if (spellVisualPacket.start().m_82557_(vec3) > 16384.0) continue;
                painter.beam("wave", spellVisualPacket.start(), vec3, 0.15, f2 * 0.74f);
                ++n2;
                continue;
            }
            model = GongdiSpellModel.find(spellVisualPacket.spell().id);
            vec3 = model.anchor().equals("CASTER") && entity3 != null ? entity3.m_20318_(f) : spellVisualPacket.start();
            if (vec3.m_82557_((Vec3)optimizedSpellVisual2) > 4096.0 || renderLevelStageEvent.getFrustum() != null && !renderLevelStageEvent.getFrustum().m_113029_(new AABB(vec3, vec3).m_82400_(14.0))) continue;
            for (Entity entity4 : object22.m_6249_(entity3, new AABB(vec3, vec3).m_82400_(8.0), entity2 -> entity2 instanceof LivingEntity && (!bl || entity2 != entity))) {
                painter.actor(GongdiSpellRenderer.bounds(entity4.m_20191_()));
            }
            Vec3 vec33 = spellVisualPacket.end().m_82546_(spellVisualPacket.start());
            if (vec33.f_82479_ * vec33.f_82479_ + vec33.f_82481_ * vec33.f_82481_ < 1.0E-5 && entity3 != null) {
                vec33 = entity3.m_20252_(f);
            }
            double d4 = Math.atan2(vec33.f_82479_, vec33.f_82481_);
            double d5 = entity3 != null ? Math.max(0.6, (double)Math.min(12.0f, entity3.m_20205_())) : 0.6;
            double d6 = SpellVisuals.radiusScale(entity3, spellVisualPacket.spell().id);
            double d7 = Double.isFinite(d6) && d6 > 0.0 ? Math.max(0.78, Math.min(1.65, d6)) : 1.0;
            for (GongdiSpellModel.Part part : model.parts()) {
                GongdiSpellModel.State state = GongdiSpellModel.sample(part, spellVisualPacket.phase().name(), Math.min(1.0, d / d3));
                if ((state = GongdiVerdictPlacement.stateForCamera(state, model, part, vec3, (Vec3)optimizedSpellVisual2, d4, d7, bl)).alpha() < 0.01) continue;
                Quaterniond quaterniond = GongdiSpellModel.rotation(state);
                double[][][] dArray = new double[part.faces().length][4][];
                double d8 = Double.POSITIVE_INFINITY;
                for (int i = 0; i < part.faces().length; ++i) {
                    for (int j = 0; j < 4; ++j) {
                        double[] dArray2 = GongdiSpellModel.pose(part.faces()[i].points()[j], part, state, quaterniond);
                        dArray[i][j] = dArray2;
                        d8 = Math.min(d8, dArray2[1]);
                    }
                }
                double d9 = Math.max(0.0, (part.telegraph() ? 0.04 : 0.12) - d8);
                double d10 = (double)part.sideSign() * Math.max(0.0, d5 / 2.0 - 0.3);
                double d11 = model.id().equals("rending_cleave") ? d5 / 2.0 + 0.48 : 0.0;
                double d12 = Math.sin(d4);
                double d13 = Math.cos(d4);
                for (int i = 0; i < part.faces().length; ++i) {
                    double[][] dArray3;
                    GongdiSpellModel.Face face = part.faces()[i];
                    for (double[] dArray4 : dArray3 = dArray[i]) {
                        double d14 = (dArray4[0] + d10) * d7;
                        double d15 = (dArray4[2] + d11) * d7;
                        dArray4[0] = vec3.f_82479_ + d14 * d13 + d15 * d12;
                        dArray4[1] = vec3.f_82480_ + (dArray4[1] + d9) * d7;
                        dArray4[2] = vec3.f_82481_ - d14 * d12 + d15 * d13;
                    }
                    painter.queue(face.material(), dArray3, face.uv(), face.tint(), (float)((double)f2 * state.alpha() * face.alpha()), GongdiVerdictPlacement.avoidActors(!part.telegraph(), model, part));
                }
            }
            ++n2;
        }
        for (Map.Entry entry : painter.batches.entrySet()) {
            painter.solidWeapon = ((String)entry.getKey()).equals("metal");
            spellVisualPacket = Surfaces.ALL.get(entry.getKey());
            VertexConsumer vertexConsumer = bufferSource.m_6299_((RenderType)spellVisualPacket);
            for (Painter.Draw draw : (List)entry.getValue()) {
                painter.draw(vertexConsumer, draw.points(), draw.uv(), draw.tint(), draw.alpha(), draw.avoidActors());
            }
            bufferSource.m_109912_((RenderType)spellVisualPacket);
        }
    }

    private static double[] bounds(AABB aABB) {
        return new double[]{aABB.f_82288_ - 0.035, aABB.f_82289_ - 0.03, aABB.f_82290_ - 0.035, aABB.f_82291_ + 0.035, aABB.f_82292_ + 0.04, aABB.f_82293_ + 0.035};
    }

    public static double separation(double[][] dArray, double[] dArray2) {
        double d = 0.0;
        for (int i = 0; i < 3; ++i) {
            double d2 = Double.POSITIVE_INFINITY;
            double d3 = Double.NEGATIVE_INFINITY;
            for (double[] dArray3 : dArray) {
                d2 = Math.min(d2, dArray3[i]);
                d3 = Math.max(d3, dArray3[i]);
            }
            double d4 = Math.max(0.0, Math.max(dArray2[i] - d3, d2 - dArray2[i + 3]));
            d += d4 * d4;
        }
        return Math.sqrt(d);
    }

    public static double[] blend(double[][] dArray, double d, double d2) {
        double[] dArray2 = new double[dArray[0].length];
        for (int i = 0; i < dArray2.length; ++i) {
            dArray2[i] = dArray[0][i] * (1.0 - d) * (1.0 - d2) + dArray[1][i] * d * (1.0 - d2) + dArray[2][i] * d * d2 + dArray[3][i] * (1.0 - d) * d2;
        }
        return dArray2;
    }

    private static /* synthetic */ double lambda$render$1(Vec3 vec3, OptimizedSpellVisual optimizedSpellVisual) {
        return optimizedSpellVisual.packet().start().m_82557_(vec3);
    }

    private static final class Painter {
        private final Matrix4f matrix;
        private final Matrix3f normal = new Matrix3f();
        private final double[] camera;
        private double[] look = new double[]{0.0, 0.0, 1.0};
        private boolean solidWeapon;
        private final double[] cameraBox;
        private final boolean firstPerson;
        private final int limit;
        private int written;
        private final List<double[]> actors = new ArrayList<double[]>();
        private final Map<String, List<Draw>> batches = new LinkedHashMap<String, List<Draw>>();

        private void actor(double[] dArray) {
            if (this.actors.size() < 96 && this.actors.stream().noneMatch(dArray2 -> Arrays.equals(dArray2, dArray))) {
                this.actors.add(dArray);
            }
        }

        private Painter(Matrix4f matrix4f, Vec3 vec3, boolean bl, int n) {
            this.matrix = matrix4f;
            this.camera = new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
            this.cameraBox = new double[]{this.camera[0], this.camera[1], this.camera[2], this.camera[0], this.camera[1], this.camera[2]};
            this.firstPerson = bl;
            this.limit = n;
        }

        private void queue(String string2, double[][] dArray, double[][] dArray2, int n, float f, boolean bl) {
            if ((double)f >= 0.009) {
                this.batches.computeIfAbsent(string2, string -> new ArrayList()).add(new Draw(dArray, dArray2, n, f, bl));
            }
        }

        private void draw(VertexConsumer vertexConsumer, double[][] dArray, double[][] dArray2, int n, float f, boolean bl) {
            if ((double)f < 0.009 || this.written >= this.limit) {
                return;
            }
            double d = GongdiSpellRenderer.separation(dArray, this.cameraBox);
            int n2 = 1;
            int n3 = 1;
            if (!this.firstPerson && d < 1.3) {
                n2 = Math.min(24, Math.max(1, (int)Math.ceil(Math.max(Painter.edge(dArray[0], dArray[1]), Painter.edge(dArray[3], dArray[2])) / 0.42)));
                n3 = Math.min(12, Math.max(1, (int)Math.ceil(Math.max(Painter.edge(dArray[0], dArray[3]), Painter.edge(dArray[1], dArray[2])) / 0.42)));
            }
            if (n2 == 1 && n3 == 1) {
                this.face(vertexConsumer, dArray, dArray2, n, f, bl);
                return;
            }
            for (int i = 0; i < n3 && this.written < this.limit; ++i) {
                for (int j = 0; j < n2 && this.written < this.limit; ++j) {
                    double d2 = (double)j / (double)n2;
                    double d3 = (double)(j + 1) / (double)n2;
                    double d4 = (double)i / (double)n3;
                    double d5 = (double)(i + 1) / (double)n3;
                    double[][] dArrayArray = new double[][]{GongdiSpellRenderer.blend(dArray, d2, d4), GongdiSpellRenderer.blend(dArray, d3, d4), GongdiSpellRenderer.blend(dArray, d3, d5), GongdiSpellRenderer.blend(dArray, d2, d5)};
                    double[][] dArrayArray2 = new double[][]{GongdiSpellRenderer.blend(dArray2, d2, d4), GongdiSpellRenderer.blend(dArray2, d3, d4), GongdiSpellRenderer.blend(dArray2, d3, d5), GongdiSpellRenderer.blend(dArray2, d2, d5)};
                    this.face(vertexConsumer, dArrayArray, dArrayArray2, n, f, bl);
                }
            }
        }

        private void face(VertexConsumer vertexConsumer, double[][] dArray, double[][] dArray2, int n, float f, boolean bl) {
            double[] dArray32;
            double d;
            double d2 = GongdiSpellRenderer.separation(dArray, this.cameraBox);
            double d3 = this.firstPerson ? 0.62 : 0.12;
            double d4 = d = this.firstPerson ? 1.17 : 0.3;
            if (!this.firstPerson) {
                f = (float)((double)f * Painter.smooth(d3, d, d2));
            }
            if (bl) {
                for (double[] dArray32 : this.actors) {
                    if (!(GongdiSpellRenderer.separation(dArray, dArray32) < 0.07)) continue;
                    return;
                }
            }
            if ((double)f < 0.009 || this.written >= this.limit) {
                return;
            }
            Object object = Painter.subtract(dArray[1], dArray[0]);
            dArray32 = Painter.subtract(dArray[3], dArray[0]);
            double[] dArray4 = new double[]{(double)(object[1] * dArray32[2] - object[2] * dArray32[1]), (double)(object[2] * dArray32[0] - object[0] * dArray32[2]), (double)(object[0] * dArray32[1] - object[1] * dArray32[0])};
            double[] dArray5 = dArray4;
            double d5 = Math.sqrt(dArray5[0] * dArray5[0] + dArray5[1] * dArray5[1] + dArray5[2] * dArray5[2]);
            if (d5 < 1.0E-7) {
                return;
            }
            for (int i = 0; i < 4; ++i) {
                vertexConsumer.m_252986_(this.matrix, (float)(dArray[i][0] - this.camera[0]), (float)(dArray[i][1] - this.camera[1]), (float)(dArray[i][2] - this.camera[2])).m_6122_(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, Math.min(255, Math.round(f * 255.0f))).m_7421_((float)dArray2[i][0], (float)dArray2[i][1]).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(this.normal, (float)(dArray5[0] / d5), (float)(dArray5[1] / d5), (float)(dArray5[2] / d5)).m_5752_();
            }
            ++this.written;
        }

        private void beam(String string, Vec3 vec3, Vec3 vec32, double d, float f) {
            double[] dArray;
            double[] dArray2 = new double[]{vec32.f_82479_ - vec3.f_82479_, vec32.f_82480_ - vec3.f_82480_, vec32.f_82481_ - vec3.f_82481_};
            double[] dArray3 = dArray2;
            double d2 = Math.sqrt(dArray3[0] * dArray3[0] + dArray3[1] * dArray3[1] + dArray3[2] * dArray3[2]);
            if (d2 < 0.02) {
                return;
            }
            double d3 = Math.hypot(dArray3[0], dArray3[2]);
            if (d3 > 1.0E-4) {
                double[] dArray4 = new double[3];
                dArray4[0] = -dArray3[2] / d3 * d;
                dArray4[1] = 0.0;
                dArray = dArray4;
                dArray4[2] = dArray3[0] / d3 * d;
            } else {
                double[] dArray5 = new double[3];
                dArray5[0] = d;
                dArray5[1] = 0.0;
                dArray = dArray5;
                dArray5[2] = 0.0;
            }
            double[] dArray6 = dArray;
            double[][] dArrayArray = new double[][]{{vec3.f_82479_ - dArray6[0], vec3.f_82480_, vec3.f_82481_ - dArray6[2]}, {vec32.f_82479_ - dArray6[0], vec32.f_82480_, vec32.f_82481_ - dArray6[2]}, {vec32.f_82479_ + dArray6[0], vec32.f_82480_, vec32.f_82481_ + dArray6[2]}, {vec3.f_82479_ + dArray6[0], vec3.f_82480_, vec3.f_82481_ + dArray6[2]}};
            this.queue(string, dArrayArray, new double[][]{{0.0, 0.0}, {1.0, 0.0}, {1.0, 1.0}, {0.0, 1.0}}, 0xFFFFFF, f, false);
        }

        private static double edge(double[] dArray, double[] dArray2) {
            return Math.sqrt(Math.pow(dArray[0] - dArray2[0], 2.0) + Math.pow(dArray[1] - dArray2[1], 2.0) + Math.pow(dArray[2] - dArray2[2], 2.0));
        }

        private static double smooth(double d, double d2, double d3) {
            double d4 = Math.max(0.0, Math.min(1.0, (d3 - d) / (d2 - d)));
            return d4 * d4 * (3.0 - 2.0 * d4);
        }

        private static double[] subtract(double[] dArray, double[] dArray2) {
            return new double[]{dArray[0] - dArray2[0], dArray[1] - dArray2[1], dArray[2] - dArray2[2]};
        }

        private record Draw(double[][] points, double[][] uv, int tint, float alpha, boolean avoidActors) {
        }
    }

    private static final class Surfaces {
        private static final Map<String, RenderType> ALL = GongdiSpellRenderer.surfaces();

        private Surfaces() {
        }
    }
}

