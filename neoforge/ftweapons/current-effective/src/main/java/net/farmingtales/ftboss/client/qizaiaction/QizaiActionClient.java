package net.farmingtales.ftboss.client.qizaiaction;

import java.io.BufferedReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.qizairun.QizaiRunClient;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.minecraft.core.registries.BuiltInRegistries;
import org.apache.logging.log4j.LogManager;

public final class QizaiActionClient extends SimplePreparableReloadListener<QizaiActionRig> {
    private static volatile QizaiActionRig loaded;
    private static final Map<UUID, QizaiActionClock> actors = new LinkedHashMap<>();
    private static ClientLevel scene;

    public static boolean attached(EasterBossEntity var0) {
        ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getMainHandItem().getItem());
        return loaded != null && QizaiActionClock.eligible(var0.getVariant(), var0.isAlive(), var0.isRemoved(), var1 == null ? "" : var1.toString());
    }

    protected QizaiActionRig prepare(ResourceManager var1, ProfilerFiller var2) {
        try {
            QizaiActionRig var4;
            try (BufferedReader var3 = var1.openAsReader(ResourceLocation.fromNamespaceAndPath("ftboss", "qizai_actions/caster_actions.json"))) {
                var4 = QizaiActionRig.read(var3);
            }

            return var4;
        } catch (Exception var8) {
            LogManager.getLogger("QizaiActions").error("Action data unavailable; preserving original humanoid pose", (Throwable)var8);
            return null;
        }
    }

    protected void apply(QizaiActionRig var1, ResourceManager var2, ProfilerFiller var3) {
        loaded = var1;
        actors.clear();
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
        QizaiRunClient.prepare(var0, var1, var2, var3, var4, var5, var6);
        QizaiActionRig var7 = loaded;
        if (var7 != null && var1.getVariant() == 11 && var1.isAlive() && !var1.isRemoved()) {
            ItemStack var8 = var1.getMainHandItem();
            ResourceLocation var9 = BuiltInRegistries.ITEM.getKey(var8.getItem());
            if (!QizaiActionClock.eligible(var1.getVariant(), var1.isAlive(), var1.isRemoved(), var9 == null ? "" : var9.toString())) {
                actors.remove(var1.getUUID());
                QizaiRunClient.finish(var0, var1);
            } else if (var1.level() instanceof ClientLevel var10) {
                if (scene != var10) {
                    actors.clear();
                    scene = var10;
                }

                double var26 = (double)Math.max(0.0F, Math.min(1.0F, var4 - (float)var1.tickCount));
                double var13 = (double)var10.getGameTime() + var26;
                CompoundTag var15 = var1.getSkillCue();
                int var16 = var15.getInt("qizai");
                QizaiActionClock.Selection var17 = QizaiActionClock.select(
                    new QizaiActionClock.Cue(
                        var16,
                        var15.getLong("start"),
                        var15.getInt("duration"),
                        var15.getBoolean("released"),
                        var15.getInt("volley"),
                        var15.getLong("windupStart"),
                        var15.getInt("windupDuration"),
                        var15.getInt("elapsed"),
                        var15.getInt("round"),
                        var15.getInt("trackTicks"),
                        var15.getInt("lockTicks"),
                        var15.getInt("fireGap"),
                        var15.getInt("activeTicks"),
                        var15.getInt("warningTicks"),
                        var15.getInt("roundStart")
                    ),
                    var13
                );
                QizaiActionClock var18 = actors.get(var1.getUUID());
                boolean var19 = var16 >= 0 && var16 <= 10;
                if (var17 == null && var19 && !var1.isCastingLike() && var1.getCombatAnimationWeight((float)var26) <= 0.01F) {
                    CompoundTag var20 = var1.getQizaiArsenalCue();
                    if (var20.contains("push", 10)) {
                        CompoundTag var21 = var20.getCompound("push");
                        var17 = QizaiActionClock.auxiliary(102, var21.getLong("start"), var21.getBoolean("released"), var13);
                    }

                    if (var17 == null) {
                        ListTag var28 = var20.getList("salvos", 10);

                        for (int var22 = Math.min(4, var28.size()) - 1; var22 >= 0 && var17 == null; var22--) {
                            var17 = QizaiActionClock.auxiliary(101, var28.getCompound(var22).getLong("start"), false, var13);
                        }
                    }
                }

                if (var17 != null
                    || var19 && (!var1.isCastingLike() && !(var1.getCombatAnimationWeight((float)var26) > 0.01F) || var18 != null && var18.canRecover(var13))) {
                    if (var17 == null && var3 > 0.025F) {
                        actors.remove(var1.getUUID());
                        QizaiRunClient.finish(var0, var1);
                    } else {
                        actors.values().removeIf(var2x -> var2x.expired(var13));
                        if (!actors.containsKey(var1.getUUID()) && actors.size() >= 128) {
                            actors.remove(actors.keySet().iterator().next());
                        }

                        QizaiActionClock var27 = actors.computeIfAbsent(var1.getUUID(), var0x -> new QizaiActionClock());
                        double[] var29 = var27.sample(var7, var17, var13);
                        QizaiActionRig.Pose var30 = QizaiActionRig.pose(
                            var29, var1.getMainArm() == HumanoidArm.LEFT, Math.toRadians((double)var5), Math.toRadians((double)var6)
                        );
                        ModelPart[] var23 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};

                        for (int var24 = 0; var24 < var23.length; var24++) {
                            if (var24 < 4 || !(var3 > 0.025F)) {
                                ModelPart var25 = var23[var24];
                                var25.x = (float)var30.position()[var24][0];
                                var25.y = (float)var30.position()[var24][1];
                                var25.z = (float)var30.position()[var24][2];
                                var25.xRot = (float)var30.rotation()[var24][0];
                                var25.yRot = (float)var30.rotation()[var24][1];
                                var25.zRot = (float)var30.rotation()[var24][2];
                            }
                        }

                        var0.hat.copyFrom(var0.head);
                        var0.jacket.copyFrom(var0.body);
                        var0.rightSleeve.copyFrom(var0.rightArm);
                        var0.leftSleeve.copyFrom(var0.leftArm);
                        var0.rightPants.copyFrom(var0.rightLeg);
                        var0.leftPants.copyFrom(var0.leftLeg);
                        QizaiRunClient.finish(var0, var1);
                    }
                } else {
                    actors.remove(var1.getUUID());
                    QizaiRunClient.finish(var0, var1);
                }
            } else {
                QizaiRunClient.finish(var0, var1);
            }
        } else {
            QizaiRunClient.finish(var0, var1);
        }
    }

    @EventBusSubscriber(
        modid = "ftboss",
        value = {Dist.CLIENT}
    )
    public static final class Events {
        private Events() {
        }

        @SubscribeEvent
        public static void unloaded(Unload var0) {
            if (var0.getLevel() == QizaiActionClient.scene) {
                QizaiActionClient.actors.clear();
                QizaiActionClient.scene = null;
            }
        }
    }

    @EventBusSubscriber(
        modid = "ftboss",
        value = {Dist.CLIENT},
        bus = Bus.MOD
    )
    public static final class Setup {
        private Setup() {
        }

        @SubscribeEvent
        public static void reload(RegisterClientReloadListenersEvent var0) {
            var0.registerReloadListener(new QizaiActionClient());
        }
    }
}
