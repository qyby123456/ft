package net.farmingtales.ftboss.item.weapons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.PlayerWeaponTargeting;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.PufferBatRules;
import net.farmingtales.ftboss.util.WeaponFxRules.Kind;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 玩家河豚棒技能：右键铺设胀气毒雾，潜行右键发动近身毒刺膨爆。
 * 服务端只保存瞬态毒云刻表；全部表现通过有界网络包交给客户端世界网格绘制。
 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PufferBatCombat {
    private static final Map<UUID, List<Cloud>> CLOUDS = new HashMap<>();

    private PufferBatCombat() {}

    public static boolean canTarget(ServerPlayer player, LivingEntity target) {
        if (!PlayerWeaponTargeting.canTarget(player, target)) return false;
        if (target instanceof AbstractHorse horse && horse.isTamed()) return false;
        return target instanceof Player || PlayerWeaponTargeting.isHostileTo(player, target);
    }

    private static boolean tamedByAnyone(LivingEntity target) {
        return target instanceof TamableAnimal animal && animal.isTame();
    }

    private static boolean visible(ServerPlayer player, LivingEntity target) {
        return PlayerWeaponTargeting.hasClearPath(player, player.getEyePosition(), target.getBoundingBox().getCenter());
    }

    /** 普通右键：在真实射线落点铺设毒云。 */
    public static boolean cast(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        ServerLevel level = player.serverLevel();
        if (activeClouds(player) >= PufferBatRules.MAX_ACTIVE_CLOUDS) return false;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 reach = eye.add(look.scale(PufferBatRules.CAST_RANGE));
        Vec3 hit = level.clip(new ClipContext(eye, reach, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player)).getLocation();
        double[] point = PufferBatRules.castPoint(eye.x, eye.y, eye.z, hit.x, hit.y, hit.z);
        if (point == null) return false;

        long now = level.getGameTime();
        CLOUDS.computeIfAbsent(player.getUUID(), key -> new ArrayList<>())
                .add(new Cloud(level, player.getUUID(), point[0], point[1], point[2], now));
        Vec3 center = new Vec3(point[0], point[1] + 0.08, point[2]);
        WeaponFx.send(level, Kind.PUFFER_BURST, Theme.COMPOST, center, center,
                PufferBatRules.CLOUD_RADIUS, player.getYRot());
        WeaponFx.send(level, Kind.PUFFER_CLOUD, Theme.COMPOST, center, center,
                PufferBatRules.CLOUD_RADIUS, player.getYRot());
        level.playSound(null, point[0], point[1], point[2], SoundEvents.PUFFER_FISH_BLOW_OUT,
                SoundSource.PLAYERS, 1.0F, 0.85F);
        level.playSound(null, point[0], point[1], point[2], SoundEvents.BUBBLE_COLUMN_BUBBLE_POP,
                SoundSource.PLAYERS, 0.8F, 0.7F);
        stack.hurtAndBreak(PufferBatRules.DURABILITY_COST, player, owner -> owner.broadcastBreakEvent(hand));
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "message.ftboss.puffer_bat.cast", (int) PufferBatRules.CLOUD_RADIUS,
                PufferBatRules.CLOUD_TICKS / 20, (int) PufferBatRules.totalDamage()), true);
        FTBossMod.LOGGER.info("[FTBoss][河豚棒] {} 放出胀气毒雾: {} 格 / {} 秒 / 每跳 {} 点",
                player.getGameProfile().getName(), PufferBatRules.CLOUD_RADIUS,
                PufferBatRules.CLOUD_TICKS / 20, PufferBatRules.tickDamage());
        return true;
    }

    /** 潜行右键：以玩家为中心发动毒刺震波；已中毒目标承受额外伤害。 */
    public static boolean shockwave(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        ServerLevel level = player.serverLevel();
        Vec3 center = player.position().add(0, 0.08, 0);
        double radius = PufferBatRules.SHOCKWAVE_RADIUS;
        AABB bounds = player.getBoundingBox().inflate(radius, 3.0, radius);
        int hits = 0;
        int poisonedHits = 0;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bounds)) {
            if (tamedByAnyone(target) || !canTarget(player, target) || !visible(player, target)) continue;
            Vec3 targetCenter = target.getBoundingBox().getCenter();
            Vec3 offset = targetCenter.subtract(center);
            if (!PufferBatRules.inside(offset.x, offset.y, offset.z, radius)) continue;
            boolean poisoned = target.hasEffect(MobEffects.POISON);
            if (!target.hurt(level.damageSources().indirectMagic(player, player),
                    PufferBatRules.shockwaveDamage(poisoned))) continue;
            target.addEffect(new MobEffectInstance(MobEffects.POISON, PufferBatRules.SHOCKWAVE_POISON_TICKS,
                    PufferBatRules.SHOCKWAVE_POISON_AMPLIFIER, false, true, true), player);
            Vec3 push = new Vec3(offset.x, 0, offset.z);
            if (push.lengthSqr() < 1.0E-6) push = player.getLookAngle().multiply(1, 0, 1);
            if (push.lengthSqr() < 1.0E-6) push = new Vec3(0, 0, 1);
            push = push.normalize().scale(PufferBatRules.SHOCKWAVE_KNOCKBACK);
            target.push(push.x, PufferBatRules.SHOCKWAVE_LIFT, push.z);
            target.hurtMarked = true;
            hits++;
            if (poisoned) poisonedHits++;
        }
        WeaponFx.send(level, Kind.PUFFER_SHOCKWAVE, Theme.COMPOST, center, center,
                radius, player.getYRot());
        level.playSound(null, player.blockPosition(), SoundEvents.PUFFER_FISH_BLOW_OUT,
                SoundSource.PLAYERS, 1.15F, 0.62F);
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_2,
                SoundSource.PLAYERS, 0.85F, 1.25F);
        stack.hurtAndBreak(PufferBatRules.SHOCKWAVE_DURABILITY_COST, player,
                owner -> owner.broadcastBreakEvent(hand));
        player.causeFoodExhaustion(0.2F);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "message.ftboss.puffer_bat.shockwave", hits, poisonedHits), true);
        FTBossMod.LOGGER.info("[FTBoss][河豚棒] {} 发动毒刺膨爆: 命中 {}，中毒增伤 {}",
                player.getGameProfile().getName(), hits, poisonedHits);
        return true;
    }

    private static int activeClouds(ServerPlayer player) {
        List<Cloud> mine = CLOUDS.get(player.getUUID());
        return mine == null ? 0 : mine.size();
    }

    private static final class Cloud {
        final ServerLevel level;
        final UUID owner;
        final double x;
        final double y;
        final double z;
        final long born;
        long nextDamage;
        long nextVisual;

        Cloud(ServerLevel level, UUID owner, double x, double y, double z, long born) {
            this.level = level;
            this.owner = owner;
            this.x = x;
            this.y = y;
            this.z = z;
            this.born = born;
            this.nextDamage = born;
            this.nextVisual = born + PufferBatRules.CLOUD_VISUAL_REFRESH_TICKS;
        }

        boolean expired(long now) {
            return now - this.born >= PufferBatRules.CLOUD_TICKS;
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || CLOUDS.isEmpty()) return;
        Iterator<Map.Entry<UUID, List<Cloud>>> owners = CLOUDS.entrySet().iterator();
        while (owners.hasNext()) {
            Map.Entry<UUID, List<Cloud>> entry = owners.next();
            List<Cloud> mine = entry.getValue();
            ServerPlayer owner = findOwner(mine, entry.getKey());
            mine.removeIf(cloud -> tick(cloud, owner));
            if (mine.isEmpty()) owners.remove();
        }
    }

    private static ServerPlayer findOwner(List<Cloud> clouds, UUID ownerId) {
        for (Cloud cloud : clouds) {
            ServerPlayer player = cloud.level.getServer().getPlayerList().getPlayer(ownerId);
            if (player != null) return player;
        }
        return null;
    }

    private static boolean tick(Cloud cloud, ServerPlayer owner) {
        long now = cloud.level.getGameTime();
        if (cloud.expired(now)) return true;
        if (now >= cloud.nextVisual) {
            Vec3 center = new Vec3(cloud.x, cloud.y + 0.08, cloud.z);
            WeaponFx.send(cloud.level, Kind.PUFFER_CLOUD, Theme.COMPOST, center, center,
                    PufferBatRules.CLOUD_RADIUS, 0);
            cloud.nextVisual = now + PufferBatRules.CLOUD_VISUAL_REFRESH_TICKS;
        }
        if (now < cloud.nextDamage) return false;
        cloud.nextDamage = now + PufferBatRules.TICK_INTERVAL;

        boolean ownerHere = owner != null && owner.level() == cloud.level && owner.isAlive() && !owner.isSpectator();
        if (!ownerHere) return false;
        AABB box = new AABB(cloud.x - PufferBatRules.CLOUD_RADIUS, cloud.y - 3.0, cloud.z - PufferBatRules.CLOUD_RADIUS,
                cloud.x + PufferBatRules.CLOUD_RADIUS, cloud.y + 3.0, cloud.z + PufferBatRules.CLOUD_RADIUS);
        int hits = 0;
        for (LivingEntity target : cloud.level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (tamedByAnyone(target) || !canTarget(owner, target)) continue;
            double dx = target.getX() - cloud.x;
            double dz = target.getZ() - cloud.z;
            double dy = target.getY() - cloud.y;
            if (!PufferBatRules.inside(dx, dy, dz, PufferBatRules.CLOUD_RADIUS)) continue;
            if (!target.hurt(cloud.level.damageSources().indirectMagic(owner, owner),
                    PufferBatRules.tickDamage())) continue;
            target.addEffect(new MobEffectInstance(MobEffects.POISON,
                    PufferBatRules.POISON_TICKS, PufferBatRules.POISON_AMPLIFIER, false, true, true), owner);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    PufferBatRules.SLOW_TICKS, PufferBatRules.SLOW_AMPLIFIER, false, true, true), owner);
            hits++;
        }
        if (hits > 0) {
            cloud.level.playSound(null, cloud.x, cloud.y, cloud.z, SoundEvents.PUFFER_FISH_STING,
                    SoundSource.PLAYERS, 0.7F, 1.1F);
        }
        return false;
    }
}
