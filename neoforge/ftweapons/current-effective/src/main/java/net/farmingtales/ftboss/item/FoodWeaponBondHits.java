package net.farmingtales.ftboss.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.transaction.FoundationApprovedRules;
import net.farmingtales.ftboss.util.FoodWeaponBondFingerprint;
import net.farmingtales.ftboss.util.FoodWeaponBondRules;
import net.farmingtales.ftboss.util.FoodWeaponBondRules.Kind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class FoodWeaponBondHits {
    private static final Map<UUID, FoodWeaponBondHits.Melee> MELEE = new HashMap<>();

    private FoodWeaponBondHits() {
    }

    static CompoundTag originalContextFingerprint(FoodWeaponBondHits.Context var0) {
        return var0 == null ? null : var0.fingerprint;
    }

    public static FoodWeaponBondHits.Context quoteHitContext(LivingEntity var0, DamageSource var1) {
        return quoteContext(var0, var1, FoodWeaponBondHits.Phase.HIT, null);
    }

    public static FoodWeaponBondHits.Context quoteDeathContext(LivingDeathEvent var0) {
        return var0 != null && !var0.isCanceled() ? quoteContext(var0.getEntity(), var0.getSource(), FoodWeaponBondHits.Phase.DEATH, var0) : null;
    }

    private static FoodWeaponBondHits.Context quoteContext(LivingEntity var0, DamageSource var1, FoodWeaponBondHits.Phase var2, LivingDeathEvent var3) {
        if (var0 != null
            && var1 != null
            && !secondary(var1)
            && var1.getEntity() instanceof ServerPlayer var4
            && var4.isAlive()
            && !var4.isSpectator()
            && var4.serverLevel().getServer().isSameThread()
            && hostile(var4, var0)
            && phaseTarget(var0, var2)
            && (var2 != FoodWeaponBondHits.Phase.DEATH || var3 != null && !var3.isCanceled() && !LemonSummonRewards.suppressed(var0))) {
            FoodWeaponBondHits.Hit var7 = resolve(var1, var4);
            if (var7 != null && !equipped(var4, var7).isEmpty()) {
                FoodWeaponBondHits.Context var6 = new FoodWeaponBondHits.Context(var4, var0, var1, var7, var2, var3);
                return matchesContext(var6) ? var6 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static boolean phaseTarget(LivingEntity var0, FoodWeaponBondHits.Phase var1) {
        return var1 == FoodWeaponBondHits.Phase.HIT
            ? var0.isAlive()
            : var1 == FoodWeaponBondHits.Phase.DEATH && var0.isDeadOrDying() && var0.getHealth() <= 0.0F;
    }

    public static boolean matchesContext(FoodWeaponBondHits.Context var0) {
        if (var0 != null
            && var0.server == var0.player.serverLevel().getServer()
            && var0.server.isSameThread()
            && Thread.currentThread() == var0.thread
            && var0.player.level() == var0.level
            && var0.target.level() == var0.level
            && var0.targetId.equals(var0.target.getUUID())
            && clock(var0.player) == var0.tick
            && var0.player.isAlive()
            && !var0.player.isSpectator()
            && !secondary(var0.source)
            && hostile(var0.player, var0.target)
            && phaseTarget(var0.target, var0.phase)
            && var0.player.getItemInHand(var0.hand) == var0.actualWeapon
            && !equipped(var0.player, var0.hit).isEmpty()
            && fingerprint(var0.actualWeapon).equals(var0.fingerprint)
            && !var0.materials.isEmpty()
            && FoodWeaponSourceLedger.materialFingerprint(var0.actualWeapon).equals(var0.materials)
            && (
                var0.phase != FoodWeaponBondHits.Phase.DEATH
                    || var0.death != null
                        && !var0.death.isCanceled()
                        && var0.death.getEntity() == var0.target
                        && var0.death.getSource() == var0.source
                        && !LemonSummonRewards.suppressed(var0.target)
            )) {
            FoodWeaponBondHits.Hit var1 = resolve(var0.source, var0.player);
            return var1 != null && var1.attack().equals(var0.hit.attack()) && var1.kind() == var0.hit.kind() && var1.snapshot().equals(var0.hit.snapshot());
        } else {
            return false;
        }
    }

    private static boolean phaseDomain(FoodWeaponBondHits.Phase var0, FoodWeaponSourceLedger.EffectDomain var1) {
        if (var0 != null && var1 != null) {
            return switch (var1) {
                case SERIES_ECHO, COLOR_REPEL, PROCESS_BURST, WOLF_PACK, PASSIVE_HIT -> var0 == FoodWeaponBondHits.Phase.HIT;
                case PASSIVE_KILL, MATERIAL_KILL, KILL_FEAST, SOCKET_KILL -> var0 == FoodWeaponBondHits.Phase.DEATH;
            };
        } else {
            return false;
        }
    }

    public static Optional<FoodWeaponSourceLedger.AttemptRequest> quoteAttempt(FoodWeaponBondHits.Context var0, FoodWeaponSourceLedger.EffectDomain var1) {
        if (matchesContext(var0) && phaseDomain(var0.phase, var1)) {
            FoodWeaponSourceLedger.AttemptDomain var2 = FoodWeaponSourceLedger.AttemptDomain.valueOf(var1.name());
            return FoodWeaponSourceLedger.quoteAttempt(var0.snapshot(), var0.player, var2, 1.0).status() == FoodWeaponSourceLedger.AttemptStatus.AVAILABLE
                ? Optional.of(new FoodWeaponSourceLedger.AttemptRequest(var2, 1.0))
                : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    public static boolean matchesBatch(
        FoodWeaponBondHits.Context var0,
        FoodWeaponSourceLedger.EffectDomain var1,
        FoodWeaponSourceLedger.AttemptBatchQuote var2,
        FoodWeaponSourceLedger.AttemptBatchAdmission var3,
        List<FoodWeaponSourceLedger.AttemptRequest> var4,
        List<FoodWeaponSourceLedger.TargetRequest> var5
    ) {
        if (matchesContext(var0)
            && phaseDomain(var0.phase, var1)
            && var2 != null
            && var3 != null
            && var3.status() == FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY
            && FoodWeaponSourceLedger.matchesAttemptRequests(var2, var4, var5)
            && FoodWeaponSourceLedger.matchesAttemptAdmission(var0.snapshot(), var0.player, var2, var3, var0.tick)) {
            FoodWeaponSourceLedger.AttemptDomain var6 = FoodWeaponSourceLedger.AttemptDomain.valueOf(var1.name());
            int var7 = 0;

            for (FoodWeaponSourceLedger.AttemptRequest var9 : var4) {
                if (var9.domain() == var6) {
                    if (++var7 != 1 || Double.compare(var9.chance(), 1.0) != 0) {
                        return false;
                    }
                }
            }

            if (var7 != 1) {
                return false;
            } else {
                for (FoodWeaponSourceLedger.AttemptQuote var11 : var2.entries()) {
                    if (var11.domain() == var6) {
                        return var11.status() == FoodWeaponSourceLedger.AttemptStatus.AVAILABLE && Double.compare(var11.chance(), 1.0) == 0 && var11.success();
                    }
                }

                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean matchesHitReceipt(
        FoodWeaponBondHits.Context var0, Object var1, FoodWeaponSourceLedger.EffectDomain var2, float var3, FoodWeaponFinalDamage.OffenseReceipt var4
    ) {
        if (matchesContext(var0)
            && var0.phase == FoodWeaponBondHits.Phase.HIT
            && phaseDomain(var0.phase, var2)
            && var1 != null
            && var4 != null
            && Float.isFinite(var3)
            && !(var3 <= 0.0F)
            && var4.matches(var1, var0.player, var0.source, var0.tick, var3)) {
            FoodWeaponSourceLedger.AttemptDomain var5 = FoodWeaponSourceLedger.AttemptDomain.valueOf(var2.name());
            return var4.attempt(var5)
                .filter(var1x -> var1x.domain() == var5 && var1x.commit() == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS)
                .isPresent();
        } else {
            return false;
        }
    }

    private static boolean foundationHit(DamageSource var0, ServerPlayer var1, CompoundTag var2, ItemStack var3) {
        if (FoodWeaponSourceLedger.resolveReadOnly(var0, var1).equals(var2)
            && FoodWeaponSourceLedger.validReadOnly(var2, var1)
            && FoodWeaponSourceLedger.originalHandContinuousReadOnly(var2, var1)
            && var2.contains("FoundationSnapshot", 10)
            && var2.contains("FoundationApprovedRules", 8)) {
            FourWeaponFoundationAdapter.View var4 = FourWeaponFoundationAdapter.read(var3);
            if (var4.valid()
                && var4.profile() != null
                && var4.status() != null
                && var4.status().ready()
                && FourWeaponFoundationAdapter.matchesType(var3, var4.profile().family())
                && !FoodWeaponLifecycle.isBroken(var3)) {
                CompoundTag var5 = var2.getCompound("FoundationSnapshot");
                CompoundTag var6 = FoodWeaponMaterialFingerprint.foundationSnapshot(var3);
                if (!var6.isEmpty() && var6.equals(var5)) {
                    try {
                        String var7 = new FoundationApprovedRules().fingerprint(var1, var3);
                        return var7 != null && !var7.isEmpty() && var7.equals(var2.getString("FoundationApprovedRules"));
                    } catch (LinkageError | IllegalArgumentException var8) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean secondary(DamageSource var0) {
        return FoodWeaponDamageSources.isAttributeSecondary(var0) || !FoodWeaponRanged.allowsWeaponEffects(var0);
    }

    public static FoodWeaponBondHits.Hit resolve(DamageSource var0, ServerPlayer var1) {
        if (var0 != null && var1 != null && !secondary(var0) && var0.getEntity() == var1 && var1.isAlive() && !var1.isSpectator()) {
            CompoundTag var2 = FoodWeaponSourceLedger.resolveReadOnly(var0, var1);
            boolean var3 = var0.getDirectEntity() == var1
                && var0.is(FoodWeaponDamageSources.PRIMARY)
                && "GENERIC_J".equals(var2.getString("DamagePurpose"))
                && FoodWeaponSourceLedger.validReadOnly(var2, var1)
                && FoodWeaponSourceLedger.originalHandContinuousReadOnly(var2, var1);
            boolean var4 = !var3 && var0.getDirectEntity() == var1 && var0.is(DamageTypes.PLAYER_ATTACK);
            CompoundTag var5;
            if (var4 && !(FoodWeaponSourceLedger.actualHurtSnapshot(var2) && "NATIVE_MELEE_ACTUAL_HURT".equals(var2.getString("DamagePurpose")))) {
                FoodWeaponBondHits.Melee var6 = MELEE.get(var1.getUUID());
                if (var6 == null || var6.at() != clock(var1)) {
                    return null;
                }

                var5 = var6.snapshot().copy();
            } else {
                var5 = var2.copy();
            }

            if (!FoodWeaponSourceLedger.validReadOnly(var5, var1)) {
                return null;
            } else {
                ItemStack var11 = ItemStack.parseOptional(var1.registryAccess(), var5.getCompound("Weapon"));
                boolean var7 = FourWeaponFoundationAdapter.applies(var11);
                if (var7 && !foundationHit(var0, var1, var5, var11)) {
                    return null;
                } else if (!var11.isEmpty()
                    && var11.getCount() == 1
                    && (var7 || FoodWeaponEvolution.isEvolutionCandidate(var11))
                    && FoodWeaponFamilyBuild.kind(var11).name().equals(var5.getString("Family"))) {
                    boolean var10000;
                    label97: {
                        if (var0.getDirectEntity() instanceof ThrownTrident var9 && var9.getOwner() == var1 && FoodWeaponSourceLedger.isTrident(var11)) {
                            var10000 = true;
                            break label97;
                        }

                        var10000 = false;
                    }

                    boolean var8 = var10000;
                    Kind var12 = var3
                        ? (FoodWeaponSourceLedger.isTrident(var11) ? Kind.TRIDENT : Kind.valueOf(var5.getString("Family")))
                        : FoodWeaponBondRules.hitKind(
                            var5.getString("Family"),
                            var5.getString("SourceKind"),
                            var4,
                            var8,
                            var0.is(DamageTypeTags.IS_PROJECTILE),
                            var5.getBoolean("TridentLaunchAuthorized")
                        );
                    if (var12 != null && var5.getUUID("WeaponId").equals(FoodWeaponLifecycle.weaponId(var11))) {
                        if (var12 == Kind.TRIDENT) {
                            if (FoodWeaponSourceLedger.boundProfile(var11).isEmpty() && !var7) {
                                return null;
                            }
                        } else if (FoodWeaponLifecycle.isBroken(var11)) {
                            return null;
                        }

                        if (!var3 && !var4 && var12 != Kind.TRIDENT && !(var0.getDirectEntity() instanceof Projectile) && var12 != Kind.STAFF) {
                            return null;
                        } else {
                            return var12 == Kind.STAFF && !var0.is(DamageTypeTags.IS_PROJECTILE) && !magicSource(var0)
                                ? null
                                : new FoodWeaponBondHits.Hit(var11, var12, var5.getUUID("AttackId"), var5);
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    private static boolean magicSource(DamageSource var0) {
        if (!var0.is(DamageTypes.MAGIC) && !var0.is(DamageTypes.INDIRECT_MAGIC)) {
            for (Class var1 = var0.getClass(); var1 != null; var1 = var1.getSuperclass()) {
                if (var1.getName().equals("io.redspace.ironsspellbooks.damage.SpellDamageSource")) {
                    return true;
                }
            }

            return false;
        } else {
            return true;
        }
    }

    public static boolean claim(ServerPlayer var0, FoodWeaponBondHits.Hit var1, LivingEntity var2, FoodWeaponSourceLedger.EffectDomain var3) {
        return var1 != null && var2 != null && var3 != null && FoodWeaponSourceLedger.claim(var1.snapshot(), var0, var2.getUUID(), var3, false, clock(var0));
    }

    public static ItemStack equipped(ServerPlayer var0, FoodWeaponBondHits.Hit var1) {
        if (var1 != null && "NATIVE_PROJECTILE".equals(var1.snapshot().getString("DamagePurpose"))
            && "TRIDENT".equals(var1.snapshot().getString("NativeEntry"))) return FoodWeaponSourceLedger.nativeTridentImpactWeapon(var1.snapshot(), var0);
        if (var1 != null && FoodWeaponSourceLedger.originalHandContinuousReadOnly(var1.snapshot(), var0)) {
            String var2 = var1.snapshot().getString("OriginalHand");
            ItemStack var3 = var2.equals(InteractionHand.MAIN_HAND.name())
                ? var0.getMainHandItem()
                : (var2.equals(InteractionHand.OFF_HAND.name()) ? var0.getOffhandItem() : ItemStack.EMPTY);
            UUID var4 = FoodWeaponLifecycle.weaponId(var3);
            return var3.getCount() == 1
                    && var4 != null
                    && var4.equals(var1.snapshot().getUUID("WeaponId"))
                    && var3.getItem() == var1.weapon().getItem()
                    && fingerprint(var3).equals(fingerprint(var1.weapon()))
                    && !FoodWeaponLifecycle.isBroken(var3)
                ? var3
                : ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static CompoundTag fingerprint(ItemStack var0) {
        return FoodWeaponBondFingerprint.wrap(FoodWeaponSourceLedger.socketFingerprint(var0, FoodWeaponFamilyBuild.kind(var0).name()));
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void attack(AttackEntityEvent var0) {
        if (!var0.isCanceled() && var0.getEntity() instanceof ServerPlayer var1 && var1.isAlive() && !var1.isSpectator()) {
            MELEE.remove(var1.getUUID());
            ItemStack var4 = var1.getMainHandItem();
            if (var4.getCount() == 1 && FoodWeaponEvolution.isEvolutionCandidate(var4) && !FoodWeaponLifecycle.isBroken(var4)) {
                FoodWeaponLifecycle.ensureTracked(var4);
                CompoundTag var3 = new CompoundTag();
                var3.putUUID("Owner", var1.getUUID());
                var3.putString("Family", FoodWeaponFamilyBuild.kind(var4).name());
                var3.putString("SourceKind", "MELEE");
                var3 = FoodWeaponSourceLedger.decorate(var1, var4, var3, InteractionHand.MAIN_HAND);
                if (FoodWeaponSourceLedger.valid(var3, var1)) {
                    MELEE.put(var1.getUUID(), new FoodWeaponBondHits.Melee(var3.copy(), clock(var1)));
                }
            }
        }
    }

    public static boolean hostile(ServerPlayer var0, LivingEntity var1) {
        return var1 instanceof Monster
            && !var1.isRemoved()
            && var1.level() == var0.level()
            && !var1.isAlliedTo(var0)
            && !var0.isAlliedTo(var1)
            && (!(var1 instanceof TamableAnimal var3) || !var3.isTame())
            && (!(var1 instanceof OwnableEntity var2) || var2.getOwnerUUID() == null);
    }

    public static ItemStack preferred(ServerPlayer var0, Predicate<ItemStack> var1) {
        ItemStack var2 = var0.getMainHandItem();
        if (var2.getCount() == 1 && !FoodWeaponLifecycle.isBroken(var2) && var1.test(var2)) {
            return var2;
        } else {
            ItemStack var3 = var0.getOffhandItem();
            return var3.getCount() == 1
                    && (
                        FoodWeaponFamilyBuild.kind(var3) != net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind.MELEE
                            || var3.getItem() instanceof AxeItem
                            || FoodWeaponSourceLedger.isTrident(var3)
                    )
                    && !FoodWeaponLifecycle.isBroken(var3)
                    && var1.test(var3)
                ? var3
                : ItemStack.EMPTY;
        }
    }

    public static boolean bind(ServerPlayer var0, ItemStack var1, CompoundTag var2, String var3) {
        FoodWeaponLifecycle.ensureTracked(var1);
        CompoundTag var4 = new CompoundTag();
        var4.putString("Family", FoodWeaponFamilyBuild.kind(var1).name());
        var4 = FoodWeaponSourceLedger.decorate(var0, var1, var4, var0.getMainHandItem() == var1 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        return saveBinding(var0, var1, var2, var3, var4);
    }

    public static boolean bind(ServerPlayer var0, ItemStack var1, CompoundTag var2, String var3, FoodWeaponBondHits.Hit var4) {
        return var4 != null && saveBinding(var0, var1, var2, var3, var4.snapshot());
    }

    private static boolean saveBinding(ServerPlayer var0, ItemStack var1, CompoundTag var2, String var3, CompoundTag var4) {
        if (var1.getCount() == 1 && FoodWeaponSourceLedger.originalHandContinuous(var4, var0)) {
            var2.putUUID(var3 + "WeaponId", var4.getUUID("WeaponId"));
            var2.putString(var3 + "Hand", var4.getString("OriginalHand"));
            var2.putString(var3 + "Dimension", var4.getString("Dimension"));
            var2.put(var3 + "Fingerprint", fingerprint(var1));
            var2.put(var3 + "Source", var4.copy());
            return true;
        } else {
            return false;
        }
    }

    public static boolean bound(ServerPlayer var0, ItemStack var1, CompoundTag var2, String var3) {
        if (var2.contains(var3 + "Source", 10) && FoodWeaponSourceLedger.originalHandContinuous(var2.getCompound(var3 + "Source"), var0)) {
            String var4 = var2.getString(var3 + "Hand");
            ItemStack var5 = var4.equals("MAIN_HAND") ? var0.getMainHandItem() : (var4.equals("OFF_HAND") ? var0.getOffhandItem() : ItemStack.EMPTY);
            return var5.getItem() == var1.getItem()
                && var5.getCount() == 1
                && !FoodWeaponLifecycle.isBroken(var5)
                && var2.hasUUID(var3 + "WeaponId")
                && var2.getUUID(var3 + "WeaponId").equals(FoodWeaponLifecycle.weaponId(var1))
                && var2.getString(var3 + "Dimension").equals(var0.level().dimension().location().toString())
                && var2.contains(var3 + "Fingerprint", 10)
                && var2.getCompound(var3 + "Fingerprint").equals(fingerprint(var1));
        } else {
            return false;
        }
    }

    public static long clock(ServerPlayer var0) {
        return FoodWeaponSourceLedger.clock(var0);
    }

    @SubscribeEvent
    public static void logout(PlayerLoggedOutEvent var0) {
        clear(var0.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void login(PlayerLoggedInEvent var0) {
        clear(var0.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void clonePlayer(Clone var0) {
        clear(var0.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void dimension(PlayerChangedDimensionEvent var0) {
        clear(var0.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent var0) {
        MELEE.clear();
    }

    private static void clear(UUID var0) {
        MELEE.remove(var0);
    }

    public static final class Context {
        private final ServerPlayer player;
        private final LivingEntity target;
        private final DamageSource source;
        private final FoodWeaponBondHits.Hit hit;
        private final FoodWeaponBondHits.Phase phase;
        private final LivingDeathEvent death;
        private final MinecraftServer server;
        private final Level level;
        private final Thread thread;
        private final InteractionHand hand;
        private final ItemStack actualWeapon;
        private final CompoundTag fingerprint;
        private final String materials;
        private final long tick;
        private final UUID targetId;

        private Context(
            ServerPlayer var1, LivingEntity var2, DamageSource var3, FoodWeaponBondHits.Hit var4, FoodWeaponBondHits.Phase var5, LivingDeathEvent var6
        ) {
            this.player = var1;
            this.target = var2;
            this.source = var3;
            this.hit = var4;
            this.phase = var5;
            this.death = var6;
            this.server = var1.serverLevel().getServer();
            this.level = var1.level();
            this.thread = Thread.currentThread();
            this.hand = InteractionHand.valueOf(var4.snapshot().getString("OriginalHand"));
            this.actualWeapon = var1.getItemInHand(this.hand);
            this.fingerprint = FoodWeaponBondHits.fingerprint(this.actualWeapon).copy();
            this.materials = FoodWeaponSourceLedger.materialFingerprint(this.actualWeapon);
            this.tick = FoodWeaponBondHits.clock(var1);
            this.targetId = var2.getUUID();
        }

        public ServerPlayer player() {
            return this.player;
        }

        public LivingEntity target() {
            return this.target;
        }

        public DamageSource source() {
            return this.source;
        }

        public ItemStack weapon() {
            return this.hit.weapon();
        }

        public ItemStack actualWeapon() {
            return this.actualWeapon;
        }

        public InteractionHand originalHand() {
            return this.hand;
        }

        public long quotedAt() {
            return this.tick;
        }

        public UUID targetId() {
            return this.targetId;
        }

        public UUID attackId() {
            return this.hit.attack();
        }

        public CompoundTag snapshot() {
            return this.hit.snapshot();
        }
    }

    public static record Hit(ItemStack weapon, Kind kind, UUID attack, CompoundTag snapshot) {
        public Hit(ItemStack weapon, Kind kind, UUID attack, CompoundTag snapshot) {
            weapon = weapon.copy();
            snapshot = snapshot.copy();
            this.weapon = weapon;
            this.kind = kind;
            this.attack = attack;
            this.snapshot = snapshot;
        }

        public ItemStack weapon() {
            return this.weapon.copy();
        }

        public CompoundTag snapshot() {
            return this.snapshot.copy();
        }
    }

    private static record Melee(CompoundTag snapshot, long at) {
    }

    private static enum Phase {
        HIT,
        DEATH;
    }
}
