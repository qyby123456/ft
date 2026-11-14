/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.AnvilUpdateEvent
 *  net.minecraftforge.event.GrindstoneEvent$OnPlaceItem
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingEntityUseItemEvent$Start
 *  net.minecraftforge.event.entity.living.LivingEntityUseItemEvent$Stop
 *  net.minecraftforge.event.entity.player.AttackEntityEvent
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$BreakSpeed
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$EntityInteract
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$EntityInteractSpecific
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickItem
 *  net.minecraftforge.event.level.BlockEvent$BreakEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class FoodWeaponUseGuard {
    private FoodWeaponUseGuard() {
    }

    private static void deny(PlayerInteractEvent playerInteractEvent) {
        playerInteractEvent.setCanceled(true);
        playerInteractEvent.setCancellationResult(InteractionResult.FAIL);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void attack(AttackEntityEvent attackEntityEvent) {
        if (FoodWeaponLifecycle.isBroken(attackEntityEvent.getEntity().m_21205_())) {
            attackEntityEvent.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void useItem(PlayerInteractEvent.RightClickItem rightClickItem) {
        if (FoodWeaponLifecycle.isBroken(rightClickItem.getItemStack())) {
            FoodWeaponUseGuard.deny((PlayerInteractEvent)rightClickItem);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void useBlock(PlayerInteractEvent.RightClickBlock rightClickBlock) {
        if (!FoodWeaponLifecycle.isBroken(rightClickBlock.getItemStack()) || rightClickBlock.getLevel().m_8055_(rightClickBlock.getPos()).m_60734_() instanceof CandyStatueBlock) {
            return;
        }
        FoodWeaponUseGuard.deny((PlayerInteractEvent)rightClickBlock);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void useEntity(PlayerInteractEvent.EntityInteract entityInteract) {
        if (FoodWeaponLifecycle.isBroken(entityInteract.getItemStack())) {
            FoodWeaponUseGuard.deny((PlayerInteractEvent)entityInteract);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void useEntityPart(PlayerInteractEvent.EntityInteractSpecific entityInteractSpecific) {
        if (FoodWeaponLifecycle.isBroken(entityInteractSpecific.getItemStack())) {
            FoodWeaponUseGuard.deny((PlayerInteractEvent)entityInteractSpecific);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void breakSpeed(PlayerEvent.BreakSpeed breakSpeed) {
        if (FoodWeaponLifecycle.isBroken(breakSpeed.getEntity().m_21205_())) {
            breakSpeed.setNewSpeed(0.0f);
            breakSpeed.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void breakBlock(BlockEvent.BreakEvent breakEvent) {
        if (FoodWeaponLifecycle.isBroken(breakEvent.getPlayer().m_21205_())) {
            breakEvent.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void startUse(LivingEntityUseItemEvent.Start start) {
        if (FoodWeaponLifecycle.isBroken(start.getItem())) {
            start.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void releaseUse(LivingEntityUseItemEvent.Stop stop) {
        if (FoodWeaponLifecycle.isBroken(stop.getItem())) {
            stop.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void stopBrokenUse(TickEvent.PlayerTickEvent playerTickEvent) {
        if (playerTickEvent.phase == TickEvent.Phase.END && playerTickEvent.player.m_6117_() && FoodWeaponLifecycle.isBroken(playerTickEvent.player.m_21211_())) {
            playerTickEvent.player.m_5810_();
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent itemTooltipEvent) {
        ItemStack itemStack = itemTooltipEvent.getItemStack();
        if (!FoodWeaponLifecycle.managed(itemStack)) {
            return;
        }
        if (FoodWeaponLifecycle.durabilityDepleted(itemStack)) {
            itemTooltipEvent.getToolTip().add(Component.m_237115_((String)"tooltip.ftboss.weapon.broken").m_130940_(ChatFormatting.RED));
        }
        if (FoodWeaponLifecycle.requiresStatueRepair(itemStack)) {
            itemTooltipEvent.getToolTip().add(Component.m_237110_((String)"tooltip.ftboss.weapon.statue_repair", (Object[])new Object[]{Component.m_237115_((String)("profile.ftboss.evolution." + FoodWeaponLifecycle.repairProfile(itemStack)))}).m_130940_(ChatFormatting.GOLD));
        }
        itemTooltipEvent.getToolTip().add(Component.m_237115_((String)"tooltip.ftboss.weapon.wear_loss").m_130940_(ChatFormatting.DARK_GRAY));
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void anvilRepair(AnvilUpdateEvent anvilUpdateEvent) {
        ItemStack itemStack = anvilUpdateEvent.getLeft();
        ItemStack itemStack2 = anvilUpdateEvent.getRight();
        if (!NativeFoodWeaponWear.eligible(itemStack) || itemStack2.m_41619_()) {
            return;
        }
        if (itemStack.m_41720_().m_6832_(itemStack, itemStack2)) {
            anvilUpdateEvent.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void grindstoneRepair(GrindstoneEvent.OnPlaceItem onPlaceItem) {
        if (onPlaceItem.getTopItem().m_41619_() || onPlaceItem.getBottomItem().m_41619_()) {
            return;
        }
        if (NativeFoodWeaponWear.eligible(onPlaceItem.getTopItem()) || NativeFoodWeaponWear.eligible(onPlaceItem.getBottomItem())) {
            onPlaceItem.setCanceled(true);
        }
    }
}

