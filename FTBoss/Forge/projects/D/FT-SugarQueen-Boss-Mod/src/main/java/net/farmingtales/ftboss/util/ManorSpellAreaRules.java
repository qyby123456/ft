package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.List;

/** 庄园玩家法术范围规则：半径倍率、领域布局与特殊形状判定；2026-10-27 统一翻倍。 */
public final class ManorSpellAreaRules {
    public static final double PLAYER_RADIUS_SCALE = 2.0;

    private ManorSpellAreaRules() {}

    public static double scale(boolean player) { return player ? PLAYER_RADIUS_SCALE : 1.0; }

    public static double cueScale(double value) {
        return Double.isFinite(value) && value == PLAYER_RADIUS_SCALE ? PLAYER_RADIUS_SCALE : 1.0;
    }

    public static boolean inside(int kind, double offsetX, double height, double offsetZ, double scale) {
        return kind == QizaiRules.STAR_CROSS ? QizaiRules.cross(offsetX / scale, height, offsetZ / scale)
                : QizaiRules.inside((offsetX * offsetX + offsetZ * offsetZ) / (scale * scale), height, kind);
    }

    public static List<ZicaiVoidRules.Mark> voidLayout(boolean fountain, double angle, double distance, double scale) {
        List<ZicaiVoidRules.Mark> original = ZicaiVoidRules.layout(fountain, angle, distance);
        List<ZicaiVoidRules.Mark> result = new ArrayList<>(original.size());
        for (int index = 0; index < original.size(); index++) {
            ZicaiVoidRules.Mark mark = original.get(index);
            double spread = fountain && index == 0 ? 1.0 : scale;
            result.add(new ZicaiVoidRules.Mark(mark.x() * spread, mark.z() * spread, mark.warning(), mark.strike()));
        }
        return List.copyOf(result);
    }
}
