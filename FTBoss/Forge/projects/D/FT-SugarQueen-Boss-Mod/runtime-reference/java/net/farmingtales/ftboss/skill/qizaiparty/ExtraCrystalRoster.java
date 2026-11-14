/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.skill.qizaiparty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

public final class ExtraCrystalRoster<T> {
    private final List<Slot<T>> slots = new ArrayList<Slot<T>>();
    private long lastTick = Long.MIN_VALUE;

    public static int count(int n) {
        return n <= 1 ? 0 : Math.multiplyExact(n - 1, 2);
    }

    public static double angle(int n, double d) {
        return d * 0.022 + ((double)(n % 8) + 0.5) * Math.PI / 4.0;
    }

    public static double radius(int n) {
        return 7.6 + (double)(n / 8) * 1.4;
    }

    public static double height(int n) {
        return 3.5 + (double)(n / 8) * 0.45;
    }

    public int size() {
        return this.slots.size();
    }

    public List<T> members() {
        return this.slots.stream().map(slot -> slot.crystal).filter(object -> object != null).toList();
    }

    public int indexOf(T t) {
        if (t == null) {
            return -1;
        }
        for (int i = 0; i < this.slots.size(); ++i) {
            if (this.slots.get((int)i).crystal != t) continue;
            return i;
        }
        return -1;
    }

    public void close(Consumer<T> consumer) {
        List<T> list = this.members();
        this.slots.clear();
        this.lastTick = Long.MIN_VALUE;
        list.forEach(consumer);
    }

    public void update(long l, int n, boolean bl, IntSupplier intSupplier, Predicate<T> predicate, BiFunction<Integer, Integer, T> biFunction, Consumer<T> consumer) {
        Slot<T> slot;
        int n2;
        if (n < 0) {
            throw new IllegalArgumentException("Negative extra crystal count");
        }
        if (l < this.lastTick) {
            this.close(consumer);
        }
        for (n2 = this.slots.size() - 1; n2 >= n; --n2) {
            slot = this.slots.remove(n2);
            if (slot.crystal == null) continue;
            consumer.accept(slot.crystal);
        }
        while (this.slots.size() < n) {
            this.slots.add(new Slot());
        }
        if (l == this.lastTick) {
            return;
        }
        this.lastTick = l;
        for (n2 = 0; n2 < this.slots.size(); ++n2) {
            slot = this.slots.get(n2);
            if (slot.crystal != null && !predicate.test(slot.crystal)) {
                Object t = slot.crystal;
                slot.crystal = null;
                slot.role = -1;
                slot.ready = l + 240L;
                consumer.accept(t);
            }
            if (!bl || slot.crystal != null || l < slot.ready) continue;
            if (slot.role < 0) {
                slot.role = intSupplier.getAsInt();
                if (slot.role < 0 || slot.role > 2) {
                    throw new IllegalArgumentException("Unsupported crystal role");
                }
            }
            slot.crystal = biFunction.apply(n2, slot.role);
            if (slot.crystal != null) continue;
            slot.ready = l + 20L;
        }
    }

    private static final class Slot<T> {
        T crystal;
        int role = -1;
        long ready;

        private Slot() {
        }
    }
}

