package net.farmingtales.ftboss.youzi.spells.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.youzi.YouziHomingFlight.Vector;
import net.farmingtales.ftboss.youzi.facing.YouziFacing;
import net.farmingtales.ftboss.youzi.spells.YouziSpellAssets;
import net.farmingtales.ftboss.youzi.spells.YouziSpellGeometry;
import net.farmingtales.ftboss.youzi.spells.YouziSpellPacket;
import net.farmingtales.ftboss.youzi.spells.YouziSpellTiming;
import net.farmingtales.ftboss.youzi.spells.YouziStarProjectile;
import net.farmingtales.ftboss.youzi.spells.YouziVisibility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@EventBusSubscriber(
    modid = "ftboss",
    value = {Dist.CLIENT}
)
public final class YouziSpellVisual {
    public static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("ftboss", "youzi_spells/youzi_spell_atlas.png");
    private static final ResourceLocation RIBBON = ResourceLocation.fromNamespaceAndPath("ftboss", "youzi_spells/effigy_flight_ribbon.png");
    private static final RenderType MODEL_TYPE = RenderType.entityTranslucentEmissive(ATLAS);
    private static final RenderType TRAIL_TYPE = RenderType.entityTranslucentEmissive(RIBBON);
    private static final Map<String, YouziSpellVisual.Active> ACTIVE = new LinkedHashMap<>();
    private static final Map<Integer, YouziSpellVisual.Star> QUEUED = new LinkedHashMap<>();
    private static final Map<Integer, ArrayDeque<Vector>> HISTORY = new LinkedHashMap<>();
    private static final List<YouziSpellVisual.Impact> IMPACTS = new ArrayList<>();
    private static ClientLevel world;
    private static int budget;

    public static boolean supports(String var0) {
        return var0.equals("effigy_volley") || var0.equals("effigy_ward") || var0.equals("effigy_procession");
    }

    private static void synchronize() {
        ClientLevel var0 = Minecraft.getInstance().level;
        if (var0 != world) {
            ACTIVE.clear();
            QUEUED.clear();
            HISTORY.clear();
            IMPACTS.clear();
            world = var0;
        }
    }

    public static boolean accept(SpellVisualPacket var0) {
        if (var0 != null && var0.valid() && supports(var0.spell().id) && YouziSpellAssets.geometry() != null) {
            synchronize();
            if (world != null && world.dimension().location().equals(var0.dimension())) {
                long var1 = world.getGameTime();
                String var3 = var0.owner() + ":" + var0.spell().id;
                if (ACTIVE.containsKey(var3) && ACTIVE.get(var3).authoritative) {
                    return true;
                } else if (var0.phase() == Phase.BEAM) {
                    if (IMPACTS.size() >= 24) {
                        IMPACTS.remove(0);
                    }

                    IMPACTS.add(new YouziSpellVisual.Impact(vector(var0.end()), var1));
                    return true;
                } else {
                    String var4 = var0.owner() + ":" + var0.spell().id;
                    YouziSpellVisual.Active var5 = ACTIVE.get(var4);
                    if (var0.phase() == Phase.PULSE && var5 != null && var5.packet.phase() != Phase.CHARGE) {
                        var5.packet = var0;
                        var5.refreshed = var1;
                        return true;
                    } else if (var0.phase() == Phase.CHARGE && var5 != null && var5.packet.phase() == Phase.CHARGE) {
                        var5.packet = var0;
                        var5.refreshed = var1;
                        return true;
                    } else {
                        if (ACTIVE.size() >= 12 && !ACTIVE.containsKey(var4)) {
                            ACTIVE.remove(ACTIVE.keySet().iterator().next());
                        }

                        ACTIVE.put(var4, new YouziSpellVisual.Active(var0, var1));
                        return true;
                    }
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public static void acceptAuthoritative(YouziSpellPacket var0) {
        synchronize();
        SpellVisualPacket var1 = var0.visual();
        if (world != null && world.dimension().location().equals(var1.dimension()) && supports(var1.spell().id)) {
            long var2 = world.getGameTime();
            if (YouziSpellTiming.eventFresh(var2, var0.sentTick(), var1.phase().name())) {
                if (var1.phase() == Phase.BEAM) {
                    if (IMPACTS.size() >= 24) {
                        IMPACTS.remove(0);
                    }

                    IMPACTS.add(new YouziSpellVisual.Impact(vector(var1.end()), var0.sentTick()));
                } else {
                    String var4 = var1.owner() + ":" + var1.spell().id;
                    YouziSpellVisual.Active var5 = ACTIVE.get(var4);
                    if (var5 == null
                        || !var5.authoritative
                        || YouziSpellTiming.ordered(var0.sentTick(), var1.phase().name(), var5.refreshed, var5.packet.phase().name())) {
                        boolean var6 = var5 != null
                            && (
                                var1.phase() == Phase.PULSE && var5.packet.phase() != Phase.CHARGE
                                    || var1.phase() == Phase.CHARGE && var5.packet.phase() == Phase.CHARGE
                            );
                        YouziSpellVisual.Active var7 = var6
                            ? var5
                            : new YouziSpellVisual.Active(var1, var1.phase() == Phase.CHARGE ? var0.castStarted() : var0.sentTick());
                        var7.packet = var1;
                        var7.refreshed = var0.sentTick();
                        var7.castStarted = var0.castStarted();
                        var7.castDuration = var0.castDuration();
                        var7.serverScale = var0.scale();
                        var7.authoritative = true;
                        if (ACTIVE.size() >= 12 && !ACTIVE.containsKey(var4)) {
                            ACTIVE.remove(ACTIVE.keySet().iterator().next());
                        }

                        ACTIVE.put(var4, var7);
                    }
                }
            }
        }
    }

    public static void queue(YouziStarProjectile var0, float var1) {
        synchronize();
        if (QUEUED.size() < 64) {
            QUEUED.put(var0.getId(), new YouziSpellVisual.Star(var0, var1));
        }
    }

    private static LivingEntity owner(YouziSpellVisual.Active var0) {
        if (world.getEntity(var0.packet.ownerId()) instanceof LivingEntity var2 && var2.isAlive() && var2.getUUID().equals(var0.packet.owner())) {
            return var2;
        }

        return null;
    }

    private static boolean expired(YouziSpellVisual.Active var0, long var1) {
        LivingEntity var3 = owner(var0);
        return YouziSpellTiming.expired(
            var0.packet.spell().id,
            var0.packet.phase().name(),
            var1,
            var0.started,
            var0.refreshed,
            var3 != null,
            var3 != null && (var3.hasEffect(MobEffects.DAMAGE_RESISTANCE) || var3.hasEffect(MobEffects.ABSORPTION)),
            var3 == null ? 0.0 : vector(var3.position()).subtract(vector(var0.packet.start())).length()
        );
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post var0) {
        synchronize();
        if (world != null) {
            ACTIVE.values().removeIf(var0x -> expired(var0x, world.getGameTime()));
            IMPACTS.removeIf(var0x -> world.getGameTime() - var0x.tick() > 4L);
            HISTORY.keySet().removeIf(var0x -> !(world.getEntity(var0x) instanceof YouziStarProjectile));

            for (Entity var2 : world.entitiesForRendering()) {
                if (var2 instanceof YouziStarProjectile && (HISTORY.size() < 64 || HISTORY.containsKey(var2.getId()))) {
                    ArrayDeque var3 = HISTORY.computeIfAbsent(var2.getId(), var0x -> new ArrayDeque<>());
                    var3.addLast(vector(var2.position()));

                    while (var3.size() > 3) {
                        var3.removeFirst();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void unload(Unload var0) {
        if (var0.getLevel() == world) {
            clear();
        }
    }

    @SubscribeEvent
    public static void logout(LoggingOut var0) {
        clear();
    }

    private static void clear() {
        ACTIVE.clear();
        QUEUED.clear();
        HISTORY.clear();
        IMPACTS.clear();
        world = null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent var0) {
        if (var0.getStage() == Stage.AFTER_PARTICLES) {
            synchronize();
            YouziSpellGeometry var1 = YouziSpellAssets.geometry();
            if (world != null && var1 != null && (!ACTIVE.isEmpty() || !QUEUED.isEmpty() || !IMPACTS.isEmpty())) {
                boolean var2 = SpellBloomRenderer.begin(var0, true);

                try {
                    Vector var3 = vector(var0.getCamera().getPosition());
                    Vector3f var4 = var0.getCamera().getLookVector();
                    Vector var5 = new Vector((double)var4.x(), (double)var4.y(), (double)var4.z());
                    boolean var6 = !var0.getCamera().isDetached();
                    Matrix4f var7 = var0.getPoseStack().last().pose();
                    BufferSource var8 = Minecraft.getInstance().renderBuffers().bufferSource();
                    VertexConsumer var9 = var8.getBuffer(MODEL_TYPE);
                    budget = 6000;

                    for (YouziSpellVisual.Star var11 : QUEUED.values()) {
                        Vector var12 = vector(var11.entity().getPosition(var11.partial()));
                        Vector var13 = vector(var11.entity().getDeltaMovement()).normalize();
                        var1.seed(var11.entity().scale(), var7x -> draw(var9, var7, oriented(var7x, var12, var13), var3, var5, var6, 1.0));
                    }

                    List<YouziSpellVisual.Active> var32 = ACTIVE.values()
                        .stream()
                        .filter(var0x -> !expired(var0x, world.getGameTime()))
                        .sorted(Comparator.comparingDouble(var1x -> vector(var1x.packet.start()).subtract(var3).length()))
                        .toList();

                    for (YouziSpellVisual.Active var35 : var32) {
                        LivingEntity var39 = owner(var35);
                        String var14 = var35.packet.spell().id;
                        boolean var15 = var14.equals("effigy_procession");
                        Vector var16 = vector(var15 ? var35.packet.start() : var39.getPosition(var0.getPartialTick().getGameTimeDeltaPartialTick(false)));
                        if (!(var16.subtract(var3).length() > 64.0) && budget >= 1200) {
                            double var17 = (double)Math.max(0.0F, ((float)world.getGameTime() + var0.getPartialTick().getGameTimeDeltaPartialTick(false) - (float)var35.started) / 20.0F);
                            MagicData var19 = MagicData.getPlayerMagicData(var39);
                            double var20 = var35.authoritative && var35.castDuration > 0
                                ? (double)(((float)world.getGameTime() + var0.getPartialTick().getGameTimeDeltaPartialTick(false) - (float)var35.castStarted) / (float)var35.castDuration)
                                : (double)(var19.getCastCompletionPercent() + var0.getPartialTick().getGameTimeDeltaPartialTick(false) / (float)Math.max(1, var19.getCastDuration()));
                            YouziSpellTiming.Sample var22 = YouziSpellTiming.sample(
                                var14, var35.packet.phase() == Phase.CHARGE, var17, var20, var1.length(var14, "charge"), var1.length(var14, "release")
                            );
                            Vector var23 = vector(var35.packet.end()).subtract(vector(var35.packet.start()));
                            double var24 = YouziFacing.modelYaw(
                                Math.toDegrees(Math.atan2(-var23.coordinateX(), -var23.coordinateZ())), var39, var35.packet, var0.getPartialTick().getGameTimeDeltaPartialTick(false)
                            );
                            double var26 = var35.authoritative ? var35.serverScale : BossSpellTuning.radiusScale(var39);
                            int var28 = var16.subtract(var3).length() > 32.0 ? 4 : (var16.subtract(var3).length() > 16.0 ? 2 : 1);
                            var1.sample(
                                var14,
                                var22.channel(),
                                var22.seconds(),
                                var26,
                                var15,
                                var22.channel().equals("charge"),
                                var28,
                                var9x -> draw(var9, var7, transformed(var9x, var16, var24), var3, var5, var6, var22.opacity())
                            );
                        }
                    }

                    var8.endBatch(MODEL_TYPE);
                    VertexConsumer var34 = var8.getBuffer(TRAIL_TYPE);
                    budget = 1600;

                    for (YouziSpellVisual.Active var40 : var32) {
                        if (var40.packet.spell().id.equals("effigy_procession") && var40.packet.phase() != Phase.CHARGE) {
                            Vector var43 = vector(var40.packet.start());
                            if (!(var43.subtract(var3).length() > 64.0)) {
                                double var46 = var40.authoritative ? var40.serverScale : BossSpellTuning.radiusScale(owner(var40));
                                double var50 = (double)(((float)world.getGameTime() + var0.getPartialTick().getGameTimeDeltaPartialTick(false) - (float)var40.started) / 20.0F);
                                double var53 = YouziVisibility.smooth((60.0 - var50 * 20.0) / 8.0) * 0.24;

                                for (int var21 = 0; var21 < 64; var21++) {
                                    double var58 = (Math.PI * 2) * (double)var21 / 64.0;
                                    double var61 = (Math.PI * 2) * (double)(var21 + 1) / 64.0;
                                    Vector[] var63 = new Vector[]{
                                        var43.add(new Vector(Math.cos(var58) * 4.0 * var46, 0.025, Math.sin(var58) * 4.0 * var46)),
                                        var43.add(new Vector(Math.cos(var61) * 4.0 * var46, 0.025, Math.sin(var61) * 4.0 * var46)),
                                        var43.add(new Vector(Math.cos(var61) * 3.95 * var46, 0.025, Math.sin(var61) * 3.95 * var46)),
                                        var43.add(new Vector(Math.cos(var58) * 3.95 * var46, 0.025, Math.sin(var58) * 3.95 * var46))
                                    };
                                    draw(
                                        var34,
                                        var7,
                                        new YouziSpellGeometry.Quad(var63, new double[][]{{0.0, 0.0}, {1.0, 0.0}, {1.0, 1.0}, {0.0, 1.0}}, var53, 1.0, true),
                                        var3,
                                        var5,
                                        var6,
                                        1.0
                                    );
                                }
                            }
                        }
                    }

                    for (YouziSpellVisual.Star var41 : QUEUED.values()) {
                        ArrayDeque var44 = HISTORY.get(var41.entity().getId());
                        if (var44 != null) {
                            ArrayList var47 = new ArrayList(var44);
                            var47.add(vector(var41.entity().getPosition(var41.partial())));

                            for (int var48 = 1; var48 < var47.size(); var48++) {
                                Vector var51 = (Vector)var47.get(var48 - 1);
                                Vector var18 = (Vector)var47.get(var48);
                                Vector var54 = var18.subtract(var51).cross(var3.subtract(var51)).normalize().multiply(0.065 * var41.entity().scale());
                                YouziSpellGeometry.Quad var56 = new YouziSpellGeometry.Quad(
                                    new Vector[]{var51.add(var54), var18.add(var54), var18.subtract(var54), var51.subtract(var54)},
                                    new double[][]{{0.0, 1.0}, {1.0, 0.0}, {1.0, 1.0}, {0.0, 0.0}},
                                    0.55 * (double)var48 / (double)var47.size(),
                                    1.0,
                                    true
                                );
                                draw(var34, var7, var56, var3, var5, var6, 1.0);
                            }
                        }
                    }

                    for (YouziSpellVisual.Impact var42 : IMPACTS) {
                        double var45 = (double)(((float)world.getGameTime() + var0.getPartialTick().getGameTimeDeltaPartialTick(false) - (float)var42.tick()) / 20.0F);
                        if (!(var45 < 0.0) && !(var45 >= 0.18)) {
                            double var49 = 0.08 + var45 / 0.18 * 0.3;

                            for (int var52 = 0; var52 < 8; var52++) {
                                double var55 = (Math.PI * 2) * (double)var52 / 8.0;
                                Vector var57 = new Vector(Math.cos(var55), 0.25, Math.sin(var55));
                                Vector var59 = var42.point().add(var57.multiply(var49));
                                Vector var60 = var42.point().add(var57.multiply(var49 + 0.12));
                                Vector var62 = var57.cross(var3.subtract(var59)).normalize().multiply(0.018);
                                draw(
                                    var34,
                                    var7,
                                    new YouziSpellGeometry.Quad(
                                        new Vector[]{var59.add(var62), var60.add(var62), var60.subtract(var62), var59.subtract(var62)},
                                        new double[][]{{0.0, 0.0}, {1.0, 0.0}, {1.0, 1.0}, {0.0, 1.0}},
                                        1.0 - var45 / 0.18,
                                        1.0,
                                        true
                                    ),
                                    var3,
                                    var5,
                                    var6,
                                    1.0
                                );
                            }
                        }
                    }

                    var8.endBatch(TRAIL_TYPE);
                } finally {
                    QUEUED.clear();
                    SpellBloomRenderer.end(var2);
                }
            } else {
                QUEUED.clear();
            }
        }
    }

    private static YouziSpellGeometry.Quad transformed(YouziSpellGeometry.Quad var0, Vector var1, double var2) {
        Vector[] var4 = new Vector[4];

        for (int var5 = 0; var5 < 4; var5++) {
            var4[var5] = YouziSpellGeometry.rotate(var0.points()[var5], new Vector(0.0, var2, 0.0)).add(var1);
        }

        return new YouziSpellGeometry.Quad(var4, var0.uv(), var0.opacity(), var0.emission(), var0.accent());
    }

    private static YouziSpellGeometry.Quad oriented(YouziSpellGeometry.Quad var0, Vector var1, Vector var2) {
        Vector var3 = var2.cross(new Vector(0.0, 1.0, 0.0));
        if (var3.length() < 1.0E-6) {
            var3 = var2.cross(new Vector(1.0, 0.0, 0.0));
        }

        var3 = var3.normalize();
        Vector var4 = var3.cross(var2).normalize();
        Vector[] var5 = new Vector[4];

        for (int var6 = 0; var6 < 4; var6++) {
            Vector var7 = var0.points()[var6];
            var5[var6] = var1.add(var3.multiply(var7.coordinateX())).add(var4.multiply(var7.coordinateY())).subtract(var2.multiply(var7.coordinateZ()));
        }

        return new YouziSpellGeometry.Quad(var5, var0.uv(), var0.opacity(), var0.emission(), var0.accent());
    }

    private static void draw(VertexConsumer var0, Matrix4f var1, YouziSpellGeometry.Quad var2, Vector var3, Vector var4, boolean var5, double var6) {
        if (budget-- > 0) {
            Vector var8 = var2.points()[0].add(var2.points()[1]).add(var2.points()[2]).add(var2.points()[3]).multiply(0.25);
            float var9 = (float)(var2.opacity() * var6 * YouziVisibility.opacity(var8, var3, var4, var5));
            if (!((double)var9 < 0.005)) {
                Vector var10 = var2.points()[1].subtract(var2.points()[0]).cross(var2.points()[2].subtract(var2.points()[0])).normalize();

                for (int var11 = 0; var11 < 4; var11++) {
                    Vector var12 = var2.points()[var11].subtract(var3);
                    var0.addVertex(var1, (float)var12.coordinateX(), (float)var12.coordinateY(), (float)var12.coordinateZ())
                        .setColor(1.0F, 1.0F, 1.0F, var9)
                        .setUv((float)var2.uv()[var11][0], (float)var2.uv()[var11][1])
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(15728880)
                        .setNormal((float)var10.coordinateX(), (float)var10.coordinateY(), (float)var10.coordinateZ());
                }
            }
        }
    }

    private static Vector vector(Vec3 var0) {
        return new Vector(var0.x, var0.y, var0.z);
    }

    private static final class Active {
        SpellVisualPacket packet;
        final long started;
        long refreshed;
        long castStarted;
        int castDuration;
        double serverScale = 1.0;
        boolean authoritative;

        Active(SpellVisualPacket var1, long var2) {
            this.packet = var1;
            this.started = this.refreshed = var2;
        }
    }

    private static record Impact(Vector point, long tick) {
    }

    private static record Star(YouziStarProjectile entity, float partial) {
    }
}
