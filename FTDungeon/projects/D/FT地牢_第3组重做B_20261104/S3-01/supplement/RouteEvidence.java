import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class RouteEvidence {
    private static final Map<BlockPos, BlockState> blocks = new HashMap<>();

    private static boolean safe(BlockPos pos) {
        BlockState feet = blocks.get(pos);
        BlockState head = blocks.get(pos.above());
        BlockState floor = blocks.get(pos.below());
        return feet != null && head != null && floor != null
            && feet.getCollisionShape(EmptyBlockGetter.INSTANCE, pos).isEmpty()
            && head.getCollisionShape(EmptyBlockGetter.INSTANCE, pos.above()).isEmpty()
            && floor.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, pos.below());
    }

    private static int[] coordinates(BlockPos pos) {
        return new int[]{pos.getX(), pos.getY(), pos.getZ()};
    }

    private static Map<String, Object> route(BlockPos start, BlockPos goal) {
        Map<BlockPos, BlockPos> previous = new HashMap<>();
        ArrayList<BlockPos> queue = new ArrayList<>();
        if (safe(start)) { queue.add(start); previous.put(start, null); }
        for (int index = 0; index < queue.size(); index++) {
            BlockPos current = queue.get(index);
            for (BlockPos next : new BlockPos[]{current.north(), current.east(), current.south(), current.west()}) {
                if (previous.containsKey(next) || !safe(next)) continue;
                queue.add(next);
                previous.put(next, current);
            }
        }
        boolean reached = previous.containsKey(goal);
        ArrayList<int[]> steps = new ArrayList<>();
        if (reached) for (BlockPos cursor = goal; cursor != null; cursor = previous.get(cursor)) steps.add(coordinates(cursor));
        Collections.reverse(steps);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("start", coordinates(start));
        result.put("goal", coordinates(goal));
        result.put("reachable", reached);
        result.put("steps", steps);
        if (!reached) throw new IllegalStateException("Unreachable target " + goal);
        return result;
    }

    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path source = Path.of(arguments[0]);
        Path output = Path.of(arguments[1]);
        byte[] bytes = Files.readAllBytes(source.resolve("room.nbt"));
        String sha256 = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
        JsonObject targets = new Gson().fromJson(Files.readString(source.resolve("supplement/route-targets.json")), JsonObject.class);
        if (!sha256.equals(targets.get("sha256").getAsString())) throw new IllegalStateException("Hash mismatch");
        CompoundTag nbt = NbtIo.readCompressed(source.resolve("room.nbt").toFile());
        ListTag palette = nbt.getList("palette", Tag.TAG_COMPOUND);
        ListTag records = nbt.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            ListTag pos = record.getList("pos", Tag.TAG_INT);
            blocks.put(new BlockPos(pos.getInt(0), pos.getInt(1), pos.getInt(2)), NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette.getCompound(record.getInt("state"))));
        }
        Map<String, BlockPos> points = new LinkedHashMap<>();
        for (var element : targets.getAsJsonArray("targets")) {
            JsonObject entry = element.getAsJsonObject();
            var pos = entry.getAsJsonArray("pos");
            points.put(entry.get("id").getAsString(), new BlockPos(pos.get(0).getAsInt(), pos.get(1).getAsInt(), pos.get(2).getAsInt()));
        }
        Map<String, Object> routes = new LinkedHashMap<>();
        for (var entry : points.entrySet()) routes.put(entry.getKey(), route(points.get("north"), entry.getValue()));
        var ring = targets.getAsJsonArray("ring_order");
        for (int index = 1; index < ring.size(); index++) {
            String start = ring.get(index - 1).getAsString();
            String goal = ring.get(index).getAsString();
            routes.put(start + "_to_" + goal, route(points.get(start), points.get(goal)));
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "PASS");
        report.put("sha256", sha256);
        report.put("scope", "native 1.20.1 full-block floor and empty foot/head collision at integer feet Y=2; static cardinal motion, no diagonal, step, jump, entity simulation or world ticks");
        report.put("routes", routes);
        report.put("world_tick_test", "NOT_RUN");
        Files.writeString(output.resolve("route-collision.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println("PASS " + routes.size() + " native route checks, " + sha256);
    }
}
