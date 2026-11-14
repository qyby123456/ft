package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.Profile;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Builder;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Plan;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.State;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Actor;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Compound;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.PlayerDataStorage;

public final class OwnerFieldExpectedAuthority {
    private static final ThreadLocal<Boolean> FOUNDATION_RAW_READ = new ThreadLocal<>();

    private OwnerFieldExpectedAuthority() {
    }

    static boolean ownerCaller() {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0 -> var0.filter(
                            var0x -> var0x.getDeclaringClass() != OwnerFieldExpectedAuthority.class
                                    && var0x.getDeclaringClass() != FoodWeaponOriginalSequence.class
                        )
                        .findFirst()
                        .map(var0x -> var0x.getDeclaringClass() == FoodWeaponSourceLedger.class)
                        .orElse(false)
            );
    }

    private static boolean entityHook(String var0) {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var1 -> var1.filter(var0xx -> var0xx.getDeclaringClass() != OwnerFieldExpectedAuthority.class)
                        .findFirst()
                        .map(var1x -> var1x.getDeclaringClass() == Entity.class && var1x.getMethodName().contains(var0))
                        .orElse(false)
            );
    }

    private static OwnerFieldExpectedAuthority.DomainBefore find(Entity var0) {
        synchronized (OwnerFullRepairGuard.class) {
            return var0 instanceof OwnerFieldEntityAccess var2 ? var2.ftboss$ownerFieldDomain() : null;
        }
    }

    private static boolean install(Entity var0, OwnerFieldExpectedAuthority.DomainBefore var1) {
        synchronized (OwnerFullRepairGuard.class) {
            if (var0 instanceof OwnerFieldEntityAccess var3) {
                OwnerFieldExpectedAuthority.DomainBefore var4 = var3.ftboss$ownerFieldDomain();
                if (var4 != null) {
                    var4.retired = true;
                }

                var3.ftboss$ownerFieldInstallDomain(var1);
                return var3.ftboss$ownerFieldDomain() == var1;
            } else {
                return false;
            }
        }
    }

    public static void constructed(Entity var0, Object var1) {
        synchronized (OwnerFullRepairGuard.class) {
            if (entityHook("ftboss$ownerFieldConstructed") && var0 != null && var0 instanceof OwnerFieldEntityAccess && find(var0) == null) {
                install(var0, new OwnerFieldExpectedAuthority.DomainBefore(var0, FoodWeaponExpectedTrace.emptyCompound(), var1 == null));
            }
        }
    }

    public static void loadQuoted(Entity var0, CompoundTag var1) {
        synchronized (OwnerFullRepairGuard.class) {
            if (entityHook("ftboss$ownerFieldLoadQuoted") && var0 != null && var1 != null && var0 instanceof OwnerFieldEntityAccess) {
                List<StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE) .walk(stream -> stream.skip(1L).limit(12L).toList());
                boolean hook = frames.size() >= 3 && frames.get(0).getDeclaringClass() == Entity.class && frames.get(0).getMethodName().endsWith("$ftboss$ownerFieldLoadQuoted") && frames.get(0).getDescriptor().equals("(Lnet/minecraft/nbt/CompoundTag;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V")
                    && frames.get(1).getDeclaringClass() == Entity.class && frames.get(1).getMethodName().equals("load") && frames.get(1).getDescriptor().equals("(Lnet/minecraft/nbt/CompoundTag;)V");
                boolean recursive = hook && frames.get(2).getDeclaringClass() == EntityType.class && frames.get(2).getMethodName().equals("lambda$create$5") && frames.get(2).getDescriptor().equals("(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/entity/Entity;)V")
                    && frames.stream().anyMatch(frame -> frame.getDeclaringClass() == EntityType.class && frame.getMethodName().equals("loadEntityRecursive") && frame.getDescriptor().equals("(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/Level;Ljava/util/function/Function;)Lnet/minecraft/world/entity/Entity;"));
                boolean player = hook && frames.get(2).getDeclaringClass() == PlayerDataStorage.class && frames.get(2).getMethodName().equals("lambda$load$1") && frames.get(2).getDescriptor().equals("(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;")
                    && frames.stream().anyMatch(frame -> frame.getDeclaringClass() == PlayerDataStorage.class && frame.getMethodName().equals("load") && frame.getDescriptor().equals("(Lnet/minecraft/world/entity/player/Player;)Ljava/util/Optional;"));
                boolean var3 = recursive || player; if (!var3) {
                    changed(var0);
                } else {
                    Tag var4 = ((Compound)var1).ftboss$rawCompoundFields().get("NeoForgeData");
                    if (var4 == null && var0 instanceof Actor actor && actor.ftboss$rawPersistentData() == null) { install(var0, new OwnerFieldExpectedAuthority.DomainBefore(var0, FoodWeaponExpectedTrace.emptyCompound(), true)); } else if (!(var4 instanceof CompoundTag)) {
                        changed(var0);
                    } else {
                        OwnerFieldExpectedAuthority.DomainBefore var5 = new OwnerFieldExpectedAuthority.DomainBefore(
                            var0, NativeExpectedRawFields.observe(var4), true
                        );
                        var5.loading = true;
                        install(var0, var5);
                    }
                }
            }
        }
    }

    public static void loadAssigned(Entity var0, CompoundTag var1) {
        synchronized (OwnerFullRepairGuard.class) {
            if (entityHook("ftboss$ownerFieldLoadAssigned")) {
                OwnerFieldExpectedAuthority.DomainBefore var3 = find(var0);
                if (var3 != null
                    && var3.loading
                    && !var3.retired
                    && var0 instanceof Actor var4
                    && var4.ftboss$rawPersistentData() == var1
                    && FoodWeaponExpectedTrace.same(var3.expected, NativeExpectedRawFields.observe(var1))) {
                    var3.originalPersistent = var1;
                    var3.loading = false;
                    OwnerFullRepairGuard.fieldDomain(var1, var3);
                    return;
                }

                if (var3 != null) {
                    var3.retired = true;
                }
            }
        }
    }

    public static void persistentExposed(Entity var0, Object var1) {
        synchronized (OwnerFullRepairGuard.class) {
            OwnerFieldExpectedAuthority.DomainBefore var3 = find(var0);
            if (var3 != null) {
                if (var3.originalPersistent == null
                    && var1 != null
                    && var3.expected == FoodWeaponExpectedTrace.emptyCompound()
                    && var0 instanceof Actor var4
                    && var4.ftboss$rawPersistentData() == var1
                    && FoodWeaponExpectedTrace.same(var3.expected, NativeExpectedRawFields.observe((Tag)var1))) {
                    var3.originalPersistent = var1;
                    OwnerFullRepairGuard.fieldDomain(var1, var3);
                }

                if (var3.originalPersistent != var1) {
                    var3.retired = true;
                }
            }
        }
    }

    public static byte[] foundationCanonicalRead(Supplier<byte[]> var0) {
        boolean var1 = var0 != null
            && StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0x -> var0x.filter(var0xx -> var0xx.getDeclaringClass() != OwnerFieldExpectedAuthority.class)
                            .findFirst()
                            .map(var0xx -> var0xx.getDeclaringClass() == FoundationCanonicalDigest.class && var0xx.getMethodName().equals("canonical"))
                            .orElse(false)
                );
        if (!var1) {
            throw new IllegalStateException("Foundation raw-read scope denied");
        } else {
            Boolean var2 = FOUNDATION_RAW_READ.get();
            FOUNDATION_RAW_READ.set(Boolean.TRUE);

            byte[] var3;
            try {
                var3 = (byte[])var0.get();
            } finally {
                if (var2 == null) {
                    FOUNDATION_RAW_READ.remove();
                } else {
                    FOUNDATION_RAW_READ.set(var2);
                }
            }

            return var3;
        }
    }

    public static void rawExposed(Object var0, Object var1) {
        if (!Boolean.TRUE.equals(FOUNDATION_RAW_READ.get())) {
            List<StackFrame> var2 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(var0x -> var0x.skip(1L).filter(var0xx -> !var0xx.getMethodName().contains("ftboss$ownerRawExposed")).limit(4L).toList());
            StackFrame var3 = var2.stream().filter(var0x -> !rawAccessor(var0x)).findFirst().orElse(null);
            if (var3 != null && var3.getDeclaringClass() == OwnerNativeSemanticAuthority.class) {
                StackFrame var4 = var2.stream().filter(OwnerFieldExpectedAuthority::rawAccessor).findFirst().orElse(null);
                if (var4 != null
                    && OwnerNativeSemanticAuthority.rawReadReturned(
                            var0,
                            var1,
                            var4.getDeclaringClass(),
                            var4.getMethodName(),
                            var4.getDescriptor(),
                            var3.getDeclaringClass(),
                            var3.getMethodName(),
                            var3.getDescriptor()
                        )
                        != 0) {
                    return;
                }
            }

            boolean var5 = var3 != null
                && (
                    var3.getDeclaringClass() == OwnerFieldExpectedAuthority.class
                        || var3.getDeclaringClass().getNestHost() == NativeExpectedRawFields.class
                        || var3.getDeclaringClass().getNestHost() == NativeNbtGraphObservation.class
                        || var3.getDeclaringClass().getNestHost() == FoodWeaponSourceLedger.class
                        || var3.getDeclaringClass().getNestHost() == OwnerFullRepairGuard.class
                        || var3.getDeclaringClass().getNestHost() == OwnerRepairGenerationAuthority.class
                        || exactNativeObserver(var3.getDeclaringClass())
                );
            if (!var5) {
                changed(var0);
                OwnerFullRepairGuard.changed(var0, 40);
            }
        }
    }

    private static boolean rawAccessor(StackFrame frame) {
        if (frame.getMethodName().startsWith("ftboss$raw")) return true;
        if (frame.getDeclaringClass() == net.minecraft.world.item.ItemStack.class) {
            return frame.getMethodName().equals("ftboss$nativeComponents") && frame.getDescriptor().equals("()Lnet/minecraft/core/component/PatchedDataComponentMap;");
        }
        if (frame.getDeclaringClass() == net.minecraft.core.component.PatchedDataComponentMap.class) {
            return frame.getMethodName().equals("ftboss$nativePrototype") && frame.getDescriptor().equals("()Lnet/minecraft/core/component/DataComponentMap;")
                || frame.getMethodName().equals("ftboss$nativePatch") && frame.getDescriptor().equals("()Lit/unimi/dsi/fastutil/objects/Reference2ObjectMap;");
        }
        return false;
    }

    private static boolean exactNativeObserver(Class<?> var0) {
        if (var0.getNestHost() == NativeItemComponentObservation.class || var0.getNestHost() == NativeItemCapabilityObservation.class) return true;
        try {
            return var0.getNestHost()
                == Class.forName("net.farmingtales.ftboss.item.NativeExpectedFieldObservation", false, OwnerFieldExpectedAuthority.class.getClassLoader());
        } catch (ClassNotFoundException var2) {
            return false;
        }
    }

    public static void changed(Object var0) {
        synchronized (OwnerFullRepairGuard.class) {
            OwnerFieldExpectedAuthority.DomainBefore var2 = var0 instanceof Entity var3 ? find(var3) : OwnerFullRepairGuard.fieldDomain(var0);
            if (var2 != null) {
                if (var2.initialization != null) var2.initialization.notice(var0);
                var2.retired = true;
            }
        }
    }

    static void alias(Object var0, Object var1, boolean var2) {
        synchronized (OwnerFullRepairGuard.class) {
            OwnerFieldExpectedAuthority.DomainBefore var4 = OwnerFullRepairGuard.fieldDomain(var0);
            if (var4 != null && var1 != null) {
                if (var4.initialization != null) var4.initialization.failed = true;
                if (var1 instanceof OwnerRevocationNodeAccess) {
                    OwnerFullRepairGuard.fieldDomain(var1, var4);
                } else if (var2 && !OwnerFullRepairGuard.immutableValue(var1)) {
                    var4.retired = true;
                }
            }
        }
    }

    private static boolean live(OwnerFieldExpectedAuthority.DomainBefore var0) {
        synchronized (OwnerFullRepairGuard.class) {
            if (var0 != null && var0.originated && !var0.retired && !var0.loading && var0.thread == Thread.currentThread()) {
                Entity var2 = var0.entity.get();
                if (var2 instanceof OwnerFieldEntityAccess var4
                    && var4.ftboss$ownerFieldRawLevel() == var0.world
                    && var4.ftboss$ownerFieldDomain() == var0
                    && var2 instanceof Actor var3
                    && var3.ftboss$rawPersistentData() == var0.originalPersistent) {
                    return true;
                }

                return false;
            } else {
                return false;
            }
        }
    }

    public static OwnerFieldExpectedAuthority.TimerQuote timerQuoted(
        Object var0, Object var1, Object var2, Object var3, Object var4, CompoundTag var5, CompoundTag var6
    ) {
        boolean var7 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(
                            var0xx -> var0xx.getDeclaringClass().getName().equals("net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf")
                                    && var0xx.getDeclaringClass().getNestHost() == FoodWeaponHitPlan.class
                                    && var0xx.getMethodName().contains("ftboss$ownerTimerQuoted")
                        )
                        .orElse(false)
            );
        return var7 && var0 != null && var2 != null && var5 != null && var6 != null
            ? new OwnerFieldExpectedAuthority.TimerQuote(var0, var1, var2, var3, var4, var5, var6, OwnerTimerSemanticAuthority.claim(var5, var6))
            : null;
    }

    static OwnerFieldExpectedAuthority.Prepared prepare(
        Object var0,
        Entity var1,
        Entity var2,
        FoodWeaponOriginalSequence.Lineage var3,
        byte[] var4,
        Object var5,
        Object var6,
        MinecraftServer var7,
        Object var8,
        Object var9
    ) {
        if (ownerCaller()
            && var0 != null
            && var3 != null
            && var3.issuer == var0
            && var7 != null
            && var8 != null
            && var9 != null
            && var1 instanceof OwnerFieldEntityAccess
            && (var2 == null || var2 instanceof OwnerFieldEntityAccess)) {
            OwnerFieldExpectedAuthority.DomainBefore var10 = find(var1);
            if (var10 == null) {
                var10 = new OwnerFieldExpectedAuthority.DomainBefore(var1, FoodWeaponExpectedTrace.emptyCompound(), false);
            }

            OwnerFieldExpectedAuthority.DomainBefore var11 = var2 == null ? null : find(var2);
            if (var2 != null && var11 == null) {
                var11 = new OwnerFieldExpectedAuthority.DomainBefore(var2, FoodWeaponExpectedTrace.emptyCompound(), false);
            }

            return new OwnerFieldExpectedAuthority.Prepared(
                var0, var10, var11, var3, FoodWeaponExpectedFieldsCodec.decodeD084(var4), var5, var6, var7, var8, var9
            );
        } else {
            return null;
        }
    }

    static void quoteTimers(OwnerFieldExpectedAuthority.Prepared var0, Object var1, Object var2, Object var3, Object var4) {
        if (ownerCaller() && issued(var0) && !var0.sealed && var1 instanceof OwnerOriginalPublicationFields var5) {
            if (var0.timerSemantics == null) {
                var0.timerSemantics = new OwnerTimerSemanticAuthority.Book(
                    var0, var0.player.originalPersistent, OwnerNativeSemanticAuthority.semanticRangedBuffer(var0.nativeBefore)
                );
            }

            for (Object var7 : var5.ftboss$ownerOriginalProducers()) {
                if (FoodWeaponFinalDamage.originalAttributeHasNoWrites(var7, var2, var3, var4)) {
                    continue;
                }
                if (var7 instanceof OwnerEvolutionPlanFields var8) {
                    FoodWeaponHitPlan var9 = var8.ftboss$ownerOriginalPlan();
                    if ((Object)var9 instanceof OwnerOriginalPlanFields var10 && var9 instanceof FoodWeaponHitPlan) {
                        FoodWeaponHitPlan var11 = var9;
                        IdentityHashMap var12 = new IdentityHashMap();

                        for (Object var14 : var10.ftboss$ownerOriginalLeaves()) {
                            if (var14 != null && !var12.containsKey(var14)) {
                                var12.put(var14, FoodWeaponSourceLedger.ownerNativeLeafAccepted(var0, var7, var9, var14));
                            } else {
                                var0.unsupported = true;
                            }
                        }

                        try {
                            var0.timerSemantics
                                .quote(
                                    var11,
                                    var12,
                                    var2,
                                    var3,
                                    var4,
                                    var0x -> var0x instanceof OwnerTimerQuoteFields var1x ? var1x.ftboss$ownerTimerQuote() : null
                                );
                        } catch (Throwable var17) {
                            var0.unsupported = true;
                        }

                        for (Object var19 : var10.ftboss$ownerOriginalLeaves()) {
                            if (Boolean.TRUE.equals(var12.get(var19)) && !var0.timerSemantics.local(var19) && var19 instanceof OwnerNativePutQuoteFields) {
                                OwnerNativePutQuoteFields var15 = (OwnerNativePutQuoteFields)var19;
                                OwnerNativeSemanticAuthority.PutBinding var16 = OwnerNativeSemanticAuthority.appendPut(
                                    var0.nativeBefore, var0.builder, var19, var15.ftboss$ownerNativePutQuote()
                                );
                                if (var16 == null) {
                                    var0.unsupported = true;
                                } else {
                                    var0.nativePuts.put(var19, var16);
                                }
                            }
                        }
                        continue;
                    }

                    var0.unsupported = true;
                } else {
                    var0.unsupported = true;
                }
            }
        }
    }

    static void unsupported(OwnerFieldExpectedAuthority.Prepared var0) {
        if (issued(var0)) {
            var0.unsupported = true;
        }
    }

    static boolean seal(OwnerFieldExpectedAuthority.Prepared var0) {
        if (ownerCaller() && issued(var0) && !var0.sealed) {
            Plan var1 = var0.builder.seal();
            var0.session = FoodWeaponOriginalSequence.prepare(var0.lineage, var1);
            if (var0.timerSemantics != null) {
                var0.timerSemantics.bind(var0.session);
                var0.session.timerSemantics = var0.timerSemantics;
            }

            var0.sealed = true;
            var0.catalogueGeneration = var0.unsupported || !live(var0.player) || var0.carrier != null && !live(var0.carrier)
                ? null
                : OwnerFieldCoverageRegistry.generation(var1);
            var0.issuing = var0.catalogueGeneration != null;

            try {
                if (var0.issuing) {
                    var0.coverage = FoodWeaponOriginalSequence.issueOwnerCoverage(var0, var0.catalogueGeneration);
                }
            } finally {
                var0.issuing = false;
            }

            return true;
        } else {
            return false;
        }
    }

    static FoodWeaponOriginalSequence.Session session(OwnerFieldExpectedAuthority.Prepared var0) {
        return issued(var0) ? var0.session : null;
    }

    static FoodWeaponOriginalSequence.CoverageReceipt coverage(OwnerFieldExpectedAuthority.Prepared var0) {
        return issued(var0) ? var0.coverage : null;
    }

    static Plan expected(OwnerFieldExpectedAuthority.Prepared var0) {
        return issued(var0) && var0.session != null ? var0.session.expected : null;
    }

    static boolean authorizesIssue(OwnerFieldExpectedAuthority.Prepared var0, Object var1) {
        boolean var2 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.filter(var0xx -> var0xx.getDeclaringClass() != OwnerFieldExpectedAuthority.class)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == FoodWeaponOriginalSequence.class && var0xx.getMethodName().equals("issueOwnerCoverage"))
                        .orElse(false)
            );
        return var2
            && issued(var0)
            && var0.sealed
            && var0.issuing
            && var0.coverage == null
            && FoodWeaponSourceLedger.ownerFieldPreparedEpochs(var0.issuer, var0.server, var0.serverEpoch, var0.ownerEpoch)
            && var0.catalogueGeneration == var1
            && OwnerFieldCoverageRegistry.current(var1, var0.session.expected);
    }

    static boolean authentic(
        OwnerFieldExpectedAuthority.Prepared var0, FoodWeaponOriginalSequence.CoverageReceipt var1, Object var2, FoodWeaponOriginalSequence.Session var3
    ) {
        return issued(var0)
            && var0.sealed
            && var0.session == var3
            && var0.coverage == var1
            && var1 != null
            && !var0.unsupported
            && FoodWeaponSourceLedger.ownerFieldPreparedEpochs(var0.issuer, var0.server, var0.serverEpoch, var0.ownerEpoch)
            && var0.catalogueGeneration == var2
            && OwnerFieldCoverageRegistry.current(var2, var3.expected)
            && live(var0.player)
            && (var0.carrier == null || live(var0.carrier));
    }

    private static boolean issued(OwnerFieldExpectedAuthority.Prepared var0) {
        return var0 != null
            && var0.thread == Thread.currentThread()
            && var0.player.thread == Thread.currentThread()
            && !var0.retired
            && FoodWeaponSourceLedger.ownerFieldPrepared(var0.issuer) == var0;
    }

    static OwnerFieldExpectedAuthority.Prepared matching(Object var0, FoodWeaponOriginalSequence.Session var1, FoodWeaponOriginalSequence.CoverageReceipt var2) {
        OwnerFieldExpectedAuthority.Prepared var3 = FoodWeaponSourceLedger.ownerFieldPrepared(var0);
        return !issued(var3)
                || var3.session != var1
                || var3.coverage != var2
                || var2 == null
                || !var3.sealed
                || var3.bound
                || !live(var3.player)
                || var3.carrier != null && !live(var3.carrier)
            ? null
            : var3;
    }

    static boolean pending(OwnerFieldExpectedAuthority.Prepared var0) {
        return issued(var0)
            && var0.sealed
            && !var0.unsupported
            && var0.coverage == null
            && live(var0.player)
            && (var0.carrier == null || live(var0.carrier))
            && OwnerFieldCoverageRegistry.pending(var0.session.expected);
    }

    static FoodWeaponOriginalSequence.CoverageReceipt refreshCoverage(OwnerFieldExpectedAuthority.Prepared var0) {
        if (!ownerCaller()
            || !issued(var0)
            || !var0.sealed
            || var0.unsupported
            || var0.coverage != null
            || !live(var0.player)
            || var0.carrier != null && !live(var0.carrier)) {
            return var0 == null ? null : var0.coverage;
        } else {
            var0.catalogueGeneration = OwnerFieldCoverageRegistry.generation(var0.session.expected);
            if (var0.catalogueGeneration == null) {
                return null;
            } else {
                var0.issuing = true;

                try {
                    var0.coverage = FoodWeaponOriginalSequence.issueOwnerCoverage(var0, var0.catalogueGeneration);
                } finally {
                    var0.issuing = false;
                }

                return var0.coverage;
            }
        }
    }

    static void bound(OwnerFieldExpectedAuthority.Prepared var0) {
        if (issued(var0)) {
            var0.bound = true;
        }
    }

    static boolean nativeValidationLive(
        OwnerFieldExpectedAuthority.Prepared var0, OwnerNativeSemanticAuthority.Before var1, FoodWeaponOriginalSequence.Session var2
    ) {
        boolean var3 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.filter(var0xx -> var0xx.getDeclaringClass() != OwnerFieldExpectedAuthority.class)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerNativeSemanticAuthority.class)
                        .orElse(false)
            );
        if (var3
            && var0 != null
            && var0.thread == Thread.currentThread()
            && !var0.retired
            && !var0.unsupported
            && FoodWeaponSourceLedger.ownerFieldPreparedEpochs(var0.issuer, var0.server, var0.serverEpoch, var0.ownerEpoch)
            && live(var0.player)
            && var0.playerGeneration == var0.player.generation
            && (var0.carrier == null || live(var0.carrier) && var0.carrierGeneration == var0.carrier.generation)) {
            return var1 == null
                ? var0.nativeBefore == null
                    && !var0.sealed
                    && !var0.bound
                    && var0.session == null
                    && var2 == null
                    && StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                        .walk(
                            var0x -> var0x.anyMatch(
                                    var0xx -> var0xx.getDeclaringClass() == OwnerFieldExpectedAuthority.Prepared.class
                                            && var0xx.getMethodName().equals("<init>")
                                )
                        )
                : issued(var0)
                    && var0.nativeBefore == var1
                    && (var2 == null ? !var0.sealed && !var0.bound : var0.bound && var0.session == var2 && var0.lineage == var2.lineage);
        } else {
            return false;
        }
    }

    static void nativeValidationFailed(OwnerFieldExpectedAuthority.Prepared var0, OwnerNativeSemanticAuthority.Before var1) {
        boolean var2 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.filter(var0xx -> var0xx.getDeclaringClass() != OwnerFieldExpectedAuthority.class)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerNativeSemanticAuthority.class)
                        .orElse(false)
            );
        if (var2 && var0 != null && var0.nativeBefore == var1) {
            var0.retired = true;
        }
    }

    static boolean timerWriterExpected(
        OwnerFieldExpectedAuthority.Prepared var0, FoodWeaponOriginalSequence.Session var1, Object var2, FoodWeaponHitPlan.TimerSemanticStep var3
    ) {
        return ownerCaller()
            && issued(var0)
            && var0.sealed
            && var0.session == var1
            && var1 != null
            && var1.lineage == var0.lineage
            && var0.timerSemantics != null
            && var0.timerSemantics.writerStep(var1, var2, var3);
    }

    static boolean unchanged(OwnerFieldExpectedAuthority.Prepared var0) {
        return issued(var0)
            && var0.bound
            && live(var0.player)
            && var0.playerGeneration == var0.player.generation
            && (var0.carrier == null || live(var0.carrier) && var0.carrierGeneration == var0.carrier.generation);
    }

    static boolean covered(OwnerFieldExpectedAuthority.Prepared var0) {
        return unchanged(var0) && var0.coverage != null && var0.session != null && var0.session.extraAuthorityRetained();
    }

    static void retire(OwnerFieldExpectedAuthority.Prepared var0) {
        if (var0 != null) {
            var0.retired = true;
        }
    }

    static boolean advance(FoodWeaponOriginalSequence.Session session, FoodWeaponOriginalSequence.Step step) {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == FoodWeaponOriginalSequence.Session.class
                && frame.getMethodName().equals("close")
                && frame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;)V")).orElse(false));
        if (!caller || session == null || step == null || step.session != session) return false;
        synchronized (OwnerFullRepairGuard.class) {
            Prepared prepared = FoodWeaponSourceLedger.ownerFieldPrepared(session.lineage.issuer);
            if (!unchanged(prepared) || prepared.session != session || prepared.lineage != session.lineage
                || !session.extraAuthorityRetained()
                || !FoodWeaponExpectedTrace.same(prepared.player.expected, step.expected.before.player)
                || prepared.carrier != null && !FoodWeaponExpectedTrace.same(prepared.carrier.expected, step.expected.before.carrier)) return false;
            Value player = prepared.player.originalPersistent == null ? FoodWeaponExpectedTrace.emptyCompound()
                : NativeExpectedRawFields.observe((CompoundTag)prepared.player.originalPersistent);
            Value carrier = prepared.carrier == null || prepared.carrier.originalPersistent == null ? FoodWeaponExpectedTrace.emptyCompound()
                : NativeExpectedRawFields.observe((CompoundTag)prepared.carrier.originalPersistent);
            if (!FoodWeaponExpectedTrace.same(step.expected.after.player, player)
                || !FoodWeaponExpectedTrace.same(step.expected.after.carrier, carrier)) {
                prepared.player.retired = true;
                if (prepared.carrier != null) prepared.carrier.retired = true;
                prepared.retired = true;
                return false;
            }
            if (!OwnerNativeSemanticAuthority.advance(prepared.nativeBefore, session, step)) {
                prepared.player.retired = true;
                if (prepared.carrier != null) prepared.carrier.retired = true;
                prepared.retired = true;
                return false;
            }
            prepared.player.expected = step.expected.after.player;
            if (prepared.carrier != null) prepared.carrier.expected = step.expected.after.carrier;
            return true;
        }
    }

    public static final class DomainBefore {
        private final WeakReference<Entity> entity;
        private final Level world;
        private final Thread thread = Thread.currentThread();
        private final Object generation = new Object();
        private Value expected;
        private final boolean originated;
        private volatile Object originalPersistent;
        private volatile boolean loading;
        private volatile boolean retired;
        private volatile NativeProfileInitialization initialization;

        private DomainBefore(Entity var1, Value var2, boolean var3) {
            this.entity = new WeakReference<>(var1);
            this.world = ((OwnerFieldEntityAccess)var1).ftboss$ownerFieldRawLevel();
            this.expected = var2;
            this.originated = var3;
        }
    }

    static final class Prepared {
        final Object issuer;
        final OwnerFieldExpectedAuthority.DomainBefore player;
        final OwnerFieldExpectedAuthority.DomainBefore carrier;
        final Object playerGeneration;
        final Object carrierGeneration;
        final FoodWeaponOriginalSequence.Lineage lineage;
        final OwnerNativeSemanticAuthority.Before nativeBefore;
        volatile OwnerTimerSemanticAuthority.Book timerSemantics;
        final IdentityHashMap<Object, OwnerNativeSemanticAuthority.PutBinding> nativePuts = new IdentityHashMap<>();
        private final Builder builder;
        private final MinecraftServer server;
        private final Object serverEpoch;
        private final Object ownerEpoch;
        private volatile Object catalogueGeneration;
        private volatile boolean issuing;
        private volatile FoodWeaponOriginalSequence.Session session;
        private volatile FoodWeaponOriginalSequence.CoverageReceipt coverage;
        private volatile boolean unsupported;
        private volatile boolean sealed;
        private volatile boolean bound;
        private volatile boolean retired;
        private final Thread thread = Thread.currentThread();

        private Prepared(
            Object var1,
            OwnerFieldExpectedAuthority.DomainBefore var2,
            OwnerFieldExpectedAuthority.DomainBefore var3,
            FoodWeaponOriginalSequence.Lineage var4,
            Value var5,
            Object var6,
            Object var7,
            MinecraftServer var8,
            Object var9,
            Object var10
        ) {
            this.issuer = var1;
            this.server = var8;
            this.serverEpoch = var9;
            this.ownerEpoch = var10;
            this.player = var2;
            this.carrier = var3;
            this.playerGeneration = var2.generation;
            this.carrierGeneration = var3 == null ? null : var3.generation;
            this.lineage = var4;
            this.nativeBefore = OwnerFieldExpectedAuthority.live(var2) && (var3 == null || OwnerFieldExpectedAuthority.live(var3))
                ? OwnerNativeSemanticAuthority.before(
                    this,
                    var6,
                    var2.entity.get(),
                    var3 == null ? null : var3.entity.get(),
                    var2.originalPersistent,
                    var3 == null ? null : var3.originalPersistent,
                    var7,
                    var5,
                    var2.expected,
                    var3 == null ? FoodWeaponExpectedTrace.emptyCompound() : var3.expected
                )
                : null;
            Value var11 = OwnerNativeSemanticAuthority.expectedWeapon(this.nativeBefore);
            this.builder = new Builder(
                new State(var11 == null ? var5 : var11, var2.expected, var3 == null ? FoodWeaponExpectedTrace.emptyCompound() : var3.expected)
            );
            this.unsupported = !OwnerFieldExpectedAuthority.live(var2) || var3 != null && !OwnerFieldExpectedAuthority.live(var3);
        }
    }

    public static final class TimerQuote {
        final Object leaf;
        final Object weapon;
        final Object player;
        final Object source;
        final String profile;
        final String[] keys;
        final String[] rangedKeys;
        final Value[] values;
        final Value data;
        final Value baseline;
        final OwnerTimerSemanticAuthority.Origin origin;

        private TimerQuote(
            Object var1, Object var2, Object var3, Object var4, Object var5, CompoundTag var6, CompoundTag var7, OwnerTimerSemanticAuthority.Origin var8
        ) {
            this.origin = var8;
            this.leaf = var1;
            this.weapon = var2;
            this.profile = FoodWeaponEvolution.planHitTimerProfile((Profile)var3);
            this.player = var4;
            this.source = var5;
            this.data = NativeExpectedRawFields.observe(var7);
            Value var9 = NativeExpectedRawFields.observe(var6);
            this.baseline = var9;
            Map<String, Tag> var10 = ((Compound)var7).ftboss$rawCompoundFields();
            this.keys = var10.keySet()
                .stream()
                .filter(var2x -> !FoodWeaponExpectedTrace.same(FoodWeaponExpectedTrace.field(var9, var2x), FoodWeaponExpectedTrace.field(this.data, var2x)))
                .toArray(String[]::new);
            this.rangedKeys = var10.keySet()
                .stream()
                .filter(var1x -> var1x.startsWith("Last") && FoodWeaponExpectedTrace.field(this.data, var1x).kind == 4)
                .toArray(String[]::new);
            this.values = new Value[this.keys.length];

            for (int var11 = 0; var11 < this.keys.length; var11++) {
                this.values[var11] = FoodWeaponExpectedTrace.field(this.data, this.keys[var11]);
            }
        }
    }
    static boolean nativeProjectileProfile(Entity entity, CompoundTag persistent, String profile) {
        synchronized (OwnerFullRepairGuard.class) {
            DomainBefore before = find(entity);
            NativeProfileInitialization initialization = null;
            if (ownerCaller() && entity instanceof net.minecraft.world.entity.projectile.AbstractArrow
                && persistent.getClass() == CompoundTag.class && profile != null && before != null && live(before)
                && before.initialization == null && before.originalPersistent == persistent
                && FoodWeaponExpectedTrace.field(before.expected, "FTBossIntrinsicProjectileProfile") == null
                && FoodWeaponExpectedTrace.same(before.expected, NativeExpectedRawFields.observe(persistent))) {
                initialization = new NativeProfileInitialization(before, persistent,
                    FoodWeaponExpectedTrace.put(before.expected, "FTBossIntrinsicProjectileProfile", FoodWeaponExpectedTrace.string(profile)));
                before.initialization = initialization;
            }
            boolean returned = false;
            try {
                persistent.putString("FTBossIntrinsicProjectileProfile", profile);
                returned = true;
            } finally {
                if (initialization != null) {
                    before.initialization = null;
                    if (returned && !initialization.failed && initialization.notices > 0 && find(entity) == before
                        && before.originalPersistent == persistent
                        && ((Actor)entity).ftboss$rawPersistentData() == persistent
                        && FoodWeaponExpectedTrace.same(initialization.expected, NativeExpectedRawFields.observe(persistent))) {
                        DomainBefore after = new DomainBefore(entity, initialization.expected, true);
                        after.originalPersistent = persistent;
                        if (install(entity, after)) {
                            OwnerFullRepairGuard.fieldDomain(persistent, after);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    static boolean nativeProjectileImage(Entity entity, CompoundTag persistent, CompoundTag image) {
        synchronized (OwnerFullRepairGuard.class) {
            DomainBefore before = find(entity);
            Value imageFields = nativeInitializationFields(image);
            NativeProfileInitialization initialization = null;
            if (imageFields != null && ownerCaller() && entity instanceof net.minecraft.world.entity.projectile.AbstractArrow
                && persistent.getClass() == CompoundTag.class && image != null && image.getClass() == CompoundTag.class && before != null && live(before)
                && before.initialization == null && before.originalPersistent == persistent
                && FoodWeaponExpectedTrace.field(before.expected, "FTBossWeaponMaterials") == null
                && FoodWeaponExpectedTrace.same(before.expected, nativeInitializationFields(persistent))) {
                initialization = new NativeProfileInitialization(before, persistent,
                    FoodWeaponExpectedTrace.put(before.expected, "FTBossWeaponMaterials", imageFields));
                before.initialization = initialization;
            }
            boolean returned = false;
            try {
                persistent.put("FTBossWeaponMaterials", image);
                returned = true;
            } finally {
                if (initialization != null) {
                    before.initialization = null;
                    if (returned && !initialization.failed && initialization.notices > 0 && find(entity) == before
                        && before.originalPersistent == persistent
                        && ((Actor)entity).ftboss$rawPersistentData() == persistent
                        && FoodWeaponExpectedTrace.same(initialization.expected, nativeInitializationFields(persistent))) {
                        DomainBefore after = new DomainBefore(entity, initialization.expected, true);
                        after.originalPersistent = persistent;
                        if (install(entity, after)) {
                            OwnerFullRepairGuard.fieldDomain(persistent, after);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    private static Value nativeInitializationFields(net.minecraft.nbt.Tag tag) {
        try {
            return NativeExpectedRawFields.observe(tag);
        } catch (IllegalArgumentException unsupported) {
            return null;
        }
    }

    static boolean nativeProjectileOwner(Entity entity, CompoundTag persistent, Entity owner, java.util.UUID value) {
        synchronized (OwnerFullRepairGuard.class) {
            DomainBefore before = find(entity);
            DomainBefore source = find(owner);
            Value expectedOwner = source == null ? null : FoodWeaponExpectedTrace.field(source.expected, "ftboss_manor_owner");
            if (expectedOwner == null && owner != null) expectedOwner = NativeExpectedRawFields.observe(net.minecraft.nbt.NbtUtils.createUUID(owner.getUUID()));
            NativeProfileInitialization initialization = null;
            if (ownerCaller() && entity instanceof net.minecraft.world.entity.projectile.AbstractArrow
                && persistent.getClass() == CompoundTag.class && value != null && before != null && live(before)
                && source != null && live(source) && source.originalPersistent instanceof CompoundTag sourcePersistent
                && FoodWeaponExpectedTrace.same(source.expected, NativeExpectedRawFields.observe(sourcePersistent))
                && FoodWeaponExpectedTrace.same(expectedOwner, NativeExpectedRawFields.observe(net.minecraft.nbt.NbtUtils.createUUID(value)))
                && before.initialization == null && before.originalPersistent == persistent
                && FoodWeaponExpectedTrace.field(before.expected, "ftboss_manor_owner") == null
                && FoodWeaponExpectedTrace.same(before.expected, NativeExpectedRawFields.observe(persistent))) {
                initialization = new NativeProfileInitialization(before, persistent,
                    FoodWeaponExpectedTrace.put(before.expected, "ftboss_manor_owner", expectedOwner));
                before.initialization = initialization;
            }
            boolean returned = false;
            try {
                persistent.putUUID("ftboss_manor_owner", value);
                returned = true;
            } finally {
                if (initialization != null) {
                    before.initialization = null;
                    if (returned && !initialization.failed && initialization.notices > 0 && find(entity) == before
                        && before.originalPersistent == persistent
                        && ((Actor)entity).ftboss$rawPersistentData() == persistent
                        && FoodWeaponExpectedTrace.same(initialization.expected, NativeExpectedRawFields.observe(persistent))) {
                        DomainBefore after = new DomainBefore(entity, initialization.expected, true);
                        after.originalPersistent = persistent;
                        if (install(entity, after)) {
                            OwnerFullRepairGuard.fieldDomain(persistent, after);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    private static final class NativeProfileInitialization {
        private final DomainBefore before;
        private final CompoundTag persistent;
        private final Value expected;
        private final Thread thread = Thread.currentThread();
        private volatile boolean failed;
        private int notices;

        private NativeProfileInitialization(DomainBefore before, CompoundTag persistent, Value expected) {
            this.before = before; this.persistent = persistent; this.expected = expected;
        }

        private void notice(Object receiver) {
            boolean original = false;
            for (StackFrame frame : StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(java.util.stream.Stream::toList)) {
                if (frame.getDeclaringClass() == CompoundTag.class && (frame.getMethodName().equals("putString") || frame.getMethodName().equals("put") || frame.getMethodName().equals("putUUID"))) original = true;
                if (frame.getDeclaringClass() == OwnerFieldExpectedAuthority.class && (frame.getMethodName().equals("nativeProjectileProfile") || frame.getMethodName().equals("nativeProjectileImage") || frame.getMethodName().equals("nativeProjectileOwner"))) break;
            }
            if (receiver != this.persistent || Thread.currentThread() != this.thread || this.before.initialization != this || !original) this.failed = true;
            this.notices++;
        }
    }
}
