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
            for (int sample : new int[]{5}) {
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
                var useResult = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                row.put("actualItemStackUseResult", useResult.getResult().toString());
                row.put("originalFrostQueenStaffUseReturned", true); row.put("healthBefore", healthBefore); row.put("healthAfter", target.getHealth());
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
                    }
                }
                row.put("sameLeaseAndGenerationAfterAttack", leaseMethod.invoke(null, actual) == actualLease && generationMethod.invoke(null, actual) == generation);
                row.put("generationRetiredAfterAttack", read(generation, "retired"));
                row.put("capturedNativeFrostPurpose", capturedSigned == null ? "" : capturedSigned.getString("DamagePurpose"));
                boolean outsideDenied = capturedSigned != null && capturedSource != null
                    && net.farmingtales.ftboss.item.FoodWeaponSourceLedger.resolveReadOnly(capturedSource, player).isEmpty()
                    && !net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(capturedSigned, player);
                row.put("sameActualSourceAndSnapshotDeniedAfterFrostReturn", outsideDenied);
                if (!outsideDenied || actual.getDamageValue() != wearBefore + 1 || !useResult.getResult().consumesAction())
                    throw new AssertionError("Actual original Frost ray did not preserve use/wear or close its actual-hurt permission");
                target.discard();
                if (target.getHealth() >= healthBefore || damageEvents.isEmpty() || publications.isEmpty()
                    || publications.stream().anyMatch(value -> !Boolean.TRUE.equals(value.get("traceComplete")) || Boolean.TRUE.equals(value.get("prepared_retired"))))
                    throw new AssertionError("Actual paid original Frost ray did not damage target and complete live signed Session for " + id);
                if (damageEvents.stream().noneMatch(value -> Boolean.TRUE.equals(value.get("resolvedSnapshotValid")) && Boolean.TRUE.equals(value.get("repairEpochPresent"))
                        && Boolean.TRUE.equals(value.get("serverSignaturePresent")))
                    || !Boolean.TRUE.equals(row.get("sameLeaseAndGenerationAfterAttack")) || Boolean.TRUE.equals(row.get("generationRetiredAfterAttack")))
                    throw new AssertionError("Actual original Frost ray lost signed non-null repair authority for " + id);
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
                    || !committedRecord.getString("Status").equals("COMMITTED_ORIGINAL_SAVE") || ticket.getLong("LeaseGeneration") != 3L
                    || !ticket.getUUID("Save").equals(saved.getUUID("FTBossRepairSave")))
                    throw new AssertionError("Actual post-Frost original save did not commit the genuine generation-three continuation");
                row.put("actualOriginalPostCombatSaveAuthenticatedCommitted", true);

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
        report.put("allSixFamilySignedCombatAccepted", false); report.put("staffCastingSubstitutedWithMelee", false);
        report.put("staffEntry", "Actual ItemStack.use -> original FrostQueenStaffItem.use/ray/strike, not synthetic direct hurt or Iron's spell cast");
        report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
