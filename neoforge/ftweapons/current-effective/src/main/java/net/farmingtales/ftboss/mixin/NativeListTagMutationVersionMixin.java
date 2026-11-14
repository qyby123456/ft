package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeNbtMutationFields;
import net.farmingtales.ftboss.item.NativeNbtAliasPolicy;
import net.minecraft.nbt.ListTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ListTag.class, remap = false)
public abstract class NativeListTagMutationVersionMixin implements NativeNbtMutationFields {
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

    @Inject(method = {"add(ILnet/minecraft/nbt/Tag;)V", "clear()V", "add(ILjava/lang/Object;)V"}, at = @At("HEAD"), require = 3, remap = false)
    private void ftboss$nativeNbtVoidMutation(CallbackInfo callback) {
        ftboss$advanceNativeNbtVersion();
    }

    @Inject(method = {"remove(I)Lnet/minecraft/nbt/Tag;", "set(ILnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", "setTag(ILnet/minecraft/nbt/Tag;)Z", "addTag(ILnet/minecraft/nbt/Tag;)Z", "remove(I)Ljava/lang/Object;", "set(ILjava/lang/Object;)Ljava/lang/Object;"}, at = @At("HEAD"), require = 6, remap = false)
    private void ftboss$nativeNbtReturnedMutation(CallbackInfoReturnable<?> callback) {
        ftboss$advanceNativeNbtVersion();
    }

    @Inject(method = {"ftboss$rawListFields()Ljava/util/List;"}, at = @At("RETURN"), require = 1, remap = false)
    private void ftboss$nativeRawAlias(CallbackInfoReturnable<?> callback) {
        if (callback.getReturnValue() != null && !NativeNbtAliasPolicy.trustedRawFieldRead()) {
            this.ftboss$nativeNbtRawEscaped = true;
        }
    }

    @Inject(method = {"getIntArray(I)[I", "getLongArray(I)[J"}, at = @At("RETURN"), require = 2, remap = false)
    private void ftboss$nativePublicAlias(CallbackInfoReturnable<?> callback) {
        if (callback.getReturnValue() != null) this.ftboss$nativeNbtRawEscaped = true;
    }
}
