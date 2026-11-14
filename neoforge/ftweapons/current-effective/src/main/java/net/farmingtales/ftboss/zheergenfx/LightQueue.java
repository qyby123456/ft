package net.farmingtales.ftboss.zheergenfx;

import java.util.List;

public final class LightQueue {
    private LightQueue() {
    }

    public static void retain(List<LightPacket> var0, long var1) {
        var0.removeIf(var2 -> var1 - var2.born() < -20L || !LightClock.alive((double)Math.max(0L, var1 - var2.born()), var2.duration()));
    }

    public static void offer(List<LightPacket> var0, LightPacket var1, long var2) {
        if (var1.valid() && var2 - var1.born() >= -20L && LightClock.alive((double)Math.max(0L, var2 - var1.born()), var1.duration())) {
            retain(var0, var2);
            if (var1.kind() == 13) {
                var0.removeIf(var1x -> var1x.owner().equals(var1.owner()) && LightClock.charge(var1x.kind()) && var1x.born() <= var1.born());
            } else if (!var0.stream()
                .anyMatch(
                    var1x -> var1x.owner().equals(var1.owner())
                            && var1x.kind() == var1.kind()
                            && var1x.born() == var1.born()
                            && var1x.index() == var1.index()
                            && var1x.origin().equals(var1.origin())
                )) {
                if (LightClock.charge(var1.kind())) {
                    if (var0.stream()
                        .anyMatch(
                            var1x -> var1x.owner().equals(var1.owner())
                                    && LightClock.family(var1x.kind()) == LightClock.family(var1.kind())
                                    && (var1x.born() > var1.born() || var1x.born() == var1.born() && !LightClock.charge(var1x.kind()))
                        )) {
                        return;
                    }

                    var0.removeIf(var1x -> var1x.owner().equals(var1.owner()) && LightClock.charge(var1x.kind()) && var1x.born() <= var1.born());
                }

                if (var1.kind() == 1 || var1.kind() == 3 || var1.kind() == 5) {
                    var0.removeIf(
                        var1x -> var1x.owner().equals(var1.owner())
                                && LightClock.charge(var1x.kind())
                                && LightClock.family(var1x.kind()) == LightClock.family(var1.kind())
                                && var1x.born() <= var1.born()
                    );
                }

                if (var1.kind() == 9) {
                    var0.removeIf(
                        var1x -> var1x.owner().equals(var1.owner())
                                && var1x.kind() == 8
                                && var1x.born() <= var1.born()
                                && var1x.origin().distanceToSqr(var1.origin()) < 0.01
                    );
                }

                while (var0.size() >= 64) {
                    var0.remove(0);
                }

                var0.add(var1);
            }
        }
    }
}
