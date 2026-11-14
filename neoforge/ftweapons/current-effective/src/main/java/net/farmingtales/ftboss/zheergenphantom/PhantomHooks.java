package net.farmingtales.ftboss.zheergenphantom;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;

public final class PhantomHooks {
    private static final Map<EasterBossEntity, PhantomHooks.State> ACTIVE = new WeakHashMap<>();

    private PhantomHooks() {
    }

    private static boolean enabled(EasterBossEntity var0) {
        return var0.getVariant() == 7 && !var0.isMini() && !var0.isGiant() && var0.isAlive() && var0.level() instanceof ServerLevel;
    }

    public static void capture(EasterBossEntity var0) {
        if (!enabled(var0) || !((Object)var0 instanceof PhantomAccess var1)) {
            return;
        }
        ServerLevel var2 = (ServerLevel)var0.level();
        PhantomHooks.State var3 = ACTIVE.get(var0);
        if (var3 == null || var3.level() != var2) {
            var3 = new PhantomHooks.State(var2, new PhantomTimeline());
            ACTIVE.put(var0, var3);
        }
        PhantomSnapshot var4 = var1.ftboss$phantomSnapshot();
        if (var4.skill() == null) {
            return;
        }
        var3.timeline().schedule(var2.getGameTime(), var4);
        var1.ftboss$phantomClear();
    }

    public static void tick(EasterBossEntity var0) {
        PhantomHooks.State var1 = ACTIVE.get(var0);
        if (var1 == null) {
            return;
        }
        if (!enabled(var0) || var1.level() != var0.level()) {
            ACTIVE.remove(var0);
            return;
        }
        if (!((Object)var0 instanceof PhantomAccess var2)) {
            ACTIVE.remove(var0);
            return;
        }
        for (PhantomSnapshot var4 : var1.timeline().poll(var1.level().getGameTime())) {
            var2.ftboss$phantomRestore(var4);
            try {
                var2.ftboss$phantomReplay(var1.level());
            } finally {
                var2.ftboss$phantomClear();
            }
        }
        if (var1.timeline().size() == 0) {
            ACTIVE.remove(var0);
        }
    }

    private static record State(ServerLevel level, PhantomTimeline timeline) {
    }
}

