package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.Profile;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Kind;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;

public final class FoodWeaponHitPlan {
    private final float amount;
    private final boolean primaryPaymentRequired;
    private final List<FoodWeaponHitPlan.Attempt> attempts;
    private final List<FoodWeaponHitPlan.Target> targets;
    private final List<FoodWeaponHitPlan.Commit> commits;
    private final List<FoodWeaponHitPlan.TypedLeaf> typedLeaves;
    private final FoodWeaponHitPlan.TimerSemanticCatalogue timerSemanticCatalogue;
    private boolean committed;

    private FoodWeaponHitPlan(
        float var1,
        boolean var2,
        List<FoodWeaponHitPlan.Attempt> var3,
        List<FoodWeaponHitPlan.Target> var4,
        List<FoodWeaponHitPlan.Commit> var5,
        List<FoodWeaponHitPlan.TypedLeaf> var6
    ) {
        this.amount = var1;
        this.primaryPaymentRequired = var2;
        this.attempts = var3;
        this.targets = var4;
        this.commits = var5;
        this.typedLeaves = var6;
        this.timerSemanticCatalogue = compileTimerSemantics(var6);
    }

    public float amount() {
        return this.amount;
    }

    public boolean primaryPaymentRequired() {
        return this.primaryPaymentRequired;
    }

    public List<FoodWeaponHitPlan.Attempt> attempts() {
        return this.attempts;
    }

    public List<FoodWeaponSourceLedger.TargetRequest> targets() {
        return this.targets.stream().map(FoodWeaponHitPlan.Target::request).toList();
    }

    public boolean matchesContext(ServerPlayer var1) {
        return var1 != null && !this.committed && this.targets.stream().allMatch(var1x -> var1x.stable(var1));
    }

    private static FoodWeaponHitPlan.TimerSemanticSeed timerSemanticSeed(CompoundTag var0, CompoundTag var1) {
        try {
            Value var2 = NativeExpectedRawFields.observe(var0);
            Value var3 = NativeExpectedRawFields.observe(var1);
            FoodWeaponExpectedTrace.compoundKeys(var2);
            FoodWeaponExpectedTrace.compoundKeys(var3);
            return new FoodWeaponHitPlan.TimerSemanticSeed(var2, var3, null);
        } catch (Throwable var4) {
            return new FoodWeaponHitPlan.TimerSemanticSeed(null, null, var4);
        }
    }

    private static FoodWeaponHitPlan.TimerSemanticCatalogue compileTimerSemantics(List<FoodWeaponHitPlan.TypedLeaf> var0) {
        try {
            ArrayList<FoodWeaponHitPlan.TimerSemanticProgram> var1 = new ArrayList<>();

            for (int var2 = 0; var2 < var0.size(); var2++) {
                FoodWeaponHitPlan.TypedLeaf candidate = var0.get(var2);
                if (candidate instanceof FoodWeaponHitPlan.TimerSaveLeaf var3) {
                    ArrayList<FoodWeaponHitPlan.TimerSemanticInstruction> var4 = new ArrayList<>();
                    Throwable var5 = var3.semanticSeed.unsupported();

                    try {
                        for (int var6 = 0; var6 < var2; var6++) {
                            FoodWeaponHitPlan.TypedLeaf var7;
                            String var8;
                            Value var9;
                            boolean var10;
                            label78: {
                                var7 = (FoodWeaponHitPlan.TypedLeaf)var0.get(var6);
                                var10 = false;
                                if (var7 instanceof FoodWeaponHitPlan.TagLeaf var12 && var12.data == var3.data) {
                                    var8 = var12.key;
                                    var9 = NativeExpectedRawFields.observe(var12.value);
                                    break label78;
                                }

                                if (var7 instanceof FoodWeaponHitPlan.OutcomeTagLeaf var13 && var13.data == var3.data) {
                                    var8 = var13.key;
                                    var9 = NativeExpectedRawFields.observe(var13.value);
                                    break label78;
                                }

                                if (!(var7 instanceof FoodWeaponHitPlan.ChainLeaf var11) || var11.data != var3.data || var11.key == null) {
                                    continue;
                                }

                                var8 = var11.key;
                                var9 = FoodWeaponExpectedTrace.number(4, var11.tick);
                                var10 = true;
                            }

                            if (var8 == null) {
                                throw new IllegalArgumentException("Unsupported null timer semantic key");
                            }

                            var4.add(new FoodWeaponHitPlan.TimerSemanticInstruction(var7, var6, var7.attempt, var8, var9, var10));
                        }
                    } catch (Throwable var14) {
                        if (var5 == null) {
                            var5 = var14;
                        }
                    }

                    var1.add(
                        new FoodWeaponHitPlan.TimerSemanticProgram(
                            var3, var3.weapon, var3.profile, var3.player, var3.source, var3.semanticSeed.baseline(), var3.semanticSeed.data(), var4, var5
                        )
                    );
                }
            }

            return new FoodWeaponHitPlan.TimerSemanticCatalogue(var1, null);
        } catch (Throwable var15) {
            return new FoodWeaponHitPlan.TimerSemanticCatalogue(List.of(), var15);
        }
    }

    static OwnerNativeSemanticAuthority.TargetWrite ownerTargetWrite(FoodWeaponHitPlan var0, Object var1) {
        boolean var2 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerNativeSemanticAuthority.class && var0xx.getMethodName().equals("reserveTarget"))
                        .orElse(false)
            );
        if (var2 && var0 != null && var1 instanceof FoodWeaponHitPlan.TagLeaf var3 && var3.targetWrite != null) {
            int var4 = 0;

            for (FoodWeaponHitPlan.TypedLeaf var6 : var0.typedLeaves) {
                if (var6 == var1) {
                    var4++;
                }
            }

            return var4 == 1 ? var3.targetWrite : null;
        }

        return null;
    }

    static OwnerNativeSemanticAuthority.TargetWrite ownerTargetWriteAtOriginal(
        FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1, Object var2
    ) {
        boolean var3 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerNativeSemanticAuthority.class && var0xx.getMethodName().equals("targetLive"))
                        .orElse(false)
            );
        if (var3
            && var0 != null
            && var2 instanceof FoodWeaponHitPlan.TagLeaf var4
            && var4.targetWrite != null
            && var0.currentLeaf == var2
            && var0.executed
            && !var0.originalStopped
            && var0.originalSequence != null
            && FoodWeaponFinalDamage.originalHitPlanSequence(var0, var1) == var0.originalSequence
            && var0.originalSequence.original.atLeaf(var2)
            && var0.leaves.contains(var4)
            && var0.prepared.plan.typedLeaves.contains(var4)
            && OwnerNativeSemanticAuthority.targetPlan(var4.targetWrite, var0.prepared.plan)) {
            return var4.targetWrite;
        }

        return null;
    }

    static FoodWeaponHitPlan.TimerSemanticCatalogue timerSemanticCatalogue(FoodWeaponHitPlan var0) {
        return var0 == null ? null : var0.timerSemanticCatalogue;
    }

    static FoodWeaponHitPlan.TimerSemanticProjection timerSemanticQuoteProjection(
        FoodWeaponHitPlan.TimerSemanticProgram var0, List<FoodWeaponHitPlan.TimerSemanticChoice> var1
    ) {
        if (var0 != null && var0.unsupported() == null && var1 != null && var1.size() == var0.instructions().size()) {
            Value var2 = var0.initialData();

            for (int var3 = 0; var3 < var1.size(); var3++) {
                FoodWeaponHitPlan.TimerSemanticChoice var4 = (FoodWeaponHitPlan.TimerSemanticChoice)var1.get(var3);
                FoodWeaponHitPlan.TimerSemanticInstruction var5 = var0.instructions().get(var3);
                if (var4 == null
                    || var4.instruction() != var5
                    || !var4.selected() && var4.applied()
                    || var4.selected() && !var5.conditional() && !var4.applied()) {
                    throw new IllegalArgumentException("Timer choice does not match quoted instruction");
                }

                if (var4.applied()) {
                    var2 = var5.value() == null
                        ? FoodWeaponExpectedTrace.remove(var2, var5.key())
                        : FoodWeaponExpectedTrace.put(var2, var5.key(), var5.value());
                }
            }

            return timerSemanticProject(var0, var2);
        } else {
            throw new IllegalArgumentException("Expected complete quoted timer choices");
        }
    }

    private static FoodWeaponHitPlan.TimerSemanticProjection timerSemanticProject(FoodWeaponHitPlan.TimerSemanticProgram var0, Value var1) {
        LinkedHashMap var2 = new LinkedHashMap();
        LinkedHashMap var3 = new LinkedHashMap();

        for (String var7 : FoodWeaponExpectedTrace.compoundKeys(var1)) {
            Value var8 = FoodWeaponExpectedTrace.field(var1, var7);
            if (!FoodWeaponExpectedTrace.same(var8, FoodWeaponExpectedTrace.field(var0.baseline(), var7))) {
                var2.put(var7, var8);
            }

            if (var7.startsWith("Last") && var8.kind == 4) {
                var3.put(var7, var8.bits);
            }
        }

        return new FoodWeaponHitPlan.TimerSemanticProjection(var0, var1, var2, var3);
    }

    private static void timerSemanticStart(FoodWeaponHitPlan.FinalEffectsCommit var0) {
        if (var0 != null) {
            try {
                for (FoodWeaponHitPlan.TimerSemanticCursor var2 : var0.timerSemantics) {
                    if (var2.thread != null || var2.closed) {
                        throw new IllegalStateException("Timer semantic cursor reused");
                    }

                    var2.thread = Thread.currentThread();
                }
            } catch (Throwable var3) {
                if (var0.timerSemanticFailure == null) {
                    var0.timerSemanticFailure = var3;
                }
            }
        }
    }

    private static void timerSemanticMutation(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponHitPlan.TypedLeaf var1, boolean var2) {
        if (var0 != null) {
            try {
                for (FoodWeaponHitPlan.TimerSemanticCursor var4 : var0.timerSemantics) {
                    if (var4.program.unsupported() == null && !var4.closed && var4.next != var4.instructions.size()) {
                        FoodWeaponHitPlan.TimerSemanticInstruction var5 = var4.instructions.get(var4.next);
                        if (var5.leaf() == var1) {
                            if (var4.thread != Thread.currentThread() || var0.currentLeaf != var1 || var4.timerEntered || !var2 && !var5.conditional()) {
                                throw new IllegalStateException("Timer semantic mutation outside original leaf");
                            }

                            if (var2) {
                                var4.image = var5.value() == null
                                    ? FoodWeaponExpectedTrace.remove(var4.image, var5.key())
                                    : FoodWeaponExpectedTrace.put(var4.image, var5.key(), var5.value());
                            }

                            var4.next++;
                            var4.status = "SEMANTIC_PROGRESS";
                        }
                    }
                }
            } catch (Throwable var6) {
                if (var0.timerSemanticFailure == null) {
                    var0.timerSemanticFailure = var6;
                }
            }
        }
    }

    private static void timerSemanticLeafReturned(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponHitPlan.TypedLeaf var1) {
        if (var0 != null) {
            try {
                for (FoodWeaponHitPlan.TimerSemanticCursor var3 : var0.timerSemantics) {
                    if (var3.program.unsupported() == null && var3.next < var3.instructions.size() && var3.instructions.get(var3.next).leaf() == var1) {
                        throw new IllegalStateException("Missing original timer semantic branch result");
                    }
                }
            } catch (Throwable var4) {
                if (var0.timerSemanticFailure == null) {
                    var0.timerSemanticFailure = var4;
                }
            }
        }
    }

    private static FoodWeaponHitPlan.TimerSemanticCursor timerSemanticCursor(FoodWeaponHitPlan.FinalEffectsCommit var0, Object var1) {
        for (FoodWeaponHitPlan.TimerSemanticCursor var3 : var0.timerSemantics) {
            if (var3.program.leaf() == var1) {
                return var3;
            }
        }

        return null;
    }

    private static void timerSemanticTimerBegins(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponHitPlan.TimerSaveLeaf var1) {
        if (var0 != null) {
            try {
                FoodWeaponHitPlan.TimerSemanticCursor var2 = timerSemanticCursor(var0, var1);
                if (var2 == null || var2.program.unsupported() != null) {
                    return;
                }

                if (var2.thread != Thread.currentThread()
                    || var2.closed
                    || var2.timerEntered
                    || var2.next != var2.instructions.size()
                    || var0.currentLeaf != var1) {
                    throw new IllegalStateException("Timer semantic projection before original branches resolved");
                }

                var2.projection = timerSemanticProject(var2.program, var2.image);
                var2.timerEntered = true;
                var2.status = "PROJECTED_FROM_QUOTE_AND_ORIGINAL_BRANCHES";
            } catch (Throwable var3) {
                if (var0.timerSemanticFailure == null) {
                    var0.timerSemanticFailure = var3;
                }
            }
        }
    }

    private static void timerSemanticOriginalBegins(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponHitPlan.TimerSaveLeaf var1, String var2, int var3) {
        if (var0 != null) {
            try {
                FoodWeaponHitPlan.TimerSemanticCursor var4 = timerSemanticCursor(var0, var1);
                if (var4 == null || var4.program.unsupported() != null) {
                    return;
                }

                if (!var4.timerEntered
                    || var4.closed
                    || var4.currentStep != null
                    || var3 != var4.admittedKeys.size()
                    || !var4.projection.writes().containsKey(var2)
                    || var4.admittedKeys.containsKey(var2)) {
                    throw new IllegalStateException("Original timer key not in independently progressed semantic bag");
                }

                var4.admittedKeys.put(var2, var3);
                var4.currentStep = new FoodWeaponHitPlan.TimerSemanticStep(var4.projection, var2, var4.projection.writes().get(var2), var3);
            } catch (Throwable var5) {
                if (var0.timerSemanticFailure == null) {
                    var0.timerSemanticFailure = var5;
                }
            }
        }
    }

    private static void timerSemanticOriginalReturned(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponHitPlan.TimerSaveLeaf var1) {
        if (var0 != null) {
            try {
                FoodWeaponHitPlan.TimerSemanticCursor var2 = timerSemanticCursor(var0, var1);
                if (var2 == null || var2.program.unsupported() != null) {
                    return;
                }

                if (var2.currentStep == null) {
                    throw new IllegalStateException("Original timer helper returned without admitted key");
                }

                var2.returnedHelpers++;
            } catch (Throwable var3) {
                if (var0.timerSemanticFailure == null) {
                    var0.timerSemanticFailure = var3;
                }
            }
        }
    }

    private static void timerSemanticOriginalClosed(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponHitPlan.TimerSaveLeaf var1) {
        if (var0 != null) {
            try {
                FoodWeaponHitPlan.TimerSemanticCursor var2 = timerSemanticCursor(var0, var1);
                if (var2 != null) {
                    var2.currentStep = null;
                }
            } catch (Throwable var3) {
                if (var0.timerSemanticFailure == null) {
                    var0.timerSemanticFailure = var3;
                }
            }
        }
    }

    private static void timerSemanticRanged(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponHitPlan.TimerSaveLeaf var1, boolean var2) {
        if (var0 != null) {
            try {
                FoodWeaponHitPlan.TimerSemanticCursor var3 = timerSemanticCursor(var0, var1);
                if (var3 == null || var3.program.unsupported() != null) {
                    return;
                }

                if (var2 && (var3.projection == null || var3.projection.writes().isEmpty() || var3.returnedHelpers != var3.projection.writes().size())) {
                    throw new IllegalStateException("Original ranged save before complete semantic timer helper prefix");
                }

                var3.rangedActive = var2;
                if (var2) {
                    var3.rangedEntered = true;
                }
            } catch (Throwable var4) {
                if (var0.timerSemanticFailure == null) {
                    var0.timerSemanticFailure = var4;
                }
            }
        }
    }

    private static void timerSemanticTimerReturned(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponHitPlan.TimerSaveLeaf var1, boolean var2) {
        if (var0 != null) {
            try {
                FoodWeaponHitPlan.TimerSemanticCursor var3 = timerSemanticCursor(var0, var1);
                if (var3 == null || var3.program.unsupported() != null) {
                    return;
                }

                if (var3.projection == null
                    || var3.admittedKeys.size() != var3.projection.writes().size()
                    || var3.returnedHelpers != var3.projection.writes().size()
                    || var2 != !var3.projection.writes().isEmpty()) {
                    throw new IllegalStateException("Original timer traversal differs from quoted semantic key bag");
                }

                var3.timerReturned = true;
                var3.status = "ORIGINAL_HELPERS_RETURNED_NOT_FIELD_AUTHORITY";
            } catch (Throwable var4) {
                if (var0.timerSemanticFailure == null) {
                    var0.timerSemanticFailure = var4;
                }
            }
        }
    }

    private static boolean timerSemanticFrame(FoodWeaponHitPlan.FinalEffectsCommit var0, Object var1, FoodWeaponSourceLedger.OwnerWriterPublication var2) {
        if (var0 != null
            && var1 != null
            && var2 != null
            && var0.timerSemanticFailure == null
            && var0.executed
            && !var0.stopped
            && !var0.originalStopped
            && var0.currentLeaf == var1
            && var0.writerPublication == var2
            && var0.originalSequence != null
            && var0.originalSequence.original.atLeaf(var1)) {
            FoodWeaponHitPlan.TimerSemanticCursor var3 = timerSemanticCursor(var0, var1);
            return var3 != null && var3.program.unsupported() == null && !var3.closed && var3.thread == Thread.currentThread() && var3.timerEntered;
        } else {
            return false;
        }
    }

    static FoodWeaponHitPlan.TimerSemanticStep timerSemanticAtOriginal(
        FoodWeaponHitPlan.FinalEffectsCommit var0, Object var1, FoodWeaponSourceLedger.OwnerWriterPublication var2, int var3, String var4
    ) {
        if (timerSemanticFrame(var0, var1, var2)
            && var1 instanceof FoodWeaponHitPlan.TimerSaveLeaf var5
            && var5.currentOriginalStepOrdinal == var3
            && Objects.equals(var5.currentOriginalKey, var4)) {
            FoodWeaponHitPlan.TimerSemanticStep var6 = timerSemanticCursor(var0, var1).currentStep;
            return var6 != null && var6.originalStepOrdinal() == var3 && var6.key().equals(var4) ? var6 : null;
        }

        return null;
    }

    static FoodWeaponHitPlan.TimerSemanticProjection timerSemanticAtRanged(
        FoodWeaponHitPlan.FinalEffectsCommit var0, Object var1, FoodWeaponSourceLedger.OwnerWriterPublication var2
    ) {
        if (!timerSemanticFrame(var0, var1, var2)) {
            return null;
        } else {
            FoodWeaponHitPlan.TimerSemanticCursor var3 = timerSemanticCursor(var0, var1);
            return var3.rangedActive ? var3.projection : null;
        }
    }

    static FoodWeaponHitPlan.TimerSemanticStep timerSemanticAtSessionOriginal(FoodWeaponOriginalSequence.Session var0, Object var1, String var2) {
        if (var0 != null && var1 instanceof FoodWeaponHitPlan.TimerSaveLeaf var3) {
            FoodWeaponHitPlan.FinalEffectsCommit var4 = var3.currentSemanticCommit;
            return var4 != null && var4.originalSequence == var0
                ? timerSemanticAtOriginal(var4, var1, var4.writerPublication, var3.currentOriginalStepOrdinal, var2)
                : null;
        } else {
            return null;
        }
    }

    static FoodWeaponHitPlan.TimerSemanticProjection timerSemanticAtSessionRanged(FoodWeaponOriginalSequence.Session var0, Object var1) {
        if (var0 != null && var1 instanceof FoodWeaponHitPlan.TimerSaveLeaf var2) {
            FoodWeaponHitPlan.FinalEffectsCommit var3 = var2.currentSemanticCommit;
            return var3 != null && var3.originalSequence == var0 ? timerSemanticAtRanged(var3, var1, var3.writerPublication) : null;
        } else {
            return null;
        }
    }

    static List<FoodWeaponHitPlan.TimerSemanticResult> timerSemanticResults(FoodWeaponHitPlan.FinalEffectsCommit var0) {
        return var0 == null
            ? List.of()
            : var0.timerSemantics
                .stream()
                .map(
                    var1 -> new FoodWeaponHitPlan.TimerSemanticResult(
                            var1.program.leaf(),
                            var1.next,
                            var1.admittedKeys.size(),
                            var1.returnedHelpers,
                            var1.timerEntered,
                            var1.rangedEntered,
                            var1.timerReturned,
                            var1.closed,
                            var1.status,
                            var1.originalFailure,
                            var0.timerSemanticFailure == null ? var1.program.unsupported() : var0.timerSemanticFailure
                        )
                )
                .toList();
    }

    private static void timerSemanticFailure(FoodWeaponHitPlan.FinalEffectsCommit var0, Throwable var1) {
        if (var0 != null) {
            try {
                for (FoodWeaponHitPlan.TimerSemanticCursor var3 : var0.timerSemantics) {
                    if (var3.originalFailure == null && !var3.timerReturned) {
                        var3.originalFailure = var1;
                    }

                    if (!var3.timerReturned) {
                        var3.status = "ORIGINAL_THROW_SAME_PREFIX";
                    }
                }
            } catch (Throwable var4) {
                if (var0.timerSemanticFailure == null) {
                    var0.timerSemanticFailure = var4;
                }
            }
        }
    }

    private static void timerSemanticClose(FoodWeaponHitPlan.FinalEffectsCommit var0) {
        if (var0 != null) {
            try {
                for (FoodWeaponHitPlan.TimerSemanticCursor var2 : var0.timerSemantics) {
                    var2.currentStep = null;
                    var2.rangedActive = false;
                    var2.closed = true;
                    if (!var2.timerReturned && var2.originalFailure == null && var2.program.unsupported() == null) {
                        var2.status = "CLOSED_BEFORE_TIMER_RETURN";
                    }
                }
            } catch (Throwable var3) {
                if (var0.timerSemanticFailure == null) {
                    var0.timerSemanticFailure = var3;
                }
            }
        }
    }

    static FoodWeaponHitPlan.OriginalRepairFacts originalRepairFacts(
        FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1, Object var2
    ) {
        if (var2 instanceof FoodWeaponHitPlan.RepairLeaf var3 && var0 != null) {
            FoodWeaponHitPlan.OriginalRepairFacts var4 = var3.originalFacts;
            return var4.entered
                    && var4.bound
                    && !var4.closed
                    && var4.originalCommit == var0
                    && var4.originalSequence == var0.originalSequence
                    && var4.originalLeaf == var2
                    && var4.originalThread == Thread.currentThread()
                    && FoodWeaponFinalDamage.matchesOriginalHitPlanLeaf(var0, var1, var2, var0.currentLeafOrdinal)
                ? var4
                : null;
        }

        return null;
    }

    private boolean matchesClosedContext(ServerPlayer var1) {
        return var1 != null && !this.committed && this.targets.stream().allMatch(var1x -> var1x.closedRecipient(var1));
    }

    static FoodWeaponHitPlan.PreparedFinalEffects prepareFinalEffects(
        FoodWeaponHitPlan var0,
        FoodWeaponSourceLedger.AttemptBatchQuote var1,
        FoodWeaponSourceLedger.AttemptBatchAdmission var2,
        List<FoodWeaponSourceLedger.AttemptRequest> var3,
        List<FoodWeaponSourceLedger.TargetRequest> var4,
        ServerPlayer var5,
        DamageSource var6
    ) {
        if (var0 == null
            || !var0.commits.isEmpty()
            || var0.typedLeaves.stream().anyMatch(var0x -> !var0x.recipientClosureComplete())
            || !var0.matchesClosedContext(var5)
            || var1 == null
            || var2 == null
            || var2.status() != FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY
            || !FoodWeaponSourceLedger.matchesAttemptRequests(var1, var3, var4)) {
            return null;
        } else {
            return var3.containsAll(var0.attempts.stream().map(var0x -> new FoodWeaponSourceLedger.AttemptRequest(var0x.effect(), var0x.chance())).toList())
                    && var4.containsAll(var0.targets())
                ? new FoodWeaponHitPlan.PreparedFinalEffects(var0, var1, var2, var3, var4, var5, var6)
                : null;
        }
    }

    static boolean ownerReserveLeafSelected(
        FoodWeaponHitPlan.PreparedFinalEffects var0,
        FoodWeaponHitPlan var1,
        FoodWeaponSourceLedger.AttemptBatchQuote var2,
        FoodWeaponSourceLedger.AttemptBatchAdmission var3,
        FoodWeaponSourceLedger.AttemptBatchCommit var4,
        Object var5,
        ServerPlayer var6,
        DamageSource var7,
        long var8,
        List<FoodWeaponSourceLedger.AttemptRequest> var10,
        List<FoodWeaponSourceLedger.TargetRequest> var11
    ) {
        boolean var12 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == FoodWeaponFinalDamage.class && (var0xx.getMethodName().equals("acceptsOwnerReserveLeaf") || var0xx.getMethodName().equals("acceptsOwnerReserveMaterialLeaf")))
                        .orElse(false)
            );
        if (var12
            && Thread.holdsLock(FoodWeaponSourceLedger.class)
            && var0 != null
            && var1 != null
            && var0.plan == var1
            && var0.batch == var2
            && var0.admission == var3
            && var0.player == var6
            && var0.source == var7
            && var0.tick == var8
            && var10 != null
            && var11 != null
            && var10.equals(var0.requests)
            && var11.equals(var0.targets)
            && !var1.committed
            && var1.commits.isEmpty()
            && var4 != null
            && var4.status() == FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED
            && var5 instanceof FoodWeaponHitPlan.TypedLeaf var13) {
            int var14 = 0;

            for (FoodWeaponHitPlan.TypedLeaf var16 : var1.typedLeaves) {
                if (var16 == var13) {
                    var14++;
                }
            }

            if (var14 != 1) {
                return false;
            } else {
                for (FoodWeaponHitPlan.Target var21 : var1.targets) {
                    if (!var4.target(var21.request().target(), var21.request().domain())
                        .map(var0x -> var0x.commit() == FoodWeaponSourceLedger.TargetCommit.COMMITTED)
                        .orElse(false)) {
                        return false;
                    }
                }

                EnumMap var20 = new EnumMap<>(FoodWeaponSourceLedger.AttemptDomain.class);

                for (FoodWeaponHitPlan.Attempt var17 : var1.attempts) {
                    FoodWeaponSourceLedger.AttemptCommit var18 = var4.attempt(var17.effect()).map(FoodWeaponSourceLedger.AttemptResult::commit).orElse(null);
                    if (var18
                        != (
                            var17.outcome().success()
                                ? FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS
                                : FoodWeaponSourceLedger.AttemptCommit.COMMITTED_FAILURE
                        )) {
                        return false;
                    }

                    var20.put(var17.effect(), var18);
                }

                return var13.attempt == null || var20.get(var13.attempt) == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS;
            }
        } else {
            return false;
        }
    }

    static FoodWeaponHitPlan.FinalEffectsCommit commitFinalState(
        FoodWeaponHitPlan.PreparedFinalEffects var0, float var1, FoodWeaponFinalDamage.OffenseReceipt var2
    ) {
        boolean var3 = FoodWeaponFinalDamage.matchesOriginalFinalPreparation(var0, var2);
        if (var0 != null
            && var2 != null
            && Float.isFinite(var1)
            && !(var1 <= 0.0F)
            && (var3 || var0.plan.matchesClosedContext(var0.player))
            && var2.matchesBatch(var0.batch, var0.admission, var0.requests, var0.targets)
            && var2.matches(var0.plan, var0.player, var0.source, var0.tick, var1)) {
            FoodWeaponHitPlan var4 = var0.plan;

            for (FoodWeaponHitPlan.Target var6 : var4.targets) {
                if (var2.target(var6.request().target(), var6.request().domain()).orElseThrow().commit() != FoodWeaponSourceLedger.TargetCommit.COMMITTED) {
                    return null;
                }
            }

            EnumMap var9 = new EnumMap<>(FoodWeaponSourceLedger.AttemptDomain.class);

            for (FoodWeaponHitPlan.Attempt var7 : var4.attempts) {
                FoodWeaponSourceLedger.AttemptCommit var8 = var2.attempt(var7.effect()).orElseThrow().commit();
                if (var8
                    != (
                        var7.outcome().success()
                            ? FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS
                            : FoodWeaponSourceLedger.AttemptCommit.COMMITTED_FAILURE
                    )) {
                    return null;
                }

                var9.put(var7.effect(), var8);
            }

            List var11 = var4.typedLeaves
                .stream()
                .filter(var1x -> var1x.attempt == null || var9.get(var1x.attempt) == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS)
                .toList();
            var4.committed = true;
            return new FoodWeaponHitPlan.FinalEffectsCommit(var0, var2, var1, var11);
        } else {
            return null;
        }
    }

    static boolean leafSelectionReady(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        if (var0 != null && var0.originalSequence != null) {
            return FoodWeaponFinalDamage.originalHitPlanSequence(var0, var1) == var0.originalSequence
                && !var0.originalStopped
                && var0.executed
                && var0.currentLeaf == null
                && var0.currentLeafOrdinal == -1
                && var0.writerPublication == null;
        } else if (var0 != null
            && !var0.stopped
            && var0.executed
            && var0.currentLeaf == null
            && var0.currentLeafOrdinal == -1
            && var0.writerPublication == null) {
            if (var0.prepared.plan.typedLeaves.stream().anyMatch(var0x -> !var0x.recipientClosureComplete())) {
                var0.stopped = true;
                return false;
            } else {
                return FoodWeaponFinalDamage.matchesHitPlanSelectionPhase(var0, var1);
            }
        } else {
            return false;
        }
    }

    static FoodWeaponSourceLedger.OwnerWriterStep planTimerAtOriginalSite(
        FoodWeaponHitPlan.FinalEffectsCommit var0,
        Object var1,
        int var2,
        FoodWeaponSourceLedger.OwnerWriterPublication var3,
        ItemStack var4,
        Profile var5,
        String var6,
        Tag var7,
        CompoundTag var8,
        Tag var9
    ) {
        try {
            if (var0 != null
                && !var0.stopped
                && var0.executed
                && var3 != null
                && var0.writerPublication == var3
                && var0.currentLeaf == var1
                && var1 instanceof FoodWeaponHitPlan.TimerSaveLeaf var10
                && var10.weapon == var4
                && var10.profile == var5
                && var10.currentOriginalKey == var6
                && var10.currentOriginalValue == var7
                && var10.currentOriginalStepOrdinal == var2
                && var8 != null
                && var9 != null
                && Thread.holdsLock(FoodWeaponSourceLedger.class)) {
                var0.timerOriginalReceiver = var8;
                var0.timerOriginalArgument = var9;
                var0.timerOriginalValue = var7;
                var0.timerOriginalKey = var6;
                var0.timerOriginalStepOrdinal = var2;
                var0.timerOriginalWriter = var3;
                FoodWeaponSourceLedger.OwnerWriterStep var11 = FoodWeaponSourceLedger.planOwnerTimerWriter(
                    var3, var0, var1, var2, FoodWeaponEvolution.planHitTimerProfile(var5), var6, var7
                );
                if (var11 == null) {
                    retainTimerObserverFailure(var0, var1, var2, var3, null, FoodWeaponHitPlan.TimerObserverPhase.PLANNING, null);
                }

                return var11;
            }

            retainTimerObserverFailure(var0, var1, var2, var3, null, FoodWeaponHitPlan.TimerObserverPhase.PLANNING, null);
            return null;
        } catch (Throwable var12) {
            retainTimerObserverFailure(var0, var1, var2, var3, null, FoodWeaponHitPlan.TimerObserverPhase.PLANNING, var12);
            return null;
        }
    }

    static void retainTimerObserverFailure(
        FoodWeaponHitPlan.FinalEffectsCommit var0,
        Object var1,
        int var2,
        FoodWeaponSourceLedger.OwnerWriterPublication var3,
        FoodWeaponSourceLedger.OwnerWriterStep var4,
        FoodWeaponHitPlan.TimerObserverPhase var5,
        Throwable var6
    ) {
        if (var0 != null) {
            if (!var0.timerObservationIncomplete) {
                var0.incompleteTimerLeaf = var1;
                var0.incompleteTimerStepOrdinal = var2;
                var0.incompleteTimerStep = var4;
                var0.incompleteTimerWriterPublication = var3;
            } else if (var0.incompleteTimerStep == null && var4 != null) {
                var0.incompleteTimerStep = var4;
            }

            var0.timerObservationIncomplete = true;
            if (var0.originalSequence != null) {
                var0.originalSequence.observationFailed(null, var5.ordinal(), var6);
                switch (var5) {
                    case PLANNING:
                        if (var0.timerPlanningFailure == null) {
                            var0.timerPlanningFailure = var6;
                        }
                        break;
                    case ENTERED:
                        if (var0.timerEnteredFailure == null) {
                            var0.timerEnteredFailure = var6;
                        }
                        break;
                    case RETURNED:
                        if (var0.timerReturnedFailure == null) {
                            var0.timerReturnedFailure = var6;
                        }
                        break;
                    case EXCEPTION:
                        if (var0.timerExceptionObservationFailure == null) {
                            var0.timerExceptionObservationFailure = var6;
                        }
                        break;
                    case CLOSE:
                        if (var0.timerCloseFailure == null) {
                            var0.timerCloseFailure = var6;
                        }
                }
            } else {
                var0.stopped = true;

                byte var7 = switch (var5) {
                    case PLANNING, ENTERED -> 0;
                    case RETURNED -> 1;
                    case EXCEPTION -> 2;
                    case CLOSE -> 3;
                };
                switch (var5) {
                    case PLANNING:
                        if (var0.timerPlanningFailure == null) {
                            var0.timerPlanningFailure = var6;
                        }
                        break;
                    case ENTERED:
                        if (var0.timerEnteredFailure == null) {
                            var0.timerEnteredFailure = var6;
                        }
                        break;
                    case RETURNED:
                        if (var0.timerReturnedFailure == null) {
                            var0.timerReturnedFailure = var6;
                        }
                        break;
                    case EXCEPTION:
                        if (var0.timerExceptionObservationFailure == null) {
                            var0.timerExceptionObservationFailure = var6;
                        }
                        break;
                    case CLOSE:
                        if (var0.timerCloseFailure == null) {
                            var0.timerCloseFailure = var6;
                        }
                }

                try {
                    if (var4 == null) {
                        FoodWeaponSourceLedger.ownerPublicationObservationFailed(var3, var7, var6);
                    } else {
                        FoodWeaponSourceLedger.ownerWriterObservationFailed(var4, var7, var6);
                    }
                } catch (Throwable var9) {
                    switch (var7) {
                        case 0:
                            if (var0.timerEntryRecordingFailure == null) {
                                var0.timerEntryRecordingFailure = var9;
                            }
                            break;
                        case 1:
                            if (var0.timerNormalRecordingFailure == null) {
                                var0.timerNormalRecordingFailure = var9;
                            }
                            break;
                        case 2:
                            if (var0.timerExceptionRecordingFailure == null) {
                                var0.timerExceptionRecordingFailure = var9;
                            }
                            break;
                        case 3:
                            if (var0.timerCloseRecordingFailure == null) {
                                var0.timerCloseRecordingFailure = var9;
                            }
                    }
                }
            }
        }
    }

    static void timerOriginalPutReturned(FoodWeaponHitPlan.FinalEffectsCommit var0) {
        if (var0 != null) {
            var0.timerOriginalPutReturned = true;
        }
    }

    static void retainTimerOriginalFailure(
        FoodWeaponHitPlan.FinalEffectsCommit var0,
        Object var1,
        int var2,
        FoodWeaponSourceLedger.OwnerWriterPublication var3,
        FoodWeaponSourceLedger.OwnerWriterStep var4,
        Throwable var5
    ) {
        if (var0 != null) {
            if (var0.timerOriginalFailure == null) {
                var0.timerOriginalFailure = var5;
            }

            try {
                if (var4 == null) {
                    FoodWeaponSourceLedger.ownerPublicationOriginalFailed(var3, var5);
                } else {
                    FoodWeaponSourceLedger.ownerWriterOriginalFailed(var4, var5);
                }
            } catch (Throwable var7) {
                retainTimerObserverFailure(var0, var1, var2, var3, var4, FoodWeaponHitPlan.TimerObserverPhase.EXCEPTION, var7);
            }
        }
    }

    static boolean timerObservationNotKnownIncomplete(FoodWeaponHitPlan.FinalEffectsCommit var0) {
        return var0 != null && !var0.timerObservationIncomplete && (var0.originalSequence == null || var0.originalSequence.extraAuthorityRetained());
    }

    static FoodWeaponOriginalSequence.Session originalSequence(FoodWeaponHitPlan.FinalEffectsCommit var0) {
        return var0 == null ? null : var0.originalSequence;
    }

    static boolean bindOriginalSequence(
        FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponOriginalSequence.Session var1, FoodWeaponFinalDamage.OffenseReceipt var2
    ) {
        if (var0 != null && var1 != null && !var0.executed && var0.originalSequence == null && var0.receipt == var2 && var1.lineage.acceptedCallback == var2) {
            var0.originalSequence = var1;
            return true;
        } else {
            return false;
        }
    }

    static FoodWeaponOriginalSequence.Step beginOriginalTimer(FoodWeaponHitPlan.FinalEffectsCommit var0, Object var1, String var2, String var3) {
        if (var0 == null) {
            return null;
        } else if (var0.originalSequence == null) {
            var0.timerObservationIncomplete = true;
            return null;
        } else {
            FoodWeaponOriginalSequence.Session var4 = var0.originalSequence;

            try {
                return var4.originalBegins(var1, Kind.TIMER, var2, var3);
            } catch (Throwable var6) {
                var4.observationFailed(null, 0, var6);
                return null;
            }
        }
    }

    static void retainOriginalTimerThrowable(FoodWeaponHitPlan.FinalEffectsCommit var0, Throwable var1) {
        if (var0 != null && var0.timerOriginalFailure == null) {
            var0.timerOriginalFailure = var1;
        }
    }

    static boolean leafLive(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        if (var0 != null && var0.originalSequence != null) {
            return !var0.originalStopped
                && var0.executed
                && FoodWeaponFinalDamage.matchesOriginalHitPlanLeaf(var0, var1, var0.currentLeaf, var0.currentLeafOrdinal);
        } else if (var0 != null && !var0.stopped) {
            FoodWeaponHitPlan.PreparedFinalEffects var2 = var0.prepared;
            if (var2.plan.typedLeaves.stream().anyMatch(var0x -> !var0x.recipientClosureComplete())) {
                var0.stopped = true;
                return false;
            } else {
                return var0.executed
                    && FoodWeaponFinalDamage.matchesHitPlanInvocation(var0, var1)
                    && var0.receipt.matchesBatch(var2.batch, var2.admission, var2.requests, var2.targets)
                    && var0.receipt.matches(var2.plan, var2.player, var2.source, var2.tick, var0.amount);
            }
        } else {
            return false;
        }
    }

    static boolean matchesBoundRecipient(FoodWeaponHitPlan.FinalEffectsCommit var0, LivingEntity var1, FoodWeaponSourceLedger.TargetEffectDomain var2) {
        return var0 != null
            && var1 != null
            && var0.prepared
                .plan
                .targets
                .stream()
                .anyMatch(var2x -> var2x.entity() == var1 && var2x.request().domain() == var2 && var2x.request().target().equals(var1.getUUID()));
    }

    static void stopFinalEffects(FoodWeaponHitPlan.FinalEffectsCommit var0) {
        if (var0 != null) {
            var0.stopped = true;
            var0.originalStopped = true;
            timerSemanticClose(var0);
        }
    }

    static boolean matchesSelectedFinalLeaf(FoodWeaponHitPlan.FinalEffectsCommit var0, Object var1, int var2) {
        return var0 != null
            && var0.executed
            && !var0.stopped
            && var0.currentLeaf == var1
            && var0.currentLeafOrdinal == var2
            && var2 >= 0
            && var2 < var0.leaves.size()
            && var0.leaves.get(var2) == var1;
    }

    static boolean matchesOriginalLeafAdmission(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponOriginalSequence.Session var1, Object var2, int var3) {
        return var0 != null
            && !var0.originalStopped
            && var0.executed
            && var1 != null
            && var0.originalSequence == var1
            && var2 != null
            && var0.currentLeaf == var2
            && var0.currentLeafOrdinal == var3
            && var3 >= 0
            && var3 < var0.leaves.size()
            && var0.leaves.get(var3) == var2;
    }

    static boolean matchesOriginalSelectedFinalLeaf(FoodWeaponHitPlan.FinalEffectsCommit var0, Object var1, int var2) {
        return var0 != null
            && !var0.originalStopped
            && var0.executed
            && var0.originalSequence != null
            && var0.currentLeaf == var1
            && var0.currentLeafOrdinal == var2
            && var2 >= 0
            && var2 < var0.leaves.size()
            && var0.leaves.get(var2) == var1
            && var0.originalSequence.original.atLeaf(var1);
    }

    static boolean matchesOwnerWriterSelectedLeaf(
        FoodWeaponHitPlan.FinalEffectsCommit var0,
        FoodWeaponHitPlan.PreparedFinalEffects var1,
        FoodWeaponFinalDamage.OffenseReceipt var2,
        Object var3,
        int var4
    ) {
        return var0 != null
            && var1 != null
            && var2 != null
            && var0.prepared == var1
            && var0.receipt == var2
            && var1.plan.committed
            && matchesSelectedFinalLeaf(var0, var3, var4);
    }

    static void applyFinalEffects(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        if (var0 != null && !var0.executed) {
            FoodWeaponOriginalSequence.Session var2 = FoodWeaponFinalDamage.originalHitPlanSequence(var0, var1);
            boolean var3 = var2 != null && var2 == var0.originalSequence;
            var0.executed = true;
            timerSemanticStart(var0);

            try {
                for (int var4 = 0; var4 < var0.leaves.size(); var4++) {
                    FoodWeaponHitPlan.TypedLeaf var5 = var0.leaves.get(var4);
                    if (!leafSelectionReady(var0, var1)) {
                        return;
                    }

                    var0.currentLeaf = var5;
                    var0.currentLeafOrdinal = var4;
                    boolean var6 = false;

                    try {
                        if (var3) {
                            var6 = var2.original.enterCurrentLeaf(var5, var4);
                            if (!var6) {
                                return;
                            }
                        }

                        var0.writerPublication = FoodWeaponFinalDamage.selectHitPlanWriterLeaf(var0, var1, var5, var4);
                        if (var0.writerPublication == null && !var3) {
                            var0.stopped = true;
                            return;
                        }

                        var5.apply(var0, var1);
                        timerSemanticLeafReturned(var0, var5);
                        if (!var3 && !leafLive(var0, var1)) {
                            return;
                        }
                    } catch (Throwable var16) {
                        timerSemanticFailure(var0, var16);
                        if (var3) {
                            var2.original.originalThrown(var16);
                        }

                        throw var16;
                    } finally {
                        if (var6) {
                            var2.original.leaveLeaf(var5);
                        }

                        var0.writerPublication = null;
                        var0.currentLeaf = null;
                        var0.currentLeafOrdinal = -1;
                    }
                }
            } finally {
                timerSemanticClose(var0);
            }
        }
    }

    public void commit(float var1, DamageSource var2, ServerPlayer var3, FoodWeaponFinalDamage.OffenseReceipt var4) {
        if (!this.committed && Float.isFinite(var1) && !(var1 <= 0.0F)) {
            if (var4 != null && var4.matches(this, var3, var2, FoodWeaponSourceLedger.clock(var3), var1)) {
                if (!this.matchesContext(var3)) {
                    throw new IllegalStateException("Quoted target context changed before publication");
                } else {
                    for (FoodWeaponHitPlan.Target var6 : this.targets) {
                        if (var4.target(var6.request().target(), var6.request().domain()).orElseThrow().commit()
                            != FoodWeaponSourceLedger.TargetCommit.COMMITTED) {
                            throw new IllegalStateException("Every target intent needs a fresh atomic Owner target result");
                        }
                    }

                    EnumMap var8 = new EnumMap<>(FoodWeaponSourceLedger.AttemptDomain.class);

                    for (FoodWeaponHitPlan.Attempt var7 : this.attempts) {
                        var8.put(var7.effect(), var4.attempt(var7.effect()).orElseThrow().commit());
                    }

                    if (this.attempts
                        .stream()
                        .anyMatch(
                            var1x -> var8.get(var1x.effect())
                                    != (
                                        var1x.outcome().success()
                                            ? FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS
                                            : FoodWeaponSourceLedger.AttemptCommit.COMMITTED_FAILURE
                                    )
                        )) {
                        throw new IllegalStateException("Owner must atomically submit the full quoted attempt batch before effects");
                    } else {
                        this.committed = true;

                        for (FoodWeaponHitPlan.Commit var11 : this.commits) {
                            if (var11.effect() == null || var8.get(var11.effect()) == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS) {
                                var11.action().run();
                            }
                        }
                    }
                }
            } else {
                throw new IllegalStateException("The current private Final execution receipt for this exact plan is required");
            }
        }
    }

    public static record Attempt(FoodWeaponSourceLedger.AttemptDomain effect, double chance, FoodWeaponSourceLedger.AttemptQuote outcome) {
    }

    public static final class Builder {
        public boolean ftboss$meleeEnhancement;
        private float amount;
        private boolean primaryPaymentRequired;
        private final Map<FoodWeaponSourceLedger.AttemptDomain, FoodWeaponHitPlan.Attempt> attempts = new EnumMap<>(FoodWeaponSourceLedger.AttemptDomain.class);
        private final Map<FoodWeaponSourceLedger.TargetRequest, FoodWeaponHitPlan.Target> targets = new LinkedHashMap<>();
        private final List<FoodWeaponHitPlan.Commit> commits = new ArrayList<>();
        private final List<FoodWeaponHitPlan.TypedLeaf> typedLeaves = new ArrayList<>();

        public Builder(float var1) {
            this.amount = var1;
        }

        public float amount() {
            return this.amount;
        }

        public void requirePrimaryPayment() {
            this.primaryPaymentRequired = true;
        }

        public void multiply(float var1) {
            this.primaryPaymentRequired = true;
            this.amount *= var1;
        }

        public void add(float var1) {
            this.primaryPaymentRequired = true;
            this.amount += var1;
        }

        void effect(
            FoodWeaponSourceLedger.AttemptDomain var1,
            FoodWeaponSourceLedger.TargetEffectDomain var2,
            LivingEntity var3,
            MobEffectInstance var4,
            ServerPlayer var5
        ) {
            this.typed(var1, var2, var3, new FoodWeaponHitPlan.EffectLeaf(var1, var3, var4, var5));
        }

        void heal(FoodWeaponSourceLedger.AttemptDomain var1, LivingEntity var2, float var3) {
            this.typed(var1, null, null, new FoodWeaponHitPlan.HealLeaf(var1, var2, var3));
        }

        void fire(FoodWeaponSourceLedger.AttemptDomain var1, FoodWeaponSourceLedger.TargetEffectDomain var2, LivingEntity var3, int var4) {
            this.typed(var1, var2, var3, new FoodWeaponHitPlan.FireLeaf(var1, var3, var4));
        }

        void materialKnockback(ServerPlayer var1, LivingEntity var2) {
            this.typed(null, FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE, var2, new FoodWeaponHitPlan.MaterialKnockbackLeaf(var1, var2));
        }

        void materialBaseProc(ServerPlayer var1, LivingEntity var2, List<Material> var3) {
            this.typed(null, FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE, var2, new FoodWeaponHitPlan.MaterialBaseProcLeaf(var1, var2, var3));
        }

        void materialPowers(BeforeMitigation var1, ServerPlayer var2, List<Material> var3, List<TamableAnimal> var4) {
            this.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE, var1.getEntity());
            ArrayList var5 = new ArrayList();

            for (TamableAnimal var7 : var4) {
                this.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE, var7);
                var5.add(this.targets.get(new FoodWeaponSourceLedger.TargetRequest(var7.getUUID(), FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE)));
            }

            this.typed(null, null, null, new FoodWeaponHitPlan.MaterialPowersLeaf(var1, var2, var3, var4, var5));
        }

        void knockback(
            FoodWeaponSourceLedger.AttemptDomain var1, FoodWeaponSourceLedger.TargetEffectDomain var2, LivingEntity var3, double var4, double var6, double var8
        ) {
            this.typed(var1, var2, var3, new FoodWeaponHitPlan.KnockbackLeaf(var1, var3, var4, var6, var8));
        }

        void write(FoodWeaponSourceLedger.AttemptDomain var1, CompoundTag var2, String var3, Tag var4) {
            FoodWeaponHitPlan.TagLeaf var5 = new FoodWeaponHitPlan.TagLeaf(var1, var2, var3, var4);

            try {
                var5.targetWrite = OwnerNativeSemanticAuthority.targetWriteConstructed(this, var5, var1, var2, var3, var4, var5.value);
            } catch (Throwable var7) {
            }

            this.typed(var1, null, null, var5);
        }

        void writeAfterWorld(FoodWeaponSourceLedger.AttemptDomain var1, CompoundTag var2, String var3, Tag var4) {
            this.typed(var1, null, null, new FoodWeaponHitPlan.OutcomeTagLeaf(var1, var2, var3, var4));
        }

        void pushAway(FoodWeaponSourceLedger.AttemptDomain var1, LivingEntity var2, Vec3 var3, double var4) {
            this.typed(var1, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_KNOCKBACK, var2, new FoodWeaponHitPlan.PushLeaf(var1, var2, var3, var4));
        }

        void repair(FoodWeaponSourceLedger.AttemptDomain var1, ItemStack var2, int var3) {
            this.typed(var1, null, null, new FoodWeaponHitPlan.RepairLeaf(var1, var2, var3));
        }

        void saveHitTimers(ItemStack var1, Profile var2, ServerPlayer var3, DamageSource var4, CompoundTag var5, CompoundTag var6) {
            this.typed(null, null, null, new FoodWeaponHitPlan.TimerSaveLeaf(var1, var2, var3, var4, var5, var6));
        }

        void chain(FoodWeaponSourceLedger.AttemptDomain var1, ServerPlayer var2, LivingEntity var3, float var4, CompoundTag var5, String var6, long var7) {
            this.typed(
                var1,
                FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_DAMAGE,
                var3,
                new FoodWeaponHitPlan.ChainLeaf(var1, var2, var3, var4, var5, var6, var7)
            );
        }

        void overload(FoodWeaponSourceLedger.AttemptDomain var1, ServerPlayer var2, LivingEntity var3, List<LivingEntity> var4, float var5) {
            for (LivingEntity var7 : var4) {
                this.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_DAMAGE, var7);
            }

            this.typed(var1, null, null, new FoodWeaponHitPlan.OverloadLeaf(var1, var2, var3, var4, var5));
        }

        private void typed(
            FoodWeaponSourceLedger.AttemptDomain var1, FoodWeaponSourceLedger.TargetEffectDomain var2, LivingEntity var3, FoodWeaponHitPlan.TypedLeaf var4
        ) {
            if (var1 == null || this.attempts.get(var1) != null && this.attempts.get(var1).outcome().success()) {
                if (var2 != null) {
                    this.requireTarget(var2, var3);
                }

                this.typedLeaves.add(var4);
            } else {
                throw new IllegalStateException("No original successful typed attempt");
            }
        }

        public void defer(Runnable var1) {
            this.commits.add(new FoodWeaponHitPlan.Commit(null, var1));
        }

        public void requireTarget(FoodWeaponSourceLedger.TargetEffectDomain var1, LivingEntity var2) {
            if (var1 != null && var2 != null && var2.isAlive() && !var2.isRemoved() && var2.getUUID() != null) {
                FoodWeaponSourceLedger.TargetRequest var3 = new FoodWeaponSourceLedger.TargetRequest(var2.getUUID(), var1);
                FoodWeaponHitPlan.Target var4 = this.targets.get(var3);
                if (var4 != null && var4.entity() != var2) {
                    throw new IllegalArgumentException("One target identity cannot refer to two entity objects");
                } else {
                    if (var4 == null) {
                        this.targets.put(var3, new FoodWeaponHitPlan.Target(var3, var2, var2.level(), var2.position(), var2.getHealth(), var2.getMaxHealth()));
                    }
                }
            } else {
                throw new IllegalArgumentException("A live exact target and typed target domain are required");
            }
        }

        public void deferTarget(FoodWeaponSourceLedger.TargetEffectDomain var1, LivingEntity var2, Runnable var3) {
            if (var3 == null) {
                throw new IllegalArgumentException("Target effect is required");
            } else {
                this.requireTarget(var1, var2);
                this.defer(var3);
            }
        }

        public void deferTargetRandom(
            FoodWeaponSourceLedger.AttemptDomain var1, FoodWeaponSourceLedger.TargetEffectDomain var2, LivingEntity var3, Runnable var4
        ) {
            FoodWeaponHitPlan.Attempt var5 = this.attempts.get(var1);
            if (var5 == null || !var5.outcome().success()) {
                throw new IllegalStateException("No successful quote");
            } else if (var4 == null) {
                throw new IllegalArgumentException("Target effect is required");
            } else {
                this.requireTarget(var2, var3);
                this.deferRandom(var1, var4);
            }
        }

        public boolean roll(FoodWeaponSourceLedger.AttemptDomain var1, double var2, FoodWeaponHitPlan.RandomQuotes var4) {
            if (var4 != null && Double.isFinite(var2) && !(var2 < 0.0) && !(var2 > 1.0) && !this.attempts.containsKey(var1)) {
                FoodWeaponSourceLedger.AttemptQuote var5 = var4.quote(var1, var2);
                if (var5 != null
                    && var5.status() == FoodWeaponSourceLedger.AttemptStatus.AVAILABLE
                    && var5.domain() == var1
                    && Double.compare(var5.chance(), var2) == 0) {
                    this.attempts.put(var1, new FoodWeaponHitPlan.Attempt(var1, var2, var5));
                    return var5.success();
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        public void deferRandom(FoodWeaponSourceLedger.AttemptDomain var1, Runnable var2) {
            FoodWeaponHitPlan.Attempt var3 = this.attempts.get(var1);
            if (var3 != null && var3.outcome().success()) {
                this.commits.add(new FoodWeaponHitPlan.Commit(var1, var2));
            } else {
                throw new IllegalStateException("No successful quote");
            }
        }

        public FoodWeaponHitPlan build() {
            return new FoodWeaponHitPlan(
                this.amount,
                this.primaryPaymentRequired,
                List.copyOf(this.attempts.values()),
                List.copyOf(this.targets.values()),
                List.copyOf(this.commits),
                List.copyOf(this.typedLeaves)
            );
        }
    }

    private static final class ChainLeaf extends FoodWeaponHitPlan.TypedLeaf {
        private final ServerPlayer player;
        private final LivingEntity target;
        private final float damage;
        private final CompoundTag data;
        private final String key;
        private final long tick;

        private ChainLeaf(FoodWeaponSourceLedger.AttemptDomain var1, ServerPlayer var2, LivingEntity var3, float var4, CompoundTag var5, String var6, long var7) {
            super(var1);
            this.player = var2;
            this.target = var3;
            this.damage = var4;
            this.data = var5;
            this.key = var6;
            this.tick = var7;
        }

        @Override
        void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            boolean var3 = FoodWeaponEvolution.hurtPlanSecondary(this.player, this.target, this.damage, var2);
            if (var3 && FoodWeaponHitPlan.leafLive(var1, var2)) {
                if (this.key != null) {
                    this.data.putLong(this.key, this.tick);
                    FoodWeaponHitPlan.timerSemanticMutation(var1, this, true);
                }

                if (FoodWeaponHitPlan.leafLive(var1, var2)) {
                    ((ServerLevel)this.target.level())
                        .sendParticles(
                            ParticleTypes.ELECTRIC_SPARK,
                            this.target.getX(),
                            this.target.getY() + (double)this.target.getBbHeight() * 0.5,
                            this.target.getZ(),
                            12,
                            0.22,
                            0.35,
                            0.22,
                            0.12
                        );
                }
            } else {
                if (this.key != null) {
                    FoodWeaponHitPlan.timerSemanticMutation(var1, this, false);
                }
            }
        }
    }

    private static record Commit(FoodWeaponSourceLedger.AttemptDomain effect, Runnable action) {
    }

    private static final class EffectLeaf extends FoodWeaponHitPlan.TypedLeaf {
        private final LivingEntity entity;
        private final MobEffectInstance effect;
        private final ServerPlayer player;

        private EffectLeaf(FoodWeaponSourceLedger.AttemptDomain var1, LivingEntity var2, MobEffectInstance var3, ServerPlayer var4) {
            super(var1);
            this.entity = var2;
            this.effect = new MobEffectInstance(var3);
            this.player = var4;
        }

        @Override
        void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            this.entity.addEffect(new MobEffectInstance(this.effect), this.player);
        }
    }

    static final class FinalEffectsCommit {
        private FoodWeaponOriginalSequence.Session originalSequence;
        private final FoodWeaponHitPlan.PreparedFinalEffects prepared;
        private final FoodWeaponFinalDamage.OffenseReceipt receipt;
        private final float amount;
        private final List<FoodWeaponHitPlan.TypedLeaf> leaves;
        private List<FoodWeaponHitPlan.TimerSemanticCursor> timerSemantics = List.of();
        private Throwable timerSemanticFailure;
        private FoodWeaponHitPlan.TypedLeaf currentLeaf;
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

        private FinalEffectsCommit(
            FoodWeaponHitPlan.PreparedFinalEffects var1, FoodWeaponFinalDamage.OffenseReceipt var2, float var3, List<FoodWeaponHitPlan.TypedLeaf> var4
        ) {
            this.prepared = var1;
            this.receipt = var2;
            this.amount = var3;
            this.leaves = List.copyOf(var4);

            try {
                FoodWeaponHitPlan.TimerSemanticCatalogue var5 = var1.plan.timerSemanticCatalogue;
                if (var5.unsupported() != null) {
                    this.timerSemanticFailure = var5.unsupported();
                    return;
                }

                this.timerSemantics = var5.programs()
                    .stream()
                    .filter(var1x -> this.leaves.contains(var1x.leaf()))
                    .map(var1x -> new FoodWeaponHitPlan.TimerSemanticCursor(var1x, this.leaves))
                    .toList();
            } catch (Throwable var6) {
                this.timerSemanticFailure = var6;
            }
        }
    }

    private static final class FireLeaf extends FoodWeaponHitPlan.TypedLeaf {
        private final LivingEntity entity;
        private final int seconds;

        private FireLeaf(FoodWeaponSourceLedger.AttemptDomain var1, LivingEntity var2, int var3) {
            super(var1);
            this.entity = var2;
            this.seconds = var3;
        }

        @Override
        void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            this.entity.igniteForSeconds(this.seconds);
        }
    }

    private static final class HealLeaf extends FoodWeaponHitPlan.TypedLeaf {
        private final LivingEntity entity;
        private final float amount;

        private HealLeaf(FoodWeaponSourceLedger.AttemptDomain var1, LivingEntity var2, float var3) {
            super(var1);
            this.entity = var2;
            this.amount = var3;
        }

        @Override
        void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            this.entity.heal(this.amount);
        }
    }

    private static final class KnockbackLeaf extends FoodWeaponHitPlan.TypedLeaf {
        private final LivingEntity entity;
        private final double strength;
        private final double horizontal;
        private final double vertical;

        private KnockbackLeaf(FoodWeaponSourceLedger.AttemptDomain var1, LivingEntity var2, double var3, double var5, double var7) {
            super(var1);
            this.entity = var2;
            this.strength = var3;
            this.horizontal = var5;
            this.vertical = var7;
        }

        @Override
        void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            this.entity.knockback(this.strength, this.horizontal, this.vertical);
        }
    }

    private static final class MaterialBaseProcLeaf extends FoodWeaponHitPlan.TypedLeaf {
        private final ServerPlayer player;
        private final LivingEntity target;
        private final List<Material> materials;

        private MaterialBaseProcLeaf(ServerPlayer var1, LivingEntity var2, List<Material> var3) {
            super(null);
            this.player = var1;
            this.target = var2;
            this.materials = List.copyOf(var3);
        }

        @Override
        void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            WeaponMaterialBuild.applyClosedBaseProc(this.player, this.target, this.materials, var1, var2);
        }
    }

    private static final class MaterialKnockbackLeaf extends FoodWeaponHitPlan.TypedLeaf {
        private final ServerPlayer player;
        private final LivingEntity target;

        private MaterialKnockbackLeaf(ServerPlayer var1, LivingEntity var2) {
            super(null);
            this.player = var1;
            this.target = var2;
        }

        @Override
        void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            this.target.knockback(0.2, this.player.getX() - this.target.getX(), this.player.getZ() - this.target.getZ());
        }
    }

    static boolean materialPowersLeaf(Object leaf) {
        return leaf instanceof MaterialPowersLeaf;
    }

    static OwnerMaterialProcAuthority.Program materialProcProgram(Object leaf) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != OwnerTimerSemanticAuthority.Book.class)
            throw new SecurityException("Original material quote requires its semantic book");
        return leaf instanceof MaterialPowersLeaf material ? material.materialProgram : null;
    }

    private static final class MaterialPowersLeaf extends FoodWeaponHitPlan.TypedLeaf {
        private final BeforeMitigation event;
        private final OwnerMaterialProcAuthority.Program materialProgram;
        private final ServerPlayer player;
        private final List<Material> materials;
        private final List<TamableAnimal> potentialPets;
        private final List<FoodWeaponHitPlan.Target> petRecipients;
        private final Set<TamableAnimal> potentialPetIdentities;
        private final Level petLevel;

        private MaterialPowersLeaf(BeforeMitigation var1, ServerPlayer var2, List<Material> var3, List<TamableAnimal> var4, List<FoodWeaponHitPlan.Target> var5) {
            super(null);
            this.event = var1;
            this.player = var2;
            this.materials = List.copyOf(var3);
            this.materialProgram = OwnerMaterialProcAuthority.capture(var2, var1, this.materials);
            this.potentialPets = List.copyOf(var4);
            this.petRecipients = List.copyOf(var5);
            Set var6 = Collections.newSetFromMap(new IdentityHashMap());
            var6.addAll(this.potentialPets);
            this.potentialPetIdentities = Collections.unmodifiableSet(var6);
            this.petLevel = var2.level();
        }

        @Override
        boolean recipientClosureComplete() {
            return !this.materials.contains(Material.BOSS_SHEPHERD)
                ? this.potentialPets.isEmpty() && this.petRecipients.isEmpty()
                : this.player.level() == this.petLevel
                    && this.event.getEntity().level() == this.petLevel
                    && this.petRecipients.size() == this.potentialPets.size()
                    && this.potentialPetIdentities.size() == this.potentialPets.size()
                    && this.petRecipients
                        .stream()
                        .allMatch(
                            var1 -> var1.closedRecipient(this.player) && var1.request().domain() == FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE
                        )
                    && WeaponMaterialPowers.matchesPetRecipientClosure(this.player, this.potentialPetIdentities);
        }

        @Override
        void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            OwnerMaterialProcAuthority.Leaf observation = FoodWeaponSourceLedger.ownerMaterialLeafEntered(var1, var2, this);
            try {
                WeaponMaterialPowers.applyClosedHitSteps(this.event, this.player, this.materials, this.potentialPets, var1, var2);
            } catch (Throwable failure) {
                OwnerMaterialProcAuthority.leave(observation, failure);
                throw failure;
            }
            OwnerMaterialProcAuthority.leave(observation, null);
        }
    }

    static final class OriginalRepairFacts {
        private final ItemStack weapon;
        private final int reduction;
        private FoodWeaponHitPlan.FinalEffectsCommit originalCommit;
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

        private OriginalRepairFacts(ItemStack var1, int var2) {
            this.weapon = var1;
            this.reduction = var2;
        }

        private void begin(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2, Object var3) {
            if (!this.entered) {
                this.entered = true;
                this.originalCommit = var1;
                this.originalLeaf = var3;
                this.originalThread = Thread.currentThread();
                this.originalSequence = var1 == null ? null : var1.originalSequence;
                this.bound = this.originalSequence != null && FoodWeaponFinalDamage.matchesOriginalHitPlanLeaf(var1, var2, var3, var1.currentLeafOrdinal);
            }
        }

        private void readReturned(int var1, int var2) {
            this.originalDamage = var1;
            this.requestedDamage = var2;
            this.readReturned = true;
        }

        private void setterEntered() {
            this.setterEntered = true;
            if (this.bound) {
                this.originalSequence.interferenceObserved(this.originalSequence.lineage.ownerSelection, this.originalSequence.lineage.generation, null);
            }
        }

        private void observationFailed(Throwable var1) {
            if (this.observerFailure == null) {
                this.observerFailure = var1;
            }

            if (this.bound) {
                this.originalSequence.observationFailed(null, 0, var1);
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

    private static final class OutcomeTagLeaf extends FoodWeaponHitPlan.TypedLeaf {
        private final CompoundTag data;
        private final String key;
        private final Tag value;

        private OutcomeTagLeaf(FoodWeaponSourceLedger.AttemptDomain var1, CompoundTag var2, String var3, Tag var4) {
            super(var1);
            this.data = var2;
            this.key = var3;
            this.value = var4 == null ? null : var4.copy();
        }

        @Override
        void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            if (this.value == null) {
                this.data.remove(this.key);
            } else {
                this.data.put(this.key, this.value.copy());
            }

            FoodWeaponHitPlan.timerSemanticMutation(var1, this, true);
        }
    }

    private static final class OverloadLeaf extends FoodWeaponHitPlan.TypedLeaf {
        private final ServerPlayer player;
        private final LivingEntity origin;
        private final List<LivingEntity> victims;
        private final float damage;

        private OverloadLeaf(FoodWeaponSourceLedger.AttemptDomain var1, ServerPlayer var2, LivingEntity var3, List<LivingEntity> var4, float var5) {
            super(var1);
            this.player = var2;
            this.origin = var3;
            this.victims = List.copyOf(var4);
            this.damage = var5;
        }

        @Override
        void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            FoodWeaponEvolution.applyPlanOverload(var1, var2, this.player, this.origin, this.victims, this.damage);
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

        private PreparedFinalEffects(
            FoodWeaponHitPlan var1,
            FoodWeaponSourceLedger.AttemptBatchQuote var2,
            FoodWeaponSourceLedger.AttemptBatchAdmission var3,
            List<FoodWeaponSourceLedger.AttemptRequest> var4,
            List<FoodWeaponSourceLedger.TargetRequest> var5,
            ServerPlayer var6,
            DamageSource var7
        ) {
            this.plan = var1;
            this.batch = var2;
            this.admission = var3;
            this.requests = List.copyOf(var4);
            this.targets = List.copyOf(var5);
            this.player = var6;
            this.source = var7;
            this.tick = FoodWeaponSourceLedger.clock(var6);
        }
    }

    private static final class PushLeaf extends FoodWeaponHitPlan.TypedLeaf {
        private final LivingEntity entity;
        private final Vec3 origin;
        private final double strength;

        private PushLeaf(FoodWeaponSourceLedger.AttemptDomain var1, LivingEntity var2, Vec3 var3, double var4) {
            super(var1);
            this.entity = var2;
            this.origin = var3;
            this.strength = var4;
        }

        @Override
        void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            Vec3 var3 = this.entity.position().subtract(this.origin).multiply(1.0, 0.0, 1.0).normalize();
            double var4 = Math.max(0.0, Math.min(1.0, this.entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
            double var6 = this.strength * (1.0 - var4);
            if (!(var3.lengthSqr() < 1.0E-6) && !(var6 <= 0.0)) {
                this.entity.push(var3.x * var6, 0.18 * (1.0 - var4), var3.z * var6);
                if (FoodWeaponHitPlan.leafLive(var1, var2)) {
                    this.entity.hurtMarked = true;
                }
            }
        }
    }

    @FunctionalInterface
    public interface RandomQuotes {
        FoodWeaponSourceLedger.AttemptQuote quote(FoodWeaponSourceLedger.AttemptDomain var1, double var2);
    }

    private static final class RepairLeaf extends FoodWeaponHitPlan.TypedLeaf {
        private final ItemStack weapon;
        private final int reduction;
        private final FoodWeaponHitPlan.OriginalRepairFacts originalFacts;

        private RepairLeaf(FoodWeaponSourceLedger.AttemptDomain var1, ItemStack var2, int var3) {
            super(var1);
            this.weapon = var2;
            this.reduction = var3;
            this.originalFacts = new FoodWeaponHitPlan.OriginalRepairFacts(var2, var3);
        }

        @Override
        void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            try {
                this.originalFacts.begin(var1, var2, this);
            } catch (Throwable var27) {
                Throwable var3 = var27;

                try {
                    this.originalFacts.observationFailed(var3);
                } catch (Throwable var26) {
                }
            }

            try {
                int var28 = this.weapon.getDamageValue();
                int var4 = Math.max(0, var28 - this.reduction);

                try {
                    this.originalFacts.readReturned(var28, var4);
                } catch (Throwable var23) {
                    Throwable var5 = var23;

                    try {
                        this.originalFacts.observationFailed(var5);
                    } catch (Throwable var22) {
                    }
                }

                try {
                    this.originalFacts.setterEntered();
                } catch (Throwable var21) {
                    Throwable var29 = var21;

                    try {
                        this.originalFacts.observationFailed(var29);
                    } catch (Throwable var20) {
                    }
                }

                this.weapon.setDamageValue(var4);

                try {
                    this.originalFacts.setterReturned = true;
                } catch (Throwable var19) {
                    Throwable var30 = var19;

                    try {
                        this.originalFacts.observationFailed(var30);
                    } catch (Throwable var18) {
                    }
                }
            } catch (Throwable var24) {
                this.originalFacts.originalFailure = var24;
                throw var24;
            } finally {
                this.originalFacts.closed = true;
            }
        }
    }

    private static final class TagLeaf extends FoodWeaponHitPlan.TypedLeaf {
        private final CompoundTag data;
        private final String key;
        private final Tag value;
        private OwnerNativeSemanticAuthority.TargetWrite targetWrite;

        private TagLeaf(FoodWeaponSourceLedger.AttemptDomain var1, CompoundTag var2, String var3, Tag var4) {
            super(var1);
            this.data = var2;
            this.key = var3;
            this.value = var4 == null ? null : var4.copy();
        }

        @Override
        void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            OwnerNativeSemanticAuthority.TargetObservation var3 = null;

            try {
                var3 = OwnerNativeSemanticAuthority.targetEntered(var1, var2, this, this.targetWrite, this.data, this.key, this.value);
            } catch (Throwable var32) {
            }

            try {
                if (this.value == null) {
                    this.data.remove(this.key);

                    try {
                        OwnerNativeSemanticAuthority.targetRemoved(var3, this.data, this.key);
                    } catch (Throwable var29) {
                        Throwable var33 = var29;

                        try {
                            OwnerNativeSemanticAuthority.targetFailed(var3, var33);
                        } catch (Throwable var28) {
                        }
                    }
                } else {
                    Tag var34 = this.value.copy();

                    try {
                        OwnerNativeSemanticAuthority.targetCopied(var3, this.value, var34);
                    } catch (Throwable var27) {
                        Throwable var5 = var27;

                        try {
                            OwnerNativeSemanticAuthority.targetFailed(var3, var5);
                        } catch (Throwable var26) {
                        }
                    }

                    Tag var35 = this.data.put(this.key, var34);

                    try {
                        OwnerNativeSemanticAuthority.targetReturned(var3, this.data, this.key, var34, var35);
                    } catch (Throwable var25) {
                        Throwable var6 = var25;

                        try {
                            OwnerNativeSemanticAuthority.targetFailed(var3, var6);
                        } catch (Throwable var24) {
                        }
                    }
                }

                FoodWeaponHitPlan.timerSemanticMutation(var1, this, true);
            } catch (Throwable var30) {
                Throwable var4 = var30;

                try {
                    OwnerNativeSemanticAuthority.targetFailed(var3, var4);
                } catch (Throwable var23) {
                }

                throw var30;
            } finally {
                try {
                    OwnerNativeSemanticAuthority.targetClosed(var3);
                } catch (Throwable var22) {
                }
            }
        }
    }

    private static record Target(FoodWeaponSourceLedger.TargetRequest request, LivingEntity entity, Level level, Vec3 position, float health, float maxHealth) {
        private boolean closedRecipient(ServerPlayer var1) {
            return this.entity != var1
                && this.entity.isAlive()
                && !this.entity.isRemoved()
                && this.entity.level() == this.level
                && this.level == var1.level()
                && this.entity.getUUID().equals(this.request.target());
        }

        private boolean stable(ServerPlayer var1) {
            return this.entity != var1
                && this.entity.isAlive()
                && !this.entity.isRemoved()
                && this.entity.level() == this.level
                && this.level == var1.level()
                && this.entity.getUUID().equals(this.request.target())
                && this.entity.position().equals(this.position)
                && Float.compare(this.entity.getHealth(), this.health) == 0
                && Float.compare(this.entity.getMaxHealth(), this.maxHealth) == 0;
        }
    }

    static enum TimerObserverPhase {
        PLANNING,
        ENTERED,
        RETURNED,
        EXCEPTION,
        CLOSE;
    }

    private static final class TimerSaveLeaf extends FoodWeaponHitPlan.TypedLeaf {
        private final ItemStack weapon;
        private final Profile profile;
        private final ServerPlayer player;
        private final DamageSource source;
        private final CompoundTag baseline;
        private final CompoundTag data;
        private final FoodWeaponHitPlan.TimerSemanticSeed semanticSeed;
        private FoodWeaponHitPlan.FinalEffectsCommit currentSemanticCommit;
        private String currentOriginalKey;
        private Tag currentOriginalValue;
        private int currentOriginalStepOrdinal = -1;

        private TimerSaveLeaf(ItemStack var1, Profile var2, ServerPlayer var3, DamageSource var4, CompoundTag var5, CompoundTag var6) {
            super(null);
            this.weapon = var1;
            this.profile = var2;
            this.player = var3;
            this.source = var4;
            this.baseline = var5.copy();
            this.data = var6;
            this.semanticSeed = FoodWeaponHitPlan.timerSemanticSeed(this.baseline, var6);
        }

        @Override
        void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            this.currentSemanticCommit = var1;

            try {
                FoodWeaponHitPlan.timerSemanticTimerBegins(var1, this);
                boolean var3 = false;
                int var4 = 0;

                for (String var6 : this.data.getAllKeys()) {
                    if (!Objects.equals(this.baseline.get(var6), this.data.get(var6))) {
                        Tag var7 = this.data.get(var6);
                        FoodWeaponSourceLedger.OwnerWriterPublication var8 = var1.writerPublication;
                        var1.timerOriginalPutReturned = false;
                        this.currentOriginalKey = var6;
                        this.currentOriginalValue = var7;
                        this.currentOriginalStepOrdinal = var4;
                        FoodWeaponHitPlan.timerSemanticOriginalBegins(var1, this, var6, var4);

                        try {
                            FoodWeaponEvolution.writePlanHitTimer(this.weapon, this.profile, var6, var7, null, var1, var4, var8, this);
                            FoodWeaponHitPlan.timerSemanticOriginalReturned(var1, this);
                        } finally {
                            FoodWeaponHitPlan.timerSemanticOriginalClosed(var1, this);
                            this.currentOriginalKey = null;
                            this.currentOriginalValue = null;
                            this.currentOriginalStepOrdinal = -1;
                        }

                        var4++;
                        var3 = true;
                    }
                }

                if (var3) {
                    FoodWeaponHitPlan.timerSemanticRanged(var1, this, true);

                    try {
                        FoodWeaponRanged.saveHitTimers(this.player, this.source, this.data);
                    } finally {
                        FoodWeaponHitPlan.timerSemanticRanged(var1, this, false);
                    }
                }

                FoodWeaponHitPlan.timerSemanticTimerReturned(var1, this, var3);
            } finally {
                if (this.currentSemanticCommit == var1) {
                    this.currentSemanticCommit = null;
                }
            }
        }
    }

    static record TimerSemanticCatalogue(List<FoodWeaponHitPlan.TimerSemanticProgram> programs, Throwable unsupported) {
        TimerSemanticCatalogue(List<FoodWeaponHitPlan.TimerSemanticProgram> programs, Throwable unsupported) {
            programs = List.copyOf(programs);
            this.programs = programs;
            this.unsupported = unsupported;
        }
    }

    static record TimerSemanticChoice(FoodWeaponHitPlan.TimerSemanticInstruction instruction, boolean selected, boolean applied) {
    }

    private static final class TimerSemanticCursor {
        private final FoodWeaponHitPlan.TimerSemanticProgram program;
        private final List<FoodWeaponHitPlan.TimerSemanticInstruction> instructions;
        private final Map<String, Integer> admittedKeys = new LinkedHashMap<>();
        private Value image;
        private FoodWeaponHitPlan.TimerSemanticProjection projection;
        private FoodWeaponHitPlan.TimerSemanticStep currentStep;
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

        private TimerSemanticCursor(FoodWeaponHitPlan.TimerSemanticProgram var1, List<FoodWeaponHitPlan.TypedLeaf> var2) {
            this.program = var1;
            this.instructions = var1.instructions().stream().filter(var1x -> var2.contains(var1x.leaf())).toList();
            this.image = var1.initialData();
            this.status = var1.unsupported() == null ? "QUOTED" : "UNSUPPORTED_QUOTE";
        }
    }

    static record TimerSemanticInstruction(
        Object leaf, int planOrdinal, FoodWeaponSourceLedger.AttemptDomain attempt, String key, Value value, boolean conditional
    ) {
    }

    static record TimerSemanticProgram(
        Object leaf,
        Object weapon,
        Object profile,
        Object player,
        Object source,
        Value baseline,
        Value initialData,
        List<FoodWeaponHitPlan.TimerSemanticInstruction> instructions,
        Throwable unsupported
    ) {
        TimerSemanticProgram(
            Object leaf,
            Object weapon,
            Object profile,
            Object player,
            Object source,
            Value baseline,
            Value initialData,
            List<FoodWeaponHitPlan.TimerSemanticInstruction> instructions,
            Throwable unsupported
        ) {
            instructions = List.copyOf(instructions);
            this.leaf = leaf;
            this.weapon = weapon;
            this.profile = profile;
            this.player = player;
            this.source = source;
            this.baseline = baseline;
            this.initialData = initialData;
            this.instructions = instructions;
            this.unsupported = unsupported;
        }
    }

    static record TimerSemanticProjection(
        FoodWeaponHitPlan.TimerSemanticProgram program, Value localData, Map<String, Value> writes, Map<String, Long> rangedLongs
    ) {
        TimerSemanticProjection(FoodWeaponHitPlan.TimerSemanticProgram program, Value localData, Map<String, Value> writes, Map<String, Long> rangedLongs) {
            writes = Map.copyOf(writes);
            rangedLongs = Map.copyOf(rangedLongs);
            this.program = program;
            this.localData = localData;
            this.writes = writes;
            this.rangedLongs = rangedLongs;
        }
    }

    static record TimerSemanticResult(
        Object leaf,
        int resolvedInstructions,
        int admittedKeys,
        int returnedHelpers,
        boolean timerEntered,
        boolean rangedEntered,
        boolean timerReturned,
        boolean closed,
        String status,
        Throwable originalFailure,
        Throwable observationFailure
    ) {
    }

    private static record TimerSemanticSeed(Value baseline, Value data, Throwable unsupported) {
    }

    static record TimerSemanticStep(FoodWeaponHitPlan.TimerSemanticProjection projection, String key, Value value, int originalStepOrdinal) {
    }

    private abstract static sealed class TypedLeaf
        permits FoodWeaponHitPlan.EffectLeaf,
        FoodWeaponHitPlan.HealLeaf,
        FoodWeaponHitPlan.FireLeaf,
        FoodWeaponHitPlan.KnockbackLeaf,
        FoodWeaponHitPlan.TagLeaf,
        FoodWeaponHitPlan.RepairLeaf,
        FoodWeaponHitPlan.ChainLeaf,
        FoodWeaponHitPlan.OverloadLeaf,
        FoodWeaponHitPlan.OutcomeTagLeaf,
        FoodWeaponHitPlan.PushLeaf,
        FoodWeaponHitPlan.TimerSaveLeaf,
        FoodWeaponHitPlan.MaterialKnockbackLeaf,
        FoodWeaponHitPlan.MaterialBaseProcLeaf,
        FoodWeaponHitPlan.MaterialPowersLeaf {
        private final FoodWeaponSourceLedger.AttemptDomain attempt;

        private TypedLeaf(FoodWeaponSourceLedger.AttemptDomain var1) {
            this.attempt = var1;
        }

        boolean recipientClosureComplete() {
            return true;
        }

        abstract void apply(FoodWeaponHitPlan.FinalEffectsCommit var1, FoodWeaponFinalDamage.ChildHurtBridge var2);
    }
}
