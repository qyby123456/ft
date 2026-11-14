package net.farmingtales.ftboss.skill.qizaicross;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiCrystalEntity;
import net.farmingtales.ftboss.skill.QizaiCrystals;
import net.farmingtales.ftboss.skill.qizaicross.TriadRules.Lease;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent.Post;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent.Pre;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class TemporaryTriads {
    private static final Map<ServerLevel, Map<UUID, TemporaryTriads.Batch>> active = new WeakHashMap<>();
    private static final Map<ServerLevel, Set<EasterBossEntity>> pending = new WeakHashMap<>();

    private TemporaryTriads() {
    }

    private static boolean current(TemporaryTriads.Batch var0) {
        if (var0 != null
            && QizaiCrystals.validOwner(var0.owner)
            && var0.owner.level() instanceof ServerLevel var1
            && active.getOrDefault(var1, Map.of()).get(var0.owner.getUUID()) == var0
            && var1.getEntity(var0.owner.getUUID()) == var0.owner) {
            CompoundTag var3 = var0.owner.getSkillCue();
            return var0.lease.currentDuringTick(var3.getInt("qizai"), var3.getLong("start"), var3.getInt("duration"), var1.getGameTime());
        } else {
            return false;
        }
    }

    private static TemporaryTriads.Batch batch(EasterBossEntity var0) {
        if (var0 != null && var0.level() instanceof ServerLevel var1) {
            TemporaryTriads.Batch var3 = active.getOrDefault(var1, Map.of()).get(var0.getUUID());
            return var3 != null && var3.owner == var0 ? var3 : null;
        } else {
            return null;
        }
    }

    private static TemporaryTriads.Batch batch(QizaiCrystalEntity var0) {
        if (var0 == null) {
            return null;
        } else {
            TemporaryTriads.Batch var1 = batch(var0.getOwner());
            int var2 = var0.role();
            return var1 != null && var2 >= 0 && var2 < 3 && var1.crystals[var2] == var0 ? var1 : null;
        }
    }

    public static boolean accept(boolean var0, QizaiCrystalEntity var1) {
        return var0 || var1 != null && var1.isAlive() && !var1.isRemoved() && current(batch(var1));
    }

    public static List<QizaiCrystalEntity> members(List<QizaiCrystalEntity> var0, EasterBossEntity var1) {
        TemporaryTriads.Batch var2 = batch(var1);
        if (!current(var2)) {
            return var0;
        } else {
            ArrayList var3 = new ArrayList(var0);

            for (QizaiCrystalEntity var7 : var2.crystals) {
                if (var7 != null && var7.isAlive() && !var7.isRemoved() && !var3.contains(var7)) {
                    var3.add(var7);
                }
            }

            return List.copyOf(var3);
        }
    }

    public static double angle(double var0, QizaiCrystalEntity var2) {
        return batch(var2) == null ? var0 : var0 + (Math.PI / 3);
    }

    public static double radius(double var0, QizaiCrystalEntity var2) {
        TemporaryTriads.Batch var3 = batch(var2);
        return var3 == null ? var0 : 6.0 * TriadRules.scale((double)var3.owner.getBbWidth(), 0.6);
    }

    public static double height(double var0, QizaiCrystalEntity var2) {
        TemporaryTriads.Batch var3 = batch(var2);
        return var3 == null ? var0 : 2.8 * TriadRules.scale((double)var3.owner.getBbHeight(), 1.8);
    }

    public static double bob(double var0, QizaiCrystalEntity var2) {
        TemporaryTriads.Batch var3 = batch(var2);
        return var3 == null ? var0 : var0 * TriadRules.scale((double)var3.owner.getBbHeight(), 1.8);
    }

    public static void cancel(EasterBossEntity var0) {
        TemporaryTriads.Batch var1 = batch(var0);
        if (var1 != null) {
            var1.close();
        }
    }

    private static void remove(EasterBossEntity var0) {
        if (var0 != null && var0.level() instanceof ServerLevel var1) {
            Map var4 = active.get(var1);
            if (var4 != null) {
                TemporaryTriads.Batch var3 = (TemporaryTriads.Batch)var4.remove(var0.getUUID());
                if (var3 != null) {
                    var3.close();
                }

                if (var4.isEmpty()) {
                    active.remove(var1);
                }
            }
        }
    }

    private static void pulse(QizaiCrystalEntity var0, boolean var1) {
        if (var0.level() instanceof ServerLevel var2) {
            Vec3 var4 = var0.position();
            var2.sendParticles(ParticleTypes.END_ROD, var4.x, var4.y + 0.35, var4.z, var1 ? 10 : 16, 0.3, 0.45, 0.3, 0.035);
        }
    }

    private static void tick(EasterBossEntity var0) {
        if (var0.level() instanceof ServerLevel var1) {
            CompoundTag var17 = var0.getSkillCue();
            long var3 = var1.getGameTime();
            long var5 = var17.getLong("start");
            int var7 = var17.getInt("qizai");
            int var8 = var17.getInt("duration");
            if (QizaiCrystals.validOwner(var0) && TriadRules.active(var7, var5, var8, var3)) {
                Map var9 = active.computeIfAbsent(var1, var0x -> new HashMap<>());
                TemporaryTriads.Batch var10 = (TemporaryTriads.Batch)var9.get(var0.getUUID());
                if (var10 != null && var10.owner != var0) {
                    var10.close();
                    var9.remove(var0.getUUID());
                    var10 = null;
                }

                if (var10 == null) {
                    if (var9.size() >= 64) {
                        return;
                    }

                    var10 = new TemporaryTriads.Batch(var0, var3, var5);
                    var9.put(var0.getUUID(), var10);
                }

                if (!var10.lease.advance(var7, var5, var8, var3)) {
                    var10.close();
                } else {
                    for (int var11 = 0; var11 < 3; var11++) {
                        if (var10.lease.claim(var11)) {
                            QizaiCrystalEntity var12 = new QizaiCrystalEntity(var1, var0, var11);
                            var12.getPersistentData().putBoolean("ftboss_qizai_cross_temporary", true);
                            double[] var13 = TriadRules.position(var11, 0.0, (double)var0.getBbWidth(), (double)var0.getBbHeight());
                            Vec3 var14 = var0.position().add(var13[0], var13[1], var13[2]);
                            boolean var15 = false;

                            for (int var16 = 0; var16 < 3; var16++) {
                                var12.setPos(var14.add(0.0, (double)var16 * 0.6, 0.0));
                                if (var1.isUnobstructed(var12)) {
                                    var10.crystals[var11] = var12;
                                    if (var1.addFreshEntity(var12)) {
                                        var15 = true;
                                        pulse(var12, true);
                                        break;
                                    }

                                    var10.crystals[var11] = null;
                                }
                            }

                            if (!var15) {
                                var12.discard();
                            }
                        }
                    }
                }
            } else {
                remove(var0);
            }
        }
    }

    @SubscribeEvent
    public static void observed(Pre var0) {
        if (var0.getEntity() instanceof EasterBossEntity var1 && var1.getVariant() == 11 && var1.level() instanceof ServerLevel var2) {
            pending.computeIfAbsent(var2, var0x -> new LinkedHashSet<>()).add(var1);
        }
    }

    @SubscribeEvent
    public static void ended(Post var0) {
        if (var0.getLevel() instanceof ServerLevel var1) {
            Set<EasterBossEntity> var8 = pending.remove(var1);
            if (var8 != null) {
                for (EasterBossEntity var4 : var8) {
                    if (var4.level() == var1 && var1.getEntity(var4.getUUID()) == var4) {
                        tick(var4);
                    }
                }
            }

            Map<UUID, TemporaryTriads.Batch> var9 = active.get(var1);
            if (var9 != null) {
                for (Entry var5 : List.copyOf(var9.entrySet())) {
                    TemporaryTriads.Batch var6 = (TemporaryTriads.Batch)var5.getValue();
                    CompoundTag var7 = var6.owner.getSkillCue();
                    if (var6.owner.level() != var1
                        || var1.getEntity(var6.owner.getUUID()) != var6.owner
                        || !QizaiCrystals.validOwner(var6.owner)
                        || !var6.lease.current(var7.getInt("qizai"), var7.getLong("start"), var7.getInt("duration"), var1.getGameTime())) {
                        var6.close();
                        if (var6.owner.level() != var1
                            || var1.getEntity(var6.owner.getUUID()) != var6.owner
                            || !QizaiCrystals.validOwner(var6.owner)
                            || !TriadRules.active(var7.getInt("qizai"), var7.getLong("start"), var7.getInt("duration"), var1.getGameTime())) {
                            var9.remove(var5.getKey());
                        }
                    }
                }

                if (var9.isEmpty()) {
                    active.remove(var1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent var0) {
        if (var0.getEntity() instanceof EasterBossEntity var1) {
            remove(var1);
        }
    }

    @SubscribeEvent
    public static void left(EntityLeaveLevelEvent var0) {
        if (var0.getEntity() instanceof EasterBossEntity var1) {
            remove(var1);
        }
    }

    @SubscribeEvent
    public static void unload(Unload var0) {
        pending.remove(var0.getLevel());
        Map<UUID, TemporaryTriads.Batch> var1 = active.remove(var0.getLevel());
        if (var1 != null) {
            for (TemporaryTriads.Batch var3 : List.copyOf(var1.values())) {
                var3.close();
            }
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent var0) {
        for (Map<UUID, TemporaryTriads.Batch> var2 : List.copyOf(active.values())) {
            for (TemporaryTriads.Batch var4 : List.copyOf(var2.values())) {
                var4.close();
            }
        }

        active.clear();
        pending.clear();
    }

    private static final class Batch {
        final EasterBossEntity owner;
        final Lease lease;
        final QizaiCrystalEntity[] crystals = new QizaiCrystalEntity[3];

        Batch(EasterBossEntity var1, long var2, long var4) {
            this.owner = var1;
            this.lease = new Lease(var2, var4);
        }

        void close() {
            this.lease.close();

            for (int var1 = 0; var1 < this.crystals.length; var1++) {
                QizaiCrystalEntity var2 = this.crystals[var1];
                this.crystals[var1] = null;
                if (var2 != null && !var2.isRemoved()) {
                    TemporaryTriads.pulse(var2, false);
                    var2.discard();
                }
            }
        }
    }
}
