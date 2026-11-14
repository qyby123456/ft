package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.CandyStatueBlock;
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

/** 食武不可用与普通修复拦截；2026-10-13 保留石像入口，提示区分基础功能不足与耐久耗尽，不改法术或净化。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class FoodWeaponUseGuard {
    private FoodWeaponUseGuard() {}

    private static void deny(PlayerInteractEvent event) {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void attack(AttackEntityEvent event) {
        if (FoodWeaponLifecycle.isBroken(event.getEntity().getMainHandItem())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void useItem(PlayerInteractEvent.RightClickItem event) {
        if (FoodWeaponLifecycle.isBroken(event.getItemStack())) deny(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void useBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!FoodWeaponLifecycle.isBroken(event.getItemStack())
                || event.getLevel().getBlockState(event.getPos()).getBlock() instanceof CandyStatueBlock) return;
        deny(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void useEntity(PlayerInteractEvent.EntityInteract event) {
        if (FoodWeaponLifecycle.isBroken(event.getItemStack())) deny(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void useEntityPart(PlayerInteractEvent.EntityInteractSpecific event) {
        if (FoodWeaponLifecycle.isBroken(event.getItemStack())) deny(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void breakSpeed(PlayerEvent.BreakSpeed event) {
        if (FoodWeaponLifecycle.isBroken(event.getEntity().getMainHandItem())) {
            event.setNewSpeed(0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void breakBlock(BlockEvent.BreakEvent event) {
        if (FoodWeaponLifecycle.isBroken(event.getPlayer().getMainHandItem())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void startUse(LivingEntityUseItemEvent.Start event) {
        if (FoodWeaponLifecycle.isBroken(event.getItem())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void releaseUse(LivingEntityUseItemEvent.Stop event) {
        if (FoodWeaponLifecycle.isBroken(event.getItem())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void stopBrokenUse(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player.isUsingItem()
                && FoodWeaponLifecycle.isBroken(event.player.getUseItem())) event.player.stopUsingItem();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void anvilRepair(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (FoodWeaponLifecycle.requiresStatueRepair(right) && right.is(left.getItem())) {
            event.setCanceled(true);
            return;
        }
        if (!FoodWeaponLifecycle.requiresStatueRepair(left)) return;
        if (right.is(left.getItem()) || left.getItem().isValidRepairItem(left, right)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void grindstoneRepair(GrindstoneEvent.OnPlaceItem event) {
        if (event.getTopItem().isEmpty() || event.getBottomItem().isEmpty()) return;
        if (FoodWeaponLifecycle.requiresStatueRepair(event.getTopItem())
                || FoodWeaponLifecycle.requiresStatueRepair(event.getBottomItem())) {
            event.setOutput(ItemStack.EMPTY);
            event.setXp(0);
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!FoodWeaponLifecycle.managed(stack)) return;
        if (FoodWeaponLifecycle.durabilityDepleted(stack)) event.getToolTip().add(
                Component.translatable("tooltip.ftboss.weapon.broken").withStyle(ChatFormatting.RED));
        if (FoodWeaponLifecycle.requiresStatueRepair(stack)) event.getToolTip().add(
                Component.translatable("tooltip.ftboss.weapon.statue_repair",
                        Component.translatable("profile.ftboss.evolution." + FoodWeaponLifecycle.repairProfile(stack)))
                        .withStyle(ChatFormatting.GOLD));
        event.getToolTip().add(Component.translatable("tooltip.ftboss.weapon.wear_loss").withStyle(ChatFormatting.DARK_GRAY));
    }
}
