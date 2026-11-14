package net.farmingtales.ftboss.xueli_multiplayer;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class CadenceServer {
    private static final Map<EasterBossEntity, CadenceServer.Snapshot> COUNTS = new WeakHashMap<>();

    private CadenceServer() {
    }

    public static int players(EasterBossEntity var0) {
        if (var0 != null
            && CadenceRules.enabled(
                var0.getVariant(),
                var0.isMini(),
                var0.isGiant(),
                var0.isManorIntroHidden(),
                var0.isNoAi(),
                var0.isAlive(),
                var0.level().isClientSide,
                ManorFight.isFinaleMember(var0)
            )
            && var0.level() instanceof ServerLevel var1) {
            long var12 = var1.getGameTime();
            CadenceServer.Snapshot var4 = COUNTS.get(var0);
            if (var4 != null && var4.level() == var1 && var4.tick() == var12) {
                return var4.players();
            } else {
                UUID var5 = ManorFightMembers.fightId(var0);
                Vec3 var6 = var5 != null && ManorFight.hasActive(var5) ? ManorFight.anchorOf(var5) : null;
                double var7 = var6 == null ? 2304.0 : 9216.0;
                if (var6 == null) {
                    var6 = var0.position();
                }

                int var9 = 0;

                for (ServerPlayer var11 : var1.players()) {
                    if (CadenceRules.participant(
                        var11.isAlive(), var11.isCreative(), var11.isSpectator(), var11.level() == var1, var11.distanceToSqr(var6.x, var6.y, var6.z), var7
                    )) {
                        if (++var9 >= 6) {
                            break;
                        }
                    }
                }

                int var13 = Math.max(1, var9);
                COUNTS.put(var0, new CadenceServer.Snapshot(var1, var12, var13));
                return var13;
            }
        } else {
            return 1;
        }
    }

    public static int melee(int var0, EasterBossEntity var1) {
        return CadenceRules.interval(var0, players(var1), 10);
    }

    public static int skill(int var0, EasterBossEntity var1) {
        return CadenceRules.interval(var0, players(var1), 40);
    }

    public static long cooldown(long var0, EasterBossEntity var2, long var3) {
        return CadenceRules.deadline(var0, var3, 0, players(var2), 40);
    }

    public static long ready(long var0, EasterBossEntity var2, long var3, int var5) {
        return CadenceRules.deadline(var0, var3, var5, players(var2), 20);
    }

    private static record Snapshot(ServerLevel level, long tick, int players) {
    }
}
