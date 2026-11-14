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

@Mixin(targets={"net.minecraft.world.item.ItemStack"}, remap=true)
public abstract class OwnerFullRepairItemStackMixin {
    @Inject(method={"setDamageValue(I)V", "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", "hurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;)V", "mineBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V", "onCraftedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;I)V", "releaseUsing(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V", "removeTagKey(Ljava/lang/String;)V", "setTag(Lnet/minecraft/nbt/CompoundTag;)V", "resetHoverName()V", "hideTooltipPart(Lnet/minecraft/world/item/ItemStack$TooltipPart;)V", "enchant(Lnet/minecraft/world/item/enchantment/Enchantment;I)V", "addTagElement(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V", "setEntityRepresentation(Lnet/minecraft/world/entity/Entity;)V", "setRepairCost(I)V", "addAttributeModifier(Lnet/minecraft/world/entity/ai/attributes/Attribute;Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;Lnet/minecraft/world/entity/EquipmentSlot;)V", "setCount(I)V", "grow(I)V", "shrink(I)V", "onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V", "onDestroyed(Lnet/minecraft/world/entity/item/ItemEntity;)V", "deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V"}, at={@At(value="HEAD")}, require=21)
    private void ftboss$fullRepairVoidMutation(CallbackInfo callbackInfo) {
        try {
            OwnerFullRepairGuard.changed(this, 20);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Inject(method={"split(I)Lnet/minecraft/world/item/ItemStack;", "copyAndClear()Lnet/minecraft/world/item/ItemStack;", "copy()Lnet/minecraft/world/item/ItemStack;", "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", "onItemUseFirst(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;", "finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;", "hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", "overrideStackedOnOther(Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/inventory/ClickAction;Lnet/minecraft/world/entity/player/Player;)Z", "overrideOtherStackedOnMe(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/inventory/ClickAction;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/SlotAccess;)Z", "interactLivingEntity(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", "setHoverName(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/world/item/ItemStack;"}, at={@At(value="HEAD")}, require=12)
    private void ftboss$fullRepairReturnedMutation(CallbackInfoReturnable<?> callbackInfoReturnable) {
        try {
            OwnerFullRepairGuard.changed(this, 21);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Inject(method={"getOrCreateTag()Lnet/minecraft/nbt/CompoundTag;"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$fullRepairGetOrCreateTag(CallbackInfoReturnable<?> callbackInfoReturnable) {
        try {
            OwnerFullRepairGuard.tagReadOrCreateEntered(this, null);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Inject(method={"getOrCreateTagElement(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$fullRepairGetOrCreateElement(String string, CallbackInfoReturnable<?> callbackInfoReturnable) {
        try {
            OwnerFullRepairGuard.tagReadOrCreateEntered(this, string);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Inject(method={"getTag()Lnet/minecraft/nbt/CompoundTag;", "getOrCreateTag()Lnet/minecraft/nbt/CompoundTag;", "getOrCreateTagElement(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;", "getTagElement(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;", "getEnchantmentTags()Lnet/minecraft/nbt/ListTag;"}, at={@At(value="RETURN")}, require=5)
    private void ftboss$fullRepairAlias(CallbackInfoReturnable<?> callbackInfoReturnable) {
        try {
            OwnerFullRepairGuard.alias(this, callbackInfoReturnable.getReturnValue(), true);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

