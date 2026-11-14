/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
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
 *  org.apache.logging.log4j.LogManager
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.youzi.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.youzi.YouziCombatGeometry;
import net.farmingtales.ftboss.youzi.YouziCombatPacket;
import net.farmingtales.ftboss.youzi.YouziHomingFlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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
import org.apache.logging.log4j.LogManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class YouziCombatVisual {
    private static final ResourceLocation WHITE = new ResourceLocation("ftboss", "youzi_spells/combat_white.png");
    private static final Map<Long, YouziCombatPacket> ACTIVE = new LinkedHashMap<Long, YouziCombatPacket>();
    private static final Map<Long, Long> SEEN = new LinkedHashMap<Long, Long>();
    private static final RenderType TYPE = RenderType.m_234338_((ResourceLocation)WHITE);
    private static final int MAXIMUM_ACTIVE = 48;
    private static ClientLevel currentLevel;
    private static YouziCombatGeometry geometry;
    private static boolean resourceFailed;

    private static YouziCombatGeometry geometry() {
        if (geometry != null || resourceFailed) {
            return geometry;
        }
        try (InputStream inputStream = YouziCombatVisual.class.getResourceAsStream("/assets/ftboss/youzi_spells/combat_fx.json");){
            if (inputStream == null) {
                throw new IllegalStateException("Missing independent Youzi combat resource");
            }
            JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
            geometry = new YouziCombatGeometry(jsonObject);
        }
        catch (Exception exception) {
            resourceFailed = true;
            LogManager.getLogger((String)"YouziCombatVisual").error("Youzi combat resource unavailable; independent effect disabled", (Throwable)exception);
        }
        return geometry;
    }

    private static void synchronizeLevel() {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel != currentLevel) {
            ACTIVE.clear();
            SEEN.clear();
            currentLevel = clientLevel;
        }
    }

    public static void accept(YouziCombatPacket youziCombatPacket) {
        YouziCombatVisual.synchronizeLevel();
        if (currentLevel == null || YouziCombatVisual.geometry() == null || !currentLevel.m_46472_().m_135782_().toString().equals(youziCombatPacket.dimension())) {
            return;
        }
        double d = (double)(currentLevel.m_46467_() - youziCombatPacket.startedTick()) / 20.0;
        if (d < -0.1 || d >= geometry.duration(youziCombatPacket.effect()) || SEEN.containsKey(youziCombatPacket.sequence())) {
            return;
        }
        if (SEEN.size() >= 512) {
            SEEN.remove(SEEN.keySet().iterator().next());
        }
        SEEN.put(youziCombatPacket.sequence(), youziCombatPacket.startedTick());
        if (ACTIVE.size() >= 48) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
        ACTIVE.put(youziCombatPacket.sequence(), youziCombatPacket);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        YouziCombatVisual.synchronizeLevel();
        if (currentLevel == null || YouziCombatVisual.geometry() == null) {
            return;
        }
        ACTIVE.values().removeIf(youziCombatPacket -> YouziCombatVisual.expired(youziCombatPacket));
        SEEN.values().removeIf(l -> currentLevel.m_46467_() - l > 80L);
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        ACTIVE.clear();
        SEEN.clear();
        currentLevel = null;
        geometry = null;
        resourceFailed = false;
    }

    private static boolean expired(YouziCombatPacket youziCombatPacket) {
        EasterBossEntity easterBossEntity;
        if ((double)(currentLevel.m_46467_() - youziCombatPacket.startedTick()) / 20.0 >= geometry.duration(youziCombatPacket.effect())) {
            return true;
        }
        if (!youziCombatPacket.effect().equals("cast_focus")) {
            return false;
        }
        Entity entity = currentLevel.m_6815_(youziCombatPacket.actorId());
        return !(entity instanceof EasterBossEntity) || !(easterBossEntity = (EasterBossEntity)entity).m_6084_() || !easterBossEntity.m_20148_().equals(youziCombatPacket.actor()) || !easterBossEntity.isCastingLike();
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == currentLevel) {
            ACTIVE.clear();
            SEEN.clear();
            currentLevel = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty() || YouziCombatVisual.geometry() == null) {
            return;
        }
        YouziCombatVisual.synchronizeLevel();
        if (currentLevel == null) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        YouziHomingFlight.Vector vector = new YouziHomingFlight.Vector(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_);
        Vector3f vector3f = renderLevelStageEvent.getCamera().m_253058_();
        YouziHomingFlight.Vector vector2 = new YouziHomingFlight.Vector(vector3f.x(), vector3f.y(), vector3f.z()).normalize();
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        MultiBufferSource.BufferSource bufferSource = Minecraft.m_91087_().m_91269_().m_110104_();
        boolean bl2 = SpellBloomRenderer.begin(renderLevelStageEvent, true);
        try {
            VertexConsumer vertexConsumer = bufferSource.m_6299_(TYPE);
            Matrix4f matrix4f = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
            int n = 6000;
            block3: for (YouziCombatPacket youziCombatPacket2 : ACTIVE.values().stream().sorted(Comparator.comparingDouble(youziCombatPacket -> youziCombatPacket.origin().subtract(vector).length())).toList()) {
                Object object;
                Object object2;
                if (n <= 0 || YouziCombatVisual.expired(youziCombatPacket2) || youziCombatPacket2.origin().subtract(vector).length() > 64.0) continue;
                double d = Math.max(0.0, (double)((float)currentLevel.m_46467_() + renderLevelStageEvent.getPartialTick() - (float)youziCombatPacket2.startedTick()) / 20.0);
                YouziHomingFlight.Vector vector3 = youziCombatPacket2.origin();
                YouziHomingFlight.Vector vector4 = youziCombatPacket2.target();
                if ((youziCombatPacket2.effect().equals("cast_focus") || youziCombatPacket2.effect().equals("melee_swing")) && (object2 = currentLevel.m_6815_(youziCombatPacket2.actorId())) != null && object2.m_20148_().equals(youziCombatPacket2.actor())) {
                    object = object2.m_20318_(renderLevelStageEvent.getPartialTick());
                    vector4 = vector3 = new YouziHomingFlight.Vector(((Vec3)object).f_82479_, ((Vec3)object).f_82480_, ((Vec3)object).f_82481_);
                }
                object2 = geometry.sample(youziCombatPacket2.effect(), d, vector3, vector4, youziCombatPacket2.scale(), youziCombatPacket2.yaw());
                object = object2.iterator();
                while (object.hasNext()) {
                    YouziCombatGeometry.Quad quad = (YouziCombatGeometry.Quad)object.next();
                    if (n-- <= 0) continue block3;
                    float f = (float)(quad.opacity() * 0.8);
                    if ((double)f < 0.005) continue;
                    YouziHomingFlight.Vector vector5 = quad.second().subtract(quad.first()).cross(quad.third().subtract(quad.first())).normalize();
                    YouziHomingFlight.Vector[] vectorArray = new YouziHomingFlight.Vector[]{quad.first(), quad.second(), quad.third(), quad.fourth()};
                    for (int i = 0; i < vectorArray.length; ++i) {
                        YouziHomingFlight.Vector vector6 = vectorArray[i].subtract(vector);
                        vertexConsumer.m_252986_(matrix4f, (float)vector6.coordinateX(), (float)vector6.coordinateY(), (float)vector6.coordinateZ()).m_85950_((float)quad.color().coordinateX(), (float)quad.color().coordinateY(), (float)quad.color().coordinateZ(), f).m_7421_(i == 0 || i == 3 ? 0.0f : 1.0f, i < 2 ? 0.0f : 1.0f).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_5601_((float)vector5.coordinateX(), (float)vector5.coordinateY(), (float)vector5.coordinateZ()).m_5752_();
                    }
                }
            }
            bufferSource.m_109912_(TYPE);
        }
        finally {
            SpellBloomRenderer.end(bl2);
        }
    }

    private static double smooth(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }
}

