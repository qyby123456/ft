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
public final class LoadedWeapon429 {
    public LoadedWeapon429() {
        NeoForge.EVENT_BUS.addListener(LoadedWeapon429::started);
        NeoForge.EVENT_BUS.addListener(LoadedWeapon429::timerFixtureTick);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST, LoadedWeapon429::unknownJoinMutation);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, LoadedWeapon429::mitigation);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, LoadedWeapon429::health);
    }

    private static boolean injectUnknown;
    private static void unknownJoinMutation(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (injectUnknown && event.getEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow
            && arrow.getOwner() instanceof ServerPlayer) arrow.getPersistentData().putInt("ft333_unknown_join_mutation", 1);
    }
    private static final List<Object> observations = new ArrayList<>();
    private static final List<Object> roots = new ArrayList<>();
    private static final List<Object> actions = new ArrayList<>();


    private static Object inspect429(Object value, int depth) throws Exception {
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
            if (depth > 0) for (int index = 0; index < Math.min(length, 64); index++) elements.add(inspect429(java.lang.reflect.Array.get(value, index), depth - 1));
            return Map.of("length", length, "elements", elements);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", value.getClass().getName());
        if (depth > 0 && value.getClass().getName().startsWith("net.farmingtales.ftboss.item.")) {
            for (Field field : value.getClass().getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                result.put(field.getName(), inspect429(field.get(value), depth - 1));
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
        try {
            report.put("sixWeaponScope", verifySixWeaponScope429());
            report.put("retiredFoodEnchantment", verifyRetiredFoodEnchantment429(event));
            report.put("socketRecipeParity429", RecipeSocketParity429.run(event));
            report.put("componentStackParity429", StackParity429.run(event));
            PlayerDataStorage storage = (PlayerDataStorage)read(event.getServer().getPlayerList(), "playerIo");
            for (int sample = 0; sample < 2; sample++) {
                injectUnknown = sample == 1;
                GameProfile profile = new GameProfile(new UUID(334, sample + 1), "ft317_" + sample);
                ServerPlayer original = new ServerPlayer(event.getServer(), event.getServer().overworld(), profile, ClientInformation.createDefault());
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
                storage.save(original);
                ServerPlayer restored = new ServerPlayer(event.getServer(), event.getServer().overworld(), profile, ClientInformation.createDefault());
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
                row.put("rangedRootPresentBefore429", restored.getPersistentData().contains("FTBossRangedProcTimers", 10));

                float before = target.getHealth();
                try {
                    row.put("fingerprintGraphChecks", net.farmingtales.ftboss.item.FingerprintChecks339.run());
                    if (net.farmingtales.ftboss.item.NativeNbtAliasPolicy.trustedRawFieldRead()) throw new AssertionError("Direct raw authority admitted");
                    CompoundTag rawProbe = new CompoundTag();
                    ((net.farmingtales.ftboss.item.NativeExpectedFieldViews.Compound)(Object)rawProbe).ftboss$rawCompoundFields();
                    boolean rawRejected = ((net.farmingtales.ftboss.item.NativeNbtMutationFields)(Object)rawProbe).ftboss$nativeNbtRawEscaped();
                    if (!rawRejected) throw new AssertionError("Unknown raw getter did not revoke");
                    row.put("unknownRawGetterRevoked", rawRejected);

                    CompoundTag baselineDecodeInput = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                    net.farmingtales.ftboss.item.NativeNbtGraphObservation baselineDecode = net.farmingtales.ftboss.item.NativeNbtGraphObservation.capture(baselineDecodeInput);
                    ItemStack baselineDecoded = ItemStack.parseOptional(restored.registryAccess(), baselineDecodeInput);
                    row.put("directCodecInputUnchanged", baselineDecode.matches(baselineDecodeInput));
                    CompoundTag isolatedDecodeInput = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                    net.farmingtales.ftboss.item.NativeNbtGraphObservation isolatedDecode = net.farmingtales.ftboss.item.NativeNbtGraphObservation.capture(isolatedDecodeInput);
                    ItemStack isolatedDecoded = net.farmingtales.ftboss.item.NativeRepairStackImage.decode(restored.registryAccess(), isolatedDecodeInput);
                    boolean inputPreserved = isolatedDecode.matches(isolatedDecodeInput);
                    boolean decodeEqual = net.farmingtales.ftboss.item.NativeFoundationItemData.image(baselineDecoded).equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(isolatedDecoded));
                    row.put("isolatedDecodeInputUnchanged", inputPreserved); row.put("decodedItemImageEqual", decodeEqual);
                    if (!inputPreserved || !decodeEqual) throw new AssertionError("Isolated decode changed source or item");

                    if (net.farmingtales.ftboss.item.NativeNbtAliasPolicy.trustedFullRepairUuidRead()) throw new AssertionError("Direct UUID authority admitted");
                    net.minecraft.nbt.IntArrayTag directArray = new net.minecraft.nbt.IntArrayTag(new int[]{1, 2, 3, 4});
                    net.farmingtales.ftboss.item.NativeNbtGraphObservation arrayBefore = net.farmingtales.ftboss.item.NativeNbtGraphObservation.capture(directArray);
                    directArray.getAsIntArray();
                    boolean directArrayRevoked = !arrayBefore.matches(directArray);
                    row.put("directArrayAliasRevoked", directArrayRevoked);
                    if (!directArrayRevoked) throw new AssertionError("Direct array alias admitted");
                    String fingerprintBefore = net.farmingtales.ftboss.item.FingerprintBefore339.create(actual);
                    String fingerprintAfter = net.farmingtales.ftboss.item.FoodWeaponMaterialFingerprint.create(actual);
                    row.put("fingerprintUnchanged", !fingerprintAfter.isEmpty() && fingerprintAfter.equals(fingerprintBefore));
                    if (!fingerprintAfter.equals(fingerprintBefore) || fingerprintAfter.isEmpty()) throw new AssertionError("Material fingerprint changed");
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
                    for (net.minecraft.world.entity.Entity entity : event.getServer().overworld().getAllEntities()) {
                        if (!(entity instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow) || arrow.getOwner() != restored) continue;
                        Map<String, Object> flight = new LinkedHashMap<>();
                        flight.put("launchPosition429", arrow.position().toString());
                        flight.put("launchVelocity429", arrow.getDeltaMovement().toString());
                        flight.put("targetBounds429", target.getBoundingBox().toString());
                        flight.put("ownerIdentity", arrow.getOwner() == restored);
                        flight.put("paidSnapshotPresent", arrow.getPersistentData().contains("FTBossWeaponMaterials", 10));
                        CompoundTag image = arrow.getPersistentData().getCompound("FTBossWeaponMaterials");
                        flight.put("paidSnapshotValid", net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(image, restored));
                        flight.put("damagePurpose", image.getString("DamagePurpose"));
                        flight.put("paidCarrier", arrow instanceof net.farmingtales.ftboss.item.NativePaidWeaponCarrier carrier && carrier.ftboss$originalOwnerPaidWeapon() != null);
                        if (arrow instanceof net.farmingtales.ftboss.item.NativePaidWeaponCarrier carrier) {
                            Object paid = carrier.ftboss$originalOwnerPaidWeapon();
                            if (paid != null) flight.put("profileFieldInitialized", read(read(paid, "receipt"), "profileFieldInitialized"));
                            if (paid != null) flight.put("imageFieldInitialized", read(read(paid, "receipt"), "imageFieldInitialized"));
                            if (paid != null) flight.put("manorOwnerFieldInitialized", read(read(paid, "receipt"), "manorOwnerFieldInitialized"));
                        }
                        for (int tick = 0; tick < 12 && !arrow.isRemoved() && target.getHealth() == before; tick++) arrow.tick();
                        flight.put("finalPosition429", arrow.position().toString());
                        flight.put("targetHealthAfterFlight429", target.getHealth());
                        flight.put("nativeImpactObserved429", !observations.isEmpty());
                        flight.put("removedAfterFlight", arrow.isRemoved()); arrows.add(flight); arrow.discard();
                    }
                    if (sample == 0 && (observations.isEmpty() || target.getHealth() >= before)) {
                        row.put("projectileFlightDiagnostic429", arrows);
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
                                publicationRow.put("sessionDetail429", inspect429(read(preparation, "session"), 3));
                                Object actualSession = read(preparation, "session");
                                Object actualBook = read(actualSession, "timerSemantics");
                                if (actualBook != null) {
                                    Object plannedRoot = read(actualBook, "rangedRoot");
                                    Object plannedBuffer = read(actualBook, "rangedBuffer");
                                    Map<String, Object> acquisition = new LinkedHashMap<>();
                                    acquisition.put("plannedRootIsActualPersistentRoot", plannedRoot == restored.getPersistentData());
                                    acquisition.put("plannedBufferWasNull", plannedBuffer == null);
                                    acquisition.put("plannedBufferIsCurrentStoredBuffer", plannedBuffer == restored.getPersistentData().get("FTBossRangedProcTimers"));
                                    acquisition.put("storedBufferPresentAfter", restored.getPersistentData().contains("FTBossRangedProcTimers", 10));
                                    acquisition.put("authorityFieldsWritten", false);
                                    publicationRow.put("rangedAcquisitionIdentity429", acquisition);
                                }

                                List<Object> groups = new ArrayList<>();
                                if (actualBook != null) for (Object group : ((Map<?, ?>)read(actualBook, "groups")).values()) groups.add(inspect429(group, 2));
                                publicationRow.put("actualTimerGroups429", groups);
                                Object lineage = read(actualSession, "lineage");
                                publicationRow.put("actualWriterBinding429", inspect429(read(read(lineage, "issuer"), "fieldSequenceBinding"), 2));

                                Class<?> timerAuthority = Class.forName("net.farmingtales.ftboss.item.OwnerTimerSemanticAuthority");
                                var caller = timerAuthority.getDeclaredMethod("caller", Class.class, String.class);
                                caller.setAccessible(true);
                                Map<String, Object> callerNegatives = new LinkedHashMap<>();
                                callerNegatives.put("reflectiveExactNameRejected", !Boolean.TRUE.equals(caller.invoke(null, net.farmingtales.ftboss.item.FoodWeaponHitPlan.class, "ftboss$ownerSemanticLeaf")));
                                callerNegatives.put("reflectiveWrongNameRejected", !Boolean.TRUE.equals(caller.invoke(null, net.farmingtales.ftboss.item.FoodWeaponHitPlan.class, "unknown")));
                                publicationRow.put("callerNegativeChecks429", callerNegatives);
                                Class<?> consumer = Class.forName("net.farmingtales.ftboss.item.FoodWeaponConsumerSequence");
                                var consumerCaller = consumer.getDeclaredMethod("finalCaller", String.class, String.class, String.class);
                                consumerCaller.setAccessible(true);
                                String descriptor = "(FLnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$OffenseReceipt;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingCleanup;)Z";
                                Map<String, Object> consumerNegatives = new LinkedHashMap<>();
                                consumerNegatives.put("reflectionWithoutLockRejected", !Boolean.TRUE.equals(consumerCaller.invoke(null, "AttributeProducer", "publish", descriptor)));
                                synchronized (net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class) {
                                    consumerNegatives.put("reflectionWithLockRejected", !Boolean.TRUE.equals(consumerCaller.invoke(null, "AttributeProducer", "publish", descriptor)));
                                }
                                publicationRow.put("consumerCallerNegatives429", consumerNegatives);


                                publicationRow.put("sequenceDetail429", inspect429(sequence, 2));
                                publicationRow.put("preparationDetail429", inspect429(preparation, 1));
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
                    if (sample == 0) ItemStackProof361.run(row);
                    if (sample == 0) Coverage429.run(row);

                    row.put("arrows", arrows); row.put("attackReturned", true); }
                catch (Throwable failure) {
                    row.put("attackReturned", false); row.put("attackFailure", failure.toString());
                    java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); row.put("attackTrace", trace.toString());
                }
                row.put("targetHealthBefore", before); row.put("targetHealthAfter", target.getHealth());
                row.put("playerHealthAfter", restored.getHealth());
                Object afterImage = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                row.put("actualWeaponImageAfter", afterImage.toString());
                row.put("actualPlayerRangedTimersAfter", restored.getPersistentData().getCompound("FTBossRangedProcTimers").toString());

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
            report.put("failures", cases.stream().anyMatch(value -> Boolean.FALSE.equals(((Map<?, ?>)value).get("attackReturned"))) ? 1 : 0);
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        }
        report.put("cases", cases); report.put("applicationAuthorityWrittenByReflection", false);
        report.put("positiveSignedSessionAccepted", false); report.put("authenticatedMultiplayerAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }

    private static Object verifySixWeaponScope429() {
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

    private static Object verifyRetiredFoodEnchantment429(ServerStartedEvent event) {
        ServerPlayer player = new ServerPlayer(event.getServer(), event.getServer().overworld(), new GameProfile(new UUID(429, 1), "retired_enchant"), ClientInformation.createDefault());
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
