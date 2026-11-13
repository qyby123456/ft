package com.farmingtales.drinkanim.mixin;

import com.farmingtales.drinkanim.client.DrinkController;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class DrinkPlacementMixin {
    @Unique private ItemStack farmingtales$placedStack=ItemStack.EMPTY;
    @Inject(method="place",at=@At("HEAD"))
    private void farmingtales$capturePlacement(BlockPlaceContext context,CallbackInfoReturnable<InteractionResult> cir) {
        if(context.getLevel().isClientSide&&context.getHand()==InteractionHand.MAIN_HAND)
            farmingtales$placedStack=DrinkController.supports(context.getItemInHand())?context.getItemInHand().copy():ItemStack.EMPTY;
    }
    @Inject(method="place",at=@At("RETURN"))
    private void farmingtales$animatePlacedDrink(BlockPlaceContext context,CallbackInfoReturnable<InteractionResult> cir) {
        if(!context.getLevel().isClientSide||context.getHand()!=InteractionHand.MAIN_HAND)return;
        if(cir.getReturnValue().consumesAction()&&!farmingtales$placedStack.isEmpty())
            DrinkController.placed(farmingtales$placedStack);
        farmingtales$placedStack=ItemStack.EMPTY;
    }
}
