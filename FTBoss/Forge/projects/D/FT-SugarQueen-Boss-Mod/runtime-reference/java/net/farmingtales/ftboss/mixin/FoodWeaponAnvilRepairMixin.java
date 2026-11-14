/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AnvilMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Group
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.minecraft.nbt.CompoundTag;
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

@Mixin(value={AnvilMenu.class}, remap=false)
public abstract class FoodWeaponAnvilRepairMixin {
    @Redirect(method={"m_6640_()V"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;m_41763_()Z", ordinal=2), require=0, remap=false)
    @Group(name="ftbossAnvilRepairBranch", min=1, max=1)
    private boolean ftboss$srgRepairBranch(ItemStack itemStack) {
        return itemStack.m_41763_() && !NativeFoodWeaponWear.eligible(((AnvilMenu)this).m_38853_(0).m_7993_());
    }

    @Inject(method={"createResult()V", "m_6640_()V"}, at={@At(value="RETURN")}, remap=false)
    private void ftboss$preserveResultDamage(CallbackInfo callbackInfo) {
        AnvilMenu anvilMenu = (AnvilMenu)this;
        ItemStack itemStack = anvilMenu.m_38853_(0).m_7993_();
        if (!NativeFoodWeaponWear.eligible(itemStack)) {
            return;
        }
        Slot slot = anvilMenu.m_38853_(2);
        ItemStack itemStack2 = slot.m_7993_();
        if (itemStack2.m_41619_() || !itemStack2.m_150930_(itemStack.m_41720_()) || itemStack2.m_41773_() >= itemStack.m_41773_()) {
            return;
        }
        ItemStack itemStack3 = itemStack2.m_41777_();
        itemStack3.m_41721_(itemStack.m_41773_());
        if (FoodWeaponAnvilRepairMixin.ftboss$onlyRepairChanged(itemStack, itemStack3)) {
            slot.m_5852_(ItemStack.f_41583_);
            anvilMenu.setMaximumCost(0);
        } else {
            slot.m_5852_(itemStack3);
        }
        anvilMenu.m_38946_();
    }

    @Inject(method={"mayPickup(Lnet/minecraft/world/entity/player/Player;Z)Z", "m_6560_(Lnet/minecraft/world/entity/player/Player;Z)Z"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void ftboss$rejectStaleRepair(Player player, boolean bl, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        AnvilMenu anvilMenu = (AnvilMenu)this;
        ItemStack itemStack = anvilMenu.m_38853_(0).m_7993_();
        if (!NativeFoodWeaponWear.eligible(itemStack)) {
            return;
        }
        ItemStack itemStack2 = anvilMenu.m_38853_(2).m_7993_();
        if (!itemStack2.m_41619_() && itemStack2.m_150930_(itemStack.m_41720_()) && itemStack2.m_41773_() < itemStack.m_41773_()) {
            callbackInfoReturnable.setReturnValue((Object)false);
        }
    }

    @Unique
    private static boolean ftboss$onlyRepairChanged(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack.m_41613_() != itemStack2.m_41613_()) {
            return false;
        }
        ItemStack itemStack3 = itemStack.m_41777_();
        ItemStack itemStack4 = itemStack2.m_41777_();
        FoodWeaponAnvilRepairMixin.ftboss$removeRepairFields(itemStack3);
        FoodWeaponAnvilRepairMixin.ftboss$removeRepairFields(itemStack4);
        return ItemStack.m_150942_((ItemStack)itemStack3, (ItemStack)itemStack4);
    }

    @Unique
    private static void ftboss$removeRepairFields(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41783_();
        if (compoundTag == null) {
            return;
        }
        compoundTag.m_128473_("Damage");
        compoundTag.m_128473_("RepairCost");
        if (compoundTag.m_128456_()) {
            itemStack.m_41751_(null);
        }
    }
}

