package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.util.WeaponFxRules.Theme;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.weapons.WineBottleMaceItem;
import net.farmingtales.ftboss.util.WineMaceRules;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class WineMaceCombat {
    private static final Map<UUID, Cast> CASTS = new HashMap<>();

    private static final class Cast {
        private final ResourceKey<Level> dimension;
        private final Vec3 start;
        private final Vec3 impact;
        private final ItemStack weapon;
        private final int pressure;
        private final float damage;
        private int age;

        private Cast(ServerPlayer player, Vec3 impact, int pressure) {
            this.dimension = player.level().dimension();
            this.start = player.position();
            this.impact = impact;
            this.weapon = player.getMainHandItem();
            this.pressure = pressure;
            this.damage = WineMaceRules.damage(player.getAttributeValue(Attributes.ATTACK_DAMAGE), pressure);
        }
    }

    private WineMaceCombat() {}

    private static long now(ServerPlayer player) {
        return player.serverLevel().getServer().overworld().getGameTime();
    }

    private static int pressure(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData().getCompound(WineMaceRules.PRESSURE_TAG);
        return WineMaceRules.pressure(tag.getInt("count"), tag.getLong("expires"), now(player));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void melee(LivingDamageEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !player.isAlive() || player.isSpectator()
                || event.getSource().getDirectEntity() != player || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(player.getMainHandItem().getItem() instanceof WineBottleMaceItem)
                || CASTS.containsKey(player.getUUID()) || !NonManorWeaponSkills.canTarget(player, event.getEntity())) return;
        CompoundTag tag = player.getPersistentData().getCompound(WineMaceRules.PRESSURE_TAG);
        long now = now(player);
        if (!WineMaceRules.hitReady(tag.getLong("ready"), now)) return;
        int pressure = Math.min(WineMaceRules.MAX_PRESSURE, pressure(player) + 1);
        tag.putInt("count", pressure);
        tag.putLong("expires", now + WineMaceRules.PRESSURE_TICKS);
        tag.putLong("ready", now + WineMaceRules.HIT_INTERVAL);
        player.getPersistentData().put(WineMaceRules.PRESSURE_TAG, tag);
        player.displayClientMessage(Component.translatable("message.ftboss.wine_mace.pressure", pressure, WineMaceRules.MAX_PRESSURE), true);
        player.serverLevel().sendParticles(ParticleTypes.WAX_ON, event.getEntity().getX(),
                event.getEntity().getY() + 0.8, event.getEntity().getZ(), 7, 0.25, 0.3, 0.25, 0.03);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL,
                SoundSource.PLAYERS, 0.45F, 0.8F + pressure * 0.18F);
    }

    public static boolean begin(ServerPlayer player) {
        if (!(player.getMainHandItem().getItem() instanceof WineBottleMaceItem) || CASTS.containsKey(player.getUUID())) return false;
        Vec3 impact = NonManorWeaponSkills.aim(player, WineMaceRules.RANGE);
        Cast cast = new Cast(player, impact, pressure(player));
        CASTS.put(player.getUUID(), cast);
        CompoundTag pressureTag = player.getPersistentData().getCompound(WineMaceRules.PRESSURE_TAG);
        pressureTag.putInt("count", 0);
        pressureTag.putLong("expires", 0);
        player.getPersistentData().put(WineMaceRules.PRESSURE_TAG, pressureTag);
        ring(player.serverLevel(), impact, WineMaceRules.radius(cast.pressure), false);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW,
                SoundSource.PLAYERS, 0.8F, 0.75F);
        return true;
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        Cast cast = CASTS.get(player.getUUID());
        if (cast == null) return;
        if (!player.isAlive() || player.isRemoved() || player.isSpectator() || player.isSleeping()
                || !player.level().dimension().equals(cast.dimension) || player.getMainHandItem() != cast.weapon
                || cast.weapon.isEmpty() || player.isUsingItem() || player.distanceToSqr(cast.start) > 16
                || !NonManorWeaponSkills.clearLine(player.serverLevel(), player, player.getEyePosition(), cast.impact)) {
            CASTS.remove(player.getUUID());
            player.displayClientMessage(Component.translatable("message.ftboss.wine_mace.cancelled"), true);
            return;
        }
        cast.age++;
        if (cast.age < WineMaceRules.WINDUP_TICKS) {
            if (cast.age % 3 == 0) {
                ring(player.serverLevel(), cast.impact, WineMaceRules.radius(cast.pressure), false);
                player.serverLevel().sendParticles(ParticleTypes.WAX_ON, cast.impact.x, cast.impact.y, cast.impact.z,
                        6, 0.3, 0.5, 0.3, 0.04);
            }
            return;
        }
        try {
            impact(player, cast);
        } finally {
            CASTS.remove(player.getUUID());
        }
    }

    private static void impact(ServerPlayer player, Cast cast) {
        ServerLevel level = player.serverLevel();
        double radius = WineMaceRules.radius(cast.pressure);
        var candidates = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class,
                new AABB(cast.impact, cast.impact).inflate(radius, 3.0, radius), target -> NonManorWeaponSkills.canTarget(player, target)));
        candidates.sort(Comparator.comparingDouble(target -> target.distanceToSqr(cast.impact)));
        int hits = 0;
        for (LivingEntity target : candidates) {
            Vec3 center = target.position().add(0, Math.min(0.5, target.getBbHeight() * 0.5), 0);
            Vec3 offset = center.subtract(cast.impact);
            if (!WineMaceRules.inBlast(offset.horizontalDistanceSqr(), offset.y, cast.pressure)
                    || !NonManorWeaponSkills.clearLine(level, player, cast.impact, target.getBoundingBox().getCenter())
                    || !target.hurt(player.damageSources().playerAttack(player), cast.damage)) continue;
            hits++;
            if (target.isAlive()) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60 + cast.pressure * 20, 1), player);
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0), player);
                NonManorWeaponSkills.knockAway(target, cast.impact, 0.55 + cast.pressure * 0.2);
            }
            if (hits >= WineMaceRules.MAX_TARGETS) break;
        }
        if (hits > 0 && cast.pressure == WineMaceRules.MAX_PRESSURE) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 0), player);
        }
        ring(level, cast.impact, radius, true);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GREEN_STAINED_GLASS.defaultBlockState()),
                cast.impact.x, cast.impact.y, cast.impact.z, 16, 0.65, 0.6, 0.65, 0.16);
        level.sendParticles(ParticleTypes.CLOUD, cast.impact.x, cast.impact.y + 0.2, cast.impact.z,
                8, radius * 0.3, 0.15, radius * 0.3, 0.04);
        level.playSound(null, cast.impact.x, cast.impact.y, cast.impact.z, SoundEvents.GLASS_BREAK,
                SoundSource.PLAYERS, 1.0F, 0.7F);
        level.playSound(null, cast.impact.x, cast.impact.y, cast.impact.z, SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS, 0.55F, 1.25F);
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    private static void ring(ServerLevel level, Vec3 center, double radius, boolean released) {
        if (released) WeaponFx.burst(level, center.add(0, 0.05, 0), radius, Theme.WINE);
        else WeaponFx.ring(level, center.add(0, 0.05, 0), radius, Theme.GOLD);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        CASTS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        CASTS.remove(event.getEntity().getUUID());
        event.getEntity().getPersistentData().remove(WineMaceRules.PRESSURE_TAG);
        if (!event.isWasDeath() && event.getOriginal().getPersistentData().contains(WineMaceRules.PRESSURE_TAG)) {
            event.getEntity().getPersistentData().put(WineMaceRules.PRESSURE_TAG,
                    event.getOriginal().getPersistentData().getCompound(WineMaceRules.PRESSURE_TAG).copy());
        }
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) {
        CASTS.clear();
    }
}
