/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.DoubleTag
 *  net.minecraft.nbt.IntArrayTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.item;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.item.OwnerRepairContinuation;
import net.farmingtales.ftboss.item.OwnerStatueTaskOrigin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

final class OwnerStatueTaskWrites {
    private OwnerStatueTaskWrites() {
    }

    private static void write(ItemStack itemStack, CompoundTag compoundTag, String string, Supplier<Tag> supplier, Runnable runnable) {
        OwnerStatueTaskOrigin.Write write = null;
        try {
            write = OwnerStatueTaskOrigin.beforeWrite(itemStack, compoundTag, string, supplier.get(), false);
        }
        catch (Throwable throwable) {
            OwnerStatueTaskOrigin.failedWrite(itemStack);
        }
        Throwable throwable = null;
        try {
            runnable.run();
        }
        catch (Error | RuntimeException throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            try {
                OwnerStatueTaskOrigin.afterWrite(write, throwable);
            }
            catch (Throwable throwable3) {
                OwnerStatueTaskOrigin.failedWrite(itemStack);
            }
        }
    }

    static boolean hasWeaponIdentity(ItemStack itemStack, CompoundTag compoundTag) {
        OwnerRepairContinuation.IdentityRead identityRead = null;
        try {
            identityRead = OwnerRepairContinuation.identityRead(itemStack, compoundTag);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        OwnerStatueTaskOrigin.ProgressRead progressRead = null;
        try {
            progressRead = OwnerStatueTaskOrigin.identityRead(itemStack, compoundTag);
        }
        catch (Throwable throwable) {
            OwnerStatueTaskOrigin.failedWrite(itemStack);
        }
        Throwable throwable = null;
        try {
            boolean bl = compoundTag.m_128403_("Weapon");
            return bl;
        }
        catch (Error | RuntimeException throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            try {
                OwnerRepairContinuation.identityReturned(identityRead, throwable);
            }
            catch (Throwable throwable3) {}
            try {
                OwnerStatueTaskOrigin.identityReturned(progressRead, throwable);
            }
            catch (Throwable throwable4) {
                OwnerStatueTaskOrigin.failedWrite(itemStack);
            }
        }
    }

    static UUID weaponIdentity(ItemStack itemStack, CompoundTag compoundTag) {
        OwnerRepairContinuation.IdentityRead identityRead = null;
        try {
            identityRead = OwnerRepairContinuation.identityRead(itemStack, compoundTag);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        OwnerStatueTaskOrigin.ProgressRead progressRead = null;
        try {
            progressRead = OwnerStatueTaskOrigin.identityRead(itemStack, compoundTag);
        }
        catch (Throwable throwable) {
            OwnerStatueTaskOrigin.failedWrite(itemStack);
        }
        Throwable throwable = null;
        try {
            UUID uUID = compoundTag.m_128342_("Weapon");
            return uUID;
        }
        catch (Error | RuntimeException throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            try {
                OwnerRepairContinuation.identityReturned(identityRead, throwable);
            }
            catch (Throwable throwable3) {}
            try {
                OwnerStatueTaskOrigin.identityReturned(progressRead, throwable);
            }
            catch (Throwable throwable4) {
                OwnerStatueTaskOrigin.failedWrite(itemStack);
            }
        }
    }

    static int[] progress(ItemStack itemStack, CompoundTag compoundTag) {
        OwnerStatueTaskOrigin.ProgressRead progressRead = null;
        try {
            progressRead = OwnerStatueTaskOrigin.progressRead(itemStack, compoundTag);
        }
        catch (Throwable throwable) {
            OwnerStatueTaskOrigin.failedWrite(itemStack);
        }
        int[] nArray = null;
        Throwable throwable = null;
        try {
            int[] nArray2 = nArray = compoundTag.m_128465_("Progress");
            return nArray2;
        }
        catch (Error | RuntimeException throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            try {
                OwnerStatueTaskOrigin.progressReturned(progressRead, nArray, throwable);
            }
            catch (Throwable throwable3) {
                OwnerStatueTaskOrigin.failedWrite(itemStack);
            }
        }
    }

    static void advanceProgress(ItemStack itemStack, CompoundTag compoundTag, int[] nArray, int n, int n2) {
        OwnerStatueTaskOrigin.Write write = null;
        try {
            write = OwnerStatueTaskOrigin.beforeArrayWrite(itemStack, compoundTag, nArray, n, n2);
        }
        catch (Throwable throwable) {
            OwnerStatueTaskOrigin.failedWrite(itemStack);
        }
        Throwable throwable = null;
        try {
            int n3 = n;
            nArray[n3] = nArray[n3] + n2;
        }
        catch (Error | RuntimeException throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            try {
                OwnerStatueTaskOrigin.afterWrite(write, throwable);
            }
            catch (Throwable throwable3) {
                OwnerStatueTaskOrigin.failedWrite(itemStack);
            }
        }
    }

    static void put(ItemStack itemStack, CompoundTag compoundTag, String string, Tag tag) {
        OwnerStatueTaskWrites.write(itemStack, compoundTag, string, () -> tag, () -> compoundTag.m_128365_(string, tag));
    }

    static void putInt(ItemStack itemStack, CompoundTag compoundTag, String string, int n) {
        OwnerStatueTaskWrites.write(itemStack, compoundTag, string, () -> IntTag.m_128679_((int)n), () -> compoundTag.m_128405_(string, n));
    }

    static void putIntArray(ItemStack itemStack, CompoundTag compoundTag, String string, int[] nArray) {
        OwnerStatueTaskWrites.write(itemStack, compoundTag, string, () -> new IntArrayTag((int[])nArray.clone()), () -> compoundTag.m_128385_(string, nArray));
    }

    static void putDouble(ItemStack itemStack, CompoundTag compoundTag, String string, double d) {
        OwnerStatueTaskWrites.write(itemStack, compoundTag, string, () -> DoubleTag.m_128500_((double)d), () -> compoundTag.m_128347_(string, d));
    }

    static void remove(ItemStack itemStack, CompoundTag compoundTag, String string) {
        OwnerStatueTaskWrites.write(itemStack, compoundTag, string, () -> null, () -> compoundTag.m_128473_(string));
    }
}

