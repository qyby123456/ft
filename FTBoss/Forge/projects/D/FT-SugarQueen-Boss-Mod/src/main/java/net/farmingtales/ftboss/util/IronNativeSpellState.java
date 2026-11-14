package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.farmingtales.ftboss.util.IronNativeSpellRules.Style;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;

public final class IronNativeSpellState<T> {
    public record Entry<T>(UUID owner, Spell spell, Style style, T payload, long born) {}
    private final List<Entry<T>> entries = new ArrayList<>();
    private Object world;
    private long clock = Long.MIN_VALUE;

    public void advance(Object currentWorld, long now, Predicate<T> alive) {
        if (world != currentWorld || now < clock) clear();
        world = currentWorld;
        clock = now;
        entries.removeIf(entry -> now - entry.born() >= IronNativeSpellRules.lifetime(entry.style()) || !alive.test(entry.payload()));
    }

    public void add(UUID owner, Spell spell, Style style, T payload, long now) {
        entries.removeIf(entry -> entry.owner().equals(owner) && entry.spell() == spell);
        if (entries.size() >= IronNativeSpellRules.MAX_ACTIVE) entries.remove(0);
        entries.add(new Entry<>(owner, spell, style, payload, now));
    }

    public List<Entry<T>> entries() { return List.copyOf(entries); }

    public void clear() {
        entries.clear();
        world = null;
        clock = Long.MIN_VALUE;
    }
}
