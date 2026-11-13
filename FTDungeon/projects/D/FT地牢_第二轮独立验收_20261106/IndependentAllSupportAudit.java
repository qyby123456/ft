import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
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

public final class IndependentAllSupportAudit {
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        JsonObject tags = JsonParser.parseString(Files.readString(root.resolve("vanilla-block-tags.json"))).getAsJsonObject();
        Map<TagKey<Block>, List<Holder<Block>>> bindings = new HashMap<>();
        for (var tag : tags.entrySet()) {
            List<Holder<Block>> holders = new ArrayList<>();
            for (var value : tag.getValue().getAsJsonArray()) {
                ResourceLocation identifier = new ResourceLocation(value.getAsString());
                if (!BuiltInRegistries.BLOCK.containsKey(identifier)) throw new IllegalStateException("Unknown tag block " + identifier);
                holders.add(BuiltInRegistries.BLOCK.getHolderOrThrow(net.minecraft.resources.ResourceKey.create(Registries.BLOCK, identifier)));
            }
            bindings.put(TagKey.create(Registries.BLOCK, new ResourceLocation(tag.getKey())), holders);
        }
        BuiltInRegistries.BLOCK.bindTags(bindings);
        List<Object> reports = new ArrayList<>();
        for (var input : JsonParser.parseString(Files.readString(root.resolve("route-input.json"))).getAsJsonArray()) {
            JsonObject specification = input.getAsJsonObject();
            Path source = Path.of(specification.get("candidate").getAsString());
            String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
            if (!digest.equals(specification.get("sha256").getAsString())) throw new IllegalStateException("Source hash mismatch " + source);
            var data = NbtIo.readCompressed(source.toFile());
            List<BlockState> palette = new ArrayList<>();
            for (var entry : data.getList("palette", Tag.TAG_COMPOUND)) {
                palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), (net.minecraft.nbt.CompoundTag) entry));
            }
            Map<BlockPos, BlockState> blocks = new HashMap<>();
            for (var entry : data.getList("blocks", Tag.TAG_COMPOUND)) {
                var block = (net.minecraft.nbt.CompoundTag) entry;
                var position = block.getList("pos", Tag.TAG_INT);
                BlockPos coordinates = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
                if (blocks.put(coordinates, palette.get(block.getInt("state"))) != null) throw new IllegalStateException("Duplicate coordinate " + coordinates);
            }
            int height = data.getList("size", Tag.TAG_INT).getInt(1);
            LevelReader view = (LevelReader) Proxy.newProxyInstance(LevelReader.class.getClassLoader(), new Class<?>[]{LevelReader.class}, (proxy, method, parameters) -> {
                return switch (method.getName()) {
                    case "getBlockState" -> blocks.getOrDefault((BlockPos) parameters[0], Blocks.AIR.defaultBlockState());
                    case "getFluidState" -> blocks.getOrDefault((BlockPos) parameters[0], Blocks.AIR.defaultBlockState()).getFluidState();
                    case "isEmptyBlock" -> blocks.getOrDefault((BlockPos) parameters[0], Blocks.AIR.defaultBlockState()).isAir();
                    case "getMinBuildHeight" -> 0;
                    case "getHeight", "getMaxBuildHeight" -> height;
                    case "getRawBrightness", "getMaxLocalRawBrightness" -> 15;
                    case "hasChunk", "hasChunkAt", "hasChunksAt" -> true;
                    case "toString" -> "Independent immutable all-support view";
                    default -> throw new UnsupportedOperationException(method.getName());
                };
            });
            List<Object> failures = new ArrayList<>();
            List<Object> unsupported = new ArrayList<>();
            int checked = 0;
            for (var block : blocks.entrySet()) {
                if (block.getValue().isAir()) continue;
                checked++;
                String identifier = BuiltInRegistries.BLOCK.getKey(block.getValue().getBlock()).toString();
                List<Integer> position = List.of(block.getKey().getX(), block.getKey().getY(), block.getKey().getZ());
                try {
                    if (!block.getValue().canSurvive(view, block.getKey())) failures.add(Map.of("block", identifier, "position", position));
                } catch (Exception failure) {
                    unsupported.add(Map.of("block", identifier, "position", position, "error", failure.toString()));
                }
            }
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("group", specification.get("group").getAsInt());
            report.put("sha256", digest);
            report.put("nonair_checked", checked);
            report.put("failures", failures);
            report.put("unsupported", unsupported);
            report.put("scope", "Every nonair state's vanilla 1.20.1 canSurvive with real block tags, immutable LevelReader, brightness 15. No world placement, neighbor updates, gravity, fluid ticks, lighting propagation, interactions or gameplay certification. Registry/property checks remain a separate audit.");
            reports.add(report);
        }
        String output = new GsonBuilder().setPrettyPrinting().create().toJson(reports);
        Files.writeString(root.resolve("independent-all-support.json"), output);
        System.out.println(output);
    }
}
