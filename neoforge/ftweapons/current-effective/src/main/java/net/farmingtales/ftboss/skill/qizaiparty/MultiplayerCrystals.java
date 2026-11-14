package net.farmingtales.ftboss.skill.qizaiparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.BossScaling;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiCrystalEntity;
import net.farmingtales.ftboss.skill.QizaiCrystals;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;

public final class MultiplayerCrystals {
    private static final Map<ServerLevel, Map<UUID, MultiplayerCrystals.Group>> active = new WeakHashMap<>();

    private MultiplayerCrystals() {
    }

    private static MultiplayerCrystals.Group group(EasterBossEntity var0) {
        if (var0 != null && var0.level() instanceof ServerLevel var1) {
            MultiplayerCrystals.Group var3 = active.getOrDefault(var1, Map.of()).get(var0.getUUID());
            return var3 != null && var3.owner == var0 ? var3 : null;
        } else {
            return null;
        }
    }

    private static int index(QizaiCrystalEntity var0) {
        MultiplayerCrystals.Group var1 = var0 == null ? null : group(var0.getOwner());
        return var1 == null ? -1 : var1.roster.indexOf(var0);
    }

    public static int participants(EasterBossEntity var0) {
        if (!(var0.level() instanceof ServerLevel var1)) {
            return 1;
        } else {
            UUID var7 = ManorFightMembers.fightId(var0);
            Vec3 var3 = var7 != null && ManorFight.hasActive(var7) ? ManorFight.anchorOf(var7) : null;
            if (var3 == null) {
                return BossScaling.playersInFight(var0);
            } else {
                int var4 = 0;

                for (ServerPlayer var6 : var1.players()) {
                    if (var6.isAlive()
                        && !var6.isRemoved()
                        && !var6.isCreative()
                        && !var6.isSpectator()
                        && var6.distanceToSqr(var3.x, var3.y, var3.z) <= 9216.0) {
                        var4++;
                    }
                }

                return Math.max(1, var4);
            }
        }
    }

    public static void tick(EasterBossEntity var0) {
        if (var0.level() instanceof ServerLevel var1) {
            if (QizaiCrystals.validOwner(var0) && var1.getEntity(var0.getUUID()) == var0) {
                int var7 = ExtraCrystalRoster.count(participants(var0));
                MultiplayerCrystals.Group var3 = group(var0);
                if (var7 == 0) {
                    cancel(var0);
                } else {
                    boolean var4 = QizaiCrystals.combatTarget(var0) != null;
                    if (var3 == null) {
                        if (!var4) {
                            return;
                        }

                        Map var5 = active.computeIfAbsent(var1, var0x -> new HashMap<>());
                        if (var5.size() >= 64) {
                            return;
                        }

                        MultiplayerCrystals.Group var6 = (MultiplayerCrystals.Group)var5.remove(var0.getUUID());
                        if (var6 != null) {
                            var6.close();
                        }

                        var3 = new MultiplayerCrystals.Group(var0);
                        var5.put(var0.getUUID(), var3);
                    }

                    var3.roster
                        .update(
                            var1.getGameTime(),
                            var7,
                            var4,
                            () -> var0.getRandom().nextInt(3),
                            var1x -> var1x.isAlive() && !var1x.isRemoved() && var1.getEntity(var1x.getUUID()) == var1x,
                            (var2, var3x) -> summon(var1, var0, var2, var3x),
                            var0x -> var0x.discard()
                        );
                }
            } else {
                cancel(var0);
            }
        }
    }

    private static QizaiCrystalEntity summon(ServerLevel var0, EasterBossEntity var1, int var2, int var3) {
        QizaiCrystalEntity var4 = new QizaiCrystalEntity(var0, var1, var3);
        double var5 = ExtraCrystalRoster.angle(var2, (double)var1.tickCount);
        double var7 = ExtraCrystalRoster.radius(var2);
        Vec3 var9 = var1.position().add(Math.cos(var5) * var7, ExtraCrystalRoster.height(var2), Math.sin(var5) * var7);

        for (int var10 = 0; var10 < 3; var10++) {
            var4.setPos(var9.add(0.0, (double)var10 * 0.6, 0.0));
            if (var0.isUnobstructed(var4) && var0.addFreshEntity(var4)) {
                return var4;
            }
        }

        var4.discard();
        return null;
    }

    public static boolean accept(boolean var0, QizaiCrystalEntity var1) {
        if (var0) {
            return true;
        } else if (var1 != null && var1.isAlive() && !var1.isRemoved()) {
            EasterBossEntity var2 = var1.getOwner();
            return QizaiCrystals.validOwner(var2)
                && var2.level() instanceof ServerLevel var3
                && var3.getEntity(var2.getUUID()) == var2
                && var3.getEntity(var1.getUUID()) == var1
                && index(var1) >= 0;
        } else {
            return false;
        }
    }

    public static List<QizaiCrystalEntity> members(List<QizaiCrystalEntity> var0, EasterBossEntity var1) {
        MultiplayerCrystals.Group var2 = group(var1);
        if (var2 == null) {
            return var0;
        } else {
            ArrayList var3 = new ArrayList(var0);

            for (QizaiCrystalEntity var5 : var2.roster.members()) {
                if (accept(false, var5) && !var3.contains(var5)) {
                    var3.add(var5);
                }
            }

            return List.copyOf(var3);
        }
    }

    public static double angle(double var0, QizaiCrystalEntity var2) {
        int var3 = index(var2);
        return var3 < 0 ? var0 : ExtraCrystalRoster.angle(var3, (double)var2.getOwner().tickCount);
    }

    public static double radius(double var0, QizaiCrystalEntity var2) {
        int var3 = index(var2);
        return var3 < 0 ? var0 : ExtraCrystalRoster.radius(var3);
    }

    public static double height(double var0, QizaiCrystalEntity var2) {
        int var3 = index(var2);
        return var3 < 0 ? var0 : ExtraCrystalRoster.height(var3);
    }

    public static double bob(double var0, QizaiCrystalEntity var2) {
        return index(var2) < 0 ? var0 : 0.3;
    }

    public static void cancel(EasterBossEntity var0) {
        if (var0 != null && var0.level() instanceof ServerLevel var1) {
            Map var4 = active.get(var1);
            if (var4 != null) {
                MultiplayerCrystals.Group var3 = (MultiplayerCrystals.Group)var4.remove(var0.getUUID());
                if (var3 != null) {
                    var3.close();
                }

                if (var4.isEmpty()) {
                    active.remove(var1);
                }
            }
        }
    }

    public static void unload(Unload var0) {
        Map<UUID, MultiplayerCrystals.Group> var1 = active.remove(var0.getLevel());
        if (var1 != null) {
            List.copyOf(var1.values()).forEach(MultiplayerCrystals.Group::close);
        }
    }

    public static void stop() {
        List<Map<UUID, MultiplayerCrystals.Group>> var0 = List.copyOf(active.values());
        active.clear();

        for (Map<UUID, MultiplayerCrystals.Group> var2 : var0) {
            List.copyOf(var2.values()).forEach(MultiplayerCrystals.Group::close);
        }
    }

    private static final class Group {
        final EasterBossEntity owner;
        final ExtraCrystalRoster<QizaiCrystalEntity> roster = new ExtraCrystalRoster<>();

        Group(EasterBossEntity var1) {
            this.owner = var1;
        }

        void close() {
            this.roster.close(var0 -> var0.discard());
        }
    }
}
