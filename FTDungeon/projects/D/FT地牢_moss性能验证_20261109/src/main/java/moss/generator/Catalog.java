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
    private static final Map<String, Catalog> INSTANCES = new HashMap<>();
    public final Profiles.Style style;
    private final Map<String, List<Cell>> decoded = new HashMap<>();
    private final Map<BlockState, PaletteState> paletteStates = new HashMap<>();
    private record PaletteState(BlockState state, boolean replaced) {}

    public static final class Template {
        public String key, template, child, sha256, source_sha256, resource;
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

    private Catalog(String styleId) {
        style = Profiles.get(styleId);
        byte[] raw = Profiles.bytes(style.catalog, ".json");
        Manifest manifest = GSON.fromJson(new String(raw, StandardCharsets.UTF_8), Manifest.class);
        require(manifest.schema == 1 && manifest.variants != null && !manifest.variants.isEmpty(), "Catalog schema/count");
        fingerprint = hash((hash(raw) + ":" + style.fingerprint).getBytes(StandardCharsets.UTF_8));
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
        pools = new LinkedHashMap<>(manifest.pools);
        pools.putAll(style.pools);
        for (List<Weighted> pool : pools.values()) {
            require(!pool.isEmpty(), "Empty pool");
            for (Weighted item : pool) require(item.weight > 0 && item.weight <= 16 && (templates.containsKey(item.template) || item.template.equals("straight/room")), "Invalid pool member");
        }
    }

    public static synchronized Catalog get() {
        return get(Profiles.DEFAULT);
    }

    public static synchronized Catalog get(String style) {
        Catalog catalog = INSTANCES.get(style);
        if (catalog == null || catalog.style != Profiles.get(style)) {
            catalog = new Catalog(style);
            INSTANCES.put(style, catalog);
        }
        return catalog;
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

    public static List<Cell> blocks(String key) { return blocks(Profiles.DEFAULT, key); }
    public static List<Cell> blocks(String style, String key) {
        Catalog catalog = get(style);
        return catalog.decoded.computeIfAbsent(key, catalog::decode);
    }

    private List<Cell> decode(String key) {
        try {
            Template template = templates.get(key);
            require(template != null, "Unknown template: " + key);
            byte[] bytes = template.resource == null ? resource("data/ftmossgen/structures/" + key + ".nbt") : Profiles.bytes(template.resource, "");
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
            List<Cell> mapped = new ArrayList<>();
            for (Cell cell : cells) {
                PaletteState mapping = paletteStates.computeIfAbsent(cell.state(), this::mapState);
                BlockState state = mapping.state();
                if (mapping.replaced()) {
                    require(!state.hasBlockEntity() || cell.nbt() != null, "Palette cannot introduce a block entity");
                    if (cell.nbt() != null && state.getBlock() != cell.state().getBlock()) throw new IllegalStateException("Palette cannot change block entity type");
                }
                mapped.add(new Cell(cell.position(), state, cell.nbt()));
            }
            return List.copyOf(StyleGeometry.process(style, template, mapped));
        } catch (Exception exception) { throw new IllegalStateException("Invalid packaged template " + key, exception); }
    }

    private PaletteState mapState(BlockState source) {
        String replacement = style.palette.get(net.minecraft.commands.arguments.blocks.BlockStateParser.serialize(source));
        if (replacement == null) replacement = style.palette.get(BuiltInRegistries.BLOCK.getKey(source.getBlock()).toString());
        if (replacement == null) return new PaletteState(source, false);
        BlockState state = Profiles.state(replacement);
        for (var property : source.getProperties()) state = carryProperty(state, source, property, replacement);
        return new PaletteState(state, true);
    }

    private static <T extends Comparable<T>> BlockState carryProperty(BlockState target, BlockState source,
            net.minecraft.world.level.block.state.properties.Property<T> property, String replacement) {
        if (replacement.contains(property.getName() + "=")) return target;
        var next = target.getBlock().getStateDefinition().getProperty(property.getName());
        if (next == null) return target;
        return copyProperty(target, next, property.getName(source.getValue(property)));
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState target,
            net.minecraft.world.level.block.state.properties.Property<T> property, String value) {
        return target.setValue(property, property.getValue(value).orElseThrow(() -> new IllegalStateException("Palette property incompatible: " + property.getName())));
    }

    public static BlockPos pos(int[] values) { return new BlockPos(values[0], values[1], values[2]); }
    public static int[] vec(BlockPos position) { return new int[] {position.getX(), position.getY(), position.getZ()}; }
    public static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
