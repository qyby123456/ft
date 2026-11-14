package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import java.util.function.Consumer;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.render.QingyiConfluenceTridentRenderer;
import net.farmingtales.ftboss.entity.QingyiConfluenceTridentEntity;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.farmingtales.ftboss.skill.QingyiConfluenceSkill;
import net.farmingtales.ftboss.util.QingyiConfluenceRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class QingyiConfluenceTridentItem extends TridentItem implements GeoItem {
    private static final String SKILL_USE = "ftboss:qingyi_skill_use";
    private static final String SKILL_READY_AT = "ftboss:qingyi_skill_ready_at";
    private static final String CONTROLLER = "confluence";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.qingyi_confluence.idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.qingyi_confluence.attack");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay("animation.qingyi_confluence.charge");
    private static final RawAnimation RELEASE = RawAnimation.begin().thenPlay("animation.qingyi_confluence.release");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Multimap<Attribute, AttributeModifier> modifiers;

    public QingyiConfluenceTridentItem() {
        super(new Properties().durability(FTWeaponTier.INSTANCE.getUses()).rarity(Rarity.EPIC).fireResistant());
        modifiers = ImmutableMultimap.<Attribute, AttributeModifier>builder()
                .put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                        // 2026-09-18 按用户要求「青衣的掉落三叉戟伤害翻倍」：近战基础 8 → 16
                        16 + FTWeaponTier.INSTANCE.getAttackDamageBonus(), AttributeModifier.Operation.ADDITION))
                .put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                        -2.8F, AttributeModifier.Operation.ADDITION)).build();
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? modifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public int getEnchantmentValue() {
        return FTWeaponTier.INSTANCE.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return FTWeaponTier.INSTANCE.getRepairIngredient().test(ingredient) || super.isValidRepairItem(stack, ingredient);
    }

    private static boolean isSkillUse(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(SKILL_USE);
    }

    private static void clearSkillUse(ItemStack stack) {
        if (stack.hasTag()) stack.getTag().remove(SKILL_USE);
    }

    private static long skillCooldown(Player player) {
        return QingyiConfluenceRules.skillCooldownRemaining(player.level().getGameTime(),
                player.getPersistentData().getLong(SKILL_READY_AT));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 0, state -> state.setAndContinue(IDLE))
                .triggerableAnim("attack", ATTACK)
                .triggerableAnim("charge", CHARGE)
                .triggerableAnim("release", RELEASE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static void animate(LivingEntity holder, String action) {
        ItemStack stack = holder.getMainHandItem();
        if (stack.getItem() instanceof QingyiConfluenceTridentItem item && holder.level() instanceof ServerLevel level) {
            item.triggerAnim(holder, GeoItem.getOrAssignId(stack, level), CONTROLLER, action);
        }
    }

    public static void cancel(LivingEntity holder, ItemStack stack) {
        if (stack.getItem() instanceof QingyiConfluenceTridentItem item && holder.level() instanceof ServerLevel
                && stack.hasTag() && stack.getTag().contains(GeoItem.ID_NBT_KEY, 99)) {
            long instanceId = GeoItem.getId(stack);
            item.stopTriggeredAnim(holder, instanceId, CONTROLLER, "charge");
            item.stopTriggeredAnim(holder, instanceId, CONTROLLER, "release");
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity user, int remaining) {
        if (!isSkillUse(stack)) return;
        clearSkillUse(stack);
        cancel(user, stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (selected && entity instanceof Player player && level instanceof ServerLevel serverLevel) {
            GeoItem.getOrAssignId(stack, serverLevel);
            if (player.swinging && player.swingTime == 1 && !player.isUsingItem()) animate(player, "attack");
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (hit && !attacker.level().isClientSide && QingyiConfluenceSkill.canTarget(attacker, target)) {
            QingyiConfluenceSkill.mark(target, 40);
        }
        return hit;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        clearSkillUse(stack);
        if (!player.isAlive() || player.isSpectator()
                || stack.getDamageValue() >= stack.getMaxDamage() - 1) return InteractionResultHolder.fail(stack);
        if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) {
            InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
            if (!level.isClientSide) FTBossMod.LOGGER.info("[FTBoss][QingyiUse] throw start player={} result={} duration={}",
                    player.getName().getString(), result.getResult(), player.getUseItemRemainingTicks());
            return result;
        }
        if (player.getCooldowns().isOnCooldown(this) || skillCooldown(player) > 0) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable(
                    "tooltip.ftboss.qingyi_confluence_trident.cooldown", (skillCooldown(player) + 19) / 20), true);
            return InteractionResultHolder.fail(stack);
        }
        stack.getOrCreateTag().putBoolean(SKILL_USE, true);
        player.startUsingItem(hand);
        animate(player, "charge");
        if (!level.isClientSide) FTBossMod.LOGGER.info("[FTBoss][QingyiUse] skill start player={} duration={}",
                player.getName().getString(), player.getUseItemRemainingTicks());
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return isSkillUse(stack) ? QingyiConfluenceRules.useTicks() : QingyiConfluenceRules.THROW_USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (!isSkillUse(stack) || !(level instanceof ServerLevel serverLevel)) return;
        int elapsed = getUseDuration(stack) - remaining + 1;
        if (elapsed == QingyiConfluenceRules.CHARGE_TICKS) animate(user, "release");
        if (elapsed < QingyiConfluenceRules.CHARGE_TICKS && elapsed % 4 == 0) {
            QingyiConfluenceSkill.chargeFx(serverLevel, user, elapsed);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
        if (isSkillUse(stack)) {
            clearSkillUse(stack);
            cancel(user, stack);
            if (!level.isClientSide) FTBossMod.LOGGER.info("[FTBoss][QingyiUse] skill cancelled player={} remaining={}",
                    user.getName().getString(), remaining);
            return;
        }
        if (!(user instanceof Player player) || !player.isAlive() || player.isSpectator()
                || player.getItemInHand(player.getUsedItemHand()) != stack || !QingyiConfluenceRules.canThrow(remaining)) return;
        if (EnchantmentHelper.getRiptide(stack) > 0) {
            super.releaseUsing(stack, level, user, remaining);
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) return;
        stack.hurtAndBreak(1, player, holder -> holder.broadcastBreakEvent(player.getUsedItemHand()));
        QingyiConfluenceTridentEntity trident = new QingyiConfluenceTridentEntity(serverLevel, player, stack);
        trident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, TridentItem.SHOOT_POWER, 1.0F);
        trident.pickup = player.getAbilities().instabuild ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
        if (!serverLevel.addFreshEntity(trident)) return;
        if (!player.getAbilities().instabuild) player.getInventory().removeItem(stack);
        FTBossMod.LOGGER.info("[FTBoss][QingyiUse] thrown player={} entity={} chargeTicks={}",
                player.getName().getString(), trident.getId(), QingyiConfluenceRules.THROW_USE_TICKS - remaining);
        player.awardStat(Stats.ITEM_USED.get(this));
        serverLevel.playSound(null, trident.getX(), trident.getY(), trident.getZ(),
                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        boolean skill = isSkillUse(stack);
        clearSkillUse(stack);
        if (!skill) return stack;
        if (!(level instanceof ServerLevel serverLevel) || !(user instanceof Player player)
                || !player.isAlive() || player.isSpectator() || player.getMainHandItem() != stack
                || player.getCooldowns().isOnCooldown(this) || skillCooldown(player) > 0) return stack;
        QingyiConfluenceSkill.burst(serverLevel, player, QingyiConfluenceSkill.aim(player));
        FTBossMod.LOGGER.info("[FTBoss][QingyiUse] skill released player={}", player.getName().getString());
        player.getPersistentData().putLong(SKILL_READY_AT, level.getGameTime() + QingyiConfluenceRules.COOLDOWN_TICKS);
        stack.hurtAndBreak(3, player, holder -> holder.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        lines.add(Component.translatable("tooltip.ftboss.qingyi_confluence_trident.elements").withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("tooltip.ftboss.qingyi_confluence_trident.throw").withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("tooltip.ftboss.qingyi_confluence_trident.skill").withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.ftboss.qingyi_confluence_trident.cancel").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private QingyiConfluenceTridentRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new QingyiConfluenceTridentRenderer();
                return renderer;
            }
        });
    }
}
