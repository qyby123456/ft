/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RepairItemRecipe
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={RepairItemRecipe.class}, remap=false)
public abstract class FoodWeaponRepairRecipeMixin {
    @Inject(method={"matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z", "m_5818_(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void ftboss$repairMatch(CraftingContainer craftingContainer, Level level, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (FoodWeaponRepairRecipeMixin.ftboss$protectedInput(craftingContainer)) {
            callbackInfoReturnable.setReturnValue((Object)false);
        }
    }

    @Inject(method={"assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", "m_5874_(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void ftboss$repairAssembly(CraftingContainer craftingContainer, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
        if (FoodWeaponRepairRecipeMixin.ftboss$protectedInput(craftingContainer)) {
            callbackInfoReturnable.setReturnValue((Object)ItemStack.f_41583_);
        }
    }

    private static boolean ftboss$protectedInput(CraftingContainer craftingContainer) {
        for (int i = 0; i < craftingContainer.m_6643_(); ++i) {
            if (!NativeFoodWeaponWear.eligible(craftingContainer.m_8020_(i))) continue;
            return true;
        }
        return false;
    }
}

