package net.farmingtales.ftboss.client.xueli_combat;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.render.NativeLegacyVertexNormals;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.combat.xueli.CombatPacket;
import net.farmingtales.ftboss.combat.xueli.CombatRules;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@EventBusSubscriber(
    modid = "ftboss",
    value = {Dist.CLIENT}
)
public final class XueliCombatRenderer {
    private static final Map<String, CombatAsset.Effect> ASSETS = CombatAsset.load();
    private static final RenderType LIGHT = RenderType.entityTranslucentEmissive(ResourceLocation.fromNamespaceAndPath("ftboss", "xueli_combat/frost_arcana.png"));
    private static final List<XueliCombatRenderer.Activation> ACTIVE = new ArrayList<>();
    private static final Map<XueliCombatRenderer.StampKey, Long> STAMPS = new HashMap<>();
    private static ClientLevel world;

    private XueliCombatRenderer() {
    }

    private static void clear() {
        ACTIVE.clear();
        STAMPS.clear();
        world = null;
    }

    @SubscribeEvent
    public static void disconnect(LoggingOut var0) {
        clear();
    }

    @SubscribeEvent
    public static void unload(Unload var0) {
        if (var0.getLevel() == world) {
            clear();
        }
    }

    public static void accept(CombatPacket var0) {
        ClientLevel var1 = Minecraft.getInstance().level;
        if (var1 != null && var0 != null && var0.valid() && var0.dimension().equals(var1.dimension().location())) {
            if (world != var1) {
                clear();
                world = var1;
            }

            long var2 = var1.getGameTime();
            int var4 = (var0.kind() == 6 || var0.kind() == 11) && !var0.guards().isEmpty() ? var0.guards().get(0).entityId() : var0.ownerId();
            XueliCombatRenderer.StampKey var5 = new XueliCombatRenderer.StampKey(var0.owner(), var0.kind(), var4);
            Long var6 = STAMPS.get(var5);
            if (var6 == null || CombatRules.fresh(var0.stamp(), var6)) {
                Long var7 = STAMPS.get(
                    new XueliCombatRenderer.StampKey(
                        var0.owner(), var0.kind() == 0 ? 1 : (var0.kind() == 3 ? 4 : (var0.kind() == 7 ? 8 : (var0.kind() == 9 ? 10 : -1))), var0.ownerId()
                    )
                );
                if (var7 == null || var0.stamp() > var7) {
                    STAMPS.put(var5, var0.stamp());
                    ACTIVE.removeIf(var2x -> var2x.expired(var2));
                    if (var0.kind() == 1) {
                        ACTIVE.removeIf(var1x -> var1x.packet.owner().equals(var0.owner()) && var1x.packet.kind() == 0);
                    }

                    if (var0.kind() == 4) {
                        ACTIVE.removeIf(var1x -> var1x.packet.owner().equals(var0.owner()) && var1x.packet.kind() == 3);
                    }

                    if (var0.kind() == 8) {
                        ACTIVE.removeIf(var1x -> var1x.packet.owner().equals(var0.owner()) && var1x.packet.kind() == 7);
                    }

                    if (var0.kind() == 10) {
                        ACTIVE.removeIf(var1x -> var1x.packet.owner().equals(var0.owner()) && var1x.packet.kind() == 9);
                    }

                    if (CombatRules.persistent(var0.kind()) || var0.kind() == 6 || var0.kind() == 11) {
                        for (XueliCombatRenderer.Activation var9 : ACTIVE) {
                            if (var9.packet.owner().equals(var0.owner()) && var9.packet.kind() == var0.kind() && var9.targetId() == var4) {
                                var9.packet = var0;
                                var9.received = var2;
                                if (!CombatRules.persistent(var0.kind())) {
                                    var9.born = Math.min(var2, var0.stamp());
                                }

                                return;
                            }
                        }
                    }

                    ACTIVE.add(new XueliCombatRenderer.Activation(var0, var2));

                    while (ACTIVE.size() > 64) {
                        ACTIVE.remove(0);
                    }

                    if (STAMPS.size() > 256) {
                        STAMPS.keySet().removeIf(var0x -> ACTIVE.stream().noneMatch(var1x -> var1x.packet.owner().equals(var0x.owner())));
                    }
                }
            }
        }
    }

    public static CompoundTag cue(EasterBossEntity var0) {
        return filterLegacyCue(var0.getSkillCue(), var0);
    }

    public static CompoundTag filterLegacyCue(CompoundTag var0, EasterBossEntity var1) {
        if (world != null && var1.getVariant() == 10) {
            for (XueliCombatRenderer.Activation var3 : ACTIVE) {
                if (var3.packet.owner().equals(var1.getUUID())
                    && (var3.packet.kind() == 3 || var3.packet.kind() == 4 || var3.packet.kind() == 7 || var3.packet.kind() == 8)
                    && !var3.expired(world.getGameTime())) {
                    return new CompoundTag();
                }
            }
        }

        return var0;
    }

    private static Entity entity(ClientLevel var0, int var1, UUID var2) {
        Entity var3 = var0.getEntity(var1);
        return var3 != null && var3.getUUID().equals(var2) && var3.isAlive() && !var3.isRemoved() ? var3 : null;
    }

    private static Vec3 position(Entity var0, float var1) {
        return new Vec3(
            var0.xOld + (var0.getX() - var0.xOld) * (double)var1,
            var0.yOld + (var0.getY() - var0.yOld) * (double)var1,
            var0.zOld + (var0.getZ() - var0.zOld) * (double)var1
        );
    }

    private static double[] vector(Vec3 var0) {
        return new double[]{var0.x, var0.y, var0.z};
    }

    private static void append(
        List<XueliCombatRenderer.Surface> var0,
        CombatAsset.Effect var1,
        XueliCombatRenderer.Placement var2,
        double var3,
        double var5,
        double[] var7,
        double[] var8,
        boolean var9,
        List<double[]> var10,
        int var11
    ) {
        CombatPose.State[] var12 = var1.bones().stream().map(var2x -> CombatPose.sample(var2x.track(), var3)).toArray(CombatPose.State[]::new);

        for (CombatAsset.Face var14 : var1.faces()) {
            if (var0.size() >= var11) {
                return;
            }

            CombatAsset.Bone var15 = var1.bones().get(var14.bone());
            CombatPose.State var16 = var12[var14.bone()];
            double var17 = var14.alpha() * var16.alpha() * var5;
            if (!(var17 < 0.003)) {
                List<CombatSafety.Corner> var19 = new ArrayList<>();

                for (int var20 = 0; var20 < var14.positions().length; var20++) {
                    double[] var21 = CombatPose.place(var14.positions()[var20], var15.pivot(), var16);
                    Vector3f var22 = new Vector3f(
                            (float)(var21[0] * var2.horizontal()), (float)(var21[1] * var2.vertical()), (float)(var21[2] * var2.longitudinal())
                        )
                        .rotate(var2.rotation());
                    var19.add(
                        new CombatSafety.Corner(
                            new double[]{var2.root().x + (double)var22.x, var2.root().y + (double)var22.y, var2.root().z + (double)var22.z}, var14.uv()[var20]
                        )
                    );
                }

                if (!var10.stream().anyMatch(var1x -> CombatSafety.bodyOverlap(var19, var1x))) {
                    List<CombatSafety.Corner> var23 = CombatSafety.cut(var19, var7, var8, 0.55);
                    if (var23.size() >= 3) {
                        double var24 = var23.stream().mapToDouble(var1x -> CombatSafety.distance(var1x.position(), var7)).average().orElse(0.0);
                        if (var9) {
                            var17 *= CombatSafety.visibility(((CombatSafety.Corner)var23.get(0)).position(), var7, var8, true, false);
                        }

                        if (var17 > 0.003) {
                            var0.add(new XueliCombatRenderer.Surface(var23, var17, var24));
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void paint(RenderLevelStageEvent var0) {
        if (var0.getStage() == Stage.AFTER_PARTICLES) {
            Minecraft var1 = Minecraft.getInstance();
            ClientLevel var2 = var1.level;
            if (var2 != null && !ACTIVE.isEmpty()) {
                if (world != var2) {
                    clear();
                } else {
                    long var3 = var2.getGameTime();
                    ACTIVE.removeIf(var2x -> var2x.expired(var3));
                    if (!ACTIVE.isEmpty()) {
                        float var5 = var1.isPaused() ? 0.0F : var0.getPartialTick().getGameTimeDeltaPartialTick(false);
                        Vec3 var6 = var0.getCamera().getPosition();
                        double[] var7 = vector(var6);
                        double var8 = Math.toRadians((double)var0.getCamera().getYRot());
                        double var10 = Math.toRadians((double)var0.getCamera().getXRot());
                        double[] var12 = new double[]{-Math.sin(var8) * Math.cos(var10), -Math.sin(var10), Math.cos(var8) * Math.cos(var10)};
                        boolean var13 = !var0.getCamera().isDetached();
                        ParticleStatus var14 = var1.options.particles().get();
                        int var15 = var14 == ParticleStatus.MINIMAL ? 500 : (var14 == ParticleStatus.DECREASED ? 1200 : 2200);
                        List<double[]> var16 = new ArrayList<>();

                        for (Entity var18 : var2.entitiesForRendering()) {
                            if (var18 instanceof LivingEntity && var18.distanceToSqr(var6) < 36.0) {
                                AABB var19 = var18.getBoundingBox().inflate(0.06);
                                var16.add(new double[]{var19.minX, var19.minY, var19.minZ, var19.maxX, var19.maxY, var19.maxZ});
                            }
                        }

                        List<XueliCombatRenderer.Surface> var52 = new ArrayList<>();
                        List<XueliCombatRenderer.Activation> var53 = new ArrayList<>(ACTIVE);
                        var53.sort(Comparator.comparingDouble(var1x -> var1x.packet.origin().distanceToSqr(var6)));

                        for (XueliCombatRenderer.Activation var20 : var53) {
                            CombatPacket var21 = var20.packet;
                            int var22 = var21.kind();
                            boolean var23 = var22 == 6 || var22 == 11;
                            CombatPacket.Anchor var24 = var23 ? var21.guards().get(0) : null;
                            Entity var25 = entity(var2, var23 ? var24.entityId() : var21.ownerId(), var23 ? var24.uuid() : var21.owner());
                            if (var25 != null || CombatRules.fixedGround(var22)) {
                                Vec3 var26 = CombatRules.fixedGround(var22) ? var21.origin() : position(var25, var5);
                                if (!(var26.distanceToSqr(var6) > 4096.0)) {
                                    double var27 = (double)((float)(var3 - var20.born) + var5);
                                    double var29 = (double)((float)(var3 - var20.received) + var5);
                                    double var31 = CombatRules.time(
                                        var22,
                                        var21.duration(),
                                        var21.progress(),
                                        var22 == 9 ? (double)Math.max(0.0F, (float)(var3 - var21.stamp()) + var5) : var27
                                    );
                                    double var33 = CombatRules.persistent(var22)
                                        ? Math.min(1.0, ((double)var21.duration() - var29) / 3.0)
                                        : Math.min(1.0, ((double)var21.duration() - var27) / 2.0);
                                    CombatAsset.Effect var35 = ASSETS.get(CombatRules.IDS[var22]);
                                    boolean var36 = var35.anchor().equals("GROUND");
                                    double var37 = var36 ? var21.radius() : (var22 <= 2 ? var21.radius() / 1.3 : 1.0);
                                    double var39 = var36 ? 1.0 : (var25 == null ? 1.0 : Math.max(0.5, (double)var25.getBbHeight() / 1.8));
                                    if (var23 && var25 != null) {
                                        var37 = Math.max(1.0, (double)var25.getBbWidth() / Math.sqrt(2.0) / 0.52);
                                        var39 = var22 == 11 ? (double)var25.getBbHeight() / 1.8 : 1.0;
                                    }

                                    Quaternionf var41 = new Quaternionf().rotateY((float)Math.toRadians((double)var21.yaw()));
                                    append(
                                        var52,
                                        var35,
                                        new XueliCombatRenderer.Placement(var26, var41, var37, var39, var37),
                                        var31,
                                        var33,
                                        var7,
                                        var12,
                                        var13,
                                        var16,
                                        var15
                                    );
                                    if (var22 == 0 && var25 != null) {
                                        for (CombatPacket.Anchor var43 : var21.guards()) {
                                            Entity var44 = entity(var2, var43.entityId(), var43.uuid());
                                            if (var44 != null) {
                                                Vec3 var45 = position(var44, var5).add(0.0, Math.min(0.9, (double)var44.getBbHeight() * 0.58), 0.0);
                                                Vec3 var46 = var26.add(0.0, Math.min(1.05, (double)var25.getBbHeight() * 0.6), 0.0);
                                                Vec3 var47 = var2.clip(new ClipContext(var45, var46, Block.COLLIDER, Fluid.NONE, var44)).getLocation();
                                                Vec3 var48 = var47.subtract(var45);
                                                double var49 = var48.length();
                                                if (!(var49 < 0.2) && !(var49 > 48.0)) {
                                                    Quaternionf var51 = new Quaternionf()
                                                        .rotationTo(
                                                            new Vector3f(0.0F, 0.0F, 1.0F),
                                                            new Vector3f((float)var48.x, (float)var48.y, (float)var48.z).normalize()
                                                        );
                                                    append(
                                                        var52,
                                                        ASSETS.get("guard_thread"),
                                                        new XueliCombatRenderer.Placement(var45, var51, 1.0, 1.0, var49),
                                                        var31,
                                                        var33,
                                                        var7,
                                                        var12,
                                                        var13,
                                                        var16,
                                                        var15
                                                    );
                                                }
                                            }
                                        }
                                    }

                                    if (var52.size() >= var15) {
                                        break;
                                    }
                                }
                            }
                        }

                        var52.sort(Comparator.comparingDouble(XueliCombatRenderer.Surface::depth).reversed());
                        BufferSource var55 = var1.renderBuffers().bufferSource();
                        VertexConsumer var56 = var55.getBuffer(LIGHT);
                        Matrix4f var57 = var0.getPoseStack().last().pose();
                        Matrix3f var58 = var0.getPoseStack().last().normal();
                        int var59 = 0;

                        for (XueliCombatRenderer.Surface var61 : var52) {
                            for (int var62 = 1; var62 < var61.corners().size() - 1 && var59 < var15; var62++) {
                                CombatSafety.Corner var63 = var61.corners().get(0);
                                CombatSafety.Corner var28 = var61.corners().get(var62);
                                CombatSafety.Corner var64 = var61.corners().get(var62 + 1);
                                Vector3f var30 = new Vector3f(
                                        (float)(var28.position()[0] - var63.position()[0]),
                                        (float)(var28.position()[1] - var63.position()[1]),
                                        (float)(var28.position()[2] - var63.position()[2])
                                    )
                                    .cross(
                                        new Vector3f(
                                            (float)(var64.position()[0] - var63.position()[0]),
                                            (float)(var64.position()[1] - var63.position()[1]),
                                            (float)(var64.position()[2] - var63.position()[2])
                                        )
                                    );
                                if (!((double)var30.lengthSquared() < 1.0E-12)) {
                                    var30.normalize();

                                    for (CombatSafety.Corner var32 : List.of(var63, var28, var64, var64)) {
                                        int var66 = (int)Math.round(
                                            var61.alpha() * 255.0 * (var13 ? CombatSafety.visibility(var32.position(), var7, var12, true, false) : 1.0)
                                        );
                                        NativeLegacyVertexNormals.normal(var56.addVertex(
                                                var57,
                                                (float)(var32.position()[0] - var7[0]),
                                                (float)(var32.position()[1] - var7[1]),
                                                (float)(var32.position()[2] - var7[2])
                                            )
                                            .setColor(255, 255, 255, Math.max(0, Math.min(255, var66)))
                                            .setUv((float)var32.uv()[0], (float)var32.uv()[1])
                                            .setOverlay(655360)
                                            .setLight(15728880)
                                            , var58, var30.x, var30.y, var30.z);
                                    }

                                    var59++;
                                }
                            }
                        }

                        var55.endBatch(LIGHT);
                    }
                }
            }
        }
    }

    private static final class Activation {
        private CombatPacket packet;
        private long received;
        private long born;

        private Activation(CombatPacket var1, long var2) {
            this.packet = var1;
            this.received = var2;
            this.born = Math.min(var2, var1.stamp());
        }

        private boolean expired(long var1) {
            return var1 - (CombatRules.persistent(this.packet.kind()) ? this.received : this.born) >= (long)this.packet.duration();
        }

        private int targetId() {
            return (this.packet.kind() == 6 || this.packet.kind() == 11) && !this.packet.guards().isEmpty()
                ? this.packet.guards().get(0).entityId()
                : this.packet.ownerId();
        }
    }

    private static record Placement(Vec3 root, Quaternionf rotation, double horizontal, double vertical, double longitudinal) {
    }

    private static record StampKey(UUID owner, int kind, int target) {
    }

    private static record Surface(List<CombatSafety.Corner> corners, double alpha, double depth) {
    }
}
