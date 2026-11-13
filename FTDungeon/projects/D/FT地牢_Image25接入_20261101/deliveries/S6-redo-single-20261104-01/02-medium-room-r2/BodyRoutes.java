import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class BodyRoutes {
    private record Point(int halfX, int halfZ) {}
    private static final Map<BlockPos, BlockState> BLOCKS = new HashMap<>();

    private static boolean safe(double centerX, double centerZ) {
        var body = new AABB(centerX - 0.3, 3.000001, centerZ - 0.3, centerX + 0.3, 4.8, centerZ + 0.3);
        if (body.minX < 0 || body.minZ < 0 || body.maxX > 25 || body.maxZ > 25) return false;
        for (int blockX = (int) Math.floor(body.minX); blockX <= Math.floor(body.maxX); blockX++) {
            for (int blockZ = (int) Math.floor(body.minZ); blockZ <= Math.floor(body.maxZ); blockZ++) {
                var floorPos = new BlockPos(blockX, 2, blockZ);
                var floor = BLOCKS.getOrDefault(floorPos, Blocks.AIR.defaultBlockState());
                if (!floor.getFluidState().isEmpty() || !floor.isFaceSturdy(EmptyBlockGetter.INSTANCE, floorPos, Direction.UP)) return false;
                for (int blockY = 3; blockY <= 4; blockY++) {
                    var position = new BlockPos(blockX, blockY, blockZ);
                    var state = BLOCKS.getOrDefault(position, Blocks.AIR.defaultBlockState());
                    if (!state.getFluidState().isEmpty() || state.is(Blocks.FIRE) || state.is(Blocks.MAGMA_BLOCK)) return false;
                    for (var bounds : state.getCollisionShape(EmptyBlockGetter.INSTANCE, position).toAabbs()) {
                        if (body.intersects(bounds.move(blockX, blockY, blockZ))) return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean sweep(Point start, Point end) {
        for (int sample = 0; sample <= 5; sample++) {
            double fraction = sample / 5.0;
            if (!safe((start.halfX + (end.halfX - start.halfX) * fraction) / 2.0, (start.halfZ + (end.halfZ - start.halfZ) * fraction) / 2.0)) return false;
        }
        return true;
    }

    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var root = Path.of(arguments[0]);
        var metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        var data = NbtIo.readCompressed(root.resolve("room.nbt").toFile());
        var palette = data.getList("palette", 10);
        var entries = data.getList("blocks", 10);
        for (int index = 0; index < entries.size(); index++) {
            var record = entries.getCompound(index);
            var position = record.getList("pos", 3);
            BLOCKS.put(new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2)), NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette.getCompound(record.getInt("state"))));
        }
        Point entrance = new Point(25, 1);
        if (!safe(12.5, 0.5)) throw new IllegalStateException("Entrance body blocked");
        Set<Point> visited = new HashSet<>();
        var pending = new ArrayDeque<Point>();
        visited.add(entrance);
        pending.add(entrance);
        int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!pending.isEmpty()) {
            var current = pending.removeFirst();
            for (var offset : offsets) {
                var next = new Point(current.halfX + offset[0], current.halfZ + offset[1]);
                if (next.halfX < 1 || next.halfX > 49 || next.halfZ < 1 || next.halfZ > 49 || visited.contains(next)) continue;
                if (sweep(current, next)) {
                    visited.add(next);
                    pending.addLast(next);
                }
            }
        }
        var targets = new ArrayList<Object>();
        for (var target : metadata.getAsJsonArray("validation_targets")) {
            var point = target.getAsJsonArray();
            int blockX = point.get(0).getAsInt();
            int blockZ = point.get(2).getAsInt();
            if (!visited.contains(new Point(blockX * 2 + 1, blockZ * 2 + 1))) throw new IllegalStateException("Body route blocked: " + target);
            targets.add(Map.of("feet_cell", point, "body_center", java.util.List.of(blockX + 0.5, 3, blockZ + 0.5), "status", "PASS"));
        }
        var report = Map.of("status", "PASS", "sha256", metadata.get("sha256").getAsString(), "body_width_height", java.util.List.of(0.6, 1.8), "grid_step", 0.5, "edge_sample_step", 0.1, "reachable_grid_points", visited.size(), "targets", targets, "scope", "Static native collision AABBs and sturdy dry floor under entire footprint; y3 flat routes, half-block grid and 0.1-block edge samples; not continuous physics, Forge placement or tick simulation.");
        Files.writeString(root.resolve("qa/body-routes.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println("PASS 0.6x1.8 body at y3: " + targets.size() + " targets, " + visited.size() + " half-grid points, sampled edge sweeps");
    }
}
