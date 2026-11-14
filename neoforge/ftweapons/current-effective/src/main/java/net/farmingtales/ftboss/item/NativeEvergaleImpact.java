package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.NativeEvergaleBridge.RawCarry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;

public final class NativeEvergaleImpact {
    private NativeEvergaleImpact() {
    }

    public static boolean arrowHurt(AbstractArrow var0, Entity var1, DamageSource var2, float var3) {
        boolean var4 = false;

        try {
            var4 = NativeEvergaleBridge.isAirStrike(var0);
        } catch (Throwable var6) {
            NativeEvergaleBridge.observationFailed(null, var6);
        }

        return !var4 ? NativeVanillaImpact.arrowHurt(var0, var1, var2, var3) : hurt(var0, var1, var2, var3);
    }

    private static boolean hurt(AbstractArrow var0, Entity var1, DamageSource var2, float var3) {
        NativeEvergaleImpact.RawImpact var4 = null;

        try {
            var4 = new NativeEvergaleImpact.RawImpact(var0, var1, var2, var3);
            if (var0 instanceof NativeEvergaleCarrier var5) {
                var4.originalCarry = var5.ftboss$originalEvergaleCarry();
            }

            var4.observation = NativeObservationFrame.open(var0, var4, "NativeEvergaleImpact.RawImpact");
            if (var4.observation != null && var4.observation.failed()) {
                for (int var24 = 0; var24 <= 3; var24++) {
                    Throwable var6 = var4.observation.observationFailure(var24);
                    if (var6 != null) {
                        observationFailed(var4, var24, var6);
                    }
                }
            }
        } catch (Throwable var23) {
            observationFailed(var4, 0, var23);
        }

        try {
            NativeReadCutObserver.source(var2);
        } catch (Throwable var19) {
            observationFailed(var4, 0, var19);
        }

        if (var4 != null) {
            var4.originalEntered = true;
        }

        Throwable var25 = null;

        boolean var7;
        try {
            boolean var27 = var1.hurt(var2, var3);
            if (var4 != null) {
                var4.originalReturn = var27;
                var4.originalReturned = true;
            }

            var7 = var27;
        } catch (Throwable var21) {
            Throwable var26 = var21;
            var25 = var21;
            if (var4 != null) {
                var4.originalFailure = var21;

                try {
                    if (var4.observation != null) {
                        var4.observation.originalFailed(var26);
                    }
                } catch (Throwable var18) {
                    observationFailed(var4, 2, var18);
                }
            }

            throw var21;
        } finally {
            try {
                NativeReadCutObserver.source(var2);
            } catch (Throwable var20) {
                observationFailed(var4, var25 == null ? 1 : 2, var20);
            }

            if (var4 != null) {
                var4.closed = true;
            }
        }

        return var7;
    }

    private static void observationFailed(NativeEvergaleImpact.RawImpact var0, int var1, Throwable var2) {
        NativeEvergaleBridge.observationFailed(var0 != null && var0.originalCarry != null ? var0.originalCarry.action : null, var2);
        if (var0 != null) {
            if (var0.observationFailure == null) {
                var0.observationFailure = var2;
                var0.observationFailurePhase = var1;
            }

            try {
                if (var0.observation != null) {
                    var0.observation.observationFailed(var1, var2);
                }
            } catch (Throwable var4) {
                NativeEvergaleBridge.observationFailed(null, var4);
            }
        }
    }

    public static final class RawImpact {
        final AbstractArrow originalCarrier;
        final Entity originalTarget;
        final DamageSource originalSource;
        final float originalAmount;
        final int originalAmountBits;
        final Thread originalThread = Thread.currentThread();
        RawCarry originalCarry;
        NativeObservationFrame observation;
        boolean originalEntered;
        boolean originalReturned;
        boolean originalReturn;
        Throwable originalFailure;
        Throwable observationFailure;
        int observationFailurePhase = -1;
        boolean closed;

        private RawImpact(AbstractArrow var1, Entity var2, DamageSource var3, float var4) {
            this.originalCarrier = var1;
            this.originalTarget = var2;
            this.originalSource = var3;
            this.originalAmount = var4;
            this.originalAmountBits = Float.floatToRawIntBits(var4);
        }
    }
}
