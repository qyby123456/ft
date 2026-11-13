import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.ParseResults;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class VerifyDungeon {
    private VerifyDungeon() {}

    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        Path pack = root.resolve("out/datapack");
        Map<String, Object> result = new LinkedHashMap<>();
        CompoundTag tag = NbtIo.readCompressed(pack.resolve("data/ftobsidian/structures/chamber.nbt").toFile());
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag state = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(state.getString("Name"));
            if (!BuiltInRegistries.BLOCK.containsKey(identifier)) {
                throw new IllegalStateException("Unknown block: " + identifier);
            }
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            CompoundTag properties = state.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                if (property == null || property.getValue(properties.getString(key)).isEmpty()) {
                    throw new IllegalStateException("Invalid property: " + identifier + " " + key + "=" + properties.getString(key));
                }
            }
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        CompoundTag roundtrip = template.save(new CompoundTag());
        int blockCount = tag.getList("blocks", Tag.TAG_COMPOUND).size();
        if (roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() != blockCount) {
            throw new IllegalStateException("StructureTemplate round-trip lost blocks");
        }
        JsonObject airTagJson = new Gson().fromJson(Files.readString(pack.resolve("data/ftobsidian/tags/blocks/air.json"), StandardCharsets.UTF_8), JsonObject.class);
        List<Holder<Block>> airHolders = new ArrayList<>();
        for (var identifier : airTagJson.getAsJsonArray("values")) {
            ResourceLocation location = new ResourceLocation(identifier.getAsString());
            if (!BuiltInRegistries.BLOCK.containsKey(location)) {
                throw new IllegalStateException("Unknown tagged block: " + location);
            }
            Block block = BuiltInRegistries.BLOCK.get(location);
            if (block != Blocks.AIR && block != Blocks.CAVE_AIR && block != Blocks.VOID_AIR) {
                throw new IllegalStateException("Unsafe air tag member: " + location);
            }
            airHolders.add(block.builtInRegistryHolder());
        }
        TagKey<Block> airTag = TagKey.create(Registries.BLOCK, new ResourceLocation("ftobsidian", "air"));
        BuiltInRegistries.BLOCK.bindTags(Map.of(airTag, airHolders));
        Commands commands = new Commands(Commands.CommandSelection.DEDICATED, CommandBuildContext.simple(VanillaRegistries.createLookup(), FeatureFlags.REGISTRY.allFlags()));
        CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, 4, "verify", Component.literal("verify"), null, null);
        List<String> errors = new ArrayList<>();
        int errorCount = 0;
        int commandCount = 0;
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
                        errorCount++;
                        if (errors.size() < 20) {
                            errors.add(pack.relativize(path) + ":" + lineNumber + " cursor=" + parsed.getReader().getCursor() + " " + parsed.getExceptions());
                        }
                    }
                }
            }
        }
        result.put("status", errors.isEmpty() ? "PASS" : "FAIL");
        result.put("minecraft", SharedConstants.getCurrentVersion().getName());
        result.put("template", Map.of("size", template.getSize().toShortString(), "blocks", blockCount, "palette", palette.size(), "roundtrip", "PASS"));
        result.put("parsed_commands", commandCount);
        result.put("custom_air_tag_members", airHolders.size());
        result.put("command_error_count", errorCount);
        result.put("command_errors", errors);
        result.put("scope", "Minecraft registry, block properties, NBT StructureTemplate round-trip and Brigadier syntax. Not a gameplay test.");
        Files.writeString(root.resolve("qa/minecraft-format-verification.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result), StandardCharsets.UTF_8);
        System.out.println(new GsonBuilder().create().toJson(result));
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid commands: " + errors.size());
        }
    }
}
