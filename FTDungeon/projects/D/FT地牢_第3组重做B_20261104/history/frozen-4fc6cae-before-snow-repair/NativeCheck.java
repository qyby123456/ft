import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class NativeCheck {
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        CompoundTag nbt = NbtIo.readCompressed(root.resolve("room.nbt").toFile());
        ListTag palette = nbt.getList("palette", Tag.TAG_COMPOUND);
        ArrayList<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation id = new ResourceLocation(entry.getString("Name"));
            require(BuiltInRegistries.BLOCK.containsKey(id), "Unknown vanilla block " + id);
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
            CompoundTag expected = entry.getCompound("Properties");
            CompoundTag actual = NbtUtils.writeBlockState(state).getCompound("Properties");
            for (String key : expected.getAllKeys()) require(expected.getString(key).equals(actual.getString(key)), "Invalid property " + id + " " + key);
            states.add(state);
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), nbt);
        require(template.getSize().equals(new net.minecraft.core.Vec3i(35, 22, 35)), "Wrong native template dimensions");
        CompoundTag saved = template.save(new CompoundTag());
        require(saved.getList("blocks", Tag.TAG_COMPOUND).size() == 26950, "Native roundtrip lost records");
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        ListTag records = nbt.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag entry = records.getCompound(index);
            ListTag pos = entry.getList("pos", Tag.TAG_INT);
            blocks.put(new BlockPos(pos.getInt(0), pos.getInt(1), pos.getInt(2)), states.get(entry.getInt("state")));
        }
        ArrayList<BlockPos> queue = new ArrayList<>();
        HashSet<BlockPos> seen = new HashSet<>();
        BlockPos start = new BlockPos(17, 2, 0);
        queue.add(start);
        seen.add(start);
        for (int index = 0; index < queue.size(); index++) {
            BlockPos pos = queue.get(index);
            for (BlockPos next : new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west()}) {
                if (seen.contains(next) || !blocks.containsKey(next) || !blocks.containsKey(next.above())) continue;
                BlockState feet = blocks.get(next);
                BlockState head = blocks.get(next.above());
                if (!feet.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, next).isEmpty()) continue;
                if (!head.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, next.above()).isEmpty()) continue;
                if (blocks.get(next.below()).getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, next.below()).isEmpty()) continue;
                seen.add(next);
                queue.add(next);
            }
        }
        require(seen.contains(new BlockPos(0, 2, 13)) && seen.contains(new BlockPos(34, 2, 13)), "Native collision route blocked");
        Map<String, Object> report = new java.util.LinkedHashMap<>();
        report.put("status", "PASS");
        report.put("sha256", java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(root.resolve("room.nbt")))));
        report.put("minecraft", "1.20.1");
        report.put("palette_entries_validated", states.size());
        report.put("template_load_save", "PASS");
        report.put("flat_collision_reachable_cells", seen.size());
        report.put("north_west_east_connected", true);
        report.put("world_placement_ticks_gameplay", "NOT_RUN");
        Files.writeString(root.resolve("native-check.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println("PASS native 1.20.1 registry, properties, template roundtrip, collision connectivity");
    }
}
