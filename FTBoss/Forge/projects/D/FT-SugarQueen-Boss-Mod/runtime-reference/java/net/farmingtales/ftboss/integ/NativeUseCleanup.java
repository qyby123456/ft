/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.integ;

public abstract class NativeUseCleanup<Token> {
    private final Thread thread = Thread.currentThread();
    private Token token;
    private boolean prepared;
    private boolean captured;
    private boolean closing;
    private boolean ownerCloseAttempted;
    private boolean ownerCloseReturned;
    private boolean nativeRemoveAttempted;
    private boolean nativeRemoveCompleted;
    private Throwable lastOwnerFailure;
    private Throwable lastNativeFailure;

    public final void prepare() {
        if (this.thread != Thread.currentThread() || this.prepared || this.ownerCloseAttempted || this.nativeRemoveAttempted) {
            throw new IllegalStateException("Iron use frame cannot be prepared");
        }
        this.prepared = true;
    }

    public final void capture(Token Token) {
        if (this.thread != Thread.currentThread() || !this.prepared || this.captured || this.ownerCloseAttempted || this.nativeRemoveAttempted) {
            throw new IllegalStateException("Iron use token cannot be recorded");
        }
        this.token = Token;
        this.captured = true;
    }

    public final boolean acceptsAttempt() {
        return this.thread == Thread.currentThread() && this.captured && !this.closing && !this.ownerCloseAttempted && !this.nativeRemoveAttempted;
    }

    public final Snapshot snapshot() {
        if (this.thread != Thread.currentThread()) {
            throw new IllegalStateException("Iron use frame belongs to another thread");
        }
        return new Snapshot(this.prepared, this.captured, this.closing, this.ownerCloseAttempted, this.ownerCloseReturned, this.nativeRemoveAttempted, this.nativeRemoveCompleted, this.lastOwnerFailure, this.lastNativeFailure);
    }

    public final void end() throws Throwable {
        if (this.thread != Thread.currentThread() || this.closing || this.ownerCloseReturned && this.nativeRemoveCompleted) {
            return;
        }
        this.closing = true;
        Throwable throwable = null;
        try {
            block11: {
                if (!this.ownerCloseReturned) {
                    this.ownerCloseAttempted = true;
                    try {
                        this.closeOwner(this.token);
                        this.ownerCloseReturned = true;
                    }
                    catch (Throwable throwable2) {
                        this.lastOwnerFailure = throwable2;
                        throwable = throwable2;
                    }
                }
                if (!this.nativeRemoveCompleted) {
                    this.nativeRemoveAttempted = true;
                    try {
                        this.removeNative();
                        this.nativeRemoveCompleted = true;
                    }
                    catch (Throwable throwable3) {
                        this.lastNativeFailure = throwable3;
                        if (throwable != null) break block11;
                        throw throwable3;
                    }
                }
            }
            if (throwable != null) {
                throw throwable;
            }
        }
        finally {
            this.closing = false;
        }
    }

    protected abstract void closeOwner(Token var1) throws Throwable;

    protected abstract void removeNative() throws Throwable;

    public record Snapshot(boolean prepared, boolean captured, boolean closing, boolean ownerCloseAttempted, boolean ownerCloseReturned, boolean nativeRemoveAttempted, boolean nativeRemoveCompleted, Throwable lastOwnerFailure, Throwable lastNativeFailure) {
    }
}

