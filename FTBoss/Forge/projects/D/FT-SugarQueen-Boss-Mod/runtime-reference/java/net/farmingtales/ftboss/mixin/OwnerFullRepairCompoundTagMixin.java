/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets={"net.minecraft.nbt.CompoundTag"}, remap=true)
public abstract class OwnerFullRepairCompoundTagMixin {
    @Inject(method={"putByte(Ljava/lang/String;B)V", "putShort(Ljava/lang/String;S)V", "putInt(Ljava/lang/String;I)V", "putLong(Ljava/lang/String;J)V", "putUUID(Ljava/lang/String;Ljava/util/UUID;)V", "putFloat(Ljava/lang/String;F)V", "putDouble(Ljava/lang/String;D)V", "putString(Ljava/lang/String;Ljava/lang/String;)V", "putByteArray(Ljava/lang/String;[B)V", "putByteArray(Ljava/lang/String;Ljava/util/List;)V", "putIntArray(Ljava/lang/String;[I)V", "putIntArray(Ljava/lang/String;Ljava/util/List;)V", "putLongArray(Ljava/lang/String;[J)V", "putLongArray(Ljava/lang/String;Ljava/util/List;)V", "putBoolean(Ljava/lang/String;Z)V", "remove(Ljava/lang/String;)V"}, at={@At(value="HEAD")}, require=16)
    private void ftboss$fullRepairVoidMutation(CallbackInfo callbackInfo) {
        try {
            OwnerFullRepairGuard.changed(this, 20);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Inject(method={"put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", "merge(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"}, at={@At(value="HEAD")}, require=2)
    private void ftboss$fullRepairReturnedMutation(CallbackInfoReturnable<?> callbackInfoReturnable) {
        try {
            OwnerFullRepairGuard.changed(this, 21);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Inject(method={"getAllKeys()Ljava/util/Set;", "get(Ljava/lang/String;)Lnet/minecraft/nbt/Tag;", "getByteArray(Ljava/lang/String;)[B", "getIntArray(Ljava/lang/String;)[I", "getLongArray(Ljava/lang/String;)[J", "getCompound(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;", "getList(Ljava/lang/String;I)Lnet/minecraft/nbt/ListTag;", "entries()Ljava/util/Map;"}, at={@At(value="RETURN")}, require=8)
    private void ftboss$fullRepairAlias(CallbackInfoReturnable<?> callbackInfoReturnable) {
        try {
            OwnerFullRepairGuard.alias(this, callbackInfoReturnable.getReturnValue(), true);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

