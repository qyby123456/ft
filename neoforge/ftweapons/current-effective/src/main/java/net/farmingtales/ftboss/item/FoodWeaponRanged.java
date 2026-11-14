package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import net.farmingtales.ftboss.integ.FoodWeaponSpellSession;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Charge;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.core.component.DataComponents;
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
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Start;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Stop;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Tick;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class FoodWeaponRanged {
    private static final Map<LivingEntity, FoodWeaponRanged.ChargeState> CHARGES = new WeakHashMap<>();
    private static final ThreadLocal<FoodWeaponRanged.CastState> CAST = new ThreadLocal<>();
    private static final ThreadLocal<FoodWeaponRanged.HitState> HIT = new ThreadLocal<>();
    private static final int SNAPSHOT_VERSION = 3;
    private static final String PROC_TIMERS = "FTBossRangedProcTimers";
    private static final String PENDING_BOW_SHOT = "FTBossPendingBowShot";

    static boolean interactionViewAllows(ServerPlayer var0, CompoundTag var1, DamageSource var2, Entity var3) {
        FoodWeaponRanged.HitState var4 = HIT.get();
        if (var4 != null) {
            return var4.thread() == Thread.currentThread()
                && !var4.nested()
                && var4.player() == var0
                && var4.snapshot().equals(var1)
                && (var2 == null || var4.source() == var2 && var4.target() == var3);
        } else if (var2 == null && var3 == null) {
            FoodWeaponRanged.CastState var5 = CAST.get();
            return var5 != null && var5.player() == var0 && var5.snapshot().equals(var1);
        } else {
            return false;
        }
    }

    private FoodWeaponRanged() {
    }

    public static void clearOwnerScopes(ServerPlayer var0) {
        synchronized (CHARGES) {
            CHARGES.keySet()
                .removeIf(
                    var1 -> {
                        if (var1 instanceof ServerPlayer var2x
                            && var2x.getUUID().equals(var0.getUUID())
                            && var2x.serverLevel().getServer() == var0.serverLevel().getServer()) {
                            return true;
                        }

                        return false;
                    }
                );
        }

        FoodWeaponRanged.CastState var4 = CAST.get();
        if (var4 != null && var4.player().getUUID().equals(var0.getUUID()) && var4.player().serverLevel().getServer() == var0.serverLevel().getServer()) {
            CAST.remove();
        }

        FoodWeaponRanged.HitState var2 = HIT.get();
        if (var2 != null
            && var2.player() != null
            && var2.player().getUUID().equals(var0.getUUID())
            && var2.player().serverLevel().getServer() == var0.serverLevel().getServer()) {
            HIT.remove();
        }

        if (ModList.get().isLoaded("irons_spellbooks")) {
            FoodWeaponSpellSession.clearOwner(var0);
        }
    }

    public static void clearServerScopes(MinecraftServer var0) {
        synchronized (CHARGES) {
            CHARGES.keySet().removeIf(var1 -> {
                if (var1 instanceof ServerPlayer var2x && var2x.serverLevel().getServer() == var0) {
                    return true;
                }

                return false;
            });
        }

        FoodWeaponRanged.CastState var4 = CAST.get();
        if (var4 != null && var4.player().serverLevel().getServer() == var0) {
            CAST.remove();
        }

        FoodWeaponRanged.HitState var2 = HIT.get();
        if (var2 != null && var2.player() != null && var2.player().serverLevel().getServer() == var0) {
            HIT.remove();
        }

        if (ModList.get().isLoaded("irons_spellbooks")) {
            FoodWeaponSpellSession.clearServer(var0);
        }
    }

    @SubscribeEvent
    public static void beginCharge(Start var0) {
        synchronized (CHARGES) {
            CHARGES.remove(var0.getEntity());
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOW
    )
    public static void charge(Tick var0) {
        ItemStack var1 = var0.getItem();
        if (var1.getItem() instanceof BowItem
            && FoodWeaponData.read(var1, "FTBossWeaponMaterials") != null
            && !FoodWeaponLifecycle.isBroken(var1)
            && var0.getDuration() > 0) {
            synchronized (CHARGES) {
                FoodWeaponRanged.ChargeState var3 = CHARGES.get(var0.getEntity());
                double var4 = var3 != null && var3.weapon() == var1 && var0.getDuration() <= var3.remaining() ? var3.carry() : 0.0;
                Charge var6 = FoodWeaponFamilyRules.chargeStep(
                    var0.getDuration(), FoodWeaponFamilyRules.speedMultiplier(FoodWeaponFamilyBuild.build(var1)), var4
                );
                var0.setDuration(var6.remaining());
                CHARGES.put(var0.getEntity(), new FoodWeaponRanged.ChargeState(var1, var6.remaining(), var6.carry()));
            }
        }
    }

    @SubscribeEvent
    public static void endCharge(Stop var0) {
        synchronized (CHARGES) {
            CHARGES.remove(var0.getEntity());
        }
    }

    @SubscribeEvent
    public static void finishCharge(Finish var0) {
        synchronized (CHARGES) {
            CHARGES.remove(var0.getEntity());
        }
    }

    public static CompoundTag capture(ServerPlayer var0, ItemStack var1) {
        return captureBound(var0, var1, FoodWeaponFamilyBuild.kind(var1));
    }

    public static CompoundTag captureBound(ServerPlayer var0, ItemStack var1, Kind var2) {
        InteractionHand var3 = var0.getOffhandItem() == var1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        return captureBound(var0, var1, var2, var3);
    }

    public static CompoundTag captureBound(ServerPlayer var0, ItemStack var1, Kind var2, InteractionHand var3) {
        FoodWeaponSourceLedger.NativeMaterialCapture var4 = null;

        try {
            var4 = FoodWeaponSourceLedger.nativeMaterialCaptureEntered(var0, var1, var3);
        } catch (Throwable var12) {
        }

        try {
            if (!var1.isEmpty()
                && (var1.get(DataComponents.CUSTOM_DATA) == null || FoodWeaponSourceLedger.canonicalInputValid(var1.get(DataComponents.CUSTOM_DATA).copyTag()))
                && FoodWeaponSourceLedger.canonicalInputValid(var1.saveOptional(var0.registryAccess()))
                && (var2 != Kind.MELEE || FourWeaponFoundationAdapter.applies(var1))
                && !FoodWeaponLifecycle.isBroken(var1)) {
                FoodWeaponLifecycle.ensureTracked(var1);
                CompoundTag var14 = FoodWeaponData.read(var1, "FTBossWeaponMaterials");
                CompoundTag var6 = var14 == null ? new CompoundTag() : var14.copy();
                var6.putInt("SnapshotVersion", 3);
                var6.putUUID("Owner", var0.getUUID());
                var6.putString("Family", var2.name());
                Build var7 = FourWeaponFoundationAdapter.applies(var1)
                    ? FourWeaponFoundationAdapter.combat(var1)
                    : FoodWeaponFamilyRules.build(var2, WeaponMaterialBuild.materials(var1));
                var6.putDouble("ProjectileDamage", FoodWeaponFamilyRules.damageMultiplier(var7));
                var6.putDouble("LaunchSpeed", FoodWeaponFamilyRules.rangeMultiplier(var7));
                var6.put("Weapon", var1.saveOptional(var0.registryAccess()));
                WeaponMaterialPowers.snapshot(var1, var6);
                CompoundTag var8 = FoodWeaponSourceLedger.decorate(var0, var1, var6, var3);

                try {
                    FoodWeaponSourceLedger.nativeMaterialCaptureReturned(var0, var1, var3, var8);
                } catch (Throwable var11) {
                }

                return var8;
            } else {
                return new CompoundTag();
            }
        } catch (Throwable var13) {
            Throwable var5 = var13;

            try {
                FoodWeaponSourceLedger.nativeMaterialCaptureFailed(var4, var5);
            } catch (Throwable var10) {
            }

            throw var13;
        }
    }

    public static void launch(Entity var0, CompoundTag var1) {
        FoodWeaponSourceLedger.NativeMaterialTransfer var2 = null;

        try {
            var2 = FoodWeaponSourceLedger.nativeMaterialLaunchEntered(var0, var1);
        } catch (Throwable var19) {
        }

        try {
            if (var0 instanceof Projectile var20
                && var20.getOwner() instanceof ServerPlayer var4
                && owned(var1, var4)
                && !var1.getString("SourceKind").equals("TRIDENT")
                && !var0.getPersistentData().contains("FTBossWeaponMaterials", 10)) {
                CompoundTag var21 = var0.getPersistentData();
                String var6 = "FTBossWeaponMaterials";
                CompoundTag var7 = var1.copy();
                FoodWeaponSourceLedger.NativeMaterialPublication var8 = null;

                try {
                    var8 = FoodWeaponSourceLedger.nativeMaterialPutEntered(var2, var0, var21, var6, var1, var7);
                } catch (Throwable var17) {
                }

                Tag var9;
                try {
                    var9 = var21.put(var6, var7);
                } catch (Throwable var16) {
                    Throwable var10 = var16;

                    try {
                        FoodWeaponSourceLedger.nativeMaterialPutFailed(var8, var10);
                    } catch (Throwable var13) {
                    }

                    throw var16;
                }

                try {
                    FoodWeaponSourceLedger.nativeMaterialPutReturned(var8, var9);
                } catch (Throwable var15) {
                    Throwable var22 = var15;

                    try {
                        FoodWeaponSourceLedger.nativeMaterialObservationFailed(var8, var22);
                    } catch (Throwable var14) {
                    }
                }

                return;
            }
        } catch (Throwable var18) {
            Throwable var3 = var18;

            try {
                FoodWeaponSourceLedger.nativeMaterialTransferFailed(var2, var3);
            } catch (Throwable var12) {
            }

            throw var18;
        }
    }

    public static void prepareBowShot(Entity var0, CompoundTag var1) {
        FoodWeaponSourceLedger.NativeMaterialTransfer var2 = null;

        try {
            var2 = FoodWeaponSourceLedger.nativeMaterialPendingEntered(var0, var1);
        } catch (Throwable var17) {
        }

        try {
            if (var0 instanceof AbstractArrow var18
                && var18.getOwner() instanceof ServerPlayer var4
                && owned(var1, var4)
                && var1.getString("Family").equals(Kind.BOW.name())
                && !var0.getPersistentData().contains("FTBossWeaponMaterials", 10)
                && !var0.getPersistentData().contains("FTBossPendingBowShot", 10)) {
                CompoundTag var19 = var0.getPersistentData();
                String var6 = "FTBossPendingBowShot";
                CompoundTag var7 = var1.copy();

                try {
                    FoodWeaponSourceLedger.nativeMaterialPendingCopied(var2, var19, var6, var1, var7);
                } catch (Throwable var15) {
                    Throwable var8 = var15;

                    try {
                        FoodWeaponSourceLedger.nativeMaterialTransferObservationFailed(var2, var8);
                    } catch (Throwable var14) {
                    }
                }

                Tag var20 = var19.put(var6, var7);

                try {
                    FoodWeaponSourceLedger.nativeMaterialPendingReturned(var2, var20);
                } catch (Throwable var13) {
                    Throwable var9 = var13;

                    try {
                        FoodWeaponSourceLedger.nativeMaterialTransferObservationFailed(var2, var9);
                    } catch (Throwable var12) {
                    }
                }

                return;
            }
        } catch (Throwable var16) {
            Throwable var3 = var16;

            try {
                FoodWeaponSourceLedger.nativeMaterialTransferFailed(var2, var3);
            } catch (Throwable var11) {
            }

            throw var16;
        }
    }

    public static void launchPreparedBow(Entity var0) {
        FoodWeaponSourceLedger.NativeMaterialTransfer var1 = null;

        try {
            var1 = FoodWeaponSourceLedger.nativeMaterialPendingReadEntered(var0);
        } catch (Throwable var12) {
        }

        try {
            CompoundTag var13 = var0.getPersistentData();
            String var3 = "FTBossPendingBowShot";
            CompoundTag var4 = var13.getCompound(var3);
            CompoundTag var5 = var0.getPersistentData();
            var5.remove("FTBossPendingBowShot");

            try {
                FoodWeaponSourceLedger.nativeMaterialPendingTaken(var1, var13, var3, var4, var5);
            } catch (Throwable var10) {
                Throwable var6 = var10;

                try {
                    FoodWeaponSourceLedger.nativeMaterialTransferObservationFailed(var1, var6);
                } catch (Throwable var9) {
                }
            }

            launch(var0, var4);
        } catch (Throwable var11) {
            Throwable var2 = var11;

            try {
                FoodWeaponSourceLedger.nativeMaterialTransferFailed(var1, var2);
            } catch (Throwable var8) {
            }

            throw var11;
        }
    }

    public static void projectileJoin(EntityJoinLevelEvent var0) {
        if (!var0.getLevel().isClientSide
            && var0.getEntity() instanceof Projectile var1
            && var1.getOwner() instanceof ServerPlayer var2
            && !var1.getPersistentData().contains("FTBossWeaponMaterials", 10)) {
            FoodWeaponRanged.CastState var5 = CAST.get();
            if (var5 != null && var5.player() == var2) {
                launch(var1, var5.snapshot());
            }
        }
    }

    private static boolean owned(CompoundTag var0, ServerPlayer var1) {
        return FoodWeaponSourceLedger.validReadOnly(var0, var1);
    }

    public static CompoundTag materialSnapshot(DamageSource var0, ServerPlayer var1) {
        return materialSnapshotReadOnly(var0, var1);
    }

    static CompoundTag scopedSnapshot(DamageSource var0, ServerPlayer var1) {
        FoodWeaponRanged.CastState var2 = CAST.get();
        if (var0.getDirectEntity() == var1
            && var2 != null
            && var2.player() == var1
            && !var2.snapshot().getString("SourceKind").equals("TRIDENT")
            && var0.getEntity() == var1
            && var0.is(FoodWeaponDamageSources.PRIMARY)
            && "GENERIC_J".equals(var2.snapshot().getString("DamagePurpose"))) {
            return owned(var2.snapshot(), var1) ? var2.snapshot().copy() : new CompoundTag();
        } else {
            return new CompoundTag();
        }
    }

    static CompoundTag scopedSnapshotReadOnly(DamageSource var0, ServerPlayer var1) {
        FoodWeaponRanged.CastState var2 = CAST.get();
        return var0.getDirectEntity() == var1
                && var2 != null
                && var2.player() == var1
                && !var2.snapshot().getString("SourceKind").equals("TRIDENT")
                && var0.getEntity() == var1
                && var0.is(FoodWeaponDamageSources.PRIMARY)
                && "GENERIC_J".equals(var2.snapshot().getString("DamagePurpose"))
            ? var2.snapshot().copy()
            : new CompoundTag();
    }

    public static CompoundTag materialSnapshotReadOnly(DamageSource var0, ServerPlayer var1) {
        FoodWeaponRanged.HitState var2 = HIT.get();
        if (var2 == null) {
            return FoodWeaponSourceLedger.resolveReadOnly(var0, var1);
        } else {
            return var2.source() == var0 && var2.player() == var1 && !var2.nested() && FoodWeaponSourceLedger.validReadOnly(var2.snapshot(), var1)
                ? var2.snapshot().copy()
                : new CompoundTag();
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

    public static List<Material> attackMaterials(DamageSource var0, ServerPlayer var1) {
        CompoundTag var2 = materialSnapshot(var0, var1);
        if (var2.isEmpty()) {
            return List.of();
        } else {
            ArrayList var3 = new ArrayList();

            for (Part var7 : Part.values()) {
                var3.add(Material.byId(var2.getString(var7.name())));
            }

            return List.copyOf(var3);
        }
    }

    public static ItemStack sourceWeapon(DamageSource var0, ServerPlayer var1) {
        if (!allowsWeaponEffects(var0)) {
            return ItemStack.EMPTY;
        } else {
            CompoundTag var2 = materialSnapshot(var0, var1);
            if (!var2.isEmpty()) {
                return ItemStack.parseOptional(var1.registryAccess(), var2.getCompound("Weapon"));
            } else {
                return var0.getDirectEntity() == var1 && var0.is(DamageTypes.PLAYER_ATTACK) && !magicSource(var0) ? var1.getMainHandItem() : ItemStack.EMPTY;
            }
        }
    }

    public static void prepareHitTimers(ServerPlayer var0, DamageSource var1, CompoundTag var2) {
        if (!materialSnapshot(var1, var0).isEmpty()) {
            CompoundTag var3 = var0.getPersistentData().getCompound("FTBossRangedProcTimers");

            for (String var5 : var3.getAllKeys()) {
                if (var5.startsWith("Last") && var3.contains(var5, 4)) {
                    var2.putLong(var5, Math.max(var2.getLong(var5), var3.getLong(var5)));
                }
            }
        }
    }

    public static void saveHitTimers(ServerPlayer var0, DamageSource var1, CompoundTag var2) {
        if (!materialSnapshot(var1, var0).isEmpty()) {
            CompoundTag var3 = var0.getPersistentData().getCompound("FTBossRangedProcTimers");

            for (String var5 : var2.getAllKeys()) {
                if (var5.startsWith("Last") && var2.contains(var5, 4)) {
                    var3.putLong(var5, Math.max(var2.getLong(var5), var3.getLong(var5)));
                }
            }

            var0.getPersistentData().put("FTBossRangedProcTimers", var3);
        }
    }

    public static boolean canCast(ServerPlayer var0, ItemStack var1) {
        return var1 != null && FoodWeaponFamilyBuild.kind(var1) == Kind.STAFF
            ? (var0.getMainHandItem() == var1 || var0.getOffhandItem() == var1) && !FoodWeaponLifecycle.isBroken(var1)
            : true;
    }

    public static boolean allowsWeaponEffects(DamageSource var0) {
        FoodWeaponRanged.HitState var1 = HIT.get();
        return var1 == null || !var1.nested() && var1.source() == var0;
    }

    public static <Result> Result withHitSource(DamageSource var0, Supplier<Result> var1) {
        return withHitSource(null, var0, var1);
    }

    public static <Result> Result withHitSource(Entity var0, DamageSource var1, Supplier<Result> var2) {
        FoodWeaponRanged.HitState var3 = HIT.get();
        ServerPlayer var4 = var1.getEntity() instanceof ServerPlayer var5 ? var5 : null;
        CompoundTag var14 = var3 == null && var4 != null ? materialSnapshot(var1, var4).copy() : new CompoundTag();
        boolean var15 = FoodWeaponSourceLedger.actualHurtSnapshot(var14) && !FoodWeaponSourceLedger.matchesNativeHurt(var1, var4, var0, var14);
        FoodWeaponRanged.HitState var7 = new FoodWeaponRanged.HitState(
            var1, var4, var0, var15 ? new CompoundTag() : var14, var3 != null || var15, Thread.currentThread()
        );
        HIT.set(var7);

        Object var8;
        try {
            var8 = var2.get();
        } finally {
            if (HIT.get() == var7) {
                if (var3 == null) {
                    HIT.remove();
                } else {
                    HIT.set(var3);
                    boolean var11 = var3.thread() == Thread.currentThread()
                        && (
                            var3.player() == null
                                || var3.snapshot().isEmpty()
                                || owned(var3.snapshot(), var3.player())
                                    && (
                                        !FoodWeaponSourceLedger.actualHurtSnapshot(var3.snapshot())
                                            || FoodWeaponSourceLedger.matchesNativeHurt(var3.source(), var3.player(), var3.target(), var3.snapshot())
                                    )
                        );
                    if (!var11) {
                        HIT.remove();
                        FoodWeaponSourceLedger.revokeActualHurt(var3.source(), var3.player());
                    }
                }
            } else {
                FoodWeaponSourceLedger.revokeActualHurt(var1, var4);
                if (var3 != null) {
                    FoodWeaponSourceLedger.revokeActualHurt(var3.source(), var3.player());
                }
            }
        }

        return (Result)var8;
    }

    public static <Result> Result withSource(ServerPlayer var0, CompoundTag var1, Supplier<Result> var2) {
        FoodWeaponRanged.CastState var3 = CAST.get();
        if (var0 != null && owned(var1, var0)) {
            CAST.set(new FoodWeaponRanged.CastState(var0, var1.copy()));
        } else {
            CAST.remove();
        }

        Object var4;
        try {
            var4 = var2.get();
        } finally {
            if (var3 != null && owned(var3.snapshot(), var3.player())) {
                CAST.set(var3);
            } else {
                CAST.remove();
            }
        }

        return (Result)var4;
    }

    public static Runnable deferSource(Runnable var0) {
        FoodWeaponRanged.CastState var1 = CAST.get();
        ServerPlayer var2 = var1 == null ? null : var1.player();
        CompoundTag var3 = var1 == null ? new CompoundTag() : var1.snapshot().copy();
        return () -> withSource(var2, var3, () -> {
                var0.run();
                return null;
            });
    }

    public static void tickSource(Entity var0) {
        ServerPlayer var10000;
        label12: {
            if (var0 instanceof Projectile var3 && var3.getOwner() instanceof ServerPlayer var2) {
                var10000 = var2;
                break label12;
            }

            var10000 = null;
        }

        ServerPlayer var1 = var10000;
        CompoundTag var5 = var0.getPersistentData().getCompound("FTBossWeaponMaterials");
        withSource(var1, var5, () -> {
            var0.tick();
            return null;
        });
    }

    public static void withStaffCast(ServerPlayer var0, ItemStack var1, int var2, Runnable var3) {
        withStaffCast(var0, var1, var2, null, var3);
    }

    public static void withStaffCast(ServerPlayer var0, ItemStack var1, int var2, CompoundTag var3, Runnable var4) {
        boolean var5 = var1 != null && FoodWeaponFamilyBuild.kind(var1) == Kind.STAFF;
        if (!var5 || canCast(var0, var1)) {
            withSource(null, new CompoundTag(), () -> {
                var4.run();
                return null;
            });
        }
    }

    private static record CastState(ServerPlayer player, CompoundTag snapshot) {
    }

    private static record ChargeState(ItemStack weapon, int remaining, double carry) {
    }

    private static record HitState(DamageSource source, ServerPlayer player, Entity target, CompoundTag snapshot, boolean nested, Thread thread) {
    }
}
