/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.Resource
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
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
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.tidefish;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.client.tidefish.TideFishMesh;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class TideFishSpellVisual {
    private static final Set<String> IDS = Set.of("tidal_edict", "abyssal_geyser", "undertow_snare", "abyssal_carapace", "riptide_corridor");
    private static final List<Visual> ACTIVE = new ArrayList<Visual>();
    private static final Map<String, TideFishMesh> MESHES = new HashMap<String, TideFishMesh>();
    private static final int LIMIT = 24;
    private static ClientLevel world;

    private TideFishSpellVisual() {
    }

    public static boolean supports(String string) {
        return IDS.contains(string);
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket) {
        if (spellVisualPacket == null || !spellVisualPacket.valid()) {
            return false;
        }
        if (!TideFishSpellVisual.supports(spellVisualPacket.spell().id)) {
            return false;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !spellVisualPacket.valid() || !clientLevel.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) {
            return true;
        }
        if (clientLevel != world) {
            ACTIVE.clear();
            world = clientLevel;
        }
        long l = clientLevel.m_46467_();
        ACTIVE.removeIf(visual -> TideFishSpellVisual.sameOwner(visual.packet, spellVisualPacket) && (visual.packet.phase() == SpellVisualRules.Phase.CHARGE || spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM && spellVisualPacket.spell().id.equals("tidal_edict") && visual.packet.phase() == SpellVisualRules.Phase.RELEASE || visual.packet.phase() == spellVisualPacket.phase() && visual.packet.start().m_82554_(spellVisualPacket.start()) < 0.04));
        if (ACTIVE.size() >= 24) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Visual(spellVisualPacket, l));
        return true;
    }

    private static boolean sameOwner(SpellVisualPacket spellVisualPacket, SpellVisualPacket spellVisualPacket2) {
        return spellVisualPacket.owner().equals(spellVisualPacket2.owner()) && spellVisualPacket.spell() == spellVisualPacket2.spell();
    }

    @SubscribeEvent
    public static void tideFishTick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || clientLevel != world) {
            ACTIVE.clear();
            world = clientLevel;
            return;
        }
        ACTIVE.removeIf(visual -> {
            LivingEntity livingEntity;
            long l = clientLevel.m_46467_() - visual.born;
            Entity entity = TideFishSpellVisual.owner(clientLevel, visual.packet);
            return l < 0L || l >= (long)visual.packet.spell().lifetime(visual.packet.phase()) || entity == null || entity instanceof LivingEntity && !(livingEntity = (LivingEntity)entity).m_6084_();
        });
    }

    private static Entity owner(ClientLevel clientLevel, SpellVisualPacket spellVisualPacket) {
        Entity entity = clientLevel.m_6815_(spellVisualPacket.ownerId());
        return entity != null && entity.m_20148_().equals(spellVisualPacket.owner()) ? entity : null;
    }

    @SubscribeEvent
    public static void tideFishUnload(LevelEvent.Unload unload) {
        if (unload.getLevel() == world) {
            ACTIVE.clear();
            world = null;
        }
    }

    @SubscribeEvent
    public static void tideFishLogout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        ACTIVE.clear();
        world = null;
    }

    public static void reload() {
        MESHES.clear();
        ACTIVE.clear();
    }

    private static TideFishMesh mesh(String string2) {
        return MESHES.computeIfAbsent(string2, string -> {
            ResourceManager resourceManager = Minecraft.m_91087_().m_91098_();
            try (InputStreamReader inputStreamReader = new InputStreamReader(((Resource)resourceManager.m_213713_(new ResourceLocation("ftboss", "geo/tidefish/" + string + ".geo.json")).orElseThrow()).m_215507_(), StandardCharsets.UTF_8);){
                TideFishMesh tideFishMesh;
                try (InputStreamReader inputStreamReader2 = new InputStreamReader(((Resource)resourceManager.m_213713_(new ResourceLocation("ftboss", "motion_data/tidefish/" + string + ".json")).orElseThrow()).m_215507_(), StandardCharsets.UTF_8);){
                    tideFishMesh = new TideFishMesh(inputStreamReader, inputStreamReader2);
                }
                return tideFishMesh;
            }
            catch (Exception exception) {
                throw new IllegalStateException("Fish king model: " + string, exception);
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void tideFishRender(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty() || world == null) {
            return;
        }
        boolean bl = SpellBloomRenderer.begin(renderLevelStageEvent, true);
        try {
            Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
            ArrayList<Visual> arrayList = new ArrayList<Visual>(ACTIVE);
            arrayList.sort(Comparator.comparingDouble(visual -> visual.packet.start().m_82554_(vec3)).reversed());
            MultiBufferSource.BufferSource bufferSource = Minecraft.m_91087_().m_91269_().m_110104_();
            for (Visual visual2 : arrayList) {
                TideFishSpellVisual.draw(renderLevelStageEvent, visual2, vec3, bufferSource);
            }
            bufferSource.m_109911_();
        }
        finally {
            SpellBloomRenderer.end(bl);
        }
    }

    private static void draw(RenderLevelStageEvent renderLevelStageEvent, Visual visual, Vec3 vec3, MultiBufferSource.BufferSource bufferSource) {
        Vec3 vec32;
        SpellVisualPacket spellVisualPacket = visual.packet;
        Entity entity = TideFishSpellVisual.owner(world, spellVisualPacket);
        if (entity == null) {
            return;
        }
        double d = (float)(world.m_46467_() - visual.born) + renderLevelStageEvent.getPartialTick();
        int n = spellVisualPacket.spell().lifetime(spellVisualPacket.phase());
        if (d < 0.0 || d >= (double)n) {
            return;
        }
        Vec3 vec33 = vec32 = spellVisualPacket.spell().followsCaster(spellVisualPacket.phase()) ? entity.m_20182_() : spellVisualPacket.start();
        if (vec32.m_82554_(vec3) > 9216.0) {
            return;
        }
        Vec3 vec34 = spellVisualPacket.end().m_82546_(spellVisualPacket.start());
        float f = (float)Math.atan2(vec34.f_82479_, vec34.f_82481_);
        if (vec34.m_82556_() < 0.01) {
            f = (float)Math.toRadians(-entity.m_146908_());
        }
        float f2 = (float)(Math.min(1.0, (d + 1.0) / 2.0) * Math.min(1.0, ((double)n - d) / 3.0));
        float f3 = (float)Math.max(0.0, Math.min(1.0, d / (double)n));
        TideFishMesh tideFishMesh = TideFishSpellVisual.mesh(spellVisualPacket.spell().id);
        Matrix4f matrix4f = new Matrix4f().translation((float)(vec32.f_82479_ - vec3.f_82479_), (float)(vec32.f_82480_ - vec3.f_82480_ + 0.035), (float)(vec32.f_82481_ - vec3.f_82481_));
        if (spellVisualPacket.spell().id.equals("tidal_edict") || spellVisualPacket.spell().id.equals("riptide_corridor")) {
            matrix4f.rotateY(f);
        }
        if (spellVisualPacket.spell().id.equals("tidal_edict") && spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM) {
            vec32 = spellVisualPacket.start().m_165921_(spellVisualPacket.end(), 0.5);
            matrix4f.identity().translation((float)(vec32.f_82479_ - vec3.f_82479_), (float)(vec32.f_82480_ - vec3.f_82480_), (float)(vec32.f_82481_ - vec3.f_82481_)).rotateY(f - 1.5707964f).scale((float)Math.sqrt(vec34.m_82556_()) / 13.9f, 0.4f, 0.15f);
        }
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        PoseStack.Pose pose = poseStack.m_85850_();
        ArrayList<Face> arrayList = new ArrayList<Face>();
        for (TideFishMesh.Part object : tideFishMesh.parts) {
            Matrix4f matrix4f2 = new Matrix4f((Matrix4fc)matrix4f).mul((Matrix4fc)tideFishMesh.pose(spellVisualPacket.phase().name().toLowerCase(Locale.ROOT), object.name(), f3));
            for (TideFishMesh.Quad quad : object.quads()) {
                float[][] fArray = new float[4][3];
                Vector3f vector3f = new Vector3f();
                for (int i = 0; i < 4; ++i) {
                    Vector3f vector3f2 = matrix4f2.transformPosition(new Vector3f(quad.points()[i]));
                    fArray[i] = new float[]{vector3f2.x, vector3f2.y, vector3f2.z};
                    vector3f.add((Vector3fc)vector3f2);
                }
                Vector3f vector3f3 = matrix4f2.transformDirection(new Vector3f(quad.normal())).normalize();
                arrayList.add(new Face(fArray, quad.uv(), new float[]{vector3f3.x, vector3f3.y, vector3f3.z}, vector3f.lengthSquared()));
            }
        }
        arrayList.sort(Comparator.comparingDouble(Face::depth).reversed());
        VertexConsumer vertexConsumer = bufferSource.m_6299_(RenderType.m_234338_((ResourceLocation)new ResourceLocation("ftboss", "textures/fx/tidefish/" + spellVisualPacket.spell().id + ".png")));
        for (Face face : arrayList) {
            for (int i = 0; i < 4; ++i) {
                float[] fArray = face.points[i];
                vertexConsumer.m_252986_(pose.m_252922_(), fArray[0], fArray[1], fArray[2]).m_85950_(1.0f, 1.0f, 1.0f, f2 * 0.87f).m_7421_(face.uv[i][0], face.uv[i][1]).m_86008_(655360).m_85969_(0xF000F0).m_252939_(pose.m_252943_(), face.normal[0], face.normal[1], face.normal[2]).m_5752_();
            }
        }
    }

    private record Visual(SpellVisualPacket packet, long born) {
    }

    private record Face(float[][] points, float[][] uv, float[] normal, double depth) {
    }

    @Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
    public static final class Resources {
        @SubscribeEvent
        public static void registerTideFishReload(RegisterClientReloadListenersEvent registerClientReloadListenersEvent) {
            registerClientReloadListenersEvent.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)resourceManager -> TideFishSpellVisual.reload()));
        }
    }
}

