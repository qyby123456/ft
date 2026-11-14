package net.farmingtales.ftboss.doramotion.client;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.doramotion.DoraMotionAccess;
import net.farmingtales.ftboss.doramotion.DoraMotionCurves;
import net.farmingtales.ftboss.doramotion.DoraMotionRig;
import net.farmingtales.ftboss.doramotion.DoraMotionSnapshot;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.Level;

public final class DoraAnimator {
    private static final DoraMotionCurves CURVES = load();
    private static final Map<Integer, DoraAnimator.Frame> FRAMES = new LinkedHashMap<>();
    private static Level world;
    private static long lastTime;
    private static long lastCleanup;

    private DoraAnimator() {
    }

    private static DoraMotionCurves load() {
        try {
            DoraMotionCurves var1;
            try (InputStream var0 = DoraAnimator.class.getClassLoader().getResourceAsStream("assets/ftboss/dora_motion/motions.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Dora motion resource missing");
                }

                var1 = new DoraMotionCurves(new String(var0.readAllBytes(), StandardCharsets.UTF_8));
            }

            return var1;
        } catch (Exception var5) {
            FTBossMod.LOGGER.warn("[DoraMotion] Keeping original poses: {}", var5.toString());
            return null;
        }
    }

    private static double age(CompoundTag var0, String var1, long var2, float var4) {
        return var0.contains(var1) ? (double)((float)(var2 - var0.getLong(var1)) + var4) : 100000.0;
    }

    private static double[][] copy(double[][] var0) {
        double[][] var1 = new double[6][];

        for (int var2 = 0; var2 < 6; var2++) {
            var1[var2] = (double[])var0[var2].clone();
        }

        return var1;
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
        if (CURVES != null
            && var1.getVariant() == 2
            && !var1.isMini()
            && var1.isAlive()
            && !var1.isNoAi()
            && !var1.isPassenger()
            && (Object)var1 instanceof DoraMotionAccess var7) {
            long var8 = var1.level().getGameTime();
            if (world != var1.level() || var8 < lastTime) {
                FRAMES.clear();
                world = var1.level();
                lastCleanup = var8;
            }

            lastTime = var8;
            CompoundTag var10 = var7.doraMotionState();
            if (!var10.isEmpty()) {
                float var11 = Math.max(0.0F, Math.min(1.0F, var4 - (float)var1.tickCount));
                boolean var12 = var1.getMainArm() == HumanoidArm.LEFT;
                int var13 = var12 ? 3 : 2;
                int var14 = var12 ? 2 : 3;
                double[][] var15 = CURVES.sample("ready", (double)(var4 % 40.0F / 40.0F), var12);
                double var16 = age(var10, "shotAt", var8, var11);
                double var18 = age(var10, "fireAt", var8, var11);
                boolean var20 = age(var10, "sampleAt", var8, var11) <= 8.0;
                boolean var21 = var20 && var10.getBoolean("sniper");
                boolean var22 = var20 && var10.getInt("reload") > 0;
                boolean var23 = var20 && var10.getInt("shots") > 0 || var16 >= 0.0 && var16 < 4.0;
                String var24 = "ready";
                double var25 = 0.0;
                int var27 = Math.max(1, var10.getInt("recovery"));
                if (var21 && var18 >= 0.0 && var18 < (double)var27) {
                    var24 = "sniper_fire";
                    var15 = CURVES.sample(var24, var18 / (double)var27, var12);
                    var25 = 1.0 - DoraMotionCurves.smooth((var18 / (double)var27 - 0.42) / 0.58);
                } else if (var21 && var10.contains("sniperAt")) {
                    var24 = "sniper_charge";
                    double var44 = age(var10, "sniperAt", var8, var11) / (double)Math.max(1, var10.getInt("charge"));
                    var15 = CURVES.sample(var24, var44, var12);
                    var25 = DoraMotionCurves.smooth(var44 / 0.28);
                } else if (var22) {
                    var24 = "reload";
                    double var28 = DoraMotionSnapshot.reloadProgress(var10, var8, var11);
                    var15 = CURVES.sample(var24, var28, var12);
                    var25 = DoraMotionCurves.smooth((var28 - 0.63) / 0.25) * (1.0 - DoraMotionCurves.smooth((var28 - 0.86) / 0.14));
                } else if (var23) {
                    var24 = "suppress";
                    var15 = CURVES.sample(var24, 0.5, var12);
                    var25 = 1.0;
                    if (var16 >= 0.0 && var16 < 4.0) {
                        double var43 = Math.exp(-var16 * 1.2);
                        var15[var13][0] = var15[var13][0] - 5.0 * var43;
                        var15[1][0] = var15[1][0] - 1.4 * var43;
                    }
                }

                CompoundTag var45 = var10.getCompound("spell");
                String var29 = var45.getString("id");
                if (var29.equals("arcane_fusillade") || var29.equals("breach_shell") || var29.equals("prism_battery")) {
                    int var30 = var45.getInt("phase");
                    int var31 = Math.max(1, var45.getInt("duration"));
                    double var32 = age(var45, "start", var8, var11);
                    double var34 = age(var45, "stop", var8, var11);
                    double var36 = var30 == 0 ? var32 / (double)var31 : Math.max(0.0, (double)(var45.getLong("stop") - var45.getLong("start")) / (double)var31);
                    double var38 = var30 == 0 && var32 >= 0.0 && var32 < (double)(var31 + 4)
                        ? 1.0
                        : (var30 > 0 && var34 >= 0.0 ? 1.0 - DoraMotionCurves.smooth(var34 / (double)(var30 == 2 ? 4 : 10)) : 0.0);
                    if (var38 > 0.0) {
                        double[][] var40 = CURVES.sample(var29, var36, var12);
                        if (var30 == 1) {
                            double var41 = Math.exp(-Math.max(0.0, var34) * 0.65) * (double)(var29.equals("breach_shell") ? 13 : 5);
                            var40[var13][0] = var40[var13][0] - var41;
                            var40[1][0] = var40[1][0] - var41 * 0.25;
                        }

                        if (var24.equals("ready")) {
                            DoraMotionCurves.blend(var15, var40, var38);
                            var24 = var29 + (var30 == 1 ? ":release" : (var30 == 2 ? ":cancel" : ""));
                            var25 = var38 * DoraMotionCurves.smooth(var36 / 0.65);
                        } else {
                            for (int var56 = 0; var56 < 3; var56++) {
                                var15[1][var56] = var15[1][var56] + (var40[1][var56] - var15[1][var56]) * var38 * 0.15;
                            }
                        }
                    }
                }

                age(var10, "spearAt", var8, var11);
                double var46 = 100000.0;
                double var47 = (double)Math.min(1.0F, Math.max(0.0F, var3)) * (var22 ? 1.0 : 0.35);
                var15[4][0] = var15[4][0] + Math.cos((double)var2 * 0.6662) * 45.0 * var47;
                var15[5][0] = var15[5][0] - Math.cos((double)var2 * 0.6662) * 45.0 * var47;
                var15[0][0] = var15[0][0] + (double)Math.max(-25.0F, Math.min(25.0F, var6));
                var15[0][1] = var15[0][1] + (double)Math.max(-25.0F, Math.min(25.0F, var5));
                var15[var13][0] = var15[var13][0] + (double)Math.max(-15.0F, Math.min(15.0F, var6)) * var25;
                var15[var13][1] = var15[var13][1] + (double)Math.max(-15.0F, Math.min(15.0F, var5)) * var25;
                if (var8 - lastCleanup >= 20L) {
                    FRAMES.values().removeIf(var2x -> var8 - var2x.lastSeen > 200L);
                    lastCleanup = var8;
                }

                DoraAnimator.Frame var48 = FRAMES.get(var1.getId());
                if (var48 == null || !var1.getUUID().equals(var48.owner)) {
                    var48 = new DoraAnimator.Frame();
                    var48.owner = var1.getUUID();
                    FRAMES.put(var1.getId(), var48);
                }

                var48.lastSeen = var8;
                boolean var35 = var46 >= 0.0 && var46 < 4.0;
                if (var24.equals("ready") && !var35 && var48.identity.equals("ready") && var8 - var48.changedAt >= 3L) {
                    var48.current = null;
                    var48.prior = null;
                } else {
                    if (!var24.equals(var48.identity)) {
                        var48.prior = var48.current == null ? copy(var15) : copy(var48.current);
                        var48.identity = var24;
                        var48.changedAt = var8;
                    }

                    if (var48.prior != null && !var24.equals("sniper_fire") && !var24.endsWith(":release")) {
                        double[][] var49 = copy(var48.prior);
                        DoraMotionCurves.blend(var49, var15, DoraMotionCurves.smooth((double)(((float)(var8 - var48.changedAt) + var11) / 3.0F)));
                        var15 = var49;
                    }

                    var48.current = copy(var15);
                    if (var35) {
                        double[][] var50 = CURVES.sample("spear", var46 / 4.0, var12);
                        var15[var14] = var50[var14];

                        for (int var37 = 0; var37 < 3; var37++) {
                            var15[1][var37] = var15[1][var37] + (var50[1][var37] - var15[1][var37]) * 0.3;
                        }

                        double var52 = 1.0 - DoraMotionCurves.smooth(var46 / 4.0);
                        var15[var14][0] = var15[var14][0] + (double)Math.max(-20.0F, Math.min(20.0F, var6)) * var52;
                        var15[var14][1] = var15[var14][1] + (double)Math.max(-20.0F, Math.min(20.0F, var5)) * var52;
                    }

                    if (FRAMES.size() > 64) {
                        FRAMES.remove(FRAMES.keySet().iterator().next());
                    }

                    DoraMotionRig.Bone[] var51 = DoraMotionRig.solve(var15, var1.onGround() && !var1.isPassenger() && (double)var3 < 0.02, var13, var25);
                    ModelPart[] var53 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};

                    for (int var54 = 0; var54 < 6; var54++) {
                        ModelPart var39 = var53[var54];
                        float[] var55 = var51[var54].position();
                        float[] var57 = var51[var54].rotation();
                        var39.x = var55[0];
                        var39.y = var55[1];
                        var39.z = var55[2];
                        var39.xRot = var57[0];
                        var39.yRot = var57[1];
                        var39.zRot = var57[2];
                    }

                    var0.hat.copyFrom(var0.head);
                    var0.jacket.copyFrom(var0.body);
                    var0.rightSleeve.copyFrom(var0.rightArm);
                    var0.leftSleeve.copyFrom(var0.leftArm);
                    var0.rightPants.copyFrom(var0.rightLeg);
                    var0.leftPants.copyFrom(var0.leftLeg);
                }
            }
        }
    }

    private static final class Frame {
        UUID owner;
        long lastSeen;
        String identity = "";
        long changedAt;
        double[][] prior;
        double[][] current;
    }
}
