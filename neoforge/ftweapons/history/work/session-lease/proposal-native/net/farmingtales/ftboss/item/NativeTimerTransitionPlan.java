package net.farmingtales.ftboss.item;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation.Fact;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation.Scope;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

final class NativeTimerTransitionPlan {
    private final Thread thread = Thread.currentThread();
    private final Scope scope;
    private final ItemStack source;
    private final FoodWeaponEvolution.Profile profile;
    private final String key;
    private final Tag argument;
    private final NativeItemComponentObservation sourceBefore;
    private final NativeNbtGraphObservation argumentBefore;
    private final Value expectedBefore;
    private final Value expectedReceiver;
    private final Value expectedPut;
    private final Value expectedInstalled;
    private final Value expectedArgument;
    private final FoodWeaponExpectedTrace.Plan nativeExpectedCatalogue;
    private final FoodWeaponExpectedTrace.Operation nativeExpectedOperation;
    private final IdentityHashMap<Object,Boolean> protectedNodes;
    private final Map<DataComponentType<?>,Object> otherComponents;
    private CompoundTag sourceCustomDataBefore;
    private Fact nativeAssignmentFact;
    private boolean nativeAssignmentEntry;
    private boolean nativeStorageEntry;
    private NativeItemComponentObservation nativeAssignmentImage;
    private Fact detachedProfileFact;
    private Tag detachedProfilePrevious;
    private boolean detachedProfileMatched;
    private Fact parent;
    private CompoundTag buffer;
    private Tag copied;
    private Tag previous;
    private NativeNbtGraphObservation bufferImage;
    private NativeNbtGraphObservation copiedImage;
    private NativeItemComponentObservation installedImage;
    private long lastExit;
    private int phase;
    private boolean matched;
    private Throwable originalFailure;
    private boolean exceptionSourcePreserved;
    Throwable failure;

    private NativeTimerTransitionPlan(Scope scope,FoodWeaponEvolution.Profile profile,
            NativeItemComponentObservation sourceBefore,NativeNbtGraphObservation argumentBefore,
            IdentityHashMap<Object,Boolean> protectedNodes) {
        this.scope = scope;
        this.source = scope.sourceA;
        this.profile = profile;
        this.key = scope.timerKey;
        this.argument = scope.timerValue;
        this.sourceBefore = sourceBefore;
        this.argumentBefore = argumentBefore;
        this.protectedNodes = new IdentityHashMap<>(protectedNodes);
        this.otherComponents = components(source);
        CustomData data = source.get(DataComponents.CUSTOM_DATA);
        Value root = data == null ? FoodWeaponExpectedTrace.emptyCompound() : NativeExpectedRawFields.observe(data.getUnsafe());
        Value child = FoodWeaponExpectedTrace.field(root,"FTBossFoodEvolution");
        this.expectedBefore = child != null && child.kind == FoodWeaponExpectedTrace.COMPOUND ? child : FoodWeaponExpectedTrace.emptyCompound();
        this.expectedArgument = NativeExpectedRawFields.observe(argument);
        FoodWeaponExpectedTrace.Builder builder = new FoodWeaponExpectedTrace.Builder(new FoodWeaponExpectedTrace.State(
            nativeWeaponBefore(source,sourceBefore,root),FoodWeaponExpectedTrace.emptyCompound(),FoodWeaponExpectedTrace.emptyCompound()));
        this.nativeExpectedOperation = argument == null ? null : builder.nativeTimer(FoodWeaponEvolution.planHitTimerProfile(profile),key,expectedArgument);
        this.nativeExpectedCatalogue = builder.seal();
        if (nativeExpectedOperation == null) {
            Value receiver = this.expectedBefore;
            String id = FoodWeaponEvolution.planHitTimerProfile(profile);
            Value existing = FoodWeaponExpectedTrace.field(receiver,"Profile");
            if (id != null && (existing == null || existing.kind != FoodWeaponExpectedTrace.STRING)) {
                receiver = FoodWeaponExpectedTrace.put(receiver,"Profile",FoodWeaponExpectedTrace.string(id));
            }
            this.expectedReceiver = receiver;
            this.expectedPut = null;
            this.expectedInstalled = null;
        } else {
            this.expectedReceiver = FoodWeaponExpectedTrace.compoundAt(nativeExpectedOperation.entry.weapon,new String[]{"components","minecraft:custom_data","FTBossFoodEvolution"});
            this.expectedPut = FoodWeaponExpectedTrace.compoundAt(nativeExpectedOperation.after.weapon,new String[]{"components","minecraft:custom_data","FTBossFoodEvolution"});
            this.expectedInstalled = FoodWeaponExpectedTrace.compoundAt(nativeExpectedOperation.after.weapon,new String[]{"components","minecraft:custom_data"});
            require(FoodWeaponExpectedTrace.same(this.expectedBefore,FoodWeaponExpectedTrace.compoundAt(nativeExpectedOperation.before.weapon,new String[]{"components","minecraft:custom_data","FTBossFoodEvolution"})),
                "Native original semantic catalogue BEFORE differs from the actual native public component view");
        }
        require(sourceLive(),"Native timer inputs changed while preparing the complete original operation");
    }

    private static void caller() {
        Class<?> actual = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames ->
            frames.filter(frame -> frame.getDeclaringClass() != NativeTimerTransitionPlan.class).findFirst()
                .map(StackWalker.StackFrame::getDeclaringClass).orElse(null));
        if (actual != NativeExpectedFieldObservation.class) throw new IllegalStateException("Complete native timer plans require the actual field observer");
    }

    static NativeTimerTransitionPlan prepare(Scope scope,FoodWeaponEvolution.Profile profile,
            NativeItemComponentObservation sourceBefore,NativeNbtGraphObservation argumentBefore,
            IdentityHashMap<Object,Boolean> protectedNodes) {
        caller();
        if (scope == null || scope.sourceA == null || scope.timerKey == null || sourceBefore == null || protectedNodes == null) {
            throw new IllegalArgumentException("Actual original timer scope, source, key and immutable BEFORE required");
        }
        return new NativeTimerTransitionPlan(scope,profile,sourceBefore,argumentBefore,protectedNodes);
    }

    private static Map<DataComponentType<?>,Object> components(ItemStack source) {
        return source.getComponents().stream().filter(value -> value.type() != DataComponents.CUSTOM_DATA)
            .collect(Collectors.toUnmodifiableMap(value -> value.type(),value -> value.value()));
    }

    private void require(boolean valid,String message) {
        if (!valid) {
            if (failure == null) failure = new IllegalStateException(message);
            matched = false;
            throw (IllegalStateException) failure;
        }
    }

    private boolean argumentLive() {
        return argument == null ? argumentBefore == null : argumentBefore != null && argumentBefore.matches(argument);
    }

    private boolean sourceLive() {
        return thread == Thread.currentThread() && sourceBefore.matches(source) && argumentLive();
    }

    private void event(Fact fact,int expectedPhase) {
        require(failure == null && phase == expectedPhase && fact != null && fact.scope == scope
            && fact.sourceA == source && fact.thread == thread && thread == Thread.currentThread()
            && fact.parent == parent && fact.entry > lastExit && parent != null && parent.exit == 0L
            && parent.attempted && !parent.returned && parent.originalFailure == null,
            "Native timer stage source, thread, parent, order or single-use phase differs");
    }

    void before(Fact fact) {
        caller();
        if (fact.site.equals("TIMER")) {
            require(phase == 0 && parent == null && fact.scope == scope && fact.sourceA == source
                && fact.receiver == source && fact.argument == argument && fact.key == key && sourceLive(),
                "Native timer entry differs from the operation planned before its original callback");
            parent = fact;
            phase = 1;
        } else if (fact.site.equals("RECEIVER")) {
            event(fact,1);
            require(scope.facts.stream().noneMatch(previousFact -> previousFact != fact && previousFact.parent == parent
                && java.util.Set.of("RECEIVER","COPY","TIMER_PUT","CUSTOM_DATA_COMMIT").contains(previousFact.site)),
                "Native timer receiver was preceded by an unplanned original copy, put or component installation");
            require(fact.receiver == source && fact.argument == profile && sourceLive(),"Native timer writableData source or profile changed");
            phase = 2;
        } else if (fact.site.equals("COPY")) {
            event(fact,3);
            require(fact.receiver == argument && sourceLive() && bufferImage.matches(buffer),"Native timer original copy input or detached receiver changed");
            phase = 4;
        } else if (fact.site.equals("TIMER_PUT")) {
            event(fact,5);
            require(fact.receiver == buffer && fact.argument == copied && fact.key == key && sourceLive()
                && bufferImage.matches(buffer) && copiedImage.matches(copied),"Native timer original put receiver, key or original copy result changed");
            previous = buffer.get(key);
            phase = 6;
        }
    }

    void after(Fact fact,NativeNbtGraphObservation image,IdentityHashMap<Object,Boolean> nodes,CompoundTag installedRoot) {
        caller();
        if (fact.site.equals("TIMER")) {
            require(fact == parent && phase == 9 && installedImage.matches(source) && argumentLive()
                && bufferImage.matches(buffer) && copiedImage.matches(copied),"Complete native timer did not return from exactly one unchanged original component installation");
            phase = 10;
            matched = true;
        } else if (fact.site.equals("RECEIVER")) {
            event(fact,2);
            require(fact.returned && fact.originalFailure == null && fact.originalReturn instanceof CompoundTag
                && fact.nativeTimerBufferMatched && sourceLive() && image != null
                && image.matches((Tag) fact.originalReturn)
                && FoodWeaponExpectedTrace.same(expectedReceiver,NativeExpectedRawFields.observe((Tag) fact.originalReturn)),
                "Native timer detached return differs from the complete pre-callback profile prediction");
            require(FoodWeaponExpectedTrace.same(expectedBefore,expectedReceiver)
                ? detachedProfileFact == null
                : detachedProfileMatched && detachedProfileFact != null && detachedProfileFact.physicalOwner == fact.originalReturn,
                "Native detached receiver lacks its exact original conditional Profile backing-map event");
            buffer = (CompoundTag) fact.originalReturn;
            bufferImage = image;
            disjoint(nodes);
            protectedNodes.putAll(nodes);
            phase = 3;
        } else if (fact.site.equals("COPY")) {
            event(fact,4);
            require(fact.returned && fact.originalFailure == null && fact.originalReturn instanceof Tag && sourceLive()
                && bufferImage.matches(buffer) && image != null && image.matches((Tag) fact.originalReturn)
                && FoodWeaponExpectedTrace.same(expectedArgument,NativeExpectedRawFields.observe((Tag) fact.originalReturn)),
                "Native timer returned copy differs from its immutable original argument");
            disjoint(nodes);
            protectedNodes.putAll(nodes);
            copied = (Tag) fact.originalReturn;
            copiedImage = image;
            phase = 5;
        } else if (fact.site.equals("TIMER_PUT")) {
            event(fact,6);
            require(fact.returned && fact.originalFailure == null && fact.originalReturn == previous && sourceLive()
                && buffer.get(key) == copied && copiedImage.matches(copied)
                && FoodWeaponExpectedTrace.same(expectedPut,NativeExpectedRawFields.observe(buffer)),
                "Native timer put changed an unplanned field, original return or copied value");
            bufferImage = image;
            phase = 7;
        } else if (fact.site.equals("CUSTOM_DATA_COMMIT")) {
            event(fact,8);
            require(fact.returned && fact.originalFailure == null && fact.originalReturn instanceof NativeItemComponentObservation
                && argumentLive() && bufferImage.matches(buffer) && copiedImage.matches(copied),
                "Native timer original component installation failed or changed a detached input");
            NativeItemComponentObservation installed = (NativeItemComponentObservation) fact.originalReturn;
            require(nativeAssignmentFact != null && nativeAssignmentFact.parent == fact && nativeAssignmentImage != null
                && nativeAssignmentImage.matches(source) && nativeAssignmentImage.same(installed),
                "Native timer final commit lacks its exact physical ItemStack CUSTOM_DATA assignment return");
            require(installed.matches(source) && installed.itemIdentity() == sourceBefore.itemIdentity() && installed.count() == sourceBefore.count()
                && FoodWeaponExpectedTrace.same(expectedInstalled,NativeExpectedRawFields.observe(installedRoot)),
                "Native timer whole installed native root, original item or raw count differs");
            Map<DataComponentType<?>,Object> current = components(source);
            require(current.keySet().equals(otherComponents.keySet()) && otherComponents.entrySet().stream().allMatch(entry -> current.get(entry.getKey()) == entry.getValue()),
                "Native timer changed a non-CUSTOM_DATA component identity");
            disjoint(nodes);
            installedImage = installed;
            phase = 9;
        }
    }

    void commit(Fact fact,CompoundTag before,CompoundTag after,IdentityHashMap<Object,Boolean> nodes) {
        caller();
        event(fact,7);
        require(fact.receiver == source && "FTBossFoodEvolution".equals(fact.key) && sourceLive()
            && bufferImage.matches(buffer) && copiedImage.matches(copied)
            && FoodWeaponExpectedTrace.same(expectedBefore,NativeExpectedRawFields.observe(before))
            && FoodWeaponExpectedTrace.same(expectedPut,NativeExpectedRawFields.observe(after)),
            "Native timer component commit is not the original preplanned source/profile/copy/put delta");
        disjoint(nodes);
        protectedNodes.putAll(nodes);
        phase = 8;
    }

    void nativeAssignment(Fact fact,NativeItemComponentObservation installed,CompoundTag root,CompoundTag previousRoot,IdentityHashMap<Object,Boolean> nodes) {
        caller();
        require(thread == Thread.currentThread() && phase == 8 && failure == null && nativeAssignmentFact == null
            && (scope.sequence == null || nativeAssignmentEntry && nativeStorageEntry)
            && fact != null && fact.scope == scope && scope.nativeTimerTransition == this && scope.sourceA == source
            && fact.site.equals("NATIVE_CUSTOM_DATA_ASSIGNED") && fact.receiver == source && fact.key == DataComponents.CUSTOM_DATA
            && fact.argument instanceof CustomData
            && previousRoot == sourceCustomDataBefore && fact.attempted && fact.returned && fact.originalFailure == null
            && fact.parent != null && fact.parent == scope.active && fact.parent.site.equals("CUSTOM_DATA_COMMIT")
            && fact.parent.parent == parent && fact.parent.receiver == source && fact.parent.attempted
            && !fact.parent.returned && fact.parent.originalFailure == null,
            "Native timer assignment is not the single real ItemStack.set CUSTOM_DATA return inside its original commit");
        require(installed != null && installed.matches(source) && installed.itemIdentity() == sourceBefore.itemIdentity()
            && installed.count() == sourceBefore.count() && argumentLive() && bufferImage.matches(buffer) && copiedImage.matches(copied)
            && FoodWeaponExpectedTrace.same(expectedInstalled,NativeExpectedRawFields.observe(root)),
            "Native physical component assignment differs from the preplanned whole source or detached copy");
        Map<DataComponentType<?>,Object> current = components(source);
        require(current.keySet().equals(otherComponents.keySet()),"Native physical CUSTOM_DATA assignment changed unrelated component keys");
        for (var entry : otherComponents.entrySet()) {
            require(current.get(entry.getKey()) == entry.getValue(),"Native physical CUSTOM_DATA assignment changed an unrelated component identity");
        }
        disjoint(nodes);
        nativeAssignmentFact = fact;
        nativeAssignmentImage = installed;
    }

    boolean assignmentEntering(Object receiver, CompoundTag root, boolean storage) {
        caller();
        Fact active = scope.active;
        if ((storage ? !nativeAssignmentEntry || nativeStorageEntry || receiver != source.getComponents() : receiver != source || nativeAssignmentEntry)
            || nativeAssignmentFact != null || failure != null || phase != 8
            || !sourceLive() || scope.retired || scope.closed || scope.step == null || scope.sequence == null
            || scope.step.session != scope.sequence || !sameSequenceOperation(scope.step.expected)
            || active == null || active.scope != scope || active.sourceA != source || active.thread != thread
            || active.parent != parent || !active.site.equals("CUSTOM_DATA_COMMIT") || active.receiver != source
            || !"FTBossFoodEvolution".equals(active.key) || !active.attempted || active.returned
            || active.originalFailure != null || active.exit != 0L || !bufferImage.matches(buffer) || !copiedImage.matches(copied)
            || !FoodWeaponExpectedTrace.same(expectedInstalled, NativeExpectedRawFields.observe(root))) return false;
        if (storage) nativeStorageEntry = true;
        else nativeAssignmentEntry = true;
        return true;
    }

    void commit(Fact fact,CompoundTag before,CompoundTag after,IdentityHashMap<Object,Boolean> nodes,CompoundTag physicalBefore) {
        commit(fact,before,after,nodes);
        sourceCustomDataBefore = physicalBefore;
    }

    private void disjoint(IdentityHashMap<Object,Boolean> nodes) {
        require(nodes != null,"Complete native timer mutable graph evidence missing");
        for (Object node : nodes.keySet()) require(!protectedNodes.containsKey(node),"Complete native timer copy or installed root shares mutable original storage");
    }

    void finished(Fact fact) {
        caller();
        if (fact.originalFailure != null && (fact == parent || fact.parent == parent)) {
            if (originalFailure == null) originalFailure = fact.originalFailure;
            require(originalFailure == fact.originalFailure,"Complete native timer substituted the original throwable identity");
            exceptionSourcePreserved = installedImage == null && sourceBefore.matches(source);
            matched = false;
            phase = -1;
        } else if (fact.exit > 0 && fact.parent == parent && !fact.site.equals("MAP_PUT")) {
            lastExit = fact.exit;
        }
    }

    private static Value nativeWeaponBefore(ItemStack source,NativeItemComponentObservation image,Value root) {
        ItemStack detached = NativeFoundationItemData.decode(NativeFoundationItemData.image(source));
        if (detached.isEmpty() || detached.getItem() != source.getItem() || detached.getCount() != source.getCount()
            || source.getComponents().stream().count() != detached.getComponents().stream().count()
            || !source.getComponents().stream().allMatch(component -> Objects.equals(component.value(), detached.get(component.type())))) {
            throw new IllegalStateException("Native timer component copy differs from the original stack");
        }
        Tag encoded = image.timerPatchSnapshot(source);
        Value components = NativeExpectedRawFields.observe(encoded);
        components = source.get(DataComponents.CUSTOM_DATA) == null
            ? FoodWeaponExpectedTrace.remove(components,"minecraft:custom_data")
            : FoodWeaponExpectedTrace.put(components,"minecraft:custom_data",root);
        if (FoodWeaponExpectedTrace.compoundKeys(components).length == 0) {
            return FoodWeaponExpectedTrace.compound(new String[]{"count","id"},new Value[]{
                FoodWeaponExpectedTrace.number(FoodWeaponExpectedTrace.INT,image.count()),
                FoodWeaponExpectedTrace.string(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(image.itemIdentity()).toString())});
        }
        return FoodWeaponExpectedTrace.compound(new String[]{"components","count","id"},new Value[]{components,
            FoodWeaponExpectedTrace.number(FoodWeaponExpectedTrace.INT,image.count()),
            FoodWeaponExpectedTrace.string(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(image.itemIdentity()).toString())});
    }

    void detachedProfileBefore(Fact fact,IdentityHashMap<Object,Boolean> nodes,Map<String,Tag> backingMap) {
        caller();
        require(phase == 2 && failure == null && detachedProfileFact == null && fact != null
            && fact.scope == scope && fact.thread == thread && thread == Thread.currentThread()
            && fact.site.equals("MAP_PUT") && fact.parent != null && fact.parent.scope == scope
            && fact.parent.site.equals("RECEIVER") && fact.parent.parent == parent
            && fact.parent.receiver == source && fact.parent.argument == profile && fact.parent.attempted
            && !fact.parent.returned && fact.parent.originalFailure == null && fact.parent.exit == 0L
            && fact.entry > fact.parent.entry && "Profile".equals(fact.key) && fact.physicalOwner != null
            && fact.receiver == backingMap && nodes != null && nodes.containsKey(fact.physicalOwner) && nodes.containsKey(backingMap)
            && !FoodWeaponExpectedTrace.same(expectedBefore,expectedReceiver) && sourceLive(),
            "Detached native Profile write has a foreign receiver, phase, source, parent, key or duplicate event");
        require(FoodWeaponExpectedTrace.same(expectedBefore,NativeExpectedRawFields.observe(fact.physicalOwner))
            && fact.argument instanceof Tag && FoodWeaponExpectedTrace.same(FoodWeaponExpectedTrace.field(expectedReceiver,"Profile"),
                NativeExpectedRawFields.observe((Tag) fact.argument)),
            "Detached native Profile mutation differs from the independent complete original BEFORE and value");
        disjoint(nodes);
        detachedProfilePrevious = fact.physicalOwner.get("Profile");
        detachedProfileFact = fact;
    }

    void detachedProfileAfter(Fact fact) {
        caller();
        require(fact == detachedProfileFact && !detachedProfileMatched && phase == 2 && sourceLive()
            && fact.attempted && fact.returned && fact.originalFailure == null && fact.originalReturn == detachedProfilePrevious
            && fact.physicalOwner.get("Profile") == fact.argument
            && FoodWeaponExpectedTrace.same(expectedReceiver,NativeExpectedRawFields.observe(fact.physicalOwner)),
            "Detached native Profile write failed to publish exactly its original argument or changed another field");
        detachedProfileMatched = true;
    }

    boolean detachedProfileEvent(Fact fact) {
        caller();
        return failure == null && detachedProfileFact == fact && phase == 2;
    }

    CompoundTag sequenceReceiver() {
        caller();
        require(failure == null && phase >= 3 && phase <= 8 && scope.step != null && scope.sequence != null
            && !scope.retired && buffer != null && sourceLive()
            && (phase == 6 ? sequencePutBoundary() : bufferImage.matches(buffer)),
            "Native signed timer receiver is not the exact live original detached buffer");
        return buffer;
    }

    private boolean sequencePutBoundary() {
        Fact active = scope.active;
        if (active == null || active.scope != scope || active.parent != parent || active.sourceA != source
            || active.thread != thread || !active.site.equals("TIMER_PUT") || active.receiver != buffer
            || active.argument != copied || active.key != key || active.exit != 0L || active.originalFailure != null
            || copiedImage == null || !copiedImage.matches(copied)) return false;
        if (!active.attempted) {
            return !active.returned && active.directMapChildren == 0 && bufferImage.matches(buffer);
        }
        return active.returned && active.directMapChildren == 1 && active.originalReturn == previous
            && buffer.get(key) == copied && FoodWeaponExpectedTrace.same(expectedPut,NativeExpectedRawFields.observe(buffer));
    }

    boolean sequenceBeforeInstallation(FoodWeaponOriginalSequence.Step step) {
        caller();
        return failure == null && phase == 8 && scope.step == step && step != null
            && step.session == scope.sequence && sameSequenceOperation(step.expected) && buffer == scope.timerReceiver
            && bufferImage.matches(buffer) && copiedImage.matches(copied) && sequenceAssignedBoundary();
    }

    private boolean sequenceAssignedBoundary() {
        Fact active = scope.active;
        return thread == Thread.currentThread() && !scope.retired && argumentLive()
            && active != null && active.scope == scope && active.sourceA == source && active.thread == thread
            && active.parent == parent && active.site.equals("CUSTOM_DATA_COMMIT") && active.receiver == source
            && "FTBossFoodEvolution".equals(active.key) && active.attempted && active.returned
            && active.originalFailure == null && active.exit == 0L && active.nativeTransitionMatched
            && active.originalReturn instanceof NativeItemComponentObservation installed
            && nativeAssignmentFact != null && nativeAssignmentFact.parent == active && nativeAssignmentFact.scope == scope
            && nativeAssignmentFact.site.equals("NATIVE_CUSTOM_DATA_ASSIGNED") && nativeAssignmentFact.receiver == source
            && nativeAssignmentFact.key == DataComponents.CUSTOM_DATA && nativeAssignmentFact.attempted
            && nativeAssignmentFact.returned && nativeAssignmentFact.originalFailure == null && nativeAssignmentFact.exit > active.entry
            && nativeAssignmentImage != null && nativeAssignmentImage.matches(source) && nativeAssignmentImage.same(installed);
    }

    boolean sequenceReceiverMatches(FoodWeaponOriginalSequence.Session session,FoodWeaponOriginalSequence.Step step,Object receiver) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != FoodWeaponOriginalSequence.Session.class) {
            throw new IllegalStateException("Native timer receiver evidence requires the authentic original Session");
        }
        return failure == null && phase == 3 && scope.sequence == session && scope.step == step && step != null
            && step.session == session && !scope.retired && buffer == receiver && buffer == scope.timerReceiver
            && bufferImage.matches(buffer) && sourceLive() && sameSequenceOperation(step.expected);
    }

    boolean sequenceInstallationMatches(FoodWeaponOriginalSequence.Session session,FoodWeaponOriginalSequence.Step step,Object receiver) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != FoodWeaponOriginalSequence.Session.class) {
            throw new IllegalStateException("Native timer installation evidence requires the authentic original Session");
        }
        return failure == null && phase == 9 && scope.sequence == session && scope.step == step && step != null
            && step.session == session && !scope.retired && buffer == receiver && buffer == scope.timerReceiver
            && bufferImage.matches(buffer) && copiedImage.matches(copied) && installedImage != null
            && installedImage.matches(source) && argumentLive() && thread == Thread.currentThread()
            && sameSequenceOperation(step.expected);
    }

    private boolean sameSequenceOperation(FoodWeaponExpectedTrace.Operation operation) {
        FoodWeaponExpectedTrace.Operation local = nativeExpectedOperation;
        if (operation == null || local == null || operation.kind != FoodWeaponExpectedTrace.Kind.TIMER
            || !Objects.equals(operation.profile,local.profile) || !Objects.equals(operation.key,local.key)
            || !FoodWeaponExpectedTrace.same(operation.argument,local.argument) || operation.write == null
            || !operation.write.matches(FoodWeaponExpectedTrace.Domain.WEAPON,new String[]{"components","minecraft:custom_data","FTBossFoodEvolution"},key,expectedArgument)
            || !FoodWeaponExpectedTrace.same(operation.before.weapon,local.before.weapon)
            || !FoodWeaponExpectedTrace.same(operation.entry.weapon,local.entry.weapon)
            || !FoodWeaponExpectedTrace.same(operation.after.weapon,local.after.weapon)
            || !sameOtherDomains(operation.before,operation.entry) || !sameOtherDomains(operation.before,operation.after)
            || operation.prefixCount() != local.prefixCount()) return false;
        for (int index = 0; index < local.prefixCount(); index++) {
            FoodWeaponExpectedTrace.Mutation expected = local.prefix(index), actual = operation.prefix(index);
            if (actual.domain != expected.domain || !Objects.equals(actual.key,expected.key)
                || !FoodWeaponExpectedTrace.same(actual.before,expected.before)
                || !FoodWeaponExpectedTrace.same(actual.after,expected.after)
                || !FoodWeaponExpectedTrace.same(actual.value,expected.value)) return false;
        }
        return true;
    }

    private static boolean sameOtherDomains(FoodWeaponExpectedTrace.State before,FoodWeaponExpectedTrace.State after) {
        return before.player == after.player && before.carrier == after.carrier && before.rangedBuffer == after.rangedBuffer
            && before.rangedAttached == after.rangedAttached && before.rangedAcquired == after.rangedAcquired
            && before.rangedPlayerIdentity == after.rangedPlayerIdentity && before.rangedBufferIdentity == after.rangedBufferIdentity;
    }

}
