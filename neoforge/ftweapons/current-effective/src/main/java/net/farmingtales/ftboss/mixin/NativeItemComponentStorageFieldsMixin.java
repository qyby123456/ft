package net.farmingtales.ftboss.mixin;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.Optional;
import net.farmingtales.ftboss.item.NativeItemComponentFields;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PatchedDataComponentMap.class, remap = false)
public abstract class NativeItemComponentStorageFieldsMixin implements NativeItemComponentFields.Storage {
    @Shadow(remap = false) @Final private DataComponentMap prototype;
    @Shadow(remap = false) private Reference2ObjectMap<DataComponentType<?>, Optional<?>> patch;
    @Shadow(remap = false) private boolean copyOnWrite;
    @Unique private long ftboss$nativeComponentMutationVersion;

    @Override
    public DataComponentMap ftboss$nativePrototype() {
        NativeItemComponentFields.requireObserver(PatchedDataComponentMap.class, "ftboss$nativePrototype");
        return this.prototype;
    }

    @Override
    public Reference2ObjectMap<DataComponentType<?>, Optional<?>> ftboss$nativePatch() {
        NativeItemComponentFields.requireObserver(PatchedDataComponentMap.class, "ftboss$nativePatch");
        return this.patch;
    }

    @Override
    public boolean ftboss$nativeCopyOnWrite() {
        NativeItemComponentFields.requireObserver(PatchedDataComponentMap.class, "ftboss$nativeCopyOnWrite");
        return this.copyOnWrite;
    }

    @Override
    public long ftboss$nativeComponentMutationVersion() {
        NativeItemComponentFields.requireObserver(PatchedDataComponentMap.class, "ftboss$nativeComponentMutationVersion");
        return this.ftboss$nativeComponentMutationVersion;
    }

    @Unique
    private void ftboss$advanceNativeComponentMutationVersion() {
        if (this.ftboss$nativeComponentMutationVersion == Long.MAX_VALUE) {
            throw new IllegalStateException("Native component observation mutation version exhausted");
        }
        this.ftboss$nativeComponentMutationVersion++;
    }

    @Inject(method = {"set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", "remove(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"}, at = @At("HEAD"), require = 2, remap = false)
    private void ftboss$nativeComponentValueWrite(CallbackInfoReturnable<?> callback) {
        this.ftboss$advanceNativeComponentMutationVersion();
    }

    @Inject(method = {"applyPatch(Lnet/minecraft/core/component/DataComponentPatch;)V", "restorePatch(Lnet/minecraft/core/component/DataComponentPatch;)V", "setAll(Lnet/minecraft/core/component/DataComponentMap;)V"}, at = @At("HEAD"), require = 3, remap = false)
    private void ftboss$nativeComponentMapWrite(CallbackInfo callback) {
        this.ftboss$advanceNativeComponentMutationVersion();
    }
}
