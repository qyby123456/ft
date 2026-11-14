package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeActionObservation;
import net.farmingtales.ftboss.item.NativeActionObservation.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
    value = {ItemStack.class},
    remap = false
)
public abstract class NativeSpecialMeleeMixin {
    @Redirect(
        method = {"hurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;)Z"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;hurtEnemy(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"
        ),
        require = 0,
        remap = false
    )
    @Group(
        name = "native$actualMelee",
        min = 1,
        max = 1
    )
    private boolean nativeActualMeleeSrg(Item var1, ItemStack var2, LivingEntity var3, LivingEntity var4) {
        return this.nativeMelee(var1, var2, var3, var4);
    }

    private boolean nativeMelee(Item var1, ItemStack var2, LivingEntity var3, LivingEntity var4) {
        boolean var5 = var4.getMainHandItem() == var2;
        boolean var6 = var4.getOffhandItem() == var2;
        InteractionHand var7 = var5 == var6 ? null : (var5 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        return NativeActionObservation.invoke(Action.MELEE_HIT, var2, var4, var7, () -> var1.hurtEnemy(var2, var3, var4));
    }
}
