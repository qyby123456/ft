package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeNbtMutationFields;
import net.farmingtales.ftboss.item.NativeMutationMap276;
import net.farmingtales.ftboss.item.NativeMapMutationScope280;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Map;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.farmingtales.ftboss.item.NativeNbtAliasPolicy;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CompoundTag.class, remap = false)
public abstract class NativeCompoundTagMutationVersionMixin implements NativeNbtMutationFields {
    @Shadow @Final private Map<String, Tag> tags;

    @ModifyVariable(method = "<init>(Ljava/util/Map;)V", at = @At(value = "INVOKE", target = "Ljava/lang/Object;<init>()V", shift = At.Shift.AFTER), argsOnly = true, index = 1, require = 1, remap = false)
    private Map<String, Tag> ftboss$observeConstructorMap280(Map<String, Tag> input) {
        this.ftboss$nativeNbtRawEscaped = !net.farmingtales.ftboss.item.NativeFreshFactoryTrust298.freshMap(this, input);
        return input == null ? null : new NativeMutationMap276<>(this, input, this::ftboss$nativeMapChanging280);
    }

    @Unique
    private void ftboss$nativeMapChanging280() {
        if (!NativeMapMutationScope280.suppressed(this.tags)) {
            ftboss$advanceNativeNbtVersion();
            try { OwnerFullRepairGuard.changed(this, 20); }
            catch (Throwable failure) {
                this.ftboss$nativeNbtRawEscaped = true;
                throw new IllegalStateException("Native map owner notification failed", failure);
            }
        }
    }

    @WrapOperation(method = {"put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", "putByte(Ljava/lang/String;B)V", "putShort(Ljava/lang/String;S)V", "putInt(Ljava/lang/String;I)V", "putLong(Ljava/lang/String;J)V", "putUUID(Ljava/lang/String;Ljava/util/UUID;)V", "putFloat(Ljava/lang/String;F)V", "putDouble(Ljava/lang/String;D)V", "putString(Ljava/lang/String;Ljava/lang/String;)V", "putByteArray(Ljava/lang/String;[B)V", "putByteArray(Ljava/lang/String;Ljava/util/List;)V", "putIntArray(Ljava/lang/String;[I)V", "putIntArray(Ljava/lang/String;Ljava/util/List;)V", "putLongArray(Ljava/lang/String;[J)V", "putLongArray(Ljava/lang/String;Ljava/util/List;)V", "putBoolean(Ljava/lang/String;Z)V"}, at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), require = 16, expect = 16, allow = 16, remap = false)
    private Object ftboss$nativeMapPutScope280(Map<Object, Object> map, Object key, Object value, Operation<Object> original) {
        return NativeMapMutationScope280.put(map, key, value, original);
    }

    @WrapOperation(method = "remove(Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"), require = 1, expect = 1, allow = 1, remap = false)
    private Object ftboss$nativeMapRemoveScope280(Map<Object, Object> map, Object key, Operation<Object> original) {
        return NativeMapMutationScope280.remove(map, key, original);
    }

    @Unique
    private long ftboss$nativeNbtVersion;
    @Unique
    private boolean ftboss$nativeNbtRawEscaped;

    @Override
    public final boolean ftboss$nativeNbtRawEscaped() {
        return this.ftboss$nativeNbtRawEscaped;
    }

    @Override
    public final long ftboss$nativeNbtMutationVersion() {
        return this.ftboss$nativeNbtVersion;
    }

    @Unique
    private void ftboss$advanceNativeNbtVersion() {
        if (this.ftboss$nativeNbtVersion != Long.MAX_VALUE) this.ftboss$nativeNbtVersion++;
    }

    @Inject(method = {"putByte(Ljava/lang/String;B)V", "putShort(Ljava/lang/String;S)V", "putInt(Ljava/lang/String;I)V", "putLong(Ljava/lang/String;J)V", "putUUID(Ljava/lang/String;Ljava/util/UUID;)V", "putFloat(Ljava/lang/String;F)V", "putDouble(Ljava/lang/String;D)V", "putString(Ljava/lang/String;Ljava/lang/String;)V", "putByteArray(Ljava/lang/String;[B)V", "putByteArray(Ljava/lang/String;Ljava/util/List;)V", "putIntArray(Ljava/lang/String;[I)V", "putIntArray(Ljava/lang/String;Ljava/util/List;)V", "putLongArray(Ljava/lang/String;[J)V", "putLongArray(Ljava/lang/String;Ljava/util/List;)V", "putBoolean(Ljava/lang/String;Z)V", "remove(Ljava/lang/String;)V"}, at = @At("HEAD"), require = 16, remap = false)
    private void ftboss$nativeNbtVoidMutation(CallbackInfo callback) {
        ftboss$advanceNativeNbtVersion();
    }

    @Inject(method = {"put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", "merge(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"}, at = @At("HEAD"), require = 2, remap = false)
    private void ftboss$nativeNbtReturnedMutation(CallbackInfoReturnable<?> callback) {
        ftboss$advanceNativeNbtVersion();
    }

    @Inject(method = {"ftboss$rawCompoundFields()Ljava/util/Map;"}, at = @At("RETURN"), require = 1, remap = false)
    private void ftboss$nativeRawAlias(CallbackInfoReturnable<?> callback) {
        if (callback.getReturnValue() != null && !NativeNbtAliasPolicy.trustedRawFieldRead()) {
            this.ftboss$nativeNbtRawEscaped = true;
        }
    }

    @Inject(method = {"getAllKeys()Ljava/util/Set;", "getByteArray(Ljava/lang/String;)[B", "getIntArray(Ljava/lang/String;)[I", "getLongArray(Ljava/lang/String;)[J", "entrySet()Ljava/util/Set;"}, at = @At("RETURN"), require = 5, remap = false)
    private void ftboss$nativePublicAlias(CallbackInfoReturnable<?> callback) {
        if (callback.getReturnValue() != null) this.ftboss$nativeNbtRawEscaped = true;
    }
}
