/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.item.ArrowItem
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import java.util.function.Consumer;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={BowItem.class})
public abstract class NativeBowWearEntryMixin {
    @Redirect(method={"releaseUsing"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ArrowItem;createArrow(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/entity/projectile/AbstractArrow;"), require=1, expect=1, allow=1)
    private AbstractArrow ftboss$v6$ammoFactory(ArrowItem arrowItem, Level level, ItemStack itemStack, LivingEntity livingEntity) {
        return FoodWeaponSourceLedger.nativeArrowFactory(arrowItem, level, itemStack, livingEntity);
    }

    @Redirect(method={"releaseUsing"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"), require=1, expect=1, allow=1)
    private void ftboss$v6$wear(ItemStack itemStack, int n, LivingEntity livingEntity, Consumer<LivingEntity> consumer) {
        FoodWeaponSourceLedger.nativeWear(itemStack, n, livingEntity, consumer);
    }
}

