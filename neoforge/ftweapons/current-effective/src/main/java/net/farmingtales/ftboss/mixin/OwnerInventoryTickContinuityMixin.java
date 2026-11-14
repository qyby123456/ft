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

@Mixin({ItemStack.class})
public abstract class OwnerInventoryTickContinuityMixin {
    @WrapOperation(
        method = {"inventoryTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;inventoryTick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V"
        )},
        require = 1
    )
    private void ftboss$ownerInventoryTickCallback(Item item, ItemStack actual, Level world, Entity entity, int slot, boolean selected, Operation<Void> original) throws Throwable {
        if (!world.isClientSide && item.getClass() != SwordItem.class) {
            OwnerFullRepairGuard.TickCut saved = null;

            try {
                if ((Object)actual == this) {
                    saved = OwnerFullRepairGuard.inventoryTickEntered(actual, item);
                } else {
                    OwnerFullRepairGuard.changed(this, 42);
                }
            } catch (Throwable var14) {
            }

            try {
                original.call(new Object[]{item, actual, world, entity, slot, selected});
            } catch (Throwable var13) {
                Throwable originalFailure = var13;

                try {
                    OwnerFullRepairGuard.inventoryTickReturned(saved, originalFailure);
                } catch (Throwable var11) {
                }

                throw var13;
            }

            try {
                OwnerFullRepairGuard.inventoryTickReturned(saved, null);
            } catch (Throwable var12) {
            }
        } else {
            original.call(new Object[]{item, actual, world, entity, slot, selected});
        }
    }
}
