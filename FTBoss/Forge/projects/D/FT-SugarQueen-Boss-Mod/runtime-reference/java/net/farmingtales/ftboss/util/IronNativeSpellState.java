/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.farmingtales.ftboss.util.IronNativeSpellRules;
import net.farmingtales.ftboss.util.SpellVisualRules;

public final class IronNativeSpellState<T> {
    private final List<Entry<T>> entries = new ArrayList<Entry<T>>();
    private Object world;
    private long clock = Long.MIN_VALUE;

    public void advance(Object currentWorld, long now, Predicate<T> alive) {
        if (this.world != currentWorld || now < this.clock) {
            this.clear();
        }
        this.world = currentWorld;
        this.clock = now;
        this.entries.removeIf(entry -> now - entry.born() >= (long)IronNativeSpellRules.lifetime(entry.style()) || !alive.test(entry.payload()));
    }

    public void add(UUID owner, SpellVisualRules.Spell spell, IronNativeSpellRules.Style style, T payload, long now) {
        this.entries.removeIf(entry -> entry.owner().equals(owner) && entry.spell() == spell);
        if (this.entries.size() >= 24) {
            this.entries.remove(0);
        }
        this.entries.add(new Entry<T>(owner, spell, style, payload, now));
    }

    public List<Entry<T>> entries() {
        return List.copyOf(this.entries);
    }

    public void clear() {
        this.entries.clear();
        this.world = null;
        this.clock = Long.MIN_VALUE;
    }

    public record Entry<T>(UUID owner, SpellVisualRules.Spell spell, IronNativeSpellRules.Style style, T payload, long born) {
    }
}

