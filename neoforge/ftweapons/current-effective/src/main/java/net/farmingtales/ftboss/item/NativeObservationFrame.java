package net.farmingtales.ftboss.item;

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

    private NativeObservationFrame(Object var1, Object var2, String var3, FoodWeaponSourceLedger.NativeObservationCut var4) {
        this.receiver = var1;
        this.context = var2;
        this.site = var3;
        this.originalCut = var4;
    }

    private NativeObservationFrame(Object var1, Object var2, String var3, NativeObservationState var4) {
        this(var1, var2, var3, (FoodWeaponSourceLedger.NativeObservationCut)null);
        this.state = var4;
    }

    public static NativeObservationFrame open(Object var0, String var1) {
        return open(var0, var0, var1);
    }

    public static NativeObservationFrame open(Object var0, Object var1, String var2) {
        FoodWeaponSourceLedger.NativeObservationCut var3 = null;
        Throwable var4 = null;
        if (var0 == var1) {
            try {
                var3 = FoodWeaponSourceLedger.beginNativeObservation(var0);
            } catch (Throwable var9) {
                var4 = var9;
            }
        }

        NativeObservationFrame var5 = new NativeObservationFrame(var0, var1, var2, var3);
        if (var4 != null) {
            var5.observationFailed(0, var4);
        }

        try {
            var5.state = var0 instanceof NativeObservationCarrier var6 ? var6.ftboss$nativeObservationState() : null;
        } catch (Throwable var8) {
            var5.observationFailed(0, var8);
        }

        if (var0 == var1) {
            try {
                FoodWeaponSourceLedger.bindNativeObservationFrame(var3, var0, var5);
                var5.ownerBound = true;
            } catch (Throwable var7) {
                var5.observationFailed(0, var7);
            }
        }

        return var5;
    }

    public void observationFailed(int var1, Throwable var2) {
        if (var2 != null) {
            try {
                FoodWeaponSourceLedger.nativeObservationFailed(this.originalCut, this.receiver, this.ownerBound ? this : null, var1, var2);
            } catch (Throwable var4) {
                this.store(3, var4);
            }

            this.store(var1, var2);
            this.retain();
        }
    }

    void closeOriginalFailure() {
        try {
            FoodWeaponSourceLedger.nativeObservationFailed(this.originalCut, this.receiver, this.ownerBound ? this : null, 3, null);
        } catch (Throwable var2) {
            this.store(3, var2);
            this.retain();
        }
    }

    private synchronized void store(int var1, Throwable var2) {
        if (this.eventCount < this.events.length) {
            this.phases[this.eventCount] = var1;
            this.events[this.eventCount++] = var2;
        } else {
            this.overflow++;
        }

        switch (var1) {
            case 0:
                if (this.entry == null) {
                    this.entry = var2;
                }
                break;
            case 1:
                if (this.returned == null) {
                    this.returned = var2;
                }
                break;
            case 2:
                if (this.exceptional == null) {
                    this.exceptional = var2;
                }
                break;
            default:
                if (this.close == null) {
                    this.close = var2;
                }
        }
    }

    private void retain() {
        if (this.state != null) {
            try {
                this.state.retain(this);
            } catch (Throwable var2) {
                this.store(3, var2);
            }
        }
    }

    public void originalFailed(Throwable var1) {
        try {
            FoodWeaponSourceLedger.nativeObservationOriginalFailed(this.originalCut, this.receiver, this.ownerBound ? this : null, var1);
        } catch (Throwable var4) {
            this.store(3, var4);
        }

        synchronized (this) {
            if (this.original == null) {
                this.original = var1;
            }
        }

        this.retain();
    }

    public synchronized Throwable observationFailure(int var1) {
        return switch (var1) {
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

    public synchronized int eventPhase(int var1) {
        return var1 >= 0 && var1 < this.eventCount ? this.phases[var1] : -1;
    }

    public synchronized Throwable eventFailure(int var1) {
        return var1 >= 0 && var1 < this.eventCount ? this.events[var1] : null;
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
