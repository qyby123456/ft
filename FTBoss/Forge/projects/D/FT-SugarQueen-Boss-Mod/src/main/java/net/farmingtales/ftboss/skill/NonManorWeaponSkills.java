package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.farmingtales.ftboss.util.NonManorWeaponRules;
import net.farmingtales.ftboss.util.NonManorWeaponRules.Shape;
import net.farmingtales.ftboss.util.NonManorWeaponRules.Skill;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.farmingtales.ftboss.util.WeaponFxRules.Kind;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class NonManorWeaponSkills {
    private NonManorWeaponSkills() {}

    public static void cast(ServerPlayer player, ResourceLocation requestedItem) {
        if (!player.isAlive() || player.isRemoved() || player.isSpectator() || player.isSleeping()
                || player.isUsingItem()) return;
        ItemStack stack = player.getMainHandItem();
        ResourceLocation item = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (stack.isEmpty() || item == null || !item.equals(requestedItem)
                || net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(stack)) return;
        // Evolved weapons (including weapons from other mods) share this key and packet. Their
        // statue skill replaces the legacy fixed cast while the item is evolved.
        if (WeaponEvolutionSkills.supports(stack)) {
            WeaponEvolutionSkills.cast(player, stack);
            return;
        }
        Skill skill = NonManorWeaponRules.forItem(item.toString());
        if (skill == null) return;
        long now = player.serverLevel().getServer().overworld().getGameTime();
        CompoundTag cooldowns = player.getPersistentData().getCompound(NonManorWeaponRules.COOLDOWN_TAG);
        if (cooldowns.contains("request") && now >= cooldowns.getLong("request")
                && now - cooldowns.getLong("request") < NonManorWeaponRules.REQUEST_INTERVAL) return;
        cooldowns.putLong("request", now);
        player.getPersistentData().put(NonManorWeaponRules.COOLDOWN_TAG, cooldowns);
        int remaining = NonManorWeaponRules.remaining(skill, now, cooldowns.getLong(skill.item));
        if (remaining > 0) {
            player.displayClientMessage(Component.translatable("message.ftboss.non_manor_cooldown",
                    Component.translatable(skill.translationKey()), (remaining + 19) / 20), true);
            return;
        }
        if (!execute(player, skill)) {
            player.displayClientMessage(WeaponTypeSkills.failureMessage(stack), true);
            return;
        }
        int evolvedCooldown = net.farmingtales.ftboss.item.FoodWeaponEvolution.skillCooldown(stack, skill.cooldownTicks);
        cooldowns.putLong(skill.item, now + evolvedCooldown);
        stack.hurtAndBreak(NonManorWeaponRules.DURABILITY_COST, player,
                holder -> holder.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        player.swing(InteractionHand.MAIN_HAND, true);
        player.displayClientMessage(Component.translatable("message.ftboss.non_manor_cast",
                Component.translatable(skill.translationKey()), evolvedCooldown / 20), true);
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        WeaponEvolutionSkills.copyCooldownData(event);
        CompoundTag previous = event.getOriginal().getPersistentData();
        if (previous.contains(NonManorWeaponRules.COOLDOWN_TAG)) {
            event.getEntity().getPersistentData().put(NonManorWeaponRules.COOLDOWN_TAG,
                    previous.getCompound(NonManorWeaponRules.COOLDOWN_TAG).copy());
        }
    }

    private static boolean execute(ServerPlayer player, Skill skill) {
        if (skill == Skill.BARREL || skill == Skill.TIDE) return WeaponTypeSkills.executeNative(player, skill);
        return executeLegacy(player, skill);
    }

    private static boolean executeLegacy(ServerPlayer player, Skill skill) {
        ServerLevel level = player.serverLevel();
        if (skill == Skill.ARC) return chain(player, skill);
        if (skill == Skill.WINE) return WineMaceCombat.begin(player);
        Vec3 origin = skill.shape == Shape.BURST ? aim(player, skill.range) : player.position().add(0, 0.4, 0);
        int hits = 0;
        if (skill.shape != Shape.SELF) {
            double radius = skill.shape == Shape.BURST ? skill.radius : skill.range;
            List<LivingEntity> candidates = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(origin, origin).inflate(radius + 1), target -> canTarget(player, target)));
            candidates.sort(Comparator.comparingDouble(target -> target.distanceToSqr(origin)));
            for (LivingEntity target : candidates) {
                Vec3 position = target.position().add(0, Math.min(0.5, target.getBbHeight() / 2), 0);
                double distanceSquared = position.distanceToSqr(origin);
                if (!NonManorWeaponRules.within(distanceSquared, radius)) continue;
                if (skill.shape == Shape.CONE) {
                    Vec3 direction = target.getBoundingBox().getCenter().subtract(player.getEyePosition()).normalize();
                    if (!NonManorWeaponRules.inCone(distanceSquared, direction.dot(player.getLookAngle()), radius)) continue;
                }
                if (skill.shape == Shape.BURST && !clearLine(level, player, origin, target.getBoundingBox().getCenter())) continue;
                if (strike(player, target, skill, 0, origin)) hits++;
                if (hits >= skill.maxTargets) break;
            }
        }
        switch (skill) {
            case MEAT -> player.heal(NonManorWeaponRules.meatHealing(hits));
            case THORNS -> {
                if (hits > 0) effect(player, MobEffects.REGENERATION, 100, 0, player);
            }
            case CHEESE -> effect(player, MobEffects.MOVEMENT_SPEED, 120, 1, player);
            case APPLE -> {
                player.removeEffect(MobEffects.POISON);
                player.heal(6);
                effect(player, MobEffects.REGENERATION, 100, 0, player);
                effect(player, MobEffects.MOVEMENT_SPEED, 100, 0, player);
            }
            case TIDE -> {
                effect(player, MobEffects.DAMAGE_RESISTANCE, 120, 0, player);
                effect(player, MobEffects.WATER_BREATHING, 240, 0, player);
            }
            default -> {}
        }
        visual(player, skill, origin);
        return true;
    }

    static boolean canTarget(ServerPlayer player, LivingEntity target) {
        if (target == player || !target.isAlive() || target.isRemoved() || target.isSpectator()
                || target instanceof ArmorStand || player.isAlliedTo(target) || target.isAlliedTo(player)
                || player.isPassengerOfSameVehicle(target) || !player.hasLineOfSight(target)) return false;
        if (target instanceof OwnableEntity owned && owned.getOwnerUUID() != null) return false;
        if (target instanceof AbstractHorse horse && horse.isTamed()) return false;
        if (target instanceof Player other) {
            return !other.isCreative() && !other.isSpectator() && player.serverLevel().getServer().isPvpAllowed()
                    && player.canHarmPlayer(other);
        }
        return true;
    }

    static boolean clearLine(ServerLevel level, ServerPlayer player, Vec3 start, Vec3 end) {
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS;
    }

    static Vec3 aim(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(range));
        HitResult block = player.serverLevel().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        end = block.getLocation();
        double nearest = start.distanceToSqr(end);
        for (LivingEntity target : player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                new AABB(start, end).inflate(0.3), candidate -> canTarget(player, candidate))) {
            var intersection = target.getBoundingBox().inflate(0.15).clip(start, end);
            if (intersection.isPresent() && start.distanceToSqr(intersection.get()) < nearest) {
                end = intersection.get();
                nearest = start.distanceToSqr(end);
            }
        }
        return end.subtract(player.getLookAngle().scale(0.15));
    }

    private static boolean chain(ServerPlayer player, Skill skill) {
        Vec3 impact = aim(player, skill.range);
        LivingEntity current = player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                        new AABB(impact, impact).inflate(0.65), target -> canTarget(player, target))
                .stream().min(Comparator.comparingDouble(target -> target.getBoundingBox().getCenter().distanceToSqr(impact)))
                .orElse(null);
        if (current == null || !NonManorWeaponRules.within(
                player.getEyePosition().distanceToSqr(current.getBoundingBox().getCenter()), skill.range)) return false;
        Set<Integer> visited = new HashSet<>();
        Vec3 previous = player.getEyePosition();
        for (int index = 0; index < skill.maxTargets && current != null; index++) {
            visited.add(current.getId());
            Vec3 center = current.getBoundingBox().getCenter();
            WeaponFx.beam(player.serverLevel(), previous, center, Theme.ARC);
            strike(player, current, skill, index, previous);
            Vec3 lastCenter = center;
            previous = center;
            current = player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                            new AABB(center, center).inflate(skill.radius), target -> !visited.contains(target.getId())
                                    && canTarget(player, target)
                                    && NonManorWeaponRules.within(target.getBoundingBox().getCenter().distanceToSqr(lastCenter), skill.radius)
                                    && NonManorWeaponRules.within(player.getEyePosition().distanceToSqr(target.getBoundingBox().getCenter()), skill.range)
                                    && clearLine(player.serverLevel(), player, lastCenter, target.getBoundingBox().getCenter()))
                    .stream().min(Comparator.comparingDouble(target -> target.getBoundingBox().getCenter().distanceToSqr(lastCenter)))
                    .orElse(null);
        }
        sound(player, SoundEvents.TRIDENT_THUNDER, 0.45F, 1.6F);
        return true;
    }

    private static boolean strike(ServerPlayer player, LivingEntity target, Skill skill, int chainIndex, Vec3 origin) {
        float damage = NonManorWeaponRules.damage(skill, player.getAttributeValue(Attributes.ATTACK_DAMAGE), chainIndex)
                * net.farmingtales.ftboss.item.FoodWeaponEvolution.activeSkillMultiplier(player.getMainHandItem());
        if (!target.hurt(player.damageSources().playerAttack(player), damage)) return false;
        if (!target.isAlive()) return true;
        switch (skill) {
            case FROST -> effect(target, MobEffects.MOVEMENT_SLOWDOWN, 80, 1, player);
            case MEAT -> knockAway(target, origin, 0.8);
            case THORNS -> effect(target, MobEffects.MOVEMENT_SLOWDOWN, 60, 2, player);
            case CHEESE -> effect(target, MobEffects.WEAKNESS, 100, 0, player);
            case ARC -> {
                effect(target, MobEffects.GLOWING, 60, 0, player);
                effect(target, MobEffects.MOVEMENT_SLOWDOWN, 40, 1, player);
            }
            case COMPOST -> effect(target, MobEffects.POISON, 100, 1, player);
            case HOWL -> {
                effect(target, MobEffects.WEAKNESS, 100, 0, player);
                effect(target, MobEffects.GLOWING, 100, 0, player);
                knockAway(target, origin, 0.6);
            }
            case BARREL -> {
                effect(target, MobEffects.MOVEMENT_SLOWDOWN, 60, 1, player);
                knockAway(target, origin, 0.65);
            }
            case TIDE -> knockAway(target, origin, 0.8);
            default -> {}
        }
        return true;
    }

    private static void effect(LivingEntity target, MobEffect effect, int ticks, int amplifier, ServerPlayer player) {
        target.addEffect(new MobEffectInstance(effect, ticks, amplifier), player);
    }

    static void knockAway(LivingEntity target, Vec3 origin, double strength) {
        Vec3 direction = target.position().subtract(origin).multiply(1, 0, 1).normalize();
        double resistance = Math.max(0, Math.min(1, target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
        double magnitude = strength * (1 - resistance);
        if (direction.lengthSqr() < 1.0E-6 || magnitude <= 0) return;
        target.push(direction.x * magnitude, 0.18 * (1 - resistance), direction.z * magnitude);
        target.hurtMarked = true;
    }

    private static void visual(ServerPlayer player, Skill skill, Vec3 origin) {
        ServerLevel level = player.serverLevel();
        Theme theme = WeaponFxRules.forItem(skill.item);
        double radius = skill.shape == Shape.SELF ? 1.2 : skill.shape == Shape.BURST ? skill.radius : skill.range;
        WeaponFx.send(level, skill.shape == Shape.CONE ? Kind.SWEEP : Kind.BLAST,
                theme, origin, origin, radius, player.getYRot());
        if (skill.shape == Shape.BURST) WeaponFx.beam(level, player.getEyePosition(), origin, theme);
        SoundEvent sound = switch (skill) {
            case FROST -> SoundEvents.GLASS_BREAK;
            case MEAT -> SoundEvents.GENERIC_EXPLODE;
            case THORNS, APPLE -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case WINE, BARREL -> SoundEvents.GENERIC_DRINK;
            case CHEESE -> SoundEvents.PLAYER_BURP;
            case HOWL -> SoundEvents.WOLF_GROWL;
            case COMPOST -> SoundEvents.SLIME_SQUISH;
            case TIDE -> SoundEvents.TRIDENT_RIPTIDE_1;
            default -> SoundEvents.PLAYER_ATTACK_SWEEP;
        };
        sound(player, sound, 0.85F, 1.0F);
    }

    private static void sound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.serverLevel().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
    }
}
