package eclipse.generator;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraftforge.fml.loading.FMLPaths;

public final class Profiles {
    public static final String DEFAULT = "fteclipsegen:clockwork";
    private static Map<String, Style> loaded;
    private static final Map<String, List<Catalog.Cell>> CELLS = new LinkedHashMap<>();
    public static final class Style {
        public int schema = 1;
        public String id, title, icon;
        public Map<String, String> palette = new LinkedHashMap<>();
        public transient String fingerprint;
    }
    private Profiles() {}

    public static synchronized List<Style> all() {
        if (loaded == null) {
            Map<String, Style> next = new LinkedHashMap<>();
            for (String name : List.of("clockwork", "forest", "desert", "frost")) {
                add(next, Catalog.resource("data/fteclipsegen/styles/" + name + ".json"));
            }
            Path directory = FMLPaths.CONFIGDIR.get().resolve("ft-eclipse-styles");
            if (Files.exists(directory)) try (var paths = Files.list(directory)) {
                for (Path file : paths.filter(path -> path.getFileName().toString().endsWith(".json")).sorted().toList()) {
                    Catalog.require(Files.isRegularFile(file) && Files.size(file) <= 1024 * 1024, "Invalid Eclipse style file");
                    add(next, Files.readAllBytes(file));
                }
            } catch (Exception exception) { throw new IllegalStateException("Cannot load Eclipse styles; refusing partial registry", exception); }
            loaded = Map.copyOf(next);
        }
        return loaded.values().stream().sorted(java.util.Comparator.comparing(style -> style.id)).toList();
    }

    private static void add(Map<String, Style> next, byte[] raw) {
        Style style = new Gson().fromJson(new String(raw, StandardCharsets.UTF_8), Style.class);
        Catalog.require(style != null && style.schema == 1 && style.id != null && ResourceLocation.tryParse(style.id) != null
            && style.id.contains(":") && style.title != null && !style.title.isBlank() && style.palette != null, "Invalid Eclipse style");
        ResourceLocation icon = ResourceLocation.tryParse(style.icon == null ? "" : style.icon);
        Catalog.require(icon != null && BuiltInRegistries.ITEM.containsKey(icon), "Unknown Eclipse icon");
        for (var mapping : style.palette.entrySet()) {
            ResourceLocation source = ResourceLocation.tryParse(mapping.getKey());
            ResourceLocation target = ResourceLocation.tryParse(mapping.getValue());
            Catalog.require(source != null && target != null && BuiltInRegistries.BLOCK.containsKey(source)
                && BuiltInRegistries.BLOCK.containsKey(target), "Unknown Eclipse palette material");
            BlockState before = BuiltInRegistries.BLOCK.get(source).defaultBlockState();
            BlockState after = BuiltInRegistries.BLOCK.get(target).defaultBlockState();
            Catalog.require(!before.isAir() && !after.isAir() && !before.hasBlockEntity() && !after.hasBlockEntity()
                && before.getFluidState().isEmpty() && after.getFluidState().isEmpty(), "Unsafe Eclipse palette material");
        }
        Catalog.require(!DEFAULT.equals(style.id) || style.palette.isEmpty(), "Clockwork geometry must remain unchanged");
        style.fingerprint = Catalog.hash(raw);
        Catalog.require(next.putIfAbsent(style.id, style) == null, "Duplicate Eclipse style: " + style.id);
    }

    public static Style get(String id) {
        all();
        Style style = loaded.get(id);
        Catalog.require(style != null, "Unknown Eclipse style: " + id);
        return style;
    }

    public static String style(Planner.Plan plan) { return plan.style == null ? DEFAULT : plan.style; }
    public static Style get(Planner.Plan plan) { return get(style(plan)); }
    public static BlockState state(String value) {
        try { return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), value, false).blockState(); }
        catch (Exception exception) { throw new IllegalArgumentException("Invalid Eclipse block state: " + value, exception); }
    }

    public static void validate(Planner.Plan plan) {
        Style style = get(plan);
        if (plan.style == null) Catalog.require(plan.styleFingerprint == null, "Partial legacy Eclipse style");
        else Catalog.require(style.fingerprint.equals(plan.styleFingerprint), "Eclipse style fingerprint mismatch");
    }

    public static List<Catalog.Cell> blocks(Planner.Plan plan, String key) {
        validate(plan);
        Style style = get(plan);
        return CELLS.computeIfAbsent(style.id + ":" + style.fingerprint + ":" + key, unused ->
            Catalog.blocks(key).stream().map(cell -> new Catalog.Cell(cell.position(),
                cell.nbt() != null || cell.state().hasBlockEntity() ? cell.state() : map(style, cell.state()), cell.nbt())).toList());
    }

    public static BlockState map(Planner.Plan plan, BlockState source) { validate(plan); return map(get(plan), source); }

    private static BlockState map(Style style, BlockState source) {
        String replacement = style.palette.get(BuiltInRegistries.BLOCK.getKey(source.getBlock()).toString());
        if (replacement == null || source.hasBlockEntity()) return source;
        BlockState target = state(replacement);
        Catalog.require(source.getProperties().size() == target.getProperties().size(), "Palette would drop block properties");
        for (Property<?> property : source.getProperties()) target = copyProperty(source, target, property);
        Catalog.require(!Shapes.joinIsNotEmpty(source.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO),
            target.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO), BooleanOp.NOT_SAME), "Palette changes collision shape");
        Catalog.require(source.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, net.minecraft.core.Direction.UP)
            == target.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, net.minecraft.core.Direction.UP), "Palette changes full top support");
        return target;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState source, BlockState target, Property<T> property) {
        Property<?> destination = target.getBlock().getStateDefinition().getProperty(property.getName());
        Catalog.require(destination != null, "Palette drops property " + property.getName());
        return setProperty(target, destination, property.getName(source.getValue(property)));
    }
    private static <T extends Comparable<T>> BlockState setProperty(BlockState target, Property<T> property, String value) {
        return target.setValue(property, property.getValue(value).orElseThrow(() -> new IllegalStateException("Incompatible palette property")));
    }
}
