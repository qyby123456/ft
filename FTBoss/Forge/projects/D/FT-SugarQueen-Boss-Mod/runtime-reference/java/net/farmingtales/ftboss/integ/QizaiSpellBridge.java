/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.integ;

import java.util.List;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class QizaiSpellBridge {
    private static Handler handler;
    private static SupportHandler supportHandler;

    private QizaiSpellBridge() {
    }

    public static void install(Handler value) {
        handler = value;
    }

    public static void installSupport(SupportHandler value) {
        supportHandler = value;
    }

    public static boolean cast(ServerLevel level, EasterBossEntity boss, int kind, List<Vec3> marks) {
        return handler != null && handler.cast(level, boss, kind, List.copyOf(marks));
    }

    public static boolean castSupport(ServerLevel level, EasterBossEntity boss, String spellId) {
        return supportHandler != null && supportHandler.cast(level, boss, spellId);
    }

    @FunctionalInterface
    public static interface Handler {
        public boolean cast(ServerLevel var1, EasterBossEntity var2, int var3, List<Vec3> var4);
    }

    @FunctionalInterface
    public static interface SupportHandler {
        public boolean cast(ServerLevel var1, EasterBossEntity var2, String var3);
    }
}

