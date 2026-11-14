package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
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
import net.farmingtales.ftboss.item.FoodWeaponEchoQueue.Entry;
import net.farmingtales.ftboss.item.FoodWeaponEchoQueue.Key;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.SocketView;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules;
import net.farmingtales.ftboss.util.FoodWeaponBondRules;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules.Ingredient;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules.Power;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules.Resolution;
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
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.GAME
)
public final class FoodSocketAttributePowers {
    private static final String COOLDOWNS = "FTBossFoodSocketAttributeCooldowns";
    private static final float ECHO_DAMAGE = 2.0F;
    private static final float BURST_DAMAGE = 4.0F;
    private static final double SPLASH_RADIUS = 3.0;
    private static final double REPEL_STRENGTH = 0.6;
    private static final int DELAY_GRACE_TICKS = 20;
    private static final FoodWeaponEchoQueue<FoodSocketAttributePowers.PendingEcho> ECHOES = new FoodWeaponEchoQueue<>();
    private static final Map<UUID, FoodSocketAttributePowers.CombatState> GUARDS = new HashMap<>();
    private static final Map<UUID, FoodSocketAttributePowers.ProcessState> PROCESS = new HashMap<>();
    private static final ThreadLocal<Boolean> EXTRA_HIT = ThreadLocal.withInitial(() -> false);

    private FoodSocketAttributePowers() {
    }

    public static Resolution inspect(ItemStack var0) {
        ArrayList var1 = new ArrayList();
        if (var0 != null && !var0.isEmpty() && FoodWeaponEvolution.isEvolutionCandidate(var0)) {
            for (SocketView var3 : FoodWeaponEvolution.socketViews(var0)) {
                Ingredient var4 = FoodSocketAttributeCatalog.ingredient(var3.stack());
                if (var4 != null) {
                    var1.add(var4);
                }
            }
        }

        return FoodSocketAttributeRules.resolve(var1);
    }

    private static String id(Power var0) {
        return var0.name().toLowerCase(Locale.ROOT);
    }

    private static Component title(Power var0, Resolution var1) {
        return Component.translatable(
            "tooltip.ftboss.socket_attribute." + id(var0) + ".title",
            Component.translatable(var1.has(var0) ? "tooltip.ftboss.socket_attribute.active" : "tooltip.ftboss.socket_attribute.missing")
        );
    }

    private static Component description(Power var0, Resolution var1) {
        String var2 = "tooltip.ftboss.socket_attribute." + id(var0) + ".description";

        return switch (var0) {
            case PRICE_GUARD -> Component.translatable(var2, var1.priceProgress(), 3, var1.guardHealth());
            case SERIES_ECHO -> Component.translatable(var2, var1.seriesProgress(), var1.series().isEmpty() ? "—" : var1.series());
            case COLOR_REPEL -> Component.translatable(var2, var1.colorProgress());
            case PROCESS_BURST -> Component.translatable(var2, var1.processProgress());
        };
    }

    private static long clock(ServerPlayer var0) {
        return var0.serverLevel().getServer().overworld().getGameTime();
    }

    private static int cooldown(Power var0) {
        return switch (var0) {
            case PRICE_GUARD -> 600;
            case SERIES_ECHO, PROCESS_BURST -> 400;
            case COLOR_REPEL -> 400;
        };
    }

    private static boolean ready(ServerPlayer var0, Power var1, long var2) {
        if (var0.getPersistentData().contains("FTBossFoodSocketAttributeCooldowns")
            && !var0.getPersistentData().contains("FTBossFoodSocketAttributeCooldowns", 10)) {
            CompoundTag var10 = new CompoundTag();

            for (Power var13 : Power.values()) {
                var10.putLong(var13.name(), FoodSocketAttributeRules.deadline(var2, cooldown(var13)));
            }

            var0.getPersistentData().put("FTBossFoodSocketAttributeCooldowns", var10);
            return false;
        } else {
            CompoundTag var4 = var0.getPersistentData().getCompound("FTBossFoodSocketAttributeCooldowns");
            String var5 = var1.name();
            if (!var4.contains(var5)) {
                return true;
            } else {
                long var6 = var4.contains(var5, 4) ? var4.getLong(var5) : -1L;
                long var8 = FoodSocketAttributeRules.boundedCooldown(var2, var6, cooldown(var1));
                if (var8 != var6 || !var4.contains(var5, 4)) {
                    var4.putLong(var5, var8);
                    var0.getPersistentData().put("FTBossFoodSocketAttributeCooldowns", var4);
                }

                return var2 >= var8;
            }
        }
    }

    private static void consume(ServerPlayer var0, Power var1, long var2) {
        CompoundTag var4 = var0.getPersistentData().getCompound("FTBossFoodSocketAttributeCooldowns");
        var4.putLong(var1.name(), FoodSocketAttributeRules.deadline(var2, cooldown(var1)));
        var0.getPersistentData().put("FTBossFoodSocketAttributeCooldowns", var4);
    }

    private static FoodSocketAttributePowers.CombatState guardState(ServerPlayer var0, ItemStack var1) {
        if (var0.isAlive() && !var0.isSpectator() && var1.getCount() == 1 && !FoodWeaponLifecycle.isBroken(var1)) {
            Resolution var2 = inspect(var1);
            if (!var2.has(Power.PRICE_GUARD)) {
                return null;
            } else {
                FoodSocketAttributePowers.CombatState var3 = GUARDS.get(var0.getUUID());
                if (var3 == null
                    || !FoodWeaponSourceLedger.originalHandContinuous(var3.source, var0)
                    || !var3.weaponId.equals(FoodWeaponLifecycle.weaponId(var1))) {
                    FoodWeaponLifecycle.ensureTracked(var1);
                    CompoundTag var4 = new CompoundTag();
                    var4.putString("Family", FoodWeaponFamilyBuild.kind(var1).name());
                    var4 = FoodWeaponSourceLedger.decorate(
                        var0, var1, var4, var0.getMainHandItem() == var1 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND
                    );
                    if (!FoodWeaponSourceLedger.originalHandContinuous(var4, var0)) {
                        return null;
                    }

                    var3 = new FoodSocketAttributePowers.CombatState(var0, var1, var2, var4);
                    GUARDS.put(var0.getUUID(), var3);
                }

                return var3;
            }
        } else {
            return null;
        }
    }

    private static boolean target(ServerPlayer var0, LivingEntity var1) {
        return var1 instanceof Monster
            && var1.isAlive()
            && !var1.isRemoved()
            && var1.level() == var0.level()
            && !var1.isAlliedTo(var0)
            && !var0.isAlliedTo(var1)
            && (!(var1 instanceof TamableAnimal var3) || !var3.isTame())
            && (!(var1 instanceof OwnableEntity var2) || var2.getOwnerUUID() == null);
    }

    private static void cue(ServerPlayer var0, Power var1) {
        var0.displayClientMessage(Component.translatable("message.ftboss.socket_attribute." + id(var1)), true);
    }

    private static boolean readyView(ServerPlayer var0, Power var1, long var2) {
        CompoundTag var4 = var0.getPersistentData();
        if (!var4.contains("FTBossFoodSocketAttributeCooldowns")) {
            return true;
        } else if (!var4.contains("FTBossFoodSocketAttributeCooldowns", 10)) {
            return false;
        } else {
            CompoundTag var5 = var4.getCompound("FTBossFoodSocketAttributeCooldowns");
            if (!var5.contains(var1.name())) {
                return true;
            } else if (!var5.contains(var1.name(), 4)) {
                return false;
            } else {
                long var6 = var5.getLong(var1.name());
                return var6 == FoodSocketAttributeRules.boundedCooldown(var2, var6, cooldown(var1)) && var2 >= var6;
            }
        }
    }

    public static FoodSocketAttributePowers.DefenseQuote quoteDefense(ServerPlayer var0, DamageSource var1, float var2) {
        if (var1 == null || !Float.isFinite(var2) || var2 <= 0.0F) {
            return new FoodSocketAttributePowers.DefenseQuote(0.0F, null, FoodSocketAttributePowers.DefenseRejection.INVALID_INPUT);
        } else if (var0 == null || !var0.isAlive() || var0.isSpectator()) {
            return new FoodSocketAttributePowers.DefenseQuote(var2, null, FoodSocketAttributePowers.DefenseRejection.INVALID_DEFENDER);
        } else if (!EXTRA_HIT.get() && !FoodWeaponBondHits.secondary(var1) && !var1.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            ItemStack var3 = FoodWeaponBondHits.preferred(
                var0, var0x -> FoodWeaponEvolution.isEvolutionCandidate(var0x) && inspect(var0x).has(Power.PRICE_GUARD)
            );
            if (var3.isEmpty()) {
                return new FoodSocketAttributePowers.DefenseQuote(var2, null, FoodSocketAttributePowers.DefenseRejection.NO_ELIGIBLE_WEAPON);
            } else if (FoodWeaponLifecycle.weaponId(var3) == null) {
                return new FoodSocketAttributePowers.DefenseQuote(var2, null, FoodSocketAttributePowers.DefenseRejection.UNTRACKED_WEAPON);
            } else if (FoodWeaponSourceLedger.materialFingerprint(var3).isEmpty()) {
                return new FoodSocketAttributePowers.DefenseQuote(var2, null, FoodSocketAttributePowers.DefenseRejection.INVALID_MATERIALS);
            } else {
                Resolution var4 = inspect(var3);
                FoodSocketAttributePowers.CombatState var5 = GUARDS.get(var0.getUUID());
                boolean var6 = var5 != null
                    && FoodWeaponSourceLedger.originalHandContinuousReadOnly(var5.source, var0)
                    && var5.weaponId.equals(FoodWeaponLifecycle.weaponId(var3));
                long var7 = clock(var0);
                float var9 = var6 && FoodSocketAttributeRules.active(var7, var5.guardUntil, 80) ? var5.guard : 0.0F;
                boolean var10 = var9 <= 0.0F && readyView(var0, Power.PRICE_GUARD, var7);
                if (var10) {
                    var9 = var4.guardHealth();
                }

                if (Float.isFinite(var9) && !(var9 <= 0.0F)) {
                    long var11 = var10 ? FoodSocketAttributeRules.deadline(var7, 80) : var5.guardUntil;
                    FoodSocketAttributePowers.DefensePlan var13 = new FoodSocketAttributePowers.DefensePlan(
                        var0,
                        var1,
                        var0.getMainHandItem() == var3 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                        var3,
                        var5,
                        var7,
                        var2,
                        var9,
                        var11,
                        var10
                    );
                    return new FoodSocketAttributePowers.DefenseQuote(var13.amount(), var13, FoodSocketAttributePowers.DefenseRejection.NONE);
                } else {
                    return new FoodSocketAttributePowers.DefenseQuote(var2, null, FoodSocketAttributePowers.DefenseRejection.NO_GUARD_AVAILABLE);
                }
            }
        } else {
            return new FoodSocketAttributePowers.DefenseQuote(var2, null, FoodSocketAttributePowers.DefenseRejection.SECONDARY_OR_BYPASS);
        }
    }

    public static void commitDefense(FoodSocketAttributePowers.DefensePlan var0, float var1, FoodWeaponFinalDamage.DefenseReceipt var2) {
        if (var0 != null
            && !var0.committed
            && var2 != null
            && Float.isFinite(var1)
            && !(var1 < 0.0F)
            && Float.isFinite(var0.input)
            && !(var0.input <= 0.0F)
            && Float.isFinite(var0.blocked)
            && !(var0.blocked <= 0.0F)
            && !EXTRA_HIT.get()
            && var0.player.isAlive()
            && !var0.player.isSpectator()
            && !FoodWeaponBondHits.secondary(var0.source)
            && !var0.source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (var2.matches(var0, var0.player, var0.source, var0.actualWeapon, var0.hand, var0.at, var1)
                && (var2.reason() != FoodWeaponFinalDamage.DefenseReason.POSITIVE_DEFENSE || !(var1 <= 0.0F) && !(var1 > var0.amount))
                && (var2.reason() != FoodWeaponFinalDamage.DefenseReason.SHIELD_ONLY_ZERO || var1 == 0.0F && var0.amount == 0.0F && var0.blocked == var0.input)
                )
             {
                ServerPlayer var3 = var0.player;
                ItemStack var4 = var3.getItemInHand(var0.hand);
                FoodSocketAttributePowers.CombatState var5 = GUARDS.get(var3.getUUID());
                if (var0.matchesContext(var3, var0.source, var4, var0.hand, var0.at)
                    && clock(var3) == var0.at
                    && var3.level().dimension().equals(var0.dimension)
                    && !var4.isEmpty()
                    && var4.getCount() == 1
                    && var4.getItem() == var0.weapon.getItem()
                    && Objects.equals(FoodWeaponLifecycle.weaponId(var4), FoodWeaponLifecycle.weaponId(var0.weapon))
                    && !FoodWeaponLifecycle.isBroken(var4)
                    && inspect(var4).has(Power.PRICE_GUARD)
                    && FoodWeaponBondHits.fingerprint(var4).equals(var0.fingerprint)
                    && FoodWeaponSourceLedger.materialFingerprint(var4).equals(var0.materials)
                    && var5 == var0.previous
                    && (var5 == null || var5.guard == var0.previousGuard && var5.guardUntil == var0.previousUntil)
                    && (!var0.starts || readyView(var3, Power.PRICE_GUARD, var0.at))) {
                    FoodSocketAttributePowers.CombatState var6 = guardState(var3, var4);
                    if (var6 != null) {
                        var0.committed = true;
                        var6.guard = var0.balance;
                        var6.guardUntil = var0.until;
                        if (var0.starts) {
                            consume(var3, Power.PRICE_GUARD, var0.at);
                            cue(var3, Power.PRICE_GUARD);
                            var3.serverLevel().sendParticles(ParticleTypes.ENCHANT, var3.getX(), var3.getY() + 1.0, var3.getZ(), 10, 0.4, 0.4, 0.4, 0.02);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(
        priority = EventPriority.NORMAL
    )
    public static void attack(BeforeMitigation var0) {
        FoodSocketAttributePowers.OffensePlan var1 = quoteOffense(var0.getEntity(), var0.getSource(), var0.getAmount());
        FoodWeaponFinalDamage.captureAttribute(var0, var1);
    }

    public static FoodSocketAttributePowers.OffensePlan quoteOffense(LivingEntity var0, DamageSource var1, float var2) {
        if (var0 != null
            && var1 != null
            && Float.isFinite(var2)
            && !(var2 <= 0.0F)
            && !FoodWeaponBondHits.secondary(var1)
            && !EXTRA_HIT.get()
            && var1.getEntity() instanceof ServerPlayer var3
            && var3.isAlive()
            && !var3.isSpectator()
            && target(var3, var0)) {
            FoodWeaponBondHits.Hit var11 = FoodWeaponBondHits.resolve(var1, var3);
            if (var11 == null) {
                return null;
            } else {
                long var5 = clock(var3);
                Resolution var7 = inspect(var11.weapon());
                ArrayList var8 = new ArrayList();
                CompoundTag var9 = var11.snapshot();
                Key var10 = new Key(var3.getUUID(), var9.getUUID("WeaponId"), var9.getString("OriginalHand"), var11.attack());
                if (var7.has(Power.SERIES_ECHO)
                    && readyView(var3, Power.SERIES_ECHO, var5)
                    && FoodWeaponSourceLedger.originalHandContinuousReadOnly(var9, var3)
                    && ECHOES.canOffer(var10)
                    && availableAttempt(var9, var3, FoodWeaponSourceLedger.AttemptDomain.SERIES_ECHO)) {
                    var8.add(new FoodWeaponSourceLedger.AttemptRequest(FoodWeaponSourceLedger.AttemptDomain.SERIES_ECHO, 1.0));
                }

                if (var7.has(Power.COLOR_REPEL)
                    && readyView(var3, Power.COLOR_REPEL, var5)
                    && availableAttempt(var9, var3, FoodWeaponSourceLedger.AttemptDomain.COLOR_REPEL)) {
                    var8.add(new FoodWeaponSourceLedger.AttemptRequest(FoodWeaponSourceLedger.AttemptDomain.COLOR_REPEL, 1.0));
                }

                if (var7.has(Power.PROCESS_BURST)
                    && readyView(var3, Power.PROCESS_BURST, var5)
                    && availableAttempt(var9, var3, FoodWeaponSourceLedger.AttemptDomain.PROCESS_BURST)) {
                    var8.add(new FoodWeaponSourceLedger.AttemptRequest(FoodWeaponSourceLedger.AttemptDomain.PROCESS_BURST, 1.0));
                }

                return new FoodSocketAttributePowers.OffensePlan(var3, var0, var1, var11, var7, var2, var5, var8);
            }
        } else {
            return null;
        }
    }

    private static boolean availableAttempt(CompoundTag var0, ServerPlayer var1, FoodWeaponSourceLedger.AttemptDomain var2) {
        return FoodWeaponSourceLedger.quoteAttempt(var0, var1, var2, 1.0).status() == FoodWeaponSourceLedger.AttemptStatus.AVAILABLE;
    }

    private static Power ownedPower(FoodWeaponSourceLedger.AttemptDomain var0) {
        if (var0 == null) {
            return null;
        } else {
            return switch (var0) {
                case SERIES_ECHO -> Power.SERIES_ECHO;
                case COLOR_REPEL -> Power.COLOR_REPEL;
                case PROCESS_BURST -> Power.PROCESS_BURST;
                default -> null;
            };
        }
    }

    private static boolean offenseWeaponContinuous(FoodSocketAttributePowers.OffensePlan plan) {
        if (plan.hit.kind() == net.farmingtales.ftboss.util.FoodWeaponBondRules.Kind.TRIDENT
            && "NATIVE_PROJECTILE".equals(plan.hit.snapshot().getString("DamagePurpose"))
            && "TRIDENT".equals(plan.hit.snapshot().getString("NativeEntry"))) {
            return !plan.actualWeapon.isEmpty()
                && FoodWeaponBondHits.equipped(plan.player, plan.hit) == plan.actualWeapon;
        }
        return plan.player.getItemInHand(plan.hand) == plan.actualWeapon;
    }

    private static boolean offenseContext(FoodSocketAttributePowers.OffensePlan var0) {
        if (var0 != null
            && !var0.committed
            && !EXTRA_HIT.get()
            && var0.player.serverLevel().getServer().isSameThread()
            && clock(var0.player) == var0.quotedAt
            && var0.player.isAlive()
            && !var0.player.isSpectator()
            && target(var0.player, var0.target)
            && !FoodWeaponBondHits.secondary(var0.source)
            && offenseWeaponContinuous(var0)
            && !FoodWeaponBondHits.equipped(var0.player, var0.hit).isEmpty()
            && FoodWeaponSourceLedger.validReadOnly(var0.hit.snapshot(), var0.player)) {
            if (Objects.equals(var0.cooldowns, var0.player.getPersistentData().get("FTBossFoodSocketAttributeCooldowns"))
                && ECHOES.entries(var0.player.getUUID()).equals(var0.echoes)) {
                Resolution var1 = inspect(var0.actualWeapon);

                for (FoodWeaponSourceLedger.AttemptRequest var3 : var0.attempts) {
                    Power var4 = ownedPower(var3.domain());
                    if (var4 != null && var1.has(var4) && readyView(var0.player, var4, var0.quotedAt)) {
                        if (var4 == Power.SERIES_ECHO
                            && !ECHOES.canOffer(new Key(var0.player.getUUID(), var0.weaponId(), var0.hit.snapshot().getString("OriginalHand"), var0.attackId()))
                            )
                         {
                            return false;
                        }

                        if (var4 != Power.PROCESS_BURST) {
                            continue;
                        }

                        FoodSocketAttributePowers.ProcessState var5 = PROCESS.get(var0.player.getUUID());
                        if (var5 == var0.process && (var5 == null || var5.hits == var0.processHits && var5.lastHit == var0.processLastHit)) {
                            continue;
                        }

                        return false;
                    }

                    return false;
                }

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean offenseRequests(
        FoodSocketAttributePowers.OffensePlan var0,
        FoodWeaponSourceLedger.AttemptBatchQuote var1,
        List<FoodWeaponSourceLedger.AttemptRequest> var2,
        List<FoodWeaponSourceLedger.TargetRequest> var3
    ) {
        if (var0 != null && FoodWeaponSourceLedger.matchesAttemptRequests(var1, var2, var3)) {
            EnumSet var4 = EnumSet.noneOf(FoodWeaponSourceLedger.AttemptDomain.class);

            for (FoodWeaponSourceLedger.AttemptRequest var6 : var0.attempts) {
                if (var6 == null || ownedPower(var6.domain()) == null || Double.compare(var6.chance(), 1.0) != 0 || !var4.add(var6.domain())) {
                    return false;
                }
            }

            EnumSet var8 = EnumSet.noneOf(FoodWeaponSourceLedger.AttemptDomain.class);

            for (FoodWeaponSourceLedger.AttemptRequest var7 : var2) {
                if (ownedPower(var7.domain()) != null && (!var4.contains(var7.domain()) || !var8.add(var7.domain()))) {
                    return false;
                }
            }

            if (!var8.equals(var4)) {
                return false;
            } else {
                for (FoodWeaponSourceLedger.AttemptQuote var11 : var1.entries()) {
                    if (var4.contains(var11.domain())
                        && (var11.status() != FoodWeaponSourceLedger.AttemptStatus.AVAILABLE || Double.compare(var11.chance(), 1.0) != 0 || !var11.success())) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public static boolean matchesOffenseBatch(
        FoodSocketAttributePowers.OffensePlan var0,
        FoodWeaponSourceLedger.AttemptBatchQuote var1,
        FoodWeaponSourceLedger.AttemptBatchAdmission var2,
        List<FoodWeaponSourceLedger.AttemptRequest> var3,
        List<FoodWeaponSourceLedger.TargetRequest> var4
    ) {
        return offenseContext(var0)
            && offenseRequests(var0, var1, var3, var4)
            && FoodWeaponSourceLedger.matchesAttemptAdmission(var0.hit.snapshot(), var0.player, var1, var2, var0.quotedAt);
    }

    private static boolean orderedAttributeBatch(FoodSocketAttributePowers.CompleteBatchBinding var0) {
        if (var0 != null && var0.requests.size() == var0.batch.entries().size() && var0.targets.size() == var0.batch.targets().size()) {
            Iterator var1 = var0.batch.entries().iterator();

            for (FoodWeaponSourceLedger.AttemptRequest var3 : var0.requests) {
                FoodWeaponSourceLedger.AttemptQuote var4 = (FoodWeaponSourceLedger.AttemptQuote)var1.next();
                if (var3.domain() != var4.domain() || Double.compare(var3.chance(), var4.chance()) != 0) {
                    return false;
                }
            }

            Iterator var6 = var0.batch.targets().iterator();

            for (FoodWeaponSourceLedger.TargetRequest var8 : var0.targets) {
                FoodWeaponSourceLedger.TargetQuote var5 = (FoodWeaponSourceLedger.TargetQuote)var6.next();
                if (!var8.target().equals(var5.target()) || var8.domain() != var5.domain()) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    static boolean bindOriginalSequence(
        FoodSocketAttributePowers.PreparedOffense var0,
        FoodWeaponOriginalSequence.Session var1,
        Object var2,
        int var3,
        FoodWeaponFinalDamage.OffenseReceipt var4,
        float var5,
        FoodWeaponFinalDamage.PublishingCleanup var6
    ) {
        if (var0 != null
            && !var0.publishAttempted
            && var0.originalSequence == null
            && FoodWeaponFinalDamage.matchesOffensePublishing(var0, var4, var6)
            && matchesAttributeReceipt(var0, var5, var4)) {
            FoodSocketAttributePowers.CompleteBatchBinding var7 = var0.binding;
            FoodWeaponConsumerSequence.Accepted var8 = FoodWeaponConsumerSequence.bind(
                var1,
                var2,
                var3,
                var0,
                var0.quote,
                var4,
                var6,
                var7.batch,
                var7.admission,
                var7.requests,
                var7.targets,
                var0.quote.player,
                var0.quote.target,
                var0.quote.source,
                var0.quote.actualWeapon,
                var0.quote.hand,
                var0.quote.quotedAt,
                var5,
                "AttributeProducer"
            );
            if (var8 == null) {
                return false;
            } else {
                var0.originalSequence = var8;
                return true;
            }
        } else {
            return false;
        }
    }

    static FoodSocketAttributePowers.PreparedOffense prepareOffense(
        FoodSocketAttributePowers.OffensePlan var0,
        FoodWeaponSourceLedger.AttemptBatchQuote var1,
        FoodWeaponSourceLedger.AttemptBatchAdmission var2,
        List<FoodWeaponSourceLedger.AttemptRequest> var3,
        List<FoodWeaponSourceLedger.TargetRequest> var4
    ) {
        if (var1 != null
            && var2 != null
            && var3 != null
            && var4 != null
            && var2.status() == FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY
            && matchesOffenseBatch(var0, var1, var2, var3, var4)) {
            FoodSocketAttributePowers.CompleteBatchBinding var5 = new FoodSocketAttributePowers.CompleteBatchBinding(var1, var2, var3, var4);
            return orderedAttributeBatch(var5) ? new FoodSocketAttributePowers.PreparedOffense(var0, var5) : null;
        } else {
            return null;
        }
    }

    private static boolean matchesAttributeReceipt(FoodSocketAttributePowers.PreparedOffense var0, float var1, FoodWeaponFinalDamage.OffenseReceipt var2) {
        if (var0 != null && var0.originalSequence != null) {
            return var0.originalSequence.matchesReceipt(var0, var2, var1);
        } else if (var0 != null && var2 != null && Float.isFinite(var1) && !(var1 <= 0.0F)) {
            FoodSocketAttributePowers.CompleteBatchBinding var3 = var0.binding;
            FoodSocketAttributePowers.OffensePlan var4 = var0.quote;
            return var2.matchesBatch(var3.batch, var3.admission, var3.requests, var3.targets)
                && var2.matches(var4, var4.player, var4.source, var4.quotedAt, var1);
        } else {
            return false;
        }
    }

    static boolean matchesPreparedOffense(
        FoodSocketAttributePowers.PreparedOffense var0,
        FoodSocketAttributePowers.OffensePlan var1,
        FoodWeaponSourceLedger.AttemptBatchQuote var2,
        FoodWeaponSourceLedger.AttemptBatchAdmission var3,
        List<FoodWeaponSourceLedger.AttemptRequest> var4,
        List<FoodWeaponSourceLedger.TargetRequest> var5
    ) {
        return var0 != null
            && var1 != null
            && var2 != null
            && var3 != null
            && var4 != null
            && var5 != null
            && var0.quote == var1
            && var0.binding.batch == var2
            && var0.binding.admission == var3
            && var0.binding.requests.equals(var4)
            && var0.binding.targets.equals(var5);
    }

    static boolean matchesOffenseCommit(
        FoodSocketAttributePowers.OffenseCommit var0,
        FoodSocketAttributePowers.PreparedOffense var1,
        FoodSocketAttributePowers.OffensePlan var2,
        FoodWeaponFinalDamage.OffenseReceipt var3,
        float var4,
        FoodWeaponSourceLedger.AttemptBatchQuote var5,
        FoodWeaponSourceLedger.AttemptBatchAdmission var6,
        List<FoodWeaponSourceLedger.AttemptRequest> var7,
        List<FoodWeaponSourceLedger.TargetRequest> var8
    ) {
        return var0 != null
            && var1 != null
            && var0.prepared == var1
            && var2 != null
            && var2.committed
            && var3 != null
            && var0.receipt == var3
            && Float.isFinite(var4)
            && var4 > 0.0F
            && Float.floatToRawIntBits(var0.amount) == Float.floatToRawIntBits(var4)
            && matchesPreparedOffense(var1, var2, var5, var6, var7, var8);
    }

    static FoodSocketAttributePowers.OffenseCommit commitPreparedOffense(
        FoodSocketAttributePowers.PreparedOffense var0, float var1, FoodWeaponFinalDamage.OffenseReceipt var2, FoodWeaponFinalDamage.PublishingCleanup var3
    ) {
        FoodWeaponConsumerSequence.Accepted var4 = var0 == null ? null : var0.originalSequence;
        int var5 = var4 == null ? 0 : var4.publicationCallEntered(var0);
        Throwable var6 = null;

        FoodSocketAttributePowers.OffenseCommit var7;
        try {
            if (var0 == null || var0.publishAttempted || var0.publishingCleanup != null || !FoodWeaponFinalDamage.matchesOffensePublishing(var0, var2, var3)) {
                return null;
            }

            var0.publishingCleanup = var3;
            var7 = commitPreparedOffense(var0, var1, var2);
        } catch (Throwable var11) {
            var6 = var11;
            throw var11;
        } finally {
            if (var4 != null) {
                var4.publicationExited(var0, var5, var6);
            }
        }

        return var7;
    }

    static FoodSocketAttributePowers.OffenseCommit commitPreparedOffense(
        FoodSocketAttributePowers.PreparedOffense var0, float var1, FoodWeaponFinalDamage.OffenseReceipt var2
    ) {
        FoodWeaponConsumerSequence.Accepted var3 = var0 == null ? null : var0.originalSequence;
        boolean var4 = false;
        int var5 = var3 == null ? 0 : var3.publicationCallEntered(var0);
        Throwable var6 = null;

        try {
            if (var0 == null
                || var0.publishAttempted
                || var0.publishingCleanup != null && !FoodWeaponFinalDamage.matchesOffensePublishing(var0, var2, var0.publishingCleanup)
                || !matchesAttributeReceipt(var0, var1, var2)
                || !offenseContext(var0.quote)) {
                return null;
            } else {
                var0.publishAttempted = true;
                var0.publicationReceipt = var2;
                var0.publicationAmountBits = Float.floatToRawIntBits(var1);
                var4 = true;
                FoodSocketAttributePowers.OffensePlan var7 = var0.quote;
                EnumSet var8 = EnumSet.noneOf(FoodWeaponSourceLedger.AttemptDomain.class);
                EnumSet var9 = EnumSet.noneOf(Power.class);

                for (FoodWeaponSourceLedger.AttemptRequest var11 : var7.attempts) {
                    Power var12 = ownedPower(var11.domain());
                    if (var12 == null || Double.compare(var11.chance(), 1.0) != 0 || !var8.add(var11.domain())) {
                        return null;
                    }

                    var9.add(var12);
                }

                for (FoodWeaponSourceLedger.AttemptDomain var52 : List.of(
                    FoodWeaponSourceLedger.AttemptDomain.SERIES_ECHO,
                    FoodWeaponSourceLedger.AttemptDomain.COLOR_REPEL,
                    FoodWeaponSourceLedger.AttemptDomain.PROCESS_BURST
                )) {
                    FoodWeaponSourceLedger.AttemptResult var54 = (var0.originalSequence == null ? var2.attempt(var52) : var0.originalSequence.attempt(var52))
                        .orElse(null);
                    if (var54 != null != var8.contains(var52)
                        || var54 != null && (var54.domain() != var52 || var54.commit() != FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS)) {
                        return null;
                    }
                }

                var7.committed = true;
                ServerPlayer var51 = var7.player;
                long var53 = var7.quotedAt;
                Key var56 = null;
                FoodSocketAttributePowers.PendingEcho var14 = null;
                ArrayList var15 = new ArrayList();
                boolean var16 = false;

                try {
                    if (var9.contains(Power.SERIES_ECHO)) {
                        var56 = new Key(var51.getUUID(), var7.weaponId(), var7.hit.snapshot().getString("OriginalHand"), var7.attackId());
                        double var17 = FoodWeaponBondRules.echoRange(var7.hit.kind());
                        var14 = new FoodSocketAttributePowers.PendingEcho(
                            var7.hit.snapshot(), var7.target.getUUID(), FoodSocketAttributeRules.deadline(var53, 12), var17 * var17, var7.actualWeapon
                        );
                        var0.echoKey = var56;
                        var0.echoValue = var14;
                        var0.echoOfferEntered = true;
                        boolean var19 = ECHOES.offer(var56, var14);
                        var0.echoOffered = var19;
                        var0.echoOfferCompleted = true;
                        if (!var19) {
                            return null;
                        }

                        Entry var20 = null;

                        for (Entry var22 : ECHOES.entries(var51.getUUID())) {
                            if (var22.key() == var56 && var22.value() == var14) {
                                var20 = var22;
                                break;
                            }
                        }

                        FoodSocketAttributePowers.CompleteBatchBinding var62 = var0.binding;
                        FoodWeaponSourceLedger.PendingEchoLineage var65 = FoodWeaponSourceLedger.issuePendingEchoLineage(
                            var0,
                            var7,
                            var2,
                            var1,
                            var62.batch,
                            var62.admission,
                            var62.requests,
                            var62.targets,
                            var20,
                            var51,
                            var7.target,
                            var7.source,
                            var14.dueAt(),
                            var14.rangeSquared()
                        );
                        if (var65 == null) {
                            return null;
                        }

                        var14.lineage = var65;
                        consume(var51, Power.SERIES_ECHO, var53);
                        var15.add(Power.SERIES_ECHO);
                    }

                    if (var9.contains(Power.COLOR_REPEL)) {
                        consume(var51, Power.COLOR_REPEL, var53);
                        var15.add(Power.COLOR_REPEL);
                    }

                    boolean var57 = false;
                    FoodSocketAttributePowers.ProcessState var18 = null;
                    if (var9.contains(Power.PROCESS_BURST)) {
                        FoodSocketAttributePowers.ProcessState var58 = PROCESS.get(var51.getUUID());
                        if (var58 == null
                            || !var58.weaponId.equals(var7.weaponId())
                            || !var58.fingerprint.equals(FoodWeaponBondHits.fingerprint(var7.hit.weapon()))
                            || var58.dimension != var51.level().dimension()) {
                            var58 = new FoodSocketAttributePowers.ProcessState(var51, var7.hit);
                            PROCESS.put(var51.getUUID(), var58);
                        }

                        var58.hits = FoodSocketAttributeRules.advance(var58.hits, var58.lastHit, var53);
                        var58.lastHit = var53;
                        if (var58.hits >= 3) {
                            var58.hits = 0;
                            consume(var51, Power.PROCESS_BURST, var53);
                            var15.add(Power.PROCESS_BURST);
                            var57 = true;
                        }

                        var18 = var58;
                    }

                    Tag var59 = var51.getPersistentData().get("FTBossFoodSocketAttributeCooldowns");
                    FoodSocketAttributePowers.OffenseCommit var60 = new FoodSocketAttributePowers.OffenseCommit(
                        var0, var2, var1, List.copyOf(var9), var15, var57, var59 instanceof CompoundTag var63 ? var63 : null, var18, var56, var14
                    );
                    var16 = true;
                    var0.commitConstructed = true;
                    if (var0.originalSequence != null) {
                        var0.originalSequence.published(var0, var60);
                    }

                    return var60;
                } catch (Error | RuntimeException var45) {
                    var0.publishFailure = var45;
                    throw var45;
                } finally {
                    if (!var16 && var56 != null && var14 != null) {
                        try {
                            retireOffenseEcho(var0);
                        } catch (Error | RuntimeException var44) {
                            var0.echoRetirementFailure = var44;
                        }
                    }
                }
            }
        } catch (Throwable var47) {
            var6 = var47;
            if (var4 && (var47 instanceof RuntimeException || var47 instanceof Error)) {
                var0.publishFailure = var47;
                if (var0.originalSequence != null) {
                    var0.originalSequence.originalThrown(var47);
                }
            }

            throw var47;
        } finally {
            if (var4) {
                var0.publishExited = true;
            }

            if (var3 != null) {
                var3.publicationExited(var0, var5, var6);
            }
        }
    }

    static void finishOffensePublication(FoodSocketAttributePowers.PreparedOffense var0, FoodWeaponFinalDamage.PublishingCleanup var1) {
        FoodWeaponConsumerSequence.Accepted var2 = var0 == null ? null : var0.originalSequence;
        Throwable var3 = null;

        try {
            if (!preparedCleanupCaller()
                || var0 == null
                || var0.publishingCleanup != var1
                || (var0.originalSequence == null || !var0.originalSequence.cleanup(var0, var1))
                    && !FoodWeaponFinalDamage.matchesOffensePublicationCleanup(var0, var1)
                || !var0.publishAttempted
                || !var0.publishExited
                || var0.publicationReceipt == null) {
                return;
            }

            FoodSocketAttributePowers.CompleteBatchBinding var4 = var0.binding;
            if (!matchesPreparedOffense(var0, var0.quote, var4.batch, var4.admission, var4.requests, var4.targets)) {
                return;
            }

            var0.finalizingCleanup = var1;

            try {
                retireOffenseEcho(var0);
            } finally {
                if (var0.originalSequence != null) {
                    var0.originalSequence.retired(var0);
                }
            }
        } catch (Throwable var14) {
            var3 = var14;
            throw var14;
        } finally {
            if (var2 != null) {
                var2.publicationCleanupExited(var0, var1, var3);
            }
        }
    }

    private static boolean preparedCleanupCaller() {
        return Thread.holdsLock(FoodWeaponSourceLedger.class)
            && StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0 -> var0.filter(var0x -> var0x.getDeclaringClass() != FoodSocketAttributePowers.class)
                            .findFirst()
                            .map(
                                var0x -> var0x.getDeclaringClass() == FoodWeaponFinalDamage.class
                                        && var0x.getMethodName().equals("finalizeFailedPublication")
                                        && var0x.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingCleanup;)V")
                            )
                            .orElse(false)
                );
    }

    private static boolean offenseCleanupPhase(FoodSocketAttributePowers.PreparedOffense var0) {
        if (var0.originalSequence != null && var0.originalSequence.cleanup(var0, var0.publishingCleanup)) {
            return true;
        } else {
            FoodWeaponFinalDamage.PublishingCleanup var1 = var0.publishingCleanup;
            if (var1 == null) {
                return true;
            } else {
                return var0.finalizingCleanup == null
                    ? FoodWeaponFinalDamage.matchesOffensePublishing(var0, var0.publicationReceipt, var1)
                    : var0.finalizingCleanup == var1 && FoodWeaponFinalDamage.matchesOffensePublicationCleanup(var0, var1);
            }
        }
    }

    private static void retireOffenseEcho(FoodSocketAttributePowers.PreparedOffense var0) {
        if (!var0.echoRetirementStarted
            && var0.echoKey != null
            && var0.echoValue != null
            && var0.echoOfferEntered
            && (!var0.echoOfferCompleted || var0.echoOffered)) {
            var0.echoRetirementStarted = true;

            try {
                FoodSocketAttributePowers.CompleteBatchBinding var1 = var0.binding;
                if (offenseCleanupPhase(var0)
                    && var1.originalBinding == FoodWeaponSourceLedger.originalInteractionBinding(var1.batch)
                    && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(
                        var1.originalBinding, FoodWeaponSourceLedger.originalInteractionBinding(var1.batch), var0.quote.player
                    )
                    && offenseCleanupPhase(var0)) {
                    var0.echoRetired = ECHOES.remove(var0.echoKey, var0.echoValue);
                    var0.echoRetirementCompleted = true;
                } else {
                    var0.echoDisplaced = true;
                }
            } catch (Error | RuntimeException var2) {
                var0.echoRetirementFailure = var2;
                throw var2;
            }
        }
    }

    private static FoodSocketAttributePowers.OffenseCommit commitOffensePublication(
        FoodSocketAttributePowers.PreparedOffense var0, float var1, FoodWeaponFinalDamage.OffenseReceipt var2
    ) {
        return commitPreparedOffense(var0, var1, var2);
    }

    static boolean matchesOriginalPendingEchoOffer(
        FoodSocketAttributePowers.PreparedOffense var0,
        FoodSocketAttributePowers.OffensePlan var1,
        FoodWeaponFinalDamage.OffenseReceipt var2,
        float var3,
        Entry<?> var4,
        ServerPlayer var5,
        LivingEntity var6,
        DamageSource var7,
        long var8,
        double var10
    ) {
        return var0 != null
                && var0.originalSequence != null
                && var0.quote == var1
                && var1 != null
                && var0.originalSequence.matchesReceipt(var0, var2, var3)
                && var0.echoOfferCompleted
                && var0.echoOffered
                && !var0.echoRetirementStarted
                && var0.echoKey != null
                && var0.echoValue != null
                && var4 != null
                && var4.key() == var0.echoKey
                && var4.value() == var0.echoValue
                && var5 == var1.player
                && var6 == var1.target
                && var7 == var1.source
                && var8 == var0.echoValue.dueAt()
                && Double.doubleToRawLongBits(var10) == Double.doubleToRawLongBits(var0.echoValue.rangeSquared())
                && var0.originalSequence.ordinaryContext()
            ? var0.originalSequence
                    .attempt(FoodWeaponSourceLedger.AttemptDomain.SERIES_ECHO)
                    .filter(
                        var0x -> var0x.domain() == FoodWeaponSourceLedger.AttemptDomain.SERIES_ECHO
                                && var0x.commit() == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS
                    )
                    .isPresent()
                && ECHOES.entries(var5.getUUID()).stream().anyMatch(var1x -> var1x.key() == var0.echoKey && var1x.value() == var0.echoValue)
            : false;
    }

    private static boolean publishedAttribute(FoodSocketAttributePowers.OffenseCommit var0) {
        FoodSocketAttributePowers.OffensePlan var1 = var0.prepared.quote;
        if (!var0.cooldownPowers.isEmpty()) {
            if (var1.player.getPersistentData().get("FTBossFoodSocketAttributeCooldowns") != var0.publishedCooldowns) {
                return false;
            }

            for (Power var3 : var0.cooldownPowers) {
                if (!Objects.equals(var0.publishedCooldowns.get(var3.name()), var0.publishedCooldownState.get(var3.name()))) {
                    return false;
                }
            }
        }

        return var0.publishedProcess == null
                || PROCESS.get(var1.player.getUUID()) == var0.publishedProcess
                    && var0.publishedProcess.hits == var0.publishedHits
                    && var0.publishedProcess.lastHit == var0.publishedLastHit
            ? var0.echoKey == null
                || !ECHOES.entries(var1.player.getUUID()).stream().noneMatch(var1x -> var1x.key().equals(var0.echoKey) && var1x.value() == var0.echoValue)
            : false;
    }

    private static boolean attributeWorldContext(FoodSocketAttributePowers.OffenseCommit var0) {
        if (var0 != null && var0.prepared.originalSequence != null) {
            return var0.prepared.originalSequence.ordinaryContext();
        } else {
            FoodSocketAttributePowers.OffensePlan var1 = var0.prepared.quote;
            return var1.committed
                && !EXTRA_HIT.get()
                && var1.player.serverLevel().getServer() == var0.server
                && var0.server.isSameThread()
                && Thread.currentThread() == var0.thread
                && clock(var1.player) == var1.quotedAt
                && var1.player.isAlive()
                && !var1.player.isSpectator()
                && target(var1.player, var1.target)
                && !FoodWeaponBondHits.secondary(var1.source)
                && offenseWeaponContinuous(var1)
                && !FoodWeaponBondHits.equipped(var1.player, var1.hit).isEmpty()
                && FoodWeaponSourceLedger.validReadOnly(var1.hit.snapshot(), var1.player);
        }
    }

    private static boolean attributeLeafLive(FoodSocketAttributePowers.OffenseCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        return var0 != null && var0.prepared.originalSequence != null
            ? var0.executed && publishedAttribute(var0) && var0.prepared.originalSequence.continuing(var0, var1)
            : var0 != null
                && var0.executed
                && publishedAttribute(var0)
                && matchesAttributeReceipt(var0.prepared, var0.amount, var0.receipt)
                && FoodWeaponFinalDamage.matchesAttributeInvocation(var0, var1);
    }

    static void applyOffense(FoodSocketAttributePowers.OffenseCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        if (var0 != null
            && !var0.executed
            && Thread.currentThread() == var0.thread
            && var0.server == var0.prepared.quote.player.serverLevel().getServer()
            && var0.server.isSameThread()) {
            FoodWeaponConsumerSequence.Accepted var2 = var0.prepared.originalSequence;
            if (var2 == null || var2.enter(var0, var1)) {
                Throwable var3 = null;

                try {
                    var0.executed = true;
                    if ((var2 == null ? attributeWorldContext(var0) : var2.ordinaryContext()) && attributeLeafLive(var0, var1)) {
                        FoodSocketAttributePowers.OffensePlan var4 = var0.prepared.quote;
                        ServerPlayer var5 = var4.player;
                        List var6 = var0.powers;
                        if (var6.contains(Power.SERIES_ECHO)) {
                            if (!attributeLeafLive(var0, var1)) {
                                return;
                            }

                            cue(var5, Power.SERIES_ECHO);
                            if (!attributeLeafLive(var0, var1)) {
                                return;
                            }
                        }

                        if (var6.contains(Power.COLOR_REPEL)) {
                            LivingEntity var7 = var4.target;
                            if (!attributeLeafLive(var0, var1)) {
                                return;
                            }

                            List<Monster> var8 = var5.serverLevel()
                                .getEntitiesOfClass(
                                    Monster.class, var7.getBoundingBox().inflate(3.0), var2x -> target(var5, var2x) && var2x.distanceToSqr(var7) <= 9.0
                                );
                            var8.sort(Comparator.comparingDouble(var1x -> var1x.distanceToSqr(var7)));
                            if (!attributeLeafLive(var0, var1)) {
                                return;
                            }

                            for (Monster var10 : var8.stream().limit(4L).toList()) {
                                if (!attributeLeafLive(var0, var1)) {
                                    return;
                                }

                                var10.knockback(0.6, var7.getX() - var10.getX(), var7.getZ() - var10.getZ());
                                if (!attributeLeafLive(var0, var1)) {
                                    return;
                                }
                            }

                            if (!attributeLeafLive(var0, var1)) {
                                return;
                            }

                            cue(var5, Power.COLOR_REPEL);
                            if (!attributeLeafLive(var0, var1)) {
                                return;
                            }

                            var5.serverLevel()
                                .sendParticles(ParticleTypes.HAPPY_VILLAGER, var7.getX(), var7.getY() + 0.5, var7.getZ(), 12, 1.0, 0.3, 1.0, 0.01);
                            if (!attributeLeafLive(var0, var1)) {
                                return;
                            }
                        }

                        if (var0.bursts) {
                            if (attributeLeafLive(var0, var1)) {
                                burst(var0, var5, var4.target, var1);
                                if (attributeLeafLive(var0, var1)) {
                                    cue(var5, Power.PROCESS_BURST);
                                    if (!attributeLeafLive(var0, var1)) {
                                        ;
                                    }
                                }
                            }
                        }
                    }
                } catch (Error | RuntimeException var14) {
                    var3 = var14;
                    throw var14;
                } finally {
                    if (var2 != null) {
                        var2.finish(var0, var1, var3);
                    }
                }
            }
        }
    }

    static boolean matchesPendingEchoDispatch(
        FoodSocketAttributePowers.PendingEchoDispatch var0, ServerPlayer var1, LivingEntity var2, DamageSource var3, float var4
    ) {
        return var0 != null
            && var0.active
            && !var0.hurtEntered
            && var0.player == var1
            && var0.target == var2
            && var0.damageSource == var3
            && Float.isFinite(var4)
            && var4 > 0.0F
            && Float.floatToRawIntBits(var0.amount) == Float.floatToRawIntBits(var4)
            && var0.entry.key() == var0.key
            && var0.entry.value() == var0.pending
            && var0.key.owner().equals(var1.getUUID())
            && var0.pending.target().equals(var2.getUUID())
            && var0.pending.dueAt() == var0.dueAt
            && Double.doubleToRawLongBits(var0.pending.rangeSquared()) == Double.doubleToRawLongBits(var0.rangeSquared)
            && var0.paidSource.equals(var0.pending.source())
            && Thread.currentThread() == var0.thread
            && var0.server == var1.serverLevel().getServer()
            && var0.server.isSameThread()
            && clock(var1) == var0.dispatchedAt;
    }

    static CompoundTag pendingEchoDispatchSource(FoodSocketAttributePowers.PendingEchoDispatch var0) {
        return var0 != null && var0.active ? var0.paidSource.copy() : null;
    }

    static ItemStack originalPendingEchoWeapon(FoodSocketAttributePowers.PendingEchoDispatch var0) {
        return var0 != null
                && var0.originalWeapon != null
                && var0.originalWeapon == var0.pending.originalWeapon
                && originalPendingEchoWeaponCaller()
                && matchesPendingEchoDispatch(var0, var0.player, var0.target, var0.damageSource, var0.amount)
            ? var0.originalWeapon
            : null;
    }

    private static boolean originalPendingEchoWeaponCaller() {
        return Thread.holdsLock(FoodWeaponSourceLedger.class)
            && StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0 -> {
                        Iterator var1 = var0.dropWhile(var0x -> var0x.getDeclaringClass() == FoodSocketAttributePowers.class).iterator();
                        if (!var1.hasNext()) {
                            return false;
                        } else {
                            StackFrame var2 = (StackFrame)var1.next();
                            if (var2.getDeclaringClass() == FoodWeaponSourceLedger.class
                                && var2.getMethodName().equals("beginPendingEchoInteraction")
                                && var2.getDescriptor()
                                    .equals(
                                        "(Lnet/farmingtales/ftboss/item/FoodSocketAttributePowers$PendingEchoDispatch;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$PendingEchoInteraction;"
                                    )) {
                                while (var1.hasNext()) {
                                    StackFrame var3 = (StackFrame)var1.next();
                                    if (var3.getDeclaringClass() != FoodWeaponSourceLedger.class) {
                                        return var3.getDeclaringClass() == FoodSocketAttributePowers.class
                                            && var3.getMethodName().equals("damagePendingEcho")
                                            && var3.getDescriptor()
                                                .equals(
                                                    "(Lnet/farmingtales/ftboss/item/FoodWeaponEchoQueue$Entry;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;J)V"
                                                );
                                    }
                                }

                                return false;
                            } else {
                                return false;
                            }
                        }
                    }
                );
    }

    private static boolean queuedPendingEcho(Entry<FoodSocketAttributePowers.PendingEcho> var0) {
        return ECHOES.entries(var0.key().owner()).stream().anyMatch(var1 -> var1.key() == var0.key() && var1.value() == var0.value());
    }

    private static void damagePendingEcho(Entry<FoodSocketAttributePowers.PendingEcho> var0, CompoundTag var1, ServerPlayer var2, LivingEntity var3, long var4) {
        if (Float.isFinite(2.0F) && target(var2, var3)) {
            DamageSource var6 = FoodWeaponDamageSources.attributeSecondary(var2);
            if (var6 == null) {
                pendingEchoParticles(var2, var3);
            } else {
                FoodSocketAttributePowers.PendingEchoDispatch var7 = new FoodSocketAttributePowers.PendingEchoDispatch(var0, var1, var2, var3, var6, 2.0F, var4);
                FoodWeaponSourceLedger.PendingEchoInteraction var8 = null;

                try {
                    var8 = FoodWeaponSourceLedger.beginDurablePendingEchoInteraction(
                        ((FoodSocketAttributePowers.PendingEcho)var0.value()).lineage, var0, var7, var2, var3, var6, 2.0F
                    );
                    if (var8 == null) {
                        return;
                    }

                    hurtPendingEcho(var7, var8);
                    if (!FoodWeaponSourceLedger.matchesPendingEchoInteraction(var8, var7, var2, var3, var6, 2.0F)) {
                        return;
                    }

                    pendingEchoParticles(var2, var3);
                    if (FoodWeaponSourceLedger.matchesPendingEchoInteraction(var8, var7, var2, var3, var6, 2.0F)) {
                        return;
                    }
                } finally {
                    try {
                        FoodWeaponSourceLedger.closePendingEchoInteraction(var8);
                    } finally {
                        var7.active = false;
                    }
                }
            }
        } else {
            pendingEchoParticles(var2, var3);
        }
    }

    private static void hurtPendingEcho(FoodSocketAttributePowers.PendingEchoDispatch var0, FoodWeaponSourceLedger.PendingEchoInteraction var1) {
        if (var0 != null && var1 != null && !var0.hurtEntered && matchesPendingEchoDispatch(var0, var0.player, var0.target, var0.damageSource, var0.amount)) {
            boolean var2 = EXTRA_HIT.get();
            EXTRA_HIT.set(true);

            try {
                FoodWeaponDamageSources.withSecondary(() -> hurtPendingEchoRaw(var0, var1));
            } finally {
                EXTRA_HIT.set(var2);
            }
        }
    }

    private static boolean hurtPendingEchoRaw(FoodSocketAttributePowers.PendingEchoDispatch var0, FoodWeaponSourceLedger.PendingEchoInteraction var1) {
        if (var0 != null && var1 != null && !var0.hurtEntered && matchesPendingEchoDispatch(var0, var0.player, var0.target, var0.damageSource, var0.amount)) {
            var0.hurtEntered = true;
            return FoodWeaponSourceLedger.hurtPendingEchoInteraction(var1, var0, var0.player, var0.target, var0.damageSource, var0.amount);
        } else {
            return false;
        }
    }

    private static void pendingEchoParticles(ServerPlayer var0, LivingEntity var1) {
        var0.serverLevel().sendParticles(ParticleTypes.ENCHANTED_HIT, var1.getX(), var1.getY() + 0.5, var1.getZ(), 6, 0.3, 0.3, 0.3, 0.01);
    }

    private static void damage(ServerPlayer var0, LivingEntity var1, float var2) {
        if (Float.isFinite(var2) && !(var2 <= 0.0F) && target(var0, var1)) {
            DamageSource var3 = FoodWeaponDamageSources.attributeSecondary(var0);
            if (var3 != null) {
                boolean var4 = EXTRA_HIT.get();
                EXTRA_HIT.set(true);

                try {
                    FoodWeaponDamageSources.withSecondary(() -> var1.hurt(var3, var2));
                } finally {
                    EXTRA_HIT.set(var4);
                }
            }
        }
    }

    private static void burst(FoodSocketAttributePowers.OffenseCommit var0, ServerPlayer var1, LivingEntity var2, FoodWeaponFinalDamage.ChildHurtBridge var3) {
        if (attributeLeafLive(var0, var3)) {
            List<Monster> var4 = var1.serverLevel()
                .getEntitiesOfClass(
                    Monster.class, var2.getBoundingBox().inflate(3.0), var2x -> var2x != var2 && target(var1, var2x) && var2x.distanceToSqr(var2) <= 9.0
                );
            var4.sort(Comparator.comparingDouble(var1x -> var1x.distanceToSqr(var2)));
            if (attributeLeafLive(var0, var3)) {
                for (Monster var6 : var4.stream().limit(4L).toList()) {
                    if (!attributeLeafLive(var0, var3)) {
                        return;
                    }

                    damage(var0, var1, var6, 4.0F, var3);
                    if (!attributeLeafLive(var0, var3)) {
                        return;
                    }
                }

                if (attributeLeafLive(var0, var3)) {
                    var1.serverLevel().sendParticles(ParticleTypes.CRIT, var2.getX(), var2.getY() + 0.5, var2.getZ(), 16, 0.8, 0.3, 0.8, 0.01);
                    if (attributeLeafLive(var0, var3)) {
                        ;
                    }
                }
            }
        }
    }

    static FoodWeaponConsumerSequence.OriginalChild originalAttributeChild(
        FoodSocketAttributePowers.OffenseCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1, LivingEntity var2, DamageSource var3, float var4
    ) {
        if (var0 != null && var0.prepared.originalSequence != null) {
            FoodWeaponConsumerSequence.OriginalChild var5 = var0.originalChild;
            return var0.prepared.originalSequence.matchesChild(var5, var0, var1, var2, var3, var4) ? var5 : null;
        } else {
            return null;
        }
    }

    private static boolean hurtOriginalAttributeChild(
        FoodSocketAttributePowers.OffenseCommit var0,
        FoodWeaponFinalDamage.ChildHurtBridge var1,
        FoodWeaponConsumerSequence.OriginalChild var2,
        LivingEntity var3,
        DamageSource var4,
        float var5
    ) {
        if (var2 != null && !var2.entered()) {
            return false;
        } else {
            try {
                boolean var6 = FoodWeaponFinalDamage.hurtListedChild(var1, var3, var4, var5);
                if (var2 != null) {
                    var2.returned(var6);
                }

                return var6;
            } catch (Error | RuntimeException var7) {
                if (var2 != null) {
                    var2.originalThrown(var7);
                }

                throw var7;
            }
        }
    }

    private static void damage(
        FoodSocketAttributePowers.OffenseCommit var0, ServerPlayer var1, LivingEntity var2, float var3, FoodWeaponFinalDamage.ChildHurtBridge var4
    ) {
        if (attributeLeafLive(var0, var4) && Float.isFinite(var3) && !(var3 <= 0.0F) && target(var1, var2)) {
            DamageSource var5 = FoodWeaponDamageSources.attributeSecondary(var1);
            if (var5 != null && attributeLeafLive(var0, var4)) {
                FoodWeaponConsumerSequence.Accepted var6 = var0.prepared.originalSequence;
                FoodWeaponConsumerSequence.OriginalChild var7 = var6 == null ? null : var6.beginChild(var0, var4, var2, var5, var3);
                if (var6 == null || var7 != null) {
                    var0.originalChild = var7;
                    boolean var8 = EXTRA_HIT.get();
                    EXTRA_HIT.set(true);

                    try {
                        FoodWeaponDamageSources.withSecondary(() -> hurtOriginalAttributeChild(var0, var4, var7, var2, var5, var3));
                    } finally {
                        try {
                            EXTRA_HIT.set(var8);
                        } finally {
                            if (var6 != null) {
                                var6.closeChild(var7);
                            }

                            if (var0.originalChild == var7) {
                                var0.originalChild = null;
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void tick(Post var0) {
        if (var0.getEntity() instanceof ServerPlayer var1) {
            long var10 = clock(var1);

            for (Entry var5 : ECHOES.entries(var1.getUUID())) {
                FoodSocketAttributePowers.PendingEcho var6 = (FoodSocketAttributePowers.PendingEcho)var5.value();
                CompoundTag var7 = var6.source();
                if (!var1.isAlive()
                    || var1.isSpectator()
                    || !FoodWeaponSourceLedger.pendingEchoSourceContinuous(var6.lineage, var5, var7, var1, var6.target(), var6.dueAt(), var6.rangeSquared())
                    || var1.getItemInHand(InteractionHand.valueOf(var7.getString("OriginalHand"))).getCount() != 1
                    || var6.dueAt() > FoodSocketAttributeRules.deadline(var10, 12)) {
                    ECHOES.remove(var5.key(), var6);
                } else if (var10 >= var6.dueAt() && queuedPendingEcho(var5) && ECHOES.remove(var5.key(), var6) && var10 - var6.dueAt() <= 20L) {
                    Entity var8 = var1.serverLevel().getEntity(var6.target());
                    if (var8 instanceof LivingEntity) {
                        LivingEntity var9 = (LivingEntity)var8;
                        if (target(var1, var9) && var9.distanceToSqr(var1) <= var6.rangeSquared()) {
                            damagePendingEcho(var5, var7, var1, var9, var10);
                        }
                    }
                }
            }
        }
    }

    private static void clear(Player var0) {
        ECHOES.clear(var0.getUUID());
        GUARDS.remove(var0.getUUID());
        PROCESS.remove(var0.getUUID());
    }

    @SubscribeEvent
    public static void login(PlayerLoggedInEvent var0) {
        clear(var0.getEntity());
    }

    @SubscribeEvent
    public static void logout(PlayerLoggedOutEvent var0) {
        clear(var0.getEntity());
    }

    @SubscribeEvent
    public static void dimension(PlayerChangedDimensionEvent var0) {
        clear(var0.getEntity());
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void death(LivingDeathEvent var0) {
        if (var0.getEntity() instanceof ServerPlayer var1) {
            clear(var1);
        }
    }

    @SubscribeEvent
    public static void clonePlayer(Clone var0) {
        if (var0.getOriginal().getPersistentData().contains("FTBossFoodSocketAttributeCooldowns")) {
            var0.getEntity()
                .getPersistentData()
                .put("FTBossFoodSocketAttributeCooldowns", var0.getOriginal().getPersistentData().get("FTBossFoodSocketAttributeCooldowns").copy());
        }

        clear(var0.getOriginal());
        clear(var0.getEntity());
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent var0) {
        ECHOES.clear();
        GUARDS.clear();
        PROCESS.clear();
        EXTRA_HIT.remove();
    }

    public static List<WeaponDetailView> details(ItemStack var0) {
        Resolution var1 = inspect(var0);
        ArrayList var2 = new ArrayList();

        for (Power var6 : Power.values()) {
            if (var1.has(var6)) {
                var2.add(new WeaponDetailView(Component.translatable("tooltip.ftboss.socket_attribute.category"), title(var6, var1), description(var6, var1)));
            }
        }

        return List.copyOf(var2);
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent var0) {
        if (FoodWeaponEvolution.isEvolutionCandidate(var0.getItemStack())) {
            Resolution var1 = inspect(var0.getItemStack());

            for (Power var5 : Power.values()) {
                if (var1.has(var5)) {
                    int var6 = switch (var5) {
                        case PRICE_GUARD -> var1.priceProgress();
                        case SERIES_ECHO -> var1.seriesProgress();
                        case COLOR_REPEL -> var1.colorProgress();
                        case PROCESS_BURST -> var1.processProgress();
                    };
                    var0.getToolTip().add(Component.translatable("tooltip.ftboss.socket_attribute.progress", title(var5, var1), Math.min(3, var6)));
                }
            }
        }
    }

    private static final class CombatState {
        private final CompoundTag source;
        private final UUID weaponId;
        private final Resolution recipe;
        private float guard;
        private long guardUntil;

        private CombatState(ServerPlayer var1, ItemStack var2, Resolution var3, CompoundTag var4) {
            this.source = var4.copy();
            FoodWeaponLifecycle.ensureTracked(var2);
            this.weaponId = FoodWeaponLifecycle.weaponId(var2);
            this.recipe = var3;
        }
    }

    private static final class CompleteBatchBinding {
        private final FoodWeaponSourceLedger.AttemptBatchQuote batch;
        private final FoodWeaponSourceLedger.AttemptBatchAdmission admission;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final List<FoodWeaponSourceLedger.AttemptRequest> requests;
        private final List<FoodWeaponSourceLedger.TargetRequest> targets;

        private CompleteBatchBinding(
            FoodWeaponSourceLedger.AttemptBatchQuote var1,
            FoodWeaponSourceLedger.AttemptBatchAdmission var2,
            List<FoodWeaponSourceLedger.AttemptRequest> var3,
            List<FoodWeaponSourceLedger.TargetRequest> var4
        ) {
            this.batch = var1;
            this.admission = var2;
            this.originalBinding = FoodWeaponSourceLedger.originalInteractionBinding(var1);
            this.requests = List.copyOf(var3);
            this.targets = List.copyOf(var4);
        }
    }

    public static enum DefenseAcceptance {
        REJECTED,
        POSITIVE_FINAL_DAMAGE,
        SHIELD_ONLY_ZERO;
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
        private final FoodSocketAttributePowers.CombatState previous;
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

        private DefensePlan(
            ServerPlayer var1,
            DamageSource var2,
            InteractionHand var3,
            ItemStack var4,
            FoodSocketAttributePowers.CombatState var5,
            long var6,
            float var8,
            float var9,
            long var10,
            boolean var12
        ) {
            this.player = var1;
            this.source = var2;
            this.hand = var3;
            this.weapon = var4.copy();
            this.actualWeapon = var4;
            this.server = var1.serverLevel().getServer();
            this.fingerprint = FoodWeaponBondHits.fingerprint(var4);
            this.materials = FoodWeaponSourceLedger.materialFingerprint(var4);
            this.dimension = var1.level().dimension();
            this.previous = var5;
            this.previousGuard = var5 == null ? 0.0F : var5.guard;
            this.previousUntil = var5 == null ? 0L : var5.guardUntil;
            this.at = var6;
            this.input = var8;
            this.blocked = FoodSocketAttributeRules.absorb(var8, var9);
            this.amount = var8 - this.blocked;
            this.balance = var9 - this.blocked;
            this.until = var10;
            this.starts = var12;
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

        public boolean matchesContext(ServerPlayer var1, DamageSource var2, ItemStack var3, InteractionHand var4, long var5) {
            return var1 == this.player && var2 == this.source && var3 == this.actualWeapon && var4 == this.hand && var5 == this.at && !this.committed
                ? this.player.isAlive()
                    && !this.player.isSpectator()
                    && this.player.serverLevel().getServer() == this.server
                    && FoodSocketAttributePowers.clock(this.player) == this.at
                    && this.player.level().dimension().equals(this.dimension)
                    && this.player.getItemInHand(this.hand) == this.actualWeapon
                    && !this.actualWeapon.isEmpty()
                    && this.actualWeapon.getCount() == 1
                    && this.actualWeapon.getItem() == this.weapon.getItem()
                    && Objects.equals(FoodWeaponLifecycle.weaponId(this.actualWeapon), FoodWeaponLifecycle.weaponId(this.weapon))
                    && !FoodWeaponLifecycle.isBroken(this.actualWeapon)
                    && FoodWeaponBondHits.fingerprint(this.actualWeapon).equals(this.fingerprint)
                    && FoodWeaponSourceLedger.materialFingerprint(this.actualWeapon).equals(this.materials)
                : false;
        }
    }

    public static record DefenseQuote(float amount, FoodSocketAttributePowers.DefensePlan plan, FoodSocketAttributePowers.DefenseRejection rejection) {
        public boolean shieldAbsorbed() {
            return this.plan != null && this.plan.amount() == 0.0F && this.plan.blocked() == this.plan.input();
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

    static final class OffenseCommit {
        private final FoodSocketAttributePowers.PreparedOffense prepared;
        private final FoodWeaponFinalDamage.OffenseReceipt receipt;
        private final float amount;
        private final List<Power> powers;
        private final List<Power> cooldownPowers;
        private final boolean bursts;
        private final Thread thread;
        private final MinecraftServer server;
        private final CompoundTag publishedCooldowns;
        private final CompoundTag publishedCooldownState;
        private final FoodSocketAttributePowers.ProcessState publishedProcess;
        private final int publishedHits;
        private final long publishedLastHit;
        private final Key echoKey;
        private final FoodSocketAttributePowers.PendingEcho echoValue;
        private boolean executed;
        private FoodWeaponConsumerSequence.OriginalChild originalChild;

        private OffenseCommit(
            FoodSocketAttributePowers.PreparedOffense var1,
            FoodWeaponFinalDamage.OffenseReceipt var2,
            float var3,
            List<Power> var4,
            List<Power> var5,
            boolean var6,
            CompoundTag var7,
            FoodSocketAttributePowers.ProcessState var8,
            Key var9,
            FoodSocketAttributePowers.PendingEcho var10
        ) {
            this.prepared = var1;
            this.receipt = var2;
            this.amount = var3;
            this.powers = List.copyOf(var4);
            this.cooldownPowers = List.copyOf(var5);
            this.bursts = var6;
            this.thread = Thread.currentThread();
            this.server = var1.quote.player.serverLevel().getServer();
            this.publishedCooldowns = var7;
            this.publishedCooldownState = var7 == null ? null : var7.copy();
            this.publishedProcess = var8;
            this.publishedHits = var8 == null ? 0 : var8.hits;
            this.publishedLastHit = var8 == null ? 0L : var8.lastHit;
            this.echoKey = var9;
            this.echoValue = var10;
        }
    }

    public static final class OffensePlan {
        private final ServerPlayer player;
        private final LivingEntity target;
        private final DamageSource source;
        private final FoodWeaponBondHits.Hit hit;
        private final Resolution recipe;
        private final float amount;
        private final long quotedAt;
        private final List<FoodWeaponSourceLedger.AttemptRequest> attempts;
        private final FoodSocketAttributePowers.ProcessState process;
        private final Tag cooldowns;
        private final List<Entry<FoodSocketAttributePowers.PendingEcho>> echoes;
        private final ItemStack actualWeapon;
        private final InteractionHand hand;
        private final int processHits;
        private final long processLastHit;
        private boolean committed;

        private OffensePlan(
            ServerPlayer var1,
            LivingEntity var2,
            DamageSource var3,
            FoodWeaponBondHits.Hit var4,
            Resolution var5,
            float var6,
            long var7,
            List<FoodWeaponSourceLedger.AttemptRequest> var9
        ) {
            this.player = var1;
            this.target = var2;
            this.source = var3;
            this.hit = var4;
            this.recipe = var5;
            this.amount = var6;
            this.quotedAt = var7;
            this.attempts = List.copyOf(var9);
            this.hand = InteractionHand.valueOf(var4.snapshot().getString("OriginalHand"));
            this.actualWeapon = var4.kind() == net.farmingtales.ftboss.util.FoodWeaponBondRules.Kind.TRIDENT
                && "NATIVE_PROJECTILE".equals(var4.snapshot().getString("DamagePurpose"))
                && "TRIDENT".equals(var4.snapshot().getString("NativeEntry"))
                ? FoodWeaponBondHits.equipped(var1, var4) : var1.getItemInHand(this.hand);
            Tag var10 = var1.getPersistentData().get("FTBossFoodSocketAttributeCooldowns");
            this.cooldowns = var10 == null ? null : var10.copy();
            this.echoes = FoodSocketAttributePowers.ECHOES.entries(var1.getUUID());
            this.process = FoodSocketAttributePowers.PROCESS.get(var1.getUUID());
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
            return this.hit.snapshot().getUUID("WeaponId");
        }

        public CompoundTag snapshot() {
            return this.hit.snapshot();
        }

        public List<FoodWeaponSourceLedger.AttemptRequest> attempts() {
            return this.attempts;
        }
    }

    private static final class PendingEcho {
        private final CompoundTag source;
        private final ItemStack originalWeapon;
        private final UUID target;
        private final long dueAt;
        private final double rangeSquared;
        private FoodWeaponSourceLedger.PendingEchoLineage lineage;

        private PendingEcho(CompoundTag var1, UUID var2, long var3, double var5, ItemStack var7) {
            this.source = var1.copy();
            this.target = var2;
            this.dueAt = var3;
            this.rangeSquared = var5;
            this.originalWeapon = var7;
        }

        public CompoundTag source() {
            return this.source.copy();
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
        private final Entry<FoodSocketAttributePowers.PendingEcho> entry;
        private final Key key;
        private final FoodSocketAttributePowers.PendingEcho pending;
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

        private PendingEchoDispatch(
            Entry<FoodSocketAttributePowers.PendingEcho> var1, CompoundTag var2, ServerPlayer var3, LivingEntity var4, DamageSource var5, float var6, long var7
        ) {
            this.entry = var1;
            this.key = var1.key();
            this.pending = (FoodSocketAttributePowers.PendingEcho)var1.value();
            this.originalWeapon = this.pending.originalWeapon;
            this.paidSource = var2.copy();
            this.player = var3;
            this.target = var4;
            this.damageSource = var5;
            this.amount = var6;
            this.dueAt = this.pending.dueAt();
            this.rangeSquared = this.pending.rangeSquared();
            this.dispatchedAt = var7;
            this.thread = Thread.currentThread();
            this.server = var3.serverLevel().getServer();
            this.active = true;
        }
    }

    static final class PreparedOffense {
        private final FoodSocketAttributePowers.OffensePlan quote;
        private final FoodSocketAttributePowers.CompleteBatchBinding binding;
        private FoodWeaponFinalDamage.PublishingCleanup publishingCleanup;
        private FoodWeaponFinalDamage.PublishingCleanup finalizingCleanup;
        private FoodWeaponConsumerSequence.Accepted originalSequence;
        private FoodWeaponFinalDamage.OffenseReceipt publicationReceipt;
        private int publicationAmountBits;
        private boolean publishAttempted;
        private boolean publishExited;
        private boolean commitConstructed;
        private Key echoKey;
        private FoodSocketAttributePowers.PendingEcho echoValue;
        private boolean echoOfferEntered;
        private boolean echoOfferCompleted;
        private boolean echoOffered;
        private boolean echoRetirementStarted;
        private boolean echoRetirementCompleted;
        private boolean echoRetired;
        private boolean echoDisplaced;
        private Throwable publishFailure;
        private Throwable echoRetirementFailure;

        private PreparedOffense(FoodSocketAttributePowers.OffensePlan var1, FoodSocketAttributePowers.CompleteBatchBinding var2) {
            this.quote = var1;
            this.binding = var2;
        }
    }

    private static final class ProcessState {
        private final UUID weaponId;
        private final CompoundTag fingerprint;
        private final ResourceKey<Level> dimension;
        private int hits;
        private long lastHit;

        private ProcessState(ServerPlayer var1, FoodWeaponBondHits.Hit var2) {
            this.weaponId = var2.snapshot().getUUID("WeaponId");
            this.fingerprint = FoodWeaponBondHits.fingerprint(var2.weapon());
            this.dimension = var1.level().dimension();
        }
    }
}
