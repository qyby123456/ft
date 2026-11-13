import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class ReachableEyesExport {
    private record Node(int eastHalf, int upHalf, int southHalf) {}
    private record Cell(List<AABB> boxes, boolean fluid, boolean hazard) {}
    private static Map<BlockPos, Cell> cells;
    private static Map<Node, Boolean> standing;
    private static boolean swimming;
    private static boolean verticalSwimming;
    private static int[] size;

    private static AABB body(double east, double up, double south) {
        return new AABB(east - 0.299, up + 0.0001, south - 0.299, east + 0.299, up + 1.7999, south + 0.299);
    }

    private static boolean clear(AABB box) {
        for (int east = (int) Math.floor(box.minX); east <= (int) Math.floor(box.maxX); east++) {
            for (int up = (int) Math.floor(box.minY); up <= (int) Math.floor(box.maxY); up++) {
                for (int south = (int) Math.floor(box.minZ); south <= (int) Math.floor(box.maxZ); south++) {
                    Cell cell = cells.get(new BlockPos(east, up, south));
                    if (cell == null) continue;
                    if (cell.hazard || (!swimming && cell.fluid)) return false;
                    for (AABB collision : cell.boxes) if (box.intersects(collision)) return false;
                }
            }
        }
        return true;
    }

    private static boolean supported(double east, double up, double south) {
        AABB probe = new AABB(east - 0.28, up - 0.055, south - 0.28, east + 0.28, up - 0.0001, south + 0.28);
        for (int floorEast = (int) Math.floor(probe.minX); floorEast <= (int) Math.floor(probe.maxX); floorEast++) {
            for (int floorUp = (int) Math.floor(probe.minY); floorUp <= (int) Math.floor(probe.maxY); floorUp++) {
                for (int floorSouth = (int) Math.floor(probe.minZ); floorSouth <= (int) Math.floor(probe.maxZ); floorSouth++) {
                    Cell cell = cells.get(new BlockPos(floorEast, floorUp, floorSouth));
                    if (cell == null || cell.hazard) continue;
                    for (AABB collision : cell.boxes) if (probe.intersects(collision)) return true;
                }
            }
        }
        return false;
    }

    private static boolean stand(Node node) {
        return standing.computeIfAbsent(node, position -> {
            double east = position.eastHalf / 2.0;
            double up = position.upHalf / 2.0;
            double south = position.southHalf / 2.0;
            if (east < 0.5 || east > size[0] - 0.5 || south < 0.5 || south > size[2] - 0.5 || up < 0 || up + 1.8 > size[1]) return false;
            return clear(body(east, up, south)) && (swimming || supported(east, up, south));
        });
    }

    private static boolean transition(Node first, Node second) {
        double firstEast = first.eastHalf / 2.0;
        double firstUp = first.upHalf / 2.0;
        double firstSouth = first.southHalf / 2.0;
        double nextUp = second.upHalf / 2.0;
        double raisedUp = Math.max(firstUp, nextUp);
        for (int index = 0; index <= 5; index++) {
            double fraction = index / 5.0;
            if (!clear(body(firstEast, firstUp + (raisedUp - firstUp) * fraction, firstSouth))) return false;
            double east = firstEast + (second.eastHalf - first.eastHalf) / 2.0 * fraction;
            double south = firstSouth + (second.southHalf - first.southHalf) / 2.0 * fraction;
            if (!clear(body(east, raisedUp, south))) return false;
            if (!clear(body(second.eastHalf / 2.0, raisedUp + (nextUp - raisedUp) * fraction, second.southHalf / 2.0))) return false;
        }
        return true;
    }

    private static Node node(com.google.gson.JsonArray coordinates) {
        return new Node((int) Math.round(coordinates.get(0).getAsDouble() * 2) + 1, (int) Math.round(coordinates.get(1).getAsDouble() * 2), (int) Math.round(coordinates.get(2).getAsDouble() * 2) + 1);
    }

    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        var input = JsonParser.parseString(Files.readString(root.resolve("route-input.json"))).getAsJsonArray();
        List<Object> reports = new ArrayList<>();
        for (var item : input) {
            JsonObject specification = item.getAsJsonObject();
            Path candidate = Path.of(specification.get("candidate").getAsString());
            String sha256 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(candidate)));
            if (!sha256.equals(specification.get("sha256").getAsString())) throw new IllegalStateException("Snapshot hash drift");
            var template = NbtIo.readCompressed(candidate.toFile());
            var palette = template.getList("palette", Tag.TAG_COMPOUND);
            List<BlockState> states = new ArrayList<>();
            for (int index = 0; index < palette.size(); index++) states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette.getCompound(index)));
            cells = new HashMap<>();
            standing = new HashMap<>();
            swimming = specification.get("swimming").getAsBoolean();
            verticalSwimming = swimming && specification.has("vertical_swimming") && specification.get("vertical_swimming").getAsBoolean();
            var dimensions = template.getList("size", Tag.TAG_INT);
            size = new int[]{dimensions.getInt(0), dimensions.getInt(1), dimensions.getInt(2)};
            var blocks = template.getList("blocks", Tag.TAG_COMPOUND);
            for (int index = 0; index < blocks.size(); index++) {
                var entry = blocks.getCompound(index);
                var coordinate = entry.getList("pos", Tag.TAG_INT);
                BlockPos position = new BlockPos(coordinate.getInt(0), coordinate.getInt(1), coordinate.getInt(2));
                BlockState state = states.get(entry.getInt("state"));
                String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                boolean hazard = name.equals("minecraft:lava") || name.equals("minecraft:fire") || name.equals("minecraft:magma_block") || name.equals("minecraft:cactus") || name.equals("minecraft:campfire") || name.equals("minecraft:powder_snow");
                List<AABB> boxes = state.getCollisionShape(EmptyBlockGetter.INSTANCE, position).toAabbs().stream().map(box -> box.move(position)).toList();
                if (!boxes.isEmpty() || !state.getFluidState().isEmpty() || hazard) cells.put(position, new Cell(boxes, !state.getFluidState().isEmpty(), hazard));
            }
            Node start = node(specification.getAsJsonArray("start"));
            Set<Node> visited = new HashSet<>();
            Map<Node,Node> parents = new HashMap<>();
            ArrayDeque<Node> queue = new ArrayDeque<>();
            if (stand(start)) {
                visited.add(start);
                queue.add(start);
            }
            while (!queue.isEmpty()) {
                Node current = queue.removeFirst();
                for (int[] direction : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                    for (int elevation : swimming && !verticalSwimming ? new int[]{0} : new int[]{0,1,-1}) {
                        Node next = new Node(current.eastHalf + direction[0], current.upHalf + elevation, current.southHalf + direction[1]);
                        if (visited.contains(next) || !stand(next) || !transition(current, next)) continue;
                        visited.add(next);
                        parents.put(next,current);
                        queue.add(next);
                    }
                }
            }
            Files.writeString(root.resolve("reachable-feet.json"), new GsonBuilder().create().toJson(visited.stream().map(point -> List.of(point.eastHalf/2.0,point.upHalf/2.0,point.southHalf/2.0)).toList()));
            List<Object> results = new ArrayList<>();
            for (var target : specification.getAsJsonArray("targets")) {
                JsonObject targetObject = target.getAsJsonObject();
                Node point = node(targetObject.getAsJsonArray("position"));
                List<Object> path = new ArrayList<>();
                if(visited.contains(point)){
                    Node cursor=point;
                    while(cursor!=null){path.add(List.of(cursor.eastHalf/2.0,cursor.upHalf/2.0,cursor.southHalf/2.0));cursor=parents.get(cursor);}
                    java.util.Collections.reverse(path);
                }
                results.add(Map.of("label", targetObject.get("label").getAsString(), "position", targetObject.get("position"), "standing_clear", stand(point), "reachable", visited.contains(point), "path_feet",path));
            }
            reports.add(Map.of("group", specification.get("group").getAsInt(), "sha256", sha256, "start_clear", stand(start), "reachable_half_grid_positions", visited.size(), "targets", results, "vertical_swimming_enabled", verticalSwimming, "scope", "Independent native collision boxes; 0.6x1.8 player, half-block grid, no jumps, maximum step 0.5; swept sampled vertical/horizontal clearance. Swimming elevation changes enabled only by explicit specification flag; no buoyancy, breath or movement physics certification. Immutable offline shapes, not game physics/world simulation. Shapes queried with EmptyBlockGetter; context-sensitive shape behavior is not certified."));
            System.out.println("GROUP " + specification.get("group") + " reachable=" + visited.size());
        }
        Files.writeString(root.resolve("author-paths.json"), new GsonBuilder().setPrettyPrinting().create().toJson(reports));
    }
}
