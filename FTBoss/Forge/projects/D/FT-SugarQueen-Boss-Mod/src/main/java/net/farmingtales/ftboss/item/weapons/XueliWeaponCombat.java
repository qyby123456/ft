package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.PlayerWeaponTargeting;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.XueliWeaponItems;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.WeaponFxRules.Kind;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class XueliWeaponCombat {
    private static final String MARK_KEY = "ftboss_xueli_rose";

    private XueliWeaponCombat() {}

    public static boolean canTarget(ServerPlayer player, LivingEntity target) {
        if (!PlayerWeaponTargeting.isValidTarget(player, target) || !player.hasLineOfSight(target)) return false;
        if (target instanceof Player) return PlayerWeaponTargeting.canHarmPlayer(player, target);
        if (target instanceof TamableAnimal animal && animal.isTame()) return false;
        if (target instanceof AbstractHorse horse && horse.isTamed()) return false;
        return PlayerWeaponTargeting.isHostileTo(player, target);
    }

    private static Vec3 direction(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 1.0E-6D) {
            double yaw = Math.toRadians(player.getYRot());
            return new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        }
        return horizontal.normalize();
    }

    private static List<LivingEntity> candidates(ServerPlayer player, double radius) {
        return player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius, 2.0D, radius), target -> canTarget(player, target));
    }

    private static void durability(ServerPlayer player, ItemStack stack, int amount) {
        stack.hurtAndBreak(amount, player, owner -> owner.broadcastBreakEvent(InteractionHand.MAIN_HAND));
    }

    private static void sound(ServerPlayer player, SoundEvent sound, float pitch) {
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 0.8F, pitch);
    }

    private static void halo(ServerLevel level, Vec3 center, double radius, int count, boolean gold) {
        WeaponFx.ring(level, center, radius, gold ? Theme.GOLD : Theme.ROSE);
    }

    public static void mark(ServerPlayer player, LivingEntity target) {
        CompoundTag rose = new CompoundTag();
        rose.putUUID("owner", player.getUUID());
        rose.putLong("expires", player.serverLevel().getGameTime() + XueliWeaponRules.MARK_DURATION);
        target.getPersistentData().put(MARK_KEY, rose);
        halo(player.serverLevel(), target.position().add(0.0D, target.getBbHeight() * 0.65D, 0.0D), 0.42D, 8, false);
    }

    private static boolean hasMark(ServerPlayer player, LivingEntity target) {
        CompoundTag rose = target.getPersistentData().getCompound(MARK_KEY);
        return rose.hasUUID("owner") && rose.getUUID("owner").equals(player.getUUID())
                && rose.getLong("expires") > player.serverLevel().getGameTime();
    }

    public static void sweep(ServerPlayer player, ItemStack stack) {
        ServerLevel level = player.serverLevel();
        Vec3 forward = direction(player);
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        player.getCooldowns().addCooldown(stack.getItem(), XueliWeaponRules.SKILLET_COOLDOWN);
        List<LivingEntity> targets = candidates(player, XueliWeaponRules.SWEEP_RADIUS).stream().filter(target -> {
            Vec3 offset = target.position().subtract(player.position());
            return XueliWeaponRules.inSweep(offset.dot(forward), offset.dot(side));
        }).sorted(Comparator.comparingDouble(player::distanceToSqr)).limit(XueliWeaponRules.MAX_TARGETS).toList();
        for (LivingEntity target : targets) {
            if (target.hurt(player.damageSources().playerAttack(player), XueliWeaponRules.sweepDamage()) && target.isAlive()) {
                mark(player, target);
                target.knockback(0.6D, player.getX() - target.getX(), player.getZ() - target.getZ());
            }
        }
        Vec3 center = player.position().add(0.0D, 0.9D, 0.0D);
        WeaponFx.send(level, Kind.SWEEP, Theme.ROSE, center, center, XueliWeaponRules.SWEEP_RADIUS, player.getYRot());
        sound(player, SoundEvents.PLAYER_ATTACK_SWEEP, 0.8F);
        sound(player, SoundEvents.ANVIL_HIT, 1.65F);
        durability(player, stack, 3);
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    public static void roll(ServerPlayer player, ItemStack stack, int chargeTicks) {
        ServerLevel level = player.serverLevel();
        Vec3 forward = direction(player);
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 origin = player.position().add(0.0D, 0.65D, 0.0D);
        Vec3 end = origin.add(forward.scale(XueliWeaponRules.ROLL_LENGTH));
        Vec3 stop = level.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
        double length = stop.distanceTo(origin);
        player.getCooldowns().addCooldown(stack.getItem(), XueliWeaponRules.PIN_COOLDOWN);
        List<LivingEntity> targets = candidates(player, XueliWeaponRules.ROLL_LENGTH).stream().filter(target -> {
            Vec3 offset = target.position().subtract(player.position());
            return XueliWeaponRules.inRoll(offset.dot(forward), offset.dot(side), length);
        }).sorted(Comparator.comparingDouble(player::distanceToSqr)).limit(XueliWeaponRules.MAX_TARGETS).toList();
        for (LivingEntity target : targets) {
            boolean combo = hasMark(player, target);
            float damage = XueliWeaponRules.rollDamage(chargeTicks) + (combo ? XueliWeaponRules.comboDamage() : 0.0F);
            if (!target.hurt(player.damageSources().playerAttack(player), damage)) continue;
            if (target.isAlive()) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, XueliWeaponRules.SLOW_DURATION, 1), player);
            if (combo) {
                target.getPersistentData().remove(MARK_KEY);
                halo(level, target.position().add(0.0D, 0.8D, 0.0D), 0.9D, 16, false);
                level.sendParticles(ParticleTypes.FIREWORK, target.getX(), target.getY() + 0.8D, target.getZ(), 8, 0.25D, 0.3D, 0.25D, 0.04D);
            }
        }
        Vec3 trackStart = origin.add(0, -0.45, 0);
        Vec3 trackEnd = stop.add(0, -0.45, 0);
        WeaponFx.beam(level, trackStart.add(side.scale(0.8)), trackEnd.add(side.scale(0.8)), Theme.ROSE);
        WeaponFx.beam(level, trackStart.add(side.scale(-0.8)), trackEnd.add(side.scale(-0.8)), Theme.ROSE);
        halo(level, stop.add(0.0D, -0.4D, 0.0D), 0.8D, 16, true);
        sound(player, SoundEvents.IRON_GOLEM_ATTACK, 0.8F);
        sound(player, SoundEvents.ANVIL_LAND, 1.5F);
        durability(player, stack, 4);
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    public static void chargeEffect(ServerPlayer player, boolean full) {
        Vec3 center = player.position().add(direction(player).scale(0.85D)).add(0.0D, 1.1D, 0.0D);
        halo(player.serverLevel(), center, full ? 0.55D : 0.3D, full ? 16 : 8, full);
        sound(player, SoundEvents.NOTE_BLOCK_CHIME.value(), full ? 1.6F : 1.1F);
    }

    public static void guardEffect(ServerPlayer player) {
        Vec3 center = player.position().add(direction(player).scale(0.8D)).add(0.0D, 1.0D, 0.0D);
        halo(player.serverLevel(), center, 0.6D, 12, false);
        sound(player, SoundEvents.ARMOR_EQUIP_IRON, 1.3F);
        durability(player, player.getMainHandItem(), 1);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void guard(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isUsingItem() || event.getAmount() <= 0.0F
                || player.getUsedItemHand() != InteractionHand.MAIN_HAND || !player.getUseItem().is(XueliWeaponItems.skillet())
                || event.getSource().is(DamageTypeTags.BYPASSES_SHIELD)) return;
        Vec3 source = event.getSource().getSourcePosition();
        if (source == null || event.getSource().getDirectEntity() == null) return;
        Vec3 offset = source.subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        if (offset.lengthSqr() < 1.0E-6D || offset.normalize().dot(direction(player)) < 0.5D) return;
        event.setAmount(event.getAmount() * 0.4F);
        ItemStack stack = player.getUseItem();
        player.stopUsingItem();
        durability(player, stack, 2);
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity living && canTarget(player, living) && player.distanceToSqr(living) <= 36.0D) mark(player, living);
        halo(player.serverLevel(), player.position().add(direction(player)).add(0.0D, 1.0D, 0.0D), 0.8D, 20, true);
        sound(player, SoundEvents.SHIELD_BLOCK, 1.35F);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void drops(LivingDropsEvent event) {
        /* ★ v180 按用户要求「庄园 boss 单个击杀不要掉落掉落物, 得全部击杀才获取」:
           雪黎的厨具(平底锅/擀面杖)属于"单只庄园 Boss 掉落", 一并关闭;
           通关宝箱 ManorVictoryLoot 里本来就含 ftboss:xueli_skillet / xueli_rolling_pin。 */
        if (!net.farmingtales.ftboss.entity.EasterBossEntity.PER_BOSS_DROPS) return;
        if (!(event.getEntity() instanceof EasterBossEntity boss) || !(boss.level() instanceof ServerLevel level)) return;
        if (boss.isNoAi() || net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(boss)) return;
        boolean playerKill = event.isRecentlyHit() || event.getSource().getEntity() instanceof ServerPlayer;
        if (!XueliWeaponRules.dropsWeapons(boss.getVariant(), boss.isMini(), playerKill, level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))) return;
        for (ItemStack reward : List.of(new ItemStack(XueliWeaponItems.skillet()), new ItemStack(XueliWeaponItems.rollingPin()))) {
            if (event.getDrops().stream().anyMatch(drop -> drop.getItem().is(reward.getItem()))) continue;
            ItemEntity drop = new ItemEntity(level, boss.getX(), boss.getY() + 0.4D, boss.getZ(), reward);
            drop.setDefaultPickUpDelay();
            event.getDrops().add(drop);
        }
    }
}
