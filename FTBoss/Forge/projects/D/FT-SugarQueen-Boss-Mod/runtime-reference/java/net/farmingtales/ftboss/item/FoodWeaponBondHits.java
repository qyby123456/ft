/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.OwnableEntity
 *  net.minecraft.world.entity.TamableAnimal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.ThrownTrident
 *  net.minecraft.world.item.AxeItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.player.AttackEntityEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$Clone
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.item.FoodWeaponDamageSources;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponFinalDamage;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponMaterialFingerprint;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.transaction.FoundationApprovedRules;
import net.farmingtales.ftboss.util.FoodWeaponBondFingerprint;
import net.farmingtales.ftboss.util.FoodWeaponBondRules;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class FoodWeaponBondHits {
    private static final Map<UUID, Melee> MELEE = new HashMap<UUID, Melee>();

    private FoodWeaponBondHits() {
    }

    static CompoundTag originalContextFingerprint(Context context) {
        return context == null ? null : context.fingerprint;
    }

    public static Context quoteHitContext(LivingEntity livingEntity, DamageSource damageSource) {
        return FoodWeaponBondHits.quoteContext(livingEntity, damageSource, Phase.HIT, null);
    }

    public static Context quoteDeathContext(LivingDeathEvent livingDeathEvent) {
        return livingDeathEvent == null || livingDeathEvent.isCanceled() ? null : FoodWeaponBondHits.quoteContext(livingDeathEvent.getEntity(), livingDeathEvent.getSource(), Phase.DEATH, livingDeathEvent);
    }

    private static Context quoteContext(LivingEntity livingEntity, DamageSource damageSource, Phase phase, LivingDeathEvent livingDeathEvent) {
        ServerPlayer serverPlayer;
        Object object;
        if (livingEntity == null || damageSource == null || FoodWeaponBondHits.secondary(damageSource) || !((object = damageSource.m_7639_()) instanceof ServerPlayer) || !(serverPlayer = (ServerPlayer)object).m_6084_() || serverPlayer.m_5833_() || !serverPlayer.m_284548_().m_7654_().m_18695_() || !FoodWeaponBondHits.hostile(serverPlayer, livingEntity) || !FoodWeaponBondHits.phaseTarget(livingEntity, phase) || phase == Phase.DEATH && (livingDeathEvent == null || livingDeathEvent.isCanceled() || LemonSummonRewards.suppressed((Entity)livingEntity))) {
            return null;
        }
        object = FoodWeaponBondHits.resolve(damageSource, serverPlayer);
        if (object == null || FoodWeaponBondHits.equipped(serverPlayer, (Hit)object).m_41619_()) {
            return null;
        }
        Context context = new Context(serverPlayer, livingEntity, damageSource, (Hit)object, phase, livingDeathEvent);
        return FoodWeaponBondHits.matchesContext(context) ? context : null;
    }

    private static boolean phaseTarget(LivingEntity livingEntity, Phase phase) {
        return phase == Phase.HIT ? livingEntity.m_6084_() : phase == Phase.DEATH && livingEntity.m_21224_() && livingEntity.m_21223_() <= 0.0f;
    }

    public static boolean matchesContext(Context context) {
        if (context == null || context.server != context.player.m_284548_().m_7654_() || !context.server.m_18695_() || Thread.currentThread() != context.thread || context.player.m_9236_() != context.level || context.target.m_9236_() != context.level || !context.targetId.equals(context.target.m_20148_()) || FoodWeaponBondHits.clock(context.player) != context.tick || !context.player.m_6084_() || context.player.m_5833_() || FoodWeaponBondHits.secondary(context.source) || !FoodWeaponBondHits.hostile(context.player, context.target) || !FoodWeaponBondHits.phaseTarget(context.target, context.phase) || context.player.m_21120_(context.hand) != context.actualWeapon || FoodWeaponBondHits.equipped(context.player, context.hit).m_41619_() || !FoodWeaponBondHits.fingerprint(context.actualWeapon).equals((Object)context.fingerprint) || context.materials.isEmpty() || !FoodWeaponSourceLedger.materialFingerprint(context.actualWeapon).equals(context.materials) || context.phase == Phase.DEATH && (context.death == null || context.death.isCanceled() || context.death.getEntity() != context.target || context.death.getSource() != context.source || LemonSummonRewards.suppressed((Entity)context.target))) {
            return false;
        }
        Hit hit = FoodWeaponBondHits.resolve(context.source, context.player);
        return hit != null && hit.attack().equals(context.hit.attack()) && hit.kind() == context.hit.kind() && hit.snapshot().equals((Object)context.hit.snapshot());
    }

    private static boolean phaseDomain(Phase phase, FoodWeaponSourceLedger.EffectDomain effectDomain) {
        if (phase == null || effectDomain == null) {
            return false;
        }
        return switch (effectDomain) {
            default -> throw new IncompatibleClassChangeError();
            case FoodWeaponSourceLedger.EffectDomain.SERIES_ECHO, FoodWeaponSourceLedger.EffectDomain.COLOR_REPEL, FoodWeaponSourceLedger.EffectDomain.PROCESS_BURST, FoodWeaponSourceLedger.EffectDomain.WOLF_PACK, FoodWeaponSourceLedger.EffectDomain.PASSIVE_HIT -> {
                if (phase == Phase.HIT) {
                    yield true;
                }
                yield false;
            }
            case FoodWeaponSourceLedger.EffectDomain.PASSIVE_KILL, FoodWeaponSourceLedger.EffectDomain.MATERIAL_KILL, FoodWeaponSourceLedger.EffectDomain.KILL_FEAST, FoodWeaponSourceLedger.EffectDomain.SOCKET_KILL -> phase == Phase.DEATH;
        };
    }

    public static Optional<FoodWeaponSourceLedger.AttemptRequest> quoteAttempt(Context context, FoodWeaponSourceLedger.EffectDomain effectDomain) {
        if (!FoodWeaponBondHits.matchesContext(context) || !FoodWeaponBondHits.phaseDomain(context.phase, effectDomain)) {
            return Optional.empty();
        }
        FoodWeaponSourceLedger.AttemptDomain attemptDomain = FoodWeaponSourceLedger.AttemptDomain.valueOf(effectDomain.name());
        return FoodWeaponSourceLedger.quoteAttempt(context.snapshot(), context.player, attemptDomain, 1.0).status() == FoodWeaponSourceLedger.AttemptStatus.AVAILABLE ? Optional.of(new FoodWeaponSourceLedger.AttemptRequest(attemptDomain, 1.0)) : Optional.empty();
    }

    public static boolean matchesBatch(Context context, FoodWeaponSourceLedger.EffectDomain effectDomain, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        if (!(FoodWeaponBondHits.matchesContext(context) && FoodWeaponBondHits.phaseDomain(context.phase, effectDomain) && attemptBatchQuote != null && attemptBatchAdmission != null && attemptBatchAdmission.status() == FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY && FoodWeaponSourceLedger.matchesAttemptRequests(attemptBatchQuote, list, list2) && FoodWeaponSourceLedger.matchesAttemptAdmission(context.snapshot(), context.player, attemptBatchQuote, attemptBatchAdmission, context.tick))) {
            return false;
        }
        FoodWeaponSourceLedger.AttemptDomain attemptDomain = FoodWeaponSourceLedger.AttemptDomain.valueOf(effectDomain.name());
        int n = 0;
        for (FoodWeaponSourceLedger.AttemptRequest object : list) {
            if (object.domain() != attemptDomain || ++n == 1 && Double.compare(object.chance(), 1.0) == 0) continue;
            return false;
        }
        if (n != 1) {
            return false;
        }
        for (FoodWeaponSourceLedger.AttemptQuote attemptQuote : attemptBatchQuote.entries()) {
            if (attemptQuote.domain() != attemptDomain) continue;
            return attemptQuote.status() == FoodWeaponSourceLedger.AttemptStatus.AVAILABLE && Double.compare(attemptQuote.chance(), 1.0) == 0 && attemptQuote.success();
        }
        return false;
    }

    public static boolean matchesHitReceipt(Context context, Object object, FoodWeaponSourceLedger.EffectDomain effectDomain, float f, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt) {
        if (!(FoodWeaponBondHits.matchesContext(context) && context.phase == Phase.HIT && FoodWeaponBondHits.phaseDomain(context.phase, effectDomain) && object != null && offenseReceipt != null && Float.isFinite(f) && !(f <= 0.0f) && offenseReceipt.matches(object, context.player, context.source, context.tick, f))) {
            return false;
        }
        FoodWeaponSourceLedger.AttemptDomain attemptDomain = FoodWeaponSourceLedger.AttemptDomain.valueOf(effectDomain.name());
        return offenseReceipt.attempt(attemptDomain).filter(attemptResult -> attemptResult.domain() == attemptDomain && attemptResult.commit() == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS).isPresent();
    }

    private static boolean foundationHit(DamageSource damageSource, ServerPlayer serverPlayer, CompoundTag compoundTag, ItemStack itemStack) {
        if (!(FoodWeaponSourceLedger.resolveReadOnly(damageSource, serverPlayer).equals((Object)compoundTag) && FoodWeaponSourceLedger.validReadOnly(compoundTag, serverPlayer) && FoodWeaponSourceLedger.originalHandContinuousReadOnly(compoundTag, serverPlayer) && compoundTag.m_128425_("FoundationSnapshot", 10) && compoundTag.m_128425_("FoundationApprovedRules", 8))) {
            return false;
        }
        FourWeaponFoundationAdapter.View view = FourWeaponFoundationAdapter.read(itemStack);
        if (!view.valid() || view.profile() == null || view.status() == null || !view.status().ready() || !FourWeaponFoundationAdapter.matchesType(itemStack, view.profile().family()) || FoodWeaponLifecycle.isBroken(itemStack)) {
            return false;
        }
        CompoundTag compoundTag2 = compoundTag.m_128469_("FoundationSnapshot");
        CompoundTag compoundTag3 = FoodWeaponMaterialFingerprint.foundationSnapshot(itemStack);
        if (compoundTag3.m_128456_() || !compoundTag3.equals((Object)compoundTag2)) {
            return false;
        }
        try {
            String string = new FoundationApprovedRules().fingerprint(serverPlayer, itemStack);
            return string != null && !string.isEmpty() && string.equals(compoundTag.m_128461_("FoundationApprovedRules"));
        }
        catch (IllegalArgumentException | LinkageError throwable) {
            return false;
        }
    }

    public static boolean secondary(DamageSource damageSource) {
        return FoodWeaponDamageSources.isAttributeSecondary(damageSource) || !FoodWeaponRanged.allowsWeaponEffects(damageSource);
    }

    public static Hit resolve(DamageSource damageSource, ServerPlayer serverPlayer) {
        Object object;
        boolean bl;
        CompoundTag compoundTag;
        Melee melee;
        boolean bl2;
        if (damageSource == null || serverPlayer == null || FoodWeaponBondHits.secondary(damageSource) || damageSource.m_7639_() != serverPlayer || !serverPlayer.m_6084_() || serverPlayer.m_5833_()) {
            return null;
        }
        CompoundTag compoundTag2 = FoodWeaponSourceLedger.resolveReadOnly(damageSource, serverPlayer);
        boolean bl3 = damageSource.m_7640_() == serverPlayer && damageSource.m_276093_(FoodWeaponDamageSources.PRIMARY) && "GENERIC_J".equals(compoundTag2.m_128461_("DamagePurpose")) && FoodWeaponSourceLedger.validReadOnly(compoundTag2, serverPlayer) && FoodWeaponSourceLedger.originalHandContinuousReadOnly(compoundTag2, serverPlayer);
        boolean bl4 = bl2 = !bl3 && damageSource.m_7640_() == serverPlayer && damageSource.m_276093_(DamageTypes.f_268464_);
        if (bl2) {
            melee = MELEE.get(serverPlayer.m_20148_());
            if (melee == null || melee.at() != FoodWeaponBondHits.clock(serverPlayer)) {
                return null;
            }
            compoundTag = melee.snapshot().m_6426_();
        } else {
            compoundTag = compoundTag2.m_6426_();
        }
        if (!FoodWeaponSourceLedger.validReadOnly(compoundTag, serverPlayer)) {
            return null;
        }
        melee = ItemStack.m_41712_((CompoundTag)compoundTag.m_128469_("Weapon"));
        boolean bl5 = FourWeaponFoundationAdapter.applies((ItemStack)melee);
        if (bl5 && !FoodWeaponBondHits.foundationHit(damageSource, serverPlayer, compoundTag, (ItemStack)melee)) {
            return null;
        }
        if (melee.m_41619_() || melee.m_41613_() != 1 || !bl5 && !FoodWeaponEvolution.isEvolutionCandidate((ItemStack)melee) || !FoodWeaponFamilyBuild.kind((ItemStack)melee).name().equals(compoundTag.m_128461_("Family"))) {
            return null;
        }
        Entity entity = damageSource.m_7640_();
        boolean bl6 = bl = entity instanceof ThrownTrident && (object = (ThrownTrident)entity).m_19749_() == serverPlayer && FoodWeaponSourceLedger.isTrident((ItemStack)melee);
        FoodWeaponBondRules.Kind kind = bl3 ? (FoodWeaponSourceLedger.isTrident((ItemStack)melee) ? FoodWeaponBondRules.Kind.TRIDENT : FoodWeaponBondRules.Kind.valueOf(compoundTag.m_128461_("Family"))) : (object = FoodWeaponBondRules.hitKind(compoundTag.m_128461_("Family"), compoundTag.m_128461_("SourceKind"), bl2, bl, damageSource.m_269533_(DamageTypeTags.f_268524_), compoundTag.m_128471_("TridentLaunchAuthorized")));
        if (object == null || !compoundTag.m_128342_("WeaponId").equals(FoodWeaponLifecycle.weaponId((ItemStack)melee))) {
            return null;
        }
        if (object == FoodWeaponBondRules.Kind.TRIDENT ? FoodWeaponSourceLedger.boundProfile((ItemStack)melee).isEmpty() && !bl5 : FoodWeaponLifecycle.isBroken((ItemStack)melee)) {
            return null;
        }
        if (!(bl3 || bl2 || object == FoodWeaponBondRules.Kind.TRIDENT || damageSource.m_7640_() instanceof Projectile || object == FoodWeaponBondRules.Kind.STAFF)) {
            return null;
        }
        if (object == FoodWeaponBondRules.Kind.STAFF && !damageSource.m_269533_(DamageTypeTags.f_268524_) && !FoodWeaponBondHits.magicSource(damageSource)) {
            return null;
        }
        return new Hit((ItemStack)melee, (FoodWeaponBondRules.Kind)((Object)object), compoundTag.m_128342_("AttackId"), compoundTag);
    }

    private static boolean magicSource(DamageSource damageSource) {
        if (damageSource.m_276093_(DamageTypes.f_268515_) || damageSource.m_276093_(DamageTypes.f_268530_)) {
            return true;
        }
        for (Class<?> clazz = damageSource.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            if (!clazz.getName().equals("io.redspace.ironsspellbooks.damage.SpellDamageSource")) continue;
            return true;
        }
        return false;
    }

    public static boolean claim(ServerPlayer serverPlayer, Hit hit, LivingEntity livingEntity, FoodWeaponSourceLedger.EffectDomain effectDomain) {
        return hit != null && livingEntity != null && effectDomain != null && FoodWeaponSourceLedger.claim(hit.snapshot(), serverPlayer, livingEntity.m_20148_(), effectDomain, false, FoodWeaponBondHits.clock(serverPlayer));
    }

    public static ItemStack equipped(ServerPlayer serverPlayer, Hit hit) {
        if (hit == null || !FoodWeaponSourceLedger.originalHandContinuousReadOnly(hit.snapshot(), serverPlayer)) {
            return ItemStack.f_41583_;
        }
        String string = hit.snapshot().m_128461_("OriginalHand");
        ItemStack itemStack = string.equals(InteractionHand.MAIN_HAND.name()) ? serverPlayer.m_21205_() : (string.equals(InteractionHand.OFF_HAND.name()) ? serverPlayer.m_21206_() : ItemStack.f_41583_);
        UUID uUID = FoodWeaponLifecycle.weaponId(itemStack);
        return itemStack.m_41613_() == 1 && uUID != null && uUID.equals(hit.snapshot().m_128342_("WeaponId")) && itemStack.m_41720_() == hit.weapon().m_41720_() && FoodWeaponBondHits.fingerprint(itemStack).equals((Object)FoodWeaponBondHits.fingerprint(hit.weapon())) && !FoodWeaponLifecycle.isBroken(itemStack) ? itemStack : ItemStack.f_41583_;
    }

    public static CompoundTag fingerprint(ItemStack itemStack) {
        return FoodWeaponBondFingerprint.wrap(FoodWeaponSourceLedger.socketFingerprint(itemStack, FoodWeaponFamilyBuild.kind(itemStack).name()));
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void attack(AttackEntityEvent attackEntityEvent) {
        ServerPlayer serverPlayer;
        Player player;
        if (attackEntityEvent.isCanceled() || !((player = attackEntityEvent.getEntity()) instanceof ServerPlayer) || !(serverPlayer = (ServerPlayer)player).m_6084_() || serverPlayer.m_5833_()) {
            return;
        }
        MELEE.remove(serverPlayer.m_20148_());
        player = serverPlayer.m_21205_();
        if (player.m_41613_() != 1 || !FoodWeaponEvolution.isEvolutionCandidate((ItemStack)player) || FoodWeaponLifecycle.isBroken((ItemStack)player)) {
            return;
        }
        FoodWeaponLifecycle.ensureTracked((ItemStack)player);
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128362_("Owner", serverPlayer.m_20148_());
        compoundTag.m_128359_("Family", FoodWeaponFamilyBuild.kind((ItemStack)player).name());
        compoundTag.m_128359_("SourceKind", "MELEE");
        compoundTag = FoodWeaponSourceLedger.decorate(serverPlayer, (ItemStack)player, compoundTag, InteractionHand.MAIN_HAND);
        if (FoodWeaponSourceLedger.valid(compoundTag, serverPlayer)) {
            MELEE.put(serverPlayer.m_20148_(), new Melee(compoundTag.m_6426_(), FoodWeaponBondHits.clock(serverPlayer)));
        }
    }

    public static boolean hostile(ServerPlayer serverPlayer, LivingEntity livingEntity) {
        OwnableEntity ownableEntity;
        TamableAnimal tamableAnimal;
        return !(!(livingEntity instanceof Monster) || livingEntity.m_213877_() || livingEntity.m_9236_() != serverPlayer.m_9236_() || livingEntity.m_7307_((Entity)serverPlayer) || serverPlayer.m_7307_((Entity)livingEntity) || livingEntity instanceof TamableAnimal && (tamableAnimal = (TamableAnimal)livingEntity).m_21824_() || livingEntity instanceof OwnableEntity && (ownableEntity = (OwnableEntity)livingEntity).m_21805_() != null);
    }

    public static ItemStack preferred(ServerPlayer serverPlayer, Predicate<ItemStack> predicate) {
        ItemStack itemStack = serverPlayer.m_21205_();
        if (itemStack.m_41613_() == 1 && !FoodWeaponLifecycle.isBroken(itemStack) && predicate.test(itemStack)) {
            return itemStack;
        }
        ItemStack itemStack2 = serverPlayer.m_21206_();
        return itemStack2.m_41613_() == 1 && (FoodWeaponFamilyBuild.kind(itemStack2) != FoodWeaponFamilyRules.Kind.MELEE || itemStack2.m_41720_() instanceof AxeItem || FoodWeaponSourceLedger.isTrident(itemStack2)) && !FoodWeaponLifecycle.isBroken(itemStack2) && predicate.test(itemStack2) ? itemStack2 : ItemStack.f_41583_;
    }

    public static boolean bind(ServerPlayer serverPlayer, ItemStack itemStack, CompoundTag compoundTag, String string) {
        FoodWeaponLifecycle.ensureTracked(itemStack);
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.m_128359_("Family", FoodWeaponFamilyBuild.kind(itemStack).name());
        compoundTag2 = FoodWeaponSourceLedger.decorate(serverPlayer, itemStack, compoundTag2, serverPlayer.m_21205_() == itemStack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        return FoodWeaponBondHits.saveBinding(serverPlayer, itemStack, compoundTag, string, compoundTag2);
    }

    public static boolean bind(ServerPlayer serverPlayer, ItemStack itemStack, CompoundTag compoundTag, String string, Hit hit) {
        return hit != null && FoodWeaponBondHits.saveBinding(serverPlayer, itemStack, compoundTag, string, hit.snapshot());
    }

    private static boolean saveBinding(ServerPlayer serverPlayer, ItemStack itemStack, CompoundTag compoundTag, String string, CompoundTag compoundTag2) {
        if (itemStack.m_41613_() != 1 || !FoodWeaponSourceLedger.originalHandContinuous(compoundTag2, serverPlayer)) {
            return false;
        }
        compoundTag.m_128362_(string + "WeaponId", compoundTag2.m_128342_("WeaponId"));
        compoundTag.m_128359_(string + "Hand", compoundTag2.m_128461_("OriginalHand"));
        compoundTag.m_128359_(string + "Dimension", compoundTag2.m_128461_("Dimension"));
        compoundTag.m_128365_(string + "Fingerprint", (Tag)FoodWeaponBondHits.fingerprint(itemStack));
        compoundTag.m_128365_(string + "Source", (Tag)compoundTag2.m_6426_());
        return true;
    }

    public static boolean bound(ServerPlayer serverPlayer, ItemStack itemStack, CompoundTag compoundTag, String string) {
        if (!compoundTag.m_128425_(string + "Source", 10) || !FoodWeaponSourceLedger.originalHandContinuous(compoundTag.m_128469_(string + "Source"), serverPlayer)) {
            return false;
        }
        String string2 = compoundTag.m_128461_(string + "Hand");
        ItemStack itemStack2 = string2.equals("MAIN_HAND") ? serverPlayer.m_21205_() : (string2.equals("OFF_HAND") ? serverPlayer.m_21206_() : ItemStack.f_41583_);
        return itemStack2.m_41720_() == itemStack.m_41720_() && itemStack2.m_41613_() == 1 && !FoodWeaponLifecycle.isBroken(itemStack2) && compoundTag.m_128403_(string + "WeaponId") && compoundTag.m_128342_(string + "WeaponId").equals(FoodWeaponLifecycle.weaponId(itemStack)) && compoundTag.m_128461_(string + "Dimension").equals(serverPlayer.m_9236_().m_46472_().m_135782_().toString()) && compoundTag.m_128425_(string + "Fingerprint", 10) && compoundTag.m_128469_(string + "Fingerprint").equals((Object)FoodWeaponBondHits.fingerprint(itemStack));
    }

    public static long clock(ServerPlayer serverPlayer) {
        return FoodWeaponSourceLedger.clock(serverPlayer);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent playerLoggedOutEvent) {
        FoodWeaponBondHits.clear(playerLoggedOutEvent.getEntity().m_20148_());
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent) {
        FoodWeaponBondHits.clear(playerLoggedInEvent.getEntity().m_20148_());
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone clone) {
        FoodWeaponBondHits.clear(clone.getEntity().m_20148_());
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent playerChangedDimensionEvent) {
        FoodWeaponBondHits.clear(playerChangedDimensionEvent.getEntity().m_20148_());
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent serverStoppedEvent) {
        MELEE.clear();
    }

    private static void clear(UUID uUID) {
        MELEE.remove(uUID);
    }

    public static final class Context {
        private final ServerPlayer player;
        private final LivingEntity target;
        private final DamageSource source;
        private final Hit hit;
        private final Phase phase;
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

        private Context(ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, Hit hit, Phase phase, LivingDeathEvent livingDeathEvent) {
            this.player = serverPlayer;
            this.target = livingEntity;
            this.source = damageSource;
            this.hit = hit;
            this.phase = phase;
            this.death = livingDeathEvent;
            this.server = serverPlayer.m_284548_().m_7654_();
            this.level = serverPlayer.m_9236_();
            this.thread = Thread.currentThread();
            this.hand = InteractionHand.valueOf((String)hit.snapshot().m_128461_("OriginalHand"));
            this.actualWeapon = serverPlayer.m_21120_(this.hand);
            this.fingerprint = FoodWeaponBondHits.fingerprint(this.actualWeapon).m_6426_();
            this.materials = FoodWeaponSourceLedger.materialFingerprint(this.actualWeapon);
            this.tick = FoodWeaponBondHits.clock(serverPlayer);
            this.targetId = livingEntity.m_20148_();
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

    private static enum Phase {
        HIT,
        DEATH;

    }

    public record Hit(ItemStack weapon, FoodWeaponBondRules.Kind kind, UUID attack, CompoundTag snapshot) {
        private final ItemStack weapon;
        private final CompoundTag snapshot;

        public Hit {
            itemStack = itemStack.m_41777_();
            compoundTag = compoundTag.m_6426_();
        }

        public ItemStack weapon() {
            return this.weapon.m_41777_();
        }

        public CompoundTag snapshot() {
            return this.snapshot.m_6426_();
        }
    }

    private record Melee(CompoundTag snapshot, long at) {
    }
}

