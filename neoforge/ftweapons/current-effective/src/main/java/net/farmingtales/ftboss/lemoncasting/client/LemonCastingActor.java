package net.farmingtales.ftboss.lemoncasting.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.lemoncasting.CastingClock;
import net.farmingtales.ftboss.lemoncasting.CastingPacket;
import net.farmingtales.ftboss.lemonmotion.LemonMotionAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.HumanoidArm;

public final class LemonCastingActor {
    private static final Map<UUID, LemonCastingActor.State> ACTIVE = new HashMap<>();
    private static final ThreadLocal<LemonCastingActor.Base> BASE = new ThreadLocal<>();
    private static Object world;
    private static long cleanedTick = Long.MIN_VALUE;
    private static final double RAD = Math.PI / 180.0;

    private LemonCastingActor() {
    }

    private static ModelPart[] parts(PlayerModel<?> var0) {
        return new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};
    }

    private static void resetWorld() {
        ClientLevel var0 = Minecraft.getInstance().level;
        if (var0 != world) {
            ACTIVE.clear();
            BASE.remove();
            world = var0;
            cleanedTick = Long.MIN_VALUE;
        }

        if (var0 != null && cleanedTick != var0.getGameTime()) {
            cleanedTick = var0.getGameTime();
            ACTIVE.entrySet().removeIf(var0x -> CastingClock.expiredHistory(var0x.getValue().cue(), (double)cleanedTick));
        }
    }

    public static void receive(CastingPacket var0) {
        resetWorld();
        ClientLevel var1 = Minecraft.getInstance().level;
        if (var1 != null && var0.valid() && var0.dimension().equals(var1.dimension().location()) && !CastingClock.stale(var0.cue(), (double)var1.getGameTime())
            )
         {
            LemonCastingActor.State var2 = ACTIVE.get(var0.owner());
            if (CastingClock.supersedes(var2 == null ? null : var2.cue(), var0.cue())) {
                if (ACTIVE.size() < 128 || ACTIVE.containsKey(var0.owner())) {
                    ACTIVE.put(var0.owner(), new LemonCastingActor.State(var0.cue()));
                }
            }
        }
    }

    public static void capture(PlayerModel<?> var0, EasterBossEntity var1) {
        BASE.remove();
        if (var1.getVariant() == 4 && !var1.isMini()) {
            resetWorld();
            if (ACTIVE.containsKey(var1.getUUID())) {
                float[][] var2 = new float[6][];
                ModelPart[] var3 = parts(var0);

                for (int var4 = 0; var4 < 6; var4++) {
                    ModelPart var5 = var3[var4];
                    var2[var4] = new float[]{var5.x, var5.y, var5.z, var5.xRot, var5.yRot, var5.zRot};
                }

                BASE.set(new LemonCastingActor.Base(var0, var1, var2));
            }
        }
    }

    private static boolean ritualPriority(EasterBossEntity var0) {
        if (var0.isCastingLike() && var0 instanceof LemonMotionAccess var1) {
            String var2 = var1.lemonMotionData().getString("lemon_motion_v1");
            return "absorb".equals(var2) || "summon".equals(var2);
        } else {
            return false;
        }
    }

    public static void animate(
        PlayerModel<?> var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6, ModelPart var7, ModelPart var8, ModelPart var9
    ) {
        LemonCastingActor.Base var10 = BASE.get();
        BASE.remove();
        resetWorld();
        if (var10 != null && var10.model() == var0 && var10.owner() == var1 && var1.isAlive() && !ritualPriority(var1)) {
            LemonCastingActor.State var11 = ACTIVE.get(var1.getUUID());
            ClientLevel var12 = Minecraft.getInstance().level;
            if (var11 != null && var1.level() == var12) {
                CastingClock.Cue var13 = var11.cue();
                double var14 = (double)var12.getGameTime() + LemonCastingCurves.clamp((double)(var4 - (float)var1.tickCount), 0.0, 1.0);
                double var16 = CastingClock.age(var13, var14);
                if (!(var16 < 0.0) && !CastingClock.stale(var13, var14)) {
                    int var18 = var1.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
                    int var19 = var13.phase() == 0 ? var13.duration() : CastingClock.recovery(var13.duration());
                    CastingPose.Frame var20 = CastingPose.evaluate(var13, var14, var18, (double)var2, (double)var3, (double)var5, (double)var6);
                    double[][] var21 = var20.delta();
                    double var22 = var20.influence();
                    ModelPart[] var24 = parts(var0);

                    for (int var28 : new int[]{0, 1, 2, 3}) {
                        ModelPart var29 = var24[var28];
                        float[] var30 = var10.poses()[var28];
                        var29.x = (float)((double)var30[0] * (1.0 - var22) + LemonCastingCurves.PIVOTS[var28][0] * var22 + var21[var28][0]);
                        var29.y = (float)((double)var30[1] * (1.0 - var22) + LemonCastingCurves.PIVOTS[var28][1] * var22 + var21[var28][1]);
                        var29.z = (float)((double)var30[2] * (1.0 - var22) + LemonCastingCurves.PIVOTS[var28][2] * var22 + var21[var28][2]);
                        var29.xRot = (float)((double)var30[3] * (1.0 - var22) + var21[var28][3] * (Math.PI / 180.0));
                        var29.yRot = (float)((double)var30[4] * (1.0 - var22) + var21[var28][4] * (Math.PI / 180.0));
                        var29.zRot = (float)((double)var30[5] * (1.0 - var22) + var21[var28][5] * (Math.PI / 180.0));
                    }

                    for (int var35 : new int[]{4, 5}) {
                        ModelPart var36 = var24[var35];
                        float[] var37 = var10.poses()[var35];
                        var36.x = var37[0];
                        var36.y = var37[1];
                        var36.z = var37[2];
                        var36.xRot = var37[3];
                        var36.yRot = var37[4];
                        var36.zRot = var37[5];
                    }

                    var7.resetPose();
                    var8.resetPose();
                    var9.resetPose();
                    double var32 = Math.sin(Math.PI * LemonCastingCurves.clamp(var16 / (double)var19, 0.0, 1.0)) * var22;
                    var7.zRot += (float)(var32 * 0.022 * (double)var18);
                    var8.xRot += (float)(var32 * 0.08);
                    var9.xRot += (float)(var32 * 0.028);
                    var0.hat.copyFrom(var0.head);
                    var0.jacket.copyFrom(var0.body);
                    var0.leftSleeve.copyFrom(var0.leftArm);
                    var0.rightSleeve.copyFrom(var0.rightArm);
                    var0.leftPants.copyFrom(var0.leftLeg);
                    var0.rightPants.copyFrom(var0.rightLeg);
                }
            }
        }
    }

    private static record Base(PlayerModel<?> model, EasterBossEntity owner, float[][] poses) {
    }

    private static record State(CastingClock.Cue cue) {
    }
}
