/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.BlockAndTintGetter
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
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.slf4j.LoggerFactory
 */
package net.farmingtales.ftboss.client.wineking;

import com.google.gson.Gson;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.wineking.WineSpellMotion;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class WineSpellFx {
    private static final ResourceLocation ATLAS = new ResourceLocation("ftboss", "textures/wineking/atlas.png");
    private static final Map<String, Sculpture> MODELS = new HashMap<String, Sculpture>();
    private static final List<Scene> ACTIVE = new ArrayList<Scene>();
    private static final int MAX_SCENES = 12;
    private static final int MAX_QUADS = 8000;
    private static final Gson JSON = new Gson();

    private WineSpellFx() {
    }

    public static boolean owns(String string) {
        return string.equals("vintage_vortex") || string.equals("cask_rupture") || string.equals("cinder_breath") || string.equals("intoxicating_toast") || string.equals("vintage_minefield");
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket) {
        if (spellVisualPacket == null || spellVisualPacket.spell() == null || !WineSpellFx.owns(spellVisualPacket.spell().id)) {
            return false;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (!spellVisualPacket.valid() || minecraft.f_91073_ == null || !minecraft.f_91073_.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) {
            return true;
        }
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM) {
            WineSpellMotion.BeamAnchor beamAnchor;
            if (spellVisualPacket.spell().id.equals("cinder_breath") && (beamAnchor = WineSpellMotion.breathAnchor(new double[]{spellVisualPacket.start().f_82479_, spellVisualPacket.start().f_82480_, spellVisualPacket.start().f_82481_}, new double[]{spellVisualPacket.end().f_82479_, spellVisualPacket.end().f_82480_, spellVisualPacket.end().f_82481_})) != null) {
                for (Scene scene2 : ACTIVE) {
                    if (!scene2.packet.owner().equals(spellVisualPacket.owner()) || scene2.packet.spell() != spellVisualPacket.spell() || scene2.packet.phase() != SpellVisualRules.Phase.PULSE) continue;
                    scene2.beamOrigin = new Vec3(beamAnchor.position()[0], beamAnchor.position()[1], beamAnchor.position()[2]);
                    scene2.beamYaw = beamAnchor.yaw();
                }
            }
            return true;
        }
        long l = minecraft.f_91073_.m_46467_();
        String string = spellVisualPacket.spell().id;
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE) {
            for (Scene scene3 : ACTIVE) {
                if (!scene3.packet.owner().equals(spellVisualPacket.owner()) || scene3.packet.spell() != spellVisualPacket.spell() || scene3.packet.phase() != SpellVisualRules.Phase.CHARGE) continue;
                scene3.packet = spellVisualPacket;
                scene3.lastSeen = l;
                return true;
            }
        } else {
            ACTIVE.removeIf(scene -> scene.packet.owner().equals(spellVisualPacket.owner()) && scene.packet.spell() == spellVisualPacket.spell() && WineSpellMotion.replace(scene.packet.phase().name(), spellVisualPacket.phase().name(), string, scene.packet.start().m_82557_(spellVisualPacket.start())));
        }
        Sculpture sculpture = WineSpellFx.model(string);
        if (sculpture == null) {
            return false;
        }
        if (ACTIVE.size() >= 12) {
            ACTIVE.remove(ACTIVE.stream().min(Comparator.comparingLong(scene -> scene.started)).orElseThrow());
        }
        ACTIVE.add(new Scene(spellVisualPacket, l));
        return true;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static Sculpture model(String string) {
        if (MODELS.containsKey(string)) {
            return MODELS.get(string);
        }
        try (InputStream inputStream = WineSpellFx.class.getResourceAsStream("/assets/ftboss/wineking/" + string + ".json");){
            if (inputStream == null) {
                Sculpture sculpture2 = null;
                return sculpture2;
            }
            Sculpture sculpture3 = (Sculpture)JSON.fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), Sculpture.class);
            for (Part part : sculpture3.parts) {
                part.prepare();
            }
            sculpture3.frames = new RigFrame[sculpture3.rigs.length];
            for (int i = 0; i < sculpture3.frames.length; ++i) {
                sculpture3.frames[i] = new RigFrame();
            }
            MODELS.put(string, sculpture3);
            Sculpture sculpture = sculpture3;
            return sculpture;
        }
        catch (Exception exception) {
            LoggerFactory.getLogger((String)"FTBoss-WineKing").error("Unable to load independent wine spell {}", (Object)string, (Object)exception);
            return null;
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel().m_5776_()) {
            ACTIVE.clear();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        ACTIVE.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null || ACTIVE.isEmpty()) {
            return;
        }
        double d = (float)minecraft.f_91073_.m_46467_() + renderLevelStageEvent.getPartialTick();
        ACTIVE.removeIf(scene -> !scene.packet.dimension().equals((Object)minecraft.f_91073_.m_46472_().m_135782_()) || (scene.packet.phase() == SpellVisualRules.Phase.CHARGE ? d - (double)scene.lastSeen > 8.0 : d - (double)scene.started > (double)WineSpellMotion.duration(scene.packet.spell().id, scene.packet.phase().name())));
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        Vector3f vector3f = new Vector3f(0.0f, 0.0f, -1.0f).rotate((Quaternionfc)renderLevelStageEvent.getCamera().m_253121_());
        Vec3 vec32 = new Vec3((double)vector3f.x, (double)vector3f.y, (double)vector3f.z);
        ACTIVE.sort(Comparator.comparingDouble(scene -> scene.packet.start().m_82557_(vec3)));
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        RenderType renderType = RenderType.m_110473_((ResourceLocation)ATLAS);
        VertexConsumer vertexConsumer = bufferSource.m_6299_(renderType);
        int n = 0;
        for (Scene scene2 : ACTIVE) {
            Sculpture sculpture;
            MagicData magicData;
            Vec3 vec33;
            double d2;
            Entity entity = minecraft.f_91073_.m_6815_(scene2.packet.ownerId());
            if (entity == null || !entity.m_20148_().equals(scene2.packet.owner()) || (d2 = (vec33 = scene2.beamOrigin != null ? scene2.beamOrigin : (scene2.packet.spell().attached ? entity.m_20182_() : scene2.packet.start())).m_82557_(vec3)) > 4096.0) continue;
            String string = scene2.packet.spell().id;
            String string2 = scene2.packet.phase().name();
            double d3 = (d - (double)scene2.started) / (double)WineSpellMotion.duration(string, string2);
            if (scene2.packet.phase() == SpellVisualRules.Phase.CHARGE && entity instanceof LivingEntity && (magicData = MagicData.getPlayerMagicData((LivingEntity)(sculpture = (LivingEntity)entity))) != null && magicData.isCasting() && magicData.getCastingSpellId().equals("ftboss:" + string)) {
                d3 = Math.max(0.0f, Math.min(1.0f, magicData.getCastCompletionPercent()));
            }
            sculpture = WineSpellFx.model(string);
            magicData = scene2.packet.end().m_82546_(scene2.packet.start());
            double d4 = scene2.beamOrigin != null ? scene2.beamYaw : WineSpellMotion.headingYaw(string, magicData.f_82479_, magicData.f_82481_);
            int n2 = LevelRenderer.m_109541_((BlockAndTintGetter)minecraft.f_91073_, (BlockPos)new BlockPos((int)Math.floor(vec33.f_82479_), (int)Math.floor(vec33.f_82480_ + 0.5), (int)Math.floor(vec33.f_82481_)));
            poseStack.m_85836_();
            try {
                poseStack.m_85837_(vec33.f_82479_ - vec3.f_82479_, vec33.f_82480_ - vec3.f_82480_, vec33.f_82481_ - vec3.f_82481_);
                poseStack.m_252781_(new Quaternionf().rotationY((float)d4));
                for (int i = 0; i < sculpture.rigs.length; ++i) {
                    Rig rig = sculpture.rigs[i];
                    Part[] partArray = sculpture.frames[i];
                    WineSpellMotion.Pose pose = WineSpellMotion.pose(rig.kind, rig.index, d3, string2, string);
                    partArray.alpha = pose.alpha();
                    if (partArray.alpha < 0.005) continue;
                    WineSpellMotion.writeRigMatrix(partArray.local, rig.pivot, pose);
                    partArray.vertices.set((Matrix4fc)poseStack.m_85850_().m_252922_()).mul((Matrix4fc)partArray.local);
                    partArray.vertices.normal(partArray.normals);
                    partArray.cameraRelative.identity().translate((float)(vec33.f_82479_ - vec3.f_82479_), (float)(vec33.f_82480_ - vec3.f_82480_), (float)(vec33.f_82481_ - vec3.f_82481_)).rotateY((float)d4).mul((Matrix4fc)partArray.local);
                }
                Vector3f vector3f2 = new Vector3f();
                boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
                for (Part part : sculpture.parts) {
                    RigFrame rigFrame = sculpture.frames[part.rig];
                    if (d2 > 576.0 && part.detail || rigFrame.alpha < 0.005 || n + part.faces.length > 8000) continue;
                    for (int i = 0; i < part.faces.length; ++i) {
                        double d5;
                        int[] nArray = part.faces[i];
                        double[] dArray = part.centers[i];
                        Vector3f vector3f3 = vector3f2.set((float)dArray[0], (float)dArray[1], (float)dArray[2]).mulPosition((Matrix4fc)rigFrame.cameraRelative);
                        double d6 = (d5 = (double)vector3f3.length()) < 1.0E-4 ? 1.0 : ((double)vector3f3.x * vec32.f_82479_ + (double)vector3f3.y * vec32.f_82480_ + (double)vector3f3.z * vec32.f_82481_) / d5;
                        double d7 = rigFrame.alpha * WineSpellMotion.viewAlpha(d5, d6, !part.luminous, bl);
                        if (d7 < 0.008) continue;
                        float[] fArray = part.normals[i];
                        for (int j = 0; j < 4; ++j) {
                            int n3 = nArray[Math.min(j, nArray.length - 1)];
                            double[] dArray2 = part.vertices[n3];
                            float[] fArray2 = part.uv[Math.min(j, nArray.length - 1)];
                            vertexConsumer.m_252986_(rigFrame.vertices, (float)dArray2[0], (float)dArray2[1], (float)dArray2[2]).m_6122_(255, 255, 255, (int)(d7 * 255.0)).m_7421_(fArray2[0], fArray2[1]).m_86008_(OverlayTexture.f_118083_).m_85969_(part.luminous ? 0xF000F0 : n2).m_252939_(rigFrame.normals, fArray[0], fArray[1], fArray[2]).m_5752_();
                        }
                        ++n;
                    }
                }
            }
            finally {
                poseStack.m_85849_();
            }
        }
        bufferSource.m_109912_(renderType);
    }

    private static final class Scene {
        private SpellVisualPacket packet;
        private final long started;
        private long lastSeen;
        private Vec3 beamOrigin;
        private double beamYaw;

        private Scene(SpellVisualPacket spellVisualPacket, long l) {
            this.packet = spellVisualPacket;
            this.started = l;
            this.lastSeen = l;
        }
    }

    private static final class Sculpture {
        private Part[] parts;
        private Rig[] rigs;
        private transient RigFrame[] frames;

        private Sculpture() {
        }
    }

    private static final class Part {
        private String name;
        private double[][] vertices;
        private int[][] faces;
        private float[][] uv;
        private int rig;
        private boolean luminous;
        private transient double[][] centers;
        private transient float[][] normals;
        private transient boolean detail;

        private Part() {
        }

        private void prepare() {
            this.detail = this.name.contains("rivet") || this.name.contains("grape_seal") || this.name.contains("mote");
            this.centers = new double[this.faces.length][3];
            this.normals = new float[this.faces.length][3];
            for (int i = 0; i < this.faces.length; ++i) {
                int[] nArray;
                for (int n : nArray = this.faces[i]) {
                    for (int j = 0; j < 3; ++j) {
                        double[] dArray = this.centers[i];
                        int n2 = j;
                        dArray[n2] = dArray[n2] + this.vertices[n][j] / (double)nArray.length;
                    }
                }
                double[] objectArray = this.vertices[nArray[0]];
                double[] dArray = this.vertices[nArray[1]];
                double[] dArray2 = this.vertices[nArray[2]];
                double n = (dArray[1] - objectArray[1]) * (dArray2[2] - objectArray[2]) - (dArray[2] - objectArray[2]) * (dArray2[1] - objectArray[1]);
                double d = (dArray[2] - objectArray[2]) * (dArray2[0] - objectArray[0]) - (dArray[0] - objectArray[0]) * (dArray2[2] - objectArray[2]);
                double d2 = (dArray[0] - objectArray[0]) * (dArray2[1] - objectArray[1]) - (dArray[1] - objectArray[1]) * (dArray2[0] - objectArray[0]);
                double d3 = Math.sqrt(n * n + d * d + d2 * d2);
                this.normals[i] = new float[]{(float)(n / d3), (float)(d / d3), (float)(d2 / d3)};
            }
        }
    }

    private static final class Rig {
        private String kind;
        private double index;
        private double[] pivot;

        private Rig() {
        }
    }

    private static final class RigFrame {
        private final Matrix4f local = new Matrix4f();
        private final Matrix4f vertices = new Matrix4f();
        private final Matrix4f cameraRelative = new Matrix4f();
        private final Matrix3f normals = new Matrix3f();
        private double alpha;

        private RigFrame() {
        }
    }
}

