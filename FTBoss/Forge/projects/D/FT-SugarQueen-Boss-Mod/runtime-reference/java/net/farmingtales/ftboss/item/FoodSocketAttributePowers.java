/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.OwnableEntity
 *  net.minecraft.world.entity.TamableAnimal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$Clone
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.farmingtales.ftboss.item.FoodSocketAttributeCatalog;
import net.farmingtales.ftboss.item.FoodWeaponBondHits;
import net.farmingtales.ftboss.item.FoodWeaponConsumerSequence;
import net.farmingtales.ftboss.item.FoodWeaponDamageSources;
import net.farmingtales.ftboss.item.FoodWeaponEchoQueue;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponFinalDamage;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponOriginalSequence;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class FoodSocketAttributePowers {
    private static final String COOLDOWNS = "FTBossFoodSocketAttributeCooldowns";
    private static final float ECHO_DAMAGE = 2.0f;
    private static final float BURST_DAMAGE = 4.0f;
    private static final double SPLASH_RADIUS = 3.0;
    private static final double REPEL_STRENGTH = 0.6;
    private static final int DELAY_GRACE_TICKS = 20;
    private static final FoodWeaponEchoQueue<PendingEcho> ECHOES = new FoodWeaponEchoQueue();
    private static final Map<UUID, CombatState> GUARDS = new HashMap<UUID, CombatState>();
    private static final Map<UUID, ProcessState> PROCESS = new HashMap<UUID, ProcessState>();
    private static final ThreadLocal<Boolean> EXTRA_HIT = ThreadLocal.withInitial(() -> false);

    private FoodSocketAttributePowers() {
    }

    public static FoodSocketAttributeRules.Resolution inspect(ItemStack itemStack) {
        ArrayList<FoodSocketAttributeRules.Ingredient> arrayList = new ArrayList<FoodSocketAttributeRules.Ingredient>();
        if (itemStack != null && !itemStack.m_41619_() && FoodWeaponEvolution.isEvolutionCandidate(itemStack)) {
            for (FoodWeaponEvolution.SocketView socketView : FoodWeaponEvolution.socketViews(itemStack)) {
                FoodSocketAttributeRules.Ingredient ingredient = FoodSocketAttributeCatalog.ingredient(socketView.stack());
                if (ingredient == null) continue;
                arrayList.add(ingredient);
            }
        }
        return FoodSocketAttributeRules.resolve(arrayList);
    }

    private static String id(FoodSocketAttributeRules.Power power) {
        return power.name().toLowerCase(Locale.ROOT);
    }

    private static Component title(FoodSocketAttributeRules.Power power, FoodSocketAttributeRules.Resolution resolution) {
        return Component.m_237110_((String)("tooltip.ftboss.socket_attribute." + FoodSocketAttributePowers.id(power) + ".title"), (Object[])new Object[]{Component.m_237115_((String)(resolution.has(power) ? "tooltip.ftboss.socket_attribute.active" : "tooltip.ftboss.socket_attribute.missing"))});
    }

    private static Component description(FoodSocketAttributeRules.Power power, FoodSocketAttributeRules.Resolution resolution) {
        String string = "tooltip.ftboss.socket_attribute." + FoodSocketAttributePowers.id(power) + ".description";
        return switch (power) {
            default -> throw new IncompatibleClassChangeError();
            case FoodSocketAttributeRules.Power.PRICE_GUARD -> Component.m_237110_((String)string, (Object[])new Object[]{resolution.priceProgress(), 3, Float.valueOf(resolution.guardHealth())});
            case FoodSocketAttributeRules.Power.SERIES_ECHO -> Component.m_237110_((String)string, (Object[])new Object[]{resolution.seriesProgress(), resolution.series().isEmpty() ? "\u2014" : resolution.series()});
            case FoodSocketAttributeRules.Power.COLOR_REPEL -> Component.m_237110_((String)string, (Object[])new Object[]{resolution.colorProgress()});
            case FoodSocketAttributeRules.Power.PROCESS_BURST -> Component.m_237110_((String)string, (Object[])new Object[]{resolution.processProgress()});
        };
    }

    private static long clock(ServerPlayer serverPlayer) {
        return serverPlayer.m_284548_().m_7654_().m_129783_().m_46467_();
    }

    private static int cooldown(FoodSocketAttributeRules.Power power) {
        return switch (power) {
            default -> throw new IncompatibleClassChangeError();
            case FoodSocketAttributeRules.Power.PRICE_GUARD -> 600;
            case FoodSocketAttributeRules.Power.COLOR_REPEL -> 400;
            case FoodSocketAttributeRules.Power.SERIES_ECHO, FoodSocketAttributeRules.Power.PROCESS_BURST -> 400;
        };
    }

    private static boolean ready(ServerPlayer serverPlayer, FoodSocketAttributeRules.Power power, long l) {
        String string;
        if (serverPlayer.getPersistentData().m_128441_(COOLDOWNS) && !serverPlayer.getPersistentData().m_128425_(COOLDOWNS, 10)) {
            CompoundTag compoundTag = new CompoundTag();
            for (FoodSocketAttributeRules.Power power2 : FoodSocketAttributeRules.Power.values()) {
                compoundTag.m_128356_(power2.name(), FoodSocketAttributeRules.deadline(l, FoodSocketAttributePowers.cooldown(power2)));
            }
            serverPlayer.getPersistentData().m_128365_(COOLDOWNS, (Tag)compoundTag);
            return false;
        }
        CompoundTag compoundTag = serverPlayer.getPersistentData().m_128469_(COOLDOWNS);
        if (!compoundTag.m_128441_(string = power.name())) {
            return true;
        }
        long l2 = compoundTag.m_128425_(string, 4) ? compoundTag.m_128454_(string) : -1L;
        long l3 = FoodSocketAttributeRules.boundedCooldown(l, l2, FoodSocketAttributePowers.cooldown(power));
        if (l3 != l2 || !compoundTag.m_128425_(string, 4)) {
            compoundTag.m_128356_(string, l3);
            serverPlayer.getPersistentData().m_128365_(COOLDOWNS, (Tag)compoundTag);
        }
        return l >= l3;
    }

    private static void consume(ServerPlayer serverPlayer, FoodSocketAttributeRules.Power power, long l) {
        CompoundTag compoundTag = serverPlayer.getPersistentData().m_128469_(COOLDOWNS);
        compoundTag.m_128356_(power.name(), FoodSocketAttributeRules.deadline(l, FoodSocketAttributePowers.cooldown(power)));
        serverPlayer.getPersistentData().m_128365_(COOLDOWNS, (Tag)compoundTag);
    }

    private static CombatState guardState(ServerPlayer serverPlayer, ItemStack itemStack) {
        if (!serverPlayer.m_6084_() || serverPlayer.m_5833_() || itemStack.m_41613_() != 1 || FoodWeaponLifecycle.isBroken(itemStack)) {
            return null;
        }
        FoodSocketAttributeRules.Resolution resolution = FoodSocketAttributePowers.inspect(itemStack);
        if (!resolution.has(FoodSocketAttributeRules.Power.PRICE_GUARD)) {
            return null;
        }
        CombatState combatState = GUARDS.get(serverPlayer.m_20148_());
        if (combatState == null || !FoodWeaponSourceLedger.originalHandContinuous(combatState.source, serverPlayer) || !combatState.weaponId.equals(FoodWeaponLifecycle.weaponId(itemStack))) {
            FoodWeaponLifecycle.ensureTracked(itemStack);
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.m_128359_("Family", FoodWeaponFamilyBuild.kind(itemStack).name());
            compoundTag = FoodWeaponSourceLedger.decorate(serverPlayer, itemStack, compoundTag, serverPlayer.m_21205_() == itemStack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            if (!FoodWeaponSourceLedger.originalHandContinuous(compoundTag, serverPlayer)) {
                return null;
            }
            combatState = new CombatState(serverPlayer, itemStack, resolution, compoundTag);
            GUARDS.put(serverPlayer.m_20148_(), combatState);
        }
        return combatState;
    }

    private static boolean target(ServerPlayer serverPlayer, LivingEntity livingEntity) {
        OwnableEntity ownableEntity;
        TamableAnimal tamableAnimal;
        return !(!(livingEntity instanceof Monster) || !livingEntity.m_6084_() || livingEntity.m_213877_() || livingEntity.m_9236_() != serverPlayer.m_9236_() || livingEntity.m_7307_((Entity)serverPlayer) || serverPlayer.m_7307_((Entity)livingEntity) || livingEntity instanceof TamableAnimal && (tamableAnimal = (TamableAnimal)livingEntity).m_21824_() || livingEntity instanceof OwnableEntity && (ownableEntity = (OwnableEntity)livingEntity).m_21805_() != null);
    }

    private static void cue(ServerPlayer serverPlayer, FoodSocketAttributeRules.Power power) {
        serverPlayer.m_5661_((Component)Component.m_237115_((String)("message.ftboss.socket_attribute." + FoodSocketAttributePowers.id(power))), true);
    }

    private static boolean readyView(ServerPlayer serverPlayer, FoodSocketAttributeRules.Power power, long l) {
        CompoundTag compoundTag = serverPlayer.getPersistentData();
        if (!compoundTag.m_128441_(COOLDOWNS)) {
            return true;
        }
        if (!compoundTag.m_128425_(COOLDOWNS, 10)) {
            return false;
        }
        CompoundTag compoundTag2 = compoundTag.m_128469_(COOLDOWNS);
        if (!compoundTag2.m_128441_(power.name())) {
            return true;
        }
        if (!compoundTag2.m_128425_(power.name(), 4)) {
            return false;
        }
        long l2 = compoundTag2.m_128454_(power.name());
        return l2 == FoodSocketAttributeRules.boundedCooldown(l, l2, FoodSocketAttributePowers.cooldown(power)) && l >= l2;
    }

    public static DefenseQuote quoteDefense(ServerPlayer serverPlayer, DamageSource damageSource, float f) {
        boolean bl;
        if (damageSource == null || !Float.isFinite(f) || f <= 0.0f) {
            return new DefenseQuote(0.0f, null, DefenseRejection.INVALID_INPUT);
        }
        if (serverPlayer == null || !serverPlayer.m_6084_() || serverPlayer.m_5833_()) {
            return new DefenseQuote(f, null, DefenseRejection.INVALID_DEFENDER);
        }
        if (EXTRA_HIT.get().booleanValue() || FoodWeaponBondHits.secondary(damageSource) || damageSource.m_269533_(DamageTypeTags.f_268738_)) {
            return new DefenseQuote(f, null, DefenseRejection.SECONDARY_OR_BYPASS);
        }
        ItemStack itemStack2 = FoodWeaponBondHits.preferred(serverPlayer, itemStack -> FoodWeaponEvolution.isEvolutionCandidate(itemStack) && FoodSocketAttributePowers.inspect(itemStack).has(FoodSocketAttributeRules.Power.PRICE_GUARD));
        if (itemStack2.m_41619_()) {
            return new DefenseQuote(f, null, DefenseRejection.NO_ELIGIBLE_WEAPON);
        }
        if (FoodWeaponLifecycle.weaponId(itemStack2) == null) {
            return new DefenseQuote(f, null, DefenseRejection.UNTRACKED_WEAPON);
        }
        if (FoodWeaponSourceLedger.materialFingerprint(itemStack2).isEmpty()) {
            return new DefenseQuote(f, null, DefenseRejection.INVALID_MATERIALS);
        }
        FoodSocketAttributeRules.Resolution resolution = FoodSocketAttributePowers.inspect(itemStack2);
        CombatState combatState = GUARDS.get(serverPlayer.m_20148_());
        boolean bl2 = combatState != null && FoodWeaponSourceLedger.originalHandContinuousReadOnly(combatState.source, serverPlayer) && combatState.weaponId.equals(FoodWeaponLifecycle.weaponId(itemStack2));
        long l = FoodSocketAttributePowers.clock(serverPlayer);
        float f2 = bl2 && FoodSocketAttributeRules.active(l, combatState.guardUntil, 80) ? combatState.guard : 0.0f;
        boolean bl3 = bl = f2 <= 0.0f && FoodSocketAttributePowers.readyView(serverPlayer, FoodSocketAttributeRules.Power.PRICE_GUARD, l);
        if (bl) {
            f2 = resolution.guardHealth();
        }
        if (!Float.isFinite(f2) || f2 <= 0.0f) {
            return new DefenseQuote(f, null, DefenseRejection.NO_GUARD_AVAILABLE);
        }
        long l2 = bl ? FoodSocketAttributeRules.deadline(l, 80) : combatState.guardUntil;
        DefensePlan defensePlan = new DefensePlan(serverPlayer, damageSource, serverPlayer.m_21205_() == itemStack2 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, itemStack2, combatState, l, f, f2, l2, bl);
        return new DefenseQuote(defensePlan.amount(), defensePlan, DefenseRejection.NONE);
    }

    public static void commitDefense(DefensePlan defensePlan, float f, FoodWeaponFinalDamage.DefenseReceipt defenseReceipt) {
        if (defensePlan == null || defensePlan.committed || defenseReceipt == null || !Float.isFinite(f) || f < 0.0f || !Float.isFinite(defensePlan.input) || defensePlan.input <= 0.0f || !Float.isFinite(defensePlan.blocked) || defensePlan.blocked <= 0.0f || EXTRA_HIT.get() != false || !defensePlan.player.m_6084_() || defensePlan.player.m_5833_() || FoodWeaponBondHits.secondary(defensePlan.source) || defensePlan.source.m_269533_(DamageTypeTags.f_268738_)) {
            return;
        }
        if (!defenseReceipt.matches(defensePlan, defensePlan.player, defensePlan.source, defensePlan.actualWeapon, defensePlan.hand, defensePlan.at, f) || defenseReceipt.reason() == FoodWeaponFinalDamage.DefenseReason.POSITIVE_DEFENSE && (f <= 0.0f || f > defensePlan.amount) || defenseReceipt.reason() == FoodWeaponFinalDamage.DefenseReason.SHIELD_ONLY_ZERO && (f != 0.0f || defensePlan.amount != 0.0f || defensePlan.blocked != defensePlan.input)) {
            return;
        }
        ServerPlayer serverPlayer = defensePlan.player;
        ItemStack itemStack = serverPlayer.m_21120_(defensePlan.hand);
        CombatState combatState = GUARDS.get(serverPlayer.m_20148_());
        if (!defensePlan.matchesContext(serverPlayer, defensePlan.source, itemStack, defensePlan.hand, defensePlan.at) || FoodSocketAttributePowers.clock(serverPlayer) != defensePlan.at || !serverPlayer.m_9236_().m_46472_().equals(defensePlan.dimension) || itemStack.m_41619_() || itemStack.m_41613_() != 1 || itemStack.m_41720_() != defensePlan.weapon.m_41720_() || !Objects.equals(FoodWeaponLifecycle.weaponId(itemStack), FoodWeaponLifecycle.weaponId(defensePlan.weapon)) || FoodWeaponLifecycle.isBroken(itemStack) || !FoodSocketAttributePowers.inspect(itemStack).has(FoodSocketAttributeRules.Power.PRICE_GUARD) || !FoodWeaponBondHits.fingerprint(itemStack).equals((Object)defensePlan.fingerprint) || !FoodWeaponSourceLedger.materialFingerprint(itemStack).equals(defensePlan.materials) || combatState != defensePlan.previous || combatState != null && (combatState.guard != defensePlan.previousGuard || combatState.guardUntil != defensePlan.previousUntil) || defensePlan.starts && !FoodSocketAttributePowers.readyView(serverPlayer, FoodSocketAttributeRules.Power.PRICE_GUARD, defensePlan.at)) {
            return;
        }
        CombatState combatState2 = FoodSocketAttributePowers.guardState(serverPlayer, itemStack);
        if (combatState2 == null) {
            return;
        }
        defensePlan.committed = true;
        combatState2.guard = defensePlan.balance;
        combatState2.guardUntil = defensePlan.until;
        if (defensePlan.starts) {
            FoodSocketAttributePowers.consume(serverPlayer, FoodSocketAttributeRules.Power.PRICE_GUARD, defensePlan.at);
            FoodSocketAttributePowers.cue(serverPlayer, FoodSocketAttributeRules.Power.PRICE_GUARD);
            serverPlayer.m_284548_().m_8767_((ParticleOptions)ParticleTypes.f_123809_, serverPlayer.m_20185_(), serverPlayer.m_20186_() + 1.0, serverPlayer.m_20189_(), 10, 0.4, 0.4, 0.4, 0.02);
        }
    }

    @SubscribeEvent(priority=EventPriority.NORMAL)
    public static void attack(LivingHurtEvent livingHurtEvent) {
        OffensePlan offensePlan = FoodSocketAttributePowers.quoteOffense(livingHurtEvent.getEntity(), livingHurtEvent.getSource(), livingHurtEvent.getAmount());
        FoodWeaponFinalDamage.captureAttribute(livingHurtEvent, offensePlan);
    }

    public static OffensePlan quoteOffense(LivingEntity livingEntity, DamageSource damageSource, float f) {
        ServerPlayer serverPlayer;
        Object object;
        if (livingEntity == null || damageSource == null || !Float.isFinite(f) || f <= 0.0f || FoodWeaponBondHits.secondary(damageSource) || EXTRA_HIT.get().booleanValue() || !((object = damageSource.m_7639_()) instanceof ServerPlayer) || !(serverPlayer = (ServerPlayer)object).m_6084_() || serverPlayer.m_5833_() || !FoodSocketAttributePowers.target(serverPlayer, livingEntity)) {
            return null;
        }
        object = FoodWeaponBondHits.resolve(damageSource, serverPlayer);
        if (object == null) {
            return null;
        }
        long l = FoodSocketAttributePowers.clock(serverPlayer);
        FoodSocketAttributeRules.Resolution resolution = FoodSocketAttributePowers.inspect(((FoodWeaponBondHits.Hit)object).weapon());
        ArrayList<FoodWeaponSourceLedger.AttemptRequest> arrayList = new ArrayList<FoodWeaponSourceLedger.AttemptRequest>();
        CompoundTag compoundTag = ((FoodWeaponBondHits.Hit)object).snapshot();
        FoodWeaponEchoQueue.Key key = new FoodWeaponEchoQueue.Key(serverPlayer.m_20148_(), compoundTag.m_128342_("WeaponId"), compoundTag.m_128461_("OriginalHand"), ((FoodWeaponBondHits.Hit)object).attack());
        if (resolution.has(FoodSocketAttributeRules.Power.SERIES_ECHO) && FoodSocketAttributePowers.readyView(serverPlayer, FoodSocketAttributeRules.Power.SERIES_ECHO, l) && FoodWeaponSourceLedger.originalHandContinuousReadOnly(compoundTag, serverPlayer) && ECHOES.canOffer(key) && FoodSocketAttributePowers.availableAttempt(compoundTag, serverPlayer, FoodWeaponSourceLedger.AttemptDomain.SERIES_ECHO)) {
            arrayList.add(new FoodWeaponSourceLedger.AttemptRequest(FoodWeaponSourceLedger.AttemptDomain.SERIES_ECHO, 1.0));
        }
        if (resolution.has(FoodSocketAttributeRules.Power.COLOR_REPEL) && FoodSocketAttributePowers.readyView(serverPlayer, FoodSocketAttributeRules.Power.COLOR_REPEL, l) && FoodSocketAttributePowers.availableAttempt(compoundTag, serverPlayer, FoodWeaponSourceLedger.AttemptDomain.COLOR_REPEL)) {
            arrayList.add(new FoodWeaponSourceLedger.AttemptRequest(FoodWeaponSourceLedger.AttemptDomain.COLOR_REPEL, 1.0));
        }
        if (resolution.has(FoodSocketAttributeRules.Power.PROCESS_BURST) && FoodSocketAttributePowers.readyView(serverPlayer, FoodSocketAttributeRules.Power.PROCESS_BURST, l) && FoodSocketAttributePowers.availableAttempt(compoundTag, serverPlayer, FoodWeaponSourceLedger.AttemptDomain.PROCESS_BURST)) {
            arrayList.add(new FoodWeaponSourceLedger.AttemptRequest(FoodWeaponSourceLedger.AttemptDomain.PROCESS_BURST, 1.0));
        }
        return new OffensePlan(serverPlayer, livingEntity, damageSource, (FoodWeaponBondHits.Hit)object, resolution, f, l, arrayList);
    }

    private static boolean availableAttempt(CompoundTag compoundTag, ServerPlayer serverPlayer, FoodWeaponSourceLedger.AttemptDomain attemptDomain) {
        return FoodWeaponSourceLedger.quoteAttempt(compoundTag, serverPlayer, attemptDomain, 1.0).status() == FoodWeaponSourceLedger.AttemptStatus.AVAILABLE;
    }

    private static FoodSocketAttributeRules.Power ownedPower(FoodWeaponSourceLedger.AttemptDomain attemptDomain) {
        if (attemptDomain == null) {
            return null;
        }
        return switch (attemptDomain) {
            case FoodWeaponSourceLedger.AttemptDomain.SERIES_ECHO -> FoodSocketAttributeRules.Power.SERIES_ECHO;
            case FoodWeaponSourceLedger.AttemptDomain.COLOR_REPEL -> FoodSocketAttributeRules.Power.COLOR_REPEL;
            case FoodWeaponSourceLedger.AttemptDomain.PROCESS_BURST -> FoodSocketAttributeRules.Power.PROCESS_BURST;
            default -> null;
        };
    }

    private static boolean offenseContext(OffensePlan offensePlan) {
        if (offensePlan == null || offensePlan.committed || EXTRA_HIT.get().booleanValue() || !offensePlan.player.m_284548_().m_7654_().m_18695_() || FoodSocketAttributePowers.clock(offensePlan.player) != offensePlan.quotedAt || !offensePlan.player.m_6084_() || offensePlan.player.m_5833_() || !FoodSocketAttributePowers.target(offensePlan.player, offensePlan.target) || FoodWeaponBondHits.secondary(offensePlan.source) || offensePlan.player.m_21120_(offensePlan.hand) != offensePlan.actualWeapon || FoodWeaponBondHits.equipped(offensePlan.player, offensePlan.hit).m_41619_() || !FoodWeaponSourceLedger.validReadOnly(offensePlan.hit.snapshot(), offensePlan.player)) {
            return false;
        }
        if (!Objects.equals(offensePlan.cooldowns, offensePlan.player.getPersistentData().m_128423_(COOLDOWNS)) || !ECHOES.entries(offensePlan.player.m_20148_()).equals(offensePlan.echoes)) {
            return false;
        }
        FoodSocketAttributeRules.Resolution resolution = FoodSocketAttributePowers.inspect(offensePlan.actualWeapon);
        for (FoodWeaponSourceLedger.AttemptRequest attemptRequest : offensePlan.attempts) {
            ProcessState processState;
            FoodSocketAttributeRules.Power power = FoodSocketAttributePowers.ownedPower(attemptRequest.domain());
            if (power == null || !resolution.has(power) || !FoodSocketAttributePowers.readyView(offensePlan.player, power, offensePlan.quotedAt)) {
                return false;
            }
            if (power == FoodSocketAttributeRules.Power.SERIES_ECHO && !ECHOES.canOffer(new FoodWeaponEchoQueue.Key(offensePlan.player.m_20148_(), offensePlan.weaponId(), offensePlan.hit.snapshot().m_128461_("OriginalHand"), offensePlan.attackId()))) {
                return false;
            }
            if (power != FoodSocketAttributeRules.Power.PROCESS_BURST || (processState = PROCESS.get(offensePlan.player.m_20148_())) == offensePlan.process && (processState == null || processState.hits == offensePlan.processHits && processState.lastHit == offensePlan.processLastHit)) continue;
            return false;
        }
        return true;
    }

    private static boolean offenseRequests(OffensePlan offensePlan, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        if (offensePlan == null || !FoodWeaponSourceLedger.matchesAttemptRequests(attemptBatchQuote, list, list2)) {
            return false;
        }
        EnumSet<FoodWeaponSourceLedger.AttemptDomain> enumSet = EnumSet.noneOf(FoodWeaponSourceLedger.AttemptDomain.class);
        for (FoodWeaponSourceLedger.AttemptRequest object : offensePlan.attempts) {
            if (object != null && FoodSocketAttributePowers.ownedPower(object.domain()) != null && Double.compare(object.chance(), 1.0) == 0 && enumSet.add(object.domain())) continue;
            return false;
        }
        EnumSet<FoodWeaponSourceLedger.AttemptDomain> enumSet2 = EnumSet.noneOf(FoodWeaponSourceLedger.AttemptDomain.class);
        for (FoodWeaponSourceLedger.AttemptRequest attemptRequest : list) {
            if (FoodSocketAttributePowers.ownedPower(attemptRequest.domain()) == null || enumSet.contains((Object)attemptRequest.domain()) && enumSet2.add(attemptRequest.domain())) continue;
            return false;
        }
        if (!enumSet2.equals(enumSet)) {
            return false;
        }
        for (FoodWeaponSourceLedger.AttemptQuote attemptQuote : attemptBatchQuote.entries()) {
            if (!enumSet.contains((Object)attemptQuote.domain()) || attemptQuote.status() == FoodWeaponSourceLedger.AttemptStatus.AVAILABLE && Double.compare(attemptQuote.chance(), 1.0) == 0 && attemptQuote.success()) continue;
            return false;
        }
        return true;
    }

    public static boolean matchesOffenseBatch(OffensePlan offensePlan, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        return FoodSocketAttributePowers.offenseContext(offensePlan) && FoodSocketAttributePowers.offenseRequests(offensePlan, attemptBatchQuote, list, list2) && FoodWeaponSourceLedger.matchesAttemptAdmission(offensePlan.hit.snapshot(), offensePlan.player, attemptBatchQuote, attemptBatchAdmission, offensePlan.quotedAt);
    }

    private static boolean orderedAttributeBatch(CompleteBatchBinding completeBatchBinding) {
        if (completeBatchBinding == null || completeBatchBinding.requests.size() != completeBatchBinding.batch.entries().size() || completeBatchBinding.targets.size() != completeBatchBinding.batch.targets().size()) {
            return false;
        }
        Iterator<FoodWeaponSourceLedger.AttemptQuote> iterator = completeBatchBinding.batch.entries().iterator();
        for (FoodWeaponSourceLedger.AttemptRequest object : completeBatchBinding.requests) {
            FoodWeaponSourceLedger.AttemptQuote attemptQuote = iterator.next();
            if (object.domain() == attemptQuote.domain() && Double.compare(object.chance(), attemptQuote.chance()) == 0) continue;
            return false;
        }
        Iterator<Object> iterator2 = completeBatchBinding.batch.targets().iterator();
        for (FoodWeaponSourceLedger.TargetRequest targetRequest : completeBatchBinding.targets) {
            FoodWeaponSourceLedger.TargetQuote targetQuote = (FoodWeaponSourceLedger.TargetQuote)iterator2.next();
            if (targetRequest.target().equals(targetQuote.target()) && targetRequest.domain() == targetQuote.domain()) continue;
            return false;
        }
        return true;
    }

    static boolean bindOriginalSequence(PreparedOffense preparedOffense, FoodWeaponOriginalSequence.Session session, Object object, int n, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, float f, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup) {
        if (preparedOffense == null || preparedOffense.publishAttempted || preparedOffense.originalSequence != null || !FoodWeaponFinalDamage.matchesOffensePublishing(preparedOffense, offenseReceipt, publishingCleanup) || !FoodSocketAttributePowers.matchesAttributeReceipt(preparedOffense, f, offenseReceipt)) {
            return false;
        }
        CompleteBatchBinding completeBatchBinding = preparedOffense.binding;
        FoodWeaponConsumerSequence.Accepted accepted = FoodWeaponConsumerSequence.bind(session, object, n, preparedOffense, preparedOffense.quote, offenseReceipt, publishingCleanup, completeBatchBinding.batch, completeBatchBinding.admission, completeBatchBinding.requests, completeBatchBinding.targets, preparedOffense.quote.player, preparedOffense.quote.target, preparedOffense.quote.source, preparedOffense.quote.actualWeapon, preparedOffense.quote.hand, preparedOffense.quote.quotedAt, f, "AttributeProducer");
        if (accepted == null) {
            return false;
        }
        preparedOffense.originalSequence = accepted;
        return true;
    }

    static PreparedOffense prepareOffense(OffensePlan offensePlan, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        if (attemptBatchQuote == null || attemptBatchAdmission == null || list == null || list2 == null || attemptBatchAdmission.status() != FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY || !FoodSocketAttributePowers.matchesOffenseBatch(offensePlan, attemptBatchQuote, attemptBatchAdmission, list, list2)) {
            return null;
        }
        CompleteBatchBinding completeBatchBinding = new CompleteBatchBinding(attemptBatchQuote, attemptBatchAdmission, list, list2);
        return FoodSocketAttributePowers.orderedAttributeBatch(completeBatchBinding) ? new PreparedOffense(offensePlan, completeBatchBinding) : null;
    }

    private static boolean matchesAttributeReceipt(PreparedOffense preparedOffense, float f, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt) {
        if (preparedOffense != null && preparedOffense.originalSequence != null) {
            return preparedOffense.originalSequence.matchesReceipt(preparedOffense, offenseReceipt, f);
        }
        if (preparedOffense == null || offenseReceipt == null || !Float.isFinite(f) || f <= 0.0f) {
            return false;
        }
        CompleteBatchBinding completeBatchBinding = preparedOffense.binding;
        OffensePlan offensePlan = preparedOffense.quote;
        return offenseReceipt.matchesBatch(completeBatchBinding.batch, completeBatchBinding.admission, completeBatchBinding.requests, completeBatchBinding.targets) && offenseReceipt.matches(offensePlan, offensePlan.player, offensePlan.source, offensePlan.quotedAt, f);
    }

    static boolean matchesPreparedOffense(PreparedOffense preparedOffense, OffensePlan offensePlan, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        return preparedOffense != null && offensePlan != null && attemptBatchQuote != null && attemptBatchAdmission != null && list != null && list2 != null && preparedOffense.quote == offensePlan && preparedOffense.binding.batch == attemptBatchQuote && preparedOffense.binding.admission == attemptBatchAdmission && preparedOffense.binding.requests.equals(list) && preparedOffense.binding.targets.equals(list2);
    }

    static boolean matchesOffenseCommit(OffenseCommit offenseCommit, PreparedOffense preparedOffense, OffensePlan offensePlan, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, float f, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        return offenseCommit != null && preparedOffense != null && offenseCommit.prepared == preparedOffense && offensePlan != null && offensePlan.committed && offenseReceipt != null && offenseCommit.receipt == offenseReceipt && Float.isFinite(f) && f > 0.0f && Float.floatToRawIntBits(offenseCommit.amount) == Float.floatToRawIntBits(f) && FoodSocketAttributePowers.matchesPreparedOffense(preparedOffense, offensePlan, attemptBatchQuote, attemptBatchAdmission, list, list2);
    }

    static OffenseCommit commitPreparedOffense(PreparedOffense preparedOffense, float f, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup) {
        FoodWeaponConsumerSequence.Accepted accepted = preparedOffense == null ? null : preparedOffense.originalSequence;
        int n = accepted == null ? 0 : accepted.publicationCallEntered(preparedOffense);
        Throwable throwable = null;
        try {
            if (preparedOffense == null || preparedOffense.publishAttempted || preparedOffense.publishingCleanup != null || !FoodWeaponFinalDamage.matchesOffensePublishing(preparedOffense, offenseReceipt, publishingCleanup)) {
                OffenseCommit offenseCommit = null;
                return offenseCommit;
            }
            preparedOffense.publishingCleanup = publishingCleanup;
            OffenseCommit offenseCommit = FoodSocketAttributePowers.commitPreparedOffense(preparedOffense, f, offenseReceipt);
            return offenseCommit;
        }
        catch (Throwable throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            if (accepted != null) {
                accepted.publicationExited(preparedOffense, n, throwable);
            }
        }
    }

    /*
     * Exception decompiling
     */
    static OffenseCommit commitPreparedOffense(PreparedOffense var0, float var1_1, FoodWeaponFinalDamage.OffenseReceipt var2_2) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [4[TRYBLOCK]], but top level block is 41[SIMPLE_IF_TAKEN]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void finishOffensePublication(PreparedOffense preparedOffense, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup) {
        FoodWeaponConsumerSequence.Accepted accepted = preparedOffense == null ? null : preparedOffense.originalSequence;
        Throwable throwable = null;
        try {
            if (!(FoodSocketAttributePowers.preparedCleanupCaller() && preparedOffense != null && preparedOffense.publishingCleanup == publishingCleanup && (preparedOffense.originalSequence != null && preparedOffense.originalSequence.cleanup(preparedOffense, publishingCleanup) || FoodWeaponFinalDamage.matchesOffensePublicationCleanup(preparedOffense, publishingCleanup)) && preparedOffense.publishAttempted && preparedOffense.publishExited && preparedOffense.publicationReceipt != null)) {
                return;
            }
            CompleteBatchBinding completeBatchBinding = preparedOffense.binding;
            if (!FoodSocketAttributePowers.matchesPreparedOffense(preparedOffense, preparedOffense.quote, completeBatchBinding.batch, completeBatchBinding.admission, completeBatchBinding.requests, completeBatchBinding.targets)) {
                return;
            }
            preparedOffense.finalizingCleanup = publishingCleanup;
            try {
                FoodSocketAttributePowers.retireOffenseEcho(preparedOffense);
            }
            finally {
                if (preparedOffense.originalSequence != null) {
                    preparedOffense.originalSequence.retired(preparedOffense);
                }
            }
        }
        catch (Throwable throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            if (accepted != null) {
                accepted.publicationCleanupExited(preparedOffense, publishingCleanup, throwable);
            }
        }
    }

    private static boolean preparedCleanupCaller() {
        return Thread.holdsLock(FoodWeaponSourceLedger.class) && StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodSocketAttributePowers.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponFinalDamage.class && stackFrame.getMethodName().equals("finalizeFailedPublication") && stackFrame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingCleanup;)V")).orElse(false)) != false;
    }

    private static boolean offenseCleanupPhase(PreparedOffense preparedOffense) {
        if (preparedOffense.originalSequence != null && preparedOffense.originalSequence.cleanup(preparedOffense, preparedOffense.publishingCleanup)) {
            return true;
        }
        FoodWeaponFinalDamage.PublishingCleanup publishingCleanup = preparedOffense.publishingCleanup;
        if (publishingCleanup == null) {
            return true;
        }
        return preparedOffense.finalizingCleanup == null ? FoodWeaponFinalDamage.matchesOffensePublishing(preparedOffense, preparedOffense.publicationReceipt, publishingCleanup) : preparedOffense.finalizingCleanup == publishingCleanup && FoodWeaponFinalDamage.matchesOffensePublicationCleanup(preparedOffense, publishingCleanup);
    }

    private static void retireOffenseEcho(PreparedOffense preparedOffense) {
        if (preparedOffense.echoRetirementStarted || preparedOffense.echoKey == null || preparedOffense.echoValue == null || !preparedOffense.echoOfferEntered || preparedOffense.echoOfferCompleted && !preparedOffense.echoOffered) {
            return;
        }
        preparedOffense.echoRetirementStarted = true;
        try {
            CompleteBatchBinding completeBatchBinding = preparedOffense.binding;
            if (!(FoodSocketAttributePowers.offenseCleanupPhase(preparedOffense) && completeBatchBinding.originalBinding == FoodWeaponSourceLedger.originalInteractionBinding(completeBatchBinding.batch) && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(completeBatchBinding.originalBinding, FoodWeaponSourceLedger.originalInteractionBinding(completeBatchBinding.batch), preparedOffense.quote.player) && FoodSocketAttributePowers.offenseCleanupPhase(preparedOffense))) {
                preparedOffense.echoDisplaced = true;
                return;
            }
            preparedOffense.echoRetired = ECHOES.remove(preparedOffense.echoKey, preparedOffense.echoValue);
            preparedOffense.echoRetirementCompleted = true;
        }
        catch (Error | RuntimeException throwable) {
            preparedOffense.echoRetirementFailure = throwable;
            throw throwable;
        }
    }

    private static OffenseCommit commitOffensePublication(PreparedOffense preparedOffense, float f, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt) {
        return FoodSocketAttributePowers.commitPreparedOffense(preparedOffense, f, offenseReceipt);
    }

    static boolean matchesOriginalPendingEchoOffer(PreparedOffense preparedOffense, OffensePlan offensePlan, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, float f, FoodWeaponEchoQueue.Entry<?> entry2, ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, long l, double d) {
        if (!(preparedOffense != null && preparedOffense.originalSequence != null && preparedOffense.quote == offensePlan && offensePlan != null && preparedOffense.originalSequence.matchesReceipt(preparedOffense, offenseReceipt, f) && preparedOffense.echoOfferCompleted && preparedOffense.echoOffered && !preparedOffense.echoRetirementStarted && preparedOffense.echoKey != null && preparedOffense.echoValue != null && entry2 != null && entry2.key() == preparedOffense.echoKey && entry2.value() == preparedOffense.echoValue && serverPlayer == offensePlan.player && livingEntity == offensePlan.target && damageSource == offensePlan.source && l == preparedOffense.echoValue.dueAt() && Double.doubleToRawLongBits(d) == Double.doubleToRawLongBits(preparedOffense.echoValue.rangeSquared()) && preparedOffense.originalSequence.ordinaryContext())) {
            return false;
        }
        return preparedOffense.originalSequence.attempt(FoodWeaponSourceLedger.AttemptDomain.SERIES_ECHO).filter(attemptResult -> attemptResult.domain() == FoodWeaponSourceLedger.AttemptDomain.SERIES_ECHO && attemptResult.commit() == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS).isPresent() && ECHOES.entries(serverPlayer.m_20148_()).stream().anyMatch(entry -> entry.key() == preparedOffense.echoKey && entry.value() == preparedOffense.echoValue);
    }

    private static boolean publishedAttribute(OffenseCommit offenseCommit) {
        OffensePlan offensePlan = offenseCommit.prepared.quote;
        if (!offenseCommit.cooldownPowers.isEmpty()) {
            if (offensePlan.player.getPersistentData().m_128423_(COOLDOWNS) != offenseCommit.publishedCooldowns) {
                return false;
            }
            for (FoodSocketAttributeRules.Power power : offenseCommit.cooldownPowers) {
                if (Objects.equals(offenseCommit.publishedCooldowns.m_128423_(power.name()), offenseCommit.publishedCooldownState.m_128423_(power.name()))) continue;
                return false;
            }
        }
        if (offenseCommit.publishedProcess != null && (PROCESS.get(offensePlan.player.m_20148_()) != offenseCommit.publishedProcess || offenseCommit.publishedProcess.hits != offenseCommit.publishedHits || offenseCommit.publishedProcess.lastHit != offenseCommit.publishedLastHit)) {
            return false;
        }
        return offenseCommit.echoKey == null || !ECHOES.entries(offensePlan.player.m_20148_()).stream().noneMatch(entry -> entry.key().equals(offenseCommit.echoKey) && entry.value() == offenseCommit.echoValue);
    }

    private static boolean attributeWorldContext(OffenseCommit offenseCommit) {
        if (offenseCommit != null && offenseCommit.prepared.originalSequence != null) {
            return offenseCommit.prepared.originalSequence.ordinaryContext();
        }
        OffensePlan offensePlan = offenseCommit.prepared.quote;
        return offensePlan.committed && EXTRA_HIT.get() == false && offensePlan.player.m_284548_().m_7654_() == offenseCommit.server && offenseCommit.server.m_18695_() && Thread.currentThread() == offenseCommit.thread && FoodSocketAttributePowers.clock(offensePlan.player) == offensePlan.quotedAt && offensePlan.player.m_6084_() && !offensePlan.player.m_5833_() && FoodSocketAttributePowers.target(offensePlan.player, offensePlan.target) && !FoodWeaponBondHits.secondary(offensePlan.source) && offensePlan.player.m_21120_(offensePlan.hand) == offensePlan.actualWeapon && !FoodWeaponBondHits.equipped(offensePlan.player, offensePlan.hit).m_41619_() && FoodWeaponSourceLedger.validReadOnly(offensePlan.hit.snapshot(), offensePlan.player);
    }

    private static boolean attributeLeafLive(OffenseCommit offenseCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (offenseCommit != null && offenseCommit.prepared.originalSequence != null) {
            return offenseCommit.executed && FoodSocketAttributePowers.publishedAttribute(offenseCommit) && offenseCommit.prepared.originalSequence.continuing(offenseCommit, childHurtBridge);
        }
        return offenseCommit != null && offenseCommit.executed && FoodSocketAttributePowers.publishedAttribute(offenseCommit) && FoodSocketAttributePowers.matchesAttributeReceipt(offenseCommit.prepared, offenseCommit.amount, offenseCommit.receipt) && FoodWeaponFinalDamage.matchesAttributeInvocation(offenseCommit, childHurtBridge);
    }

    static void applyOffense(OffenseCommit offenseCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (offenseCommit == null || offenseCommit.executed || Thread.currentThread() != offenseCommit.thread || offenseCommit.server != offenseCommit.prepared.quote.player.m_284548_().m_7654_() || !offenseCommit.server.m_18695_()) {
            return;
        }
        FoodWeaponConsumerSequence.Accepted accepted = offenseCommit.prepared.originalSequence;
        if (accepted != null && !accepted.enter(offenseCommit, childHurtBridge)) {
            return;
        }
        Throwable throwable = null;
        try {
            offenseCommit.executed = true;
            if (!(accepted == null ? FoodSocketAttributePowers.attributeWorldContext(offenseCommit) : accepted.ordinaryContext()) || !FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
                return;
            }
            OffensePlan offensePlan = offenseCommit.prepared.quote;
            ServerPlayer serverPlayer = offensePlan.player;
            List<FoodSocketAttributeRules.Power> list = offenseCommit.powers;
            if (list.contains((Object)FoodSocketAttributeRules.Power.SERIES_ECHO)) {
                if (!FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
                    return;
                }
                FoodSocketAttributePowers.cue(serverPlayer, FoodSocketAttributeRules.Power.SERIES_ECHO);
                if (!FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
                    return;
                }
            }
            if (list.contains((Object)FoodSocketAttributeRules.Power.COLOR_REPEL)) {
                LivingEntity livingEntity = offensePlan.target;
                if (!FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
                    return;
                }
                List list2 = serverPlayer.m_284548_().m_6443_(Monster.class, livingEntity.m_20191_().m_82400_(3.0), monster -> FoodSocketAttributePowers.target(serverPlayer, (LivingEntity)monster) && monster.m_20280_((Entity)livingEntity) <= 9.0);
                list2.sort(Comparator.comparingDouble(monster -> monster.m_20280_((Entity)livingEntity)));
                if (!FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
                    return;
                }
                for (Monster monster2 : list2.stream().limit(4L).toList()) {
                    if (!FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
                        return;
                    }
                    monster2.m_147240_(0.6, livingEntity.m_20185_() - monster2.m_20185_(), livingEntity.m_20189_() - monster2.m_20189_());
                    if (FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) continue;
                    return;
                }
            }
            if (offenseCommit.bursts) {
                if (!FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
                    return;
                }
                FoodSocketAttributePowers.burst(offenseCommit, serverPlayer, offensePlan.target, childHurtBridge);
                if (!FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
                    return;
                }
                FoodSocketAttributePowers.cue(serverPlayer, FoodSocketAttributeRules.Power.PROCESS_BURST);
                if (!FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
                    return;
                }
            }
        }
        catch (Error | RuntimeException throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            if (accepted != null) {
                accepted.finish(offenseCommit, childHurtBridge, throwable);
            }
        }
    }

    static boolean matchesPendingEchoDispatch(PendingEchoDispatch pendingEchoDispatch, ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, float f) {
        return pendingEchoDispatch != null && pendingEchoDispatch.active && !pendingEchoDispatch.hurtEntered && pendingEchoDispatch.player == serverPlayer && pendingEchoDispatch.target == livingEntity && pendingEchoDispatch.damageSource == damageSource && Float.isFinite(f) && f > 0.0f && Float.floatToRawIntBits(pendingEchoDispatch.amount) == Float.floatToRawIntBits(f) && pendingEchoDispatch.entry.key() == pendingEchoDispatch.key && pendingEchoDispatch.entry.value() == pendingEchoDispatch.pending && pendingEchoDispatch.key.owner().equals(serverPlayer.m_20148_()) && pendingEchoDispatch.pending.target().equals(livingEntity.m_20148_()) && pendingEchoDispatch.pending.dueAt() == pendingEchoDispatch.dueAt && Double.doubleToRawLongBits(pendingEchoDispatch.pending.rangeSquared()) == Double.doubleToRawLongBits(pendingEchoDispatch.rangeSquared) && pendingEchoDispatch.paidSource.equals((Object)pendingEchoDispatch.pending.source()) && Thread.currentThread() == pendingEchoDispatch.thread && pendingEchoDispatch.server == serverPlayer.m_284548_().m_7654_() && pendingEchoDispatch.server.m_18695_() && FoodSocketAttributePowers.clock(serverPlayer) == pendingEchoDispatch.dispatchedAt;
    }

    static CompoundTag pendingEchoDispatchSource(PendingEchoDispatch pendingEchoDispatch) {
        return pendingEchoDispatch != null && pendingEchoDispatch.active ? pendingEchoDispatch.paidSource.m_6426_() : null;
    }

    static ItemStack originalPendingEchoWeapon(PendingEchoDispatch pendingEchoDispatch) {
        if (pendingEchoDispatch == null || pendingEchoDispatch.originalWeapon == null || pendingEchoDispatch.originalWeapon != pendingEchoDispatch.pending.originalWeapon || !FoodSocketAttributePowers.originalPendingEchoWeaponCaller() || !FoodSocketAttributePowers.matchesPendingEchoDispatch(pendingEchoDispatch, pendingEchoDispatch.player, pendingEchoDispatch.target, pendingEchoDispatch.damageSource, pendingEchoDispatch.amount)) {
            return null;
        }
        return pendingEchoDispatch.originalWeapon;
    }

    private static boolean originalPendingEchoWeaponCaller() {
        return Thread.holdsLock(FoodWeaponSourceLedger.class) && StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
            Iterator iterator = stream.dropWhile(stackFrame -> stackFrame.getDeclaringClass() == FoodSocketAttributePowers.class).iterator();
            if (!iterator.hasNext()) {
                return false;
            }
            StackWalker.StackFrame stackFrame2 = (StackWalker.StackFrame)iterator.next();
            if (stackFrame2.getDeclaringClass() != FoodWeaponSourceLedger.class || !stackFrame2.getMethodName().equals("beginPendingEchoInteraction") || !stackFrame2.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodSocketAttributePowers$PendingEchoDispatch;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$PendingEchoInteraction;")) {
                return false;
            }
            while (iterator.hasNext()) {
                StackWalker.StackFrame stackFrame3 = (StackWalker.StackFrame)iterator.next();
                if (stackFrame3.getDeclaringClass() == FoodWeaponSourceLedger.class) continue;
                return stackFrame3.getDeclaringClass() == FoodSocketAttributePowers.class && stackFrame3.getMethodName().equals("damagePendingEcho") && stackFrame3.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponEchoQueue$Entry;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;J)V");
            }
            return false;
        }) != false;
    }

    private static boolean queuedPendingEcho(FoodWeaponEchoQueue.Entry<PendingEcho> entry) {
        return ECHOES.entries(entry.key().owner()).stream().anyMatch(entry2 -> entry2.key() == entry.key() && entry2.value() == entry.value());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void damagePendingEcho(FoodWeaponEchoQueue.Entry<PendingEcho> entry, CompoundTag compoundTag, ServerPlayer serverPlayer, LivingEntity livingEntity, long l) {
        FoodWeaponSourceLedger.PendingEchoInteraction pendingEchoInteraction;
        block25: {
            PendingEchoDispatch pendingEchoDispatch;
            DamageSource damageSource;
            block23: {
                block21: {
                    if (!Float.isFinite(2.0f) || !FoodSocketAttributePowers.target(serverPlayer, livingEntity)) {
                        FoodSocketAttributePowers.pendingEchoParticles(serverPlayer, livingEntity);
                        return;
                    }
                    damageSource = FoodWeaponDamageSources.attributeSecondary(serverPlayer);
                    if (damageSource == null) {
                        FoodSocketAttributePowers.pendingEchoParticles(serverPlayer, livingEntity);
                        return;
                    }
                    pendingEchoDispatch = new PendingEchoDispatch(entry, compoundTag, serverPlayer, livingEntity, damageSource, 2.0f, l);
                    pendingEchoInteraction = null;
                    try {
                        pendingEchoInteraction = FoodWeaponSourceLedger.beginDurablePendingEchoInteraction(entry.value().lineage, entry, pendingEchoDispatch, serverPlayer, livingEntity, damageSource, 2.0f);
                        if (pendingEchoInteraction != null) break block21;
                    }
                    catch (Throwable throwable) {
                        try {
                            FoodWeaponSourceLedger.closePendingEchoInteraction(pendingEchoInteraction);
                        }
                        finally {
                            pendingEchoDispatch.active = false;
                        }
                        throw throwable;
                    }
                    try {
                        FoodWeaponSourceLedger.closePendingEchoInteraction(pendingEchoInteraction);
                    }
                    finally {
                        pendingEchoDispatch.active = false;
                    }
                    return;
                }
                FoodSocketAttributePowers.hurtPendingEcho(pendingEchoDispatch, pendingEchoInteraction);
                if (FoodWeaponSourceLedger.matchesPendingEchoInteraction(pendingEchoInteraction, pendingEchoDispatch, serverPlayer, livingEntity, damageSource, 2.0f)) break block23;
                try {
                    FoodWeaponSourceLedger.closePendingEchoInteraction(pendingEchoInteraction);
                }
                finally {
                    pendingEchoDispatch.active = false;
                }
                return;
            }
            FoodSocketAttributePowers.pendingEchoParticles(serverPlayer, livingEntity);
            if (FoodWeaponSourceLedger.matchesPendingEchoInteraction(pendingEchoInteraction, pendingEchoDispatch, serverPlayer, livingEntity, damageSource, 2.0f)) break block25;
            try {
                FoodWeaponSourceLedger.closePendingEchoInteraction(pendingEchoInteraction);
            }
            finally {
                pendingEchoDispatch.active = false;
            }
            return;
        }
        try {
            FoodWeaponSourceLedger.closePendingEchoInteraction(pendingEchoInteraction);
        }
        finally {
            pendingEchoDispatch.active = false;
        }
    }

    private static void hurtPendingEcho(PendingEchoDispatch pendingEchoDispatch, FoodWeaponSourceLedger.PendingEchoInteraction pendingEchoInteraction) {
        if (pendingEchoDispatch == null || pendingEchoInteraction == null || pendingEchoDispatch.hurtEntered || !FoodSocketAttributePowers.matchesPendingEchoDispatch(pendingEchoDispatch, pendingEchoDispatch.player, pendingEchoDispatch.target, pendingEchoDispatch.damageSource, pendingEchoDispatch.amount)) {
            return;
        }
        boolean bl = EXTRA_HIT.get();
        EXTRA_HIT.set(true);
        try {
            FoodWeaponDamageSources.withSecondary(() -> FoodSocketAttributePowers.hurtPendingEchoRaw(pendingEchoDispatch, pendingEchoInteraction));
        }
        finally {
            EXTRA_HIT.set(bl);
        }
    }

    private static boolean hurtPendingEchoRaw(PendingEchoDispatch pendingEchoDispatch, FoodWeaponSourceLedger.PendingEchoInteraction pendingEchoInteraction) {
        if (pendingEchoDispatch == null || pendingEchoInteraction == null || pendingEchoDispatch.hurtEntered || !FoodSocketAttributePowers.matchesPendingEchoDispatch(pendingEchoDispatch, pendingEchoDispatch.player, pendingEchoDispatch.target, pendingEchoDispatch.damageSource, pendingEchoDispatch.amount)) {
            return false;
        }
        pendingEchoDispatch.hurtEntered = true;
        return FoodWeaponSourceLedger.hurtPendingEchoInteraction(pendingEchoInteraction, pendingEchoDispatch, pendingEchoDispatch.player, pendingEchoDispatch.target, pendingEchoDispatch.damageSource, pendingEchoDispatch.amount);
    }

    private static void pendingEchoParticles(ServerPlayer serverPlayer, LivingEntity livingEntity) {
        serverPlayer.m_284548_().m_8767_((ParticleOptions)ParticleTypes.f_123808_, livingEntity.m_20185_(), livingEntity.m_20186_() + 0.5, livingEntity.m_20189_(), 6, 0.3, 0.3, 0.3, 0.01);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void damage(ServerPlayer serverPlayer, LivingEntity livingEntity, float f) {
        if (!Float.isFinite(f) || f <= 0.0f || !FoodSocketAttributePowers.target(serverPlayer, livingEntity)) {
            return;
        }
        DamageSource damageSource = FoodWeaponDamageSources.attributeSecondary(serverPlayer);
        if (damageSource == null) {
            return;
        }
        boolean bl = EXTRA_HIT.get();
        EXTRA_HIT.set(true);
        try {
            FoodWeaponDamageSources.withSecondary(() -> livingEntity.m_6469_(damageSource, f));
        }
        finally {
            EXTRA_HIT.set(bl);
        }
    }

    private static void burst(OffenseCommit offenseCommit, ServerPlayer serverPlayer, LivingEntity livingEntity, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (!FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
            return;
        }
        List list = serverPlayer.m_284548_().m_6443_(Monster.class, livingEntity.m_20191_().m_82400_(3.0), monster -> monster != livingEntity && FoodSocketAttributePowers.target(serverPlayer, (LivingEntity)monster) && monster.m_20280_((Entity)livingEntity) <= 9.0);
        list.sort(Comparator.comparingDouble(monster -> monster.m_20280_((Entity)livingEntity)));
        if (!FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
            return;
        }
        for (Monster monster2 : list.stream().limit(4L).toList()) {
            if (!FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
                return;
            }
            FoodSocketAttributePowers.damage(offenseCommit, serverPlayer, (LivingEntity)monster2, 4.0f, childHurtBridge);
            if (FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) continue;
            return;
        }
        if (!FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
            return;
        }
        serverPlayer.m_284548_().m_8767_((ParticleOptions)ParticleTypes.f_123797_, livingEntity.m_20185_(), livingEntity.m_20186_() + 0.5, livingEntity.m_20189_(), 16, 0.8, 0.3, 0.8, 0.01);
        if (!FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
            return;
        }
    }

    static FoodWeaponConsumerSequence.OriginalChild originalAttributeChild(OffenseCommit offenseCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge, LivingEntity livingEntity, DamageSource damageSource, float f) {
        if (offenseCommit == null || offenseCommit.prepared.originalSequence == null) {
            return null;
        }
        FoodWeaponConsumerSequence.OriginalChild originalChild = offenseCommit.originalChild;
        return offenseCommit.prepared.originalSequence.matchesChild(originalChild, offenseCommit, childHurtBridge, livingEntity, damageSource, f) ? originalChild : null;
    }

    private static boolean hurtOriginalAttributeChild(OffenseCommit offenseCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge, FoodWeaponConsumerSequence.OriginalChild originalChild, LivingEntity livingEntity, DamageSource damageSource, float f) {
        if (originalChild != null && !originalChild.entered()) {
            return false;
        }
        try {
            boolean bl = FoodWeaponFinalDamage.hurtListedChild(childHurtBridge, livingEntity, damageSource, f);
            if (originalChild != null) {
                originalChild.returned(bl);
            }
            return bl;
        }
        catch (Error | RuntimeException throwable) {
            if (originalChild != null) {
                originalChild.originalThrown(throwable);
            }
            throw throwable;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void damage(OffenseCommit offenseCommit, ServerPlayer serverPlayer, LivingEntity livingEntity, float f, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        FoodWeaponConsumerSequence.OriginalChild originalChild;
        if (!FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge) || !Float.isFinite(f) || f <= 0.0f || !FoodSocketAttributePowers.target(serverPlayer, livingEntity)) {
            return;
        }
        DamageSource damageSource = FoodWeaponDamageSources.attributeSecondary(serverPlayer);
        if (damageSource == null || !FoodSocketAttributePowers.attributeLeafLive(offenseCommit, childHurtBridge)) {
            return;
        }
        FoodWeaponConsumerSequence.Accepted accepted = offenseCommit.prepared.originalSequence;
        FoodWeaponConsumerSequence.OriginalChild originalChild2 = originalChild = accepted == null ? null : accepted.beginChild(offenseCommit, childHurtBridge, livingEntity, damageSource, f);
        if (accepted != null && originalChild == null) {
            return;
        }
        offenseCommit.originalChild = originalChild;
        boolean bl = EXTRA_HIT.get();
        EXTRA_HIT.set(true);
        try {
            FoodWeaponDamageSources.withSecondary(() -> FoodSocketAttributePowers.hurtOriginalAttributeChild(offenseCommit, childHurtBridge, originalChild, livingEntity, damageSource, f));
        }
        finally {
            try {
                EXTRA_HIT.set(bl);
            }
            finally {
                if (accepted != null) {
                    accepted.closeChild(originalChild);
                }
                if (offenseCommit.originalChild == originalChild) {
                    offenseCommit.originalChild = null;
                }
            }
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent playerTickEvent) {
        Player player;
        if (playerTickEvent.phase != TickEvent.Phase.END || !((player = playerTickEvent.player) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        long l = FoodSocketAttributePowers.clock(serverPlayer);
        for (FoodWeaponEchoQueue.Entry<PendingEcho> entry : ECHOES.entries(serverPlayer.m_20148_())) {
            LivingEntity livingEntity;
            Entity entity;
            PendingEcho pendingEcho = entry.value();
            CompoundTag compoundTag = pendingEcho.source();
            if (!serverPlayer.m_6084_() || serverPlayer.m_5833_() || !FoodWeaponSourceLedger.pendingEchoSourceContinuous(pendingEcho.lineage, entry, compoundTag, serverPlayer, pendingEcho.target(), pendingEcho.dueAt(), pendingEcho.rangeSquared()) || serverPlayer.m_21120_(InteractionHand.valueOf((String)compoundTag.m_128461_("OriginalHand"))).m_41613_() != 1 || pendingEcho.dueAt() > FoodSocketAttributeRules.deadline(l, 12)) {
                ECHOES.remove(entry.key(), pendingEcho);
                continue;
            }
            if (l < pendingEcho.dueAt() || !FoodSocketAttributePowers.queuedPendingEcho(entry) || !ECHOES.remove(entry.key(), pendingEcho) || l - pendingEcho.dueAt() > 20L || !((entity = serverPlayer.m_284548_().m_8791_(pendingEcho.target())) instanceof LivingEntity) || !FoodSocketAttributePowers.target(serverPlayer, livingEntity = (LivingEntity)entity) || !(livingEntity.m_20280_((Entity)serverPlayer) <= pendingEcho.rangeSquared())) continue;
            FoodSocketAttributePowers.damagePendingEcho(entry, compoundTag, serverPlayer, livingEntity, l);
        }
    }

    private static void clear(Player player) {
        ECHOES.clear(player.m_20148_());
        GUARDS.remove(player.m_20148_());
        PROCESS.remove(player.m_20148_());
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent) {
        FoodSocketAttributePowers.clear(playerLoggedInEvent.getEntity());
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent playerLoggedOutEvent) {
        FoodSocketAttributePowers.clear(playerLoggedOutEvent.getEntity());
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent playerChangedDimensionEvent) {
        FoodSocketAttributePowers.clear(playerChangedDimensionEvent.getEntity());
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void death(LivingDeathEvent livingDeathEvent) {
        LivingEntity livingEntity = livingDeathEvent.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            FoodSocketAttributePowers.clear((Player)serverPlayer);
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone clone) {
        if (clone.getOriginal().getPersistentData().m_128441_(COOLDOWNS)) {
            clone.getEntity().getPersistentData().m_128365_(COOLDOWNS, clone.getOriginal().getPersistentData().m_128423_(COOLDOWNS).m_6426_());
        }
        FoodSocketAttributePowers.clear(clone.getOriginal());
        FoodSocketAttributePowers.clear(clone.getEntity());
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent serverStoppedEvent) {
        ECHOES.clear();
        GUARDS.clear();
        PROCESS.clear();
        EXTRA_HIT.remove();
    }

    public static List<FoodWeaponEvolution.WeaponDetailView> details(ItemStack itemStack) {
        FoodSocketAttributeRules.Resolution resolution = FoodSocketAttributePowers.inspect(itemStack);
        ArrayList<FoodWeaponEvolution.WeaponDetailView> arrayList = new ArrayList<FoodWeaponEvolution.WeaponDetailView>();
        for (FoodSocketAttributeRules.Power power : FoodSocketAttributeRules.Power.values()) {
            if (!resolution.has(power)) continue;
            arrayList.add(new FoodWeaponEvolution.WeaponDetailView((Component)Component.m_237115_((String)"tooltip.ftboss.socket_attribute.category"), FoodSocketAttributePowers.title(power, resolution), FoodSocketAttributePowers.description(power, resolution)));
        }
        return List.copyOf(arrayList);
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent itemTooltipEvent) {
        if (!FoodWeaponEvolution.isEvolutionCandidate(itemTooltipEvent.getItemStack())) {
            return;
        }
        FoodSocketAttributeRules.Resolution resolution = FoodSocketAttributePowers.inspect(itemTooltipEvent.getItemStack());
        for (FoodSocketAttributeRules.Power power : FoodSocketAttributeRules.Power.values()) {
            if (!resolution.has(power)) continue;
            int n = switch (power) {
                default -> throw new IncompatibleClassChangeError();
                case FoodSocketAttributeRules.Power.PRICE_GUARD -> resolution.priceProgress();
                case FoodSocketAttributeRules.Power.SERIES_ECHO -> resolution.seriesProgress();
                case FoodSocketAttributeRules.Power.COLOR_REPEL -> resolution.colorProgress();
                case FoodSocketAttributeRules.Power.PROCESS_BURST -> resolution.processProgress();
            };
            itemTooltipEvent.getToolTip().add(Component.m_237110_((String)"tooltip.ftboss.socket_attribute.progress", (Object[])new Object[]{FoodSocketAttributePowers.title(power, resolution), Math.min(3, n)}));
        }
    }

    private static final class CombatState {
        private final CompoundTag source;
        private final UUID weaponId;
        private final FoodSocketAttributeRules.Resolution recipe;
        private float guard;
        private long guardUntil;

        private CombatState(ServerPlayer serverPlayer, ItemStack itemStack, FoodSocketAttributeRules.Resolution resolution, CompoundTag compoundTag) {
            this.source = compoundTag.m_6426_();
            FoodWeaponLifecycle.ensureTracked(itemStack);
            this.weaponId = FoodWeaponLifecycle.weaponId(itemStack);
            this.recipe = resolution;
        }
    }

    public record DefenseQuote(float amount, DefensePlan plan, DefenseRejection rejection) {
        public boolean shieldAbsorbed() {
            return this.plan != null && this.plan.amount() == 0.0f && this.plan.blocked() == this.plan.input();
        }
    }

    public static enum DefenseRejection {
        NONE,
        INVALID_INPUT,
        INVALID_DEFENDER,
        SECONDARY_OR_BYPASS,
        NO_ELIGIBLE_WEAPON,
        UNTRACKED_WEAPON,
        INVALID_MATERIALS,
        NO_GUARD_AVAILABLE;

    }

    public static final class DefensePlan {
        private final ServerPlayer player;
        private final DamageSource source;
        private final InteractionHand hand;
        private final ItemStack weapon;
        private final ItemStack actualWeapon;
        private final MinecraftServer server;
        private final CompoundTag fingerprint;
        private final String materials;
        private final ResourceKey<Level> dimension;
        private final CombatState previous;
        private final float previousGuard;
        private final long previousUntil;
        private final long at;
        private final float input;
        private final float amount;
        private final float blocked;
        private final float balance;
        private final long until;
        private final boolean starts;
        private boolean committed;

        private DefensePlan(ServerPlayer serverPlayer, DamageSource damageSource, InteractionHand interactionHand, ItemStack itemStack, CombatState combatState, long l, float f, float f2, long l2, boolean bl) {
            this.player = serverPlayer;
            this.source = damageSource;
            this.hand = interactionHand;
            this.weapon = itemStack.m_41777_();
            this.actualWeapon = itemStack;
            this.server = serverPlayer.m_284548_().m_7654_();
            this.fingerprint = FoodWeaponBondHits.fingerprint(itemStack);
            this.materials = FoodWeaponSourceLedger.materialFingerprint(itemStack);
            this.dimension = serverPlayer.m_9236_().m_46472_();
            this.previous = combatState;
            this.previousGuard = combatState == null ? 0.0f : combatState.guard;
            this.previousUntil = combatState == null ? 0L : combatState.guardUntil;
            this.at = l;
            this.input = f;
            this.blocked = FoodSocketAttributeRules.absorb(f, f2);
            this.amount = f - this.blocked;
            this.balance = f2 - this.blocked;
            this.until = l2;
            this.starts = bl;
        }

        public float input() {
            return this.input;
        }

        public float amount() {
            return this.amount;
        }

        public float blocked() {
            return this.blocked;
        }

        public ItemStack actualWeapon() {
            return this.actualWeapon;
        }

        public InteractionHand originalHand() {
            return this.hand;
        }

        public long quotedAt() {
            return this.at;
        }

        public boolean matchesContext(ServerPlayer serverPlayer, DamageSource damageSource, ItemStack itemStack, InteractionHand interactionHand, long l) {
            if (serverPlayer != this.player || damageSource != this.source || itemStack != this.actualWeapon || interactionHand != this.hand || l != this.at || this.committed) {
                return false;
            }
            return this.player.m_6084_() && !this.player.m_5833_() && this.player.m_284548_().m_7654_() == this.server && FoodSocketAttributePowers.clock(this.player) == this.at && this.player.m_9236_().m_46472_().equals(this.dimension) && this.player.m_21120_(this.hand) == this.actualWeapon && !this.actualWeapon.m_41619_() && this.actualWeapon.m_41613_() == 1 && this.actualWeapon.m_41720_() == this.weapon.m_41720_() && Objects.equals(FoodWeaponLifecycle.weaponId(this.actualWeapon), FoodWeaponLifecycle.weaponId(this.weapon)) && !FoodWeaponLifecycle.isBroken(this.actualWeapon) && FoodWeaponBondHits.fingerprint(this.actualWeapon).equals((Object)this.fingerprint) && FoodWeaponSourceLedger.materialFingerprint(this.actualWeapon).equals(this.materials);
        }
    }

    public static final class OffensePlan {
        private final ServerPlayer player;
        private final LivingEntity target;
        private final DamageSource source;
        private final FoodWeaponBondHits.Hit hit;
        private final FoodSocketAttributeRules.Resolution recipe;
        private final float amount;
        private final long quotedAt;
        private final List<FoodWeaponSourceLedger.AttemptRequest> attempts;
        private final ProcessState process;
        private final Tag cooldowns;
        private final List<FoodWeaponEchoQueue.Entry<PendingEcho>> echoes;
        private final ItemStack actualWeapon;
        private final InteractionHand hand;
        private final int processHits;
        private final long processLastHit;
        private boolean committed;

        private OffensePlan(ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, FoodWeaponBondHits.Hit hit, FoodSocketAttributeRules.Resolution resolution, float f, long l, List<FoodWeaponSourceLedger.AttemptRequest> list) {
            this.player = serverPlayer;
            this.target = livingEntity;
            this.source = damageSource;
            this.hit = hit;
            this.recipe = resolution;
            this.amount = f;
            this.quotedAt = l;
            this.attempts = List.copyOf(list);
            this.hand = InteractionHand.valueOf((String)hit.snapshot().m_128461_("OriginalHand"));
            this.actualWeapon = serverPlayer.m_21120_(this.hand);
            Tag tag = serverPlayer.getPersistentData().m_128423_(FoodSocketAttributePowers.COOLDOWNS);
            this.cooldowns = tag == null ? null : tag.m_6426_();
            this.echoes = ECHOES.entries(serverPlayer.m_20148_());
            this.process = PROCESS.get(serverPlayer.m_20148_());
            this.processHits = this.process == null ? 0 : this.process.hits;
            this.processLastHit = this.process == null ? 0L : this.process.lastHit;
        }

        public float amount() {
            return this.amount;
        }

        public long quotedAt() {
            return this.quotedAt;
        }

        public ItemStack actualWeapon() {
            return this.actualWeapon;
        }

        public InteractionHand originalHand() {
            return this.hand;
        }

        public UUID attackId() {
            return this.hit.attack();
        }

        public UUID weaponId() {
            return this.hit.snapshot().m_128342_("WeaponId");
        }

        public CompoundTag snapshot() {
            return this.hit.snapshot();
        }

        public List<FoodWeaponSourceLedger.AttemptRequest> attempts() {
            return this.attempts;
        }
    }

    private static final class ProcessState {
        private final UUID weaponId;
        private final CompoundTag fingerprint;
        private final ResourceKey<Level> dimension;
        private int hits;
        private long lastHit;

        private ProcessState(ServerPlayer serverPlayer, FoodWeaponBondHits.Hit hit) {
            this.weaponId = hit.snapshot().m_128342_("WeaponId");
            this.fingerprint = FoodWeaponBondHits.fingerprint(hit.weapon());
            this.dimension = serverPlayer.m_9236_().m_46472_();
        }
    }

    private static final class CompleteBatchBinding {
        private final FoodWeaponSourceLedger.AttemptBatchQuote batch;
        private final FoodWeaponSourceLedger.AttemptBatchAdmission admission;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final List<FoodWeaponSourceLedger.AttemptRequest> requests;
        private final List<FoodWeaponSourceLedger.TargetRequest> targets;

        private CompleteBatchBinding(FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
            this.batch = attemptBatchQuote;
            this.admission = attemptBatchAdmission;
            this.originalBinding = FoodWeaponSourceLedger.originalInteractionBinding(attemptBatchQuote);
            this.requests = List.copyOf(list);
            this.targets = List.copyOf(list2);
        }
    }

    static final class PreparedOffense {
        private final OffensePlan quote;
        private final CompleteBatchBinding binding;
        private FoodWeaponFinalDamage.PublishingCleanup publishingCleanup;
        private FoodWeaponFinalDamage.PublishingCleanup finalizingCleanup;
        private FoodWeaponConsumerSequence.Accepted originalSequence;
        private FoodWeaponFinalDamage.OffenseReceipt publicationReceipt;
        private int publicationAmountBits;
        private boolean publishAttempted;
        private boolean publishExited;
        private boolean commitConstructed;
        private FoodWeaponEchoQueue.Key echoKey;
        private PendingEcho echoValue;
        private boolean echoOfferEntered;
        private boolean echoOfferCompleted;
        private boolean echoOffered;
        private boolean echoRetirementStarted;
        private boolean echoRetirementCompleted;
        private boolean echoRetired;
        private boolean echoDisplaced;
        private Throwable publishFailure;
        private Throwable echoRetirementFailure;

        private PreparedOffense(OffensePlan offensePlan, CompleteBatchBinding completeBatchBinding) {
            this.quote = offensePlan;
            this.binding = completeBatchBinding;
        }
    }

    static final class OffenseCommit {
        private final PreparedOffense prepared;
        private final FoodWeaponFinalDamage.OffenseReceipt receipt;
        private final float amount;
        private final List<FoodSocketAttributeRules.Power> powers;
        private final List<FoodSocketAttributeRules.Power> cooldownPowers;
        private final boolean bursts;
        private final Thread thread;
        private final MinecraftServer server;
        private final CompoundTag publishedCooldowns;
        private final CompoundTag publishedCooldownState;
        private final ProcessState publishedProcess;
        private final int publishedHits;
        private final long publishedLastHit;
        private final FoodWeaponEchoQueue.Key echoKey;
        private final PendingEcho echoValue;
        private boolean executed;
        private FoodWeaponConsumerSequence.OriginalChild originalChild;

        private OffenseCommit(PreparedOffense preparedOffense, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, float f, List<FoodSocketAttributeRules.Power> list, List<FoodSocketAttributeRules.Power> list2, boolean bl, CompoundTag compoundTag, ProcessState processState, FoodWeaponEchoQueue.Key key, PendingEcho pendingEcho) {
            this.prepared = preparedOffense;
            this.receipt = offenseReceipt;
            this.amount = f;
            this.powers = List.copyOf(list);
            this.cooldownPowers = List.copyOf(list2);
            this.bursts = bl;
            this.thread = Thread.currentThread();
            this.server = preparedOffense.quote.player.m_284548_().m_7654_();
            this.publishedCooldowns = compoundTag;
            this.publishedCooldownState = compoundTag == null ? null : compoundTag.m_6426_();
            this.publishedProcess = processState;
            this.publishedHits = processState == null ? 0 : processState.hits;
            this.publishedLastHit = processState == null ? 0L : processState.lastHit;
            this.echoKey = key;
            this.echoValue = pendingEcho;
        }
    }

    private static final class PendingEcho {
        private final CompoundTag source;
        private final ItemStack originalWeapon;
        private final UUID target;
        private final long dueAt;
        private final double rangeSquared;
        private FoodWeaponSourceLedger.PendingEchoLineage lineage;

        private PendingEcho(CompoundTag compoundTag, UUID uUID, long l, double d, ItemStack itemStack) {
            this.source = compoundTag.m_6426_();
            this.target = uUID;
            this.dueAt = l;
            this.rangeSquared = d;
            this.originalWeapon = itemStack;
        }

        public CompoundTag source() {
            return this.source.m_6426_();
        }

        public UUID target() {
            return this.target;
        }

        public long dueAt() {
            return this.dueAt;
        }

        public double rangeSquared() {
            return this.rangeSquared;
        }
    }

    static final class PendingEchoDispatch {
        private final FoodWeaponEchoQueue.Entry<PendingEcho> entry;
        private final FoodWeaponEchoQueue.Key key;
        private final PendingEcho pending;
        private final ItemStack originalWeapon;
        private final CompoundTag paidSource;
        private final ServerPlayer player;
        private final LivingEntity target;
        private final DamageSource damageSource;
        private final float amount;
        private final long dueAt;
        private final double rangeSquared;
        private final long dispatchedAt;
        private final Thread thread;
        private final MinecraftServer server;
        private boolean active;
        private boolean hurtEntered;

        private PendingEchoDispatch(FoodWeaponEchoQueue.Entry<PendingEcho> entry, CompoundTag compoundTag, ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, float f, long l) {
            this.entry = entry;
            this.key = entry.key();
            this.pending = entry.value();
            this.originalWeapon = this.pending.originalWeapon;
            this.paidSource = compoundTag.m_6426_();
            this.player = serverPlayer;
            this.target = livingEntity;
            this.damageSource = damageSource;
            this.amount = f;
            this.dueAt = this.pending.dueAt();
            this.rangeSquared = this.pending.rangeSquared();
            this.dispatchedAt = l;
            this.thread = Thread.currentThread();
            this.server = serverPlayer.m_284548_().m_7654_();
            this.active = true;
        }
    }

    public static enum DefenseAcceptance {
        REJECTED,
        POSITIVE_FINAL_DAMAGE,
        SHIELD_ONLY_ZERO;

    }
}

