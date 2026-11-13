import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;

public final class NativeQA {
    private static final Map<BlockPos, BlockState> blocks = new HashMap<>();
    private static LevelReader view;

    private static boolean fits(double gridX, double gridZ) {
        double centerX = gridX * 0.5;
        double centerZ = gridZ * 0.5;
        AABB body = new AABB(centerX - 0.3, 2.00001, centerZ - 0.3, centerX + 0.3, 3.8, centerZ + 0.3);
        if (body.minX < 0 || body.minZ < 0 || body.maxX > 35 || body.maxZ > 57) return false;
        for (int x = (int)Math.floor(body.minX); x <= Math.floor(body.maxX); x++) {
            for (int z = (int)Math.floor(body.minZ); z <= Math.floor(body.maxZ); z++) {
                BlockPos floor = new BlockPos(x, 1, z);
                if (!blocks.getOrDefault(floor, Blocks.AIR.defaultBlockState()).isCollisionShapeFullBlock(view, floor)) return false;
                for (int y = 2; y <= 3; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    for (AABB shape : blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState()).getCollisionShape(view, pos).toAabbs()) if (shape.move(x, y, z).intersects(body)) return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path source = Path.of(arguments[0]);
        Path output = Path.of(arguments[1]);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        byte[] bytes = Files.readAllBytes(source.resolve("room.nbt"));
        String hash = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
        JsonObject tags = gson.fromJson(Files.readString(output.resolve("vanilla-block-tags.json")), JsonObject.class);
        Map<TagKey<Block>, List<Holder<Block>>> bindings = new HashMap<>();
        for (var tag : tags.entrySet()) {
            List<Holder<Block>> values = new ArrayList<>();
            for (var value : tag.getValue().getAsJsonArray()) values.add(BuiltInRegistries.BLOCK.getHolderOrThrow(net.minecraft.resources.ResourceKey.create(Registries.BLOCK, new ResourceLocation(value.getAsString()))));
            bindings.put(TagKey.create(Registries.BLOCK, new ResourceLocation(tag.getKey())), values);
        }
        BuiltInRegistries.BLOCK.bindTags(bindings);
        CompoundTag data = NbtIo.readCompressed(source.resolve("room.nbt").toFile());
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation id = new ResourceLocation(entry.getString("Name"));
            if (!BuiltInRegistries.BLOCK.containsKey(id)) throw new IllegalStateException("Unknown block " + id);
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
            for (String key : entry.getCompound("Properties").getAllKeys()) {
                var property = state.getBlock().getStateDefinition().getProperty(key);
                if (property == null || property.getValue(entry.getCompound("Properties").getString(key)).isEmpty()) throw new IllegalStateException("Invalid state " + id + " " + key);
            }
            states.add(state);
        }
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag entry = records.getCompound(index);
            ListTag pos = entry.getList("pos", Tag.TAG_INT);
            blocks.put(new BlockPos(pos.getInt(0), pos.getInt(1), pos.getInt(2)), states.get(entry.getInt("state")));
        }
        view = (LevelReader)Proxy.newProxyInstance(LevelReader.class.getClassLoader(), new Class<?>[]{LevelReader.class}, (proxy, method, params) -> {
            String name = method.getName();
            if (name.equals("getBlockState")) return blocks.getOrDefault((BlockPos)params[0], Blocks.AIR.defaultBlockState());
            if (name.equals("getFluidState")) return blocks.getOrDefault((BlockPos)params[0], Blocks.AIR.defaultBlockState()).getFluidState();
            if (name.equals("getMinBuildHeight")) return 0;
            if (name.equals("getHeight") || name.equals("getMaxBuildHeight")) return 27;
            if (name.equals("getRawBrightness") || name.equals("getMaxLocalRawBrightness")) return 15;
            if (name.startsWith("hasChunk")) return true;
            if (name.equals("toString")) return "S3-04 immutable native tagged view";
            throw new UnsupportedOperationException(name);
        });
        List<Object> failures = new ArrayList<>();
        for (var entry : blocks.entrySet()) if (!entry.getValue().canSurvive(view, entry.getKey())) failures.add(Map.of("pos", List.of(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()), "block", entry.getValue().toString()));
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        if (template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() != 53865) throw new IllegalStateException("Native roundtrip count");
        JsonObject routeInput = gson.fromJson(Files.readString(source.resolve("route-targets.json")), JsonObject.class);
        if (!routeInput.get("sha256").getAsString().equals(hash)) throw new IllegalStateException("Route hash mismatch");
        int start = 35 * 1000 + 1;
        Map<Integer, Integer> previous = new HashMap<>();
        List<Integer> queue = new ArrayList<>();
        if (fits(35, 1)) { previous.put(start, null); queue.add(start); }
        for (int index = 0; index < queue.size(); index++) {
            int current = queue.get(index);
            int gridX = current / 1000;
            int gridZ = current % 1000;
            for (int[] next : new int[][]{{gridX - 1, gridZ}, {gridX + 1, gridZ}, {gridX, gridZ - 1}, {gridX, gridZ + 1}}) {
                int key = next[0] * 1000 + next[1];
                if (previous.containsKey(key) || !fits(next[0], next[1]) || !fits((gridX + next[0]) * 0.5, (gridZ + next[1]) * 0.5)) continue;
                previous.put(key, current);
                queue.add(key);
            }
        }
        List<Object> routes = new ArrayList<>();
        boolean routesPass = true;
        for (var element : routeInput.getAsJsonArray("targets")) {
            JsonObject target = element.getAsJsonObject();
            var pos = target.getAsJsonArray("pos");
            int key = (pos.get(0).getAsInt() * 2 + 1) * 1000 + pos.get(2).getAsInt() * 2 + 1;
            boolean reachable = previous.containsKey(key);
            routesPass &= reachable;
            List<double[]> steps = new ArrayList<>();
            if (reachable) for (Integer cursor = key; cursor != null; cursor = previous.get(cursor)) steps.add(new double[]{cursor / 1000 * 0.5, 2, cursor % 1000 * 0.5});
            Collections.reverse(steps);
            routes.add(Map.of("id", target.get("id").getAsString(), "reachable", reachable, "steps", steps));
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("sha256", hash);
        report.put("status", failures.isEmpty() && routesPass ? "PASS" : "FAIL");
        report.put("tag_count", bindings.size());
        report.put("survival_checked_all_records", blocks.size());
        report.put("survival_failures", failures);
        report.put("unsupported", List.of());
        report.put("registry_states_template_roundtrip", "PASS");
        report.put("route_model", "0.6x1.8 body, half-block grid at feetY2; quarter-block midpoint body checks on each edge; full block floors; static samples, not continuous swept simulation; no step/jump/dynamics/ticks");
        report.put("reachable_grid_positions", previous.size());
        report.put("routes", routes);
        report.put("world_placement_ticks_gameplay", "NOT_RUN");
        Files.writeString(output.resolve("native-qa.json"), gson.toJson(report));
        System.out.println("Native static survival failures " + failures.size() + "; routes pass " + routesPass);
        if (!failures.isEmpty() || !routesPass) throw new IllegalStateException("Native QA failed; read native-qa.json");
    }
}
