/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Group
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeActionObservation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={ItemStack.class}, remap=false)
public abstract class NativeSpecialMeleeMixin {
    @Redirect(method={"m_41640_(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;m_7579_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"), require=0, remap=false)
    @Group(name="native$actualMelee", min=1, max=1)
    private boolean nativeActualMeleeSrg(Item item, ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        return this.nativeMelee(item, itemStack, livingEntity, livingEntity2);
    }

    private boolean nativeMelee(Item item, ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        boolean bl;
        boolean bl2 = livingEntity2.m_21205_() == itemStack;
        boolean bl3 = bl = livingEntity2.m_21206_() == itemStack;
        InteractionHand interactionHand = bl2 == bl ? null : (bl2 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        return NativeActionObservation.invoke(NativeActionObservation.Action.MELEE_HIT, itemStack, livingEntity2, interactionHand, () -> item.m_7579_(itemStack, livingEntity, livingEntity2));
    }
}

