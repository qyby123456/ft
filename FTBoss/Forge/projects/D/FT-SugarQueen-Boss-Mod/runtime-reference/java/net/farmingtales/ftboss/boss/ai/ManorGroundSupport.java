/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingHealEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.PacketDistributor$TargetPoint
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.ManorDomainPacket;
import net.farmingtales.ftboss.util.ManorDamageRules;
import net.farmingtales.ftboss.util.ManorDomainEffectRules;
import net.farmingtales.ftboss.util.ManorDomainRules;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class ManorGroundSupport {
    private static final UUID ARMOR = UUID.fromString("a4c82f12-85d1-4c92-a7c6-e32a54f97511");
    private static final UUID SPEED = UUID.fromString("a4c82f12-85d1-4c92-a7c6-e32a54f97512");
    private static final UUID STABILITY = UUID.fromString("a4c82f12-85d1-4c92-a7c6-e32a54f97513");
    private static final UUID ATTACK = UUID.fromString("a4c82f12-85d1-4c92-a7c6-e32a54f97514");
    private static final UUID PLAYER_ARMOR = UUID.fromString("a4c82f12-85d1-4c92-a7c6-e32a54f97521");
    private static final UUID PLAYER_SPEED = UUID.fromString("a4c82f12-85d1-4c92-a7c6-e32a54f97522");
    private static final UUID PLAYER_ATTACK = UUID.fromString("a4c82f12-85d1-4c92-a7c6-e32a54f97524");
    private static final Map<UUID, State> ACTIVE = new HashMap<UUID, State>();
    private static final Map<UUID, State> INTRODUCED = new HashMap<UUID, State>();
    private static final Map<UUID, PlayerState> PLAYERS = new HashMap<UUID, PlayerState>();
    private static final Map<UUID, Long> LEECH_AT = new HashMap<UUID, Long>();
    private static final List<PendingLeech> PENDING = new ArrayList<PendingLeech>();
    private static long clock;

    private ManorGroundSupport() {
    }

    private static boolean eligible(EasterBossEntity easterBossEntity, UUID uUID) {
        return uUID != null && ManorFight.hasActive(uUID) && easterBossEntity.m_6084_() && !easterBossEntity.m_213877_() && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && ManorDomainRules.supports(easterBossEntity.getVariant()) && easterBossEntity.m_20148_().equals(ManorFight.currentBoss(uUID)) && !easterBossEntity.isManorIntroHidden() && !easterBossEntity.m_21525_();
    }

    public static void tick(EasterBossEntity easterBossEntity) {
        Vec3 vec3;
        Object object = easterBossEntity.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = ManorFight.fightId((Entity)easterBossEntity);
        Vec3 vec32 = vec3 = object == null ? null : ManorFight.anchorOf((UUID)object);
        if (!ManorGroundSupport.eligible(easterBossEntity, (UUID)object) || vec3 == null) {
            ManorGroundSupport.stop(serverLevel, easterBossEntity);
            return;
        }
        State state = ACTIVE.get(easterBossEntity.m_20148_());
        if (state == null || state.variant != easterBossEntity.getVariant() || state.level != serverLevel || !state.fight.equals(object) || state.anchor.m_82557_(vec3) > 0.01) {
            ManorGroundSupport.stop(serverLevel, easterBossEntity);
            BlockHitResult blockHitResult = serverLevel.m_45547_(new ClipContext(vec3.m_82520_(0.0, 0.5, 0.0), vec3.m_82520_(0.0, -8.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)easterBossEntity));
            Vec3 vec33 = blockHitResult.m_6662_() == HitResult.Type.MISS ? vec3 : blockHitResult.m_82450_();
            state = new State(serverLevel, easterBossEntity, (UUID)object, vec3, vec33);
            state.healAt = serverLevel.m_46467_() + 100L;
            ACTIVE.put(easterBossEntity.m_20148_(), state);
        }
        state.seen = clock;
        boolean bl = ManorGroundSupport.contains(state, (Entity)easterBossEntity);
        if (state.inside != bl || serverLevel.m_46467_() >= state.refresh) {
            state.inside = bl;
            ManorGroundSupport.updateAttributes(easterBossEntity, bl);
            ManorGroundSupport.send(state, state.variant);
            for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
                if (!(serverPlayer.m_20238_(state.center) <= 9216.0) || !ManorLivingTargets.valid((Entity)easterBossEntity, (LivingEntity)serverPlayer)) continue;
                ManorGroundSupport.announce(state, serverPlayer);
            }
            state.refresh = serverLevel.m_46467_() + 20L;
        }
        if (bl && state.variant == 11 && serverLevel.m_46467_() >= state.healAt) {
            float f = ManorDomainEffectRules.prayer(easterBossEntity.m_21223_(), easterBossEntity.m_21233_(), false);
            if (f > 0.0f) {
                easterBossEntity.m_5634_(f);
            }
            state.healAt = serverLevel.m_46467_() + 100L;
        }
    }

    private static boolean contains(State state, Entity entity) {
        if (entity.m_9236_() != state.level) {
            return false;
        }
        Vec3 vec3 = entity.m_20182_().m_82546_(state.boss.m_20182_());
        return ManorDomainRules.contains(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_);
    }

    private static boolean live(State state) {
        return clock - state.seen <= 2L && ManorGroundSupport.eligible(state.boss, state.fight);
    }

    private static State state(EasterBossEntity easterBossEntity) {
        State state = ACTIVE.get(easterBossEntity.m_20148_());
        return state != null && ManorGroundSupport.live(state) && ManorGroundSupport.contains(state, (Entity)easterBossEntity) ? state : null;
    }

    private static State playerDomain(ServerPlayer serverPlayer) {
        State state = null;
        double d = Double.POSITIVE_INFINITY;
        for (State state2 : ACTIVE.values()) {
            double d2;
            if (!ManorGroundSupport.live(state2) || !ManorGroundSupport.contains(state2, (Entity)serverPlayer) || !ManorLivingTargets.valid((Entity)state2.boss, (LivingEntity)serverPlayer) || !((d2 = serverPlayer.m_20238_(state2.boss.m_20182_())) < d) && (d2 != d || state == null || state2.boss.m_20148_().compareTo(state.boss.m_20148_()) >= 0)) continue;
            state = state2;
            d = d2;
        }
        return state;
    }

    private static void updateAttributes(EasterBossEntity easterBossEntity, boolean bl) {
        int n = easterBossEntity.getVariant();
        ManorGroundSupport.modifier((LivingEntity)easterBossEntity, Attributes.f_22284_, ARMOR, bl ? ManorDomainRules.armor(n) : 0.0, AttributeModifier.Operation.ADDITION);
        ManorGroundSupport.modifier((LivingEntity)easterBossEntity, Attributes.f_22279_, SPEED, bl ? ManorDomainEffectRules.bossSpeed(n) : 0.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        ManorGroundSupport.modifier((LivingEntity)easterBossEntity, Attributes.f_22278_, STABILITY, bl ? ManorDomainRules.knockbackResistance(n) : 0.0, AttributeModifier.Operation.ADDITION);
        ManorGroundSupport.modifier((LivingEntity)easterBossEntity, Attributes.f_22281_, ATTACK, bl ? ManorDomainRules.attack(n) : 0.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static void playerAttributes(ServerPlayer serverPlayer, State state) {
        int n = state == null ? -1 : state.variant;
        ManorGroundSupport.modifier((LivingEntity)serverPlayer, Attributes.f_22284_, PLAYER_ARMOR, ManorDomainEffectRules.playerArmor(n), AttributeModifier.Operation.ADDITION);
        ManorGroundSupport.modifier((LivingEntity)serverPlayer, Attributes.f_22279_, PLAYER_SPEED, ManorDomainEffectRules.playerSpeed(n), AttributeModifier.Operation.MULTIPLY_TOTAL);
        ManorGroundSupport.modifier((LivingEntity)serverPlayer, Attributes.f_22281_, PLAYER_ATTACK, ManorDomainEffectRules.playerAttack(n), AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static void modifier(LivingEntity livingEntity, Attribute attribute, UUID uUID, double d, AttributeModifier.Operation operation) {
        AttributeInstance attributeInstance = livingEntity.m_21051_(attribute);
        if (attributeInstance == null) {
            return;
        }
        AttributeModifier attributeModifier = attributeInstance.m_22111_(uUID);
        if (attributeModifier != null && attributeModifier.m_22218_() == d && attributeModifier.m_22217_() == operation) {
            return;
        }
        attributeInstance.m_22120_(uUID);
        if (d != 0.0) {
            attributeInstance.m_22118_(new AttributeModifier(uUID, "ftboss.manor_domain", d, operation));
        }
    }

    private static void send(State state, int n) {
        ManorDomainPacket manorDomainPacket = new ManorDomainPacket(state.level.m_46472_().m_135782_(), state.boss.m_20148_(), state.boss.m_19879_(), state.center, n, n >= 0);
        FTNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(null, state.center.f_82479_, state.center.f_82480_, state.center.f_82481_, 96.0, state.level.m_46472_())), (Object)manorDomainPacket);
    }

    private static void announce(State state, ServerPlayer serverPlayer) {
        if (state.announced.add(serverPlayer.m_20148_())) {
            String string = "\u00a76\u9886\u57df\u5c55\u5f00\uff1a\u00a7e" + ManorDomainEffectRules.name(state.variant);
            if (serverPlayer.f_8906_ != null) {
                serverPlayer.f_8906_.m_9829_((Packet)new ClientboundSetTitlesAnimationPacket(10, 40, 10));
                serverPlayer.f_8906_.m_9829_((Packet)new ClientboundSetSubtitleTextPacket((Component)Component.m_237113_((String)("\u00a77" + ManorDomainEffectRules.kind(state.variant) + " \u00b7 Boss\u7ea2\u5708\u5185\u751f\u6548"))));
                serverPlayer.f_8906_.m_9829_((Packet)new ClientboundSetTitleTextPacket((Component)Component.m_237113_((String)string)));
            }
            serverPlayer.m_213846_((Component)Component.m_237113_((String)(string + " \u00a77[" + ManorDomainEffectRules.kind(state.variant) + "] \u00a7f" + ManorDomainEffectRules.description(state.variant) + " \u00a77\u8303\u56f4\uff1aBoss\u7ea2\u5708\u518576\u683c\uff0c\u968fBoss\u79fb\u52a8\uff0c\u4e0e\u5c55\u5f00\u56fe\u6848\u8303\u56f4\u65e0\u5173\uff1b\u79bb\u5708\u89e3\u9664\u9886\u57df\u5c5e\u6027\u3002")));
        }
    }

    private static void stop(ServerLevel serverLevel, EasterBossEntity easterBossEntity) {
        ManorGroundSupport.updateAttributes(easterBossEntity, false);
        State state = ACTIVE.remove(easterBossEntity.m_20148_());
        if (state != null) {
            ManorGroundSupport.send(state, -1);
            for (PlayerState playerState : new ArrayList<PlayerState>(PLAYERS.values())) {
                if (!playerState.owner.equals(easterBossEntity.m_20148_())) continue;
                ManorGroundSupport.clearPlayer(playerState.player, false);
            }
        }
        LEECH_AT.remove(easterBossEntity.m_20148_());
    }

    private static Entity actor(Entity entity) {
        Entity entity2;
        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile)entity;
            entity2 = projectile.m_19749_();
        } else {
            entity2 = entity;
        }
        return entity2;
    }

    private static boolean opponent(State state, Entity entity, LivingEntity livingEntity) {
        return (ManorLivingTargets.valid(entity, livingEntity) && state.fight.equals(ManorFight.fightId((Entity)livingEntity))) & ManorGroundSupport.contains(state, (Entity)livingEntity);
    }

    private static boolean behind(Entity entity, LivingEntity livingEntity) {
        Vec3 vec3 = entity.m_20182_().m_82546_(livingEntity.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
        return livingEntity.m_20154_().m_82526_(vec3) < -0.35;
    }

    @SubscribeEvent
    public static void hurt(LivingHurtEvent livingHurtEvent) {
        boolean bl;
        Object object;
        State state;
        EasterBossEntity easterBossEntity;
        if (livingHurtEvent.isCanceled() || !Float.isFinite(livingHurtEvent.getAmount()) || livingHurtEvent.getAmount() <= 0.0f || livingHurtEvent.getEntity().m_9236_().f_46443_) {
            return;
        }
        LivingEntity livingEntity = livingHurtEvent.getEntity();
        if (livingEntity instanceof EasterBossEntity) {
            easterBossEntity = (EasterBossEntity)livingEntity;
            state = ManorGroundSupport.state(easterBossEntity);
            if (state != null) {
                livingHurtEvent.setAmount((float)((double)livingHurtEvent.getAmount() * ManorDomainEffectRules.incoming(state.variant)));
            }
        } else if (livingEntity instanceof ServerPlayer && (state = ManorGroundSupport.playerDomain(object = (ServerPlayer)livingEntity)) != null && state.variant == 5) {
            livingHurtEvent.setAmount((float)((double)livingHurtEvent.getAmount() * ManorDomainEffectRules.incoming(5)));
        }
        easterBossEntity = ManorGroundSupport.actor(livingHurtEvent.getSource().m_7639_());
        if (easterBossEntity instanceof ServerPlayer) {
            object = (ServerPlayer)easterBossEntity;
            state = ManorGroundSupport.playerDomain(object);
            if (state != null && ManorGroundSupport.opponent(state, (Entity)object, livingEntity)) {
                boolean bl2 = ManorDomainRules.rangedHit(-1, livingHurtEvent.getSource().m_7640_() != object, livingHurtEvent.getSource().m_19385_(), livingHurtEvent.getSource().getClass().getName(), object.m_20270_((Entity)livingEntity));
                livingHurtEvent.setAmount((float)((double)livingHurtEvent.getAmount() * ManorDomainEffectRules.playerOutgoing(state.variant, bl2, object.m_20270_((Entity)livingEntity), ManorGroundSupport.behind((Entity)object, livingEntity))));
            }
            return;
        }
        if (!(easterBossEntity instanceof EasterBossEntity)) {
            return;
        }
        object = easterBossEntity;
        state = ManorGroundSupport.state((EasterBossEntity)object);
        if (state == null || !(ManorLivingTargets.valid((Entity)object, livingEntity) & ManorGroundSupport.contains(state, (Entity)livingEntity))) {
            return;
        }
        double d = object.m_20270_((Entity)livingEntity);
        boolean bl3 = ManorDomainRules.rangedHit(state.variant, livingHurtEvent.getSource().m_7640_() != object, livingHurtEvent.getSource().m_19385_(), livingHurtEvent.getSource().getClass().getName(), d);
        if (bl3) {
            ++state.cadence;
        }
        double d2 = ManorDomainRules.outgoing(state.variant, bl3, d, livingEntity.m_21223_() / Math.max(1.0f, livingEntity.m_21233_()), ManorGroundSupport.behind((Entity)object, livingEntity), state.cadence);
        livingHurtEvent.setAmount((float)((double)livingHurtEvent.getAmount() * d2));
        switch (state.variant) {
            case 1: 
            case 5: 
            case 6: {
                boolean bl4 = bl3;
                break;
            }
            case 4: 
            case 8: 
            case 10: {
                boolean bl4;
                if (!bl3) {
                    bl4 = true;
                    break;
                }
                bl4 = false;
                break;
            }
            default: {
                boolean bl4 = bl = false;
            }
        }
        if (!bl || state.level.m_46467_() < state.effectAt) {
            return;
        }
        state.effectAt = state.level.m_46467_() + 40L;
        switch (state.variant) {
            case 1: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19615_, 40, 0, false, true));
                break;
            }
            case 4: {
                livingEntity.m_20254_(3);
                break;
            }
            case 5: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 35, 0, false, true));
                break;
            }
            case 6: {
                livingEntity.m_147240_(0.35, object.m_20185_() - livingEntity.m_20185_(), object.m_20189_() - livingEntity.m_20189_());
                break;
            }
            case 8: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19614_, 60, 0, false, true));
                livingEntity.m_147240_(0.45, object.m_20185_() - livingEntity.m_20185_(), object.m_20189_() - livingEntity.m_20189_());
                break;
            }
            case 10: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 60, 1, false, true));
                break;
            }
        }
    }

    @SubscribeEvent
    public static void heal(LivingHealEvent livingHealEvent) {
        ServerPlayer serverPlayer;
        Object object;
        if (!livingHealEvent.isCanceled() && (object = livingHealEvent.getEntity()) instanceof ServerPlayer && (object = ManorGroundSupport.playerDomain(serverPlayer = (ServerPlayer)object)) != null) {
            livingHealEvent.setAmount((float)((double)livingHealEvent.getAmount() * ManorDomainEffectRules.healing(object.variant)));
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void damage(LivingDamageEvent livingDamageEvent) {
        State state;
        ServerPlayer serverPlayer;
        if (livingDamageEvent.isCanceled() || !Float.isFinite(livingDamageEvent.getAmount()) || livingDamageEvent.getAmount() <= 0.0f) {
            return;
        }
        Entity entity = ManorGroundSupport.actor(livingDamageEvent.getSource().m_7639_());
        if (entity instanceof EasterBossEntity) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)entity;
            v0 = ManorGroundSupport.state(easterBossEntity);
        } else if (entity instanceof ServerPlayer) {
            serverPlayer = (ServerPlayer)entity;
            v0 = ManorGroundSupport.playerDomain(serverPlayer);
        } else {
            v0 = state = null;
        }
        if (state == null || state.variant != 1 || !(entity instanceof LivingEntity)) {
            return;
        }
        serverPlayer = (LivingEntity)entity;
        if (entity instanceof ServerPlayer ? !ManorGroundSupport.opponent(state, entity, livingDamageEvent.getEntity()) : !(ManorLivingTargets.valid(entity, livingDamageEvent.getEntity()) & ManorGroundSupport.contains(state, (Entity)livingDamageEvent.getEntity()))) {
            return;
        }
        PENDING.add(new PendingLeech(livingDamageEvent, (LivingEntity)serverPlayer, state.boss.m_20148_(), livingDamageEvent.getEntity().m_21223_()));
    }

    private static void leech() {
        ArrayList<PendingLeech> arrayList = new ArrayList<PendingLeech>(PENDING);
        PENDING.clear();
        for (PendingLeech pendingLeech : arrayList) {
            float f;
            long l;
            State state;
            Object object;
            EasterBossEntity easterBossEntity;
            State state2 = ACTIVE.get(pendingLeech.owner);
            LivingEntity livingEntity = pendingLeech.actor;
            if (livingEntity instanceof EasterBossEntity) {
                easterBossEntity = (EasterBossEntity)livingEntity;
                v0 = ManorGroundSupport.state(easterBossEntity);
            } else {
                livingEntity = pendingLeech.actor;
                if (livingEntity instanceof ServerPlayer) {
                    object = (ServerPlayer)livingEntity;
                    v0 = ManorGroundSupport.playerDomain((ServerPlayer)object);
                } else {
                    v0 = state = null;
                }
            }
            if (state2 == null || state2 != state || pendingLeech.event.isCanceled() || !pendingLeech.actor.m_6084_() || (easterBossEntity = pendingLeech.event.getEntity()) instanceof EasterBossEntity && !((EasterBossEntity)(object = easterBossEntity)).isMini() && ManorDamageRules.ignored(pendingLeech.event.getAmount()) || (l = clock) < LEECH_AT.getOrDefault(pendingLeech.actor.m_20148_(), Long.MIN_VALUE) || !((f = ManorDomainEffectRules.leech(pendingLeech.event.getAmount(), pendingLeech.availableHealth)) > 0.0f)) continue;
            pendingLeech.actor.m_5634_(f);
            LEECH_AT.put(pendingLeech.actor.m_20148_(), l + 20L);
        }
    }

    private static void clearPlayer(ServerPlayer serverPlayer, boolean bl) {
        PlayerState playerState = PLAYERS.remove(serverPlayer.m_20148_());
        ManorGroundSupport.playerAttributes(serverPlayer, null);
        if (bl && playerState != null && serverPlayer.m_6084_()) {
            serverPlayer.m_5661_((Component)Component.m_237113_((String)"\u00a7a\u3010\u5e84\u56ed\u9886\u57df\u3011\u00a77\u5df2\u79bb\u5f00Boss\u7ea2\u5708\uff0c\u9886\u57df\u5c5e\u6027\u89e3\u9664\u3002"), true);
        }
    }

    private static void updatePlayers() {
        HashSet<ServerPlayer> hashSet = new HashSet<ServerPlayer>();
        for (State object : ACTIVE.values()) {
            hashSet.addAll(object.level.m_6907_());
        }
        for (PlayerState playerState : PLAYERS.values()) {
            hashSet.add(playerState.player);
        }
        for (ServerPlayer serverPlayer : hashSet) {
            State state = ManorGroundSupport.playerDomain(serverPlayer);
            if (state == null) {
                ManorGroundSupport.clearPlayer(serverPlayer, true);
                continue;
            }
            PlayerState playerState = PLAYERS.get(serverPlayer.m_20148_());
            if (playerState == null || playerState.player != serverPlayer || !playerState.owner.equals(state.boss.m_20148_())) {
                if (playerState != null) {
                    ManorGroundSupport.playerAttributes(playerState.player, null);
                }
                playerState = new PlayerState(serverPlayer, state.boss.m_20148_(), state.level.m_46467_() + 100L);
                PLAYERS.put(serverPlayer.m_20148_(), playerState);
                ManorGroundSupport.announce(state, serverPlayer);
                serverPlayer.m_5661_((Component)Component.m_237113_((String)("\u00a76\u3010\u9886\u57df\u751f\u6548\u3011\u00a7e" + ManorDomainEffectRules.name(state.variant) + " \u00a77[" + ManorDomainEffectRules.kind(state.variant) + "]")), true);
            }
            ManorGroundSupport.playerAttributes(serverPlayer, state);
            if (state.variant != 11 || state.level.m_46467_() < playerState.healAt) continue;
            float f = ManorDomainEffectRules.prayer(serverPlayer.m_21223_(), serverPlayer.m_21233_(), true);
            if (f > 0.0f) {
                serverPlayer.m_5634_(f);
            }
            playerState.healAt = state.level.m_46467_() + 100L;
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent livingDeathEvent) {
        EasterBossEntity easterBossEntity;
        LivingEntity livingEntity = livingDeathEvent.getEntity();
        if (livingEntity instanceof EasterBossEntity && (livingEntity = (easterBossEntity = (EasterBossEntity)livingEntity).m_9236_()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)livingEntity;
            ManorGroundSupport.stop(serverLevel, easterBossEntity);
        } else {
            livingEntity = livingDeathEvent.getEntity();
            if (livingEntity instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
                ManorGroundSupport.clearPlayer(serverPlayer, false);
            }
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent playerLoggedOutEvent) {
        Player player = playerLoggedOutEvent.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            ManorGroundSupport.clearPlayer(serverPlayer, false);
        }
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent playerChangedDimensionEvent) {
        Player player = playerChangedDimensionEvent.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            ManorGroundSupport.clearPlayer(serverPlayer, false);
        }
    }

    @SubscribeEvent
    public static void tickServer(TickEvent.ServerTickEvent serverTickEvent) {
        if (serverTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        for (State state2 : new ArrayList<State>(ACTIVE.values())) {
            if (ManorGroundSupport.live(state2)) continue;
            ManorGroundSupport.stop(state2.level, state2.boss);
        }
        ManorGroundSupport.updatePlayers();
        ManorGroundSupport.leech();
        ++clock;
        LEECH_AT.entrySet().removeIf(entry -> (Long)entry.getValue() <= clock);
        INTRODUCED.values().removeIf(state -> !state.boss.m_6084_() || state.boss.m_213877_() || !ManorFight.hasActive(state.fight) || !state.boss.m_20148_().equals(ManorFight.currentBoss(state.fight)));
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        for (State state2 : new ArrayList<State>(ACTIVE.values())) {
            if (state2.level != unload.getLevel()) continue;
            ManorGroundSupport.stop(state2.level, state2.boss);
        }
        INTRODUCED.values().removeIf(state -> state.level == unload.getLevel());
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent serverStoppedEvent) {
        for (State object : new ArrayList<State>(ACTIVE.values())) {
            ManorGroundSupport.updateAttributes(object.boss, false);
        }
        for (PlayerState playerState : new ArrayList<PlayerState>(PLAYERS.values())) {
            ManorGroundSupport.clearPlayer(playerState.player, false);
        }
        ACTIVE.clear();
        INTRODUCED.clear();
        PENDING.clear();
        LEECH_AT.clear();
        clock = 0L;
    }

    private static final class State {
        final ServerLevel level;
        final EasterBossEntity boss;
        final UUID fight;
        final Vec3 anchor;
        final Vec3 center;
        final int variant;
        final Set<UUID> announced;
        boolean inside;
        long seen;
        long refresh;
        long effectAt;
        long healAt;
        int cadence;

        State(ServerLevel serverLevel, EasterBossEntity easterBossEntity, UUID uUID, Vec3 vec3, Vec3 vec32) {
            this.level = serverLevel;
            this.boss = easterBossEntity;
            this.fight = uUID;
            this.anchor = vec3;
            this.center = vec32;
            this.variant = easterBossEntity.getVariant();
            State state = INTRODUCED.get(easterBossEntity.m_20148_());
            this.announced = state != null && state.variant == this.variant && state.level == serverLevel && state.fight.equals(uUID) ? state.announced : new HashSet<UUID>();
            INTRODUCED.put(easterBossEntity.m_20148_(), this);
        }
    }

    private static final class PlayerState {
        final ServerPlayer player;
        final UUID owner;
        long healAt;

        PlayerState(ServerPlayer serverPlayer, UUID uUID, long l) {
            this.player = serverPlayer;
            this.owner = uUID;
            this.healAt = l;
        }
    }

    private record PendingLeech(LivingDamageEvent event, LivingEntity actor, UUID owner, float availableHealth) {
    }
}

