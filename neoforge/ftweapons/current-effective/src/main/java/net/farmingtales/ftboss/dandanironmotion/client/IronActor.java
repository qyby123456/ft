package net.farmingtales.ftboss.dandanironmotion.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.dandaniron.IronPacket;
import net.farmingtales.ftboss.dandanironmotion.IronActorRules;
import net.farmingtales.ftboss.dandanironmotion.IronPoses;
import net.farmingtales.ftboss.dandanmotion.MotionCurves;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
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
public final class IronActor {
    private static final Map<UUID, IronActor.Active> ACTIVE = new LinkedHashMap<>();
    private static ClientLevel world;

    private IronActor() {
    }

    public static void receive(IronPacket var0) {
        ClientLevel var1 = Minecraft.getInstance().level;
        if (var1 != null && var0.valid() && var0.visual().dimension().equals(var1.dimension().location())) {
            if (var1 != world) {
                ACTIVE.clear();
                world = var1;
            }

            if (var1.getEntity(var0.visual().ownerId()) instanceof EasterBossEntity var3 && eligible(var3) && var3.getUUID().equals(var0.visual().owner())) {
                IronActor.Active var4 = ACTIVE.get(var0.visual().owner());
                if (var4 != null && var4.packet().cast() > var0.cast()) {
                    return;
                }

                if (var0.cancelled()) {
                    if (var4 != null && !var4.recovering() && var4.packet().cast() == var0.cast() && var4.packet().visual().phase() == Phase.CHARGE) {
                        double var5 = MotionCurves.clamp((double)(var1.getGameTime() - var4.packet().start()) / (double)var4.packet().ticks());
                        ACTIVE.put(var0.visual().owner(), new IronActor.Active(var4.packet(), true, var1.getGameTime(), var5));
                    }

                    return;
                }

                if (IronActorRules.autonomous(var0.visual().phase().name())) {
                    return;
                }

                if (var4 != null
                    && IronActorRules.stale(
                        var4.packet().visual().phase().name(),
                        var4.packet().cast(),
                        var4.packet().event(),
                        var0.visual().phase().name(),
                        var0.cast(),
                        var0.event()
                    )) {
                    return;
                }

                if (ACTIVE.size() >= 64 && !ACTIVE.containsKey(var0.visual().owner())) {
                    ACTIVE.remove(ACTIVE.keySet().iterator().next());
                }

                ACTIVE.put(var0.visual().owner(), new IronActor.Active(var0, false, 0L, 0.0));
                return;
            }
        }
    }

    private static boolean eligible(EasterBossEntity var0) {
        return var0.getVariant() == 1 && !var0.isMini() && !var0.isGiant() && var0.isAlive();
    }

    private static boolean expired(IronActor.Active var0, long var1) {
        if (world.getEntity(var0.packet().visual().ownerId()) instanceof EasterBossEntity var4
            && eligible(var4)
            && var4.getUUID().equals(var0.packet().visual().owner())) {
            return IronActorRules.expires(
                var0.recovering(),
                var1,
                var0.recoveryStart(),
                var0.packet().visual().phase() == Phase.CHARGE,
                var0.packet().start(),
                var0.packet().ticks(),
                var0.packet().event()
            );
        }

        return true;
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post var0) {
        ClientLevel var1 = Minecraft.getInstance().level;
        if (var1 != null && var1 == world) {
            ACTIVE.values().removeIf(var1x -> expired(var1x, var1.getGameTime()));
        } else {
            ACTIVE.clear();
            world = var1;
        }
    }

    public static void animate(PlayerModel<?> var0, EasterBossEntity var1, float var2) {
        if (eligible(var1) && world != null && var1.level() == world) {
            IronActor.Active var3 = ACTIVE.get(var1.getUUID());
            if (var3 != null) {
                if (expired(var3, world.getGameTime())) {
                    ACTIVE.remove(var1.getUUID());
                } else {
                    float var4 = (float)MotionCurves.clamp((double)(var2 - (float)var1.tickCount));
                    double var5 = (double)((float)world.getGameTime() + var4);
                    CompoundTag var8 = ((net.farmingtales.ftboss.dandanmotion.MotionAccess)(Object)var1).dandanMotionData();
                    if (!IronActorRules.nativeHasPriority(var8.getLong("dandan_motion_start"), var8.getInt("dandan_motion_ticks"), var3.packet().event(), var5)
                        )
                     {
                        if (!var3.recovering() && var3.packet().visual().phase() != Phase.RELEASE || !var1.isCastingLike()) {
                            String var9 = var3.packet().visual().spell().id + (var3.packet().visual().phase() == Phase.CHARGE ? "_charge" : "_release");
                            double var10 = MotionCurves.clamp((var5 - (double)var3.packet().start()) / (double)var3.packet().ticks());
                            double[][] var12 = var3.recovering()
                                ? IronPoses.recover(var9, var3.recoveryPose(), (var5 - (double)var3.recoveryStart()) / 7.0)
                                : IronPoses.sample(var9, var10);
                            double[][] var13 = MotionCurves.resolve(var12);
                            double var14 = var3.recovering()
                                ? 1.0
                                : (
                                    var3.packet().visual().phase() == Phase.CHARGE
                                        ? 1.0
                                        : MotionCurves.clamp(((double)(var3.packet().start() + (long)var3.packet().ticks()) - var5) / 3.0)
                                );
                            ModelPart[] var16 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm};

                            for (int var17 = 0; var17 < var16.length; var17++) {
                                ModelPart var18 = var16[var17];
                                var18.x = (float)((double)var18.x * (1.0 - var14) + var13[var17][0] * var14);
                                var18.y = (float)((double)var18.y * (1.0 - var14) + var13[var17][1] * var14);
                                var18.z = (float)((double)var18.z * (1.0 - var14) + var13[var17][2] * var14);
                                var18.xRot = (float)((double)var18.xRot * (1.0 - var14) + Math.toRadians(var13[var17][3]) * var14);
                                var18.yRot = (float)((double)var18.yRot * (1.0 - var14) + Math.toRadians(var13[var17][4]) * var14);
                                var18.zRot = (float)((double)var18.zRot * (1.0 - var14) + Math.toRadians(var13[var17][5]) * var14);
                            }

                            var0.hat.copyFrom(var0.head);
                            var0.jacket.copyFrom(var0.body);
                            var0.leftSleeve.copyFrom(var0.leftArm);
                            var0.rightSleeve.copyFrom(var0.rightArm);
                        }
                    }
                }
            }
        }
    }

    private static record Active(IronPacket packet, boolean recovering, long recoveryStart, double recoveryPose) {
    }
}
