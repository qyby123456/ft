package ftboss.storage308;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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
    public PositiveWeapon436() {
        NeoForge.EVENT_BUS.addListener(PositiveWeapon436::started);
    }

    private static Object read(Object instance, String name) throws Exception {
        for (Class<?> type = instance.getClass(); type != null; type = type.getSuperclass()) {
            try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(instance); }
            catch (NoSuchFieldException missing) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static Method method(Class<?> type, String name, Class<?>... parameters) throws Exception {
        Method result = type.getDeclaredMethod(name, parameters);
        result.setAccessible(true);
        return result;
    }

    private static String digest(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static void forceReceipt(Path path, Map<String, Object> report) throws Exception {
        ByteBuffer bytes = ByteBuffer.wrap(new GsonBuilder().setPrettyPrinting().create().toJson(report).getBytes(StandardCharsets.UTF_8));
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            while (bytes.hasRemaining()) channel.write(bytes);
            channel.force(true);
        }
    }

    private static void started(ServerStartedEvent event) {
        Map<String, Object> failureReport = new LinkedHashMap<>();
        CountDownLatch acquireMonitor = new CountDownLatch(1);
        CountDownLatch monitorHeld = new CountDownLatch(1);
        CountDownLatch releaseMonitor = new CountDownLatch(1);
        AtomicReference<Throwable> readerFailure = new AtomicReference<>();
        Path releaseFile = Path.of("release-lineage-lock.signal");
        try {
            var server = event.getServer();
            Thread serverThread = Thread.currentThread();
            UUID owner = new UUID(334, 1);
            ServerPlayer player = new ServerPlayer(server, server.overworld(), new GameProfile(owner, "ft317_0"), ClientInformation.createDefault());
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            Path playerFile = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(owner + ".dat");
            byte[] originalPlayer = Files.readAllBytes(playerFile);
            CompoundTag saved = NbtIo.readCompressed(playerFile, NbtAccounter.unlimitedHeap());
            CompoundTag ticket = saved.getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
            UUID weapon = ticket.getUUID("Weapon");
            UUID epoch = ticket.getUUID("LeaseEpoch");
            long savedGeneration = ticket.getLong("LeaseGeneration");
            int savedSlot = ticket.getInt("Slot");
            String savedPlayerHash = digest(originalPlayer);
            var threadBean = ManagementFactory.getThreadMXBean();
            Class<?> storeType = Class.forName("net.farmingtales.ftboss.item.OwnerLineageRevocationStore");
            Object store = method(storeType, "open", net.minecraft.server.MinecraftServer.class).invoke(null, server);
            Method readDocument = method(storeType, "readRepairDocument", UUID.class, boolean.class);
            UUID documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-continuation/v1/" + owner).getBytes(StandardCharsets.UTF_8));
            CompoundTag before = (CompoundTag)readDocument.invoke(store, documentOwner, false);
            Path documentPath = (Path)method(storeType, "repairDocumentPath", UUID.class, boolean.class).invoke(store, documentOwner, false);
            CompoundTag originalRecord = before.getCompound("Continuations").getCompound(weapon.toString());
            if (!before.getString("Status").equals("COMMITTED_ORIGINAL_SAVE") || !originalRecord.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")
                || !ticket.equals(originalRecord.getCompound("Ticket"))) throw new AssertionError("Crash fixture lacks its original committed ticket");
            Path lineagePath = (Path)method(storeType, "recordPath", UUID.class, UUID.class).invoke(store, owner, weapon);
            Method readRecord = method(storeType, "read", Path.class, UUID.class, UUID.class);
            Object predecessor = readRecord.invoke(store, lineagePath, owner, weapon);
            if (!Boolean.TRUE.equals(read(predecessor, "active")) || !ticket.getUUID("LeaseEpoch").equals(read(predecessor, "incarnation"))
                || ticket.getLong("LeaseGeneration") != ((Number)read(predecessor, "generation")).longValue())
                throw new AssertionError("Crash fixture lacks its exact active predecessor");
            Path lockPath = lineagePath.resolveSibling(weapon + ".lock");
            try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.WRITE)) {
                var unexpected = channel.tryLock();
                if (unexpected != null) { unexpected.release(); throw new AssertionError("External original-lineage lock is not held"); }
            }
            Class<?> guardType = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            Thread holder = new Thread(() -> {
                try {
                    if (!acquireMonitor.await(90L, TimeUnit.SECONDS)) throw new AssertionError("Reservation was not observed");
                    synchronized (guardType) {
                        monitorHeld.countDown();
                        if (!releaseMonitor.await(90L, TimeUnit.SECONDS)) throw new AssertionError("Verified crash cut was not reached");
                    }
                } catch (Throwable failure) {
                    readerFailure.compareAndSet(null, failure);
                }
            }, "ftboss-crash-cut-monitor-holder");
            holder.setDaemon(true);
            holder.start();
            Thread reader = new Thread(() -> {
                try {
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(60L);
                    CompoundTag reservation;
                    while (true) {
                        CompoundTag observed = (CompoundTag)readDocument.invoke(store, documentOwner, false);
                        reservation = observed.getCompound("Continuations").getCompound(weapon.toString());
                        if (reservation.getString("Status").equals("RESERVED_ORIGINAL_LOAD_NO_REISSUE")) break;
                        if (System.nanoTime() > deadline || readerFailure.get() != null) throw new AssertionError("Actual reservation was not reached");
                        Thread.sleep(5L);
                    }
                    if (!ticket.equals(reservation.getCompound("Ticket")) || !originalRecord.getCompound("Expected").equals(reservation.getCompound("Expected"))
                        || !reservation.hasUUID("Load")) throw new AssertionError("Observed reservation differs from original authenticated checkpoint");
                    UUID load = reservation.getUUID("Load");
                    String reservedDocumentText = ((CompoundTag)readDocument.invoke(store, documentOwner, false)).toString();
                    byte[] reservedDocumentBytes = Files.readAllBytes(documentPath);
                    Object loadedActual = player.getInventory().getItem(savedSlot);
                    var actualBeforeMonitor = player.getInventory().getItem(savedSlot);
                    if (actualBeforeMonitor.isEmpty()
                        || ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)loadedActual).ftboss$ownerNativeWeaponInput() == null)
                        throw new AssertionError("Original reserved load has no actual semantic input");
                    Object node = ((net.farmingtales.ftboss.item.OwnerRevocationNodeAccess)loadedActual).ftboss$ownerRevocationNode();
                    Files.writeString(Path.of("active-cas-phase.txt"), "reserved-authenticated-before-monitor", StandardCharsets.UTF_8);
                    acquireMonitor.countDown();
                    if (!monitorHeld.await(10L, TimeUnit.SECONDS)) throw new AssertionError("Original publication monitor was not held");
                    Files.writeString(releaseFile, "release", StandardCharsets.UTF_8);
                    Files.writeString(Path.of("active-cas-phase.txt"), "external-lock-released", StandardCharsets.UTF_8);
                    Object successor;
                    ThreadInfo info;
                    while (true) {
                        successor = readRecord.invoke(store, lineagePath, owner, weapon);
                        info = threadBean.getThreadInfo(serverThread.threadId(), 64);
                        boolean cas = Boolean.TRUE.equals(read(successor, "active"))
                            && epoch.equals(read(successor, "incarnation"))
                            && ((Number)read(successor, "generation")).longValue() == savedGeneration + 1L
                            && load.equals(read(successor, "transaction"));
                        boolean publicationBlocked = info != null && info.getThreadState() == Thread.State.BLOCKED
                            && info.getLockOwnerId() == holder.threadId()
                            && Arrays.stream(info.getStackTrace()).anyMatch(frame -> frame.getClassName().equals("net.farmingtales.ftboss.item.OwnerRepairGenerationAuthority")
                                && frame.getMethodName().equals("publishNativeReload"));
                        if (cas && publicationBlocked) break;
                        if (System.nanoTime() > deadline || readerFailure.get() != null) throw new AssertionError("Original active CAS / blocked physical publication cut was missed");
                        Thread.sleep(5L);
                    }
                    Files.writeString(Path.of("active-cas-phase.txt"), "active-cas-and-blocked-publication-observed", StandardCharsets.UTF_8);
                    if (!Arrays.equals(reservedDocumentBytes, Files.readAllBytes(documentPath)))
                        throw new AssertionError("Authenticated reservation bytes changed before verified crash cut");
                    if (node != null && (read(node, "lease") != null || read(node, "generation") != null))
                        throw new AssertionError("Physical paid authority already published or original loaded item absent");
                    if (!Arrays.equals(originalPlayer, Files.readAllBytes(playerFile))) throw new AssertionError("Crash cut changed original saved player bytes");
                    Map<String, Object> cut = new LinkedHashMap<>();
                    cut.put("failures", 0); cut.put("plannedExitCode", 86);
                    cut.put("originalReservationAuthenticated", true); cut.put("originalActivePredecessorCasActuallyPersisted", true);
                    cut.put("owner", owner.toString()); cut.put("weapon", weapon.toString()); cut.put("epoch", epoch.toString());
                    cut.put("predecessorGeneration", savedGeneration); cut.put("successorGeneration", read(successor, "generation"));
                    cut.put("reservedLoadEqualsActiveSuccessorTransaction", load.toString());
                    cut.put("continuationStillReservedAndUnconsumed", true); cut.put("actualLoadedItemHasNoPhysicalLeaseOrGeneration", true);
                    cut.put("serverThreadBlockedInOriginalPublishNativeReload", true); cut.put("serverThreadId", serverThread.threadId());
                    cut.put("monitorHolderThreadId", holder.threadId()); cut.put("observedLockOwnerId", info.getLockOwnerId()); cut.put("observedLockName", info.getLockName());
                    cut.put("serverStack", Arrays.stream(info.getStackTrace()).limit(14L).map(StackTraceElement::toString).toList());
                    cut.put("originalSavedPlayerSha256", savedPlayerHash); cut.put("authenticatedReservedDocument", reservedDocumentText);
                    cut.put("authenticatedReservedDocumentBytesUnchangedAtCut", true);
                    cut.put("authenticatedReservedDocumentSha256", digest(reservedDocumentBytes));
                    cut.put("authorityWrittenByReflection", false); cut.put("normalStopOrWorldSaveClaimed", false);
                    cut.put("installable", false); cut.put("authenticatedMultiplayerAccepted", false);
                    forceReceipt(Path.of("active-cas-crash-cut.json"), cut);
                    Runtime.getRuntime().halt(86);
                } catch (Throwable failure) {
                    readerFailure.compareAndSet(null, failure);
                } finally {
                    releaseMonitor.countDown();
                    try { Files.writeString(releaseFile, "release", StandardCharsets.UTF_8); }
                    catch (Exception failure) { readerFailure.compareAndSet(null, failure); }
                }
            }, "ftboss-crash-cut-authenticated-reader");
            reader.setDaemon(true);
            reader.start();
            storage.load(player);
            throw new AssertionError("Original load returned instead of reaching deliberate verified halt", readerFailure.get());
        } catch (Throwable failure) {
            failureReport.put("failures", 1); failureReport.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); failureReport.put("trace", trace.toString());
            Throwable observed = readerFailure.get();
            if (observed != null) failureReport.put("readerFailure", observed.toString());
        } finally {
            acquireMonitor.countDown(); releaseMonitor.countDown();
            try { Files.writeString(releaseFile, "release", StandardCharsets.UTF_8); }
            catch (Exception failure) { failureReport.put("releaseFailure", failure.toString()); }
        }
        failureReport.put("authorityWrittenByReflection", false); failureReport.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(failureReport), StandardCharsets.UTF_8); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
