/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeObservationFrame;
import net.farmingtales.ftboss.item.NativePaidWeaponCarrier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public final class NativeReadCutObserver {
    public static void failed(Object object, NativeObservationFrame nativeObservationFrame) {
        if (nativeObservationFrame != null && nativeObservationFrame.receiver() == object) {
            nativeObservationFrame.closeOriginalFailure();
        }
    }

    private NativeReadCutObserver() {
    }

    public static void weapon(Object object) {
        if (object instanceof ItemStack) {
            ItemStack itemStack = (ItemStack)object;
            FoodWeaponSourceLedger.invalidateWeaponReadProjection(itemStack);
        }
    }

    public static void player(Object object) {
        Entity entity;
        if (object instanceof Entity) {
            entity = (Entity)object;
            FoodWeaponSourceLedger.invalidateTargetReadProjection(entity);
        }
        if (object instanceof ServerPlayer) {
            entity = (ServerPlayer)object;
            FoodWeaponSourceLedger.invalidateOwnerReadProjection((ServerPlayer)entity);
        }
    }

    public static void removed(Object object) {
        if (object instanceof NativePaidWeaponCarrier) {
            NativePaidWeaponCarrier nativePaidWeaponCarrier = (NativePaidWeaponCarrier)object;
            nativePaidWeaponCarrier.ftboss$retireOwnerPaidWeapon();
        }
        NativeReadCutObserver.player(object);
    }

    public static void projectile(Object object) {
        if (object instanceof NativePaidWeaponCarrier) {
            NativePaidWeaponCarrier nativePaidWeaponCarrier = (NativePaidWeaponCarrier)object;
            nativePaidWeaponCarrier.ftboss$originalOwnerPaidWeapon();
        }
        NativeReadCutObserver.player(object);
    }

    public static void server(Object object) {
        if (object instanceof MinecraftServer) {
            MinecraftServer minecraftServer = (MinecraftServer)object;
            FoodWeaponSourceLedger.invalidateServerReadProjection(minecraftServer);
        }
    }

    public static void source(Object object) {
        if (object instanceof DamageSource) {
            DamageSource damageSource = (DamageSource)object;
            FoodWeaponSourceLedger.invalidateSourceReadProjection(damageSource);
        }
    }
}

