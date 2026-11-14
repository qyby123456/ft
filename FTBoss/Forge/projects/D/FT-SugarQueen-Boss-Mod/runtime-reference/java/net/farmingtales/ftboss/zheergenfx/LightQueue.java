/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.zheergenfx;

import java.util.List;
import net.farmingtales.ftboss.zheergenfx.LightClock;
import net.farmingtales.ftboss.zheergenfx.LightPacket;

public final class LightQueue {
    private LightQueue() {
    }

    public static void retain(List<LightPacket> list, long l) {
        list.removeIf(lightPacket -> l - lightPacket.born() < -20L || !LightClock.alive(Math.max(0L, l - lightPacket.born()), lightPacket.duration()));
    }

    public static void offer(List<LightPacket> list, LightPacket lightPacket, long l) {
        if (!lightPacket.valid() || l - lightPacket.born() < -20L || !LightClock.alive(Math.max(0L, l - lightPacket.born()), lightPacket.duration())) {
            return;
        }
        LightQueue.retain(list, l);
        if (lightPacket.kind() == 13) {
            list.removeIf(lightPacket2 -> lightPacket2.owner().equals(lightPacket.owner()) && LightClock.charge(lightPacket2.kind()) && lightPacket2.born() <= lightPacket.born());
            return;
        }
        if (list.stream().anyMatch(lightPacket2 -> lightPacket2.owner().equals(lightPacket.owner()) && lightPacket2.kind() == lightPacket.kind() && lightPacket2.born() == lightPacket.born() && lightPacket2.index() == lightPacket.index() && lightPacket2.origin().equals((Object)lightPacket.origin()))) {
            return;
        }
        if (LightClock.charge(lightPacket.kind())) {
            if (list.stream().anyMatch(lightPacket2 -> lightPacket2.owner().equals(lightPacket.owner()) && LightClock.family(lightPacket2.kind()) == LightClock.family(lightPacket.kind()) && (lightPacket2.born() > lightPacket.born() || lightPacket2.born() == lightPacket.born() && !LightClock.charge(lightPacket2.kind())))) {
                return;
            }
            list.removeIf(lightPacket2 -> lightPacket2.owner().equals(lightPacket.owner()) && LightClock.charge(lightPacket2.kind()) && lightPacket2.born() <= lightPacket.born());
        }
        if (lightPacket.kind() == 1 || lightPacket.kind() == 3 || lightPacket.kind() == 5) {
            list.removeIf(lightPacket2 -> lightPacket2.owner().equals(lightPacket.owner()) && LightClock.charge(lightPacket2.kind()) && LightClock.family(lightPacket2.kind()) == LightClock.family(lightPacket.kind()) && lightPacket2.born() <= lightPacket.born());
        }
        if (lightPacket.kind() == 9) {
            list.removeIf(lightPacket2 -> lightPacket2.owner().equals(lightPacket.owner()) && lightPacket2.kind() == 8 && lightPacket2.born() <= lightPacket.born() && lightPacket2.origin().m_82557_(lightPacket.origin()) < 0.01);
        }
        while (list.size() >= 64) {
            list.remove(0);
        }
        list.add(lightPacket);
    }
}

