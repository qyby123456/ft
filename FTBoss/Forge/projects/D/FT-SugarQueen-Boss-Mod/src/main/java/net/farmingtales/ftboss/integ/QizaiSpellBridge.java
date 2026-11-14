package net.farmingtales.ftboss.integ;

import java.util.List;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class QizaiSpellBridge {
    @FunctionalInterface
    public interface Handler {
        boolean cast(ServerLevel level, EasterBossEntity boss, int kind, List<Vec3> marks);
    }

    private static Handler handler;
    /** 2026-09-20 v188：辅助（防御）法术入口 —— 用户要求「允许祁崽抽庄园的辅助法术」。 */
    @FunctionalInterface
    public interface SupportHandler {
        boolean cast(ServerLevel level, EasterBossEntity boss, String spellId);
    }

    private static SupportHandler supportHandler;

    private QizaiSpellBridge() {}

    public static void install(Handler value) { handler = value; }

    public static void installSupport(SupportHandler value) { supportHandler = value; }

    public static boolean cast(ServerLevel level, EasterBossEntity boss, int kind, List<Vec3> marks) {
        return handler != null && handler.cast(level, boss, kind, List.copyOf(marks));
    }

    /** 施放一门支援型法术（按法术 id）；铁魔法不可用、或那门法术被禁用时返回 false。 */
    public static boolean castSupport(ServerLevel level, EasterBossEntity boss, String spellId) {
        return supportHandler != null && supportHandler.cast(level, boss, spellId);
    }
}
