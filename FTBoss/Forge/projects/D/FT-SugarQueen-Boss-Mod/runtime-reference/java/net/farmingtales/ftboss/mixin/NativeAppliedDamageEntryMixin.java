/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={ItemStack.class})
public abstract class NativeAppliedDamageEntryMixin {
    @Redirect(method={"hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V"), require=1, expect=1, allow=1)
    private void ftboss$v6$applied(ItemStack itemStack, int n, int n2, RandomSource randomSource, ServerPlayer serverPlayer) {
        FoodWeaponSourceLedger.nativeAppliedDamage(itemStack, n, n2, randomSource, serverPlayer);
    }
}

