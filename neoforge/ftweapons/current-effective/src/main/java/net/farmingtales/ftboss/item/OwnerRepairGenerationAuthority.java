package net.farmingtales.ftboss.item;

import java.io.IOException;
import java.lang.StackWalker.Option;
import java.security.MessageDigest;
import java.util.UUID;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.farmingtales.ftboss.item.OwnerRepairExpectedImage.Plan;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

final class OwnerRepairGenerationAuthority {
    private OwnerRepairGenerationAuthority() {
    }

    static Object current(Object var0) {
        return OwnerFullRepairGuard.generation(var0);
    }

    static boolean publishTaskOrigin(OwnerStatueTaskOrigin.Publication var0, OwnerLineageRevocationStore.Lease var1) {
        boolean var2 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerStatueTaskOrigin.class && var0xx.getMethodName().equals("outerCommitted"))
                        .orElse(false)
            );
        if (var2 && var0 != null && var0.candidate(var1) && OwnerLineageRevocationStore.liveLease(var1)) {
            ItemStack var3 = var0.actualForPublisher();
            if (var3 == null) {
                return false;
            } else {
                synchronized (OwnerFullRepairGuard.class) {
                    if (knownStackPolicy(var3)
                        && current(var3) == null
                        && OwnerFullRepairGuard.lease(var3) == null
                        && OwnerFullRepairGuard.intent(var3) == null
                        && var0.actualForPublisher() == var3) {
                        OwnerRepairGenerationAuthority.Generation var5 = new OwnerRepairGenerationAuthority.Generation(var1, false);
                        OwnerFullRepairGuard.freshGeneration(var3, var1, var5);
                        if (current(var3) == var5 && OwnerFullRepairGuard.lease(var3) == var1 && var0.actualForPublisher() == var3) {
                            var5.repaired = true;
                            if (!var0.bindContinuation(var5)) {
                                var5.retired = true;
                                return false;
                            } else {
                                return true;
                            }
                        } else {
                            var5.retired = true;
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
    }


    static boolean publishNativeRepair(NativeFoodWeaponWear.Claim claim, ItemStack actual, OwnerLineageRevocationStore.Lease lease) {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == NativeFoodWeaponWear.class && frame.getMethodName().equals("committedStatueRepair")).orElse(false));
        if (!caller || claim == null || lease == null) throw new IllegalStateException("Native repair publisher caller or authority missing");
        if (!nativeTaskObjectPolicy(actual)) throw new IllegalStateException("Native repair publisher object policy rejected");
        if (!claim.owns(actual, lease)) throw new IllegalStateException("Native repair publisher lease custody rejected");
        if (!claim.capabilities().live()) throw new IllegalStateException("Native repair publisher capabilities changed");
        synchronized (OwnerFullRepairGuard.class) {
            if (OwnerFullRepairGuard.intent(actual) != null) throw new IllegalStateException("Native repair publisher conflicting intent");
            if (!claim.expected().equals(NativeFoundationItemData.image(actual))) throw new IllegalStateException("Native repair publisher image changed");
            Generation generation = new Generation(lease, false);
            generation.nativeCapabilities = claim.capabilities();
            NativeCuriosProviderFields260.watchRepair(actual);
            OwnerFullRepairGuard.freshGeneration(actual, lease, generation);
            if (current(actual) != generation || OwnerFullRepairGuard.lease(actual) != lease) throw new IllegalStateException("Native repair publisher physical generation rejected");
            generation.repaired = true;
            return true;
        }
    }

    static boolean publishNativeReload(OwnerStatueWearAdmission.Load context, int slot, ItemStack actual,
                                       OwnerLineageRevocationStore.Lease lease, NativeItemCapabilityObservation capabilities) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerStatueWearAdmission.class || !caller.getMethodName().equals("restoreNativeLoaded")
            || !nativeTaskObjectPolicy(actual) || lease == null || capabilities == null || !capabilities.live()
            || !OwnerStatueWearAdmission.nativeReloadCandidate(context, slot, actual, lease, capabilities)
            || !OwnerLineageRevocationStore.liveLease(lease)) throw new IllegalStateException("Native original-load successor publication rejected");
        NativeCuriosProviderFields260.retireReloadPredecessors(actual, lease);
        synchronized (OwnerFullRepairGuard.class) {
            if (OwnerFullRepairGuard.intent(actual) != null || OwnerFullRepairGuard.lease(actual) != null || current(actual) != null)
                throw new IllegalStateException("Native loaded physical item already has repair authority");
            Generation generation = new Generation(lease, false);
            generation.nativeCapabilities = capabilities;
            NativeCuriosProviderFields260.watchRepair(actual);
            OwnerFullRepairGuard.freshGeneration(actual, lease, generation);
            if (current(actual) != generation || OwnerFullRepairGuard.lease(actual) != lease)
                throw new IllegalStateException("Native loaded physical generation installation failed");
            generation.repaired = true;
            return true;
        }
    }

    static boolean fresh(Object var0) {
        if (!nativePolicyCurrent(var0)) {
            unknownMutation(var0);
            return false;
        }
        Object var1;
        OwnerLineageRevocationStore.Lease var2;
        synchronized (OwnerFullRepairGuard.class) {
            var1 = current(var0);
            var2 = OwnerFullRepairGuard.lease(var0);
            if (!repaired(var0, var2)) {
                return false;
            }
        }

        if (!OwnerLineageRevocationStore.liveLease(var2)) {
            return false;
        } else {
            synchronized (OwnerFullRepairGuard.class) {
                return current(var0) == var1 && repaired(var0, var2);
            }
        }
    }

    private static OwnerRepairGenerationAuthority.Intent preparePlan(Object var0, Object var1, OwnerFieldExpectedAuthority.Prepared var2, Plan var3) {
        net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Plan var4 = var2 == null ? null : OwnerFieldExpectedAuthority.expected(var2);
        boolean var5 = var4 != null && var4.size() != 0;
        OwnerRepairGenerationAuthority.Intent var6;
        synchronized (FoodWeaponSourceLedger.class) {
            label165: {
                Object var10000;
                synchronized (OwnerFullRepairGuard.class) {
                    if (var0 == null
                        || !knownStackPolicy(var1)
                        || var2 == null
                        || var2.issuer != var0
                        || var3 == null
                        || !OwnerFieldExpectedAuthority.covered(var2)
                        || !var5
                        || OwnerFullRepairGuard.intent(var1) != null) {
                        return null;
                    }

                    OwnerLineageRevocationStore.Lease var9 = OwnerFullRepairGuard.lease(var1);
                    if (var9 == null) {
                        return null;
                    }

                    var6 = new OwnerRepairGenerationAuthority.Intent(var0, var1, var2, var3);
                    OwnerFullRepairGuard.intent(var1, var6);
                    if (OwnerFullRepairGuard.intent(var1) == var6) {
                        break label165;
                    }

                    var10000 = null;
                }

                return (OwnerRepairGenerationAuthority.Intent)var10000;
            }
        }

        try {
            boolean var21 = OwnerLineageRevocationStore.liveLease(var6.priorLease);
            synchronized (FoodWeaponSourceLedger.class) {
                OwnerRepairGenerationAuthority.Intent var24;
                synchronized (OwnerFullRepairGuard.class) {
                    if (!var21
                        || !var3.weapon.equals(var6.priorLease.weapon)
                        || !issued(var6)
                        || current(var1) != var6.priorGeneration
                        || OwnerFullRepairGuard.lease(var1) != var6.priorLease
                        || !knownStackPolicy(var1)
                        || !OwnerFieldExpectedAuthority.covered(var2)
                        || !var5) {
                        failed(var6, null);
                        return null;
                    }

                    var24 = var6;
                }

                return var24;
            }
        } catch (Error | RuntimeException var17) {
            Throwable var20 = var17;

            try {
                failed(var6, var20);
            } catch (Error | RuntimeException var14) {
            }

            throw var17;
        }
    }

    static OwnerRepairGenerationAuthority.Intent prepare(Object var0, Object var1, OwnerFieldExpectedAuthority.Prepared var2, byte[] var3, int var4) {
        return preparePlan(var0, var1, var2, OwnerRepairExpectedImage.statue(var3, var4));
    }

    static OwnerRepairGenerationAuthority.Intent prepareReduction(Object var0, Object var1, OwnerFieldExpectedAuthority.Prepared var2, byte[] var3, int var4) {
        return preparePlan(var0, var1, var2, OwnerRepairExpectedImage.reduction(var3, var4));
    }

    static boolean statueTaskObjectPolicy(Object var0) {
        return knownStackPolicy(var0);
    }

    static boolean nativeTaskObjectPolicy(Object actual) {
        if (actual == null || actual.getClass() != ItemStack.class || !(actual instanceof ItemStack stack) || stack.getCount() != 1
            || !NativeFoodWeaponWear.eligible(stack) || !(actual instanceof OwnerRepairCapabilityFields)) return false;
        Class<?> type = stack.getItem().getClass();
        return type == SwordItem.class || type == BowItem.class || type == CrossbowItem.class || type == AxeItem.class
            || type == TridentItem.class || type == FrostQueenStaffItem.class || nativeMaterialRangedPolicy(stack);
    }

    static boolean nativeMaterialRangedPolicy(ItemStack stack) {
        if (stack == null || stack.getCount() != 1) return false;
        Item item = stack.getItem();
        Class<?> type = item.getClass();
        boolean bow = type == net.farmingtales.ftboss.item.weapons.MaterialBowItem.class;
        if (!bow && type != net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem.class) return false;
        net.minecraft.resources.ResourceLocation identity = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
        if (identity == null || !identity.getNamespace().equals("ftboss")
            || net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(identity).orElse(null) != item) return false;
        String suffix = bow ? "_bow" : "_crossbow";
        String path = identity.getPath();
        if (!path.endsWith(suffix)) return false;
        MaterialBowItems.Kind expected = switch (path.substring(0, path.length() - suffix.length())) {
            case "wooden" -> MaterialBowItems.Kind.WOOD;
            case "stone" -> MaterialBowItems.Kind.STONE;
            case "iron" -> MaterialBowItems.Kind.IRON;
            case "golden" -> MaterialBowItems.Kind.GOLD;
            case "diamond" -> MaterialBowItems.Kind.DIAMOND;
            case "netherite" -> MaterialBowItems.Kind.NETHERITE;
            default -> null;
        };
        if (expected == null) return false;
        net.minecraft.world.item.Tiers expectedTier = switch (path.substring(0, path.length() - suffix.length())) {
            case "wooden" -> net.minecraft.world.item.Tiers.WOOD;
            case "stone" -> net.minecraft.world.item.Tiers.STONE;
            case "iron" -> net.minecraft.world.item.Tiers.IRON;
            case "golden" -> net.minecraft.world.item.Tiers.GOLD;
            case "diamond" -> net.minecraft.world.item.Tiers.DIAMOND;
            default -> net.minecraft.world.item.Tiers.NETHERITE;
        };
        if (expected.tier != expectedTier) return false;
        try {
            Class<?> original = bow ? BowItem.class : CrossbowItem.class;
            if (!java.lang.reflect.Modifier.isFinal(type.getModifiers())
                || type.getMethod("use", net.minecraft.world.level.Level.class, net.minecraft.world.entity.player.Player.class,
                    net.minecraft.world.InteractionHand.class).getDeclaringClass() != original
                || type.getMethod("releaseUsing", ItemStack.class, net.minecraft.world.level.Level.class,
                    net.minecraft.world.entity.LivingEntity.class, int.class).getDeclaringClass() != original) return false;
            if (bow) {
                if (((net.farmingtales.ftboss.item.weapons.MaterialBowItem)item).kind() != expected || !expected.id().equals(identity)) return false;
            } else {
                java.lang.reflect.Field tier = type.getDeclaredField("tier");
                if (tier.getType() != net.minecraft.world.item.Tier.class || !java.lang.reflect.Modifier.isFinal(tier.getModifiers())
                    || !tier.trySetAccessible() || tier.get(item) != expectedTier) return false;
            }
            try (java.io.InputStream bytes = type.getResourceAsStream("/" + type.getName().replace('.', '/') + ".class")) {
                if (bytes == null) return false;
                String expectedDigest = bow ? "63152f97b9356aff02c6f6c44a07cdbd048d2538504dbf82593017bcdee33129"
                    : "08dba713dc75071b0af18d8a523e15474f5f15c8f9ffa55dc06b2badd1d82006";
                return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes.readAllBytes())).equals(expectedDigest);
            }
        } catch (ReflectiveOperationException | IOException | java.security.NoSuchAlgorithmException | SecurityException rejected) {
            return false;
        }
    }

    private static boolean nativePolicyCurrent(Object actual) {
        Object generation = current(actual);
        if (!(generation instanceof Generation value) || value.nativeCapabilities == null) return true;
        if (!value.nativeCapabilities.live()) return false;
        if (actual instanceof ItemStack stack
            && (stack.getItem().getClass() == net.farmingtales.ftboss.item.weapons.MaterialBowItem.class
                || stack.getItem().getClass() == net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem.class))
            return nativeMaterialRangedPolicy(stack);
        return true;
    }

    private static boolean knownStackPolicy(Object var0) {
        if (var0 != null && var0.getClass() == ItemStack.class && var0 instanceof ItemStack stack
            && var0 instanceof OwnerRepairCapabilityFields capabilities && stack.getCount() == 1
            && capabilities.ftboss$ownerRepairInitializedNoProviders()) {
            Item item = stack.getItem();
            if (item == null) return false;
            Class<?> type = item.getClass();
            return type == SwordItem.class || type == BowItem.class || type == CrossbowItem.class || type == TridentItem.class;
        }
        return false;
    }

    private static boolean issued(OwnerRepairGenerationAuthority.Intent var0) {
        return var0 != null && var0.thread == Thread.currentThread() && !var0.closed && !var0.rejected && OwnerFullRepairGuard.intent(var0.actual) == var0;
    }

    static void entered(OwnerRepairGenerationAuthority.Intent var0, Object var1) {
        try {
            synchronized (FoodWeaponSourceLedger.class) {
                label141: {
                    synchronized (OwnerFullRepairGuard.class) {
                        if (!issued(var0)) {
                            return;
                        }

                        if (var0.actual == var1
                            && !var0.entered
                            && current(var1) == var0.priorGeneration
                            && OwnerFullRepairGuard.lease(var1) == var0.priorLease
                            && OwnerFieldExpectedAuthority.covered(var0.ancestry)) {
                            var0.entered = true;
                            if (var0.priorGeneration instanceof OwnerRepairGenerationAuthority.Generation var4) {
                                var4.retired = true;
                            }
                            break label141;
                        }

                        failed(var0, null);
                    }

                    return;
                }
            }

            OwnerLineageRevocationStore.Advance var18;
            try {
                var18 = OwnerLineageRevocationStore.beginLease(var0.priorLease, var0.transaction);
            } catch (IOException var11) {
                failed(var0, var11);
                return;
            }

            synchronized (FoodWeaponSourceLedger.class) {
                synchronized (OwnerFullRepairGuard.class) {
                    if (var18 == null
                        || !issued(var0)
                        || var0.actual != var1
                        || current(var1) != var0.priorGeneration
                        || OwnerFullRepairGuard.lease(var1) != var0.priorLease
                        || !OwnerFieldExpectedAuthority.covered(var0.ancestry)) {
                        failed(var0, null);
                        return;
                    }

                    var0.advance = var18;
                }
            }
        } catch (Error | RuntimeException var16) {
            Throwable var2 = var16;

            try {
                failed(var0, var2);
            } catch (Error | RuntimeException var10) {
            }

            throw var16;
        }
    }

    static void observed(OwnerRepairGenerationAuthority.Intent var0, Object var1, int var2, byte[] var3) {
        synchronized (FoodWeaponSourceLedger.class) {
            synchronized (OwnerFullRepairGuard.class) {
                if (!issued(var0)) {
                    return;
                }

                if (!var0.entered
                    || var0.actual != var1
                    || var2 != var0.next
                    || var2 >= var0.plan.prefixes.length
                    || var3 == null
                    || !MessageDigest.isEqual(var0.plan.prefixes[var2], var3)
                    || !FoodWeaponSourceLedger.ownerRepairAcceptedIntentLive(var0.issuer, var0.ancestry)) {
                    failed(var0, null);
                    return;
                }

                var0.next++;
            }
        }
    }

    static void observedValue(OwnerRepairGenerationAuthority.Intent var0, Object var1, int var2, Value var3) {
        synchronized (FoodWeaponSourceLedger.class) {
            synchronized (OwnerFullRepairGuard.class) {
                if (!issued(var0)) {
                    return;
                }

                if (!var0.entered
                    || var0.actual != var1
                    || var2 != var0.next
                    || var2 >= var0.plan.prefixes.length
                    || !FoodWeaponExpectedTrace.same(FoodWeaponExpectedFieldsCodec.decodeD084(var0.plan.prefixes[var2]), var3)
                    || !FoodWeaponSourceLedger.ownerRepairAcceptedIntentLive(var0.issuer, var0.ancestry)) {
                    failed(var0, null);
                    return;
                }

                var0.next++;
            }
        }
    }

    static boolean completed(OwnerRepairGenerationAuthority.Intent var0, Object var1, boolean var2) {
        OwnerLineageRevocationStore.Advance var3 = null;
        OwnerLineageRevocationStore.Lease var4 = null;
        boolean var5 = false;
        Object var6 = null;

        boolean var10;
        try {
            synchronized (FoodWeaponSourceLedger.class) {
                synchronized (OwnerFullRepairGuard.class) {
                    if (!issued(var0)) {
                        return false;
                    }

                    if (!var2
                        || !knownStackPolicy(var1)
                        || var0.actual != var1
                        || !var0.entered
                        || var0.next != var0.plan.prefixes.length
                        || var0.originalFailure != null
                        || var0.advance == null
                        || var0.committing
                        || current(var1) != var0.priorGeneration
                        || OwnerFullRepairGuard.lease(var1) != var0.priorLease
                        || !FoodWeaponSourceLedger.ownerRepairAcceptedIntentLive(var0.issuer, var0.ancestry)) {
                        failed(var0, null);
                        return false;
                    }

                    var0.committing = true;
                    var3 = var0.advance;
                }
            }

            var4 = OwnerLineageRevocationStore.commitAdvance(var3);
            if (var4 == null || !OwnerLineageRevocationStore.liveLease(var4)) {
                failed(var0, null);
                return false;
            }

            synchronized (FoodWeaponSourceLedger.class) {
                synchronized (OwnerFullRepairGuard.class) {
                    if (!issued(var0)
                        || !var0.committing
                        || var0.advance != var3
                        || var0.actual != var1
                        || !knownStackPolicy(var1)
                        || var0.next != var0.plan.prefixes.length
                        || var0.originalFailure != null
                        || current(var1) != var0.priorGeneration
                        || OwnerFullRepairGuard.lease(var1) != var0.priorLease
                        || !FoodWeaponSourceLedger.ownerRepairAcceptedIntentLive(var0.issuer, var0.ancestry)) {
                        failed(var0, null);
                        return false;
                    }

                    OwnerRepairGenerationAuthority.Generation var9 = new OwnerRepairGenerationAuthority.Generation(var4, false);
                    OwnerFullRepairGuard.freshGeneration(var1, var4, var9);
                    if (!issued(var0) || current(var1) != var9 || OwnerFullRepairGuard.lease(var1) != var4) {
                        failed(var0, null);
                        return false;
                    }

                    var9.repaired = true;
                    var0.closed = true;
                    if (OwnerFullRepairGuard.intent(var1) == var0) {
                        OwnerFullRepairGuard.intent(var1, null);
                    }

                    var5 = true;
                    var10 = true;
                }
            }
        } catch (IOException var80) {
            var6 = var80;
            failed(var0, var80);
            return false;
        } catch (Error | RuntimeException var81) {
            Throwable var7 = var81;
            var6 = var81;

            try {
                failed(var0, var7);
            } catch (Error | RuntimeException var74) {
            }

            throw var81;
        } finally {
            Throwable var26 = null;

            try {
                synchronized (OwnerFullRepairGuard.class) {
                    if (var0 != null && var0.thread == Thread.currentThread()) {
                        var0.committing = false;
                    }

                    if (!var5 && var4 != null && current(var1) instanceof OwnerRepairGenerationAuthority.Generation var28 && var28.lease == var4) {
                        var28.retired = true;
                    }
                }
            } catch (Error | RuntimeException var73) {
                var26 = var73;
            }

            if (var3 != null && !var5) {
                try {
                    OwnerLineageRevocationStore.abortAdvance(var3);
                } catch (Error | RuntimeException var75) {
                    if (var26 == null) {
                        var26 = var75;
                    }
                }
            }

            if (var26 != null) {
                try {
                    failed(var0, (Throwable)(var6 == null ? var26 : var6));
                } catch (Error | RuntimeException var71) {
                }

                if (var6 == null) {
                    if (var26 instanceof RuntimeException var93) {
                        throw var93;
                    }

                    if (var26 instanceof Error var92) {
                        throw var92;
                    }
                }
            }
        }

        return var10;
    }

    static void failed(OwnerRepairGenerationAuthority.Intent var0, Throwable var1) {
        synchronized (OwnerFullRepairGuard.class) {
            if (var0 != null && var0.thread == Thread.currentThread()) {
                var0.rejected = true;
                var0.closed = true;
                if (var0.originalFailure == null) {
                    var0.originalFailure = var1;
                }

                if (OwnerFullRepairGuard.intent(var0.actual) == var0) {
                    OwnerFullRepairGuard.intent(var0.actual, null);
                }
            }
        }
    }

    static boolean matches(Object var0, Object var1) {
        if (!nativePolicyCurrent(var0)) {
            unknownMutation(var0);
            return false;
        }
        OwnerLineageRevocationStore.Lease var2;
        synchronized (OwnerFullRepairGuard.class) {
            var2 = OwnerFullRepairGuard.lease(var0);
            if (current(var0) != var1) {
                return false;
            }

            if (var1 instanceof OwnerRepairGenerationAuthority.Generation var4 && var4.retired) {
                return false;
            }

            if (var2 == null && !(var1 instanceof OwnerRepairGenerationAuthority.Generation)) {
                return true;
            }
        }

        OwnerLineageRevocationStore.Lease var9 = var1 instanceof OwnerRepairGenerationAuthority.Generation var10 ? var10.lease : var2;
        if (!OwnerLineageRevocationStore.liveLease(var9)) {
            return false;
        } else {
            synchronized (OwnerFullRepairGuard.class) {
                return OwnerLineageRevocationStore.locallyLive(var9)
                    && current(var0) == var1
                    && OwnerFullRepairGuard.lease(var0) == var2
                    && (!(var1 instanceof OwnerRepairGenerationAuthority.Generation var5) || !var5.retired);
            }
        }
    }

    static boolean currentSnapshotWeapon(UUID var0) {
        return var0 != null && FoodWeaponSourceLedger.ownerRepairScopedWeaponLive(var0);
    }

    static boolean currentSnapshotWeapon(UUID var0, CompoundTag var1, ServerPlayer var2) {
        return var0 != null && FoodWeaponSourceLedger.ownerRepairScopedWeaponLive(var0, var1, var2);
    }

    static boolean stamp(Object var0, CompoundTag var1, OwnerLineageRevocationStore var2) {
        boolean var3 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(var0x -> var0x.skip(1L).findFirst().map(var0xx -> var0xx.getDeclaringClass() == FoodWeaponSourceLedger.class).orElse(false));
        if (var3 && var2 != null && var1 != null && var1.hasUUID("Owner") && var1.hasUUID("WeaponId") && OwnerFullRepairGuard.permits(var0)) {
            UUID var4 = var1.getUUID("Owner");
            UUID var5 = var1.getUUID("WeaponId");
            OwnerLineageRevocationStore.Lease var6;
            Object var7;
            synchronized (OwnerFullRepairGuard.class) {
                var6 = OwnerFullRepairGuard.lease(var0);
                var7 = current(var0);
            }

            boolean var37 = var6 == null;
            boolean var9 = false;
            Object var10 = null;

            boolean var40;
            try {
                if (var37) {
                    var6 = var2.admit(var4, var5);
                    synchronized (OwnerFullRepairGuard.class) {
                        if (current(var0) != var7 || OwnerFullRepairGuard.lease(var0) != null) {
                            return false;
                        }

                        OwnerFullRepairGuard.lease(var0, var6);
                    }
                }

                if (var6 == null || !var2.owns(var6) || !var6.owner.equals(var4) || !var6.weapon.equals(var5) || !OwnerLineageRevocationStore.liveLease(var6)) {
                    return false;
                }

                synchronized (OwnerFullRepairGuard.class) {
                    if (current(var0) != var7
                        || OwnerFullRepairGuard.lease(var0) != var6
                        || !var1.hasUUID("Owner")
                        || !var1.hasUUID("WeaponId")
                        || !var4.equals(var1.getUUID("Owner"))
                        || !var5.equals(var1.getUUID("WeaponId"))) {
                        return false;
                    }

                    var1.putUUID("OwnerRepairAuthorityEpoch", var6.incarnation);
                    var1.putLong("OwnerRepairAuthorityGeneration", var6.generation);
                    OwnerFullRepairGuard.lease(var1, var6);
                    var9 = OwnerFullRepairGuard.lease(var1) == var6;
                    var40 = var9;
                }
            } catch (IOException var34) {
                var10 = var34;
                return false;
            } catch (Error | RuntimeException var35) {
                var10 = var35;
                throw var35;
            } finally {
                if (var37 && !var9 && var6 != null) {
                    try {
                        OwnerLineageRevocationStore.retireLease(var6);
                    } catch (Error | RuntimeException var31) {
                        if (var10 == null) {
                            throw var31;
                        }
                    }
                }
            }

            return var40;
        } else {
            return false;
        }
    }

    static boolean snapshotAllowed(CompoundTag var0) {
        OwnerLineageRevocationStore.Lease var1;
        synchronized (OwnerFullRepairGuard.class) {
            var1 = OwnerFullRepairGuard.lease(var0);
            if (!snapshotFields(var0, var1)) {
                return false;
            }
        }

        if (!OwnerLineageRevocationStore.liveLease(var1)) {
            return false;
        } else {
            synchronized (OwnerFullRepairGuard.class) {
                return OwnerFullRepairGuard.lease(var0) == var1 && snapshotFields(var0, var1) && OwnerLineageRevocationStore.locallyLive(var1);
            }
        }
    }

    static void unknownMutation(Object var0) {
        OwnerLineageRevocationStore.retireLease(invalidate(var0));
    }

    static boolean repaired(Object var0, OwnerLineageRevocationStore.Lease var1) {
        synchronized (OwnerFullRepairGuard.class) {
            if (!nativePolicyCurrent(var0)) {
                if (current(var0) instanceof Generation generation) generation.retired = true;
                return false;
            }
            return var1 != null
                && OwnerLineageRevocationStore.locallyLive(var1)
                && current(var0) instanceof OwnerRepairGenerationAuthority.Generation var3
                && var3.repaired
                && !var3.retired
                && var3.lease == var1
                && OwnerFullRepairGuard.lease(var0) == var1;
        }
    }

    static OwnerLineageRevocationStore.Lease invalidate(Object var0) {
        synchronized (OwnerFullRepairGuard.class) {
            OwnerRepairGenerationAuthority.Intent var2 = OwnerFullRepairGuard.intent(var0);
            if (var2 != null && var2.committing) {
                var2.rejected = true;
            }

            Object var3 = current(var0);
            OwnerLineageRevocationStore.Lease var4 = OwnerFullRepairGuard.lease(var0);
            if (var3 instanceof OwnerRepairGenerationAuthority.Generation var5) {
                if (!var5.retired && var5.nativeCapabilities != null) {
                    net.farmingtales.ftboss.FTBossMod.LOGGER.error("Original native repaired generation invalidated", new IllegalStateException("First native repair generation invalidation"));
                }
                var5.retired = true;
            } else if (var4 != null) {
                OwnerRepairGenerationAuthority.Generation var6 = new OwnerRepairGenerationAuthority.Generation(var4, false);
                var6.retired = true;
                OwnerFullRepairGuard.generation(var0, var6);
            }

            return var4;
        }
    }

    static boolean identityMatches(Object var0, Object var1, OwnerLineageRevocationStore.Lease var2) {
        synchronized (OwnerFullRepairGuard.class) {
            return current(var0) == var1
                && OwnerFullRepairGuard.lease(var0) == var2
                && (!(var1 instanceof OwnerRepairGenerationAuthority.Generation var4) || !var4.retired && var4.lease == var2)
                && (var2 == null || OwnerLineageRevocationStore.locallyLive(var2));
        }
    }

    static boolean snapshotIdentity(CompoundTag var0, OwnerLineageRevocationStore.Lease var1) {
        synchronized (OwnerFullRepairGuard.class) {
            return var1 != null && OwnerLineageRevocationStore.locallyLive(var1) && OwnerFullRepairGuard.lease(var0) == var1 && snapshotFields(var0, var1);
        }
    }

    private static boolean snapshotFields(CompoundTag var0, OwnerLineageRevocationStore.Lease var1) {
        return var0 != null
            && var1 != null
            && var0.hasUUID("Owner")
            && var0.hasUUID("WeaponId")
            && var0.hasUUID("OwnerRepairAuthorityEpoch")
            && var0.contains("OwnerRepairAuthorityGeneration", 4)
            && var1.owner.equals(var0.getUUID("Owner"))
            && var1.weapon.equals(var0.getUUID("WeaponId"))
            && var1.incarnation.equals(var0.getUUID("OwnerRepairAuthorityEpoch"))
            && var1.generation == var0.getLong("OwnerRepairAuthorityGeneration");
    }

    private static final class Generation {
        private final OwnerLineageRevocationStore.Lease lease;
        private boolean repaired;
        private boolean retired;
        private NativeItemCapabilityObservation nativeCapabilities;

        private Generation(OwnerLineageRevocationStore.Lease var1, boolean var2) {
            this.lease = var1;
            this.repaired = var2;
        }
    }

    static final class Intent {
        private final Object issuer;
        private final Object actual;
        private final Thread thread = Thread.currentThread();
        private final OwnerFieldExpectedAuthority.Prepared ancestry;
        private final Plan plan;
        private final Object priorGeneration;
        private final OwnerLineageRevocationStore.Lease priorLease;
        private final UUID transaction = UUID.randomUUID();
        private OwnerLineageRevocationStore.Advance advance;
        private int next;
        private boolean entered;
        private boolean committing;
        private boolean closed;
        private boolean rejected;
        private Throwable originalFailure;

        private Intent(Object var1, Object var2, OwnerFieldExpectedAuthority.Prepared var3, Plan var4) {
            this.issuer = var1;
            this.actual = var2;
            this.ancestry = var3;
            this.plan = var4;
            this.priorGeneration = OwnerRepairGenerationAuthority.current(var2);
            this.priorLease = OwnerFullRepairGuard.lease(var2);
        }
    }
}
