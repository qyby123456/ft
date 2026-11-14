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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
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
            }
            row.put("actualDamageSourceClass", event.getSource().getClass().getName());
            Object captureStorage = readStatic(net.farmingtales.ftboss.item.FoodWeaponFinalDamage.class, "EVENT_CAPTURES");
            java.util.Deque<?> currentCaptures = (java.util.Deque<?>)((ThreadLocal<?>)captureStorage).get();
            row.put("originalEventCaptureCount", currentCaptures.size());
            Object currentCapture = currentCaptures.peekLast();
            if (currentCapture != null) {
                row.put("originalEventCaptureFailed", read(currentCapture, "failed"));
                row.put("originalEventCaptureClosed", read(currentCapture, "closed"));
                row.put("originalEventCapturedProducerCount", ((java.util.List<?>)read(currentCapture, "producers")).size());
                Object binding = read(currentCapture, "originalBinding");
                Object currentRoot = read(binding, "root");
                row.put("originalEventRootSite", String.valueOf(read(currentRoot, "site")));
                row.put("originalEventRootRetired", read(currentRoot, "retired"));
                row.put("originalEventRootClosed", read(currentRoot, "closed"));
                row.put("originalEventBindingClosed", read(binding, "closed"));
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
            report.put("retiredFoodRequests", verifyRetiredFoodEnchantment436(event));
            report.put("retiredFoodCatalogProtocolAndEffects", verifyRetiredClosure(event));
            var server = event.getServer();
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var leaseMethod = guard.getDeclaredMethod("lease", Object.class); leaseMethod.setAccessible(true);
            var generationMethod = guard.getDeclaredMethod("generation", Object.class); generationMethod.setAccessible(true);
            for (int sample : new int[]{0, 1, 2, 3, 5, 4}) {
                UUID owner = new UUID(620, sample + 1);
                ServerPlayer player = new ServerPlayer(server, server.overworld(), new GameProfile(owner, "ft_six_" + sample), ClientInformation.createDefault());
                scratches.add(player);
                if (storage.load(player).isEmpty()) throw new AssertionError("Original paid native melee weapon reload did not run");
                var actual = player.getInventory().getItem(0);
                String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(actual.getItem()).toString();
                Map<String, Object> row = new LinkedHashMap<>(); row.put("id", id); cases.add(row);
                Object actualLease = leaseMethod.invoke(null, actual);
                Object generation = generationMethod.invoke(null, actual);
                if (actualLease == null || generation == null || ((Number)read(actualLease, "generation")).longValue() != 3L)
                    throw new AssertionError("Melee test requires genuine paid generation-three original reload, not empty authority");
                row.put("genuineNonNullLeaseAndGenerationBeforeAttack", true);
                double dispatchedMaterial = net.farmingtales.ftboss.item.WeaponMaterialBuild.build(actual).damage();
                double nativeMaterial = net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.combat(actual).damage();
                row.put("dispatchedMaterialFactor", dispatchedMaterial);
                row.put("nativeFoundationMaterialFactor", nativeMaterial);
                if (net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(actual)
                    && Double.doubleToLongBits(dispatchedMaterial) != Double.doubleToLongBits(nativeMaterial))
                    throw new AssertionError("Damage accounting ignored the authoritative native foundation for " + id);

                row.put("nativeFoundationApplies", net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(actual));
                row.put("nativeFoundationBlocksUse", net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.blocksUse(actual));
                row.put("nativeFoundationStatus", String.valueOf(net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.status(actual)));
                row.put("oldSwordRequiredMissing", net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(actual));
                row.put("lifecycleBrokenBeforeUse", net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual));
                if (net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual))
                    throw new AssertionError("Actual paid native foundation was falsely broken for " + id);
                if (sample != 4) continue;
                CompoundTag missingImage = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                missingImage.getCompound("components").getCompound("minecraft:custom_data").getCompound("FTBossFourWeaponFoundation").put("Cells", new net.minecraft.nbt.ListTag());
                var missingWeapon = net.minecraft.world.item.ItemStack.parseOptional(server.registryAccess(), missingImage);
                boolean missingDenied = !missingWeapon.isEmpty() && net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.blocksUse(missingWeapon)
                    && net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(missingWeapon);
                row.put("missingNativeCellsDenied", missingDenied);
                CompoundTag invalidImage = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                invalidImage.getCompound("components").getCompound("minecraft:custom_data").getCompound("FTBossFourWeaponFoundation").putInt("Schema", 0);
                var invalidWeapon = net.minecraft.world.item.ItemStack.parseOptional(server.registryAccess(), invalidImage);
                boolean invalidDenied = !invalidWeapon.isEmpty() && net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.blocksUse(invalidWeapon)
                    && net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(invalidWeapon);
                row.put("malformedNativeFoundationDenied", invalidDenied);
                row.put("sameGenuineAuthorityAfterGateControls", leaseMethod.invoke(null, actual) == actualLease
                    && generationMethod.invoke(null, actual) == generation && !Boolean.TRUE.equals(read(generation, "retired")));
                if (!missingDenied || !invalidDenied || !Boolean.TRUE.equals(row.get("sameGenuineAuthorityAfterGateControls")))
                    throw new AssertionError("Native foundation gate or negative control changed authority");

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
                actions.clear(); damageEvents.clear();
                float healthBefore = target.getHealth();
                int wearBefore = actual.getDamageValue();
                row.put("actualItemClass", actual.getItem().getClass().getName());
                row.put("selectedSlot", player.getInventory().selected);
                row.put("actualIsMainHandBeforeUse", player.getMainHandItem() == actual);
                row.put("actualCountBeforeUse", actual.getCount());
                row.put("actualDamageBeforeUse", actual.getDamageValue());
                row.put("playerAliveBeforeUse", player.isAlive());
                Class<?> tridentCoverageType = Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry");
                var tridentCertificate = tridentCoverageType.getDeclaredMethod("nativeTridentCertified"); tridentCertificate.setAccessible(true);
                row.put("actualTridentWholeInstallationCertified", tridentCertificate.invoke(null));
                row.put("brokenBeforeOriginalUse", net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual));
                row.put("durabilityDepletedBeforeUse", net.farmingtales.ftboss.item.FoodWeaponLifecycle.durabilityDepleted(actual));
                row.put("cavityAppliesBeforeUse", net.farmingtales.ftboss.item.SwordCavityBuild.applies(actual));
                row.put("cavityMissingBeforeUse", net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(actual));
                row.put("functionProfileBeforeUse", String.valueOf(net.farmingtales.ftboss.api.SwordCavityExtensions.functionProfile(actual)));
                row.put("functionStatusBeforeUse", String.valueOf(net.farmingtales.ftboss.item.SwordCavityBuild.functionStatus(actual)));
                row.put("nativePartsBeforeUse", String.valueOf(net.farmingtales.ftboss.item.SwordNativeMaterials.parts(actual)));
                row.put("cavityCellsBeforeUse", String.valueOf(net.farmingtales.ftboss.item.SwordCavityBuild.cells(actual)));
                row.put("generationRetiredAfterStructureReads", read(generation, "retired"));

                var useResult = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                row.put("actualOriginalUseResult", useResult.getResult().toString());
                row.put("actualUseStarted", player.isUsingItem());
                row.put("generationRetiredAfterUse", read(generation, "retired"));
                row.put("brokenAfterOriginalUse", net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual));
                Class<?> entryRegistry = Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry");
                row.put("coverageRegistryInvalidAfterOriginalUse", readStatic(entryRegistry, "invalid"));
                var certificate = entryRegistry.getDeclaredMethod("nativeItemStackCertified165"); certificate.setAccessible(true);
                row.put("itemStackCertificateAfterOriginalUse", certificate.invoke(null));
                var sourceNodes = entryRegistry.getDeclaredMethod("nativeItemStackSourceNodes165"); sourceNodes.setAccessible(true);
                Object nodes = sourceNodes.invoke(null);
                row.put("actualItemStackProofSourcesPresent", nodes != null);
                try {
                    Class<?> proof = Class.forName("net.farmingtales.ftboss.item.NativeItemStackInstallationProof165");
                    var verify = java.util.Arrays.stream(proof.getDeclaredMethods()).filter(method -> method.getName().equals("verify") && method.getParameterCount() == 3).findFirst().orElseThrow();
                    verify.setAccessible(true);
                    verify.invoke(null, readStatic(entryRegistry, "nativeItemStackTarget165"), nodes, readStatic(entryRegistry, "nativeItemStackPristine165"));
                    row.put("actualItemStackWholeParentProofAfterOriginalUse", true);
                } catch (java.lang.reflect.InvocationTargetException failure) {
                    row.put("actualItemStackWholeParentProofFailure", failure.getCause().toString());
                }

                if (!useResult.getResult().consumesAction() || !player.isUsingItem() || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Original paid Trident ItemStack.use did not start with live authority");
                CompoundTag beforeEarlyRelease = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                Object beforeEarlyInput = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                actual.releaseUsing(server.overworld(), player, actual.getUseDuration(player) - 1);
                boolean earlyUnchanged = beforeEarlyRelease.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    && beforeEarlyInput == ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput()
                    && leaseMethod.invoke(null, actual) == actualLease && generationMethod.invoke(null, actual) == generation
                    && !Boolean.TRUE.equals(read(generation, "retired")) && player.getMainHandItem() == actual;
                row.put("actualEarlyReleasePreservesExactInputIdentity", earlyUnchanged);
                Object afterEarlyInput = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                boolean earlyImageSame = beforeEarlyRelease.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual));
                boolean earlyLeaseSame = leaseMethod.invoke(null, actual) == actualLease;
                boolean earlyGenerationSame = generationMethod.invoke(null, actual) == generation;
                boolean earlyGenerationLive = !Boolean.TRUE.equals(read(generation, "retired"));
                boolean earlyHandSame = player.getMainHandItem() == actual;
                boolean earlyCurrentInputLive = afterEarlyInput != null && !Boolean.TRUE.equals(read(afterEarlyInput, "retired"));
                row.put("earlyImageSame", earlyImageSame);
                row.put("earlyLeaseSame", earlyLeaseSame);
                row.put("earlyGenerationSame", earlyGenerationSame);
                row.put("earlyGenerationLive", earlyGenerationLive);
                row.put("earlyHandSame", earlyHandSame);
                row.put("earlyInputSame", beforeEarlyInput == afterEarlyInput);
                row.put("earlyPreviousInputRetired", beforeEarlyInput == null ? null : read(beforeEarlyInput, "retired"));
                row.put("earlyCurrentInputLive", earlyCurrentInputLive);
                boolean earlyPaidStateLive = earlyImageSame && earlyLeaseSame && earlyGenerationSame && earlyGenerationLive && earlyHandSame && earlyCurrentInputLive;
                row.put("earlyPaidStateLiveWithObservedSemanticTransition", earlyPaidStateLive);
                if (!earlyPaidStateLive) throw new AssertionError("Original early Trident release lost paid state");
                player.stopUsingItem();
                var secondUse = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                if (!secondUse.getResult().consumesAction() || !player.isUsingItem() || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Original paid Trident use after early release did not restart");
                actual.releaseUsing(server.overworld(), player, actual.getUseDuration(player) - 20);
                row.put("actualOriginalReleaseReturned", true);
                row.put("generationRetiredAfterRelease", read(generation, "retired"));
                row.put("originalMainHandEmptyAfterThrow", player.getMainHandItem().isEmpty());
                player.stopUsingItem();
                List<Map<String, Object>> flights = new ArrayList<>(); row.put("actualProjectileFlights", flights);
                List<net.minecraft.world.entity.projectile.AbstractArrow> arrows = new ArrayList<>();
                for (var entity : server.overworld().getAllEntities()) {
                    if (entity instanceof net.minecraft.world.entity.projectile.ThrownTrident arrow && arrow.getOwner() == player) arrows.add(arrow);
                }
                for (var arrow : arrows) {
                    Map<String, Object> flight = new LinkedHashMap<>(); flights.add(flight);
                    CompoundTag image = arrow.getPersistentData().getCompound("FTBossWeaponMaterials");
                    flight.put("snapshotPresent", !image.isEmpty());
                    flight.put("snapshotValid", net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(image, player));
                    flight.put("repairEpochPresent", image.hasUUID("OwnerRepairAuthorityEpoch"));
                    flight.put("signaturePresent", image.getString("ServerSignature").length() == 64);
                    flight.put("purpose", image.getString("DamagePurpose"));
                    flight.put("nativeEntry", image.getString("NativeEntry"));
                    flight.put("paidCarrierPresent", arrow instanceof net.farmingtales.ftboss.item.NativePaidWeaponCarrier carrier && carrier.ftboss$originalOwnerPaidWeapon() != null);
                    net.minecraft.world.item.ItemStack copiedPickup = net.farmingtales.ftboss.integ.FoodWeaponTridentCarrierReader.readOriginalItem((net.minecraft.world.entity.projectile.ThrownTrident)arrow);
                    boolean pickupUnprivileged = copiedPickup != actual && copiedPickup.getCount() == 1
                        && leaseMethod.invoke(null, copiedPickup) == null && generationMethod.invoke(null, copiedPickup) == null;
                    flight.put("copiedPickupHasNoPrivateAuthority", pickupUnprivileged);
                    if (!pickupUnprivileged) throw new AssertionError("Copied pickup inherited private repair authority");
                    for (int ticks = 0; ticks < 12 && !arrow.isRemoved() && target.getHealth() == healthBefore; ticks++) arrow.tick();
                    flight.put("targetHealthAfterFlight", target.getHealth());
                }
                row.put("originalTridentUseAndReleaseReturned", true);
                row.put("healthBefore", healthBefore); row.put("healthAfter", target.getHealth());
                row.put("wearBefore", wearBefore); row.put("wearAfter", actual.getDamageValue());
                row.put("damageEvents", new ArrayList<>(damageEvents));
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
                        Class<?> registry = Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry");
                        for (java.lang.reflect.Field field : registry.getDeclaredFields()) {
                            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()) || !field.getName().toLowerCase(java.util.Locale.ROOT).contains("failure")) continue;
                            field.setAccessible(true);
                            Object failure = field.get(null);
                            publicationRow.put("registry_" + field.getName(), failure == null ? null : failure.toString());
                        }

                        for (String flag : List.of("traceRevoked", "coverageInstalled", "next")) publicationRow.put("session_" + flag, read(session, flag));
                        Object revocationSite = read(session, "firstRevocationSite434");
                        if (revocationSite instanceof Throwable failure) {
                            java.io.StringWriter diagnostic = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(diagnostic));
                            publicationRow.put("firstRevocationSite434", diagnostic.toString());
                        }
                        Object failedStep = read(session, "firstFailedStep");
                        if (failedStep != null) {
                            Map<String, Object> failed = new LinkedHashMap<>(); publicationRow.put("firstFailedStep", failed);
                            for (String name : List.of("observedPrefixes", "phase", "started", "entered", "copyVerified", "directFieldsVerified", "writeObserved", "originalReturned", "closed", "failedPhase", "nativeTimerPrefixesPrepared", "nativeTimerComponentAssigned")) failed.put(name, read(failedStep, name));
                            Object operation = read(failedStep, "expected");
                            failed.put("expectedKind", String.valueOf(read(operation, "kind")));
                            for (String name : List.of("leaf", "originalReceiver", "originalValue", "originalArgument", "originalReturn")) {
                                Object value = read(failedStep, name); failed.put(name + "Class", value == null ? null : value.getClass().getName());
                            }
                        }
                        Object observationFailure = read(session, "firstObservationFailure");
                        if (observationFailure instanceof Throwable failure) {
                            java.io.StringWriter diagnostic = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(diagnostic));
                            publicationRow.put("firstObservationFailure", diagnostic.toString());
                        }

                    }
                }
                row.put("sameLeaseAndGenerationAfterAttack", leaseMethod.invoke(null, actual) == actualLease && generationMethod.invoke(null, actual) == generation);
                row.put("generationRetiredAfterAttack", read(generation, "retired"));
                boolean closedImpactDenied = !net.farmingtales.ftboss.item.FoodWeaponSourceLedger.originalHandContinuousReadOnly(capturedSigned, player);
                var foreignCustody = net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class.getDeclaredMethod("nativeTridentImpactWeapon", CompoundTag.class, ServerPlayer.class);
                foreignCustody.setAccessible(true);
                boolean foreignCustodyDenied = ((net.minecraft.world.item.ItemStack)foreignCustody.invoke(null, capturedSigned, player)).isEmpty();
                row.put("closedRealSignedImpactCustodyDenied", closedImpactDenied);
                row.put("foreignRealSignedCustodyHelperDenied", foreignCustodyDenied);
                if (!closedImpactDenied || !foreignCustodyDenied) throw new AssertionError("Closed or foreign impact custody accepted");
                if (publications.isEmpty() || publications.stream().anyMatch(value ->
                    !Boolean.TRUE.equals(value.get("publicationPresent")) || !Boolean.TRUE.equals(value.get("preparedCommitted"))
                    || !Boolean.TRUE.equals(value.get("originalSequencePresent")) || !Boolean.TRUE.equals(value.get("sessionPresent"))
                    || !Boolean.FALSE.equals(value.get("prepared_unsupported")) || !Boolean.TRUE.equals(value.get("prepared_sealed"))
                    || !Boolean.TRUE.equals(value.get("prepared_bound")) || !Boolean.FALSE.equals(value.get("prepared_retired"))
                    || !Boolean.TRUE.equals(value.get("session_coverageInstalled")) || !Boolean.FALSE.equals(value.get("session_traceRevoked"))
                    || !Boolean.TRUE.equals(value.get("traceComplete")) || value.get("firstObservationFailure") != null))
                    throw new AssertionError("Original signed Session is not completely covered and live");
                if (flights.size() != 1 || flights.stream().anyMatch(value -> !Boolean.TRUE.equals(value.get("snapshotValid"))
                    || !Boolean.TRUE.equals(value.get("repairEpochPresent")) || !Boolean.TRUE.equals(value.get("signaturePresent"))
                    || !Boolean.TRUE.equals(value.get("paidCarrierPresent")) || !"TRIDENT".equals(value.get("nativeEntry"))))
                    throw new AssertionError("Actual original Trident did not publish one genuinely paid signed native carrier");
                target.discard();
                if (target.getHealth() >= healthBefore || damageEvents.isEmpty() || publications.isEmpty()
                    || publications.stream().anyMatch(value -> !Boolean.TRUE.equals(value.get("traceComplete")) || Boolean.TRUE.equals(value.get("prepared_retired"))))
                    throw new AssertionError("Actual paid original Trident arrow did not damage target and complete live signed Session for " + id);
                if (damageEvents.stream().noneMatch(value -> Boolean.TRUE.equals(value.get("resolvedSnapshotValid")) && Boolean.TRUE.equals(value.get("repairEpochPresent"))
                        && Boolean.TRUE.equals(value.get("serverSignaturePresent")))
                    || !Boolean.TRUE.equals(row.get("sameLeaseAndGenerationAfterAttack")) || Boolean.TRUE.equals(row.get("generationRetiredAfterAttack")))
                    throw new AssertionError("Actual original Trident arrow lost signed non-null repair authority for " + id);
                var physical = (net.minecraft.world.entity.projectile.ThrownTrident)arrows.get(0);
                CompoundTag originalBeforePickup = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                Map<String, Object> pickupRow = new LinkedHashMap<>(); row.put("actualPhysicalPickup", pickupRow);
                net.minecraft.world.item.ItemStack picked = net.minecraft.world.item.ItemStack.EMPTY;
                int physicalTicks = 0;
                for (; physicalTicks < 160 && !physical.isRemoved(); physicalTicks++) {
                    physical.tick();
                    player.moveTo(physical.getX(), physical.getY(), physical.getZ(), 0.0F, 0.0F);
                    physical.playerTouch(player);
                }
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    var candidate = player.getInventory().getItem(slot);
                    if (!candidate.isEmpty() && candidate.getItem() == actual.getItem()) {
                        if (!picked.isEmpty()) throw new AssertionError("Original pickup duplicated the thrown weapon");
                        picked = candidate;
                    }
                }
                pickupRow.put("originalPlayerTouchPhysicsTicks", physicalTicks);
                pickupRow.put("actualProjectileRemovedByPickup", physical.isRemoved());
                pickupRow.put("pickedPresent", !picked.isEmpty());
                pickupRow.put("pickedCount", picked.getCount());
                pickupRow.put("pickedWear", picked.getDamageValue());
                pickupRow.put("sameOriginalObject", picked == actual);
                pickupRow.put("sameOriginalWholeImage", originalBeforePickup.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(picked)));
                pickupRow.put("pickedNonNullLease", leaseMethod.invoke(null, picked) != null);
                pickupRow.put("pickedNonNullGeneration", generationMethod.invoke(null, picked) != null);
                pickupRow.put("sameGenuineLease", leaseMethod.invoke(null, picked) == actualLease);
                pickupRow.put("sameGenuineGeneration", generationMethod.invoke(null, picked) == generation);
                pickupRow.put("originalGenerationRetired", read(generation, "retired"));
                if (!physical.isRemoved() || picked.isEmpty() || picked.getCount() != 1 || picked.getDamageValue() != wearBefore + 1)
                    throw new AssertionError("Original physical Trident pickup did not return exactly one worn weapon");
                if (picked != actual || !Boolean.TRUE.equals(pickupRow.get("sameOriginalWholeImage"))
                    || !Boolean.TRUE.equals(pickupRow.get("sameGenuineLease")) || !Boolean.TRUE.equals(pickupRow.get("sameGenuineGeneration"))
                    || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Original physical Trident pickup lost genuine paid original custody or semantic state");
                storage.save(player);
                pickupRow.put("originalPostPickupSaveReturned", true);
                row.put("originalThrowPickupAndPersistenceAccepted", false);
                for (var projectile : arrows) projectile.discard();
                target.discard();
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(player);
                ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).remove(owner, player);
            }
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
        report.put("allSixFamilySignedCombatAccepted", false);
        report.put("tridentEntry", "Actual paid ItemStack.use -> original Trident releaseUsing/constructor/add/remove inventory -> actual ThrownTrident.tick impact; pickup and persistence still unaccepted");
        report.put("foundationGateScopeOnly", true); report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
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
    private static Object verifyRetiredClosure(ServerStartedEvent event) throws Exception {
        for (String profile : List.of("sugar", "butcher", "saya", "wine", "rat", "defect", "compost", "apple", "tide", "benger", "", "unknown")) {
            if (!net.farmingtales.ftboss.item.StatueBlessingCatalog.pool(profile).isEmpty()
                || !net.farmingtales.ftboss.item.ArmorBlessingCatalog.pool(profile).isEmpty()) throw new AssertionError("Retired food catalogue still publishes entries");
        }
        if (!net.farmingtales.ftboss.item.StatueBlessingCatalog.all().isEmpty() || !net.farmingtales.ftboss.item.ArmorBlessingCatalog.all().isEmpty()
            || net.farmingtales.ftboss.item.StatueBlessingCatalog.byId("butcher_hook_pull") != null
            || net.farmingtales.ftboss.item.ArmorBlessingCatalog.byId("armor_butcher_strike") != null) throw new AssertionError("Retired food catalogue remains discoverable");
        ItemStack stored = new ItemStack(Items.IRON_SWORD);
        CompoundTag legacy = new CompoundTag();
        legacy.putString("Profile", "butcher"); legacy.putInt("Level", 3);
        var blessings = new net.minecraft.nbt.ListTag();
        for (String identity : List.of("blood_feast", "cleaver_might", "syrup_step", "butcher_hook_pull")) {
            CompoundTag row = new CompoundTag(); row.putString("Blessing", identity); row.putInt("Level", 3); blessings.add(row);
        }
        legacy.put("FoodBlessings", blessings); CompoundTag root = new CompoundTag(); root.put("FTBossFoodEvolution", legacy);
        stored.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        var sharpness = event.getServer().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS);
        var enchantments = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        enchantments.set(sharpness, 3); stored.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
        CompoundTag before = net.farmingtales.ftboss.item.NativeFoundationItemData.image(stored);
        if (net.farmingtales.ftboss.item.FoodBlessingCombat.eligible(stored)
            || net.farmingtales.ftboss.item.FoodBlessingCombat.level(stored, "blood_feast") != 0
            || net.farmingtales.ftboss.item.FoodBlessingCombat.level(stored, "cleaver_might") != 0
            || net.farmingtales.ftboss.item.FoodBlessingCombat.crossbowChargeTicks(25, stored) != 25) throw new AssertionError("Stored retired blessing still enables gameplay");
        net.farmingtales.ftboss.item.FoodBlessingCombat.blessingAttributes(null, stored);
        net.farmingtales.ftboss.item.FoodBlessingCombat.blessingAttributes(null, stored, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        net.farmingtales.ftboss.item.FoodBlessingCombat.quoteDamage(null, null, stored);
        net.farmingtales.ftboss.item.FoodBlessingCombat.defense(null); net.farmingtales.ftboss.item.FoodBlessingCombat.killFeast(null);
        net.farmingtales.ftboss.item.StatueBlessingCombat.quote(null, null, null, root, 80L, stored, null);
        net.farmingtales.ftboss.item.StatueBlessingCombat.defense(null); net.farmingtales.ftboss.item.StatueBlessingCombat.kill(null);
        net.farmingtales.ftboss.item.StatueBlessingCombat.attributes(null, stored);
        net.farmingtales.ftboss.item.StatueBlessingCombat.attributes(null, stored, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        if (!before.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(stored))
            || stored.get(DataComponents.ENCHANTMENTS).getLevel(sharpness) != 3) throw new AssertionError("Retirement rewrote stored data or vanilla enchantment component");
        net.farmingtales.ftboss.network.StatueMenuNetwork.Action.send(net.minecraft.core.BlockPos.ZERO, 7, 1);
        net.farmingtales.ftboss.network.StatueMenuNetwork.Action.handle(new net.farmingtales.ftboss.network.StatueMenuNetwork.Action(net.minecraft.core.BlockPos.ZERO, 7, 1), null);
        net.farmingtales.ftboss.network.StatueMenuNetwork.OrdinaryAction.send(net.minecraft.core.BlockPos.ZERO, 7, 1);
        for (String language : List.of("en_us", "zh_cn")) {
            var actualResource = net.neoforged.fml.ModList.get().getModFileById("ftboss").getFile().findResource("assets", "ftboss", "lang", language + ".json");
            try (var stream = Files.newInputStream(actualResource)) {
                if (stream == null) throw new AssertionError("Actual retired language resource not found");
                var values = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                for (String key : values.keySet()) if (key.startsWith("food_blessing.ftboss.") || key.startsWith("tooltip.ftboss.food_blessing.")
                    || key.startsWith("message.ftboss.food_blessing.") || key.startsWith("screen.ftboss.statue_hub.enchant.")
                    || key.equals("screen.ftboss.statue_hub.tab.enchant")) throw new AssertionError("Actual language resource still exposes retired food enchantment");
                for (String key : List.of("item.ftboss.maque_blessing_feather", "tooltip.ftboss.maque_feather.blessing", "message.ftboss.maque_feather.blessing"))
                    if (!values.has(key)) throw new AssertionError("Unrelated maque blessing translation removed");
            }
        }
        return Map.of("cataloguesEmpty", true, "storedFoodEffectsInert", true, "storedDataAndVanillaSharpnessThreePreserved", true,
            "retiredPacketSendAndHandleReturnWithoutNetworkOrContext", true, "actualLanguageResourcesRetiredAndMaqueKeysRetained", true);
    }


}
