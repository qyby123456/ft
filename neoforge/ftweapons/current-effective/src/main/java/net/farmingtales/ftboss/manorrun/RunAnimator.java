package net.farmingtales.ftboss.manorrun;

import java.util.WeakHashMap;
import net.farmingtales.ftboss.body.BodyMotion;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.qizairun.QizaiRunClient;
import net.farmingtales.ftboss.dandanmotion.MotionAccess;
import net.farmingtales.ftboss.doramotion.DoraMotionAccess;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.HumanoidArm;

public final class RunAnimator {
    private static final WeakHashMap<EasterBossEntity, RunState> STATES = new WeakHashMap<>();
    private static final ThreadLocal<RunAnimator.Layer> LAYER = new ThreadLocal<>();

    private RunAnimator() {
    }

    public static boolean owns(EasterBossEntity var0) {
        return var0.getVariant() == 7;
    }

    public static void generic(EasterBossModel var0, EasterBossEntity var1, float var2) {
        if (!QizaiRunClient.owns(var1)) {
            if (!owns(var1)) {
                net.farmingtales.ftboss.client.manorrun.RunAnimator.apply(var0, var1, var2);
            }
        }
    }

    public static void begin(EasterBossEntity var0) {
        if (owns(var0)) {
            LAYER.set(new RunAnimator.Layer(var0));
        } else {
            LAYER.remove();
        }
    }

    public static void reserve(double var0, EasterBossEntity var2) {
        RunAnimator.Layer var3 = LAYER.get();
        if (var3 != null && var3.owner == var2 && Double.isFinite(var0) && var0 > 0.001) {
            var3.reserved = true;
        }
    }

    public static void reserveBody(BodyMotion.Pose var0, EasterBossEntity var1) {
        reserve(var0.activity(), var1);
    }

    public static boolean timed(long var0, int var2, double var3) {
        return var2 > 0 && var2 <= 2400 && var3 >= (double)var0 && var3 < (double)(var0 + (long)var2 + 6L);
    }

    private static boolean cueBusy(CompoundTag var0, double var1, int var3) {
        if (var3 <= 3 && !var0.isEmpty()) {
            if (var0.contains("start") && timed(var0.getLong("start"), var0.getInt("duration"), var1)) {
                return true;
            } else if (var0.contains("dandan_motion_start") && timed(var0.getLong("dandan_motion_start"), var0.getInt("dandan_motion_ticks"), var1)) {
                return true;
            } else if ((!var0.contains("gunTick") || !timed(var0.getLong("gunTick"), 9, var1))
                && (!var0.contains("spearTick") || !timed(var0.getLong("spearTick"), 12, var1))) {
                for (String var5 : var0.getAllKeys()) {
                    if (var0.get(var5) instanceof CompoundTag var6 && cueBusy(var6, var1, var3 + 1)) {
                        return true;
                    }
                }

                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public static boolean busy(EasterBossEntity var0, float var1) {
        double var2 = (double)((float)var0.level().getGameTime() + var1);
        if (!var0.isCastingLike()
            && !((double)var0.getCombatAnimationWeight(var1) > 0.001)
            && !var0.isDoraReloading()
            && !var0.isXuanlanSkyCasting()
            && !cueBusy(var0.getSkillCue(), var2, 0)) {
            if (var0 instanceof MotionAccess var4 && cueBusy(var4.dandanMotionData(), var2, 0)) {
                return true;
            }

            if (var0 instanceof DoraMotionAccess var6 && cueBusy(var6.doraMotionState(), var2, 0)) {
                return true;
            }

            return false;
        } else {
            return true;
        }
    }

    public static void finish(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
        if (!QizaiRunClient.owns(var1)) {
            RunAnimator.Layer var7 = LAYER.get();
            LAYER.remove();
            if (owns(var1) && !var1.isMini() && !var1.isGiant() && var1.isAlive() && !var1.isManorIntroHidden() && !var1.isPassenger() && !var1.isSleeping()) {
                float var8 = (float)RunMotion.clamp((double)(var4 - (float)var1.tickCount), 0.0, 1.0);
                boolean var9 = var7 != null && var7.owner == var1 && var7.reserved || var1.hurtTime > 0 || busy(var1, var8);
                boolean var10 = var1.onGround() && !var1.isInWaterOrBubble() && !var1.isInLava();
                RunState var11 = STATES.computeIfAbsent(var1, var0x -> new RunState());
                RunState.Frame var12 = var11.advance(
                    var1.getVariant(), var1.tickCount, (double)((float)var1.tickCount + var8), var1.getX(), var1.getZ(), var10, var9
                );
                if (!(var12.blend() < 1.0E-4) && !var9) {
                    RunMotion.Pose var13 = RunMotion.sample(
                        var1.getVariant(), var12.phase(), var12.speed(), var1.getMainArm() == HumanoidArm.LEFT, (double)var5, (double)var6
                    );
                    ModelPart[] var14 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};

                    for (int var15 = 0; var15 < 6; var15++) {
                        ModelPart var16 = var14[var15];
                        double var17 = var12.blend();
                        double[] var19 = var13.position()[var15];
                        double[] var20 = var13.rotation()[var15];
                        var16.x = var16.x + (float)((var19[0] - (double)var16.x) * var17);
                        var16.y = var16.y + (float)((var19[1] - (double)var16.y) * var17);
                        var16.z = var16.z + (float)((var19[2] - (double)var16.z) * var17);
                        var16.xRot = var16.xRot + (float)((var20[0] - (double)var16.xRot) * var17);
                        var16.yRot = var16.yRot + (float)((var20[1] - (double)var16.yRot) * var17);
                        var16.zRot = var16.zRot + (float)((var20[2] - (double)var16.zRot) * var17);
                    }

                    var0.hat.copyFrom(var0.head);
                    var0.jacket.copyFrom(var0.body);
                    var0.rightSleeve.copyFrom(var0.rightArm);
                    var0.leftSleeve.copyFrom(var0.leftArm);
                    var0.rightPants.copyFrom(var0.rightLeg);
                    var0.leftPants.copyFrom(var0.leftLeg);
                }
            } else {
                STATES.remove(var1);
            }
        }
    }

    private static final class Layer {
        private final EasterBossEntity owner;
        private boolean reserved;

        private Layer(EasterBossEntity var1) {
            this.owner = var1;
        }
    }
}
