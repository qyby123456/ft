package net.farmingtales.mossroomverification;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class VerifyMossRoom {
    private VerifyMossRoom() {}

    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        Path structure = root.resolve("out/room.nbt");
        CompoundTag tag = NbtIo.readCompressed(structure.toFile());
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag savedState = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(savedState.getString("Name"));
            if (!BuiltInRegistries.BLOCK.containsKey(identifier)) {
                throw new IllegalStateException("Unknown block: " + identifier);
            }
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            CompoundTag properties = savedState.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                if (property == null || property.getValue(properties.getString(key)).isEmpty()) {
                    throw new IllegalStateException("Invalid block property: " + identifier + " " + key + "=" + properties.getString(key));
                }
            }
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), savedState));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        CompoundTag saved = template.save(new CompoundTag());
        ListTag blocks = tag.getList("blocks", Tag.TAG_COMPOUND);
        if (template.getSize().getX() != 11 || template.getSize().getY() != 9 || template.getSize().getZ() != 15
                || blocks.size() != 1485 || saved.getList("blocks", Tag.TAG_COMPOUND).size() != blocks.size()
                || !tag.getList("entities", Tag.TAG_COMPOUND).isEmpty()) {
            throw new IllegalStateException("Unexpected dimensions, entities or NBT roundtrip");
        }
        Map<BlockPos, BlockState> positions = new LinkedHashMap<>();
        int blockEntities = 0;
        int emptyContainers = 0;
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag entry = blocks.getCompound(index);
            ListTag coordinates = entry.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            if (position.getX() < 0 || position.getX() >= 11 || position.getY() < 0 || position.getY() >= 9
                    || position.getZ() < 0 || position.getZ() >= 15 || positions.putIfAbsent(position, states.get(entry.getInt("state"))) != null) {
                throw new IllegalStateException("Duplicate or out-of-range coordinate: " + position);
            }
            if (!entry.contains("nbt")) {
                continue;
            }
            CompoundTag data = entry.getCompound("nbt");
            BlockEntity entity = BlockEntity.loadStatic(position, states.get(entry.getInt("state")), data);
            if (entity == null || !entity.getType().isValid(states.get(entry.getInt("state")))) {
                throw new IllegalStateException("Invalid block entity: " + position);
            }
            if (data.contains("LootTable") || !data.getList("Items", Tag.TAG_COMPOUND).isEmpty()) {
                throw new IllegalStateException("Unexpected loot contents: " + position);
            }
            blockEntities++;
            if (data.getString("id").equals("minecraft:barrel") || data.getString("id").equals("minecraft:chest")) {
                emptyContainers++;
            }
        }
        for (int blockX = 4; blockX <= 6; blockX++) {
            for (int blockY = 2; blockY <= 4; blockY++) {
                if (!positions.get(new BlockPos(blockX, blockY, 0)).isAir()) {
                    throw new IllegalStateException("Blocked north entrance");
                }
            }
        }
        for (int blockX = 0; blockX < 11; blockX++) {
            for (int blockZ = 0; blockZ < 15; blockZ++) {
                BlockPos position = new BlockPos(blockX, 7, blockZ);
                BlockState state = positions.get(position);
                if (!Block.isShapeFullBlock(state.getCollisionShape(EmptyBlockGetter.INSTANCE, position))) {
                    throw new IllegalStateException("Unsealed roof: " + position + " " + state);
                }
            }
        }
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(structure)));
        String expected = JsonParser.parseString(Files.readString(root.resolve("out/room.json"))).getAsJsonObject().get("sha256").getAsString();
        if (!digest.equals(expected)) {
            throw new IllegalStateException("Room metadata hash mismatch");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "PASS");
        result.put("minecraft", SharedConstants.getCurrentVersion().getName());
        result.put("structure_sha256", digest);
        result.put("size", List.of(11, 9, 15));
        result.put("records", blocks.size());
        result.put("palette_states", palette.size());
        result.put("nbt_roundtrip", "PASS");
        result.put("native_block_entities_loaded", blockEntities);
        result.put("native_empty_containers", emptyContainers);
        result.put("native_entrance_check", "PASS");
        result.put("native_roof_check", "PASS");
        result.put("scope", "Actual Minecraft 1.20.1 block registry, property values, NBT loading and roundtrip, block-entity loading, open north interface and solid roof. No player saves opened; not a client/server placement or gameplay test.");
        Files.writeString(root.resolve("qa/minecraft-format-verification.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result), StandardCharsets.UTF_8);
        System.out.println(new GsonBuilder().create().toJson(result));
    }
}
