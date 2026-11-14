package net.farmingtales.ftboss.item;

public final class NativeObservationState {
    private NativeObservationFrame latest;
    private boolean failed;

    synchronized void retain(NativeObservationFrame var1) {
        if (!var1.retained) {
            var1.previous = this.latest;
            var1.retained = true;
            this.latest = var1;
        }

        this.failed = this.failed | var1.failed();
    }

    public synchronized boolean failed() {
        return this.failed;
    }

    public synchronized NativeObservationFrame latest() {
        return this.latest;
    }
}
