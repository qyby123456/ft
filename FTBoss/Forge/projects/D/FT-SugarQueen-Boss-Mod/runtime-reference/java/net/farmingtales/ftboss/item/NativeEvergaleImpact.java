/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.AbstractArrow
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.NativeEvergaleBridge;
import net.farmingtales.ftboss.item.NativeEvergaleCarrier;
import net.farmingtales.ftboss.item.NativeObservationFrame;
import net.farmingtales.ftboss.item.NativeReadCutObserver;
import net.farmingtales.ftboss.item.NativeVanillaImpact;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;

public final class NativeEvergaleImpact {
    private NativeEvergaleImpact() {
    }

    public static boolean arrowHurt(AbstractArrow abstractArrow, Entity entity, DamageSource damageSource, float f) {
        boolean bl = false;
        try {
            bl = NativeEvergaleBridge.isAirStrike(abstractArrow);
        }
        catch (Throwable throwable) {
            NativeEvergaleBridge.observationFailed(null, throwable);
        }
        if (!bl) {
            return NativeVanillaImpact.arrowHurt(abstractArrow, entity, damageSource, f);
        }
        return NativeEvergaleImpact.hurt(abstractArrow, entity, damageSource, f);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static boolean hurt(AbstractArrow abstractArrow, Entity entity, DamageSource damageSource, float f) {
        boolean bl2;
        Object object;
        RawImpact rawImpact = null;
        try {
            rawImpact = new RawImpact(abstractArrow, entity, damageSource, f);
            if (abstractArrow instanceof NativeEvergaleCarrier) {
                object = (NativeEvergaleCarrier)abstractArrow;
                rawImpact.originalCarry = object.ftboss$originalEvergaleCarry();
            }
            rawImpact.observation = NativeObservationFrame.open(abstractArrow, rawImpact, "NativeEvergaleImpact.RawImpact");
            if (rawImpact.observation != null && rawImpact.observation.failed()) {
                for (int i = 0; i <= 3; ++i) {
                    Throwable throwable = rawImpact.observation.observationFailure(i);
                    if (throwable == null) continue;
                    NativeEvergaleImpact.observationFailed(rawImpact, i, throwable);
                }
            }
        }
        catch (Throwable throwable) {
            NativeEvergaleImpact.observationFailed(rawImpact, 0, throwable);
        }
        try {
            NativeReadCutObserver.source(damageSource);
        }
        catch (Throwable throwable) {
            NativeEvergaleImpact.observationFailed(rawImpact, 0, throwable);
        }
        if (rawImpact != null) {
            rawImpact.originalEntered = true;
        }
        object = null;
        try {
            boolean bl = entity.m_6469_(damageSource, f);
            if (rawImpact != null) {
                rawImpact.originalReturn = bl;
                rawImpact.originalReturned = true;
            }
            bl2 = bl;
        }
        catch (Throwable throwable) {
            try {
                object = throwable;
                if (rawImpact == null) throw throwable;
                rawImpact.originalFailure = throwable;
                try {
                    if (rawImpact.observation == null) throw throwable;
                    rawImpact.observation.originalFailed(throwable);
                    throw throwable;
                }
                catch (Throwable throwable2) {
                    NativeEvergaleImpact.observationFailed(rawImpact, 2, throwable2);
                }
                throw throwable;
            }
            catch (Throwable throwable3) {
                try {
                    NativeReadCutObserver.source(damageSource);
                }
                catch (Throwable throwable4) {
                    NativeEvergaleImpact.observationFailed(rawImpact, object == null ? 1 : 2, throwable4);
                }
                if (rawImpact == null) throw throwable3;
                rawImpact.closed = true;
                throw throwable3;
            }
        }
        try {
            NativeReadCutObserver.source(damageSource);
        }
        catch (Throwable throwable) {
            NativeEvergaleImpact.observationFailed(rawImpact, object == null ? 1 : 2, throwable);
        }
        if (rawImpact == null) return bl2;
        rawImpact.closed = true;
        return bl2;
    }

    private static void observationFailed(RawImpact rawImpact, int n, Throwable throwable) {
        NativeEvergaleBridge.observationFailed(rawImpact == null || rawImpact.originalCarry == null ? null : rawImpact.originalCarry.action, throwable);
        if (rawImpact == null) {
            return;
        }
        if (rawImpact.observationFailure == null) {
            rawImpact.observationFailure = throwable;
            rawImpact.observationFailurePhase = n;
        }
        try {
            if (rawImpact.observation != null) {
                rawImpact.observation.observationFailed(n, throwable);
            }
        }
        catch (Throwable throwable2) {
            NativeEvergaleBridge.observationFailed(null, throwable2);
        }
    }

    public static final class RawImpact {
        final AbstractArrow originalCarrier;
        final Entity originalTarget;
        final DamageSource originalSource;
        final float originalAmount;
        final int originalAmountBits;
        final Thread originalThread = Thread.currentThread();
        NativeEvergaleBridge.RawCarry originalCarry;
        NativeObservationFrame observation;
        boolean originalEntered;
        boolean originalReturned;
        boolean originalReturn;
        Throwable originalFailure;
        Throwable observationFailure;
        int observationFailurePhase = -1;
        boolean closed;

        private RawImpact(AbstractArrow abstractArrow, Entity entity, DamageSource damageSource, float f) {
            this.originalCarrier = abstractArrow;
            this.originalTarget = entity;
            this.originalSource = damageSource;
            this.originalAmount = f;
            this.originalAmountBits = Float.floatToRawIntBits(f);
        }
    }
}

