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
                CompoundTag beforeForeign = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                Object beforeForeignInput = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                Class<?> semantic = Class.forName("net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority");
                var mutation = semantic.getDeclaredMethod("nativeOperationMutation", Object.class, int.class); mutation.setAccessible(true);
                if (Boolean.TRUE.equals(mutation.invoke(null, actual, 21))) throw new AssertionError("Foreign mutation callback granted authority");
                var factoryReady = semantic.getDeclaredMethod("nativeTridentFactoryReady", net.minecraft.world.item.ItemStack.class, Object.class, Object.class); factoryReady.setAccessible(true);
                if (Boolean.TRUE.equals(factoryReady.invoke(null, actual, actualLease, generation))) throw new AssertionError("Foreign factory terminal granted authority");
                Class<?> foreignGuard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
                var refresh = foreignGuard.getDeclaredMethod("refreshNativeOperation", net.minecraft.world.item.ItemStack.class, actualLease.getClass(), Object.class); refresh.setAccessible(true);
                if (Boolean.TRUE.equals(refresh.invoke(null, actual, actualLease, generation))) throw new AssertionError("Foreign refresh granted authority");
                RuntimeException sentinel = new RuntimeException("original-trident-factory-sentinel");
                int[] originalCalls = new int[]{0};
                boolean originalExceptionPreserved = false;
                try {
                    net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeTridentFactory(server.overworld(), player, actual, arguments -> {
                        originalCalls[0]++;
                        throw sentinel;
                    });
                } catch (RuntimeException originalFailure) {
                    originalExceptionPreserved = originalFailure == sentinel;
                }
                boolean helpersUnchanged = beforeForeign.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    && beforeForeignInput == ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput()
                    && leaseMethod.invoke(null, actual) == actualLease && generationMethod.invoke(null, actual) == generation
                    && !Boolean.TRUE.equals(read(generation, "retired"));
                row.put("foreignCallbacksPreserveExactInputImageAndAuthority", helpersUnchanged);
                row.put("foreignOriginalFactoryExceptionIdentityPreserved", originalExceptionPreserved);
                row.put("foreignOriginalFactoryCallCount", originalCalls[0]);
                if (!helpersUnchanged || !originalExceptionPreserved || originalCalls[0] != 1)
                    throw new AssertionError("Foreign callbacks modified authority or changed original exception identity");
                actual.remove(net.minecraft.core.component.DataComponents.INTANGIBLE_PROJECTILE);
                row.put("externalAbsentComponentRemovalImageUnchanged", beforeForeign.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual)));
                row.put("externalAbsentComponentRemovalRetiresAuthority", read(generation, "retired"));
                if (!Boolean.TRUE.equals(row.get("externalAbsentComponentRemovalImageUnchanged")) || !Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Outside absent-component removal was incorrectly admitted as a native factory operation");
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

                boolean unsignedCarrier = flights.size() == 1 && flights.stream().noneMatch(value -> Boolean.TRUE.equals(value.get("snapshotPresent"))
                    || Boolean.TRUE.equals(value.get("snapshotValid")) || Boolean.TRUE.equals(value.get("signaturePresent")) || Boolean.TRUE.equals(value.get("paidCarrierPresent")));
                boolean scopesClosed = ((ThreadLocal<?>)readStatic(semantic, "NATIVE_REPAIR_OPERATION")).get() == null
                    && ((ThreadLocal<?>)readStatic(semantic, "NATIVE_WEAR")).get() == null
                    && ((ThreadLocal<?>)readStatic(Class.forName("net.farmingtales.ftboss.item.FoodWeaponSourceLedger"), "NATIVE_OPERATION")).get() == null
                    && Integer.valueOf(0).equals(((ThreadLocal<?>)readStatic(Class.forName("net.farmingtales.ftboss.item.FoodWeaponSourceLedger"), "NATIVE_DEPTH")).get());
                row.put("foreignNativeEntryOriginalThrowReturnedOnce", arrows.size() == 1 && actual.getDamageValue() == wearBefore + 1 && target.getHealth() < healthBefore);
                row.put("foreignNativeEntryNoSignedCarrier", unsignedCarrier);
                row.put("foreignNativeEntryAuthorityRetired", read(generation, "retired"));
                row.put("foreignNativeEntryScopesClosed", scopesClosed);
                if (!Boolean.TRUE.equals(row.get("foreignNativeEntryOriginalThrowReturnedOnce")) || !unsignedCarrier || !scopesClosed
                    || !Boolean.TRUE.equals(read(generation, "retired")) || !publications.isEmpty() || !actions.isEmpty()
                    || damageEvents.stream().anyMatch(value -> Boolean.TRUE.equals(value.get("resolvedSnapshotValid")) || Boolean.TRUE.equals(value.get("serverSignaturePresent"))))
                    throw new AssertionError("Foreign Trident entry minted paid signed authority or changed the original operation");
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
        report.put("tridentEntry", "Genuine paid original use, rejected outside helpers and original factory exception identity, then external-remove original throw and real impact without signed authority");
        report.put("foundationGateScopeOnly", true); report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
