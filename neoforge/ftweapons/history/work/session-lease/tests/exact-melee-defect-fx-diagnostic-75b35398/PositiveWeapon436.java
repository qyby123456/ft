package ftboss.storage308;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_storage308")
public final class PositiveWeapon436 {
    private static final List<Object> actions = new ArrayList<>();
    private static final List<Map<String, Object>> damageEvents = new ArrayList<>();
    private static ServerStartedEvent pending;
    private static net.minecraft.world.damagesource.DamageSource capturedSource;
    private static CompoundTag capturedSigned;

    public PositiveWeapon436() {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> pending = event);
        NeoForge.EVENT_BUS.addListener(PositiveWeapon436::tick);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, PositiveWeapon436::beforeMitigation);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, PositiveWeapon436::beforeHealth);
    }

    private static boolean exactIdentityCoverage(java.util.Collection<?> expected, java.util.Collection<?> returned) {
        return expected.size() == returned.size() && expected.stream().allMatch(value -> returned.stream().anyMatch(observed -> observed == value));
    }

    private static int verifyActualOriginalSessionReturns(Object session, Object preparation, Map<String, Object> row) throws Exception {
        Object[] staticSteps = (Object[])read(session, "steps");
        Object plan = read(session, "expected");
        var size = plan.getClass().getDeclaredMethod("size"); size.setAccessible(true);
        int staticSize = ((Number)size.invoke(plan)).intValue();
        row.put("originalStaticExpectedPlanSize", staticSize);
        row.put("actualStaticSessionStepCount", staticSteps.length);
        if (staticSteps.length != staticSize || read(session, "active") != null)
            throw new AssertionError("Original declared static plan or active terminal step mismatch");
        Object book = read(session, "timerSemantics");
        row.put("originalDynamicSemanticBookPresent", book != null);
        if (book == null) {
            boolean cursorMatches = read(session, "expectedCursor") == read(plan, "expectedAfter");
            row.put("exactExpectedTerminalCursorIdentity", cursorMatches);
            if (!cursorMatches || staticSize <= 0) throw new AssertionError("Original static plan lacks exact completed terminal cursor");
            var operation = plan.getClass().getDeclaredMethod("operation", int.class); operation.setAccessible(true);
            for (int index = 0; index < staticSteps.length; index++) {
                Object step = staticSteps[index];
                if (read(step, "session") != session || read(step, "expected") != operation.invoke(plan, index)
                    || !Boolean.TRUE.equals(read(step, "entered")) || !Boolean.TRUE.equals(read(step, "originalReturned"))
                    || !Boolean.TRUE.equals(read(step, "closed")) || read(step, "originalFailure") != null || read(step, "observationFailure") != null)
                    throw new AssertionError("An exact original static plan step failed to return/close");
            }
            row.put("exactOriginalStaticStepIdentityAndReturnCoverage", true);
            row.put("originalExpectedReturnedStepCount", staticSize);
            return staticSize;
        }
        if (!Boolean.TRUE.equals(read(book, "frozen")) || read(book, "session") != session || read(book, "prepared") != preparation
            || read(preparation, "timerSemantics") != book || read(session, "lineage") != read(preparation, "lineage"))
            throw new AssertionError("Original dynamic semantic book is not frozen/bound to the actual prepared Session");
        Map<?, ?> nativePuts = (Map<?, ?>)read(preparation, "nativePuts");
        Map<?, ?> materialized = (Map<?, ?>)read(book, "materialized");
        java.util.Set<?> nativeReturned = (java.util.Set<?>)read(book, "nativeReturned");
        Map<?, ?> targetWrites = (Map<?, ?>)read(book, "targetWrites");
        java.util.Set<?> targetReturned = (java.util.Set<?>)read(book, "targetReturned");
        if (!exactIdentityCoverage(nativePuts.keySet(), nativeReturned)
            || !exactIdentityCoverage(nativePuts.values(), materialized.values())
            || !exactIdentityCoverage(targetWrites.keySet(), targetReturned))
            throw new AssertionError("Exact original quoted native/target leaves or bindings did not all return");
        List<Object> nativeOperations = new ArrayList<>(); row.put("actualOriginalMaterializedNativeOperations", nativeOperations);
        for (var entry : materialized.entrySet()) {
            String kind = read(entry.getKey(), "kind").toString();
            if (!kind.equals("NATIVE_PUT")) throw new AssertionError("Unexpected materialized original native operation kind");
            nativeOperations.add(Map.of("kind", kind, "key", String.valueOf(read(entry.getKey(), "key")), "exactQuotedBindingAndReturnedLeafIdentity", true));
        }
        int expectedReturns = materialized.size();
        Map<?, ?> groups = (Map<?, ?>)read(book, "groups");
        List<Object> observedGroups = new ArrayList<>(); row.put("actualOriginalCompletedSemanticGroups", observedGroups);
        for (Object group : groups.values()) {
            Object projection = read(group, "projection");
            java.util.Set<?> timers = (java.util.Set<?>)read(group, "timers");
            java.util.Set<?> ranged = (java.util.Set<?>)read(group, "ranged");
            boolean acquired = Boolean.TRUE.equals(read(group, "acquired"));
            boolean rooted = Boolean.TRUE.equals(read(group, "rooted"));
            if (!Boolean.TRUE.equals(read(group, "done")) || projection == null)
                throw new AssertionError("Original selected dynamic semantic group was not complete");
            Map<?, ?> writes = (Map<?, ?>)read(projection, "writes");
            Map<?, ?> rangedLongs = (Map<?, ?>)read(projection, "rangedLongs");
            if (!timers.equals(writes.keySet()) || acquired != rooted
                || (acquired ? !ranged.equals(rangedLongs.keySet()) : !ranged.isEmpty()))
                throw new AssertionError("Original semantic group lacks exact quoted timer/ranged key return coverage");
            expectedReturns += timers.size() + ranged.size() + (acquired ? 1 : 0) + (rooted ? 1 : 0);
            observedGroups.add(Map.of("done", true, "profile", String.valueOf(read(group, "profile")),
                "quotedTimerWrites", writes.size(), "returnedTimers", timers.size(), "returnedRanged", ranged.size(),
                "acquired", acquired, "rooted", rooted, "exactQuotedKeyCoverage", true));
        }
        row.put("originalQuotedNativePutCount", nativePuts.size());
        row.put("originalReturnedNativeLeafCount", nativeReturned.size());
        row.put("originalQuotedTargetWriteCount", targetWrites.size());
        row.put("originalReturnedTargetLeafCount", targetReturned.size());
        row.put("exactOriginalNativeBindingAndTargetReturnIdentityCoverage", true);
        row.put("originalExpectedReturnedStepCount", expectedReturns);
        if (expectedReturns <= 0) throw new AssertionError("No actual original dynamic semantic step returned");
        return expectedReturns;
    }

    private static Object read(Object instance, String name) throws Exception {
        for (Class<?> type = instance.getClass(); type != null; type = type.getSuperclass()) {
            try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(instance); }
            catch (NoSuchFieldException missing) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static Object readStatic(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(null);
    }

    private static void observe(String phase, NativeWeaponDamageEvent event) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("phase", phase); row.put("amount", event.getAmount());
        try {
            if (event.getSource().getEntity() instanceof ServerPlayer player) {
                CompoundTag signed = net.farmingtales.ftboss.item.FoodWeaponSourceLedger.resolveReadOnly(event.getSource(), player);
                capturedSource = event.getSource(); capturedSigned = signed.copy();
                row.put("resolvedSnapshotPresent", !signed.isEmpty());
                row.put("resolvedSnapshotValid", net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(signed, player));
                row.put("repairEpochPresent", signed.hasUUID("OwnerRepairAuthorityEpoch"));
                row.put("serverSignaturePresent", signed.getString("ServerSignature").length() == 64);
                Object domainAtEvent = ((net.farmingtales.ftboss.item.OwnerFieldEntityAccess)player).ftboss$ownerFieldDomain();
                row.put("diagnostic_ownerDomainPresentAtActualDamagePhase", domainAtEvent != null);
                if (domainAtEvent != null) row.put("diagnostic_ownerDomainRetiredAtActualDamagePhase", read(domainAtEvent, "retired"));
                Object persistentAtEvent = read(player, "persistentData");
                row.put("diagnostic_originalFxCooldownAtActualDamagePhase", persistentAtEvent instanceof CompoundTag tag ? tag.getLong("ftboss:weapon_fx_hit") : -1L);
                row.put("diagnostic_actualDamageTick", player.serverLevel().getGameTime());
            }
            Object waiters = readStatic(net.farmingtales.ftboss.item.FoodWeaponFinalDamage.class, "GUARD_WAITERS");
            java.util.Deque<?> queued = (java.util.Deque<?>)((ThreadLocal<?>)waiters).get();
            row.put("guardWaiters", queued.size());
            for (Object action : queued) if (!actions.contains(action)) actions.add(action);
        } catch (Throwable failure) { row.put("observationFailure", failure.toString()); }
        damageEvents.add(row);
    }

    private static void beforeMitigation(NativeWeaponDamageEvent.BeforeMitigation event) { observe("beforeMitigation", event); }
    private static void beforeHealth(NativeWeaponDamageEvent.BeforeHealth event) { observe("beforeHealth", event); }

    private static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        if (pending == null) return;
        ServerStartedEvent started = pending; pending = null; run(started);
    }

    private static void run(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        List<Map<String, Object>> cases = new ArrayList<>(); report.put("cases", cases);
        List<ServerPlayer> scratches = new ArrayList<>();
        List<io.netty.channel.embedded.EmbeddedChannel> channels = new ArrayList<>();
        try {
            var server = event.getServer();
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var leaseMethod = guard.getDeclaredMethod("lease", Object.class); leaseMethod.setAccessible(true);
            var generationMethod = guard.getDeclaredMethod("generation", Object.class); generationMethod.setAccessible(true);
            for (int sample : new int[]{13}) {
                UUID owner = new UUID(690, sample + 1);
                ServerPlayer player = new ServerPlayer(server, server.overworld(), new GameProfile(owner, "ft_exact_" + sample), ClientInformation.createDefault());
                scratches.add(player);
                if (storage.load(player).isEmpty()) throw new AssertionError("Original paid native melee weapon reload did not run");
                var actual = player.getInventory().getItem(0);
                String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(actual.getItem()).toString();
                Map<String, Object> row = new LinkedHashMap<>(); row.put("id", id); cases.add(row);
                if (!List.of("minecraft:wooden_sword", "minecraft:wooden_axe", "minecraft:stone_sword", "minecraft:stone_axe", "minecraft:golden_sword", "minecraft:golden_axe", "minecraft:diamond_sword", "minecraft:diamond_axe", "minecraft:netherite_sword", "minecraft:netherite_axe", "cataclysm:khopesh", "cataclysm:black_steel_sword", "cataclysm:black_steel_axe", "ftboss:defect_claw", "ftboss:turkey_legs", "ftboss:apple_stem", "ftboss:blooming_thorns", "bakery:rolling_pin", "bakery:bread_knife").get(sample).equals(id)) throw new AssertionError("Exact registered variant changed on original reload");
                Object actualLease = leaseMethod.invoke(null, actual);
                Object generation = generationMethod.invoke(null, actual);
                if (actualLease == null || generation == null || ((Number)read(actualLease, "generation")).longValue() != 2L)
                    throw new AssertionError("Melee test requires genuine paid generation-two original reload, not empty authority");
                row.put("genuineNonNullLeaseAndGenerationBeforeAttack", true);
                if (net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual)
                    || net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(actual))
                    throw new AssertionError("Original installed material configuration was not preserved combat-ready on genuine paid reload");
                row.put("actualPaidReloadRetainedOriginalInstalledFunctionReadyConfiguration", true);
                row.put("originalConfiguredMaximumDamage", actual.getMaxDamage());
                int expectedMaximum = new int[]{59, 59, 131, 131, 32, 32, 1561, 1561, 2031, 2031, 750, 750, 750, 1561, 1500, 2031, 2031, 59, 250}[sample];
                String expectedClass = List.of("net.minecraft.world.item.SwordItem", "net.minecraft.world.item.AxeItem", "net.minecraft.world.item.SwordItem", "net.minecraft.world.item.AxeItem", "net.minecraft.world.item.SwordItem", "net.minecraft.world.item.AxeItem", "net.minecraft.world.item.SwordItem", "net.minecraft.world.item.AxeItem", "net.minecraft.world.item.SwordItem", "net.minecraft.world.item.AxeItem", "net.minecraft.world.item.SwordItem", "net.minecraft.world.item.SwordItem", "net.minecraft.world.item.AxeItem", "net.minecraft.world.item.SwordItem", "net.minecraft.world.item.SwordItem", "net.minecraft.world.item.SwordItem", "net.minecraft.world.item.SwordItem", "net.minecraft.world.item.SwordItem", "net.minecraft.world.item.SwordItem").get(sample);
                if (actual.getMaxDamage() != expectedMaximum || !actual.getItem().getClass().getName().equals(expectedClass))
                    throw new AssertionError("Original configured exact registered implementation/maximum damage drift");
                row.put("originalExactRegisteredClassAndMaximumPreserved", true);
                CompoundTag beforeTracking = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                Object componentBeforeTracking = actual.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                for (int repetition = 0; repetition < 2; repetition++) net.farmingtales.ftboss.item.FoodWeaponLifecycle.ensureTracked(actual);
                if (actual.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) != componentBeforeTracking
                    || !beforeTracking.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    || leaseMethod.invoke(null, actual) != actualLease || generationMethod.invoke(null, actual) != generation
                    || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Repeated original ensureTracked no-op replaced actual component or retired genuine paid authority");
                row.put("repeatedOriginalEnsureTrackedPreservedExactComponentIdentityWholeImageAndLiveAuthority", true);
                var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
                var channel = new io.netty.channel.embedded.EmbeddedChannel(connection); channels.add(channel);
                player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(server, connection, player,
                    net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(), false));
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).add(player);
                ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).put(owner, player);
                player.moveTo(0.0, -59.0, 0.0, 0.0F, 0.0F);
                player.setHealth(10.0F);
                var target = new net.minecraft.world.entity.monster.Zombie(server.overworld());
                target.setNoAi(true); target.moveTo(0.0, -59.0, 1.5, 0.0F, 0.0F);
                if (!server.overworld().addFreshEntity(target)) throw new AssertionError("Actual melee target was not added");
                int[] attempts = new int[]{0};
                boolean foreignResult = net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeMeleeActualHurt(player, target,
                    player.damageSources().playerAttack(player), 1.0F, arguments -> {
                        attempts[0]++;
                        try {
                            if (((ThreadLocal<?>)readStatic(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, "PRIVATE_NATIVE_HURT")).get() != null
                                || ((ThreadLocal<?>)readStatic(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, "PRIVATE_INGRESS")).get() != null)
                                throw new AssertionError("Foreign direct callback manufactured native melee authority");
                        } catch (Exception failure) { throw new IllegalStateException(failure); }
                        return false;
                    });
                if (foreignResult || attempts[0] != 1 || !beforeTracking.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    || leaseMethod.invoke(null, actual) != actualLease || generationMethod.invoke(null, actual) != generation
                    || Boolean.TRUE.equals(read(generation, "retired"))) throw new AssertionError("Foreign direct native melee call changed original outcome or authority");
                row.put("foreignDirectEntryDelegatesExactlyOnceWithoutAuthorityOrDataMutation", true);
                RuntimeException originalFailure = new RuntimeException("original foreign-call identity sentinel");
                try {
                    net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeMeleeActualHurt(player, target,
                        player.damageSources().playerAttack(player), 1.0F, arguments -> { throw originalFailure; });
                    throw new AssertionError("Original callback exception was swallowed");
                } catch (RuntimeException returned) {
                    if (returned != originalFailure) throw new AssertionError("Original callback exception identity changed", returned);
                }
                row.put("foreignDirectEntryPreservesOriginalCallbackExceptionIdentity", true);
                capturedSource = null; capturedSigned = null;
                actions.clear(); damageEvents.clear();
                float healthBefore = target.getHealth();
                int wearBefore = actual.getDamageValue();
                player.attack(target);
                row.put("originalPlayerAttackReturned", true); row.put("healthBefore", healthBefore); row.put("healthAfter", target.getHealth());
                row.put("wearBefore", wearBefore); row.put("wearAfter", actual.getDamageValue()); row.put("damageEvents", new ArrayList<>(damageEvents));
                List<Map<String, Object>> publications = new ArrayList<>(); row.put("publications", publications);
                for (Object action : actions) {
                    Map<String, Object> publicationRow = new LinkedHashMap<>(); publications.add(publicationRow);
                    Object publication = read(action, "publication");
                    publicationRow.put("publicationPresent", publication != null);
                    publicationRow.put("preparedCommitted", read(read(action, "prepared"), "committed"));
                    if (publication == null) continue;
                    publicationRow.put("originalSequencePresent", read(publication, "originalSequence") != null);
                    Object preparation = read(publication, "ownerReservePreparation");
                    publicationRow.put("preparationPresent", preparation != null);
                    if (preparation == null) continue;
                    for (String flag : List.of("unsupported", "sealed", "bound", "retired")) publicationRow.put("prepared_" + flag, read(preparation, flag));
                    Object session = read(preparation, "session");
                    publicationRow.put("sessionPresent", session != null);
                    if (session != null) {
                        var complete = session.getClass().getDeclaredMethod("traceComplete"); complete.setAccessible(true);
                        publicationRow.put("traceComplete", complete.invoke(session));
                        for (String flag : List.of("traceRevoked", "coverageInstalled", "next")) publicationRow.put("session_" + flag, read(session, flag));
                        for (String field : List.of("firstObservationFailure", "firstRevocationSite434")) {
                            Object problem = read(session, field);
                            java.io.StringWriter diagnostic = new java.io.StringWriter();
                            if (problem instanceof Throwable throwable) throwable.printStackTrace(new java.io.PrintWriter(diagnostic));
                            publicationRow.put("diagnostic_" + field, diagnostic.toString());
                        }
                        List<Object> producerDiagnostics = new ArrayList<>(); publicationRow.put("diagnostic_producerKindsAndAttempts", producerDiagnostics);
                        for (Object producer : (List<?>)read(action, "producers")) {
                            Map<String, Object> producerRow = new LinkedHashMap<>(); producerDiagnostics.add(producerRow);
                            producerRow.put("class", producer.getClass().getName());
                            for (String field : List.of("plan", "consumerPrepared")) {
                                try {
                                    Object value = read(producer, field);
                                    producerRow.put(field + "Class", value == null ? null : value.getClass().getName());
                                    if (field.equals("plan") && value != null) {
                                        try { producerRow.put("actualQuotedAttempts", String.valueOf(read(value, "attempts"))); } catch (NoSuchFieldException absent) { }
                                        try {
                                            List<Object> leafRows = new ArrayList<>(); producerRow.put("actualLeaves", leafRows);
                                            for (Object leaf : (List<?>)read(value, "typedLeaves")) {
                                                Map<String, Object> leafRow = new LinkedHashMap<>(); leafRows.add(leafRow); leafRow.put("class", leaf.getClass().getName());
                                                for (String leafField : List.of("attempt", "key", "value", "amount")) {
                                                    try { leafRow.put(leafField, String.valueOf(read(leaf, leafField))); } catch (NoSuchFieldException absent) { }
                                                }
                                            }
                                        } catch (NoSuchFieldException absent) { }
                                    }
                                } catch (NoSuchFieldException absent) { }
                            }
                        }
                        Map<String, Object> preparedDiagnostic = new LinkedHashMap<>(); publicationRow.put("diagnostic_preparedFields", preparedDiagnostic);
                        for (String field : List.of("unsupported", "sealed", "bound", "retired", "catalogueGeneration", "coverage", "nativePuts", "nativeBefore"))
                            preparedDiagnostic.put(field, String.valueOf(read(preparation, field)));
                        for (String side : List.of("player", "carrier")) {
                            Object domain = read(preparation, side);
                            if (domain == null) continue;
                            Map<String, Object> domainFields = new LinkedHashMap<>(); preparedDiagnostic.put(side, domainFields);
                            for (Field field : domain.getClass().getDeclaredFields()) {
                                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                                field.setAccessible(true); domainFields.put(field.getName(), String.valueOf(field.get(domain)));
                            }
                        }
                        publicationRow.put("diagnostic_fieldRegistryInvalid", readStatic(Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry"), "invalid"));
                        Object diagnosticCommit = null;
                        for (Object producer : (List<?>)read(action, "producers")) {
                            if (producer.getClass().getSimpleName().equals("EvolutionProducer")) diagnosticCommit = read(producer, "consumerCommit");
                        }
                        if (diagnosticCommit == null) throw new AssertionError("Actual original evolution consumer commit missing");
                        publicationRow.put("diagnostic_commitTimerSemanticFailure", String.valueOf(read(diagnosticCommit, "timerSemanticFailure")));
                        List<Object> cursorRows = new ArrayList<>(); publicationRow.put("diagnostic_actualSemanticCursors", cursorRows);
                        for (Object cursor : (List<?>)read(diagnosticCommit, "timerSemantics")) {
                            Map<String, Object> observedCursor = new LinkedHashMap<>(); cursorRows.add(observedCursor);
                            for (String field : List.of("next", "returnedHelpers", "timerEntered", "rangedActive", "rangedEntered", "timerReturned", "closed", "originalFailure", "status", "admittedKeys", "image", "projection"))
                                observedCursor.put(field, String.valueOf(read(cursor, field)));
                            Object program = read(cursor, "program");
                            for (String field : List.of("profile", "unsupported", "initialData", "baseline", "instructions"))
                                observedCursor.put("program_" + field, String.valueOf(read(program, field)));
                        }
                        Object diagnosticBook = read(session, "timerSemantics");
                        List<Object> diagnosticGroups = new ArrayList<>(); publicationRow.put("diagnostic_actualSemanticGroups", diagnosticGroups);
                        if (diagnosticBook != null) for (Object group : ((Map<?, ?>)read(diagnosticBook, "groups")).values()) {
                            Map<String, Object> observedGroup = new LinkedHashMap<>(); diagnosticGroups.add(observedGroup);
                            for (String field : List.of("profile", "selectedInstructions", "emptyProjection", "alternatives", "timers", "ranged", "projection", "acquired", "rooted", "guardObserved", "guardEmpty", "done"))
                                observedGroup.put(field, String.valueOf(read(group, field)));
                        }
                        if (Boolean.TRUE.equals(publicationRow.get("session_traceRevoked"))) throw new AssertionError("Excluded read-only diagnostic captured genuine defect semantic revocation");
                        int expectedSize = verifyActualOriginalSessionReturns(session, preparation, publicationRow);
                        Object failure = read(session, "firstObservationFailure");
                        publicationRow.put("firstObservationFailure", failure == null ? null : failure.toString());
                        if (!Boolean.TRUE.equals(publicationRow.get("traceComplete"))
                            || !Boolean.TRUE.equals(publicationRow.get("session_coverageInstalled"))
                            || Boolean.TRUE.equals(publicationRow.get("session_traceRevoked"))
                            || ((Number)publicationRow.get("session_next")).intValue() != expectedSize || failure != null)
                            throw new AssertionError("Original paid melee Session incomplete or revoked for " + id);

                    }
                }
                Object snapshot = ((Map<?, ?>)readStatic(net.farmingtales.ftboss.item.FoodWeaponBondHits.class, "MELEE")).get(owner);
                row.put("originalMeleeSnapshotPresent", snapshot != null);
                if (snapshot != null) {
                    CompoundTag signed = (CompoundTag)read(snapshot, "snapshot");
                    row.put("originalMeleeSnapshotValid", net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(signed, player));
                    row.put("originalMeleeSnapshotRepairEpochPresent", signed.hasUUID("OwnerRepairAuthorityEpoch"));
                }
                row.put("sameLeaseAndGenerationAfterAttack", leaseMethod.invoke(null, actual) == actualLease && generationMethod.invoke(null, actual) == generation);
                row.put("generationRetiredAfterAttack", read(generation, "retired"));
                row.put("capturedNativeMeleePurpose", capturedSigned == null ? "" : capturedSigned.getString("DamagePurpose"));
                boolean sourceDeniedAfterReturn = capturedSource != null && capturedSigned != null
                    && net.farmingtales.ftboss.item.FoodWeaponSourceLedger.resolveReadOnly(capturedSource, player).isEmpty()
                    && !net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(capturedSigned, player);
                row.put("sameOriginalSourceAndSignedSnapshotDeniedOutsideActualHurtScope", sourceDeniedAfterReturn);
                if (!sourceDeniedAfterReturn) throw new AssertionError("Original melee callback returned with reusable actual-hurt authority");
                target.discard();
                if (target.getHealth() >= healthBefore || damageEvents.isEmpty() || publications.isEmpty()
                    || publications.stream().anyMatch(value -> !Boolean.TRUE.equals(value.get("traceComplete")) || Boolean.TRUE.equals(value.get("prepared_retired"))))
                    throw new AssertionError("Actual paid original melee did not damage target and complete live signed Session for " + id);
                if (!Boolean.TRUE.equals(row.get("originalMeleeSnapshotValid")) || !Boolean.TRUE.equals(row.get("originalMeleeSnapshotRepairEpochPresent"))
                    || !Boolean.TRUE.equals(row.get("sameLeaseAndGenerationAfterAttack")) || Boolean.TRUE.equals(row.get("generationRetiredAfterAttack")))
                    throw new AssertionError("Actual original melee lost signed non-null repair authority for " + id);
                storage.save(player);
                row.put("actualOriginalPostCombatSaveCalled", true);
                Class<?> storeType = Class.forName("net.farmingtales.ftboss.item.OwnerLineageRevocationStore");
                var readDocument = storeType.getDeclaredMethod("readRepairDocument", UUID.class, boolean.class); readDocument.setAccessible(true);
                Object store = read(actualLease, "store");
                UUID documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-continuation/v1/" + owner).getBytes(StandardCharsets.UTF_8));
                CompoundTag committed = (CompoundTag)readDocument.invoke(store, documentOwner, false);
                Path playerFile = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_DATA_DIR).resolve(owner + ".dat");
                CompoundTag saved = net.minecraft.nbt.NbtIo.readCompressed(playerFile, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                CompoundTag ticket = saved.getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
                CompoundTag committedRecord = committed.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString());
                if (!committed.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")
                    || !committedRecord.getString("Status").equals("COMMITTED_ORIGINAL_SAVE") || ticket.getLong("LeaseGeneration") != 2L
                    || !ticket.getUUID("Save").equals(saved.getUUID("FTBossRepairSave")))
                    throw new AssertionError("Actual post-melee original save did not commit the genuine generation-two continuation");
                row.put("actualOriginalPostCombatSaveAuthenticatedCommitted", true);
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(player);
                ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).remove(owner, player);
            }
            report.put("singleDefectDiagnosticUnexpectedlyCompletedOriginalSession", true);
            report.put("failures", 0);
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        } finally {
            try {
                var players = (List<ServerPlayer>)read(event.getServer().getPlayerList(), "players");
                var byId = (Map<UUID, ServerPlayer>)read(event.getServer().getPlayerList(), "playersByUUID");
                for (ServerPlayer scratch : scratches) { players.remove(scratch); byId.remove(scratch.getUUID(), scratch); }
                for (var channel : channels) channel.finishAndReleaseAll();
                report.put("scratchPlayersRemoved", scratches.stream().noneMatch(players::contains));
            } catch (Exception failure) { report.put("failures", 1); report.put("cleanupFailure", failure.toString()); }
        }
        report.put("authorityWrittenByReflection", false); report.put("authenticatedMultiplayerAccepted", false);
        report.put("allSixFamilySignedCombatAccepted", false); report.put("staffCastingSubstitutedWithMelee", false);
        report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
