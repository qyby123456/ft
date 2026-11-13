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
                Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
                var generation = guard.getDeclaredMethod("generation", Object.class); generation.setAccessible(true);
                Object current = generation.invoke(null, player.getMainHandItem());
                row.put("diagnosticGenerationPresentAtDamage", current != null);
                if (current != null) row.put("diagnosticGenerationRetiredAtDamage", read(current, "retired"));
                Object ingress = ((ThreadLocal<?>)readStatic(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, "PRIVATE_INGRESS")).get();
                Object hurt = ((ThreadLocal<?>)readStatic(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, "PRIVATE_NATIVE_HURT")).get();
                row.put("diagnosticOriginalIngressPresentAtDamage", ingress != null);
                row.put("diagnosticOriginalHurtScopePresentAtDamage", hurt != null);

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
            for (int sample = 0; sample < 1; sample++) {
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
                row.put("diagnosticFoundationApplies", net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(actual));
                row.put("diagnosticEvolutionCandidate", net.farmingtales.ftboss.item.FoodWeaponEvolution.isEvolutionCandidate(actual));
                row.put("diagnosticNativeFamily", net.farmingtales.ftboss.item.FoodWeaponUseBridge.kind(actual).name());
                Class<?> inputFields = Class.forName("net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields");
                Object nativeInput = inputFields.getMethod("ftboss$ownerNativeWeaponInput").invoke(actual);
                row.put("diagnosticNativeInputPresentBeforeAttack", nativeInput != null);
                if (nativeInput != null) {
                    row.put("diagnosticNativeInputRetiredBeforeAttack", read(nativeInput, "retired"));
                    row.put("diagnosticNativeInputSameThread", read(nativeInput, "thread") == Thread.currentThread());
                    row.put("diagnosticNativeInputActualSame", ((java.lang.ref.WeakReference<?>)read(nativeInput, "actual")).get() == actual);
                }
                var permits = guard.getDeclaredMethod("permits", Object.class); permits.setAccessible(true);
                row.put("diagnosticGuardPermitsBeforeAttack", permits.invoke(null, actual));
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
                        Object failure = read(session, "firstObservationFailure");
                        publicationRow.put("firstObservationFailure", failure == null ? null : failure.toString());
                        if (!Boolean.TRUE.equals(publicationRow.get("traceComplete"))
                            || !Boolean.TRUE.equals(publicationRow.get("session_coverageInstalled"))
                            || Boolean.TRUE.equals(publicationRow.get("session_traceRevoked"))
                            || ((Number)publicationRow.get("session_next")).intValue() != 4 || failure != null)
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
            report.put("nineteenExactOriginalMeleeVariantsGenerationTwoSignedSessions", true);
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
