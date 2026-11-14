/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RegisterClientReloadListenersEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.apple;

import com.google.gson.Gson;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.farmingtales.ftboss.client.apple.ApplePoseSampler;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class AppleKnightEffects {
    private static final Set<String> IDS = Set.of("gilded_verdict", "orchard_aegis", "solar_orchard", "judgment_seed", "golden_harvest");
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "apple_knight/materials.png");
    private static final RenderType TYPE = RenderType.m_110473_((ResourceLocation)TEXTURE);
    private static final Map<String, Model> MODELS = new HashMap<String, Model>();
    private static final List<Cast> CASTS = new ArrayList<Cast>();
    private static ClientLevel world;
    private static final Vector3f CAMERA_POINT;
    private static final Quaternionf BONE_ROTATION;
    private static final float[] BEAM_NORMAL;

    private AppleKnightEffects() {
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket) {
        if (spellVisualPacket == null || spellVisualPacket.spell() == null || !IDS.contains(spellVisualPacket.spell().id)) {
            return false;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel != world) {
            CASTS.clear();
            world = clientLevel;
        }
        if (clientLevel == null || !spellVisualPacket.valid() || !clientLevel.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) {
            return true;
        }
        long l = clientLevel.m_46467_();
        CASTS.removeIf(cast -> l - cast.refreshed >= (long)cast.packet.spell().lifetime(cast.packet.phase()));
        if (spellVisualPacket.spell() == SpellVisualRules.Spell.ORCHARD_AEGIS && spellVisualPacket.phase() != SpellVisualRules.Phase.CHARGE) {
            CASTS.removeIf(cast -> cast.packet.spell() == spellVisualPacket.spell() && cast.packet.owner().equals(spellVisualPacket.owner()));
        }
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE) {
            for (Cast cast2 : CASTS) {
                if (cast2.packet.phase() != spellVisualPacket.phase() || cast2.packet.spell() != spellVisualPacket.spell() || !cast2.packet.owner().equals(spellVisualPacket.owner()) || l - cast2.refreshed >= (long)spellVisualPacket.spell().lifetime(spellVisualPacket.phase())) continue;
                cast2.packet = spellVisualPacket;
                cast2.refreshed = l;
                return true;
            }
        } else if (spellVisualPacket.phase() == SpellVisualRules.Phase.RELEASE) {
            CASTS.removeIf(cast -> cast.packet.phase() == SpellVisualRules.Phase.CHARGE && cast.packet.spell() == spellVisualPacket.spell() && cast.packet.owner().equals(spellVisualPacket.owner()));
        }
        if (CASTS.size() >= 32) {
            CASTS.remove(0);
        }
        CASTS.add(new Cast(spellVisualPacket, l));
        return true;
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        CASTS.clear();
        world = null;
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == world) {
            CASTS.clear();
            world = null;
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null || clientLevel != world) {
            CASTS.clear();
            world = clientLevel;
            return;
        }
        long l = clientLevel.m_46467_();
        CASTS.removeIf(cast -> l - cast.refreshed >= (long)cast.packet.spell().lifetime(cast.packet.phase()) || !cast.packet.dimension().equals((Object)clientLevel.m_46472_().m_135782_()));
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        VertexConsumer vertexConsumer = bufferSource.m_6299_(TYPE);
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        int n = 12000;
        for (Cast cast2 : CASTS) {
            int n2;
            Vec3 vec32;
            Entity entity;
            SpellVisualPacket spellVisualPacket = cast2.packet;
            double d = (float)(l - cast2.born) + renderLevelStageEvent.getPartialTick();
            double d2 = Math.min(0.999, Math.max(0.0, d / (double)spellVisualPacket.spell().lifetime(spellVisualPacket.phase())));
            if (spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE) {
                d2 = d / (double)spellVisualPacket.spell().lifetime(spellVisualPacket.phase()) % 1.0;
            }
            if ((entity = clientLevel.m_6815_(spellVisualPacket.ownerId())) != null && !entity.m_20148_().equals(spellVisualPacket.owner())) {
                entity = null;
            }
            if (entity != null && !entity.m_6084_() || spellVisualPacket.spell() == SpellVisualRules.Spell.ORCHARD_AEGIS && spellVisualPacket.phase() != SpellVisualRules.Phase.CHARGE && d > 3.0 && entity instanceof LivingEntity && (vec32 = (LivingEntity)entity).m_6103_() <= 0.0f) continue;
            vec32 = spellVisualPacket.spell().followsCaster(spellVisualPacket.phase()) && entity != null ? entity.m_20252_(renderLevelStageEvent.getPartialTick()) : spellVisualPacket.start();
            double d3 = spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM ? AppleKnightEffects.beamDistanceSquared(vec3, spellVisualPacket.start(), spellVisualPacket.end()) : vec3.m_82557_(vec32);
            if (d3 > 4096.0 || n <= 0) continue;
            if (spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM) {
                boolean bl;
                boolean bl2 = bl = spellVisualPacket.spell() == SpellVisualRules.Spell.GOLDEN_HARVEST;
                if (!bl && spellVisualPacket.spell() != SpellVisualRules.Spell.GILDED_VERDICT || n < 24) continue;
                n -= AppleKnightEffects.beam(vertexConsumer, poseStack, vec3, spellVisualPacket.start(), spellVisualPacket.end(), d2, bl);
                continue;
            }
            Model model = AppleKnightEffects.model(spellVisualPacket.spell().id);
            if (model == null) continue;
            boolean bl = minecraft.f_91066_.m_92176_().m_90612_() && renderLevelStageEvent.getCamera().m_90592_() == entity;
            boolean bl3 = vec3.m_82557_(vec32) > 576.0 && model.lodParts != null;
            Part[] partArray = bl3 ? model.lodParts : model.parts;
            int n3 = n2 = bl3 ? model.lodFaces : model.fullFaces;
            if (n2 > n) continue;
            n -= n2;
            for (Part part : partArray) {
                Pose[] poseArray = part.frames[spellVisualPacket.phase().ordinal()];
                if (poseArray == null) continue;
                Pose pose = part.pose;
                ApplePoseSampler.sampleInto(poseArray, d2, pose);
                float f = (float)pose.alpha;
                if (bl && spellVisualPacket.spell().followsCaster(spellVisualPacket.phase())) {
                    f *= 0.28f;
                }
                if (f < 0.008f) continue;
                poseStack.m_85836_();
                poseStack.m_85837_(vec32.f_82479_ - vec3.f_82479_ + part.pivot[0] + pose.position[0], vec32.f_82480_ - vec3.f_82480_ + part.pivot[1] + pose.position[1], vec32.f_82481_ - vec3.f_82481_ + part.pivot[2] + pose.position[2]);
                poseStack.m_252781_(BONE_ROTATION.rotationYXZ((float)Math.toRadians(pose.rotation[1]), (float)Math.toRadians(pose.rotation[0]), (float)Math.toRadians(pose.rotation[2])));
                poseStack.m_85841_((float)pose.scale[0], (float)pose.scale[1], (float)pose.scale[2]);
                for (Face face : part.faces) {
                    for (int i = 0; i < 4; ++i) {
                        int n4 = i * 3;
                        float f2 = i == 0 || i == 3 ? face.textureLeft : face.textureRight;
                        float f3 = i < 2 ? 0.046875f : 0.953125f;
                        AppleKnightEffects.vertex(vertexConsumer, poseStack.m_85850_(), face.local[n4], face.local[n4 + 1], face.local[n4 + 2], f2, f3, f, face.normal);
                    }
                }
                poseStack.m_85849_();
            }
        }
        bufferSource.m_109912_(TYPE);
    }

    private static Model model(String string) {
        Model throwable2;
        if (MODELS.containsKey(string)) {
            return MODELS.get(string);
        }
        InputStreamReader inputStreamReader = new InputStreamReader(Minecraft.m_91087_().m_91098_().m_215593_(new ResourceLocation("ftboss", "apple_knight/" + string + ".json")).m_215507_(), StandardCharsets.UTF_8);
        try {
            Model model = (Model)new Gson().fromJson((Reader)inputStreamReader, Model.class);
            for (Part[] partArray : new Part[][]{model.parts, model.lodParts}) {
                if (partArray == null) continue;
                int n = 0;
                for (Part part : partArray) {
                    Vector3f vector3f;
                    part.pose = ApplePoseSampler.reusablePose();
                    part.frames = new Pose[SpellVisualRules.Phase.values().length][];
                    for (SpellVisualRules.Phase phase : SpellVisualRules.Phase.values()) {
                        vector3f = model.phases.get(phase.name());
                        if (vector3f == null) continue;
                        part.frames[phase.ordinal()] = new Pose[((Map<String, Pose>[])vector3f).length];
                        for (int i = 0; i < ((Map<String, Pose>[])vector3f).length; ++i) {
                            part.frames[phase.ordinal()][i] = vector3f[i].get(part.name);
                        }
                    }
                    n += part.faces.length;
                    for (Face face : part.faces) {
                        vector3f = new Vector3f((float)(face.points[1][0] - face.points[0][0]), (float)(face.points[1][1] - face.points[0][1]), (float)(face.points[1][2] - face.points[0][2]));
                        Vector3f vector3f2 = new Vector3f((float)(face.points[2][0] - face.points[0][0]), (float)(face.points[2][1] - face.points[0][1]), (float)(face.points[2][2] - face.points[0][2]));
                        vector3f.cross((Vector3fc)vector3f2).normalize();
                        face.normal = new float[]{vector3f.x, vector3f.y, vector3f.z};
                        face.local = new float[12];
                        for (int i = 0; i < 4; ++i) {
                            for (int j = 0; j < 3; ++j) {
                                face.local[i * 3 + j] = (float)(face.points[i][j] - part.pivot[j]);
                            }
                        }
                        face.textureLeft = (float)(face.material * 32 + 3) / 256.0f;
                        face.textureRight = (float)(face.material * 32 + 29) / 256.0f;
                    }
                }
                if (partArray == model.parts) {
                    model.fullFaces = n;
                    continue;
                }
                model.lodFaces = n;
            }
            MODELS.put(string, model);
            throwable2 = model;
        }
        catch (Throwable throwable) {
            try {
                try {
                    inputStreamReader.close();
                }
                catch (Throwable throwable3) {
                    throwable.addSuppressed(throwable3);
                }
                throw throwable;
            }
            catch (Exception exception) {
                System.err.println("[FTBoss AppleKnight] Cannot load " + string + ": " + String.valueOf(exception));
                MODELS.put(string, null);
                return null;
            }
        }
        inputStreamReader.close();
        return throwable2;
    }

    public static double beamDistanceSquared(Vec3 vec3, Vec3 vec32, Vec3 vec33) {
        return ApplePoseSampler.segmentDistanceSquared(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, vec32.f_82479_, vec32.f_82480_, vec32.f_82481_, vec33.f_82479_, vec33.f_82480_, vec33.f_82481_);
    }

    private static int beam(VertexConsumer vertexConsumer, PoseStack poseStack, Vec3 vec3, Vec3 vec32, Vec3 vec33, double d, boolean bl) {
        Vec3 vec34 = vec33.m_82546_(vec32);
        if (vec34.m_82556_() < 1.0E-4) {
            return 0;
        }
        Vec3 vec35 = vec34.m_82541_().m_82537_(new Vec3(0.0, 1.0, 0.0));
        if (vec35.m_82556_() < 0.001) {
            vec35 = new Vec3(1.0, 0.0, 0.0);
        }
        vec35 = vec35.m_82541_().m_82490_(0.035);
        float f = (float)(Math.sin(Math.PI * d) * 0.65);
        for (int i = 0; i < 24; ++i) {
            double d2 = (double)i / 24.0;
            double d3 = (double)(i + 1) / 24.0;
            Vec3 vec36 = vec32.m_82549_(vec34.m_82490_(d2)).m_82520_(0.0, bl ? Math.sin(d2 * Math.PI) * 0.25 : 0.0, 0.0).m_82546_(vec3);
            Vec3 vec37 = vec32.m_82549_(vec34.m_82490_(d3)).m_82520_(0.0, bl ? Math.sin(d3 * Math.PI) * 0.25 : 0.0, 0.0).m_82546_(vec3);
            for (int j = 0; j < 4; ++j) {
                Vec3 vec38 = j == 0 || j == 3 ? vec36 : vec37;
                double d4 = j < 2 ? 1.0 : -1.0;
                AppleKnightEffects.vertex(vertexConsumer, poseStack.m_85850_(), (float)(vec38.f_82479_ + vec35.f_82479_ * d4), (float)(vec38.f_82480_ + vec35.f_82480_ * d4), (float)(vec38.f_82481_ + vec35.f_82481_ * d4), 0.546875f, 0.390625f, f, BEAM_NORMAL);
            }
        }
        return 24;
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float f, float f2, float f3, float f4, float f5, float f6, float[] fArray) {
        pose.m_252922_().transformPosition(f, f2, f3, CAMERA_POINT);
        float f7 = Math.max(0.0f, Math.min(1.0f, (CAMERA_POINT.length() - 0.35f) / 0.6f));
        vertexConsumer.m_252986_(pose.m_252922_(), f, f2, f3).m_6122_(255, 255, 255, Math.round((f6 *= f7 * f7 * (3.0f - 2.0f * f7)) * 255.0f)).m_7421_(f4, f5).m_86008_(0).m_85969_(0xF000F0).m_252939_(pose.m_252943_(), fArray[0], fArray[1], fArray[2]).m_5752_();
    }

    static {
        CAMERA_POINT = new Vector3f();
        BONE_ROTATION = new Quaternionf();
        BEAM_NORMAL = new float[]{0.0f, 1.0f, 0.0f};
    }

    private static final class Cast {
        private SpellVisualPacket packet;
        private final long born;
        private long refreshed;

        private Cast(SpellVisualPacket spellVisualPacket, long l) {
            this.packet = spellVisualPacket;
            this.born = l;
            this.refreshed = l;
        }
    }

    public static final class Model {
        public Part[] parts;
        public Part[] lodParts;
        public Map<String, Map<String, Pose>[]> phases;
        public transient int fullFaces;
        public transient int lodFaces;
    }

    public static final class Part {
        public String name;
        public double[] pivot;
        public Face[] faces;
        public transient Pose pose;
        public transient Pose[][] frames;
    }

    public static final class Pose {
        public double[] position;
        public double[] rotation;
        public double[] scale;
        public double alpha;
    }

    public static final class Face {
        public double[][] points;
        public int material;
        public transient float[] normal;
        public transient float[] local;
        public transient float textureLeft;
        public transient float textureRight;
    }

    @Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Reload {
        private Reload() {
        }

        @SubscribeEvent
        public static void register(RegisterClientReloadListenersEvent registerClientReloadListenersEvent) {
            registerClientReloadListenersEvent.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)resourceManager -> MODELS.clear()));
        }
    }
}

