package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import java.util.function.Consumer;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Creative-only blank weapon. It deliberately has no built-in attack modifier: every combat
 * attribute, passive food skill and visual sigil is earned through statue evolution or sockets.
 */
public final class GourmetBlankBladeItem extends Item implements GeoItem, GourmetEvolutionWeapon {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.gourmet_blank_blade.idle");
    private static final RawAnimation EVOLVE = RawAnimation.begin().thenPlay("animation.gourmet_blank_blade.evolve");
    private static final RawAnimation SOCKET = RawAnimation.begin().thenPlay("animation.gourmet_blank_blade.socket");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.gourmet_blank_blade.attack");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GourmetBlankBladeItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "blade", 2, state -> state.setAndContinue(IDLE))
                .triggerableAnim("evolve", EVOLVE)
                .triggerableAnim("socket", SOCKET)
                .triggerableAnim("attack", ATTACK));
    }

    public static void animateEvolution(ServerPlayer holder, ItemStack stack) {
        if (!(stack.getItem() instanceof GourmetBlankBladeItem blade)) return;
        blade.triggerAnim(holder, GeoItem.getOrAssignId(stack, holder.serverLevel()), "blade", "evolve");
    }

    public static void animateSocket(ServerPlayer holder, ItemStack stack) {
        if (!(stack.getItem() instanceof GourmetBlankBladeItem blade)) return;
        blade.triggerAnim(holder, GeoItem.getOrAssignId(stack, holder.serverLevel()), "blade", "socket");
    }

    public static void animateAttack(ServerPlayer holder, ItemStack stack) {
        if (!(stack.getItem() instanceof GourmetBlankBladeItem blade)) return;
        blade.triggerAnim(holder, GeoItem.getOrAssignId(stack, holder.serverLevel()), "blade", "attack");
    }

    @Override
    public void triggerEvolution(ServerPlayer holder, ItemStack stack) {
        triggerAnim(holder, GeoItem.getOrAssignId(stack, holder.serverLevel()), "blade", "evolve");
    }

    @Override
    public void triggerSocket(ServerPlayer holder, ItemStack stack) {
        triggerAnim(holder, GeoItem.getOrAssignId(stack, holder.serverLevel()), "blade", "socket");
    }

    @Override
    public void triggerActive(ServerPlayer holder, ItemStack stack) {
        // The blade uses its existing flourish when a J active skill is released.
        triggerAnim(holder, GeoItem.getOrAssignId(stack, holder.serverLevel()), "blade", "evolve");
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    /** The blade has no durability by design, so the vanilla Item implementation would reject enchanting. */
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return 22;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.canEnchant(new ItemStack(Items.NETHERITE_SWORD));
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return EnchantmentHelper.getEnchantments(book).keySet().stream()
                .allMatch(enchantment -> canApplyAtEnchantingTable(stack, enchantment));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        super.inventoryTick(stack, level, holder, slot, selected);
        if (!selected || !(level instanceof ServerLevel)) return;
        if (holder instanceof ServerPlayer player
                && player.swinging && player.swingTime == 1 && !player.isUsingItem()) {
            animateAttack(player, stack);
        }
        GourmetEvolutionWeapon.inventoryParticles(stack, level, holder, selected);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        GourmetEvolutionWeapon.droppedParticles(stack, entity);
        return false;
    }

    public static void playPickupEffect(EntityItemPickupEvent event) {
        GourmetEvolutionWeapon.playPickupEffect(event);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        lines.add(Component.translatable("tooltip.ftboss.gourmet.zero_base")
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.ftboss.gourmet.capacity",
                FoodWeaponEvolution.GOURMET_SOCKET_CAPACITY, FoodWeaponEvolution.GOURMET_MAX_LEVEL)
                .withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("tooltip.ftboss.gourmet.use").withStyle(ChatFormatting.DARK_GRAY));
        net.farmingtales.ftboss.item.GourmetAccessoryEffects.tooltip(lines);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new net.farmingtales.ftboss.client.render.GourmetBlankBladeRenderer();
                }
                return renderer;
            }
        });
    }
}
