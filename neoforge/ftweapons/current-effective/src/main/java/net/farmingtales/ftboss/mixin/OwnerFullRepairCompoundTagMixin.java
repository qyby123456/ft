package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    targets = {"net.minecraft.nbt.CompoundTag"},
    remap = true
)
public abstract class OwnerFullRepairCompoundTagMixin {
    @WrapMethod(method = "putLong(Ljava/lang/String;J)V")
    private void ftboss$nativeFxCooldown(String key, long value, Operation<Void> original) {
        OwnerFieldExpectedAuthority.nativeFxCooldown((CompoundTag)(Object)this, key, value, original);
    }

    @Inject(
        method = {"putByte(Ljava/lang/String;B)V", "putShort(Ljava/lang/String;S)V", "putInt(Ljava/lang/String;I)V", "putLong(Ljava/lang/String;J)V", "putUUID(Ljava/lang/String;Ljava/util/UUID;)V", "putFloat(Ljava/lang/String;F)V", "putDouble(Ljava/lang/String;D)V", "putString(Ljava/lang/String;Ljava/lang/String;)V", "putByteArray(Ljava/lang/String;[B)V", "putByteArray(Ljava/lang/String;Ljava/util/List;)V", "putIntArray(Ljava/lang/String;[I)V", "putIntArray(Ljava/lang/String;Ljava/util/List;)V", "putLongArray(Ljava/lang/String;[J)V", "putLongArray(Ljava/lang/String;Ljava/util/List;)V", "putBoolean(Ljava/lang/String;Z)V", "remove(Ljava/lang/String;)V"},
        at = {@At("HEAD")},
        require = 16
    )
    private void ftboss$fullRepairVoidMutation(CallbackInfo var1) {
        try {
            OwnerFullRepairGuard.changed(this, 20);
        } catch (Throwable var3) {
        }
    }

    @Inject(
        method = {"put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", "merge(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"},
        at = {@At("HEAD")},
        require = 2
    )
    private void ftboss$fullRepairReturnedMutation(CallbackInfoReturnable<?> var1) {
        try {
            OwnerFullRepairGuard.changed(this, 21);
        } catch (Throwable var3) {
        }
    }

    @Inject(
        method = {"getAllKeys()Ljava/util/Set;", "get(Ljava/lang/String;)Lnet/minecraft/nbt/Tag;", "getByteArray(Ljava/lang/String;)[B", "getIntArray(Ljava/lang/String;)[I", "getLongArray(Ljava/lang/String;)[J", "getCompound(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;", "getList(Ljava/lang/String;I)Lnet/minecraft/nbt/ListTag;", "entrySet()Ljava/util/Set;"},
        at = {@At("RETURN")},
        require = 8
    )
    private void ftboss$fullRepairAlias(CallbackInfoReturnable<?> var1) {
        try {
            OwnerFullRepairGuard.alias(this, var1.getReturnValue(), true);
        } catch (Throwable var3) {
        }
    }
}
