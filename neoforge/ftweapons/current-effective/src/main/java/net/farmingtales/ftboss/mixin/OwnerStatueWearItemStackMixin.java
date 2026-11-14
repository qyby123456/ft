package net.farmingtales.ftboss.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemStack.class, remap = false)
public abstract class OwnerStatueWearItemStackMixin {
    @Inject(
        method = {"releaseUsing(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$anchor01(CallbackInfo var1) {
    }

    @Inject(
        method = {"hurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;)Z"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$anchor02(CallbackInfoReturnable<Boolean> var1) {
    }

    @Inject(
        method = {"mineBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$anchor03(CallbackInfo var1) {
    }

    @Inject(
        method = {"use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$anchor04(CallbackInfoReturnable<?> var1) {
    }

    @Inject(
        method = {"onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$anchor05(CallbackInfo var1) {
    }

    @Inject(
        method = {"finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$anchor06(CallbackInfoReturnable<?> var1) {
    }

    @Inject(
        method = {"hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$anchor07(CallbackInfo var1) {
    }

    @Inject(
        method = {"hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$anchor08(CallbackInfo var1) {
    }

    @Inject(
        method = {"setDamageValue(I)V"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$anchor09(CallbackInfo var1) {
    }

    @Inject(
        method = {"useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$anchor10(CallbackInfoReturnable<?> var1) {
    }

    @Inject(
        method = {"onItemUseFirst(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$anchor11(CallbackInfoReturnable<?> var1) {
    }

    @Inject(
        method = {"interactLivingEntity(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$anchor12(CallbackInfoReturnable<?> var1) {
    }

    @Inject(
        method = {"inventoryTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$anchor13(CallbackInfo var1) {
    }

    @Inject(
        method = {"onCraftedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;I)V"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$anchor14(CallbackInfo var1) {
    }
}
