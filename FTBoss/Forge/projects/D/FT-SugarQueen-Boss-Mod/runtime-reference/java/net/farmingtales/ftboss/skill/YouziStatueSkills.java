/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.OwnableEntity
 *  net.minecraft.world.entity.animal.horse.AbstractHorse
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingDropsEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.skill.YouziStatuePacket;
import net.farmingtales.ftboss.util.YouziStatueRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
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

public final class YouziStatueSkills {
    private static final Map<UUID, Session> ACTIVE = new HashMap<UUID, Session>();

    private YouziStatueSkills() {
    }

    public static boolean begin(ServerPlayer player, InteractionHand hand, boolean ward) {
        int cost;
        ItemStack stack = player.m_21120_(hand);
        if (!stack.m_150930_((Item)FTBossItems.STATUE_YOUZI.get()) || !player.m_6084_() || player.m_5833_() || player.m_6117_() || ACTIVE.containsKey(player.m_20148_()) || player.m_36335_().m_41519_(stack.m_41720_())) {
            return false;
        }
        int n = cost = ward ? 4 : 2;
        if (!player.m_150110_().f_35937_ && stack.m_41776_() - stack.m_41773_() <= cost) {
            player.m_5661_((Component)Component.m_237115_((String)"message.ftboss.statue_youzi.repair"), true);
            return false;
        }
        Session session = new Session(player, hand, ward);
        ACTIVE.put(player.m_20148_(), session);
        player.m_36335_().m_41524_(stack.m_41720_(), ward ? 320 : 160);
        stack.m_41622_(cost, (LivingEntity)player, owner -> owner.m_21190_(hand));
        YouziStatuePacket.send(player, ward ? 1 : 0, ward ? 120 : 32, player.m_20182_(), player.m_20182_());
        session.level.m_5594_(null, player.m_20183_(), SoundEvents.f_144243_, SoundSource.PLAYERS, 0.7f, ward ? 0.9f : 1.35f);
        player.m_5661_((Component)Component.m_237115_((String)(ward ? "message.ftboss.statue_youzi.ward" : "message.ftboss.statue_youzi.echo")), true);
        return true;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player;
        if (event.phase != TickEvent.Phase.END || !((player = event.player) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        Session session = ACTIVE.get(player2.m_20148_());
        if (session == null) {
            return;
        }
        if (!session.valid(player2)) {
            YouziStatueSkills.stop(player2);
            return;
        }
        if (session.ward) {
            return;
        }
        int due = YouziStatueRules.dueEchoes(session.level.m_46467_() - session.started);
        if (session.fired < due) {
            YouziStatueSkills.fire(player2, session);
            ++session.fired;
        }
    }

    private static void fire(ServerPlayer player, Session session) {
        Vec3 direction;
        Vec3 end;
        Vec3 origin = player.m_146892_();
        BlockHitResult block = session.level.m_45547_(new ClipContext(origin, end = origin.m_82549_((direction = player.m_20154_()).m_82490_(18.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
        if (block.m_6662_() != HitResult.Type.MISS) {
            end = block.m_82450_();
        }
        double nearestDistance = origin.m_82557_(end);
        LivingEntity nearest = null;
        for (LivingEntity candidate : session.level.m_6443_(LivingEntity.class, new AABB(origin, end).m_82400_(0.4), target -> target != player && target.m_6084_() && target.m_6087_() && !target.m_5833_() && !(target instanceof ArmorStand))) {
            Optional intersection = candidate.m_20191_().m_82400_(0.15).m_82371_(origin, end);
            Vec3 impact = candidate.m_20191_().m_82390_(origin) ? origin : (Vec3)intersection.orElse(null);
            if (impact == null || origin.m_82557_(impact) >= nearestDistance) continue;
            nearestDistance = origin.m_82557_(impact);
            end = impact;
            nearest = candidate;
        }
        if (nearest != null && YouziStatueSkills.canHarm(player, nearest) && player.m_142582_(nearest)) {
            nearest.m_6469_(session.level.m_269111_().m_269104_((Entity)player, (Entity)player), YouziStatueRules.echoDamage());
        }
        YouziStatuePacket.send(player, 2, 8, origin, end);
        session.level.m_8767_((ParticleOptions)ParticleTypes.f_123810_, end.f_82479_, end.f_82480_, end.f_82481_, 7, 0.12, 0.12, 0.12, 0.035);
        session.level.m_5594_(null, player.m_20183_(), SoundEvents.f_144245_, SoundSource.PLAYERS, 0.65f, 1.2f + (float)session.fired * 0.2f);
    }

    private static boolean canHarm(ServerPlayer player, LivingEntity target) {
        AbstractHorse horse;
        OwnableEntity owned;
        if (target == player || target.m_7307_((Entity)player) || player.m_7307_((Entity)target)) {
            return false;
        }
        if (target instanceof OwnableEntity && (owned = (OwnableEntity)target).m_21805_() != null) {
            return false;
        }
        if (target instanceof AbstractHorse && (horse = (AbstractHorse)target).m_30614_()) {
            return false;
        }
        if (target instanceof Player) {
            Player other = (Player)target;
            return player.m_284548_().m_7654_().m_129799_() && player.m_7099_(other) && !other.m_7500_() && !other.m_5833_();
        }
        return true;
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void onHurt(LivingHurtEvent event) {
        ServerPlayer player;
        block6: {
            block5: {
                LivingEntity livingEntity = event.getEntity();
                if (!(livingEntity instanceof ServerPlayer)) break block5;
                player = (ServerPlayer)livingEntity;
                if (!(event.getAmount() <= 0.0f) && !event.getSource().m_269533_(DamageTypeTags.f_268738_)) break block6;
            }
            return;
        }
        Session session = ACTIVE.get(player.m_20148_());
        if (session == null || !session.ward || !session.valid(player)) {
            return;
        }
        event.setAmount(YouziStatueRules.protectedDamage(event.getAmount()));
        long now = session.level.m_46467_();
        if (session.lastFlash == Long.MIN_VALUE || now - session.lastFlash >= 4L) {
            session.lastFlash = now;
            YouziStatuePacket.send(player, 4, 10, player.m_20182_(), player.m_20182_());
            session.level.m_5594_(null, player.m_20183_(), SoundEvents.f_144245_, SoundSource.PLAYERS, 0.45f, 1.8f);
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
    }

    private static void stop(ServerPlayer player) {
        if (ACTIVE.remove(player.m_20148_()) != null) {
            YouziStatuePacket.send(player, 3, 0, player.m_20182_(), player.m_20182_());
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)livingEntity;
            YouziStatueSkills.stop(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            YouziStatueSkills.stop(player2);
        }
    }

    @SubscribeEvent
    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            YouziStatueSkills.stop(player2);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE.clear();
    }

    private static /* synthetic */ boolean lambda$onDrops$2(ItemEntity drop) {
        return drop.m_32055_().m_150930_((Item)FTBossItems.STATUE_YOUZI.get());
    }

    private static final class Session {
        private final ItemStack stack;
        private final InteractionHand hand;
        private final ServerLevel level;
        private final long started;
        private final boolean ward;
        private int fired;
        private long lastFlash = Long.MIN_VALUE;

        private Session(ServerPlayer player, InteractionHand hand, boolean ward) {
            this.stack = player.m_21120_(hand);
            this.hand = hand;
            this.level = player.m_284548_();
            this.started = this.level.m_46467_();
            this.ward = ward;
        }

        private boolean valid(ServerPlayer player) {
            long age = this.level.m_46467_() - this.started;
            return player.m_6084_() && !player.m_213877_() && !player.m_5833_() && player.m_284548_() == this.level && player.m_21120_(this.hand) == this.stack && this.stack.m_150930_((Item)FTBossItems.STATUE_YOUZI.get()) && age >= 0L && age < (long)(this.ward ? 120 : 32);
        }
    }
}

