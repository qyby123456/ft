/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.UUID;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;

public final class TransactionWire {
    public static final int MAX_BYTES = 8192;

    private TransactionWire() {
    }

    public static byte[] encode(Command command) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream;
        block9: {
            byteArrayOutputStream = new ByteArrayOutputStream();
            try (DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);){
                dataOutputStream.writeByte(2);
                if (command instanceof Opening) {
                    Opening opening = (Opening)command;
                    dataOutputStream.writeByte(0);
                    TransactionWire.uuid(dataOutputStream, opening.open().request());
                    TransactionWire.binding(dataOutputStream, opening.open().binding());
                    break block9;
                }
                if (command instanceof Submit) {
                    Submit submit = (Submit)command;
                    FourWeaponTransactions.Request request = submit.request();
                    dataOutputStream.writeByte(1);
                    TransactionWire.uuid(dataOutputStream, request.request());
                    TransactionWire.uuid(dataOutputStream, request.session());
                    TransactionWire.uuid(dataOutputStream, request.credential());
                    TransactionWire.binding(dataOutputStream, request.binding());
                    TransactionWire.version(dataOutputStream, request.expected());
                    dataOutputStream.writeByte(request.operation().ordinal());
                    dataOutputStream.writeInt(request.part());
                    dataOutputStream.writeInt(request.value());
                    dataOutputStream.writeUTF(request.offeringFingerprint());
                    break block9;
                }
                if (command instanceof Close) {
                    Close close = (Close)command;
                    dataOutputStream.writeByte(2);
                    TransactionWire.uuid(dataOutputStream, close.session());
                    break block9;
                }
                if (command instanceof TargetOpen) {
                    TargetOpen targetOpen = (TargetOpen)command;
                    dataOutputStream.writeByte(3);
                    TransactionWire.uuid(dataOutputStream, targetOpen.request());
                    dataOutputStream.writeInt(targetOpen.hand());
                    dataOutputStream.writeUTF(targetOpen.dimension());
                    dataOutputStream.writeLong(targetOpen.statue());
                    TransactionWire.uuid(dataOutputStream, targetOpen.weapon());
                    dataOutputStream.writeInt(targetOpen.revision());
                    dataOutputStream.writeUTF(targetOpen.fingerprint());
                    break block9;
                }
                throw new IOException("unsupported command");
            }
        }
        return TransactionWire.bounded(byteArrayOutputStream.toByteArray());
    }

    public static Command decode(byte[] byArray) throws IOException {
        Record record;
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(TransactionWire.bounded(byArray)));
        try {
            TransactionWire.protocol(dataInputStream);
            Record record2 = switch (dataInputStream.readUnsignedByte()) {
                case 0 -> new Opening(new FourWeaponTransactions.Open(TransactionWire.uuid(dataInputStream), TransactionWire.binding(dataInputStream)));
                case 1 -> new Submit(new FourWeaponTransactions.Request(TransactionWire.uuid(dataInputStream), TransactionWire.uuid(dataInputStream), TransactionWire.uuid(dataInputStream), TransactionWire.binding(dataInputStream), TransactionWire.version(dataInputStream), TransactionWire.value(FourWeaponTransactions.Operation.values(), dataInputStream.readUnsignedByte()), dataInputStream.readInt(), dataInputStream.readInt(), TransactionWire.text(dataInputStream)));
                case 2 -> new Close(TransactionWire.uuid(dataInputStream));
                case 3 -> new TargetOpen(TransactionWire.uuid(dataInputStream), dataInputStream.readInt(), TransactionWire.text(dataInputStream), dataInputStream.readLong(), TransactionWire.uuid(dataInputStream), dataInputStream.readInt(), TransactionWire.text(dataInputStream));
                default -> throw new IOException("unknown command");
            };
            TransactionWire.end(dataInputStream);
            record = record2;
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
            catch (IllegalArgumentException illegalArgumentException) {
                throw new IOException("invalid payload", illegalArgumentException);
            }
        }
        dataInputStream.close();
        return record;
    }

    public static byte[] encodeReply(FourWeaponTransactions.Reply reply) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);){
            dataOutputStream.writeByte(2);
            TransactionWire.uuid(dataOutputStream, reply.request());
            dataOutputStream.writeByte(reply.code().ordinal());
            dataOutputStream.writeBoolean(reply.snapshot() != null);
            if (reply.snapshot() != null) {
                FourWeaponTransactions.Snapshot snapshot = reply.snapshot();
                TransactionWire.uuid(dataOutputStream, snapshot.session());
                TransactionWire.uuid(dataOutputStream, snapshot.credential());
                TransactionWire.binding(dataOutputStream, snapshot.binding());
                TransactionWire.version(dataOutputStream, snapshot.version());
                int n = 0;
                for (FourWeaponTransactions.Operation operation : snapshot.allowed()) {
                    n |= 1 << operation.ordinal();
                }
                dataOutputStream.writeInt(n);
            }
        }
        return TransactionWire.bounded(byteArrayOutputStream.toByteArray());
    }

    public static FourWeaponTransactions.Reply decodeReply(byte[] byArray) throws IOException {
        Object object;
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(TransactionWire.bounded(byArray)));
        try {
            TransactionWire.protocol(dataInputStream);
            UUID uUID = TransactionWire.uuid(dataInputStream);
            FourWeaponTransactions.Code code = TransactionWire.value(FourWeaponTransactions.Code.values(), dataInputStream.readUnsignedByte());
            FourWeaponTransactions.Snapshot snapshot = null;
            int n = dataInputStream.readUnsignedByte();
            if (n > 1) {
                throw new IOException("invalid boolean");
            }
            if (n == 1) {
                object = TransactionWire.uuid(dataInputStream);
                UUID uUID2 = TransactionWire.uuid(dataInputStream);
                FourWeaponTransactions.Binding binding = TransactionWire.binding(dataInputStream);
                FourWeaponTransactions.Version version = TransactionWire.version(dataInputStream);
                int n2 = dataInputStream.readInt();
                if (n2 < 0 || n2 >= 1 << FourWeaponTransactions.Operation.values().length) {
                    throw new IOException("unknown policy bits");
                }
                EnumSet<FourWeaponTransactions.Operation> enumSet = EnumSet.noneOf(FourWeaponTransactions.Operation.class);
                for (FourWeaponTransactions.Operation operation : FourWeaponTransactions.Operation.values()) {
                    if ((n2 & 1 << operation.ordinal()) == 0) continue;
                    enumSet.add(operation);
                }
                snapshot = new FourWeaponTransactions.Snapshot((UUID)object, uUID2, binding, version, enumSet);
            }
            if ((code == FourWeaponTransactions.Code.COMMITTED || code == FourWeaponTransactions.Code.OPENED) && snapshot == null) {
                throw new IOException("missing authority");
            }
            TransactionWire.end(dataInputStream);
            object = new FourWeaponTransactions.Reply(uUID, code, snapshot);
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
            catch (IllegalArgumentException illegalArgumentException) {
                throw new IOException("invalid payload", illegalArgumentException);
            }
        }
        dataInputStream.close();
        return object;
    }

    public static FourWeaponTransactions.Reply dispatch(FourWeaponTransactions fourWeaponTransactions, UUID uUID, Command command) {
        if (command instanceof Opening) {
            Opening opening = (Opening)command;
            return fourWeaponTransactions.open(uUID, opening.open());
        }
        if (command instanceof Submit) {
            Submit submit = (Submit)command;
            return fourWeaponTransactions.submit(uUID, submit.request());
        }
        if (command instanceof Close) {
            Close close = (Close)command;
            fourWeaponTransactions.close(uUID, close.session());
        }
        return null;
    }

    private static byte[] bounded(byte[] byArray) throws IOException {
        if (byArray == null || byArray.length == 0 || byArray.length > 8192) {
            throw new IOException("packet size");
        }
        return byArray;
    }

    private static void protocol(DataInputStream dataInputStream) throws IOException {
        if (dataInputStream.readUnsignedByte() != 2) {
            throw new IOException("protocol mismatch");
        }
    }

    private static void end(DataInputStream dataInputStream) throws IOException {
        if (dataInputStream.available() != 0) {
            throw new IOException("trailing bytes");
        }
    }

    private static String text(DataInputStream dataInputStream) throws IOException {
        String string = dataInputStream.readUTF();
        if (string.length() > 256) {
            throw new IOException("text limit");
        }
        return string;
    }

    private static <Value> Value value(Value[] ValueArray, int n) throws IOException {
        if (n >= ValueArray.length) {
            throw new IOException("unknown enum");
        }
        return ValueArray[n];
    }

    private static UUID uuid(DataInputStream dataInputStream) throws IOException {
        return new UUID(dataInputStream.readLong(), dataInputStream.readLong());
    }

    private static void uuid(DataOutputStream dataOutputStream, UUID uUID) throws IOException {
        dataOutputStream.writeLong(uUID.getMostSignificantBits());
        dataOutputStream.writeLong(uUID.getLeastSignificantBits());
    }

    private static FourWeaponTransactions.Binding binding(DataInputStream dataInputStream) throws IOException {
        return new FourWeaponTransactions.Binding(TransactionWire.uuid(dataInputStream), TransactionWire.uuid(dataInputStream), TransactionWire.text(dataInputStream), dataInputStream.readInt(), dataInputStream.readInt(), TransactionWire.text(dataInputStream), dataInputStream.readLong(), TransactionWire.text(dataInputStream));
    }

    private static void binding(DataOutputStream dataOutputStream, FourWeaponTransactions.Binding binding) throws IOException {
        TransactionWire.uuid(dataOutputStream, binding.owner());
        TransactionWire.uuid(dataOutputStream, binding.weapon());
        dataOutputStream.writeUTF(binding.item());
        dataOutputStream.writeInt(binding.hand());
        dataOutputStream.writeInt(binding.inventorySlot());
        dataOutputStream.writeUTF(binding.dimension());
        dataOutputStream.writeLong(binding.statue());
        dataOutputStream.writeUTF(binding.statueIdentity());
    }

    private static FourWeaponTransactions.Version version(DataInputStream dataInputStream) throws IOException {
        return new FourWeaponTransactions.Version(TransactionWire.uuid(dataInputStream), dataInputStream.readLong(), dataInputStream.readInt(), dataInputStream.readInt(), dataInputStream.readInt(), TransactionWire.text(dataInputStream), TransactionWire.text(dataInputStream));
    }

    private static void version(DataOutputStream dataOutputStream, FourWeaponTransactions.Version version) throws IOException {
        TransactionWire.uuid(dataOutputStream, version.epoch());
        dataOutputStream.writeLong(version.sequence());
        dataOutputStream.writeInt(version.cavity());
        dataOutputStream.writeInt(version.repair());
        dataOutputStream.writeInt(version.evolution());
        dataOutputStream.writeUTF(version.fingerprint());
        dataOutputStream.writeUTF(version.rulesFingerprint());
    }

    public record Opening(FourWeaponTransactions.Open open) implements Command
    {
    }

    public record Submit(FourWeaponTransactions.Request request) implements Command
    {
    }

    public record Close(UUID session) implements Command
    {
    }

    public record TargetOpen(UUID request, int hand, String dimension, long statue, UUID weapon, int revision, String fingerprint) implements Command
    {
        public TargetOpen {
            if (uUID == null || n < 0 || n > 1 || string == null || string.isBlank() || string.length() > 256 || uUID2 == null || n2 < 0 || string2 == null || string2.length() != 64 || !string2.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("target");
            }
        }
    }

    /*
     * Uses 'sealed' constructs - enablewith --sealed true
     */
    public static interface Command {
    }
}

