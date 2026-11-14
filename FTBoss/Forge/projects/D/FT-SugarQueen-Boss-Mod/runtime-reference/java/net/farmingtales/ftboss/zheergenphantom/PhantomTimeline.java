/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.zheergenphantom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.zheergenphantom.PhantomSnapshot;

public final class PhantomTimeline {
    private final List<Entry> pending = new ArrayList<Entry>();
    private long sequence;

    public void schedule(long l, PhantomSnapshot phantomSnapshot) {
        this.pending.add(new Entry(l + (long)PhantomTimeline.readableDelay(phantomSnapshot.delay()), this.sequence++, phantomSnapshot));
        this.pending.sort(Comparator.comparingLong(Entry::due).thenComparingLong(Entry::sequence));
    }

    public List<PhantomSnapshot> poll(long l) {
        ArrayList<PhantomSnapshot> arrayList = new ArrayList<PhantomSnapshot>();
        while (!this.pending.isEmpty() && this.pending.get(0).due() <= l) {
            arrayList.add(this.pending.remove(0).snapshot());
        }
        return arrayList;
    }

    public int size() {
        return this.pending.size();
    }

    public static int readableDelay(int n) {
        return Math.max(20, Math.min(100, n));
    }

    private record Entry(long due, long sequence, PhantomSnapshot snapshot) {
    }
}

