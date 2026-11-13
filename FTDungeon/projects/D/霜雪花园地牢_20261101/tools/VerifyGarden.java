package net.farmingtales.frostgardenverification;

import com.google.gson.GsonBuilder;
import com.mojang.brigadier.ParseResults;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HexFormat;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class VerifyGarden {
    private VerifyGarden() {}

    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        Path pack = root.resolve("out/datapack");
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> templates = new ArrayList<>();
        for (String name : List.of("garden", "approach")) {
            Path path = pack.resolve("data/ftfrostgarden/structures/" + name + ".nbt");
            CompoundTag tag = NbtIo.readCompressed(path.toFile());
            ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
            for (int index = 0; index < palette.size(); index++) {
                CompoundTag state = palette.getCompound(index);
                net.minecraft.resources.ResourceLocation identifier = new net.minecraft.resources.ResourceLocation(state.getString("Name"));
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
            }
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
            CompoundTag roundtrip = template.save(new CompoundTag());
            if (roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() != tag.getList("blocks", Tag.TAG_COMPOUND).size()) {
                throw new IllegalStateException("Lost blocks on StructureTemplate round-trip: " + name);
            }
            templates.add(Map.of("name", name, "blocks", tag.getList("blocks", Tag.TAG_COMPOUND).size(), "palette", palette.size(), "size", template.getSize().toShortString(), "roundtrip", "PASS"));
        }
        HolderLookup.Provider access = VanillaRegistries.createLookup();
        Commands commands = new Commands(Commands.CommandSelection.DEDICATED, CommandBuildContext.simple(access, FeatureFlags.REGISTRY.allFlags()));
        CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, 4, "verify", Component.literal("verify"), null, null);
        List<String> errors = new ArrayList<>();
        int count = 0;
        try (var files = Files.walk(pack)) {
            for (Path path : files.filter(path -> path.toString().endsWith(".mcfunction")).toList()) {
                int lineNumber = 0;
                for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                    lineNumber++;
                    if (line.isBlank() || line.startsWith("#")) {
                        continue;
                    }
                    count++;
                    ParseResults<CommandSourceStack> parsed = commands.getDispatcher().parse(line, source);
                    if (parsed.getReader().canRead()) {
                        errors.add(pack.relativize(path) + ":" + lineNumber + " cursor=" + parsed.getReader().getCursor() + " " + parsed.getExceptions());
                    }
                }
            }
        }
        result.put("status", errors.isEmpty() ? "PASS" : "FAIL");
        result.put("minecraft", SharedConstants.getCurrentVersion().getName());
        result.put("structure_templates", templates);
        result.put("parsed_commands", count);
        result.put("command_errors", errors);
        String packageHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(root.resolve("out/ft-frost-garden-1.20.1.zip"))));
        result.put("package_sha256", packageHash);
        result.put("scope", "Real Minecraft 1.20.1 registry, StructureTemplate NBT roundtrip, Brigadier command parser. Not a world/client gameplay test.");
        Files.writeString(root.resolve("qa/minecraft-format-verification.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result), StandardCharsets.UTF_8);
        System.out.println(new GsonBuilder().create().toJson(result));
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid commands: " + errors.size());
        }
        Files.writeString(root.resolve("qa/verified-package-sha256.txt"), packageHash + "\n", StandardCharsets.UTF_8);
    }
}
