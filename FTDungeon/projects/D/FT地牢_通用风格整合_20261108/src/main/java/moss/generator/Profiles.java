package moss.generator;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.FMLPaths;

public final class Profiles {
    public static final String DEFAULT = "ftmossgen:forest";
    private static final Gson GSON = new Gson();
    private static Map<String, Style> loaded;

    public static final class Style {
        public int schema = 1;
        public String id, title, icon;
        public String catalog = "ftmossgen:catalog";
        public String capBlock = "minecraft:mossy_stone_bricks";
        public String barrierBlock = "minecraft:green_stained_glass";
        public String melee = "minecraft:zombie", ranged = "minecraft:skeleton";
        public String bossType = "minecraft:wither_skeleton", bossName = "古林守望者";
        public double bossHealth = 200, bossDamage = 8;
        public long roomCooldownTicks = 3600;
        public int instanceCap = 64;
        public Map<String, String> palette = new LinkedHashMap<>();
        public Map<String, List<Catalog.Weighted>> pools = new LinkedHashMap<>();
        public List<Decoration> decorations = new ArrayList<>();
        public transient String fingerprint;
    }

    public static final class Decoration {
        public String block, mode = "air", role = "all";
        public int[] from, to;
        public int spacing = 1;
    }

    private Profiles() {}

    public static Path directory() {
        return FMLPaths.CONFIGDIR.get().resolve("ft-dungeon-styles").toAbsolutePath().normalize();
    }

    public static synchronized List<Style> all() {
        if (loaded == null) {
            Map<String, Style> next = new LinkedHashMap<>();
            for (String name : List.of("forest", "desert", "frost")) {
                add(next, Catalog.resource("data/ftmossgen/styles/" + name + ".json"), name);
            }
            Path folder = directory();
            if (Files.exists(folder)) {
                try (var paths = Files.list(folder)) {
                    for (Path path : paths.filter(value -> value.getFileName().toString().endsWith(".json")).sorted().toList()) {
                        Catalog.require(Files.size(path) <= 1024 * 1024, "Style JSON exceeds 1 MiB: " + path);
                        add(next, Files.readAllBytes(path), path.toString());
                    }
                } catch (Exception exception) {
                    throw new IllegalStateException("Cannot load style configuration; refusing partial registry", exception);
                }
            }
            loaded = Map.copyOf(next);
        }
        return loaded.values().stream().sorted(java.util.Comparator.comparing(value -> value.id)).toList();
    }

    private static void add(Map<String, Style> result, byte[] raw, String source) {
        Style style = GSON.fromJson(new String(raw, StandardCharsets.UTF_8), Style.class);
        validate(style);
        style.fingerprint = Catalog.hash(raw);
        Catalog.require(result.putIfAbsent(style.id, style) == null, "Duplicate style ID: " + style.id + " in " + source);
    }

    public static Style get(String id) {
        all();
        Style style = loaded.get(id);
        Catalog.require(style != null, "Unknown dungeon style: " + id);
        return style;
    }

    public static void validate(Style style) {
        Catalog.require(style != null && style.schema == 1 && style.id != null && ResourceLocation.tryParse(style.id) != null
            && style.id.contains(":") && style.title != null && !style.title.isBlank() && style.title.length() <= 100, "Invalid style identity/schema");
        ResourceLocation icon = ResourceLocation.tryParse(style.icon == null ? "" : style.icon);
        Catalog.require(icon != null && BuiltInRegistries.ITEM.containsKey(icon), "Unknown style icon: " + style.icon);
        Catalog.require(style.catalog != null && !style.catalog.isBlank() && style.palette != null && style.pools != null
            && style.decorations != null, "Incomplete style data");
        for (String material : List.of(style.capBlock, style.barrierBlock)) {
            BlockState block = state(material);
            Catalog.require(!block.isAir() && block.getFluidState().isEmpty() && !block.hasBlockEntity()
                && block.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO), "Caps/barriers require solid non-BE material");
        }
        for (var entry : style.palette.entrySet()) {
            state(entry.getKey());
            state(entry.getValue());
            Catalog.require(!state(entry.getKey()).isAir() && !state(entry.getValue()).isAir()
                && state(entry.getValue()).getFluidState().isEmpty(), "Palette cannot replace AIR or introduce AIR/fluid");
        }
        for (String type : List.of(style.melee, style.ranged, style.bossType)) {
            ResourceLocation identifier = ResourceLocation.tryParse(type);
            Catalog.require(identifier != null && BuiltInRegistries.ENTITY_TYPE.containsKey(identifier), "Unknown encounter entity: " + type);
        }
        Catalog.require(Double.isFinite(style.bossHealth) && style.bossHealth >= 1 && style.bossHealth <= 10000
            && Double.isFinite(style.bossDamage) && style.bossDamage >= 0 && style.bossDamage <= 1000
            && style.roomCooldownTicks >= 20 && style.roomCooldownTicks <= 72000 && style.instanceCap >= 8 && style.instanceCap <= 256
            && style.bossName != null && !style.bossName.isBlank(), "Invalid encounter settings");
        for (Decoration decoration : style.decorations) {
            Catalog.require(decoration != null && "air".equals(decoration.mode) && decoration.role != null
                && decoration.from != null && decoration.to != null && decoration.from.length == 3 && decoration.to.length == 3
                && decoration.spacing >= 1 && decoration.spacing <= 64, "Invalid geometry recipe");
            BlockState block = state(decoration.block);
            Catalog.require(!block.isAir() && block.getFluidState().isEmpty() && !block.hasBlockEntity(), "Unsafe geometry recipe material");
            for (int axis = 0; axis < 3; axis++) Catalog.require(decoration.from[axis] >= 0
                && decoration.to[axis] >= decoration.from[axis] && decoration.to[axis] < 64, "Recipe coordinate outside supported template bounds");
        }
    }

    public static BlockState state(String value) {
        try {
            return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), value, false).blockState();
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid style block state: " + value, exception);
        }
    }

    public static byte[] bytes(String location, String suffix) {
        if (location.startsWith("file:")) {
            try {
                Path relative = Path.of(location.substring(5));
                Catalog.require(!relative.isAbsolute(), "Style file path must be relative");
                Path file = directory().resolve(relative).normalize();
                Catalog.require(file.startsWith(directory()) && Files.isRegularFile(file)
                    && file.toRealPath().startsWith(directory().toRealPath()), "Style file escapes configuration directory");
                Catalog.require(Files.size(file) <= 16 * 1024 * 1024, "Style asset exceeds size limit");
                return Files.readAllBytes(file);
            } catch (Exception exception) { throw new IllegalStateException("Cannot read style asset: " + location, exception); }
        }
        ResourceLocation id = ResourceLocation.tryParse(location);
        Catalog.require(id != null, "Invalid style asset ID: " + location);
        return Catalog.resource("data/" + id.getNamespace() + "/" + id.getPath() + suffix);
    }
}
