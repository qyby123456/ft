package net.farmingtales.ftboss.skill.qizaicrossguard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiCrystalEntity;
import net.farmingtales.ftboss.skill.QizaiCrystals;
import net.farmingtales.ftboss.skill.qizaicross.TriadRules;
import net.farmingtales.ftboss.skill.qizaicross.TriadRules.Lease;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent.Post;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent.Pre;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class CrossGuard {
    private static final String MARKER = "ftboss_qizai_cross_extra_guard";
    private static final String ARMED = "ftboss_qizai_cross_extra_guard_armed";
    private static final Map<ServerLevel, Map<UUID, CrossGuard.Group>> ACTIVE = new WeakHashMap<>();

    private CrossGuard() {
    }

    public static boolean holding(EasterBossEntity var0) {
        if (QizaiCrystals.validOwner(var0) && var0.level() instanceof ServerLevel var1 && var1.getEntity(var0.getUUID()) == var0) {
            CompoundTag var4 = var0.getSkillCue();
            CrossGuard.Group var3 = ACTIVE.getOrDefault(var1, Map.of()).get(var0.getUUID());
            return TriadRules.active(var4.getInt("qizai"), var4.getLong("start"), var4.getInt("duration"), var1.getGameTime())
                || var3 != null
                    && var3.owner == var0
                    && var3.lease.currentDuringTick(var4.getInt("qizai"), var4.getLong("start"), var4.getInt("duration"), var1.getGameTime());
        } else {
            return false;
        }
    }

    private static CrossGuard.Group group(EasterBossEntity var0) {
        if (var0 != null && var0.level() instanceof ServerLevel var1) {
            CrossGuard.Group var3 = ACTIVE.getOrDefault(var1, Map.of()).get(var0.getUUID());
            return var3 != null && var3.owner == var0 ? var3 : null;
        } else {
            return null;
        }
    }

    private static boolean current(CrossGuard.Group var0) {
        if (var0 != null
            && QizaiCrystals.validOwner(var0.owner)
            && var0.owner.level() instanceof ServerLevel var1
            && var1.getEntity(var0.owner.getUUID()) == var0.owner
            && group(var0.owner) == var0) {
            CompoundTag var3 = var0.owner.getSkillCue();
            return var0.lease.currentDuringTick(var3.getInt("qizai"), var3.getLong("start"), var3.getInt("duration"), var1.getGameTime());
        } else {
            return false;
        }
    }

    private static CrossGuard.Group group(QizaiCrystalEntity var0) {
        if (var0 != null && var0.isAlive() && !var0.isRemoved()) {
            CrossGuard.Group var1 = group(var0.getOwner());
            if (current(var1) && var1.crystal == var0 && var0.level() == var1.owner.level()) {
                ServerLevel var2 = (ServerLevel)var1.owner.level();
                return var2.getEntity(var0.getUUID()) == var0 ? var1 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static void stabilize(EasterBossEntity var0) {
        if (holding(var0)) {
            var0.getNavigation().stop();
            Vec3 var1 = var0.getDeltaMovement();
            var0.setDeltaMovement(new Vec3(0.0, Math.min(0.0, var1.y), 0.0));
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void livingTick(Pre var0) {
        if (var0.getEntity() instanceof EasterBossEntity var2 && var2.getVariant() == 11 && var2.level() instanceof ServerLevel) {
            tick(var2);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST,
        receiveCanceled = true
    )
    public static void knockback(LivingKnockBackEvent var0) {
        if (var0.getEntity() instanceof EasterBossEntity var1 && holding(var1)) {
            var0.setCanceled(true);
            stabilize(var1);
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent var0) {
        if (var0.getEntity() instanceof EasterBossEntity var1) {
            cancel(var1);
        }
    }

    @SubscribeEvent
    public static void leave(EntityLeaveLevelEvent var0) {
        if (var0.getEntity() instanceof EasterBossEntity var2) {
            cancel(var2);
        }
    }

    @SubscribeEvent
    public static void unload(Unload var0) {
        unloadLevel(var0);
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent var0) {
        stop();
    }

    public static Vec3 movement(EasterBossEntity var0, MoverType var1, Vec3 var2) {
        if (holding(var0) && var2 != null) {
            stabilize(var0);
            return new Vec3(0.0, Math.min(0.0, var2.y), 0.0);
        } else {
            return var2;
        }
    }

    public static void cue(EasterBossEntity var0) {
        stabilize(var0);
        tick(var0);
    }

    public static void tick(EasterBossEntity var0) {
        if (!holding(var0)) {
            cancel(var0);
        } else {
            ServerLevel var1 = (ServerLevel)var0.level();
            CompoundTag var2 = var0.getSkillCue();
            Map var3 = ACTIVE.computeIfAbsent(var1, var0x -> new HashMap<>());
            CrossGuard.Group var4 = (CrossGuard.Group)var3.get(var0.getUUID());
            if (var4 != null && var4.owner != var0) {
                var4.close();
                var3.remove(var0.getUUID());
                var4 = null;
            }

            if (var4 == null) {
                if (var3.size() >= 64) {
                    return;
                }

                var4 = new CrossGuard.Group(var0, var1.getGameTime(), var2.getLong("start"));
                var3.put(var0.getUUID(), var4);
            }

            if (TriadRules.active(var2.getInt("qizai"), var2.getLong("start"), var2.getInt("duration"), var1.getGameTime())) {
                if (!var4.lease.advance(var2.getInt("qizai"), var2.getLong("start"), var2.getInt("duration"), var1.getGameTime())) {
                    var4.close();
                } else if (var4.lease.claim(2)) {
                    QizaiCrystalEntity var5 = new QizaiCrystalEntity(var1, var0, 2);
                    var5.getPersistentData().putBoolean("ftboss_qizai_cross_extra_guard", true);
                    double var6 = 0.0;
                    double var8 = 6.0 * TriadRules.scale((double)var0.getBbWidth(), 0.6);
                    double var10 = (2.8 + Math.sin(2.0) * 0.4) * TriadRules.scale((double)var0.getBbHeight(), 1.8);
                    Vec3 var12 = var0.position().add(Math.cos(var6) * var8, var10, Math.sin(var6) * var8);

                    for (int var13 = 0; var13 < 3; var13++) {
                        var5.setPos(var12.add(0.0, (double)var13 * 0.6, 0.0));
                        if (var1.isUnobstructed(var5)) {
                            var4.crystal = var5;
                            if (var1.addFreshEntity(var5)) {
                                return;
                            }

                            var4.crystal = null;
                        }
                    }

                    var5.discard();
                }
            }
        }
    }

    public static boolean accept(boolean var0, QizaiCrystalEntity var1) {
        return var0 || group(var1) != null;
    }

    public static List<QizaiCrystalEntity> members(List<QizaiCrystalEntity> var0, EasterBossEntity var1) {
        CrossGuard.Group var2 = group(var1);
        if (current(var2) && group(var2.crystal) != null && !var0.contains(var2.crystal)) {
            ArrayList var3 = new ArrayList(var0);
            var3.add(var2.crystal);
            return List.copyOf(var3);
        } else {
            return var0;
        }
    }

    public static int arm(int var0, QizaiCrystalEntity var1) {
        if (group(var1) != null && var1.role() == 2) {
            CompoundTag var2 = var1.getPersistentData();
            if (var2.getBoolean("ftboss_qizai_cross_extra_guard") && !var2.getBoolean("ftboss_qizai_cross_extra_guard_armed")) {
                var2.putBoolean("ftboss_qizai_cross_extra_guard_armed", true);
                return 0;
            } else {
                return var0;
            }
        } else {
            return var0;
        }
    }

    public static float absorb(float var0, float var1, QizaiCrystalEntity var2) {
        if (var2 != null && Float.isFinite(var0) && !(var0 < 0.0F) && Float.isFinite(var1) && !(var1 <= 0.0F)) {
            CrossGuard.Group var3 = group(var2.getOwner());
            if (current(var3) && group(var3.crystal) != null && var2 != var3.crystal) {
                float var4 = Math.max(0.0F, var1 * 0.35F - var0);
                return var0 + var3.crystal.absorb(var4);
            } else {
                return var0;
            }
        } else {
            return var0;
        }
    }

    public static double angle(double var0, QizaiCrystalEntity var2) {
        CrossGuard.Group var3 = group(var2);
        return var3 == null ? var0 : (double)var2.tickCount * 0.022;
    }

    public static double radius(double var0, QizaiCrystalEntity var2) {
        CrossGuard.Group var3 = group(var2);
        return var3 == null ? var0 : 6.0 * TriadRules.scale((double)var3.owner.getBbWidth(), 0.6);
    }

    public static double height(double var0, QizaiCrystalEntity var2) {
        CrossGuard.Group var3 = group(var2);
        return var3 == null ? var0 : 2.8 * TriadRules.scale((double)var3.owner.getBbHeight(), 1.8);
    }

    public static double bob(double var0, QizaiCrystalEntity var2) {
        CrossGuard.Group var3 = group(var2);
        return var3 == null ? var0 : var0 * TriadRules.scale((double)var3.owner.getBbHeight(), 1.8);
    }

    public static void cancel(EasterBossEntity var0) {
        if (var0 != null) {
            for (Map var2 : List.copyOf(ACTIVE.values())) {
                CrossGuard.Group var3 = (CrossGuard.Group)var2.get(var0.getUUID());
                if (var3 != null && var3.owner == var0) {
                    var2.remove(var0.getUUID());
                    var3.close();
                }
            }

            ACTIVE.values().removeIf(Map::isEmpty);
        }
    }

    @SubscribeEvent
    public static void ended(Post var0) {
        if (var0.getLevel() instanceof ServerLevel var1) {
            Map<UUID, CrossGuard.Group> var6 = ACTIVE.get(var1);
            if (var6 != null) {
                for (CrossGuard.Group var4 : List.copyOf(var6.values())) {
                    CompoundTag var5 = var4.owner.getSkillCue();
                    if (var4.owner.level() != var1
                        || var1.getEntity(var4.owner.getUUID()) != var4.owner
                        || !QizaiCrystals.validOwner(var4.owner)
                        || !var4.lease.current(var5.getInt("qizai"), var5.getLong("start"), var5.getInt("duration"), var1.getGameTime())) {
                        var4.close();
                        var6.remove(var4.owner.getUUID(), var4);
                    }
                }

                if (var6.isEmpty()) {
                    ACTIVE.remove(var1);
                }
            }
        }
    }

    private static void unloadLevel(Unload var0) {
        Map<UUID, CrossGuard.Group> var1 = ACTIVE.remove(var0.getLevel());
        if (var1 != null) {
            var1.values().forEach(CrossGuard.Group::close);
        }
    }

    public static void stop() {
        ACTIVE.values().forEach(var0 -> var0.values().forEach(CrossGuard.Group::close));
        ACTIVE.clear();
    }

    private static final class Group {
        final EasterBossEntity owner;
        final Lease lease;
        QizaiCrystalEntity crystal;

        Group(EasterBossEntity var1, long var2, long var4) {
            this.owner = var1;
            this.lease = new Lease(var2, var4);
        }

        void close() {
            this.lease.close();
            if (this.crystal != null) {
                this.crystal.discard();
                this.crystal = null;
            }
        }
    }
}
