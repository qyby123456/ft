package cn.farmingtales.qysevent;

import java.lang.reflect.Method;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

final class SeasonIntegration {
    private final Method getState;
    private final Method getCycle;
    private final Method getDayDuration;
    private final Method getTicks;

    SeasonIntegration(ClassLoader loader, String helperName, String stateName) {
        Method state = null;
        Method cycle = null;
        Method dayDuration = null;
        Method ticks = null;
        try {
            Class<?> helper = Class.forName(helperName, false, loader);
            Class<?> type = Class.forName(stateName, false, loader);
            state = helper.getMethod("getSeasonState", Level.class);
            cycle = type.getMethod("getCycleDuration");
            dayDuration = type.getMethod("getDayDuration");
            ticks = type.getMethod("getSeasonCycleTicks");
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            state = null;
        }
        getState = state;
        getCycle = cycle;
        getDayDuration = dayDuration;
        getTicks = ticks;
    }

    SolarTerms.TermInfo atDayOffset(ServerLevel level, int offsetDays) {
        if (getState == null) return null;
        try {
            Object state = getState.invoke(null, level);
            int cycle = Math.max(24, ((Number) getCycle.invoke(state)).intValue());
            int dayDuration = Math.max(1, ((Number) getDayDuration.invoke(state)).intValue());
            long shifted = ((Number) getTicks.invoke(state)).longValue() + (long) offsetDays * dayDuration;
            int cycleTicks = (int) Math.floorMod(shifted, (long) cycle);
            return SolarTerms.fromCycle(cycleTicks, cycle, dayDuration);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            return null;
        }
    }
}
