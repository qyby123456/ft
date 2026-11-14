package net.farmingtales.ftboss.qingyilight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class LightQueue {
    private final List<LightPacket> active = new ArrayList<>();
    private final Map<String, Long> released = new HashMap<>();

    public void clear() {
        this.active.clear();
        this.released.clear();
    }

    public void removeIf(Predicate<LightPacket> var1) {
        this.active.removeIf(var1);
    }

    public void accept(LightPacket var1, long var2) {
        this.expire(var2);
        if (var1.valid() && var1.born() <= var2 + 20L) {
            if (var1.kind() == 11) {
                if (var2 - var1.born() <= 640L) {
                    this.active
                        .removeIf(
                            var1x -> var1x.owner().equals(var1.owner())
                                    && var1x.born() <= var1.born()
                                    && (var1x.phase() == 0 && var1x.entityId() < 0 || var1x.kind() == 4)
                        );

                    for (int var7 = 0; var7 <= 3; var7++) {
                        this.released.merge(var1.owner() + "/" + var7, var1.born(), Math::max);
                    }
                }
            } else if (var2 - var1.born() < (long)var1.duration()) {
                String var4 = var1.owner() + "/" + var1.kind();
                if (var1.kind() <= 3) {
                    if (var1.phase() == 0 && this.released.getOrDefault(var4, -1L) >= var1.born()) {
                        return;
                    }

                    if (var1.phase() > 0) {
                        this.released.merge(var4, var1.born(), Math::max);
                        this.active
                            .removeIf(
                                var1x -> var1x.owner().equals(var1.owner()) && var1x.kind() == var1.kind() && var1x.phase() == 0 && var1x.born() <= var1.born()
                            );
                    }
                }

                if (!this.active.stream().anyMatch(var1x -> var1x.equals(var1))) {
                    if (var1.kind() == 4) {
                        this.active.removeIf(var1x -> var1x.owner().equals(var1.owner()) && var1x.kind() == var1.kind());
                    }

                    if (var1.kind() == 9) {
                        List var5 = this.active
                            .stream()
                            .filter(var1x -> var1x.owner().equals(var1.owner()) && var1x.kind() == 9)
                            .sorted(Comparator.comparingLong(LightPacket::born))
                            .toList();

                        for (int var6 = 0; var6 < var5.size() - 1; var6++) {
                            this.active.remove(var5.get(var6));
                        }
                    }

                    if (this.active.size() >= 96) {
                        int var8 = -1;

                        for (int var9 = 0; var9 < this.active.size(); var9++) {
                            if (this.active.get(var9).phase() > 0 && this.active.get(var9).entityId() < 0) {
                                var8 = var9;
                                break;
                            }
                        }

                        this.active.remove(var8 < 0 ? 0 : var8);
                    }

                    this.active.add(var1);
                }
            }
        }
    }

    public void expire(long var1) {
        this.active.removeIf(var2 -> var1 - var2.born() >= (long)var2.duration() || var2.born() > var1 + 20L);
        this.released.entrySet().removeIf(var2 -> var1 - var2.getValue() > 640L);
    }

    public List<LightPacket> snapshot(long var1) {
        this.expire(var1);
        return new ArrayList<>(this.active);
    }
}
