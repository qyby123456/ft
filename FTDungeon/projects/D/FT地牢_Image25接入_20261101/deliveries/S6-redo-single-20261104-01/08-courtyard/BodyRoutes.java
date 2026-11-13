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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class BodyRoutes {
    private record Point(int halfX, int halfY, int halfZ) {}
    private static final Map<BlockPos, BlockState> BLOCKS = new HashMap<>();

    private static boolean clear(double centerX, double feetY, double centerZ) {
        var body = new AABB(centerX - 0.3, feetY + 0.000001, centerZ - 0.3, centerX + 0.3, feetY + 1.8, centerZ + 0.3);
        if (body.minX < 0 || body.minZ < 0 || body.maxX > 37 || body.maxZ > 37) return false;
        for (int blockX = (int) Math.floor(body.minX); blockX <= Math.floor(body.maxX); blockX++) {
            for (int blockZ = (int) Math.floor(body.minZ); blockZ <= Math.floor(body.maxZ); blockZ++) {
                for (int blockY = (int) Math.floor(feetY); blockY <= Math.floor(feetY + 1.8); blockY++) {
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

    private static double ground(double centerX, double centerZ, double ceiling) {
        double highest = -1;
        for (int blockX = (int) Math.floor(centerX - 0.3); blockX <= Math.floor(centerX + 0.3); blockX++) {
            for (int blockZ = (int) Math.floor(centerZ - 0.3); blockZ <= Math.floor(centerZ + 0.3); blockZ++) {
                for (int blockY = 1; blockY <= 14; blockY++) {
                    var position = new BlockPos(blockX, blockY, blockZ);
                    var state = BLOCKS.getOrDefault(position, Blocks.AIR.defaultBlockState());
                    for (var local : state.getCollisionShape(EmptyBlockGetter.INSTANCE, position).toAabbs()) {
                        var bounds = local.move(blockX, blockY, blockZ);
                        if (bounds.maxY <= ceiling + 0.000001 && bounds.maxX > centerX - 0.3 && bounds.minX < centerX + 0.3 && bounds.maxZ > centerZ - 0.3 && bounds.minZ < centerZ + 0.3) highest = Math.max(highest, bounds.maxY);
                    }
                }
            }
        }
        if (highest < 3 || highest > 13 || Math.abs(highest * 2 - Math.rint(highest * 2)) > 0.000001) return -1;
        return clear(centerX, highest, centerZ) ? highest : -1;
    }

    private static boolean sweep(Point start, double startY, Point end, double endY) {
        if (Math.abs(startY - endY) > 0.500001) return false;
        double raisedY = Math.max(startY, endY);
        if (!clear(start.halfX / 2.0, raisedY, start.halfZ / 2.0) || !clear(end.halfX / 2.0, raisedY, end.halfZ / 2.0)) return false;
        for (int sample = 0; sample <= 5; sample++) {
            double fraction = sample / 5.0;
            double centerX = (start.halfX + (end.halfX - start.halfX) * fraction) / 2.0;
            double centerZ = (start.halfZ + (end.halfZ - start.halfZ) * fraction) / 2.0;
            double surface = ground(centerX, centerZ, raisedY);
            if (surface < 0 || surface > raisedY + 0.000001 || !clear(centerX, raisedY, centerZ)) return false;
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
        var elevations = new HashMap<Point, Double>();
        for (int halfX = 1; halfX <= 73; halfX++) {
            for (int halfZ = 1; halfZ <= 73; halfZ++) {
                for (int halfY = 6; halfY <= 26; halfY++) {
                    double feetY = ground(halfX / 2.0, halfZ / 2.0, halfY / 2.0);
                    if (feetY == halfY / 2.0) elevations.put(new Point(halfX, halfY, halfZ), feetY);
                }
            }
        }
        Point entrance = new Point(37, 6, 1);
        if (!elevations.containsKey(entrance) || elevations.get(entrance) != 3) throw new IllegalStateException("Entrance body blocked");
        Set<Point> visited = new HashSet<>();
        var pending = new ArrayDeque<Point>();
        visited.add(entrance);
        pending.add(entrance);
        int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!pending.isEmpty()) {
            var current = pending.removeFirst();
            for (var offset : offsets) {
                for (int halfRise = -1; halfRise <= 1; halfRise++) {
                    var next = new Point(current.halfX + offset[0], current.halfY + halfRise, current.halfZ + offset[1]);
                    if (visited.contains(next) || !elevations.containsKey(next)) continue;
                    if (sweep(current, elevations.get(current), next, elevations.get(next))) {
                        visited.add(next);
                        pending.addLast(next);
                    }
                }
            }
        }
        var targets = new ArrayList<Object>();
        for (var target : metadata.getAsJsonArray("validation_targets")) {
            var point = target.getAsJsonArray();
            int blockX = point.get(0).getAsInt();
            int feetY = point.get(1).getAsInt();
            int blockZ = point.get(2).getAsInt();
            var node = new Point(blockX * 2 + 1, feetY * 2, blockZ * 2 + 1);
            if (!visited.contains(node) || elevations.get(node) != feetY) throw new IllegalStateException("Body route blocked: " + target);
            targets.add(Map.of("feet_cell", point, "body_center", java.util.List.of(blockX + 0.5, feetY, blockZ + 0.5), "status", "PASS"));
        }
        long halfStepPoints = visited.stream().filter(point -> elevations.get(point) % 1 == 0.5).count();
        if (halfStepPoints == 0) throw new IllegalStateException("No actual half-step stair transition checked");
        var report = Map.of("status", "PASS", "sha256", metadata.get("sha256").getAsString(), "body_width_height", java.util.List.of(0.6, 1.8), "grid_step", 0.5, "edge_sample_step", 0.1, "reachable_grid_points", visited.size(), "half_height_stair_points", halfStepPoints, "targets", targets, "scope", "Native 0.6x1.8 AABBs; layered half-grid elevations3..13; max0.5 step and lift/horizontal/drop clearance with0.1 samples. Ground and upper gallery retained as separate nodes at same XZ. Conservative discrete sampling, not continuous physics or world ticks.");
        Files.writeString(root.resolve("qa/body-routes.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println("PASS 0.6x1.8 body: " + targets.size() + " targets, " + visited.size() + " grid points, " + halfStepPoints + " half-step stair points");
    }
}
