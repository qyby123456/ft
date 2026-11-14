/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.qingyilight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.farmingtales.ftboss.qingyilight.LightPacket;

public final class LightQueue {
    private final List<LightPacket> active = new ArrayList<LightPacket>();
    private final Map<String, Long> released = new HashMap<String, Long>();

    public void clear() {
        this.active.clear();
        this.released.clear();
    }

    public void removeIf(Predicate<LightPacket> predicate) {
        this.active.removeIf(predicate);
    }

    public void accept(LightPacket lightPacket, long l) {
        int n;
        this.expire(l);
        if (!lightPacket.valid() || lightPacket.born() > l + 20L) {
            return;
        }
        if (lightPacket.kind() == 11) {
            if (l - lightPacket.born() > 640L) {
                return;
            }
            this.active.removeIf((? super E lightPacket2) -> lightPacket2.owner().equals(lightPacket.owner()) && lightPacket2.born() <= lightPacket.born() && (lightPacket2.phase() == 0 && lightPacket2.entityId() < 0 || lightPacket2.kind() == 4));
            for (int i = 0; i <= 3; ++i) {
                this.released.merge(String.valueOf(lightPacket.owner()) + "/" + i, lightPacket.born(), Math::max);
            }
            return;
        }
        if (l - lightPacket.born() >= (long)lightPacket.duration()) {
            return;
        }
        String string = String.valueOf(lightPacket.owner()) + "/" + lightPacket.kind();
        if (lightPacket.kind() <= 3) {
            if (lightPacket.phase() == 0 && this.released.getOrDefault(string, -1L) >= lightPacket.born()) {
                return;
            }
            if (lightPacket.phase() > 0) {
                this.released.merge(string, lightPacket.born(), Math::max);
                this.active.removeIf((? super E lightPacket2) -> lightPacket2.owner().equals(lightPacket.owner()) && lightPacket2.kind() == lightPacket.kind() && lightPacket2.phase() == 0 && lightPacket2.born() <= lightPacket.born());
            }
        }
        if (this.active.stream().anyMatch(lightPacket2 -> lightPacket2.equals(lightPacket))) {
            return;
        }
        if (lightPacket.kind() == 4) {
            this.active.removeIf((? super E lightPacket2) -> lightPacket2.owner().equals(lightPacket.owner()) && lightPacket2.kind() == lightPacket.kind());
        }
        if (lightPacket.kind() == 9) {
            List<LightPacket> list = this.active.stream().filter(lightPacket2 -> lightPacket2.owner().equals(lightPacket.owner()) && lightPacket2.kind() == 9).sorted(Comparator.comparingLong(LightPacket::born)).toList();
            for (n = 0; n < list.size() - 1; ++n) {
                this.active.remove(list.get(n));
            }
        }
        if (this.active.size() >= 96) {
            int n2 = -1;
            for (n = 0; n < this.active.size(); ++n) {
                if (this.active.get(n).phase() <= 0 || this.active.get(n).entityId() >= 0) continue;
                n2 = n;
                break;
            }
            this.active.remove(n2 < 0 ? 0 : n2);
        }
        this.active.add(lightPacket);
    }

    public void expire(long l) {
        this.active.removeIf((? super E lightPacket) -> l - lightPacket.born() >= (long)lightPacket.duration() || lightPacket.born() > l + 20L);
        this.released.entrySet().removeIf((? super E entry) -> l - (Long)entry.getValue() > 640L);
    }

    public List<LightPacket> snapshot(long l) {
        this.expire(l);
        return new ArrayList<LightPacket>(this.active);
    }
}

