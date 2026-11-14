package net.farmingtales.ftboss.zheergenphantom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PhantomTimeline {
    private final List<PhantomTimeline.Entry> pending = new ArrayList<>();
    private long sequence;

    public void schedule(long var1, PhantomSnapshot var3) {
        this.pending.add(new PhantomTimeline.Entry(var1 + (long)readableDelay(var3.delay()), this.sequence++, var3));
        this.pending.sort(Comparator.comparingLong(PhantomTimeline.Entry::due).thenComparingLong(PhantomTimeline.Entry::sequence));
    }

    public List<PhantomSnapshot> poll(long var1) {
        List<PhantomSnapshot> var3 = new ArrayList<>();

        while (!this.pending.isEmpty() && this.pending.get(0).due() <= var1) {
            var3.add(this.pending.remove(0).snapshot());
        }

        return var3;
    }

    public int size() {
        return this.pending.size();
    }

    public static int readableDelay(int var0) {
        return Math.max(20, Math.min(100, var0));
    }

    private static record Entry(long due, long sequence, PhantomSnapshot snapshot) {
    }
}

