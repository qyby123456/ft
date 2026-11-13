import java.nio.file.*;
import java.util.*;
import com.google.gson.GsonBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class NativeCheck {
    static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    static boolean clear(Map<BlockPos, BlockState> blocks, BlockPos feet) {
        for (int rise = 0; rise < 2; rise++) {
            BlockPos target = feet.above(rise);
            BlockState state = blocks.get(target);
            if (state == null || !state.getCollisionShape(EmptyBlockGetter.INSTANCE, target).isEmpty()) return false;
        }
        return true;
    }
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        CompoundTag data = NbtIo.readCompressed(root.resolve("nbt/s5-01-coral-chamber-redo.nbt").toFile());
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        Map<String, Integer> materials = new TreeMap<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(entry.getString("Name"));
            require(BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown block " + identifier);
            var block = BuiltInRegistries.BLOCK.get(identifier);
            CompoundTag properties = entry.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identifier + " " + key);
            }
            if (identifier.getPath().contains("coral") && !identifier.getPath().endsWith("_block")) require(properties.getString("waterlogged").equals("true"), "Dry coral");
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        ListTag entries = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            ListTag position = entry.getList("pos", Tag.TAG_INT);
            BlockPos coordinate = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(coordinate.getX() >= 0 && coordinate.getX() < 35 && coordinate.getY() >= 0 && coordinate.getY() < 22 && coordinate.getZ() >= 0 && coordinate.getZ() < 35, "Bounds");
            BlockState state = states.get(entry.getInt("state"));
            require(blocks.put(coordinate, state) == null, "Duplicate position");
            materials.merge(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(), 1, Integer::sum);
        }
        require(blocks.size() == 35 * 22 * 35, "Incomplete cuboid");
        for (var entry : blocks.entrySet()) {
            String name = BuiltInRegistries.BLOCK.getKey(entry.getValue().getBlock()).getPath();
            if (name.endsWith("coral_block")) {
                boolean wet = false;
                for (Direction direction : Direction.values()) {
                    BlockState neighbor = blocks.get(entry.getKey().relative(direction));
                    if (neighbor != null && !neighbor.getFluidState().isEmpty()) wet = true;
                }
                require(wet, "Coral block has no water neighbor: " + entry.getKey());
            }
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        CompoundTag saved = template.save(new CompoundTag());
        require(saved.getList("blocks", Tag.TAG_COMPOUND).size() == blocks.size(), "Template load/save mismatch");
        require(template.getSize().equals(new Vec3i(35, 22, 35)), "Template dimensions");
        BlockPos start = new BlockPos(17, 3, 31);
        require(clear(blocks, start), "Blocked entry-side observation point");
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(start);
        pending.add(start);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (!visited.contains(next) && clear(blocks, next)) { visited.add(next); pending.add(next); }
            }
        }
        for (BlockPos gate : List.of(new BlockPos(17, 3, 0), new BlockPos(0, 3, 10), new BlockPos(34, 3, 10))) {
            require(visited.contains(gate), "Unreachable exit " + gate);
            for (int offset = -2; offset <= 2; offset++) {
                BlockPos feet = gate.getZ() == 0 ? gate.offset(offset, 0, 0) : gate.offset(0, 0, offset);
                require(clear(blocks, feet) && clear(blocks, feet.above()), "Exit does not have 5x3 clearance");
            }
        }
        for (BlockPos waypoint : List.of(new BlockPos(8, 3, 19), new BlockPos(26, 3, 19), new BlockPos(17, 3, 10))) require(visited.contains(waypoint), "Broken arena ring");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "PASS");
        result.put("engine", "Vanilla Java 1.20.1 registry, NbtIo, StructureTemplate, native collision shapes");
        result.put("size", List.of(35, 22, 35));
        result.put("blocks", blocks.size());
        result.put("palette", materials);
        result.put("reachable_swimming_feet_at_y3", visited.size());
        result.put("gates", "North/West/East connected; 5 wide x 3 high minimum verified");
        result.put("scope_limit", "Not Forge world placement, fluid/random ticks, gameplay, or coordinator acceptance");
        Files.writeString(root.resolve("qa/native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result));
        System.out.println("PASS vanilla load/save, registry, bounds, 3 gates, arena ring; " + visited.size() + " reachable columns");
    }
}
