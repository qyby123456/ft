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
            for (int sample : new int[]{4}) {
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
                var useResult = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                row.put("actualOriginalUseResult", useResult.getResult().toString());
                row.put("actualUseStarted", player.isUsingItem());
                row.put("generationRetiredAfterUse", read(generation, "retired"));
                row.put("brokenAfterOriginalUse", net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual));
                Class<?> registry = Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry");
                row.put("coverageRegistryInvalidAfterOriginalUse", readStatic(registry, "invalid"));
                var certificate = registry.getDeclaredMethod("nativeItemStackCertified165"); certificate.setAccessible(true);
                row.put("itemStackCertificateAfterOriginalUse", certificate.invoke(null));
                var sourceNodes = registry.getDeclaredMethod("nativeItemStackSourceNodes165"); sourceNodes.setAccessible(true);
                Object nodes = sourceNodes.invoke(null);
                row.put("actualItemStackProofSourcesPresent", nodes != null);
                try {
                    Class<?> proof = Class.forName("net.farmingtales.ftboss.item.NativeItemStackInstallationProof165");
                    var verify = java.util.Arrays.stream(proof.getDeclaredMethods()).filter(method -> method.getName().equals("verify") && method.getParameterCount() == 3).findFirst().orElseThrow();
                    verify.setAccessible(true);
                    verify.invoke(null, readStatic(registry, "nativeItemStackTarget165"), nodes, readStatic(registry, "nativeItemStackPristine165"));
                    row.put("actualItemStackWholeParentProofAfterOriginalUse", true);
                } catch (java.lang.reflect.InvocationTargetException failure) {
                    row.put("actualItemStackWholeParentProofFailure", failure.getCause().toString());
                }

                if (!useResult.getResult().consumesAction() || !player.isUsingItem() || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Original paid Trident ItemStack.use did not start with live authority");
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
                        Object observationFailure = read(session, "firstObservationFailure");
                        if (observationFailure instanceof Throwable failure) {
                            java.io.StringWriter diagnostic = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(diagnostic));
                            publicationRow.put("firstObservationFailure", diagnostic.toString());
                        }

                    }
                }
                row.put("sameLeaseAndGenerationAfterAttack", leaseMethod.invoke(null, actual) == actualLease && generationMethod.invoke(null, actual) == generation);
                row.put("generationRetiredAfterAttack", read(generation, "retired"));
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
        report.put("readOnlyDiagnosticNotPositiveAcceptance", true); report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
