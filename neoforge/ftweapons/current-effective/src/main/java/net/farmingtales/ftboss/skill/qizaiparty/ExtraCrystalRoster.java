package net.farmingtales.ftboss.skill.qizaiparty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

public final class ExtraCrystalRoster<T> {
    private final List<ExtraCrystalRoster.Slot<T>> slots = new ArrayList<>();
    private long lastTick = Long.MIN_VALUE;

    public static int count(int var0) {
        return var0 <= 1 ? 0 : Math.multiplyExact(var0 - 1, 2);
    }

    public static double angle(int var0, double var1) {
        return var1 * 0.022 + ((double)(var0 % 8) + 0.5) * Math.PI / 4.0;
    }

    public static double radius(int var0) {
        return 7.6 + (double)(var0 / 8) * 1.4;
    }

    public static double height(int var0) {
        return 3.5 + (double)(var0 / 8) * 0.45;
    }

    public int size() {
        return this.slots.size();
    }

    public List<T> members() {
        return this.slots.stream().map(var0 -> var0.crystal).filter(var0 -> var0 != null).toList();
    }

    public int indexOf(T var1) {
        if (var1 == null) {
            return -1;
        } else {
            for (int var2 = 0; var2 < this.slots.size(); var2++) {
                if (this.slots.get(var2).crystal == var1) {
                    return var2;
                }
            }

            return -1;
        }
    }

    public void close(Consumer<T> var1) {
        List var2 = this.members();
        this.slots.clear();
        this.lastTick = Long.MIN_VALUE;
        var2.forEach(var1);
    }

    public void update(long var1, int var3, boolean var4, IntSupplier var5, Predicate<T> var6, BiFunction<Integer, Integer, T> var7, Consumer<T> var8) {
        if (var3 < 0) {
            throw new IllegalArgumentException("Negative extra crystal count");
        } else {
            if (var1 < this.lastTick) {
                this.close(var8);
            }

            for (int var9 = this.slots.size() - 1; var9 >= var3; var9--) {
                ExtraCrystalRoster.Slot<T> var10 = this.slots.remove(var9);
                if (var10.crystal != null) {
                    var8.accept(var10.crystal);
                }
            }

            while (this.slots.size() < var3) {
                this.slots.add(new ExtraCrystalRoster.Slot<>());
            }

            if (var1 != this.lastTick) {
                this.lastTick = var1;

                for (int var12 = 0; var12 < this.slots.size(); var12++) {
                    ExtraCrystalRoster.Slot<T> var13 = this.slots.get(var12);
                    if (var13.crystal != null && !var6.test(var13.crystal)) {
                        T var11 = var13.crystal;
                        var13.crystal = null;
                        var13.role = -1;
                        var13.ready = var1 + 240L;
                        var8.accept(var11);
                    }

                    if (var4 && var13.crystal == null && var1 >= var13.ready) {
                        if (var13.role < 0) {
                            var13.role = var5.getAsInt();
                            if (var13.role < 0 || var13.role > 2) {
                                throw new IllegalArgumentException("Unsupported crystal role");
                            }
                        }

                        var13.crystal = (T)var7.apply(var12, var13.role);
                        if (var13.crystal == null) {
                            var13.ready = var1 + 20L;
                        }
                    }
                }
            }
        }
    }

    private static final class Slot<T> {
        T crystal;
        int role = -1;
        long ready;
    }
}
