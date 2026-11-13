import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.AABB;

public class RouteCollisionAudit {
    static final Map<BlockPos, BlockState> BLOCKS = new HashMap<>();
    static final Map<BlockPos, List<AABB>> SHAPES = new HashMap<>();
    static final BlockGetter VIEW = new BlockGetter() {
        public BlockState getBlockState(BlockPos point) { return BLOCKS.getOrDefault(point, Blocks.AIR.defaultBlockState()); }
        public net.minecraft.world.level.block.entity.BlockEntity getBlockEntity(BlockPos point) { return null; }
        public net.minecraft.world.level.material.FluidState getFluidState(BlockPos point) { return getBlockState(point).getFluidState(); }
        public int getHeight() { return 20; }
        public int getMinBuildHeight() { return 0; }
    };
    record Node(int quarterX, int halfY, int quarterZ) {
        double x() { return quarterX / 4.0; }
        double y() { return halfY / 2.0; }
        double z() { return quarterZ / 4.0; }
        List<Double> position() { return List.of(x(), y(), z()); }
    }
    record Edge(Node from, String mode, List<List<Double>> sweepWaypoints) {}
    static void require(boolean value, String description) {
        if (!value) throw new IllegalStateException(description);
    }
    static List<AABB> shape(BlockPos point) {
        return SHAPES.computeIfAbsent(point.immutable(), key -> VIEW.getBlockState(key).getCollisionShape(VIEW, key).toAabbs().stream().map(box -> box.move(key)).toList());
    }
    static boolean collision(AABB volume) {
        for (int blockX = (int)Math.floor(volume.minX); blockX <= (int)Math.floor(volume.maxX); blockX++) {
            for (int blockY = (int)Math.floor(volume.minY); blockY <= (int)Math.floor(volume.maxY); blockY++) {
                for (int blockZ = (int)Math.floor(volume.minZ); blockZ <= (int)Math.floor(volume.maxZ); blockZ++) {
                    for (AABB box : shape(new BlockPos(blockX, blockY, blockZ))) if (box.intersects(volume)) return true;
                }
            }
        }
        return false;
    }
    static AABB body(double positionX, double positionY, double positionZ) {
        return new AABB(positionX - .3 + 1e-6, positionY + 1e-6, positionZ - .3 + 1e-6,
                positionX + .3 - 1e-6, positionY + 1.8 - 1e-6, positionZ + .3 - 1e-6);
    }
    static boolean dry(Node node) {
        AABB bounds = body(node.x(), node.y(), node.z());
        for (int blockX = (int)Math.floor(bounds.minX); blockX <= (int)Math.floor(bounds.maxX); blockX++) {
            for (int blockY = (int)Math.floor(bounds.minY); blockY <= (int)Math.floor(bounds.maxY); blockY++) {
                for (int blockZ = (int)Math.floor(bounds.minZ); blockZ <= (int)Math.floor(bounds.maxZ); blockZ++) {
                    if (!VIEW.getFluidState(new BlockPos(blockX, blockY, blockZ)).isEmpty()) return false;
                }
            }
        }
        return true;
    }
    static boolean supported(Node node) {
        return collision(new AABB(node.x() - .299, node.y() - .015, node.z() - .299, node.x() + .299, node.y() - .000001, node.z() + .299));
    }
    static boolean stand(Node node) {
        return node.x() >= .3 && node.x() <= 28.7 && node.z() >= .3 && node.z() <= 28.7 && node.y() >= 1 && node.y() <= 16
                && !collision(body(node.x(), node.y(), node.z())) && supported(node) && dry(node);
    }
    static boolean sweep(double fromX, double fromY, double fromZ, double toX, double toY, double toZ) {
        require(fromX == toX && fromY == toY || fromX == toX && fromZ == toZ || fromY == toY && fromZ == toZ, "Only axis-aligned swept segments");
        return !collision(body(fromX, fromY, fromZ).expandTowards(toX - fromX, toY - fromY, toZ - fromZ));
    }
    static Edge transition(Node from, int deltaX, int deltaZ, Node to, String mode) {
        List<List<Double>> waypoints = new ArrayList<>();
        waypoints.add(from.position());
        if (to.halfY > from.halfY) {
            if (!sweep(from.x(), from.y(), from.z(), from.x(), to.y(), from.z())
                    || !sweep(from.x(), to.y(), from.z(), to.x(), to.y(), to.z())) return null;
            waypoints.add(List.of(from.x(), to.y(), from.z()));
        } else if (to.halfY < from.halfY) {
            if (!sweep(from.x(), from.y(), from.z(), to.x(), from.y(), to.z())
                    || !sweep(to.x(), from.y(), to.z(), to.x(), to.y(), to.z())) return null;
            waypoints.add(List.of(to.x(), from.y(), to.z()));
        } else if (!sweep(from.x(), from.y(), from.z(), to.x(), to.y(), to.z())) return null;
        waypoints.add(to.position());
        return new Edge(from, mode, waypoints);
    }
    static Map<Node, Edge> search(Node start) {
        require(stand(start), "Start volume not standable");
        Map<Node, Edge> previous = new HashMap<>();
        ArrayDeque<Node> pending = new ArrayDeque<>();
        previous.put(start, new Edge(start, "start", List.of(start.position())));
        pending.add(start);
        while (!pending.isEmpty()) {
            Node from = pending.removeFirst();
            for (int[] offset : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                for (int deltaY : new int[]{0,1,-1}) {
                    Node to = new Node(from.quarterX + offset[0], from.halfY + deltaY, from.quarterZ + offset[1]);
                    if (previous.containsKey(to) || !stand(to)) continue;
                    Edge edge = transition(from, offset[0], offset[1], to, deltaY == 0 ? "level" : deltaY > 0 ? "step_up_0.5" : "step_down_0.5");
                    if (edge == null) continue;
                    previous.put(to, edge);
                    pending.addLast(to);
                }
            }
        }
        return previous;
    }
    static Node feet(int blockX, int blockY, int blockZ) { return new Node(blockX * 4 + 2, blockY * 2, blockZ * 4 + 2); }
    static Map<String,Object> route(Map<Node,Edge> previous, int[] target) {
        Node cursor = feet(target[0], target[1], target[2]);
        require(previous.containsKey(cursor), "Target unreachable by swept collision: " + Arrays.toString(target));
        List<Map<String,Object>> steps = new ArrayList<>();
        while (true) {
            Edge edge = previous.get(cursor);
            steps.add(Map.of("feet_center", cursor.position(), "mode", edge.mode, "sweep_waypoints", edge.sweepWaypoints));
            if (edge.mode.equals("start")) break;
            cursor = edge.from;
        }
        Collections.reverse(steps);
        return Map.of("target_block_feet", target, "status", "PASS", "path", steps);
    }
    static List<Map<String,Object>> stairs(int baseX) {
        List<Map<String,Object>> records = new ArrayList<>();
        for (int lane = baseX; lane <= baseX + 2; lane++) {
            for (int step = 0; step < 7; step++) {
                BlockPos point = new BlockPos(lane, step + 2, 26 - step);
                BlockState state = VIEW.getBlockState(point);
                require(state.getBlock() instanceof StairBlock, "Stair missing: " + point);
                require(state.getValue(StairBlock.FACING) == Direction.NORTH && state.getValue(StairBlock.HALF) == Half.BOTTOM
                        && state.getValue(StairBlock.SHAPE) == StairsShape.STRAIGHT, "Stair facing/half/shape mismatch: " + point);
                double lowZ = point.getZ() + .85;
                double highZ = point.getZ() + .35;
                double lowY = point.getY() + .5;
                double highY = point.getY() + 1;
                require(!collision(body(lane + .5, lowY, lowZ)), "Low tread head/body obstructed: " + point);
                require(!collision(body(lane + .5, highY, highZ)), "High tread head/body obstructed: " + point);
                require(collision(new AABB(lane + .45, lowY - .01, lowZ - .02, lane + .55, lowY - .000001, lowZ + .02)), "Low tread absent");
                require(collision(new AABB(lane + .45, highY - .01, highZ - .02, lane + .55, highY - .000001, highZ + .02)), "High tread absent");
                records.add(Map.of("block", List.of(point.getX(),point.getY(),point.getZ()), "facing", "north", "half", "bottom", "shape", "straight",
                        "low_tread_center", List.of(lane + .5,lowY,lowZ), "high_tread_center", List.of(lane + .5,highY,highZ),
                        "player_body_clearance", List.of(.6,1.8), "native_collision_boxes", shape(point).stream().map(box -> List.of(box.minX,box.minY,box.minZ,box.maxX,box.maxY,box.maxZ)).toList()));
            }
        }
        return records;
    }
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path templatePath = Path.of(arguments[0]);
        CompoundTag data = NbtIo.readCompressed(templatePath.toFile());
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index=0; index<palette.size(); index++) states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette.getCompound(index)));
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index=0; index<records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            ListTag pos = record.getList("pos", Tag.TAG_INT);
            BLOCKS.put(new BlockPos(pos.getInt(0),pos.getInt(1),pos.getInt(2)), states.get(record.getInt("state")));
        }
        List<Map<String,Object>> left = stairs(2);
        List<Map<String,Object>> right = stairs(24);
        Map<Node,Edge> previous = search(feet(14,2,0));
        List<Map<String,Object>> paths = new ArrayList<>();
        for (int[] target : new int[][]{{14,2,3},{0,2,14},{28,2,14},{14,9,0},{3,9,18},{25,9,18},{14,5,16}}) paths.add(route(previous,target));
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("status", "PASS_OFFLINE_SWEPT_AABB");
        result.put("source_sha256", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(templatePath))));
        result.put("player", Map.of("width",.6,"height",1.8,"step_height_used",.5,"horizontal_resolution",.25,"no_jump",true,"no_flight",true));
        result.put("scope", "Actual vanilla collision shapes in a read-only BlockGetter, swept axis-aligned body per edge, support and dry volume. Conservative quarter-block sampled route, not ServerPlayer navigation, physics tick or world placement.");
        result.put("reachable_sample_nodes", previous.size());
        result.put("left_stairs",left);
        result.put("right_stairs",right);
        result.put("target_paths",paths);
        Files.writeString(Path.of(arguments[1]),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
        System.out.println("PASS all 7 targets; north-facing bottom straight stairs 42/42; native .6x1.8 body, .5 step, swept head clearance; nodes="+previous.size());
    }
}
