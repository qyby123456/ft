/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ItemStack.class})
public abstract class OwnerInventoryTickContinuityMixin {
    @WrapOperation(method={"inventoryTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;inventoryTick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V")}, require=1)
    private void ftboss$ownerInventoryTickCallback(Item item, ItemStack actual, Level world, Entity entity, int slot, boolean selected, Operation<Void> original) throws Throwable {
        if (world.f_46443_ || item.getClass() == SwordItem.class) {
            original.call(new Object[]{item, actual, world, entity, slot, selected});
            return;
        }
        OwnerFullRepairGuard.TickCut saved = null;
        try {
            if (actual == this) {
                saved = OwnerFullRepairGuard.inventoryTickEntered(actual, item);
            } else {
                OwnerFullRepairGuard.changed(this, 42);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            original.call(new Object[]{item, actual, world, entity, slot, selected});
        }
        catch (Throwable originalFailure) {
            try {
                OwnerFullRepairGuard.inventoryTickReturned(saved, originalFailure);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            throw originalFailure;
        }
        try {
            OwnerFullRepairGuard.inventoryTickReturned(saved, null);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

