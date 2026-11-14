/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.item;

import java.util.Objects;
import java.util.UUID;

public final class NativePaidCarrierState<Token> {
    private Token original;
    private UUID originalOwner;
    private Thread thread;
    private boolean bound;
    private boolean retired;

    public void bind(Token Token, UUID uUID) {
        if (this.retired || Token == null) {
            return;
        }
        if (this.bound && this.thread != Thread.currentThread()) {
            return;
        }
        if (this.bound) {
            if (this.original != Token || !Objects.equals(this.originalOwner, uUID)) {
                this.retire();
            }
            return;
        }
        if (uUID == null) {
            return;
        }
        this.original = Token;
        this.originalOwner = uUID;
        this.thread = Thread.currentThread();
        this.bound = true;
    }

    public Token original(UUID uUID) {
        if (!this.bound || this.retired || this.thread != Thread.currentThread()) {
            return null;
        }
        if (!Objects.equals(this.originalOwner, uUID)) {
            this.retire();
            return null;
        }
        return this.original;
    }

    public void retire() {
        this.retired = true;
        this.original = null;
        this.originalOwner = null;
    }
}

