/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.living.LivingEntityUseItemEvent$Finish
 *  net.minecraftforge.event.entity.living.LivingEntityUseItemEvent$Start
 *  net.minecraftforge.event.entity.living.LivingEntityUseItemEvent$Stop
 *  net.minecraftforge.event.entity.living.LivingEntityUseItemEvent$Tick
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.ModList
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import net.farmingtales.ftboss.integ.FoodWeaponSpellSession;
import net.farmingtales.ftboss.item.FoodWeaponDamageSources;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.item.WeaponMaterialPowers;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class FoodWeaponRanged {
    private static final Map<LivingEntity, ChargeState> CHARGES = new WeakHashMap<LivingEntity, ChargeState>();
    private static final ThreadLocal<CastState> CAST = new ThreadLocal();
    private static final ThreadLocal<HitState> HIT = new ThreadLocal();
    private static final int SNAPSHOT_VERSION = 3;
    private static final String PROC_TIMERS = "FTBossRangedProcTimers";
    private static final String PENDING_BOW_SHOT = "FTBossPendingBowShot";

    static boolean interactionViewAllows(ServerPlayer serverPlayer, CompoundTag compoundTag, DamageSource damageSource, Entity entity) {
        HitState hitState = HIT.get();
        if (hitState != null) {
            return hitState.thread() == Thread.currentThread() && !hitState.nested() && hitState.player() == serverPlayer && hitState.snapshot().equals((Object)compoundTag) && (damageSource == null || hitState.source() == damageSource && hitState.target() == entity);
        }
        if (damageSource != null || entity != null) {
            return false;
        }
        CastState castState = CAST.get();
        return castState != null && castState.player() == serverPlayer && castState.snapshot().equals((Object)compoundTag);
    }

    private FoodWeaponRanged() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clearOwnerScopes(ServerPlayer serverPlayer) {
        HitState hitState;
        Object object = CHARGES;
        synchronized (object) {
            CHARGES.keySet().removeIf(livingEntity -> {
                ServerPlayer serverPlayer2;
                return livingEntity instanceof ServerPlayer && (serverPlayer2 = (ServerPlayer)livingEntity).m_20148_().equals(serverPlayer.m_20148_()) && serverPlayer2.m_284548_().m_7654_() == serverPlayer.m_284548_().m_7654_();
            });
        }
        object = CAST.get();
        if (object != null && ((CastState)object).player().m_20148_().equals(serverPlayer.m_20148_()) && ((CastState)object).player().m_284548_().m_7654_() == serverPlayer.m_284548_().m_7654_()) {
            CAST.remove();
        }
        if ((hitState = HIT.get()) != null && hitState.player() != null && hitState.player().m_20148_().equals(serverPlayer.m_20148_()) && hitState.player().m_284548_().m_7654_() == serverPlayer.m_284548_().m_7654_()) {
            HIT.remove();
        }
        if (ModList.get().isLoaded("irons_spellbooks")) {
            FoodWeaponSpellSession.clearOwner(serverPlayer);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clearServerScopes(MinecraftServer minecraftServer) {
        HitState hitState;
        Object object = CHARGES;
        synchronized (object) {
            CHARGES.keySet().removeIf(livingEntity -> {
                ServerPlayer serverPlayer;
                return livingEntity instanceof ServerPlayer && (serverPlayer = (ServerPlayer)livingEntity).m_284548_().m_7654_() == minecraftServer;
            });
        }
        object = CAST.get();
        if (object != null && ((CastState)object).player().m_284548_().m_7654_() == minecraftServer) {
            CAST.remove();
        }
        if ((hitState = HIT.get()) != null && hitState.player() != null && hitState.player().m_284548_().m_7654_() == minecraftServer) {
            HIT.remove();
        }
        if (ModList.get().isLoaded("irons_spellbooks")) {
            FoodWeaponSpellSession.clearServer(minecraftServer);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void beginCharge(LivingEntityUseItemEvent.Start start) {
        Map<LivingEntity, ChargeState> map = CHARGES;
        synchronized (map) {
            CHARGES.remove(start.getEntity());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent(priority=EventPriority.LOW)
    public static void charge(LivingEntityUseItemEvent.Tick tick) {
        ItemStack itemStack = tick.getItem();
        if (!(itemStack.m_41720_() instanceof BowItem) || itemStack.m_41737_("FTBossWeaponMaterials") == null || FoodWeaponLifecycle.isBroken(itemStack) || tick.getDuration() <= 0) {
            return;
        }
        Map<LivingEntity, ChargeState> map = CHARGES;
        synchronized (map) {
            ChargeState chargeState = CHARGES.get(tick.getEntity());
            double d = chargeState == null || chargeState.weapon() != itemStack || tick.getDuration() > chargeState.remaining() ? 0.0 : chargeState.carry();
            FoodWeaponFamilyRules.Charge charge = FoodWeaponFamilyRules.chargeStep(tick.getDuration(), FoodWeaponFamilyRules.speedMultiplier(FoodWeaponFamilyBuild.build(itemStack)), d);
            tick.setDuration(charge.remaining());
            CHARGES.put(tick.getEntity(), new ChargeState(itemStack, charge.remaining(), charge.carry()));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void endCharge(LivingEntityUseItemEvent.Stop stop) {
        Map<LivingEntity, ChargeState> map = CHARGES;
        synchronized (map) {
            CHARGES.remove(stop.getEntity());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void finishCharge(LivingEntityUseItemEvent.Finish finish) {
        Map<LivingEntity, ChargeState> map = CHARGES;
        synchronized (map) {
            CHARGES.remove(finish.getEntity());
        }
    }

    public static CompoundTag capture(ServerPlayer serverPlayer, ItemStack itemStack) {
        return FoodWeaponRanged.captureBound(serverPlayer, itemStack, FoodWeaponFamilyBuild.kind(itemStack));
    }

    public static CompoundTag captureBound(ServerPlayer serverPlayer, ItemStack itemStack, FoodWeaponFamilyRules.Kind kind) {
        InteractionHand interactionHand = serverPlayer.m_21206_() == itemStack ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        return FoodWeaponRanged.captureBound(serverPlayer, itemStack, kind, interactionHand);
    }

    public static CompoundTag captureBound(ServerPlayer serverPlayer, ItemStack itemStack, FoodWeaponFamilyRules.Kind kind, InteractionHand interactionHand) {
        FoodWeaponSourceLedger.NativeMaterialCapture nativeMaterialCapture = null;
        try {
            nativeMaterialCapture = FoodWeaponSourceLedger.nativeMaterialCaptureEntered(serverPlayer, itemStack, interactionHand);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            if (itemStack.m_41619_() || itemStack.m_41783_() != null && !FoodWeaponSourceLedger.canonicalInputValid((Tag)itemStack.m_41783_()) || !FoodWeaponSourceLedger.canonicalInputValid((Tag)itemStack.m_41739_(new CompoundTag())) || kind == FoodWeaponFamilyRules.Kind.MELEE && !FourWeaponFoundationAdapter.applies(itemStack) || FoodWeaponLifecycle.isBroken(itemStack)) {
                return new CompoundTag();
            }
            FoodWeaponLifecycle.ensureTracked(itemStack);
            CompoundTag compoundTag = itemStack.m_41737_("FTBossWeaponMaterials");
            CompoundTag compoundTag2 = compoundTag == null ? new CompoundTag() : compoundTag.m_6426_();
            compoundTag2.m_128405_("SnapshotVersion", 3);
            compoundTag2.m_128362_("Owner", serverPlayer.m_20148_());
            compoundTag2.m_128359_("Family", kind.name());
            FoodWeaponBuildRules.Build build = FourWeaponFoundationAdapter.applies(itemStack) ? FourWeaponFoundationAdapter.combat(itemStack) : FoodWeaponFamilyRules.build(kind, WeaponMaterialBuild.materials(itemStack));
            compoundTag2.m_128347_("ProjectileDamage", FoodWeaponFamilyRules.damageMultiplier(build));
            compoundTag2.m_128347_("LaunchSpeed", FoodWeaponFamilyRules.rangeMultiplier(build));
            compoundTag2.m_128365_("Weapon", (Tag)itemStack.m_41739_(new CompoundTag()));
            WeaponMaterialPowers.snapshot(itemStack, compoundTag2);
            CompoundTag compoundTag3 = FoodWeaponSourceLedger.decorate(serverPlayer, itemStack, compoundTag2, interactionHand);
            try {
                FoodWeaponSourceLedger.nativeMaterialCaptureReturned(serverPlayer, itemStack, interactionHand, compoundTag3);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return compoundTag3;
        }
        catch (Throwable throwable) {
            try {
                FoodWeaponSourceLedger.nativeMaterialCaptureFailed(nativeMaterialCapture, throwable);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
            throw throwable;
        }
    }

    public static void launch(Entity entity, CompoundTag compoundTag) {
        FoodWeaponSourceLedger.NativeMaterialTransfer nativeMaterialTransfer = null;
        try {
            nativeMaterialTransfer = FoodWeaponSourceLedger.nativeMaterialLaunchEntered(entity, compoundTag);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            Tag tag;
            ServerPlayer serverPlayer;
            Projectile projectile;
            Entity entity2;
            if (!(entity instanceof Projectile) || !((entity2 = (projectile = (Projectile)entity).m_19749_()) instanceof ServerPlayer) || !FoodWeaponRanged.owned(compoundTag, serverPlayer = (ServerPlayer)entity2) || compoundTag.m_128461_("SourceKind").equals("TRIDENT") || entity.getPersistentData().m_128425_("FTBossWeaponMaterials", 10)) {
                return;
            }
            entity2 = entity.getPersistentData();
            String string = "FTBossWeaponMaterials";
            CompoundTag compoundTag2 = compoundTag.m_6426_();
            FoodWeaponSourceLedger.NativeMaterialPublication nativeMaterialPublication = null;
            try {
                nativeMaterialPublication = FoodWeaponSourceLedger.nativeMaterialPutEntered(nativeMaterialTransfer, entity, (CompoundTag)entity2, string, compoundTag, compoundTag2);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            try {
                tag = entity2.m_128365_(string, (Tag)compoundTag2);
            }
            catch (Throwable throwable) {
                try {
                    FoodWeaponSourceLedger.nativeMaterialPutFailed(nativeMaterialPublication, throwable);
                }
                catch (Throwable throwable2) {
                    // empty catch block
                }
                throw throwable;
            }
            try {
                FoodWeaponSourceLedger.nativeMaterialPutReturned(nativeMaterialPublication, tag);
            }
            catch (Throwable throwable) {
                try {
                    FoodWeaponSourceLedger.nativeMaterialObservationFailed(nativeMaterialPublication, throwable);
                }
                catch (Throwable throwable3) {}
            }
        }
        catch (Throwable throwable) {
            try {
                FoodWeaponSourceLedger.nativeMaterialTransferFailed(nativeMaterialTransfer, throwable);
            }
            catch (Throwable throwable4) {
                // empty catch block
            }
            throw throwable;
        }
    }

    public static void prepareBowShot(Entity entity, CompoundTag compoundTag) {
        FoodWeaponSourceLedger.NativeMaterialTransfer nativeMaterialTransfer = null;
        try {
            nativeMaterialTransfer = FoodWeaponSourceLedger.nativeMaterialPendingEntered(entity, compoundTag);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            ServerPlayer serverPlayer;
            AbstractArrow abstractArrow;
            Entity entity2;
            if (!(entity instanceof AbstractArrow) || !((entity2 = (abstractArrow = (AbstractArrow)entity).m_19749_()) instanceof ServerPlayer) || !FoodWeaponRanged.owned(compoundTag, serverPlayer = (ServerPlayer)entity2) || !compoundTag.m_128461_("Family").equals(FoodWeaponFamilyRules.Kind.BOW.name()) || entity.getPersistentData().m_128425_("FTBossWeaponMaterials", 10) || entity.getPersistentData().m_128425_(PENDING_BOW_SHOT, 10)) {
                return;
            }
            entity2 = entity.getPersistentData();
            String string = PENDING_BOW_SHOT;
            CompoundTag compoundTag2 = compoundTag.m_6426_();
            try {
                FoodWeaponSourceLedger.nativeMaterialPendingCopied(nativeMaterialTransfer, (CompoundTag)entity2, string, compoundTag, compoundTag2);
            }
            catch (Throwable throwable) {
                try {
                    FoodWeaponSourceLedger.nativeMaterialTransferObservationFailed(nativeMaterialTransfer, throwable);
                }
                catch (Throwable throwable2) {
                    // empty catch block
                }
            }
            Tag tag = entity2.m_128365_(string, (Tag)compoundTag2);
            try {
                FoodWeaponSourceLedger.nativeMaterialPendingReturned(nativeMaterialTransfer, tag);
            }
            catch (Throwable throwable) {
                try {
                    FoodWeaponSourceLedger.nativeMaterialTransferObservationFailed(nativeMaterialTransfer, throwable);
                }
                catch (Throwable throwable3) {}
            }
        }
        catch (Throwable throwable) {
            try {
                FoodWeaponSourceLedger.nativeMaterialTransferFailed(nativeMaterialTransfer, throwable);
            }
            catch (Throwable throwable4) {
                // empty catch block
            }
            throw throwable;
        }
    }

    public static void launchPreparedBow(Entity entity) {
        FoodWeaponSourceLedger.NativeMaterialTransfer nativeMaterialTransfer = null;
        try {
            nativeMaterialTransfer = FoodWeaponSourceLedger.nativeMaterialPendingReadEntered(entity);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            CompoundTag compoundTag = entity.getPersistentData();
            String string = PENDING_BOW_SHOT;
            CompoundTag compoundTag2 = compoundTag.m_128469_(string);
            CompoundTag compoundTag3 = entity.getPersistentData();
            compoundTag3.m_128473_(PENDING_BOW_SHOT);
            try {
                FoodWeaponSourceLedger.nativeMaterialPendingTaken(nativeMaterialTransfer, compoundTag, string, compoundTag2, compoundTag3);
            }
            catch (Throwable throwable) {
                try {
                    FoodWeaponSourceLedger.nativeMaterialTransferObservationFailed(nativeMaterialTransfer, throwable);
                }
                catch (Throwable throwable2) {
                    // empty catch block
                }
            }
            FoodWeaponRanged.launch(entity, compoundTag2);
        }
        catch (Throwable throwable) {
            try {
                FoodWeaponSourceLedger.nativeMaterialTransferFailed(nativeMaterialTransfer, throwable);
            }
            catch (Throwable throwable3) {
                // empty catch block
            }
            throw throwable;
        }
    }

    public static void projectileJoin(EntityJoinLevelEvent entityJoinLevelEvent) {
        ServerPlayer serverPlayer;
        Projectile projectile;
        Object object;
        block5: {
            block4: {
                if (entityJoinLevelEvent.getLevel().f_46443_ || !((object = entityJoinLevelEvent.getEntity()) instanceof Projectile) || !((object = (projectile = (Projectile)object).m_19749_()) instanceof ServerPlayer)) break block4;
                serverPlayer = (ServerPlayer)object;
                if (!projectile.getPersistentData().m_128425_("FTBossWeaponMaterials", 10)) break block5;
            }
            return;
        }
        object = CAST.get();
        if (object != null && ((CastState)object).player() == serverPlayer) {
            FoodWeaponRanged.launch((Entity)projectile, ((CastState)object).snapshot());
            return;
        }
    }

    private static boolean owned(CompoundTag compoundTag, ServerPlayer serverPlayer) {
        return FoodWeaponSourceLedger.validReadOnly(compoundTag, serverPlayer);
    }

    public static CompoundTag materialSnapshot(DamageSource damageSource, ServerPlayer serverPlayer) {
        return FoodWeaponRanged.materialSnapshotReadOnly(damageSource, serverPlayer);
    }

    static CompoundTag scopedSnapshot(DamageSource damageSource, ServerPlayer serverPlayer) {
        CastState castState = CAST.get();
        if (damageSource.m_7640_() == serverPlayer && castState != null && castState.player() == serverPlayer && !castState.snapshot().m_128461_("SourceKind").equals("TRIDENT") && damageSource.m_7639_() == serverPlayer && damageSource.m_276093_(FoodWeaponDamageSources.PRIMARY) && "GENERIC_J".equals(castState.snapshot().m_128461_("DamagePurpose"))) {
            return FoodWeaponRanged.owned(castState.snapshot(), serverPlayer) ? castState.snapshot().m_6426_() : new CompoundTag();
        }
        return new CompoundTag();
    }

    static CompoundTag scopedSnapshotReadOnly(DamageSource damageSource, ServerPlayer serverPlayer) {
        CastState castState = CAST.get();
        if (damageSource.m_7640_() == serverPlayer && castState != null && castState.player() == serverPlayer && !castState.snapshot().m_128461_("SourceKind").equals("TRIDENT") && damageSource.m_7639_() == serverPlayer && damageSource.m_276093_(FoodWeaponDamageSources.PRIMARY) && "GENERIC_J".equals(castState.snapshot().m_128461_("DamagePurpose"))) {
            return castState.snapshot().m_6426_();
        }
        return new CompoundTag();
    }

    public static CompoundTag materialSnapshotReadOnly(DamageSource damageSource, ServerPlayer serverPlayer) {
        HitState hitState = HIT.get();
        if (hitState != null) {
            return hitState.source() == damageSource && hitState.player() == serverPlayer && !hitState.nested() && FoodWeaponSourceLedger.validReadOnly(hitState.snapshot(), serverPlayer) ? hitState.snapshot().m_6426_() : new CompoundTag();
        }
        return FoodWeaponSourceLedger.resolveReadOnly(damageSource, serverPlayer);
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

    public static List<FoodWeaponBuildRules.Material> attackMaterials(DamageSource damageSource, ServerPlayer serverPlayer) {
        CompoundTag compoundTag = FoodWeaponRanged.materialSnapshot(damageSource, serverPlayer);
        if (compoundTag.m_128456_()) {
            return List.of();
        }
        ArrayList<FoodWeaponBuildRules.Material> arrayList = new ArrayList<FoodWeaponBuildRules.Material>();
        for (FoodWeaponBuildRules.Part part : FoodWeaponBuildRules.Part.values()) {
            arrayList.add(FoodWeaponBuildRules.Material.byId(compoundTag.m_128461_(part.name())));
        }
        return List.copyOf(arrayList);
    }

    public static ItemStack sourceWeapon(DamageSource damageSource, ServerPlayer serverPlayer) {
        if (!FoodWeaponRanged.allowsWeaponEffects(damageSource)) {
            return ItemStack.f_41583_;
        }
        CompoundTag compoundTag = FoodWeaponRanged.materialSnapshot(damageSource, serverPlayer);
        if (!compoundTag.m_128456_()) {
            return ItemStack.m_41712_((CompoundTag)compoundTag.m_128469_("Weapon"));
        }
        return damageSource.m_7640_() == serverPlayer && damageSource.m_276093_(DamageTypes.f_268464_) && !FoodWeaponRanged.magicSource(damageSource) ? serverPlayer.m_21205_() : ItemStack.f_41583_;
    }

    public static void prepareHitTimers(ServerPlayer serverPlayer, DamageSource damageSource, CompoundTag compoundTag) {
        if (FoodWeaponRanged.materialSnapshot(damageSource, serverPlayer).m_128456_()) {
            return;
        }
        CompoundTag compoundTag2 = serverPlayer.getPersistentData().m_128469_(PROC_TIMERS);
        for (String string : compoundTag2.m_128431_()) {
            if (!string.startsWith("Last") || !compoundTag2.m_128425_(string, 4)) continue;
            compoundTag.m_128356_(string, Math.max(compoundTag.m_128454_(string), compoundTag2.m_128454_(string)));
        }
    }

    public static void saveHitTimers(ServerPlayer serverPlayer, DamageSource damageSource, CompoundTag compoundTag) {
        if (FoodWeaponRanged.materialSnapshot(damageSource, serverPlayer).m_128456_()) {
            return;
        }
        CompoundTag compoundTag2 = serverPlayer.getPersistentData().m_128469_(PROC_TIMERS);
        for (String string : compoundTag.m_128431_()) {
            if (!string.startsWith("Last") || !compoundTag.m_128425_(string, 4)) continue;
            compoundTag2.m_128356_(string, Math.max(compoundTag.m_128454_(string), compoundTag2.m_128454_(string)));
        }
        serverPlayer.getPersistentData().m_128365_(PROC_TIMERS, (Tag)compoundTag2);
    }

    public static boolean canCast(ServerPlayer serverPlayer, ItemStack itemStack) {
        if (itemStack == null || FoodWeaponFamilyBuild.kind(itemStack) != FoodWeaponFamilyRules.Kind.STAFF) {
            return true;
        }
        return (serverPlayer.m_21205_() == itemStack || serverPlayer.m_21206_() == itemStack) && !FoodWeaponLifecycle.isBroken(itemStack);
    }

    public static boolean allowsWeaponEffects(DamageSource damageSource) {
        HitState hitState = HIT.get();
        return hitState == null || !hitState.nested() && hitState.source() == damageSource;
    }

    public static <Result> Result withHitSource(DamageSource damageSource, Supplier<Result> supplier) {
        return FoodWeaponRanged.withHitSource(null, damageSource, supplier);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static <Result> Result withHitSource(Entity entity, DamageSource damageSource, Supplier<Result> supplier) {
        Result Result2;
        block12: {
            ServerPlayer serverPlayer;
            HitState hitState;
            block11: {
                boolean bl;
                ServerPlayer serverPlayer2;
                hitState = HIT.get();
                Entity entity2 = damageSource.m_7639_();
                serverPlayer = entity2 instanceof ServerPlayer ? (serverPlayer2 = (ServerPlayer)entity2) : null;
                serverPlayer2 = hitState == null && serverPlayer != null ? FoodWeaponRanged.materialSnapshot(damageSource, serverPlayer).m_6426_() : new CompoundTag();
                boolean bl2 = FoodWeaponSourceLedger.actualHurtSnapshot((CompoundTag)serverPlayer2) && !FoodWeaponSourceLedger.matchesNativeHurt(damageSource, serverPlayer, entity, (CompoundTag)serverPlayer2);
                HitState hitState2 = new HitState(damageSource, serverPlayer, entity, (CompoundTag)(bl2 ? new CompoundTag() : serverPlayer2), hitState != null || bl2, Thread.currentThread());
                HIT.set(hitState2);
                try {
                    Result2 = supplier.get();
                    if (HIT.get() != hitState2) break block11;
                    if (hitState == null) {
                        HIT.remove();
                        break block12;
                    }
                    HIT.set(hitState);
                }
                catch (Throwable throwable) {
                    if (HIT.get() == hitState2) {
                        if (hitState == null) {
                            HIT.remove();
                        } else {
                            boolean bl3;
                            HIT.set(hitState);
                            boolean bl4 = bl3 = hitState.thread() == Thread.currentThread() && (hitState.player() == null || hitState.snapshot().m_128456_() || FoodWeaponRanged.owned(hitState.snapshot(), hitState.player()) && (!FoodWeaponSourceLedger.actualHurtSnapshot(hitState.snapshot()) || FoodWeaponSourceLedger.matchesNativeHurt(hitState.source(), hitState.player(), hitState.target(), hitState.snapshot())));
                            if (!bl3) {
                                HIT.remove();
                                FoodWeaponSourceLedger.revokeActualHurt(hitState.source(), hitState.player());
                            }
                        }
                    } else {
                        FoodWeaponSourceLedger.revokeActualHurt(damageSource, serverPlayer);
                        if (hitState != null) {
                            FoodWeaponSourceLedger.revokeActualHurt(hitState.source(), hitState.player());
                        }
                    }
                    throw throwable;
                }
                boolean bl5 = bl = hitState.thread() == Thread.currentThread() && (hitState.player() == null || hitState.snapshot().m_128456_() || FoodWeaponRanged.owned(hitState.snapshot(), hitState.player()) && (!FoodWeaponSourceLedger.actualHurtSnapshot(hitState.snapshot()) || FoodWeaponSourceLedger.matchesNativeHurt(hitState.source(), hitState.player(), hitState.target(), hitState.snapshot())));
                if (!bl) {
                    HIT.remove();
                    FoodWeaponSourceLedger.revokeActualHurt(hitState.source(), hitState.player());
                }
                break block12;
            }
            FoodWeaponSourceLedger.revokeActualHurt(damageSource, serverPlayer);
            if (hitState != null) {
                FoodWeaponSourceLedger.revokeActualHurt(hitState.source(), hitState.player());
            }
        }
        return Result2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static <Result> Result withSource(ServerPlayer serverPlayer, CompoundTag compoundTag, Supplier<Result> supplier) {
        CastState castState = CAST.get();
        if (serverPlayer != null && FoodWeaponRanged.owned(compoundTag, serverPlayer)) {
            CAST.set(new CastState(serverPlayer, compoundTag.m_6426_()));
        } else {
            CAST.remove();
        }
        try {
            Result Result2 = supplier.get();
            return Result2;
        }
        finally {
            if (castState == null || !FoodWeaponRanged.owned(castState.snapshot(), castState.player())) {
                CAST.remove();
            } else {
                CAST.set(castState);
            }
        }
    }

    public static Runnable deferSource(Runnable runnable) {
        CastState castState = CAST.get();
        ServerPlayer serverPlayer = castState == null ? null : castState.player();
        CompoundTag compoundTag = castState == null ? new CompoundTag() : castState.snapshot().m_6426_();
        return () -> FoodWeaponRanged.withSource(serverPlayer, compoundTag, () -> {
            runnable.run();
            return null;
        });
    }

    public static void tickSource(Entity entity) {
        CompoundTag compoundTag;
        Projectile projectile;
        Entity entity2;
        CompoundTag compoundTag2 = entity instanceof Projectile && (entity2 = (projectile = (Projectile)entity).m_19749_()) instanceof ServerPlayer ? (compoundTag = (ServerPlayer)entity2) : null;
        compoundTag = entity.getPersistentData().m_128469_("FTBossWeaponMaterials");
        FoodWeaponRanged.withSource(compoundTag2, compoundTag, () -> {
            entity.m_8119_();
            return null;
        });
    }

    public static void withStaffCast(ServerPlayer serverPlayer, ItemStack itemStack, int n, Runnable runnable) {
        FoodWeaponRanged.withStaffCast(serverPlayer, itemStack, n, null, runnable);
    }

    public static void withStaffCast(ServerPlayer serverPlayer, ItemStack itemStack, int n, CompoundTag compoundTag, Runnable runnable) {
        boolean bl;
        boolean bl2 = bl = itemStack != null && FoodWeaponFamilyBuild.kind(itemStack) == FoodWeaponFamilyRules.Kind.STAFF;
        if (bl && !FoodWeaponRanged.canCast(serverPlayer, itemStack)) {
            return;
        }
        FoodWeaponRanged.withSource(null, new CompoundTag(), () -> {
            runnable.run();
            return null;
        });
    }

    private record HitState(DamageSource source, ServerPlayer player, Entity target, CompoundTag snapshot, boolean nested, Thread thread) {
    }

    private record CastState(ServerPlayer player, CompoundTag snapshot) {
    }

    private record ChargeState(ItemStack weapon, int remaining, double carry) {
    }
}

