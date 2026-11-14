package net.farmingtales.ftboss.item;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

final class OwnerStatueTaskWrites {
    private OwnerStatueTaskWrites() {
    }

    private static void write(ItemStack var0, CompoundTag var1, String var2, Supplier<Tag> var3, Runnable var4) {
        NativeFoodWeaponWear.beforeNativeRepairWrite(var0, var1, var2, var3.get());
        OwnerStatueTaskOrigin.Write var5 = null;

        try {
            var5 = OwnerStatueTaskOrigin.beforeWrite(var0, var1, var2, (Tag)var3.get(), false);
        } catch (Throwable var18) {
            OwnerStatueTaskOrigin.failedWrite(var0);
        }

        Throwable var6 = null;

        try {
            var4.run();
        } catch (Error | RuntimeException var16) {
            var6 = var16;
            throw var16;
        } finally {
            NativeFoodWeaponWear.afterNativeRepairWrite(var0, var6);
            try {
                OwnerStatueTaskOrigin.afterWrite(var5, var6);
            } catch (Throwable var15) {
                OwnerStatueTaskOrigin.failedWrite(var0);
            }
        }
    }

    static boolean hasWeaponIdentity(ItemStack var0, CompoundTag var1) {
        OwnerRepairContinuation.IdentityRead var2 = null;

        try {
            var2 = OwnerRepairContinuation.identityRead(var0, var1);
        } catch (Throwable var22) {
        }

        OwnerStatueTaskOrigin.ProgressRead var3 = null;

        try {
            var3 = OwnerStatueTaskOrigin.identityRead(var0, var1);
        } catch (Throwable var21) {
            OwnerStatueTaskOrigin.failedWrite(var0);
        }

        Throwable var4 = null;

        boolean var5;
        try {
            var5 = var1.hasUUID("Weapon");
        } catch (Error | RuntimeException var19) {
            var4 = var19;
            throw var19;
        } finally {
            try {
                OwnerRepairContinuation.identityReturned(var2, var4);
            } catch (Throwable var18) {
            }

            try {
                OwnerStatueTaskOrigin.identityReturned(var3, var4);
            } catch (Throwable var17) {
                OwnerStatueTaskOrigin.failedWrite(var0);
            }
        }

        return var5;
    }

    static UUID weaponIdentity(ItemStack var0, CompoundTag var1) {
        OwnerRepairContinuation.IdentityRead var2 = null;

        try {
            var2 = OwnerRepairContinuation.identityRead(var0, var1);
        } catch (Throwable var22) {
        }

        OwnerStatueTaskOrigin.ProgressRead var3 = null;

        try {
            var3 = OwnerStatueTaskOrigin.identityRead(var0, var1);
        } catch (Throwable var21) {
            OwnerStatueTaskOrigin.failedWrite(var0);
        }

        Throwable var4 = null;

        UUID var5;
        try {
            var5 = var1.getUUID("Weapon");
        } catch (Error | RuntimeException var19) {
            var4 = var19;
            throw var19;
        } finally {
            try {
                OwnerRepairContinuation.identityReturned(var2, var4);
            } catch (Throwable var18) {
            }

            try {
                OwnerStatueTaskOrigin.identityReturned(var3, var4);
            } catch (Throwable var17) {
                OwnerStatueTaskOrigin.failedWrite(var0);
            }
        }

        return var5;
    }

    static int[] progress(ItemStack var0, CompoundTag var1) {
        if (NativeFoodWeaponWear.eligible(var0)) return NativeRepairTaskData.progressCopy(var0, var1);
        OwnerStatueTaskOrigin.ProgressRead var2 = null;

        try {
            var2 = OwnerStatueTaskOrigin.progressRead(var0, var1);
        } catch (Throwable var17) {
            OwnerStatueTaskOrigin.failedWrite(var0);
        }

        int[] var3 = null;
        Throwable var4 = null;

        int[] var5;
        try {
            var3 = var1.getIntArray("Progress");
            var5 = var3;
        } catch (Error | RuntimeException var15) {
            var4 = var15;
            throw var15;
        } finally {
            try {
                OwnerStatueTaskOrigin.progressReturned(var2, var3, var4);
            } catch (Throwable var14) {
                OwnerStatueTaskOrigin.failedWrite(var0);
            }
        }

        return var5;
    }

    static void advanceProgress(ItemStack var0, CompoundTag var1, int[] var2, int var3, int var4) {
        int[] planned = var2.clone();
        planned[var3] = Math.addExact(planned[var3], var4);
        NativeFoodWeaponWear.beforeNativeRepairWrite(var0, var1, "ProgressArray", new IntArrayTag(planned));
        OwnerStatueTaskOrigin.Write var5 = null;

        try {
            var5 = OwnerStatueTaskOrigin.beforeArrayWrite(var0, var1, var2, var3, var4);
        } catch (Throwable var18) {
            OwnerStatueTaskOrigin.failedWrite(var0);
        }

        Throwable var6 = null;

        try {
            var2[var3] += var4;
        } catch (Error | RuntimeException var16) {
            var6 = var16;
            throw var16;
        } finally {
            NativeFoodWeaponWear.afterNativeRepairWrite(var0, var6);
            try {
                OwnerStatueTaskOrigin.afterWrite(var5, var6);
            } catch (Throwable var15) {
                OwnerStatueTaskOrigin.failedWrite(var0);
            }
        }
    }

    static void put(ItemStack var0, CompoundTag var1, String var2, Tag var3) {
        write(var0, var1, var2, () -> var3, () -> var1.put(var2, var3));
    }

    static void putInt(ItemStack var0, CompoundTag var1, String var2, int var3) {
        write(var0, var1, var2, () -> IntTag.valueOf(var3), () -> var1.putInt(var2, var3));
    }

    static void putIntArray(ItemStack var0, CompoundTag var1, String var2, int[] var3) {
        write(var0, var1, var2, () -> new IntArrayTag((int[])var3.clone()), () -> var1.putIntArray(var2, var3));
    }

    static void putDouble(ItemStack var0, CompoundTag var1, String var2, double var3) {
        write(var0, var1, var2, () -> DoubleTag.valueOf(var3), () -> var1.putDouble(var2, var3));
    }

    static void remove(ItemStack var0, CompoundTag var1, String var2) {
        write(var0, var1, var2, () -> null, () -> var1.remove(var2));
    }
}
