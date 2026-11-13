import com.google.gson.GsonBuilder;
import com.mojang.brigadier.ParseResults;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class VerifyVault {
    private VerifyVault() {}

    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        Path pack = root.resolve("out/datapack");
        Path templateFile = arguments.length > 1 ? Path.of(arguments[1]) : pack.resolve("data/ftcoppervault/structures/treasury.nbt");
        CompoundTag tag = NbtIo.readCompressed(templateFile.toFile());
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag value = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(value.getString("Name"));
            if (!BuiltInRegistries.BLOCK.containsKey(identifier)) {
                throw new IllegalStateException("Unknown block: " + identifier);
            }
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            CompoundTag properties = value.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                if (property == null || property.getValue(properties.getString(key)).isEmpty()) {
                    throw new IllegalStateException("Invalid property: " + identifier + " " + key);
                }
            }
            states.add(net.minecraft.nbt.NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), value));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        CompoundTag roundtrip = template.save(new CompoundTag());
        int blockCount = tag.getList("blocks", Tag.TAG_COMPOUND).size();
        if (roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() != blockCount) {
            throw new IllegalStateException("Template round-trip lost blocks");
        }
        Map<BlockPos, BlockState> world = new HashMap<>();
        for (Tag entry : tag.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) entry;
            ListTag position = record.getList("pos", Tag.TAG_INT);
            world.put(new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2)), states.get(record.getInt("state")));
        }
        LevelReader reader = (LevelReader) Proxy.newProxyInstance(LevelReader.class.getClassLoader(), new Class<?>[] {LevelReader.class}, (proxy, method, values) -> {
            return switch (method.getName()) {
                case "getBlockState" -> world.getOrDefault((BlockPos) values[0], Blocks.AIR.defaultBlockState());
                case "getFluidState" -> world.getOrDefault((BlockPos) values[0], Blocks.AIR.defaultBlockState()).getFluidState();
                case "getBlockEntity" -> null;
                case "getHeight" -> 17;
                case "getMinBuildHeight" -> 0;
                default -> method.isDefault() ? InvocationHandler.invokeDefault(proxy, method, values) : null;
            };
        });
        int supportChecks = 0;
        for (var entry : world.entrySet()) {
            String name = BuiltInRegistries.BLOCK.getKey(entry.getValue().getBlock()).getPath();
            if (name.contains("pressure_plate") || name.contains("button") || name.equals("bell") || name.equals("iron_door")) {
                if (!entry.getValue().canSurvive(reader, entry.getKey())) {
                    throw new IllegalStateException("Unsupported decoration: " + name + " " + entry.getKey());
                }
                supportChecks++;
            }
        }
        int blockEntities = 0;
        for (Tag entry : tag.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) entry;
            BlockState state = states.get(record.getInt("state"));
            if (state.is(Blocks.CHEST)) {
                ListTag position = record.getList("pos", Tag.TAG_INT);
                int expected = position.getInt(0) == 15 ? 1 : -1;
                if (ChestBlock.getConnectedDirection(state).getStepX() != expected) {
                    throw new IllegalStateException("Disconnected double chest");
                }
            }
            if (record.contains("nbt")) {
                String blockEntityId = record.getCompound("nbt").getString("id");
                if (!BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(new ResourceLocation(blockEntityId))) {
                    throw new IllegalStateException("Unknown block entity: " + blockEntityId);
                }
                ListTag position = record.getList("pos", Tag.TAG_INT);
                BlockEntity loaded = BlockEntity.loadStatic(new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2)), state, record.getCompound("nbt"));
                if (loaded == null) {
                    throw new IllegalStateException("Block entity load failed: " + blockEntityId);
                }
                blockEntities++;
            }
        }
        List<Holder<Block>> airHolders = List.of(Blocks.AIR.builtInRegistryHolder(), Blocks.CAVE_AIR.builtInRegistryHolder(), Blocks.VOID_AIR.builtInRegistryHolder());
        BuiltInRegistries.BLOCK.bindTags(Map.of(TagKey.create(Registries.BLOCK, new ResourceLocation("ftcoppervault", "air")), airHolders));
        Commands commands = new Commands(Commands.CommandSelection.DEDICATED, CommandBuildContext.simple(VanillaRegistries.createLookup(), FeatureFlags.REGISTRY.allFlags()));
        CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, 4, "verify", Component.literal("verify"), null, null);
        int commandCount = 0;
        List<String> errors = new ArrayList<>();
        try (var files = Files.walk(pack)) {
            for (Path path : files.filter(path -> path.toString().endsWith(".mcfunction")).toList()) {
                int lineNumber = 0;
                for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                    lineNumber++;
                    if (line.isBlank() || line.startsWith("#")) {
                        continue;
                    }
                    commandCount++;
                    ParseResults<CommandSourceStack> parsed = commands.getDispatcher().parse(line, source);
                    if (parsed.getReader().canRead()) {
                        errors.add(pack.relativize(path) + ":" + lineNumber + " " + parsed.getExceptions());
                    }
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", errors.isEmpty() ? "PASS" : "FAIL");
        result.put("sha256", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(templateFile))));
        result.put("minecraft", SharedConstants.getCurrentVersion().getName());
        result.put("size", template.getSize().toShortString());
        result.put("palette", palette.size());
        result.put("template_blocks", blockCount);
        result.put("double_chest_connection", "PASS");
        result.put("nbt_roundtrip", "PASS");
        result.put("supported_decorations_checked", supportChecks);
        result.put("block_entities_loaded", blockEntities);
        result.put("commands_parsed", commandCount);
        result.put("command_errors", errors);
        result.put("scope", "Native registry, state properties, chest direction, block entity IDs, StructureTemplate round-trip and Brigadier syntax. No game world loaded.");
        Path reportFile = root.resolve(arguments.length > 2 ? arguments[2] : "qa/native-verification.json");
        Files.writeString(reportFile, new GsonBuilder().setPrettyPrinting().create().toJson(result), StandardCharsets.UTF_8);
        System.out.println(new GsonBuilder().create().toJson(result));
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid commands: " + errors.size());
        }
    }
}
