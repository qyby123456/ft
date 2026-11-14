/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.entity.projectile.Arrow
 *  net.minecraft.world.entity.projectile.SpectralArrow
 *  net.minecraft.world.entity.projectile.ThrownTrident
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeObservationFrame;
import net.farmingtales.ftboss.item.NativePaidWeaponCarrier;
import net.farmingtales.ftboss.item.NativeReadCutObserver;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;

public final class NativeVanillaImpact {
    private NativeVanillaImpact() {
    }

    static void bindOwnerPaidWeapon(Entity entity, FoodWeaponSourceLedger.NativePaidWeapon nativePaidWeapon) {
        if (entity != null && (entity.getClass() == Arrow.class || entity.getClass() == SpectralArrow.class || entity instanceof ThrownTrident) && entity instanceof NativePaidWeaponCarrier) {
            NativePaidWeaponCarrier nativePaidWeaponCarrier = (NativePaidWeaponCarrier)entity;
            nativePaidWeaponCarrier.ftboss$bindOwnerPaidWeapon(nativePaidWeapon);
        }
    }

    static FoodWeaponSourceLedger.NativePaidWeapon originalOwnerPaidWeapon(RawImpact rawImpact) {
        FoodWeaponSourceLedger.NativePaidWeapon nativePaidWeapon;
        AbstractArrow abstractArrow;
        boolean bl;
        if (rawImpact == null || rawImpact.thread != Thread.currentThread() || rawImpact.closed || rawImpact.originalEntered || rawImpact.observation == null || !rawImpact.observation.readable() || rawImpact.failed || rawImpact.site == null || rawImpact.carrier == null || rawImpact.target == null || rawImpact.source == null || !Float.isFinite(rawImpact.amount) || rawImpact.amount <= 0.0f || rawImpact.rawAmountBits != Float.floatToRawIntBits(rawImpact.amount)) {
            return null;
        }
        boolean bl2 = rawImpact.site == OriginalSite.TRIDENT ? rawImpact.carrier instanceof ThrownTrident : (bl = rawImpact.carrier.getClass() == Arrow.class || rawImpact.carrier.getClass() == SpectralArrow.class);
        if (bl && (abstractArrow = rawImpact.carrier) instanceof NativePaidWeaponCarrier) {
            NativePaidWeaponCarrier nativePaidWeaponCarrier = (NativePaidWeaponCarrier)abstractArrow;
            nativePaidWeapon = nativePaidWeaponCarrier.ftboss$originalOwnerPaidWeapon();
        } else {
            nativePaidWeapon = null;
        }
        return nativePaidWeapon;
    }

    public static boolean arrowHurt(AbstractArrow abstractArrow, Entity entity, DamageSource damageSource, float f) {
        return NativeVanillaImpact.hurt(OriginalSite.ARROW, abstractArrow, entity, damageSource, f);
    }

    public static boolean tridentHurt(ThrownTrident thrownTrident, Entity entity, DamageSource damageSource, float f) {
        return NativeVanillaImpact.hurt(OriginalSite.TRIDENT, (AbstractArrow)thrownTrident, entity, damageSource, f);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean hurt(OriginalSite originalSite, AbstractArrow abstractArrow, Entity entity, DamageSource damageSource, float f) {
        RawImpact rawImpact = new RawImpact(originalSite, abstractArrow, entity, damageSource, f);
        rawImpact.open();
        try {
            try {
                if (!rawImpact.failed) {
                    rawImpact.scope = FoodWeaponSourceLedger.beginNativeProjectileImpact(rawImpact, rawImpact.originalObservation);
                }
            }
            catch (Throwable throwable) {
                rawImpact.observationFailed(0, throwable);
            }
            boolean bl = rawImpact.invokeOriginal();
            return bl;
        }
        finally {
            try {
                try {
                    FoodWeaponSourceLedger.closeNativeProjectileImpact(rawImpact.scope, rawImpact, rawImpact.originalObservation);
                }
                catch (Throwable throwable) {
                    rawImpact.observationFailed(3, throwable);
                }
            }
            finally {
                rawImpact.close();
            }
        }
    }

    static final class RawImpact {
        final OriginalSite site;
        final AbstractArrow carrier;
        final Entity target;
        final DamageSource source;
        final float amount;
        final int rawAmountBits;
        private final Thread thread = Thread.currentThread();
        private NativeObservationFrame observation;
        private FoodWeaponSourceLedger.NativeProjectileObservation originalObservation;
        private FoodWeaponSourceLedger.NativeProjectileInteraction scope;
        private boolean failed;
        private boolean originalEntered;
        private boolean closed;

        private RawImpact(OriginalSite originalSite, AbstractArrow abstractArrow, Entity entity, DamageSource damageSource, float f) {
            this.site = originalSite;
            this.carrier = abstractArrow;
            this.target = entity;
            this.source = damageSource;
            this.amount = f;
            this.rawAmountBits = Float.floatToRawIntBits(f);
        }

        private void open() {
            try {
                this.originalObservation = FoodWeaponSourceLedger.observeNativeProjectileImpact(this);
            }
            catch (Throwable throwable) {
                this.observationFailed(0, throwable);
            }
            try {
                this.observation = NativeObservationFrame.open(this.carrier, this, "NativeVanillaImpact.RawImpact");
            }
            catch (Throwable throwable) {
                this.observationFailed(0, throwable);
            }
            if (this.observation != null && this.observation.failed()) {
                for (int i = 0; i <= 3; ++i) {
                    Throwable throwable = this.observation.observationFailure(i);
                    if (throwable == null) continue;
                    this.observationFailed(i, throwable);
                }
            }
        }

        boolean originalEntered() {
            return this.originalEntered;
        }

        boolean closed() {
            return this.closed;
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        private boolean invokeOriginal() {
            boolean bl;
            if (this.closed) throw new IllegalStateException("Native raw impact already entered or closed");
            if (this.originalEntered) {
                throw new IllegalStateException("Native raw impact already entered or closed");
            }
            try {
                NativeReadCutObserver.source(this.source);
            }
            catch (Throwable throwable) {
                this.observationFailed(0, throwable);
            }
            this.originalEntered = true;
            Throwable throwable = null;
            try {
                bl = this.target.m_6469_(this.source, this.amount);
            }
            catch (Throwable throwable3) {
                try {
                    throwable = throwable3;
                    try {
                        FoodWeaponSourceLedger.nativeProjectileOriginalFailed(this.originalObservation, this, throwable3);
                    }
                    catch (Throwable throwable4) {
                        this.observationFailed(2, throwable4);
                    }
                    if (this.observation == null) throw throwable3;
                    this.observation.originalFailed(throwable3);
                    throw throwable3;
                }
                catch (Throwable throwable5) {
                    try {
                        NativeReadCutObserver.source(this.source);
                        throw throwable5;
                    }
                    catch (Throwable throwable6) {
                        this.observationFailed(throwable == null ? 1 : 2, throwable6);
                    }
                    throw throwable5;
                }
            }
            try {
                NativeReadCutObserver.source(this.source);
                return bl;
            }
            catch (Throwable throwable2) {
                this.observationFailed(throwable == null ? 1 : 2, throwable2);
            }
            return bl;
        }

        private void observationFailed(int n, Throwable throwable) {
            block3: {
                this.failed = true;
                try {
                    FoodWeaponSourceLedger.nativeProjectileObservationFailed(this.originalObservation, this, this.scope, this.observation, n, throwable);
                }
                catch (Throwable throwable2) {
                    if (this.observation == null) break block3;
                    this.observation.observationFailed(3, throwable2);
                }
            }
            if (this.observation != null) {
                this.observation.observationFailed(n, throwable);
            }
        }

        private void close() {
            this.closed = true;
        }
    }

    static enum OriginalSite {
        ARROW,
        TRIDENT;

    }
}

