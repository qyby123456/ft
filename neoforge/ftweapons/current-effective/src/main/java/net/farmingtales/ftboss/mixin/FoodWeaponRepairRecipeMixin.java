package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = {RepairItemRecipe.class},
    remap = false
)
public abstract class FoodWeaponRepairRecipeMixin {
    @Inject(
        method = {"matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z", "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z"},
        at = {@At("HEAD")},
        cancellable = true,
        remap = false
    )
    private void ftboss$repairMatch(CraftingInput var1, Level var2, CallbackInfoReturnable<Boolean> var3) {
        if (ftboss$protectedInput(var1)) {
            var3.setReturnValue(false);
        }
    }

    @Inject(
        method = {"assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;"},
        at = {@At("HEAD")},
        cancellable = true,
        remap = false
    )
    private void ftboss$repairAssembly(CraftingInput var1, HolderLookup.Provider var2, CallbackInfoReturnable<ItemStack> var3) {
        if (ftboss$protectedInput(var1)) {
            var3.setReturnValue(ItemStack.EMPTY);
        }
    }

    private static boolean ftboss$protectedInput(CraftingInput var0) {
        for (int var1 = 0; var1 < var0.size(); var1++) {
            if (NativeFoodWeaponWear.eligible(var0.getItem(var1))) {
                return true;
            }
        }

        return false;
    }
}
