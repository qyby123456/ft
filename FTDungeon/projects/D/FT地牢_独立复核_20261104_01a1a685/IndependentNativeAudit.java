import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class IndependentNativeAudit {
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        boolean frozen = arguments.length > 1;
        var input = frozen ? JsonParser.parseString(Files.readString(root.resolve("route-input.json"))).getAsJsonArray() : JsonParser.parseString(Files.readString(root.resolve("file-audit.json"))).getAsJsonObject().getAsJsonArray("groups");
        if (frozen) {
            JsonObject tags = JsonParser.parseString(Files.readString(root.resolve("vanilla-block-tags.json"))).getAsJsonObject();
            Map<TagKey<Block>, List<Holder<Block>>> bindings = new HashMap<>();
            for (var tag : tags.entrySet()) {
                List<Holder<Block>> values = new ArrayList<>();
                for (var value : tag.getValue().getAsJsonArray()) {
                    ResourceLocation identifier = new ResourceLocation(value.getAsString());
                    if (!BuiltInRegistries.BLOCK.containsKey(identifier)) throw new IllegalStateException("Unknown block in vanilla tag " + identifier);
                    values.add(BuiltInRegistries.BLOCK.getHolderOrThrow(net.minecraft.resources.ResourceKey.create(Registries.BLOCK, identifier)));
                }
                bindings.put(TagKey.create(Registries.BLOCK, new ResourceLocation(tag.getKey())), values);
            }
            BuiltInRegistries.BLOCK.bindTags(bindings);
        }
        List<Object> reports = new ArrayList<>();
        for (var entry : input) {
            JsonObject group = entry.getAsJsonObject();
            CompoundTag data = NbtIo.readCompressed(Path.of(group.get("candidate").getAsString()).toFile());
            var palette = data.getList("palette", Tag.TAG_COMPOUND);
            List<BlockState> states = new ArrayList<>();
            List<String> failures = new ArrayList<>();
            for (int index = 0; index < palette.size(); index++) {
                CompoundTag value = palette.getCompound(index);
                ResourceLocation identifier = new ResourceLocation(value.getString("Name"));
                if (!BuiltInRegistries.BLOCK.containsKey(identifier)) {
                    failures.add("Unknown block " + identifier);
                }
                BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), value);
                CompoundTag properties = value.getCompound("Properties");
                for (String name : properties.getAllKeys()) {
                    var property = state.getBlock().getStateDefinition().getProperty(name);
                    if (property == null || property.getValue(properties.getString(name)).isEmpty()) {
                        failures.add("Invalid property " + identifier + " " + name + "=" + properties.getString(name));
                    }
                }
                states.add(state);
            }
            Map<BlockPos, BlockState> blocks = new HashMap<>();
            var entries = data.getList("blocks", Tag.TAG_COMPOUND);
            for (int index = 0; index < entries.size(); index++) {
                CompoundTag value = entries.getCompound(index);
                var position = value.getList("pos", Tag.TAG_INT);
                blocks.put(new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2)), states.get(value.getInt("state")));
            }
            int height = data.getList("size", Tag.TAG_INT).getInt(1);
            LevelReader view = (LevelReader) Proxy.newProxyInstance(LevelReader.class.getClassLoader(), new Class<?>[]{LevelReader.class}, (proxy, method, parameters) -> {
                String name = method.getName();
                if (name.equals("getBlockState")) return blocks.getOrDefault((BlockPos) parameters[0], Blocks.AIR.defaultBlockState());
                if (name.equals("getFluidState")) return blocks.getOrDefault((BlockPos) parameters[0], Blocks.AIR.defaultBlockState()).getFluidState();
                if (name.equals("getMinBuildHeight")) return 0;
                if (name.equals("getHeight") || name.equals("getMaxBuildHeight")) return height;
                if (name.equals("getRawBrightness") || name.equals("getMaxLocalRawBrightness")) return 15;
                if (name.equals("hasChunk") || name.equals("hasChunkAt") || name.equals("hasChunksAt")) return true;
                if (name.equals("toString")) return "Independent immutable block-state view";
                throw new UnsupportedOperationException(name);
            });
            List<Object> survivalFailures = new ArrayList<>();
            List<Object> unsupported = new ArrayList<>();
            int survivalChecked = 0;
            for (var block : blocks.entrySet()) {
                String identifier = BuiltInRegistries.BLOCK.getKey(block.getValue().getBlock()).toString();
                boolean fragile = identifier.contains("coral") || identifier.contains("kelp") || identifier.endsWith(":vine") || identifier.contains("seagrass") || identifier.contains("sea_pickle") || identifier.contains("lily_pad") || identifier.contains("dead_bush") || identifier.endsWith("lantern") || identifier.contains("flower_pot") || identifier.contains("amethyst_cluster") || frozen && (identifier.equals("minecraft:snow") || identifier.equals("minecraft:red_mushroom"));
                if (!fragile) continue;
                if (!frozen && identifier.contains("dead_bush")) {
                    unsupported.add(Map.of("block", identifier, "position", block.getKey().toShortString(), "error", "NOT_EVALUATED: vanilla tag bindings are not loaded by Bootstrap; do not treat canSurvive false as a structure defect. Separate tag-resource review required."));
                    continue;
                }
                try {
                    survivalChecked++;
                    if (!block.getValue().canSurvive(view, block.getKey())) survivalFailures.add(Map.of("block", identifier, "position", List.of(block.getKey().getX(), block.getKey().getY(), block.getKey().getZ())));
                } catch (Exception failure) {
                    unsupported.add(Map.of("block", identifier, "position", block.getKey().toShortString(), "error", failure.toString()));
                }
            }
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            CompoundTag saved = template.save(new CompoundTag());
            boolean roundtrip = saved.getList("blocks", Tag.TAG_COMPOUND).size() == entries.size();
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("group", group.get("group").getAsInt());
            report.put("sha256", group.get("sha256").getAsString());
            report.put("registry_properties", failures.isEmpty() ? "PASS" : failures);
            report.put("template_roundtrip_block_count", roundtrip ? "PASS" : "FAIL");
            report.put("survival_checked", survivalChecked);
            report.put("survival_failures", survivalFailures);
            report.put("survival_unsupported", unsupported);
            report.put("vanilla_block_tags_bound", frozen);
            report.put("brightness_for_canSurvive", 15);
            report.put("scope", "Actual vanilla 1.20.1 registry and StructureTemplate plus static canSurvive over an immutable LevelReader. No world placement, neighbor updates, light propagation, random/fluid ticks, rotations or gameplay.");
            reports.add(report);
        }
        String output = new GsonBuilder().setPrettyPrinting().create().toJson(reports);
        Files.writeString(root.resolve(frozen ? "independent-native-frozen.json" : "independent-native.json"), output);
        System.out.println(output);
    }
}
