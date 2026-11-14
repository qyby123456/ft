package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemStack.class, remap = false)
public abstract class OwnerFullRepairItemStackMixin {
    @Inject(method = {"getComponents()Lnet/minecraft/core/component/DataComponentMap;", "getComponentsPatch()Lnet/minecraft/core/component/DataComponentPatch;"}, at = @At("RETURN"), require = 2, remap = false)
    private void ftboss$fullRepairAlias(CallbackInfoReturnable<?> callback) {
        try {
            OwnerFullRepairGuard.alias(this, callback.getReturnValue(), true);
        } catch (Throwable failure) {
        }
    }

    @Inject(method = {"setDamageValue(I)V","hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V","hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V","hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V","postHurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;)V","mineBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V","onCraftedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;I)V","onCraftedBySystem(Lnet/minecraft/world/level/Level;)V","releaseUsing(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V","setEntityRepresentation(Lnet/minecraft/world/entity/Entity;)V","enchant(Lnet/minecraft/core/Holder;I)V","setCount(I)V","limitSize(I)V","grow(I)V","shrink(I)V","consume(ILnet/minecraft/world/entity/LivingEntity;)V","onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V","onDestroyed(Lnet/minecraft/world/entity/item/ItemEntity;)V","applyComponentsAndValidate(Lnet/minecraft/core/component/DataComponentPatch;)V","applyComponents(Lnet/minecraft/core/component/DataComponentPatch;)V","applyComponents(Lnet/minecraft/core/component/DataComponentMap;)V"}, at = @At("HEAD"), require = 21, remap = false)
    private void ftboss$fullRepairVoidMutation(CallbackInfo callback) {
        try {
            OwnerFullRepairGuard.changed(this, 20);
        } catch (Throwable failure) {
        }
    }

    @Inject(method = {"split(I)Lnet/minecraft/world/item/ItemStack;","copyAndClear()Lnet/minecraft/world/item/ItemStack;","copy()Lnet/minecraft/world/item/ItemStack;","copyWithCount(I)Lnet/minecraft/world/item/ItemStack;","transmuteCopy(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/ItemStack;","transmuteCopy(Lnet/minecraft/world/level/ItemLike;I)Lnet/minecraft/world/item/ItemStack;","useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;","onItemUseFirst(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;","use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;","finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;","hurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;)Z","overrideStackedOnOther(Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/inventory/ClickAction;Lnet/minecraft/world/entity/player/Player;)Z","overrideOtherStackedOnMe(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/inventory/ClickAction;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/SlotAccess;)Z","interactLivingEntity(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;","set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;","update(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;","update(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;Ljava/util/function/UnaryOperator;)Ljava/lang/Object;","remove(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;","consumeAndReturn(ILnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;","hurtAndConvertOnBreak(ILnet/minecraft/world/level/ItemLike;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"}, at = @At("HEAD"), require = 20, remap = false)
    private void ftboss$fullRepairReturnedMutation(CallbackInfoReturnable<?> callback) {
        try {
            OwnerFullRepairGuard.changed(this, 21);
        } catch (Throwable failure) {
        }
    }
}
