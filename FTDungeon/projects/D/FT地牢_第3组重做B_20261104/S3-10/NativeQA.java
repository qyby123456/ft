import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;

public final class NativeQA {
    private static final Map<BlockPos, BlockState> blocks = new HashMap<>();
    private static final Map<String, Boolean> stableCache = new HashMap<>();
    private static LevelReader view;
    private record Node(int east, int south, int elevation) {}

    private static boolean clear(double gridEast, double gridSouth, double feet) {
        double centerEast = gridEast * 0.5;
        double centerSouth = gridSouth * 0.5;
        AABB body = new AABB(centerEast - 0.3, feet + 0.00001, centerSouth - 0.3, centerEast + 0.3, feet + 1.8, centerSouth + 0.3);
        if (body.minX < 0 || body.maxX > 89 || body.minZ < 0 || body.maxZ > 89) return false;
        for (int east = (int)Math.floor(body.minX); east <= Math.floor(body.maxX); east++) for (int south = (int)Math.floor(body.minZ); south <= Math.floor(body.maxZ); south++) for (int height = (int)Math.floor(feet); height <= Math.floor(feet + 1.8); height++) {
            BlockPos position = new BlockPos(east, height, south);
            for (AABB shape : blocks.getOrDefault(position, Blocks.AIR.defaultBlockState()).getCollisionShape(view, position).toAabbs()) if (shape.move(east, height, south).intersects(body)) return false;
        }
        return true;
    }

    private static boolean stable(Node node) {
        String key = node.toString();
        if (stableCache.containsKey(key)) return stableCache.get(key);
        double centerEast = node.east * 0.5;
        double centerSouth = node.south * 0.5;
        double feet = node.elevation * 0.5;
        boolean touches = false;
        boolean centerSupported = false;
        if (!clear(node.east, node.south, feet)) { stableCache.put(key, false); return false; }
        for (int east = (int)Math.floor(centerEast - 0.3); east <= Math.floor(centerEast + 0.3); east++) for (int south = (int)Math.floor(centerSouth - 0.3); south <= Math.floor(centerSouth + 0.3); south++) for (int height = Math.max(0, (int)Math.floor(feet) - 2); height <= Math.floor(feet); height++) {
            BlockPos position = new BlockPos(east, height, south);
            for (AABB local : blocks.getOrDefault(position, Blocks.AIR.defaultBlockState()).getCollisionShape(view, position).toAabbs()) {
                AABB shape = local.move(east, height, south);
                if (Math.abs(shape.maxY - feet) < 0.0001 && shape.maxX > centerEast - 0.3 && shape.minX < centerEast + 0.3 && shape.maxZ > centerSouth - 0.3 && shape.minZ < centerSouth + 0.3) touches = true;
                if (shape.minX <= centerEast && shape.maxX >= centerEast && shape.minZ <= centerSouth && shape.maxZ >= centerSouth && shape.maxY <= feet + 0.0001 && shape.maxY >= feet - 0.5001) centerSupported = true;
            }
        }
        boolean result = touches && centerSupported;
        stableCache.put(key, result);
        return result;
    }

    private static Map<Node, Node> traverse() {
        stableCache.clear();
        Map<Node, Node> previous = new HashMap<>();
        ArrayList<Node> queue = new ArrayList<>();
        Node start = new Node(87, 177, 4);
        if (stable(start)) { previous.put(start, null); queue.add(start); }
        for (int index = 0; index < queue.size(); index++) {
            Node current = queue.get(index);
            for (int[] direction : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) for (int change = -1; change <= 1; change++) {
                Node next = new Node(current.east + direction[0], current.south + direction[1], current.elevation + change);
                if (next.elevation < 4 || next.elevation > 16 || previous.containsKey(next) || !stable(next)) continue;
                double upperFeet = Math.max(current.elevation, next.elevation) * 0.5;
                if (!clear(current.east, current.south, upperFeet) || !clear(next.east, next.south, upperFeet) || !clear((current.east + next.east) * 0.5, (current.south + next.south) * 0.5, upperFeet)) continue;
                previous.put(next, current);
                queue.add(next);
            }
        }
        return previous;
    }

    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path source = Path.of(arguments[0]);
        Path output = Path.of(arguments[1]);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        byte[] bytes = Files.readAllBytes(source.resolve("room.nbt"));
        String hash = HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
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
            ListTag position = entry.getList("pos", Tag.TAG_INT);
            blocks.put(new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2)), states.get(entry.getInt("state")));
        }
        view = (LevelReader)Proxy.newProxyInstance(LevelReader.class.getClassLoader(), new Class<?>[]{LevelReader.class}, (proxy, method, params) -> {
            String name = method.getName();
            if (name.equals("getBlockState")) return blocks.getOrDefault((BlockPos)params[0], Blocks.AIR.defaultBlockState());
            if (name.equals("getFluidState")) return blocks.getOrDefault((BlockPos)params[0], Blocks.AIR.defaultBlockState()).getFluidState();
            if (name.equals("getMinBuildHeight")) return 0;
            if (name.equals("getHeight") || name.equals("getMaxBuildHeight")) return 25;
            if (name.equals("getRawBrightness") || name.equals("getMaxLocalRawBrightness")) return 15;
            if (name.startsWith("hasChunk")) return true;
            if (name.equals("getEntitiesOfClass")) return List.of();
            if (name.equals("toString")) return "S3-10 immutable native tagged view";
            throw new UnsupportedOperationException(name);
        });
        List<Object> failures = new ArrayList<>();
        List<Object> loadedEntities = new ArrayList<>();
        int chestCount = 0;
        boolean containerPass = true;
        for (int index = 0; index < records.size(); index++) {
            CompoundTag entry = records.getCompound(index);
            if (!entry.contains("nbt", Tag.TAG_COMPOUND)) continue;
            ListTag coordinates = entry.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            BlockEntity entity = BlockEntity.loadStatic(position, blocks.get(position), entry.getCompound("nbt"));
            if (entity == null) throw new IllegalStateException("Invalid block entity " + position);
            loadedEntities.add(Map.of("pos", List.of(position.getX(), position.getY(), position.getZ()), "type", entity.getType().toString(), "roundtrip", entity.saveWithFullMetadata().toString()));
            if (entity instanceof ChestBlockEntity chest) {
                chestCount++;
                boolean empty = true;
                for (int slot = 0; slot < chest.getContainerSize(); slot++) empty &= chest.getItem(slot).isEmpty();
                var obstructionCheck = ChestBlock.class.getDeclaredMethod("isBlockedChestByBlock", net.minecraft.world.level.BlockGetter.class, BlockPos.class);
                obstructionCheck.setAccessible(true);
                boolean blocked = (Boolean)obstructionCheck.invoke(null, view, position);
                containerPass &= empty && chest.getContainerSize() == 27 && !blocked;
            }
        }
        for (var entry : blocks.entrySet()) if (!entry.getValue().canSurvive(view, entry.getKey())) failures.add(Map.of("pos", List.of(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()), "block", entry.getValue().toString()));
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        if (template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() != 198025) throw new IllegalStateException("Native roundtrip count");
        List<Object> fragmentChecks = new ArrayList<>();
        ArrayList<Path> fragmentFiles = new ArrayList<>();
        try (var stream = Files.list(source.resolve("fragments"))) { stream.filter(file -> file.toString().endsWith(".nbt")).sorted().forEach(fragmentFiles::add); }
        fragmentFiles.add(source.resolve("reassembled.nbt"));
        for (Path file : fragmentFiles) {
            CompoundTag fragment = NbtIo.readCompressed(file.toFile());
            StructureTemplate fragmentTemplate = new StructureTemplate();
            fragmentTemplate.load(BuiltInRegistries.BLOCK.asLookup(), fragment);
            int count = fragment.getList("blocks", Tag.TAG_COMPOUND).size();
            if (fragmentTemplate.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() != count) throw new IllegalStateException("Fragment template failed");
            fragmentChecks.add(Map.of("file", file.getFileName().toString(), "records", count, "sha256", HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file))), "template", "PASS"));
        }
        if (fragmentChecks.size() != 5) throw new IllegalStateException("Missing fragment/reassembled files");
        JsonObject routeInput = gson.fromJson(Files.readString(source.resolve("route-targets.json")), JsonObject.class);
        if (!routeInput.get("sha256").getAsString().equals(hash)) throw new IllegalStateException("Route hash mismatch");
        Map<Node, Node> previous = traverse();
        List<Object> routes = new ArrayList<>();
        boolean routesPass = true;
        for (var element : routeInput.getAsJsonArray("targets")) {
            JsonObject target = element.getAsJsonObject();
            var position = target.getAsJsonArray("pos");
            Node destination = new Node(position.get(0).getAsInt() * 2 + 1, position.get(2).getAsInt() * 2 + 1, (int)Math.round(position.get(1).getAsDouble() * 2));
            boolean reachable = previous.containsKey(destination);
            routesPass &= reachable;
            List<double[]> steps = new ArrayList<>();
            if (reachable) for (Node cursor = destination; cursor != null; cursor = previous.get(cursor)) steps.add(new double[]{cursor.east * 0.5, cursor.elevation * 0.5, cursor.south * 0.5});
            Collections.reverse(steps);
            routes.add(Map.of("id", target.get("id").getAsString(), "reachable", reachable, "steps", steps));
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("sha256", hash);
        report.put("status", failures.isEmpty() && routesPass && containerPass && chestCount == 1 && loadedEntities.size() == 1 ? "PASS" : "FAIL");
        report.put("block_entities_native_load_save", loadedEntities);
        report.put("fragment_native_template_checks", fragmentChecks);
        report.put("container_validation", Map.of("single_chest", chestCount, "empty_27_slots_and_native_unblocked", containerPass, "scope", "Actual BlockEntity.loadStatic/save and native ChestBlock.isBlockedChestByBlock; cat/entity test NOT_RUN, template has no entities; real interaction NOT_RUN"));
        report.put("tag_count", bindings.size());
        report.put("survival_checked_all_records", blocks.size());
        report.put("survival_failures", failures);
        report.put("unsupported", List.of());
        report.put("registry_states_template_roundtrip", "PASS");
        report.put("route_model", "0.6x1.8 body, half-block X/Z and elevation grid; center support within0.5 and footprint contact; native collision shapes; <=0.5 step transitions with raised-body endpoints/midpoint. Offline static accessibility, not continuous physics/jump/ice sliding/ticks.");
        report.put("reachable_grid_positions", previous.size());
        report.put("routes", routes);
        report.put("world_placement_ticks_gameplay", "NOT_RUN");
        Files.writeString(output.resolve("native-qa.json"), gson.toJson(report));
        if (!failures.isEmpty() || !routesPass || !containerPass || chestCount != 1 || loadedEntities.size() != 1) throw new IllegalStateException("Native QA failed; read native-qa.json");
    }
}
