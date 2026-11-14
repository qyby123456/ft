package net.farmingtales.ftboss.dandanrun.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.dandaniron.IronPacket;
import net.farmingtales.ftboss.dandanrun.RunCycle;
import net.farmingtales.ftboss.dandanrun.RunLease;
import net.farmingtales.ftboss.dandanrun.RunState;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss",
    value = {Dist.CLIENT}
)
public final class RunActor {
    private static final Map<UUID, RunActor.Entry> STATES = new LinkedHashMap<>();
    private static ClientLevel world;

    private RunActor() {
    }

    private static boolean eligible(EasterBossEntity var0) {
        return var0.getVariant() == 1
            && EasterBossEntity.isManorVariant(1)
            && !var0.isMini()
            && !var0.isGiant()
            && var0.isAlive()
            && !var0.isManorIntroHidden();
    }

    private static RunActor.Entry entry(EasterBossEntity var0) {
        RunActor.Entry var1 = STATES.get(var0.getUUID());
        if (var1 != null && var1.ownerId != var0.getId()) {
            STATES.remove(var0.getUUID());
        }

        if (STATES.size() >= 64 && !STATES.containsKey(var0.getUUID())) {
            STATES.remove(STATES.keySet().iterator().next());
        }

        return STATES.computeIfAbsent(var0.getUUID(), var1x -> new RunActor.Entry(var0.getId(), world.getGameTime()));
    }

    private static boolean level() {
        ClientLevel var0 = Minecraft.getInstance().level;
        if (var0 != world) {
            STATES.clear();
            world = var0;
        }

        return var0 != null;
    }

    public static void notice(IronPacket var0) {
        if (level() && var0.valid() && var0.visual().dimension().equals(world.dimension().location())) {
            if (world.getEntity(var0.visual().ownerId()) instanceof EasterBossEntity var2 && eligible(var2) && var2.getUUID().equals(var0.visual().owner())) {
                RunActor.Entry var3 = entry(var2);
                if (!var3.lease
                    .receive(var0.visual().phase().name(), var0.cast(), var0.event(), var0.start(), var0.ticks(), var0.cancelled(), world.getGameTime())) {
                    return;
                }

                var3.run.reset();
                var3.touched = world.getGameTime();
                return;
            }
        }
    }

    private static boolean allowed(EasterBossEntity var0, RunActor.Entry var1, long var2) {
        if (eligible(var0)
            && var0.onGround()
            && !var0.isInWater()
            && !var0.isInWaterOrBubble()
            && !var0.isShiftKeyDown()
            && !var0.isPassenger()
            && !var0.isFallFlying()) {
            if (!var0.isCastingLike() && var0.getActionTicks() <= 0 && !((double)var0.getCombatAnimationWeight(0.0F) > 0.02) && !var1.lease.busy(var2)) {
                CompoundTag var5 = ((net.farmingtales.ftboss.dandanmotion.MotionAccess)(Object)var0).dandanMotionData();
                return !RunState.activeTag(var2, var5.getLong("dandan_motion_start"), var5.getInt("dandan_motion_ticks"), var5.getInt("dandan_motion_schema"));
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post var0) {
        if (level()) {
            long var1 = world.getGameTime();
            STATES.entrySet()
                .removeIf(
                    var2 -> {
                        if (world.getEntity(var2.getValue().ownerId) instanceof EasterBossEntity var4x
                            && eligible(var4x)
                            && var4x.getUUID().equals(var2.getKey())
                            && var1 - var2.getValue().touched <= 40L) {
                            return false;
                        }

                        return true;
                    }
                );

            for (RunActor.Entry var4 : STATES.values()) {
                EasterBossEntity var5 = (EasterBossEntity)world.getEntity(var4.ownerId);
                var4.run.update(var1, var5.getX(), var5.getZ(), allowed(var5, var4, var1));
            }
        }
    }

    public static void animate(PlayerModel<?> var0, EasterBossEntity var1, float var2) {
        if (level() && eligible(var1) && var1.level() == world) {
            RunActor.Entry var3 = entry(var1);
            long var4 = world.getGameTime();
            var3.touched = var4;
            if (!allowed(var1, var3, var4)) {
                var3.run.reset();
            } else {
                if (var3.run.tick == Long.MIN_VALUE) {
                    var3.run.update(var4, var1.getX(), var1.getZ(), true);
                }

                double var6 = (double)Math.max(0.0F, Math.min(1.0F, var2 - (float)var1.tickCount));
                double var8 = var3.run.weightAt(var6);
                if (!(var8 < 0.001)) {
                    double[][] var10 = RunCycle.sample(var3.run.phaseAt(var6), Math.max(0.0, Math.min(1.0, (var3.run.speed - 0.18) / 0.22)));
                    ModelPart[] var11 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};
                    float var12 = var0.head.xRot;
                    float var13 = var0.head.yRot;

                    for (int var14 = 0; var14 < 6; var14++) {
                        ModelPart var15 = var11[var14];
                        var15.x = (float)((double)var15.x * (1.0 - var8) + var10[var14][0] * var8);
                        var15.y = (float)((double)var15.y * (1.0 - var8) + var10[var14][1] * var8);
                        var15.z = (float)((double)var15.z * (1.0 - var8) + var10[var14][2] * var8);
                        var15.xRot = (float)((double)var15.xRot * (1.0 - var8) + Math.toRadians(var10[var14][3]) * var8);
                        var15.yRot = (float)((double)var15.yRot * (1.0 - var8) + Math.toRadians(var10[var14][4]) * var8);
                        var15.zRot = (float)((double)var15.zRot * (1.0 - var8) + Math.toRadians(var10[var14][5]) * var8);
                        if (var14 >= 4) {
                            var15.y = Math.min(var15.y, (float)RunCycle.footPivot(Math.toDegrees((double)var15.xRot)));
                        }
                    }

                    var0.head.xRot = (float)((double)var0.head.xRot + (double)var12 * var8);
                    var0.head.yRot = (float)((double)var0.head.yRot + (double)var13 * var8);
                    var0.hat.copyFrom(var0.head);
                    var0.jacket.copyFrom(var0.body);
                    var0.rightSleeve.copyFrom(var0.rightArm);
                    var0.leftSleeve.copyFrom(var0.leftArm);
                    var0.leftPants.copyFrom(var0.rightLeg);
                    var0.rightPants.copyFrom(var0.leftLeg);
                }
            }
        }
    }

    private static final class Entry {
        private final RunState run = new RunState();
        private final RunLease lease = new RunLease();
        private final int ownerId;
        private long touched;

        private Entry(int var1, long var2) {
            this.ownerId = var1;
            this.touched = var2;
        }
    }
}
