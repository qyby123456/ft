package net.farmingtales.ftboss.client.qizairun;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss",
    value = {Dist.CLIENT}
)
public final class QizaiRunClient {
    private static final Map<UUID, QizaiRunClient.Frame> actors = new LinkedHashMap<>();
    private static ClientLevel scene;

    private QizaiRunClient() {
    }

    public static boolean owns(EasterBossEntity var0) {
        return var0.getVariant() == 11 && !var0.isMini() && !var0.isGiant();
    }

    private static ModelPart[] parts(EasterBossModel var0) {
        return new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};
    }

    private static void read(ModelPart[] var0, double[] var1) {
        for (int var2 = 0; var2 < 6; var2++) {
            ModelPart var3 = var0[var2];
            int var4 = var2 * 6;
            var1[var4] = (double)var3.x;
            var1[var4 + 1] = (double)var3.y;
            var1[var4 + 2] = (double)var3.z;
            var1[var4 + 3] = (double)var3.xRot;
            var1[var4 + 4] = (double)var3.yRot;
            var1[var4 + 5] = (double)var3.zRot;
        }
    }

    public static void prepare(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
        if (owns(var1)) {
            ClientLevel var7 = Minecraft.getInstance().level;
            if (var7 != null && var1.level() == var7 && var1.isAlive() && !var1.isRemoved()) {
                if (scene != var7) {
                    actors.clear();
                    scene = var7;
                }

                float var8 = Float.isFinite(var4) ? (float)QizaiRunPose.clamp((double)(var4 - (float)var1.tickCount), 0.0, 1.0) : 0.0F;
                double var9 = (double)((float)var7.getGameTime() + var8);
                actors.values().removeIf(var2x -> var9 - var2x.lastSeen > 60.0 || var9 < var2x.lastSeen);
                if (!actors.containsKey(var1.getUUID()) && actors.size() >= 96) {
                    actors.remove(actors.keySet().iterator().next());
                }

                QizaiRunClient.Frame var11 = actors.computeIfAbsent(var1.getUUID(), var0x -> new QizaiRunClient.Frame());
                var11.elapsed = QizaiRunPose.clamp(var9 - var11.lastSeen, 0.0, 6.0);
                var11.lastSeen = var9;
                var11.partial = var8;
                var11.captured = false;
                boolean var12 = var1.onGround()
                    && !var1.isInWater()
                    && !var1.isPassenger()
                    && !var1.isFallFlying()
                    && !var1.isNoAi()
                    && var1.getPose() == Pose.STANDING;
                Vec3 var13 = var1.getPosition(var8);
                var11.state = var11.clock
                    .update(
                        var9, var13.x, var13.y, var13.z, var12 && Float.isFinite(var4) && Float.isFinite(var2) && Float.isFinite(var3), (double)var1.yBodyRot
                    );
                if (!(var11.state.blend() <= 0.001)) {
                    read(parts(var0), var11.before);
                    var11.captured = true;
                }
            } else {
                actors.remove(var1.getUUID());
            }
        }
    }

    public static void finish(EasterBossModel var0, EasterBossEntity var1) {
        QizaiRunClient.Frame var2 = actors.get(var1.getUUID());
        if (var2 != null && var2.captured && owns(var1)) {
            var2.captured = false;
            if (var1.isAlive() && !var1.isRemoved() && var1.level() == scene) {
                ModelPart[] var3 = parts(var0);
                read(var3, var2.pose);
                boolean var4 = var1.isCastingLike() || var1.getCombatAnimationWeight(var2.partial) > 0.02F || var1.getActionTicks() > 0;

                for (int var5 = 0; var5 < 24; var5++) {
                    if (Math.abs(var2.pose[var5] - var2.before[var5]) > 0.025) {
                        var4 = true;
                    }
                }

                var2.upperWeight = var4 ? 0.0 : 1.0 - (1.0 - var2.upperWeight) * Math.exp(-var2.elapsed / 3.0);
                QizaiRunPose.apply(
                    var2.pose,
                    var2.state.phase(),
                    var2.state.speed(),
                    var2.state.blend(),
                    var1.getMainArm() == HumanoidArm.LEFT,
                    !var1.getMainHandItem().isEmpty(),
                    !var1.getOffhandItem().isEmpty(),
                    var2.upperWeight,
                    var2.state.forward(),
                    var2.state.side()
                );

                for (int var8 = 0; var8 < 6; var8++) {
                    ModelPart var6 = var3[var8];
                    int var7 = var8 * 6;
                    var6.x = (float)var2.pose[var7];
                    var6.y = (float)var2.pose[var7 + 1];
                    var6.z = (float)var2.pose[var7 + 2];
                    var6.xRot = (float)var2.pose[var7 + 3];
                    var6.yRot = (float)var2.pose[var7 + 4];
                    var6.zRot = (float)var2.pose[var7 + 5];
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

    @SubscribeEvent
    public static void unloaded(Unload var0) {
        if (var0.getLevel() == scene) {
            actors.clear();
            scene = null;
        }
    }

    private static final class Frame {
        final QizaiRunClock clock = new QizaiRunClock();
        final double[] before = new double[36];
        final double[] pose = new double[36];
        QizaiRunClock.State state;
        double lastSeen;
        double elapsed;
        double upperWeight;
        float partial;
        boolean captured;
    }
}
