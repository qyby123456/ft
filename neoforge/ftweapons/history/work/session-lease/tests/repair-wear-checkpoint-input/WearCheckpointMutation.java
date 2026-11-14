package ftboss.storage308;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

public final class WearCheckpointMutation {
    public static void main(String[] arguments) throws Exception {
        Path world = Path.of(arguments[0]).toRealPath();
        Path playerFile = world.resolve("playerdata").resolve(arguments[1] + ".dat");
        Path wearFile = world.resolve("data/ftboss_native_weapon_wear.dat");
        if (!playerFile.toRealPath().startsWith(world) || !wearFile.toRealPath().startsWith(world)
            || Files.isSymbolicLink(playerFile) || Files.isSymbolicLink(wearFile)) throw new IllegalArgumentException("Copied-world input boundary changed");
        byte[] originalPlayer = Files.readAllBytes(playerFile);
        CompoundTag player = NbtIo.readCompressed(playerFile, NbtAccounter.unlimitedHeap());
        CompoundTag item = player.getList("Inventory", 10).getCompound(0);
        UUID identity = item.getCompound("components").getCompound("minecraft:custom_data").getCompound("FTBossWeaponLifecycle").getCompound("NativeWear").getUUID("Identity");
        CompoundTag wear = NbtIo.readCompressed(wearFile, NbtAccounter.unlimitedHeap());
        CompoundTag record = wear.getCompound("data").getCompound("Weapons").getCompound(identity.toString());
        int before = record.getInt("Damage");
        int maximum = record.getInt("Maximum");
        int actual = item.getCompound("components").getInt("minecraft:damage");
        if (before != actual || before < 0 || maximum <= before + 1) throw new IllegalArgumentException("Original copied wear checkpoint is not coherent");
        record.putInt("Damage", before + 1);
        NbtIo.writeCompressed(wear, wearFile);
        CompoundTag written = NbtIo.readCompressed(wearFile, NbtAccounter.unlimitedHeap()).getCompound("data").getCompound("Weapons").getCompound(identity.toString());
        if (written.getInt("Damage") != before + 1 || written.getInt("Maximum") != maximum
            || !Arrays.equals(originalPlayer, Files.readAllBytes(playerFile))) throw new AssertionError("Copied negative input mutation changed unexpected data");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("negativeInputOnly", true);
        result.put("copiedWorld", world.toString());
        result.put("identity", identity.toString());
        result.put("originalDamage", before);
        result.put("alteredDamage", before + 1);
        result.put("maximum", maximum);
        result.put("originalPlayerFileUnchanged", true);
        result.put("applicationAuthorityWrittenByReflection", false);
        System.out.println(new GsonBuilder().create().toJson(result));
    }
}
