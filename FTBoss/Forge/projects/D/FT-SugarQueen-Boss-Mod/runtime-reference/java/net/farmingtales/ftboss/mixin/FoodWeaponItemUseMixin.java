/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Group
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponUseBridge;
import net.farmingtales.ftboss.item.NativeActionObservation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={ItemStack.class}, remap=false)
public abstract class FoodWeaponItemUseMixin {
    @Redirect(method={"m_41682_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;m_7203_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;"), require=0, remap=false)
    @Group(name="ftboss$itemUse", min=1, max=1)
    private InteractionResultHolder<ItemStack> ftboss$useSrg(Item item, Level level, Player player, InteractionHand interactionHand) {
        return NativeActionObservation.invoke(NativeActionObservation.Action.USE, (ItemStack)this, (LivingEntity)player, interactionHand, () -> FoodWeaponUseBridge.use(item, (ItemStack)this, level, player, interactionHand));
    }

    @Redirect(method={"m_41674_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;m_5551_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"), require=0, remap=false)
    @Group(name="ftboss$itemRelease", min=1, max=1)
    private void ftboss$releaseSrg(Item item, ItemStack itemStack, Level level, LivingEntity livingEntity, int n) {
        NativeActionObservation.invoke(NativeActionObservation.Action.RELEASE, itemStack, livingEntity, livingEntity.m_7655_(), () -> {
            FoodWeaponUseBridge.release(item, itemStack, level, livingEntity, n);
            return null;
        });
    }

    @Redirect(method={"m_41731_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;m_5929_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V"), require=0, remap=false)
    @Group(name="ftboss$itemTick", min=1, max=1)
    private void ftboss$tickSrg(Item item, Level level, LivingEntity livingEntity, ItemStack itemStack, int n) {
        NativeActionObservation.invoke(NativeActionObservation.Action.USE_TICK, itemStack, livingEntity, livingEntity.m_7655_(), () -> {
            FoodWeaponUseBridge.useTick(item, itemStack, level, livingEntity, n);
            return null;
        });
    }

    @Redirect(method={"m_41671_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;m_5922_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"), require=0, remap=false)
    @Group(name="ftboss$itemFinish", min=1, max=1)
    private ItemStack ftboss$finishSrg(Item item, ItemStack itemStack, Level level, LivingEntity livingEntity) {
        return NativeActionObservation.invoke(NativeActionObservation.Action.FINISH, itemStack, livingEntity, livingEntity.m_7655_(), () -> FoodWeaponUseBridge.finish(item, itemStack, level, livingEntity));
    }
}

