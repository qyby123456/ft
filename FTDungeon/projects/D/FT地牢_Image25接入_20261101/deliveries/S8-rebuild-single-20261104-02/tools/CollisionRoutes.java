import com.google.gson.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class CollisionRoutes {
    static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    static Map<BlockPos, List<AABB>> boxes;
    static final double HALF_WIDTH = 0.3;
    static final double HEIGHT = 1.8;
    static final double EPSILON = 0.000001;
    record Node(int halfX, int halfY, int halfZ) {
        double x() { return halfX / 2.0; }
        double y() { return halfY / 2.0; }
        double z() { return halfZ / 2.0; }
        List<Double> point() { return List.of(x(), y(), z()); }
    }
    static AABB body(Node node) {
        return new AABB(node.x() - HALF_WIDTH + EPSILON, node.y() + EPSILON, node.z() - HALF_WIDTH + EPSILON, node.x() + HALF_WIDTH - EPSILON, node.y() + HEIGHT - EPSILON, node.z() + HALF_WIDTH - EPSILON);
    }
    static boolean clear(AABB region) {
        for (int blockX = (int)Math.floor(region.minX); blockX <= Math.floor(region.maxX); blockX++) {
            for (int blockY = (int)Math.floor(region.minY); blockY <= Math.floor(region.maxY); blockY++) {
                for (int blockZ = (int)Math.floor(region.minZ); blockZ <= Math.floor(region.maxZ); blockZ++) {
                    for (AABB obstacle : boxes.getOrDefault(new BlockPos(blockX, blockY, blockZ), List.of())) {
                        if (obstacle.intersects(region)) return false;
                    }
                }
            }
        }
        return true;
    }
    static boolean stand(Node node) {
        if (!clear(body(node))) return false;
        AABB sole = new AABB(node.x() - HALF_WIDTH + EPSILON, node.y() - 0.0001, node.z() - HALF_WIDTH + EPSILON, node.x() + HALF_WIDTH - EPSILON, node.y() + EPSILON, node.z() + HALF_WIDTH - EPSILON);
        return !clear(sole);
    }
    static boolean edge(Node first, Node second) {
        if (Math.abs(first.y() - second.y()) > 0.6) return false;
        double raised = Math.max(first.y(), second.y());
        AABB horizontal = new AABB(Math.min(first.x(), second.x()) - HALF_WIDTH + EPSILON, raised + EPSILON, Math.min(first.z(), second.z()) - HALF_WIDTH + EPSILON, Math.max(first.x(), second.x()) + HALF_WIDTH - EPSILON, raised + HEIGHT - EPSILON, Math.max(first.z(), second.z()) + HALF_WIDTH - EPSILON);
        AABB firstLift = new AABB(first.x() - HALF_WIDTH + EPSILON, first.y() + EPSILON, first.z() - HALF_WIDTH + EPSILON, first.x() + HALF_WIDTH - EPSILON, raised + HEIGHT - EPSILON, first.z() + HALF_WIDTH - EPSILON);
        AABB secondLift = new AABB(second.x() - HALF_WIDTH + EPSILON, second.y() + EPSILON, second.z() - HALF_WIDTH + EPSILON, second.x() + HALF_WIDTH - EPSILON, raised + HEIGHT - EPSILON, second.z() + HALF_WIDTH - EPSILON);
        return clear(horizontal) && clear(firstLift) && clear(secondLift);
    }
    static Node feet(JsonArray array) {
        return new Node(array.get(0).getAsInt() * 2 + 1, array.get(1).getAsInt() * 2, array.get(2).getAsInt() * 2 + 1);
    }
    static Node rotate(Node node) { return new Node(50 - node.halfZ, node.halfY, node.halfX); }
    static List<Integer> anchor(Node node) { return List.of((node.halfX - 1) / 2, node.halfY / 2, (node.halfZ - 1) / 2); }
    static String facing(String input, int turn) {
        List<String> order = List.of("north", "east", "south", "west");
        return order.get((order.indexOf(input) + turn) % 4);
    }
    static boolean stripReachable() {
        Node start = new Node(1, 2, 1);
        Node end = new Node(1, 4, 5);
        Set<Node> visited = new HashSet<>();
        ArrayDeque<Node> pending = new ArrayDeque<>();
        if (stand(start)) { visited.add(start); pending.add(start); }
        while (!pending.isEmpty()) {
            Node current = pending.removeFirst();
            for (int deltaZ : new int[]{-1, 1}) for (int deltaY : new int[]{-1, 0, 1}) {
                Node next = new Node(1, current.halfY + deltaY, current.halfZ + deltaZ);
                if (next.halfZ < 1 || next.halfZ > 5 || next.halfY < 2 || next.halfY > 5 || visited.contains(next)) continue;
                if (stand(next) && edge(current, next)) { visited.add(next); pending.add(next); }
            }
        }
        return visited.contains(end);
    }
    static void shapeBlock(int blockY, int blockZ, BlockState state) {
        BlockPos position = new BlockPos(0, blockY, blockZ);
        boxes.put(position, state.getCollisionShape(EmptyBlockGetter.INSTANCE, position).toAabbs().stream().map(shape -> shape.move(position)).toList());
    }
    static List<Object> controls() {
        List<Object> result = new ArrayList<>();
        for (String name : List.of("south_stair_forward", "north_stair_reverse", "full_block_no_jump", "south_stair_low_headroom")) {
            boxes = new HashMap<>();
            for (int blockZ = 0; blockZ <= 3; blockZ++) shapeBlock(0, blockZ, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            shapeBlock(1, 2, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            BlockState stair = net.minecraft.world.level.block.Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, name.equals("north_stair_reverse") ? net.minecraft.core.Direction.NORTH : net.minecraft.core.Direction.SOUTH);
            shapeBlock(1, 1, name.equals("full_block_no_jump") ? net.minecraft.world.level.block.Blocks.STONE.defaultBlockState() : stair);
            if (name.equals("south_stair_low_headroom")) shapeBlock(3, 1, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            boolean reachable = stripReachable();
            boolean expected = name.equals("south_stair_forward");
            if (reachable != expected) throw new IllegalStateException("Control failed: " + name);
            result.add(Map.of("name", name, "expected_reachable", expected, "observed_reachable", reachable, "status", "PASS", "scope", "Synthetic in-memory native collision shape control; no world placement"));
        }
        return result;
    }
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path nbt = Path.of(arguments[0]);
        JsonObject metadata = JsonParser.parseString(Files.readString(Path.of(arguments[1]))).getAsJsonObject();
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(nbt)));
        if (!hash.equals(metadata.get("sha256").getAsString())) throw new IllegalStateException("Wrong S8-02 NBT");
        CompoundTag data = NbtIo.readCompressed(nbt.toFile());
        ListTag paletteNbt = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        if (data.getInt("DataVersion") != 3465) throw new IllegalStateException("DataVersion");
        for (int index = 0; index < paletteNbt.size(); index++) {
            CompoundTag value = paletteNbt.getCompound(index);
            var identifier = new net.minecraft.resources.ResourceLocation(value.getString("Name"));
            if (!identifier.getNamespace().equals("minecraft") || !BuiltInRegistries.BLOCK.containsKey(identifier)) throw new IllegalStateException("Invalid block " + identifier);
            var block = BuiltInRegistries.BLOCK.get(identifier);
            for (String key : value.getCompound("Properties").getAllKeys()) {
                var property = block.getStateDefinition().getProperty(key);
                if (property == null || property.getValue(value.getCompound("Properties").getString(key)).isEmpty()) throw new IllegalStateException("Invalid property " + key);
            }
        }
        for (int index = 0; index < paletteNbt.size(); index++) palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), paletteNbt.getCompound(index)));
        Map<BlockPos, BlockState> states = new HashMap<>();
        ListTag blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag block = blocks.getCompound(index);
            ListTag position = block.getList("pos", Tag.TAG_INT);
            states.put(new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2)), palette.get(block.getInt("state")));
        }
        var structureTemplate = new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate();
        structureTemplate.load(BuiltInRegistries.BLOCK.asLookup(), data);
        CompoundTag saved = structureTemplate.save(new CompoundTag());
        if (saved.getList("blocks", Tag.TAG_COMPOUND).size() != blocks.size()) throw new IllegalStateException("Roundtrip blocks changed");
        Map<String, Node> targets = new LinkedHashMap<>();
        targets.put("interior", feet(metadata.getAsJsonArray("interior")));
        for (JsonElement item : metadata.getAsJsonArray("route_targets")) targets.put(item.getAsJsonObject().get("label").getAsString(), feet(item.getAsJsonObject().getAsJsonArray("position")));
        Node entrance = feet(metadata.getAsJsonArray("entrance"));
        List<Object> turns = new ArrayList<>();
        boolean allPass = true;
        for (int turn = 0; turn < 4; turn++) {
            boxes = new HashMap<>();
            List<Object> stairs = new ArrayList<>();
            for (var entry : states.entrySet()) {
                BlockPos position = entry.getKey();
                List<AABB> local = entry.getValue().getCollisionShape(EmptyBlockGetter.INSTANCE, position).toAabbs();
                if (!local.isEmpty()) boxes.put(position, local.stream().map(shape -> shape.move(position)).toList());
                if (entry.getValue().getBlock() instanceof net.minecraft.world.level.block.StairBlock) {
                    stairs.add(Map.of("block_position", List.of(position.getX(), position.getY(), position.getZ()), "state", entry.getValue().toString(), "native_local_shape_boxes", local.stream().map(shape -> List.of(shape.minX, shape.minY, shape.minZ, shape.maxX, shape.maxY, shape.maxZ)).toList()));
                }
            }
            final Map<BlockPos, BlockState> snapshot = states;
            net.minecraft.world.level.LevelReader view = (net.minecraft.world.level.LevelReader) java.lang.reflect.Proxy.newProxyInstance(net.minecraft.world.level.LevelReader.class.getClassLoader(), new Class<?>[]{net.minecraft.world.level.LevelReader.class}, (proxy, method, parameters) -> {
                String name = method.getName();
                if (name.equals("getBlockState")) return snapshot.getOrDefault((BlockPos)parameters[0], net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                if (name.equals("getFluidState")) return snapshot.getOrDefault((BlockPos)parameters[0], net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()).getFluidState();
                if (name.equals("getMinBuildHeight")) return 0;
                if (name.equals("getHeight") || name.equals("getMaxBuildHeight")) return 15;
                if (name.equals("toString")) return "S8-02 immutable static block snapshot";
                throw new UnsupportedOperationException(name);
            });
            List<Object> supportChecks = new ArrayList<>();
            for (var entry : states.entrySet()) {
                String identifier = BuiltInRegistries.BLOCK.getKey(entry.getValue().getBlock()).toString();
                if (identifier.endsWith("amethyst_cluster") || identifier.endsWith("lantern") || identifier.endsWith("_door")) {
                    boolean survives = entry.getValue().canSurvive(view, entry.getKey());
                    supportChecks.add(Map.of("block", identifier, "position", List.of(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()), "canSurvive", survives));
                    if (!survives) throw new IllegalStateException("Unsupported fragile block " + entry.getKey());
                }
            }
            Map<Node, Node> previous = new HashMap<>();
            ArrayDeque<Node> pending = new ArrayDeque<>();
            Map<Node, Boolean> standing = new HashMap<>();
            if (stand(entrance)) { previous.put(entrance, entrance); pending.add(entrance); }
            while (!pending.isEmpty()) {
                Node current = pending.removeFirst();
                for (int[] direction : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                    for (int deltaY : new int[]{0,1,-1}) {
                        Node next = new Node(current.halfX + direction[0], current.halfY + deltaY, current.halfZ + direction[1]);
                        if (next.halfX < 1 || next.halfX > 49 || next.halfZ < 1 || next.halfZ > 49 || next.halfY < 2 || next.halfY > 25 || previous.containsKey(next)) continue;
                        if (!standing.computeIfAbsent(next, CollisionRoutes::stand) || !edge(current, next)) continue;
                        previous.put(next, current);
                        pending.add(next);
                    }
                }
            }
            List<Object> routeReports = new ArrayList<>();
            for (var target : targets.entrySet()) {
                List<List<Double>> path = new ArrayList<>();
                boolean reachable = previous.containsKey(target.getValue());
                if (reachable) {
                    Node cursor = target.getValue();
                    while (!cursor.equals(entrance)) { path.add(cursor.point()); cursor = previous.get(cursor); }
                    path.add(entrance.point());
                    Collections.reverse(path);
                } else allPass = false;
                routeReports.add(Map.of("label", target.getKey(), "anchor_block_feet", anchor(target.getValue()), "body_center_destination", target.getValue().point(), "reachable_without_jump", reachable, "full_path_centers", path));
            }
            List<Object> anchors = new ArrayList<>();
            for (JsonElement item : metadata.getAsJsonArray("connections")) {
                JsonObject connection = item.getAsJsonObject();
                Node port = feet(connection.getAsJsonArray("position"));
                for (int count = 0; count < turn; count++) port = rotate(port);
                String direction = facing(connection.get("facing").getAsString(), turn);
                boolean supported = true;
                boolean headroom = true;
                for (int offset = -1; offset <= 1; offset++) {
                    Node lane = new Node(port.halfX + ((direction.equals("east") || direction.equals("west")) ? 0 : offset * 2), port.halfY, port.halfZ + ((direction.equals("east") || direction.equals("west")) ? offset * 2 : 0));
                    supported &= stand(lane);
                    headroom &= clear(new AABB(lane.x() - 0.49, lane.y() + EPSILON, lane.z() - 0.49, lane.x() + 0.49, lane.y() + 3 - EPSILON, lane.z() + 0.49));
                }
                allPass &= supported && headroom;
                anchors.add(Map.of("original_facing", connection.get("facing").getAsString(), "rotated_facing", direction, "local_block_feet", anchor(port), "three_lanes_supported", supported, "three_block_headroom", headroom, "world_anchor_if_origin_100_64_200", List.of(anchor(port).get(0)+100, anchor(port).get(1)+64, anchor(port).get(2)+200)));
            }
            turns.add(Map.of("degrees_clockwise", turn * 90, "reachable_half_grid_nodes", previous.size(), "anchors", anchors, "routes", routeReports, "stairs", stairs, "static_fragile_support", supportChecks));
            Map<BlockPos, BlockState> nextStates = new HashMap<>();
            for (var entry : states.entrySet()) { BlockPos position = entry.getKey(); nextStates.put(new BlockPos(24-position.getZ(), position.getY(), position.getX()), entry.getValue().rotate(Rotation.CLOCKWISE_90)); }
            states = nextStates;
            entrance = rotate(entrance);
            targets.replaceAll((label, target) -> rotate(target));
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", allPass ? "COLLISION_ROUTE_SUPPLEMENT_PASS" : "COLLISION_ROUTE_SUPPLEMENT_FOUND_UNREACHABLE");
        report.put("source_nbt_sha256", hash);
        report.put("native_palette_properties_template_roundtrip", "PASS");
        report.put("size", List.of(25,15,25));
        report.put("body_width_height", List.of(0.6, 1.8));
        report.put("max_step_height", 0.6);
        report.put("horizontal_vertical_grid", List.of(0.5, 0.5));
        report.put("criterion", "Native collision AABBs, supported non-intersecting standing nodes; each cardinal edge <=0.5 horizontal and <=0.5 vertical. Full swept horizontal box at raised feet plus source/destination vertical sweeps clear. Symmetric reversible edges; no jump and no stair-class adjacency exemption. AABB sole intersection provides partial-footprint support. Conservative sampled step model, not vanilla Entity.move or world simulation.");
        report.put("turns", turns);
        report.put("positive_negative_controls", controls());
        report.put("actual_world_placement", "NOT_RUN");
        report.put("400tick_stability", "NOT_RUN");
        report.put("independent_acceptance", "NOT_GRANTED_PRODUCER_SUPPLEMENT_ONLY");
        Files.writeString(Path.of(arguments[2]), JSON.toJson(report));
        System.out.println(report.get("status"));
    }
}
