package net.farmingtales.ftboss.youzi.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.apache.logging.log4j.LogManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@EventBusSubscriber(
    modid = "ftboss",
    value = {Dist.CLIENT}
)
public final class YouziCombatVisual {
    private static final ResourceLocation WHITE = ResourceLocation.fromNamespaceAndPath("ftboss", "youzi_spells/combat_white.png");
    private static final Map<Long, YouziCombatPacket> ACTIVE = new LinkedHashMap<>();
    private static final Map<Long, Long> SEEN = new LinkedHashMap<>();
    private static final RenderType TYPE = RenderType.entityTranslucentEmissive(WHITE);
    private static final int MAXIMUM_ACTIVE = 48;
    private static ClientLevel currentLevel;
    private static YouziCombatGeometry geometry;
    private static boolean resourceFailed;

    private static YouziCombatGeometry geometry() {
        if (geometry == null && !resourceFailed) {
            try (InputStream var0 = YouziCombatVisual.class.getResourceAsStream("/assets/ftboss/youzi_spells/combat_fx.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Missing independent Youzi combat resource");
                }

                JsonObject var1 = JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject();
                geometry = new YouziCombatGeometry(var1);
            } catch (Exception var5) {
                resourceFailed = true;
                LogManager.getLogger("YouziCombatVisual").error("Youzi combat resource unavailable; independent effect disabled", (Throwable)var5);
            }

            return geometry;
        } else {
            return geometry;
        }
    }

    private static void synchronizeLevel() {
        ClientLevel var0 = Minecraft.getInstance().level;
        if (var0 != currentLevel) {
            ACTIVE.clear();
            SEEN.clear();
            currentLevel = var0;
        }
    }

    public static void accept(YouziCombatPacket var0) {
        synchronizeLevel();
        if (currentLevel != null && geometry() != null && currentLevel.dimension().location().toString().equals(var0.dimension())) {
            double var1 = (double)(currentLevel.getGameTime() - var0.startedTick()) / 20.0;
            if (!(var1 < -0.1) && !(var1 >= geometry.duration(var0.effect())) && !SEEN.containsKey(var0.sequence())) {
                if (SEEN.size() >= 512) {
                    SEEN.remove(SEEN.keySet().iterator().next());
                }

                SEEN.put(var0.sequence(), var0.startedTick());
                if (ACTIVE.size() >= 48) {
                    ACTIVE.remove(ACTIVE.keySet().iterator().next());
                }

                ACTIVE.put(var0.sequence(), var0);
            }
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post var0) {
        {
            synchronizeLevel();
            if (currentLevel != null && geometry() != null) {
                ACTIVE.values().removeIf(var0x -> expired(var0x));
                SEEN.values().removeIf(var0x -> currentLevel.getGameTime() - var0x > 80L);
            }
        }
    }

    @SubscribeEvent
    public static void logout(LoggingOut var0) {
        ACTIVE.clear();
        SEEN.clear();
        currentLevel = null;
        geometry = null;
        resourceFailed = false;
    }

    private static boolean expired(YouziCombatPacket var0) {
        if ((double)(currentLevel.getGameTime() - var0.startedTick()) / 20.0 >= geometry.duration(var0.effect())) {
            return true;
        } else if (!var0.effect().equals("cast_focus")) {
            return false;
        } else {
            if (currentLevel.getEntity(var0.actorId()) instanceof EasterBossEntity var2
                && var2.isAlive()
                && var2.getUUID().equals(var0.actor())
                && var2.isCastingLike()) {
                return false;
            }

            return true;
        }
    }

    @SubscribeEvent
    public static void unload(Unload var0) {
        if (var0.getLevel() == currentLevel) {
            ACTIVE.clear();
            SEEN.clear();
            currentLevel = null;
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent var0) {
        if (var0.getStage() == Stage.AFTER_PARTICLES && !ACTIVE.isEmpty() && geometry() != null) {
            synchronizeLevel();
            if (currentLevel != null) {
                Vec3 var1 = var0.getCamera().getPosition();
                YouziHomingFlight.Vector var2 = new YouziHomingFlight.Vector(var1.x, var1.y, var1.z);
                Vector3f var3 = var0.getCamera().getLookVector();
                YouziHomingFlight.Vector var4 = new YouziHomingFlight.Vector((double)var3.x(), (double)var3.y(), (double)var3.z()).normalize();
                boolean var5 = !var0.getCamera().isDetached();
                BufferSource var6 = Minecraft.getInstance().renderBuffers().bufferSource();
                boolean var7 = SpellBloomRenderer.begin(var0, true);

                try {
                    VertexConsumer var8 = var6.getBuffer(TYPE);
                    Matrix4f var9 = var0.getPoseStack().last().pose();
                    int var10 = 6000;

                    for (YouziCombatPacket var12 : ACTIVE.values()
                        .stream()
                        .sorted(Comparator.comparingDouble(var1x -> var1x.origin().subtract(var2).length()))
                        .toList()) {
                        if (var10 > 0 && !expired(var12) && !(var12.origin().subtract(var2).length() > 64.0)) {
                            double var13 = Math.max(
                                0.0, (double)((float)currentLevel.getGameTime() + var0.getPartialTick().getGameTimeDeltaPartialTick(false) - (float)var12.startedTick()) / 20.0
                            );
                            YouziHomingFlight.Vector var15 = var12.origin();
                            YouziHomingFlight.Vector var16 = var12.target();
                            if (var12.effect().equals("cast_focus") || var12.effect().equals("melee_swing")) {
                                Entity var17 = currentLevel.getEntity(var12.actorId());
                                if (var17 != null && var17.getUUID().equals(var12.actor())) {
                                    Vec3 var18 = var17.getPosition(var0.getPartialTick().getGameTimeDeltaPartialTick(false));
                                    var15 = new YouziHomingFlight.Vector(var18.x, var18.y, var18.z);
                                    var16 = var15;
                                }
                            }

                            for (YouziCombatGeometry.Quad var19 : geometry.sample(var12.effect(), var13, var15, var16, var12.scale(), var12.yaw())) {
                                if (var10-- <= 0) {
                                    break;
                                }

                                float var20 = (float)(var19.opacity() * 0.8);
                                if (!((double)var20 < 0.005)) {
                                    YouziHomingFlight.Vector var21 = var19.second()
                                        .subtract(var19.first())
                                        .cross(var19.third().subtract(var19.first()))
                                        .normalize();
                                    YouziHomingFlight.Vector[] var22 = new YouziHomingFlight.Vector[]{
                                        var19.first(), var19.second(), var19.third(), var19.fourth()
                                    };

                                    for (int var23 = 0; var23 < var22.length; var23++) {
                                        YouziHomingFlight.Vector var24 = var22[var23].subtract(var2);
                                        var8.addVertex(var9, (float)var24.coordinateX(), (float)var24.coordinateY(), (float)var24.coordinateZ())
                                            .setColor(
                                                (float)var19.color().coordinateX(),
                                                (float)var19.color().coordinateY(),
                                                (float)var19.color().coordinateZ(),
                                                var20
                                            )
                                            .setUv(var23 != 0 && var23 != 3 ? 1.0F : 0.0F, var23 < 2 ? 0.0F : 1.0F)
                                            .setOverlay(OverlayTexture.NO_OVERLAY)
                                            .setLight(15728880)
                                            .setNormal((float)var21.coordinateX(), (float)var21.coordinateY(), (float)var21.coordinateZ());
                                    }
                                }
                            }
                        }
                    }

                    var6.endBatch(TYPE);
                } finally {
                    SpellBloomRenderer.end(var7);
                }
            }
        }
    }

    private static double smooth(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, var0));
        return var2 * var2 * (3.0 - 2.0 * var2);
    }
}
