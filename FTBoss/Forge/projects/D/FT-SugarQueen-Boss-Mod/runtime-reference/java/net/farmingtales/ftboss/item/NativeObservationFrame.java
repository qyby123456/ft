/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeObservationCarrier;
import net.farmingtales.ftboss.item.NativeObservationState;

public final class NativeObservationFrame {
    private final Object receiver;
    private final Object context;
    private final String site;
    private final FoodWeaponSourceLedger.NativeObservationCut originalCut;
    private volatile NativeObservationState state;
    private volatile boolean ownerBound;
    private final Throwable[] events = new Throwable[16];
    private final int[] phases = new int[16];
    private int eventCount;
    private int overflow;
    private Throwable entry;
    private Throwable returned;
    private Throwable exceptional;
    private Throwable close;
    private Throwable original;
    volatile NativeObservationFrame previous;
    boolean retained;

    private NativeObservationFrame(Object object, Object object2, String string, FoodWeaponSourceLedger.NativeObservationCut nativeObservationCut) {
        this.receiver = object;
        this.context = object2;
        this.site = string;
        this.originalCut = nativeObservationCut;
    }

    private NativeObservationFrame(Object object, Object object2, String string, NativeObservationState nativeObservationState) {
        this(object, object2, string, (FoodWeaponSourceLedger.NativeObservationCut)null);
        this.state = nativeObservationState;
    }

    public static NativeObservationFrame open(Object object, String string) {
        return NativeObservationFrame.open(object, object, string);
    }

    public static NativeObservationFrame open(Object object, Object object2, String string) {
        FoodWeaponSourceLedger.NativeObservationCut nativeObservationCut = null;
        Throwable throwable = null;
        if (object == object2) {
            try {
                nativeObservationCut = FoodWeaponSourceLedger.beginNativeObservation(object);
            }
            catch (Throwable throwable2) {
                throwable = throwable2;
            }
        }
        NativeObservationFrame nativeObservationFrame = new NativeObservationFrame(object, object2, string, nativeObservationCut);
        if (throwable != null) {
            nativeObservationFrame.observationFailed(0, throwable);
        }
        try {
            NativeObservationState nativeObservationState;
            if (object instanceof NativeObservationCarrier) {
                NativeObservationCarrier nativeObservationCarrier = (NativeObservationCarrier)object;
                nativeObservationState = nativeObservationCarrier.ftboss$nativeObservationState();
            } else {
                nativeObservationState = null;
            }
            nativeObservationFrame.state = nativeObservationState;
        }
        catch (Throwable throwable3) {
            nativeObservationFrame.observationFailed(0, throwable3);
        }
        if (object == object2) {
            try {
                FoodWeaponSourceLedger.bindNativeObservationFrame(nativeObservationCut, object, nativeObservationFrame);
                nativeObservationFrame.ownerBound = true;
            }
            catch (Throwable throwable4) {
                nativeObservationFrame.observationFailed(0, throwable4);
            }
        }
        return nativeObservationFrame;
    }

    public void observationFailed(int n, Throwable throwable) {
        if (throwable == null) {
            return;
        }
        try {
            FoodWeaponSourceLedger.nativeObservationFailed(this.originalCut, this.receiver, this.ownerBound ? this : null, n, throwable);
        }
        catch (Throwable throwable2) {
            this.store(3, throwable2);
        }
        this.store(n, throwable);
        this.retain();
    }

    void closeOriginalFailure() {
        try {
            FoodWeaponSourceLedger.nativeObservationFailed(this.originalCut, this.receiver, this.ownerBound ? this : null, 3, null);
        }
        catch (Throwable throwable) {
            this.store(3, throwable);
            this.retain();
        }
    }

    private synchronized void store(int n, Throwable throwable) {
        if (this.eventCount < this.events.length) {
            this.phases[this.eventCount] = n;
            this.events[this.eventCount++] = throwable;
        } else {
            ++this.overflow;
        }
        switch (n) {
            case 0: {
                if (this.entry != null) break;
                this.entry = throwable;
                break;
            }
            case 1: {
                if (this.returned != null) break;
                this.returned = throwable;
                break;
            }
            case 2: {
                if (this.exceptional != null) break;
                this.exceptional = throwable;
                break;
            }
            default: {
                if (this.close != null) break;
                this.close = throwable;
            }
        }
    }

    private void retain() {
        if (this.state == null) {
            return;
        }
        try {
            this.state.retain(this);
        }
        catch (Throwable throwable) {
            this.store(3, throwable);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void originalFailed(Throwable throwable) {
        try {
            FoodWeaponSourceLedger.nativeObservationOriginalFailed(this.originalCut, this.receiver, this.ownerBound ? this : null, throwable);
        }
        catch (Throwable throwable2) {
            this.store(3, throwable2);
        }
        NativeObservationFrame nativeObservationFrame = this;
        synchronized (nativeObservationFrame) {
            if (this.original == null) {
                this.original = throwable;
            }
        }
        this.retain();
    }

    public synchronized Throwable observationFailure(int n) {
        return switch (n) {
            case 0 -> this.entry;
            case 1 -> this.returned;
            case 2 -> this.exceptional;
            case 3 -> this.close;
            default -> null;
        };
    }

    public synchronized Throwable originalFailure() {
        return this.original;
    }

    public synchronized int eventCount() {
        return this.eventCount;
    }

    public synchronized int eventPhase(int n) {
        return n < 0 || n >= this.eventCount ? -1 : this.phases[n];
    }

    public synchronized Throwable eventFailure(int n) {
        return n < 0 || n >= this.eventCount ? null : this.events[n];
    }

    public synchronized int overflowCount() {
        return this.overflow;
    }

    public synchronized boolean failed() {
        return this.entry != null || this.returned != null || this.exceptional != null || this.close != null;
    }

    public synchronized boolean readable() {
        return this.state != null && !this.failed();
    }

    public synchronized Object receiver() {
        return this.receiver;
    }

    public synchronized Object context() {
        return this.context;
    }

    public synchronized String site() {
        return this.site;
    }

    public synchronized NativeObservationFrame previous() {
        return this.previous;
    }
}

