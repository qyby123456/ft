import com.google.gson.GsonBuilder;
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

public final class SupportAudit {
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        var tags = JsonParser.parseString(Files.readString(root.resolve("vanilla-block-tags.json"))).getAsJsonObject();
        Map<TagKey<Block>, List<Holder<Block>>> bindings = new HashMap<>();
        for (var tag : tags.entrySet()) {
            List<Holder<Block>> values = new ArrayList<>();
            for (var value : tag.getValue().getAsJsonArray()) values.add(BuiltInRegistries.BLOCK.getHolderOrThrow(net.minecraft.resources.ResourceKey.create(Registries.BLOCK, new ResourceLocation(value.getAsString()))));
            bindings.put(TagKey.create(Registries.BLOCK, new ResourceLocation(tag.getKey())), values);
        }
        BuiltInRegistries.BLOCK.bindTags(bindings);
        var specification = JsonParser.parseString(Files.readString(root.resolve("route-input.json"))).getAsJsonArray().get(0).getAsJsonObject();
        var data = NbtIo.readCompressed(Path.of(specification.get("candidate").getAsString()).toFile());
        var palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette.getCompound(index)));
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        var entries = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            var entry = entries.getCompound(index);
            var coordinate = entry.getList("pos", Tag.TAG_INT);
            blocks.put(new BlockPos(coordinate.getInt(0), coordinate.getInt(1), coordinate.getInt(2)), states.get(entry.getInt("state")));
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
            if (name.equals("toString")) return "S2 immutable support view";
            throw new UnsupportedOperationException(name);
        });
        List<Object> failures = new ArrayList<>();
        List<Object> unsupported = new ArrayList<>();
        int checked = 0;
        for (var block : blocks.entrySet()) {
            if (block.getValue().isAir()) continue;
            checked++;
            String identifier = BuiltInRegistries.BLOCK.getKey(block.getValue().getBlock()).toString();
            try {
                if (!block.getValue().canSurvive(view, block.getKey())) failures.add(Map.of("block", identifier, "position", block.getKey().toShortString()));
            } catch (Exception failure) {
                unsupported.add(Map.of("block", identifier, "position", block.getKey().toShortString(), "error", failure.toString()));
            }
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("source_sha256", specification.get("sha256").getAsString());
        report.put("nonair_checked", checked);
        report.put("failures", failures);
        report.put("unsupported", unsupported);
        report.put("scope", "Vanilla 1.20.1 canSurvive for every nonair state, immutable LevelReader with real block tags; no ticks/world simulation; gravity stability checked separately.");
        Files.writeString(root.resolve("support-all.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println("Support checked=" + checked + " failures=" + failures.size() + " unsupported=" + unsupported.size());
    }
}
