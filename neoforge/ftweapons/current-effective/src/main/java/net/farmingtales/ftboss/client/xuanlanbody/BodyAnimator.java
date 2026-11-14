package net.farmingtales.ftboss.client.xuanlanbody;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.body.BodyCueAccess;
import net.farmingtales.ftboss.body.BodyMotion;
import net.farmingtales.ftboss.body.BodyRig;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.XuanlanGunPoseMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.joml.Vector3f;

@EventBusSubscriber(
    modid = "ftboss",
    value = {Dist.CLIENT},
    bus = Bus.MOD
)
public final class BodyAnimator {
    private static final ResourceLocation MOTIONS = ResourceLocation.fromNamespaceAndPath("ftboss", "xuanlan_body/motions.json");
    private static BodyMotion.Library library;
    private static boolean attempted;
    private static Object world;
    private static double lastClock = -1.0;
    private static long lastSweep = Long.MIN_VALUE;
    private static final Map<UUID, BodyAnimator.Transition> STATES = new LinkedHashMap<>();

    private BodyAnimator() {
    }

    private static void load(ResourceManager var0) {
        attempted = true;

        try (InputStream var1 = var0.open(MOTIONS)) {
            library = BodyMotion.read(new String(var1.readAllBytes(), StandardCharsets.UTF_8));
            STATES.clear();
        } catch (Exception var6) {
            FTBossMod.LOGGER.error("[FTBoss][XuanlanBody] Animation library rejected; retaining last valid library", (Throwable)var6);
        }
    }

    @SubscribeEvent
    public static void register(RegisterClientReloadListenersEvent var0) {
        var0.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)BodyAnimator::load);
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
        if (var1.getVariant() == 6 && var1.isAlive() && !var1.isManorIntroHidden() && !var1.isPassenger() && !var1.isSleeping()) {
            if (!attempted) {
                load(Minecraft.getInstance().getResourceManager());
            }

            if (library != null) {
                float var7 = Mth.clamp(var4 - (float)var1.tickCount, 0.0F, 1.0F);
                double var8 = (double)((float)var1.level().getGameTime() + var7);
                if (world != var1.level() || var8 + 1.0 < lastClock) {
                    STATES.clear();
                    world = var1.level();
                    lastClock = var8;
                    lastSweep = Long.MIN_VALUE;
                }

                lastClock = Math.max(lastClock, var8);
                long var10 = var1.level().getGameTime();
                if (lastSweep != var10) {
                    STATES.values().removeIf(var2x -> var8 - var2x.touched > 200.0 || var2x.owner == null || var2x.owner.isRemoved());
                    lastSweep = var10;
                }

                CompoundTag var12 = ((BodyCueAccess)var1).ftboss$bodyRead();
                CompoundTag var13 = var12.getCompound("xuanlanBody");
                CompoundTag var14 = var1.getSkillCue();
                String var15 = var13.getString("id");
                int var16 = var13.getInt("windup");
                long var17 = var13.getLong("start");
                double var19 = var8 - (double)var17;
                boolean var21 = var1.getChallengeDifficulty().acceleratedTicks(30) < 30;
                float var22 = var6;
                float var23 = var5;
                if (var15.isEmpty()) {
                    var15 = "ready";
                    var19 = (double)var4;
                    var16 = 0;
                    var17 = 0L;
                }

                if (var15.equals("ready")) {
                    var12.getBoolean("cast");
                    if (false && var12.getInt("duration") > 0) {
                        double var24 = var8 - (double)var12.getLong("start");
                        if (var24 >= 0.0 && var24 < (double)(var12.getInt("duration") + 8)) {
                            var15 = "contact";
                            var19 = var24;
                            var16 = var12.getInt("duration");
                            var17 = var12.getLong("start");
                        }
                    }
                }

                if (!STATES.containsKey(var1.getUUID()) && STATES.size() >= 64) {
                    STATES.remove(STATES.keySet().iterator().next());
                }

                BodyAnimator.Transition var40 = STATES.computeIfAbsent(var1.getUUID(), var0x -> new BodyAnimator.Transition());
                if (var40.owner != var1) {
                    var40 = new BodyAnimator.Transition();
                    var40.owner = var1;
                    STATES.put(var1.getUUID(), var40);
                }

                String var25 = var15 + ":" + var17;
                if (!var25.equals(var40.aimToken)) {
                    var40.aimToken = var25;
                    var40.hasAim = false;
                }

                float var26 = Mth.rotLerp(var7, var1.yBodyRotO, var1.yBodyRot);
                if (var13.getBoolean("lockedAim")) {
                    var40.hasAim = true;
                    var40.pitch = var13.getDouble("aimPitch");
                    var40.worldYaw = var13.getDouble("aimYaw");
                }

                if (var15.equals("beam") || var15.equals("combo")) {
                    double var27 = var8 - (double)var14.getLong("start");
                    if (var14.getBoolean("beam") && var27 >= 0.0 && var27 < (double)var14.getInt("duration")) {
                        double var29 = var14.getDouble("endX") - var14.getDouble("x");
                        double var31 = var14.getDouble("endY") - var14.getDouble("y");
                        double var33 = var14.getDouble("endZ") - var14.getDouble("z");
                        if (var29 * var29 + var31 * var31 + var33 * var33 > 1.0E-6) {
                            var6 = (float)Math.toDegrees((double)XuanlanGunPoseMath.beamPitch(var29, var31, var33));
                            var5 = (float)Math.toDegrees((double)XuanlanGunPoseMath.beamYaw(var29, var33, var26));
                            var40.hasAim = true;
                            var40.pitch = (double)var6;
                            var40.worldYaw = Math.toDegrees(-Math.atan2(var29, var33));
                        }
                    }
                }

                if (var40.hasAim) {
                    var6 = (float)var40.pitch;
                    var5 = Mth.wrapDegrees((float)var40.worldYaw - var26);
                }

                BodyMotion.Clip var41;
                try {
                    var41 = BodyMotion.clip(library, var15);
                } catch (IllegalArgumentException var39) {
                    var41 = BodyMotion.clip(library, "ready");
                    var15 = "ready";
                    var19 = (double)var4;
                    var16 = 0;
                    var17 = 0L;
                }

                BodyMotion.Pose var28 = BodyMotion.sample(var41, var19, var21, var16, (double)var6, (double)var5, var1.getMainArm() == HumanoidArm.LEFT);
                if (!var41.loop && var19 > var28.life()) {
                    var15 = "ready";
                    var17 = 0L;
                    var40.hasAim = false;
                    var28 = BodyMotion.sample(
                        BodyMotion.clip(library, var15), (double)var4, var21, 0, (double)var22, (double)var23, var1.getMainArm() == HumanoidArm.LEFT
                    );
                }

                BodyRig.Bone[] var42 = BodyRig.solve(var28, var2, var3, var4);
                String var30 = var15 + ":" + var17;
                if (!var30.equals(var40.token)) {
                    var40.token = var30;
                    var40.changed = var8;
                    var40.from = var40.last == null ? var42 : var40.last;
                }

                double var43 = BodyMotion.smooth((var8 - var40.changed) / 4.0);
                BodyRig.Bone[] var44 = var43 >= 1.0 ? var42 : BodyRig.blend(var40.from, var42, var43, !var28.air() && (double)var3 <= 0.1);
                var40.last = var44;
                var40.touched = var8;
                ModelPart[] var34 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};

                for (int var35 = 0; var35 < var34.length; var35++) {
                    ModelPart var36 = var34[var35];
                    BodyRig.Bone var37 = var44[var35];
                    Vector3f var38 = BodyModelAngles.zyx(var37.rotation(), new Vector3f());
                    var36.x = var37.position().x;
                    var36.y = var37.position().y;
                    var36.z = var37.position().z;
                    var36.xRot = var38.x;
                    var36.yRot = var38.y;
                    var36.zRot = var38.z;
                }

                var0.hat.copyFrom(var0.head);
                var0.jacket.copyFrom(var0.body);
                var0.rightSleeve.copyFrom(var0.rightArm);
                var0.leftSleeve.copyFrom(var0.leftArm);
                var0.rightPants.copyFrom(var0.rightLeg);
                var0.leftPants.copyFrom(var0.leftLeg);
            }
        } else {
            STATES.remove(var1.getUUID());
        }
    }

    @EventBusSubscriber(
        modid = "ftboss",
        value = {Dist.CLIENT}
    )
    public static final class Cleanup {
        @SubscribeEvent
        public static void logout(LoggingOut var0) {
            BodyAnimator.STATES.clear();
            BodyAnimator.world = null;
            BodyAnimator.lastClock = -1.0;
        }
    }

    private static final class Transition {
        EasterBossEntity owner;
        String token;
        String aimToken;
        double changed;
        double touched;
        double pitch;
        double worldYaw;
        boolean hasAim;
        BodyRig.Bone[] from;
        BodyRig.Bone[] last;
    }
}
