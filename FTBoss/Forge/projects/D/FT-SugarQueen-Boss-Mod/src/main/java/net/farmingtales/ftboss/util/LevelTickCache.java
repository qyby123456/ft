package net.farmingtales.ftboss.util;

import java.util.Objects;
import java.util.function.Supplier;

public final class LevelTickCache<T> {
    private Object level;
    private long tick;
    private T value;

    public T get(Object currentLevel, long currentTick, Supplier<T> factory) {
        if (currentLevel == null) {
            clear();
            return null;
        }
        if (level != currentLevel || tick != currentTick || value == null) {
            T next = Objects.requireNonNull(factory.get());
            level = currentLevel;
            tick = currentTick;
            value = next;
        }
        return value;
    }

    public void clear(Object unloadedLevel) {
        if (level == unloadedLevel) clear();
    }

    public void clear() {
        level = null;
        value = null;
        tick = 0;
    }
}
