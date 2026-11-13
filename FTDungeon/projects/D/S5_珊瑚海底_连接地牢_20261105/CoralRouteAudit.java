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

public final class CoralRouteAudit {
    private record Node(int eastHalf, int upHalf, int southHalf) {}
    private record Search(Node position, int cost, int score, int remaining) {}
    private static int distance(Node first, Node second) {
        return Math.abs(first.eastHalf-second.eastHalf)+Math.abs(first.upHalf-second.upHalf)+Math.abs(first.southHalf-second.southHalf);
    }
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
            if (stand(start)) visited.add(start);
            List<Object> pathEvidence = new ArrayList<>();
            for (var target : specification.getAsJsonArray("targets")) {
                JsonObject targetObject = target.getAsJsonObject();
                Node goal = node(targetObject.getAsJsonArray("position"));
                if (!stand(goal) || visited.isEmpty()) continue;
                Node seed = visited.stream().min(java.util.Comparator.comparingInt(position -> distance(position, goal))).orElse(start);
                Map<Node,Integer> cost = new HashMap<>();
                Map<Node,Node> parent = new HashMap<>();
                java.util.PriorityQueue<Search> queue = new java.util.PriorityQueue<>(java.util.Comparator.comparingInt(Search::score).thenComparingInt(Search::remaining));
                cost.put(seed,0);
                queue.add(new Search(seed,0,distance(seed,goal),distance(seed,goal)));
                int expanded = 0;
                boolean found = false;
                while (!queue.isEmpty()) {
                    Search current = queue.remove();
                    if (current.cost != cost.getOrDefault(current.position,Integer.MAX_VALUE)) continue;
                    if (current.position.equals(goal)) { found = true; break; }
                    if (++expanded > 1500000) throw new IllegalStateException("Search bound exceeded: "+targetObject);
                    for (int[] direction : new int[][]{{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}}) {
                        Node next = new Node(current.position.eastHalf+direction[0],current.position.upHalf+direction[1],current.position.southHalf+direction[2]);
                        int nextCost = current.cost+1;
                        if (nextCost >= cost.getOrDefault(next,Integer.MAX_VALUE) || !stand(next) || !transition(current.position,next)) continue;
                        cost.put(next,nextCost);
                        parent.put(next,current.position);
                        int remaining = distance(next,goal);
                        queue.add(new Search(next,nextCost,nextCost+remaining,remaining));
                    }
                }
                if (found) {
                    List<Node> path = new ArrayList<>();
                    Node current = goal;
                    while (true) {
                        visited.add(current);
                        path.add(current);
                        if (current.equals(seed)) break;
                        current = parent.get(current);
                    }
                    java.util.Collections.reverse(path);
                    pathEvidence.add(Map.of("label",targetObject.get("label").getAsString(),"seed_connected_to_start",true,"expanded",expanded,"path_half_grid",path));
                }
            }
            Files.writeString(root.resolve("swim-paths.json"),new GsonBuilder().create().toJson(pathEvidence));            List<Object> results = new ArrayList<>();
            for (var target : specification.getAsJsonArray("targets")) {
                JsonObject targetObject = target.getAsJsonObject();
                Node point = node(targetObject.getAsJsonArray("position"));
                results.add(Map.of("label", targetObject.get("label").getAsString(), "position", targetObject.get("position"), "standing_clear", stand(point), "reachable", visited.contains(point)));
            }
            reports.add(Map.of("group", specification.get("group").getAsInt(), "sha256", sha256, "start_clear", stand(start), "reachable_half_grid_positions", visited.size(), "targets", results, "vertical_swimming_enabled", verticalSwimming, "scope", "Targeted A-star with native collision boxes; 0.6x1.8 player, six-axis half-block grid, swept sampling, no jumps. Each recorded path starts on an already spawn-connected path; all source targets including upper galleries retained. Visited count is recorded connected path positions, not exhaustive volume. No buoyancy, oxygen, water flow, movement physics or live world certification. EmptyBlockGetter context-sensitive behavior not certified."));
            System.out.println("GROUP " + specification.get("group") + " reachable=" + visited.size());
        }
        Files.writeString(root.resolve("independent-routes.json"), new GsonBuilder().setPrettyPrinting().create().toJson(reports));
    }
}

