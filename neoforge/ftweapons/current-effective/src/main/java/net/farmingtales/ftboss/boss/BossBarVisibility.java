package net.farmingtales.ftboss.boss;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class BossBarVisibility {
    private static final Map<ServerBossEvent, WeakReference<Entity>> OWNERS = Collections.synchronizedMap(new WeakHashMap<>());

    private BossBarVisibility() {
    }

    private static boolean shouldHide(boolean var0, boolean var1, boolean var2) {
        return var0 || var1 || var2;
    }

    public static boolean summoned(Entity var0) {
        if (var0 != null) {
            boolean var10000;
            label23: {
                if (var0 instanceof EasterBossEntity var1 && var1.isMini()) {
                    var10000 = true;
                    break label23;
                }

                var10000 = false;
            }

            if (shouldHide(var10000, LemonSummonRewards.suppressed(var0), ManorFightMembers.ownerId(var0) != null)) {
                return true;
            }
        }

        return false;
    }

    public static void track(Entity var0, ServerBossEvent var1) {
        if (var0 != null && var1 != null) {
            OWNERS.put(var1, new WeakReference<>(var0));
        }
    }

    private static boolean hidden(ServerBossEvent var0) {
        WeakReference var1 = OWNERS.get(var0);
        return var1 != null && summoned((Entity)var1.get());
    }

    public static void visible(ServerBossEvent var0, boolean var1) {
        var0.setVisible(var1 && !hidden(var0));
    }

    public static void addPlayer(ServerBossEvent var0, ServerPlayer var1) {
        if (hidden(var0)) {
            BossBars.hide(var0);
        } else {
            var0.addPlayer(var1);
        }
    }
}
