package cn.ft.biomedungeon.runtime;

import cn.ft.biomedungeon.core.DungeonApi;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

final class TemplateCatalog {
    private static final Set<String> STATIC_BLOCKS = Set.of("air", "cave_air", "void_air", "amethyst_block",
            "amethyst_cluster", "calcite", "chain", "deepslate_brick_stairs", "deepslate_bricks", "deepslate_tiles",
            "lantern", "polished_deepslate", "smooth_quartz_stairs", "cobbled_deepslate", "stone", "stone_bricks",
            "smooth_stone", "glass", "quartz_block", "smooth_quartz", "deepslate", "cobblestone");
    record Cell(BlockPos local, BlockState state) {}
    record Loaded(DungeonApi.Template metadata, List<Cell> cells) {}
    final Map<String, Loaded> templates = new LinkedHashMap<>();
    final String manifestHash;
    @FunctionalInterface
    interface ResourceSource { byte[] read(String path, int limit) throws IOException; }

    TemplateCatalog(ServerLevel level) throws IOException {
        this(level.holderLookup(net.minecraft.core.registries.Registries.BLOCK));
    }

    TemplateCatalog(HolderGetter<Block> registry) throws IOException {
        this(registry, TemplateCatalog::resource);
    }

    TemplateCatalog(HolderGetter<Block> registry, ResourceSource source) throws IOException {
        byte[] bytes = source.read("/data/biomedungeon/manifest.json", 2_000_000);
        manifestHash = AtomicFiles.sha256(bytes);
        JsonObject root = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        require(integer(root, "schemaVersion") == 1, "Unsupported manifest schema");
        JsonArray entries = root.getAsJsonArray("templates");
        require(entries != null && entries.size() > 0 && entries.size() <= 256, "Empty/oversized manifest");
        for (JsonElement entry : entries) {
            JsonObject item = entry.getAsJsonObject();
            require(item.has("accepted") && item.get("accepted").isJsonPrimitive()
                    && item.get("accepted").getAsJsonPrimitive().isBoolean()
                    && item.get("accepted").getAsBoolean(), "Template is not accepted");
            String id = text(item, "id");
            require(id.matches("[a-z0-9_/-]{1,128}") && !id.contains(".."), "Illegal template ID");
            String resourceId = text(item, "resourceId");
            require(resourceId.equals("biomedungeon:" + id), "Resource not whitelisted");
            String hash = text(item, "sha256");
            require(hash.matches("[a-f0-9]{64}"), "Invalid SHA256");
            String biome = text(item, "biome");
            require(biome.length() <= 128, "Invalid biome");
            String role = text(item, "role");
            require(Set.of("入口", "普通", "终局", "entrance", "normal", "terminal").contains(role), "Invalid role");
            require(item.has("independentAcceptance") && item.get("independentAcceptance").isJsonObject(), "Independent acceptance evidence missing");
            JsonObject evidence = item.getAsJsonObject("independentAcceptance");
            require(text(evidence, "sourceSha256").equals(hash) && evidence.has("unchangedBytes")
                    && evidence.get("unchangedBytes").getAsBoolean(), "Acceptance hash does not cover original NBT bytes");
            require(text(evidence.getAsJsonObject("independentFileAudit"), "sha256").matches("[a-f0-9]{64}")
                    && evidence.has("acceptanceRecords") && !evidence.getAsJsonArray("acceptanceRecords").isEmpty(), "Missing independent acceptance hashes");
            DungeonApi.Vec3i size = vector(item.getAsJsonObject("size"));
            require(size.x() > 0 && size.y() > 0 && size.z() > 0
                    && size.x() <= 128 && size.y() <= 96 && size.z() <= 128, "Template size limits");
            List<DungeonApi.Port> ports = new ArrayList<>();
            Set<String> portIds = new HashSet<>();
            JsonArray portArray = item.getAsJsonArray("ports");
            require(portArray != null && portArray.size() > 0 && portArray.size() <= 16, "Invalid ports");
            for (JsonElement element : portArray) {
                JsonObject port = element.getAsJsonObject();
                String portId = text(port, "id");
                require(portIds.add(portId), "Duplicate port");
                DungeonApi.Vec3i feet = vector(port.getAsJsonObject("feet"));
                String facing = text(port, "facing");
                String medium = text(port, "medium");
                int width = integer(port, "width");
                int height = integer(port, "height");
                require(Set.of("north", "east", "south", "west").contains(facing), "Invalid facing");
                require(Set.of("dry", "wet").contains(medium), "Invalid medium");
                require(medium.equals("dry"), "Wet ports not supported by isolated-air runtime");
                require(width >= 1 && width <= 7 && height >= 2 && height <= 8, "Invalid aperture");
                require(feet.x() >= 0 && feet.y() > 0 && feet.z() >= 0
                        && feet.x() < size.x() && feet.y() + height <= size.y() && feet.z() < size.z(), "Port outside template");
                ports.add(new DungeonApi.Port(portId, feet, facing, width, height, medium));
            }
            DungeonApi.Template metadata = new DungeonApi.Template(id, biome, resourceId, hash, size, List.copyOf(ports), role);
            byte[] raw = source.read("/data/biomedungeon/structures/" + id + ".nbt", 16_000_000);
            require(AtomicFiles.sha256(raw).equals(hash), "NBT SHA256 mismatch: " + id);
            List<Cell> cells = decode(registry, raw, size);
            require(templates.putIfAbsent(id, new Loaded(metadata, cells)) == null, "Duplicate template ID");
        }
    }

    private static List<Cell> decode(HolderGetter<Block> registry, byte[] raw, DungeonApi.Vec3i size) throws IOException {
        CompoundTag nbt = readNbt(raw);
        require(nbt.contains("DataVersion", 3) && nbt.getInt("DataVersion") == 3465, "Only native 1.20.1 structure data supported");
        ListTag dimensions = nbt.getList("size", 3);
        require(dimensions.size() == 3 && dimensions.getInt(0) == size.x() && dimensions.getInt(1) == size.y()
                && dimensions.getInt(2) == size.z(), "NBT size mismatch");
        require(!nbt.contains("palettes"), "Multi-palette payload unsupported");
        require(nbt.contains("entities", 9), "Missing/malformed entity list");
        ListTag entities = (ListTag) nbt.get("entities");
        require(entities.isEmpty() || entities.getElementType() == 10, "Malformed entity payload");
        require(nbt.getList("entities", 10).isEmpty(), "Entity payload unsupported; never discarded silently");
        ListTag palette = nbt.getList("palette", 10);
        require(!palette.isEmpty() && palette.size() <= 4096, "Invalid palette");
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag tag = palette.getCompound(index);
            ResourceLocation name = ResourceLocation.tryParse(tag.getString("Name"));
            require(name != null && name.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(name), "Unknown/modded block rejected");
            require(STATIC_BLOCKS.contains(name.getPath()), "Block outside static MVP allowlist: " + name);
            BlockState state = NbtUtils.readBlockState(registry, tag);
            if (tag.contains("Properties", 10)) {
                CompoundTag properties = tag.getCompound("Properties");
                for (String propertyName : properties.getAllKeys()) {
                    Property<?> property = state.getBlock().getStateDefinition().getProperty(propertyName);
                    require(property != null && property.getValue(properties.getString(propertyName)).isPresent(), "Unknown block property");
                }
            }
            require(state.getFluidState().isEmpty() && !state.hasBlockEntity()
                    && (!state.isRandomlyTicking() || state.is(Blocks.AMETHYST_CLUSTER))
                    && !(state.getBlock() instanceof FallingBlock)
                    && !state.is(Blocks.FIRE) && !state.is(Blocks.SOUL_FIRE) && !state.is(Blocks.TNT)
                    && !state.is(Blocks.NETHER_PORTAL) && !state.is(Blocks.END_PORTAL)
                    && !state.is(Blocks.STRUCTURE_VOID) && !state.is(Blocks.BARRIER), "Unsafe/dynamic payload rejected: " + name);
            states.add(state);
        }
        List<Cell> result = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        ListTag blocks = nbt.getList("blocks", 10);
        require(blocks.size() <= 500_000, "Too many cells");
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag block = blocks.getCompound(index);
            require(!block.contains("nbt"), "Block entity payload unsupported; never discarded silently");
            ListTag coordinates = block.getList("pos", 3);
            require(coordinates.size() == 3, "Malformed block position");
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            int state = block.getInt("state");
            require(position.getX() >= 0 && position.getX() < size.x() && position.getY() >= 0
                    && position.getY() < size.y() && position.getZ() >= 0 && position.getZ() < size.z(), "Block outside declared size");
            require(block.contains("state", 3) && state >= 0 && state < states.size() && seen.add(position), "Invalid/duplicate state cell");
            result.add(new Cell(position, states.get(state)));
        }
        require(!result.isEmpty(), "Empty template");
        require(result.size() == (long) size.x() * size.y() * size.z(), "Sparse templates not supported; explicit original air is required");
        return List.copyOf(result);
    }

    static CompoundTag readNbt(byte[] raw) throws IOException {
        try (DataInputStream input = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(raw)))) {
            return NbtIo.read(input, new net.minecraft.nbt.NbtAccounter(64_000_000L));
        }
    }

    static byte[] resource(String path, int limit) throws IOException {
        try (InputStream input = TemplateCatalog.class.getResourceAsStream(path)) {
            if (input == null) { throw new IOException("Missing bundled resource " + path); }
            byte[] bytes = input.readNBytes(limit + 1);
            require(bytes.length <= limit, "Resource exceeds limit");
            return bytes;
        }
    }

    private static String text(JsonObject object, String key) {
        require(object != null && object.has(key) && object.get(key).isJsonPrimitive()
                && object.get(key).getAsJsonPrimitive().isString(), "Missing string: " + key);
        String result = object.get(key).getAsString();
        require(!result.isBlank(), "Blank field " + key);
        return result;
    }

    private static int integer(JsonObject object, String key) {
        require(object != null && object.has(key) && object.get(key).isJsonPrimitive()
                && object.get(key).getAsJsonPrimitive().isNumber(), "Missing integer: " + key);
        return object.get(key).getAsBigDecimal().intValueExact();
    }

    private static DungeonApi.Vec3i vector(JsonObject object) {
        return new DungeonApi.Vec3i(integer(object, "x"), integer(object, "y"), integer(object, "z"));
    }

    static void require(boolean condition, String reason) {
        if (!condition) { throw new IllegalArgumentException(reason); }
    }
}
