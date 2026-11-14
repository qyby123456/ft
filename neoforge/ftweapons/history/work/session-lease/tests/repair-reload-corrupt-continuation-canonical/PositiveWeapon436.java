package ftboss.storage308;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_storage308")
public final class PositiveWeapon436 {
    public PositiveWeapon436() { NeoForge.EVENT_BUS.addListener(PositiveWeapon436::started); }

    private static Object read(Object instance, String name) throws Exception {
        for (Class<?> type = instance.getClass(); type != null; type = type.getSuperclass()) {
            try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(instance); }
            catch (NoSuchFieldException missing) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static String digest(Path path) throws Exception {
        return Files.exists(path) ? HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))) : "ABSENT";
    }

    private static Map<String, String> otherAuthority(Path root, Path excluded) throws Exception {
        root = root.toAbsolutePath().normalize();
        Path excludedAbsolute = excluded.toAbsolutePath().normalize();
        Map<String, String> result = new LinkedHashMap<>();
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(value -> value.toString().endsWith(".bin") && !value.equals(excludedAbsolute)).sorted().toList())
                result.put(root.relativize(path).toString(), digest(path));
        }
        return result;
    }

    private static void started(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        try {
            var server = event.getServer();
            UUID owner = new UUID(334, 1);
            Path playerFile = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(owner + ".dat");
            CompoundTag saved = NbtIo.readCompressed(playerFile, NbtAccounter.unlimitedHeap());
            CompoundTag ticket = saved.getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
            Class<?> storeType = Class.forName("net.farmingtales.ftboss.item.OwnerLineageRevocationStore");
            var open = storeType.getDeclaredMethod("open", net.minecraft.server.MinecraftServer.class); open.setAccessible(true);
            Object store = open.invoke(null, server);
            UUID documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-continuation/v1/" + owner).getBytes(StandardCharsets.UTF_8));
            var documentPath = storeType.getDeclaredMethod("repairDocumentPath", UUID.class, boolean.class); documentPath.setAccessible(true);
            Path document = (Path)documentPath.invoke(store, documentOwner, false);
            Path authority = server.getWorldPath(LevelResource.ROOT).resolve("ftboss-authority");
            if (!document.toRealPath().startsWith(authority.toRealPath()) || Files.isSymbolicLink(document))
                throw new AssertionError("Corruption fixture path escaped copied authority directory");
            Path backup = Path.of("authentic-unused-continuation-backup.bin");
            byte[] authentic = Files.readAllBytes(backup);
            byte[] initialCorrupt = authentic.clone(); initialCorrupt[initialCorrupt.length - 1] ^= 1;
            if (!Arrays.equals(initialCorrupt, Files.readAllBytes(document))) throw new AssertionError("Initial corruption was not present before original server boot");
            var readBackup = storeType.getDeclaredMethod("readRepairDocument", Path.class, UUID.class, boolean.class); readBackup.setAccessible(true);
            CompoundTag original = (CompoundTag)readBackup.invoke(store, backup, documentOwner, false);
            CompoundTag record = original.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString());
            if (!original.getString("Status").equals("COMMITTED_ORIGINAL_SAVE") || !record.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")
                || !ticket.equals(record.getCompound("Ticket"))) throw new AssertionError("Backup is not the exact authentic unused committed original continuation");
            var recordPath = storeType.getDeclaredMethod("recordPath", UUID.class, UUID.class); recordPath.setAccessible(true);
            Path lineagePath = (Path)recordPath.invoke(store, owner, ticket.getUUID("Weapon"));
            var readRecord = storeType.getDeclaredMethod("read", Path.class, UUID.class, UUID.class); readRecord.setAccessible(true);
            Object lineage = readRecord.invoke(store, lineagePath, owner, ticket.getUUID("Weapon"));
            if (!Boolean.TRUE.equals(read(lineage, "active")) || !ticket.getUUID("LeaseEpoch").equals(read(lineage, "incarnation"))
                || ticket.getLong("LeaseGeneration") != ((Number)read(lineage, "generation")).longValue())
                throw new AssertionError("Corrupt continuation fixture also has an inactive or mismatching lineage");
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var lease = guard.getDeclaredMethod("lease", Object.class); lease.setAccessible(true);
            var generation = guard.getDeclaredMethod("generation", Object.class); generation.setAccessible(true);
            var readDocument = storeType.getDeclaredMethod("readRepairDocument", UUID.class, boolean.class); readDocument.setAccessible(true);
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            UUID journalOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-journal/v1/" + owner).getBytes(StandardCharsets.UTF_8));
            Path journal = (Path)documentPath.invoke(store, journalOwner, false);
            byte[] wrongDomain = Files.readAllBytes(journal);
            readBackup.invoke(store, journal, journalOwner, false);
            Path wearFile = server.getWorldPath(LevelResource.ROOT).resolve("data/ftboss_native_weapon_wear.dat");
            String playerBefore = digest(playerFile);
            String wearBefore = digest(wearFile);
            Map<String, String> otherBefore = otherAuthority(authority, document);
            List<Map<String, Object>> cases = new ArrayList<>();
            for (String mode : List.of("preboot-hmac-byte-flip", "truncated-document", "authentic-wrong-domain-document", "missing-document")) {
                if (mode.equals("truncated-document")) Files.write(document, Arrays.copyOf(authentic, 16));
                if (mode.equals("authentic-wrong-domain-document")) Files.write(document, wrongDomain);
                if (mode.equals("missing-document")) Files.delete(document);
                String corruptedHash = digest(document);
                String authenticationResult;
                try {
                    CompoundTag observed = (CompoundTag)readDocument.invoke(store, documentOwner, false);
                    if (!mode.equals("missing-document") || !observed.isEmpty()) throw new AssertionError("Corrupt document authenticated");
                    authenticationResult = "absent-empty-document";
                } catch (InvocationTargetException rejected) {
                    if (!(rejected.getCause() instanceof java.io.IOException)) throw rejected;
                    authenticationResult = rejected.getCause().getMessage();
                }
                for (int repetition = 0; repetition < 2; repetition++) {
                    ServerPlayer rejected = new ServerPlayer(server, server.overworld(), new GameProfile(owner, "ft317_0"), ClientInformation.createDefault());
                    if (storage.load(rejected).isEmpty()) throw new AssertionError("Original load did not run");
                    var actual = rejected.getInventory().getItem(0);
                    CompoundTag image = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                    if (lease.invoke(null, actual) != null || generation.invoke(null, actual) != null || !image.equals(record.getCompound("Expected")))
                        throw new AssertionError("Corrupt/absent continuation issued authority or changed actual whole item");
                    UUID identity = image.getCompound("components").getCompound("minecraft:custom_data").getCompound("FTBossWeaponLifecycle").getCompound("NativeWear").getUUID("Identity");
                    CompoundTag wear = NbtIo.readCompressed(wearFile, NbtAccounter.unlimitedHeap()).getCompound("data").getCompound("Weapons").getCompound(identity.toString());
                    if (wear.getInt("Damage") != actual.getDamageValue() || wear.getInt("Maximum") != actual.getMaxDamage())
                        throw new AssertionError("Corruption fixture has incoherent original wear");
                    if (!corruptedHash.equals(digest(document)) || !otherBefore.equals(otherAuthority(authority, document))
                        || !playerBefore.equals(digest(playerFile)) || !wearBefore.equals(digest(wearFile)))
                        throw new AssertionError("Rejected original load changed state: mode=" + mode + ", document=" + !corruptedHash.equals(digest(document))
                            + ", otherAuthority=" + !otherBefore.equals(otherAuthority(authority, document)) + ", player=" + !playerBefore.equals(digest(playerFile))
                            + ", wear=" + !wearBefore.equals(digest(wearFile)));
                }
                cases.add(Map.of("mode", mode, "authenticationResult", authenticationResult, "documentSha256OrAbsent", corruptedHash,
                    "actualOriginalLoadAndReplayDeniedWithoutStateChange", true));
            }
            Files.write(document, authentic);
            if (!Arrays.equals(authentic, Files.readAllBytes(document)) || !otherBefore.equals(otherAuthority(authority, document)))
                throw new AssertionError("Explicit backup restoration changed original authority state");
            ServerPlayer recovered = new ServerPlayer(server, server.overworld(), new GameProfile(owner, "ft317_0"), ClientInformation.createDefault());
            if (storage.load(recovered).isEmpty()) throw new AssertionError("Original load after exact unused backup restoration did not run");
            var actual = recovered.getInventory().getItem(0);
            Object actualLease = lease.invoke(null, actual);
            Object actualGeneration = generation.invoke(null, actual);
            if (actualLease == null || actualGeneration == null || !net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual).equals(record.getCompound("Expected")))
                throw new AssertionError("Exact authentic unused backup did not permit genuine successor through original load");
            Object advanced = readRecord.invoke(store, lineagePath, owner, ticket.getUUID("Weapon"));
            if (!Boolean.TRUE.equals(read(advanced, "active")) || !ticket.getUUID("LeaseEpoch").equals(read(advanced, "incarnation"))
                || ticket.getLong("LeaseGeneration") + 1 != ((Number)read(advanced, "generation")).longValue())
                throw new AssertionError("Recovery did not advance the same original active lineage exactly once");
            CompoundTag consumed = (CompoundTag)readDocument.invoke(store, documentOwner, false);
            if (!consumed.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString()).getString("Status").equals("CONSUMED_ORIGINAL_LOAD"))
                throw new AssertionError("Recovery did not consume original continuation");
            String consumedHash = digest(document);
            String advancedHash = digest(lineagePath);
            ServerPlayer replayed = new ServerPlayer(server, server.overworld(), new GameProfile(owner, "ft317_0"), ClientInformation.createDefault());
            storage.load(replayed);
            if (lease.invoke(null, replayed.getInventory().getItem(0)) != null || generation.invoke(null, replayed.getInventory().getItem(0)) != null
                || lease.invoke(null, actual) != actualLease || generation.invoke(null, actual) != actualGeneration
                || !consumedHash.equals(digest(document)) || !advancedHash.equals(digest(lineagePath)))
                throw new AssertionError("Consumed recovery replay reissued or changed live successor");
            storage.save(recovered);
            CompoundTag savedAgain = (CompoundTag)readDocument.invoke(store, documentOwner, false);
            if (!savedAgain.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")) throw new AssertionError("Original post-recovery save failed to commit");
            report.put("failures", 0); report.put("cases", cases);
            report.put("actualNewServerBootWithHmacCorruption", true);
            report.put("allFourCorruptOrMissingCasesAndEightOriginalLoadsDeniedAuthority", true);
            report.put("originalPlayerWholeItemAndWearCoherentAndActiveLineageMatching", true);
            report.put("allOtherSignedAuthorityBinsAndOriginalPlayerWearUnchangedDuringRejections", true);
            report.put("explicitRestorationOfExactAuthenticPreviouslyUnusedBackupOnly", true);
            report.put("recoveryIsNotAutomaticProductBackupRepair", true);
            report.put("actualOriginalLoadAfterBackupRestorationIssuedGenuineSuccessor", true);
            report.put("sameEpochGenerationAdvancedExactlyOnceAfterRestoration", true);
            report.put("consumedRecoveryReplayDeniedWithoutChangingFirstSuccessor", true);
            report.put("actualOriginalPostRecoverySaveCommitted", true);
            report.put("originalGeneration", ticket.getLong("LeaseGeneration"));
            report.put("successorGeneration", ticket.getLong("LeaseGeneration") + 1);
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        }
        report.put("authorityWrittenByReflection", false); report.put("authorityDocumentBytesIntentionallyFaultInjectedInCopiedWorld", true);
        report.put("signedAuthorityForgedOrResigned", false); report.put("retiredStateReset", false);
        report.put("authenticatedMultiplayerAccepted", false); report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
