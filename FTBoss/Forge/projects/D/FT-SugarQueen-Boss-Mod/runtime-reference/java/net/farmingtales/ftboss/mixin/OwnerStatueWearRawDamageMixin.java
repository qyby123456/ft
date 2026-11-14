/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.OwnerStatueWearWrites;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ItemStack.class}, remap=true)
public abstract class OwnerStatueWearRawDamageMixin {
    @WrapOperation(method={"setDamageValue(I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;setDamage(Lnet/minecraft/world/item/ItemStack;I)V", remap=false)}, require=1, expect=1, allow=1)
    private void ftboss$statueRawDamage(Item item, ItemStack itemStack, int n, Operation<Void> operation) {
        OwnerStatueWearWrites.rawDamage(item, itemStack, n, operation);
    }
}

