/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.NativeObservationFrame;

public final class NativeObservationState {
    private NativeObservationFrame latest;
    private boolean failed;

    synchronized void retain(NativeObservationFrame nativeObservationFrame) {
        if (!nativeObservationFrame.retained) {
            nativeObservationFrame.previous = this.latest;
            nativeObservationFrame.retained = true;
            this.latest = nativeObservationFrame;
        }
        this.failed |= nativeObservationFrame.failed();
    }

    public synchronized boolean failed() {
        return this.failed;
    }

    public synchronized NativeObservationFrame latest() {
        return this.latest;
    }
}

