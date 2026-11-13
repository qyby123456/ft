package ftboss.storage308;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_storage308")
public final class PositiveWeapon436 {
    public PositiveWeapon436() {
        NeoForge.EVENT_BUS.addListener(PositiveWeapon436::started);
        NeoForge.EVENT_BUS.addListener(PositiveWeapon436::timerFixtureTick);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST, PositiveWeapon436::unknownJoinMutation);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, PositiveWeapon436::mitigation);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, PositiveWeapon436::health);
    }

    private static boolean injectUnknown;
    private static void unknownJoinMutation(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (injectUnknown && event.getEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow
            && arrow.getOwner() instanceof ServerPlayer) arrow.getPersistentData().putInt("ft333_unknown_join_mutation", 1);
    }
    private static final List<Object> observations = new ArrayList<>();
    private static final List<Object> roots = new ArrayList<>();
    private static final List<Object> actions = new ArrayList<>();


    private static Object inspect436(Object value, int depth) throws Exception {
        if (value == null) return "null";
        if (value instanceof Boolean || value instanceof Number || value instanceof String) return value;
        if (value instanceof Enum<?> enumeration) return enumeration.name();
        if (value instanceof Throwable failure) {
            java.io.StringWriter trace = new java.io.StringWriter();
            failure.printStackTrace(new java.io.PrintWriter(trace));
            return Map.of("failure", failure.toString(), "trace", trace.toString());
        }
        if (value instanceof java.util.Collection<?> collection) return Map.of("type", value.getClass().getName(), "size", collection.size());
        if (value instanceof java.util.Map<?, ?> map) return Map.of("type", value.getClass().getName(), "size", map.size());
        if (value.getClass().isArray()) {
            List<Object> elements = new ArrayList<>();
            int length = java.lang.reflect.Array.getLength(value);
            if (depth > 0) for (int index = 0; index < Math.min(length, 64); index++) elements.add(inspect436(java.lang.reflect.Array.get(value, index), depth - 1));
            return Map.of("length", length, "elements", elements);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", value.getClass().getName());
        if (depth > 0 && value.getClass().getName().startsWith("net.farmingtales.ftboss.item.")) {
            for (Field field : value.getClass().getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                result.put(field.getName(), inspect436(field.get(value), depth - 1));
            }
        }
        return result;
    }

    private static Object readStatic(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(null);
    }

    private static void observe(String phase, net.farmingtales.ftboss.event.NativeWeaponDamageEvent event) {
        Map<String, Object> row = new LinkedHashMap<>(); row.put("phase", phase); row.put("amount", event.getAmount());
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            try { row.put("playerDomainRetired", read(((net.farmingtales.ftboss.item.OwnerFieldEntityAccess)player).ftboss$ownerFieldDomain(), "retired")); }
            catch (Throwable failure) { throw new IllegalStateException(failure); }
            CompoundTag image = net.farmingtales.ftboss.item.FoodWeaponSourceLedger.resolveReadOnly(event.getSource(), player);
            row.put("resolvedSnapshotPresent", !image.isEmpty());
            row.put("resolvedSnapshotValid", net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(image, player));
        }
        try {
            Object[] active = (Object[])readStatic(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, "ACTIVE_ROOT_PROJECTIONS");
            int count = 0;
            for (Object root : active) {
                if (root == null) continue;
                count++; if (!roots.contains(root)) roots.add(root);
                for (String flag : List.of("closed", "retired", "projectionSlot")) row.put("root_" + flag, read(root, flag));
                Object binding = read(root, "current"), cut = read(root, "readCut"), expectation = read(root, "expectation");
                for (String flag : List.of("closed", "depth", "rawAmountBits")) row.put("binding_" + flag, read(binding, flag));
                row.put("bindingSourceIdentity", read(binding, "source") == event.getSource());
                row.put("bindingTargetIdentity", read(binding, "target") == event.getEntity());
                row.put("cutClosed", read(cut, "closed")); row.put("acceptedImagePresent", read(cut, "acceptedImage") != null);
                row.put("expectationClosed", expectation == null ? null : read(expectation, "closed"));
                if (expectation != null) {
                    Object expectedActual = read(expectation, "actual");
                    row.put("expectationItemClass", expectedActual.getClass().getName());
                    row.put("expectationRepairGeneration", read(expectation, "repairGeneration") != null);
                }
                Object observation = read(root, "nativeProjectileObservation");
                if (observation != null) {
                    Object failure = read(observation, "failure");
                    row.put("projectileObservationFailed", read(failure, "failed"));
                    row.put("projectileObservationFinished", read(observation, "finished"));
                }
                Object publication = read(root, "writerPublication");
                row.put("writerPublicationPresent", publication != null);
                if (publication != null) {
                    Object finalPublication = read(read(publication, "receipt"), "publication");
                    row.put("finalPublicationPresent", finalPublication != null);
                    if (finalPublication != null) row.put("originalSequencePresent", read(finalPublication, "originalSequence") != null);
                }
            }
            row.put("activeRoots", count);
            Object local = readStatic(net.farmingtales.ftboss.item.FoodWeaponFinalDamage.class, "EVENT_CAPTURES");
            row.put("eventCaptureDepth", ((java.util.Deque<?>)((ThreadLocal<?>)local).get()).size());
            Object capture = ((java.util.Deque<?>)((ThreadLocal<?>)local).get()).peekLast();
            if (capture != null) {
                row.put("captureFailed", read(capture, "failed"));
                row.put("capturedProducerCount", ((List<?>)read(capture, "producers")).size());
            }
            Object waiters = readStatic(net.farmingtales.ftboss.item.FoodWeaponFinalDamage.class, "GUARD_WAITERS");
            java.util.Deque<?> queued = (java.util.Deque<?>)((ThreadLocal<?>)waiters).get();
            row.put("guardWaiters", queued.size());
            for (Object action : queued) if (!actions.contains(action)) actions.add(action);
        } catch (Throwable failure) { row.put("observationFailure", failure.toString()); }
        observations.add(row);
    }

    private static void mitigation(net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation event) { observe("beforeMitigation", event); }
    private static void health(net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeHealth event) { observe("beforeHealth", event); }

    private static Object read(Object instance, String name) throws Exception {
        for (Class<?> type = instance.getClass(); type != null; type = type.getSuperclass()) {
            try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(instance); }
            catch (NoSuchFieldException missing) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static ServerStartedEvent pendingTimerFixture;

    private static void started(ServerStartedEvent event) {
        pendingTimerFixture = event;
    }

    private static void timerFixtureTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post tick) {
        ServerStartedEvent pending = pendingTimerFixture;
        if (pending == null || pending.getServer().overworld().getGameTime() < 80L) return;
        pendingTimerFixture = null;
        runTimerFixture(pending);
    }

    private static void runTimerFixture(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        List<Object> cases = new ArrayList<>();
        List<ServerPlayer> scratchPlayers = new ArrayList<>();
        try {




            PlayerDataStorage storage = (PlayerDataStorage)read(event.getServer().getPlayerList(), "playerIo");
            for (int sample : new int[]{0}) {
                injectUnknown = sample == 1;
                GameProfile profile = new GameProfile(new UUID(334, sample + 1), "ft317_" + sample);
                ServerPlayer original = new ServerPlayer(event.getServer(), event.getServer().overworld(), profile, ClientInformation.createDefault());
                scratchPlayers.add(original);
                net.minecraft.world.item.Item selected = Items.BOW;
                if (selected == Items.AIR) throw new AssertionError("Native weapon missing");
                ItemStack sword = new ItemStack(selected);
                CompoundTag data = new CompoundTag(), nested = new CompoundTag(); nested.putInt("value", sample + 17); data.put("nested", nested);
                CompoundTag evolution = new CompoundTag();
                evolution.putString("Profile", "butcher"); evolution.putString("EvolutionSkill", "blood_feast"); evolution.putInt("Level", 1); evolution.putInt("Experience", 0); evolution.putInt("ProgressionVersion", 2);
                data.put("FTBossFoodEvolution", evolution);
                sword.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
                if (!net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.initializeForServerEdit(sword)) throw new AssertionError("Native foundation initialization failed: " + net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.read(sword).failure());
                original.getInventory().setItem(0, sword);
                original.getInventory().setItem(1, new ItemStack(Items.ARROW, 8));
                original.getPersistentData().putInt("ft317_player", sample + 31);
                if (sample == 3) {
                    CompoundTag timers = new CompoundTag();
                    timers.putLong("LastSkillProc.blood_feast", 5L);
                    original.getPersistentData().put("FTBossRangedProcTimers", timers);
                }
                if (sample == 4) original.getPersistentData().putInt("FTBossRangedProcTimers", 19);

                ((List<ServerPlayer>)read(event.getServer().getPlayerList(), "players")).add(original);
                net.farmingtales.ftboss.item.NativeFoodWeaponWear.manage(original, sword);
                net.farmingtales.ftboss.item.FoodWeaponLifecycle.ensureTracked(sword);
                sword.setDamageValue(10);
                storage.save(original);
                ((List<ServerPlayer>)read(event.getServer().getPlayerList(), "players")).remove(original);
                ServerPlayer restored = new ServerPlayer(event.getServer(), event.getServer().overworld(), profile, ClientInformation.createDefault());
                scratchPlayers.add(restored);
                if (storage.load(restored).isEmpty()) throw new AssertionError("Actual player storage load failed");
                ItemStack actual = restored.getInventory().getItem(0);
                Object input = ((OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("sample", sample); row.put("actualSword", actual.is(selected)); row.put("count", actual.getCount());
                row.put("inputInstalled", input != null);
                if (input != null) {
                    row.put("retired", read(input, "retired")); row.put("generation", read(input, "generation"));
                    Object reference = read(input, "actual");
                    row.put("actualIdentityBound", reference instanceof java.lang.ref.WeakReference<?> weak && weak.get() == actual);
                    row.put("rootPresent", read(input, "root") != null);
                }
                row.put("scratchPlayerNotAuthenticated", !event.getServer().getPlayerList().getPlayers().contains(restored));
                cases.add(row); report.put("cases", cases);
                if (!actual.is(selected) || actual.getCount() != 1 || input == null || Boolean.TRUE.equals(read(input, "retired")) || !Boolean.TRUE.equals(row.get("actualIdentityBound"))) throw new AssertionError("Actual persisted weapon input did not bind " + row);
                if (restored.getPersistentData().getInt("ft317_player") != sample + 31) throw new AssertionError("Native player persistent data changed");
                runNativeRepair(event, restored, actual, row);
                input = ((OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                for (int hit = 0; hit < 1; hit++) {
                if (hit > 0) {
                    row = new LinkedHashMap<>();
                    row.put("sample", sample);
                    cases.add(row);
                    input = ((OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                }
                row.put("hit", hit);
                if (hit == 2) ((net.minecraft.world.level.storage.ServerLevelData)event.getServer().overworld().getLevelData()).setGameTime(event.getServer().overworld().getGameTime() + 200L);
                observations.clear(); roots.clear(); actions.clear();
                net.minecraft.world.entity.monster.Zombie target = new net.minecraft.world.entity.monster.Zombie(event.getServer().overworld());
                target.setNoAi(true); target.moveTo(0.0, -59.0, 3.0, 0.0F, 0.0F);
                restored.moveTo(0.0, -59.0, 0.0, 0.0F, 0.0F);
                if (!event.getServer().overworld().addFreshEntity(target)) throw new AssertionError("Target not added");
                row.put("unknownJoinMutationInjected", injectUnknown);
                row.put("weaponId", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(selected).toString());
                row.put("evolutionCandidate", net.farmingtales.ftboss.item.FoodWeaponEvolution.isEvolutionCandidate(actual));
                row.put("fixtureEvolutionLevel", net.farmingtales.ftboss.item.FoodWeaponEvolution.evolutionLevel(actual));
                row.put("fixtureFoundationLevel", net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.read(actual).level());
                row.put("fixtureBoundProfile", net.farmingtales.ftboss.item.FoodWeaponEvolution.firstProfileId(actual));
                row.put("fixtureGameTime", event.getServer().overworld().getGameTime());
                row.put("fixturePassiveSkills", net.farmingtales.ftboss.item.FoodWeaponProgression.passiveSkills(actual).stream().map(Enum::name).toList());
                restored.setHealth(10.0F);
                row.put("playerHealthBefore", restored.getHealth());
                row.put("rangedRootPresentBefore436", restored.getPersistentData().contains("FTBossRangedProcTimers", 10));

                float before = target.getHealth();
                try {
                    int arrowsBefore = restored.getInventory().getItem(1).getCount();
                    int damageBefore = actual.getDamageValue();
                    restored.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
                    if (sample == 2) System.getProperties().put("ft348.test.actualStack", actual);
                    try {
                        actual.releaseUsing(event.getServer().overworld(), restored, actual.getUseDuration(restored) - 20);
                    } finally {
                        System.getProperties().remove("ft348.test.actualStack");
                    }
                    row.put("unknownWearRoundTripInjected", sample == 2 && Boolean.TRUE.equals(System.getProperties().remove("ft348.test.roundTripObserved")));

                    row.put("arrowsBefore", arrowsBefore); row.put("arrowsAfter", restored.getInventory().getItem(1).getCount());
                    row.put("weaponDamageBefore", damageBefore); row.put("weaponDamageAfter", actual.getDamageValue());
                    Object inputAfterWear = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                    row.put("inputAfterWearPresent", inputAfterWear != null);
                    row.put("inputAfterWearSame", inputAfterWear == input);
                    row.put("originalInputRetiredAfterWear", read(input, "retired"));
                    if (inputAfterWear != null) {
                        row.put("inputAfterWearRetired", read(inputAfterWear, "retired"));
                        Class<?> expectedType = Class.forName("net.farmingtales.ftboss.item.FoodWeaponExpectedTrace");
                        Object expected = read(inputAfterWear, "expected");
                        java.lang.reflect.Method materialize = expectedType.getDeclaredMethod("materialize", expected.getClass()); materialize.setAccessible(true);
                        CompoundTag expectedInputImage = (CompoundTag)materialize.invoke(null, expected);
                        row.put("inputExpectedWear", expectedInputImage.getCompound("components").getInt("minecraft:damage"));
                        row.put("inputExpectedMatchesWornItem", expectedInputImage.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual)));
                        if (sample == 2) {
                            if (!Boolean.TRUE.equals(row.get("unknownWearRoundTripInjected")) || !Boolean.TRUE.equals(read(inputAfterWear, "retired"))) throw new AssertionError("Write-restore wear transaction was admitted");
                            row.put("unknownWearRoundTripRejected", true);
                        } else if (inputAfterWear == input || !Boolean.TRUE.equals(read(input, "retired")) || Boolean.TRUE.equals(read(inputAfterWear, "retired"))
                            || !expectedInputImage.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))) throw new AssertionError("Native lifecycle/wear input did not advance");
                        if (((OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerAdvanceNativeWeaponInput((net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority.WeaponInput)inputAfterWear, (net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority.WeaponInput)input)) throw new AssertionError("Direct input replacement admitted");
                        if (((OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput() != inputAfterWear) throw new AssertionError("Direct replacement changed input");
                        row.put("directInputReplacementRejected", true);

                    }

                    List<Object> arrows = new ArrayList<>();
                    row.put("arrows", arrows);
                    for (net.minecraft.world.entity.Entity entity : event.getServer().overworld().getAllEntities()) {
                        if (!(entity instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow) || arrow.getOwner() != restored) continue;
                        Map<String, Object> flight = new LinkedHashMap<>();
                        arrows.add(flight);
                        flight.put("launchPosition436", arrow.position().toString());
                        flight.put("launchVelocity436", arrow.getDeltaMovement().toString());
                        flight.put("targetBounds436", target.getBoundingBox().toString());
                        flight.put("ownerIdentity", arrow.getOwner() == restored);
                        flight.put("paidSnapshotPresent", arrow.getPersistentData().contains("FTBossWeaponMaterials", 10));
                        CompoundTag image = arrow.getPersistentData().getCompound("FTBossWeaponMaterials");
                        flight.put("paidSnapshotValid", net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(image, restored));
                        flight.put("damagePurpose", image.getString("DamagePurpose"));
                        flight.put("serverSignaturePresent", image.getString("ServerSignature").length() == 64);
                        flight.put("repairEpochPresent", image.hasUUID("OwnerRepairAuthorityEpoch"));
                        if (!image.hasUUID("OwnerRepairAuthorityEpoch") || !net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(image, restored)) throw new AssertionError("Repaired native signed snapshot missing");
                        flight.put("paidCarrier", arrow instanceof net.farmingtales.ftboss.item.NativePaidWeaponCarrier carrier && carrier.ftboss$originalOwnerPaidWeapon() != null);
                        if (arrow instanceof net.farmingtales.ftboss.item.NativePaidWeaponCarrier carrier) {
                            Object paid = carrier.ftboss$originalOwnerPaidWeapon();
                            if (paid != null) flight.put("profileFieldInitialized", read(read(paid, "receipt"), "profileFieldInitialized"));
                            if (paid != null) flight.put("imageFieldInitialized", read(read(paid, "receipt"), "imageFieldInitialized"));
                            if (paid != null) flight.put("manorOwnerFieldInitialized", read(read(paid, "receipt"), "manorOwnerFieldInitialized"));
                        }
                        for (int tick = 0; tick < 12 && !arrow.isRemoved() && target.getHealth() == before; tick++) arrow.tick();
                        flight.put("finalPosition436", arrow.position().toString());
                        flight.put("targetHealthAfterFlight436", target.getHealth());
                        flight.put("nativeImpactObserved436", !observations.isEmpty());
                        flight.put("removedAfterFlight", arrow.isRemoved()); arrow.discard();
                    }
                    if (sample == 0 && (observations.isEmpty() || target.getHealth() >= before)) {
                        row.put("projectileFlightDiagnostic436", arrows);
                        throw new AssertionError("Native projectile did not damage the positive fixture target");
                    }
                    List<Object> publications = new ArrayList<>();
                    for (Object action : actions) {
                        Map<String, Object> publicationRow = new LinkedHashMap<>();
                        List<Object> producerRows = new ArrayList<>();
                        for (Object producer : (List<?>)read(action, "producers")) {
                            Map<String, Object> producerRow = new LinkedHashMap<>();
                            producerRow.put("producerClass", producer.getClass().getName());
                            Object plan = read(producer, "plan");
                            if (plan instanceof net.farmingtales.ftboss.item.FoodWeaponHitPlan) {
                                Object catalogue = read(plan, "timerSemanticCatalogue");
                                List<Object> timerRows = new ArrayList<>();
                                for (Object program : (List<?>)read(catalogue, "programs")) {
                                    Map<String, Object> timerRow = new LinkedHashMap<>();
                                    Object leaf = read(program, "leaf");
                                    Object timerQuote = ((net.farmingtales.ftboss.item.OwnerTimerQuoteFields)leaf).ftboss$ownerTimerQuote();
                                    timerRow.put("quotePresent", timerQuote != null);
                                    timerRow.put("originPresent", timerQuote != null && read(timerQuote, "origin") != null);
                                    timerRow.put("programUnsupported", String.valueOf(read(program, "unsupported")));
                                    timerRows.add(timerRow);
                                }
                                producerRow.put("timerPrograms", timerRows);
                            }

                            producerRow.put("planClass", plan.getClass().getName());
                            producerRow.put("attemptCount", ((List<?>)read(plan, "attempts")).size());
                            producerRow.put("typedPlanBridge", producer instanceof net.farmingtales.ftboss.item.OwnerEvolutionPlanFields);
                            if (plan instanceof net.farmingtales.ftboss.item.FoodWeaponHitPlan) {
                                List<Object> leaves = new ArrayList<>();
                                for (Object leaf : (List<?>)read(plan, "typedLeaves")) leaves.add(leaf.getClass().getName());
                                producerRow.put("leaves", leaves);
                            }
                            producerRows.add(producerRow);
                        }
                        publicationRow.put("producers", producerRows);
                        Object capture = read(action, "capture");
                        publicationRow.put("finalCapturedProducerCount", ((List<?>)read(capture, "producers")).size());
                        publicationRow.put("listedProducerCount", ((List<?>)read(action, "producers")).size());
                        Object guard = read(action, "guardResult");
                        publicationRow.put("guardResultPresent", guard != null);
                        publicationRow.put("preparedCommitted", read(read(action, "prepared"), "committed"));
                        Object publication = read(action, "publication");
                        publicationRow.put("publicationPresent", publication != null);
                        if (publication != null) {
                            for (String flag : List.of("closed", "writerPlanningObservationFailure", "writerNormalObservationFailure", "writerExceptionObservationFailure", "writerCloseObservationFailure", "writerOriginalFailure")) {
                                Object value = read(publication, flag); publicationRow.put(flag, value instanceof Throwable error ? error.toString() : value);
                            }
                            Object sequence = read(publication, "originalSequence");
                            publicationRow.put("originalSequencePresent", sequence != null);
                            Object preparation = read(publication, "ownerReservePreparation");
                            publicationRow.put("preparationPresent", preparation != null);
                            if (preparation != null) {
                                for (String flag : List.of("unsupported", "sealed", "bound", "retired")) publicationRow.put("prepared_" + flag, read(preparation, flag));
                                publicationRow.put("preparedSessionPresent", read(preparation, "session") != null);
                                publicationRow.put("sessionDetail436", inspect436(read(preparation, "session"), 3));
                                Object actualSession = read(preparation, "session");
                                if (actualSession != null) {
                                    var complete = actualSession.getClass().getDeclaredMethod("traceComplete");
                                    complete.setAccessible(true);
                                    publicationRow.put("traceComplete", complete.invoke(actualSession));
                                }
                                Object actualBook = read(actualSession, "timerSemantics");
                                if (actualBook != null) {
                                    Object plannedRoot = read(actualBook, "rangedRoot");
                                    Object plannedBuffer = read(actualBook, "rangedBuffer");
                                    Map<String, Object> acquisition = new LinkedHashMap<>();
                                    acquisition.put("plannedRootIsActualPersistentRoot", plannedRoot == restored.getPersistentData());
                                    acquisition.put("plannedBufferWasNull", plannedBuffer == null);
                                    Object acquired = read(actualBook, "acquiredRangedBuffer431");
                                    acquisition.put("freshBufferBound", acquired != null);
                                    acquisition.put("freshBufferIsCurrentStoredBuffer", acquired != null && acquired == restored.getPersistentData().get("FTBossRangedProcTimers"));

                                    acquisition.put("plannedBufferIsCurrentStoredBuffer", plannedBuffer == restored.getPersistentData().get("FTBossRangedProcTimers"));
                                    acquisition.put("storedBufferPresentAfter", restored.getPersistentData().contains("FTBossRangedProcTimers", 10));
                                    acquisition.put("authorityFieldsWritten", false);
                                    publicationRow.put("rangedAcquisitionIdentity436", acquisition);
                                }

                                List<Object> groups = new ArrayList<>();
                                if (actualBook != null) for (Object group : ((Map<?, ?>)read(actualBook, "groups")).values()) groups.add(inspect436(group, 2));
                                publicationRow.put("actualTimerGroups436", groups);
                                Object lineage = read(actualSession, "lineage");
                                publicationRow.put("actualWriterBinding436", inspect436(read(read(lineage, "issuer"), "fieldSequenceBinding"), 2));

                                publicationRow.put("sequenceDetail436", inspect436(sequence, 2));
                                publicationRow.put("preparationDetail436", inspect436(preparation, 1));
                                Object nativeBefore = read(preparation, "nativeBefore");
                                publicationRow.put("nativeBeforePresent", nativeBefore != null);
                                publicationRow.put("coveragePresent", read(preparation, "coverage") != null);
                                publicationRow.put("catalogueGenerationPresent", read(preparation, "catalogueGeneration") != null);
                                for (String domainName : List.of("player", "carrier")) {
                                    Object domain = read(preparation, domainName);
                                    if (domain != null) for (String flag : List.of("originated", "retired", "loading")) publicationRow.put(domainName + "_" + flag, read(domain, flag));
                                }
                                publicationRow.put("timerSemanticsPresent", read(preparation, "timerSemantics") != null);
                            }
                        }
                        publications.add(publicationRow);
                    }
                    row.put("guardActionsObserved", actions.size()); row.put("publications", publications);
                    if (publications.isEmpty() || publications.stream().anyMatch(value ->
                        !Boolean.TRUE.equals(((Map<?, ?>)value).get("traceComplete"))
                        || Boolean.TRUE.equals(((Map<?, ?>)value).get("prepared_retired")))) {
                        throw new AssertionError("Repeated native Session did not complete");
                    }
                    Map<String, Object> proofState = new LinkedHashMap<>();
                    Class<?> registry = Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry");
                    proofState.put("registryInvalid", readStatic(registry, "invalid"));
                    proofState.put("itemStackFailure165", readStatic(registry, "nativeItemStackFailure165"));
                    proofState.put("itemStackTarget165Present", readStatic(registry, "nativeItemStackTarget165") != null);
                    Class<?> capability = Class.forName("net.farmingtales.ftboss.item.NativeCapabilityCertificate170");
                    proofState.put("capabilityFailures170", java.util.Arrays.asList((Object[])readStatic(capability, "FAILURES")));
                    List<Boolean> capabilityTargets = new ArrayList<>();
                    for (Object value : (Object[])readStatic(capability, "TARGETS")) capabilityTargets.add(value != null);
                    proofState.put("capabilityTargets170", capabilityTargets);
                    List<Boolean> componentTargets = new ArrayList<>();
                    for (Object value : (Object[])readStatic(registry, "nativeComponentTargets")) componentTargets.add(value != null);
                    proofState.put("componentTargets163", componentTargets);
                    row.put("coverageProofState", proofState);



                    row.put("arrows", arrows); row.put("attackReturned", true); }
                catch (Throwable failure) {
                    row.put("attackReturned", false); row.put("attackFailure", failure.toString());
                    java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); row.put("attackTrace", trace.toString());
                }
                row.put("targetHealthBefore", before); row.put("targetHealthAfter", target.getHealth());
                row.put("playerHealthAfter", restored.getHealth());
                Object afterImage = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                row.put("actualWeaponImageAfter", afterImage.toString());
                Object playerDomain = ((net.farmingtales.ftboss.item.OwnerFieldEntityAccess)restored).ftboss$ownerFieldDomain();
                row.put("playerDomainRetiredBeforeRead", read(playerDomain, "retired"));
                var encoded = new java.io.ByteArrayOutputStream();
                restored.getPersistentData().write(new java.io.DataOutputStream(encoded));
                CompoundTag playerCopy = net.minecraft.nbt.CompoundTag.TYPE.load(
                    new java.io.DataInputStream(new java.io.ByteArrayInputStream(encoded.toByteArray())),
                    net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                row.put("actualPlayerRangedTimersAfter", playerCopy.getCompound("FTBossRangedProcTimers").toString());
                row.put("playerDomainRetiredAfterRead", read(playerDomain, "retired"));
                if (!java.util.Objects.equals(row.get("playerDomainRetiredBeforeRead"), row.get("playerDomainRetiredAfterRead"))) {
                    throw new AssertionError("Read projection retired the live player domain");
                }

                row.put("damageEvents", new ArrayList<>(observations)); row.put("observedRoots", roots.size());
                Object melee = readStatic(net.farmingtales.ftboss.item.FoodWeaponBondHits.class, "MELEE");
                Object snapshot = ((Map<?, ?>)melee).get(restored.getUUID());
                row.put("signedMeleeSnapshotPresent", snapshot != null);
                if (snapshot != null) {
                    Object image = read(snapshot, "snapshot");
                    if (image instanceof CompoundTag signed) {
                        row.put("signedSnapshotValid", net.farmingtales.ftboss.item.FoodWeaponSourceLedger.valid(signed, restored));
                        row.put("serverSignaturePresent", signed.getString("ServerSignature").length() == 64);
                    }
                }
                target.discard();
                }
            }
            report.put("failures", cases.stream().anyMatch(value -> Boolean.FALSE.equals(((Map<?, ?>)value).get("attackReturned"))) ? 1 : 0);
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        } finally {
            try {
                var players = (List<ServerPlayer>)read(event.getServer().getPlayerList(), "players");
                var byId = (Map<UUID, ServerPlayer>)read(event.getServer().getPlayerList(), "playersByUUID");
                for (var scratch : scratchPlayers) {
                    players.remove(scratch);
                    byId.remove(scratch.getUUID(), scratch);
                }
                report.put("scratchPlayersRemoved", scratchPlayers.stream().noneMatch(players::contains));
            } catch (Exception cleanupFailure) {
                report.put("failures", 1);
                report.put("cleanupFailure", cleanupFailure.toString());
            }
        }
        report.put("cases", cases); report.put("applicationAuthorityWrittenByReflection", false);
        report.put("positiveSignedSessionAccepted", false); report.put("authenticatedMultiplayerAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }


    private static void runNativeRepair(ServerStartedEvent event, ServerPlayer player, ItemStack weapon, Map<String, Object> row) throws Exception {
        var server = event.getServer();
        player.moveTo(0.0, -59.0, 0.0, 0.0F, 0.0F);
        player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(server,
            new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND), player,
            net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(), false));
        ((List<ServerPlayer>)read(server.getPlayerList(), "players")).add(player);
        ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).put(player.getUUID(), player);
        row.put("scratchRegistrationNotAuthenticated", true);
        var statuePos = new net.minecraft.core.BlockPos(1, -59, 0);
        net.farmingtales.ftboss.block.CandyStatueBlock statue = null;
        for (var block : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
            if (block instanceof net.farmingtales.ftboss.block.CandyStatueBlock candidate && candidate.ownTint() == 2) { statue = candidate; break; }
        }
        if (statue == null) throw new AssertionError("Native repair statue missing");
        var half = net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF;
        server.overworld().setBlock(statuePos.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
        server.overworld().setBlock(statuePos, statue.defaultBlockState().setValue(half, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER), 2);
        server.overworld().setBlock(statuePos.above(), statue.defaultBlockState().setValue(half, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 2);
        var authority = (net.farmingtales.ftboss.transaction.FourWeaponTransactions)readStatic(net.farmingtales.ftboss.transaction.FourWeaponTransactionRuntime.class, "authority");
        if (authority == null) throw new AssertionError("Original runtime transaction authority absent");
        var boundary = new net.farmingtales.ftboss.transaction.ExistingTransactionBoundary(java.util.Set.of(), new net.farmingtales.ftboss.transaction.FoundationApprovedRules());
        row.put("statueLower", server.overworld().getBlockState(statuePos).toString());
        row.put("statueUpper", server.overworld().getBlockState(statuePos.above()).toString());
        if (boundary.statueIdentity(player, statuePos) == null) throw new AssertionError("Scratch statue pair invalid: " + row.get("statueLower") + "/" + row.get("statueUpper"));
        var binding = new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Binding(player.getUUID(), net.farmingtales.ftboss.item.FoodWeaponLifecycle.weaponId(weapon),
            net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.registryId(weapon), 0, 0, player.level().dimension().location().toString(), statuePos.asLong(), boundary.statueIdentity(player, statuePos));
        List<Object> transactions = new ArrayList<>();
        row.put("repairTransactions", transactions);
        row.put("nativeWearEligibleBeforeStart", net.farmingtales.ftboss.item.NativeFoodWeaponWear.eligible(weapon));
        row.put("repairDamageBeforeStart", weapon.getDamageValue());
        row.put("repairProfileBeforeStart", net.farmingtales.ftboss.item.FoodWeaponLifecycle.repairProfile(weapon));
        row.put("repairViewBeforeStart", net.farmingtales.ftboss.item.FoodWeaponRepair.view(weapon, 2).toString());
        row.put("repairImageBeforeStart", net.farmingtales.ftboss.item.NativeFoundationItemData.image(weapon).toString());
        row.put("repairStackClass", weapon.getClass().getName());
        row.put("repairItemClass", weapon.getItem().getClass().getName());
        row.put("repairCapabilityFields", (Object)weapon instanceof net.farmingtales.ftboss.item.OwnerRepairCapabilityFields);
        List<Object> nativeCapabilities = new ArrayList<>();
        row.put("nativeCapabilities", nativeCapabilities);
        for (var capability : net.neoforged.neoforge.capabilities.ItemCapability.getAll()) {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("capability", capability.toString());
            detail.put("physicalFields", (Object)capability instanceof net.farmingtales.ftboss.item.NativeItemCapabilityFields);
            Object providers = read(capability, "providers");
            Object values = providers instanceof Map<?, ?> providerMap ? providerMap.get(weapon.getItem()) : null;
            detail.put("providers", values instanceof List<?> list ? list.stream().map(value -> value.getClass().getName()).toList() : String.valueOf(values));
            nativeCapabilities.add(detail);
        }
        for (int action = 0; action < 5; action++) {
            var operation = action == 0 ? net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation.START_REPAIR
                : action == 4 ? net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation.CLAIM_REPAIR
                : net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation.SUBMIT_REPAIR;
            int index = action - 1;
            int expectedPayment = 0;
            int supplySlot = 2;
            if (action > 0 && action < 4) {
                expectedPayment = net.farmingtales.ftboss.util.FoodWeaponLifecycleRules.repairRequired(net.farmingtales.ftboss.item.FoodWeaponEvolution.evolutionLevel(weapon), index);
                net.minecraft.world.item.Item supply = index == 1 ? Items.IRON_INGOT : Items.LAPIS_LAZULI;
                if (index == 0) {
                    supply = null;
                    for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) if (net.farmingtales.ftboss.item.FoodWeaponEvolution.matchesStatueFood(new ItemStack(item), 2)) { supply = item; break; }
                }
                if (supply == null) throw new AssertionError("Native task food supply missing");
                player.getInventory().setItem(supplySlot, new ItemStack(supply, expectedPayment));
            }
            ((net.minecraft.world.level.storage.ServerLevelData)server.overworld().getLevelData()).setGameTime(server.overworld().getGameTime() + 5L);
            var opened = authority.open(player.getUUID(), new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Open(UUID.randomUUID(), binding));
            if (opened.code() != net.farmingtales.ftboss.transaction.FourWeaponTransactions.Code.OPENED || opened.snapshot() == null || !opened.snapshot().allowed().contains(operation)) throw new AssertionError("Original transaction open denied: " + opened);
            var snapshot = opened.snapshot();
            var request = new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Request(UUID.randomUUID(), snapshot.session(), snapshot.credential(), binding,
                snapshot.version(), operation, -1, action > 0 && action < 4 ? index : 0, "");
            var reply = authority.submit(player.getUUID(), request);
            Map<String, Object> transaction = new LinkedHashMap<>();
            transaction.put("operation", operation.name()); transaction.put("accepted", reply.accepted()); transaction.put("code", reply.code().name());
            transaction.put("expectedPayment", expectedPayment); transaction.put("remainingSupply", player.getInventory().getItem(supplySlot).getCount());
            transactions.add(transaction);
            if (!reply.accepted()) throw new AssertionError("Original native repair transaction failed: " + transaction);
            if (action > 0 && action < 4 && !player.getInventory().getItem(supplySlot).isEmpty()) throw new AssertionError("Actual native payment did not consume supplies");
            CompoundTag committedImage = net.farmingtales.ftboss.item.NativeFoundationItemData.image(weapon);
            CompoundTag supplyImage = net.farmingtales.ftboss.item.NativeFoundationItemData.image(player.getInventory().getItem(supplySlot));
            Object guardNode = ((net.farmingtales.ftboss.item.OwnerRevocationNodeAccess)(Object)weapon).ftboss$ownerRevocationNode();
            Object committedLease = guardNode == null ? null : read(guardNode, "lease");
            Object committedGeneration = guardNode == null ? null : read(guardNode, "generation");
            var replay = authority.submit(player.getUUID(), request);
            if (replay != reply) throw new AssertionError("Exact duplicate did not reuse the original terminal reply");
            var conflict = new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Request(request.request(), snapshot.session(), snapshot.credential(), binding,
                snapshot.version(), operation, -1, action > 0 && action < 4 ? index : 0, "conflicting duplicate input");
            var conflictReply = authority.submit(player.getUUID(), conflict);
            if (conflictReply.accepted() || conflictReply.code() != net.farmingtales.ftboss.transaction.FourWeaponTransactions.Code.REQUEST_CONFLICT)
                throw new AssertionError("Conflicting request identity was not rejected");
            var stale = new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Request(UUID.randomUUID(), snapshot.session(), snapshot.credential(), binding,
                snapshot.version(), operation, -1, action > 0 && action < 4 ? index : 0, "");
            var staleReply = authority.submit(player.getUUID(), stale);
            if (staleReply.accepted()) throw new AssertionError("Old version with a fresh request identity committed twice");
            if (!committedImage.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(weapon))
                || !supplyImage.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(player.getInventory().getItem(supplySlot))))
                throw new AssertionError("Repair replay changed actual weapon or material supply");
            Object afterNode = ((net.farmingtales.ftboss.item.OwnerRevocationNodeAccess)(Object)weapon).ftboss$ownerRevocationNode();
            if ((afterNode == null ? null : read(afterNode, "lease")) != committedLease
                || (afterNode == null ? null : read(afterNode, "generation")) != committedGeneration)
                throw new AssertionError("Repair replay reissued authority");
            transaction.put("exactDuplicateReusedTerminalReply", true);
            transaction.put("conflictCode", conflictReply.code().name());
            transaction.put("staleCode", staleReply.code().name());
            transaction.put("replayWeaponAndSuppliesUnchanged", true);
            transaction.put("replayLeaseAndGenerationIdentitiesUnchanged", true);
            authority.close(player.getUUID(), snapshot.session());
        }
        Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
        java.lang.reflect.Method leaseMethod = guard.getDeclaredMethod("lease", Object.class); leaseMethod.setAccessible(true);
        java.lang.reflect.Method generationMethod = guard.getDeclaredMethod("generation", Object.class); generationMethod.setAccessible(true);
        row.put("nonNullLease", leaseMethod.invoke(null, weapon) != null);
        row.put("repairGenerationPresent", generationMethod.invoke(null, weapon) != null);
        row.put("damageAfterRepair", weapon.getDamageValue());
        if (!Boolean.TRUE.equals(row.get("nonNullLease")) || !Boolean.TRUE.equals(row.get("repairGenerationPresent")) || weapon.getDamageValue() != 0) throw new AssertionError("Native claim did not publish genuine lease/generation");
    }

    private static Object verifySixWeaponScope436() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> retained = new ArrayList<>();
        for (String id : List.of("minecraft:iron_sword", "minecraft:bow", "minecraft:crossbow", "minecraft:iron_axe", "minecraft:trident", "irons_spellbooks:staff_of_the_nines")) {
            net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(net.minecraft.resources.ResourceLocation.parse(id)).orElseThrow(() -> new AssertionError("Retained family fixture missing: " + id));
            ItemStack stack = new ItemStack(item);
            if (!net.farmingtales.ftboss.item.WeaponSystemScope410.supports(stack)) throw new AssertionError("Retained family rejected: " + id);
            retained.add(id);
        }
        List<String> rejected = new ArrayList<>();
        for (net.minecraft.world.item.Item item : List.of(Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE, Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.SHEARS, Items.SHIELD, Items.DIAMOND_CHESTPLATE, Items.STICK)) {
            ItemStack stack = new ItemStack(item);
            CompoundTag data = new CompoundTag();
            CompoundTag evolution = new CompoundTag();
            evolution.putString("Profile", "butcher"); evolution.putInt("Level", 2);
            data.put("FTBossFoodEvolution", evolution);
            CompoundTag materials = new CompoundTag();
            for (net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part part : net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part.values()) materials.putString(part.name(), "diamond_alloy");
            data.put("FTBossWeaponMaterials", materials);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            if (net.farmingtales.ftboss.item.WeaponSystemScope410.supports(stack)
                || !net.farmingtales.ftboss.item.WeaponAttachmentPolicy.excluded(stack)
                || net.farmingtales.ftboss.item.FoodWeaponEvolution.isEvolutionCandidate(stack)
                || net.farmingtales.ftboss.item.FoodWeaponEvolution.ftboss$legacyOrdinary$isEvolutionCandidate(stack)
                || net.farmingtales.ftboss.item.WeaponDurabilityBuild.installedAlloys(stack) != 0
                || net.farmingtales.ftboss.item.WeaponMaterialBuild.materials(stack).stream().anyMatch(material -> material != net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material.ORIGINAL)) throw new AssertionError("Retired tool admitted with legacy data: " + item);
            rejected.add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString());
        }
        result.put("retainedSixFamilies", retained);
        result.put("rejectedLegacyDataItems", rejected);
        result.put("failures", 0);
        return result;
    }

    private static Object verifyRetiredFoodEnchantment436(ServerStartedEvent event) {
        ServerPlayer player = new ServerPlayer(event.getServer(), event.getServer().overworld(), new GameProfile(new UUID(436, 1), "retired_enchant"), ClientInformation.createDefault());
        player.giveExperienceLevels(20);
        ItemStack weapon = new ItemStack(Items.IRON_SWORD);
        CompoundTag data = new CompoundTag();
        CompoundTag evolution = new CompoundTag();
        evolution.putString("Profile", "butcher"); evolution.putInt("Level", 1);
        net.minecraft.nbt.ListTag blessings = new net.minecraft.nbt.ListTag();
        for (String id : List.of("blood_feast", "cleaver_might", "butcher_hide", "vintage_crit")) {
            CompoundTag blessing = new CompoundTag(); blessing.putString("Blessing", id); blessing.putInt("Level", 3); blessings.add(blessing);
        }
        evolution.put("FoodBlessings", blessings); data.put("FTBossFoodEvolution", evolution);
        weapon.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        ItemStack food = new ItemStack(Items.BEEF, 64);
        player.getInventory().setItem(0, weapon); player.getInventory().setItem(1, food);
        ItemStack beforeWeapon = weapon.copy(); ItemStack beforeFood = food.copy();
        int beforeLevel = player.experienceLevel; int beforeExperience = player.totalExperience;
        int rejected = 0;
        for (int tint = 0; tint <= 10; tint++) {
            for (int slot : new int[]{-1, 0, 1, 35, 36, 39, 40, 41}) {
                if (net.farmingtales.ftboss.item.FoodWeaponEvolution.applyFoodBlessingFromUi(player, tint, slot)
                    || net.farmingtales.ftboss.item.FoodWeaponEvolution.ftboss$legacyOrdinary$applyFoodBlessingFromUi(player, tint, slot)) throw new AssertionError("Retired food enchantment request accepted");
                rejected += 2;
            }
            if (net.farmingtales.ftboss.item.FoodWeaponEvolution.isBlessingCandidate(weapon, food, tint)
                || net.farmingtales.ftboss.item.FoodWeaponEvolution.ftboss$legacyOrdinary$isBlessingCandidate(weapon, food, tint)) throw new AssertionError("Retired enchantment ingredient accepted");
        }
        if (!net.farmingtales.ftboss.item.FoodWeaponEvolution.foodBlessingViews(weapon).isEmpty()
            || !net.farmingtales.ftboss.item.FoodWeaponEvolution.ftboss$legacyOrdinary$foodBlessingViews(weapon).isEmpty()) throw new AssertionError("Legacy enchantment still displayed");
        if (net.farmingtales.ftboss.item.ArmorFoodBlessings.apply(player, "retired", weapon, food, 0)
            || net.farmingtales.ftboss.item.ArmorFoodBlessings.eligible(new ItemStack(Items.DIAMOND_CHESTPLATE))) throw new AssertionError("Retired armor enchantment accepted");
        net.farmingtales.ftboss.item.FoodWeaponEvolution.foodBlessingDefense(null);
        net.farmingtales.ftboss.item.FoodWeaponEvolution.foodBlessingKillFeast(null);
        net.farmingtales.ftboss.item.FoodWeaponEvolution.grindstoneBlessing(null);
        if (!ItemStack.matches(weapon, beforeWeapon) || !ItemStack.matches(food, beforeFood)
            || player.experienceLevel != beforeLevel || player.totalExperience != beforeExperience) throw new AssertionError("Rejected enchantment consumed or changed inputs");
        return Map.of("failures", 0, "rejectedRequests", rejected, "legacyViewsEmpty", true, "armorRequestsRejected", true, "noFoodWeaponOrExperienceMutation", true, "scratchPlayerNotAuthenticated", true);
    }
}
