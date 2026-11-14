package net.farmingtales.ftboss.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.function.Supplier;
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

    private static void write(ItemStack var0, int var1, Object var2, String var3, Supplier<Tag> var4, Runnable var5) {
        OwnerStatueTaskOrigin.Write var6 = null;

        try {
            var6 = OwnerStatueTaskOrigin.beforeWrite(var0, var2, var3, (Tag)var4.get(), var1 == 3);
        } catch (Throwable var25) {
            OwnerStatueTaskOrigin.failedWrite(var0);
        }

        OwnerStatueWearAdmission.Write var7 = null;

        try {
            var7 = OwnerStatueWearAdmission.beforeWrite(var0, var1, var2, var3, (Tag)var4.get());
        } catch (Throwable var24) {
        }

        Throwable var8 = null;

        try {
            var5.run();
        } catch (Error | RuntimeException var22) {
            var8 = var22;
            throw var22;
        } finally {
            try {
                OwnerStatueWearAdmission.afterWrite(var7, var8);
            } catch (Throwable var21) {
            }

            try {
                OwnerStatueTaskOrigin.afterWrite(var6, var8);
            } catch (Throwable var20) {
                OwnerStatueTaskOrigin.failedWrite(var0);
            }
        }
    }

    static void carryOnly(OwnerRepairContinuation.CarryWrite var0, ItemStack var1, CompoundTag var2, double var3) {
        OwnerRepairContinuation.claimCarry(var0, var1, var2, var3);
        boolean var5 = false;

        try {
            var2.putDouble("WearCarry", var3);
            var5 = true;
        } finally {
            OwnerRepairContinuation.completeCarry(var0, var5);
        }
    }

    static void putDouble(ItemStack var0, int var1, CompoundTag var2, String var3, double var4) {
        write(var0, var1, var2, var3, () -> DoubleTag.valueOf(var4), () -> var2.putDouble(var3, var4));
    }

    static void putInt(ItemStack var0, int var1, CompoundTag var2, String var3, int var4) {
        write(var0, var1, var2, var3, () -> IntTag.valueOf(var4), () -> var2.putInt(var3, var4));
    }

    static void putBoolean(ItemStack var0, int var1, CompoundTag var2, String var3, boolean var4) {
        write(var0, var1, var2, var3, () -> ByteTag.valueOf(var4), () -> var2.putBoolean(var3, var4));
    }

    static void putString(ItemStack var0, int var1, CompoundTag var2, String var3, String var4) {
        write(var0, var1, var2, var3, () -> StringTag.valueOf(var4), () -> var2.putString(var3, var4));
    }

    static void remove(ItemStack var0, int var1, CompoundTag var2, String var3) {
        write(var0, var1, var2, var3, () -> null, () -> var2.remove(var3));
    }

    static void removeSocket(ItemStack var0, ListTag var1, int var2) {
        write(var0, 4, var1, "Sockets", () -> IntTag.valueOf(var2), () -> var1.remove(var2));
    }

    static void rollback(ItemStack var0, CompoundTag var1, Throwable var2) {
        try {
            OwnerStatueWearAdmission.rollback(var0, var1, var2);
        } catch (Throwable var4) {
        }

        write(var0, 8, var0, "", () -> var1, () -> NativeWearComponentRollback.restore(var0, var1));
    }

    public static void rawDamage(Item var0, ItemStack var1, int var2, Operation<Void> var3) {
        write(var1, 3, var1, "Damage", () -> IntTag.valueOf(Math.max(0, var2)), () -> var3.call(new Object[]{var0, var1, var2}));
    }

    static void cost(ItemStack var0, int var1) {
        try {
            OwnerStatueWearAdmission.cost(var0, var1);
        } catch (Throwable var3) {
        }
    }

    static void finish(ItemStack var0, ServerPlayer var1, boolean var2) {
        try {
            OwnerStatueWearAdmission.finish(var0, var1, var2);
        } catch (Throwable var4) {
        }
    }
}
