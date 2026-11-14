package net.farmingtales.ftboss.item;

import java.io.IOException;
import java.lang.StackWalker.Option;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.stream.Stream;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Request;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.saveddata.SavedData;

public final class OwnerStatueTaskService {
    private static final UUID CUTOVER_OWNER = new UUID(0L, 0L);
    private static final String POLICY = "EXPLICIT_HUMAN_POLICY_MIGRATION_CREDIT_NOT_VERIFIED_PAYMENT";
    private static final Map<MinecraftServer, OwnerStatueTaskService> SERVERS = new WeakHashMap<>();
    private final MinecraftServer server;
    private final OwnerLineageRevocationStore store;
    private final OwnerStatueTaskSavedData savedData;
    private final CompoundTag cutover;
    private final Map<ServerPlayer, IOException> unavailable = new IdentityHashMap<>();

    private OwnerStatueTaskService(MinecraftServer var1) throws IOException {
        this.server = var1;
        this.store = OwnerLineageRevocationStore.open(var1);
        this.savedData = (OwnerStatueTaskSavedData)var1.overworld()
            .getDataStorage()
            .computeIfAbsent(new SavedData.Factory<>(OwnerStatueTaskSavedData::new, (snapshot, registries) -> OwnerStatueTaskSavedData.load(snapshot), null), "ftboss_repair_tasks");
        CompoundTag var2 = this.store.readRepairDocument(CUTOVER_OWNER, true);
        if (var2.isEmpty()) {
            if (this.savedData.hadCutover()) {
                throw new IOException("Missing signed repair cutover; do not re-import current NBT");
            }

            CompoundTag var3 = this.captureLegacyTasks();
            this.store.writeRepairDocument(CUTOVER_OWNER, true, var2, var3);
            var2 = var3;
        }

        if (var2.getInt("Schema") == 1
            && var2.hasUUID("Cutover")
            && "EXPLICIT_HUMAN_POLICY_MIGRATION_CREDIT_NOT_VERIFIED_PAYMENT".equals(var2.getString("Policy"))) {
            this.cutover = var2.copy();
            this.savedData.cutover(this.cutover);
        } else {
            throw new IOException("Unknown repair migration cutover");
        }
    }

    public static void start(MinecraftServer var0) {
        if (!var0.isSameThread()) {
            throw new IllegalStateException("Repair task startup off original server thread");
        } else {
            try {
                OwnerStatueTaskService var1 = new OwnerStatueTaskService(var0);
                synchronized (SERVERS) {
                    if (SERVERS.putIfAbsent(var0, var1) != null) {
                        throw new IllegalStateException("Repair task service already bound");
                    }
                }
            } catch (IOException var5) {
                throw new IllegalStateException("Cannot freeze existing legacy repair tasks", var5);
            }
        }
    }

    public static void stop(MinecraftServer var0) {
        synchronized (SERVERS) {
            SERVERS.remove(var0);
        }
    }

    private static OwnerStatueTaskService service(ServerPlayer var0) {
        OwnerStatueTaskService var1;
        synchronized (SERVERS) {
            var1 = SERVERS.get(var0.getServer());
        }

        if (var1 != null && var1.server.isSameThread() && var0.getServer() == var1.server) {
            return var1;
        } else {
            throw new IllegalStateException("Repair task service has no original server binding");
        }
    }

    public static void loaded(ServerPlayer var0) {
        OwnerStatueTaskService var1 = service(var0);

        try {
            CompoundTag var2 = var1.owner(var0.getUUID());
            if (var2.isEmpty()) {
                CompoundTag var3 = var1.cutover.getCompound("Owners").getCompound(var0.getUUID().toString());
                if (var3.isEmpty()) {
                    return;
                }

                var2 = var1.initializeOwner(var0.getUUID(), var2, var3);
            }

            var1.finishOwnerInitialization(var0.getUUID(), var2);
            var1.savedData.owner(var0.getUUID(), var2);
            if (var2.contains("Journal", 10)) {
                var1.recoverLoadedPlayer(var0, var2);
            }
        } catch (IOException var4) {
            throw unavailable(var0, var4);
        } catch (LinkageError | RuntimeException var5) {
            throw unavailable(var0, new IOException("Repair task load or recovery failed", var5));
        }
    }

    public static void unloaded(ServerPlayer var0) {
        synchronized (SERVERS) {
            OwnerStatueTaskService var2 = SERVERS.get(var0.getServer());
            if (var2 != null) {
                var2.unavailable.remove(var0);
            }
        }
    }

    static RuntimeException unavailable(ServerPlayer var0, IOException var1) {
        OwnerStatueTaskService var2 = service(var0);
        var2.unavailable.put(var0, var1);

        try {
            var0.connection.disconnect(Component.literal("Repair task storage requires reconnect reconciliation."));
        } catch (RuntimeException var4) {
            var1.addSuppressed(var4);
        }

        return new IllegalStateException("Repair task requires durable reconciliation", var1);
    }

    private CompoundTag initializeOwner(UUID var1, CompoundTag var2, CompoundTag var3) throws IOException {
        CompoundTag var4 = this.store.readRepairDocument(CUTOVER_OWNER, true);
        CompoundTag var5 = var4.getCompound("BoundOwners").getCompound(var1.toString());
        CompoundTag var6;
        if (!var5.isEmpty()) {
            if (!var5.getString("Phase").equals("CREATING") || !var5.contains("Initial", 10)) {
                throw new IOException("Missing previously initialized owner journal; never reset migration credit");
            }

            var6 = var5.getCompound("Initial").copy();
        } else {
            var6 = new CompoundTag();
            var6.putInt("Schema", 1);
            var6.putUUID("Owner", var1);
            var6.putUUID("Document", UUID.randomUUID());
            var6.putUUID("Cutover", this.cutover.getUUID("Cutover"));
            var6.put("Tasks", var3.copy());
            var6.put("Requests", new CompoundTag());
            CompoundTag var7 = var4.copy();
            CompoundTag var8 = var7.contains("BoundOwners", 10) ? var7.getCompound("BoundOwners") : new CompoundTag();
            var5 = new CompoundTag();
            var5.putString("Phase", "CREATING");
            var5.putUUID("Document", var6.getUUID("Document"));
            var5.put("Initial", var6.copy());
            var8.put(var1.toString(), var5);
            var7.put("BoundOwners", var8);
            this.store.writeRepairDocument(CUTOVER_OWNER, true, var4, var7);
            this.savedData.cutover(var7);
        }

        this.writeOwner(var1, var2, var6);
        return var6;
    }

    private void finishOwnerInitialization(UUID var1, CompoundTag var2) throws IOException {
        CompoundTag var3 = this.store.readRepairDocument(CUTOVER_OWNER, true);
        CompoundTag var4 = var3.getCompound("BoundOwners").getCompound(var1.toString());
        if (!var4.hasUUID("Document") || !var2.hasUUID("Document") || !var4.getUUID("Document").equals(var2.getUUID("Document"))) {
            throw new IOException("Repair owner document incarnation mismatch");
        } else if (!var4.getString("Phase").equals("CREATED")) {
            if (!var4.getString("Phase").equals("CREATING")) {
                throw new IOException("Unknown repair owner initialization phase");
            } else {
                CompoundTag var5 = var3.copy();
                CompoundTag var6 = var4.copy();
                var6.putString("Phase", "CREATED");
                var6.remove("Initial");
                var5.getCompound("BoundOwners").put(var1.toString(), var6);
                this.store.writeRepairDocument(CUTOVER_OWNER, true, var3, var5);
                this.savedData.cutover(var5);
            }
        }
    }

    private void writeOwner(UUID var1, CompoundTag var2, CompoundTag var3) throws IOException {
        this.store.writeRepairDocument(var1, false, var2, var3);
        this.savedData.owner(var1, var3);
    }

    private void checkpointSavedData(UUID var1, CompoundTag var2) throws IOException {
        this.server.overworld().getDataStorage().save();
        Path var3 = this.server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("ftboss_repair_tasks.dat");
        if (!Files.isSymbolicLink(var3) && Files.isRegularFile(var3)) {
            CompoundTag var4 = NbtIo.readCompressed(var3, NbtAccounter.unlimitedHeap());
            if (var4 == null || !var2.equals(var4.getCompound("data").getCompound("Owners").getCompound(var1.toString()))) {
                throw new IOException("Repair SavedData checkpoint readback mismatch");
            }
        } else {
            throw new IOException("Repair SavedData checkpoint unavailable");
        }
    }

    private void checkpointPlayer(ServerPlayer var1, ListTag var2) throws IOException {
        this.server.getPlayerList().saveAll();
        Path var3 = this.server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(var1.getUUID() + ".dat");
        if (!Files.isSymbolicLink(var3) && Files.isRegularFile(var3)) {
            CompoundTag var4 = NbtIo.readCompressed(var3, NbtAccounter.unlimitedHeap());
            if (!var2.equals(var1.getInventory().save(new ListTag())) || var4 == null || !var2.equals(var4.getList("Inventory", 10))) {
                throw new IOException("Repair player checkpoint readback mismatch");
            }
        } else {
            throw new IOException("Repair player checkpoint unavailable");
        }
    }

    private void recoverLoadedPlayer(ServerPlayer var1, CompoundTag var2) throws IOException {
        CompoundTag var3 = var2.getCompound("Journal");
        String var4 = var3.getString("Phase");
        boolean var5 = var4.equals("DECIDED");
        if (!var5 && !var4.equals("PREPARED") && !var4.equals("ABORTING")) {
            throw new IOException("Unknown repair recovery phase");
        } else {
            CompoundTag var6 = var3.getCompound(var5 ? "AfterTask" : "BeforeTask");
            if (var3.hasUUID("Transaction")
                && var3.hasUUID("Task")
                && var3.hasUUID("Weapon")
                && var6.hasUUID("Task")
                && var6.getUUID("Task").equals(var3.getUUID("Task"))
                && var6.getUUID("Owner").equals(var1.getUUID())
                && var6.getUUID("Weapon").equals(var3.getUUID("Weapon"))) {
                ListTag var7 = var3.getList(var5 ? "AfterInventory" : "BeforeInventory", 10);
                if (var7.isEmpty()) {
                    throw new IOException("Missing repair recovery inventory");
                } else {
                    ListTag var8 = var1.getInventory().save(new ListTag());
                    if (var8.equals(var3.getList("BeforeInventory", 10))
                        || var3.contains("AfterInventory", 9) && var8.equals(var3.getList("AfterInventory", 10))) {
                        if (!var8.equals(var7)) {
                            var1.getInventory().load(var7.copy());
                            var1.getInventory().setChanged();
                        }

                        this.checkpointPlayer(var1, var7);
                        CompoundTag var9 = var2.copy();
                        CompoundTag var10 = var6.copy();
                        var10.putString("State", var5 ? "RECOVERED_COMMIT" : "RECOVERED_ABORT_NO_CREDIT");
                        if (var3.getBoolean("TaskAbsentBefore") && !var5) {
                            var9.getCompound("Tasks").remove(var3.getUUID("Weapon").toString());
                        } else {
                            var9.getCompound("Tasks").put(var3.getUUID("Weapon").toString(), var10);
                        }

                        if (var3.getBoolean("TaskAbsentBefore")) {
                            settleStartReceipt(var9, var3, var5);
                        }

                        if (var3.hasUUID("Request")) {
                            var9.getCompound("Requests")
                                .putString(var3.getUUID("Request").toString(), (var5 ? "COMMITTED:" : "ABORTED:") + var3.getUUID("Transaction"));
                        }

                        var9.remove("Journal");
                        this.writeOwner(var1.getUUID(), var2, var9);
                        this.checkpointSavedData(var1.getUUID(), var9);
                    } else {
                        throw new IOException("Repair recovery conflict; do not replace an unrelated current inventory");
                    }
                }
            } else {
                throw new IOException("Repair recovery task binding mismatch");
            }
        }
    }

    private CompoundTag owner(UUID var1) throws IOException {
        CompoundTag var2 = this.store.readRepairDocument(var1, false);
        if (var2.isEmpty()
            || var2.getInt("Schema") == 1
                && var2.hasUUID("Owner")
                && var1.equals(var2.getUUID("Owner"))
                && var2.hasUUID("Cutover")
                && this.cutover.getUUID("Cutover").equals(var2.getUUID("Cutover"))) {
            return var2;
        } else {
            throw new IOException("Repair task owner or cutover mismatch");
        }
    }

    private CompoundTag captureLegacyTasks() throws IOException {
        CompoundTag var1 = new CompoundTag();
        var1.putInt("Schema", 1);
        var1.putUUID("Cutover", UUID.randomUUID());
        var1.putString("Policy", "EXPLICIT_HUMAN_POLICY_MIGRATION_CREDIT_NOT_VERIFIED_PAYMENT");
        var1.putString("CaptureScope", "ORIGINAL_SERVER_PLAYER_INVENTORIES_AT_UPGRADE");
        CompoundTag var2 = new CompoundTag();
        HashMap<UUID, List<CompoundTag>> var3 = new HashMap<>();
        HashSet var4 = new HashSet();
        Path var5 = this.server.getWorldPath(LevelResource.PLAYER_DATA_DIR).toAbsolutePath().normalize();
        if (Files.isSymbolicLink(var5)) {
            throw new IOException("Repair migration player directory is a link");
        } else {
            if (Files.isDirectory(var5)) {
                List<Path> var6;
                try (Stream<Path> var7 = Files.list(var5)) {
                    var6 = var7.filter(
                            var0 -> var0.getFileName().toString().matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.dat")
                        )
                        .sorted()
                        .toList();
                }

                for (Path var8 : var6) {
                    if (Files.isSymbolicLink(var8) || !Files.isRegularFile(var8) || Files.size(var8) > 8388608L) {
                        throw new IOException("Repair migration player input budget");
                    }

                    UUID var9 = UUID.fromString(var8.getFileName().toString().substring(0, 36));
                    CompoundTag var10 = NbtIo.readCompressed(var8, NbtAccounter.unlimitedHeap());
                    if (var10 == null) {
                        throw new IOException("Missing repair migration player input");
                    }

                    CompoundTag var11 = new CompoundTag();
                    ListTag var12 = var10.getList("Inventory", 10);

                    for (int var13 = 0; var13 < var12.size(); var13++) {
                        CompoundTag var14 = var12.getCompound(var13);
                        ItemStack var15 = ItemStack.parseOptional(this.server.registryAccess(), var14.copy());

                        CompoundTag var16;
                        try {
                            var16 = OwnerStatueTaskPlan.legacyQuest(var15);
                        } catch (IllegalArgumentException var23) {
                            continue;
                        }

                        CompoundTag var17 = (CompoundTag)var15.saveOptional(this.server.registryAccess());
                        if (OwnerRepairGenerationAuthority.statueTaskObjectPolicy(var15)) {
                            UUID var18 = FoodWeaponLifecycle.weaponId(var15);
                            CompoundTag var19 = new CompoundTag();

                            UUID var20;
                            do {
                                var20 = UUID.randomUUID();
                            } while (!var4.add(var20));

                            var19.putUUID("Task", var20);
                            var19.putUUID("Owner", var9);
                            var19.putUUID("Weapon", var18);
                            var19.putUUID("Cutover", var1.getUUID("Cutover"));
                            var19.putString("Policy", "EXPLICIT_HUMAN_POLICY_MIGRATION_CREDIT_NOT_VERIFIED_PAYMENT");
                            var19.putInt("PolicyVersion", 1);
                            var19.put("OriginalQuest", var16);
                            var19.put("Expected", var17);
                            var19.putInt("Slot", var14.getByte("Slot"));
                            var19.putString("Rules", OwnerStatueTaskPlan.rules(var15));
                            var19.putIntArray("PendingPolicyCredit", var16.getIntArray("Progress"));
                            var19.putIntArray("PolicyCredit", new int[3]);
                            var19.putIntArray("ObservedCommittedPayments", new int[3]);
                            var19.putLong("Sequence", 0L);
                            var19.putString("State", "FROZEN_ELIGIBILITY_ONLY_NO_CREDIT");
                            var11.put(var18.toString(), var19);
                            var3.computeIfAbsent(var18, var0 -> new ArrayList<>()).add(var19);
                        }
                    }

                    if (!var11.isEmpty()) {
                        var2.put(var9.toString(), var11);
                    }
                }
            }

            for (List<CompoundTag> var26 : var3.values()) {
                if (var26.size() > 1) {
                    for (CompoundTag var28 : var26) {
                        var28.putBoolean("AmbiguousCopy", true);
                    }
                }
            }

            var1.put("Owners", var2);
            return var1;
        }
    }

    static void reservePrewriteTrack(OwnerRepairContinuation.Track var0) throws IOException {
        boolean var1 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerRepairContinuation.class && var0xx.getMethodName().equals("prewrite"))
                        .orElse(false)
            );
        if (!var1) {
            throw new IOException("Detached prewrite owner consumer");
        } else {
            ServerPlayer var2 = var0.ownerPlayer();
            OwnerStatueTaskService var3 = service(var2);
            if (var3.unavailable.containsKey(var2)) {
                throw new IOException("Prewrite owner unavailable");
            } else {
                CompoundTag var4 = var0.ownerRecord();
                UUID var5 = var4.getUUID("Owner");
                UUID var6 = var4.getUUID("Weapon");
                UUID var7 = var4.getUUID("Track");
                CompoundTag var8 = var3.owner(var5);
                CompoundTag var9 = var8.getCompound("Tasks").getCompound(var6.toString());
                CompoundTag var10 = var8.getCompound("ValidatedOriginalWear").getCompound(var4.getUUID("OriginalCall").toString());
                if (!var8.isEmpty()
                    && !var8.contains("Journal")
                    && var9.getBoolean("Consumed")
                    && (!var8.contains("WearHeads") || var8.contains("WearHeads", 10))
                    && (!var8.contains("WearTracks") || var8.contains("WearTracks", 10))
                    && var9.hasUUID("AcceptedWearCall")
                    && var9.getUUID("AcceptedWearCall").equals(var4.getUUID("OriginalCall"))
                    && var10.hasUUID("OriginalLoad")
                    && var10.getUUID("OriginalLoad").equals(var4.getUUID("OriginalLoad"))
                    && var9.hasUUID("Owner")
                    && var9.getUUID("Owner").equals(var5)
                    && var9.hasUUID("Weapon")
                    && var9.getUUID("Weapon").equals(var6)
                    && var9.hasUUID("Task")
                    && var9.getUUID("Task").equals(var4.getUUID("ParentTask"))
                    && var9.hasUUID("LastTransaction")
                    && var9.getUUID("LastTransaction").equals(var4.getUUID("ParentClaim"))
                    && var9.hasUUID("GenerationClaim")
                    && var9.getUUID("GenerationClaim").equals(var4.getUUID("ParentClaim"))
                    && var9.getString("State").equals("CHECKPOINTED_COMMIT")
                    && var9.getString("GenerationDisposition").equals("CONSUMED_NO_REISSUE_OR_RECOVERY_PUBLICATION")
                    && var9.getLong("Sequence") == var4.getLong("TaskSequence")
                    && var9.getString("Rules").equals(var4.getString("Rules"))
                    && var9.getCompound("Expected").equals(var4.getCompound("Expected"))
                    && !var8.getCompound("WearHeads").contains(var6.toString())
                    && !var8.getCompound("WearTracks").contains(var7.toString())
                    && var8.getCompound("WearTracks").getAllKeys().size() < 256) {
                    CompoundTag var11 = var4.copy();
                    var11.putString("Phase", "PREPARING");
                    CompoundTag var12 = var8.copy();
                    CompoundTag var13 = var12.getCompound("WearTracks").copy();
                    var13.put(var7.toString(), var11);
                    var12.put("WearTracks", var13);
                    CompoundTag var14 = var12.getCompound("WearHeads").copy();
                    var14.putUUID(var6.toString(), var7);
                    var12.put("WearHeads", var14);
                    var3.writeOwner(var5, var8, var12);
                    var3.checkpointSavedData(var5, var12);
                    var0.ownerPrepared(var3.store);
                    var3.store.consumePrewriteTrack(var0);
                    if (!var3.owner(var5).equals(var12)) {
                        throw new IOException("Prewrite owner CAS changed after lineage consumption");
                    } else {
                        CompoundTag var15 = var12.copy();
                        var15.getCompound("WearTracks").getCompound(var7.toString()).putString("Phase", "TRACK_RESERVED");
                        var3.writeOwner(var5, var12, var15);
                        var3.checkpointSavedData(var5, var15);
                        var0.reserved();
                    }
                } else {
                    throw new IOException("Prewrite owner predecessor mismatch or already reserved");
                }
            }
        }
    }

    static boolean originalWearPlanUnused(ServerPlayer var0, UUID var1) throws IOException {
        boolean var2 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerStatueWearAdmission.class && var0xx.getMethodName().equals("entered"))
                        .orElse(false)
            );
        if (var2 && var1 != null) {
            OwnerStatueTaskService var3 = service(var0);
            if (var3.unavailable.containsKey(var0)) {
                return false;
            } else {
                CompoundTag var4 = var3.owner(var0.getUUID());
                if (!var4.isEmpty()) {
                    if (!var4.contains("Journal") && var4.getCompound("Tasks").getCompound(var1.toString()).isEmpty()) {
                        CompoundTag var5 = var4.getCompound("OriginalWearEvidence");

                        for (String var7 : var5.getAllKeys()) {
                            CompoundTag var8 = NativeRepairStackImage.data(var5.getCompound(var7).getCompound("AcceptedOriginalLoadBefore"))
                                .getCompound("FTBossWeaponLifecycle");
                            if (var8.hasUUID("Weapon") && var1.equals(var8.getUUID("Weapon"))) {
                                return false;
                            }
                        }

                        for (String var10 : var4.getCompound("ValidatedOriginalWear").getAllKeys()) {
                            CompoundTag var11 = var4.getCompound("ValidatedOriginalWear").getCompound(var10);
                            if (var11.hasUUID("Weapon") && var1.equals(var11.getUUID("Weapon"))) {
                                return false;
                            }
                        }

                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return var3.cutover.getCompound("BoundOwners").getCompound(var0.getUUID().toString()).isEmpty()
                        && var3.cutover.getCompound("Owners").getCompound(var0.getUUID().toString()).getCompound(var1.toString()).isEmpty();
                }
            }
        } else {
            return false;
        }
    }

    static void requireOriginalWearNotRetired(ServerPlayer var0, UUID var1) throws IOException {
        OwnerStatueTaskService var2 = service(var0);
        if (var1 == null || var2.unavailable.containsKey(var0) || var2.store.rejectsKnownLineage(var0.getUUID(), var1)) {
            throw new IOException("Original wear has missing identity, unavailable owner, or known inactive lineage; never fresh issue");
        }
    }

    static void recordOriginalWear(OwnerStatueWearAdmission.Terminal var0) throws IOException {
        if (var0 == null) {
            throw new IOException("Missing original wear terminal");
        } else {
            ServerPlayer var1 = var0.player();
            OwnerStatueTaskService var2 = service(var1);
            if (var2.unavailable.containsKey(var1)) {
                throw new IOException("Original wear owner requires reconciliation", var2.unavailable.get(var1));
            } else {
                CompoundTag var3 = var0.take();
                if (var3.hasUUID("Owner")
                    && var1.getUUID().equals(var3.getUUID("Owner"))
                    && var3.hasUUID("Call")
                    && var0.identity().equals(var3.getUUID("Call"))
                    && var3.getString("Authority").equals("ORIGINAL_LOAD_CUSTODY_BOUND_WEAR_EVIDENCE_ONLY_NOT_TASK_OR_GENERATION_AUTHORITY")) {
                    CompoundTag var4 = var2.owner(var1.getUUID());
                    if (var4.isEmpty()) {
                        var4 = var2.initializeOwner(var1.getUUID(), var4, var2.cutover.getCompound("Owners").getCompound(var1.getUUID().toString()));
                    }

                    var2.finishOwnerInitialization(var1.getUUID(), var4);
                    if (var4.contains("Journal")) {
                        throw new IOException("Do not append wear evidence over an unresolved repair journal");
                    } else {
                        CompoundTag var5 = var4.getCompound("OriginalWearEvidence");
                        String var6 = var0.identity().toString();
                        if (!var5.contains(var6) && var5.getAllKeys().size() < 256) {
                            CompoundTag var7 = var4.copy();
                            CompoundTag var8 = var5.copy();
                            var8.put(var6, var3);
                            var7.put("OriginalWearEvidence", var8);
                            OwnerStatueWearAdmission.Accepted var9 = OwnerStatueWearAdmission.validated(var0);
                            if (var9 != null) {
                                CompoundTag var10 = var7.getCompound("ValidatedOriginalWear").copy();
                                if (var10.contains(var6) || var10.getAllKeys().size() >= 256) {
                                    throw new IOException("Duplicate or full validated original wear obligations");
                                }

                                var10.put(var6, var9.record());
                                var7.put("ValidatedOriginalWear", var10);
                            }

                            var2.writeOwner(var1.getUUID(), var4, var7);
                            var2.checkpointSavedData(var1.getUUID(), var7);
                            if (var9 != null) {
                                OwnerStatueWearAdmission.activate(var9, var0);
                            }
                        } else {
                            throw new IOException("Duplicate or full original wear evidence journal; never evict obligations");
                        }
                    }
                } else {
                    throw new IOException("Original wear evidence differs from its physical invocation");
                }
            }
        }
    }

    static OwnerStatueTaskService.Prepared prepare(ServerPlayer var0, ItemStack var1, int var2, BlockPos var3, Operation var4, int var5, Request var6) throws IOException {
        if (var4 == Operation.START_REPAIR
            && !StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0x -> var0x.skip(1L)
                            .findFirst()
                            .map(var0xx -> var0xx.getDeclaringClass() == StatueRepairMutation.class && var0xx.getMethodName().equals("commit"))
                            .orElse(false)
                )) {
            return null;
        } else {
            OwnerStatueTaskService var7 = service(var0);
            if (var7.unavailable.containsKey(var0)) {
                throw new IOException("Repair task owner unavailable", var7.unavailable.get(var0));
            } else if (var0.isAlive()
                && !var0.isSpectator()
                && !var0.isSleeping()
                && !var0.isUsingItem()
                && OwnerRepairGenerationAuthority.statueTaskObjectPolicy(var1)) {
                UUID var8 = FoodWeaponLifecycle.weaponId(var1);
                CompoundTag var9 = var7.owner(var0.getUUID());
                if (var8 == null || var9.isEmpty()) {
                    return null;
                } else if (var4 == Operation.START_REPAIR) {
                    return prepareStart(var7, var0, var1, var2, var3, var5, var6, var9, var8);
                } else {
                    CompoundTag var10 = var9.getCompound("Tasks").getCompound(var8.toString());
                    if (var10.isEmpty() || var10.getBoolean("Consumed") || var10.getBoolean("AmbiguousCopy")) {
                        return null;
                    } else if (var9.contains("Journal")) {
                        throw new IOException("Repair task requires original recovery/checkpoint consumer");
                    } else if (var6 != null && var9.getCompound("Requests").contains(var6.request().toString())) {
                        throw new IllegalStateException("Repair request already has durable outcome");
                    } else if (var2 >= 0
                        && var2 < var0.getInventory().getContainerSize()
                        && var0.getInventory().getItem(var2) == var1
                        && (var0.getMainHandItem() == var1 || var0.getOffhandItem() == var1)
                        && (var2 == 40 || var0.getInventory().selected == var2)
                        && var1.getCount() == 1
                        && var10.getCompound("Expected").equals(var1.saveOptional(var0.registryAccess()))
                        && var10.getString("Rules").equals(OwnerStatueTaskPlan.rules(var1))) {
                        if (var6 == null
                            || var6.binding().owner().equals(var0.getUUID())
                                && var6.binding().weapon().equals(var8)
                                && var6.binding().inventorySlot() == var2
                                && var6.binding().statue() == var3.asLong()
                                && var6.binding().dimension().equals(var0.level().dimension().location().toString())
                                && var6.operation() == var4) {
                            int var11 = 0;

                            for (int var12 = 0; var12 < var0.getInventory().getContainerSize(); var12++) {
                                if (var8.equals(FoodWeaponLifecycle.weaponId(var0.getInventory().getItem(var12)))) {
                                    var11++;
                                }
                            }

                            if (var11 == 1 && var0.level().hasChunkAt(var3) && var0.level().getBlockState(var3).getBlock() instanceof CandyStatueBlock var15) {
                                FoodWeaponRepair.View var16 = FoodWeaponRepair.view(var1, var15.ownTint());
                                if (!var16.eligible() || !var16.correctStatue() || !var16.active()) {
                                    return null;
                                } else if (var4 == Operation.CLAIM_REPAIR && !var16.ready()) {
                                    return null;
                                } else if (var4 == Operation.SUBMIT_REPAIR && (var5 < 0 || var5 >= 3 || var16.tasks().get(var5).complete())) {
                                    return null;
                                } else if (var4 != Operation.SUBMIT_REPAIR && var4 != Operation.CLAIM_REPAIR) {
                                    return null;
                                } else {
                                    OwnerStatueTaskService.Prepared var14 = new OwnerStatueTaskService.Prepared(
                                        var7, var0, var1, var2, var3, var15.ownTint(), var4, var5, var6, var9, var10
                                    );
                                    var14.origin = OwnerStatueTaskOrigin.find(var0, var1, var2, var10);
                                    return var14;
                                }
                            } else {
                                throw new IOException("Repair task duplicate or actual statue mismatch");
                            }
                        } else {
                            throw new IOException("Repair participant differs from original admitted request");
                        }
                    } else {
                        throw new IOException("Repair task input does not match frozen or committed record");
                    }
                }
            } else {
                return null;
            }
        }
    }

    private static void settleStartReceipt(CompoundTag var0, CompoundTag var1, boolean var2) throws IOException {
        if (!var1.hasUUID("AcceptedWearCall")) {
            throw new IOException("START has no independent original wear identity");
        } else {
            CompoundTag var3 = var0.getCompound("ValidatedOriginalWear").getCompound(var1.getUUID("AcceptedWearCall").toString());
            if (var3.hasUUID("Transaction")
                && var3.getUUID("Transaction").equals(var1.getUUID("Transaction"))
                && var3.hasUUID("Task")
                && var3.getUUID("Task").equals(var1.getUUID("Task"))
                && var3.getString("Status").equals("RESERVED_BY_ORIGINAL_START_NO_REISSUE")) {
                var3.putString("Status", var2 ? "CONSUMED_BY_ORIGINAL_START_TERMINAL" : "BURNED_BY_ORIGINAL_START_ABORT_NO_REISSUE");
            } else {
                throw new IOException("START wear consumption binding mismatch");
            }
        }
    }

    private static OwnerStatueTaskService.Prepared prepareStart(
        OwnerStatueTaskService var0, ServerPlayer var1, ItemStack var2, int var3, BlockPos var4, int var5, Request var6, CompoundTag var7, UUID var8
    ) throws IOException {
        if (var6 == null && var5 == 0 && !var7.contains("Journal") && var7.getCompound("Tasks").getCompound(var8.toString()).isEmpty()) {
            if (var3 >= 0
                && var3 < var1.getInventory().getContainerSize()
                && var1.getInventory().getItem(var3) == var2
                && (var3 == 40 ? var1.getOffhandItem() == var2 : var1.getInventory().selected == var3 && var1.getMainHandItem() == var2)
                && var2.getCount() == 1) {
                if (var6 == null
                    || !var7.getCompound("Requests").contains(var6.request().toString())
                        && var6.binding().owner().equals(var1.getUUID())
                        && var6.binding().weapon().equals(var8)
                        && var6.binding().inventorySlot() == var3
                        && var6.binding().statue() == var4.asLong()
                        && var6.binding().dimension().equals(var1.level().dimension().location().toString())
                        && var6.operation() == Operation.START_REPAIR) {
                    int var9 = 0;

                    for (int var10 = 0; var10 < var1.getInventory().getContainerSize(); var10++) {
                        if (var8.equals(FoodWeaponLifecycle.weaponId(var1.getInventory().getItem(var10)))) {
                            var9++;
                        }
                    }

                    if (var9 == 1 && var1.level().hasChunkAt(var4) && var1.level().getBlockState(var4).getBlock() instanceof CandyStatueBlock var19) {
                        FoodWeaponRepair.View var20 = FoodWeaponRepair.view(var2, var19.ownTint());
                        if (var20.eligible() && var20.correctStatue() && !var20.active()) {
                            OwnerStatueWearAdmission.Accepted var12 = OwnerStatueWearAdmission.takeForStart(var1, var2, var3);
                            if (var12 == null) {
                                return null;
                            } else {
                                CompoundTag var13 = var12.record();
                                CompoundTag var14 = var7.getCompound("ValidatedOriginalWear").getCompound(var13.getUUID("Call").toString());
                                if (!var13.equals(var14) || !var8.equals(var13.getUUID("Weapon")) || !var1.getUUID().equals(var13.getUUID("Owner"))) {
                                    throw new IOException("Live validated wear differs from its independent durable record");
                                } else if (var14.getBoolean("ReturnedNormally")
                                    && var14.getString("PrimaryClass").isEmpty()
                                    && !var14.getBoolean("OriginalRollbackEntered")
                                    && !var14.getBoolean("SwallowedOriginalRollbackReturned")) {
                                    CompoundTag var15 = new CompoundTag();
                                    var15.putUUID("Task", UUID.randomUUID());
                                    var15.putUUID("Owner", var1.getUUID());
                                    var15.putUUID("Weapon", var8);
                                    var15.putUUID("AcceptedWearCall", var13.getUUID("Call"));
                                    var15.put("Expected", var13.getCompound("Expected").copy());
                                    var15.putString("Rules", var13.getString("Rules"));
                                    var15.putInt("AcceptedLevel", var13.getInt("Level"));
                                    var15.putInt("AcceptedMaximum", var13.getInt("Maximum"));
                                    var15.putString("AcceptedProfile", var13.getString("Profile"));
                                    var15.putIntArray("PolicyCredit", new int[3]);
                                    var15.putIntArray("PendingPolicyCredit", new int[3]);
                                    var15.putIntArray("ObservedCommittedPayments", new int[3]);
                                    var15.putBoolean("MigrationCommitted", true);
                                    var15.putString("CreditClassification", "VALIDATED_ORIGINAL_WEAR_START_ZERO_CREDIT_NOT_LEGACY_MIGRATION");
                                    var15.putString("State", "START_PREPARATION_NOT_PUBLISHED_TASK");
                                    OwnerStatueTaskPlan.start(
                                        var15.getCompound("Expected"),
                                        var15.getInt("AcceptedLevel"),
                                        var15.getString("AcceptedProfile"),
                                        var15.getInt("AcceptedMaximum"),
                                        var19.ownTint(), var1.registryAccess()
                                    );
                                    OwnerStatueTaskService.Prepared var16 = new OwnerStatueTaskService.Prepared(
                                        var0, var1, var2, var3, var4, var19.ownTint(), Operation.START_REPAIR, var5, var6, var7, var15
                                    );

                                    try {
                                        var16.origin = OwnerStatueTaskOrigin.reserve(var12, var16, var0.store, var1, var2, var3, var4, var19.ownTint(), var15);
                                    } catch (Throwable var18) {
                                        var16.originFailed();
                                    }

                                    return var16;
                                } else {
                                    return null;
                                }
                            }
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    static final class Prepared {
        private final OwnerStatueTaskService service;
        private final ServerPlayer player;
        private final ItemStack weapon;
        private final int slot;
        private final BlockPos statue;
        private final BlockState statueState;
        private final ServerLevel level;
        private final int tint;
        private final Operation operation;
        private final boolean starting;
        private final int task;
        private final Request request;
        private final UUID transaction = UUID.randomUUID();
        private final Thread thread = Thread.currentThread();
        private final CompoundTag before;
        private final ListTag beforeInventory;
        private ListTag decidedInventory;
        private final Map<Integer, ItemStack> supplies = new HashMap<>();
        private final Set<ItemStack> originalSupplies = Collections.newSetFromMap(new IdentityHashMap<>());
        private CompoundTag document;
        private boolean closed;
        private OwnerStatueTaskOrigin.Session origin;

        private Prepared(
            OwnerStatueTaskService var1,
            ServerPlayer var2,
            ItemStack var3,
            int var4,
            BlockPos var5,
            int var6,
            Operation var7,
            int var8,
            Request var9,
            CompoundTag var10,
            CompoundTag var11
        ) throws IOException {
            this.service = var1;
            this.player = var2;
            this.weapon = var3;
            this.slot = var4;
            this.statue = var5.immutable();
            this.statueState = var2.level().getBlockState(var5);
            this.level = var2.serverLevel();
            this.tint = var6;
            this.operation = var7;
            this.starting = var7 == Operation.START_REPAIR;
            this.task = var8;
            this.request = var9;
            this.document = var10;
            this.before = var11.copy();
            long var12 = var11.getLong("Sequence");
            if (var12 >= 0L && var12 != Long.MAX_VALUE) {
                this.before.putLong("Sequence", var12 + 1L);
                this.before.putInt("Slot", var4 == 40 ? -106 : var4);
                this.beforeInventory = var2.getInventory().save(new ListTag()).copy();
                var1.checkpointPlayer(var2, this.beforeInventory);
                CompoundTag var14 = new CompoundTag();
                var14.putUUID("Transaction", this.transaction);
                var14.putUUID("Task", var11.getUUID("Task"));
                var14.putUUID("Weapon", var11.getUUID("Weapon"));
                if (var9 != null) {
                    var14.putUUID("Request", var9.request());
                }

                var14.putString("Phase", "PREPARED");
                var14.putString("Operation", var7.name());
                var14.putBoolean("TaskAbsentBefore", this.starting);
                if (this.starting) {
                    var14.putUUID("AcceptedWearCall", this.before.getUUID("AcceptedWearCall"));
                }

                var14.putString("Dimension", var2.level().dimension().location().toString());
                var14.putLong("Statue", var5.asLong());
                var14.putInt("Tint", var6);
                var14.putInt("TaskIndex", var8);
                var14.put("BeforeTask", this.before.copy());
                var14.put("BeforeInventory", this.beforeInventory.copy());
                var14.put("Payments", new ListTag());
                this.transition(var14);
            } else {
                throw new IOException("Repair task sequence exhausted or invalid");
            }
        }

        void beforeOriginal(StatueRepairMutation var1) {
            try {
                OwnerStatueTaskOrigin.begin(this.origin, this, var1, this.operation, this.task, this.before, this.transaction);
            } catch (Throwable var3) {
                this.originFailed();
            }
        }

        void originFailed() {
            try {
                OwnerStatueTaskOrigin.burn(this.origin);
            } catch (Throwable var2) {
            }
        }

        void afterOuterCommitted(StatueRepairMutation.Committed var1) {
            try {
                OwnerStatueTaskOrigin.outerCommitted(this.origin, this, var1);
            } catch (Throwable var3) {
                this.originFailed();
            }
        }

        CompoundTag originOutcome(StatueRepairMutation.Committed var1) throws IOException {
            if (this.closed
                && var1.remains(this)
                && !this.document.contains("Journal")
                && this.decidedInventory != null
                && this.decidedInventory.equals(this.player.getInventory().save(new ListTag()))) {
                CompoundTag var2 = this.document.getCompound("Tasks").getCompound(this.before.getUUID("Weapon").toString());
                if (var2.getString("State").equals("CHECKPOINTED_COMMIT")
                    && this.transaction.equals(var2.getUUID("LastTransaction"))
                    && this.before.getUUID("Task").equals(var2.getUUID("Task"))
                    && this.before.getUUID("Owner").equals(this.player.getUUID())
                    && this.before.getUUID("Weapon").equals(FoodWeaponLifecycle.weaponId(this.weapon))) {
                    return var2.copy();
                } else {
                    throw new IOException("Task origin checkpoint binding mismatch");
                }
            } else {
                throw new IOException("Task origin has no closed normal original checkpoint");
            }
        }

        void consumeOriginGeneration(OwnerStatueTaskOrigin.Publication var1, boolean var2) throws IOException {
            if (this.closed
                && var1.belongs(this)
                && this.origin != null
                && this.operation == Operation.CLAIM_REPAIR
                && !this.document.contains("Journal")
                && this.decidedInventory.equals(this.player.getInventory().save(new ListTag()))) {
                CompoundTag var3 = this.service.owner(this.player.getUUID());
                if (!var3.equals(this.document)) {
                    throw new IOException("Task generation owner CAS conflict");
                } else {
                    CompoundTag var4 = this.document.copy();
                    CompoundTag var5 = var4.getCompound("Tasks").getCompound(this.before.getUUID("Weapon").toString());
                    if (var5.getBoolean("Consumed")
                        && this.transaction.equals(var5.getUUID("LastTransaction"))
                        && this.before.getUUID("Task").equals(var5.getUUID("Task"))) {
                        String var6 = var2 ? "CONSUMING_NO_RECOVERY_PUBLICATION" : "";
                        if (!var5.getString("GenerationDisposition").equals(var6)) {
                            throw new IOException("Task generation already consumed or uncertain");
                        } else {
                            var5.putString("GenerationDisposition", var2 ? "CONSUMED_NO_REISSUE_OR_RECOVERY_PUBLICATION" : "CONSUMING_NO_RECOVERY_PUBLICATION");
                            var5.putUUID("GenerationClaim", this.transaction);
                            this.service.writeOwner(this.player.getUUID(), this.document, var4);
                            this.document = var4;
                            this.service.checkpointSavedData(this.player.getUUID(), var4);
                        }
                    } else {
                        throw new IOException("Task generation has no original consumed claim");
                    }
                }
            } else {
                throw new IOException("Detached task generation durable consumer");
            }
        }

        private void check() throws IOException {
            if (this.starting
                && !StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                    .walk(
                        var0 -> var0.filter(var0x -> var0x.getDeclaringClass().getNestHost() != OwnerStatueTaskService.class)
                                .findFirst()
                                .map(
                                    var0x -> var0x.getDeclaringClass() == StatueRepairMutation.class
                                            && (var0x.getMethodName().equals("terminal") || var0x.getMethodName().equals("rollback"))
                                )
                                .orElse(false)
                    )) {
                throw new IOException("Detached START terminal participant");
            } else if (this.closed
                || Thread.currentThread() != this.thread
                || !this.service.server.isSameThread()
                || this.player.getServer() != this.service.server
                || this.player.serverLevel() != this.level
                || this.player.getInventory().getItem(this.slot) != this.weapon) {
                throw new IOException("Repair task participant source changed");
            }
        }

        private void transition(CompoundTag var1) throws IOException {
            CompoundTag var2 = this.document.copy();
            var2.put("Journal", var1.copy());
            if (this.starting && !this.document.contains("Journal")) {
                String var3 = this.before.getUUID("AcceptedWearCall").toString();
                CompoundTag var4 = var2.getCompound("ValidatedOriginalWear").getCompound(var3);
                if (!var4.getString("Status").equals("AVAILABLE_LIVE_IDENTITY_ONLY")
                    || !this.before.getCompound("Expected").equals(var4.getCompound("Expected"))) {
                    throw new IOException("Validated wear START reservation conflict");
                }

                var4.putString("Status", "RESERVED_BY_ORIGINAL_START_NO_REISSUE");
                var4.putUUID("Transaction", this.transaction);
                var4.putUUID("Task", this.before.getUUID("Task"));
            }

            this.service.writeOwner(this.player.getUUID(), this.document, var2);
            this.document = var2;
        }

        void beforeShrink(ItemStack var1, int var2, int var3) throws IOException {
            this.check();
            if (this.operation == Operation.SUBMIT_REPAIR
                && this.document.getCompound("Journal").getString("Phase").equals("PREPARED")
                && var2 >= 0
                && var2 < 36
                && var1 != this.weapon
                && this.player.getInventory().getItem(var2) == var1
                && var3 > 0
                && var3 <= var1.getCount()
                && !this.supplies.containsKey(var2)
                && this.originalSupplies.add(var1)) {
                CompoundTag var4 = this.document.getCompound("Journal").copy();
                CompoundTag var5 = new CompoundTag();
                var5.putInt("Slot", var2);
                var5.putInt("Count", var3);
                var5.putInt("Before", var1.getCount());
                var5.put("BeforeImage", var1.saveOptional(this.player.registryAccess()));
                var4.getList("Payments", 10).add(var5);
                this.transition(var4);
                this.supplies.put(var2, var1);
            } else {
                throw new IOException("Repair original payment identity or phase mismatch");
            }
        }

        void afterShrink(ItemStack var1, int var2, int var3) throws IOException {
            this.check();
            CompoundTag var4 = this.document.getCompound("Journal").copy();
            ListTag var5 = var4.getList("Payments", 10);
            if (var5.isEmpty()) {
                throw new IOException("Repair payment has no before observation");
            } else {
                CompoundTag var6 = var5.getCompound(var5.size() - 1);
                if (!var6.getBoolean("Returned")
                    && var6.getInt("Slot") == var2
                    && var6.getInt("Count") == var3
                    && this.supplies.get(var2) == var1
                    && this.player.getInventory().getItem(var2) == var1
                    && var1.getCount() == var6.getInt("Before") - var3) {
                    CompoundTag var7 = var6.getCompound("BeforeImage").copy();
                    NativeRepairStackImage.putCount(var7, var1.getCount());
                    if (var1.getCount() > 0 && !var7.equals(var1.saveOptional(this.player.registryAccess()))) {
                        throw new IOException("Repair supply fields changed");
                    } else {
                        var6.putInt("After", var1.getCount());
                        var6.putBoolean("Returned", true);
                        this.transition(var4);
                        OwnerStatueTaskOrigin.paid(this.origin, this, this.task, var3);
                    }
                } else {
                    throw new IOException("Repair original shrink result mismatch");
                }
            }
        }

        void decideAtOriginalTerminal() throws IOException {
            this.check();
            CompoundTag var1 = this.document.getCompound("Journal").copy();
            if (var1.getString("Phase").equals("PREPARED") && this.statueState.equals(this.level.getBlockState(this.statue))) {
                ListTag var2 = var1.getList("Payments", 10);
                int var3 = 0;

                for (int var4 = 0; var4 < var2.size(); var4++) {
                    CompoundTag var5 = var2.getCompound(var4);
                    ItemStack var6 = this.supplies.get(var5.getInt("Slot"));
                    if (!var5.getBoolean("Returned")
                        || var6 == null
                        || this.player.getInventory().getItem(var5.getInt("Slot")) != var6
                        || var6.getCount() != var5.getInt("After")) {
                        throw new IOException("Repair payment has no coherent original return");
                    }

                    var3 = Math.addExact(var3, var5.getInt("Count"));
                }

                CompoundTag var13 = this.starting
                    ? OwnerStatueTaskPlan.start(
                        this.before.getCompound("Expected"),
                        this.before.getInt("AcceptedLevel"),
                        this.before.getString("AcceptedProfile"),
                        this.before.getInt("AcceptedMaximum"),
                        this.tint, this.player.registryAccess()
                    )
                    : OwnerStatueTaskPlan.after(this.before.getCompound("Expected"), this.operation, this.task, var3, this.tint, this.player.registryAccess());
                if (!var13.equals(this.weapon.saveOptional(this.player.registryAccess()))) {
                    throw new IOException("Repair task exact transition mismatch");
                } else {
                    ListTag var14 = this.player.getInventory().save(new ListTag());
                    Map var15 = slots(this.beforeInventory);
                    CompoundTag var7 = var13.copy();
                    var7.putByte("Slot", (byte)(this.slot == 40 ? 150 : this.slot));
                    var15.put(this.slot == 40 ? 150 : this.slot, var7);

                    for (int var8 = 0; var8 < var2.size(); var8++) {
                        CompoundTag var9 = var2.getCompound(var8);
                        CompoundTag var10 = (CompoundTag)var15.get(var9.getInt("Slot"));
                        if (var10 == null || NativeRepairStackImage.count(var10) != var9.getInt("Before")) {
                            throw new IOException("Repair inventory payment baseline mismatch");
                        }

                        if (var9.getInt("After") == 0) {
                            var15.remove(var9.getInt("Slot"));
                        } else {
                            NativeRepairStackImage.putCount(var10, var9.getInt("After"));
                        }
                    }

                    if (!var15.equals(slots(var14))) {
                        throw new IOException("Repair unrelated inventory mutation");
                    } else {
                        CompoundTag var16 = this.before.copy();
                        if (!var16.getBoolean("MigrationCommitted")) {
                            var16.putIntArray("PolicyCredit", var16.getIntArray("PendingPolicyCredit"));
                            var16.putBoolean("MigrationCommitted", true);
                            var16.putString("CreditClassification", "EXPLICIT_HUMAN_POLICY_MIGRATION_CREDIT_NOT_VERIFIED_PAYMENT");
                        }

                        int[] var17 = var16.getIntArray("ObservedCommittedPayments");
                        if (var17.length != 3) {
                            throw new IOException("Invalid repair payment accounting");
                        } else {
                            if (this.operation == Operation.SUBMIT_REPAIR) {
                                var17[this.task] = Math.addExact(var17[this.task], var3);
                            }

                            var16.putIntArray("ObservedCommittedPayments", var17);
                            var16.put("Expected", var13);
                            var16.putString("Rules", OwnerStatueTaskPlan.rules(this.weapon));
                            var16.putUUID("LastTransaction", this.transaction);
                            var16.putString("State", "ORIGINAL_TERMINAL_DECISION");
                            if (this.operation == Operation.CLAIM_REPAIR) {
                                var16.putBoolean("Consumed", true);
                            }

                            if (!var16.getBoolean("Consumed")) {
                                int[] var18 = NativeRepairStackImage.data(var13).getCompound("FTBossWeaponLifecycle").getCompound("RepairQuest").getIntArray("Progress");
                                int[] var11 = var16.getIntArray("PolicyCredit");
                                if (var18.length != 3 || var11.length != 3) {
                                    throw new IOException("Invalid repair progress accounting");
                                }

                                for (int var12 = 0; var12 < 3; var12++) {
                                    if (var11[var12] < 0 || var17[var12] < 0 || Math.addExact(var11[var12], var17[var12]) != var18[var12]) {
                                        throw new IOException("Repair progress origin mismatch");
                                    }
                                }
                            }

                            var1.put("AfterTask", var16);
                            this.decidedInventory = var14.copy();
                            var1.put("AfterInventory", this.decidedInventory.copy());
                            var1.putString("Phase", "DECIDED");
                            this.transition(var1);
                        }
                    }
                }
            } else {
                throw new IOException("Repair original terminal phase or statue mismatch");
            }
        }

        void beforeOriginalRollback() throws IOException {
            this.originFailed();
            this.check();
            CompoundTag var1 = this.document.contains("Journal", 10) ? this.document.getCompound("Journal").copy() : new CompoundTag();
            var1.putUUID("Transaction", this.transaction);
            var1.putUUID("Task", this.before.getUUID("Task"));
            var1.putUUID("Weapon", this.before.getUUID("Weapon"));
            if (this.request != null) {
                var1.putUUID("Request", this.request.request());
            }

            var1.put("BeforeTask", this.before.copy());
            var1.putBoolean("TaskAbsentBefore", this.starting);
            if (this.starting) {
                var1.putUUID("AcceptedWearCall", this.before.getUUID("AcceptedWearCall"));
            }

            var1.put("BeforeInventory", this.beforeInventory.copy());
            if (this.decidedInventory != null) {
                var1.put("AfterInventory", this.decidedInventory.copy());
            }

            var1.putString("Phase", "ABORTING");
            CompoundTag var2 = this.document.copy();
            var2.put("Journal", var1);
            if (this.starting) {
                var2.getCompound("Tasks").remove(this.before.getUUID("Weapon").toString());
            } else {
                var2.getCompound("Tasks").put(this.before.getUUID("Weapon").toString(), this.before.copy());
            }

            this.service.writeOwner(this.player.getUUID(), this.document, var2);
            this.document = var2;
        }

        void checkpointAfterOriginalSaveOrRollback() throws IOException {
            this.check();
            CompoundTag var1 = this.document.getCompound("Journal");
            boolean var2 = var1.getString("Phase").equals("DECIDED");
            if (!var2 && !var1.getString("Phase").equals("ABORTING")) {
                throw new IOException("Repair checkpoint has no decision");
            } else {
                ListTag var3 = var1.getList(var2 ? "AfterInventory" : "BeforeInventory", 10);
                if (!var3.equals(this.player.getInventory().save(new ListTag()))) {
                    throw new IOException("Repair live checkpoint mismatch");
                } else {
                    this.service.checkpointPlayer(this.player, var3);
                    CompoundTag var4 = this.document.copy();
                    CompoundTag var5 = var1.getCompound(var2 ? "AfterTask" : "BeforeTask").copy();
                    var5.putString("State", var2 ? "CHECKPOINTED_COMMIT" : "CHECKPOINTED_ABORT_NO_CREDIT");
                    if (this.starting && !var2) {
                        var4.getCompound("Tasks").remove(this.before.getUUID("Weapon").toString());
                    } else {
                        var4.getCompound("Tasks").put(this.before.getUUID("Weapon").toString(), var5);
                    }

                    if (this.starting) {
                        OwnerStatueTaskService.settleStartReceipt(var4, var1, var2);
                    }

                    if (this.request != null) {
                        var4.getCompound("Requests").putString(this.request.request().toString(), (var2 ? "COMMITTED:" : "ABORTED:") + this.transaction);
                    }

                    var4.remove("Journal");
                    this.service.writeOwner(this.player.getUUID(), this.document, var4);
                    this.document = var4;
                    this.service.checkpointSavedData(this.player.getUUID(), var4);
                    this.closed = true;
                }
            }
        }

        private static Map<Integer, CompoundTag> slots(ListTag var0) throws IOException {
            HashMap var1 = new HashMap();

            for (int var2 = 0; var2 < var0.size(); var2++) {
                CompoundTag var3 = var0.getCompound(var2).copy();
                if (var1.put(var3.getByte("Slot") & 255, var3) != null) {
                    throw new IOException("Duplicate repair inventory slot");
                }
            }

            return var1;
        }
    }
}
