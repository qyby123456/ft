package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.farmingtales.ftboss.item.NativeFoodWeaponRepairComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = {AnvilMenu.class},
    remap = false
)
public abstract class FoodWeaponAnvilRepairMixin {
    @Redirect(
        method = {"createResult()V"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isDamageableItem()Z",
            ordinal = 2
        ),
        require = 0,
        remap = false
    )
    @Group(
        name = "ftbossAnvilRepairBranch",
        min = 1,
        max = 1
    )
    private boolean ftboss$srgRepairBranch(ItemStack var1) {
        return var1.isDamageableItem() && !NativeFoodWeaponWear.eligible(((AnvilMenu)(Object)this).getSlot(0).getItem());
    }

    @Inject(
        method = {"createResult()V", "createResult()V"},
        at = {@At("RETURN")},
        remap = false
    )
    private void ftboss$preserveResultDamage(CallbackInfo var1) {
        AnvilMenu var2 = (AnvilMenu)(Object)this;
        ItemStack var3 = var2.getSlot(0).getItem();
        if (NativeFoodWeaponWear.eligible(var3)) {
            Slot var4 = var2.getSlot(2);
            ItemStack var5 = var4.getItem();
            if (!var5.isEmpty() && var5.is(var3.getItem()) && var5.getDamageValue() < var3.getDamageValue()) {
                ItemStack var6 = var5.copy();
                var6.setDamageValue(var3.getDamageValue());
                if (ftboss$onlyRepairChanged(var3, var6)) {
                    var4.set(ItemStack.EMPTY);
                    var2.setMaximumCost(0);
                } else {
                    var4.set(var6);
                }

                var2.broadcastChanges();
            }
        }
    }

    @Inject(
        method = {"mayPickup(Lnet/minecraft/world/entity/player/Player;Z)Z", "mayPickup(Lnet/minecraft/world/entity/player/Player;Z)Z"},
        at = {@At("HEAD")},
        cancellable = true,
        remap = false
    )
    private void ftboss$rejectStaleRepair(Player var1, boolean var2, CallbackInfoReturnable<Boolean> var3) {
        AnvilMenu var4 = (AnvilMenu)(Object)this;
        ItemStack var5 = var4.getSlot(0).getItem();
        if (NativeFoodWeaponWear.eligible(var5)) {
            ItemStack var6 = var4.getSlot(2).getItem();
            if (!var6.isEmpty() && var6.is(var5.getItem()) && var6.getDamageValue() < var5.getDamageValue()) {
                var3.setReturnValue(false);
            }
        }
    }

    @Unique
    private static boolean ftboss$onlyRepairChanged(ItemStack before, ItemStack after) {
        return NativeFoodWeaponRepairComponents.onlyRepairChanged(before, after);
    }
}
