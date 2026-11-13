package cn.farmingtales.qysevent;

import net.minecraft.server.level.ServerLevel;
import java.lang.reflect.Method;

public final class SolarTerms {
    public static final String[] NAMES = {
            "立春", "雨水", "惊蛰", "春分", "清明", "谷雨",
            "立夏", "小满", "芒种", "夏至", "小暑", "大暑",
            "立秋", "处暑", "白露", "秋分", "寒露", "霜降",
            "立冬", "小雪", "大雪", "冬至", "小寒", "大寒"
    };

    private static final double[] RAIN_CHANCE = {
            .28, .43, .48, .46, .40, .52,
            .38, .42, .46, .34, .39, .43,
            .34, .31, .28, .25, .23, .21,
            .20, .25, .32, .30, .23, .18
    };

    private static final double[] THUNDER_CHANCE = {
            .01, .02, .04, .05, .04, .06,
            .08, .11, .14, .16, .18, .20,
            .13, .10, .07, .04, .03, .02,
            .01, .01, .01, .01, .01, .01
    };

    public enum SeasonBand {
        SPRING("春季"), SUMMER("夏季"), AUTUMN("秋季"), WINTER("冬季");

        private final String chineseName;

        SeasonBand(String chineseName) {
            this.chineseName = chineseName;
        }

        public String chineseName() {
            return chineseName;
        }
    }

    public record TermInfo(int index, String name, int dayInTerm, int termLengthDays) {}

    public static SeasonBand seasonForTerm(int term) {
        int normalized = Math.floorMod(term, 24);
        if (normalized < 6) return SeasonBand.SPRING;
        if (normalized < 12) return SeasonBand.SUMMER;
        if (normalized < 18) return SeasonBand.AUTUMN;
        return SeasonBand.WINTER;
    }

    public static TermInfo current(ServerLevel level) {
        return atDayOffset(level, 0);
    }

    public static TermInfo atDayOffset(ServerLevel level, int offsetDays) {
        try {
            Class<?> helper = Class.forName("sereneseasons.api.season.SeasonHelper");
            Method getState = helper.getMethod("getSeasonState", net.minecraft.world.level.Level.class);
            Object state = getState.invoke(null, level);
            Class<?> type = Class.forName("sereneseasons.api.season.ISeasonState");
            int cycle = Math.max(24, ((Number) type.getMethod("getCycleDuration").invoke(state)).intValue());
            int dayDuration = Math.max(1, ((Number) type.getMethod("getDayDuration").invoke(state)).intValue());
            long shifted = ((Number) type.getMethod("getSeasonCycleTicks").invoke(state)).longValue()
                    + (long) offsetDays * dayDuration;
            int cycleTicks = (int) Math.floorMod(shifted, (long) cycle);
            return fromCycle(cycleTicks, cycle, dayDuration);
        } catch (Throwable ignored) {
            long absoluteDay = Math.floorDiv(level.getDayTime(), 24000L) + offsetDays;
            int index = Math.floorMod((int) Math.floorDiv(absoluteDay, 4L), 24);
            return new TermInfo(index, NAMES[index], Math.floorMod((int) absoluteDay, 4) + 1, 4);
        }
    }

    private static TermInfo fromCycle(int cycleTicks, int cycle, int dayDuration) {
        int index = Math.min(23, (int) ((long) cycleTicks * 24L / cycle));
        int termStart = (int) ((long) index * cycle / 24L);
        int termEnd = (int) ((long) (index + 1) * cycle / 24L);
        int day = Math.max(1, (cycleTicks - termStart) / dayDuration + 1);
        int length = Math.max(1, (termEnd - termStart + dayDuration - 1) / dayDuration);
        return new TermInfo(index, NAMES[index], day, length);
    }

    public static double rainChance(int term) {
        return RAIN_CHANCE[Math.floorMod(term, 24)];
    }

    public static double thunderChance(int term) {
        return THUNDER_CHANCE[Math.floorMod(term, 24)];
    }

    private SolarTerms() {}
}


