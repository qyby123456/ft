package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.boss.ai.PlayerWeaponTargeting;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.WeaponFxRules.Kind;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;
import net.farmingtales.ftboss.util.DoraWeaponRules;
import net.farmingtales.ftboss.util.DoraWeaponRules.Mode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

/** 朵拉独立枪/长枪；2026-09-17，12 tick 起效、24 tick 满蓄力，射线遇墙停止，不破坏方块、不依赖外部弹药。 */
public final class DoraWeaponItem extends SwordItem {
    private static final int USE_TICKS = 72000;
    private final boolean lance;

    public DoraWeaponItem(boolean lance) {
        super(FTWeaponTier.INSTANCE, lance ? 4 : 1, lance ? -2.6F : -3.0F,
                new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
        this.lance = lance;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return lance ? UseAnim.SPEAR : UseAnim.BOW;
    }

    /**
     * 地脉脉冲枪的附魔白名单（2026-09-18 按用户口径：「枪类可以只有耐久等」）。
     *
     * <p>它继承 {@code SwordItem}，默认会开放**全套剑附魔**；但它是一把蓄力射击的枪，
     * 实战几乎不靠近战，锋利那类附魔等于白占位。所以只留保养类。
     *
     * <p><b>鸣脉长枪（lance = true）不设限</b> —— 它是真的近战长枪，锋利 / 横扫之刃都该能用。
     */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.world.item.enchantment.Enchantment enchantment) {
        if (lance) return super.canApplyAtEnchantingTable(stack, enchantment);
        return enchantment == net.minecraft.world.item.enchantment.Enchantments.UNBREAKING
                || enchantment == net.minecraft.world.item.enchantment.Enchantments.MENDING
                || enchantment == net.minecraft.world.item.enchantment.Enchantments.VANISHING_CURSE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.isAlive() || player.isSpectator()
                || player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (lance || !(user instanceof ServerPlayer player) || remaining % 4 != 0) return;
        Vec3 direction = player.getLookAngle();
        Vec3 chargePoint = player.getEyePosition().add(direction.scale(0.8)).add(0, -0.15, 0);
        WeaponFx.send(player.serverLevel(), Kind.DORA_CHARGE, Theme.ARC, chargePoint,
                chargePoint.add(direction.scale(0.36)), 0.32, player.getYRot());
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
        if (!(user instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()
                || player.getMainHandItem() != stack || player.getCooldowns().isOnCooldown(this)) return;
        double charge = DoraWeaponRules.charge(USE_TICKS - remaining);
        if (charge == 0) return;
        Mode mode = DoraWeaponRules.mode(lance, player.isShiftKeyDown());
        player.getCooldowns().addCooldown(this, mode.cooldownTicks);
        if (lance) strike(player, mode, charge);
        else shoot(player, mode, charge);
        stack.hurtAndBreak(mode.durabilityCost, player, owner -> owner.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        player.causeFoodExhaustion(0.15F);
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    private static boolean canTarget(ServerPlayer player, LivingEntity target) {
        return PlayerWeaponTargeting.canTarget(player, target);
    }

    private static boolean visible(ServerPlayer player, Vec3 from, Vec3 to) {
        return PlayerWeaponTargeting.hasClearPath(player, from, to);
    }

    private static void shoot(ServerPlayer player, Mode mode, double charge) {
        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getLookAngle();
        Vec3 reach = start.add(direction.scale(DoraWeaponRules.GUN_RANGE));
        Vec3 end = level.clip(new ClipContext(start, reach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
        LivingEntity hit = null;
        double nearest = start.distanceToSqr(end);
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(1),
                entity -> entity != player && entity.isAlive() && !entity.isSpectator())) {
            AABB bounds = candidate.getBoundingBox().inflate(0.18);
            Vec3 point = bounds.contains(start) ? start : bounds.clip(start, end).orElse(null);
            if (point != null && start.distanceToSqr(point) < nearest) {
                nearest = start.distanceToSqr(point);
                end = point;
                hit = candidate;
            }
        }
        WeaponFx.send(level, mode == Mode.NOVA ? Kind.DORA_NOVA : Kind.DORA_PULSE,
                mode == Mode.NOVA ? Theme.GOLD : Theme.ARC, start, end, 0.18, player.getYRot());
        if (mode == Mode.NOVA) {
            Vec3 impact = end.subtract(direction.scale(Math.min(0.08, start.distanceTo(end))));
            double radius = DoraWeaponRules.NOVA_RADIUS;
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(impact, impact).inflate(radius))) {
                if (!canTarget(player, target) || target.getBoundingBox().getCenter().distanceToSqr(impact) > radius * radius
                        || !visible(player, impact, target.getBoundingBox().getCenter())) continue;
                target.hurt(player.damageSources().playerAttack(player), (float) (mode.damage() * charge));
            }
            WeaponFx.send(level, Kind.DORA_IMPACT, Theme.GOLD, impact, impact, radius, player.getYRot());
            level.playSound(null, impact.x, impact.y, impact.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.65F, 1.65F);
        } else if (hit != null && canTarget(player, hit)) {
            hit.hurt(player.damageSources().playerAttack(player), (float) (mode.damage() * charge));
        }
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.65F, 1.6F);
    }

    private static void strike(ServerPlayer player, Mode mode, double charge) {
        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        Vec3 forward = player.getLookAngle();
        double yaw = Math.toRadians(player.getYRot());
        Vec3 horizontal = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        double range = mode == Mode.THRUST ? DoraWeaponRules.THRUST_RANGE : DoraWeaponRules.SWEEP_RANGE;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range))) {
            if (!canTarget(player, target)) continue;
            Vec3 center = target.getBoundingBox().getCenter();
            Vec3 offset = center.subtract(start);
            double along = offset.dot(forward);
            Vec3 planar = offset.multiply(1, 0, 1).normalize();
            boolean inside = mode == Mode.THRUST
                    ? DoraWeaponRules.inThrust(along, Math.max(0, offset.lengthSqr() - along * along))
                    : DoraWeaponRules.inSweep(offset.lengthSqr(), planar.dot(horizontal));
            if (!inside || !visible(player, start, center)) continue;
            if (target.hurt(player.damageSources().playerAttack(player), (float) (mode.damage() * charge))) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 0));
                target.push(planar.x * 0.3, 0.08, planar.z * 0.3);
                target.hurtMarked = true;
            }
        }
        if (mode == Mode.THRUST) {
            Vec3 end = level.clip(new ClipContext(start, start.add(forward.scale(range)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
            WeaponFx.beam(level, start, end, Theme.ARC);
        } else {
            Vec3 origin = player.position().add(0, 1, 0);
            WeaponFx.send(level, Kind.SWEEP, Theme.GOLD, origin, origin, range, player.getYRot());
        }
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.9F, 0.85F);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        String prefix = lance ? "tooltip.ftboss.dora_lance" : "tooltip.ftboss.dora_pulse_gun";
        tooltip.add(Component.translatable(prefix + ".use").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable(prefix + ".shift").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.ftboss.dora_weapon.charge").withStyle(ChatFormatting.GRAY));
    }
}
