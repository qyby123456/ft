package ftboss.storage308;

import com.google.gson.GsonBuilder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

public final class LeaseFileLock {
    public static void main(String[] arguments) throws Exception {
        Path world = Path.of(arguments[0]).toRealPath();
        UUID owner = UUID.fromString(arguments[1]);
        Path ready = Path.of(arguments[2]);
        Path release = Path.of(arguments[3]);
        CompoundTag player = NbtIo.readCompressed(world.resolve("playerdata").resolve(owner + ".dat"), NbtAccounter.unlimitedHeap());
        CompoundTag ticket = player.getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
        if (!owner.equals(ticket.getUUID("Owner"))) throw new AssertionError("Original lock owner differs from saved ticket");
        Path directory = world.resolve("ftboss-authority").resolve(owner.toString()).toRealPath();
        Path lockPath = directory.resolve(ticket.getUUID("Weapon") + ".lock");
        if (!directory.startsWith(world) || Files.isSymbolicLink(lockPath)) throw new IllegalArgumentException("Original copied lineage lock boundary changed");
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock held = channel.lock()) {
            Files.writeString(ready, new GsonBuilder().create().toJson(Map.of("owner", owner.toString(),
                "weapon", ticket.getUUID("Weapon").toString(), "epoch", ticket.getUUID("LeaseEpoch").toString(),
                "generation", ticket.getLong("LeaseGeneration"), "lock", lockPath.toString(), "held", true)), StandardCharsets.UTF_8);
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(900L);
            while (!Files.exists(release)) {
                if (System.nanoTime() > deadline) throw new IllegalStateException("External lock observation deadline; releasing without claiming crash acceptance");
                Thread.sleep(10L);
            }
        }
    }
}
