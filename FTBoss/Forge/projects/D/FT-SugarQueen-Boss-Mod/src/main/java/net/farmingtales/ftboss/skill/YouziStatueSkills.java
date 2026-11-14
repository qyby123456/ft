package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.util.YouziStatueRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class YouziStatueSkills {
    private static final Map<UUID, Session> ACTIVE = new HashMap<>();

    private YouziStatueSkills() {}

    private static final class Session {
        private final ItemStack stack;
        private final InteractionHand hand;
        private final ServerLevel level;
        private final long started;
        private final boolean ward;
        private int fired;
        private long lastFlash = Long.MIN_VALUE;

        private Session(ServerPlayer player, InteractionHand hand, boolean ward) {
            this.stack = player.getItemInHand(hand);
            this.hand = hand;
            this.level = player.serverLevel();
            this.started = level.getGameTime();
            this.ward = ward;
        }

        private boolean valid(ServerPlayer player) {
            long age = level.getGameTime() - started;
            return player.isAlive() && !player.isRemoved() && !player.isSpectator()
                    && player.serverLevel() == level && player.getItemInHand(hand) == stack
                    && stack.is(FTBossItems.STATUE_YOUZI.get()) && age >= 0
                    && age < (ward ? YouziStatueRules.WARD_DURATION : YouziStatueRules.ECHO_DURATION);
        }
    }

    public static boolean begin(ServerPlayer player, InteractionHand hand, boolean ward) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(FTBossItems.STATUE_YOUZI.get()) || !player.isAlive() || player.isSpectator()
                || player.isUsingItem() || ACTIVE.containsKey(player.getUUID())
                || player.getCooldowns().isOnCooldown(stack.getItem())) return false;
        int cost = ward ? 4 : 2;
        if (!player.getAbilities().instabuild && stack.getMaxDamage() - stack.getDamageValue() <= cost) {
            player.displayClientMessage(Component.translatable("message.ftboss.statue_youzi.repair"), true);
            return false;
        }
        Session session = new Session(player, hand, ward);
        ACTIVE.put(player.getUUID(), session);
        player.getCooldowns().addCooldown(stack.getItem(), ward ? YouziStatueRules.WARD_COOLDOWN : YouziStatueRules.ECHO_COOLDOWN);
        stack.hurtAndBreak(cost, player, owner -> owner.broadcastBreakEvent(hand));
        YouziStatuePacket.send(player, ward ? YouziStatuePacket.WARD : YouziStatuePacket.ECHO,
                ward ? YouziStatueRules.WARD_DURATION : YouziStatueRules.ECHO_DURATION,
                player.position(), player.position());
        session.level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.7F, ward ? 0.9F : 1.35F);
        player.displayClientMessage(Component.translatable(ward
                ? "message.ftboss.statue_youzi.ward" : "message.ftboss.statue_youzi.echo"), true);
        return true;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        Session session = ACTIVE.get(player.getUUID());
        if (session == null) return;
        if (!session.valid(player)) {
            stop(player);
            return;
        }
        if (session.ward) return;
        int due = YouziStatueRules.dueEchoes(session.level.getGameTime() - session.started);
        if (session.fired < due) {
            fire(player, session);
            session.fired++;
        }
    }

    private static void fire(ServerPlayer player, Session session) {
        Vec3 origin = player.getEyePosition();
        Vec3 direction = player.getLookAngle();
        Vec3 end = origin.add(direction.scale(YouziStatueRules.ECHO_RANGE));
        HitResult block = session.level.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) end = block.getLocation();
        double nearestDistance = origin.distanceToSqr(end);
        LivingEntity nearest = null;
        for (LivingEntity candidate : session.level.getEntitiesOfClass(LivingEntity.class,
                new AABB(origin, end).inflate(0.4), target -> target != player && target.isAlive()
                        && target.isPickable() && !target.isSpectator() && !(target instanceof ArmorStand))) {
            var intersection = candidate.getBoundingBox().inflate(0.15).clip(origin, end);
            Vec3 impact = candidate.getBoundingBox().contains(origin) ? origin : intersection.orElse(null);
            if (impact == null || origin.distanceToSqr(impact) >= nearestDistance) continue;
            nearestDistance = origin.distanceToSqr(impact);
            end = impact;
            nearest = candidate;
        }
        if (nearest != null && canHarm(player, nearest) && player.hasLineOfSight(nearest)) {
            nearest.hurt(session.level.damageSources().indirectMagic(player, player), YouziStatueRules.echoDamage());
        }
        YouziStatuePacket.send(player, YouziStatuePacket.BEAM, 8, origin, end);
        session.level.sendParticles(ParticleTypes.END_ROD, end.x, end.y, end.z, 7, 0.12, 0.12, 0.12, 0.035);
        session.level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT,
                SoundSource.PLAYERS, 0.65F, 1.2F + session.fired * 0.2F);
    }

    private static boolean canHarm(ServerPlayer player, LivingEntity target) {
        if (target == player || target.isAlliedTo(player) || player.isAlliedTo(target)) return false;
        if (target instanceof OwnableEntity owned && owned.getOwnerUUID() != null) return false;
        if (target instanceof AbstractHorse horse && horse.isTamed()) return false;
        if (target instanceof Player other) {
            return player.serverLevel().getServer().isPvpAllowed() && player.canHarmPlayer(other)
                    && !other.isCreative() && !other.isSpectator();
        }
        return true;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0.0F
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        Session session = ACTIVE.get(player.getUUID());
        if (session == null || !session.ward || !session.valid(player)) return;
        event.setAmount(YouziStatueRules.protectedDamage(event.getAmount()));
        long now = session.level.getGameTime();
        if (session.lastFlash == Long.MIN_VALUE || now - session.lastFlash >= 4) {
            session.lastFlash = now;
            YouziStatuePacket.send(player, YouziStatuePacket.BLOCK, 10, player.position(), player.position());
            session.level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundSource.PLAYERS, 0.45F, 1.8F);
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        /* ★ v180 按用户要求「庄园 boss 单个击杀不要掉落掉落物, 得全部击杀才获取」:
           柚子掉自己的石像也属于"单只庄园 Boss 掉落", 一并关闭;
           通关宝箱 ManorVictoryLoot 里本来就含 ftboss:statue_youzi。
           (顺带说明: 上面那行 removeIf 是把柚子的石像从默认掉落里摘掉、再由本方法
            按概率放回去 —— 现在整段短路, 两件事都不做, 结果就是**不掉**, 符合要求。) */
        if (!net.farmingtales.ftboss.entity.EasterBossEntity.PER_BOSS_DROPS) return;
        if (!(event.getEntity() instanceof EasterBossEntity boss) || boss.level().isClientSide
                || boss.getVariant() != 0) return;
        event.getDrops().removeIf(drop -> drop.getItem().is(FTBossItems.STATUE_YOUZI.get()));
        if (!YouziStatueRules.dropsStatue(boss.getVariant(), boss.isMini(), boss.isGiant())
                || !boss.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) return;
        ItemEntity drop = new ItemEntity(boss.level(), boss.getX(), boss.getY() + 0.25, boss.getZ(),
                new ItemStack(FTBossItems.STATUE_YOUZI.get()));
        drop.setDefaultPickUpDelay();
        event.getDrops().add(drop);
    }

    private static void stop(ServerPlayer player) {
        if (ACTIVE.remove(player.getUUID()) != null) {
            YouziStatuePacket.send(player, YouziStatuePacket.STOP, 0, player.position(), player.position());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) stop(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) stop(player);
    }

    @SubscribeEvent
    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) stop(player);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE.clear();
    }
}
