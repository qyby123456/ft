/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.TamableAnimal
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace;
import net.farmingtales.ftboss.item.FoodWeaponFinalDamage;
import net.farmingtales.ftboss.item.FoodWeaponOriginalSequence;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeExpectedRawFields;
import net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.item.WeaponMaterialPowers;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public final class FoodWeaponHitPlan {
    private final float amount;
    private final boolean primaryPaymentRequired;
    private final List<Attempt> attempts;
    private final List<Target> targets;
    private final List<Commit> commits;
    private final List<TypedLeaf> typedLeaves;
    private final TimerSemanticCatalogue timerSemanticCatalogue;
    private boolean committed;

    private FoodWeaponHitPlan(float f, boolean bl, List<Attempt> list, List<Target> list2, List<Commit> list3, List<TypedLeaf> list4) {
        this.amount = f;
        this.primaryPaymentRequired = bl;
        this.attempts = list;
        this.targets = list2;
        this.commits = list3;
        this.typedLeaves = list4;
        this.timerSemanticCatalogue = FoodWeaponHitPlan.compileTimerSemantics(list4);
    }

    public float amount() {
        return this.amount;
    }

    public boolean primaryPaymentRequired() {
        return this.primaryPaymentRequired;
    }

    public List<Attempt> attempts() {
        return this.attempts;
    }

    public List<FoodWeaponSourceLedger.TargetRequest> targets() {
        return this.targets.stream().map(Target::request).toList();
    }

    public boolean matchesContext(ServerPlayer serverPlayer) {
        return serverPlayer != null && !this.committed && this.targets.stream().allMatch(target -> target.stable(serverPlayer));
    }

    private static TimerSemanticSeed timerSemanticSeed(CompoundTag compoundTag, CompoundTag compoundTag2) {
        try {
            FoodWeaponExpectedTrace.Value value = NativeExpectedRawFields.observe((Tag)compoundTag);
            FoodWeaponExpectedTrace.Value value2 = NativeExpectedRawFields.observe((Tag)compoundTag2);
            FoodWeaponExpectedTrace.compoundKeys(value);
            FoodWeaponExpectedTrace.compoundKeys(value2);
            return new TimerSemanticSeed(value, value2, null);
        }
        catch (Throwable throwable) {
            return new TimerSemanticSeed(null, null, throwable);
        }
    }

    /*
     * Unable to fully structure code
     */
    private static TimerSemanticCatalogue compileTimerSemantics(List<TypedLeaf> var0) {
        try {
            var1_1 = new ArrayList<TimerSemanticProgram>();
            for (var2_3 = 0; var2_3 < var0.size(); ++var2_3) {
                block9: {
                    var4_5 = var0.get(var2_3);
                    if (!(var4_5 instanceof TimerSaveLeaf)) continue;
                    var3_4 = (TimerSaveLeaf)var4_5;
                    var4_5 = new ArrayList<E>();
                    var5_6 = var3_4.semanticSeed.unsupported();
                    try {
                        for (var6_7 = 0; var6_7 < var2_3; ++var6_7) {
                            block11: {
                                block10: {
                                    var7_9 = var0.get(var6_7);
                                    var10_12 = false;
                                    if (!(var7_9 instanceof TagLeaf)) break block10;
                                    var12_14 = (TagLeaf)var7_9;
                                    if (var12_14.data != var3_4.data) break block10;
                                    var8_10 = var12_14.key;
                                    var9_11 = NativeExpectedRawFields.observe(var12_14.value);
                                    break block11;
                                }
                                if (!(var7_9 instanceof OutcomeTagLeaf)) ** GOTO lbl-1000
                                var13_15 = (OutcomeTagLeaf)var7_9;
                                if (var13_15.data == var3_4.data) {
                                    var8_10 = var13_15.key;
                                    var9_11 = NativeExpectedRawFields.observe(var13_15.value);
                                } else lbl-1000:
                                // 2 sources

                                {
                                    if (!(var7_9 instanceof ChainLeaf)) continue;
                                    var11_13 = (ChainLeaf)var7_9;
                                    if (var11_13.data != var3_4.data || var11_13.key == null) continue;
                                    var8_10 = var11_13.key;
                                    var9_11 = FoodWeaponExpectedTrace.number(4, var11_13.tick);
                                    var10_12 = true;
                                }
                            }
                            if (var8_10 == null) {
                                throw new IllegalArgumentException("Unsupported null timer semantic key");
                            }
                            var4_5.add(new TimerSemanticInstruction(var7_9, var6_7, var7_9.attempt, var8_10, var9_11, var10_12));
                        }
                    }
                    catch (Throwable var6_8) {
                        if (var5_6 != null) break block9;
                        var5_6 = var6_8;
                    }
                }
                var1_1.add(new TimerSemanticProgram(var3_4, var3_4.weapon, (Object)var3_4.profile, var3_4.player, var3_4.source, var3_4.semanticSeed.baseline(), var3_4.semanticSeed.data(), (List<TimerSemanticInstruction>)var4_5, var5_6));
            }
            return new TimerSemanticCatalogue(var1_1, null);
        }
        catch (Throwable var1_2) {
            return new TimerSemanticCatalogue(List.of(), var1_2);
        }
    }

    static OwnerNativeSemanticAuthority.TargetWrite ownerTargetWrite(FoodWeaponHitPlan foodWeaponHitPlan, Object object) {
        TagLeaf tagLeaf;
        block4: {
            block3: {
                boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerNativeSemanticAuthority.class && stackFrame.getMethodName().equals("reserveTarget")).orElse(false));
                if (!bl || foodWeaponHitPlan == null || !(object instanceof TagLeaf)) break block3;
                tagLeaf = (TagLeaf)object;
                if (tagLeaf.targetWrite != null) break block4;
            }
            return null;
        }
        int n = 0;
        for (TypedLeaf typedLeaf : foodWeaponHitPlan.typedLeaves) {
            if (typedLeaf != object) continue;
            ++n;
        }
        return n == 1 ? tagLeaf.targetWrite : null;
    }

    static OwnerNativeSemanticAuthority.TargetWrite ownerTargetWriteAtOriginal(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge, Object object) {
        TagLeaf tagLeaf;
        block3: {
            block2: {
                boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerNativeSemanticAuthority.class && stackFrame.getMethodName().equals("targetLive")).orElse(false));
                if (!bl || finalEffectsCommit == null || !(object instanceof TagLeaf)) break block2;
                tagLeaf = (TagLeaf)object;
                if (tagLeaf.targetWrite != null && finalEffectsCommit.currentLeaf == object && finalEffectsCommit.executed && !finalEffectsCommit.originalStopped && finalEffectsCommit.originalSequence != null && FoodWeaponFinalDamage.originalHitPlanSequence(finalEffectsCommit, childHurtBridge) == finalEffectsCommit.originalSequence && finalEffectsCommit.originalSequence.original.atLeaf(object) && finalEffectsCommit.leaves.contains(tagLeaf) && finalEffectsCommit.prepared.plan.typedLeaves.contains(tagLeaf) && OwnerNativeSemanticAuthority.targetPlan(tagLeaf.targetWrite, finalEffectsCommit.prepared.plan)) break block3;
            }
            return null;
        }
        return tagLeaf.targetWrite;
    }

    static TimerSemanticCatalogue timerSemanticCatalogue(FoodWeaponHitPlan foodWeaponHitPlan) {
        return foodWeaponHitPlan == null ? null : foodWeaponHitPlan.timerSemanticCatalogue;
    }

    static TimerSemanticProjection timerSemanticQuoteProjection(TimerSemanticProgram timerSemanticProgram, List<TimerSemanticChoice> list) {
        if (timerSemanticProgram == null || timerSemanticProgram.unsupported() != null || list == null || list.size() != timerSemanticProgram.instructions().size()) {
            throw new IllegalArgumentException("Expected complete quoted timer choices");
        }
        FoodWeaponExpectedTrace.Value value = timerSemanticProgram.initialData();
        for (int i = 0; i < list.size(); ++i) {
            TimerSemanticChoice timerSemanticChoice = list.get(i);
            TimerSemanticInstruction timerSemanticInstruction = timerSemanticProgram.instructions().get(i);
            if (timerSemanticChoice == null || timerSemanticChoice.instruction() != timerSemanticInstruction || !timerSemanticChoice.selected() && timerSemanticChoice.applied() || timerSemanticChoice.selected() && !timerSemanticInstruction.conditional() && !timerSemanticChoice.applied()) {
                throw new IllegalArgumentException("Timer choice does not match quoted instruction");
            }
            if (!timerSemanticChoice.applied()) continue;
            value = timerSemanticInstruction.value() == null ? FoodWeaponExpectedTrace.remove(value, timerSemanticInstruction.key()) : FoodWeaponExpectedTrace.put(value, timerSemanticInstruction.key(), timerSemanticInstruction.value());
        }
        return FoodWeaponHitPlan.timerSemanticProject(timerSemanticProgram, value);
    }

    private static TimerSemanticProjection timerSemanticProject(TimerSemanticProgram timerSemanticProgram, FoodWeaponExpectedTrace.Value value) {
        LinkedHashMap<String, FoodWeaponExpectedTrace.Value> linkedHashMap = new LinkedHashMap<String, FoodWeaponExpectedTrace.Value>();
        LinkedHashMap<String, Long> linkedHashMap2 = new LinkedHashMap<String, Long>();
        for (String string : FoodWeaponExpectedTrace.compoundKeys(value)) {
            FoodWeaponExpectedTrace.Value value2 = FoodWeaponExpectedTrace.field(value, string);
            if (!FoodWeaponExpectedTrace.same(value2, FoodWeaponExpectedTrace.field(timerSemanticProgram.baseline(), string))) {
                linkedHashMap.put(string, value2);
            }
            if (!string.startsWith("Last") || value2.kind != 4) continue;
            linkedHashMap2.put(string, value2.bits);
        }
        return new TimerSemanticProjection(timerSemanticProgram, value, linkedHashMap, linkedHashMap2);
    }

    private static void timerSemanticStart(FinalEffectsCommit finalEffectsCommit) {
        block5: {
            if (finalEffectsCommit == null) {
                return;
            }
            try {
                for (TimerSemanticCursor timerSemanticCursor : finalEffectsCommit.timerSemantics) {
                    if (timerSemanticCursor.thread != null || timerSemanticCursor.closed) {
                        throw new IllegalStateException("Timer semantic cursor reused");
                    }
                    timerSemanticCursor.thread = Thread.currentThread();
                }
            }
            catch (Throwable throwable) {
                if (finalEffectsCommit.timerSemanticFailure != null) break block5;
                finalEffectsCommit.timerSemanticFailure = throwable;
            }
        }
    }

    private static void timerSemanticMutation(FinalEffectsCommit finalEffectsCommit, TypedLeaf typedLeaf, boolean bl) {
        block6: {
            if (finalEffectsCommit == null) {
                return;
            }
            try {
                for (TimerSemanticCursor timerSemanticCursor : finalEffectsCommit.timerSemantics) {
                    TimerSemanticInstruction timerSemanticInstruction;
                    if (timerSemanticCursor.program.unsupported() != null || timerSemanticCursor.closed || timerSemanticCursor.next == timerSemanticCursor.instructions.size() || (timerSemanticInstruction = timerSemanticCursor.instructions.get(timerSemanticCursor.next)).leaf() != typedLeaf) continue;
                    if (timerSemanticCursor.thread != Thread.currentThread() || finalEffectsCommit.currentLeaf != typedLeaf || timerSemanticCursor.timerEntered || !bl && !timerSemanticInstruction.conditional()) {
                        throw new IllegalStateException("Timer semantic mutation outside original leaf");
                    }
                    if (bl) {
                        timerSemanticCursor.image = timerSemanticInstruction.value() == null ? FoodWeaponExpectedTrace.remove(timerSemanticCursor.image, timerSemanticInstruction.key()) : FoodWeaponExpectedTrace.put(timerSemanticCursor.image, timerSemanticInstruction.key(), timerSemanticInstruction.value());
                    }
                    ++timerSemanticCursor.next;
                    timerSemanticCursor.status = "SEMANTIC_PROGRESS";
                }
            }
            catch (Throwable throwable) {
                if (finalEffectsCommit.timerSemanticFailure != null) break block6;
                finalEffectsCommit.timerSemanticFailure = throwable;
            }
        }
    }

    private static void timerSemanticLeafReturned(FinalEffectsCommit finalEffectsCommit, TypedLeaf typedLeaf) {
        block4: {
            if (finalEffectsCommit == null) {
                return;
            }
            try {
                for (TimerSemanticCursor timerSemanticCursor : finalEffectsCommit.timerSemantics) {
                    if (timerSemanticCursor.program.unsupported() != null || timerSemanticCursor.next >= timerSemanticCursor.instructions.size() || timerSemanticCursor.instructions.get(timerSemanticCursor.next).leaf() != typedLeaf) continue;
                    throw new IllegalStateException("Missing original timer semantic branch result");
                }
            }
            catch (Throwable throwable) {
                if (finalEffectsCommit.timerSemanticFailure != null) break block4;
                finalEffectsCommit.timerSemanticFailure = throwable;
            }
        }
    }

    private static TimerSemanticCursor timerSemanticCursor(FinalEffectsCommit finalEffectsCommit, Object object) {
        for (TimerSemanticCursor timerSemanticCursor : finalEffectsCommit.timerSemantics) {
            if (timerSemanticCursor.program.leaf() != object) continue;
            return timerSemanticCursor;
        }
        return null;
    }

    private static void timerSemanticTimerBegins(FinalEffectsCommit finalEffectsCommit, TimerSaveLeaf timerSaveLeaf) {
        block5: {
            if (finalEffectsCommit == null) {
                return;
            }
            try {
                TimerSemanticCursor timerSemanticCursor = FoodWeaponHitPlan.timerSemanticCursor(finalEffectsCommit, timerSaveLeaf);
                if (timerSemanticCursor == null || timerSemanticCursor.program.unsupported() != null) {
                    return;
                }
                if (timerSemanticCursor.thread != Thread.currentThread() || timerSemanticCursor.closed || timerSemanticCursor.timerEntered || timerSemanticCursor.next != timerSemanticCursor.instructions.size() || finalEffectsCommit.currentLeaf != timerSaveLeaf) {
                    throw new IllegalStateException("Timer semantic projection before original branches resolved");
                }
                timerSemanticCursor.projection = FoodWeaponHitPlan.timerSemanticProject(timerSemanticCursor.program, timerSemanticCursor.image);
                timerSemanticCursor.timerEntered = true;
                timerSemanticCursor.status = "PROJECTED_FROM_QUOTE_AND_ORIGINAL_BRANCHES";
            }
            catch (Throwable throwable) {
                if (finalEffectsCommit.timerSemanticFailure != null) break block5;
                finalEffectsCommit.timerSemanticFailure = throwable;
            }
        }
    }

    private static void timerSemanticOriginalBegins(FinalEffectsCommit finalEffectsCommit, TimerSaveLeaf timerSaveLeaf, String string, int n) {
        block5: {
            if (finalEffectsCommit == null) {
                return;
            }
            try {
                TimerSemanticCursor timerSemanticCursor = FoodWeaponHitPlan.timerSemanticCursor(finalEffectsCommit, timerSaveLeaf);
                if (timerSemanticCursor == null || timerSemanticCursor.program.unsupported() != null) {
                    return;
                }
                if (!timerSemanticCursor.timerEntered || timerSemanticCursor.closed || timerSemanticCursor.currentStep != null || n != timerSemanticCursor.admittedKeys.size() || !timerSemanticCursor.projection.writes().containsKey(string) || timerSemanticCursor.admittedKeys.containsKey(string)) {
                    throw new IllegalStateException("Original timer key not in independently progressed semantic bag");
                }
                timerSemanticCursor.admittedKeys.put(string, n);
                timerSemanticCursor.currentStep = new TimerSemanticStep(timerSemanticCursor.projection, string, timerSemanticCursor.projection.writes().get(string), n);
            }
            catch (Throwable throwable) {
                if (finalEffectsCommit.timerSemanticFailure != null) break block5;
                finalEffectsCommit.timerSemanticFailure = throwable;
            }
        }
    }

    private static void timerSemanticOriginalReturned(FinalEffectsCommit finalEffectsCommit, TimerSaveLeaf timerSaveLeaf) {
        block5: {
            if (finalEffectsCommit == null) {
                return;
            }
            try {
                TimerSemanticCursor timerSemanticCursor = FoodWeaponHitPlan.timerSemanticCursor(finalEffectsCommit, timerSaveLeaf);
                if (timerSemanticCursor == null || timerSemanticCursor.program.unsupported() != null) {
                    return;
                }
                if (timerSemanticCursor.currentStep == null) {
                    throw new IllegalStateException("Original timer helper returned without admitted key");
                }
                ++timerSemanticCursor.returnedHelpers;
            }
            catch (Throwable throwable) {
                if (finalEffectsCommit.timerSemanticFailure != null) break block5;
                finalEffectsCommit.timerSemanticFailure = throwable;
            }
        }
    }

    private static void timerSemanticOriginalClosed(FinalEffectsCommit finalEffectsCommit, TimerSaveLeaf timerSaveLeaf) {
        block4: {
            if (finalEffectsCommit == null) {
                return;
            }
            try {
                TimerSemanticCursor timerSemanticCursor = FoodWeaponHitPlan.timerSemanticCursor(finalEffectsCommit, timerSaveLeaf);
                if (timerSemanticCursor != null) {
                    timerSemanticCursor.currentStep = null;
                }
            }
            catch (Throwable throwable) {
                if (finalEffectsCommit.timerSemanticFailure != null) break block4;
                finalEffectsCommit.timerSemanticFailure = throwable;
            }
        }
    }

    private static void timerSemanticRanged(FinalEffectsCommit finalEffectsCommit, TimerSaveLeaf timerSaveLeaf, boolean bl) {
        block6: {
            if (finalEffectsCommit == null) {
                return;
            }
            try {
                TimerSemanticCursor timerSemanticCursor = FoodWeaponHitPlan.timerSemanticCursor(finalEffectsCommit, timerSaveLeaf);
                if (timerSemanticCursor == null || timerSemanticCursor.program.unsupported() != null) {
                    return;
                }
                if (bl && (timerSemanticCursor.projection == null || timerSemanticCursor.projection.writes().isEmpty() || timerSemanticCursor.returnedHelpers != timerSemanticCursor.projection.writes().size())) {
                    throw new IllegalStateException("Original ranged save before complete semantic timer helper prefix");
                }
                timerSemanticCursor.rangedActive = bl;
                if (bl) {
                    timerSemanticCursor.rangedEntered = true;
                }
            }
            catch (Throwable throwable) {
                if (finalEffectsCommit.timerSemanticFailure != null) break block6;
                finalEffectsCommit.timerSemanticFailure = throwable;
            }
        }
    }

    private static void timerSemanticTimerReturned(FinalEffectsCommit finalEffectsCommit, TimerSaveLeaf timerSaveLeaf, boolean bl) {
        block5: {
            if (finalEffectsCommit == null) {
                return;
            }
            try {
                TimerSemanticCursor timerSemanticCursor = FoodWeaponHitPlan.timerSemanticCursor(finalEffectsCommit, timerSaveLeaf);
                if (timerSemanticCursor == null || timerSemanticCursor.program.unsupported() != null) {
                    return;
                }
                if (timerSemanticCursor.projection == null || timerSemanticCursor.admittedKeys.size() != timerSemanticCursor.projection.writes().size() || timerSemanticCursor.returnedHelpers != timerSemanticCursor.projection.writes().size() || bl != !timerSemanticCursor.projection.writes().isEmpty()) {
                    throw new IllegalStateException("Original timer traversal differs from quoted semantic key bag");
                }
                timerSemanticCursor.timerReturned = true;
                timerSemanticCursor.status = "ORIGINAL_HELPERS_RETURNED_NOT_FIELD_AUTHORITY";
            }
            catch (Throwable throwable) {
                if (finalEffectsCommit.timerSemanticFailure != null) break block5;
                finalEffectsCommit.timerSemanticFailure = throwable;
            }
        }
    }

    private static boolean timerSemanticFrame(FinalEffectsCommit finalEffectsCommit, Object object, FoodWeaponSourceLedger.OwnerWriterPublication ownerWriterPublication) {
        if (finalEffectsCommit == null || object == null || ownerWriterPublication == null || finalEffectsCommit.timerSemanticFailure != null || !finalEffectsCommit.executed || finalEffectsCommit.stopped || finalEffectsCommit.originalStopped || finalEffectsCommit.currentLeaf != object || finalEffectsCommit.writerPublication != ownerWriterPublication || finalEffectsCommit.originalSequence == null || !finalEffectsCommit.originalSequence.original.atLeaf(object)) {
            return false;
        }
        TimerSemanticCursor timerSemanticCursor = FoodWeaponHitPlan.timerSemanticCursor(finalEffectsCommit, object);
        return timerSemanticCursor != null && timerSemanticCursor.program.unsupported() == null && !timerSemanticCursor.closed && timerSemanticCursor.thread == Thread.currentThread() && timerSemanticCursor.timerEntered;
    }

    static TimerSemanticStep timerSemanticAtOriginal(FinalEffectsCommit finalEffectsCommit, Object object, FoodWeaponSourceLedger.OwnerWriterPublication ownerWriterPublication, int n, String string) {
        block3: {
            block2: {
                if (!FoodWeaponHitPlan.timerSemanticFrame(finalEffectsCommit, object, ownerWriterPublication) || !(object instanceof TimerSaveLeaf)) break block2;
                TimerSaveLeaf timerSaveLeaf = (TimerSaveLeaf)object;
                if (timerSaveLeaf.currentOriginalStepOrdinal == n && Objects.equals(timerSaveLeaf.currentOriginalKey, string)) break block3;
            }
            return null;
        }
        TimerSemanticStep timerSemanticStep = FoodWeaponHitPlan.timerSemanticCursor((FinalEffectsCommit)finalEffectsCommit, (Object)object).currentStep;
        return timerSemanticStep != null && timerSemanticStep.originalStepOrdinal() == n && timerSemanticStep.key().equals(string) ? timerSemanticStep : null;
    }

    static TimerSemanticProjection timerSemanticAtRanged(FinalEffectsCommit finalEffectsCommit, Object object, FoodWeaponSourceLedger.OwnerWriterPublication ownerWriterPublication) {
        if (!FoodWeaponHitPlan.timerSemanticFrame(finalEffectsCommit, object, ownerWriterPublication)) {
            return null;
        }
        TimerSemanticCursor timerSemanticCursor = FoodWeaponHitPlan.timerSemanticCursor(finalEffectsCommit, object);
        return timerSemanticCursor.rangedActive ? timerSemanticCursor.projection : null;
    }

    static TimerSemanticStep timerSemanticAtSessionOriginal(FoodWeaponOriginalSequence.Session session, Object object, String string) {
        if (session == null || !(object instanceof TimerSaveLeaf)) {
            return null;
        }
        TimerSaveLeaf timerSaveLeaf = (TimerSaveLeaf)object;
        FinalEffectsCommit finalEffectsCommit = timerSaveLeaf.currentSemanticCommit;
        if (finalEffectsCommit == null || finalEffectsCommit.originalSequence != session) {
            return null;
        }
        return FoodWeaponHitPlan.timerSemanticAtOriginal(finalEffectsCommit, object, finalEffectsCommit.writerPublication, timerSaveLeaf.currentOriginalStepOrdinal, string);
    }

    static TimerSemanticProjection timerSemanticAtSessionRanged(FoodWeaponOriginalSequence.Session session, Object object) {
        if (session == null || !(object instanceof TimerSaveLeaf)) {
            return null;
        }
        TimerSaveLeaf timerSaveLeaf = (TimerSaveLeaf)object;
        FinalEffectsCommit finalEffectsCommit = timerSaveLeaf.currentSemanticCommit;
        if (finalEffectsCommit == null || finalEffectsCommit.originalSequence != session) {
            return null;
        }
        return FoodWeaponHitPlan.timerSemanticAtRanged(finalEffectsCommit, object, finalEffectsCommit.writerPublication);
    }

    static List<TimerSemanticResult> timerSemanticResults(FinalEffectsCommit finalEffectsCommit) {
        if (finalEffectsCommit == null) {
            return List.of();
        }
        return finalEffectsCommit.timerSemantics.stream().map(timerSemanticCursor -> new TimerSemanticResult(timerSemanticCursor.program.leaf(), timerSemanticCursor.next, timerSemanticCursor.admittedKeys.size(), timerSemanticCursor.returnedHelpers, timerSemanticCursor.timerEntered, timerSemanticCursor.rangedEntered, timerSemanticCursor.timerReturned, timerSemanticCursor.closed, timerSemanticCursor.status, timerSemanticCursor.originalFailure, finalEffectsCommit.timerSemanticFailure == null ? timerSemanticCursor.program.unsupported() : finalEffectsCommit.timerSemanticFailure)).toList();
    }

    private static void timerSemanticFailure(FinalEffectsCommit finalEffectsCommit, Throwable throwable) {
        block5: {
            if (finalEffectsCommit == null) {
                return;
            }
            try {
                for (TimerSemanticCursor timerSemanticCursor : finalEffectsCommit.timerSemantics) {
                    if (timerSemanticCursor.originalFailure == null && !timerSemanticCursor.timerReturned) {
                        timerSemanticCursor.originalFailure = throwable;
                    }
                    if (timerSemanticCursor.timerReturned) continue;
                    timerSemanticCursor.status = "ORIGINAL_THROW_SAME_PREFIX";
                }
            }
            catch (Throwable throwable2) {
                if (finalEffectsCommit.timerSemanticFailure != null) break block5;
                finalEffectsCommit.timerSemanticFailure = throwable2;
            }
        }
    }

    private static void timerSemanticClose(FinalEffectsCommit finalEffectsCommit) {
        block4: {
            if (finalEffectsCommit == null) {
                return;
            }
            try {
                for (TimerSemanticCursor timerSemanticCursor : finalEffectsCommit.timerSemantics) {
                    timerSemanticCursor.currentStep = null;
                    timerSemanticCursor.rangedActive = false;
                    timerSemanticCursor.closed = true;
                    if (timerSemanticCursor.timerReturned || timerSemanticCursor.originalFailure != null || timerSemanticCursor.program.unsupported() != null) continue;
                    timerSemanticCursor.status = "CLOSED_BEFORE_TIMER_RETURN";
                }
            }
            catch (Throwable throwable) {
                if (finalEffectsCommit.timerSemanticFailure != null) break block4;
                finalEffectsCommit.timerSemanticFailure = throwable;
            }
        }
    }

    static OriginalRepairFacts originalRepairFacts(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge, Object object) {
        RepairLeaf repairLeaf;
        block3: {
            block2: {
                if (!(object instanceof RepairLeaf)) break block2;
                repairLeaf = (RepairLeaf)object;
                if (finalEffectsCommit != null) break block3;
            }
            return null;
        }
        OriginalRepairFacts originalRepairFacts = repairLeaf.originalFacts;
        return originalRepairFacts.entered && originalRepairFacts.bound && !originalRepairFacts.closed && originalRepairFacts.originalCommit == finalEffectsCommit && originalRepairFacts.originalSequence == finalEffectsCommit.originalSequence && originalRepairFacts.originalLeaf == object && originalRepairFacts.originalThread == Thread.currentThread() && FoodWeaponFinalDamage.matchesOriginalHitPlanLeaf(finalEffectsCommit, childHurtBridge, object, finalEffectsCommit.currentLeafOrdinal) ? originalRepairFacts : null;
    }

    private boolean matchesClosedContext(ServerPlayer serverPlayer) {
        return serverPlayer != null && !this.committed && this.targets.stream().allMatch(target -> target.closedRecipient(serverPlayer));
    }

    static PreparedFinalEffects prepareFinalEffects(FoodWeaponHitPlan foodWeaponHitPlan, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2, ServerPlayer serverPlayer, DamageSource damageSource) {
        if (foodWeaponHitPlan == null || !foodWeaponHitPlan.commits.isEmpty() || foodWeaponHitPlan.typedLeaves.stream().anyMatch(typedLeaf -> !typedLeaf.recipientClosureComplete()) || !foodWeaponHitPlan.matchesClosedContext(serverPlayer) || attemptBatchQuote == null || attemptBatchAdmission == null || attemptBatchAdmission.status() != FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY || !FoodWeaponSourceLedger.matchesAttemptRequests(attemptBatchQuote, list, list2)) {
            return null;
        }
        if (!list.containsAll(foodWeaponHitPlan.attempts.stream().map(attempt -> new FoodWeaponSourceLedger.AttemptRequest(attempt.effect(), attempt.chance())).toList()) || !list2.containsAll(foodWeaponHitPlan.targets())) {
            return null;
        }
        return new PreparedFinalEffects(foodWeaponHitPlan, attemptBatchQuote, attemptBatchAdmission, list, list2, serverPlayer, damageSource);
    }

    static boolean ownerReserveLeafSelected(PreparedFinalEffects preparedFinalEffects, FoodWeaponHitPlan foodWeaponHitPlan, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, FoodWeaponSourceLedger.AttemptBatchCommit attemptBatchCommit, Object object, ServerPlayer serverPlayer, DamageSource damageSource, long l, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponFinalDamage.class && stackFrame.getMethodName().equals("acceptsOwnerReserveLeaf")).orElse(false));
        if (!(bl && Thread.holdsLock(FoodWeaponSourceLedger.class) && preparedFinalEffects != null && foodWeaponHitPlan != null && preparedFinalEffects.plan == foodWeaponHitPlan && preparedFinalEffects.batch == attemptBatchQuote && preparedFinalEffects.admission == attemptBatchAdmission && preparedFinalEffects.player == serverPlayer && preparedFinalEffects.source == damageSource && preparedFinalEffects.tick == l && list != null && list2 != null && list.equals(preparedFinalEffects.requests) && list2.equals(preparedFinalEffects.targets) && !foodWeaponHitPlan.committed && foodWeaponHitPlan.commits.isEmpty() && attemptBatchCommit != null && attemptBatchCommit.status() == FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED && object instanceof TypedLeaf)) {
            return false;
        }
        TypedLeaf typedLeaf = (TypedLeaf)object;
        int n = 0;
        for (TypedLeaf object2 : foodWeaponHitPlan.typedLeaves) {
            if (object2 != typedLeaf) continue;
            ++n;
        }
        if (n != 1) {
            return false;
        }
        for (Target target : foodWeaponHitPlan.targets) {
            if (attemptBatchCommit.target(target.request().target(), target.request().domain()).map(targetResult -> targetResult.commit() == FoodWeaponSourceLedger.TargetCommit.COMMITTED).orElse(false).booleanValue()) continue;
            return false;
        }
        EnumMap enumMap = new EnumMap(FoodWeaponSourceLedger.AttemptDomain.class);
        for (Attempt attempt : foodWeaponHitPlan.attempts) {
            FoodWeaponSourceLedger.AttemptCommit attemptCommit = attemptBatchCommit.attempt(attempt.effect()).map(FoodWeaponSourceLedger.AttemptResult::commit).orElse(null);
            if (attemptCommit != (attempt.outcome().success() ? FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS : FoodWeaponSourceLedger.AttemptCommit.COMMITTED_FAILURE)) {
                return false;
            }
            enumMap.put(attempt.effect(), attemptCommit);
        }
        return typedLeaf.attempt == null || enumMap.get((Object)typedLeaf.attempt) == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS;
    }

    static FinalEffectsCommit commitFinalState(PreparedFinalEffects preparedFinalEffects, float f, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt) {
        boolean bl = FoodWeaponFinalDamage.matchesOriginalFinalPreparation(preparedFinalEffects, offenseReceipt);
        if (preparedFinalEffects == null || offenseReceipt == null || !Float.isFinite(f) || f <= 0.0f || !bl && !preparedFinalEffects.plan.matchesClosedContext(preparedFinalEffects.player) || !offenseReceipt.matchesBatch(preparedFinalEffects.batch, preparedFinalEffects.admission, preparedFinalEffects.requests, preparedFinalEffects.targets) || !offenseReceipt.matches(preparedFinalEffects.plan, preparedFinalEffects.player, preparedFinalEffects.source, preparedFinalEffects.tick, f)) {
            return null;
        }
        FoodWeaponHitPlan foodWeaponHitPlan = preparedFinalEffects.plan;
        for (Target object2 : foodWeaponHitPlan.targets) {
            if (offenseReceipt.target(object2.request().target(), object2.request().domain()).orElseThrow().commit() == FoodWeaponSourceLedger.TargetCommit.COMMITTED) continue;
            return null;
        }
        EnumMap enumMap = new EnumMap(FoodWeaponSourceLedger.AttemptDomain.class);
        for (Attempt attempt : foodWeaponHitPlan.attempts) {
            FoodWeaponSourceLedger.AttemptCommit attemptCommit = offenseReceipt.attempt(attempt.effect()).orElseThrow().commit();
            if (attemptCommit != (attempt.outcome().success() ? FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS : FoodWeaponSourceLedger.AttemptCommit.COMMITTED_FAILURE)) {
                return null;
            }
            enumMap.put(attempt.effect(), attemptCommit);
        }
        List<TypedLeaf> list = foodWeaponHitPlan.typedLeaves.stream().filter(typedLeaf -> typedLeaf.attempt == null || enumMap.get((Object)typedLeaf.attempt) == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS).toList();
        foodWeaponHitPlan.committed = true;
        return new FinalEffectsCommit(preparedFinalEffects, offenseReceipt, f, list);
    }

    static boolean leafSelectionReady(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (finalEffectsCommit != null && finalEffectsCommit.originalSequence != null) {
            return FoodWeaponFinalDamage.originalHitPlanSequence(finalEffectsCommit, childHurtBridge) == finalEffectsCommit.originalSequence && !finalEffectsCommit.originalStopped && finalEffectsCommit.executed && finalEffectsCommit.currentLeaf == null && finalEffectsCommit.currentLeafOrdinal == -1 && finalEffectsCommit.writerPublication == null;
        }
        if (finalEffectsCommit == null || finalEffectsCommit.stopped || !finalEffectsCommit.executed || finalEffectsCommit.currentLeaf != null || finalEffectsCommit.currentLeafOrdinal != -1 || finalEffectsCommit.writerPublication != null) {
            return false;
        }
        if (finalEffectsCommit.prepared.plan.typedLeaves.stream().anyMatch(typedLeaf -> !typedLeaf.recipientClosureComplete())) {
            finalEffectsCommit.stopped = true;
            return false;
        }
        return FoodWeaponFinalDamage.matchesHitPlanSelectionPhase(finalEffectsCommit, childHurtBridge);
    }

    static FoodWeaponSourceLedger.OwnerWriterStep planTimerAtOriginalSite(FinalEffectsCommit finalEffectsCommit, Object object, int n, FoodWeaponSourceLedger.OwnerWriterPublication ownerWriterPublication, ItemStack itemStack, FoodWeaponEvolution.Profile profile, String string, Tag tag, CompoundTag compoundTag, Tag tag2) {
        try {
            block7: {
                block6: {
                    if (finalEffectsCommit == null || finalEffectsCommit.stopped || !finalEffectsCommit.executed || ownerWriterPublication == null || finalEffectsCommit.writerPublication != ownerWriterPublication || finalEffectsCommit.currentLeaf != object || !(object instanceof TimerSaveLeaf)) break block6;
                    TimerSaveLeaf timerSaveLeaf = (TimerSaveLeaf)object;
                    if (timerSaveLeaf.weapon == itemStack && timerSaveLeaf.profile == profile && timerSaveLeaf.currentOriginalKey == string && timerSaveLeaf.currentOriginalValue == tag && timerSaveLeaf.currentOriginalStepOrdinal == n && compoundTag != null && tag2 != null && Thread.holdsLock(FoodWeaponSourceLedger.class)) break block7;
                }
                FoodWeaponHitPlan.retainTimerObserverFailure(finalEffectsCommit, object, n, ownerWriterPublication, null, TimerObserverPhase.PLANNING, null);
                return null;
            }
            finalEffectsCommit.timerOriginalReceiver = compoundTag;
            finalEffectsCommit.timerOriginalArgument = tag2;
            finalEffectsCommit.timerOriginalValue = tag;
            finalEffectsCommit.timerOriginalKey = string;
            finalEffectsCommit.timerOriginalStepOrdinal = n;
            finalEffectsCommit.timerOriginalWriter = ownerWriterPublication;
            FoodWeaponSourceLedger.OwnerWriterStep ownerWriterStep = FoodWeaponSourceLedger.planOwnerTimerWriter(ownerWriterPublication, finalEffectsCommit, object, n, FoodWeaponEvolution.planHitTimerProfile(profile), string, tag);
            if (ownerWriterStep == null) {
                FoodWeaponHitPlan.retainTimerObserverFailure(finalEffectsCommit, object, n, ownerWriterPublication, null, TimerObserverPhase.PLANNING, null);
            }
            return ownerWriterStep;
        }
        catch (Throwable throwable) {
            FoodWeaponHitPlan.retainTimerObserverFailure(finalEffectsCommit, object, n, ownerWriterPublication, null, TimerObserverPhase.PLANNING, throwable);
            return null;
        }
    }

    static void retainTimerObserverFailure(FinalEffectsCommit finalEffectsCommit, Object object, int n, FoodWeaponSourceLedger.OwnerWriterPublication ownerWriterPublication, FoodWeaponSourceLedger.OwnerWriterStep ownerWriterStep, TimerObserverPhase timerObserverPhase, Throwable throwable) {
        if (finalEffectsCommit == null) {
            return;
        }
        if (!finalEffectsCommit.timerObservationIncomplete) {
            finalEffectsCommit.incompleteTimerLeaf = object;
            finalEffectsCommit.incompleteTimerStepOrdinal = n;
            finalEffectsCommit.incompleteTimerStep = ownerWriterStep;
            finalEffectsCommit.incompleteTimerWriterPublication = ownerWriterPublication;
        } else if (finalEffectsCommit.incompleteTimerStep == null && ownerWriterStep != null) {
            finalEffectsCommit.incompleteTimerStep = ownerWriterStep;
        }
        finalEffectsCommit.timerObservationIncomplete = true;
        if (finalEffectsCommit.originalSequence != null) {
            finalEffectsCommit.originalSequence.observationFailed(null, timerObserverPhase.ordinal(), throwable);
            switch (timerObserverPhase) {
                case PLANNING: {
                    if (finalEffectsCommit.timerPlanningFailure != null) break;
                    finalEffectsCommit.timerPlanningFailure = throwable;
                    break;
                }
                case ENTERED: {
                    if (finalEffectsCommit.timerEnteredFailure != null) break;
                    finalEffectsCommit.timerEnteredFailure = throwable;
                    break;
                }
                case RETURNED: {
                    if (finalEffectsCommit.timerReturnedFailure != null) break;
                    finalEffectsCommit.timerReturnedFailure = throwable;
                    break;
                }
                case EXCEPTION: {
                    if (finalEffectsCommit.timerExceptionObservationFailure != null) break;
                    finalEffectsCommit.timerExceptionObservationFailure = throwable;
                    break;
                }
                case CLOSE: {
                    if (finalEffectsCommit.timerCloseFailure != null) break;
                    finalEffectsCommit.timerCloseFailure = throwable;
                }
            }
            return;
        }
        finalEffectsCommit.stopped = true;
        int n2 = switch (timerObserverPhase) {
            default -> throw new IncompatibleClassChangeError();
            case TimerObserverPhase.PLANNING, TimerObserverPhase.ENTERED -> 0;
            case TimerObserverPhase.RETURNED -> 1;
            case TimerObserverPhase.EXCEPTION -> 2;
            case TimerObserverPhase.CLOSE -> 3;
        };
        switch (timerObserverPhase) {
            case PLANNING: {
                if (finalEffectsCommit.timerPlanningFailure != null) break;
                finalEffectsCommit.timerPlanningFailure = throwable;
                break;
            }
            case ENTERED: {
                if (finalEffectsCommit.timerEnteredFailure != null) break;
                finalEffectsCommit.timerEnteredFailure = throwable;
                break;
            }
            case RETURNED: {
                if (finalEffectsCommit.timerReturnedFailure != null) break;
                finalEffectsCommit.timerReturnedFailure = throwable;
                break;
            }
            case EXCEPTION: {
                if (finalEffectsCommit.timerExceptionObservationFailure != null) break;
                finalEffectsCommit.timerExceptionObservationFailure = throwable;
                break;
            }
            case CLOSE: {
                if (finalEffectsCommit.timerCloseFailure != null) break;
                finalEffectsCommit.timerCloseFailure = throwable;
            }
        }
        try {
            if (ownerWriterStep == null) {
                FoodWeaponSourceLedger.ownerPublicationObservationFailed(ownerWriterPublication, n2, throwable);
            } else {
                FoodWeaponSourceLedger.ownerWriterObservationFailed(ownerWriterStep, n2, throwable);
            }
        }
        catch (Throwable throwable2) {
            switch (n2) {
                case 0: {
                    if (finalEffectsCommit.timerEntryRecordingFailure != null) break;
                    finalEffectsCommit.timerEntryRecordingFailure = throwable2;
                    break;
                }
                case 1: {
                    if (finalEffectsCommit.timerNormalRecordingFailure != null) break;
                    finalEffectsCommit.timerNormalRecordingFailure = throwable2;
                    break;
                }
                case 2: {
                    if (finalEffectsCommit.timerExceptionRecordingFailure != null) break;
                    finalEffectsCommit.timerExceptionRecordingFailure = throwable2;
                    break;
                }
                case 3: {
                    if (finalEffectsCommit.timerCloseRecordingFailure != null) break;
                    finalEffectsCommit.timerCloseRecordingFailure = throwable2;
                }
            }
        }
    }

    static void timerOriginalPutReturned(FinalEffectsCommit finalEffectsCommit) {
        if (finalEffectsCommit != null) {
            finalEffectsCommit.timerOriginalPutReturned = true;
        }
    }

    static void retainTimerOriginalFailure(FinalEffectsCommit finalEffectsCommit, Object object, int n, FoodWeaponSourceLedger.OwnerWriterPublication ownerWriterPublication, FoodWeaponSourceLedger.OwnerWriterStep ownerWriterStep, Throwable throwable) {
        if (finalEffectsCommit == null) {
            return;
        }
        if (finalEffectsCommit.timerOriginalFailure == null) {
            finalEffectsCommit.timerOriginalFailure = throwable;
        }
        try {
            if (ownerWriterStep == null) {
                FoodWeaponSourceLedger.ownerPublicationOriginalFailed(ownerWriterPublication, throwable);
            } else {
                FoodWeaponSourceLedger.ownerWriterOriginalFailed(ownerWriterStep, throwable);
            }
        }
        catch (Throwable throwable2) {
            FoodWeaponHitPlan.retainTimerObserverFailure(finalEffectsCommit, object, n, ownerWriterPublication, ownerWriterStep, TimerObserverPhase.EXCEPTION, throwable2);
        }
    }

    static boolean timerObservationNotKnownIncomplete(FinalEffectsCommit finalEffectsCommit) {
        return finalEffectsCommit != null && !finalEffectsCommit.timerObservationIncomplete && (finalEffectsCommit.originalSequence == null || finalEffectsCommit.originalSequence.extraAuthorityRetained());
    }

    static FoodWeaponOriginalSequence.Session originalSequence(FinalEffectsCommit finalEffectsCommit) {
        return finalEffectsCommit == null ? null : finalEffectsCommit.originalSequence;
    }

    static boolean bindOriginalSequence(FinalEffectsCommit finalEffectsCommit, FoodWeaponOriginalSequence.Session session, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt) {
        if (finalEffectsCommit == null || session == null || finalEffectsCommit.executed || finalEffectsCommit.originalSequence != null || finalEffectsCommit.receipt != offenseReceipt || session.lineage.acceptedCallback != offenseReceipt) {
            return false;
        }
        finalEffectsCommit.originalSequence = session;
        return true;
    }

    static FoodWeaponOriginalSequence.Step beginOriginalTimer(FinalEffectsCommit finalEffectsCommit, Object object, String string, String string2) {
        if (finalEffectsCommit == null) {
            return null;
        }
        if (finalEffectsCommit.originalSequence == null) {
            finalEffectsCommit.timerObservationIncomplete = true;
            return null;
        }
        FoodWeaponOriginalSequence.Session session = finalEffectsCommit.originalSequence;
        try {
            return session.originalBegins(object, FoodWeaponExpectedTrace.Kind.TIMER, string, string2);
        }
        catch (Throwable throwable) {
            session.observationFailed(null, 0, throwable);
            return null;
        }
    }

    static void retainOriginalTimerThrowable(FinalEffectsCommit finalEffectsCommit, Throwable throwable) {
        if (finalEffectsCommit != null && finalEffectsCommit.timerOriginalFailure == null) {
            finalEffectsCommit.timerOriginalFailure = throwable;
        }
    }

    static boolean leafLive(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (finalEffectsCommit != null && finalEffectsCommit.originalSequence != null) {
            return !finalEffectsCommit.originalStopped && finalEffectsCommit.executed && FoodWeaponFinalDamage.matchesOriginalHitPlanLeaf(finalEffectsCommit, childHurtBridge, finalEffectsCommit.currentLeaf, finalEffectsCommit.currentLeafOrdinal);
        }
        if (finalEffectsCommit == null || finalEffectsCommit.stopped) {
            return false;
        }
        PreparedFinalEffects preparedFinalEffects = finalEffectsCommit.prepared;
        if (preparedFinalEffects.plan.typedLeaves.stream().anyMatch(typedLeaf -> !typedLeaf.recipientClosureComplete())) {
            finalEffectsCommit.stopped = true;
            return false;
        }
        return finalEffectsCommit.executed && FoodWeaponFinalDamage.matchesHitPlanInvocation(finalEffectsCommit, childHurtBridge) && finalEffectsCommit.receipt.matchesBatch(preparedFinalEffects.batch, preparedFinalEffects.admission, preparedFinalEffects.requests, preparedFinalEffects.targets) && finalEffectsCommit.receipt.matches(preparedFinalEffects.plan, preparedFinalEffects.player, preparedFinalEffects.source, preparedFinalEffects.tick, finalEffectsCommit.amount);
    }

    static boolean matchesBoundRecipient(FinalEffectsCommit finalEffectsCommit, LivingEntity livingEntity, FoodWeaponSourceLedger.TargetEffectDomain targetEffectDomain) {
        return finalEffectsCommit != null && livingEntity != null && finalEffectsCommit.prepared.plan.targets.stream().anyMatch(target -> target.entity() == livingEntity && target.request().domain() == targetEffectDomain && target.request().target().equals(livingEntity.m_20148_()));
    }

    static void stopFinalEffects(FinalEffectsCommit finalEffectsCommit) {
        if (finalEffectsCommit != null) {
            finalEffectsCommit.stopped = true;
            finalEffectsCommit.originalStopped = true;
            FoodWeaponHitPlan.timerSemanticClose(finalEffectsCommit);
        }
    }

    static boolean matchesSelectedFinalLeaf(FinalEffectsCommit finalEffectsCommit, Object object, int n) {
        return finalEffectsCommit != null && finalEffectsCommit.executed && !finalEffectsCommit.stopped && finalEffectsCommit.currentLeaf == object && finalEffectsCommit.currentLeafOrdinal == n && n >= 0 && n < finalEffectsCommit.leaves.size() && finalEffectsCommit.leaves.get(n) == object;
    }

    static boolean matchesOriginalLeafAdmission(FinalEffectsCommit finalEffectsCommit, FoodWeaponOriginalSequence.Session session, Object object, int n) {
        return finalEffectsCommit != null && !finalEffectsCommit.originalStopped && finalEffectsCommit.executed && session != null && finalEffectsCommit.originalSequence == session && object != null && finalEffectsCommit.currentLeaf == object && finalEffectsCommit.currentLeafOrdinal == n && n >= 0 && n < finalEffectsCommit.leaves.size() && finalEffectsCommit.leaves.get(n) == object;
    }

    static boolean matchesOriginalSelectedFinalLeaf(FinalEffectsCommit finalEffectsCommit, Object object, int n) {
        return finalEffectsCommit != null && !finalEffectsCommit.originalStopped && finalEffectsCommit.executed && finalEffectsCommit.originalSequence != null && finalEffectsCommit.currentLeaf == object && finalEffectsCommit.currentLeafOrdinal == n && n >= 0 && n < finalEffectsCommit.leaves.size() && finalEffectsCommit.leaves.get(n) == object && finalEffectsCommit.originalSequence.original.atLeaf(object);
    }

    static boolean matchesOwnerWriterSelectedLeaf(FinalEffectsCommit finalEffectsCommit, PreparedFinalEffects preparedFinalEffects, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, Object object, int n) {
        return finalEffectsCommit != null && preparedFinalEffects != null && offenseReceipt != null && finalEffectsCommit.prepared == preparedFinalEffects && finalEffectsCommit.receipt == offenseReceipt && preparedFinalEffects.plan.committed && FoodWeaponHitPlan.matchesSelectedFinalLeaf(finalEffectsCommit, object, n);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static void applyFinalEffects(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (finalEffectsCommit == null) return;
        if (finalEffectsCommit.executed) {
            return;
        }
        FoodWeaponOriginalSequence.Session session = FoodWeaponFinalDamage.originalHitPlanSequence(finalEffectsCommit, childHurtBridge);
        boolean bl = session != null && session == finalEffectsCommit.originalSequence;
        finalEffectsCommit.executed = true;
        FoodWeaponHitPlan.timerSemanticStart(finalEffectsCommit);
        try {
            int n = 0;
            while (n < finalEffectsCommit.leaves.size()) {
                TypedLeaf typedLeaf = finalEffectsCommit.leaves.get(n);
                if (!FoodWeaponHitPlan.leafSelectionReady(finalEffectsCommit, childHurtBridge)) {
                    return;
                }
                finalEffectsCommit.currentLeaf = typedLeaf;
                finalEffectsCommit.currentLeafOrdinal = n;
                boolean bl2 = false;
                try {
                    if (bl && !(bl2 = session.original.enterCurrentLeaf(typedLeaf, n))) {
                        return;
                    }
                    finalEffectsCommit.writerPublication = FoodWeaponFinalDamage.selectHitPlanWriterLeaf(finalEffectsCommit, childHurtBridge, typedLeaf, n);
                    if (finalEffectsCommit.writerPublication == null && !bl) {
                        finalEffectsCommit.stopped = true;
                        return;
                    }
                    typedLeaf.apply(finalEffectsCommit, childHurtBridge);
                    FoodWeaponHitPlan.timerSemanticLeafReturned(finalEffectsCommit, typedLeaf);
                    if (!bl && !FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
                        return;
                    }
                }
                catch (Throwable throwable) {
                    FoodWeaponHitPlan.timerSemanticFailure(finalEffectsCommit, throwable);
                    if (!bl) throw throwable;
                    session.original.originalThrown(throwable);
                    throw throwable;
                }
                finally {
                    if (bl2) {
                        session.original.leaveLeaf(typedLeaf);
                    }
                    finalEffectsCommit.writerPublication = null;
                    finalEffectsCommit.currentLeaf = null;
                    finalEffectsCommit.currentLeafOrdinal = -1;
                }
                ++n;
            }
            return;
        }
        finally {
            FoodWeaponHitPlan.timerSemanticClose(finalEffectsCommit);
        }
    }

    public void commit(float f, DamageSource damageSource, ServerPlayer serverPlayer, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt) {
        if (this.committed || !Float.isFinite(f) || f <= 0.0f) {
            return;
        }
        if (offenseReceipt == null || !offenseReceipt.matches(this, serverPlayer, damageSource, FoodWeaponSourceLedger.clock(serverPlayer), f)) {
            throw new IllegalStateException("The current private Final execution receipt for this exact plan is required");
        }
        if (!this.matchesContext(serverPlayer)) {
            throw new IllegalStateException("Quoted target context changed before publication");
        }
        for (Target object : this.targets) {
            if (offenseReceipt.target(object.request().target(), object.request().domain()).orElseThrow().commit() == FoodWeaponSourceLedger.TargetCommit.COMMITTED) continue;
            throw new IllegalStateException("Every target intent needs a fresh atomic Owner target result");
        }
        EnumMap enumMap = new EnumMap(FoodWeaponSourceLedger.AttemptDomain.class);
        for (Attempt attempt2 : this.attempts) {
            enumMap.put(attempt2.effect(), offenseReceipt.attempt(attempt2.effect()).orElseThrow().commit());
        }
        if (this.attempts.stream().anyMatch(attempt -> enumMap.get((Object)attempt.effect()) != (attempt.outcome().success() ? FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS : FoodWeaponSourceLedger.AttemptCommit.COMMITTED_FAILURE))) {
            throw new IllegalStateException("Owner must atomically submit the full quoted attempt batch before effects");
        }
        this.committed = true;
        for (Commit commit : this.commits) {
            if (commit.effect() != null && enumMap.get((Object)commit.effect()) != FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS) continue;
            commit.action().run();
        }
    }

    record TimerSemanticCatalogue(List<TimerSemanticProgram> programs, Throwable unsupported) {
        TimerSemanticCatalogue {
            list = List.copyOf(list);
        }
    }

    private record TimerSemanticSeed(FoodWeaponExpectedTrace.Value baseline, FoodWeaponExpectedTrace.Value data, Throwable unsupported) {
    }

    private static final class TimerSaveLeaf
    extends TypedLeaf {
        private final ItemStack weapon;
        private final FoodWeaponEvolution.Profile profile;
        private final ServerPlayer player;
        private final DamageSource source;
        private final CompoundTag baseline;
        private final CompoundTag data;
        private final TimerSemanticSeed semanticSeed;
        private FinalEffectsCommit currentSemanticCommit;
        private String currentOriginalKey;
        private Tag currentOriginalValue;
        private int currentOriginalStepOrdinal = -1;

        private TimerSaveLeaf(ItemStack itemStack, FoodWeaponEvolution.Profile profile, ServerPlayer serverPlayer, DamageSource damageSource, CompoundTag compoundTag, CompoundTag compoundTag2) {
            super(null);
            this.weapon = itemStack;
            this.profile = profile;
            this.player = serverPlayer;
            this.source = damageSource;
            this.baseline = compoundTag.m_6426_();
            this.data = compoundTag2;
            this.semanticSeed = FoodWeaponHitPlan.timerSemanticSeed(this.baseline, compoundTag2);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        void apply(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            this.currentSemanticCommit = finalEffectsCommit;
            try {
                FoodWeaponHitPlan.timerSemanticTimerBegins(finalEffectsCommit, this);
                boolean bl = false;
                int n = 0;
                for (String string : this.data.m_128431_()) {
                    if (Objects.equals(this.baseline.m_128423_(string), this.data.m_128423_(string))) continue;
                    Tag tag = this.data.m_128423_(string);
                    FoodWeaponSourceLedger.OwnerWriterPublication ownerWriterPublication = finalEffectsCommit.writerPublication;
                    finalEffectsCommit.timerOriginalPutReturned = false;
                    this.currentOriginalKey = string;
                    this.currentOriginalValue = tag;
                    this.currentOriginalStepOrdinal = n;
                    FoodWeaponHitPlan.timerSemanticOriginalBegins(finalEffectsCommit, this, string, n);
                    try {
                        FoodWeaponEvolution.writePlanHitTimer(this.weapon, this.profile, string, tag, null, finalEffectsCommit, n, ownerWriterPublication, this);
                        FoodWeaponHitPlan.timerSemanticOriginalReturned(finalEffectsCommit, this);
                    }
                    finally {
                        FoodWeaponHitPlan.timerSemanticOriginalClosed(finalEffectsCommit, this);
                        this.currentOriginalKey = null;
                        this.currentOriginalValue = null;
                        this.currentOriginalStepOrdinal = -1;
                    }
                    ++n;
                    bl = true;
                }
                if (bl) {
                    FoodWeaponHitPlan.timerSemanticRanged(finalEffectsCommit, this, true);
                    try {
                        FoodWeaponRanged.saveHitTimers(this.player, this.source, this.data);
                    }
                    finally {
                        FoodWeaponHitPlan.timerSemanticRanged(finalEffectsCommit, this, false);
                    }
                }
                FoodWeaponHitPlan.timerSemanticTimerReturned(finalEffectsCommit, this, bl);
            }
            finally {
                if (this.currentSemanticCommit == finalEffectsCommit) {
                    this.currentSemanticCommit = null;
                }
            }
        }
    }

    /*
     * Uses 'sealed' constructs - enablewith --sealed true
     */
    private static abstract class TypedLeaf {
        private final FoodWeaponSourceLedger.AttemptDomain attempt;

        private TypedLeaf(FoodWeaponSourceLedger.AttemptDomain attemptDomain) {
            this.attempt = attemptDomain;
        }

        boolean recipientClosureComplete() {
            return true;
        }

        abstract void apply(FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2);
    }

    private static final class TagLeaf
    extends TypedLeaf {
        private final CompoundTag data;
        private final String key;
        private final Tag value;
        private OwnerNativeSemanticAuthority.TargetWrite targetWrite;

        private TagLeaf(FoodWeaponSourceLedger.AttemptDomain attemptDomain, CompoundTag compoundTag, String string, Tag tag) {
            super(attemptDomain);
            this.data = compoundTag;
            this.key = string;
            this.value = tag == null ? null : tag.m_6426_();
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        @Override
        void apply(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            OwnerNativeSemanticAuthority.TargetObservation targetObservation = null;
            try {
                targetObservation = OwnerNativeSemanticAuthority.targetEntered(finalEffectsCommit, childHurtBridge, this, this.targetWrite, this.data, this.key, this.value);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            try {
                if (this.value == null) {
                    this.data.m_128473_(this.key);
                    try {
                        OwnerNativeSemanticAuthority.targetRemoved(targetObservation, this.data, this.key);
                    }
                    catch (Throwable throwable) {
                        try {
                            OwnerNativeSemanticAuthority.targetFailed(targetObservation, throwable);
                        }
                        catch (Throwable throwable2) {}
                    }
                } else {
                    Tag tag = this.value.m_6426_();
                    try {
                        OwnerNativeSemanticAuthority.targetCopied(targetObservation, this.value, tag);
                    }
                    catch (Throwable throwable) {
                        try {
                            OwnerNativeSemanticAuthority.targetFailed(targetObservation, throwable);
                        }
                        catch (Throwable throwable3) {
                            // empty catch block
                        }
                    }
                    Tag tag2 = this.data.m_128365_(this.key, tag);
                    try {
                        OwnerNativeSemanticAuthority.targetReturned(targetObservation, this.data, this.key, tag, tag2);
                    }
                    catch (Throwable throwable) {
                        try {
                            OwnerNativeSemanticAuthority.targetFailed(targetObservation, throwable);
                        }
                        catch (Throwable throwable4) {
                            // empty catch block
                        }
                    }
                }
                FoodWeaponHitPlan.timerSemanticMutation(finalEffectsCommit, this, true);
            }
            catch (Throwable throwable) {
                try {
                    try {
                        OwnerNativeSemanticAuthority.targetFailed(targetObservation, throwable);
                        throw throwable;
                    }
                    catch (Throwable throwable5) {
                        // empty catch block
                    }
                    throw throwable;
                }
                catch (Throwable throwable6) {
                    try {
                        OwnerNativeSemanticAuthority.targetClosed(targetObservation);
                        throw throwable6;
                    }
                    catch (Throwable throwable7) {
                        // empty catch block
                    }
                    throw throwable6;
                }
            }
            try {
                OwnerNativeSemanticAuthority.targetClosed(targetObservation);
                return;
            }
            catch (Throwable throwable) {
                return;
            }
        }
    }

    private static final class OutcomeTagLeaf
    extends TypedLeaf {
        private final CompoundTag data;
        private final String key;
        private final Tag value;

        private OutcomeTagLeaf(FoodWeaponSourceLedger.AttemptDomain attemptDomain, CompoundTag compoundTag, String string, Tag tag) {
            super(attemptDomain);
            this.data = compoundTag;
            this.key = string;
            this.value = tag == null ? null : tag.m_6426_();
        }

        @Override
        void apply(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            if (this.value == null) {
                this.data.m_128473_(this.key);
            } else {
                this.data.m_128365_(this.key, this.value.m_6426_());
            }
            FoodWeaponHitPlan.timerSemanticMutation(finalEffectsCommit, this, true);
        }
    }

    private static final class ChainLeaf
    extends TypedLeaf {
        private final ServerPlayer player;
        private final LivingEntity target;
        private final float damage;
        private final CompoundTag data;
        private final String key;
        private final long tick;

        private ChainLeaf(FoodWeaponSourceLedger.AttemptDomain attemptDomain, ServerPlayer serverPlayer, LivingEntity livingEntity, float f, CompoundTag compoundTag, String string, long l) {
            super(attemptDomain);
            this.player = serverPlayer;
            this.target = livingEntity;
            this.damage = f;
            this.data = compoundTag;
            this.key = string;
            this.tick = l;
        }

        @Override
        void apply(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            boolean bl = FoodWeaponEvolution.hurtPlanSecondary(this.player, this.target, this.damage, childHurtBridge);
            if (!bl || !FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
                if (this.key != null) {
                    FoodWeaponHitPlan.timerSemanticMutation(finalEffectsCommit, this, false);
                }
                return;
            }
            if (this.key != null) {
                this.data.m_128356_(this.key, this.tick);
                FoodWeaponHitPlan.timerSemanticMutation(finalEffectsCommit, this, true);
            }
            if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
                return;
            }
            ((ServerLevel)this.target.m_9236_()).m_8767_((ParticleOptions)ParticleTypes.f_175830_, this.target.m_20185_(), this.target.m_20186_() + (double)this.target.m_20206_() * 0.5, this.target.m_20189_(), 12, 0.22, 0.35, 0.22, 0.12);
        }
    }

    record TimerSemanticInstruction(Object leaf, int planOrdinal, FoodWeaponSourceLedger.AttemptDomain attempt, String key, FoodWeaponExpectedTrace.Value value, boolean conditional) {
    }

    record TimerSemanticProgram(Object leaf, Object weapon, Object profile, Object player, Object source, FoodWeaponExpectedTrace.Value baseline, FoodWeaponExpectedTrace.Value initialData, List<TimerSemanticInstruction> instructions, Throwable unsupported) {
        TimerSemanticProgram {
            list = List.copyOf(list);
        }
    }

    static final class FinalEffectsCommit {
        private FoodWeaponOriginalSequence.Session originalSequence;
        private final PreparedFinalEffects prepared;
        private final FoodWeaponFinalDamage.OffenseReceipt receipt;
        private final float amount;
        private final List<TypedLeaf> leaves;
        private List<TimerSemanticCursor> timerSemantics = List.of();
        private Throwable timerSemanticFailure;
        private TypedLeaf currentLeaf;
        private int currentLeafOrdinal = -1;
        private FoodWeaponSourceLedger.OwnerWriterPublication writerPublication;
        private boolean timerObservationIncomplete;
        private Object incompleteTimerLeaf;
        private int incompleteTimerStepOrdinal = -1;
        private FoodWeaponSourceLedger.OwnerWriterStep incompleteTimerStep;
        private FoodWeaponSourceLedger.OwnerWriterPublication incompleteTimerWriterPublication;
        private Throwable timerPlanningFailure;
        private Throwable timerEnteredFailure;
        private Throwable timerReturnedFailure;
        private Throwable timerCloseFailure;
        private Throwable timerExceptionObservationFailure;
        private Throwable timerEntryRecordingFailure;
        private Throwable timerNormalRecordingFailure;
        private Throwable timerExceptionRecordingFailure;
        private Throwable timerCloseRecordingFailure;
        private Throwable timerOriginalFailure;
        private boolean timerOriginalPutReturned;
        private CompoundTag timerOriginalReceiver;
        private Tag timerOriginalArgument;
        private Tag timerOriginalValue;
        private String timerOriginalKey;
        private int timerOriginalStepOrdinal = -1;
        private FoodWeaponSourceLedger.OwnerWriterPublication timerOriginalWriter;
        private boolean executed;
        private boolean stopped;
        private boolean originalStopped;

        private FinalEffectsCommit(PreparedFinalEffects preparedFinalEffects, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, float f, List<TypedLeaf> list) {
            this.prepared = preparedFinalEffects;
            this.receipt = offenseReceipt;
            this.amount = f;
            this.leaves = List.copyOf(list);
            try {
                TimerSemanticCatalogue timerSemanticCatalogue = preparedFinalEffects.plan.timerSemanticCatalogue;
                if (timerSemanticCatalogue.unsupported() != null) {
                    this.timerSemanticFailure = timerSemanticCatalogue.unsupported();
                    return;
                }
                this.timerSemantics = timerSemanticCatalogue.programs().stream().filter(timerSemanticProgram -> this.leaves.contains(timerSemanticProgram.leaf())).map(timerSemanticProgram -> new TimerSemanticCursor((TimerSemanticProgram)timerSemanticProgram, this.leaves)).toList();
            }
            catch (Throwable throwable) {
                this.timerSemanticFailure = throwable;
            }
        }
    }

    static final class PreparedFinalEffects {
        private final FoodWeaponHitPlan plan;
        private final FoodWeaponSourceLedger.AttemptBatchQuote batch;
        private final FoodWeaponSourceLedger.AttemptBatchAdmission admission;
        private final List<FoodWeaponSourceLedger.AttemptRequest> requests;
        private final List<FoodWeaponSourceLedger.TargetRequest> targets;
        private final ServerPlayer player;
        private final DamageSource source;
        private final long tick;

        private PreparedFinalEffects(FoodWeaponHitPlan foodWeaponHitPlan, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2, ServerPlayer serverPlayer, DamageSource damageSource) {
            this.plan = foodWeaponHitPlan;
            this.batch = attemptBatchQuote;
            this.admission = attemptBatchAdmission;
            this.requests = List.copyOf(list);
            this.targets = List.copyOf(list2);
            this.player = serverPlayer;
            this.source = damageSource;
            this.tick = FoodWeaponSourceLedger.clock(serverPlayer);
        }
    }

    record TimerSemanticChoice(TimerSemanticInstruction instruction, boolean selected, boolean applied) {
    }

    record TimerSemanticProjection(TimerSemanticProgram program, FoodWeaponExpectedTrace.Value localData, Map<String, FoodWeaponExpectedTrace.Value> writes, Map<String, Long> rangedLongs) {
        TimerSemanticProjection {
            map = Map.copyOf(map);
            map2 = Map.copyOf(map2);
        }
    }

    private static final class TimerSemanticCursor {
        private final TimerSemanticProgram program;
        private final List<TimerSemanticInstruction> instructions;
        private final Map<String, Integer> admittedKeys = new LinkedHashMap<String, Integer>();
        private FoodWeaponExpectedTrace.Value image;
        private TimerSemanticProjection projection;
        private TimerSemanticStep currentStep;
        private Thread thread;
        private int next;
        private int returnedHelpers;
        private boolean timerEntered;
        private boolean rangedActive;
        private boolean rangedEntered;
        private boolean timerReturned;
        private boolean closed;
        private Throwable originalFailure;
        private String status;

        private TimerSemanticCursor(TimerSemanticProgram timerSemanticProgram, List<TypedLeaf> list) {
            this.program = timerSemanticProgram;
            this.instructions = timerSemanticProgram.instructions().stream().filter(timerSemanticInstruction -> list.contains(timerSemanticInstruction.leaf())).toList();
            this.image = timerSemanticProgram.initialData();
            this.status = timerSemanticProgram.unsupported() == null ? "QUOTED" : "UNSUPPORTED_QUOTE";
        }
    }

    record TimerSemanticStep(TimerSemanticProjection projection, String key, FoodWeaponExpectedTrace.Value value, int originalStepOrdinal) {
    }

    private static final class RepairLeaf
    extends TypedLeaf {
        private final ItemStack weapon;
        private final int reduction;
        private final OriginalRepairFacts originalFacts;

        private RepairLeaf(FoodWeaponSourceLedger.AttemptDomain attemptDomain, ItemStack itemStack, int n) {
            super(attemptDomain);
            this.weapon = itemStack;
            this.reduction = n;
            this.originalFacts = new OriginalRepairFacts(itemStack, n);
        }

        @Override
        void apply(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            try {
                this.originalFacts.begin(finalEffectsCommit, childHurtBridge, this);
            }
            catch (Throwable throwable) {
                try {
                    this.originalFacts.observationFailed(throwable);
                }
                catch (Throwable throwable2) {
                    // empty catch block
                }
            }
            try {
                int n = this.weapon.m_41773_();
                int n2 = Math.max(0, n - this.reduction);
                try {
                    this.originalFacts.readReturned(n, n2);
                }
                catch (Throwable throwable) {
                    try {
                        this.originalFacts.observationFailed(throwable);
                    }
                    catch (Throwable throwable3) {
                        // empty catch block
                    }
                }
                try {
                    this.originalFacts.setterEntered();
                }
                catch (Throwable throwable) {
                    try {
                        this.originalFacts.observationFailed(throwable);
                    }
                    catch (Throwable throwable4) {
                        // empty catch block
                    }
                }
                this.weapon.m_41721_(n2);
                try {
                    this.originalFacts.setterReturned = true;
                }
                catch (Throwable throwable) {
                    try {
                        this.originalFacts.observationFailed(throwable);
                    }
                    catch (Throwable throwable5) {
                        // empty catch block
                    }
                }
            }
            catch (Throwable throwable) {
                this.originalFacts.originalFailure = throwable;
                throw throwable;
            }
            finally {
                this.originalFacts.closed = true;
            }
        }
    }

    static final class OriginalRepairFacts {
        private final ItemStack weapon;
        private final int reduction;
        private FinalEffectsCommit originalCommit;
        private FoodWeaponOriginalSequence.Session originalSequence;
        private Object originalLeaf;
        private Thread originalThread;
        private boolean entered;
        private boolean bound;
        private boolean readReturned;
        private int originalDamage;
        private int requestedDamage;
        private boolean setterEntered;
        private boolean setterReturned;
        private Throwable originalFailure;
        private Throwable observerFailure;
        private boolean closed;

        private OriginalRepairFacts(ItemStack itemStack, int n) {
            this.weapon = itemStack;
            this.reduction = n;
        }

        private void begin(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge, Object object) {
            if (this.entered) {
                return;
            }
            this.entered = true;
            this.originalCommit = finalEffectsCommit;
            this.originalLeaf = object;
            this.originalThread = Thread.currentThread();
            this.originalSequence = finalEffectsCommit == null ? null : finalEffectsCommit.originalSequence;
            this.bound = this.originalSequence != null && FoodWeaponFinalDamage.matchesOriginalHitPlanLeaf(finalEffectsCommit, childHurtBridge, object, finalEffectsCommit.currentLeafOrdinal);
        }

        private void readReturned(int n, int n2) {
            this.originalDamage = n;
            this.requestedDamage = n2;
            this.readReturned = true;
        }

        private void setterEntered() {
            this.setterEntered = true;
            if (this.bound) {
                this.originalSequence.interferenceObserved(this.originalSequence.lineage.ownerSelection, this.originalSequence.lineage.generation, null);
            }
        }

        private void observationFailed(Throwable throwable) {
            if (this.observerFailure == null) {
                this.observerFailure = throwable;
            }
            if (this.bound) {
                this.originalSequence.observationFailed(null, 0, throwable);
            }
        }

        ItemStack weapon() {
            return this.weapon;
        }

        int reduction() {
            return this.reduction;
        }

        boolean originalReadReturned() {
            return this.readReturned;
        }

        int originalDamage() {
            return this.originalDamage;
        }

        int requestedDamage() {
            return this.requestedDamage;
        }

        boolean originalSetterEntered() {
            return this.setterEntered;
        }

        boolean originalSetterReturned() {
            return this.setterReturned;
        }

        Throwable originalFailure() {
            return this.originalFailure;
        }

        Throwable observerFailure() {
            return this.observerFailure;
        }
    }

    private record Target(FoodWeaponSourceLedger.TargetRequest request, LivingEntity entity, Level level, Vec3 position, float health, float maxHealth) {
        private boolean closedRecipient(ServerPlayer serverPlayer) {
            return this.entity != serverPlayer && this.entity.m_6084_() && !this.entity.m_213877_() && this.entity.m_9236_() == this.level && this.level == serverPlayer.m_9236_() && this.entity.m_20148_().equals(this.request.target());
        }

        private boolean stable(ServerPlayer serverPlayer) {
            return this.entity != serverPlayer && this.entity.m_6084_() && !this.entity.m_213877_() && this.entity.m_9236_() == this.level && this.level == serverPlayer.m_9236_() && this.entity.m_20148_().equals(this.request.target()) && this.entity.m_20182_().equals((Object)this.position) && Float.compare(this.entity.m_21223_(), this.health) == 0 && Float.compare(this.entity.m_21233_(), this.maxHealth) == 0;
        }
    }

    public record Attempt(FoodWeaponSourceLedger.AttemptDomain effect, double chance, FoodWeaponSourceLedger.AttemptQuote outcome) {
    }

    static enum TimerObserverPhase {
        PLANNING,
        ENTERED,
        RETURNED,
        EXCEPTION,
        CLOSE;

    }

    private record Commit(FoodWeaponSourceLedger.AttemptDomain effect, Runnable action) {
    }

    record TimerSemanticResult(Object leaf, int resolvedInstructions, int admittedKeys, int returnedHelpers, boolean timerEntered, boolean rangedEntered, boolean timerReturned, boolean closed, String status, Throwable originalFailure, Throwable observationFailure) {
    }

    private static final class OverloadLeaf
    extends TypedLeaf {
        private final ServerPlayer player;
        private final LivingEntity origin;
        private final List<LivingEntity> victims;
        private final float damage;

        private OverloadLeaf(FoodWeaponSourceLedger.AttemptDomain attemptDomain, ServerPlayer serverPlayer, LivingEntity livingEntity, List<LivingEntity> list, float f) {
            super(attemptDomain);
            this.player = serverPlayer;
            this.origin = livingEntity;
            this.victims = List.copyOf(list);
            this.damage = f;
        }

        @Override
        void apply(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            FoodWeaponEvolution.applyPlanOverload(finalEffectsCommit, childHurtBridge, this.player, this.origin, this.victims, this.damage);
        }
    }

    private static final class PushLeaf
    extends TypedLeaf {
        private final LivingEntity entity;
        private final Vec3 origin;
        private final double strength;

        private PushLeaf(FoodWeaponSourceLedger.AttemptDomain attemptDomain, LivingEntity livingEntity, Vec3 vec3, double d) {
            super(attemptDomain);
            this.entity = livingEntity;
            this.origin = vec3;
            this.strength = d;
        }

        @Override
        void apply(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            Vec3 vec3 = this.entity.m_20182_().m_82546_(this.origin).m_82542_(1.0, 0.0, 1.0).m_82541_();
            double d = Math.max(0.0, Math.min(1.0, this.entity.m_21133_(Attributes.f_22278_)));
            double d2 = this.strength * (1.0 - d);
            if (vec3.m_82556_() < 1.0E-6 || d2 <= 0.0) {
                return;
            }
            this.entity.m_5997_(vec3.f_82479_ * d2, 0.18 * (1.0 - d), vec3.f_82481_ * d2);
            if (FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
                this.entity.f_19864_ = true;
            }
        }
    }

    private static final class MaterialPowersLeaf
    extends TypedLeaf {
        private final LivingHurtEvent event;
        private final ServerPlayer player;
        private final List<FoodWeaponBuildRules.Material> materials;
        private final List<TamableAnimal> potentialPets;
        private final List<Target> petRecipients;
        private final Set<TamableAnimal> potentialPetIdentities;
        private final Level petLevel;

        private MaterialPowersLeaf(LivingHurtEvent livingHurtEvent, ServerPlayer serverPlayer, List<FoodWeaponBuildRules.Material> list, List<TamableAnimal> list2, List<Target> list3) {
            super(null);
            this.event = livingHurtEvent;
            this.player = serverPlayer;
            this.materials = List.copyOf(list);
            this.potentialPets = List.copyOf(list2);
            this.petRecipients = List.copyOf(list3);
            Set set = Collections.newSetFromMap(new IdentityHashMap());
            set.addAll(this.potentialPets);
            this.potentialPetIdentities = Collections.unmodifiableSet(set);
            this.petLevel = serverPlayer.m_9236_();
        }

        @Override
        boolean recipientClosureComplete() {
            if (!this.materials.contains((Object)FoodWeaponBuildRules.Material.BOSS_SHEPHERD)) {
                return this.potentialPets.isEmpty() && this.petRecipients.isEmpty();
            }
            return this.player.m_9236_() == this.petLevel && this.event.getEntity().m_9236_() == this.petLevel && this.petRecipients.size() == this.potentialPets.size() && this.potentialPetIdentities.size() == this.potentialPets.size() && this.petRecipients.stream().allMatch(target -> target.closedRecipient(this.player) && target.request().domain() == FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE) && WeaponMaterialPowers.matchesPetRecipientClosure(this.player, this.potentialPetIdentities);
        }

        @Override
        void apply(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            WeaponMaterialPowers.applyClosedHitSteps(this.event, this.player, this.materials, this.potentialPets, finalEffectsCommit, childHurtBridge);
        }
    }

    private static final class MaterialBaseProcLeaf
    extends TypedLeaf {
        private final ServerPlayer player;
        private final LivingEntity target;
        private final List<FoodWeaponBuildRules.Material> materials;

        private MaterialBaseProcLeaf(ServerPlayer serverPlayer, LivingEntity livingEntity, List<FoodWeaponBuildRules.Material> list) {
            super(null);
            this.player = serverPlayer;
            this.target = livingEntity;
            this.materials = List.copyOf(list);
        }

        @Override
        void apply(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            WeaponMaterialBuild.applyClosedBaseProc(this.player, this.target, this.materials, finalEffectsCommit, childHurtBridge);
        }
    }

    private static final class MaterialKnockbackLeaf
    extends TypedLeaf {
        private final ServerPlayer player;
        private final LivingEntity target;

        private MaterialKnockbackLeaf(ServerPlayer serverPlayer, LivingEntity livingEntity) {
            super(null);
            this.player = serverPlayer;
            this.target = livingEntity;
        }

        @Override
        void apply(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            this.target.m_147240_(0.2, this.player.m_20185_() - this.target.m_20185_(), this.player.m_20189_() - this.target.m_20189_());
        }
    }

    private static final class KnockbackLeaf
    extends TypedLeaf {
        private final LivingEntity entity;
        private final double strength;
        private final double horizontal;
        private final double vertical;

        private KnockbackLeaf(FoodWeaponSourceLedger.AttemptDomain attemptDomain, LivingEntity livingEntity, double d, double d2, double d3) {
            super(attemptDomain);
            this.entity = livingEntity;
            this.strength = d;
            this.horizontal = d2;
            this.vertical = d3;
        }

        @Override
        void apply(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            this.entity.m_147240_(this.strength, this.horizontal, this.vertical);
        }
    }

    private static final class FireLeaf
    extends TypedLeaf {
        private final LivingEntity entity;
        private final int seconds;

        private FireLeaf(FoodWeaponSourceLedger.AttemptDomain attemptDomain, LivingEntity livingEntity, int n) {
            super(attemptDomain);
            this.entity = livingEntity;
            this.seconds = n;
        }

        @Override
        void apply(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            this.entity.m_20254_(this.seconds);
        }
    }

    private static final class HealLeaf
    extends TypedLeaf {
        private final LivingEntity entity;
        private final float amount;

        private HealLeaf(FoodWeaponSourceLedger.AttemptDomain attemptDomain, LivingEntity livingEntity, float f) {
            super(attemptDomain);
            this.entity = livingEntity;
            this.amount = f;
        }

        @Override
        void apply(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            this.entity.m_5634_(this.amount);
        }
    }

    private static final class EffectLeaf
    extends TypedLeaf {
        private final LivingEntity entity;
        private final MobEffectInstance effect;
        private final ServerPlayer player;

        private EffectLeaf(FoodWeaponSourceLedger.AttemptDomain attemptDomain, LivingEntity livingEntity, MobEffectInstance mobEffectInstance, ServerPlayer serverPlayer) {
            super(attemptDomain);
            this.entity = livingEntity;
            this.effect = new MobEffectInstance(mobEffectInstance);
            this.player = serverPlayer;
        }

        @Override
        void apply(FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            this.entity.m_147207_(new MobEffectInstance(this.effect), (Entity)this.player);
        }
    }

    public static final class Builder {
        private float amount;
        private boolean primaryPaymentRequired;
        private final Map<FoodWeaponSourceLedger.AttemptDomain, Attempt> attempts = new EnumMap<FoodWeaponSourceLedger.AttemptDomain, Attempt>(FoodWeaponSourceLedger.AttemptDomain.class);
        private final Map<FoodWeaponSourceLedger.TargetRequest, Target> targets = new LinkedHashMap<FoodWeaponSourceLedger.TargetRequest, Target>();
        private final List<Commit> commits = new ArrayList<Commit>();
        private final List<TypedLeaf> typedLeaves = new ArrayList<TypedLeaf>();

        public Builder(float f) {
            this.amount = f;
        }

        public float amount() {
            return this.amount;
        }

        public void requirePrimaryPayment() {
            this.primaryPaymentRequired = true;
        }

        public void multiply(float f) {
            this.primaryPaymentRequired = true;
            this.amount *= f;
        }

        public void add(float f) {
            this.primaryPaymentRequired = true;
            this.amount += f;
        }

        void effect(FoodWeaponSourceLedger.AttemptDomain attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain targetEffectDomain, LivingEntity livingEntity, MobEffectInstance mobEffectInstance, ServerPlayer serverPlayer) {
            this.typed(attemptDomain, targetEffectDomain, livingEntity, new EffectLeaf(attemptDomain, livingEntity, mobEffectInstance, serverPlayer));
        }

        void heal(FoodWeaponSourceLedger.AttemptDomain attemptDomain, LivingEntity livingEntity, float f) {
            this.typed(attemptDomain, null, null, new HealLeaf(attemptDomain, livingEntity, f));
        }

        void fire(FoodWeaponSourceLedger.AttemptDomain attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain targetEffectDomain, LivingEntity livingEntity, int n) {
            this.typed(attemptDomain, targetEffectDomain, livingEntity, new FireLeaf(attemptDomain, livingEntity, n));
        }

        void materialKnockback(ServerPlayer serverPlayer, LivingEntity livingEntity) {
            this.typed(null, FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE, livingEntity, new MaterialKnockbackLeaf(serverPlayer, livingEntity));
        }

        void materialBaseProc(ServerPlayer serverPlayer, LivingEntity livingEntity, List<FoodWeaponBuildRules.Material> list) {
            this.typed(null, FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE, livingEntity, new MaterialBaseProcLeaf(serverPlayer, livingEntity, list));
        }

        void materialPowers(LivingHurtEvent livingHurtEvent, ServerPlayer serverPlayer, List<FoodWeaponBuildRules.Material> list, List<TamableAnimal> list2) {
            this.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE, livingHurtEvent.getEntity());
            ArrayList<Target> arrayList = new ArrayList<Target>();
            for (TamableAnimal tamableAnimal : list2) {
                this.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE, (LivingEntity)tamableAnimal);
                arrayList.add(this.targets.get(new FoodWeaponSourceLedger.TargetRequest(tamableAnimal.m_20148_(), FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE)));
            }
            this.typed(null, null, null, new MaterialPowersLeaf(livingHurtEvent, serverPlayer, list, list2, arrayList));
        }

        void knockback(FoodWeaponSourceLedger.AttemptDomain attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain targetEffectDomain, LivingEntity livingEntity, double d, double d2, double d3) {
            this.typed(attemptDomain, targetEffectDomain, livingEntity, new KnockbackLeaf(attemptDomain, livingEntity, d, d2, d3));
        }

        void write(FoodWeaponSourceLedger.AttemptDomain attemptDomain, CompoundTag compoundTag, String string, Tag tag) {
            TagLeaf tagLeaf = new TagLeaf(attemptDomain, compoundTag, string, tag);
            try {
                tagLeaf.targetWrite = OwnerNativeSemanticAuthority.targetWriteConstructed(this, tagLeaf, attemptDomain, compoundTag, string, tag, tagLeaf.value);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.typed(attemptDomain, null, null, tagLeaf);
        }

        void writeAfterWorld(FoodWeaponSourceLedger.AttemptDomain attemptDomain, CompoundTag compoundTag, String string, Tag tag) {
            this.typed(attemptDomain, null, null, new OutcomeTagLeaf(attemptDomain, compoundTag, string, tag));
        }

        void pushAway(FoodWeaponSourceLedger.AttemptDomain attemptDomain, LivingEntity livingEntity, Vec3 vec3, double d) {
            this.typed(attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_KNOCKBACK, livingEntity, new PushLeaf(attemptDomain, livingEntity, vec3, d));
        }

        void repair(FoodWeaponSourceLedger.AttemptDomain attemptDomain, ItemStack itemStack, int n) {
            this.typed(attemptDomain, null, null, new RepairLeaf(attemptDomain, itemStack, n));
        }

        void saveHitTimers(ItemStack itemStack, FoodWeaponEvolution.Profile profile, ServerPlayer serverPlayer, DamageSource damageSource, CompoundTag compoundTag, CompoundTag compoundTag2) {
            this.typed(null, null, null, new TimerSaveLeaf(itemStack, profile, serverPlayer, damageSource, compoundTag, compoundTag2));
        }

        void chain(FoodWeaponSourceLedger.AttemptDomain attemptDomain, ServerPlayer serverPlayer, LivingEntity livingEntity, float f, CompoundTag compoundTag, String string, long l) {
            this.typed(attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_DAMAGE, livingEntity, new ChainLeaf(attemptDomain, serverPlayer, livingEntity, f, compoundTag, string, l));
        }

        void overload(FoodWeaponSourceLedger.AttemptDomain attemptDomain, ServerPlayer serverPlayer, LivingEntity livingEntity, List<LivingEntity> list, float f) {
            for (LivingEntity livingEntity2 : list) {
                this.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_DAMAGE, livingEntity2);
            }
            this.typed(attemptDomain, null, null, new OverloadLeaf(attemptDomain, serverPlayer, livingEntity, list, f));
        }

        private void typed(FoodWeaponSourceLedger.AttemptDomain attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain targetEffectDomain, LivingEntity livingEntity, TypedLeaf typedLeaf) {
            if (!(attemptDomain == null || this.attempts.get((Object)attemptDomain) != null && this.attempts.get((Object)attemptDomain).outcome().success())) {
                throw new IllegalStateException("No original successful typed attempt");
            }
            if (targetEffectDomain != null) {
                this.requireTarget(targetEffectDomain, livingEntity);
            }
            this.typedLeaves.add(typedLeaf);
        }

        public void defer(Runnable runnable) {
            this.commits.add(new Commit(null, runnable));
        }

        public void requireTarget(FoodWeaponSourceLedger.TargetEffectDomain targetEffectDomain, LivingEntity livingEntity) {
            if (targetEffectDomain == null || livingEntity == null || !livingEntity.m_6084_() || livingEntity.m_213877_() || livingEntity.m_20148_() == null) {
                throw new IllegalArgumentException("A live exact target and typed target domain are required");
            }
            FoodWeaponSourceLedger.TargetRequest targetRequest = new FoodWeaponSourceLedger.TargetRequest(livingEntity.m_20148_(), targetEffectDomain);
            Target target = this.targets.get(targetRequest);
            if (target != null && target.entity() != livingEntity) {
                throw new IllegalArgumentException("One target identity cannot refer to two entity objects");
            }
            if (target == null) {
                this.targets.put(targetRequest, new Target(targetRequest, livingEntity, livingEntity.m_9236_(), livingEntity.m_20182_(), livingEntity.m_21223_(), livingEntity.m_21233_()));
            }
        }

        public void deferTarget(FoodWeaponSourceLedger.TargetEffectDomain targetEffectDomain, LivingEntity livingEntity, Runnable runnable) {
            if (runnable == null) {
                throw new IllegalArgumentException("Target effect is required");
            }
            this.requireTarget(targetEffectDomain, livingEntity);
            this.defer(runnable);
        }

        public void deferTargetRandom(FoodWeaponSourceLedger.AttemptDomain attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain targetEffectDomain, LivingEntity livingEntity, Runnable runnable) {
            Attempt attempt = this.attempts.get((Object)attemptDomain);
            if (attempt == null || !attempt.outcome().success()) {
                throw new IllegalStateException("No successful quote");
            }
            if (runnable == null) {
                throw new IllegalArgumentException("Target effect is required");
            }
            this.requireTarget(targetEffectDomain, livingEntity);
            this.deferRandom(attemptDomain, runnable);
        }

        public boolean roll(FoodWeaponSourceLedger.AttemptDomain attemptDomain, double d, RandomQuotes randomQuotes) {
            if (randomQuotes == null || !Double.isFinite(d) || d < 0.0 || d > 1.0 || this.attempts.containsKey((Object)attemptDomain)) {
                return false;
            }
            FoodWeaponSourceLedger.AttemptQuote attemptQuote = randomQuotes.quote(attemptDomain, d);
            if (attemptQuote == null || attemptQuote.status() != FoodWeaponSourceLedger.AttemptStatus.AVAILABLE || attemptQuote.domain() != attemptDomain || Double.compare(attemptQuote.chance(), d) != 0) {
                return false;
            }
            this.attempts.put(attemptDomain, new Attempt(attemptDomain, d, attemptQuote));
            return attemptQuote.success();
        }

        public void deferRandom(FoodWeaponSourceLedger.AttemptDomain attemptDomain, Runnable runnable) {
            Attempt attempt = this.attempts.get((Object)attemptDomain);
            if (attempt == null || !attempt.outcome().success()) {
                throw new IllegalStateException("No successful quote");
            }
            this.commits.add(new Commit(attemptDomain, runnable));
        }

        public FoodWeaponHitPlan build() {
            return new FoodWeaponHitPlan(this.amount, this.primaryPaymentRequired, List.copyOf(this.attempts.values()), List.copyOf(this.targets.values()), List.copyOf(this.commits), List.copyOf(this.typedLeaves));
        }
    }

    @FunctionalInterface
    public static interface RandomQuotes {
        public FoodWeaponSourceLedger.AttemptQuote quote(FoodWeaponSourceLedger.AttemptDomain var1, double var2);
    }
}

