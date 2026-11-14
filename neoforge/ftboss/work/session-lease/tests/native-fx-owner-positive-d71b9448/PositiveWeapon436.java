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
                if (damageEvents.size() != 2 || target.getHealth() >= healthBefore || actual.getDamageValue() != wearBefore + 1)
                    throw new AssertionError("Original actual defect damage or original one wear did not return");
                for (Map<String, Object> damage : damageEvents) {
                    if (!Boolean.TRUE.equals(damage.get("resolvedSnapshotValid")) || !Boolean.TRUE.equals(damage.get("repairEpochPresent"))
                        || !Boolean.TRUE.equals(damage.get("serverSignaturePresent")) || !Boolean.TRUE.equals(damage.get("diagnostic_ownerDomainPresentAtActualDamagePhase"))
                        || !Boolean.FALSE.equals(damage.get("diagnostic_ownerDomainRetiredAtActualDamagePhase"))
                        || !damage.get("diagnostic_originalFxCooldownAtActualDamagePhase").equals(damage.get("diagnostic_actualDamageTick")))
                        throw new AssertionError("Original FX write did not preserve exact live owner domain at the actual signed damage phase");
                }
                List<Map<String, Object>> publications = new ArrayList<>(); row.put("publications", publications);
                for (Object action : actions) {
                    Object publication = read(action, "publication");
                    Object preparation = publication == null ? null : read(publication, "ownerReservePreparation");
                    Map<String, Object> observed = new LinkedHashMap<>(); publications.add(observed);
                    observed.put("publicationPresent", publication != null); observed.put("preparationPresent", preparation != null);
                    if (preparation != null) {
                        observed.put("nativeBeforePresent", read(preparation, "nativeBefore") != null);
                        observed.put("remainingPreparedUnsupported", read(preparation, "unsupported"));
                        Object session = read(preparation, "session");
                        if (session != null) {
                            var complete = session.getClass().getDeclaredMethod("traceComplete"); complete.setAccessible(true);
                            observed.put("actualTraceComplete", complete.invoke(session));
                            observed.put("actualTraceRevoked", read(session, "traceRevoked"));
                        }
                    }
                }
                if (publications.isEmpty() || publications.stream().anyMatch(value -> !Boolean.TRUE.equals(value.get("nativeBeforePresent"))))
                    throw new AssertionError("Actual FX successor did not reach the real owner-field reservation");
                row.put("originalFxCooldownOwnerTransitionAtBothActualSignedDamagePhases", true);
                row.put("wholeNineteenSignedSessionAccepted", false);
                row.put("originalPostCombatSignedSaveOrRestartAccepted", false);
                target.discard();
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(player);
                ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).remove(owner, player);
            }
            report.put("singleOriginalFxCooldownOwnerTransitionScopeOnly", true);
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
