/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import java.util.function.Consumer;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.mixin.NativeCrossbowArrowInvoker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={CrossbowItem.class})
public abstract class NativeCrossbowWearEntryMixin {
    @Redirect(method={"shootProjectile"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/CrossbowItem;getArrow(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/AbstractArrow;"), require=1, expect=1, allow=1)
    private static AbstractArrow ftboss$v6$factory(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2) {
        return FoodWeaponSourceLedger.nativeCrossbowArrow(NativeCrossbowArrowInvoker.ftboss$v6$invokeArrow(level, livingEntity, itemStack, itemStack2), itemStack, itemStack2);
    }

    @Redirect(method={"shootProjectile"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"), require=1, expect=1, allow=1)
    private static void ftboss$v6$wear(ItemStack itemStack, int n, LivingEntity livingEntity, Consumer<LivingEntity> consumer) {
        FoodWeaponSourceLedger.nativeWear(itemStack, n, livingEntity, consumer);
    }
}

