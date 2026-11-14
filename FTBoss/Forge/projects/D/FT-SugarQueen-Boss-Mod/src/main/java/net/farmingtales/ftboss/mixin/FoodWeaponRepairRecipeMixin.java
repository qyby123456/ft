package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 禁止工作台拼合绕过石像修复；2026-10-13 拦截匹配与直接组装并保留未进化工具原行为。 */
@Mixin(value = RepairItemRecipe.class, remap = false)
public abstract class FoodWeaponRepairRecipeMixin {
    private static boolean ftboss$protectedInput(CraftingContainer inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (FoodWeaponLifecycle.requiresStatueRepair(inventory.getItem(slot))) return true;
        }
        return false;
    }

    @Inject(method = {"matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z",
            "m_5818_(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z"},
            at = @At("HEAD"), cancellable = true, remap = false)
    private void ftboss$repairMatch(CraftingContainer inventory, Level level, CallbackInfoReturnable<Boolean> callback) {
        if (ftboss$protectedInput(inventory)) callback.setReturnValue(false);
    }

    @Inject(method = {"assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
            "m_5874_(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"},
            at = @At("HEAD"), cancellable = true, remap = false)
    private void ftboss$repairAssembly(CraftingContainer inventory, RegistryAccess registry, CallbackInfoReturnable<ItemStack> callback) {
        if (ftboss$protectedInput(inventory)) callback.setReturnValue(ItemStack.EMPTY);
    }
}
