package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.boss.ai.PlayerWeaponTargeting;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.WeaponFxRules.Kind;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;

import net.farmingtales.ftboss.item.FTWeaponTier;
import net.farmingtales.ftboss.util.DoraSmgRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class DoraSubmachineGunItem extends SwordItem {
    private static final int USE_TICKS = 72000;
    private static final String ROUNDS = "FTBossDoraSmgRounds";
    private static final String RELOAD = "FTBossDoraSmgReload";
    private static final ResourceKey<DamageType> ROUND_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation("ftboss", "dora_smg_round"));

    public DoraSubmachineGunItem() {
        super(FTWeaponTier.INSTANCE, 1, -3.0F, new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    private static int rounds(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(ROUNDS)
                ? DoraSmgRules.rounds(stack.getTag().getInt(ROUNDS)) : DoraSmgRules.MAGAZINE_SIZE;
    }

    private static int reloadTicks(ItemStack stack) {
        return stack.hasTag() ? DoraSmgRules.reloadTicks(stack.getTag().getInt(RELOAD)) : 0;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    /**
     * 枪类附魔白名单（2026-09-18 按用户口径：「枪类可以只有耐久等」）。
     *
     * <p>本类继承 {@code SwordItem}，默认会把**全套剑附魔**（锋利 / 火焰附加 / 击退 / 抢夺 / 横扫之刃…）
     * 放进附魔台。但这把枪的实战输出是**连发弹道**，那套剑附魔一个都用不上 ——
     * 挂上去只会白占附魔位、误导玩家。所以只留"保养类"：耐久 / 经验修补 / 消失诅咒。
     *
     * <p>这与「附魔只适配白板基础伤害、技能不吃附魔」的口径一致：
     * 本模组的弹道与技能伤害都由固定常量决定，枪类压根不需要伤害类附魔。
     */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.world.item.enchantment.Enchantment enchantment) {
        return enchantment == net.minecraft.world.item.enchantment.Enchantments.UNBREAKING
                || enchantment == net.minecraft.world.item.enchantment.Enchantments.MENDING
                || enchantment == net.minecraft.world.item.enchantment.Enchantments.VANISHING_CURSE;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.isAlive() || player.isSpectator()
                || player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) startReload(serverPlayer, stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        player.startUsingItem(hand);
        if (player instanceof ServerPlayer serverPlayer) fireOrReload(serverPlayer, stack);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (user instanceof ServerPlayer player && player.isAlive() && !player.isSpectator()
                && player.getMainHandItem() == stack && player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
            fireOrReload(player, stack);
        }
    }

    private void fireOrReload(ServerPlayer player, ItemStack stack) {
        if (reloadTicks(stack) > 0 || player.getCooldowns().isOnCooldown(this)) return;
        int available = rounds(stack);
        if (available == 0) {
            startReload(player, stack);
            return;
        }
        player.getCooldowns().addCooldown(this, DoraSmgRules.SHOT_INTERVAL);
        stack.getOrCreateTag().putInt(ROUNDS, available - 1);
        shoot(player);
        stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        player.causeFoodExhaustion(0.015F);
        if (stack.isEmpty()) player.stopUsingItem();
        else if (available == 1) startReload(player, stack);
        else showStatus(player, stack);
    }

    private static void startReload(ServerPlayer player, ItemStack stack) {
        if (reloadTicks(stack) > 0 || rounds(stack) == DoraSmgRules.MAGAZINE_SIZE) return;
        stack.getOrCreateTag().putInt(RELOAD, DoraSmgRules.RELOAD_TICKS);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.CROSSBOW_LOADING_START,
                SoundSource.PLAYERS, 0.65F, 1.3F);
        showStatus(player, stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!(entity instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()
                || player.getMainHandItem() != stack) return;
        int remaining = reloadTicks(stack);
        if (remaining == 0) return;
        if (remaining == 1) {
            stack.getOrCreateTag().remove(RELOAD);
            stack.getOrCreateTag().putInt(ROUNDS, DoraSmgRules.MAGAZINE_SIZE);
            level.playSound(null, player.blockPosition(), SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS, 0.7F, 1.5F);
        } else stack.getOrCreateTag().putInt(RELOAD, remaining - 1);
        if (remaining == 1 || remaining % 4 == 0) showStatus(player, stack);
    }

    private static void showStatus(ServerPlayer player, ItemStack stack) {
        int reload = reloadTicks(stack);
        Component status = reload > 0
                ? Component.translatable("message.ftboss.dora_smg.reload", (reload + 1) / 2 / 10.0)
                : Component.translatable("message.ftboss.dora_smg.ammo", rounds(stack), DoraSmgRules.MAGAZINE_SIZE);
        player.displayClientMessage(status.copy().withStyle(reload > 0 ? ChatFormatting.GOLD : ChatFormatting.AQUA), true);
    }

    private static boolean canTarget(ServerPlayer player, LivingEntity target) {
        return PlayerWeaponTargeting.canTarget(player, target);
    }

    private static void shoot(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        double spread = player.isShiftKeyDown() ? 0.003 : 0.009;
        Vec3 direction = player.getLookAngle().add(player.getRandom().nextGaussian() * spread,
                player.getRandom().nextGaussian() * spread, player.getRandom().nextGaussian() * spread).normalize();
        Vec3 end = level.clip(new ClipContext(start, start.add(direction.scale(DoraSmgRules.RANGE)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
        LivingEntity hit = null;
        double nearest = start.distanceToSqr(end);
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(1),
                target -> target != player && target.isAlive() && !target.isSpectator())) {
            AABB bounds = candidate.getBoundingBox().inflate(0.08);
            Vec3 point = bounds.contains(start) ? start : bounds.clip(start, end).orElse(null);
            if (point != null && start.distanceToSqr(point) < nearest) {
                nearest = start.distanceToSqr(point);
                end = point;
                hit = candidate;
            }
        }
        if (hit != null && canTarget(player, hit)) {
            DamageSource source = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(ROUND_DAMAGE), player);
            hit.hurt(source, DoraSmgRules.damage(Math.sqrt(nearest)));
        }
        WeaponFx.send(level, Kind.DORA_TRACER, Theme.ARC, start, end, 0.12, player.getYRot());
        level.playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS,
                0.45F, 1.8F + player.getRandom().nextFloat() * 0.15F);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ftboss.dora_nova_launcher.use").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.ftboss.dora_nova_launcher.shift").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.ftboss.dora_smg.stats").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("message.ftboss.dora_smg.ammo", rounds(stack), DoraSmgRules.MAGAZINE_SIZE)
                .withStyle(ChatFormatting.DARK_AQUA));
        if (reloadTicks(stack) > 0) tooltip.add(Component.translatable("message.ftboss.dora_smg.reload",
                (reloadTicks(stack) + 1) / 2 / 10.0).withStyle(ChatFormatting.GOLD));
    }
}
