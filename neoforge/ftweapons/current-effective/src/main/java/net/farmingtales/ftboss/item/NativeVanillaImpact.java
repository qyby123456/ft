package net.farmingtales.ftboss.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;

public final class NativeVanillaImpact {
    private NativeVanillaImpact() {
    }

    static void bindOwnerPaidWeapon(Entity var0, FoodWeaponSourceLedger.NativePaidWeapon var1) {
        if (var0 != null
            && (var0.getClass() == Arrow.class || var0.getClass() == SpectralArrow.class || var0 instanceof ThrownTrident)
            && var0 instanceof NativePaidWeaponCarrier var2) {
            var2.ftboss$bindOwnerPaidWeapon(var1);
        }
    }

    static FoodWeaponSourceLedger.NativePaidWeapon originalOwnerPaidWeapon(NativeVanillaImpact.RawImpact var0) {
        if (var0 != null
            && var0.thread == Thread.currentThread()
            && !var0.closed
            && !var0.originalEntered
            && var0.observation != null
            && var0.observation.readable()
            && !var0.failed
            && var0.site != null
            && var0.carrier != null
            && var0.target != null
            && var0.source != null
            && Float.isFinite(var0.amount)
            && !(var0.amount <= 0.0F)
            && var0.rawAmountBits == Float.floatToRawIntBits(var0.amount)) {
            boolean var1 = var0.site == NativeVanillaImpact.OriginalSite.TRIDENT
                ? var0.carrier instanceof ThrownTrident
                : var0.carrier.getClass() == Arrow.class || var0.carrier.getClass() == SpectralArrow.class;
            return var1 && var0.carrier instanceof NativePaidWeaponCarrier var2 ? var2.ftboss$originalOwnerPaidWeapon() : null;
        } else {
            return null;
        }
    }

    public static boolean arrowHurt(AbstractArrow var0, Entity var1, DamageSource var2, float var3) {
        return hurt(NativeVanillaImpact.OriginalSite.ARROW, var0, var1, var2, var3);
    }

    public static boolean tridentHurt(ThrownTrident var0, Entity var1, DamageSource var2, float var3) {
        return hurt(NativeVanillaImpact.OriginalSite.TRIDENT, var0, var1, var2, var3);
    }

    private static boolean hurt(NativeVanillaImpact.OriginalSite var0, AbstractArrow var1, Entity var2, DamageSource var3, float var4) {
        NativeVanillaImpact.RawImpact var5 = new NativeVanillaImpact.RawImpact(var0, var1, var2, var3, var4);
        var5.open();

        boolean var6;
        try {
            try {
                if (!var5.failed) {
                    var5.scope = FoodWeaponSourceLedger.beginNativeProjectileImpact(var5, var5.originalObservation);
                }
            } catch (Throwable var32) {
                var5.observationFailed(0, var32);
            }

            var6 = var5.invokeOriginal();
        } finally {
            try {
                FoodWeaponSourceLedger.closeNativeProjectileImpact(var5.scope, var5, var5.originalObservation);
            } catch (Throwable var30) {
                var5.observationFailed(3, var30);
            } finally {
                var5.close();
            }
        }

        return var6;
    }

    static enum OriginalSite {
        ARROW,
        TRIDENT;
    }

    static final class RawImpact {
        final NativeVanillaImpact.OriginalSite site;
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

        private RawImpact(NativeVanillaImpact.OriginalSite var1, AbstractArrow var2, Entity var3, DamageSource var4, float var5) {
            this.site = var1;
            this.carrier = var2;
            this.target = var3;
            this.source = var4;
            this.amount = var5;
            this.rawAmountBits = Float.floatToRawIntBits(var5);
        }

        private void open() {
            try {
                this.originalObservation = FoodWeaponSourceLedger.observeNativeProjectileImpact(this);
            } catch (Throwable var4) {
                this.observationFailed(0, var4);
            }

            try {
                this.observation = NativeObservationFrame.open(this.carrier, this, "NativeVanillaImpact.RawImpact");
            } catch (Throwable var3) {
                this.observationFailed(0, var3);
            }

            if (this.observation != null && this.observation.failed()) {
                for (int var1 = 0; var1 <= 3; var1++) {
                    Throwable var2 = this.observation.observationFailure(var1);
                    if (var2 != null) {
                        this.observationFailed(var1, var2);
                    }
                }
            }
        }

        boolean originalEntered() {
            return this.originalEntered;
        }

        boolean closed() {
            return this.closed;
        }

        private boolean invokeOriginal() {
            if (!this.closed && !this.originalEntered) {
                try {
                    NativeReadCutObserver.source(this.source);
                } catch (Throwable var14) {
                    this.observationFailed(0, var14);
                }

                this.originalEntered = true;
                Throwable var1 = null;

                boolean var17;
                try {
                    var17 = this.target.hurt(this.source, this.amount);
                } catch (Throwable var13) {
                    Throwable var2 = var13;
                    var1 = var13;

                    try {
                        FoodWeaponSourceLedger.nativeProjectileOriginalFailed(this.originalObservation, this, var2);
                    } catch (Throwable var12) {
                        this.observationFailed(2, var12);
                    }

                    if (this.observation != null) {
                        this.observation.originalFailed(var13);
                    }

                    throw var13;
                } finally {
                    try {
                        NativeReadCutObserver.source(this.source);
                    } catch (Throwable var15) {
                        this.observationFailed(var1 == null ? 1 : 2, var15);
                    }
                }

                return var17;
            } else {
                throw new IllegalStateException("Native raw impact already entered or closed");
            }
        }

        private void observationFailed(int var1, Throwable var2) {
            this.failed = true;

            try {
                FoodWeaponSourceLedger.nativeProjectileObservationFailed(this.originalObservation, this, this.scope, this.observation, var1, var2);
            } catch (Throwable var4) {
                if (this.observation != null) {
                    this.observation.observationFailed(3, var4);
                }
            }

            if (this.observation != null) {
                this.observation.observationFailed(var1, var2);
            }
        }

        private void close() {
            this.closed = true;
        }
    }
}
