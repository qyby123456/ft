import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import com.google.gson.GsonBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ConnectionCheck {
    static final String EXPECTED = "9450884a348eda61ea55f0f11a8dae87a0f7d5183312b1528d9a519449f6b9a2";
    static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    static List<Integer> tuple(BlockPos position) { return List.of(position.getX(), position.getY(), position.getZ()); }
    static BlockPos rotate(BlockPos position, Rotation rotation) {
        BlockPos raw = StructureTemplate.transform(position, Mirror.NONE, rotation, BlockPos.ZERO);
        return switch (rotation) {
            case NONE -> raw;
            case CLOCKWISE_90 -> raw.offset(34, 0, 0);
            case CLOCKWISE_180 -> raw.offset(34, 0, 34);
            case COUNTERCLOCKWISE_90 -> raw.offset(0, 0, 34);
        };
    }
    static boolean clear(Map<BlockPos, BlockState> blocks, BlockPos feet, int height) {
        for (int rise = 0; rise < height; rise++) {
            BlockPos target = feet.above(rise);
            BlockState state = blocks.get(target);
            if (state == null || !state.getCollisionShape(EmptyBlockGetter.INSTANCE, target).isEmpty()) return false;
        }
        return true;
    }
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        Path nbt = Path.of(arguments[0]); Path output = Path.of(arguments[1]);
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(nbt)));
        require(EXPECTED.equals(hash), "Frozen NBT drift");
        CompoundTag data = NbtIo.readCompressed(nbt.toFile());
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette.getCompound(index)));
        ListTag entries = data.getList("blocks", Tag.TAG_COMPOUND);
        Map<BlockPos, BlockState> original = new HashMap<>();
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index); ListTag pos = entry.getList("pos", Tag.TAG_INT);
            original.put(new BlockPos(pos.getInt(0), pos.getInt(1), pos.getInt(2)), states.get(entry.getInt("state")));
        }
        List<BlockPos> targets = List.of(new BlockPos(17,3,31), new BlockPos(8,3,26), new BlockPos(26,3,26), new BlockPos(8,3,19), new BlockPos(26,3,19), new BlockPos(8,3,10), new BlockPos(26,3,10), new BlockPos(17,3,10), new BlockPos(17,3,0), new BlockPos(0,3,10), new BlockPos(34,3,10));
        List<Object> results = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            Map<BlockPos, BlockState> blocks = new HashMap<>();
            for (var entry : original.entrySet()) blocks.put(rotate(entry.getKey(), rotation), entry.getValue().rotate(rotation));
            BlockPos start = rotate(targets.get(0), rotation);
            require(clear(blocks, start, 2), "Start collision");
            Map<BlockPos, BlockPos> parents = new HashMap<>();
            ArrayDeque<BlockPos> pending = new ArrayDeque<>(); parents.put(start, start); pending.add(start);
            while (!pending.isEmpty()) {
                BlockPos current = pending.removeFirst();
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos next = current.relative(direction);
                    if (!parents.containsKey(next) && clear(blocks,next,2)) {parents.put(next,current); pending.add(next);}
                }
            }
            List<Object> paths = new ArrayList<>();
            for (BlockPos local : targets) {
                BlockPos target = rotate(local, rotation); require(parents.containsKey(target), "Unreachable " + target);
                List<List<Integer>> path = new ArrayList<>(); BlockPos cursor = target;
                while (!cursor.equals(start)) {path.add(tuple(cursor)); cursor = parents.get(cursor);}
                path.add(tuple(start)); Collections.reverse(path);
                paths.add(Map.of("original",tuple(local),"rotated",tuple(target),"path",path));
            }
            List<Object> gates = new ArrayList<>();
            for (int index = 8; index < 11; index++) {
                BlockPos gate = targets.get(index);
                Direction direction = index == 8 ? Direction.NORTH : index == 9 ? Direction.WEST : Direction.EAST;
                for (int offset = -2; offset <= 2; offset++) for (int depth = 0; depth <= 2; depth++) {
                    BlockPos feet = (index == 8 ? gate.offset(offset,0,depth) : gate.offset(index == 9 ? depth : -depth,0,offset));
                    BlockPos rotated = rotate(feet, rotation);
                    require(clear(blocks,rotated,3),"Gate clearance " + rotated);
                    require(blocks.get(rotated.below()).isFaceSturdy(EmptyBlockGetter.INSTANCE,rotated.below(),Direction.UP),"Unsupported gate");
                }
                gates.add(Map.of("anchor",tuple(rotate(gate,rotation)),"outward",rotation.rotate(direction).getName(),"clearance","5 wide x 3 high x 3 deep; solid sill"));
            }
            List<List<Integer>> reachable = parents.keySet().stream().sorted(Comparator.<BlockPos>comparingInt(position -> position.getX()).thenComparingInt(position -> position.getZ())).map(ConnectionCheck::tuple).toList();
            results.add(Map.of("rotation",rotation.name(),"gates",gates,"targets_and_paths",paths,"all_reachable_feet",reachable,"count",parents.size()));
        }
        Files.writeString(output,new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status","PASS_STATIC_NATIVE", "nbt_sha256",hash,"rotations",results,"scope","Native StructureTemplate coordinate transform, BlockState.rotate and collision shape checks in memory. NOT actual world placement or ticks.")));
        System.out.println("PASS four normalized rotations; 11 targets each; complete reachable feet and paths exported");
    }
}
