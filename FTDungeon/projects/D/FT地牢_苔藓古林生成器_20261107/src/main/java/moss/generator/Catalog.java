package moss.generator;

import com.google.gson.Gson;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public final class Catalog {
    public static final Gson GSON = new Gson();
    public final Map<String, Template> templates = new LinkedHashMap<>();
    public final Map<String, List<Weighted>> pools;
    public final String fingerprint;
    private static Catalog instance;
    private final Map<String, List<Cell>> decoded = new HashMap<>();

    public static final class Template {
        public String key, template, child, sha256, source_sha256;
        public int[] size;
        public int minimum_y;
        public int[] portal, portal_trigger;
        public int portal_facing;
        public List<Port> ports;
        public List<Socket> sockets;
    }
    public static final class Port { public List<int[]> cells = new ArrayList<>(); }
    public static final class Socket {
        public int[] position, support, spawn_position;
        public String kind;
    }
    public static final class Weighted { public String template; public int weight; }
    private static final class Manifest {
        int schema;
        List<Template> variants;
        Map<String, List<Weighted>> pools;
    }
    public record Cell(BlockPos position, BlockState state, CompoundTag nbt) {}

    private Catalog() {
        byte[] raw = resource("data/ftmossgen/catalog.json");
        Manifest manifest = GSON.fromJson(new String(raw, StandardCharsets.UTF_8), Manifest.class);
        require(manifest.schema == 1 && manifest.variants.size() == 27, "Catalog schema/count");
        fingerprint = hash(raw);
        for (Template template : manifest.variants) {
            require(template.key.matches("[a-z0-9_/]+") && template.size.length == 3, "Template ID/size");
            require(templates.put(template.key, template) == null, "Duplicate template");
            require(template.size[0] > 0 && template.size[0] <= 64 && template.size[1] > 0 && template.size[1] <= 64 && template.size[2] > 0 && template.size[2] <= 64, "Template bounds");
            if (template.portal != null) {
                require(template.portal.length == 3 && template.portal_trigger != null && template.portal_trigger.length == 3
                    && template.portal_facing >= 0 && template.portal_facing < 4, "Invalid forest portal metadata");
                require(template.key.startsWith("turn/") || template.key.startsWith("straight/room_")
                    || template.key.startsWith("terminal/") || template.key.equals("promenade"), "Portal on non-room template");
            }
        }
        pools = manifest.pools;
        for (List<Weighted> pool : pools.values()) {
            require(!pool.isEmpty(), "Empty pool");
            for (Weighted item : pool) require(item.weight > 0 && item.weight <= 16 && (templates.containsKey(item.template) || item.template.equals("straight/room")), "Invalid pool member");
        }
    }

    public static synchronized Catalog get() {
        if (instance == null) instance = new Catalog();
        return instance;
    }

    public static byte[] resource(String name) {
        try (InputStream stream = Catalog.class.getClassLoader().getResourceAsStream(name)) {
            require(stream != null, "Missing packaged resource: " + name);
            byte[] bytes = stream.readNBytes(16 * 1024 * 1024 + 1);
            require(bytes.length <= 16 * 1024 * 1024, "Oversized resource");
            return bytes;
        } catch (Exception exception) { throw new IllegalStateException("Cannot read " + name, exception); }
    }

    public static String hash(byte[] bytes) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (Exception exception) { throw new IllegalStateException(exception); }
    }

    public static List<Cell> blocks(String key) { return get().decoded.computeIfAbsent(key, get()::decode); }

    private List<Cell> decode(String key) {
        try {
            Template template = templates.get(key);
            require(template != null, "Unknown template: " + key);
            byte[] bytes = resource("data/ftmossgen/structures/" + key + ".nbt");
            require(hash(bytes).equals(template.sha256), "Template hash mismatch: " + key);
            CompoundTag data = NbtIo.readCompressed(new ByteArrayInputStream(bytes));
            require(data.getInt("DataVersion") == 3465, "NBT version");
            require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unsupported template entities");
            require(!data.contains("palettes"), "Multiple palettes unsupported");
            var size = data.getList("size", Tag.TAG_INT);
            for (int axis = 0; axis < 3; axis++) require(size.getInt(axis) == template.size[axis], "NBT size");
            List<BlockState> palette = new ArrayList<>();
            for (Tag entry : data.getList("palette", Tag.TAG_COMPOUND)) {
                CompoundTag material = (CompoundTag)entry;
                ResourceLocation id = new ResourceLocation(material.getString("Name"));
                require(id.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(id), "Unknown block: " + id);
                var block = BuiltInRegistries.BLOCK.get(id);
                var properties = material.getCompound("Properties");
                for (String propertyName : properties.getAllKeys()) {
                    var property = block.getStateDefinition().getProperty(propertyName);
                    require(property != null && property.getValue(properties.getString(propertyName)).isPresent(), "Invalid property: " + id + "/" + propertyName);
                }
                palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), material));
            }
            List<Cell> cells = new ArrayList<>();
            var seen = new java.util.HashSet<BlockPos>();
            for (Tag entry : data.getList("blocks", Tag.TAG_COMPOUND)) {
                CompoundTag cell = (CompoundTag)entry;
                var position = cell.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
                require(local.getX() >= 0 && local.getX() < template.size[0] && local.getY() >= 0 && local.getY() < template.size[1] && local.getZ() >= 0 && local.getZ() < template.size[2], "Out-of-bounds block");
                require(seen.add(local), "Duplicate NBT cell");
                int index = cell.getInt("state");
                require(index >= 0 && index < palette.size(), "Palette index");
                if (local.getY() >= template.minimum_y) cells.add(new Cell(local, palette.get(index), cell.contains("nbt") ? cell.getCompound("nbt").copy() : null));
            }
            require(seen.size() == template.size[0] * template.size[1] * template.size[2], "Sparse NBT refused");
            cells.sort(Comparator.comparingInt((Cell cell) -> cell.position().getY()).thenComparingInt(cell -> cell.position().getZ()).thenComparingInt(cell -> cell.position().getX()));
            return List.copyOf(cells);
        } catch (Exception exception) { throw new IllegalStateException("Invalid packaged template " + key, exception); }
    }

    public static BlockPos pos(int[] values) { return new BlockPos(values[0], values[1], values[2]); }
    public static int[] vec(BlockPos position) { return new int[] {position.getX(), position.getY(), position.getZ()}; }
    public static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
