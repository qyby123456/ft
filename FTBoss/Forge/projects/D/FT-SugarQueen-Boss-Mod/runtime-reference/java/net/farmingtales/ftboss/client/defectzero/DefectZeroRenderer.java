/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RegisterClientReloadListenersEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.defectzero;

import com.google.gson.Gson;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.client.defectzero.DefectSkillAnchors;
import net.farmingtales.ftboss.client.defectzero.DefectSkillGeoModel;
import net.farmingtales.ftboss.client.defectzero.DefectSkillHost;
import net.farmingtales.ftboss.client.defectzero.DefectSkillSync;
import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class DefectZeroRenderer {
    private static final Set<String> SPELLS = Set.of("capacitor_overload", "repulsor_discharge", "ion_rail", "static_lattice", "emergency_reboot");
    private static final ResourceLocation ATLAS = ResourceLocation.m_214293_((String)"ftboss", (String)"textures/fx/defectzero/plasma.png");
    private static final List<Effect> ACTIVE = new ArrayList<Effect>();
    private static final Map<String, Model> MODELS = new HashMap<String, Model>();
    private static final int MAX_EFFECTS = 48;
    private static final int MAX_FACES = 6500;
    private static final int[][] BEAM_FACES = new int[][]{{4, 5, 1, 0}, {5, 6, 2, 1}, {6, 7, 3, 2}, {7, 4, 0, 3}, {0, 1, 2, 3}, {7, 6, 5, 4}};
    private static final float[][][] MATERIAL_UV = DefectZeroRenderer.materialUv();
    private static Object resourceManager;

    private DefectZeroRenderer() {
    }

    public static boolean owns(String string) {
        return SPELLS.contains(string);
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket) {
        double d;
        if (spellVisualPacket == null || spellVisualPacket.spell() == null || !DefectZeroRenderer.owns(spellVisualPacket.spell().id)) {
            return false;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (!spellVisualPacket.valid() || minecraft.f_91073_ == null || !minecraft.f_91073_.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) {
            return true;
        }
        Entity entity = minecraft.f_91073_.m_6815_(spellVisualPacket.ownerId());
        if (entity != null && !entity.m_20148_().equals(spellVisualPacket.owner())) {
            return true;
        }
        if (spellVisualPacket.phase() != SpellVisualRules.Phase.CHARGE) {
            ACTIVE.removeIf(effect -> effect.owner.equals(spellVisualPacket.owner()) && effect.id.equals(spellVisualPacket.spell().id) && effect.phase == SpellVisualRules.Phase.CHARGE);
        }
        ACTIVE.removeIf(effect -> effect.owner.equals(spellVisualPacket.owner()) && effect.id.equals(spellVisualPacket.spell().id) && effect.phase == spellVisualPacket.phase() && (spellVisualPacket.phase() != SpellVisualRules.Phase.BEAM || effect.start.m_82557_(spellVisualPacket.start()) < 1.0E-4 && effect.end.m_82557_(spellVisualPacket.end()) < 1.0E-4));
        if (ACTIVE.size() >= 48) {
            ACTIVE.remove(0);
        }
        double d2 = d = entity == null ? 1.0 : SpellVisuals.radiusScale(entity, spellVisualPacket.spell().id);
        if (!Double.isFinite(d) || d <= 0.0) {
            d = 1.0;
        }
        ACTIVE.add(new Effect(spellVisualPacket.spell().id, spellVisualPacket.phase(), spellVisualPacket.ownerId(), spellVisualPacket.owner(), spellVisualPacket.start(), spellVisualPacket.end(), minecraft.f_91073_.m_46467_(), spellVisualPacket.spell().lifetime(spellVisualPacket.phase()), Math.min(4.0, d), entity == null ? null : entity.m_20252_(1.0f), spellVisualPacket.spell().followsCaster(spellVisualPacket.phase()) || spellVisualPacket.spell().id.equals("emergency_reboot")));
        return true;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null) {
            ACTIVE.clear();
            return;
        }
        long l = minecraft.f_91073_.m_46467_();
        ACTIVE.removeIf(effect -> l - effect.born >= (long)effect.life || l < effect.born);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        ACTIVE.clear();
        MODELS.clear();
        DefectSkillAnchors.clear();
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        ACTIVE.clear();
        MODELS.clear();
        DefectSkillAnchors.clear();
    }

    private static Model model(String string2) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (resourceManager != minecraft.m_91098_()) {
            MODELS.clear();
            resourceManager = minecraft.m_91098_();
        }
        return MODELS.computeIfAbsent(string2, string -> {
            try (InputStream inputStream = minecraft.m_91098_().m_215593_(ResourceLocation.m_214293_((String)"ftboss", (String)("defectzero/" + string + ".json"))).m_215507_();){
                Model model;
                try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);){
                    Model model2 = (Model)new Gson().fromJson((Reader)inputStreamReader, Model.class);
                    if (model2 == null || model2.parts == null || model2.parts.length > 128) {
                        throw new IllegalStateException((String)string);
                    }
                    DefectZeroRenderer.prepare(model2);
                    model = model2;
                }
                return model;
            }
            catch (Exception exception) {
                System.err.println("[ftboss defectzero] Cannot load " + string + ": " + String.valueOf(exception));
                Model model = new Model();
                model.parts = new Part[0];
                return model;
            }
        });
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        RenderType renderType = RenderType.m_110458_((ResourceLocation)ATLAS);
        VertexConsumer vertexConsumer = bufferSource.m_6299_(renderType);
        int n = 0;
        for (Effect effect : ACTIVE) {
            boolean bl;
            Vec3 vec32;
            boolean bl2;
            DefectSkillHost defectSkillHost;
            Entity entity;
            double d;
            if (effect.id.equals("ion_rail") && effect.phase != SpellVisualRules.Phase.CHARGE && effect.phase != SpellVisualRules.Phase.BEAM || (d = (double)((float)(minecraft.f_91073_.m_46467_() - effect.born) + renderLevelStageEvent.getPartialTick())) < 0.0 || d >= (double)effect.life || (entity = minecraft.f_91073_.m_6815_(effect.ownerId)) != null && !entity.m_20148_().equals(effect.owner) || effect.phase == SpellVisualRules.Phase.CHARGE && entity instanceof DefectSkillHost && !DefectSkillSync.matchesCharge((defectSkillHost = (DefectSkillHost)entity).ftDefectSkillState().name(), effect.id) || (bl2 = FirstPersonSpellVisibility.preserveThirdPerson(entity == renderLevelStageEvent.getCamera().m_90592_() && !renderLevelStageEvent.getCamera().m_90593_())) && effect.phase == SpellVisualRules.Phase.CHARGE && effect.follows) continue;
            Vec3 vec33 = effect.start;
            if (entity != null && effect.initialOwner != null && effect.follows) {
                vec33 = vec33.m_82549_(entity.m_20252_(renderLevelStageEvent.getPartialTick()).m_82546_(effect.initialOwner));
            }
            if (effect.phase == SpellVisualRules.Phase.CHARGE && effect.follows && (vec32 = DefectSkillAnchors.core(entity)) != null) {
                vec33 = vec32.m_82520_(0.0, -0.315, 0.0);
            }
            if (effect.phase != SpellVisualRules.Phase.BEAM && vec33.m_82557_(vec3) > 4096.0) continue;
            float f = (float)(d / (double)effect.life);
            float f2 = DefectZeroRenderer.opacity(f);
            if (effect.phase == SpellVisualRules.Phase.BEAM && !effect.id.equals("ion_rail")) {
                n += DefectZeroRenderer.drawBeam(poseStack, vertexConsumer, effect, vec3, f2, bl2, 6500 - n);
                continue;
            }
            Model model = DefectZeroRenderer.model(effect.id);
            Vec3 vec34 = effect.end.m_82546_(effect.start);
            double d2 = vec34.m_82553_();
            boolean bl3 = bl = effect.id.equals("ion_rail") && effect.phase == SpellVisualRules.Phase.BEAM;
            if (bl && d2 < 0.001) continue;
            poseStack.m_85836_();
            poseStack.m_85837_(vec33.f_82479_ - vec3.f_82479_, vec33.f_82480_ - vec3.f_82480_, vec33.f_82481_ - vec3.f_82481_);
            if (bl) {
                poseStack.m_252781_(new Quaternionf().rotationTo((Vector3fc)new Vector3f(0.0f, 0.0f, 1.0f), (Vector3fc)new Vector3f((float)vec34.f_82479_, (float)vec34.f_82480_, (float)vec34.f_82481_).normalize()));
            } else {
                poseStack.m_252781_(new Quaternionf().rotateY((float)Math.atan2(vec34.f_82479_, vec34.f_82481_)));
            }
            float f3 = effect.phase == SpellVisualRules.Phase.CHARGE ? 0.35f : (float)effect.scale;
            poseStack.m_85841_(f3, f3, bl ? (float)(d2 / 6.6) : f3);
            if (bl) {
                poseStack.m_85837_(0.0, -0.9, 0.0);
            }
            for (Part part : model.parts) {
                if (effect.id.equals("ion_rail") && effect.phase == SpellVisualRules.Phase.CHARGE && !part.name.startsWith("muzzle_split")) continue;
                if (n >= 6500) break;
                poseStack.m_85836_();
                float f4 = effect.phase == SpellVisualRules.Phase.CHARGE ? 0.18f + 0.37f * f : 0.18f + 0.82f * f;
                DefectZeroRenderer.transform(poseStack, part, bl ? 0.55f : f4);
                for (int i = 0; i < part.faces.length && n < 6500; ++i) {
                    int[] nArray = part.faces[i];
                    if (bl2 && DefectZeroRenderer.nearEye(poseStack, part, nArray, 0.9)) continue;
                    DefectZeroRenderer.emit(poseStack, vertexConsumer, part, nArray, part.normals[i], f2, part.material);
                    ++n;
                }
                poseStack.m_85849_();
            }
            poseStack.m_85849_();
        }
        bufferSource.m_109912_(renderType);
    }

    public static float opacity(float f) {
        if (!Float.isFinite(f) || f <= 0.0f || f >= 1.0f) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, Math.min(f / 0.08f, (1.0f - f) / 0.13f)));
    }

    private static void transform(PoseStack poseStack, Part part, float f) {
        Key key = part.keyframes[0];
        Key key2 = part.keyframes[part.keyframes.length - 1];
        for (int i = 1; i < part.keyframes.length; ++i) {
            if (!(part.keyframes[i].t >= f)) continue;
            key = part.keyframes[i - 1];
            key2 = part.keyframes[i];
            break;
        }
        float f2 = Math.max(0.0f, Math.min(1.0f, (f - key.t) / Math.max(1.0E-5f, key2.t - key.t)));
        poseStack.m_85837_((double)(part.pivot[0] + DefectZeroRenderer.interpolate(key.position, key2.position, f2, 0)), (double)(part.pivot[1] + DefectZeroRenderer.interpolate(key.position, key2.position, f2, 1)), (double)(part.pivot[2] + DefectZeroRenderer.interpolate(key.position, key2.position, f2, 2)));
        poseStack.m_252781_(new Quaternionf().rotationXYZ(DefectZeroRenderer.interpolate(key.rotation, key2.rotation, f2, 0), DefectZeroRenderer.interpolate(key.rotation, key2.rotation, f2, 1), DefectZeroRenderer.interpolate(key.rotation, key2.rotation, f2, 2)));
        poseStack.m_85841_(DefectZeroRenderer.interpolate(key.scale, key2.scale, f2, 0), DefectZeroRenderer.interpolate(key.scale, key2.scale, f2, 1), DefectZeroRenderer.interpolate(key.scale, key2.scale, f2, 2));
        poseStack.m_85837_((double)(-part.pivot[0]), (double)(-part.pivot[1]), (double)(-part.pivot[2]));
    }

    private static float interpolate(float[] fArray, float[] fArray2, float f, int n) {
        return fArray[n] + (fArray2[n] - fArray[n]) * f;
    }

    private static boolean nearEye(PoseStack poseStack, Part part, int[] nArray, double d) {
        float f = 0.0f;
        float f2 = 0.0f;
        float f3 = 0.0f;
        for (int n : nArray) {
            float[] fArray = part.vertices[n];
            f += fArray[0];
            f2 += fArray[1];
            f3 += fArray[2];
        }
        return (double)new Vector3f(f / (float)nArray.length, f2 / (float)nArray.length, f3 / (float)nArray.length).mulPosition((Matrix4fc)poseStack.m_85850_().m_252922_()).lengthSquared() < d * d;
    }

    public static void prepare(Model model) {
        for (Part part : model.parts) {
            if (part.material < 0 || part.material >= MATERIAL_UV.length) {
                throw new IllegalArgumentException("Invalid material");
            }
            part.normals = new float[part.faces.length][3];
            for (int i = 0; i < part.faces.length; ++i) {
                DefectZeroRenderer.normal(part, part.faces[i], part.normals[i]);
            }
        }
    }

    private static void normal(Part part, int[] nArray, float[] fArray) {
        float f;
        float f2;
        float[] fArray2 = part.vertices[nArray[0]];
        float[] fArray3 = part.vertices[nArray[1]];
        float[] fArray4 = part.vertices[nArray[2]];
        float f3 = fArray3[0] - fArray2[0];
        float f4 = fArray3[1] - fArray2[1];
        float f5 = fArray3[2] - fArray2[2];
        float f6 = fArray4[0] - fArray2[0];
        float f7 = fArray4[2] - fArray2[2];
        float f8 = fArray4[1] - fArray2[1];
        float f9 = Math.fma(f4, f7, -f5 * f8);
        float f10 = Math.fma(f9, f9, Math.fma(f2 = Math.fma(f5, f6, -f3 * f7), f2, (f = Math.fma(f3, f8, -f4 * f6)) * f));
        float f11 = f10 < 1.0E-12f ? 0.0f : (float)(1.0 / Math.sqrt(f10));
        fArray[0] = f9 * f11;
        fArray[1] = f2 * f11;
        fArray[2] = f * f11;
    }

    private static float[][][] materialUv() {
        float[][][] fArray = new float[8][4][2];
        for (int i = 0; i < fArray.length; ++i) {
            float f = (float)(i % 4 * 32 + 5) / 128.0f;
            float f2 = (float)(i / 4 * 64 + 7) / 128.0f;
            fArray[i] = new float[][]{{f, f2}, {f + 0.1640625f, f2}, {f + 0.1640625f, f2 + 0.390625f}, {f, f2 + 0.390625f}};
        }
        return fArray;
    }

    private static void emit(PoseStack poseStack, VertexConsumer vertexConsumer, Part part, int[] nArray, float[] fArray, float f, int n) {
        if (fArray[0] == 0.0f && fArray[1] == 0.0f && fArray[2] == 0.0f) {
            return;
        }
        float[][] fArray2 = MATERIAL_UV[n];
        for (int i = 0; i < 4; ++i) {
            float[] fArray3 = part.vertices[nArray[Math.min(i, nArray.length - 1)]];
            vertexConsumer.m_252986_(poseStack.m_85850_().m_252922_(), fArray3[0], fArray3[1], fArray3[2]).m_85950_(1.0f, 1.0f, 1.0f, f).m_7421_(fArray2[i][0], fArray2[i][1]).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(poseStack.m_85850_().m_252943_(), fArray[0], fArray[1], fArray[2]).m_5752_();
        }
    }

    private static int drawBeam(PoseStack poseStack, VertexConsumer vertexConsumer, Effect effect, Vec3 vec3, float f, boolean bl, int n) {
        Vec3 vec32 = effect.end.m_82546_(effect.start);
        double d = vec32.m_82553_();
        if (d < 0.001) {
            return 0;
        }
        Vector3f vector3f = new Vector3f((float)vec32.f_82479_, (float)vec32.f_82480_, (float)vec32.f_82481_).normalize();
        Vector3f vector3f2 = new Vector3f((Vector3fc)vector3f).cross((Vector3fc)((double)Math.abs(vector3f.y) > 0.9 ? new Vector3f(1.0f, 0.0f, 0.0f) : new Vector3f(0.0f, 1.0f, 0.0f))).normalize();
        Vector3f vector3f3 = new Vector3f((Vector3fc)vector3f2).cross((Vector3fc)vector3f).normalize();
        int n2 = Math.max(3, Math.min(64, (int)(d * 3.0)));
        int n3 = 0;
        Part part = new Part();
        part.vertices = new float[8][3];
        float[] fArray = new float[3];
        for (int i = 0; i < n2 && n3 + 6 <= n; ++i) {
            int n4;
            Vec3 vec33 = DefectZeroRenderer.beamPoint(effect, vec32, vector3f2, (double)i / (double)n2, i);
            Vec3 vec34 = DefectZeroRenderer.beamPoint(effect, vec32, vector3f2, (double)(i + 1) / (double)n2, i + 1);
            if (bl && (vec33.m_82557_(vec3) < 1.0 || vec34.m_82557_(vec3) < 1.0) || vec33.m_82557_(vec3) > 4096.0 && vec34.m_82557_(vec3) > 4096.0) continue;
            for (n4 = 0; n4 < 2; ++n4) {
                for (int j = 0; j < 4; ++j) {
                    Vec3 vec35 = n4 == 0 ? vec33 : vec34;
                    float f2 = j < 2 ? 1.0f : -1.0f;
                    float f3 = j == 0 || j == 3 ? 1.0f : -1.0f;
                    float[] fArray2 = part.vertices[n4 * 4 + j];
                    fArray2[0] = (float)(vec35.f_82479_ - vec3.f_82479_) + 0.017f * (vector3f2.x * f2 + vector3f3.x * f3);
                    fArray2[1] = (float)(vec35.f_82480_ - vec3.f_82480_) + 0.017f * (vector3f2.y * f2 + vector3f3.y * f3);
                    fArray2[2] = (float)(vec35.f_82481_ - vec3.f_82481_) + 0.017f * (vector3f2.z * f2 + vector3f3.z * f3);
                }
            }
            for (n4 = 0; n4 < 4; ++n4) {
                DefectZeroRenderer.normal(part, BEAM_FACES[n4], fArray);
                DefectZeroRenderer.emit(poseStack, vertexConsumer, part, BEAM_FACES[n4], fArray, f, 3);
            }
            n3 += 4;
            if (i == 0) {
                DefectZeroRenderer.normal(part, BEAM_FACES[4], fArray);
                DefectZeroRenderer.emit(poseStack, vertexConsumer, part, BEAM_FACES[4], fArray, f, 3);
                ++n3;
            }
            if (i != n2 - 1) continue;
            DefectZeroRenderer.normal(part, BEAM_FACES[5], fArray);
            DefectZeroRenderer.emit(poseStack, vertexConsumer, part, BEAM_FACES[5], fArray, f, 3);
            ++n3;
        }
        return n3;
    }

    private static Vec3 beamPoint(Effect effect, Vec3 vec3, Vector3f vector3f, double d, int n) {
        double d2 = n == 0 || d >= 1.0 ? 0.0 : Math.sin((double)n * 2.1 + (double)effect.born * 0.5) * 0.055;
        return effect.start.m_82549_(vec3.m_82490_(d)).m_82520_((double)vector3f.x * d2, (double)vector3f.y * d2, (double)vector3f.z * d2);
    }

    public static void clearResources() {
        ACTIVE.clear();
        MODELS.clear();
        DefectSkillAnchors.clear();
        DefectSkillGeoModel.invalidate();
    }

    private record Effect(String id, SpellVisualRules.Phase phase, int ownerId, UUID owner, Vec3 start, Vec3 end, long born, int life, double scale, Vec3 initialOwner, boolean follows) {
    }

    public static final class Model {
        public String id;
        public Part[] parts;
    }

    public static final class Part {
        public String name;
        public float[][] vertices;
        public int[][] faces;
        public int material;
        public float[] pivot;
        public Key[] keyframes;
        public transient float[][] normals;
    }

    public static final class Key {
        public float t;
        public float[] position;
        public float[] rotation;
        public float[] scale;
    }

    @Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Reload {
        private Reload() {
        }

        @SubscribeEvent
        public static void register(RegisterClientReloadListenersEvent registerClientReloadListenersEvent) {
            registerClientReloadListenersEvent.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)resourceManager -> DefectZeroRenderer.clearResources()));
        }
    }
}

