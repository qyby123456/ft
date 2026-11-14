/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ItemStack.class})
public abstract class OwnerStatueWearItemStackMixin {
    @Inject(method={"releaseUsing(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$anchor01(CallbackInfo callbackInfo) {
    }

    @Inject(method={"hurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;)V"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$anchor02(CallbackInfo callbackInfo) {
    }

    @Inject(method={"mineBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$anchor03(CallbackInfo callbackInfo) {
    }

    @Inject(method={"use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$anchor04(CallbackInfoReturnable<?> callbackInfoReturnable) {
    }

    @Inject(method={"onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$anchor05(CallbackInfo callbackInfo) {
    }

    @Inject(method={"finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$anchor06(CallbackInfoReturnable<?> callbackInfoReturnable) {
    }

    @Inject(method={"hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$anchor07(CallbackInfo callbackInfo) {
    }

    @Inject(method={"hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$anchor08(CallbackInfoReturnable<?> callbackInfoReturnable) {
    }

    @Inject(method={"setDamageValue(I)V"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$anchor09(CallbackInfo callbackInfo) {
    }

    @Inject(method={"useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$anchor10(CallbackInfoReturnable<?> callbackInfoReturnable) {
    }

    @Inject(method={"onItemUseFirst(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$anchor11(CallbackInfoReturnable<?> callbackInfoReturnable) {
    }

    @Inject(method={"interactLivingEntity(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$anchor12(CallbackInfoReturnable<?> callbackInfoReturnable) {
    }

    @Inject(method={"inventoryTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$anchor13(CallbackInfo callbackInfo) {
    }

    @Inject(method={"onCraftedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;I)V"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$statueWear$anchor14(CallbackInfo callbackInfo) {
    }
}

