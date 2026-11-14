package net.farmingtales.ftboss.item;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.StackWalker.Option;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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

    private OwnerLineageRevocationStore(Path var1, byte[] var2) {
        this.directory = var1;
        this.key = var2;
    }

    private void repairIoBoundary() throws IOException {
        if (Thread.holdsLock(FoodWeaponSourceLedger.class)
            || Thread.holdsLock(OwnerFullRepairGuard.class)
            || Thread.holdsLock(OwnerFieldExpectedAuthority.class)) {
            throw new IOException("Repair document IO under authority monitor");
        }
    }

    private Path repairDocumentPath(UUID var1, boolean var2) throws IOException {
        if (var2) {
            return this.directory.resolve("repair-cutover-v1.bin");
        } else {
            Path var3 = this.directory.resolve(var1.toString());
            if (Files.isSymbolicLink(var3)) {
                throw new IOException("Repair owner directory is a link");
            } else {
                Files.createDirectories(var3);
                return var3.resolve("repair-tasks-v1.bin");
            }
        }
    }

    private CompoundTag readRepairDocument(Path var1, UUID var2, boolean var3) throws IOException {
        if (!Files.exists(var1)) {
            return new CompoundTag();
        } else if (!Files.isSymbolicLink(var1) && Files.size(var1) >= 33L && Files.size(var1) <= 8388608L) {
            byte[] var4 = Files.readAllBytes(var1);
            byte[] var5 = Arrays.copyOf(var4, var4.length - 32);
            if (!MessageDigest.isEqual(this.mac(var5), Arrays.copyOfRange(var4, var5.length, var4.length))) {
                throw new IOException("Repair document authentication failed");
            } else {
                try {
                    CompoundTag var8;
                    try (DataInputStream var6 = new DataInputStream(new ByteArrayInputStream(var5))) {
                        if (!var6.readUTF().equals(var3 ? "ftboss/repair-cutover/v1" : "ftboss/repair-owner/v1")
                            || !new UUID(var6.readLong(), var6.readLong()).equals(var2)) {
                            throw new IOException("Repair document domain mismatch");
                        }

                        CompoundTag var7 = NbtIo.read(var6);
                        if (var7 == null || var6.available() != 0) {
                            throw new IOException("Repair document trailing or missing data");
                        }

                        var8 = var7;
                    }

                    return var8;
                } catch (RuntimeException var11) {
                    throw new IOException("Invalid repair document", var11);
                }
            }
        } else {
            throw new IOException("Repair document budget or path");
        }
    }

    CompoundTag readRepairDocument(UUID var1, boolean var2) throws IOException {
        this.repairIoBoundary();
        synchronized (stripe(var1, var1)) {
            Path var4 = this.repairDocumentPath(var1, var2);
            Path var5 = var4.resolveSibling(var4.getFileName() + ".lock");
            if (Files.isSymbolicLink(var5)) {
                throw new IOException("Repair lock is a link");
            } else {
                CompoundTag var8;
                try (
                    FileChannel var6 = FileChannel.open(var5, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    FileLock var7 = var6.lock();
                ) {
                    var8 = this.readRepairDocument(var4, var1, var2);
                }

                return var8;
            }
        }
    }

    void writeRepairDocument(UUID var1, boolean var2, CompoundTag var3, CompoundTag var4) throws IOException {
        this.repairIoBoundary();
        synchronized (stripe(var1, var1)) {
            Path var6 = this.repairDocumentPath(var1, var2);
            Path var7 = var6.resolveSibling(var6.getFileName() + ".lock");
            if (Files.isSymbolicLink(var7)) {
                throw new IOException("Repair lock is a link");
            } else {
                try (
                    FileChannel var8 = FileChannel.open(var7, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    FileLock var9 = var8.lock();
                ) {
                    if (!this.readRepairDocument(var6, var1, var2).equals(var3)) {
                        throw new IOException("Repair document compare-and-swap rejected");
                    }

                    ByteArrayOutputStream var10 = new ByteArrayOutputStream();
                    DataOutputStream var11 = new DataOutputStream(var10);

                    try {
                        var11.writeUTF(var2 ? "ftboss/repair-cutover/v1" : "ftboss/repair-owner/v1");
                        var11.writeLong(var1.getMostSignificantBits());
                        var11.writeLong(var1.getLeastSignificantBits());
                        NbtIo.write(var4.copy(), var11);
                    } catch (Throwable var18) {
                        try {
                            var11.close();
                        } catch (Throwable var17) {
                            var18.addSuppressed(var17);
                        }

                        throw var18;
                    }

                    var11.close();
                    byte[] var22 = var10.toByteArray();
                    if (var22.length > 8388576) {
                        throw new IOException("Repair document output budget");
                    }

                    writeAtomic(var6, ByteBuffer.allocate(var22.length + 32).put(var22).put(this.mac(var22)).array());
                    if (!this.readRepairDocument(var6, var1, var2).equals(var4)) {
                        throw new IOException("Repair document readback failed");
                    }
                }
            }
        }
    }

    static OwnerLineageRevocationStore open(MinecraftServer var0) throws IOException {
        Path var1 = var0.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().resolve("ftboss-authority");
        Files.createDirectories(var1);
        Path var2 = var1.resolve("authority-key.lock");

        OwnerLineageRevocationStore var7;
        try (
            FileChannel var3 = FileChannel.open(var2, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            FileLock var4 = var3.lock();
        ) {
            Path var5 = var1.resolve("authority-key.bin");
            byte[] var6;
            if (Files.exists(var5)) {
                if (Files.size(var5) != 32L) {
                    throw new IOException("Invalid authority key record");
                }

                var6 = Files.readAllBytes(var5);
            } else {
                var6 = new byte[32];
                new SecureRandom().nextBytes(var6);
                writeAtomic(var5, var6);
            }

            var7 = new OwnerLineageRevocationStore(var1, var6);
        }

        return var7;
    }

    private static void writeAtomic(Path var0, byte[] var1) throws IOException {
        Path var2 = var0.resolveSibling(var0.getFileName().toString() + ".tmp");

        try (FileChannel var3 = FileChannel.open(var2, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            ByteBuffer var4 = ByteBuffer.wrap(var1);

            while (var4.hasRemaining()) {
                var3.write(var4);
            }

            var3.force(true);
        }

        Files.move(var2, var0, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    private Path recordPath(UUID var1, UUID var2) throws IOException {
        Path var3 = this.directory.resolve(var1.toString());
        Files.createDirectories(var3);
        return var3.resolve(var2.toString() + ".bin");
    }

    private byte[] mac(byte[] var1) throws IOException {
        try {
            Mac var2 = Mac.getInstance("HmacSHA256");
            var2.init(new SecretKeySpec(this.key, "HmacSHA256"));
            return var2.doFinal(var1);
        } catch (GeneralSecurityException var3) {
            throw new IOException(var3);
        }
    }

    private static void uuid(ByteBuffer var0, UUID var1) {
        var0.putLong(var1.getMostSignificantBits()).putLong(var1.getLeastSignificantBits());
    }

    private static UUID uuid(ByteBuffer var0) {
        return new UUID(var0.getLong(), var0.getLong());
    }

    private byte[] encode(OwnerLineageRevocationStore.Record var1) throws IOException {
        ByteBuffer var2 = ByteBuffer.allocate(77);
        var2.putInt(1179931193);
        uuid(var2, var1.owner);
        uuid(var2, var1.weapon);
        uuid(var2, var1.incarnation);
        var2.putLong(var1.generation);
        uuid(var2, var1.transaction);
        var2.put((byte)(var1.active ? 1 : 0));
        return ByteBuffer.allocate(109).put(var2.array()).put(this.mac(var2.array())).array();
    }

    private OwnerLineageRevocationStore.Record read(Path var1, UUID var2, UUID var3) throws IOException {
        if (!Files.exists(var1)) {
            return null;
        } else if (Files.size(var1) != 109L) {
            throw new IOException("Invalid lineage record size");
        } else {
            byte[] var4 = Files.readAllBytes(var1);
            byte[] var5 = Arrays.copyOf(var4, 77);
            if (!MessageDigest.isEqual(this.mac(var5), Arrays.copyOfRange(var4, 77, 109))) {
                throw new IOException("Lineage record authentication failed");
            } else {
                ByteBuffer var6 = ByteBuffer.wrap(var5);
                if (var6.getInt() != 1179931193) {
                    throw new IOException("Lineage record version");
                } else {
                    UUID var7 = uuid(var6);
                    UUID var8 = uuid(var6);
                    UUID var9 = uuid(var6);
                    long var10 = var6.getLong();
                    UUID var12 = uuid(var6);
                    byte var13 = var6.get();
                    if (var2.equals(var7) && var3.equals(var8) && var10 >= 0L && var13 >= 0 && var13 <= 1) {
                        return new OwnerLineageRevocationStore.Record(var2, var3, var9, var10, var12, var13 == 1);
                    } else {
                        throw new IOException("Lineage record domain mismatch");
                    }
                }
            }
        }
    }

    private static Object stripe(UUID var0, UUID var1) {
        return STRIPES[(var0.hashCode() ^ var1.hashCode()) & STRIPES.length - 1];
    }

    private static boolean matches(OwnerLineageRevocationStore.Record var0, OwnerLineageRevocationStore.Lease var1) {
        return var0 != null
            && var0.owner.equals(var1.owner)
            && var0.weapon.equals(var1.weapon)
            && var0.incarnation.equals(var1.incarnation)
            && var0.generation == var1.generation;
    }

    boolean rejectsKnownLineage(UUID var1, UUID var2) throws IOException {
        this.repairIoBoundary();
        if (var1 != null && var2 != null) {
            synchronized (stripe(var1, var2)) {
                Path var4 = this.directory.resolve(var1.toString());
                Path var5 = var4.resolve(var2 + ".bin");
                Path var6 = var4.resolve(var2 + ".lock");
                if (Files.isSymbolicLink(this.directory) || Files.isSymbolicLink(var4) || Files.isSymbolicLink(var5) || Files.isSymbolicLink(var6)) {
                    throw new IOException("Original wear lineage path is a link");
                } else if (!Files.exists(var5)) {
                    return false;
                } else {
                    boolean var10;
                    try (
                        FileChannel var7 = FileChannel.open(var6, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        FileLock var8 = var7.lock();
                    ) {
                        OwnerLineageRevocationStore.Record var9 = this.read(var5, var1, var2);
                        var10 = var9 != null && !var9.active;
                    }

                    return var10;
                }
            }
        } else {
            throw new IOException("Missing original wear lineage domain");
        }
    }

    static void retirePrewriteLease(OwnerLineageRevocationStore.Lease var0) {
        boolean var1 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(var0x -> var0x.skip(1L).findFirst().map(var0xx -> var0xx.getDeclaringClass() == OwnerRepairContinuation.class).orElse(false));
        if (!var1) {
            throw new IllegalStateException("Detached local prewrite retirement");
        } else {
            if (var0 != null) {
                var0.retired = true;
            }
        }
    }

    void consumePrewriteTrack(OwnerRepairContinuation.Track var1) throws IOException {
        this.repairIoBoundary();
        CompoundTag var2 = var1.takeForStore(this);
        UUID var3 = var2.getUUID("Owner");
        UUID var4 = var2.getUUID("Weapon");
        synchronized (stripe(var3, var4)) {
            Path var6 = this.directory.resolve(var3.toString());
            Path var7 = var6.resolve(var4 + ".bin");
            Path var8 = var6.resolve(var4 + ".lock");
            if (!Files.isSymbolicLink(this.directory)
                && !Files.isSymbolicLink(var6)
                && !Files.isSymbolicLink(var7)
                && !Files.isSymbolicLink(var8)
                && Files.isRegularFile(var7)) {
                try (
                    FileChannel var9 = FileChannel.open(var8, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    FileLock var10 = var9.lock();
                ) {
                    OwnerLineageRevocationStore.Record var11 = this.read(var7, var3, var4);
                    if (var11 != null
                        && var11.active
                        && var11.generation >= 0L
                        && var11.generation != Long.MAX_VALUE
                        && var11.generation == var2.getLong("Generation")
                        && var11.incarnation.equals(var2.getUUID("Incarnation"))
                        && var11.transaction.equals(var2.getUUID("ParentClaim"))) {
                        OwnerLineageRevocationStore.Record var12 = new OwnerLineageRevocationStore.Record(
                            var11.owner, var11.weapon, var11.incarnation, var11.generation, var2.getUUID("Track"), false
                        );
                        writeAtomic(var7, this.encode(var12));
                        OwnerLineageRevocationStore.Record var13 = this.read(var7, var3, var4);
                        if (var13 == null
                            || var13.active
                            || var13.generation != var12.generation
                            || !var13.incarnation.equals(var12.incarnation)
                            || !var13.transaction.equals(var12.transaction)) {
                            throw new IOException("Prewrite inactive readback changed");
                        }
                    } else {
                        throw new IOException("Prewrite predecessor CAS rejected");
                    }
                }
            } else {
                throw new IOException("Prewrite lineage missing or linked");
            }
        }
    }

    OwnerLineageRevocationStore.TaskReservation reserveTaskOrigin(OwnerStatueTaskOrigin.Session var1) throws IOException {
        this.repairIoBoundary();
        CompoundTag var2 = var1.reservationBinding(this);
        UUID var3 = var2.getUUID("Owner");
        UUID var4 = var2.getUUID("Weapon");
        UUID var5 = var2.getUUID("Task");
        UUID var6 = UUID.nameUUIDFromBytes(
            ("ftboss/task-origin/v1/" + var2.getUUID("OriginalLoad") + "/" + var2.getUUID("OriginalCall") + "/" + var5).getBytes(StandardCharsets.UTF_8)
        );
        synchronized (stripe(var3, var4)) {
            Path var8 = this.recordPath(var3, var4);
            Path var9 = var8.resolveSibling(var4 + ".lock");
            if (Files.isSymbolicLink(var9)) {
                throw new IOException("Task-origin lock is a link");
            } else {
                OwnerLineageRevocationStore.TaskReservation var13;
                try (
                    FileChannel var10 = FileChannel.open(var9, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    FileLock var11 = var10.lock();
                ) {
                    if (this.read(var8, var3, var4) != null) {
                        throw new IOException("Task-origin cannot rebind an existing lineage");
                    }

                    OwnerLineageRevocationStore.TaskReservation var12 = new OwnerLineageRevocationStore.TaskReservation(this, var2, var6);
                    writeAtomic(var8, this.encode(new OwnerLineageRevocationStore.Record(var3, var4, var6, 0L, var5, false)));
                    var13 = var12;
                }

                return var13;
            }
        }
    }

    OwnerLineageRevocationStore.Lease consumeTaskOrigin(OwnerStatueTaskOrigin.Publication var1) throws IOException {
        this.repairIoBoundary();
        CompoundTag var2 = var1.takeForStore(this);
        OwnerLineageRevocationStore.TaskReservation var3 = var1.reservation();
        if (var3 != null
            && var3.store == this
            && !var3.taken
            && var3.owner.equals(var2.getUUID("Owner"))
            && var3.weapon.equals(var2.getUUID("Weapon"))
            && var3.task.equals(var2.getUUID("Task"))) {
            var3.taken = true;
            var3.claim = var2.getUUID("Claim");
            OwnerLineageRevocationStore.Lease var4 = null;
            boolean var5 = false;

            OwnerLineageRevocationStore.Lease var13;
            try {
                synchronized (stripe(var3.owner, var3.weapon)) {
                    Path var7 = this.recordPath(var3.owner, var3.weapon);
                    Path var8 = var7.resolveSibling(var3.weapon + ".lock");
                    if (Files.isSymbolicLink(var8)) {
                        throw new IOException("Task-origin lock is a link");
                    }

                    try (
                        FileChannel var9 = FileChannel.open(var8, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        FileLock var10 = var9.lock();
                    ) {
                        OwnerLineageRevocationStore.Record var11 = this.read(var7, var3.owner, var3.weapon);
                        if (var11 == null
                            || var11.active
                            || var11.generation != 0L
                            || !var11.incarnation.equals(var3.incarnation)
                            || !var11.transaction.equals(var3.task)) {
                            throw new IOException("Task-origin reservation consumed, missing or changed");
                        }

                        writeAtomic(
                            var7, this.encode(new OwnerLineageRevocationStore.Record(var11.owner, var11.weapon, var11.incarnation, 1L, var3.claim, false))
                        );
                        OwnerLineageRevocationStore.Record var12 = new OwnerLineageRevocationStore.Record(
                            var11.owner, var11.weapon, var11.incarnation, 1L, var3.claim, true
                        );
                        var4 = new OwnerLineageRevocationStore.Lease(this, var12);
                        writeAtomic(var7, this.encode(var12));
                        var5 = true;
                        var13 = var4;
                    }
                }
            } finally {
                if (!var5 && var4 != null) {
                    try {
                        retireLease(var4);
                    } catch (Throwable var26) {
                    }
                }
            }

            return var13;
        } else {
            throw new IOException("Task-origin reservation identity mismatch");
        }
    }


    Lease consumeNativeRepair(NativeFoodWeaponWear.Claim claim) throws IOException {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == NativeFoodWeaponWear.class && frame.getMethodName().equals("committedStatueRepair")).orElse(false));
        if (!caller || claim == null || !claim.takeForStore(this)) throw new IOException("Missing original signed native repair claim");
        Lease lease = null;
        boolean completed = false;
        try {
            lease = this.admit(claim.owner(), claim.weaponId());
            claim.issued(lease);
            completed = true;
            return lease;
        } finally {
            if (!completed && lease != null) retireLease(lease);
        }
    }

    Lease consumeNativeReload(OwnerStatueWearAdmission.Load context, int slot) throws IOException {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerStatueWearAdmission.class || !caller.getMethodName().equals("restoreNativeLoaded"))
            throw new IOException("Original inventory reload reservation required");
        this.repairIoBoundary();
        CompoundTag reservation = OwnerStatueWearAdmission.nativeReloadReservation(context, slot, this);
        if (reservation == null) throw new IOException("Native reload source/custody reservation changed");
        CompoundTag ticket = reservation.getCompound("Ticket");
        UUID owner = ticket.getUUID("Owner");
        UUID weapon = ticket.getUUID("Weapon");
        UUID documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-continuation/v1/" + owner).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        CompoundTag document = this.readRepairDocument(documentOwner, false);
        if (document.getInt("Schema") != 1 || !owner.equals(document.getUUID("Owner"))
            || !ticket.getUUID("Save").equals(document.getUUID("LastSave"))
            || !"COMMITTED_ORIGINAL_SAVE".equals(document.getString("Status"))
            || !reservation.equals(document.getCompound("Continuations").getCompound(weapon.toString()))
            || !"RESERVED_ORIGINAL_LOAD_NO_REISSUE".equals(reservation.getString("Status")))
            throw new IOException("Native reload authenticated reservation changed");
        synchronized (stripe(owner, weapon)) {
            Path path = this.recordPath(owner, weapon);
            Path lock = path.resolveSibling(weapon + ".lock");
            if (Files.isSymbolicLink(lock)) throw new IOException("Native reload lineage lock is a link");
            try (FileChannel channel = FileChannel.open(lock, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 FileLock held = channel.lock()) {
                Record before = this.read(path, owner, weapon);
                if (before == null || !before.active || before.generation == Long.MAX_VALUE
                    || before.generation != ticket.getLong("LeaseGeneration")
                    || !before.incarnation.equals(ticket.getUUID("LeaseEpoch")))
                    throw new IOException("Native reload active predecessor CAS rejected");
                Record after = new Record(owner, weapon, before.incarnation, Math.addExact(before.generation, 1L), reservation.getUUID("Load"), true);
                writeAtomic(path, this.encode(after));
                if (!after.equals(this.read(path, owner, weapon))) throw new IOException("Native reload lineage successor readback changed");
                return new Lease(this, after);
            }
        }
    }

    OwnerLineageRevocationStore.Lease admit(UUID var1, UUID var2) throws IOException {
        synchronized (stripe(var1, var2)) {
            Path var4 = this.recordPath(var1, var2);

            OwnerLineageRevocationStore.Lease var12;
            try (
                FileChannel var5 = FileChannel.open(var4.resolveSibling(var2 + ".lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock var6 = var5.lock();
            ) {
                OwnerLineageRevocationStore.Record var7 = this.read(var4, var1, var2);
                UUID var8 = var7 != null && var7.generation != Long.MAX_VALUE ? var7.incarnation : UUID.randomUUID();
                long var9 = var7 != null && var7.generation != Long.MAX_VALUE ? var7.generation + 1L : 0L;
                OwnerLineageRevocationStore.Record var11 = new OwnerLineageRevocationStore.Record(var1, var2, var8, var9, UUID.randomUUID(), true);
                writeAtomic(var4, this.encode(var11));
                var12 = new OwnerLineageRevocationStore.Lease(this, var11);
            }

            return var12;
        }
    }

    OwnerLineageRevocationStore.Advance begin(OwnerLineageRevocationStore.Lease var1, UUID var2) throws IOException {
        if (var1 != null && var1.store == this && var2 != null) {
            var1.retired = true;
            synchronized (stripe(var1.owner, var1.weapon)) {
                Path var4 = this.recordPath(var1.owner, var1.weapon);

                Object var15;
                try (
                    FileChannel var5 = FileChannel.open(var4.resolveSibling(var1.weapon + ".lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    FileLock var6 = var5.lock();
                ) {
                    OwnerLineageRevocationStore.Record var7 = this.read(var4, var1.owner, var1.weapon);
                    if (matches(var7, var1) && var7.active) {
                        writeAtomic(
                            var4, this.encode(new OwnerLineageRevocationStore.Record(var7.owner, var7.weapon, var7.incarnation, var7.generation, var2, false))
                        );
                        return new OwnerLineageRevocationStore.Advance(var1, var2);
                    }

                    var15 = null;
                }

                return (OwnerLineageRevocationStore.Advance)var15;
            }
        } else {
            return null;
        }
    }

    OwnerLineageRevocationStore.Lease commit(OwnerLineageRevocationStore.Advance var1) throws IOException {
        if (var1 != null && var1.previous.store == this && !var1.committed) {
            OwnerLineageRevocationStore.Lease var2 = var1.previous;
            synchronized (stripe(var2.owner, var2.weapon)) {
                Path var4 = this.recordPath(var2.owner, var2.weapon);

                Object var20;
                try (
                    FileChannel var5 = FileChannel.open(var4.resolveSibling(var2.weapon + ".lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    FileLock var6 = var5.lock();
                ) {
                    OwnerLineageRevocationStore.Record var7 = this.read(var4, var2.owner, var2.weapon);
                    if (matches(var7, var2) && !var7.active && var7.transaction.equals(var1.transaction)) {
                        UUID var8 = var7.generation == Long.MAX_VALUE ? UUID.randomUUID() : var7.incarnation;
                        long var9 = var7.generation == Long.MAX_VALUE ? 0L : var7.generation + 1L;
                        OwnerLineageRevocationStore.Record var11 = new OwnerLineageRevocationStore.Record(
                            var7.owner, var7.weapon, var8, var9, var1.transaction, true
                        );
                        OwnerLineageRevocationStore.Lease var12 = new OwnerLineageRevocationStore.Lease(this, var11);
                        var1.candidate = var12;
                        writeAtomic(var4, this.encode(var11));
                        var1.committed = true;
                        return var12;
                    }

                    var20 = null;
                }

                return (OwnerLineageRevocationStore.Lease)var20;
            }
        } else {
            return null;
        }
    }

    boolean owns(OwnerLineageRevocationStore.Lease var1) {
        return var1 != null && var1.store == this;
    }

    boolean live(OwnerLineageRevocationStore.Lease var1) {
        if (var1 != null && var1.store == this && !var1.retired) {
            synchronized (stripe(var1.owner, var1.weapon)) {
                if (var1.retired) {
                    return false;
                } else {
                    boolean var10000;
                    try {
                        OwnerLineageRevocationStore.Record var3 = this.read(this.recordPath(var1.owner, var1.weapon), var1.owner, var1.weapon);
                        var10000 = matches(var3, var1) && var3.active && !var1.retired;
                    } catch (IOException var5) {
                        var1.retired = true;
                        return false;
                    }

                    return var10000;
                }
            }
        } else {
            return false;
        }
    }

    static boolean locallyLive(OwnerLineageRevocationStore.Lease var0) {
        return var0 != null && !var0.retired;
    }

    static boolean liveLease(OwnerLineageRevocationStore.Lease var0) {
        return var0 != null && var0.store.live(var0);
    }

    static void retireLease(OwnerLineageRevocationStore.Lease var0) {
        if (var0 != null) {
            var0.store.retire(var0);
        }
    }

    static OwnerLineageRevocationStore.Advance beginLease(OwnerLineageRevocationStore.Lease var0, UUID var1) throws IOException {
        return var0 == null ? null : var0.store.begin(var0, var1);
    }

    static OwnerLineageRevocationStore.Lease commitAdvance(OwnerLineageRevocationStore.Advance var0) throws IOException {
        return var0 == null ? null : var0.previous.store.commit(var0);
    }

    void retire(OwnerLineageRevocationStore.Lease var1) {
        if (var1 != null && var1.store == this && !var1.retired) {
            try {
                this.begin(var1, UUID.randomUUID());
            } catch (IOException var3) {
                var1.retired = true;
            }
        }
    }

    static void abortAdvance(OwnerLineageRevocationStore.Advance var0) {
        if (var0 != null && var0.candidate != null) {
            OwnerLineageRevocationStore.Lease var1 = var0.candidate;

            try {
                var1.store.begin(var1, UUID.randomUUID());
            } catch (IOException var3) {
                var1.retired = true;
            }
        }
    }

    static {
        for (int var0 = 0; var0 < STRIPES.length; var0++) {
            STRIPES[var0] = new Object();
        }
    }

    static final class Advance {
        private final OwnerLineageRevocationStore.Lease previous;
        private final UUID transaction;
        private boolean committed;
        private OwnerLineageRevocationStore.Lease candidate;

        private Advance(OwnerLineageRevocationStore.Lease var1, UUID var2) {
            this.previous = var1;
            this.transaction = var2;
        }
    }

    static final class Lease {
        private final OwnerLineageRevocationStore store;
        final UUID owner;
        final UUID weapon;
        final UUID incarnation;
        final long generation;
        private volatile boolean retired;

        private Lease(OwnerLineageRevocationStore var1, OwnerLineageRevocationStore.Record var2) {
            this.store = var1;
            this.owner = var2.owner;
            this.weapon = var2.weapon;
            this.incarnation = var2.incarnation;
            this.generation = var2.generation;
        }
    }

    private static record Record(UUID owner, UUID weapon, UUID incarnation, long generation, UUID transaction, boolean active) {
    }

    static final class TaskReservation {
        private final OwnerLineageRevocationStore store;
        final UUID owner;
        final UUID weapon;
        final UUID task;
        final UUID incarnation;
        private UUID claim;
        private boolean taken;

        private TaskReservation(OwnerLineageRevocationStore var1, CompoundTag var2, UUID var3) {
            this.store = var1;
            this.owner = var2.getUUID("Owner");
            this.weapon = var2.getUUID("Weapon");
            this.task = var2.getUUID("Task");
            this.incarnation = var3;
        }
    }
}
