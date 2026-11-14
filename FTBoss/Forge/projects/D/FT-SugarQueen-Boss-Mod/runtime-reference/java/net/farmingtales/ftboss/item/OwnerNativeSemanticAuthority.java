/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.LongTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.storage.PlayerDataStorage
 *  net.minecraftforge.common.capabilities.CapabilityProvider
 *  org.spongepowered.asm.mixin.transformer.meta.MixinMerged
 */
package net.farmingtales.ftboss.item;

import java.lang.ref.WeakReference;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace;
import net.farmingtales.ftboss.item.FoodWeaponFinalDamage;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan;
import net.farmingtales.ftboss.item.FoodWeaponOriginalSequence;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.farmingtales.ftboss.item.NativeExpectedRawFields;
import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.OwnerLineageRevocationStore;
import net.farmingtales.ftboss.item.OwnerNativePutQuoteFields;
import net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields;
import net.farmingtales.ftboss.item.OwnerRepairCapabilityFields;
import net.farmingtales.ftboss.item.OwnerRepairGenerationAuthority;
import net.farmingtales.ftboss.item.OwnerStatueWearAdmission;
import net.farmingtales.ftboss.item.OwnerTimerSemanticAuthority;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public final class OwnerNativeSemanticAuthority {
    private static final ThreadLocal<ReadFrame> RAW_READ = new ThreadLocal();
    private static final ThreadLocal<TargetWriteCall> TARGET_WRITE = new ThreadLocal();
    private static final ThreadLocal<TargetReceiver> TARGET_RECEIVER = new ThreadLocal();

    private static boolean readLive(ReadContext readContext) {
        OwnerNativeWeaponInputFields ownerNativeWeaponInputFields;
        ItemStack itemStack;
        if (readContext != null && readContext.wearReading != null) {
            OwnerNativeWeaponInputFields ownerNativeWeaponInputFields2;
            ItemStack itemStack2;
            return !readContext.rejected && readContext.thread == Thread.currentThread() && readContext.preparation == null && readContext.before == null && readContext.sequence == null && readContext.step == null && readContext.binding == null && !readContext.loading && readContext.input.generation == readContext.inputGeneration && readContext.input.actual != null && readContext.input.actual.get() == readContext.actual && readContext.input.wearOrigin != null && readContext.wearReading.live(readContext.input, readContext.actual) && (!readContext.wearReading.before() || !readContext.input.retired) && OwnerFullRepairGuard.generation(readContext.actual) == readContext.guardGeneration && OwnerFullRepairGuard.lease(readContext.actual) == readContext.lease && (itemStack2 = readContext.actual) instanceof OwnerNativeWeaponInputFields && (ownerNativeWeaponInputFields2 = (OwnerNativeWeaponInputFields)itemStack2).ftboss$ownerNativeWeaponInput() == readContext.input;
        }
        if (readContext == null || readContext.rejected || readContext.thread != Thread.currentThread() || readContext.input.retired || readContext.input.thread != Thread.currentThread() || readContext.input.generation != readContext.inputGeneration || OwnerFullRepairGuard.generation(readContext.actual) != readContext.guardGeneration || OwnerFullRepairGuard.lease(readContext.actual) != readContext.lease) {
            return false;
        }
        if (readContext.loading) {
            return readContext.preparation == null && readContext.before == null && readContext.sequence == null && readContext.input.actual == null;
        }
        if (readContext.input.actual == null || readContext.input.actual.get() != readContext.actual || !((itemStack = readContext.actual) instanceof OwnerNativeWeaponInputFields) || (ownerNativeWeaponInputFields = (OwnerNativeWeaponInputFields)itemStack).ftboss$ownerNativeWeaponInput() != readContext.input) {
            return false;
        }
        if (!OwnerFieldExpectedAuthority.nativeValidationLive(readContext.preparation, readContext.before, readContext.sequence)) {
            return false;
        }
        return readContext.sequence == null || readContext.step != null && readContext.step.session == readContext.sequence && readContext.binding != null && readContext.binding.before == readContext.before && OwnerNativeSemanticAuthority.matchesSemanticStep(readContext.binding, readContext.sequence, readContext.step) && readContext.sequence.lineage == readContext.preparation.lineage && readContext.sequence.original.atLeaf(readContext.leaf);
    }

    private static void rejectRead(ReadContext readContext, Throwable throwable) {
        block6: {
            if (readContext == null || readContext.thread != Thread.currentThread()) {
                return;
            }
            readContext.rejected = true;
            if (readContext.observationFailure == null) {
                readContext.observationFailure = throwable;
            }
            if (readContext.loading) {
                readContext.input.retired = true;
            } else {
                try {
                    OwnerFieldExpectedAuthority.nativeValidationFailed(readContext.preparation, readContext.before);
                }
                catch (Throwable throwable2) {
                    if (readContext.observationFailure != null) break block6;
                    readContext.observationFailure = throwable2;
                }
            }
        }
    }

    private static void finishRead(ReadFrame readFrame) {
        readFrame.closed = true;
        if (RAW_READ.get() != readFrame) {
            return;
        }
        ReadFrame readFrame2 = readFrame.previous;
        while (readFrame2 != null && readFrame2.closed) {
            readFrame2 = readFrame2.previous;
        }
        if (readFrame2 == null) {
            RAW_READ.remove();
        } else {
            RAW_READ.set(readFrame2);
        }
    }

    private static Object read(ReadContext readContext, Object object, RawField rawField) {
        if (!OwnerNativeSemanticAuthority.readLive(readContext) || object == null || (rawField == RawField.COMPOUND ? !readContext.compounds.containsKey(object) : (rawField == RawField.PERSISTENT ? readContext.before == null || object != readContext.before.player && object != readContext.before.carrier : object != readContext.actual))) {
            throw new IllegalStateException("Saved native read context is not live");
        }
        ReadFrame readFrame = new ReadFrame(readContext, object, rawField, RAW_READ.get());
        RAW_READ.set(readFrame);
        try {
            Object object2;
            switch (rawField) {
                default: {
                    throw new IncompatibleClassChangeError();
                }
                case STACK_TAG: {
                    Object object3 = ((NativeExpectedFieldViews.Stack)object).ftboss$rawStackTag();
                    break;
                }
                case STACK_CAPS: {
                    Object object3 = ((NativeExpectedFieldViews.Stack)object).ftboss$rawStackCaps();
                    break;
                }
                case DISPATCHER: {
                    Object object3 = ((NativeExpectedFieldViews.Provider)object).ftboss$rawDispatcher();
                    break;
                }
                case PARENT: {
                    Object object3 = ((NativeExpectedFieldViews.Provider)object).ftboss$rawCapsParentSupplier();
                    break;
                }
                case LAZY_DATA: {
                    Object object3 = ((NativeExpectedFieldViews.Provider)object).ftboss$rawCapsLazyData();
                    break;
                }
                case PERSISTENT: {
                    Object object3 = ((NativeExpectedFieldViews.Actor)object).ftboss$rawPersistentData();
                    break;
                }
                case COMPOUND: {
                    Object object3 = object2 = ((NativeExpectedFieldViews.Compound)object).ftboss$rawCompoundFields();
                }
            }
            if (!(readFrame.observed && readFrame.accepted && readFrame.returned == object2 && OwnerNativeSemanticAuthority.readLive(readContext))) {
                throw new IllegalStateException("Native raw read was not authenticated");
            }
            Object object4 = object2;
            return object4;
        }
        catch (Throwable throwable) {
            OwnerNativeSemanticAuthority.rejectRead(readContext, throwable);
            throw throwable;
        }
        finally {
            OwnerNativeSemanticAuthority.finishRead(readFrame);
        }
    }

    static int rawReadReturned(Object object, Object object2, Class<?> clazz, String string, String string2, Class<?> clazz2, String string3, String string4) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerFieldExpectedAuthority.class && stackFrame.getMethodName().equals("rawExposed")).orElse(false));
        ReadFrame readFrame = RAW_READ.get();
        if (!bl || readFrame == null || readFrame.closed || readFrame.receiver != object || clazz2 != OwnerNativeSemanticAuthority.class || !"read".equals(string3) || !"(Lnet/farmingtales/ftboss/item/OwnerNativeSemanticAuthority$ReadContext;Ljava/lang/Object;Lnet/farmingtales/ftboss/item/OwnerNativeSemanticAuthority$RawField;)Ljava/lang/Object;".equals(string4)) {
            return 0;
        }
        try {
            if (readFrame.observed || clazz != readFrame.field.owner || !readFrame.field.member.equals(string) || !readFrame.field.descriptor.equals(string2) || !OwnerNativeSemanticAuthority.readLive(readFrame.context)) {
                readFrame.observed = true;
                readFrame.accepted = false;
                OwnerNativeSemanticAuthority.rejectRead(readFrame.context, null);
                return 2;
            }
            readFrame.observed = true;
            readFrame.returned = object2;
            readFrame.accepted = true;
            return 1;
        }
        catch (Throwable throwable) {
            readFrame.observed = true;
            readFrame.accepted = false;
            OwnerNativeSemanticAuthority.rejectRead(readFrame.context, throwable);
            return 2;
        }
    }

    private static Map<String, Tag> compound(ReadContext readContext, Object object) {
        Map map = (Map)OwnerNativeSemanticAuthority.read(readContext, object, RawField.COMPOUND);
        if (map == null || map.getClass() != HashMap.class || map.size() > 65536) {
            throw new IllegalArgumentException("Unsupported native validation map");
        }
        return map;
    }

    private static boolean targetWriterCaller(String string) {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerNativeSemanticAuthority.class).findFirst().map(stackFrame -> {
            String string2;
            switch (string) {
                case "ftboss$ownerTargetWrite": {
                    String string3 = "(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain;Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/nbt/CompoundTag;JLnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)V";
                    break;
                }
                case "ftboss$ownerTargetReceiver": {
                    String string3 = "(Lnet/minecraft/world/entity/LivingEntity;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/nbt/CompoundTag;JLnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)Lnet/minecraft/nbt/CompoundTag;";
                    break;
                }
                default: {
                    String string3 = string2 = null;
                }
            }
            if (string2 == null) {
                return false;
            }
            if (stackFrame.getDeclaringClass() != FoodWeaponEvolution.class || !stackFrame.getDescriptor().equals(string2) || !stackFrame.getMethodName().matches("wrapOperation\\$(?:zz[a-p]|z[b-p][a-p]|[b-p][a-p]{2,6}|[b-h][a-p]{7})(?:[0-9a-f]{3}|[1-9a-f][0-9a-f]{3,7})\\$" + Pattern.quote(string))) {
                return false;
            }
            try {
                Object object = FoodWeaponEvolution.class.getDeclaredMethod(stackFrame.getMethodName(), stackFrame.getMethodType().parameterArray());
                MixinMerged mixinMerged = ((AccessibleObject)object).getDeclaredAnnotation(MixinMerged.class);
                return mixinMerged != null && "net.farmingtales.ftboss.mixin.OwnerTimerLocalOriginMixin".equals(mixinMerged.mixin());
            }
            catch (LinkageError | ReflectiveOperationException | SecurityException throwable) {
                return false;
            }
        }).orElse(false));
    }

    public static void targetReceiverReturned(FoodWeaponHitPlan.Builder builder, LivingEntity livingEntity, LivingEntity livingEntity2, CompoundTag compoundTag) {
        if (!OwnerNativeSemanticAuthority.targetWriterCaller("ftboss$ownerTargetReceiver") || builder == null || livingEntity == null || livingEntity != livingEntity2 || compoundTag == null) {
            return;
        }
        TARGET_RECEIVER.set(new TargetReceiver(builder, livingEntity, compoundTag, Thread.currentThread()));
    }

    public static TargetWriteCall targetWriteBegins(FoodWeaponHitPlan.Builder builder, FoodWeaponHitPlan.Builder builder2, ServerPlayer serverPlayer, LivingEntity livingEntity, ItemStack itemStack, CompoundTag compoundTag, CompoundTag compoundTag2, String string, FoodWeaponSourceLedger.AttemptDomain attemptDomain, Tag tag) {
        if (!OwnerNativeSemanticAuthority.targetWriterCaller("ftboss$ownerTargetWrite")) {
            return null;
        }
        TargetReceiver targetReceiver = TARGET_RECEIVER.get();
        TARGET_RECEIVER.remove();
        if (targetReceiver == null || targetReceiver.thread() != Thread.currentThread() || targetReceiver.builder() != builder || targetReceiver.target() != livingEntity || targetReceiver.receiver() != compoundTag2 || builder == null || builder != builder2 || serverPlayer == null || livingEntity == null || livingEntity == serverPlayer || itemStack == null || compoundTag2 == null || compoundTag2 == compoundTag || string == null || compoundTag2.getClass() != CompoundTag.class || tag != null && tag.getClass() != LongTag.class && tag.getClass() != IntTag.class) {
            return null;
        }
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.anyMatch(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponEvolution.class && stackFrame.getMethodName().equals("quoteFoodBlessingHits") && stackFrame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/nbt/CompoundTag;JLnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)V")));
        if (!bl) {
            return null;
        }
        TargetWriteCall targetWriteCall = new TargetWriteCall(builder, serverPlayer, livingEntity, itemStack, compoundTag2, string, attemptDomain, tag, TARGET_WRITE.get());
        TARGET_WRITE.set(targetWriteCall);
        return targetWriteCall;
    }

    public static void targetWriteEnds(TargetWriteCall targetWriteCall) {
        if (targetWriteCall == null || targetWriteCall.closed || targetWriteCall.thread != Thread.currentThread() || TARGET_WRITE.get() != targetWriteCall) {
            return;
        }
        targetWriteCall.closed = true;
        TargetWriteCall targetWriteCall2 = targetWriteCall.previous;
        while (targetWriteCall2 != null && targetWriteCall2.closed) {
            targetWriteCall2 = targetWriteCall2.previous;
        }
        if (targetWriteCall2 == null) {
            TARGET_WRITE.remove();
        } else {
            TARGET_WRITE.set(targetWriteCall2);
        }
    }

    static TargetWrite targetWriteConstructed(FoodWeaponHitPlan.Builder builder, Object object, FoodWeaponSourceLedger.AttemptDomain attemptDomain, CompoundTag compoundTag, String string, Tag tag, Tag tag2) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponHitPlan.Builder.class && stackFrame.getMethodName().equals("write") && stackFrame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain;Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V")).orElse(false));
        TargetWriteCall targetWriteCall = TARGET_WRITE.get();
        if (!bl || targetWriteCall == null || targetWriteCall.closed || targetWriteCall.consumed || targetWriteCall.thread != Thread.currentThread() || targetWriteCall.builder != builder || object == null || targetWriteCall.attempt != attemptDomain || targetWriteCall.receiver != compoundTag || !targetWriteCall.key.equals(string) || targetWriteCall.input != tag || tag == null && tag2 != null || tag != null && (tag2 == null || !FoodWeaponExpectedTrace.same(targetWriteCall.value, NativeExpectedRawFields.observe(tag2)))) {
            return null;
        }
        targetWriteCall.consumed = true;
        return new TargetWrite(targetWriteCall, object, tag2);
    }

    static TargetWrite reserveTarget(OwnerFieldExpectedAuthority.Prepared prepared, FoodWeaponHitPlan foodWeaponHitPlan, Object object, Object object2, Object object3) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerTimerSemanticAuthority.Book.class && stackFrame.getMethodName().equals("quote")).orElse(false));
        if (!bl || prepared == null || foodWeaponHitPlan == null || object == null) {
            return null;
        }
        TargetWrite targetWrite = FoodWeaponHitPlan.ownerTargetWrite(foodWeaponHitPlan, object);
        if (targetWrite == null || targetWrite.leaf != object || targetWrite.call.thread != Thread.currentThread() || !targetWrite.call.closed || !targetWrite.call.consumed || targetWrite.call.player != object2 || targetWrite.call.weapon != object3 || targetWrite.plan != null && targetWrite.plan != foodWeaponHitPlan) {
            return null;
        }
        Before before = prepared.nativeBefore;
        if (before != null && (targetWrite.call.target == before.player || targetWrite.call.target == before.carrier || targetWrite.call.receiver == before.weapon.root || targetWrite.call.receiver == before.playerRoot || targetWrite.call.receiver == before.carrierRoot)) {
            return null;
        }
        targetWrite.plan = foodWeaponHitPlan;
        return targetWrite;
    }

    static boolean targetPlan(TargetWrite targetWrite, FoodWeaponHitPlan foodWeaponHitPlan) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponHitPlan.class && stackFrame.getMethodName().equals("ownerTargetWriteAtOriginal")).orElse(false));
        return bl && targetWrite != null && targetWrite.plan == foodWeaponHitPlan;
    }

    private static boolean targetLive(TargetObservation targetObservation) {
        return targetObservation != null && !targetObservation.closed && !targetObservation.failed && targetObservation.thread == Thread.currentThread() && targetObservation.write.call.thread == Thread.currentThread() && targetObservation.write.call.closed && targetObservation.write.call.consumed && targetObservation.session.timerSemantics == targetObservation.book && targetObservation.book.targetMatches(targetObservation.session, targetObservation.write.leaf, targetObservation.write) && targetObservation.write.plan != null && FoodWeaponHitPlan.ownerTargetWriteAtOriginal(targetObservation.commit, targetObservation.bridge, targetObservation.write.leaf) == targetObservation.write && FoodWeaponFinalDamage.originalHitPlanSequence(targetObservation.commit, targetObservation.bridge) == targetObservation.session;
    }

    static TargetObservation targetEntered(FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge, Object object, TargetWrite targetWrite, CompoundTag compoundTag, String string, Tag tag) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass().getNestHost() == FoodWeaponHitPlan.class && stackFrame.getDeclaringClass().getName().equals("net.farmingtales.ftboss.item.FoodWeaponHitPlan$TagLeaf") && stackFrame.getMethodName().equals("apply") && stackFrame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V")).orElse(false));
        if (!bl || targetWrite == null || targetWrite.leaf != object || targetWrite.call.receiver != compoundTag || !targetWrite.call.key.equals(string) || targetWrite.value != tag) {
            return null;
        }
        FoodWeaponOriginalSequence.Session session = FoodWeaponFinalDamage.originalHitPlanSequence(finalEffectsCommit, childHurtBridge);
        if (session == null || session.timerSemantics == null) {
            return null;
        }
        TargetObservation targetObservation = new TargetObservation(session.timerSemantics, session, finalEffectsCommit, childHurtBridge, targetWrite);
        return OwnerNativeSemanticAuthority.targetLive(targetObservation) ? targetObservation : null;
    }

    static void targetCopied(TargetObservation targetObservation, Tag tag, Tag tag2) {
        if (targetObservation == null) {
            return;
        }
        if (!OwnerNativeSemanticAuthority.targetLive(targetObservation) || targetObservation.copied || targetObservation.write.value == null || tag != targetObservation.write.value || tag2 == null || !FoodWeaponExpectedTrace.same(targetObservation.write.call.value, NativeExpectedRawFields.observe(tag2))) {
            OwnerNativeSemanticAuthority.targetFailed(targetObservation, null);
            return;
        }
        targetObservation.argument = tag2;
        targetObservation.copied = true;
    }

    static void targetRemoved(TargetObservation targetObservation, CompoundTag compoundTag, String string) {
        if (targetObservation == null) {
            return;
        }
        if (!OwnerNativeSemanticAuthority.targetLive(targetObservation) || targetObservation.returned || targetObservation.write.value != null || targetObservation.copied || targetObservation.write.call.receiver != compoundTag || !targetObservation.write.call.key.equals(string)) {
            OwnerNativeSemanticAuthority.targetFailed(targetObservation, null);
            return;
        }
        targetObservation.removed = true;
        targetObservation.returned = true;
    }

    static void targetReturned(TargetObservation targetObservation, CompoundTag compoundTag, String string, Tag tag, Tag tag2) {
        if (targetObservation == null) {
            return;
        }
        if (!OwnerNativeSemanticAuthority.targetLive(targetObservation) || targetObservation.returned || targetObservation.write.call.receiver != compoundTag || !targetObservation.write.call.key.equals(string) || targetObservation.write.value == null || !targetObservation.copied || targetObservation.argument != tag) {
            OwnerNativeSemanticAuthority.targetFailed(targetObservation, null);
            return;
        }
        targetObservation.originalReturn = tag2;
        targetObservation.returned = true;
    }

    static void targetFailed(TargetObservation targetObservation, Throwable throwable) {
        if (targetObservation == null || targetObservation.closed || targetObservation.thread != Thread.currentThread()) {
            return;
        }
        targetObservation.failed = true;
        targetObservation.session.observationFailed(null, 3, throwable);
    }

    static void targetClosed(TargetObservation targetObservation) {
        if (targetObservation == null || targetObservation.closed || targetObservation.thread != Thread.currentThread()) {
            return;
        }
        try {
            if (!OwnerNativeSemanticAuthority.targetLive(targetObservation) || !targetObservation.returned || targetObservation.removed != (targetObservation.write.value == null)) {
                OwnerNativeSemanticAuthority.targetFailed(targetObservation, null);
                return;
            }
            targetObservation.book.targetReturned(targetObservation.session, targetObservation.write.leaf, targetObservation.write);
        }
        catch (Throwable throwable) {
            OwnerNativeSemanticAuthority.targetFailed(targetObservation, throwable);
        }
        finally {
            targetObservation.closed = true;
        }
    }

    private OwnerNativeSemanticAuthority() {
    }

    private static boolean stackHook() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != OwnerNativeSemanticAuthority.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == ItemStack.class && stackFrame.getMethodName().contains("ftboss$ownerNativeLoadedInput")).orElse(false));
    }

    private static boolean leafHook(String string) {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != OwnerNativeSemanticAuthority.class && stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass().getNestHost() == FoodWeaponHitPlan.class && (stackFrame.getDeclaringClass().getSimpleName().equals("TagLeaf") || stackFrame.getDeclaringClass().getSimpleName().equals("OutcomeTagLeaf")) && stackFrame.getMethodName().contains(string)).orElse(false));
    }

    public static WeaponInput weaponInputQuoted(CompoundTag compoundTag) {
        if (!OwnerNativeSemanticAuthority.stackHook() || compoundTag == null) {
            return null;
        }
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != OwnerNativeSemanticAuthority.class).limit(7L).toList();
            return list.size() == 7 && list.get(0).getDeclaringClass() == ItemStack.class && list.get(0).getMethodName().contains("ftboss$ownerNativeLoadedInput") && list.get(1).getDeclaringClass() == ItemStack.class && list.get(1).getMethodName().equals("of") && list.get(2).getDeclaringClass() == Inventory.class && list.get(2).getMethodName().equals("load") && list.get(3).getDeclaringClass() == Player.class && list.get(3).getMethodName().equals("readAdditionalSaveData") && list.get(4).getDeclaringClass() == ServerPlayer.class && list.get(4).getMethodName().equals("readAdditionalSaveData") && list.get(5).getDeclaringClass() == Entity.class && list.get(5).getMethodName().equals("load") && list.get(6).getDeclaringClass() == PlayerDataStorage.class && list.get(6).getMethodName().equals("load");
        });
        if (!bl) {
            return null;
        }
        FoodWeaponExpectedTrace.Value value = NativeExpectedRawFields.observe((Tag)compoundTag);
        FoodWeaponExpectedTrace.Value value2 = FoodWeaponExpectedTrace.field(value, "id");
        FoodWeaponExpectedTrace.Value value3 = FoodWeaponExpectedTrace.field(value, "Count");
        FoodWeaponExpectedTrace.Value value4 = FoodWeaponExpectedTrace.field(value, "tag");
        if (value2 == null || value2.kind != 8 || value3 == null || value3.kind != 1 || value3.bits != 1L || FoodWeaponExpectedTrace.field(value, "ForgeCaps") != null || value4 != null && value4.kind != 10) {
            return null;
        }
        FoodWeaponExpectedTrace.Value value5 = value4 == null ? FoodWeaponExpectedTrace.compound(new String[]{"Count", "id"}, new FoodWeaponExpectedTrace.Value[]{value3, value2}) : FoodWeaponExpectedTrace.compound(new String[]{"Count", "id", "tag"}, new FoodWeaponExpectedTrace.Value[]{value3, value2, value4});
        return new WeaponInput(compoundTag, value5);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static boolean knownProvider(ReadContext readContext) {
        ItemStack itemStack = readContext.actual;
        if (!(itemStack instanceof NativeExpectedFieldViews.Provider)) return false;
        NativeExpectedFieldViews.Provider provider = (NativeExpectedFieldViews.Provider)itemStack;
        itemStack = readContext.actual;
        if (!(itemStack instanceof OwnerRepairCapabilityFields)) return false;
        OwnerRepairCapabilityFields ownerRepairCapabilityFields = (OwnerRepairCapabilityFields)itemStack;
        if (OwnerNativeSemanticAuthority.read(readContext, readContext.actual, RawField.PARENT) != null) return false;
        if (OwnerNativeSemanticAuthority.read(readContext, readContext.actual, RawField.LAZY_DATA) != null) return false;
        if (provider.ftboss$rawCapsLazy()) return false;
        if (!provider.ftboss$rawCapsInitialized()) return false;
        if (!provider.ftboss$rawCapsValid()) return false;
        if (!ownerRepairCapabilityFields.ftboss$ownerRepairInitializedNoProviders()) return false;
        return true;
    }

    public static void weaponInputLoaded(WeaponInput weaponInput, ItemStack itemStack) {
        OwnerNativeWeaponInputFields ownerNativeWeaponInputFields;
        Object object;
        block11: {
            block10: {
                if (!OwnerNativeSemanticAuthority.stackHook() || weaponInput == null || weaponInput.retired || weaponInput.actual != null || weaponInput.thread != Thread.currentThread() || itemStack == null || !((object = itemStack) instanceof OwnerNativeWeaponInputFields)) break block10;
                ownerNativeWeaponInputFields = (OwnerNativeWeaponInputFields)object;
                if (itemStack instanceof NativeExpectedFieldViews.Stack) break block11;
            }
            return;
        }
        object = new ReadContext(weaponInput, itemStack, null, null, null, null, null, null, true);
        try {
            if (!OwnerNativeSemanticAuthority.knownProvider((ReadContext)object) || OwnerNativeSemanticAuthority.read((ReadContext)object, itemStack, RawField.STACK_CAPS) != null || !OwnerNativeSemanticAuthority.matchesWeapon((ReadContext)object, weaponInput.expected)) {
                return;
            }
            CompoundTag compoundTag = (CompoundTag)OwnerNativeSemanticAuthority.read((ReadContext)object, itemStack, RawField.STACK_TAG);
            Object object2 = OwnerNativeSemanticAuthority.read((ReadContext)object, itemStack, RawField.DISPATCHER);
            if (!OwnerNativeSemanticAuthority.readLive((ReadContext)object)) {
                OwnerNativeSemanticAuthority.rejectRead((ReadContext)object, null);
                return;
            }
            weaponInput.actual = new WeakReference<ItemStack>(itemStack);
            weaponInput.root = compoundTag;
            weaponInput.dispatcher = object2;
            ownerNativeWeaponInputFields.ftboss$ownerInstallNativeWeaponInput(weaponInput);
            if (ownerNativeWeaponInputFields.ftboss$ownerNativeWeaponInput() != weaponInput) {
                weaponInput.retired = true;
            }
            if (weaponInput.root != null) {
                OwnerFullRepairGuard.alias(itemStack, weaponInput.root, false);
            }
        }
        catch (Throwable throwable) {
            OwnerNativeSemanticAuthority.rejectRead((ReadContext)object, throwable);
        }
    }

    private static boolean matchesWeapon(ReadContext readContext, FoodWeaponExpectedTrace.Value value) {
        Object object = readContext.actual;
        if (!(object instanceof NativeExpectedFieldViews.Stack)) {
            return false;
        }
        NativeExpectedFieldViews.Stack stack = (NativeExpectedFieldViews.Stack)object;
        object = FoodWeaponExpectedTrace.field(value, "Count");
        FoodWeaponExpectedTrace.Value value2 = FoodWeaponExpectedTrace.field(value, "id");
        return object != null && value2 != null && value2.kind == 8 && (long)stack.ftboss$rawStackCount() == object.bits && stack.ftboss$rawStackItem() != null && BuiltInRegistries.f_257033_.m_7981_((Object)stack.ftboss$rawStackItem()).toString().equals(value2.string) && OwnerNativeSemanticAuthority.read(readContext, readContext.actual, RawField.STACK_CAPS) == null && FoodWeaponExpectedTrace.same(FoodWeaponExpectedTrace.field(value, "tag"), NativeExpectedRawFields.observe((Tag)OwnerNativeSemanticAuthority.read(readContext, readContext.actual, RawField.STACK_TAG)));
    }

    private static ReadContext weapon(OwnerFieldExpectedAuthority.Prepared prepared, Object object) {
        ItemStack itemStack;
        block7: {
            block6: {
                if (!(object instanceof ItemStack)) break block6;
                itemStack = (ItemStack)object;
                if (object instanceof OwnerNativeWeaponInputFields) break block7;
            }
            return null;
        }
        OwnerNativeWeaponInputFields ownerNativeWeaponInputFields = (OwnerNativeWeaponInputFields)object;
        WeaponInput weaponInput = ownerNativeWeaponInputFields.ftboss$ownerNativeWeaponInput();
        if (weaponInput == null || weaponInput.retired || weaponInput.actual == null || weaponInput.actual.get() != object) {
            return null;
        }
        ReadContext readContext = new ReadContext(weaponInput, itemStack, prepared, null, null, null, null, null, false);
        try {
            return OwnerNativeSemanticAuthority.readLive(readContext) && weaponInput.root == OwnerNativeSemanticAuthority.read(readContext, object, RawField.STACK_TAG) && OwnerNativeSemanticAuthority.knownProvider(readContext) && weaponInput.dispatcher == OwnerNativeSemanticAuthority.read(readContext, object, RawField.DISPATCHER) && OwnerNativeSemanticAuthority.matchesWeapon(readContext, weaponInput.expected) && OwnerNativeSemanticAuthority.readLive(readContext) ? readContext : null;
        }
        catch (Throwable throwable) {
            OwnerNativeSemanticAuthority.rejectRead(readContext, throwable);
            return null;
        }
    }

    public static void wearInputLoaded(OwnerStatueWearAdmission.Load load, CompoundTag compoundTag, ItemStack itemStack) {
        Object object;
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerStatueWearAdmission.class && stackFrame.getMethodName().equals("loadBound")).orElse(false));
        if (!(bl && load != null && itemStack != null && OwnerRepairGenerationAuthority.statueTaskObjectPolicy(itemStack) && (object = itemStack) instanceof OwnerNativeWeaponInputFields)) {
            return;
        }
        OwnerNativeWeaponInputFields ownerNativeWeaponInputFields = (OwnerNativeWeaponInputFields)object;
        object = ownerNativeWeaponInputFields.ftboss$ownerNativeWeaponInput();
        if (object == null || object.retired || object.thread != Thread.currentThread() || object.actual == null || object.actual.get() != itemStack || object.originalInput.get() != compoundTag || object.wearOrigin != null) {
            return;
        }
        OwnerStatueWearAdmission.Origin origin = OwnerStatueWearAdmission.quotedOrigin(load, compoundTag, itemStack, (WeaponInput)object, object.expected);
        if (origin != null && OwnerFullRepairGuard.watchLoadedWear(itemStack)) {
            object.wearOrigin = origin;
        }
    }

    static OwnerStatueWearAdmission.Origin wearOrigin(ItemStack itemStack) {
        Object object;
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerStatueWearAdmission.class && (stackFrame.getMethodName().equals("entered") || stackFrame.getMethodName().equals("activeRoot"))).orElse(false));
        if (!bl || !((object = itemStack) instanceof OwnerNativeWeaponInputFields)) {
            return null;
        }
        OwnerNativeWeaponInputFields ownerNativeWeaponInputFields = (OwnerNativeWeaponInputFields)object;
        object = ownerNativeWeaponInputFields.ftboss$ownerNativeWeaponInput();
        return object == null || object.actual == null || object.actual.get() != itemStack ? null : object.wearOrigin;
    }

    static CompoundTag wearImage(OwnerStatueWearAdmission.Origin origin, ItemStack itemStack, OwnerStatueWearAdmission.Reading reading) {
        Object object;
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerStatueWearAdmission.class).orElse(false));
        if (!bl || origin == null || reading == null || itemStack == null || itemStack.getClass() != ItemStack.class || !((object = itemStack) instanceof NativeExpectedFieldViews.Stack)) {
            return null;
        }
        NativeExpectedFieldViews.Stack stack = (NativeExpectedFieldViews.Stack)object;
        object = new ReadContext(origin.input, itemStack, null, null, null, null, null, null, false);
        object.wearReading = reading;
        try {
            FoodWeaponExpectedTrace.Value value;
            if (!OwnerNativeSemanticAuthority.readLive((ReadContext)object) || !OwnerNativeSemanticAuthority.knownProvider((ReadContext)object) || OwnerNativeSemanticAuthority.read((ReadContext)object, itemStack, RawField.STACK_CAPS) != null) {
                return null;
            }
            CompoundTag compoundTag = (CompoundTag)OwnerNativeSemanticAuthority.read((ReadContext)object, itemStack, RawField.STACK_TAG);
            Object object2 = OwnerNativeSemanticAuthority.read((ReadContext)object, itemStack, RawField.DISPATCHER);
            if (stack.ftboss$rawStackItem() == null) {
                return null;
            }
            FoodWeaponExpectedTrace.Value value2 = FoodWeaponExpectedTrace.number(1, stack.ftboss$rawStackCount());
            FoodWeaponExpectedTrace.Value value3 = FoodWeaponExpectedTrace.string(BuiltInRegistries.f_257033_.m_7981_((Object)stack.ftboss$rawStackItem()).toString());
            FoodWeaponExpectedTrace.Value value4 = NativeExpectedRawFields.observe((Tag)compoundTag);
            FoodWeaponExpectedTrace.Value value5 = value = value4 == null ? FoodWeaponExpectedTrace.compound(new String[]{"Count", "id"}, new FoodWeaponExpectedTrace.Value[]{value2, value3}) : FoodWeaponExpectedTrace.compound(new String[]{"Count", "id", "tag"}, new FoodWeaponExpectedTrace.Value[]{value2, value3, value4});
            if (reading.before() && (origin.input.root != compoundTag || origin.input.dispatcher != object2 || !FoodWeaponExpectedTrace.same(origin.expected, value))) {
                return null;
            }
            if (!OwnerNativeSemanticAuthority.readLive((ReadContext)object)) {
                return null;
            }
            CompoundTag compoundTag2 = (CompoundTag)FoodWeaponExpectedTrace.materialize(value);
            return FoodWeaponExpectedTrace.same(value, NativeExpectedRawFields.observe((Tag)compoundTag2)) && OwnerNativeSemanticAuthority.readLive((ReadContext)object) ? compoundTag2 : null;
        }
        catch (Throwable throwable) {
            OwnerNativeSemanticAuthority.rejectRead((ReadContext)object, throwable);
            return null;
        }
    }

    static void changed(Object object) {
        if (!(object instanceof OwnerNativeWeaponInputFields)) {
            return;
        }
        OwnerNativeWeaponInputFields ownerNativeWeaponInputFields = (OwnerNativeWeaponInputFields)object;
        WeaponInput weaponInput = ownerNativeWeaponInputFields.ftboss$ownerNativeWeaponInput();
        if (weaponInput != null && weaponInput.actual != null && weaponInput.actual.get() == object) {
            weaponInput.retired = true;
        }
    }

    static Before before(OwnerFieldExpectedAuthority.Prepared prepared, Object object, Entity entity, Entity entity2, Object object2, Object object3, Object object4, FoodWeaponExpectedTrace.Value value, FoodWeaponExpectedTrace.Value value2, FoodWeaponExpectedTrace.Value value3) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerFieldExpectedAuthority.Prepared.class && stackFrame.getMethodName().equals("<init>")).orElse(false));
        if (!bl || prepared == null || entity == null || object4 == null) {
            return null;
        }
        ReadContext readContext = OwnerNativeSemanticAuthority.weapon(prepared, object);
        if (readContext == null || !FoodWeaponExpectedTrace.same(readContext.input.expected, value)) {
            return null;
        }
        Before before = new Before(prepared, readContext, entity, entity2, object2, object3, object4, value2, value3);
        return OwnerNativeSemanticAuthority.readLive(readContext) ? before : null;
    }

    private static boolean matchesState(ReadContext readContext, FoodWeaponExpectedTrace.State state) {
        Before before = readContext.before;
        return before != null && before.weaponGeneration == readContext.inputGeneration && before.preparation == readContext.preparation && OwnerNativeSemanticAuthority.readLive(readContext) && OwnerNativeSemanticAuthority.knownProvider(readContext) && before.weapon.root == OwnerNativeSemanticAuthority.read(readContext, readContext.actual, RawField.STACK_TAG) && before.weapon.dispatcher == OwnerNativeSemanticAuthority.read(readContext, readContext.actual, RawField.DISPATCHER) && OwnerNativeSemanticAuthority.matchesWeapon(readContext, state.weapon) && OwnerNativeSemanticAuthority.read(readContext, before.player, RawField.PERSISTENT) == before.playerRoot && FoodWeaponExpectedTrace.same(state.player, NativeExpectedRawFields.observe((Tag)((CompoundTag)before.playerRoot))) && (before.carrier == null ? FoodWeaponExpectedTrace.same(state.carrier, FoodWeaponExpectedTrace.emptyCompound()) : OwnerNativeSemanticAuthority.read(readContext, before.carrier, RawField.PERSISTENT) == before.carrierRoot && FoodWeaponExpectedTrace.same(state.carrier, NativeExpectedRawFields.observe((Tag)((CompoundTag)before.carrierRoot)))) && OwnerNativeSemanticAuthority.readLive(readContext);
    }

    static FoodWeaponExpectedTrace.Value expectedWeapon(Before before) {
        return before == null ? null : before.expected.weapon;
    }

    public static PutQuote putQuoted(Object object, CompoundTag compoundTag, String string, Tag tag) {
        if (!OwnerNativeSemanticAuthority.leafHook("ftboss$ownerNativePutQuoted") || object == null || compoundTag == null || string == null || tag == null) {
            return null;
        }
        return new PutQuote(object, compoundTag, string, tag);
    }

    private static void locate(ReadContext readContext, Object object, FoodWeaponExpectedTrace.Value value, String[] stringArray, Object object2, FoodWeaponExpectedTrace.Domain domain, ArrayList<PutBinding> arrayList, PutQuote putQuote, Before before, IdentityHashMap<Object, Boolean> identityHashMap, int[] nArray) {
        if (stringArray.length > 64 || (nArray[0] = nArray[0] + 1) > 65536) {
            throw new IllegalArgumentException("Independent native receiver path budget");
        }
        if (!(object instanceof NativeExpectedFieldViews.Compound) || value == null || value.kind != 10) {
            return;
        }
        if (identityHashMap.put(object, Boolean.TRUE) != null) {
            throw new IllegalArgumentException("Native aliases or cycle are ambiguous");
        }
        if (object == object2) {
            arrayList.add(new PutBinding(putQuote, domain, stringArray, before, null));
        }
        for (Map.Entry<String, Tag> entry : OwnerNativeSemanticAuthority.compound(readContext, object).entrySet()) {
            FoodWeaponExpectedTrace.Value value2 = FoodWeaponExpectedTrace.field(value, entry.getKey());
            if (value2 == null || value2.kind != 10) continue;
            String[] stringArray2 = Arrays.copyOf(stringArray, stringArray.length + 1);
            stringArray2[stringArray.length] = entry.getKey();
            readContext.compounds.put(entry.getValue(), Boolean.TRUE);
            OwnerNativeSemanticAuthority.locate(readContext, entry.getValue(), value2, stringArray2, object2, domain, arrayList, putQuote, before, identityHashMap, nArray);
        }
    }

    static PutBinding appendPut(Before before, FoodWeaponExpectedTrace.Builder builder, Object object, PutQuote putQuote) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerFieldExpectedAuthority.class && stackFrame.getMethodName().equals("quoteTimers")).orElse(false));
        if (!bl || before == null || builder == null || putQuote == null || putQuote.leaf != object || putQuote.thread != Thread.currentThread() || before.weapon.actual == null || before.weapon.actual.get() == null) {
            return null;
        }
        ReadContext readContext = new ReadContext(before.weapon, (ItemStack)before.weapon.actual.get(), before.preparation, before, null, null, null, object, false);
        try {
            OwnerNativePutQuoteFields ownerNativePutQuoteFields;
            if (!(object instanceof OwnerNativePutQuoteFields && (ownerNativePutQuoteFields = (OwnerNativePutQuoteFields)object).ftboss$ownerNativePutQuote() == putQuote && FoodWeaponExpectedTrace.same(putQuote.argument, NativeExpectedRawFields.observe(ownerNativePutQuoteFields.ftboss$ownerNativeOriginalValue())) && OwnerNativeSemanticAuthority.matchesState(readContext, before.expected))) {
                return null;
            }
            ArrayList<PutBinding> arrayList = new ArrayList<PutBinding>();
            IdentityHashMap<Object, Boolean> identityHashMap = new IdentityHashMap<Object, Boolean>();
            int[] nArray = new int[]{0};
            OwnerNativeSemanticAuthority.locate(readContext, before.weapon.root, FoodWeaponExpectedTrace.field(before.expected.weapon, "tag"), new String[]{"tag"}, putQuote.receiver, FoodWeaponExpectedTrace.Domain.WEAPON, arrayList, putQuote, before, identityHashMap, nArray);
            OwnerNativeSemanticAuthority.locate(readContext, before.playerRoot, before.expected.player, new String[0], putQuote.receiver, FoodWeaponExpectedTrace.Domain.PLAYER, arrayList, putQuote, before, identityHashMap, nArray);
            OwnerNativeSemanticAuthority.locate(readContext, before.carrierRoot, before.expected.carrier, new String[0], putQuote.receiver, FoodWeaponExpectedTrace.Domain.CARRIER, arrayList, putQuote, before, identityHashMap, nArray);
            if (arrayList.size() != 1 || !OwnerNativeSemanticAuthority.readLive(readContext)) {
                return null;
            }
            PutBinding putBinding = arrayList.get(0);
            FoodWeaponExpectedTrace.Operation operation = builder.nativePut(putBinding.domain, putBinding.path, putQuote.key, putQuote.argument);
            return new PutBinding(putQuote, putBinding.domain, putBinding.path, before, operation);
        }
        catch (Throwable throwable) {
            OwnerNativeSemanticAuthority.rejectRead(readContext, throwable);
            return null;
        }
    }

    private static void rejectSaved(Before before, FoodWeaponOriginalSequence.Session session, FoodWeaponOriginalSequence.Step step, Throwable throwable, boolean bl) {
        if (before != null) {
            try {
                OwnerFieldExpectedAuthority.nativeValidationFailed(before.preparation, before);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
        }
        if (session == null || step == null || step.session != session) {
            return;
        }
        try {
            if (bl) {
                session.originalThrown(step, throwable);
            } else {
                session.observationFailed(step, 1, throwable);
            }
        }
        catch (Throwable throwable3) {
            // empty catch block
        }
        try {
            session.close(step);
        }
        catch (Throwable throwable4) {
            // empty catch block
        }
    }

    private static void rejectAdmission(Admission admission, Throwable throwable, boolean bl) {
        if (!OwnerNativeSemanticAuthority.active(admission)) {
            return;
        }
        admission.closed = true;
        OwnerNativeSemanticAuthority.rejectSaved(admission.binding.before, admission.sequence, admission.step, throwable, bl);
    }

    static Object semanticRangedBuffer(Before before) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerFieldExpectedAuthority.class && stackFrame.getMethodName().equals("quoteTimers")).orElse(false));
        if (!bl || before == null || before.weapon.actual == null || before.weapon.actual.get() == null) {
            return null;
        }
        ReadContext readContext = new ReadContext(before.weapon, (ItemStack)before.weapon.actual.get(), before.preparation, before, null, null, null, null, false);
        try {
            if (!OwnerNativeSemanticAuthority.matchesState(readContext, before.expected)) {
                return null;
            }
            Tag tag = OwnerNativeSemanticAuthority.compound(readContext, before.playerRoot).get("FTBossRangedProcTimers");
            FoodWeaponExpectedTrace.Value value = FoodWeaponExpectedTrace.field(before.expected.player, "FTBossRangedProcTimers");
            return OwnerNativeSemanticAuthority.readLive(readContext) && value != null && value.kind == 10 && tag instanceof CompoundTag ? tag : null;
        }
        catch (Throwable throwable) {
            OwnerNativeSemanticAuthority.rejectRead(readContext, throwable);
            return null;
        }
    }

    static FoodWeaponExpectedTrace.Operation semanticOperation(PutBinding putBinding, String string, FoodWeaponExpectedTrace.State state, FoodWeaponExpectedTrace.Builder builder) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerTimerSemanticAuthority.Book.class && stackFrame.getMethodName().equals("resolve")).orElse(false));
        if (!bl || putBinding == null || state == null || builder == null || !Objects.equals(string, putBinding.quote.key)) {
            return null;
        }
        return builder.nativePut(putBinding.domain, putBinding.path, string, putBinding.quote.argument);
    }

    private static boolean matchesSemanticStep(PutBinding putBinding, FoodWeaponOriginalSequence.Session session, FoodWeaponOriginalSequence.Step step) {
        return putBinding != null && step != null && step.session == session && (putBinding.operation == step.expected || session.semanticNativeBinding(step, putBinding));
    }

    static Admission admit(FoodWeaponOriginalSequence.Session session, PutBinding putBinding, Object object, Tag tag) {
        OwnerNativePutQuoteFields ownerNativePutQuoteFields;
        if (!OwnerNativeSemanticAuthority.leafHook("ftboss$ownerNativeCopy") || session == null || putBinding == null || putBinding.quote.leaf != object || !session.original.atLeaf(object) || putBinding.before.weapon.actual == null || putBinding.before.weapon.retired || session.lineage != putBinding.before.preparation.lineage || !(object instanceof OwnerNativePutQuoteFields) || (ownerNativePutQuoteFields = (OwnerNativePutQuoteFields)object).ftboss$ownerNativeOriginalValue() != tag) {
            return null;
        }
        FoodWeaponOriginalSequence.Step step = null;
        try {
            step = session.originalBegins(object, FoodWeaponExpectedTrace.Kind.NATIVE_PUT, putBinding.quote.key, null);
            if (step == null) {
                return null;
            }
            ItemStack itemStack = (ItemStack)putBinding.before.weapon.actual.get();
            ReadContext readContext = new ReadContext(putBinding.before.weapon, itemStack, putBinding.before.preparation, putBinding.before, session, step, putBinding, object, false);
            if (!OwnerNativeSemanticAuthority.matchesSemanticStep(putBinding, session, step) || !OwnerNativeSemanticAuthority.matchesState(readContext, step.expected.before)) {
                OwnerNativeSemanticAuthority.rejectSaved(putBinding.before, session, step, null, false);
                return null;
            }
            Admission admission = new Admission(session, step, putBinding, tag);
            session.originalCopyBegins(step, putBinding.quote.receiver, tag);
            return admission;
        }
        catch (Throwable throwable) {
            OwnerNativeSemanticAuthority.rejectSaved(putBinding.before, session, step, throwable, false);
            return null;
        }
    }

    public static void copied(Admission admission, Tag tag, Tag tag2) {
        if (!OwnerNativeSemanticAuthority.leafHook("ftboss$ownerNativeCopy") || !OwnerNativeSemanticAuthority.active(admission)) {
            return;
        }
        try {
            if (admission.copied || admission.originalValue != tag) {
                OwnerNativeSemanticAuthority.rejectAdmission(admission, null, false);
                return;
            }
            admission.argument = tag2;
            admission.sequence.originalCopyReturned(admission.step, admission.binding.quote.receiver, tag, tag2);
            admission.sequence.originalCopyFieldsObserved(admission.step, tag2, NativeExpectedRawFields.observe(tag2));
            admission.copied = true;
        }
        catch (Throwable throwable) {
            OwnerNativeSemanticAuthority.rejectAdmission(admission, throwable, false);
        }
    }

    public static void entering(Admission admission, CompoundTag compoundTag, String string, Tag tag) {
        if (!OwnerNativeSemanticAuthority.leafHook("ftboss$ownerNativePut") || !OwnerNativeSemanticAuthority.active(admission)) {
            return;
        }
        try {
            if (!admission.copied || admission.entered || compoundTag != admission.binding.quote.receiver || !admission.binding.quote.key.equals(string) || tag != admission.argument) {
                OwnerNativeSemanticAuthority.rejectAdmission(admission, null, false);
                return;
            }
            admission.sequence.entryReturned(admission.step, compoundTag, admission.originalValue, tag);
            admission.entered = true;
        }
        catch (Throwable throwable) {
            OwnerNativeSemanticAuthority.rejectAdmission(admission, throwable, false);
        }
    }

    public static void returned(Admission admission, CompoundTag compoundTag, String string, Tag tag, Tag tag2) {
        if (!OwnerNativeSemanticAuthority.leafHook("ftboss$ownerNativePut") || !OwnerNativeSemanticAuthority.active(admission)) {
            return;
        }
        try {
            if (!admission.entered || compoundTag != admission.binding.quote.receiver || !admission.binding.quote.key.equals(string) || tag != admission.argument) {
                OwnerNativeSemanticAuthority.rejectAdmission(admission, null, false);
                return;
            }
            Before before = admission.binding.before;
            ReadContext readContext = new ReadContext(before.weapon, (ItemStack)before.weapon.actual.get(), before.preparation, before, admission.sequence, admission.step, admission.binding, admission.binding.quote.leaf, false);
            if (OwnerNativeSemanticAuthority.compound(readContext, compoundTag).get(string) != tag) {
                OwnerNativeSemanticAuthority.rejectAdmission(admission, null, false);
                return;
            }
            admission.sequence.writeObserved(admission.step, compoundTag, admission.binding.domain, admission.binding.path, string, NativeExpectedRawFields.observe(tag));
            if (!OwnerNativeSemanticAuthority.matchesState(readContext, admission.step.expected.after)) {
                OwnerNativeSemanticAuthority.rejectAdmission(admission, null, false);
                return;
            }
            admission.sequence.originalReturned(admission.step, tag2);
        }
        catch (Throwable throwable) {
            OwnerNativeSemanticAuthority.rejectAdmission(admission, throwable, false);
        }
    }

    private static boolean active(Admission admission) {
        return admission != null && !admission.closed && admission.thread == Thread.currentThread();
    }

    public static void failed(Admission admission, Throwable throwable, boolean bl) {
        if (!OwnerNativeSemanticAuthority.leafHook("ftboss$ownerNative") || !OwnerNativeSemanticAuthority.active(admission)) {
            return;
        }
        OwnerNativeSemanticAuthority.rejectAdmission(admission, throwable, bl);
    }

    public static void close(Admission admission) {
        if (!OwnerNativeSemanticAuthority.leafHook("ftboss$ownerNative") || !OwnerNativeSemanticAuthority.active(admission)) {
            return;
        }
        admission.closed = true;
        try {
            admission.sequence.close(admission.step);
        }
        catch (Throwable throwable) {
            OwnerNativeSemanticAuthority.rejectSaved(admission.binding.before, admission.sequence, admission.step, throwable, false);
        }
    }

    private static final class ReadContext {
        private final WeaponInput input;
        private final Object inputGeneration;
        private final ItemStack actual;
        private final Object guardGeneration;
        private final OwnerLineageRevocationStore.Lease lease;
        private final OwnerFieldExpectedAuthority.Prepared preparation;
        private final Before before;
        private final FoodWeaponOriginalSequence.Session sequence;
        private final FoodWeaponOriginalSequence.Step step;
        private final PutBinding binding;
        private final Object leaf;
        private final boolean loading;
        private final Thread thread = Thread.currentThread();
        private final IdentityHashMap<Object, Boolean> compounds = new IdentityHashMap();
        private boolean rejected;
        private OwnerStatueWearAdmission.Reading wearReading;
        private Throwable observationFailure;

        private ReadContext(WeaponInput weaponInput, ItemStack itemStack, OwnerFieldExpectedAuthority.Prepared prepared, Before before, FoodWeaponOriginalSequence.Session session, FoodWeaponOriginalSequence.Step step, PutBinding putBinding, Object object, boolean bl) {
            this.input = weaponInput;
            this.inputGeneration = weaponInput.generation;
            this.actual = itemStack;
            this.guardGeneration = before == null ? OwnerFullRepairGuard.generation(itemStack) : before.guardGeneration;
            this.lease = before == null ? OwnerFullRepairGuard.lease(itemStack) : before.lease;
            this.preparation = prepared;
            this.before = before;
            this.sequence = session;
            this.step = step;
            this.binding = putBinding;
            this.leaf = object;
            this.loading = bl;
            if (before != null) {
                this.compounds.put(before.weapon.root, Boolean.TRUE);
                this.compounds.put(before.playerRoot, Boolean.TRUE);
                this.compounds.put(before.carrierRoot, Boolean.TRUE);
            }
            if (putBinding != null) {
                this.compounds.put(putBinding.quote.receiver, Boolean.TRUE);
            }
        }
    }

    static final class Before {
        private final OwnerFieldExpectedAuthority.Prepared preparation;
        private final WeaponInput weapon;
        private final Object weaponGeneration;
        private final Object guardGeneration;
        private final OwnerLineageRevocationStore.Lease lease;
        private final Entity player;
        private final Entity carrier;
        private final Object playerRoot;
        private final Object carrierRoot;
        private final Object source;
        private final FoodWeaponExpectedTrace.State expected;

        private Before(OwnerFieldExpectedAuthority.Prepared prepared, ReadContext readContext, Entity entity, Entity entity2, Object object, Object object2, Object object3, FoodWeaponExpectedTrace.Value value, FoodWeaponExpectedTrace.Value value2) {
            this.preparation = prepared;
            this.weapon = readContext.input;
            this.weaponGeneration = readContext.inputGeneration;
            this.guardGeneration = readContext.guardGeneration;
            this.lease = readContext.lease;
            this.player = entity;
            this.carrier = entity2;
            this.playerRoot = object;
            this.carrierRoot = object2;
            this.source = object3;
            this.expected = new FoodWeaponExpectedTrace.State(this.weapon.expected, value, value2);
        }
    }

    static final class PutBinding {
        final PutQuote quote;
        final FoodWeaponExpectedTrace.Domain domain;
        final String[] path;
        private final Before before;
        private final FoodWeaponExpectedTrace.Operation operation;

        private PutBinding(PutQuote putQuote, FoodWeaponExpectedTrace.Domain domain, String[] stringArray, Before before, FoodWeaponExpectedTrace.Operation operation) {
            this.quote = putQuote;
            this.domain = domain;
            this.path = (String[])stringArray.clone();
            this.before = before;
            this.operation = operation;
        }
    }

    public static final class WeaponInput {
        private final WeakReference<CompoundTag> originalInput;
        private final FoodWeaponExpectedTrace.Value expected;
        private final Thread thread = Thread.currentThread();
        private final Object generation = new Object();
        private WeakReference<ItemStack> actual;
        private CompoundTag root;
        private Object dispatcher;
        private OwnerStatueWearAdmission.Origin wearOrigin;
        private boolean retired;

        private WeaponInput(CompoundTag compoundTag, FoodWeaponExpectedTrace.Value value) {
            this.originalInput = new WeakReference<CompoundTag>(compoundTag);
            this.expected = value;
        }
    }

    private static final class ReadFrame {
        private final ReadContext context;
        private final Object receiver;
        private final RawField field;
        private final ReadFrame previous;
        private boolean observed;
        private Object returned;
        private boolean accepted;
        private boolean closed;

        private ReadFrame(ReadContext readContext, Object object, RawField rawField, ReadFrame readFrame) {
            this.context = readContext;
            this.receiver = object;
            this.field = rawField;
            this.previous = readFrame;
        }
    }

    private static enum RawField {
        STACK_TAG(ItemStack.class, "ftboss$rawStackTag", "()Lnet/minecraft/nbt/CompoundTag;"),
        STACK_CAPS(ItemStack.class, "ftboss$rawStackCaps", "()Lnet/minecraft/nbt/CompoundTag;"),
        DISPATCHER(CapabilityProvider.class, "ftboss$rawDispatcher", "()Ljava/lang/Object;"),
        PARENT(CapabilityProvider.class, "ftboss$rawCapsParentSupplier", "()Ljava/lang/Object;"),
        LAZY_DATA(CapabilityProvider.class, "ftboss$rawCapsLazyData", "()Lnet/minecraft/nbt/CompoundTag;"),
        PERSISTENT(Entity.class, "ftboss$rawPersistentData", "()Lnet/minecraft/nbt/CompoundTag;"),
        COMPOUND(CompoundTag.class, "ftboss$rawCompoundFields", "()Ljava/util/Map;");

        private final Class<?> owner;
        private final String member;
        private final String descriptor;

        private RawField(Class<?> clazz, String string2, String string3) {
            this.owner = clazz;
            this.member = string2;
            this.descriptor = string3;
        }
    }

    private record TargetReceiver(FoodWeaponHitPlan.Builder builder, LivingEntity target, CompoundTag receiver, Thread thread) {
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
        private final FoodWeaponExpectedTrace.Value value;
        private final Thread thread = Thread.currentThread();
        private final TargetWriteCall previous;
        private boolean consumed;
        private boolean closed;

        private TargetWriteCall(FoodWeaponHitPlan.Builder builder, ServerPlayer serverPlayer, LivingEntity livingEntity, ItemStack itemStack, CompoundTag compoundTag, String string, FoodWeaponSourceLedger.AttemptDomain attemptDomain, Tag tag, TargetWriteCall targetWriteCall) {
            this.builder = builder;
            this.player = serverPlayer;
            this.target = livingEntity;
            this.weapon = itemStack;
            this.receiver = compoundTag;
            this.key = string;
            this.attempt = attemptDomain;
            this.input = tag;
            this.value = tag == null ? null : NativeExpectedRawFields.observe(tag);
            this.previous = targetWriteCall;
        }
    }

    static final class TargetWrite {
        private final TargetWriteCall call;
        private final Object leaf;
        private final Tag value;
        private FoodWeaponHitPlan plan;

        private TargetWrite(TargetWriteCall targetWriteCall, Object object, Tag tag) {
            this.call = targetWriteCall;
            this.leaf = object;
            this.value = tag;
        }
    }

    static final class TargetObservation {
        private final OwnerTimerSemanticAuthority.Book book;
        private final FoodWeaponOriginalSequence.Session session;
        private final FoodWeaponHitPlan.FinalEffectsCommit commit;
        private final FoodWeaponFinalDamage.ChildHurtBridge bridge;
        private final TargetWrite write;
        private final Thread thread = Thread.currentThread();
        private Tag argument;
        private Tag originalReturn;
        private boolean copied;
        private boolean returned;
        private boolean removed;
        private boolean failed;
        private boolean closed;

        private TargetObservation(OwnerTimerSemanticAuthority.Book book, FoodWeaponOriginalSequence.Session session, FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge, TargetWrite targetWrite) {
            this.book = book;
            this.session = session;
            this.commit = finalEffectsCommit;
            this.bridge = childHurtBridge;
            this.write = targetWrite;
        }
    }

    public static final class PutQuote {
        private final Object leaf;
        private final CompoundTag receiver;
        private final String key;
        private final FoodWeaponExpectedTrace.Value argument;
        private final Thread thread = Thread.currentThread();

        private PutQuote(Object object, CompoundTag compoundTag, String string, Tag tag) {
            this.leaf = object;
            this.receiver = compoundTag;
            this.key = string;
            this.argument = NativeExpectedRawFields.observe(tag);
        }
    }

    public static final class Admission {
        private final FoodWeaponOriginalSequence.Session sequence;
        private final FoodWeaponOriginalSequence.Step step;
        private final PutBinding binding;
        private final Object originalValue;
        private final Thread thread = Thread.currentThread();
        private Object argument;
        private boolean copied;
        private boolean entered;
        private boolean closed;

        private Admission(FoodWeaponOriginalSequence.Session session, FoodWeaponOriginalSequence.Step step, PutBinding putBinding, Object object) {
            this.sequence = session;
            this.step = step;
            this.binding = putBinding;
            this.originalValue = object;
        }
    }
}

