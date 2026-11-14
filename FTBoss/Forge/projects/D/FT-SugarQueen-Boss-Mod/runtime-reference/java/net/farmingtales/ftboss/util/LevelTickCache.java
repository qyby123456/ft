/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Objects;
import java.util.function.Supplier;

public final class LevelTickCache<T> {
    private Object level;
    private long tick;
    private T value;

    public T get(Object currentLevel, long currentTick, Supplier<T> factory) {
        if (currentLevel == null) {
            this.clear();
            return null;
        }
        if (this.level != currentLevel || this.tick != currentTick || this.value == null) {
            T next = Objects.requireNonNull(factory.get());
            this.level = currentLevel;
            this.tick = currentTick;
            this.value = next;
        }
        return this.value;
    }

    public void clear(Object unloadedLevel) {
        if (this.level == unloadedLevel) {
            this.clear();
        }
    }

    public void clear() {
        this.level = null;
        this.value = null;
        this.tick = 0L;
    }
}

