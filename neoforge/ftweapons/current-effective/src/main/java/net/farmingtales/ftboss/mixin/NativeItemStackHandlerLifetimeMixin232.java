package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeItemHandlerLifetime232;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemStackHandler.class, remap = false)
public abstract class NativeItemStackHandlerLifetimeMixin232 {
    @Unique
    private void ftboss$nativeHandlerChanging() {
        try { NativeItemHandlerLifetime232.changing((ItemStackHandler)(Object)this); }
        catch (Throwable observationFailure) { }
    }

    @Unique
    private void ftboss$nativeHandlerChildReturned(ItemStack child) {
        try { NativeItemHandlerLifetime232.childReturned((ItemStackHandler)(Object)this,child); }
        catch (Throwable observationFailure) { }
    }

    @Inject(method = "getStackInSlot(I)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"),
        require = 1, allow = 1, remap = false)
    private void ftboss$nativeHandlerChildReturn(CallbackInfoReturnable<ItemStack> callback) {
        this.ftboss$nativeHandlerChildReturned(callback.getReturnValue());
    }

    @Inject(method = "setSize(I)V", at = @At(value = "FIELD",
        target = "Lnet/neoforged/neoforge/items/ItemStackHandler;stacks:Lnet/minecraft/core/NonNullList;", opcode = Opcodes.PUTFIELD),
        require = 1, allow = 1, remap = false)
    private void ftboss$nativeHandlerResizeWrite(CallbackInfo callback) {
        this.ftboss$nativeHandlerChanging();
    }

    @Inject(method = {"setStackInSlot(ILnet/minecraft/world/item/ItemStack;)V", "lambda$deserializeNBT$0(ILnet/minecraft/world/item/ItemStack;)V"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"),
        require = 2, allow = 2, remap = false)
    private void ftboss$nativeHandlerSlotWrite(CallbackInfo callback) {
        this.ftboss$nativeHandlerChanging();
    }

    @Inject(method = "insertItem(ILnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"),
        require = 1, allow = 1, remap = false)
    private void ftboss$nativeHandlerInsertSlotWrite(CallbackInfoReturnable<ItemStack> callback) {
        this.ftboss$nativeHandlerChanging();
    }

    @Inject(method = "insertItem(ILnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;grow(I)V"),
        require = 1, allow = 1, remap = false)
    private void ftboss$nativeHandlerInsertCountWrite(CallbackInfoReturnable<ItemStack> callback) {
        this.ftboss$nativeHandlerChanging();
    }

    @Inject(method = "extractItem(IIZ)Lnet/minecraft/world/item/ItemStack;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"),
        require = 2, allow = 2, remap = false)
    private void ftboss$nativeHandlerExtractSlotWrite(CallbackInfoReturnable<ItemStack> callback) {
        this.ftboss$nativeHandlerChanging();
    }

    @Unique
    private void ftboss$nativeHandlerSerialized(net.minecraft.nbt.CompoundTag result) {
        try { NativeItemHandlerLifetime232.serialized((ItemStackHandler)(Object)this, result); }
        catch (Throwable observationFailure) { }
    }

    @Inject(method = "serializeNBT(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;",
        at = @At("RETURN"), require = 1, allow = 1, remap = false)
    private void ftboss$nativeHandlerSerializeReturn(CallbackInfoReturnable<net.minecraft.nbt.CompoundTag> callback) {
        this.ftboss$nativeHandlerSerialized(callback.getReturnValue());
    }
}
