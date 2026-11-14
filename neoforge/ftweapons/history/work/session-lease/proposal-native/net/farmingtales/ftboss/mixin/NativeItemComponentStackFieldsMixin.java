package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeItemComponentFields;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemStack.class, remap = false)
public abstract class NativeItemComponentStackFieldsMixin implements NativeItemComponentFields.Stack {
    @Shadow(remap = false) @Final private Item item;
    @Shadow(remap = false) private int count;
    @Shadow(remap = false) @Final private PatchedDataComponentMap components;

    @Override
    public Item ftboss$nativeItem() {
        NativeItemComponentFields.requireObserver(ItemStack.class, "ftboss$nativeItem");
        return this.item;
    }

    @Override
    public int ftboss$nativeCount() {
        NativeItemComponentFields.requireObserver(ItemStack.class, "ftboss$nativeCount");
        return this.count;
    }

    @Override
    public PatchedDataComponentMap ftboss$nativeComponents() {
        NativeItemComponentFields.requireObserver(ItemStack.class, "ftboss$nativeComponents");
        return this.components;
    }

    @Unique
    private long ftboss$nativeStackVersion;

    @Override
    public final long ftboss$nativeStackMutationVersion() {
        NativeItemComponentFields.requireObserver(ItemStack.class, "ftboss$nativeStackMutationVersion");
        return this.ftboss$nativeStackVersion;
    }

    @Inject(method = "set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
        at = @At("RETURN"), require = 1, allow = 1, remap = false)
    private void ftboss$nativeCustomDataAssigned(net.minecraft.core.component.DataComponentType<?> type,Object value,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Object> callback) {
        net.farmingtales.ftboss.item.NativeExpectedFieldObservation.nativeCustomDataAssigned((ItemStack)(Object)this,type,value,callback.getReturnValue());
        net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority.nativeCrossbowComponentReturned((ItemStack)(Object)this,type,value,callback.getReturnValue());
    }

    @Inject(method = "setCount(I)V", at = @At("HEAD"), require = 1, remap = false)
    private void ftboss$nativeStackCountWrite(CallbackInfo callback) {
        if (this.ftboss$nativeStackVersion != Long.MAX_VALUE) this.ftboss$nativeStackVersion++;
    }
}
