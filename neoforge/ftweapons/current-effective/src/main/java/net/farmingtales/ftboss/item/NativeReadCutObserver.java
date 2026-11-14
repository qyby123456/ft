package net.farmingtales.ftboss.item;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public final class NativeReadCutObserver {
    public static void failed(Object var0, NativeObservationFrame var1) {
        if (var1 != null && var1.receiver() == var0) {
            var1.closeOriginalFailure();
        }
    }

    private NativeReadCutObserver() {
    }

    public static void weapon(Object var0) {
        if (var0 instanceof ItemStack var1) {
            try {
                NativeOpaqueProviderReadFence201.read(var1);
            } catch (Throwable observationFailure) {
            }
            FoodWeaponSourceLedger.invalidateWeaponReadProjection(var1);
        }
    }

    public static void player(Object var0) {
        if (var0 instanceof Entity var1) {
            FoodWeaponSourceLedger.invalidateTargetReadProjection(var1);
        }

        if (var0 instanceof ServerPlayer var2) {
            FoodWeaponSourceLedger.invalidateOwnerReadProjection(var2);
        }
    }

    public static void removed(Object var0) {
        if (var0 instanceof NativePaidWeaponCarrier var1) {
            var1.ftboss$retireOwnerPaidWeapon();
        }

        player(var0);
    }

    public static void projectile(Object var0) {
        if (var0 instanceof NativePaidWeaponCarrier var1) {
            var1.ftboss$originalOwnerPaidWeapon();
        }

        player(var0);
    }

    public static void server(Object var0) {
        if (var0 instanceof MinecraftServer var1) {
            FoodWeaponSourceLedger.invalidateServerReadProjection(var1);
        }
    }

    public static void source(Object var0) {
        if (var0 instanceof DamageSource var1) {
            FoodWeaponSourceLedger.invalidateSourceReadProjection(var1);
        }
    }
}
