package net.farmingtales.archiveverification;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.brigadier.ParseResults;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HexFormat;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class VerifyArchive {
    private VerifyArchive() {}

    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        Path pack = root.resolve("out/datapack");
        CompoundTag tag = NbtIo.readCompressed(pack.resolve("data/fttombarchive/structures/archive.nbt").toFile());
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
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
                    throw new IllegalStateException("Invalid block property: " + identifier + " " + key + "=" + properties.getString(key));
                }
            }
            states.add(net.minecraft.nbt.NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), state));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        CompoundTag roundtrip = template.save(new CompoundTag());
        ListTag blocks = tag.getList("blocks", Tag.TAG_COMPOUND);
        if (roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() != blocks.size()) {
            throw new IllegalStateException("StructureTemplate roundtrip lost blocks");
        }
        int blockEntities = 0;
        int readableBooks = 0;
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag entry = blocks.getCompound(index);
            if (!entry.contains("nbt")) {
                continue;
            }
            ListTag position = entry.getList("pos", Tag.TAG_INT);
            BlockPos location = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            BlockEntity entity = BlockEntity.loadStatic(location, states.get(entry.getInt("state")), entry.getCompound("nbt"));
            if (entity == null) {
                throw new IllegalStateException("Invalid block entity at " + location);
            }
            blockEntities++;
            if (entity instanceof LecternBlockEntity lectern) {
                ItemStack book = lectern.getBook();
                if (!book.is(Items.WRITTEN_BOOK) || book.getCount() != 1 || book.getTag() == null || book.getTag().getList("pages", Tag.TAG_STRING).isEmpty()) {
                    throw new IllegalStateException("Unreadable lectern book at " + location);
                }
                for (Tag page : book.getTag().getList("pages", Tag.TAG_STRING)) {
                    if (Component.Serializer.fromJson(page.getAsString()) == null) {
                        throw new IllegalStateException("Malformed written book JSON");
                    }
                }
                readableBooks++;
            }
        }
        List<Holder<Block>> airBlocks = new ArrayList<>();
        for (var value : JsonParser.parseString(Files.readString(pack.resolve("data/fttombarchive/tags/blocks/air.json"))).getAsJsonObject().getAsJsonArray("values")) {
            ResourceLocation identifier = new ResourceLocation(value.getAsString());
            if (!BuiltInRegistries.BLOCK.containsKey(identifier)) {
                throw new IllegalStateException("Invalid air tag member: " + identifier);
            }
            airBlocks.add(BuiltInRegistries.BLOCK.get(identifier).builtInRegistryHolder());
        }
        BuiltInRegistries.BLOCK.bindTags(Map.of(TagKey.create(Registries.BLOCK, new ResourceLocation("fttombarchive:air")), airBlocks));
        HolderLookup.Provider lookup = VanillaRegistries.createLookup();
        RegistryAccess.Frozen registry = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        CommandBuildContext context = CommandBuildContext.simple(lookup, FeatureFlags.REGISTRY.allFlags());
        Commands commands = new Commands(Commands.CommandSelection.DEDICATED, context);
        CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, 4, "verify", Component.literal("verify"), null, null) {
            @Override
            public RegistryAccess registryAccess() {
                return registry;
            }
        };
        List<String> errors = new ArrayList<>();
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
                        errors.add(pack.relativize(path) + ":" + lineNumber + " " + parsed.getExceptions());
                    }
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", errors.isEmpty() ? "PASS" : "FAIL");
        result.put("minecraft", SharedConstants.getCurrentVersion().getName());
        result.put("template", Map.of("size", template.getSize().toShortString(), "blocks", blocks.size(), "palette", palette.size(), "roundtrip", "PASS"));
        result.put("block_entities_loaded", blockEntities);
        result.put("readable_written_books", readableBooks);
        result.put("parsed_commands", commandCount);
        result.put("command_errors", errors);
        result.put("air_tag_values_bound", airBlocks.size());
        result.put("package_sha256", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(root.resolve("out/ft-tomb-archive-1.20.1.zip")))));
        result.put("scope", "Real Minecraft 1.20.1 registry, block-state properties, StructureTemplate NBT roundtrip, native block-entity loading, written books and Brigadier command parsing. Not a live-world or gameplay test.");
        Files.writeString(root.resolve("qa/minecraft-format-verification.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result), StandardCharsets.UTF_8);
        System.out.println(new GsonBuilder().create().toJson(result));
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid commands: " + errors.size());
        }
    }
}
