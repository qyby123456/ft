import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class VerifyRoom {
    private VerifyRoom() {}

    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path directory = Path.of(arguments[0]);
        CompoundTag tag = NbtIo.readCompressed(directory.resolve("room.nbt").toFile());
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag state = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(state.getString("Name"));
            if (!identifier.getNamespace().equals("minecraft") || !BuiltInRegistries.BLOCK.containsKey(identifier)) {
                throw new IllegalStateException("Not a vanilla registered block: " + identifier);
            }
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            CompoundTag properties = state.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                if (property == null || property.getValue(properties.getString(key)).isEmpty()) {
                    throw new IllegalStateException("Invalid block property: " + identifier + " " + key);
                }
            }
        }
        ListTag entries = tag.getList("blocks", Tag.TAG_COMPOUND);
        Set<String> positions = new HashSet<>();
        int airCells = 0;
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            ListTag position = entry.getList("pos", Tag.TAG_INT);
            if (position.size() != 3 || position.getInt(0) < 0 || position.getInt(0) >= 25 || position.getInt(1) < 0 || position.getInt(1) >= 12 || position.getInt(2) < 0 || position.getInt(2) >= 25) {
                throw new IllegalStateException("Out-of-bounds block: " + position);
            }
            if (!positions.add(position.toString())) {
                throw new IllegalStateException("Duplicate block position: " + position);
            }
            int stateIndex = entry.getInt("state");
            if (stateIndex < 0 || stateIndex >= palette.size() || entry.contains("nbt")) {
                throw new IllegalStateException("Invalid state index or unexpected block entity NBT");
            }
            if (palette.getCompound(stateIndex).getString("Name").equals("minecraft:air")) {
                airCells++;
            }
        }
        if (entries.size() != 7500 || !tag.getList("entities", Tag.TAG_COMPOUND).isEmpty()) {
            throw new IllegalStateException("Expected complete explicit-air template without entities");
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        CompoundTag roundtrip = template.save(new CompoundTag());
        if (roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() != entries.size()) {
            throw new IllegalStateException("StructureTemplate round-trip lost blocks");
        }
        if (!template.getSize().toShortString().equals("25, 12, 25")) {
            throw new IllegalStateException("Unexpected template size: " + template.getSize());
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "PASS");
        result.put("minecraft", SharedConstants.getCurrentVersion().getName());
        result.put("size", new int[]{25, 12, 25});
        result.put("palette_states", palette.size());
        result.put("template_cells", entries.size());
        result.put("air_cells", airCells);
        result.put("solid_detail_blocks", entries.size() - airCells);
        result.put("vanilla_block_registry_and_properties", "PASS");
        result.put("unique_positions_and_bounds", "PASS");
        result.put("structure_template_roundtrip", "PASS");
        result.put("entities_or_block_entity_payloads", 0);
        result.put("scope", "Actual Minecraft 1.20.1 NBT decoder, registries and StructureTemplate; not a world or client gameplay test.");
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(result);
        Files.writeString(Path.of(arguments[1]), json, StandardCharsets.UTF_8);
        System.out.println(new GsonBuilder().create().toJson(result));
    }
}
