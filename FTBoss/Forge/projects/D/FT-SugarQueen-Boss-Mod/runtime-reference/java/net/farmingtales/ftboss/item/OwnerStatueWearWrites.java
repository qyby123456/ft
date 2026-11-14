/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.nbt.ByteTag
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.DoubleTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.function.Supplier;
import net.farmingtales.ftboss.item.OwnerRepairContinuation;
import net.farmingtales.ftboss.item.OwnerStatueTaskOrigin;
import net.farmingtales.ftboss.item.OwnerStatueWearAdmission;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class OwnerStatueWearWrites {
    private OwnerStatueWearWrites() {
    }

    private static void write(ItemStack itemStack, int n, Object object, String string, Supplier<Tag> supplier, Runnable runnable) {
        OwnerStatueTaskOrigin.Write write = null;
        try {
            write = OwnerStatueTaskOrigin.beforeWrite(itemStack, object, string, supplier.get(), n == 3);
        }
        catch (Throwable throwable) {
            OwnerStatueTaskOrigin.failedWrite(itemStack);
        }
        OwnerStatueWearAdmission.Write write2 = null;
        try {
            write2 = OwnerStatueWearAdmission.beforeWrite(itemStack, n, object, string, supplier.get());
        }
        catch (Throwable throwable) {
            // empty catch block
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
                OwnerStatueWearAdmission.afterWrite(write2, throwable);
            }
            catch (Throwable throwable3) {}
            try {
                OwnerStatueTaskOrigin.afterWrite(write, throwable);
            }
            catch (Throwable throwable4) {
                OwnerStatueTaskOrigin.failedWrite(itemStack);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void carryOnly(OwnerRepairContinuation.CarryWrite carryWrite, ItemStack itemStack, CompoundTag compoundTag, double d) {
        OwnerRepairContinuation.claimCarry(carryWrite, itemStack, compoundTag, d);
        boolean bl = false;
        try {
            compoundTag.m_128347_("WearCarry", d);
            bl = true;
        }
        finally {
            OwnerRepairContinuation.completeCarry(carryWrite, bl);
        }
    }

    static void putDouble(ItemStack itemStack, int n, CompoundTag compoundTag, String string, double d) {
        OwnerStatueWearWrites.write(itemStack, n, compoundTag, string, () -> DoubleTag.m_128500_((double)d), () -> compoundTag.m_128347_(string, d));
    }

    static void putInt(ItemStack itemStack, int n, CompoundTag compoundTag, String string, int n2) {
        OwnerStatueWearWrites.write(itemStack, n, compoundTag, string, () -> IntTag.m_128679_((int)n2), () -> compoundTag.m_128405_(string, n2));
    }

    static void putBoolean(ItemStack itemStack, int n, CompoundTag compoundTag, String string, boolean bl) {
        OwnerStatueWearWrites.write(itemStack, n, compoundTag, string, () -> ByteTag.m_128273_((boolean)bl), () -> compoundTag.m_128379_(string, bl));
    }

    static void putString(ItemStack itemStack, int n, CompoundTag compoundTag, String string, String string2) {
        OwnerStatueWearWrites.write(itemStack, n, compoundTag, string, () -> StringTag.m_129297_((String)string2), () -> compoundTag.m_128359_(string, string2));
    }

    static void remove(ItemStack itemStack, int n, CompoundTag compoundTag, String string) {
        OwnerStatueWearWrites.write(itemStack, n, compoundTag, string, () -> null, () -> compoundTag.m_128473_(string));
    }

    static void removeSocket(ItemStack itemStack, ListTag listTag, int n) {
        OwnerStatueWearWrites.write(itemStack, 4, listTag, "Sockets", () -> IntTag.m_128679_((int)n), () -> listTag.remove(n));
    }

    static void rollback(ItemStack itemStack, CompoundTag compoundTag, Throwable throwable) {
        try {
            OwnerStatueWearAdmission.rollback(itemStack, compoundTag, throwable);
        }
        catch (Throwable throwable2) {
            // empty catch block
        }
        OwnerStatueWearWrites.write(itemStack, 8, itemStack, "", () -> compoundTag, () -> itemStack.m_41751_(compoundTag));
    }

    public static void rawDamage(Item item, ItemStack itemStack, int n, Operation<Void> operation) {
        OwnerStatueWearWrites.write(itemStack, 3, itemStack, "Damage", () -> IntTag.m_128679_((int)Math.max(0, n)), () -> operation.call(new Object[]{item, itemStack, n}));
    }

    static void cost(ItemStack itemStack, int n) {
        try {
            OwnerStatueWearAdmission.cost(itemStack, n);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    static void finish(ItemStack itemStack, ServerPlayer serverPlayer, boolean bl) {
        try {
            OwnerStatueWearAdmission.finish(itemStack, serverPlayer, bl);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

