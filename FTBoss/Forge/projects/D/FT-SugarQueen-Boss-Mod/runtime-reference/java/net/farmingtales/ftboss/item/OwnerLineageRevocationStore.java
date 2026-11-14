/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.world.level.storage.LevelResource
 */
package net.farmingtales.ftboss.item;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.OwnerRepairContinuation;
import net.farmingtales.ftboss.item.OwnerStatueTaskOrigin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

final class OwnerLineageRevocationStore {
    private static final int MAGIC = 1179931193;
    private static final int BODY_BYTES = 77;
    private static final int RECORD_BYTES = 109;
    private static final Object[] STRIPES = new Object[64];
    private final Path directory;
    private final byte[] key;

    private OwnerLineageRevocationStore(Path path, byte[] byArray) {
        this.directory = path;
        this.key = byArray;
    }

    private void repairIoBoundary() throws IOException {
        if (Thread.holdsLock(FoodWeaponSourceLedger.class) || Thread.holdsLock(OwnerFullRepairGuard.class) || Thread.holdsLock(OwnerFieldExpectedAuthority.class)) {
            throw new IOException("Repair document IO under authority monitor");
        }
    }

    private Path repairDocumentPath(UUID uUID, boolean bl) throws IOException {
        if (bl) {
            return this.directory.resolve("repair-cutover-v1.bin");
        }
        Path path = this.directory.resolve(uUID.toString());
        if (Files.isSymbolicLink(path)) {
            throw new IOException("Repair owner directory is a link");
        }
        Files.createDirectories(path, new FileAttribute[0]);
        return path.resolve("repair-tasks-v1.bin");
    }

    private CompoundTag readRepairDocument(Path path, UUID uUID, boolean bl) throws IOException {
        CompoundTag compoundTag;
        if (!Files.exists(path, new LinkOption[0])) {
            return new CompoundTag();
        }
        if (Files.isSymbolicLink(path) || Files.size(path) < 33L || Files.size(path) > 0x800000L) {
            throw new IOException("Repair document budget or path");
        }
        byte[] byArray = Files.readAllBytes(path);
        byte[] byArray2 = Arrays.copyOf(byArray, byArray.length - 32);
        if (!MessageDigest.isEqual(this.mac(byArray2), Arrays.copyOfRange(byArray, byArray2.length, byArray.length))) {
            throw new IOException("Repair document authentication failed");
        }
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(byArray2));
        try {
            if (!dataInputStream.readUTF().equals(bl ? "ftboss/repair-cutover/v1" : "ftboss/repair-owner/v1") || !new UUID(dataInputStream.readLong(), dataInputStream.readLong()).equals(uUID)) {
                throw new IOException("Repair document domain mismatch");
            }
            CompoundTag compoundTag2 = NbtIo.m_128928_((DataInput)dataInputStream);
            if (compoundTag2 == null || dataInputStream.available() != 0) {
                throw new IOException("Repair document trailing or missing data");
            }
            compoundTag = compoundTag2;
        }
        catch (Throwable throwable) {
            try {
                try {
                    dataInputStream.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (RuntimeException runtimeException) {
                throw new IOException("Invalid repair document", runtimeException);
            }
        }
        dataInputStream.close();
        return compoundTag;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    CompoundTag readRepairDocument(UUID uUID, boolean bl) throws IOException {
        this.repairIoBoundary();
        Object object = OwnerLineageRevocationStore.stripe(uUID, uUID);
        synchronized (object) {
            Path path = this.repairDocumentPath(uUID, bl);
            Path path2 = path.resolveSibling(path.getFileName() + ".lock");
            if (Files.isSymbolicLink(path2)) {
                throw new IOException("Repair lock is a link");
            }
            try (FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.CREATE, StandardOpenOption.WRITE);){
                CompoundTag compoundTag;
                block16: {
                    FileLock fileLock = fileChannel.lock();
                    try {
                        compoundTag = this.readRepairDocument(path, uUID, bl);
                        if (fileLock == null) break block16;
                    }
                    catch (Throwable throwable) {
                        if (fileLock != null) {
                            try {
                                fileLock.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    fileLock.close();
                }
                return compoundTag;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void writeRepairDocument(UUID uUID, boolean bl, CompoundTag compoundTag, CompoundTag compoundTag2) throws IOException {
        this.repairIoBoundary();
        Object object = OwnerLineageRevocationStore.stripe(uUID, uUID);
        synchronized (object) {
            Path path = this.repairDocumentPath(uUID, bl);
            Path path2 = path.resolveSibling(path.getFileName() + ".lock");
            if (Files.isSymbolicLink(path2)) {
                throw new IOException("Repair lock is a link");
            }
            try (FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 FileLock fileLock = fileChannel.lock();){
                if (!this.readRepairDocument(path, uUID, bl).equals((Object)compoundTag)) {
                    throw new IOException("Repair document compare-and-swap rejected");
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try (Object object2 = new DataOutputStream(byteArrayOutputStream);){
                    ((DataOutputStream)object2).writeUTF(bl ? "ftboss/repair-cutover/v1" : "ftboss/repair-owner/v1");
                    ((DataOutputStream)object2).writeLong(uUID.getMostSignificantBits());
                    ((DataOutputStream)object2).writeLong(uUID.getLeastSignificantBits());
                    NbtIo.m_128941_((CompoundTag)compoundTag2.m_6426_(), (DataOutput)object2);
                }
                object2 = byteArrayOutputStream.toByteArray();
                if (((Object)object2).length > 8388576) {
                    throw new IOException("Repair document output budget");
                }
                OwnerLineageRevocationStore.writeAtomic(path, ByteBuffer.allocate(((Object)object2).length + 32).put((byte[])object2).put(this.mac((byte[])object2)).array());
                if (!this.readRepairDocument(path, uUID, bl).equals((Object)compoundTag2)) {
                    throw new IOException("Repair document readback failed");
                }
            }
        }
    }

    static OwnerLineageRevocationStore open(MinecraftServer minecraftServer) throws IOException {
        Path path = minecraftServer.m_129843_(LevelResource.f_78182_).toAbsolutePath().normalize().resolve("ftboss-authority");
        Files.createDirectories(path, new FileAttribute[0]);
        Path path2 = path.resolve("authority-key.lock");
        try (FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.CREATE, StandardOpenOption.WRITE);){
            OwnerLineageRevocationStore ownerLineageRevocationStore;
            block15: {
                FileLock fileLock = fileChannel.lock();
                try {
                    byte[] byArray;
                    Path path3 = path.resolve("authority-key.bin");
                    if (Files.exists(path3, new LinkOption[0])) {
                        if (Files.size(path3) != 32L) {
                            throw new IOException("Invalid authority key record");
                        }
                        byArray = Files.readAllBytes(path3);
                    } else {
                        byArray = new byte[32];
                        new SecureRandom().nextBytes(byArray);
                        OwnerLineageRevocationStore.writeAtomic(path3, byArray);
                    }
                    ownerLineageRevocationStore = new OwnerLineageRevocationStore(path, byArray);
                    if (fileLock == null) break block15;
                }
                catch (Throwable throwable) {
                    if (fileLock != null) {
                        try {
                            fileLock.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                fileLock.close();
            }
            return ownerLineageRevocationStore;
        }
    }

    private static void writeAtomic(Path path, byte[] byArray) throws IOException {
        Path path2 = path.resolveSibling(path.getFileName().toString() + ".tmp");
        try (FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);){
            ByteBuffer byteBuffer = ByteBuffer.wrap(byArray);
            while (byteBuffer.hasRemaining()) {
                fileChannel.write(byteBuffer);
            }
            fileChannel.force(true);
        }
        Files.move(path2, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    private Path recordPath(UUID uUID, UUID uUID2) throws IOException {
        Path path = this.directory.resolve(uUID.toString());
        Files.createDirectories(path, new FileAttribute[0]);
        return path.resolve(uUID2.toString() + ".bin");
    }

    private byte[] mac(byte[] byArray) throws IOException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(this.key, "HmacSHA256"));
            return mac.doFinal(byArray);
        }
        catch (GeneralSecurityException generalSecurityException) {
            throw new IOException(generalSecurityException);
        }
    }

    private static void uuid(ByteBuffer byteBuffer, UUID uUID) {
        byteBuffer.putLong(uUID.getMostSignificantBits()).putLong(uUID.getLeastSignificantBits());
    }

    private static UUID uuid(ByteBuffer byteBuffer) {
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }

    private byte[] encode(Record record) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(77);
        byteBuffer.putInt(1179931193);
        OwnerLineageRevocationStore.uuid(byteBuffer, record.owner);
        OwnerLineageRevocationStore.uuid(byteBuffer, record.weapon);
        OwnerLineageRevocationStore.uuid(byteBuffer, record.incarnation);
        byteBuffer.putLong(record.generation);
        OwnerLineageRevocationStore.uuid(byteBuffer, record.transaction);
        byteBuffer.put((byte)(record.active ? 1 : 0));
        return ByteBuffer.allocate(109).put(byteBuffer.array()).put(this.mac(byteBuffer.array())).array();
    }

    private Record read(Path path, UUID uUID, UUID uUID2) throws IOException {
        if (!Files.exists(path, new LinkOption[0])) {
            return null;
        }
        if (Files.size(path) != 109L) {
            throw new IOException("Invalid lineage record size");
        }
        byte[] byArray = Files.readAllBytes(path);
        byte[] byArray2 = Arrays.copyOf(byArray, 77);
        if (!MessageDigest.isEqual(this.mac(byArray2), Arrays.copyOfRange(byArray, 77, 109))) {
            throw new IOException("Lineage record authentication failed");
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(byArray2);
        if (byteBuffer.getInt() != 1179931193) {
            throw new IOException("Lineage record version");
        }
        UUID uUID3 = OwnerLineageRevocationStore.uuid(byteBuffer);
        UUID uUID4 = OwnerLineageRevocationStore.uuid(byteBuffer);
        UUID uUID5 = OwnerLineageRevocationStore.uuid(byteBuffer);
        long l = byteBuffer.getLong();
        UUID uUID6 = OwnerLineageRevocationStore.uuid(byteBuffer);
        byte by = byteBuffer.get();
        if (!uUID.equals(uUID3) || !uUID2.equals(uUID4) || l < 0L || by < 0 || by > 1) {
            throw new IOException("Lineage record domain mismatch");
        }
        return new Record(uUID, uUID2, uUID5, l, uUID6, by == 1);
    }

    private static Object stripe(UUID uUID, UUID uUID2) {
        return STRIPES[(uUID.hashCode() ^ uUID2.hashCode()) & STRIPES.length - 1];
    }

    private static boolean matches(Record record, Lease lease) {
        return record != null && record.owner.equals(lease.owner) && record.weapon.equals(lease.weapon) && record.incarnation.equals(lease.incarnation) && record.generation == lease.generation;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    boolean rejectsKnownLineage(UUID uUID, UUID uUID2) throws IOException {
        this.repairIoBoundary();
        if (uUID == null || uUID2 == null) {
            throw new IOException("Missing original wear lineage domain");
        }
        Object object = OwnerLineageRevocationStore.stripe(uUID, uUID2);
        synchronized (object) {
            Path path = this.directory.resolve(uUID.toString());
            Path path2 = path.resolve(uUID2 + ".bin");
            Path path3 = path.resolve(uUID2 + ".lock");
            if (Files.isSymbolicLink(this.directory) || Files.isSymbolicLink(path) || Files.isSymbolicLink(path2) || Files.isSymbolicLink(path3)) {
                throw new IOException("Original wear lineage path is a link");
            }
            if (!Files.exists(path2, new LinkOption[0])) {
                return false;
            }
            try (FileChannel fileChannel = FileChannel.open(path3, StandardOpenOption.CREATE, StandardOpenOption.WRITE);){
                boolean bl;
                block18: {
                    FileLock fileLock = fileChannel.lock();
                    try {
                        Record record = this.read(path2, uUID, uUID2);
                        boolean bl2 = bl = record != null && !record.active;
                        if (fileLock == null) break block18;
                    }
                    catch (Throwable throwable) {
                        if (fileLock != null) {
                            try {
                                fileLock.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    fileLock.close();
                }
                return bl;
            }
        }
    }

    static void retirePrewriteLease(Lease lease) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerRepairContinuation.class).orElse(false));
        if (!bl) {
            throw new IllegalStateException("Detached local prewrite retirement");
        }
        if (lease != null) {
            lease.retired = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void consumePrewriteTrack(OwnerRepairContinuation.Track track) throws IOException {
        this.repairIoBoundary();
        CompoundTag compoundTag = track.takeForStore(this);
        UUID uUID = compoundTag.m_128342_("Owner");
        UUID uUID2 = compoundTag.m_128342_("Weapon");
        Object object = OwnerLineageRevocationStore.stripe(uUID, uUID2);
        synchronized (object) {
            Path path = this.directory.resolve(uUID.toString());
            Path path2 = path.resolve(uUID2 + ".bin");
            Path path3 = path.resolve(uUID2 + ".lock");
            if (Files.isSymbolicLink(this.directory) || Files.isSymbolicLink(path) || Files.isSymbolicLink(path2) || Files.isSymbolicLink(path3) || !Files.isRegularFile(path2, new LinkOption[0])) {
                throw new IOException("Prewrite lineage missing or linked");
            }
            try (FileChannel fileChannel = FileChannel.open(path3, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 FileLock fileLock = fileChannel.lock();){
                Record record = this.read(path2, uUID, uUID2);
                if (!(record != null && record.active && record.generation >= 0L && record.generation != Long.MAX_VALUE && record.generation == compoundTag.m_128454_("Generation") && record.incarnation.equals(compoundTag.m_128342_("Incarnation")) && record.transaction.equals(compoundTag.m_128342_("ParentClaim")))) {
                    throw new IOException("Prewrite predecessor CAS rejected");
                }
                Record record2 = new Record(record.owner, record.weapon, record.incarnation, record.generation, compoundTag.m_128342_("Track"), false);
                OwnerLineageRevocationStore.writeAtomic(path2, this.encode(record2));
                Record record3 = this.read(path2, uUID, uUID2);
                if (record3 == null || record3.active || record3.generation != record2.generation || !record3.incarnation.equals(record2.incarnation) || !record3.transaction.equals(record2.transaction)) {
                    throw new IOException("Prewrite inactive readback changed");
                }
            }
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    TaskReservation reserveTaskOrigin(OwnerStatueTaskOrigin.Session session) throws IOException {
        this.repairIoBoundary();
        CompoundTag compoundTag = session.reservationBinding(this);
        UUID uUID = compoundTag.m_128342_("Owner");
        UUID uUID2 = compoundTag.m_128342_("Weapon");
        UUID uUID3 = compoundTag.m_128342_("Task");
        UUID uUID4 = UUID.nameUUIDFromBytes(("ftboss/task-origin/v1/" + compoundTag.m_128342_("OriginalLoad") + "/" + compoundTag.m_128342_("OriginalCall") + "/" + uUID3).getBytes(StandardCharsets.UTF_8));
        Object object = OwnerLineageRevocationStore.stripe(uUID, uUID2);
        synchronized (object) {
            Path path = this.recordPath(uUID, uUID2);
            Path path2 = path.resolveSibling(uUID2 + ".lock");
            if (Files.isSymbolicLink(path2)) {
                throw new IOException("Task-origin lock is a link");
            }
            try (FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.CREATE, StandardOpenOption.WRITE);){
                TaskReservation taskReservation;
                block17: {
                    FileLock fileLock = fileChannel.lock();
                    try {
                        if (this.read(path, uUID, uUID2) != null) {
                            throw new IOException("Task-origin cannot rebind an existing lineage");
                        }
                        TaskReservation taskReservation2 = new TaskReservation(this, compoundTag, uUID4);
                        OwnerLineageRevocationStore.writeAtomic(path, this.encode(new Record(uUID, uUID2, uUID4, 0L, uUID3, false)));
                        taskReservation = taskReservation2;
                        if (fileLock == null) break block17;
                    }
                    catch (Throwable throwable) {
                        if (fileLock != null) {
                            try {
                                fileLock.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    fileLock.close();
                }
                return taskReservation;
            }
        }
    }

    /*
     * Exception decompiling
     */
    Lease consumeTaskOrigin(OwnerStatueTaskOrigin.Publication var1_1) throws IOException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 4 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    /*
     * Enabled aggressive exception aggregation
     */
    Lease admit(UUID uUID, UUID uUID2) throws IOException {
        Object object = OwnerLineageRevocationStore.stripe(uUID, uUID2);
        synchronized (object) {
            Path path = this.recordPath(uUID, uUID2);
            try (FileChannel fileChannel = FileChannel.open(path.resolveSibling(uUID2 + ".lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);){
                Lease lease;
                block15: {
                    FileLock fileLock = fileChannel.lock();
                    try {
                        Record record = this.read(path, uUID, uUID2);
                        UUID uUID3 = record == null || record.generation == Long.MAX_VALUE ? UUID.randomUUID() : record.incarnation;
                        long l = record == null || record.generation == Long.MAX_VALUE ? 0L : record.generation + 1L;
                        Record record2 = new Record(uUID, uUID2, uUID3, l, UUID.randomUUID(), true);
                        OwnerLineageRevocationStore.writeAtomic(path, this.encode(record2));
                        lease = new Lease(this, record2);
                        if (fileLock == null) break block15;
                    }
                    catch (Throwable throwable) {
                        if (fileLock != null) {
                            try {
                                fileLock.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    fileLock.close();
                }
                return lease;
            }
        }
    }

    /*
     * Loose catch block
     * Enabled aggressive exception aggregation
     */
    Advance begin(Lease lease, UUID uUID) throws IOException {
        if (lease == null || lease.store != this || uUID == null) {
            return null;
        }
        lease.retired = true;
        Object object = OwnerLineageRevocationStore.stripe(lease.owner, lease.weapon);
        synchronized (object) {
            Advance advance;
            FileLock fileLock;
            FileChannel fileChannel;
            block20: {
                Record record;
                Path path;
                block18: {
                    Advance advance2;
                    block19: {
                        path = this.recordPath(lease.owner, lease.weapon);
                        fileChannel = FileChannel.open(path.resolveSibling(lease.weapon + ".lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        fileLock = fileChannel.lock();
                        record = this.read(path, lease.owner, lease.weapon);
                        if (OwnerLineageRevocationStore.matches(record, lease) && record.active) break block18;
                        advance2 = null;
                        if (fileLock == null) break block19;
                        fileLock.close();
                    }
                    return advance2;
                }
                OwnerLineageRevocationStore.writeAtomic(path, this.encode(new Record(record.owner, record.weapon, record.incarnation, record.generation, uUID, false)));
                advance = new Advance(lease, uUID);
                if (fileLock == null) break block20;
                fileLock.close();
            }
            return advance;
            {
                catch (Throwable throwable) {
                    if (fileLock != null) {
                        try {
                            fileLock.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
            }
            finally {
                if (fileChannel != null) {
                    fileChannel.close();
                }
            }
        }
    }

    /*
     * Loose catch block
     * Enabled aggressive exception aggregation
     */
    Lease commit(Advance advance) throws IOException {
        if (advance == null || advance.previous.store != this || advance.committed) {
            return null;
        }
        Lease lease = advance.previous;
        Object object = OwnerLineageRevocationStore.stripe(lease.owner, lease.weapon);
        synchronized (object) {
            Lease lease2;
            FileLock fileLock;
            FileChannel fileChannel;
            block20: {
                Lease lease3;
                Record record;
                Path path;
                block18: {
                    Lease lease4;
                    block19: {
                        path = this.recordPath(lease.owner, lease.weapon);
                        fileChannel = FileChannel.open(path.resolveSibling(lease.weapon + ".lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        fileLock = fileChannel.lock();
                        record = this.read(path, lease.owner, lease.weapon);
                        if (OwnerLineageRevocationStore.matches(record, lease) && !record.active && record.transaction.equals(advance.transaction)) break block18;
                        lease4 = null;
                        if (fileLock == null) break block19;
                        fileLock.close();
                    }
                    return lease4;
                }
                UUID uUID = record.generation == Long.MAX_VALUE ? UUID.randomUUID() : record.incarnation;
                long l = record.generation == Long.MAX_VALUE ? 0L : record.generation + 1L;
                Record record2 = new Record(record.owner, record.weapon, uUID, l, advance.transaction, true);
                advance.candidate = lease3 = new Lease(this, record2);
                OwnerLineageRevocationStore.writeAtomic(path, this.encode(record2));
                advance.committed = true;
                lease2 = lease3;
                if (fileLock == null) break block20;
                fileLock.close();
            }
            return lease2;
            {
                catch (Throwable throwable) {
                    if (fileLock != null) {
                        try {
                            fileLock.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
            }
            finally {
                if (fileChannel != null) {
                    fileChannel.close();
                }
            }
        }
    }

    boolean owns(Lease lease) {
        return lease != null && lease.store == this;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean live(Lease lease) {
        if (lease == null || lease.store != this || lease.retired) {
            return false;
        }
        Object object = OwnerLineageRevocationStore.stripe(lease.owner, lease.weapon);
        synchronized (object) {
            if (lease.retired) {
                return false;
            }
            try {
                Record record = this.read(this.recordPath(lease.owner, lease.weapon), lease.owner, lease.weapon);
                return OwnerLineageRevocationStore.matches(record, lease) && record.active && !lease.retired;
            }
            catch (IOException iOException) {
                lease.retired = true;
                return false;
            }
        }
    }

    static boolean locallyLive(Lease lease) {
        return lease != null && !lease.retired;
    }

    static boolean liveLease(Lease lease) {
        return lease != null && lease.store.live(lease);
    }

    static void retireLease(Lease lease) {
        if (lease != null) {
            lease.store.retire(lease);
        }
    }

    static Advance beginLease(Lease lease, UUID uUID) throws IOException {
        return lease == null ? null : lease.store.begin(lease, uUID);
    }

    static Lease commitAdvance(Advance advance) throws IOException {
        return advance == null ? null : advance.previous.store.commit(advance);
    }

    void retire(Lease lease) {
        if (lease == null || lease.store != this || lease.retired) {
            return;
        }
        try {
            this.begin(lease, UUID.randomUUID());
        }
        catch (IOException iOException) {
            lease.retired = true;
        }
    }

    static void abortAdvance(Advance advance) {
        if (advance == null || advance.candidate == null) {
            return;
        }
        Lease lease = advance.candidate;
        try {
            lease.store.begin(lease, UUID.randomUUID());
        }
        catch (IOException iOException) {
            lease.retired = true;
        }
    }

    static {
        for (int i = 0; i < STRIPES.length; ++i) {
            OwnerLineageRevocationStore.STRIPES[i] = new Object();
        }
    }

    private record Record(UUID owner, UUID weapon, UUID incarnation, long generation, UUID transaction, boolean active) {
    }

    static final class Lease {
        private final OwnerLineageRevocationStore store;
        final UUID owner;
        final UUID weapon;
        final UUID incarnation;
        final long generation;
        private volatile boolean retired;

        private Lease(OwnerLineageRevocationStore ownerLineageRevocationStore, Record record) {
            this.store = ownerLineageRevocationStore;
            this.owner = record.owner;
            this.weapon = record.weapon;
            this.incarnation = record.incarnation;
            this.generation = record.generation;
        }
    }

    static final class TaskReservation {
        private final OwnerLineageRevocationStore store;
        final UUID owner;
        final UUID weapon;
        final UUID task;
        final UUID incarnation;
        private UUID claim;
        private boolean taken;

        private TaskReservation(OwnerLineageRevocationStore ownerLineageRevocationStore, CompoundTag compoundTag, UUID uUID) {
            this.store = ownerLineageRevocationStore;
            this.owner = compoundTag.m_128342_("Owner");
            this.weapon = compoundTag.m_128342_("Weapon");
            this.task = compoundTag.m_128342_("Task");
            this.incarnation = uUID;
        }
    }

    static final class Advance {
        private final Lease previous;
        private final UUID transaction;
        private boolean committed;
        private Lease candidate;

        private Advance(Lease lease, UUID uUID) {
            this.previous = lease;
            this.transaction = uUID;
        }
    }
}

