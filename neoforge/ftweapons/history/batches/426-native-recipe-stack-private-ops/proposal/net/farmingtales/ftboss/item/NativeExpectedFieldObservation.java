package net.farmingtales.ftboss.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.Profile;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Kind;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.State;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Actor;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Compound;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.LongValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public final class NativeExpectedFieldObservation {
    private static final ThreadLocal<NativeExpectedFieldObservation.Scope> CURRENT = new ThreadLocal<>();
    private static final Map<FoodWeaponOriginalSequence.Session, NativeExpectedFieldObservation.Domain> DOMAINS = new IdentityHashMap<>();
    private static volatile Throwable infrastructureFailure;

    private NativeExpectedFieldObservation() {
    }

    private static final net.minecraft.nbt.NbtOps READ_OPS = new ReadOps();

    private static CompoundTag newReadCompound() {
        CompoundTag output = new CompoundTag();
        if (!fields(output).isEmpty()) throw new IllegalStateException("New native read compound is not empty");
        return output;
    }

    private static CompoundTag copyReadCompound(CompoundTag input) {
        CompoundTag output = newReadCompound();
        fields(output).putAll(fields(input));
        return output;
    }

    private static void requireReadCaller(Class<?> owner, String method, String descriptor) {
        boolean allowed = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(2L).findFirst()
            .map(frame -> frame.getDeclaringClass() == owner && frame.getMethodName().equals(method)
                && frame.getDescriptor().equals(descriptor)).orElse(false));
        if (!allowed) throw new IllegalStateException("Native observation encoding caller denied");
    }

    public static Tag componentValuesForRead(net.minecraft.core.HolderLookup.Provider registries,
        net.minecraft.core.component.DataComponentMap components) {
        requireReadCaller(NativeItemComponentObservation.class, "encodeValues", "()Lnet/minecraft/nbt/Tag;");
        return net.minecraft.core.component.DataComponentMap.CODEC.encodeStart(registries.createSerializationContext(READ_OPS), components)
            .getOrThrow(error -> new IllegalArgumentException("Invalid actual native component values: " + error));
    }

    public static Tag componentPatchForRead(net.minecraft.core.HolderLookup.Provider registries,
        net.minecraft.core.component.DataComponentPatch patch) {
        requireReadCaller(NativeItemComponentObservation.class, "encodePatch", "()Lnet/minecraft/nbt/Tag;");
        return net.minecraft.core.component.DataComponentPatch.CODEC.encodeStart(registries.createSerializationContext(READ_OPS), patch)
            .getOrThrow(error -> new IllegalArgumentException("Invalid actual native component patch: " + error));
    }

    public static Tag timerPatchForRead(net.minecraft.core.HolderLookup.Provider registries,
        net.minecraft.core.component.DataComponentPatch patch) {
        requireReadCaller(NativeTimerTransitionPlan.class, "nativeWeaponBefore",
            "(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/NativeItemComponentObservation;Lnet/farmingtales/ftboss/item/FoodWeaponExpectedTrace$Value;)Lnet/farmingtales/ftboss/item/FoodWeaponExpectedTrace$Value;");
        return net.minecraft.core.component.DataComponentPatch.CODEC.encodeStart(registries.createSerializationContext(READ_OPS), patch)
            .getOrThrow(message -> new IllegalArgumentException("Native timer component domain cannot be encoded: " + message));
    }

    public static Tag imageForRead(ItemStack stack, net.minecraft.core.HolderLookup.Provider registries) {
        requireReadCaller(NativeFoundationItemData.class, "image",
            "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;");
        return stack.saveOptional(new ReadRegistries(registries));
    }

    public static ItemStack decodeForRead(net.minecraft.core.HolderLookup.Provider registries, CompoundTag image) {
        requireReadCaller(NativeRepairStackImage.class, "decode",
            "(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/item/ItemStack;");
        return ItemStack.parseOptional(new ReadRegistries(registries), image);
    }

    public static CompoundTag foundationCompoundForRead404(Map<String, Tag> values) {
        StackWalker.StackFrame frame = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(NativeExpectedFieldObservation::foundationReadFrame404);
        boolean authentic = frame != null
            && frame.getDeclaringClass().getNestHost() == net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest.class
            && frame.getDeclaringClass().getName().equals("net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest$PrivateCompound")
            && frame.getMethodName().equals("finish") && frame.getDescriptor().equals("()Lnet/minecraft/nbt/CompoundTag;");
        if (!authentic || values == null || values.getClass() != HashMap.class) {
            throw new IllegalStateException("Private foundation read output requires its authentic unpublished builder");
        }
        CompoundTag output = newReadCompound();
        fields(output).putAll(values);
        return output;
    }

    private static StackWalker.StackFrame foundationReadFrame404(java.util.stream.Stream<StackWalker.StackFrame> frames) {
        return frames.skip(1).findFirst().orElse(null);
    }

    public static CompoundTag compositionCompoundForRead407(Map<String, Tag> values) {
        StackWalker.StackFrame frame = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(NativeExpectedFieldObservation::foundationReadFrame404);
        boolean authentic = frame != null && frame.getDeclaringClass().getNestHost() == FourWeaponFoundationAdapter.class
            && frame.getDeclaringClass().getName().equals("net.farmingtales.ftboss.item.FourWeaponFoundationAdapter$PrivateSnapshot407")
            && frame.getMethodName().equals("finish") && frame.getDescriptor().equals("()Lnet/minecraft/nbt/CompoundTag;");
        if (!authentic || values == null || values.getClass() != java.util.LinkedHashMap.class) {
            throw new IllegalStateException("Private composition read output requires its authentic unpublished builder");
        }
        CompoundTag output = newReadCompound();
        Map<String, Tag> privateFields = fields(output);
        for (Map.Entry<String, Tag> entry : values.entrySet()) privateFields.put(entry.getKey(), entry.getValue());
        return output;
    }

    public static CompoundTag fingerprintCompoundForRead416(CompoundTag seed, Map<String, Tag> values) {
        StackWalker.StackFrame frame = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(NativeExpectedFieldObservation::foundationReadFrame404);
        Class<?> owner = frame == null ? null : frame.getDeclaringClass();
        boolean author = owner != null
            && owner.getNestHost() == net.farmingtales.ftboss.util.foundations.FoundationAuthorRuleSnapshots.class
            && owner.getName().equals("net.farmingtales.ftboss.util.foundations.FoundationAuthorRuleSnapshots$PrivateFingerprint416");
        boolean canonical = owner != null
            && owner.getNestHost() == net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest.class
            && owner.getName().equals("net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest$PrivateFingerprint416");
        boolean material = owner != null && owner.getNestHost() == FoodWeaponMaterialFingerprint.class
            && owner.getName().equals("net.farmingtales.ftboss.item.FoodWeaponMaterialFingerprint$PrivateFingerprint416");
        if ((!author && !canonical && !material) || frame == null || !frame.getMethodName().equals("finish")
            || !frame.getDescriptor().equals("()Lnet/minecraft/nbt/CompoundTag;")
            || values == null || values.getClass() != java.util.LinkedHashMap.class
            || (seed != null && (!author || seed.getClass() != CompoundTag.class))) {
            throw new IllegalStateException("Private fingerprint output requires its authentic unpublished builder");
        }
        CompoundTag output = seed == null ? newReadCompound() : seed.copy();
        Map<String, Tag> privateFields = fields(output);
        for (Map.Entry<String, Tag> entry : values.entrySet()) privateFields.put(entry.getKey(), entry.getValue());
        return output;
    }

    public static CompoundTag socketRecipeCompoundForRead423(Map<String, Tag> values) {
        StackWalker.StackFrame frame = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(NativeExpectedFieldObservation::foundationReadFrame404);
        Class<?> owner = frame == null ? null : frame.getDeclaringClass();
        boolean socket = owner != null && owner.getNestHost() == FoodWeaponSourceLedger.class
            && owner.getName().equals("net.farmingtales.ftboss.item.FoodWeaponSourceLedger$PrivateRead423");
        boolean recipe = owner != null
            && owner.getNestHost() == net.farmingtales.ftboss.transaction.FoundationApprovedRules.class
            && owner.getName().equals("net.farmingtales.ftboss.transaction.FoundationApprovedRules$PrivateRead423");
        if ((!socket && !recipe) || frame == null || !frame.getMethodName().equals("finish")
            || !frame.getDescriptor().equals("()Lnet/minecraft/nbt/CompoundTag;")
            || values == null || values.getClass() != java.util.LinkedHashMap.class) {
            throw new IllegalStateException("Private socket or recipe output requires its authentic unpublished builder");
        }
        CompoundTag output = newReadCompound();
        Map<String, Tag> privateFields = fields(output);
        for (Map.Entry<String, Tag> entry : values.entrySet()) privateFields.put(entry.getKey(), entry.getValue());
        return output;
    }

    public static Tag recipeStackForRead426(ItemStack stack, net.minecraft.core.RegistryAccess registries) {
        requireReadCaller(net.farmingtales.ftboss.transaction.NativeRecipeEvidenceData.class, "stack",
            "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/nbt/CompoundTag;");
        return stack.saveOptional(new ReadRegistries(registries));
    }

    private static final class ReadRegistries implements net.minecraft.core.HolderLookup.Provider {
        private final net.minecraft.core.HolderLookup.Provider delegate;

        private ReadRegistries(net.minecraft.core.HolderLookup.Provider delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        public java.util.stream.Stream<net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>>> listRegistries() {
            return this.delegate.listRegistries();
        }

        @Override
        public <T> java.util.Optional<net.minecraft.core.HolderLookup.RegistryLookup<T>> lookup(
            net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<? extends T>> key) {
            return this.delegate.lookup(key);
        }

        @Override
        public <T> net.minecraft.core.HolderLookup.RegistryLookup<T> lookupOrThrow(
            net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<? extends T>> key) {
            return this.delegate.lookupOrThrow(key);
        }

        @Override
        public net.minecraft.core.HolderGetter.Provider asGetterLookup() {
            return this.delegate.asGetterLookup();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> net.minecraft.resources.RegistryOps<V> createSerializationContext(com.mojang.serialization.DynamicOps<V> ops) {
            return this.delegate.createSerializationContext(ops == net.minecraft.nbt.NbtOps.INSTANCE
                ? (com.mojang.serialization.DynamicOps<V>) READ_OPS : ops);
        }
    }

    private static final class ReadOps extends net.minecraft.nbt.NbtOps {
        @Override
        @SuppressWarnings("unchecked")
        public <U> U convertTo(com.mojang.serialization.DynamicOps<U> output, Tag input) {
            return super.convertTo(output == net.minecraft.nbt.NbtOps.INSTANCE
                ? (com.mojang.serialization.DynamicOps<U>) this : output, input);
        }

        @Override
        public Tag createMap(java.util.stream.Stream<com.mojang.datafixers.util.Pair<Tag, Tag>> values) {
            CompoundTag output = newReadCompound();
            Map<String, Tag> privateFields = fields(output);
            values.forEach(entry -> privateFields.put(entry.getFirst().getAsString(), entry.getSecond()));
            return output;
        }

        @Override
        public com.mojang.serialization.DataResult<Tag> mergeToMap(Tag prefix, Tag key, Tag value) {
            if (!(prefix instanceof CompoundTag || prefix instanceof net.minecraft.nbt.EndTag) || !(key instanceof net.minecraft.nbt.StringTag)) {
                return super.mergeToMap(prefix, key, value);
            }
            CompoundTag output = prefix instanceof CompoundTag compound ? copyReadCompound(compound) : newReadCompound();
            fields(output).put(key.getAsString(), value);
            return com.mojang.serialization.DataResult.success(output);
        }

        @Override
        public com.mojang.serialization.DataResult<Tag> mergeToMap(Tag prefix, com.mojang.serialization.MapLike<Tag> values) {
            if (!(prefix instanceof CompoundTag || prefix instanceof net.minecraft.nbt.EndTag)) return super.mergeToMap(prefix, values);
            CompoundTag output = prefix instanceof CompoundTag compound ? copyReadCompound(compound) : newReadCompound();
            Map<String, Tag> privateFields = fields(output);
            List<Tag> invalidKeys = new ArrayList<>();
            values.entries().forEach(entry -> {
                if (entry.getFirst() instanceof net.minecraft.nbt.StringTag key) privateFields.put(key.getAsString(), entry.getSecond());
                else invalidKeys.add(entry.getFirst());
            });
            return invalidKeys.isEmpty() ? com.mojang.serialization.DataResult.success(output)
                : com.mojang.serialization.DataResult.error(() -> "some keys are not strings: " + invalidKeys, output);
        }

        @Override
        public com.mojang.serialization.DataResult<Tag> mergeToMap(Tag prefix, Map<Tag, Tag> values) {
            if (!(prefix instanceof CompoundTag || prefix instanceof net.minecraft.nbt.EndTag)) return super.mergeToMap(prefix, values);
            return mergeToMap(prefix, com.mojang.serialization.MapLike.forMap(values, this));
        }

        @Override
        public com.mojang.serialization.RecordBuilder<Tag> mapBuilder() {
            return new ReadBuilder();
        }
    }

    private static final class ReadBuilder extends com.mojang.serialization.RecordBuilder.AbstractStringBuilder<Tag, CompoundTag> {
        private ReadBuilder() {
            super(READ_OPS);
        }

        @Override
        protected CompoundTag initBuilder() {
            return newReadCompound();
        }

        @Override
        protected CompoundTag append(String key, Tag value, CompoundTag output) {
            fields(output).put(key, value);
            return output;
        }

        @Override
        protected com.mojang.serialization.DataResult<Tag> build(CompoundTag entries, Tag prefix) {
            if (prefix == null || prefix == net.minecraft.nbt.EndTag.INSTANCE) return com.mojang.serialization.DataResult.success(entries);
            if (prefix instanceof CompoundTag compound) {
                CompoundTag output = copyReadCompound(compound);
                fields(output).putAll(fields(entries));
                return com.mojang.serialization.DataResult.success(output);
            }
            return com.mojang.serialization.DataResult.error(() -> "mergeToMap called with not a map: " + prefix, prefix);
        }
    }

    public static Throwable infrastructureFailure() {
        return infrastructureFailure;
    }

    private static Map<String, Tag> fields(CompoundTag var0) {
        Map var1 = ((Compound)var0).ftboss$rawCompoundFields();
        if (var1 != null && (var1.getClass() == HashMap.class || NativeFreshFactoryTrust298.readableMap(var0, var1))) {
            return var1;
        } else {
            throw new IllegalArgumentException("Unsupported raw backing-map implementation");
        }
    }

    private static CompoundTag stackTag(ItemStack stack) {
        NativeExpectedFieldObservation.Scope scope = current();
        if (scope == null || scope.sourceA != stack || scope.retired) {
            throw new IllegalStateException("Native expected custom data requires the current live source scope");
        }
        return NativeItemComponentObservation.capture(stack).expectedCustomData();
    }

    private static NativeExpectedFieldObservation.Scope current() {
        try {
            NativeExpectedFieldObservation.Scope var0 = CURRENT.get();
            if (var0 == null || !var0.closed && var0.thread == Thread.currentThread()) {
                return var0;
            } else {
                rejectExtra(var0, new IllegalStateException("Saved operation scope is closed or on another thread"));
                return null;
            }
        } catch (Throwable var1) {
            infrastructureFailure = var1;
            return null;
        }
    }

    private static void rejectExtra(NativeExpectedFieldObservation.Scope var0, Throwable var1) {
        if (var0 == null) {
            infrastructureFailure = var1;
        } else {
            var0.retired = true;
            if (var0.observationFailure == null) {
                var0.observationFailure = var1;
            }

            if (var0.domain != null) {
                synchronized (DOMAINS) {
                    var0.domain.retired = true;
                    if (var0.domain.failure == null) {
                        var0.domain.failure = var1;
                    }
                }
            }

            try {
                if (var0.sequence != null) {
                    var0.sequence.observationFailed(var0.step, 0, var1);
                }
            } catch (Throwable var5) {
                if (var0.observationFailure == null) {
                    var0.observationFailure = var5;
                }
            }
        }
    }

    private static void failed(NativeExpectedFieldObservation.Scope var0, Throwable var1) {
        rejectExtra(var0, var1);
    }

    private static boolean eligible(NativeExpectedFieldObservation.Scope var0) {
        if (var0 != null && !var0.retired && !var0.closed && var0.domain != null && !var0.domain.retired) {
            NativeExpectedFieldObservation.Domain var1 = var0.domain;
            if (var1.sourceA == var0.sourceA
                && var1.thread == Thread.currentThread()
                && var1.sequence == var0.sequence
                && var1.generation == var0.sequence.lineage.generation
                && var1.finalPublication == var0.finalPublication
                && var1.finalPublication == var0.sequence.lineage.publication
                && var1.ownerWriter == var0.writer
                && var1.ownerWriter == var0.sequence.lineage.issuer
                && var0.writer instanceof FoodWeaponSourceLedger.OwnerWriterPublication
                && var0.sequence.original.atLeaf(var0.leaf)) {
                return true;
            } else {
                rejectExtra(var0, new IllegalStateException("Saved source/publication/generation/thread/leaf domain differs"));
                return false;
            }
        } else {
            return false;
        }
    }

    private static void bind(NativeExpectedFieldObservation.Scope var0, FoodWeaponOriginalSequence.Session var1) {
        var0.sequence = var1;
        if (var0.sequence != null) {
            synchronized (DOMAINS) {
                var0.domain = DOMAINS.get(var0.sequence);
                if (var0.domain == null) {
                    var0.domain = new NativeExpectedFieldObservation.Domain(var0.sequence, var0.sourceA);
                    DOMAINS.put(var0.sequence, var0.domain);
                }
            }

            eligible(var0);
        }
    }

    private static NativeExpectedFieldObservation.Scope open(ItemStack var0, Object var1, Object var2, Object var3, ServerPlayer var4) {
        NativeExpectedFieldObservation.Scope var5 = current();
        FoodWeaponOriginalSequence.Session var6 = var1 == null ? null : FoodWeaponHitPlan.originalSequence((FoodWeaponHitPlan.FinalEffectsCommit)var1);
        NativeExpectedFieldObservation.Scope var7 = new NativeExpectedFieldObservation.Scope(
            var0,
            var1,
            var2,
            var4 != null && var3 == null && var6 != null ? var6.lineage.issuer : var3,
            var6 == null ? null : var6.lineage.publication,
            var4,
            var5
        );

        try {
            CURRENT.set(var7);
            if (var1 != null) {
                bind(var7, var6);
            }

            for (NativeExpectedFieldObservation.Scope var8 = var5; var8 != null; var8 = var8.previous) {
                if (var8.sequence != null && var8.sequence == var7.sequence) {
                    rejectExtra(var7, new IllegalStateException("Reentrant use of saved original sequence"));
                }
            }
        } catch (Throwable var9) {
            failed(var7, var9);
        }

        return var7;
    }

    private static void close(NativeExpectedFieldObservation.Scope var0) {
        if (var0 != null) {
            try {
                if (var0.previous == null) {
                    CURRENT.remove();
                } else {
                    CURRENT.set(var0.previous);
                }
            } catch (Throwable var5) {
                failed(var0, var5);
            } finally {
                var0.closed = true;
            }
        }
    }

    private static NativeExpectedFieldObservation.Fact fact(
        NativeExpectedFieldObservation.Scope var0, String var1, Object var2, Object var3, Object var4, Operation<?> var5
    ) {
        try {
            if (var0 == null) {
                return null;
            } else if (!var0.closed && var0.thread == Thread.currentThread()) {
                NativeExpectedFieldObservation.Fact var6 = new NativeExpectedFieldObservation.Fact(var0, var1, var2, var3, var4, var5);
                var0.facts.add(var6);
                return var6;
            } else {
                throw new IllegalStateException("Foreign event thread");
            }
        } catch (Throwable var7) {
            failed(var0, var7);
            return null;
        }
    }

    private static void before(NativeExpectedFieldObservation.Fact var0) {
        if (var0 != null) {
            NativeExpectedFieldObservation.Scope var1 = var0.scope;
            if (var1.active == var0.parent
                && var0.thread == var1.thread
                && var0.lifecycleDomain == var1.domain
                && var0.sourceA == var1.sourceA
                && var0.operation == var1.operation
                && (var0.lifecycleDomain == null || var0.generation == var0.lifecycleDomain.generation)) {
                var1.active = var0;
                beforeNativeTimerReceiver(var0);
                beforeNativeTimerTransition(var0);
                if (var0.site.equals("MAP_PUT")) {
                    beforeMap(var0);
                } else if (var0.site.equals("COPY")) {
                    beforeCopy(var0);
                } else if (var0.site.equals("TIMER_PUT")) {
                    beforeTimerPut(var0);
                } else if (var0.site.equals("RANGED_LONG") || var0.site.equals("RANGED_ROOT")) {
                    beforeRanged(var0);
                }
            } else {
                throw new IllegalStateException("Saved original operation/source/generation/parent entry order differs");
            }
        }
    }

    private static <Result> Result invoke(NativeExpectedFieldObservation.Fact var0, Operation<Result> var1, Object... var2) {
        try {
            before(var0);
        } catch (Throwable var18) {
            failed(var0 == null ? null : var0.scope, var18);
        }

        Object var4;
        try {
            if (var0 != null) {
                var0.attempted = true;
            }

            Object var3 = var1.call(var2);
            if (var0 != null) {
                var0.originalReturn = var3;
                var0.returned = true;
            }

            try {
                after(var0);
            } catch (Throwable var15) {
                failed(var0 == null ? null : var0.scope, var15);
            }

            var4 = var3;
        } catch (Throwable var16) {
            if (var0 != null) {
                var0.originalFailure = var16;
                var0.scope.originalFailure = var16;
            }

            throw var16;
        } finally {
            try {
                finish(var0);
            } catch (Throwable var14) {
                failed(var0 == null ? null : var0.scope, var14);
            }
        }

        return (Result)var4;
    }

    private static void finish(NativeExpectedFieldObservation.Fact var0) {
        if (var0 != null) {
            NativeExpectedFieldObservation.Scope var1 = var0.scope;
            if (var1.active != var0) {
                rejectExtra(var1, new IllegalStateException("Original child/return/throw order differs"));
            }

            var0.exit = ++var1.clock;
            var1.active = var0.parent;
            if (var0.site.equals("RANGED_LONG") || var0.site.equals("RANGED_ROOT")) {
                finishRanged(var1, var0);
            }
            finishNativeTimerTransition(var0);
        }
    }

    private static boolean parent(NativeExpectedFieldObservation.Fact var0, String var1) {
        return var0.parent != null
            && var0.parent.scope == var0.scope
            && var0.parent.site.equals(var1)
            && var0.parent.attempted
            && !var0.parent.returned
            && var0.parent.originalFailure == null
            && var0.parent.exit == 0L;
    }

    private static void require(boolean var0, NativeExpectedFieldObservation.Scope var1, String var2) {
        if (!var0) {
            rejectExtra(var1, new IllegalStateException(var2));
        }
    }

    private static CompoundTag ownedTimerRoot(NativeExpectedFieldObservation.Scope var0) {
        if (nativeTimerSequence(var0)) {
            return var0.nativeTimerTransition.sequenceReceiver();
        }
        CompoundTag var1 = stackTag(var0.sourceA);
        if (var1 != null && var1 == var0.sourceTag) {
            Tag var2 = fields(var1).get("FTBossFoodEvolution");
            return var2 instanceof CompoundTag var3 ? var3 : null;
        } else {
            rejectExtra(var0, new IllegalStateException("Original source tag pointer changed"));
            return null;
        }
    }

    private static NativeExpectedFieldObservation.Binding binding(NativeExpectedFieldObservation.Scope var0) {
        if (var0.acquisitionRoot != null && var0.rangedBuffer != null) {
            NativeExpectedFieldObservation.Binding var1 = new NativeExpectedFieldObservation.Binding(
                var0.acquisitionRoot, var0.acquisitionKey, var0.rangedBuffer
            );
            if (var0.persistentRoot != var1.root || var1.root != ((Actor)var0.player).ftboss$rawPersistentData()) {
                var0.unsupportedTopology = true;
            }

            if (var0.lastBinding != null
                && var0.lastBinding.bound != var1.bound
                && (
                    var0.active == null
                        || !var0.active.site.equals("RANGED_ROOT") && (!var0.active.site.equals("MAP_PUT") || !parent(var0.active, "RANGED_ROOT"))
                )) {
                var0.unsupportedTopology = true;
            }

            var0.lastBinding = var1;
            if (var0.unsupportedTopology) {
                rejectExtra(var0, new IllegalStateException("Unplanned original root detachment or rebind"));
            }

            return var1;
        } else {
            return null;
        }
    }

    private static void beforeCopy(NativeExpectedFieldObservation.Fact var0) {
        NativeExpectedFieldObservation.Scope var1 = var0.scope;
        if (eligible(var1)) {
            require(
                parent(var0, "TIMER")
                    && var1.step != null
                    && var1.timerReceiver != null
                    && var1.timerReceiver == ownedTimerRoot(var1)
                    && var0.receiver == var1.timerValue
                    && !var1.copyAttempted,
                var1,
                "Copy receiver/value/root or parent operation differs"
            );
            if (eligible(var1)) {
                var1.copyAttempted = true;
                var1.copyValue = (Tag)var0.receiver;
            }
        }
    }

    private static void beforeTimerPut(NativeExpectedFieldObservation.Fact var0) {
        NativeExpectedFieldObservation.Scope var1 = var0.scope;
        if (eligible(var1)) {
            require(
                parent(var0, "TIMER")
                    && var1.step != null
                    && !var1.putAttempted
                    && var1.copyReturned
                    && var1.timerReceiver == ownedTimerRoot(var1)
                    && var0.receiver == var1.timerReceiver
                    && Objects.equals(var1.timerKey, var0.key)
                    && var0.argument == var1.copyResult
                    && var1.copyValue == var1.timerValue
                    && var1.step.expected.kind == Kind.TIMER
                    && Objects.equals(var1.step.expected.key, var0.key),
                var1,
                "Timer physical receiver/key/copy-result or planned operation differs"
            );
            var1.putAttempted = true;
        }
    }

    private static void beforeRanged(NativeExpectedFieldObservation.Fact var0) {
        NativeExpectedFieldObservation.Scope var1 = var0.scope;
        var0.bindingBefore = binding(var1);
        if (eligible(var1)) {
            boolean var2 = var0.site.equals("RANGED_ROOT");
            require(
                parent(var0, "RANGED_SAVE")
                    && var1.acquisitionRoot != null
                    && "FTBossRangedProcTimers".equals(var1.acquisitionKey)
                    && (
                        var2
                            ? var0.receiver == var1.acquisitionRoot && var0.argument == var1.rangedBuffer && Objects.equals(var0.key, var1.acquisitionKey)
                            : var0.receiver == var1.rangedBuffer
                    ),
                var1,
                "Original ranged receiver/key/acquisition or parent differs"
            );
            if (eligible(var1)) {
                var1.kind = var2 ? Kind.RANGED_ROOT : Kind.RANGED_TIMER;
                var1.step = var1.sequence.originalBegins(var1.leaf, var1.kind, (String)var0.key, null);
                var0.originalStep = var1.step;
                if (var1.step == null) {
                    rejectExtra(var1, new IllegalStateException("No original preplanned ranged step"));
                } else {
                    State var3 = var1.step.expected.before;
                    require(
                        var3.rangedAcquired
                            && var3.rangedPlayerIdentity == var1.acquisitionRoot
                            && var3.rangedBufferIdentity == var1.rangedBuffer
                            && var3.rangedAttached == (var0.bindingBefore.bound == var1.rangedBuffer)
                            && FoodWeaponExpectedTrace.same(var3.player, NativeExpectedRawFields.observe(var1.acquisitionRoot))
                            && FoodWeaponExpectedTrace.same(var3.rangedBuffer, var0.bindingBefore.acquiredFields),
                        var1,
                        "Ranged expected domain identities/path/fields differ"
                    );
                    if (eligible(var1) && var2) {
                        var1.sequence.originalCopyFieldsObserved(var1.step, var0.argument, NativeExpectedRawFields.observe((Tag)var0.argument));
                        var1.sequence.entryReturned(var1.step, var0.receiver, var0.argument, var0.argument);
                    }
                }
            }
        }
    }

    private static void beforeMap(NativeExpectedFieldObservation.Fact var0) {
        NativeExpectedFieldObservation.Scope var1 = var0.scope;
        var0.bindingBefore = binding(var1);
        if (eligible(var1)) {
            require(
                var0.originalStep == var1.step
                    && var0.key instanceof String
                    && var0.argument instanceof Tag
                    && var0.parent != null
                    && var0.parent.scope == var1
                    && var0.parent.exit == 0L
                    && var0.parent.attempted,
                var1,
                "Map write lacks exact original parent entry"
            );
            if (eligible(var1)) {
                require(var0.physicalOwner != null && fields(var0.physicalOwner) == var0.receiver, var1, "Foreign physical backing map");
                if (eligible(var1)) {
                    NativeExpectedFieldObservation.Fact var2 = var0.parent;
                    if (parent(var0, "CUSTOM_DATA_COMMIT")) {
                        require(var2.nativeMergeBoundary != null, var1, "Native component Map write lacks its exact merge boundary");
                        if (eligible(var1)) var2.nativeMergeBoundary.beforeMap(var0.physicalOwner, var0.key, var0.argument, nativeComponentPublicationCaller());
                        return;
                    }
                    if (!parent(var0, "TIMER_PUT") && !parent(var0, "RANGED_ROOT")) {
                        if (parent(var0, "RANGED_LONG")) {
                            require(
                                var0.physicalOwner == var1.rangedBuffer
                                    && var0.physicalOwner == var2.receiver
                                    && Objects.equals(var0.key, var2.key)
                                    && var2.directMapChildren == 0
                                    && var0.argument.getClass() == LongTag.class
                                    && ((LongValue)var0.argument).ftboss$rawLong() == (Long)var2.argument,
                                var1,
                                "Foreign nested Map or substituted original LongTag"
                            );
                            if (eligible(var1)) {
                                var0.expectedFieldDomain = FoodWeaponExpectedTrace.Domain.RANGED_BUFFER;
                                var0.path = new String[0];
                                var1.sequence.originalCopyFieldsObserved(var1.step, var0.argument, NativeExpectedRawFields.observe((Tag)var0.argument));
                                var1.sequence.entryReturned(var1.step, var0.physicalOwner, null, var0.argument);
                            }
                        } else if (parent(var0, "RECEIVER")) {
                            if (nativeTimerSequence(var1)) {
                                require(var1.nativeTimerTransition.detachedProfileEvent(var0),var1,
                                    "Original native receiver write is not the preplanned detached Profile event");
                                if (eligible(var1)) {
                                    var0.expectedFieldDomain = FoodWeaponExpectedTrace.Domain.WEAPON;
                                    var0.path = new String[]{"components","minecraft:custom_data","FTBossFoodEvolution"};
                                    var2.directMapChildren++;
                                }
                                return;
                            }
                            if (var0.physicalOwner == var1.sourceTag && "FTBossFoodEvolution".equals(var0.key)) {
                                var0.path = new String[]{"tag"};
                            } else if (var0.physicalOwner == ownedTimerRoot(var1) && "Profile".equals(var0.key)) {
                                var0.path = new String[]{"tag", "FTBossFoodEvolution"};
                            }

                            require(
                                var0.path != null && var1.step != null && var1.prefixes < var1.step.expected.prefixCount(),
                                var1,
                                "Foreign original prefix owner/key/path"
                            );
                            if (eligible(var1)) {
                                var0.expectedFieldDomain = FoodWeaponExpectedTrace.Domain.WEAPON;
                                require(
                                    var1.step
                                        .expected
                                        .prefix(var1.prefixes)
                                        .matches(var0.expectedFieldDomain, var0.path, (String)var0.key, NativeExpectedRawFields.observe((Tag)var0.argument)),
                                    var1,
                                    "Original prefix differs from independent expected mutation"
                                );
                            }
                        } else {
                            rejectExtra(var1, new IllegalStateException("Nested or unbound Map write cannot claim a domain"));
                        }
                    } else {
                        require(
                            var0.physicalOwner == var2.receiver
                                && Objects.equals(var0.key, var2.key)
                                && var0.argument == var2.argument
                                && var2.directMapChildren == 0,
                            var1,
                            "Original put backing-map receiver/key/argument or child count differs"
                        );
                    }

                    if (eligible(var1)) {
                        var2.directMapChildren++;
                    }
                }
            }
        }
    }

    private static void after(NativeExpectedFieldObservation.Fact var0) {
        if (var0 != null) {
            NativeExpectedFieldObservation.Scope var1 = var0.scope;
            if (var0.site.equals("PERSISTENT")) {
                require(parent(var0, "RANGED_SAVE") && var0.receiver == var1.player, var1, "Foreign original player getter receiver");
                CompoundTag var2 = (CompoundTag)var0.originalReturn;
                require(
                    var2 == ((Actor)var0.receiver).ftboss$rawPersistentData() && (var1.persistentRoot == null || var1.persistentRoot == var2),
                    var1,
                    "Original persistent getter returned foreign root"
                );
                if (var1.persistentRoot == null) {
                    var1.persistentRoot = var2;
                }
            } else if (var0.site.equals("ACQUIRE")) {
                acquire(var0);
            } else if (var0.site.equals("RECEIVER")) {
                if (!nativeTimerSequence(var1)) {
                    require(
                        parent(var0, "TIMER") && var0.receiver == var1.sourceA && var1.timerReceiver == null && var0.originalReturn == ownedTimerRoot(var1),
                        var1,
                        "writableData returned foreign source root"
                    );
                    if (eligible(var1)) {
                        var1.timerReceiver = (CompoundTag)var0.originalReturn;
                    }
                }
            } else if (var0.site.equals("COPY")) {
                if (var1.copyAttempted && !var1.copyReturned) {
                    var1.copyResult = (Tag)var0.originalReturn;
                    var1.copyReturned = true;
                }

                if (eligible(var1) && var1.step != null) {
                    var0.rawFields = NativeExpectedRawFields.observe(var1.copyResult);
                    var1.sequence.originalCopyFieldsObserved(var1.step, var1.copyResult, var0.rawFields);
                }
            } else if (var0.site.equals("MAP_PUT")) {
                afterMap(var0);
            } else if (var0.site.equals("TIMER_PUT")) {
                if (eligible(var1)) {
                    require(
                        var0.directMapChildren == 1 && var0.receiver == ownedTimerRoot(var1), var1, "Timer original chain lacks one exact backing-map child"
                    );
                    if (eligible(var1)) {
                        var1.sequence
                            .writeObserved(
                                var1.step,
                                var0.receiver,
                                FoodWeaponExpectedTrace.Domain.WEAPON,
                                nativeTimerSequence(var1) ? new String[]{"components","minecraft:custom_data","FTBossFoodEvolution"}
                                    : new String[]{"tag", "FTBossFoodEvolution"},
                                (String)var0.key,
                                NativeExpectedRawFields.observe((Tag)var0.argument)
                            );
                    }
                }
            } else if (var0.site.equals("RANGED_LONG") || var0.site.equals("RANGED_ROOT")) {
                var0.bindingAfter = binding(var1);
                if (eligible(var1)) {
                    require(var0.directMapChildren == 1, var1, "Ranged original chain lacks one exact backing-map child");
                    if (eligible(var1) && var0.site.equals("RANGED_ROOT")) {
                        var1.sequence
                            .writeObserved(
                                var1.step,
                                var0.receiver,
                                FoodWeaponExpectedTrace.Domain.PLAYER,
                                new String[0],
                                (String)var0.key,
                                NativeExpectedRawFields.observe((Tag)var0.argument)
                            );
                    }
                }
            }
            afterNativeTimerReceiver(var0);
            afterNativeTimerTransition(var0);
        }
    }

    private static void acquire(NativeExpectedFieldObservation.Fact var0) {
        NativeExpectedFieldObservation.Scope var1 = var0.scope;
        require(
            parent(var0, "RANGED_SAVE")
                && var0.receiver == var1.persistentRoot
                && var0.receiver == ((Actor)var1.player).ftboss$rawPersistentData()
                && "FTBossRangedProcTimers".equals(var0.key)
                && var1.acquisitionRoot == null,
            var1,
            "Foreign or repeated original compound acquisition"
        );
        if (var1.acquisitionRoot == null) {
            var1.acquisitionRoot = (CompoundTag)var0.receiver;
            var1.acquisitionKey = (String)var0.key;
            var1.rangedBuffer = (CompoundTag)var0.originalReturn;
            var0.bindingAfter = binding(var1);
            var0.rawFields = NativeExpectedRawFields.observe(var1.rangedBuffer);
        }

        if (eligible(var1)) {
            FoodWeaponOriginalSequence.Step var2 = var1.sequence.originalBegins(var1.leaf, Kind.RANGED_ACQUIRE, (String)var0.key, null);
            var0.originalStep = var2;

            try {
                require(
                    var2 != null
                        && var2.expected.after.rangedPlayerIdentity == var0.receiver
                        && var2.expected.after.rangedBufferIdentity == var0.originalReturn,
                    var1,
                    "Original getter identities differ from independently preplanned acquisition"
                );
                if (eligible(var1)) {
                    var1.sequence.rangedAcquired(var2, var0.receiver, var0.originalReturn);
                }
            } catch (Throwable var12) {
                failed(var1, var12);
            } finally {
                try {
                    if (var2 != null) {
                        var1.sequence.close(var2);
                    }
                } catch (Throwable var11) {
                    failed(var1, var11);
                }
            }
        }
    }

    private static void finishRanged(NativeExpectedFieldObservation.Scope var0, NativeExpectedFieldObservation.Fact var1) {
        FoodWeaponOriginalSequence.Step var2 = var1.originalStep;
        if (var2 != null && (var0.kind == Kind.RANGED_TIMER || var0.kind == Kind.RANGED_ROOT)) {
            try {
                if (var1.originalFailure != null) {
                    var0.sequence.originalThrown(var2, var1.originalFailure);
                } else if (eligible(var0)) {
                    var0.sequence.originalReturned(var2, var1.originalReturn);
                }
            } catch (Throwable var12) {
                failed(var0, var12);
            } finally {
                try {
                    var0.sequence.close(var2);
                } catch (Throwable var11) {
                    failed(var0, var11);
                }

                var0.step = null;
                var0.kind = null;
            }
        }
    }

    private static void afterMap(NativeExpectedFieldObservation.Fact var0) {
        NativeExpectedFieldObservation.Scope var1 = var0.scope;
        var0.bindingAfter = binding(var1);
        if (eligible(var1)) {
            require(
                fields(var0.physicalOwner) == var0.receiver && fields(var0.physicalOwner).get((String)var0.key) == var0.argument,
                var1,
                "Original map chain did not publish its exact argument to the bound backing map"
            );
            if (eligible(var1)) {
                var0.rawFields = NativeExpectedRawFields.observe((Tag)var0.argument);
                if (parent(var0, "CUSTOM_DATA_COMMIT")) {
                    require(var0.parent.nativeMergeBoundary != null, var1, "Native component Map return lacks its exact merge boundary");
                    if (eligible(var1)) var0.parent.nativeMergeBoundary.afterMap(var0.physicalOwner, var0.key, var0.argument, var0.originalReturn);
                    return;
                }
                if (parent(var0, "RECEIVER")) {
                    if (nativeTimerSequence(var1)) {
                        require(var1.nativeTimerTransition.detachedProfileEvent(var0),var1,
                            "Native detached Profile return belongs to another original event");
                        return;
                    }
                    require(
                        stackTag(var1.sourceA) == var1.sourceTag
                            && (var0.path.length == 1 ? ownedTimerRoot(var1) == var0.argument : ownedTimerRoot(var1) == var0.physicalOwner),
                        var1,
                        "Original prefix backing map is no longer attached to the saved source path"
                    );
                    if (!eligible(var1)) {
                        return;
                    }

                    var1.sequence.prefixObserved(var1.step, var0.expectedFieldDomain, var0.path, (String)var0.key, var0.rawFields);
                    var1.prefixes++;
                } else if (parent(var0, "RANGED_LONG")) {
                    var1.sequence.writeObserved(var1.step, var0.physicalOwner, var0.expectedFieldDomain, var0.path, (String)var0.key, var0.rawFields);
                }
            }
        }
    }

    public static void timer(
        ItemStack var0,
        Profile var1,
        String var2,
        Tag var3,
        FoodWeaponSourceLedger.OwnerWriterStep var4,
        Object var5,
        int var6,
        Object var7,
        Object var8,
        Operation<Void> var9
    ) {
        NativeExpectedFieldObservation.Scope var10 = null;

        try {
            var10 = open(var0, var5, var8, var7, null);
            var10.timerKey = var2;
            var10.timerValue = var3;
        } catch (Throwable var16) {
            failed(var10, var16);
        }

        try {
            invoke(fact(var10, "TIMER", var0, var2, var3, var9), var9, var0, var1, var2, var3, var4, var5, var6, var7, var8);
        } finally {
            close(var10);
        }
    }

    public static void actualTimerStep(Object var0, Object var1, Object var2) {
        NativeExpectedFieldObservation.Scope var3 = current();

        try {
            if (var3 == null) {
                return;
            }

            require(
                var3.commit == var0 && var3.leaf == var1 && var3.step == null && var3.active != null && var3.active.site.equals("TIMER"),
                var3,
                "Actual TIMER step has foreign caller/parent"
            );
            if (!eligible(var3)) {
                return;
            }

            var3.step = (FoodWeaponOriginalSequence.Step)var2;
            require(
                var3.step != null
                    && var3.step.session == var3.sequence
                    && var3.step.expected.kind == Kind.TIMER
                    && Objects.equals(var3.timerKey, var3.step.expected.key),
                var3,
                "Original returned TIMER step identity differs"
            );
            if (!eligible(var3)) {
                return;
            }

            var3.kind = Kind.TIMER;
            var3.sourceTag = stackTag(var3.sourceA);
            require(
                FoodWeaponExpectedTrace.same(
                    var3.step.expected.before.weapon,
                    NativeInventoryItemTrace.quote(NativeFoundationItemData.image(var3.sourceA))
                ),
                var3,
                "Initial native inventory patch differs from independent expected BEFORE"
            );
        } catch (Throwable var5) {
            failed(var3, var5);
        }
    }

    public static CompoundTag timerReceiver(ItemStack var0, Profile var1, Operation<CompoundTag> var2) {
        return invoke(fact(current(), "RECEIVER", var0, null, var1, var2), var2, var0, var1);
    }

    public static Tag copiedArgument(Tag var0, Operation<Tag> var1) {
        return invoke(fact(current(), "COPY", var0, null, null, var1), var1, var0);
    }

    public static Tag timerPut(FoodWeaponSourceLedger.OwnerWriterStep var0, ItemStack var1, CompoundTag var2, String var3, Tag var4, Operation<Tag> var5) {
        NativeExpectedFieldObservation.Scope var6 = current();

        try {
            if (var6 != null) {
                require(var6.sourceA == var1, var6, "Original TIMER sourceA differs");
            }
        } catch (Throwable var8) {
            failed(var6, var8);
        }

        return invoke(fact(var6, "TIMER_PUT", var2, var3, var4, var5), var5, var2, var3, var4);
    }

    public static Object mapPut(CompoundTag var0, Map<Object, Object> var1, Object var2, Object var3, Operation<Object> var4) {
        NativeExpectedFieldObservation.Scope var5 = current();
        NativeExpectedFieldObservation.Fact var6 = fact(var5, "MAP_PUT", var1, var2, var3, var4);
        if (var6 != null) {
            var6.physicalOwner = var0;
        }

        return invoke(var6, var4, var1, var2, var3);
    }

    public static CompoundTag persistentData(Entity var0, Operation<CompoundTag> var1) {
        return invoke(fact(current(), "PERSISTENT", var0, null, null, var1), var1, var0);
    }

    public static CompoundTag compound(CompoundTag var0, String var1, Operation<CompoundTag> var2) {
        return invoke(fact(current(), "ACQUIRE", var0, var1, null, var2), var2, var0, var1);
    }

    public static void rangedLong(CompoundTag var0, String var1, long var2, Operation<Void> var4) {
        invoke(fact(current(), "RANGED_LONG", var0, var1, var2, var4), var4, var0, var1, var2);
    }

    public static Tag rangedRoot(CompoundTag var0, String var1, Tag var2, Operation<Tag> var3) {
        return invoke(fact(current(), "RANGED_ROOT", var0, var1, var2, var3), var3, var0, var1, var2);
    }

    public static void rangedSave(ServerPlayer var0, DamageSource var1, CompoundTag var2, ItemStack var3, Object var4, Object var5, Operation<Void> var6) {
        NativeExpectedFieldObservation.Scope var7 = null;

        try {
            var7 = open(var3, var4, var5, null, var0);
        } catch (Throwable var13) {
            failed(var7, var13);
        }

        try {
            invoke(fact(var7, "RANGED_SAVE", var0, var1, var2, var6), var6, var0, var1, var2);
        } finally {
            close(var7);
        }
    }

    public static CompoundTag capture(
        ServerPlayer var0, ItemStack var1, net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind var2, InteractionHand var3, Operation<CompoundTag> var4
    ) {
        NativeExpectedFieldObservation.Scope var5 = null;

        try {
            var5 = open(var1, null, null, null, var0);
        } catch (Throwable var11) {
            failed(var5, var11);
        }

        CompoundTag var6;
        try {
            var6 = invoke(fact(var5, "CAPTURE", var1, var2, var3, var4), var4, var0, var1, var2, var3);
        } finally {
            close(var5);
        }

        return var6;
    }

    public static void originalTagAssigned(ItemStack var0, CompoundTag var1) {
        NativeExpectedFieldObservation.Scope var2 = current();

        try {
            NativeExpectedFieldObservation.Fact var3 = fact(var2, "SOURCE_TAG_ASSIGNED", var0, "tag", var1, null);
            if (var3 != null) {
                var3.originalReturn = var1;
                var3.returned = true;
                var3.exit = ++var2.clock;
            }

            if (var2 == null || !eligible(var2)) {
                return;
            }

            require(
                var2.active != null
                    && var2.active.site.equals("RECEIVER")
                    && var2.sourceA == var0
                    && var2.sourceTag == null
                    && var1 == stackTag(var0)
                    && var2.step != null
                    && var2.prefixes < var2.step.expected.prefixCount(),
                var2,
                "Foreign original source tag assignment"
            );
            if (!eligible(var2)) {
                return;
            }

            Value var4 = NativeExpectedRawFields.observe(var1);
            require(
                var2.step.expected.prefix(var2.prefixes).matches(FoodWeaponExpectedTrace.Domain.WEAPON, new String[0], "tag", var4),
                var2,
                "Source tag assignment is not the independent expected prefix"
            );
            if (!eligible(var2)) {
                return;
            }

            var2.sourceTag = var1;
            var2.sequence.prefixObserved(var2.step, FoodWeaponExpectedTrace.Domain.WEAPON, new String[0], "tag", var4);
            var2.prefixes++;
        } catch (Throwable var5) {
            failed(var2, var5);
        }
    }

    public static void aliasReturned(Object var0, Object var1, Object var2) {
        NativeExpectedFieldObservation.Scope var3 = current();
        NativeExpectedFieldObservation.Fact var4 = fact(var3, "ALIAS_RETURN", var0, var1, null, null);
        if (var4 != null) {
            var4.originalReturn = var2;
            var4.returned = true;
            var4.exit = ++var3.clock;
        }
    }

    public static void providerFields(Object var0) {
        NativeExpectedFieldObservation.Scope var1 = current();
        try {
            if (var1 == null) return;
            if (!(var0 instanceof net.neoforged.neoforge.capabilities.ItemCapability)) {
                rejectExtra(var1, new IllegalStateException("Unsupported native item capability registration receiver"));
                return;
            }
            NativeItemCapabilityObservation snapshot = NativeItemCapabilityObservation.capture();
            NativeExpectedFieldObservation.Fact var3 = fact(var1, "PROVIDER_FIELDS", var0, null, snapshot, null);
            if (var3 != null) {
                var3.originalReturn = snapshot;
                var3.returned = true;
                var3.exit = ++var1.clock;
            }
            rejectExtra(var1, new IllegalStateException("Provider mutation is outside the selected field catalogue"));
        } catch (Throwable var4) {
            failed(var1, var4);
        }
    }

    private static void nativeProviderQueryCaller(String callback) {
        var frames = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(3L).toList());
        Class<?> capability = net.neoforged.neoforge.capabilities.ItemCapability.class;
        if (frames.size() != 3 || frames.get(0).getDeclaringClass() != NativeExpectedFieldObservation.class
            || !frames.get(0).getMethodName().equals(callback)
            || frames.get(1).getDeclaringClass() != capability
            || !frames.get(1).getMethodName().endsWith("$ftboss$nativeProviderQuery")
            || !frames.get(1).getDescriptor().equals("(Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
            || frames.get(2).getDeclaringClass() != capability || !frames.get(2).getMethodName().equals("getCapability")
            || !frames.get(2).getDescriptor().equals("(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;)Ljava/lang/Object;")) {
            throw new IllegalStateException("Foreign native provider query evidence caller");
        }
    }

    public static Object nativeProviderQueryBefore(Object capability, Object provider, ItemStack stack, Object context) {
        nativeProviderQueryCaller("nativeProviderQueryBefore");
        try {
            NativeOpaqueProviderReadFence201.before(provider,stack);
        } catch (Throwable observationFailure) {
        }
        NativeExpectedFieldObservation.Scope scope = current();
        if (scope == null) return null;
        NativeExpectedFieldObservation.Fact event = null;
        try {
            event = fact(scope, "PROVIDER_QUERY", capability, provider, stack, null);
            if (event == null) return null;
            event.nativeProviderContext = context;
            event.attempted = true;
            scope.active = event;
            event.nativeProviderBefore = NativeItemCapabilityObservation.capture();
            rejectExtra(scope, new IllegalStateException("Opaque native provider query is outside the signed field catalogue"));
        } catch (Throwable failure) {
            failed(scope, failure);
        }
        return event;
    }

    private static void finishNativeProviderQuery(Object token, Object result, Throwable originalFailure) {
        if (token == null) return;
        NativeExpectedFieldObservation.Scope scope = current();
        if (!(token instanceof NativeExpectedFieldObservation.Fact event) || scope == null || event.scope != scope
            || event.thread != Thread.currentThread() || scope.active != event || !event.site.equals("PROVIDER_QUERY")
            || !event.attempted || event.returned || event.originalFailure != null || event.exit != 0L) {
            if (scope != null) rejectExtra(scope, new IllegalStateException("Foreign native provider query completion"));
            return;
        }
        try {
            event.originalReturn = result;
            event.originalFailure = originalFailure;
            event.returned = originalFailure == null;
            event.exit = ++scope.clock;
            event.nativeProviderAfter = NativeItemCapabilityObservation.capture();
        } catch (Throwable observationFailure) {
            failed(scope, observationFailure);
        } finally {
            scope.active = event.parent;
        }
    }

    public static void nativeProviderQueryReturned(Object token, Object result) {
        nativeProviderQueryCaller("nativeProviderQueryReturned");
        finishNativeProviderQuery(token, result, null);
    }

    public static void nativeProviderQueryFailed(Object token, Throwable failure) {
        nativeProviderQueryCaller("nativeProviderQueryFailed");
        finishNativeProviderQuery(token, null, failure);
    }

    static final class Binding {
        final CompoundTag root;
        final String key;
        final CompoundTag acquired;
        final Tag bound;
        final Map<String, Tag> rootMap;
        final Map<String, Tag> acquiredMap;
        final Value acquiredFields;
        final Value boundFields;

        Binding(CompoundTag var1, String var2, CompoundTag var3) {
            this.root = var1;
            this.key = var2;
            this.acquired = var3;
            this.rootMap = NativeExpectedFieldObservation.fields(var1);
            this.acquiredMap = NativeExpectedFieldObservation.fields(var3);
            this.bound = this.rootMap.get(var2);
            this.acquiredFields = NativeExpectedRawFields.observe(var3);
            this.boundFields = NativeExpectedRawFields.observe(this.bound);
        }
    }

    static final class Domain {
        final FoodWeaponOriginalSequence.Session sequence;
        final ItemStack sourceA;
        final Object finalPublication;
        final Object ownerWriter;
        final long generation;
        final Thread thread = Thread.currentThread();
        volatile boolean retired;
        volatile Throwable failure;

        Domain(FoodWeaponOriginalSequence.Session var1, ItemStack var2) {
            this.sequence = var1;
            this.sourceA = var2;
            this.finalPublication = var1.lineage.publication;
            this.ownerWriter = var1.lineage.issuer;
            this.generation = var1.lineage.generation;
        }
    }

    static final class Fact {
        final NativeExpectedFieldObservation.Scope scope;
        final NativeExpectedFieldObservation.Domain lifecycleDomain;
        final ItemStack sourceA;
        final Object operation;
        final long generation;
        final NativeExpectedFieldObservation.Fact parent;
        final String site;
        final Object receiver;
        final Object key;
        final Object argument;
        final Operation<?> continuation;
        final Thread thread = Thread.currentThread();
        final long entry;
        long exit;
        Object originalReturn;
        Throwable originalFailure;
        Value rawFields;
        NativeTimerBufferPlan nativeTimerBuffer;
        NativeNbtGraphObservation nativeTimerBufferImage;
        boolean nativeTimerBufferMatched;
        NativeComponentCommitPlan nativeTransition;
        NativeComponentMergeBoundary391 nativeMergeBoundary;
        boolean nativeTransitionMatched;
        NativeExpectedFieldObservation.Binding bindingBefore;
        NativeExpectedFieldObservation.Binding bindingAfter;
        boolean attempted;
        boolean returned;
        int directMapChildren;
        Object nativeProviderContext;
        NativeItemCapabilityObservation nativeProviderBefore;
        NativeItemCapabilityObservation nativeProviderAfter;
        CompoundTag physicalOwner;
        FoodWeaponOriginalSequence.Step originalStep;
        FoodWeaponExpectedTrace.Domain expectedFieldDomain;
        String[] path;

        Fact(NativeExpectedFieldObservation.Scope var1, String var2, Object var3, Object var4, Object var5, Operation<?> var6) {
            this.scope = var1;
            this.lifecycleDomain = var1.domain;
            this.sourceA = var1.sourceA;
            this.operation = var1.operation;
            this.generation = var1.domain == null ? -1L : var1.domain.generation;
            this.originalStep = var1.step;
            this.parent = var1.active;
            this.site = var2;
            this.receiver = var3;
            this.key = var4;
            this.argument = var5;
            this.continuation = var6;
            this.entry = ++var1.clock;
        }
    }

    static final class Scope {
        final ItemStack sourceA;
        final Object commit;
        final Object leaf;
        final Object writer;
        final Object finalPublication;
        final ServerPlayer player;
        final Object operation = new Object();
        final NativeExpectedFieldObservation.Scope previous;
        final Thread thread = Thread.currentThread();
        final List<NativeExpectedFieldObservation.Fact> facts = new ArrayList<>();
        NativeExpectedFieldObservation.Domain domain;
        FoodWeaponOriginalSequence.Session sequence;
        FoodWeaponOriginalSequence.Step step;
        Kind kind;
        NativeExpectedFieldObservation.Fact active;
        long clock;
        Throwable observationFailure;
        Throwable originalFailure;
        boolean retired;
        boolean closed;
        CompoundTag sourceTag;
        CompoundTag timerReceiver;
        Tag timerValue;
        Tag copyValue;
        Tag copyResult;
        String timerKey;
        boolean copyAttempted;
        boolean copyReturned;
        boolean putAttempted;
        int prefixes;
        CompoundTag persistentRoot;
        CompoundTag acquisitionRoot;
        String acquisitionKey;
        CompoundTag rangedBuffer;
        NativeExpectedFieldObservation.Binding lastBinding;
        boolean unsupportedTopology;
        NativeTimerTransitionPlan nativeTimerTransition;

        Scope(ItemStack var1, Object var2, Object var3, Object var4, Object var5, ServerPlayer var6, NativeExpectedFieldObservation.Scope var7) {
            this.sourceA = var1;
            this.commit = var2;
            this.leaf = var3;
            this.writer = var4;
            this.finalPublication = var5;
            this.player = var6;
            this.previous = var7;
        }
    }

    private static void nativeTimerMutableNodes(Tag tag, IdentityHashMap<Object, Boolean> nodes) {
        if (tag instanceof CompoundTag compound) {
            if (nodes.put(tag, Boolean.TRUE) != null) return;
            var fields = ((NativeExpectedFieldViews.Compound) compound).ftboss$rawCompoundFields();
            nodes.put(fields, Boolean.TRUE);
            for (Tag child : fields.values()) nativeTimerMutableNodes(child, nodes);
        } else if (tag instanceof net.minecraft.nbt.ListTag list) {
            if (nodes.put(tag, Boolean.TRUE) != null) return;
            var fields = ((NativeExpectedFieldViews.Sequence) list).ftboss$rawListFields();
            nodes.put(fields, Boolean.TRUE);
            for (Tag child : fields) nativeTimerMutableNodes(child, nodes);
        } else if (tag instanceof net.minecraft.nbt.ByteArrayTag) {
            nodes.put(tag, Boolean.TRUE);
            nodes.put(((NativeExpectedFieldViews.Bytes) tag).ftboss$rawBytes(), Boolean.TRUE);
        } else if (tag instanceof net.minecraft.nbt.IntArrayTag) {
            nodes.put(tag, Boolean.TRUE);
            nodes.put(((NativeExpectedFieldViews.Ints) tag).ftboss$rawInts(), Boolean.TRUE);
        } else if (tag instanceof net.minecraft.nbt.LongArrayTag) {
            nodes.put(tag, Boolean.TRUE);
            nodes.put(((NativeExpectedFieldViews.Longs) tag).ftboss$rawLongs(), Boolean.TRUE);
        }
    }

    private static void beforeNativeTimerReceiver(NativeExpectedFieldObservation.Fact fact) {
        if (!fact.site.equals("RECEIVER")) return;
        NativeExpectedFieldObservation.Scope scope = fact.scope;
        boolean actualSource = parent(fact, "TIMER") && fact.receiver == scope.sourceA;
        require(actualSource, scope, "Native writableData receiver requires the exact current TIMER source");
        if (actualSource) {
            NativeItemComponentObservation beforeImage = NativeItemComponentObservation.capture(scope.sourceA);
            CompoundTag root = beforeImage.expectedCustomData();
            IdentityHashMap<Object, Boolean> nodes = new IdentityHashMap<>();
            nativeTimerMutableNodes(root, nodes);
            fact.nativeTimerBuffer = NativeTimerBufferPlan.prepare(scope.sourceA, (Profile) fact.argument, beforeImage, root, nodes);
        }
    }

    private static void afterNativeTimerReceiver(NativeExpectedFieldObservation.Fact fact) {
        if (fact.nativeTimerBuffer == null) return;
        CompoundTag returned = (CompoundTag) fact.originalReturn;
        fact.nativeTimerBufferImage = NativeNbtGraphObservation.capture(returned);
        NativeItemComponentObservation currentImage = NativeItemComponentObservation.capture(fact.scope.sourceA);
        IdentityHashMap<Object, Boolean> detached = new IdentityHashMap<>();
        nativeTimerMutableNodes(returned, detached);
        fact.nativeTimerBufferMatched = fact.nativeTimerBuffer.matches(returned, fact.nativeTimerBufferImage, currentImage.expectedCustomData(), detached);
        require(fact.nativeTimerBufferMatched, fact.scope, "Native writableData return differs from its detached pre-write profile plan");
    }

    static void nativeComponentCommit(ItemStack stack, String key, java.util.function.Consumer<CompoundTag> original) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != NativeLifecycleItemData.class) {
            throw new IllegalStateException("Native component commit observation requires the authentic lifecycle caller");
        }
        nativeComponentCommit(stack, key, null, null, original);
    }

    static void nativeComponentCommit(ItemStack stack, String key, CompoundTag deltaBefore, CompoundTag deltaAfter,
            java.util.function.Consumer<CompoundTag> original) {
        Class<?> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        if (caller != NativeLifecycleItemData.class && caller != NativeExpectedFieldObservation.class) {
            throw new IllegalStateException("Native component transition observation requires the authentic lifecycle caller");
        }
        NativeExpectedFieldObservation.Scope scope = current();
        NativeExpectedFieldObservation.Fact assignment = null;
        try {
            if (scope != null) {
                NativeItemComponentObservation beforeImage = NativeItemComponentObservation.capture(stack);
                assignment = fact(scope, "CUSTOM_DATA_COMMIT", stack, key, beforeImage, null);
                before(assignment);
                require(scope.sourceA == stack, scope, "Native component commit has a foreign source stack");
                if (!scope.retired && deltaBefore != null && deltaAfter != null) {
                    CompoundTag root = beforeImage.expectedCustomData();
                    assignment.nativeTransition = NativeComponentCommitPlan.prepare(stack, key, deltaBefore, deltaAfter, beforeImage, root);
                    require(assignment.nativeTransition.ready(stack, key, deltaBefore, deltaAfter, root), scope,
                        "Native component BEFORE or declared delta changed before the original write");
                }
                if (assignment.nativeTransition != null) {
                    assignment.nativeMergeBoundary = new NativeComponentMergeBoundary391(stack, key, deltaBefore, deltaAfter, beforeImage, beforeImage.expectedCustomData());
                }
                beforeNativeTimerComponentCommit(assignment, deltaBefore, deltaAfter);
            }
        } catch (Throwable failure) {
            failed(scope, failure);
        }
        final NativeExpectedFieldObservation.Fact commitFact = assignment;
        try {
            if (assignment != null) assignment.attempted = true;
            FoodWeaponData.edit(stack, key, current -> {
                try {
                    if (commitFact != null && !scope.retired) {
                        require(scope.active == commitFact && commitFact.nativeMergeBoundary != null, scope,
                            "Native component callback lacks its exact active merge plan");
                        if (!scope.retired) commitFact.nativeMergeBoundary.bind(current);
                    }
                } catch (Throwable failure) {
                    failed(scope, failure);
                }
                original.accept(current);
                try {
                    if (commitFact != null && !scope.retired) commitFact.nativeMergeBoundary.bodyReturned();
                } catch (Throwable failure) {
                    failed(scope, failure);
                }
            });
            if (assignment != null) assignment.returned = true;
        } catch (Throwable originalFailure) {
            if (assignment != null) {
                assignment.originalFailure = originalFailure;
                scope.originalFailure = originalFailure;
            }
            throw originalFailure;
        } finally {
            try {
                if (assignment != null) {
                    NativeItemComponentObservation afterImage = NativeItemComponentObservation.capture(stack);
                    assignment.originalReturn = afterImage;
                    if (assignment.nativeTransition != null && assignment.originalFailure == null && !scope.retired) {
                        assignment.nativeTransitionMatched = assignment.nativeTransition.matches(afterImage, afterImage.expectedCustomData())
                            && assignment.nativeMergeBoundary != null && assignment.nativeMergeBoundary.installed(afterImage.expectedCustomData());
                        require(assignment.nativeTransitionMatched, scope, "Actual native component commit differs from its pre-write immutable delta plan");
                    }
                    if (eligible(scope)) {
                        require(nativeTimerSequence(scope) && assignment.nativeTransitionMatched
                            && scope.nativeTimerTransition.sequenceBeforeInstallation(scope.step),scope,
                            "Native component assignment lacks the actual independent TIMER operation and original copy/put delta");
                    }
                }
            } catch (Throwable failure) {
                failed(scope, failure);
            }
            try {
                afterNativeTimerTransition(assignment);
                finish(assignment);
            } catch (Throwable failure) {
                failed(scope, failure);
            }
        }
    }

    static String[] nativeComponentMergeKeys391(CompoundTag tag) {
        Class<?> actual = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames ->
            frames.skip(1).findFirst().map(StackWalker.StackFrame::getDeclaringClass).orElse(null));
        if (actual != NativeComponentMergeBoundary391.class && actual != NativeLifecycleItemData.class) {
            throw new IllegalStateException("Native component merge key snapshots require the original merge or its exact observer plan");
        }
        return fields(tag).keySet().toArray(String[]::new);
    }

    static void nativeComponentMergeNodes391(Tag tag, IdentityHashMap<Object,Boolean> nodes) {
        Class<?> actual = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames ->
            frames.skip(1).findFirst().map(StackWalker.StackFrame::getDeclaringClass).orElse(null));
        if (actual != NativeComponentMergeBoundary391.class) {
            throw new IllegalStateException("Native component merge node reads require their exact observer plan");
        }
        nativeTimerMutableNodes(tag, nodes);
    }

    private static boolean nativeComponentPublicationCaller() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames ->
            frames.filter(frame -> frame.getDeclaringClass() != NativeExpectedFieldObservation.class
                && frame.getDeclaringClass() != CompoundTag.class).findFirst().map(frame ->
                    frame.getDeclaringClass() == FoodWeaponData.class && frame.getMethodName().equals("lambda$edit$1")
                        && frame.getDescriptor().equals("(Ljava/lang/String;Ljava/util/function/Consumer;Lnet/minecraft/nbt/CompoundTag;)V"))
                .orElse(false));
    }

    public static void nativeComponentMergeWrite(CompoundTag owner, String key, Tag changed, Runnable original) {
        boolean authentic = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames ->
            frames.skip(1).findFirst().map(frame -> frame.getDeclaringClass() == NativeLifecycleItemData.class
                && frame.getMethodName().equals("merge")
                && frame.getDescriptor().equals("(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;)V"))
                .orElse(false));
        if (!authentic) throw new IllegalStateException("Native merge write requires the original lifecycle merge method");
        NativeExpectedFieldObservation.Scope scope = current();
        NativeExpectedFieldObservation.Fact assignment = scope == null ? null : scope.active;
        try {
            if (scope != null && !scope.retired) {
                require(assignment != null && assignment.site.equals("CUSTOM_DATA_COMMIT")
                    && assignment.scope == scope && assignment.sourceA == scope.sourceA && assignment.attempted
                    && assignment.exit == 0 && assignment.nativeMergeBoundary != null, scope,
                    "Native merge write is outside its exact original component assignment");
                if (!scope.retired) assignment.nativeMergeBoundary.beforeWrite(owner, key, changed);
            }
        } catch (Throwable failure) {
            failed(scope, failure);
        }
        original.run();
        try {
            if (scope != null && !scope.retired) assignment.nativeMergeBoundary.afterWrite();
        } catch (Throwable failure) {
            failed(scope, failure);
        }
    }

    private static void prepareNativeTimerTransition(NativeExpectedFieldObservation.Scope scope,Profile profile) {
        try {
            NativeItemComponentObservation source = NativeItemComponentObservation.capture(scope.sourceA);
            IdentityHashMap<Object,Boolean> nodes = new IdentityHashMap<>();
            nativeTimerMutableNodes(source.expectedCustomData(),nodes);
            nativeTimerMutableNodes(scope.timerValue,nodes);
            scope.nativeTimerTransition = NativeTimerTransitionPlan.prepare(scope,profile,source,
                scope.timerValue == null ? null : NativeNbtGraphObservation.capture(scope.timerValue),nodes);
        } catch (Throwable failure) {
            failed(scope,failure);
        }
    }

    private static void beforeNativeTimerTransition(NativeExpectedFieldObservation.Fact fact) {
        if (fact.site.equals("RECEIVER") && fact.scope.nativeTimerTransition == null && parent(fact,"TIMER")) {
            prepareNativeTimerTransition(fact.scope,(Profile) fact.argument);
            if (fact.scope.nativeTimerTransition != null) {
                try { fact.scope.nativeTimerTransition.before(fact.parent); }
                catch (Throwable failure) { failed(fact.scope,failure); }
            }
        }
        NativeTimerTransitionPlan plan = fact.scope.nativeTimerTransition;
        if (plan == null || plan.failure != null) return;
        try {
            if (fact.site.equals("MAP_PUT") && parent(fact,"RECEIVER")) {
                IdentityHashMap<Object,Boolean> nodes = new IdentityHashMap<>();
                nativeTimerMutableNodes(fact.physicalOwner,nodes);
                plan.detachedProfileBefore(fact,nodes,fields(fact.physicalOwner));
            }
            plan.before(fact);
        } catch (Throwable failure) {
            failed(fact.scope,failure);
        }
    }

    private static void afterNativeTimerTransition(NativeExpectedFieldObservation.Fact fact) {
        if (fact == null) return;
        NativeTimerTransitionPlan plan = fact.scope.nativeTimerTransition;
        if (plan == null || plan.failure != null) return;
        try {
            if (fact.site.equals("TIMER")) {
                plan.after(fact,null,null,null);
            } else if (fact.site.equals("MAP_PUT") && parent(fact,"RECEIVER")) {
                plan.detachedProfileAfter(fact);
            } else if (fact.site.equals("RECEIVER") || fact.site.equals("COPY") || fact.site.equals("TIMER_PUT") || fact.site.equals("CUSTOM_DATA_COMMIT")) {
                CompoundTag installed = fact.site.equals("CUSTOM_DATA_COMMIT")
                    ? ((NativeItemComponentObservation) fact.originalReturn).expectedCustomData() : null;
                Tag returned = fact.site.equals("TIMER_PUT") ? (Tag) fact.receiver
                    : fact.site.equals("CUSTOM_DATA_COMMIT") ? installed : (Tag) fact.originalReturn;
                IdentityHashMap<Object,Boolean> nodes = new IdentityHashMap<>();
                nativeTimerMutableNodes(returned,nodes);
                plan.after(fact,NativeNbtGraphObservation.capture(returned),nodes,installed);
                if (nativeTimerSequence(fact.scope)) {
                    if (fact.site.equals("RECEIVER")) {
                        require(fact.scope.timerReceiver == null,fact.scope,"Native signed timer acquired a duplicate detached receiver");
                        if (eligible(fact.scope)) {
                            fact.scope.timerReceiver = (CompoundTag) fact.originalReturn;
                            fact.scope.sequence.nativeTimerReceiverReturned(fact.scope.step,plan,fact.originalReturn);
                            fact.scope.prefixes = fact.scope.step.expected.prefixCount();
                        }
                    } else if (fact.site.equals("CUSTOM_DATA_COMMIT")) {
                        fact.scope.sequence.nativeTimerComponentInstalled(fact.scope.step,plan,fact.scope.timerReceiver);
                    }
                }
            }
        } catch (Throwable failure) {
            failed(fact.scope,failure);
        }
    }

    private static void beforeNativeTimerComponentCommit(NativeExpectedFieldObservation.Fact fact,CompoundTag before,CompoundTag after) {
        NativeTimerTransitionPlan plan = fact.scope.nativeTimerTransition;
        if (plan == null || plan.failure != null) return;
        try {
            IdentityHashMap<Object,Boolean> nodes = new IdentityHashMap<>();
            nativeTimerMutableNodes(before,nodes);
            nativeTimerMutableNodes(after,nodes);
            plan.commit(fact,before,after,nodes,((NativeItemComponentObservation) fact.argument).expectedCustomData());
        } catch (Throwable failure) {
            failed(fact.scope,failure);
        }
    }

    private static void finishNativeTimerTransition(NativeExpectedFieldObservation.Fact fact) {
        NativeTimerTransitionPlan plan = fact.scope.nativeTimerTransition;
        if (plan == null) return;
        try {
            plan.finished(fact);
        } catch (Throwable failure) {
            failed(fact.scope,failure);
        }
    }

    public static void nativeCustomDataAssigned(ItemStack stack,net.minecraft.core.component.DataComponentType<?> type,Object value,Object previous) {
        List<StackWalker.StackFrame> frames = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(NativeExpectedFieldObservation::nativeAssignmentFrames);
        if (frames.size() != 2 || frames.get(0).getDeclaringClass() != ItemStack.class
            || !(frames.get(0).getMethodName().equals("ftboss$nativeCustomDataAssigned")
                || frames.get(0).getMethodName().endsWith("$ftboss$nativeCustomDataAssigned"))
            || !frames.get(0).getDescriptor().equals("(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V")
            || frames.get(1).getDeclaringClass() != ItemStack.class || !frames.get(1).getMethodName().equals("set")
            || !frames.get(1).getDescriptor().equals("(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;")) {
            throw new IllegalStateException("Native CUSTOM_DATA assignment evidence requires the actual ItemStack.set return injection");
        }
        if (type != net.minecraft.core.component.DataComponents.CUSTOM_DATA) return;
        NativeExpectedFieldObservation.Scope scope = current();
        if (scope == null || scope.nativeTimerTransition == null || scope.nativeTimerTransition.failure != null) return;
        try {
            NativeExpectedFieldObservation.Fact assignment = fact(scope,"NATIVE_CUSTOM_DATA_ASSIGNED",stack,type,value,null);
            assignment.attempted = true;
            assignment.originalReturn = previous;
            assignment.returned = true;
            assignment.exit = ++scope.clock;
            NativeItemComponentObservation installed = NativeItemComponentObservation.capture(stack);
            CompoundTag root = installed.expectedCustomData();
            IdentityHashMap<Object,Boolean> nodes = new IdentityHashMap<>();
            nativeTimerMutableNodes(root,nodes);
            require(value instanceof net.minecraft.world.item.component.CustomData
                && ((net.minecraft.world.item.component.CustomData) value).getUnsafe() == root,scope,
                "Physical native assignment argument is not the actually installed CustomData root");
            CompoundTag previousRoot = previous == null ? null : ((net.minecraft.world.item.component.CustomData) previous).getUnsafe();
            scope.nativeTimerTransition.nativeAssignment(assignment,installed,root,previousRoot,nodes);
        } catch (Throwable failure) {
            failed(scope,failure);
        }
    }

    private static List<StackWalker.StackFrame> nativeAssignmentFrames(java.util.stream.Stream<StackWalker.StackFrame> frames) {
        return frames.skip(1).limit(2).toList();
    }

    private static boolean nativeTimerSequence(NativeExpectedFieldObservation.Scope scope) {
        return eligible(scope) && scope.kind == Kind.TIMER && scope.step != null && scope.nativeTimerTransition != null;
    }

    public static Object curiosProviderFieldsBefore(Object registry, Object key, Object snapshot, Object transition) {
        StackWalker.StackFrame caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != NativeCuriosProviderFields260.class || !caller.getMethodName().equals("before")
            || !caller.getDescriptor().equals("(Ljava/util/Map;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")) {
            throw new SecurityException("Actual native Curios field entry required");
        }
        NativeExpectedFieldObservation.Scope scope = current();
        if (scope == null) return null;
        NativeExpectedFieldObservation.Fact event = fact(scope, "PROVIDER_FIELDS", registry, key, snapshot, null);
        if (event != null) {
            event.nativeProviderContext = transition;
            event.attempted = true;
            event.originalReturn = snapshot;
            event.returned = true;
            event.exit = ++scope.clock;
        }
        rejectExtra(scope, new IllegalStateException("Curios registry identity snapshots are outside the selected field catalogue"));
        return event;
    }

    public static void curiosProviderFieldsAfter(Object token, Object registry, Object key, Object snapshot, Object transition, Throwable originalFailure, boolean matched) {
        StackWalker.StackFrame caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != NativeCuriosProviderFields260.class || !caller.getMethodName().equals("after")
            || !caller.getDescriptor().equals("(Ljava/lang/Object;Ljava/util/Map;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Throwable;)V")) {
            throw new SecurityException("Actual native Curios field exit required");
        }
        NativeExpectedFieldObservation.Scope scope = current();
        if (scope == null) return;
        if (!(token instanceof NativeExpectedFieldObservation.Fact before) || before.scope != scope || before.thread != Thread.currentThread()
            || !before.site.equals("PROVIDER_FIELDS") || before.receiver != registry || before.key != key
            || before.nativeProviderContext != transition || !before.returned || before.exit <= 0L) {
            rejectExtra(scope, new IllegalStateException("Foreign native Curios field pairing"));
            return;
        }
        NativeExpectedFieldObservation.Fact event = fact(scope, originalFailure == null ? "PROVIDER_FIELDS" : "PROVIDER_FIELDS_FAILED", registry, key, snapshot, null);
        if (event != null) {
            event.nativeProviderContext = transition;
            event.attempted = true;
            event.originalReturn = snapshot;
            event.originalFailure = originalFailure;
            event.returned = originalFailure == null;
            event.exit = ++scope.clock;
        }
        rejectExtra(scope, new IllegalStateException(matched ? "Curios registry identity snapshots are outside the selected field catalogue" : "Unmatched native Curios provider field transition"));
    }

}
