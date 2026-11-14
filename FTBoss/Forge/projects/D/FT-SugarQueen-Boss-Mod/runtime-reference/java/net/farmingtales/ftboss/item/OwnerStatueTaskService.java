/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.storage.LevelResource
 */
package net.farmingtales.ftboss.item;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRepair;
import net.farmingtales.ftboss.item.OwnerLineageRevocationStore;
import net.farmingtales.ftboss.item.OwnerRepairContinuation;
import net.farmingtales.ftboss.item.OwnerRepairGenerationAuthority;
import net.farmingtales.ftboss.item.OwnerStatueTaskOrigin;
import net.farmingtales.ftboss.item.OwnerStatueTaskPlan;
import net.farmingtales.ftboss.item.OwnerStatueTaskSavedData;
import net.farmingtales.ftboss.item.OwnerStatueWearAdmission;
import net.farmingtales.ftboss.item.StatueRepairMutation;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;

public final class OwnerStatueTaskService {
    private static final UUID CUTOVER_OWNER = new UUID(0L, 0L);
    private static final String POLICY = "EXPLICIT_HUMAN_POLICY_MIGRATION_CREDIT_NOT_VERIFIED_PAYMENT";
    private static final Map<MinecraftServer, OwnerStatueTaskService> SERVERS = new WeakHashMap<MinecraftServer, OwnerStatueTaskService>();
    private final MinecraftServer server;
    private final OwnerLineageRevocationStore store;
    private final OwnerStatueTaskSavedData savedData;
    private final CompoundTag cutover;
    private final Map<ServerPlayer, IOException> unavailable = new IdentityHashMap<ServerPlayer, IOException>();

    private OwnerStatueTaskService(MinecraftServer minecraftServer) throws IOException {
        this.server = minecraftServer;
        this.store = OwnerLineageRevocationStore.open(minecraftServer);
        this.savedData = (OwnerStatueTaskSavedData)minecraftServer.m_129783_().m_8895_().m_164861_(OwnerStatueTaskSavedData::load, OwnerStatueTaskSavedData::new, "ftboss_repair_tasks");
        CompoundTag compoundTag = this.store.readRepairDocument(CUTOVER_OWNER, true);
        if (compoundTag.m_128456_()) {
            if (this.savedData.hadCutover()) {
                throw new IOException("Missing signed repair cutover; do not re-import current NBT");
            }
            CompoundTag compoundTag2 = this.captureLegacyTasks();
            this.store.writeRepairDocument(CUTOVER_OWNER, true, compoundTag, compoundTag2);
            compoundTag = compoundTag2;
        }
        if (compoundTag.m_128451_("Schema") != 1 || !compoundTag.m_128403_("Cutover") || !POLICY.equals(compoundTag.m_128461_("Policy"))) {
            throw new IOException("Unknown repair migration cutover");
        }
        this.cutover = compoundTag.m_6426_();
        this.savedData.cutover(this.cutover);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void start(MinecraftServer minecraftServer) {
        if (!minecraftServer.m_18695_()) {
            throw new IllegalStateException("Repair task startup off original server thread");
        }
        try {
            OwnerStatueTaskService ownerStatueTaskService = new OwnerStatueTaskService(minecraftServer);
            Map<MinecraftServer, OwnerStatueTaskService> map = SERVERS;
            synchronized (map) {
                if (SERVERS.putIfAbsent(minecraftServer, ownerStatueTaskService) != null) {
                    throw new IllegalStateException("Repair task service already bound");
                }
            }
        }
        catch (IOException iOException) {
            throw new IllegalStateException("Cannot freeze existing legacy repair tasks", iOException);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void stop(MinecraftServer minecraftServer) {
        Map<MinecraftServer, OwnerStatueTaskService> map = SERVERS;
        synchronized (map) {
            SERVERS.remove(minecraftServer);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static OwnerStatueTaskService service(ServerPlayer serverPlayer) {
        OwnerStatueTaskService ownerStatueTaskService;
        Map<MinecraftServer, OwnerStatueTaskService> map = SERVERS;
        synchronized (map) {
            ownerStatueTaskService = SERVERS.get(serverPlayer.m_20194_());
        }
        if (ownerStatueTaskService == null || !ownerStatueTaskService.server.m_18695_() || serverPlayer.m_20194_() != ownerStatueTaskService.server) {
            throw new IllegalStateException("Repair task service has no original server binding");
        }
        return ownerStatueTaskService;
    }

    public static void loaded(ServerPlayer serverPlayer) {
        OwnerStatueTaskService ownerStatueTaskService = OwnerStatueTaskService.service(serverPlayer);
        try {
            CompoundTag compoundTag = ownerStatueTaskService.owner(serverPlayer.m_20148_());
            if (compoundTag.m_128456_()) {
                CompoundTag compoundTag2 = ownerStatueTaskService.cutover.m_128469_("Owners").m_128469_(serverPlayer.m_20148_().toString());
                if (compoundTag2.m_128456_()) {
                    return;
                }
                compoundTag = ownerStatueTaskService.initializeOwner(serverPlayer.m_20148_(), compoundTag, compoundTag2);
            }
            ownerStatueTaskService.finishOwnerInitialization(serverPlayer.m_20148_(), compoundTag);
            ownerStatueTaskService.savedData.owner(serverPlayer.m_20148_(), compoundTag);
            if (compoundTag.m_128425_("Journal", 10)) {
                ownerStatueTaskService.recoverLoadedPlayer(serverPlayer, compoundTag);
            }
        }
        catch (IOException iOException) {
            throw OwnerStatueTaskService.unavailable(serverPlayer, iOException);
        }
        catch (LinkageError | RuntimeException throwable) {
            throw OwnerStatueTaskService.unavailable(serverPlayer, new IOException("Repair task load or recovery failed", throwable));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void unloaded(ServerPlayer serverPlayer) {
        Map<MinecraftServer, OwnerStatueTaskService> map = SERVERS;
        synchronized (map) {
            OwnerStatueTaskService ownerStatueTaskService = SERVERS.get(serverPlayer.m_20194_());
            if (ownerStatueTaskService != null) {
                ownerStatueTaskService.unavailable.remove(serverPlayer);
            }
        }
    }

    static RuntimeException unavailable(ServerPlayer serverPlayer, IOException iOException) {
        OwnerStatueTaskService ownerStatueTaskService = OwnerStatueTaskService.service(serverPlayer);
        ownerStatueTaskService.unavailable.put(serverPlayer, iOException);
        try {
            serverPlayer.f_8906_.m_9942_((Component)Component.m_237113_((String)"Repair task storage requires reconnect reconciliation."));
        }
        catch (RuntimeException runtimeException) {
            iOException.addSuppressed(runtimeException);
        }
        return new IllegalStateException("Repair task requires durable reconciliation", iOException);
    }

    private CompoundTag initializeOwner(UUID uUID, CompoundTag compoundTag, CompoundTag compoundTag2) throws IOException {
        CompoundTag compoundTag3;
        CompoundTag compoundTag4 = this.store.readRepairDocument(CUTOVER_OWNER, true);
        CompoundTag compoundTag5 = compoundTag4.m_128469_("BoundOwners").m_128469_(uUID.toString());
        if (!compoundTag5.m_128456_()) {
            if (!compoundTag5.m_128461_("Phase").equals("CREATING") || !compoundTag5.m_128425_("Initial", 10)) {
                throw new IOException("Missing previously initialized owner journal; never reset migration credit");
            }
            compoundTag3 = compoundTag5.m_128469_("Initial").m_6426_();
        } else {
            compoundTag3 = new CompoundTag();
            compoundTag3.m_128405_("Schema", 1);
            compoundTag3.m_128362_("Owner", uUID);
            compoundTag3.m_128362_("Document", UUID.randomUUID());
            compoundTag3.m_128362_("Cutover", this.cutover.m_128342_("Cutover"));
            compoundTag3.m_128365_("Tasks", (Tag)compoundTag2.m_6426_());
            compoundTag3.m_128365_("Requests", (Tag)new CompoundTag());
            CompoundTag compoundTag6 = compoundTag4.m_6426_();
            CompoundTag compoundTag7 = compoundTag6.m_128425_("BoundOwners", 10) ? compoundTag6.m_128469_("BoundOwners") : new CompoundTag();
            compoundTag5 = new CompoundTag();
            compoundTag5.m_128359_("Phase", "CREATING");
            compoundTag5.m_128362_("Document", compoundTag3.m_128342_("Document"));
            compoundTag5.m_128365_("Initial", (Tag)compoundTag3.m_6426_());
            compoundTag7.m_128365_(uUID.toString(), (Tag)compoundTag5);
            compoundTag6.m_128365_("BoundOwners", (Tag)compoundTag7);
            this.store.writeRepairDocument(CUTOVER_OWNER, true, compoundTag4, compoundTag6);
            this.savedData.cutover(compoundTag6);
        }
        this.writeOwner(uUID, compoundTag, compoundTag3);
        return compoundTag3;
    }

    private void finishOwnerInitialization(UUID uUID, CompoundTag compoundTag) throws IOException {
        CompoundTag compoundTag2 = this.store.readRepairDocument(CUTOVER_OWNER, true);
        CompoundTag compoundTag3 = compoundTag2.m_128469_("BoundOwners").m_128469_(uUID.toString());
        if (!(compoundTag3.m_128403_("Document") && compoundTag.m_128403_("Document") && compoundTag3.m_128342_("Document").equals(compoundTag.m_128342_("Document")))) {
            throw new IOException("Repair owner document incarnation mismatch");
        }
        if (compoundTag3.m_128461_("Phase").equals("CREATED")) {
            return;
        }
        if (!compoundTag3.m_128461_("Phase").equals("CREATING")) {
            throw new IOException("Unknown repair owner initialization phase");
        }
        CompoundTag compoundTag4 = compoundTag2.m_6426_();
        CompoundTag compoundTag5 = compoundTag3.m_6426_();
        compoundTag5.m_128359_("Phase", "CREATED");
        compoundTag5.m_128473_("Initial");
        compoundTag4.m_128469_("BoundOwners").m_128365_(uUID.toString(), (Tag)compoundTag5);
        this.store.writeRepairDocument(CUTOVER_OWNER, true, compoundTag2, compoundTag4);
        this.savedData.cutover(compoundTag4);
    }

    private void writeOwner(UUID uUID, CompoundTag compoundTag, CompoundTag compoundTag2) throws IOException {
        this.store.writeRepairDocument(uUID, false, compoundTag, compoundTag2);
        this.savedData.owner(uUID, compoundTag2);
    }

    private void checkpointSavedData(UUID uUID, CompoundTag compoundTag) throws IOException {
        this.server.m_129783_().m_8895_().m_78151_();
        Path path = this.server.m_129843_(LevelResource.f_78182_).resolve("data").resolve("ftboss_repair_tasks.dat");
        if (Files.isSymbolicLink(path) || !Files.isRegularFile(path, new LinkOption[0])) {
            throw new IOException("Repair SavedData checkpoint unavailable");
        }
        CompoundTag compoundTag2 = NbtIo.m_128937_((File)path.toFile());
        if (compoundTag2 == null || !compoundTag.equals((Object)compoundTag2.m_128469_("data").m_128469_("Owners").m_128469_(uUID.toString()))) {
            throw new IOException("Repair SavedData checkpoint readback mismatch");
        }
    }

    private void checkpointPlayer(ServerPlayer serverPlayer, ListTag listTag) throws IOException {
        this.server.m_6846_().m_11302_();
        Path path = this.server.m_129843_(LevelResource.f_78176_).resolve(serverPlayer.m_20148_() + ".dat");
        if (Files.isSymbolicLink(path) || !Files.isRegularFile(path, new LinkOption[0])) {
            throw new IOException("Repair player checkpoint unavailable");
        }
        CompoundTag compoundTag = NbtIo.m_128937_((File)path.toFile());
        if (!listTag.equals((Object)serverPlayer.m_150109_().m_36026_(new ListTag())) || compoundTag == null || !listTag.equals((Object)compoundTag.m_128437_("Inventory", 10))) {
            throw new IOException("Repair player checkpoint readback mismatch");
        }
    }

    private void recoverLoadedPlayer(ServerPlayer serverPlayer, CompoundTag compoundTag) throws IOException {
        CompoundTag compoundTag2 = compoundTag.m_128469_("Journal");
        String string = compoundTag2.m_128461_("Phase");
        boolean bl = string.equals("DECIDED");
        if (!(bl || string.equals("PREPARED") || string.equals("ABORTING"))) {
            throw new IOException("Unknown repair recovery phase");
        }
        CompoundTag compoundTag3 = compoundTag2.m_128469_(bl ? "AfterTask" : "BeforeTask");
        if (!(compoundTag2.m_128403_("Transaction") && compoundTag2.m_128403_("Task") && compoundTag2.m_128403_("Weapon") && compoundTag3.m_128403_("Task") && compoundTag3.m_128342_("Task").equals(compoundTag2.m_128342_("Task")) && compoundTag3.m_128342_("Owner").equals(serverPlayer.m_20148_()) && compoundTag3.m_128342_("Weapon").equals(compoundTag2.m_128342_("Weapon")))) {
            throw new IOException("Repair recovery task binding mismatch");
        }
        ListTag listTag = compoundTag2.m_128437_(bl ? "AfterInventory" : "BeforeInventory", 10);
        if (listTag.isEmpty()) {
            throw new IOException("Missing repair recovery inventory");
        }
        ListTag listTag2 = serverPlayer.m_150109_().m_36026_(new ListTag());
        if (!(listTag2.equals((Object)compoundTag2.m_128437_("BeforeInventory", 10)) || compoundTag2.m_128425_("AfterInventory", 9) && listTag2.equals((Object)compoundTag2.m_128437_("AfterInventory", 10)))) {
            throw new IOException("Repair recovery conflict; do not replace an unrelated current inventory");
        }
        if (!listTag2.equals((Object)listTag)) {
            serverPlayer.m_150109_().m_36035_(listTag.m_6426_());
            serverPlayer.m_150109_().m_6596_();
        }
        this.checkpointPlayer(serverPlayer, listTag);
        CompoundTag compoundTag4 = compoundTag.m_6426_();
        CompoundTag compoundTag5 = compoundTag3.m_6426_();
        compoundTag5.m_128359_("State", bl ? "RECOVERED_COMMIT" : "RECOVERED_ABORT_NO_CREDIT");
        if (compoundTag2.m_128471_("TaskAbsentBefore") && !bl) {
            compoundTag4.m_128469_("Tasks").m_128473_(compoundTag2.m_128342_("Weapon").toString());
        } else {
            compoundTag4.m_128469_("Tasks").m_128365_(compoundTag2.m_128342_("Weapon").toString(), (Tag)compoundTag5);
        }
        if (compoundTag2.m_128471_("TaskAbsentBefore")) {
            OwnerStatueTaskService.settleStartReceipt(compoundTag4, compoundTag2, bl);
        }
        if (compoundTag2.m_128403_("Request")) {
            compoundTag4.m_128469_("Requests").m_128359_(compoundTag2.m_128342_("Request").toString(), (bl ? "COMMITTED:" : "ABORTED:") + compoundTag2.m_128342_("Transaction"));
        }
        compoundTag4.m_128473_("Journal");
        this.writeOwner(serverPlayer.m_20148_(), compoundTag, compoundTag4);
        this.checkpointSavedData(serverPlayer.m_20148_(), compoundTag4);
    }

    private CompoundTag owner(UUID uUID) throws IOException {
        CompoundTag compoundTag = this.store.readRepairDocument(uUID, false);
        if (!(compoundTag.m_128456_() || compoundTag.m_128451_("Schema") == 1 && compoundTag.m_128403_("Owner") && uUID.equals(compoundTag.m_128342_("Owner")) && compoundTag.m_128403_("Cutover") && this.cutover.m_128342_("Cutover").equals(compoundTag.m_128342_("Cutover")))) {
            throw new IOException("Repair task owner or cutover mismatch");
        }
        return compoundTag;
    }

    private CompoundTag captureLegacyTasks() throws IOException {
        Object object;
        Object object2;
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("Schema", 1);
        compoundTag.m_128362_("Cutover", UUID.randomUUID());
        compoundTag.m_128359_("Policy", POLICY);
        compoundTag.m_128359_("CaptureScope", "ORIGINAL_SERVER_PLAYER_INVENTORIES_AT_UPGRADE");
        CompoundTag compoundTag2 = new CompoundTag();
        HashMap<UUID, List> hashMap = new HashMap<UUID, List>();
        HashSet<UUID> hashSet = new HashSet<UUID>();
        Path path2 = this.server.m_129843_(LevelResource.f_78176_).toAbsolutePath().normalize();
        if (Files.isSymbolicLink(path2)) {
            throw new IOException("Repair migration player directory is a link");
        }
        if (Files.isDirectory(path2, new LinkOption[0])) {
            List<Path> list;
            try (Object object3 = Files.list(path2);){
                list = object3.filter(path -> path.getFileName().toString().matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.dat")).sorted().toList();
            }
            object3 = list.iterator();
            while (object3.hasNext()) {
                object2 = (Path)object3.next();
                if (Files.isSymbolicLink((Path)object2) || !Files.isRegularFile((Path)object2, new LinkOption[0]) || Files.size((Path)object2) > 0x800000L) {
                    throw new IOException("Repair migration player input budget");
                }
                object = UUID.fromString(object2.getFileName().toString().substring(0, 36));
                CompoundTag compoundTag3 = NbtIo.m_128937_((File)object2.toFile());
                if (compoundTag3 == null) {
                    throw new IOException("Missing repair migration player input");
                }
                CompoundTag compoundTag4 = new CompoundTag();
                ListTag listTag = compoundTag3.m_128437_("Inventory", 10);
                for (int i = 0; i < listTag.size(); ++i) {
                    UUID uUID2;
                    CompoundTag compoundTag5;
                    CompoundTag compoundTag6 = listTag.m_128728_(i);
                    ItemStack itemStack = ItemStack.m_41712_((CompoundTag)compoundTag6.m_6426_());
                    try {
                        compoundTag5 = OwnerStatueTaskPlan.legacyQuest(itemStack);
                    }
                    catch (IllegalArgumentException illegalArgumentException) {
                        continue;
                    }
                    CompoundTag compoundTag7 = itemStack.m_41739_(new CompoundTag());
                    if (!OwnerRepairGenerationAuthority.statueTaskObjectPolicy(itemStack)) continue;
                    UUID uUID3 = FoodWeaponLifecycle.weaponId(itemStack);
                    CompoundTag compoundTag8 = new CompoundTag();
                    while (!hashSet.add(uUID2 = UUID.randomUUID())) {
                    }
                    compoundTag8.m_128362_("Task", uUID2);
                    compoundTag8.m_128362_("Owner", (UUID)object);
                    compoundTag8.m_128362_("Weapon", uUID3);
                    compoundTag8.m_128362_("Cutover", compoundTag.m_128342_("Cutover"));
                    compoundTag8.m_128359_("Policy", POLICY);
                    compoundTag8.m_128405_("PolicyVersion", 1);
                    compoundTag8.m_128365_("OriginalQuest", (Tag)compoundTag5);
                    compoundTag8.m_128365_("Expected", (Tag)compoundTag7);
                    compoundTag8.m_128405_("Slot", (int)compoundTag6.m_128445_("Slot"));
                    compoundTag8.m_128359_("Rules", OwnerStatueTaskPlan.rules(itemStack));
                    compoundTag8.m_128385_("PendingPolicyCredit", compoundTag5.m_128465_("Progress"));
                    compoundTag8.m_128385_("PolicyCredit", new int[3]);
                    compoundTag8.m_128385_("ObservedCommittedPayments", new int[3]);
                    compoundTag8.m_128356_("Sequence", 0L);
                    compoundTag8.m_128359_("State", "FROZEN_ELIGIBILITY_ONLY_NO_CREDIT");
                    compoundTag4.m_128365_(uUID3.toString(), (Tag)compoundTag8);
                    hashMap.computeIfAbsent(uUID3, uUID -> new ArrayList()).add(compoundTag8);
                }
                if (compoundTag4.m_128456_()) continue;
                compoundTag2.m_128365_(((UUID)object).toString(), (Tag)compoundTag4);
            }
        }
        for (Object object3 : hashMap.values()) {
            if (object3.size() <= 1) continue;
            object2 = object3.iterator();
            while (object2.hasNext()) {
                object = (CompoundTag)object2.next();
                object.m_128379_("AmbiguousCopy", true);
            }
        }
        compoundTag.m_128365_("Owners", (Tag)compoundTag2);
        return compoundTag;
    }

    static void reservePrewriteTrack(OwnerRepairContinuation.Track track) throws IOException {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerRepairContinuation.class && stackFrame.getMethodName().equals("prewrite")).orElse(false));
        if (!bl) {
            throw new IOException("Detached prewrite owner consumer");
        }
        ServerPlayer serverPlayer = track.ownerPlayer();
        OwnerStatueTaskService ownerStatueTaskService = OwnerStatueTaskService.service(serverPlayer);
        if (ownerStatueTaskService.unavailable.containsKey(serverPlayer)) {
            throw new IOException("Prewrite owner unavailable");
        }
        CompoundTag compoundTag = track.ownerRecord();
        UUID uUID = compoundTag.m_128342_("Owner");
        UUID uUID2 = compoundTag.m_128342_("Weapon");
        UUID uUID3 = compoundTag.m_128342_("Track");
        CompoundTag compoundTag2 = ownerStatueTaskService.owner(uUID);
        CompoundTag compoundTag3 = compoundTag2.m_128469_("Tasks").m_128469_(uUID2.toString());
        CompoundTag compoundTag4 = compoundTag2.m_128469_("ValidatedOriginalWear").m_128469_(compoundTag.m_128342_("OriginalCall").toString());
        if (compoundTag2.m_128456_() || compoundTag2.m_128441_("Journal") || !compoundTag3.m_128471_("Consumed") || compoundTag2.m_128441_("WearHeads") && !compoundTag2.m_128425_("WearHeads", 10) || compoundTag2.m_128441_("WearTracks") && !compoundTag2.m_128425_("WearTracks", 10) || !compoundTag3.m_128403_("AcceptedWearCall") || !compoundTag3.m_128342_("AcceptedWearCall").equals(compoundTag.m_128342_("OriginalCall")) || !compoundTag4.m_128403_("OriginalLoad") || !compoundTag4.m_128342_("OriginalLoad").equals(compoundTag.m_128342_("OriginalLoad")) || !compoundTag3.m_128403_("Owner") || !compoundTag3.m_128342_("Owner").equals(uUID) || !compoundTag3.m_128403_("Weapon") || !compoundTag3.m_128342_("Weapon").equals(uUID2) || !compoundTag3.m_128403_("Task") || !compoundTag3.m_128342_("Task").equals(compoundTag.m_128342_("ParentTask")) || !compoundTag3.m_128403_("LastTransaction") || !compoundTag3.m_128342_("LastTransaction").equals(compoundTag.m_128342_("ParentClaim")) || !compoundTag3.m_128403_("GenerationClaim") || !compoundTag3.m_128342_("GenerationClaim").equals(compoundTag.m_128342_("ParentClaim")) || !compoundTag3.m_128461_("State").equals("CHECKPOINTED_COMMIT") || !compoundTag3.m_128461_("GenerationDisposition").equals("CONSUMED_NO_REISSUE_OR_RECOVERY_PUBLICATION") || compoundTag3.m_128454_("Sequence") != compoundTag.m_128454_("TaskSequence") || !compoundTag3.m_128461_("Rules").equals(compoundTag.m_128461_("Rules")) || !compoundTag3.m_128469_("Expected").equals((Object)compoundTag.m_128469_("Expected")) || compoundTag2.m_128469_("WearHeads").m_128441_(uUID2.toString()) || compoundTag2.m_128469_("WearTracks").m_128441_(uUID3.toString()) || compoundTag2.m_128469_("WearTracks").m_128431_().size() >= 256) {
            throw new IOException("Prewrite owner predecessor mismatch or already reserved");
        }
        CompoundTag compoundTag5 = compoundTag.m_6426_();
        compoundTag5.m_128359_("Phase", "PREPARING");
        CompoundTag compoundTag6 = compoundTag2.m_6426_();
        CompoundTag compoundTag7 = compoundTag6.m_128469_("WearTracks").m_6426_();
        compoundTag7.m_128365_(uUID3.toString(), (Tag)compoundTag5);
        compoundTag6.m_128365_("WearTracks", (Tag)compoundTag7);
        CompoundTag compoundTag8 = compoundTag6.m_128469_("WearHeads").m_6426_();
        compoundTag8.m_128362_(uUID2.toString(), uUID3);
        compoundTag6.m_128365_("WearHeads", (Tag)compoundTag8);
        ownerStatueTaskService.writeOwner(uUID, compoundTag2, compoundTag6);
        ownerStatueTaskService.checkpointSavedData(uUID, compoundTag6);
        track.ownerPrepared(ownerStatueTaskService.store);
        ownerStatueTaskService.store.consumePrewriteTrack(track);
        if (!ownerStatueTaskService.owner(uUID).equals((Object)compoundTag6)) {
            throw new IOException("Prewrite owner CAS changed after lineage consumption");
        }
        CompoundTag compoundTag9 = compoundTag6.m_6426_();
        compoundTag9.m_128469_("WearTracks").m_128469_(uUID3.toString()).m_128359_("Phase", "TRACK_RESERVED");
        ownerStatueTaskService.writeOwner(uUID, compoundTag6, compoundTag9);
        ownerStatueTaskService.checkpointSavedData(uUID, compoundTag9);
        track.reserved();
    }

    static boolean originalWearPlanUnused(ServerPlayer serverPlayer, UUID uUID) throws IOException {
        CompoundTag compoundTag;
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerStatueWearAdmission.class && stackFrame.getMethodName().equals("entered")).orElse(false));
        if (!bl || uUID == null) {
            return false;
        }
        OwnerStatueTaskService ownerStatueTaskService = OwnerStatueTaskService.service(serverPlayer);
        if (ownerStatueTaskService.unavailable.containsKey(serverPlayer)) {
            return false;
        }
        CompoundTag compoundTag2 = ownerStatueTaskService.owner(serverPlayer.m_20148_());
        if (compoundTag2.m_128456_()) {
            return ownerStatueTaskService.cutover.m_128469_("BoundOwners").m_128469_(serverPlayer.m_20148_().toString()).m_128456_() && ownerStatueTaskService.cutover.m_128469_("Owners").m_128469_(serverPlayer.m_20148_().toString()).m_128469_(uUID.toString()).m_128456_();
        }
        if (compoundTag2.m_128441_("Journal") || !compoundTag2.m_128469_("Tasks").m_128469_(uUID.toString()).m_128456_()) {
            return false;
        }
        CompoundTag compoundTag3 = compoundTag2.m_128469_("OriginalWearEvidence");
        for (String string : compoundTag3.m_128431_()) {
            compoundTag = compoundTag3.m_128469_(string).m_128469_("AcceptedOriginalLoadBefore").m_128469_("tag").m_128469_("FTBossWeaponLifecycle");
            if (!compoundTag.m_128403_("Weapon") || !uUID.equals(compoundTag.m_128342_("Weapon"))) continue;
            return false;
        }
        for (String string : compoundTag2.m_128469_("ValidatedOriginalWear").m_128431_()) {
            compoundTag = compoundTag2.m_128469_("ValidatedOriginalWear").m_128469_(string);
            if (!compoundTag.m_128403_("Weapon") || !uUID.equals(compoundTag.m_128342_("Weapon"))) continue;
            return false;
        }
        return true;
    }

    static void requireOriginalWearNotRetired(ServerPlayer serverPlayer, UUID uUID) throws IOException {
        OwnerStatueTaskService ownerStatueTaskService = OwnerStatueTaskService.service(serverPlayer);
        if (uUID == null || ownerStatueTaskService.unavailable.containsKey(serverPlayer) || ownerStatueTaskService.store.rejectsKnownLineage(serverPlayer.m_20148_(), uUID)) {
            throw new IOException("Original wear has missing identity, unavailable owner, or known inactive lineage; never fresh issue");
        }
    }

    static void recordOriginalWear(OwnerStatueWearAdmission.Terminal terminal) throws IOException {
        String string;
        if (terminal == null) {
            throw new IOException("Missing original wear terminal");
        }
        ServerPlayer serverPlayer = terminal.player();
        OwnerStatueTaskService ownerStatueTaskService = OwnerStatueTaskService.service(serverPlayer);
        if (ownerStatueTaskService.unavailable.containsKey(serverPlayer)) {
            throw new IOException("Original wear owner requires reconciliation", ownerStatueTaskService.unavailable.get(serverPlayer));
        }
        CompoundTag compoundTag = terminal.take();
        if (!(compoundTag.m_128403_("Owner") && serverPlayer.m_20148_().equals(compoundTag.m_128342_("Owner")) && compoundTag.m_128403_("Call") && terminal.identity().equals(compoundTag.m_128342_("Call")) && compoundTag.m_128461_("Authority").equals("ORIGINAL_LOAD_CUSTODY_BOUND_WEAR_EVIDENCE_ONLY_NOT_TASK_OR_GENERATION_AUTHORITY"))) {
            throw new IOException("Original wear evidence differs from its physical invocation");
        }
        CompoundTag compoundTag2 = ownerStatueTaskService.owner(serverPlayer.m_20148_());
        if (compoundTag2.m_128456_()) {
            compoundTag2 = ownerStatueTaskService.initializeOwner(serverPlayer.m_20148_(), compoundTag2, ownerStatueTaskService.cutover.m_128469_("Owners").m_128469_(serverPlayer.m_20148_().toString()));
        }
        ownerStatueTaskService.finishOwnerInitialization(serverPlayer.m_20148_(), compoundTag2);
        if (compoundTag2.m_128441_("Journal")) {
            throw new IOException("Do not append wear evidence over an unresolved repair journal");
        }
        CompoundTag compoundTag3 = compoundTag2.m_128469_("OriginalWearEvidence");
        if (compoundTag3.m_128441_(string = terminal.identity().toString()) || compoundTag3.m_128431_().size() >= 256) {
            throw new IOException("Duplicate or full original wear evidence journal; never evict obligations");
        }
        CompoundTag compoundTag4 = compoundTag2.m_6426_();
        CompoundTag compoundTag5 = compoundTag3.m_6426_();
        compoundTag5.m_128365_(string, (Tag)compoundTag);
        compoundTag4.m_128365_("OriginalWearEvidence", (Tag)compoundTag5);
        OwnerStatueWearAdmission.Accepted accepted = OwnerStatueWearAdmission.validated(terminal);
        if (accepted != null) {
            CompoundTag compoundTag6 = compoundTag4.m_128469_("ValidatedOriginalWear").m_6426_();
            if (compoundTag6.m_128441_(string) || compoundTag6.m_128431_().size() >= 256) {
                throw new IOException("Duplicate or full validated original wear obligations");
            }
            compoundTag6.m_128365_(string, (Tag)accepted.record());
            compoundTag4.m_128365_("ValidatedOriginalWear", (Tag)compoundTag6);
        }
        ownerStatueTaskService.writeOwner(serverPlayer.m_20148_(), compoundTag2, compoundTag4);
        ownerStatueTaskService.checkpointSavedData(serverPlayer.m_20148_(), compoundTag4);
        if (accepted != null) {
            OwnerStatueWearAdmission.activate(accepted, terminal);
        }
    }

    static Prepared prepare(ServerPlayer serverPlayer, ItemStack itemStack, int n, BlockPos blockPos, FourWeaponTransactions.Operation operation, int n2, FourWeaponTransactions.Request request) throws IOException {
        Object object;
        if (operation == FourWeaponTransactions.Operation.START_REPAIR && !StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == StatueRepairMutation.class && stackFrame.getMethodName().equals("commit")).orElse(false)).booleanValue()) {
            return null;
        }
        OwnerStatueTaskService ownerStatueTaskService = OwnerStatueTaskService.service(serverPlayer);
        if (ownerStatueTaskService.unavailable.containsKey(serverPlayer)) {
            throw new IOException("Repair task owner unavailable", ownerStatueTaskService.unavailable.get(serverPlayer));
        }
        if (!serverPlayer.m_6084_() || serverPlayer.m_5833_() || serverPlayer.m_5803_() || serverPlayer.m_6117_() || !OwnerRepairGenerationAuthority.statueTaskObjectPolicy(itemStack)) {
            return null;
        }
        UUID uUID = FoodWeaponLifecycle.weaponId(itemStack);
        CompoundTag compoundTag = ownerStatueTaskService.owner(serverPlayer.m_20148_());
        if (uUID == null || compoundTag.m_128456_()) {
            return null;
        }
        if (operation == FourWeaponTransactions.Operation.START_REPAIR) {
            return OwnerStatueTaskService.prepareStart(ownerStatueTaskService, serverPlayer, itemStack, n, blockPos, n2, request, compoundTag, uUID);
        }
        CompoundTag compoundTag2 = compoundTag.m_128469_("Tasks").m_128469_(uUID.toString());
        if (compoundTag2.m_128456_() || compoundTag2.m_128471_("Consumed") || compoundTag2.m_128471_("AmbiguousCopy")) {
            return null;
        }
        if (compoundTag.m_128441_("Journal")) {
            throw new IOException("Repair task requires original recovery/checkpoint consumer");
        }
        if (request != null && compoundTag.m_128469_("Requests").m_128441_(request.request().toString())) {
            throw new IllegalStateException("Repair request already has durable outcome");
        }
        if (n < 0 || n >= serverPlayer.m_150109_().m_6643_() || serverPlayer.m_150109_().m_8020_(n) != itemStack || serverPlayer.m_21205_() != itemStack && serverPlayer.m_21206_() != itemStack || n != 40 && serverPlayer.m_150109_().f_35977_ != n || itemStack.m_41613_() != 1 || !compoundTag2.m_128469_("Expected").equals((Object)itemStack.m_41739_(new CompoundTag())) || !compoundTag2.m_128461_("Rules").equals(OwnerStatueTaskPlan.rules(itemStack))) {
            throw new IOException("Repair task input does not match frozen or committed record");
        }
        if (!(request == null || request.binding().owner().equals(serverPlayer.m_20148_()) && request.binding().weapon().equals(uUID) && request.binding().inventorySlot() == n && request.binding().statue() == blockPos.m_121878_() && request.binding().dimension().equals(serverPlayer.m_9236_().m_46472_().m_135782_().toString()) && request.operation() == operation)) {
            throw new IOException("Repair participant differs from original admitted request");
        }
        int n3 = 0;
        for (int i = 0; i < serverPlayer.m_150109_().m_6643_(); ++i) {
            if (!uUID.equals(FoodWeaponLifecycle.weaponId(serverPlayer.m_150109_().m_8020_(i)))) continue;
            ++n3;
        }
        if (n3 != 1 || !serverPlayer.m_9236_().m_46805_(blockPos) || !((object = serverPlayer.m_9236_().m_8055_(blockPos).m_60734_()) instanceof CandyStatueBlock)) {
            throw new IOException("Repair task duplicate or actual statue mismatch");
        }
        CandyStatueBlock candyStatueBlock = (CandyStatueBlock)((Object)object);
        object = FoodWeaponRepair.view(itemStack, candyStatueBlock.ownTint());
        if (!(((FoodWeaponRepair.View)object).eligible() && ((FoodWeaponRepair.View)object).correctStatue() && ((FoodWeaponRepair.View)object).active())) {
            return null;
        }
        if (operation == FourWeaponTransactions.Operation.CLAIM_REPAIR && !((FoodWeaponRepair.View)object).ready()) {
            return null;
        }
        if (operation == FourWeaponTransactions.Operation.SUBMIT_REPAIR && (n2 < 0 || n2 >= 3 || ((FoodWeaponRepair.View)object).tasks().get(n2).complete())) {
            return null;
        }
        if (operation != FourWeaponTransactions.Operation.SUBMIT_REPAIR && operation != FourWeaponTransactions.Operation.CLAIM_REPAIR) {
            return null;
        }
        Prepared prepared = new Prepared(ownerStatueTaskService, serverPlayer, itemStack, n, blockPos, candyStatueBlock.ownTint(), operation, n2, request, compoundTag, compoundTag2);
        prepared.origin = OwnerStatueTaskOrigin.find(serverPlayer, itemStack, n, compoundTag2);
        return prepared;
    }

    private static void settleStartReceipt(CompoundTag compoundTag, CompoundTag compoundTag2, boolean bl) throws IOException {
        if (!compoundTag2.m_128403_("AcceptedWearCall")) {
            throw new IOException("START has no independent original wear identity");
        }
        CompoundTag compoundTag3 = compoundTag.m_128469_("ValidatedOriginalWear").m_128469_(compoundTag2.m_128342_("AcceptedWearCall").toString());
        if (!(compoundTag3.m_128403_("Transaction") && compoundTag3.m_128342_("Transaction").equals(compoundTag2.m_128342_("Transaction")) && compoundTag3.m_128403_("Task") && compoundTag3.m_128342_("Task").equals(compoundTag2.m_128342_("Task")) && compoundTag3.m_128461_("Status").equals("RESERVED_BY_ORIGINAL_START_NO_REISSUE"))) {
            throw new IOException("START wear consumption binding mismatch");
        }
        compoundTag3.m_128359_("Status", bl ? "CONSUMED_BY_ORIGINAL_START_TERMINAL" : "BURNED_BY_ORIGINAL_START_ABORT_NO_REISSUE");
    }

    private static Prepared prepareStart(OwnerStatueTaskService ownerStatueTaskService, ServerPlayer serverPlayer, ItemStack itemStack, int n, BlockPos blockPos, int n2, FourWeaponTransactions.Request request, CompoundTag compoundTag, UUID uUID) throws IOException {
        CompoundTag compoundTag2;
        Object object;
        block14: {
            block13: {
                if (request != null || n2 != 0 || compoundTag.m_128441_("Journal") || !compoundTag.m_128469_("Tasks").m_128469_(uUID.toString()).m_128456_()) {
                    return null;
                }
                if (n < 0 || n >= serverPlayer.m_150109_().m_6643_() || serverPlayer.m_150109_().m_8020_(n) != itemStack || (n != 40 ? serverPlayer.m_150109_().f_35977_ != n || serverPlayer.m_21205_() != itemStack : serverPlayer.m_21206_() != itemStack)) break block13;
                if (itemStack.m_41613_() == 1) break block14;
            }
            return null;
        }
        if (!(request == null || !compoundTag.m_128469_("Requests").m_128441_(request.request().toString()) && request.binding().owner().equals(serverPlayer.m_20148_()) && request.binding().weapon().equals(uUID) && request.binding().inventorySlot() == n && request.binding().statue() == blockPos.m_121878_() && request.binding().dimension().equals(serverPlayer.m_9236_().m_46472_().m_135782_().toString()) && request.operation() == FourWeaponTransactions.Operation.START_REPAIR)) {
            return null;
        }
        int n3 = 0;
        for (int i = 0; i < serverPlayer.m_150109_().m_6643_(); ++i) {
            if (!uUID.equals(FoodWeaponLifecycle.weaponId(serverPlayer.m_150109_().m_8020_(i)))) continue;
            ++n3;
        }
        if (n3 != 1 || !serverPlayer.m_9236_().m_46805_(blockPos) || !((object = serverPlayer.m_9236_().m_8055_(blockPos).m_60734_()) instanceof CandyStatueBlock)) {
            return null;
        }
        CandyStatueBlock candyStatueBlock = (CandyStatueBlock)((Object)object);
        object = FoodWeaponRepair.view(itemStack, candyStatueBlock.ownTint());
        if (!((FoodWeaponRepair.View)object).eligible() || !((FoodWeaponRepair.View)object).correctStatue() || ((FoodWeaponRepair.View)object).active()) {
            return null;
        }
        OwnerStatueWearAdmission.Accepted accepted = OwnerStatueWearAdmission.takeForStart(serverPlayer, itemStack, n);
        if (accepted == null) {
            return null;
        }
        CompoundTag compoundTag3 = accepted.record();
        if (!(compoundTag3.equals((Object)(compoundTag2 = compoundTag.m_128469_("ValidatedOriginalWear").m_128469_(compoundTag3.m_128342_("Call").toString()))) && uUID.equals(compoundTag3.m_128342_("Weapon")) && serverPlayer.m_20148_().equals(compoundTag3.m_128342_("Owner")))) {
            throw new IOException("Live validated wear differs from its independent durable record");
        }
        if (!compoundTag2.m_128471_("ReturnedNormally") || !compoundTag2.m_128461_("PrimaryClass").isEmpty() || compoundTag2.m_128471_("OriginalRollbackEntered") || compoundTag2.m_128471_("SwallowedOriginalRollbackReturned")) {
            return null;
        }
        CompoundTag compoundTag4 = new CompoundTag();
        compoundTag4.m_128362_("Task", UUID.randomUUID());
        compoundTag4.m_128362_("Owner", serverPlayer.m_20148_());
        compoundTag4.m_128362_("Weapon", uUID);
        compoundTag4.m_128362_("AcceptedWearCall", compoundTag3.m_128342_("Call"));
        compoundTag4.m_128365_("Expected", (Tag)compoundTag3.m_128469_("Expected").m_6426_());
        compoundTag4.m_128359_("Rules", compoundTag3.m_128461_("Rules"));
        compoundTag4.m_128405_("AcceptedLevel", compoundTag3.m_128451_("Level"));
        compoundTag4.m_128405_("AcceptedMaximum", compoundTag3.m_128451_("Maximum"));
        compoundTag4.m_128359_("AcceptedProfile", compoundTag3.m_128461_("Profile"));
        compoundTag4.m_128385_("PolicyCredit", new int[3]);
        compoundTag4.m_128385_("PendingPolicyCredit", new int[3]);
        compoundTag4.m_128385_("ObservedCommittedPayments", new int[3]);
        compoundTag4.m_128379_("MigrationCommitted", true);
        compoundTag4.m_128359_("CreditClassification", "VALIDATED_ORIGINAL_WEAR_START_ZERO_CREDIT_NOT_LEGACY_MIGRATION");
        compoundTag4.m_128359_("State", "START_PREPARATION_NOT_PUBLISHED_TASK");
        OwnerStatueTaskPlan.start(compoundTag4.m_128469_("Expected"), compoundTag4.m_128451_("AcceptedLevel"), compoundTag4.m_128461_("AcceptedProfile"), compoundTag4.m_128451_("AcceptedMaximum"), candyStatueBlock.ownTint());
        Prepared prepared = new Prepared(ownerStatueTaskService, serverPlayer, itemStack, n, blockPos, candyStatueBlock.ownTint(), FourWeaponTransactions.Operation.START_REPAIR, n2, request, compoundTag, compoundTag4);
        try {
            prepared.origin = OwnerStatueTaskOrigin.reserve(accepted, prepared, ownerStatueTaskService.store, serverPlayer, itemStack, n, blockPos, candyStatueBlock.ownTint(), compoundTag4);
        }
        catch (Throwable throwable) {
            prepared.originFailed();
        }
        return prepared;
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
        private final FourWeaponTransactions.Operation operation;
        private final boolean starting;
        private final int task;
        private final FourWeaponTransactions.Request request;
        private final UUID transaction = UUID.randomUUID();
        private final Thread thread = Thread.currentThread();
        private final CompoundTag before;
        private final ListTag beforeInventory;
        private ListTag decidedInventory;
        private final Map<Integer, ItemStack> supplies = new HashMap<Integer, ItemStack>();
        private final Set<ItemStack> originalSupplies = Collections.newSetFromMap(new IdentityHashMap());
        private CompoundTag document;
        private boolean closed;
        private OwnerStatueTaskOrigin.Session origin;

        private Prepared(OwnerStatueTaskService ownerStatueTaskService, ServerPlayer serverPlayer, ItemStack itemStack, int n, BlockPos blockPos, int n2, FourWeaponTransactions.Operation operation, int n3, FourWeaponTransactions.Request request, CompoundTag compoundTag, CompoundTag compoundTag2) throws IOException {
            this.service = ownerStatueTaskService;
            this.player = serverPlayer;
            this.weapon = itemStack;
            this.slot = n;
            this.statue = blockPos.m_7949_();
            this.statueState = serverPlayer.m_9236_().m_8055_(blockPos);
            this.level = serverPlayer.m_284548_();
            this.tint = n2;
            this.operation = operation;
            this.starting = operation == FourWeaponTransactions.Operation.START_REPAIR;
            this.task = n3;
            this.request = request;
            this.document = compoundTag;
            this.before = compoundTag2.m_6426_();
            long l = compoundTag2.m_128454_("Sequence");
            if (l < 0L || l == Long.MAX_VALUE) {
                throw new IOException("Repair task sequence exhausted or invalid");
            }
            this.before.m_128356_("Sequence", l + 1L);
            this.before.m_128405_("Slot", n == 40 ? -106 : n);
            this.beforeInventory = serverPlayer.m_150109_().m_36026_(new ListTag()).m_6426_();
            ownerStatueTaskService.checkpointPlayer(serverPlayer, this.beforeInventory);
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.m_128362_("Transaction", this.transaction);
            compoundTag3.m_128362_("Task", compoundTag2.m_128342_("Task"));
            compoundTag3.m_128362_("Weapon", compoundTag2.m_128342_("Weapon"));
            if (request != null) {
                compoundTag3.m_128362_("Request", request.request());
            }
            compoundTag3.m_128359_("Phase", "PREPARED");
            compoundTag3.m_128359_("Operation", operation.name());
            compoundTag3.m_128379_("TaskAbsentBefore", this.starting);
            if (this.starting) {
                compoundTag3.m_128362_("AcceptedWearCall", this.before.m_128342_("AcceptedWearCall"));
            }
            compoundTag3.m_128359_("Dimension", serverPlayer.m_9236_().m_46472_().m_135782_().toString());
            compoundTag3.m_128356_("Statue", blockPos.m_121878_());
            compoundTag3.m_128405_("Tint", n2);
            compoundTag3.m_128405_("TaskIndex", n3);
            compoundTag3.m_128365_("BeforeTask", (Tag)this.before.m_6426_());
            compoundTag3.m_128365_("BeforeInventory", (Tag)this.beforeInventory.m_6426_());
            compoundTag3.m_128365_("Payments", (Tag)new ListTag());
            this.transition(compoundTag3);
        }

        void beforeOriginal(StatueRepairMutation statueRepairMutation) {
            try {
                OwnerStatueTaskOrigin.begin(this.origin, this, statueRepairMutation, this.operation, this.task, this.before, this.transaction);
            }
            catch (Throwable throwable) {
                this.originFailed();
            }
        }

        void originFailed() {
            try {
                OwnerStatueTaskOrigin.burn(this.origin);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }

        void afterOuterCommitted(StatueRepairMutation.Committed committed) {
            try {
                OwnerStatueTaskOrigin.outerCommitted(this.origin, this, committed);
            }
            catch (Throwable throwable) {
                this.originFailed();
            }
        }

        CompoundTag originOutcome(StatueRepairMutation.Committed committed) throws IOException {
            if (!this.closed || !committed.remains(this) || this.document.m_128441_("Journal") || this.decidedInventory == null || !this.decidedInventory.equals((Object)this.player.m_150109_().m_36026_(new ListTag()))) {
                throw new IOException("Task origin has no closed normal original checkpoint");
            }
            CompoundTag compoundTag = this.document.m_128469_("Tasks").m_128469_(this.before.m_128342_("Weapon").toString());
            if (!(compoundTag.m_128461_("State").equals("CHECKPOINTED_COMMIT") && this.transaction.equals(compoundTag.m_128342_("LastTransaction")) && this.before.m_128342_("Task").equals(compoundTag.m_128342_("Task")) && this.before.m_128342_("Owner").equals(this.player.m_20148_()) && this.before.m_128342_("Weapon").equals(FoodWeaponLifecycle.weaponId(this.weapon)))) {
                throw new IOException("Task origin checkpoint binding mismatch");
            }
            return compoundTag.m_6426_();
        }

        void consumeOriginGeneration(OwnerStatueTaskOrigin.Publication publication, boolean bl) throws IOException {
            String string;
            if (!this.closed || !publication.belongs(this) || this.origin == null || this.operation != FourWeaponTransactions.Operation.CLAIM_REPAIR || this.document.m_128441_("Journal") || !this.decidedInventory.equals((Object)this.player.m_150109_().m_36026_(new ListTag()))) {
                throw new IOException("Detached task generation durable consumer");
            }
            CompoundTag compoundTag = this.service.owner(this.player.m_20148_());
            if (!compoundTag.equals((Object)this.document)) {
                throw new IOException("Task generation owner CAS conflict");
            }
            CompoundTag compoundTag2 = this.document.m_6426_();
            CompoundTag compoundTag3 = compoundTag2.m_128469_("Tasks").m_128469_(this.before.m_128342_("Weapon").toString());
            if (!(compoundTag3.m_128471_("Consumed") && this.transaction.equals(compoundTag3.m_128342_("LastTransaction")) && this.before.m_128342_("Task").equals(compoundTag3.m_128342_("Task")))) {
                throw new IOException("Task generation has no original consumed claim");
            }
            String string2 = string = bl ? "CONSUMING_NO_RECOVERY_PUBLICATION" : "";
            if (!compoundTag3.m_128461_("GenerationDisposition").equals(string)) {
                throw new IOException("Task generation already consumed or uncertain");
            }
            compoundTag3.m_128359_("GenerationDisposition", bl ? "CONSUMED_NO_REISSUE_OR_RECOVERY_PUBLICATION" : "CONSUMING_NO_RECOVERY_PUBLICATION");
            compoundTag3.m_128362_("GenerationClaim", this.transaction);
            this.service.writeOwner(this.player.m_20148_(), this.document, compoundTag2);
            this.document = compoundTag2;
            this.service.checkpointSavedData(this.player.m_20148_(), compoundTag2);
        }

        private void check() throws IOException {
            if (this.starting && !StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerStatueTaskService.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == StatueRepairMutation.class && (stackFrame.getMethodName().equals("terminal") || stackFrame.getMethodName().equals("rollback"))).orElse(false)).booleanValue()) {
                throw new IOException("Detached START terminal participant");
            }
            if (this.closed || Thread.currentThread() != this.thread || !this.service.server.m_18695_() || this.player.m_20194_() != this.service.server || this.player.m_284548_() != this.level || this.player.m_150109_().m_8020_(this.slot) != this.weapon) {
                throw new IOException("Repair task participant source changed");
            }
        }

        private void transition(CompoundTag compoundTag) throws IOException {
            CompoundTag compoundTag2 = this.document.m_6426_();
            compoundTag2.m_128365_("Journal", (Tag)compoundTag.m_6426_());
            if (this.starting && !this.document.m_128441_("Journal")) {
                String string = this.before.m_128342_("AcceptedWearCall").toString();
                CompoundTag compoundTag3 = compoundTag2.m_128469_("ValidatedOriginalWear").m_128469_(string);
                if (!compoundTag3.m_128461_("Status").equals("AVAILABLE_LIVE_IDENTITY_ONLY") || !this.before.m_128469_("Expected").equals((Object)compoundTag3.m_128469_("Expected"))) {
                    throw new IOException("Validated wear START reservation conflict");
                }
                compoundTag3.m_128359_("Status", "RESERVED_BY_ORIGINAL_START_NO_REISSUE");
                compoundTag3.m_128362_("Transaction", this.transaction);
                compoundTag3.m_128362_("Task", this.before.m_128342_("Task"));
            }
            this.service.writeOwner(this.player.m_20148_(), this.document, compoundTag2);
            this.document = compoundTag2;
        }

        void beforeShrink(ItemStack itemStack, int n, int n2) throws IOException {
            this.check();
            if (this.operation != FourWeaponTransactions.Operation.SUBMIT_REPAIR || !this.document.m_128469_("Journal").m_128461_("Phase").equals("PREPARED") || n < 0 || n >= 36 || itemStack == this.weapon || this.player.m_150109_().m_8020_(n) != itemStack || n2 <= 0 || n2 > itemStack.m_41613_() || this.supplies.containsKey(n) || !this.originalSupplies.add(itemStack)) {
                throw new IOException("Repair original payment identity or phase mismatch");
            }
            CompoundTag compoundTag = this.document.m_128469_("Journal").m_6426_();
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.m_128405_("Slot", n);
            compoundTag2.m_128405_("Count", n2);
            compoundTag2.m_128405_("Before", itemStack.m_41613_());
            compoundTag2.m_128365_("BeforeImage", (Tag)itemStack.m_41739_(new CompoundTag()));
            compoundTag.m_128437_("Payments", 10).add((Object)compoundTag2);
            this.transition(compoundTag);
            this.supplies.put(n, itemStack);
        }

        void afterShrink(ItemStack itemStack, int n, int n2) throws IOException {
            this.check();
            CompoundTag compoundTag = this.document.m_128469_("Journal").m_6426_();
            ListTag listTag = compoundTag.m_128437_("Payments", 10);
            if (listTag.isEmpty()) {
                throw new IOException("Repair payment has no before observation");
            }
            CompoundTag compoundTag2 = listTag.m_128728_(listTag.size() - 1);
            if (compoundTag2.m_128471_("Returned") || compoundTag2.m_128451_("Slot") != n || compoundTag2.m_128451_("Count") != n2 || this.supplies.get(n) != itemStack || this.player.m_150109_().m_8020_(n) != itemStack || itemStack.m_41613_() != compoundTag2.m_128451_("Before") - n2) {
                throw new IOException("Repair original shrink result mismatch");
            }
            CompoundTag compoundTag3 = compoundTag2.m_128469_("BeforeImage").m_6426_();
            compoundTag3.m_128344_("Count", (byte)itemStack.m_41613_());
            if (itemStack.m_41613_() > 0 && !compoundTag3.equals((Object)itemStack.m_41739_(new CompoundTag()))) {
                throw new IOException("Repair supply fields changed");
            }
            compoundTag2.m_128405_("After", itemStack.m_41613_());
            compoundTag2.m_128379_("Returned", true);
            this.transition(compoundTag);
            OwnerStatueTaskOrigin.paid(this.origin, this, this.task, n2);
        }

        void decideAtOriginalTerminal() throws IOException {
            Object object;
            Object object2;
            CompoundTag compoundTag;
            Object object3;
            CompoundTag compoundTag2;
            this.check();
            CompoundTag compoundTag3 = this.document.m_128469_("Journal").m_6426_();
            if (!compoundTag3.m_128461_("Phase").equals("PREPARED") || !this.statueState.equals(this.level.m_8055_(this.statue))) {
                throw new IOException("Repair original terminal phase or statue mismatch");
            }
            ListTag listTag = compoundTag3.m_128437_("Payments", 10);
            int n = 0;
            for (int i = 0; i < listTag.size(); ++i) {
                compoundTag2 = listTag.m_128728_(i);
                object3 = this.supplies.get(compoundTag2.m_128451_("Slot"));
                if (!compoundTag2.m_128471_("Returned") || object3 == null || this.player.m_150109_().m_8020_(compoundTag2.m_128451_("Slot")) != object3 || object3.m_41613_() != compoundTag2.m_128451_("After")) {
                    throw new IOException("Repair payment has no coherent original return");
                }
                n = Math.addExact(n, compoundTag2.m_128451_("Count"));
            }
            CompoundTag compoundTag4 = compoundTag = this.starting ? OwnerStatueTaskPlan.start(this.before.m_128469_("Expected"), this.before.m_128451_("AcceptedLevel"), this.before.m_128461_("AcceptedProfile"), this.before.m_128451_("AcceptedMaximum"), this.tint) : OwnerStatueTaskPlan.after(this.before.m_128469_("Expected"), this.operation, this.task, n, this.tint);
            if (!compoundTag.equals((Object)this.weapon.m_41739_(new CompoundTag()))) {
                throw new IOException("Repair task exact transition mismatch");
            }
            compoundTag2 = this.player.m_150109_().m_36026_(new ListTag());
            object3 = Prepared.slots(this.beforeInventory);
            CompoundTag compoundTag5 = compoundTag.m_6426_();
            compoundTag5.m_128344_("Slot", (byte)(this.slot == 40 ? 150 : this.slot));
            object3.put(this.slot == 40 ? 150 : this.slot, compoundTag5);
            for (int i = 0; i < listTag.size(); ++i) {
                object2 = listTag.m_128728_(i);
                object = (CompoundTag)object3.get(object2.m_128451_("Slot"));
                if (object == null || object.m_128445_("Count") != object2.m_128451_("Before")) {
                    throw new IOException("Repair inventory payment baseline mismatch");
                }
                if (object2.m_128451_("After") == 0) {
                    object3.remove(object2.m_128451_("Slot"));
                    continue;
                }
                object.m_128344_("Count", (byte)object2.m_128451_("After"));
            }
            if (!object3.equals(Prepared.slots((ListTag)compoundTag2))) {
                throw new IOException("Repair unrelated inventory mutation");
            }
            CompoundTag compoundTag6 = this.before.m_6426_();
            if (!compoundTag6.m_128471_("MigrationCommitted")) {
                compoundTag6.m_128385_("PolicyCredit", compoundTag6.m_128465_("PendingPolicyCredit"));
                compoundTag6.m_128379_("MigrationCommitted", true);
                compoundTag6.m_128359_("CreditClassification", OwnerStatueTaskService.POLICY);
            }
            if (((CompoundTag)(object2 = (Object)compoundTag6.m_128465_("ObservedCommittedPayments"))).length != 3) {
                throw new IOException("Invalid repair payment accounting");
            }
            if (this.operation == FourWeaponTransactions.Operation.SUBMIT_REPAIR) {
                object2[this.task] = (CompoundTag)Math.addExact((int)object2[this.task], n);
            }
            compoundTag6.m_128385_("ObservedCommittedPayments", (int[])object2);
            compoundTag6.m_128365_("Expected", (Tag)compoundTag);
            compoundTag6.m_128359_("Rules", OwnerStatueTaskPlan.rules(this.weapon));
            compoundTag6.m_128362_("LastTransaction", this.transaction);
            compoundTag6.m_128359_("State", "ORIGINAL_TERMINAL_DECISION");
            if (this.operation == FourWeaponTransactions.Operation.CLAIM_REPAIR) {
                compoundTag6.m_128379_("Consumed", true);
            }
            if (!compoundTag6.m_128471_("Consumed")) {
                object = compoundTag.m_128469_("tag").m_128469_("FTBossWeaponLifecycle").m_128469_("RepairQuest").m_128465_("Progress");
                int[] nArray = compoundTag6.m_128465_("PolicyCredit");
                if (((CompoundTag)object).length != 3 || nArray.length != 3) {
                    throw new IOException("Invalid repair progress accounting");
                }
                for (int i = 0; i < 3; ++i) {
                    if (nArray[i] >= 0 && object2[i] >= 0 && Math.addExact(nArray[i], (int)object2[i]) == object[i]) continue;
                    throw new IOException("Repair progress origin mismatch");
                }
            }
            compoundTag3.m_128365_("AfterTask", (Tag)compoundTag6);
            this.decidedInventory = compoundTag2.m_6426_();
            compoundTag3.m_128365_("AfterInventory", (Tag)this.decidedInventory.m_6426_());
            compoundTag3.m_128359_("Phase", "DECIDED");
            this.transition(compoundTag3);
        }

        void beforeOriginalRollback() throws IOException {
            this.originFailed();
            this.check();
            CompoundTag compoundTag = this.document.m_128425_("Journal", 10) ? this.document.m_128469_("Journal").m_6426_() : new CompoundTag();
            compoundTag.m_128362_("Transaction", this.transaction);
            compoundTag.m_128362_("Task", this.before.m_128342_("Task"));
            compoundTag.m_128362_("Weapon", this.before.m_128342_("Weapon"));
            if (this.request != null) {
                compoundTag.m_128362_("Request", this.request.request());
            }
            compoundTag.m_128365_("BeforeTask", (Tag)this.before.m_6426_());
            compoundTag.m_128379_("TaskAbsentBefore", this.starting);
            if (this.starting) {
                compoundTag.m_128362_("AcceptedWearCall", this.before.m_128342_("AcceptedWearCall"));
            }
            compoundTag.m_128365_("BeforeInventory", (Tag)this.beforeInventory.m_6426_());
            if (this.decidedInventory != null) {
                compoundTag.m_128365_("AfterInventory", (Tag)this.decidedInventory.m_6426_());
            }
            compoundTag.m_128359_("Phase", "ABORTING");
            CompoundTag compoundTag2 = this.document.m_6426_();
            compoundTag2.m_128365_("Journal", (Tag)compoundTag);
            if (this.starting) {
                compoundTag2.m_128469_("Tasks").m_128473_(this.before.m_128342_("Weapon").toString());
            } else {
                compoundTag2.m_128469_("Tasks").m_128365_(this.before.m_128342_("Weapon").toString(), (Tag)this.before.m_6426_());
            }
            this.service.writeOwner(this.player.m_20148_(), this.document, compoundTag2);
            this.document = compoundTag2;
        }

        void checkpointAfterOriginalSaveOrRollback() throws IOException {
            this.check();
            CompoundTag compoundTag = this.document.m_128469_("Journal");
            boolean bl = compoundTag.m_128461_("Phase").equals("DECIDED");
            if (!bl && !compoundTag.m_128461_("Phase").equals("ABORTING")) {
                throw new IOException("Repair checkpoint has no decision");
            }
            ListTag listTag = compoundTag.m_128437_(bl ? "AfterInventory" : "BeforeInventory", 10);
            if (!listTag.equals((Object)this.player.m_150109_().m_36026_(new ListTag()))) {
                throw new IOException("Repair live checkpoint mismatch");
            }
            this.service.checkpointPlayer(this.player, listTag);
            CompoundTag compoundTag2 = this.document.m_6426_();
            CompoundTag compoundTag3 = compoundTag.m_128469_(bl ? "AfterTask" : "BeforeTask").m_6426_();
            compoundTag3.m_128359_("State", bl ? "CHECKPOINTED_COMMIT" : "CHECKPOINTED_ABORT_NO_CREDIT");
            if (this.starting && !bl) {
                compoundTag2.m_128469_("Tasks").m_128473_(this.before.m_128342_("Weapon").toString());
            } else {
                compoundTag2.m_128469_("Tasks").m_128365_(this.before.m_128342_("Weapon").toString(), (Tag)compoundTag3);
            }
            if (this.starting) {
                OwnerStatueTaskService.settleStartReceipt(compoundTag2, compoundTag, bl);
            }
            if (this.request != null) {
                compoundTag2.m_128469_("Requests").m_128359_(this.request.request().toString(), (bl ? "COMMITTED:" : "ABORTED:") + this.transaction);
            }
            compoundTag2.m_128473_("Journal");
            this.service.writeOwner(this.player.m_20148_(), this.document, compoundTag2);
            this.document = compoundTag2;
            this.service.checkpointSavedData(this.player.m_20148_(), compoundTag2);
            this.closed = true;
        }

        private static Map<Integer, CompoundTag> slots(ListTag listTag) throws IOException {
            HashMap<Integer, CompoundTag> hashMap = new HashMap<Integer, CompoundTag>();
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag = listTag.m_128728_(i).m_6426_();
                if (hashMap.put(compoundTag.m_128445_("Slot") & 0xFF, compoundTag) == null) continue;
                throw new IOException("Duplicate repair inventory slot");
            }
            return hashMap;
        }
    }
}

