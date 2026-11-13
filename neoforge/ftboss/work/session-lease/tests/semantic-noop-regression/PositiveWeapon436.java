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
            for (int sample : new int[]{0, 1, 2, 3, 4, 5}) {
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
                CompoundTag beforeTracking = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                Object componentBeforeTracking = actual.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                for (int repetition = 0; repetition < 2; repetition++) net.farmingtales.ftboss.item.FoodWeaponLifecycle.ensureTracked(actual);
                if (actual.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) != componentBeforeTracking
                    || !beforeTracking.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    || leaseMethod.invoke(null, actual) != actualLease || generationMethod.invoke(null, actual) != generation
                    || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Repeated original ensureTracked no-op replaced actual component or retired genuine paid authority");
                row.put("repeatedOriginalEnsureTrackedPreservedExactComponentIdentityWholeImageAndLiveAuthority", true);
                CompoundTag evolution = net.farmingtales.ftboss.item.FoodWeaponData.read(actual, "FTBossFoodEvolution");
                if (evolution == null) throw new AssertionError("Genuine retained representative lacks original evolution root");
                var saveEvolution = net.farmingtales.ftboss.item.FoodWeaponEvolution.class.getDeclaredMethod("saveData", net.minecraft.world.item.ItemStack.class, CompoundTag.class);
                saveEvolution.setAccessible(true);
                for (int repetition = 0; repetition < 2; repetition++) saveEvolution.invoke(null, actual, evolution.copy());
                if (actual.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) != componentBeforeTracking
                    || !beforeTracking.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    || leaseMethod.invoke(null, actual) != actualLease || generationMethod.invoke(null, actual) != generation
                    || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Identical original evolution save replaced component or retired paid authority");
                row.put("repeatedOriginalEvolutionSavePreservedExactComponentIdentityWholeImageAndLiveAuthority", true);
                if (sample % 2 == 0) {
                    CompoundTag changed = evolution.copy();
                    changed.putLong("LastSocketProc", changed.getLong("LastSocketProc") + 1L);
                    saveEvolution.invoke(null, actual, changed);
                    row.put("actualChangedOriginalEvolutionDataPersisted", changed.equals(net.farmingtales.ftboss.item.FoodWeaponData.read(actual, "FTBossFoodEvolution")));
                    saveEvolution.invoke(null, actual, evolution.copy());
                    row.put("wholeImageRestoredByOriginalEvolutionSave", beforeTracking.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual)));
                } else {
                    CompoundTag lifecycle = net.farmingtales.ftboss.item.FoodWeaponData.read(actual, "FTBossWeaponLifecycle");
                    CompoundTag changed = lifecycle.copy();
                    changed.putInt("WearStep", changed.getInt("WearStep") + 1);
                    net.farmingtales.ftboss.item.NativeLifecycleItemData.commit(actual, "FTBossWeaponLifecycle", lifecycle, changed);
                    row.put("actualChangedOriginalLifecycleDataPersisted", changed.equals(net.farmingtales.ftboss.item.FoodWeaponData.read(actual, "FTBossWeaponLifecycle")));
                    net.farmingtales.ftboss.item.NativeLifecycleItemData.commit(actual, "FTBossWeaponLifecycle", changed, lifecycle);
                    row.put("wholeImageRestoredByOriginalLifecycleCommit", beforeTracking.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual)));
                }
                if (!Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Real component change and restored-image ABA failed to retire original genuine generation");
                if (row.entrySet().stream().anyMatch(entry -> entry.getKey().startsWith("actualChanged") && !Boolean.TRUE.equals(entry.getValue()))
                    || row.entrySet().stream().anyMatch(entry -> entry.getKey().startsWith("wholeImageRestored") && !Boolean.TRUE.equals(entry.getValue())))
                    throw new AssertionError("Mutation/ABA negative control did not perform its real original data writes");
                row.put("realChangeAndRestoredWholeImageAbaNeverRevivedGeneration", true);
                row.put("negativeControlUsesOrdinaryDataMutationNotExpectedGameplayWear", true);
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
        report.put("allSixFamilySignedCombatAccepted", false); report.put("sixFamilySemanticNoopAndRealMutationAbaRegression", true); report.put("staffCastingSubstitutedWithMelee", false);
        report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
