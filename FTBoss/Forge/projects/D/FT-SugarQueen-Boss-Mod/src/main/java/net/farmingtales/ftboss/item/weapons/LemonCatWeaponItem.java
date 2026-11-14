package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.boss.ai.PlayerWeaponTargeting;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.farmingtales.ftboss.skill.LemonCatNetwork;
import net.farmingtales.ftboss.util.LemonCatWeaponRules;
import net.farmingtales.ftboss.util.ManorWeaponRules;
import net.farmingtales.ftboss.util.ManorWeaponBalanceRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class LemonCatWeaponItem extends SwordItem {
    public enum Kind { SKILLET, SPATULA }

    public static final int SKILLET_COOLDOWN = 160;
    public static final int SPATULA_COOLDOWN = 280;
    /** 技能基础伤害/治疗；实际结算与显示都走 {@link #skilletDamage(boolean)} / {@link #healAmount(boolean)}（含庄园技能倍率）。 */
    public static final float SKILLET_DAMAGE = 24.0F;
    public static final float PAIRED_DAMAGE = 32.0F;
    public static final double SKILLET_RANGE = 4.5D;
    public static final double PAIRED_RANGE = 5.5D;
    public static final float HEAL_AMOUNT = 8.0F;
    public static final float PAIRED_HEAL_AMOUNT = 12.0F;
    public static final int REGEN_TICKS = 100;
    public static final int SPEED_TICKS = 120;
    private final Kind kind;

    /** 翻锅伤害（庄园技能倍率已计入）。 */
    public static float skilletDamage(boolean paired) {
        return paired ? 96.0F : 72.0F;
    }

    /** 调味治疗量（庄园技能倍率已计入）。 */
    public static float healAmount(boolean paired) {
        return paired ? 18.0F : 12.0F;
    }

    public LemonCatWeaponItem(Kind kind) {
        super(FTWeaponTier.INSTANCE, kind == Kind.SKILLET ? 6 : 3,
                kind == Kind.SKILLET ? -2.6F : -2.0F, new Item.Properties().rarity(Rarity.EPIC));
        this.kind = kind;
    }

    public static boolean isPair(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return main.is(FTBossItems.LEMONCAT_SKILLET.get()) && off.is(FTBossItems.LEMONCAT_SPATULA.get())
                || main.is(FTBossItems.LEMONCAT_SPATULA.get()) && off.is(FTBossItems.LEMONCAT_SKILLET.get());
    }

    public static boolean isCoolingDown(Player player) {
        return player.getCooldowns().isOnCooldown(FTBossItems.LEMONCAT_SKILLET.get())
                || player.getCooldowns().isOnCooldown(FTBossItems.LEMONCAT_SPATULA.get());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!player.isAlive() || player.isSpectator() || isCoolingDown(player)) {
            return InteractionResultHolder.fail(held);
        }
        if (level.isClientSide) return InteractionResultHolder.success(held);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.pass(held);
        boolean paired = isPair(player);
        Kind action = LemonCatWeaponRules.useHealing(paired, player.isShiftKeyDown(), this.kind == Kind.SPATULA)
                ? Kind.SPATULA : Kind.SKILLET;
        Item activeItem = action == Kind.SKILLET ? FTBossItems.LEMONCAT_SKILLET.get() : FTBossItems.LEMONCAT_SPATULA.get();
        InteractionHand activeHand = player.getMainHandItem().is(activeItem) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack activeStack = player.getItemInHand(activeHand);
        int cooldown = action == Kind.SKILLET ? SKILLET_COOLDOWN : SPATULA_COOLDOWN;
        player.getCooldowns().addCooldown(FTBossItems.LEMONCAT_SKILLET.get(), cooldown);
        player.getCooldowns().addCooldown(FTBossItems.LEMONCAT_SPATULA.get(), cooldown);
        if (action == Kind.SKILLET) flip(serverPlayer, paired);
        else refresh(serverPlayer, paired);
        activeStack.hurtAndBreak(action == Kind.SKILLET ? 4 : 3, player, wearer -> wearer.broadcastBreakEvent(activeHand));
        player.swing(activeHand, true);
        return InteractionResultHolder.consume(held);
    }

    private static boolean canHit(ServerPlayer player, LivingEntity target) {
        if (!PlayerWeaponTargeting.isValidTarget(player, target) || target instanceof ArmorStand) return false;
        if (target instanceof TamableAnimal pet && pet.isTame()) return false;
        return PlayerWeaponTargeting.canHarmPlayer(player, target) && player.hasLineOfSight(target);
    }

    private static void flip(ServerPlayer player, boolean paired) {
        ServerLevel level = player.serverLevel();
        double radius = paired ? PAIRED_RANGE : SKILLET_RANGE;
        Vec3 direction = Vec3.directionFromRotation(0.0F, player.getYRot());
        int hits = 0;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius, 2.5D, radius))) {
            if (!canHit(player, target)) continue;
            Vec3 offset = target.position().subtract(player.position());
            Vec3 horizontal = new Vec3(offset.x, 0.0D, offset.z);
            if (!LemonCatWeaponRules.inCone(offset.x, offset.y, offset.z, direction.x, direction.z, radius)) continue;
            if (target.hurt(level.damageSources().playerAttack(player), skilletDamage(paired))) {
                Vec3 push = horizontal.lengthSqr() > 0.01D ? horizontal.normalize() : direction;
                target.knockback(0.45D, -push.x, -push.z);
                target.push(0.0D, 0.35D, 0.0D);
                target.hurtMarked = true;
                target.setSecondsOnFire(3);
                if (paired) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), player);
                level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 0.7D, target.getZ(), 7, 0.25D, 0.2D, 0.25D, 0.025D);
                hits++;
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.9F, 0.8F);
        LemonCatNetwork.send(player, paired ? 2 : 0);
        player.displayClientMessage(Component.translatable("message.ftboss.lemoncat.flip", hits), true);
    }

    private static void refresh(ServerPlayer player, boolean paired) {
        player.heal(healAmount(paired));
        player.removeEffect(MobEffects.POISON);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_TICKS, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SPEED_TICKS, 0));
        if (paired) {
            player.clearFire();
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, SPEED_TICKS, 0));
        }
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 14, 0.55D, 0.6D, 0.55D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.8D, player.getZ(), 10, 0.4D, 0.5D, 0.4D, 0.015D);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 1.25F);
        LemonCatNetwork.send(player, paired ? 3 : 1);
        player.displayClientMessage(Component.translatable("message.ftboss.lemoncat.refresh"), true);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (hit && attacker instanceof ServerPlayer player && canHit(player, target)) {
            player.serverLevel().sendParticles(this.kind == Kind.SKILLET ? ParticleTypes.CRIT : ParticleTypes.END_ROD,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.01D);
        }
        return hit;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.ftboss.lemoncat.origin").withStyle(ChatFormatting.DARK_PURPLE));
        if (this.kind == Kind.SKILLET) {
            tooltip.add(Component.translatable("tooltip.ftboss.lemoncat.flip", (int) skilletDamage(false), SKILLET_RANGE, SKILLET_COOLDOWN / 20)
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.ftboss.lemoncat.flip_pair", (int) skilletDamage(true), PAIRED_RANGE)
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("tooltip.ftboss.lemoncat.refresh", (int) healAmount(false), SPATULA_COOLDOWN / 20)
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("tooltip.ftboss.lemoncat.refresh_pair", (int) healAmount(true))
                    .withStyle(ChatFormatting.AQUA));
        }
        tooltip.add(Component.translatable("tooltip.ftboss.lemoncat.controls").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftboss.lemoncat.cooldown").withStyle(ChatFormatting.DARK_GRAY));
    }
}
