package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Builder;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Domain;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Kind;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Operation;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.State;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Actor;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Compound;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public final class OwnerNativeSemanticAuthority {
    private static final ThreadLocal<OwnerNativeSemanticAuthority.ReadFrame> RAW_READ = new ThreadLocal<>();
    private static final ThreadLocal<OwnerNativeSemanticAuthority.TargetWriteCall> TARGET_WRITE = new ThreadLocal<>();
    private static final ThreadLocal<OwnerNativeSemanticAuthority.TargetReceiver> TARGET_RECEIVER = new ThreadLocal<>();

    private static boolean readLive(OwnerNativeSemanticAuthority.ReadContext var0) {
        if (var0 != null && var0.wearReading != null) {
            return !var0.rejected
                && var0.thread == Thread.currentThread()
                && var0.preparation == null
                && var0.before == null
                && var0.sequence == null
                && var0.step == null
                && var0.binding == null
                && !var0.loading
                && var0.input.generation == var0.inputGeneration
                && var0.input.actual != null
                && var0.input.actual.get() == var0.actual
                && var0.input.wearOrigin != null
                && var0.wearReading.live(var0.input, var0.actual)
                && (!var0.wearReading.before() || !var0.input.retired)
                && OwnerFullRepairGuard.generation(var0.actual) == var0.guardGeneration
                && OwnerFullRepairGuard.lease(var0.actual) == var0.lease
                && (Object)var0.actual instanceof OwnerNativeWeaponInputFields var3
                && var3.ftboss$ownerNativeWeaponInput() == var0.input;
        } else if (var0 == null
            || var0.rejected
            || var0.thread != Thread.currentThread()
            || var0.input.retired
            || var0.input.thread != Thread.currentThread()
            || var0.input.generation != var0.inputGeneration
            || OwnerFullRepairGuard.generation(var0.actual) != var0.guardGeneration
            || OwnerFullRepairGuard.lease(var0.actual) != var0.lease) {
            return false;
        } else if (var0.loading) {
            return var0.preparation == null && var0.before == null && var0.sequence == null && var0.input.actual == null;
        } else if (var0.input.actual == null
            || var0.input.actual.get() != var0.actual
            || !((Object)var0.actual instanceof OwnerNativeWeaponInputFields var1)
            || var1.ftboss$ownerNativeWeaponInput() != var0.input) {
            return false;
        } else {
            return !OwnerFieldExpectedAuthority.nativeValidationLive(var0.preparation, var0.before, var0.sequence)
                ? false
                : var0.sequence == null
                    || var0.step != null
                        && var0.step.session == var0.sequence
                        && var0.binding != null
                        && var0.binding.before == var0.before
                        && matchesSemanticStep(var0.binding, var0.sequence, var0.step)
                        && var0.sequence.lineage == var0.preparation.lineage
                        && var0.sequence.original.atLeaf(var0.leaf);
        }
    }

    private static void rejectRead(OwnerNativeSemanticAuthority.ReadContext var0, Throwable var1) {
        if (var0 != null && var0.thread == Thread.currentThread()) {
            var0.rejected = true;
            if (var0.observationFailure == null) {
                var0.observationFailure = var1;
            }

            if (var0.loading) {
                var0.input.retired = true;
            } else {
                try {
                    OwnerFieldExpectedAuthority.nativeValidationFailed(var0.preparation, var0.before);
                } catch (Throwable var3) {
                    if (var0.observationFailure == null) {
                        var0.observationFailure = var3;
                    }
                }
            }
        }
    }

    private static void finishRead(OwnerNativeSemanticAuthority.ReadFrame var0) {
        var0.closed = true;
        if (RAW_READ.get() == var0) {
            OwnerNativeSemanticAuthority.ReadFrame var1 = var0.previous;

            while (var1 != null && var1.closed) {
                var1 = var1.previous;
            }

            if (var1 == null) {
                RAW_READ.remove();
            } else {
                RAW_READ.set(var1);
            }
        }
    }

    private static Object read(OwnerNativeSemanticAuthority.ReadContext var0, Object var1, OwnerNativeSemanticAuthority.RawField var2) {
        if (readLive(var0)
            && var1 != null
            && (
                var2 == OwnerNativeSemanticAuthority.RawField.COMPOUND
                    ? var0.compounds.containsKey(var1)
                    : (
                        var2 == OwnerNativeSemanticAuthority.RawField.PERSISTENT
                            ? var0.before != null && (var1 == var0.before.player || var1 == var0.before.carrier)
                            : var1 == var0.actual
                    )
            )) {
            if (var2 == OwnerNativeSemanticAuthority.RawField.CUSTOM_DATA || var2 == OwnerNativeSemanticAuthority.RawField.COMPONENTS) {
                NativeItemComponentObservation observed = NativeItemComponentObservation.capture(var0.actual, var0.input.registries);
                Object actual = observed.semanticStorage(var2 == OwnerNativeSemanticAuthority.RawField.CUSTOM_DATA);
                if (!observed.matches(var0.actual) || !readLive(var0)) {
                    rejectRead(var0, null);
                    throw new IllegalStateException("Native semantic component storage changed during authenticated read");
                }
                return actual;
            }
            OwnerNativeSemanticAuthority.ReadFrame var3 = new OwnerNativeSemanticAuthority.ReadFrame(var0, var1, var2, RAW_READ.get());
            RAW_READ.set(var3);

            Object var5;
            try {
                Object var4 = switch (var2) {
                    case CUSTOM_DATA, COMPONENTS -> throw new IllegalStateException("Native semantic storage read was not dispatched");
                    case PERSISTENT -> ((Actor)var1).ftboss$rawPersistentData();
                    case COMPOUND -> ((Compound)var1).ftboss$rawCompoundFields();
                };
                if (!var3.observed || !var3.accepted || var3.returned != var4 || !readLive(var0)) {
                    throw new IllegalStateException("Native raw read was not authenticated");
                }

                var5 = var4;
            } catch (Throwable var9) {
                rejectRead(var0, var9);
                throw var9;
            } finally {
                finishRead(var3);
            }

            return var5;
        } else {
            throw new IllegalStateException("Saved native read context is not live");
        }
    }

    static boolean authenticatedRawMapRead(Class<?> owner, String member, String descriptor) {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == NativeNbtAliasPolicy.class && frame.getMethodName().equals("trustedRawFieldRead") && frame.getDescriptor().equals("()Z")).orElse(false));
        ReadFrame frame = RAW_READ.get();
        return caller && frame != null && !frame.closed && !frame.observed && frame.field == RawField.COMPOUND
            && owner == frame.field.owner && frame.field.member.equals(member) && frame.field.descriptor.equals(descriptor)
            && frame.receiver != null && frame.context.compounds.containsKey(frame.receiver) && readLive(frame.context);
    }

    static int rawReadReturned(Object var0, Object var1, Class<?> var2, String var3, String var4, Class<?> var5, String var6, String var7) {
        boolean var8 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerFieldExpectedAuthority.class && var0xx.getMethodName().equals("rawExposed"))
                        .orElse(false)
            );
        OwnerNativeSemanticAuthority.ReadFrame var9 = RAW_READ.get();
        if (var8
            && var9 != null
            && !var9.closed
            && var9.receiver == var0
            && var5 == OwnerNativeSemanticAuthority.class
            && "read".equals(var6)
            && "(Lnet/farmingtales/ftboss/item/OwnerNativeSemanticAuthority$ReadContext;Ljava/lang/Object;Lnet/farmingtales/ftboss/item/OwnerNativeSemanticAuthority$RawField;)Ljava/lang/Object;"
                .equals(var7)) {
            try {
                if (!var9.observed
                    && var2 == var9.field.owner
                    && var9.field.member.equals(var3)
                    && var9.field.descriptor.equals(var4)
                    && readLive(var9.context)) {
                    var9.observed = true;
                    var9.returned = var1;
                    var9.accepted = true;
                    return 1;
                } else {
                    var9.observed = true;
                    var9.accepted = false;
                    rejectRead(var9.context, null);
                    return 2;
                }
            } catch (Throwable var11) {
                var9.observed = true;
                var9.accepted = false;
                rejectRead(var9.context, var11);
                return 2;
            }
        } else {
            return 0;
        }
    }

    private static Map<String, Tag> compound(OwnerNativeSemanticAuthority.ReadContext var0, Object var1) {
        Map var2 = (Map)read(var0, var1, OwnerNativeSemanticAuthority.RawField.COMPOUND);
        if (var2 != null && (var2.getClass() == HashMap.class || NativeFreshFactoryTrust298.readableMap(var1, var2)) && var2.size() <= 65536) {
            return var2;
        } else {
            throw new IllegalArgumentException("Unsupported native validation map");
        }
    }

    private static boolean targetWriterCaller(String var0) {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var1 -> var1.filter(var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerNativeSemanticAuthority.class)
                        .findFirst()
                        .map(
                            var1x -> {
                                String var2 = switch (var0) {
                                    case "ftboss$ownerTargetWrite" -> "(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain;Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/nbt/CompoundTag;JLnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)V";
                                    case "ftboss$ownerTargetReceiver" -> "(Lnet/minecraft/world/entity/LivingEntity;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/nbt/CompoundTag;JLnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)Lnet/minecraft/nbt/CompoundTag;";
                                    default -> null;
                                };
                                if (var2 == null) {
                                    return false;
                                } else if (var1x.getDeclaringClass() == FoodWeaponEvolution.class
                                    && var1x.getDescriptor().equals(var2)
                                    && var1x.getMethodName()
                                        .matches(
                                            "wrapOperation\\$(?:zz[a-p]|z[b-p][a-p]|[b-p][a-p]{2,6}|[b-h][a-p]{7})(?:[0-9a-f]{3}|[1-9a-f][0-9a-f]{3,7})\\$"
                                                + Pattern.quote(var0)
                                        )) {
                                    try {
                                        Method var3 = FoodWeaponEvolution.class
                                            .getDeclaredMethod(var1x.getMethodName(), var1x.getMethodType().parameterArray());
                                        MixinMerged var6 = var3.getDeclaredAnnotation(MixinMerged.class);
                                        return var6 != null && "net.farmingtales.ftboss.mixin.OwnerTimerLocalOriginMixin".equals(var6.mixin());
                                    } catch (LinkageError | SecurityException | ReflectiveOperationException var5) {
                                        return false;
                                    }
                                } else {
                                    return false;
                                }
                            }
                        )
                        .orElse(false)
            );
    }

    public static void targetReceiverReturned(FoodWeaponHitPlan.Builder var0, LivingEntity var1, LivingEntity var2, CompoundTag var3) {
        if (targetWriterCaller("ftboss$ownerTargetReceiver") && var0 != null && var1 != null && var1 == var2 && var3 != null) {
            TARGET_RECEIVER.set(new OwnerNativeSemanticAuthority.TargetReceiver(var0, var1, var3, Thread.currentThread()));
        }
    }

    public static OwnerNativeSemanticAuthority.TargetWriteCall targetWriteBegins(
        FoodWeaponHitPlan.Builder var0,
        FoodWeaponHitPlan.Builder var1,
        ServerPlayer var2,
        LivingEntity var3,
        ItemStack var4,
        CompoundTag var5,
        CompoundTag var6,
        String var7,
        FoodWeaponSourceLedger.AttemptDomain var8,
        Tag var9
    ) {
        if (!targetWriterCaller("ftboss$ownerTargetWrite")) {
            return null;
        } else {
            OwnerNativeSemanticAuthority.TargetReceiver var10 = TARGET_RECEIVER.get();
            TARGET_RECEIVER.remove();
            if (var10 != null
                && var10.thread() == Thread.currentThread()
                && var10.builder() == var0
                && var10.target() == var3
                && var10.receiver() == var6
                && var0 != null
                && var0 == var1
                && var2 != null
                && var3 != null
                && var3 != var2
                && var4 != null
                && var6 != null
                && var6 != var5
                && var7 != null
                && var6.getClass() == CompoundTag.class
                && (var9 == null || var9.getClass() == LongTag.class || var9.getClass() == IntTag.class)) {
                boolean var11 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                    .walk(
                        var0x -> var0x.anyMatch(
                                var0xx -> var0xx.getDeclaringClass() == FoodWeaponEvolution.class
                                        && var0xx.getMethodName().equals("quoteFoodBlessingHits")
                                        && var0xx.getDescriptor()
                                            .equals(
                                                "(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/nbt/CompoundTag;JLnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)V"
                                            )
                            )
                    );
                if (!var11) {
                    return null;
                } else {
                    OwnerNativeSemanticAuthority.TargetWriteCall var12 = new OwnerNativeSemanticAuthority.TargetWriteCall(
                        var0, var2, var3, var4, var6, var7, var8, var9, TARGET_WRITE.get()
                    );
                    TARGET_WRITE.set(var12);
                    return var12;
                }
            } else {
                return null;
            }
        }
    }

    public static void targetWriteEnds(OwnerNativeSemanticAuthority.TargetWriteCall var0) {
        if (var0 != null && !var0.closed && var0.thread == Thread.currentThread() && TARGET_WRITE.get() == var0) {
            var0.closed = true;
            OwnerNativeSemanticAuthority.TargetWriteCall var1 = var0.previous;

            while (var1 != null && var1.closed) {
                var1 = var1.previous;
            }

            if (var1 == null) {
                TARGET_WRITE.remove();
            } else {
                TARGET_WRITE.set(var1);
            }
        }
    }

    static OwnerNativeSemanticAuthority.TargetWrite targetWriteConstructed(
        FoodWeaponHitPlan.Builder var0, Object var1, FoodWeaponSourceLedger.AttemptDomain var2, CompoundTag var3, String var4, Tag var5, Tag var6
    ) {
        boolean var7 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(
                            var0xx -> var0xx.getDeclaringClass() == FoodWeaponHitPlan.Builder.class
                                    && var0xx.getMethodName().equals("write")
                                    && var0xx.getDescriptor()
                                        .equals(
                                            "(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain;Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V"
                                        )
                        )
                        .orElse(false)
            );
        OwnerNativeSemanticAuthority.TargetWriteCall var8 = TARGET_WRITE.get();
        if (var7
            && var8 != null
            && !var8.closed
            && !var8.consumed
            && var8.thread == Thread.currentThread()
            && var8.builder == var0
            && var1 != null
            && var8.attempt == var2
            && var8.receiver == var3
            && var8.key.equals(var4)
            && var8.input == var5
            && (var5 != null || var6 == null)
            && (var5 == null || var6 != null && FoodWeaponExpectedTrace.same(var8.value, NativeExpectedRawFields.observe(var6)))) {
            var8.consumed = true;
            return new OwnerNativeSemanticAuthority.TargetWrite(var8, var1, var6);
        } else {
            return null;
        }
    }

    static OwnerNativeSemanticAuthority.TargetWrite reserveTarget(
        OwnerFieldExpectedAuthority.Prepared var0, FoodWeaponHitPlan var1, Object var2, Object var3, Object var4
    ) {
        boolean var5 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerTimerSemanticAuthority.Book.class && var0xx.getMethodName().equals("quote"))
                        .orElse(false)
            );
        if (var5 && var0 != null && var1 != null && var2 != null) {
            OwnerNativeSemanticAuthority.TargetWrite var6 = FoodWeaponHitPlan.ownerTargetWrite(var1, var2);
            if (var6 != null
                && var6.leaf == var2
                && var6.call.thread == Thread.currentThread()
                && var6.call.closed
                && var6.call.consumed
                && var6.call.player == var3
                && var6.call.weapon == var4
                && (var6.plan == null || var6.plan == var1)) {
                OwnerNativeSemanticAuthority.Before var7 = var0.nativeBefore;
                if (var7 == null
                    || var6.call.target != var7.player
                        && var6.call.target != var7.carrier
                        && var6.call.receiver != var7.weapon.root
                        && var6.call.receiver != var7.playerRoot
                        && var6.call.receiver != var7.carrierRoot) {
                    var6.plan = var1;
                    return var6;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    static boolean targetPlan(OwnerNativeSemanticAuthority.TargetWrite var0, FoodWeaponHitPlan var1) {
        boolean var2 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == FoodWeaponHitPlan.class && var0xx.getMethodName().equals("ownerTargetWriteAtOriginal"))
                        .orElse(false)
            );
        return var2 && var0 != null && var0.plan == var1;
    }

    private static boolean targetLive(OwnerNativeSemanticAuthority.TargetObservation var0) {
        return var0 != null
            && !var0.closed
            && !var0.failed
            && var0.thread == Thread.currentThread()
            && var0.write.call.thread == Thread.currentThread()
            && var0.write.call.closed
            && var0.write.call.consumed
            && var0.session.timerSemantics == var0.book
            && var0.book.targetMatches(var0.session, var0.write.leaf, var0.write)
            && var0.write.plan != null
            && FoodWeaponHitPlan.ownerTargetWriteAtOriginal(var0.commit, var0.bridge, var0.write.leaf) == var0.write
            && FoodWeaponFinalDamage.originalHitPlanSequence(var0.commit, var0.bridge) == var0.session;
    }

    static OwnerNativeSemanticAuthority.TargetObservation targetEntered(
        FoodWeaponHitPlan.FinalEffectsCommit var0,
        FoodWeaponFinalDamage.ChildHurtBridge var1,
        Object var2,
        OwnerNativeSemanticAuthority.TargetWrite var3,
        CompoundTag var4,
        String var5,
        Tag var6
    ) {
        boolean var7 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(
                            var0xx -> var0xx.getDeclaringClass().getNestHost() == FoodWeaponHitPlan.class
                                    && var0xx.getDeclaringClass().getName().equals("net.farmingtales.ftboss.item.FoodWeaponHitPlan$TagLeaf")
                                    && var0xx.getMethodName().equals("apply")
                                    && var0xx.getDescriptor()
                                        .equals(
                                            "(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V"
                                        )
                        )
                        .orElse(false)
            );
        if (var7 && var3 != null && var3.leaf == var2 && var3.call.receiver == var4 && var3.call.key.equals(var5) && var3.value == var6) {
            FoodWeaponOriginalSequence.Session var8 = FoodWeaponFinalDamage.originalHitPlanSequence(var0, var1);
            if (var8 != null && var8.timerSemantics != null) {
                OwnerNativeSemanticAuthority.TargetObservation var9 = new OwnerNativeSemanticAuthority.TargetObservation(
                    var8.timerSemantics, var8, var0, var1, var3
                );
                return targetLive(var9) ? var9 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    static void targetCopied(OwnerNativeSemanticAuthority.TargetObservation var0, Tag var1, Tag var2) {
        if (var0 != null) {
            if (targetLive(var0)
                && !var0.copied
                && var0.write.value != null
                && var1 == var0.write.value
                && var2 != null
                && FoodWeaponExpectedTrace.same(var0.write.call.value, NativeExpectedRawFields.observe(var2))) {
                var0.argument = var2;
                var0.copied = true;
            } else {
                targetFailed(var0, null);
            }
        }
    }

    static void targetRemoved(OwnerNativeSemanticAuthority.TargetObservation var0, CompoundTag var1, String var2) {
        if (var0 != null) {
            if (targetLive(var0)
                && !var0.returned
                && var0.write.value == null
                && !var0.copied
                && var0.write.call.receiver == var1
                && var0.write.call.key.equals(var2)) {
                var0.removed = true;
                var0.returned = true;
            } else {
                targetFailed(var0, null);
            }
        }
    }

    static void targetReturned(OwnerNativeSemanticAuthority.TargetObservation var0, CompoundTag var1, String var2, Tag var3, Tag var4) {
        if (var0 != null) {
            if (targetLive(var0)
                && !var0.returned
                && var0.write.call.receiver == var1
                && var0.write.call.key.equals(var2)
                && var0.write.value != null
                && var0.copied
                && var0.argument == var3) {
                var0.originalReturn = var4;
                var0.returned = true;
            } else {
                targetFailed(var0, null);
            }
        }
    }

    static void targetFailed(OwnerNativeSemanticAuthority.TargetObservation var0, Throwable var1) {
        if (var0 != null && !var0.closed && var0.thread == Thread.currentThread()) {
            var0.failed = true;
            var0.session.observationFailed(null, 3, var1);
        }
    }

    static void targetClosed(OwnerNativeSemanticAuthority.TargetObservation var0) {
        if (var0 != null && !var0.closed && var0.thread == Thread.currentThread()) {
            try {
                if (targetLive(var0) && var0.returned && var0.removed == (var0.write.value == null)) {
                    var0.book.targetReturned(var0.session, var0.write.leaf, var0.write);
                    return;
                }

                targetFailed(var0, null);
            } catch (Throwable var5) {
                targetFailed(var0, var5);
                return;
            } finally {
                var0.closed = true;
            }
        }
    }

    private OwnerNativeSemanticAuthority() {
    }

    private static boolean stackHook() {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0 -> var0.filter(var0x -> var0x.getDeclaringClass() != OwnerNativeSemanticAuthority.class)
                        .findFirst()
                        .map(var0x -> var0x.getDeclaringClass() == ItemStack.class && var0x.getMethodName().contains("ftboss$ownerNativeLoadedInput"))
                        .orElse(false)
            );
    }

    private static boolean leafHook(String var0) {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var1 -> var1.filter(
                            var0xx -> var0xx.getDeclaringClass() != OwnerNativeSemanticAuthority.class
                                    && var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class
                        )
                        .findFirst()
                        .map(
                            var1x -> var1x.getDeclaringClass().getNestHost() == FoodWeaponHitPlan.class
                                    && (
                                        var1x.getDeclaringClass().getSimpleName().equals("TagLeaf")
                                            || var1x.getDeclaringClass().getSimpleName().equals("OutcomeTagLeaf")
                                    )
                                    && var1x.getMethodName().contains(var0)
                        )
                        .orElse(false)
            );
    }

    public static OwnerNativeSemanticAuthority.WeaponInput weaponInputQuoted(HolderLookup.Provider registries, CompoundTag var0) {
        if (stackHook() && registries != null && var0 != null) {
            boolean var1 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0x -> {
                        List var1x = var0x.filter(var0xx -> var0xx.getDeclaringClass() != OwnerNativeSemanticAuthority.class).limit(9L).toList();
                        return var1x.size() == 9
                            && ((StackFrame)var1x.get(0)).getDeclaringClass() == ItemStack.class
                            && ((StackFrame)var1x.get(0)).getMethodName().contains("ftboss$ownerNativeLoadedInput")
                            && ((StackFrame)var1x.get(1)).getDeclaringClass() == ItemStack.class
                            && ((StackFrame)var1x.get(1)).getMethodName().equals("parse")
                            && ((StackFrame)var1x.get(1)).getDescriptor().equals("(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/Tag;)Ljava/util/Optional;")
                            && ((StackFrame)var1x.get(2)).getDeclaringClass() == Inventory.class
                            && ((StackFrame)var1x.get(2)).getMethodName().equals("load")
                            && ((StackFrame)var1x.get(3)).getDeclaringClass() == Player.class
                            && ((StackFrame)var1x.get(3)).getMethodName().equals("readAdditionalSaveData")
                            && ((StackFrame)var1x.get(4)).getDeclaringClass() == ServerPlayer.class
                            && ((StackFrame)var1x.get(4)).getMethodName().equals("readAdditionalSaveData")
                            && ((StackFrame)var1x.get(5)).getDeclaringClass() == Entity.class
                            && ((StackFrame)var1x.get(5)).getMethodName().equals("load")
                            && ((StackFrame)var1x.get(6)).getDeclaringClass() == PlayerDataStorage.class
                            && ((StackFrame)var1x.get(6)).getMethodName().equals("lambda$load$1")
                            && ((StackFrame)var1x.get(6)).getDescriptor().equals("(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;")
                            && ((StackFrame)var1x.get(7)).getDeclaringClass() == Optional.class
                            && ((StackFrame)var1x.get(7)).getMethodName().equals("map")
                            && ((StackFrame)var1x.get(7)).getDescriptor().equals("(Ljava/util/function/Function;)Ljava/util/Optional;")
                            && ((StackFrame)var1x.get(8)).getDeclaringClass() == PlayerDataStorage.class
                            && ((StackFrame)var1x.get(8)).getMethodName().equals("load")
                            && ((StackFrame)var1x.get(8)).getDescriptor().equals("(Lnet/minecraft/world/entity/player/Player;)Ljava/util/Optional;");
                    }
                );
            if (!var1) {
                return null;
            } else {
                Value var2 = NativeExpectedRawFields.observe(var0);
                Value var6 = NativeInventoryItemTrace.project(var2);
                return var6 == null ? null : new OwnerNativeSemanticAuthority.WeaponInput(registries, var0, var6);
            }
        } else {
            return null;
        }
    }

    private static boolean knownStorage(OwnerNativeSemanticAuthority.ReadContext var0) {
        if (!readLive(var0)) return false;
        NativeItemComponentObservation observed = NativeItemComponentObservation.capture(var0.actual, var0.input.registries);
        return observed.count() == 1 && observed.itemIdentity() != null && observed.matches(var0.actual) && readLive(var0);
    }

    public static void weaponInputLoaded(OwnerNativeSemanticAuthority.WeaponInput var0, ItemStack var1) {
        if (stackHook()
            && var0 != null
            && !var0.retired
            && var0.actual == null
            && var0.thread == Thread.currentThread()
            && var1 != null
            && (Object)var1 instanceof OwnerNativeWeaponInputFields var2
            && (Object)var1 instanceof NativeItemComponentFields.Stack) {
            OwnerNativeSemanticAuthority.ReadContext var3 = new OwnerNativeSemanticAuthority.ReadContext(var0, var1, null, null, null, null, null, null, true);

            try {
                if (!knownStorage(var3) || !matchesWeapon(var3, var0.expected)) {
                    return;
                }

                CompoundTag var4 = (CompoundTag)read(var3, var1, OwnerNativeSemanticAuthority.RawField.CUSTOM_DATA);
                Object var5 = read(var3, var1, OwnerNativeSemanticAuthority.RawField.COMPONENTS);
                if (!readLive(var3)) {
                    rejectRead(var3, null);
                    return;
                }

                var0.actual = new WeakReference<>(var1);
                var0.root = var4;
                var0.components = var5;
                var2.ftboss$ownerInstallNativeWeaponInput(var0);
                if (var2.ftboss$ownerNativeWeaponInput() != var0) {
                    var0.retired = true;
                }

                if (var0.root != null) {
                    OwnerFullRepairGuard.alias(var1, var0.root, false);
                }
            } catch (Throwable var6) {
                rejectRead(var3, var6);
            }

            return;
        }
    }

    private static boolean matchesWeapon(OwnerNativeSemanticAuthority.ReadContext var0, Value var1) {
        if (!readLive(var0) || !knownStorage(var0) || NativeInventoryItemTrace.project(var1) == null) return false;
        CompoundTag actual = NativeFoundationItemData.image(var0.actual, var0.input.registries);
        return FoodWeaponExpectedTrace.same(var1, NativeExpectedRawFields.observe(actual)) && readLive(var0);
    }

    private static OwnerNativeSemanticAuthority.ReadContext weapon(OwnerFieldExpectedAuthority.Prepared var0, Object var1) {
        if (var1 instanceof ItemStack var2 && (Object)var1 instanceof OwnerNativeWeaponInputFields var3) {
            OwnerNativeSemanticAuthority.WeaponInput var4 = var3.ftboss$ownerNativeWeaponInput();
            if (var4 != null && !var4.retired && var4.actual != null && var4.actual.get() == var1) {
                OwnerNativeSemanticAuthority.ReadContext var5 = new OwnerNativeSemanticAuthority.ReadContext(
                    var4, var2, var0, null, null, null, null, null, false
                );

                try {
                    return readLive(var5)
                            && var4.root == read(var5, var1, OwnerNativeSemanticAuthority.RawField.CUSTOM_DATA)
                            && knownStorage(var5)
                            && var4.components == read(var5, var1, OwnerNativeSemanticAuthority.RawField.COMPONENTS)
                            && matchesWeapon(var5, var4.expected)
                            && readLive(var5)
                        ? var5
                        : null;
                } catch (Throwable var7) {
                    rejectRead(var5, var7);
                    return null;
                }
            }

            return null;
        }

        return null;
    }

    public static void wearInputLoaded(OwnerStatueWearAdmission.Load var0, CompoundTag var1, ItemStack var2) {
        boolean var3 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerStatueWearAdmission.class && var0xx.getMethodName().equals("loadBound"))
                        .orElse(false)
            );
        if (var3
            && var0 != null
            && var2 != null
            && OwnerRepairGenerationAuthority.statueTaskObjectPolicy(var2)
            && (Object)var2 instanceof OwnerNativeWeaponInputFields var4) {
            OwnerNativeSemanticAuthority.WeaponInput var5 = var4.ftboss$ownerNativeWeaponInput();
            if (var5 != null
                && !var5.retired
                && var5.thread == Thread.currentThread()
                && var5.actual != null
                && var5.actual.get() == var2
                && var5.originalInput.get() == var1
                && var5.wearOrigin == null) {
                OwnerStatueWearAdmission.Origin var6 = OwnerStatueWearAdmission.quotedOrigin(var0, var1, var2, var5, var5.expected);
                if (var6 != null && OwnerFullRepairGuard.watchLoadedWear(var2)) {
                    var5.wearOrigin = var6;
                }
            }
        }
    }

    static OwnerStatueWearAdmission.Origin wearOrigin(ItemStack var0) {
        boolean var1 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(
                            var0xx -> var0xx.getDeclaringClass() == OwnerStatueWearAdmission.class
                                    && (var0xx.getMethodName().equals("entered") || var0xx.getMethodName().equals("activeRoot"))
                        )
                        .orElse(false)
            );
        if (var1 && (Object)var0 instanceof OwnerNativeWeaponInputFields var2) {
            OwnerNativeSemanticAuthority.WeaponInput var3 = var2.ftboss$ownerNativeWeaponInput();
            return var3 != null && var3.actual != null && var3.actual.get() == var0 ? var3.wearOrigin : null;
        } else {
            return null;
        }
    }

    static CompoundTag wearImage(OwnerStatueWearAdmission.Origin var0, ItemStack var1, OwnerStatueWearAdmission.Reading var2) {
        boolean var3 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(var0x -> var0x.skip(1L).findFirst().map(var0xx -> var0xx.getDeclaringClass() == OwnerStatueWearAdmission.class).orElse(false));
        if (var3 && var0 != null && var2 != null && var1 != null && var1.getClass() == ItemStack.class && (Object)var1 instanceof NativeItemComponentFields.Stack) {
            OwnerNativeSemanticAuthority.ReadContext var5 = new OwnerNativeSemanticAuthority.ReadContext(
                var0.input, var1, null, null, null, null, null, null, false
            );
            var5.wearReading = var2;

            try {
                if (readLive(var5) && knownStorage(var5)) {
                    CompoundTag var6 = (CompoundTag)read(var5, var1, OwnerNativeSemanticAuthority.RawField.CUSTOM_DATA);
                    Object var7 = read(var5, var1, OwnerNativeSemanticAuthority.RawField.COMPONENTS);
                    NativeItemComponentObservation observed = NativeItemComponentObservation.capture(var1, var5.input.registries);
                    if (observed.itemIdentity() == null || observed.count() != 1 || !observed.matches(var1)) {
                        return null;
                    } else {
                        Value var11 = NativeExpectedRawFields.observe(NativeFoundationItemData.image(var1, var5.input.registries));
                        if (!var2.before() || var0.input.root == var6 && var0.input.components == var7 && FoodWeaponExpectedTrace.same(var0.expected, var11)) {
                            if (!readLive(var5)) {
                                return null;
                            } else {
                                CompoundTag var12 = (CompoundTag)FoodWeaponExpectedTrace.materialize(var11);
                                return FoodWeaponExpectedTrace.same(var11, NativeExpectedRawFields.observe(var12)) && readLive(var5) ? var12 : null;
                            }
                        } else {
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            } catch (Throwable var13) {
                rejectRead(var5, var13);
                return null;
            }
        } else {
            return null;
        }
    }

    static void changed(Object var0) {
        if ((Object)var0 instanceof OwnerNativeWeaponInputFields var1) {
            OwnerNativeSemanticAuthority.WeaponInput var2 = var1.ftboss$ownerNativeWeaponInput();
            if (var2 != null && var2.actual != null && var2.actual.get() == var0) {
                var2.retired = true;
            }
        }
    }

    static OwnerNativeSemanticAuthority.Before before(
        OwnerFieldExpectedAuthority.Prepared var0,
        Object var1,
        Entity var2,
        Entity var3,
        Object var4,
        Object var5,
        Object var6,
        Value var7,
        Value var8,
        Value var9
    ) {
        boolean var10 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerFieldExpectedAuthority.Prepared.class && var0xx.getMethodName().equals("<init>"))
                        .orElse(false)
            );
        if (var10 && var0 != null && var2 != null && var6 != null) {
            OwnerNativeSemanticAuthority.ReadContext var11 = weapon(var0, var1);
            if (var11 != null && FoodWeaponExpectedTrace.same(var11.input.expected, var7)) {
                OwnerNativeSemanticAuthority.Before var12 = new OwnerNativeSemanticAuthority.Before(var0, var11, var2, var3, var4, var5, var6, var8, var9);
                return readLive(var11) ? var12 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static boolean matchesState(OwnerNativeSemanticAuthority.ReadContext var0, State var1) {
        OwnerNativeSemanticAuthority.Before var2 = var0.before;
        return var2 != null
            && var2.weaponGeneration == var0.inputGeneration
            && var2.preparation == var0.preparation
            && readLive(var0)
            && knownStorage(var0)
            && var2.weapon.root == read(var0, var0.actual, OwnerNativeSemanticAuthority.RawField.CUSTOM_DATA)
            && var2.weapon.components == read(var0, var0.actual, OwnerNativeSemanticAuthority.RawField.COMPONENTS)
            && matchesWeapon(var0, var1.weapon)
            && read(var0, var2.player, OwnerNativeSemanticAuthority.RawField.PERSISTENT) == var2.playerRoot
            && FoodWeaponExpectedTrace.same(var1.player, NativeExpectedRawFields.observe((CompoundTag)var2.playerRoot))
            && (
                var2.carrier == null
                    ? FoodWeaponExpectedTrace.same(var1.carrier, FoodWeaponExpectedTrace.emptyCompound())
                    : read(var0, var2.carrier, OwnerNativeSemanticAuthority.RawField.PERSISTENT) == var2.carrierRoot
                        && FoodWeaponExpectedTrace.same(var1.carrier, NativeExpectedRawFields.observe((CompoundTag)var2.carrierRoot))
            )
            && readLive(var0);
    }

    static Value expectedWeapon(OwnerNativeSemanticAuthority.Before var0) {
        return var0 == null ? null : var0.expected.weapon;
    }

    public static OwnerNativeSemanticAuthority.PutQuote putQuoted(Object var0, CompoundTag var1, String var2, Tag var3) {
        return leafHook("ftboss$ownerNativePutQuoted") && var0 != null && var1 != null && var2 != null && var3 != null
            ? new OwnerNativeSemanticAuthority.PutQuote(var0, var1, var2, var3)
            : null;
    }

    private static void locate(
        OwnerNativeSemanticAuthority.ReadContext var0,
        Object var1,
        Value var2,
        String[] var3,
        Object var4,
        Domain var5,
        ArrayList<OwnerNativeSemanticAuthority.PutBinding> var6,
        OwnerNativeSemanticAuthority.PutQuote var7,
        OwnerNativeSemanticAuthority.Before var8,
        IdentityHashMap<Object, Boolean> var9,
        int[] var10
    ) {
        if (var3.length <= 64 && ++var10[0] <= 65536) {
            if (var1 instanceof Compound && var2 != null && var2.kind == 10) {
                if (var9.put(var1, Boolean.TRUE) != null) {
                    throw new IllegalArgumentException("Native aliases or cycle are ambiguous");
                } else {
                    if (var1 == var4) {
                        var6.add(new OwnerNativeSemanticAuthority.PutBinding(var7, var5, var3, var8, null));
                    }

                    for (Entry var12 : compound(var0, var1).entrySet()) {
                        Value var13 = FoodWeaponExpectedTrace.field(var2, (String)var12.getKey());
                        if (var13 != null && var13.kind == 10) {
                            String[] var14 = Arrays.copyOf(var3, var3.length + 1);
                            var14[var3.length] = (String)var12.getKey();
                            var0.compounds.put(var12.getValue(), Boolean.TRUE);
                            locate(var0, var12.getValue(), var13, var14, var4, var5, var6, var7, var8, var9, var10);
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Independent native receiver path budget");
        }
    }

    static OwnerNativeSemanticAuthority.PutBinding appendPut(
        OwnerNativeSemanticAuthority.Before var0, Builder var1, Object var2, OwnerNativeSemanticAuthority.PutQuote var3
    ) {
        boolean var4 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerFieldExpectedAuthority.class && var0xx.getMethodName().equals("quoteTimers"))
                        .orElse(false)
            );
        if (var4
            && var0 != null
            && var1 != null
            && var3 != null
            && var3.leaf == var2
            && var3.thread == Thread.currentThread()
            && var0.weapon.actual != null
            && var0.weapon.actual.get() != null) {
            OwnerNativeSemanticAuthority.ReadContext var5 = new OwnerNativeSemanticAuthority.ReadContext(
                var0.weapon, var0.weapon.actual.get(), var0.preparation, var0, null, null, null, var2, false
            );

            try {
                if (var2 instanceof OwnerNativePutQuoteFields var6
                    && var6.ftboss$ownerNativePutQuote() == var3
                    && FoodWeaponExpectedTrace.same(var3.argument, NativeExpectedRawFields.observe(var6.ftboss$ownerNativeOriginalValue()))
                    && matchesState(var5, var0.expected)) {
                    ArrayList var7 = new ArrayList();
                    IdentityHashMap var8 = new IdentityHashMap();
                    int[] var9 = new int[]{0};
                    locate(
                        var5,
                        var0.weapon.root,
                        NativeInventoryItemTrace.customDataRoot(var0.expected.weapon),
                        NativeInventoryItemTrace.customDataPath(),
                        var3.receiver,
                        Domain.WEAPON,
                        var7,
                        var3,
                        var0,
                        var8,
                        var9
                    );
                    locate(var5, var0.playerRoot, var0.expected.player, new String[0], var3.receiver, Domain.PLAYER, var7, var3, var0, var8, var9);
                    locate(var5, var0.carrierRoot, var0.expected.carrier, new String[0], var3.receiver, Domain.CARRIER, var7, var3, var0, var8, var9);
                    if (var7.size() == 1 && readLive(var5)) {
                        OwnerNativeSemanticAuthority.PutBinding var10 = (OwnerNativeSemanticAuthority.PutBinding)var7.get(0);
                        Operation var11 = var1.nativePut(var10.domain, var10.path, var3.key, var3.argument);
                        return new OwnerNativeSemanticAuthority.PutBinding(var3, var10.domain, var10.path, var0, var11);
                    }

                    return null;
                }

                return null;
            } catch (Throwable var12) {
                rejectRead(var5, var12);
                return null;
            }
        } else {
            return null;
        }
    }

    private static void rejectSaved(
        OwnerNativeSemanticAuthority.Before var0, FoodWeaponOriginalSequence.Session var1, FoodWeaponOriginalSequence.Step var2, Throwable var3, boolean var4
    ) {
        if (var0 != null) {
            try {
                OwnerFieldExpectedAuthority.nativeValidationFailed(var0.preparation, var0);
            } catch (Throwable var8) {
            }
        }

        if (var1 != null && var2 != null && var2.session == var1) {
            try {
                if (var4) {
                    var1.originalThrown(var2, var3);
                } else {
                    var1.observationFailed(var2, 1, var3);
                }
            } catch (Throwable var7) {
            }

            try {
                var1.close(var2);
            } catch (Throwable var6) {
            }
        }
    }

    private static void rejectAdmission(OwnerNativeSemanticAuthority.Admission var0, Throwable var1, boolean var2) {
        if (active(var0)) {
            var0.closed = true;
            rejectSaved(var0.binding.before, var0.sequence, var0.step, var1, var2);
        }
    }

    static Object semanticRangedBuffer(OwnerNativeSemanticAuthority.Before var0) {
        boolean var1 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerFieldExpectedAuthority.class && var0xx.getMethodName().equals("quoteTimers"))
                        .orElse(false)
            );
        if (var1 && var0 != null && var0.weapon.actual != null && var0.weapon.actual.get() != null) {
            OwnerNativeSemanticAuthority.ReadContext var2 = new OwnerNativeSemanticAuthority.ReadContext(
                var0.weapon, var0.weapon.actual.get(), var0.preparation, var0, null, null, null, null, false
            );

            try {
                if (!matchesState(var2, var0.expected)) {
                    return null;
                } else {
                    Object var3 = compound(var2, var0.playerRoot).get("FTBossRangedProcTimers");
                    Value var4 = FoodWeaponExpectedTrace.field(var0.expected.player, "FTBossRangedProcTimers");
                    return readLive(var2) && var4 != null && var4.kind == 10 && var3 instanceof CompoundTag ? var3 : null;
                }
            } catch (Throwable var5) {
                rejectRead(var2, var5);
                return null;
            }
        } else {
            return null;
        }
    }

    static Operation semanticOperation(OwnerNativeSemanticAuthority.PutBinding var0, String var1, State var2, Builder var3) {
        boolean var4 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerTimerSemanticAuthority.Book.class && var0xx.getMethodName().equals("resolve"))
                        .orElse(false)
            );
        return var4 && var0 != null && var2 != null && var3 != null && Objects.equals(var1, var0.quote.key)
            ? var3.nativePut(var0.domain, var0.path, var1, var0.quote.argument)
            : null;
    }

    private static boolean matchesSemanticStep(
        OwnerNativeSemanticAuthority.PutBinding var0, FoodWeaponOriginalSequence.Session var1, FoodWeaponOriginalSequence.Step var2
    ) {
        return var0 != null && var2 != null && var2.session == var1 && (var0.operation == var2.expected || var1.semanticNativeBinding(var2, var0));
    }

    static OwnerNativeSemanticAuthority.Admission admit(
        FoodWeaponOriginalSequence.Session var0, OwnerNativeSemanticAuthority.PutBinding var1, Object var2, Tag var3
    ) {
        if (leafHook("ftboss$ownerNativeCopy")
            && var0 != null
            && var1 != null
            && var1.quote.leaf == var2
            && var0.original.atLeaf(var2)
            && var1.before.weapon.actual != null
            && !var1.before.weapon.retired
            && var0.lineage == var1.before.preparation.lineage
            && var2 instanceof OwnerNativePutQuoteFields var4
            && var4.ftboss$ownerNativeOriginalValue() == var3) {
            FoodWeaponOriginalSequence.Step var5 = null;

            try {
                var5 = var0.originalBegins(var2, Kind.NATIVE_PUT, var1.quote.key, null);
                if (var5 == null) {
                    return null;
                }

                ItemStack var6 = var1.before.weapon.actual.get();
                OwnerNativeSemanticAuthority.ReadContext var7 = new OwnerNativeSemanticAuthority.ReadContext(
                    var1.before.weapon, var6, var1.before.preparation, var1.before, var0, var5, var1, var2, false
                );
                if (matchesSemanticStep(var1, var0, var5) && matchesState(var7, var5.expected.before)) {
                    OwnerNativeSemanticAuthority.Admission var8 = new OwnerNativeSemanticAuthority.Admission(var0, var5, var1, var3);
                    var0.originalCopyBegins(var5, var1.quote.receiver, var3);
                    return var8;
                }

                rejectSaved(var1.before, var0, var5, null, false);
                return null;
            } catch (Throwable var9) {
                rejectSaved(var1.before, var0, var5, var9, false);
                return null;
            }
        }

        return null;
    }

    public static void copied(OwnerNativeSemanticAuthority.Admission var0, Tag var1, Tag var2) {
        if (leafHook("ftboss$ownerNativeCopy") && active(var0)) {
            try {
                if (var0.copied || var0.originalValue != var1) {
                    rejectAdmission(var0, null, false);
                    return;
                }

                var0.argument = var2;
                var0.sequence.originalCopyReturned(var0.step, var0.binding.quote.receiver, var1, var2);
                var0.sequence.originalCopyFieldsObserved(var0.step, var2, NativeExpectedRawFields.observe(var2));
                var0.copied = true;
            } catch (Throwable var4) {
                rejectAdmission(var0, var4, false);
            }
        }
    }

    public static void entering(OwnerNativeSemanticAuthority.Admission var0, CompoundTag var1, String var2, Tag var3) {
        if (leafHook("ftboss$ownerNativePut") && active(var0)) {
            try {
                if (!var0.copied || var0.entered || var1 != var0.binding.quote.receiver || !var0.binding.quote.key.equals(var2) || var3 != var0.argument) {
                    rejectAdmission(var0, null, false);
                    return;
                }

                var0.sequence.entryReturned(var0.step, var1, var0.originalValue, var3);
                var0.entered = true;
            } catch (Throwable var5) {
                rejectAdmission(var0, var5, false);
            }
        }
    }

    public static void returned(OwnerNativeSemanticAuthority.Admission var0, CompoundTag var1, String var2, Tag var3, Tag var4) {
        if (leafHook("ftboss$ownerNativePut") && active(var0)) {
            try {
                if (!var0.entered || var1 != var0.binding.quote.receiver || !var0.binding.quote.key.equals(var2) || var3 != var0.argument) {
                    rejectAdmission(var0, null, false);
                    return;
                }

                OwnerNativeSemanticAuthority.Before var5 = var0.binding.before;
                OwnerNativeSemanticAuthority.ReadContext var6 = new OwnerNativeSemanticAuthority.ReadContext(
                    var5.weapon, var5.weapon.actual.get(), var5.preparation, var5, var0.sequence, var0.step, var0.binding, var0.binding.quote.leaf, false
                );
                if (compound(var6, var1).get(var2) != var3) {
                    rejectAdmission(var0, null, false);
                    return;
                }

                var0.sequence.writeObserved(var0.step, var1, var0.binding.domain, var0.binding.path, var2, NativeExpectedRawFields.observe(var3));
                if (!matchesState(var6, var0.step.expected.after)) {
                    rejectAdmission(var0, null, false);
                    return;
                }

                var0.sequence.originalReturned(var0.step, var4);
            } catch (Throwable var7) {
                rejectAdmission(var0, var7, false);
            }
        }
    }

    private static boolean active(OwnerNativeSemanticAuthority.Admission var0) {
        return var0 != null && !var0.closed && var0.thread == Thread.currentThread();
    }

    public static void failed(OwnerNativeSemanticAuthority.Admission var0, Throwable var1, boolean var2) {
        if (leafHook("ftboss$ownerNative") && active(var0)) {
            rejectAdmission(var0, var1, var2);
        }
    }

    public static void close(OwnerNativeSemanticAuthority.Admission var0) {
        if (leafHook("ftboss$ownerNative") && active(var0)) {
            var0.closed = true;

            try {
                var0.sequence.close(var0.step);
            } catch (Throwable var2) {
                rejectSaved(var0.binding.before, var0.sequence, var0.step, var2, false);
            }
        }
    }

    public static final class Admission {
        private final FoodWeaponOriginalSequence.Session sequence;
        private final FoodWeaponOriginalSequence.Step step;
        private final OwnerNativeSemanticAuthority.PutBinding binding;
        private final Object originalValue;
        private final Thread thread = Thread.currentThread();
        private Object argument;
        private boolean copied;
        private boolean entered;
        private boolean closed;

        private Admission(
            FoodWeaponOriginalSequence.Session var1, FoodWeaponOriginalSequence.Step var2, OwnerNativeSemanticAuthority.PutBinding var3, Object var4
        ) {
            this.sequence = var1;
            this.step = var2;
            this.binding = var3;
            this.originalValue = var4;
        }
    }

    static final class Before {
        private final OwnerFieldExpectedAuthority.Prepared preparation;
        private final OwnerNativeSemanticAuthority.WeaponInput weapon;
        private final Object weaponGeneration;
        private final Object guardGeneration;
        private final OwnerLineageRevocationStore.Lease lease;
        private final Entity player;
        private final Entity carrier;
        private final Object playerRoot;
        private final Object carrierRoot;
        private final Object source;
        private final State expected;

        private Before(
            OwnerFieldExpectedAuthority.Prepared var1,
            OwnerNativeSemanticAuthority.ReadContext var2,
            Entity var3,
            Entity var4,
            Object var5,
            Object var6,
            Object var7,
            Value var8,
            Value var9
        ) {
            this.preparation = var1;
            this.weapon = var2.input;
            this.weaponGeneration = var2.inputGeneration;
            this.guardGeneration = var2.guardGeneration;
            this.lease = var2.lease;
            this.player = var3;
            this.carrier = var4;
            this.playerRoot = var5;
            this.carrierRoot = var6;
            this.source = var7;
            this.expected = new State(this.weapon.expected, var8, var9);
        }
    }

    static final class PutBinding {
        final OwnerNativeSemanticAuthority.PutQuote quote;
        final Domain domain;
        final String[] path;
        private final OwnerNativeSemanticAuthority.Before before;
        private final Operation operation;

        private PutBinding(OwnerNativeSemanticAuthority.PutQuote var1, Domain var2, String[] var3, OwnerNativeSemanticAuthority.Before var4, Operation var5) {
            this.quote = var1;
            this.domain = var2;
            this.path = (String[])var3.clone();
            this.before = var4;
            this.operation = var5;
        }
    }

    public static final class PutQuote {
        private final Object leaf;
        private final CompoundTag receiver;
        private final String key;
        private final Value argument;
        private final Thread thread = Thread.currentThread();

        private PutQuote(Object var1, CompoundTag var2, String var3, Tag var4) {
            this.leaf = var1;
            this.receiver = var2;
            this.key = var3;
            this.argument = NativeExpectedRawFields.observe(var4);
        }
    }

    private static enum RawField {
        CUSTOM_DATA(net.minecraft.world.item.component.CustomData.class, "ftboss$nativeCustomData", "()Lnet/minecraft/nbt/CompoundTag;"),
        COMPONENTS(ItemStack.class, "ftboss$nativeComponents", "()Lnet/minecraft/core/component/PatchedDataComponentMap;"),
        PERSISTENT(Entity.class, "ftboss$rawPersistentData", "()Lnet/minecraft/nbt/CompoundTag;"),
        COMPOUND(CompoundTag.class, "ftboss$rawCompoundFields", "()Ljava/util/Map;");

        private final Class<?> owner;
        private final String member;
        private final String descriptor;

        private RawField(Class<?> var3, String var4, String var5) {
            this.owner = var3;
            this.member = var4;
            this.descriptor = var5;
        }
    }

    private static final class ReadContext {
        private final OwnerNativeSemanticAuthority.WeaponInput input;
        private final Object inputGeneration;
        private final ItemStack actual;
        private final Object guardGeneration;
        private final OwnerLineageRevocationStore.Lease lease;
        private final OwnerFieldExpectedAuthority.Prepared preparation;
        private final OwnerNativeSemanticAuthority.Before before;
        private final FoodWeaponOriginalSequence.Session sequence;
        private final FoodWeaponOriginalSequence.Step step;
        private final OwnerNativeSemanticAuthority.PutBinding binding;
        private final Object leaf;
        private final boolean loading;
        private final Thread thread = Thread.currentThread();
        private final IdentityHashMap<Object, Boolean> compounds = new IdentityHashMap<>();
        private boolean rejected;
        private OwnerStatueWearAdmission.Reading wearReading;
        private Throwable observationFailure;

        private ReadContext(
            OwnerNativeSemanticAuthority.WeaponInput var1,
            ItemStack var2,
            OwnerFieldExpectedAuthority.Prepared var3,
            OwnerNativeSemanticAuthority.Before var4,
            FoodWeaponOriginalSequence.Session var5,
            FoodWeaponOriginalSequence.Step var6,
            OwnerNativeSemanticAuthority.PutBinding var7,
            Object var8,
            boolean var9
        ) {
            this.input = var1;
            this.inputGeneration = var1.generation;
            this.actual = var2;
            this.guardGeneration = var4 == null ? OwnerFullRepairGuard.generation(var2) : var4.guardGeneration;
            this.lease = var4 == null ? OwnerFullRepairGuard.lease(var2) : var4.lease;
            this.preparation = var3;
            this.before = var4;
            this.sequence = var5;
            this.step = var6;
            this.binding = var7;
            this.leaf = var8;
            this.loading = var9;
            if (var4 != null) {
                this.compounds.put(var4.weapon.root, Boolean.TRUE);
                this.compounds.put(var4.playerRoot, Boolean.TRUE);
                this.compounds.put(var4.carrierRoot, Boolean.TRUE);
            }

            if (var7 != null) {
                this.compounds.put(var7.quote.receiver, Boolean.TRUE);
            }
        }
    }

    private static final class ReadFrame {
        private final OwnerNativeSemanticAuthority.ReadContext context;
        private final Object receiver;
        private final OwnerNativeSemanticAuthority.RawField field;
        private final OwnerNativeSemanticAuthority.ReadFrame previous;
        private boolean observed;
        private Object returned;
        private boolean accepted;
        private boolean closed;

        private ReadFrame(
            OwnerNativeSemanticAuthority.ReadContext var1, Object var2, OwnerNativeSemanticAuthority.RawField var3, OwnerNativeSemanticAuthority.ReadFrame var4
        ) {
            this.context = var1;
            this.receiver = var2;
            this.field = var3;
            this.previous = var4;
        }
    }

    static final class TargetObservation {
        private final OwnerTimerSemanticAuthority.Book book;
        private final FoodWeaponOriginalSequence.Session session;
        private final FoodWeaponHitPlan.FinalEffectsCommit commit;
        private final FoodWeaponFinalDamage.ChildHurtBridge bridge;
        private final OwnerNativeSemanticAuthority.TargetWrite write;
        private final Thread thread = Thread.currentThread();
        private Tag argument;
        private Tag originalReturn;
        private boolean copied;
        private boolean returned;
        private boolean removed;
        private boolean failed;
        private boolean closed;

        private TargetObservation(
            OwnerTimerSemanticAuthority.Book var1,
            FoodWeaponOriginalSequence.Session var2,
            FoodWeaponHitPlan.FinalEffectsCommit var3,
            FoodWeaponFinalDamage.ChildHurtBridge var4,
            OwnerNativeSemanticAuthority.TargetWrite var5
        ) {
            this.book = var1;
            this.session = var2;
            this.commit = var3;
            this.bridge = var4;
            this.write = var5;
        }
    }

    private static record TargetReceiver(FoodWeaponHitPlan.Builder builder, LivingEntity target, CompoundTag receiver, Thread thread) {
    }

    static final class TargetWrite {
        private final OwnerNativeSemanticAuthority.TargetWriteCall call;
        private final Object leaf;
        private final Tag value;
        private FoodWeaponHitPlan plan;

        private TargetWrite(OwnerNativeSemanticAuthority.TargetWriteCall var1, Object var2, Tag var3) {
            this.call = var1;
            this.leaf = var2;
            this.value = var3;
        }
    }

    public static final class TargetWriteCall {
        private final FoodWeaponHitPlan.Builder builder;
        private final ServerPlayer player;
        private final LivingEntity target;
        private final ItemStack weapon;
        private final CompoundTag receiver;
        private final String key;
        private final FoodWeaponSourceLedger.AttemptDomain attempt;
        private final Tag input;
        private final Value value;
        private final Thread thread = Thread.currentThread();
        private final OwnerNativeSemanticAuthority.TargetWriteCall previous;
        private boolean consumed;
        private boolean closed;

        private TargetWriteCall(
            FoodWeaponHitPlan.Builder var1,
            ServerPlayer var2,
            LivingEntity var3,
            ItemStack var4,
            CompoundTag var5,
            String var6,
            FoodWeaponSourceLedger.AttemptDomain var7,
            Tag var8,
            OwnerNativeSemanticAuthority.TargetWriteCall var9
        ) {
            this.builder = var1;
            this.player = var2;
            this.target = var3;
            this.weapon = var4;
            this.receiver = var5;
            this.key = var6;
            this.attempt = var7;
            this.input = var8;
            this.value = var8 == null ? null : NativeExpectedRawFields.observe(var8);
            this.previous = var9;
        }
    }

    static boolean advance(Before before, FoodWeaponOriginalSequence.Session session, FoodWeaponOriginalSequence.Step step) {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == OwnerFieldExpectedAuthority.class && frame.getMethodName().equals("advance")).orElse(false));
        if (!caller || before == null || session == null || step == null || step.session != session
            || !OwnerFieldExpectedAuthority.nativeValidationLive(before.preparation, before, session)) return false;
        WeaponInput input = before.weapon;
        ItemStack stack = input.actual == null ? null : input.actual.get();
        if (stack == null || input.retired || input.thread != Thread.currentThread() || input.generation != before.weaponGeneration
            || !((Object)stack instanceof OwnerNativeWeaponInputFields fields) || fields.ftboss$ownerNativeWeaponInput() != input
            || OwnerFullRepairGuard.generation(stack) != before.guardGeneration || OwnerFullRepairGuard.lease(stack) != before.lease
            || !FoodWeaponExpectedTrace.same(input.expected, step.expected.before.weapon)) return false;
        NativeItemComponentObservation after = NativeItemComponentObservation.capture(stack, input.registries);
        if (after.count() != 1 || !after.matches(stack)
            || !FoodWeaponExpectedTrace.same(step.expected.after.weapon,
                NativeExpectedRawFields.observe(NativeFoundationItemData.image(stack, input.registries)))) {
            input.retired = true;
            return false;
        }
        CompoundTag root = (CompoundTag)after.nativeWearStorage(true);
        Object components = after.nativeWearStorage(false);
        if (input.retired || fields.ftboss$ownerNativeWeaponInput() != input || !after.matches(stack)
            || OwnerFullRepairGuard.generation(stack) != before.guardGeneration || OwnerFullRepairGuard.lease(stack) != before.lease) return false;
        input.expected = step.expected.after.weapon;
        input.root = root;
        input.components = components;
        return true;
    }

    public static final class WeaponInput {
        private final HolderLookup.Provider registries;
        private final WeakReference<CompoundTag> originalInput;
        private Value expected;
        private final Thread thread = Thread.currentThread();
        private final Object generation = new Object();
        private WeakReference<ItemStack> actual;
        private CompoundTag root;
        private Object components;
        private OwnerStatueWearAdmission.Origin wearOrigin;
        private boolean retired;

        private WeaponInput(HolderLookup.Provider registries, CompoundTag var1, Value var2) {
            this.registries = Objects.requireNonNull(registries, "registries");
            this.originalInput = new WeakReference<>(var1);
            this.expected = var2;
        }
    }
    private static final ThreadLocal<NativeWear> NATIVE_WEAR = new ThreadLocal<>();

    private static boolean nativeWearCaller(String method) {
        StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(2L).findFirst().orElseThrow());
        return caller.getDeclaringClass() == FoodWeaponSourceLedger.class && caller.getMethodName().equals(method);
    }

    private static NativeWear nativeTransition(Object receipt, ItemStack stack, boolean initialization) {
        if (NATIVE_WEAR.get() != null) {
            NATIVE_WEAR.get().rejected = true;
            return null;
        }
        try {
            if (receipt == null || !((Object)stack instanceof OwnerNativeWeaponInputFields fields)) return null;
            WeaponInput input = fields.ftboss$ownerNativeWeaponInput();
            if (input == null || input.retired || input.thread != Thread.currentThread() || input.actual == null || input.actual.get() != stack) return null;
            if (!FoodWeaponExpectedTrace.same(input.expected, NativeExpectedRawFields.observe(NativeFoundationItemData.image(stack, input.registries)))) return null;
            NativeItemComponentObservation observed = NativeItemComponentObservation.capture(stack, input.registries);
            if (observed.count() != 1 || !observed.matches(stack) || observed.nativeWearStorage(true) != input.root
                || observed.nativeWearStorage(false) != input.components) return null;
            CompoundTag expected = (CompoundTag)FoodWeaponExpectedTrace.materialize(input.expected);
            ItemStack detached = NativeRepairStackImage.decode(input.registries, expected.copy());
            if (!FourWeaponFoundationAdapter.read(detached).valid()) return null;
            NativeWear transition = new NativeWear(receipt, stack, input, observed, initialization, expected, detached.getDamageValue());
            if (initialization) {
                CompoundTag custom = expected.getCompound("components").getCompound("minecraft:custom_data");
                CompoundTag lifecycle = custom.getCompound("FTBossWeaponLifecycle").copy();
                transition.identityRequired = !lifecycle.hasUUID("Weapon");
                if (!lifecycle.contains("WearStep", 3)) lifecycle.putInt("WearStep", net.farmingtales.ftboss.util.FoodWeaponLifecycleRules.wearStep(detached.getDamageValue(), detached.getMaxDamage()));
                if (FoodWeaponEvolution.evolutionLevel(detached) > 0) lifecycle.putBoolean("WasEvolved", true);
                if (lifecycle.getString("RepairProfile").isEmpty()) {
                    String profile = NativeFoodWeaponEvolutionRead.repairProfileId(detached);
                    if (!profile.isEmpty()) lifecycle.putString("RepairProfile", profile);
                }
                if (NativeFoodWeaponWear.eligible(detached) && detached.getDamageValue() >= detached.getMaxDamage()) lifecycle.putBoolean("Broken", true);
                custom.put("FTBossWeaponLifecycle", lifecycle);
            }
            NATIVE_WEAR.set(transition);
            return transition;
        } catch (LinkageError | RuntimeException refused) {
            return null;
        }
    }

    static NativeWear nativeLifecycleBefore(ItemStack stack, ServerPlayer player) {
        return nativeWearCaller("beginNative") ? nativeTransition(player, stack, true) : null;
    }

    public static java.util.UUID nativeLifecycleIdentity(ItemStack stack) {
        java.util.UUID generated = java.util.UUID.randomUUID();
        StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != FoodWeaponLifecycle.class || !caller.getMethodName().equals("ensureTracked")
            || !caller.getDescriptor().equals("(Lnet/minecraft/world/item/ItemStack;)V")) return generated;
        NativeWear transition = NATIVE_WEAR.get();
        if (transition == null || !transition.initialization || transition.stack != stack) return generated;
        if (!transition.identityRequired || transition.identity != null || transition.thread != Thread.currentThread()) transition.rejected = true;
        transition.identity = generated;
        return generated;
    }

    static void nativeLifecycleReturned(NativeWear transition, ItemStack stack, ServerPlayer player, boolean success) {
        if (!nativeWearCaller("beginNative") || transition == null || NATIVE_WEAR.get() != transition) return;
        try {
            if (!transition.initialization || transition.receipt != player || !success
                || transition.identityRequired != (transition.identity != null)) return;
            if (transition.identityRequired) transition.expected.getCompound("components").getCompound("minecraft:custom_data")
                .getCompound("FTBossWeaponLifecycle").putUUID("Weapon", transition.identity);
            nativeTransitionReturned(transition, stack, 1, true);
        } finally {
            transition.input.retired = true;
            NATIVE_WEAR.remove();
        }
    }

    static NativeWear nativeWearBefore(Object receipt, ItemStack stack, CompoundTag before, int amount) {
        if (!nativeWearCaller("nativeWear") || amount != 1) return null;
        NativeWear transition = nativeTransition(receipt, stack, false);
        if (transition != null && !FoodWeaponExpectedTrace.same(transition.input.expected, NativeExpectedRawFields.observe(before))) {
            transition.rejected = true;
        }
        return transition;
    }

    static void nativeWearReturned(NativeWear transition, ItemStack stack, boolean success, int writes, int requested) {
        if (!nativeWearCaller("nativeWear") || transition == null || NATIVE_WEAR.get() != transition) return;
        try {
            if (transition.initialization || !success || writes < 0 || writes > 1
                || writes == 1 && (requested < transition.damageBefore || requested - transition.damageBefore > 1)) return;
            CompoundTag components = transition.expected.getCompound("components");
            if (writes == 1) {
                if (transition.physical.nativeDamageOverride(requested)) components.putInt("minecraft:damage", requested);
                else components.remove("minecraft:damage");
            }
            nativeTransitionReturned(transition, stack, writes, false);
        } finally {
            transition.input.retired = true;
            NATIVE_WEAR.remove();
        }
    }

    private static void nativeTransitionReturned(NativeWear transition, ItemStack stack, int writes, boolean customData) {
        try {
            WeaponInput input = transition.input;
            if (transition.returned || transition.rejected || transition.stack != stack || transition.thread != Thread.currentThread()
                || input.retired || input.thread != Thread.currentThread()
                || !((Object)stack instanceof OwnerNativeWeaponInputFields fields) || fields.ftboss$ownerNativeWeaponInput() != input
                || input.actual == null || input.actual.get() != stack
                || OwnerFullRepairGuard.generation(stack) != transition.guardGeneration || OwnerFullRepairGuard.lease(stack) != transition.lease) return;
            transition.returned = true;
            Value expectedAfter = NativeExpectedRawFields.observe(transition.expected);
            NativeItemComponentObservation after = NativeItemComponentObservation.capture(stack, input.registries);
            if (!transition.physical.nativeWearSame(after, writes, customData)
                || !FoodWeaponExpectedTrace.same(expectedAfter, NativeExpectedRawFields.observe(NativeFoundationItemData.image(stack, input.registries)))
                || !after.matches(stack) || input.retired) return;
            WeaponInput successor = new WeaponInput(input.registries, input.originalInput.get(), expectedAfter);
            successor.actual = new WeakReference<>(stack);
            successor.root = (CompoundTag)after.nativeWearStorage(true);
            successor.components = after.nativeWearStorage(false);
            input.retired = true;
            if (!fields.ftboss$ownerAdvanceNativeWeaponInput(input, successor)) successor.retired = true;
        } catch (LinkageError | RuntimeException refused) {
            transition.rejected = true;
        }
    }

    public static final class NativeWear {
        private final Object receipt;
        private final ItemStack stack;
        private final WeaponInput input;
        private final NativeItemComponentObservation physical;
        private final Thread thread = Thread.currentThread();
        private final Object guardGeneration;
        private final Object lease;
        private final boolean initialization;
        private final CompoundTag expected;
        private final int damageBefore;
        private boolean identityRequired;
        private java.util.UUID identity;
        private boolean rejected;
        private boolean returned;

        private NativeWear(Object receipt, ItemStack stack, WeaponInput input, NativeItemComponentObservation physical, boolean initialization, CompoundTag expected, int damageBefore) {
            this.receipt = receipt;
            this.stack = stack;
            this.input = input;
            this.physical = physical;
            this.initialization = initialization;
            this.expected = expected;
            this.damageBefore = damageBefore;
            this.guardGeneration = OwnerFullRepairGuard.generation(stack);
            this.lease = OwnerFullRepairGuard.lease(stack);
        }
    }

}
